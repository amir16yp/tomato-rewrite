package tomato.core;

import tomato.core.OpenSimplexNoise;
import tomato.entity.Entity;
import tomato.entity.PlayerTank;
import tomato.entity.EnemyTank;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.HashMap;
import java.util.Map;

public class World {
    public static Entity PLAYER_ENTITY = new PlayerTank();
    private CopyOnWriteArrayList<Entity> worldEntities = new CopyOnWriteArrayList<>();
    public CopyOnWriteArrayList<Entity> getWorldEntities() { return worldEntities; }

    private int cellSize;
    private int chunkSizeCells;
    private OpenSimplexNoise noise;
    private long seed;

    // Infinite chunk storage
    private Map<Point, Chunk> chunks = new HashMap<>();

    public static final World WORLD = new World(32, 4, 12345L);

    public World(int chunkSizeCells, int cellSize, long seed) {
        this.chunkSizeCells = chunkSizeCells;
        this.cellSize = cellSize;
        this.seed = seed;
        this.noise = new OpenSimplexNoise(seed);
    }

    public void update() {
        PLAYER_ENTITY.update();
        for (Entity entity : worldEntities) {
            entity.update();
            if (entity.isMarkedForRemoval()) {
                worldEntities.remove(entity);
            }
        }
    }

    public void render(Graphics2D g, Rectangle cameraView) {
        // Figure out which chunks overlap the camera
        int startCX = (int)Math.floor(cameraView.x / (double)(chunkSizeCells * cellSize));
        int startCY = (int)Math.floor(cameraView.y / (double)(chunkSizeCells * cellSize));
        int endCX   = (int)Math.floor((cameraView.x + cameraView.width) / (double)(chunkSizeCells * cellSize));
        int endCY   = (int)Math.floor((cameraView.y + cameraView.height) / (double)(chunkSizeCells * cellSize));

        for (int cx = startCX; cx <= endCX; cx++) {
            for (int cy = startCY; cy <= endCY; cy++) {
                final int fcx = cx;
                final int fcy = cy;
                Point key = new Point(fcx, fcy);
                Chunk chunk = chunks.computeIfAbsent(key, k -> new Chunk(fcx, fcy));
                g.drawImage(chunk.image, chunk.worldX, chunk.worldY, null);
            }
        }

        PLAYER_ENTITY.render(g);
        worldEntities.forEach(entity -> entity.render(g));
    }

    public void spawnRedEnemy(double x, double y) {
        worldEntities.add(new EnemyTank(x, y));
    }

    private class Chunk {
        int cx, cy;
        int worldX, worldY;
        BufferedImage image;

        public Chunk(int cx, int cy) {
            this.cx = cx;
            this.cy = cy;
            this.worldX = cx * chunkSizeCells * cellSize;
            this.worldY = cy * chunkSizeCells * cellSize;
            bake();
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
                        color = new Color(30, 120 + (int)(100 * value), 30); // grass
                    } else {
                        int c = 140 + (int)(60 * value); // stone
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
