package tomato.core;

import tomato.Game;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class Renderer extends JPanel {

    private BufferedImage screenBuffer;
    private Camera camera;

    public Renderer() {
        // Try enabling hardware acceleration
        System.setProperty("sun.java2d.opengl", "True");
        System.setProperty("sun.java2d.d3d", "True");
        System.setProperty("sun.java2d.ddforcevram", "True");

        createScreenBuffer();
        camera = new Camera();

        setPreferredSize(new Dimension(Game.WIDTH, Game.HEIGHT));
        setBackground(Color.BLACK);
        setDoubleBuffered(false); // We're doing our own buffering
        setFocusable(true);
        requestFocus();
    }

    private void createScreenBuffer() {
        try {
            GraphicsConfiguration gc = GraphicsEnvironment
                    .getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice()
                    .getDefaultConfiguration();

            screenBuffer = gc.createCompatibleImage(
                    Game.WIDTH,
                    Game.HEIGHT,
                    Transparency.TRANSLUCENT
            );

            clearBuffer();
        } catch (Exception e) {
            System.err.println("[Renderer] Failed to create screen buffer: " + e.getMessage());
            screenBuffer = null;
        }
    }

    private void clearBuffer() {
        if (screenBuffer == null) return;
        Graphics2D g = screenBuffer.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, Game.WIDTH, Game.HEIGHT);
        g.dispose();
    }

    /**
     * Call this before painting to ensure buffer is valid.
     */
    private void validateBuffer() {
        if (screenBuffer == null) {
            createScreenBuffer();
        }
    }

    /**
     * Main update method – your game loop calls this.
     */
    public void update() {
        // In a real game you’d draw the world/entities here.
        // For now just clear to black each frame.
        validateBuffer();

        if (screenBuffer != null) {
            Graphics2D g = screenBuffer.createGraphics();
            try {
                // Fast rendering hints
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
                g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
                g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);

                // Clear screen first
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, Game.WIDTH, Game.HEIGHT);

                // Update camera
                camera.update(1.0f / 60.0f); // Assuming 60 FPS

                // Save original transform
                AffineTransform originalTransform = g.getTransform();
                
                // Apply camera transform
                camera.applyTransform(g, Game.WIDTH, Game.HEIGHT);

                // Render the world with camera transform
                World.WORLD.update();
                World.WORLD.render(g);

                // Reset transform for UI elements
                camera.resetTransform(g, originalTransform);

            } finally {
                g.dispose();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Don't call super.paintComponent to avoid clearing
        
        validateBuffer();
        if (screenBuffer == null) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
            return;
        }

        Graphics2D g2d = (Graphics2D) g;
        
        // Save hint
        Object oldHint = g2d.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        // Draw the buffer directly
        g2d.drawImage(screenBuffer, 0, 0, getWidth(), getHeight(), null);

        // Restore hint
        if (oldHint != null) {
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, oldHint);
        } else {
            g2d.getRenderingHints().remove(RenderingHints.KEY_INTERPOLATION);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(Game.WIDTH, Game.HEIGHT);
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    /**
     * Get the camera instance for controlling view
     */
    public Camera getCamera() {
        return camera;
    }
}
