package tomato.core;

import tomato.entity.EnemyTank;
import tomato.entity.Entity;
import tomato.entity.PlayerTank;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class World {

    private final CopyOnWriteArrayList<Entity> worldEntities = new CopyOnWriteArrayList<>();

    public CopyOnWriteArrayList<Entity> getWorldEntities() {
        return worldEntities;
    }

    private final int cellSize;
    private final int chunkSizeCells;
    private final OpenSimplexNoise noise;
    private final long seed;

    // Infinite chunk storage
    private final Map<Point, Chunk> chunks = new HashMap<>();

    // how far in chunks to load around the player
    private final int renderDistance = 1;

    public static final World WORLD = new World(256, 4, 12345L);
    public static Entity PLAYER_ENTITY = new PlayerTank();

    public World(int chunkSizeCells, int cellSize, long seed) {
        this.chunkSizeCells = chunkSizeCells;
        this.cellSize = cellSize;
        this.seed = seed;
        this.noise = new OpenSimplexNoise(seed);
    }

    public Chunk getChunkAtWorld(double worldX, double worldY) {
        int chunkSizePx = chunkSizeCells * cellSize;
        int cx = (int) Math.floor(worldX / chunkSizePx);
        int cy = (int) Math.floor(worldY / chunkSizePx);
        return chunks.get(new Point(cx, cy));
    }

    public void update() {
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
                Chunk chunk = chunks.computeIfAbsent(key, k -> new Chunk(cx, cy));

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

        public Chunk(int cx, int cy) {
            this.cx = cx;
            this.cy = cy;
            this.worldX = cx * chunkSizeCells * cellSize;
            this.worldY = cy * chunkSizeCells * cellSize;
            bake();
        }

        public Rectangle getBounds() {
            int w = chunkSizeCells * cellSize;
            int h = chunkSizeCells * cellSize;
            return new Rectangle(worldX, worldY, w, h);
        }

        private void bake() {
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
