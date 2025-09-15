package tomato.core;

import tomato.entity.Direction;
import tomato.entity.EntityType;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.concurrent.ConcurrentHashMap;

public class SpriteCache {
    private static final ConcurrentHashMap<EntityType, BufferedImage[]> cache = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<EntityType, Rectangle[]> hitboxCache = new ConcurrentHashMap<>();

    public static BufferedImage queryCache(EntityType entityType, Direction direction)
    {
        BufferedImage result = null;
        switch (direction)
        {
            case SOUTH:
                result = cache.get(entityType)[0];
                break;
            case WEST:
                result = cache.get(entityType)[1];
                break;
            case NORTH:
                result = cache.get(entityType)[2];
                break;
            case EAST:
                result = cache.get(entityType)[3];
                break;
        }
        return result;
    }

    public static BufferedImage[] getRotations(EntityType key, BufferedImage base) {
        return cache.computeIfAbsent(key, k -> {
            BufferedImage[] rotations = new BufferedImage[4];
            rotations[0] = base;
            rotations[1] = rotateImage(base, Mathf.toRadians(90));
            rotations[2] = rotateImage(base, Mathf.toRadians(180));
            rotations[3] = rotateImage(base, Mathf.toRadians(270));
            
            // Pre-calculate hitboxes for all rotations
            preCalculateHitboxes(key, rotations);
            
            return rotations;
        });
    }

    public static Rectangle queryHitboxCache(EntityType entityType, Direction direction) {
        Rectangle[] hitboxes = hitboxCache.get(entityType);
        if (hitboxes == null) return null;
        
        switch (direction) {
            case SOUTH: return hitboxes[0];
            case WEST: return hitboxes[1];
            case NORTH: return hitboxes[2];
            case EAST: return hitboxes[3];
            default: return null;
        }
    }

    private static void preCalculateHitboxes(EntityType entityType, BufferedImage[] sprites) {
        Rectangle[] hitboxes = new Rectangle[4];
        
        for (int i = 0; i < sprites.length; i++) {
            hitboxes[i] = calculateSpriteHitbox(sprites[i]);
        }
        
        hitboxCache.put(entityType, hitboxes);
    }

    private static Rectangle calculateSpriteHitbox(BufferedImage sprite) {
        if (sprite == null) {
            return new Rectangle(0, 0, 0, 0);
        }

        int width = sprite.getWidth();
        int height = sprite.getHeight();

        int minX = width;
        int minY = height;
        int maxX = -1;
        int maxY = -1;

        for (int yy = 0; yy < height; yy++) {
            for (int xx = 0; xx < width; xx++) {
                int pixel = sprite.getRGB(xx, yy);
                int alpha = (pixel >> 24) & 0xff;
                if (alpha > 0) { // non-transparent pixel
                    if (xx < minX) minX = xx;
                    if (yy < minY) minY = yy;
                    if (xx > maxX) maxX = xx;
                    if (yy > maxY) maxY = yy;
                }
            }
        }

        if (maxX < minX || maxY < minY) {
            return new Rectangle(0, 0, 0, 0);
        }

        int rectWidth = (maxX - minX) + 1;
        int rectHeight = (maxY - minY) + 1;

        return new Rectangle(minX, minY, rectWidth, rectHeight);
    }

    private static void loadRotationsForFile(EntityType entityType, String filePath)
    {
        BufferedImage image = Utils.loadQOI(filePath);
        if (image == null) {
            throw new RuntimeException("Failed to load image: " + filePath);
        }
        getRotations(entityType, image);
    }

    static // preloading is smarter lol
    {
        loadRotationsForFile(EntityType.PLAYER_TANK, "/tomato/assets/tank.qoi");
        loadRotationsForFile(EntityType.RED_ENEMY_TANK, "/tomato/assets/tank_red.qoi");
        loadRotationsForFile(EntityType.REGULAR_PROJECTILE, "/tomato/assets/projectile.qoi");
        loadRotationsForFile(EntityType.LANDMINE, "/tomato/assets/landmine.qoi");
        loadRotationsForFile(EntityType.GOLD_PROJECTILE, "/tomato/assets/projectile_gold.qoi");
    }

    private static BufferedImage rotateImage(BufferedImage image, double angle) {
        if (image == null) {
            throw new IllegalArgumentException("Image cannot be null");
        }
        int width = image.getWidth();
        int height = image.getHeight();

        // Create a new image with the same dimensions
        BufferedImage rotated = new BufferedImage(width, height, image.getType());
        Graphics2D g2d = rotated.createGraphics();

        // Set rendering hints for better quality
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Create transform for rotation around center
        AffineTransform transform = new AffineTransform();
        transform.translate(width / 2.0, height / 2.0);
        transform.rotate(angle);
        transform.translate(-width / 2.0, -height / 2.0);

        g2d.setTransform(transform);
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        return rotated;
    }
}
