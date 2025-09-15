package tomato.core;

import tomato.entity.EntityType;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.concurrent.ConcurrentHashMap;

public class SpriteCache {
    private static final ConcurrentHashMap<EntityType, BufferedImage[]> cache = new ConcurrentHashMap<>();

    public static BufferedImage[] getRotations(EntityType key, BufferedImage base) {
        return cache.computeIfAbsent(key, k -> {
            BufferedImage[] rotations = new BufferedImage[4];
            rotations[0] = base;
            rotations[1] = rotateImage(base, Mathf.toRadians(90));
            rotations[2] = rotateImage(base, Mathf.toRadians(180));
            rotations[3] = rotateImage(base, Mathf.toRadians(270));
            return rotations;
        });

    }

    private static BufferedImage rotateImage(BufferedImage image, double angle) {
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
