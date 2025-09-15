package tomato.vfx;

import java.awt.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class VFXManager {
    private final CopyOnWriteArrayList<VisualEffect> effects;
    private final Lighting lighting;

    public VFXManager() {
        this.effects = new CopyOnWriteArrayList<>();
        this.lighting = new Lighting();
        // Set default lighting to a slightly darker environment for better effect visibility
        this.lighting.setDaylight(0.3f);
    }

    public void addEffect(VisualEffect effect) {
        effects.add(effect);
        
        // If the effect is also a light source, add it to the lighting system
        if (effect instanceof DynamicLightSource) {
            lighting.addLightSource((DynamicLightSource) effect);
        }
    }

    public void removeEffect(VisualEffect effect) {
        effects.remove(effect);
        
        // If the effect was also a light source, remove it from the lighting system
        if (effect instanceof DynamicLightSource) {
            lighting.removeLightSource((DynamicLightSource) effect);
        }
    }

    public void update() {
        effects.removeIf(effect -> {
            effect.update();
            boolean finished = effect.isFinished();
            
            // Clean up light sources when effects finish
            if (finished && effect instanceof DynamicLightSource) {
                lighting.removeLightSource((DynamicLightSource) effect);
            }
            
            return finished;
        });
    }

    public void render(Graphics2D g2d) {
        // Draw all visual effects
        for (VisualEffect effect : effects) {
            effect.draw(g2d);
        }
        
        // Apply lighting overlay (using default screen view)
        lighting.drawLighting(g2d);
    }
    
    public void render(Graphics2D g2d, Rectangle cameraView) {
        // Draw all visual effects
        for (VisualEffect effect : effects) {
            effect.draw(g2d);
        }
        
        // Apply lighting overlay with camera view
        lighting.drawLighting(g2d, cameraView);
    }

    public Lighting getLighting() {
        return lighting;
    }

    public void createExplosion(int x, int y, int radius, float duration) {
        // Create explosion with fire colors
        Color[] ringColors = {
            new Color(255, 100, 0),   // Orange
            new Color(255, 150, 0),   // Light orange
            new Color(255, 200, 0),   // Yellow-orange
            new Color(255, 255, 0),   // Yellow
            new Color(255, 50, 0)     // Red-orange
        };
        
        Color[] particleColors = {
            new Color(255, 0, 0),     // Red
            new Color(255, 100, 0),   // Orange
            new Color(255, 200, 0),   // Yellow-orange
            new Color(100, 100, 100), // Gray (smoke)
            new Color(50, 50, 50)     // Dark gray (smoke)
        };
        
        ExplosionEffect explosion = new ExplosionEffect(x, y, radius, duration, ringColors, particleColors);
        addEffect(explosion);
    }

    public void clearAllEffects() {
        lighting.clearLightSources();
        effects.clear();
    }
}
