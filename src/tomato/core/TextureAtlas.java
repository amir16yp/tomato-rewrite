package tomato.core;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class TextureAtlas {
    private final Map<Integer, BufferedImage> tiles;
    private final int tileWidth;
    private final int tileHeight;
    private final Logger logger;
    private final String tilesetPath;
    private final BufferedImage tilesetImage;
    private BufferedImage placeholderImage;

    public TextureAtlas(String tilesetPath, int tileWidth, int tileHeight) {
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.tiles = new HashMap<>();
        this.logger = Logger.getLogger(this.getClass().getName() + "-" + tilesetPath);
        this.tilesetImage = Utils.loadQOI(tilesetPath);
        this.tilesetPath = tilesetPath;
    }

    private BufferedImage loadTile(int id) {
        if (id <= 0) {
            logger.warning("Invalid tile ID: " + id + " (must be greater than 0)");
            return createPlaceholderTile();
        }

        int cols = tilesetImage.getWidth() / tileWidth;
        int rows = tilesetImage.getHeight() / tileHeight;
        int maxId = cols * rows;

        if (id > maxId) {
            logger.warning("Tile ID " + id + " exceeds maximum tile count of " + maxId);
            return createPlaceholderTile();
        }

        int row = (id - 1) / cols;
        int col = (id - 1) % cols;

        try {
            return tilesetImage.getSubimage(
                    col * tileWidth, row * tileHeight, tileWidth, tileHeight);
        } catch (Exception e) {
            logger.warning("Failed to load tile " + id + ": " + e.getMessage());
            return createPlaceholderTile();
        }
    }

    public BufferedImage getTile(int id) {
        if (id <= 0) {
            logger.warning("Invalid tile ID: " + id + " (must be greater than 0)");
            return createPlaceholderTile();
        }

        BufferedImage tile = tiles.get(id);
        if (tile == null) {
            try {
                tile = loadTile(id);
                if (tile != null) {
                    tiles.put(id, tile);
                } else {
                    logger.warning("Failed to load tile " + id + " - returned null");
                    tile = createPlaceholderTile();
                }
            } catch (Exception e) {
                logger.warning("Error loading tile " + id + ": " + e.getMessage());
                tile = createPlaceholderTile();
            }
        }
        return tile;
    }

    private BufferedImage createPlaceholderTile() {
        if (placeholderImage != null)
        {
            return placeholderImage;
        }
        BufferedImage placeholder = new BufferedImage(tileWidth, tileHeight, BufferedImage.TYPE_INT_ARGB);
        // Fill with a noticeable color or pattern
        // For example, a magenta and black checkered pattern
        for (int y = 0; y < tileHeight; y++) {
            for (int x = 0; x < tileWidth; x++) {
                placeholder.setRGB(x, y, ((x + y) % 2 == 0) ? 0xFFFF00FF : 0xFF000000);
            }
        }
        this.placeholderImage = placeholder;
        return placeholderImage;
    }

    public int getTileWidth() {
        return tileWidth;
    }

    public int getTileHeight() {
        return tileHeight;
    }

    public int getTileCount() {
        int cols = tilesetImage.getWidth() / tileWidth;
        int rows = tilesetImage.getHeight() / tileHeight;
        return cols * rows;
    }

    public BufferedImage getTile(int x, int y) {
        int cols = tilesetImage.getWidth() / tileWidth;
        int rows = tilesetImage.getHeight() / tileHeight;

        if (x < 0 || x >= cols || y < 0 || y >= rows) {
            logger.warning("Invalid tile coordinates: (" + x + ", " + y + ") - out of bounds");
            return createPlaceholderTile();
        }

        int id = y * cols + x + 1; // Convert x,y to 1-based ID
        return getTile(id);
    }
}