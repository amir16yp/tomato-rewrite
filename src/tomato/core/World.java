package tomato.core;

import tomato.entity.EnemyTank;
import tomato.entity.Entity;
import tomato.entity.PlayerTank;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class World
{
    private int xCellCount;
    private int yCellCount;
    private int cellSize; // Size of each cell in world units

    public static Entity PLAYER_ENTITY = new PlayerTank();

    private CopyOnWriteArrayList<Entity> worldEntities = new CopyOnWriteArrayList<Entity>();

    public CopyOnWriteArrayList<Entity> getWorldEntities() {
        return worldEntities;
    }

    private int[][] CELLS = null;

    public static final World WORLD = new World(256, 256, 4);
    public World(int rowCount, int columnCount, int cellSize)
    {
        this.xCellCount = rowCount;
        this.yCellCount = columnCount;
        this.cellSize = cellSize;
        CELLS = new int[rowCount][columnCount];
        // populate the cells with Color().getRGB() ints, which will then render as colored cells in world coordinates
        initializeCells();
    }

    private double fractalNoise(OpenSimplexNoise noise, double x, double y, int octaves, double persistence) {
        double total = 0;
        double frequency = 1;
        double amplitude = 1;
        double maxValue = 0;  // for normalization

        for (int i = 0; i < octaves; i++) {
            total += noise.eval(x * frequency, y * frequency) * amplitude;
            maxValue += amplitude;
            amplitude *= persistence;
            frequency *= 2.0;
        }

        return total / maxValue;
    }


    private void initializeCells() {
        OpenSimplexNoise noise = new OpenSimplexNoise(System.currentTimeMillis());

        for (int x = 0; x < xCellCount; x++) {
            for (int y = 0; y < yCellCount; y++) {
                double nx = (double) x / xCellCount;
                double ny = (double) y / yCellCount;

                // fractal noise with multiple octaves
                double value = fractalNoise(noise, nx, ny, 5, 0.5); // 5 octaves, 0.5 persistence

                // map to palette
                if (value < -0.2) {
                    // darker dirt
                    CELLS[x][y] = new Color(100 + rnd(20), 70 + rnd(10), 50 + rnd(10)).getRGB();
                } else if (value < 0.3) {
                    // grass
                    CELLS[x][y] = new Color(30 + rnd(10), 120 + (int)(100 * value) + rnd(15), 30 + rnd(10)).getRGB();
                } else {
                    // pebbly/rock
                    int c = 140 + (int)(60 * value) + rnd(15);
                    CELLS[x][y] = new Color(c, c, c).getRGB();
                }
            }
        }
    }

    // tiny random offset for color jitter
    private int rnd(int range) {
        return (int)(Math.random() * range) - range/2;
    }


    public void update()
    {
        PLAYER_ENTITY.update();
        for (Entity entity : worldEntities)
        {
            entity.update();
            if (entity.isMarkedForRemoval())
            {
                worldEntities.remove(entity);
            }
        }
    }


    public void render(Graphics2D g)
    {
        // Render each cell in world coordinates
        for (int x = 0; x < xCellCount; x++) {
            for (int y = 0; y < yCellCount; y++) {
                // Set color from the cell's RGB value
                g.setColor(new Color(CELLS[x][y]));
                
                // Draw the cell at world coordinates
                int worldX = x * cellSize;
                int worldY = y * cellSize;
                g.fillRect(worldX, worldY, cellSize, cellSize);
//
//                // Optional: Draw grid lines for clarity
//                g.setColor(Color.BLACK);
//                g.drawRect(worldX, worldY, cellSize, cellSize);
            }
        }
        PLAYER_ENTITY.render(g);
        worldEntities.forEach(entity -> entity.render(g));
    }

    public void spawnRedEnemy(double x, double y)
    {
        worldEntities.add(new EnemyTank(x, y));
    }

}
