package tomato.core;

import tomato.entity.EnemyTank;
import tomato.entity.Entity;
import tomato.entity.LandmineEntity;
import tomato.entity.PlayerTank;
import tomato.vfx.VFXManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class World {

    private final CopyOnWriteArrayList<Entity> worldEntities = new CopyOnWriteArrayList<>();
    private final SpatialGrid spatialGrid;

    public CopyOnWriteArrayList<Entity> getWorldEntities() {
        return worldEntities;
    }
    
    public SpatialGrid getSpatialGrid() {
        return spatialGrid;
    }

    private final int cellSize;
    private final int chunkSizeCells;
    private final OpenSimplexNoise noise;

    // Infinite chunk storage
    private final Map<Point, Chunk> chunks = new HashMap<>();

    // how far in chunks to load around the player
    private final int renderDistance = 1;

    public static World WORLD;
    
    private final VFXManager vfxManager;

    public static void createWorld(WorldType type)
    {
        WORLD = new World(128, 4, 12345L, type);
        PLAYER_ENTITY = new PlayerTank();
    }

    public static Entity PLAYER_ENTITY = new PlayerTank();

    private WorldType worldType;
    private Random random;
    public World(int chunkSizeCells, int cellSize, long seed,WorldType worldType)
    {
        this.worldType = worldType;
        this.chunkSizeCells = chunkSizeCells;
        this.cellSize = cellSize;
//        this.seed = seed;
        this.random = new Random(seed);
        this.noise = new OpenSimplexNoise(random);
        this.vfxManager = new VFXManager();
        this.vfxManager.getLighting().setDaylight((float) Math.random());
        // Initialize spatial grid with cell size of 64 pixels for efficient collision detection
        this.spatialGrid = new SpatialGrid(64);
    }

    public Chunk getChunkAtWorld(double worldX, double worldY) {
        int chunkSizePx = chunkSizeCells * cellSize;
        int cx = (int) Math.floor(worldX / chunkSizePx);
        int cy = (int) Math.floor(worldY / chunkSizePx);
        return chunks.get(new Point(cx, cy));
    }

    public void update() {
        // Clear and rebuild spatial grid for this frame
        spatialGrid.clear();
        
        // Add player to spatial grid
        if (PLAYER_ENTITY != null && isEntityInLoadedChunk(PLAYER_ENTITY)) {
            spatialGrid.addEntity(PLAYER_ENTITY);
        }
        
        // Add all loaded entities to spatial grid
        for (Entity entity : worldEntities) {
            if (isEntityInLoadedChunk(entity)) {
                spatialGrid.addEntity(entity);
            }
        }
        
        // Update player
        PLAYER_ENTITY.update();

        // only update entities in loaded chunks
        for (Entity entity : worldEntities) {
            if (isEntityInLoadedChunk(entity)) {
                entity.update();
            }

            if (entity.isMarkedForRemoval()) {
                worldEntities.remove(entity);
            }
        }
        
        // Update VFX system
        vfxManager.update();
    }

    public void render(Graphics2D g, Rectangle cameraView) {
        // figure out which chunk the player is standing in
        int playerChunkX = (int) Math.floor(PLAYER_ENTITY.getX() / (double) getChunkSizePx());
        int playerChunkY = (int) Math.floor(PLAYER_ENTITY.getY() / (double) getChunkSizePx());

        Set<Point> visible = new HashSet<>();

        // load a square grid around player
        for (int dx = -renderDistance; dx <= renderDistance; dx++) {
            for (int dy = -renderDistance; dy <= renderDistance; dy++) {
                int cx = playerChunkX + dx;
                int cy = playerChunkY + dy;
                Point key = new Point(cx, cy);

                // bake or reuse
                Chunk chunk = chunks.computeIfAbsent(key, k -> new Chunk(cx, cy, random));

                // render it
                g.drawImage(chunk.image, chunk.worldX, chunk.worldY, null);
                visible.add(key);
            }
        }

        // 🔥 Unload everything else
        chunks.keySet().removeIf(key -> !visible.contains(key));

        // draw entities only if inside a loaded chunk
        if (isEntityInLoadedChunk(PLAYER_ENTITY)) {
            PLAYER_ENTITY.render(g);
        }

        worldEntities.forEach(entity -> {
            if (isEntityInLoadedChunk(entity)) {
                entity.render(g);
            }
        });
        
        // Render VFX system (particles and lighting) with camera view
        vfxManager.render(g, cameraView);
    }

    public int getLoadedChunkCount() {
        return chunks.size();
    }

    public Collection<Chunk> getChunks() {
        return chunks.values();
    }

    public boolean isChunkLoaded(int cx, int cy) {
        return chunks.containsKey(new Point(cx, cy));
    }

    public boolean isEntityInLoadedChunk(Entity e) {
        int chunkSizePx = chunkSizeCells * cellSize;
        int cx = (int) Math.floor(e.getX() / (double) chunkSizePx);
        int cy = (int) Math.floor(e.getY() / (double) chunkSizePx);
        return isChunkLoaded(cx, cy);
    }

    public void spawnRedEnemy(double x, double y) {
        worldEntities.add(new EnemyTank(x, y));
    }

    public void spawnLandmine(double x, double y)
    {
        worldEntities.add(new LandmineEntity(x, y));
    }
    
    public VFXManager getVFXManager() {
        return vfxManager;
    }

    public int getCellSize() {
        return cellSize;
    }

    public int getChunkSizeCells() {
        return chunkSizeCells;
    }

    public int getChunkSizePx() {
        return chunkSizeCells * cellSize;
    }

    public class Chunk {
        public int cx, cy;
        int worldX, worldY;
        BufferedImage image;
        private final Random rng;
        public Chunk(int cx, int cy, Random rng) {
            this.cx = cx;
            this.cy = cy;
            this.rng = rng;
            this.worldX = cx * chunkSizeCells * cellSize;
            this.worldY = cy * chunkSizeCells * cellSize;
            switch (worldType) {
                case DESERT:
                    bakeDesert();
                    break;
                case GRASSLAND:
                    bakeGrassland();
                    break;
                default:
                    bakeGrassland();
                    break;
            }
        }

        public Point getRandomWorldCoordinate() {
            int w = chunkSizeCells * cellSize;
            int h = chunkSizeCells * cellSize;

            int localX = rng.nextInt(w); // pixel offset inside this chunk
            int localY = rng.nextInt(h);

            int worldX = this.worldX + localX;
            int worldY = this.worldY + localY;

            return new Point(worldX, worldY);
        }

        public Rectangle getBounds() {
            int w = chunkSizeCells * cellSize;
            int h = chunkSizeCells * cellSize;
            return new Rectangle(worldX, worldY, w, h);
        }

        private void bakeDesert() {
            int w = chunkSizeCells * cellSize;
            int h = chunkSizeCells * cellSize;
            image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();

            for (int lx = 0; lx < chunkSizeCells; lx++) {
                for (int ly = 0; ly < chunkSizeCells; ly++) {
                    int gx = cx * chunkSizeCells + lx;
                    int gy = cy * chunkSizeCells + ly;

                    // normalize gx, gy
                    double nx = gx / 100.0;
                    double ny = gy / 100.0;
                    double value = fractalNoise(nx, ny, 4, 0.5);

                    Color color;
                    if (value < -0.3) {
                        // dark rock patch (rare, scattered)
                        int c = 100 + (int)(40 * (value + 0.3));
                        color = new Color(c, c, c);
                    } else if (value < 0.4) {
                        // main sand tone
                        int base = 200 + (int)(20 * value);
                        color = new Color(base, base, 150);
                    } else {
                        // dry cracked earth / dune highlights
                        int r = 220 + (int)(30 * value);
                        int gcol = 200 + (int)(20 * value);
                        int b = 120 + (int)(10 * value);
                        color = new Color(r, gcol, b);
                    }

                    // occasional green speckles for sparse vegetation
                    if (Math.random() < 0.001 && value > -0.1 && value < 0.3) {
                        color = new Color(50, 120, 50);
                    }

                    g.setColor(color);
                    g.fillRect(lx * cellSize, ly * cellSize, cellSize, cellSize);
                }
            }
            g.dispose();
        }


        private void bakeGrassland() {
            int w = chunkSizeCells * cellSize;
            int h = chunkSizeCells * cellSize;
            image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();

            for (int lx = 0; lx < chunkSizeCells; lx++) {
                for (int ly = 0; ly < chunkSizeCells; ly++) {
                    int gx = cx * chunkSizeCells + lx;
                    int gy = cy * chunkSizeCells + ly;

                    // normalize gx, gy to [-1,1] style coords
                    double nx = gx / 100.0;
                    double ny = gy / 100.0;
                    double value = fractalNoise(nx, ny, 5, 0.5);

                    Color color;
                    if (value < -0.2) {
                        color = new Color(90, 60, 40); // dirt
                    } else if (value < 0.3) {
                        color = new Color(30, 120 + (int) (100 * value), 30); // grass
                    } else {
                        int c = 140 + (int) (60 * value); // stone
                        color = new Color(c, c, c);
                    }

                    g.setColor(color);
                    g.fillRect(lx * cellSize, ly * cellSize, cellSize, cellSize);
                }
            }
            g.dispose();
        }
    }

    private double fractalNoise(double x, double y, int octaves, double persistence) {
        double total = 0, frequency = 1, amplitude = 1, maxValue = 0;
        for (int i = 0; i < octaves; i++) {
            total += noise.eval(x * frequency, y * frequency) * amplitude;
            maxValue += amplitude;
            amplitude *= persistence;
            frequency *= 2.0;
        }
        return total / maxValue;
    }
}
