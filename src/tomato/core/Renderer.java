package tomato.core;

import tomato.Game;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.VolatileImage;

public class Renderer extends JPanel {

    private VolatileImage backBuffer;
    private HUD hud;
    private Camera camera;

    public Renderer() {
        setDoubleBuffered(false);
        setBackground(Color.BLACK);
        setFocusable(true);
        requestFocus();
        hud = new HUD();
        camera = new Camera();
        setPreferredSize(new Dimension(Game.WIDTH, Game.HEIGHT));

        createBackBuffer();
    }

    private void createBackBuffer() {
        GraphicsConfiguration gc = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice()
                .getDefaultConfiguration();

        backBuffer = gc.createCompatibleVolatileImage(Game.WIDTH, Game.HEIGHT);
        backBuffer.setAccelerationPriority(1.0f);
    }

    /**
     * Logic update only – no drawing.
     */
    public void update() {
        if (GameState.CURRENT_STATE == GameState.GameStateType.PLAY) {
            camera.update();
            World.WORLD.update();
        }
        // menus/paused logic later
    }

    /**
     * Paints the current frame.
     * This is called by Swing when you call repaint().
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (backBuffer == null) {
            createBackBuffer();
        }

        do {
            int valid = backBuffer.validate(
                    GraphicsEnvironment.getLocalGraphicsEnvironment()
                            .getDefaultScreenDevice()
                            .getDefaultConfiguration()
            );
            if (valid == VolatileImage.IMAGE_INCOMPATIBLE) {
                createBackBuffer();
            }

            Graphics2D g2 = backBuffer.createGraphics();
            try {
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

                // Clear
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, Game.WIDTH, Game.HEIGHT);

                if (GameState.CURRENT_STATE == GameState.GameStateType.PLAY) {
                    AffineTransform original = g2.getTransform();
                    camera.applyTransform(g2, Game.WIDTH, Game.HEIGHT);

                    World.WORLD.render(g2);

                    camera.resetTransform(g2, original);
                } else {
                    // paused/menu background
                    g2.setColor(Color.DARK_GRAY);
                    g2.fillRect(0, 0, Game.WIDTH, Game.HEIGHT);
                }
                hud.render(g2);
            } finally {
                g2.dispose();
            }
        } while (backBuffer.contentsLost());

        // Blit backBuffer onto screen
        g.drawImage(backBuffer, 0, 0, getWidth(), getHeight(), null);
    }

    public Camera getCamera() {
        return camera;
    }
}
