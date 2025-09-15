package tomato.vfx;

import tomato.Game;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.CopyOnWriteArrayList;

public class Lighting {
    private float daylight = 1.0f; // 0.0 (night) to 1.0 (day)
    private float minBrightness = 0.1f; // Minimum brightness level
    private float maxDarkness = 0.9f; // Maximum darkness level
    private final CopyOnWriteArrayList<DynamicLightSource> lightSources;
    private BufferedImage lightMap;

    public Lighting() {
        this.lightSources = new CopyOnWriteArrayList<>();
    }

    public void setDaylight(float daylight) {
        this.daylight = Math.max(0.0f, Math.min(1.0f, daylight));
    }

    public void setMinBrightness(float minBrightness) {
        this.minBrightness = Math.max(0.0f, Math.min(1.0f, minBrightness));
    }

    public void setMaxDarkness(float maxDarkness) {
        this.maxDarkness = Math.max(0.0f, Math.min(1.0f, maxDarkness));
    }   

    public void addLightSource(DynamicLightSource source) {
        if (!lightSources.contains(source))
        {
            lightSources.add(source);
        }
    }

    public void removeLightSource(DynamicLightSource source) {
        lightSources.remove(source);
    }

    public void clearLightSources() {
        lightSources.clear();
    }

    public void drawLighting(Graphics2D g2d, Rectangle cameraView) {
        int width = cameraView.width;
        int height = cameraView.height;

        if (lightMap == null || lightMap.getWidth() != width || lightMap.getHeight() != height) {
            lightMap = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }

        Graphics2D lightG = lightMap.createGraphics();
        lightG.setComposite(AlphaComposite.Clear);
        lightG.fillRect(0, 0, width, height);
        lightG.setComposite(AlphaComposite.SrcOver);

        // Draw ambient light
        float nightAlpha = minBrightness + (maxDarkness - minBrightness) * (1 - daylight);
        lightG.setColor(new Color(0, 0, 0, nightAlpha));
        lightG.fillRect(0, 0, width, height);

        // Draw light sources (convert world coordinates to screen coordinates)
        for (DynamicLightSource light : lightSources) {
            if (light.getLightRadius() > 0 && light.getLightStrength() > 0) {
                drawLightSource(lightG, light, cameraView);
            }
        }

        lightG.dispose();

        // Apply lighting to the scene
        g2d.setComposite(AlphaComposite.SrcOver);
        g2d.drawImage(lightMap, cameraView.x, cameraView.y, null);
    }

    // Overloaded method for backward compatibility
    public void drawLighting(Graphics2D g2d) {
        Rectangle screenView = new Rectangle(0, 0, Game.WIDTH, Game.HEIGHT);
        drawLighting(g2d, screenView);
    }

    public float getDaylight() {
        return daylight;
    }

    private void drawLightSource(Graphics2D g, DynamicLightSource source, Rectangle cameraView) {
        // Convert world coordinates to screen coordinates
        int worldX = source.getLightX();
        int worldY = source.getLightY();
        int screenX = worldX - cameraView.x;
        int screenY = worldY - cameraView.y;
        int radius = source.getLightRadius();
        float strength = Math.max(0.0f, Math.min(1.0f, source.getLightStrength()));

        // Only draw if the light source is within or near the camera view
        if (screenX + radius >= 0 && screenX - radius < cameraView.width &&
            screenY + radius >= 0 && screenY - radius < cameraView.height) {
            
            RadialGradientPaint paint = new RadialGradientPaint(
                    screenX, screenY, radius,
                    new float[]{0, 1},
                    new Color[]{new Color(1f, 1f, 1f, strength), new Color(1f, 1f, 1f, 0)}
            );

            g.setPaint(paint);
            g.setComposite(AlphaComposite.DstOut);
            g.fillOval(screenX - radius, screenY - radius, radius * 2, radius * 2);
        }
    }

}
