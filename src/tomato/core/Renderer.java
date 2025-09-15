package tomato.core;

import tomato.Game;
import tomato.ui.MainMenu;
import tomato.ui.Menu;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.VolatileImage;

public class Renderer extends JPanel {

    private VolatileImage backBuffer;
    private final HUD hud;
    private final Camera camera;
    public static final MainMenu MAIN_MENU = new MainMenu();
    private Menu currentMenu = MAIN_MENU;

    public void setCurrentMenu(Menu menu)
    {
        currentMenu = menu;
    }

    private int scaleX(int screenX) {
        return screenX * Game.WIDTH / getWidth();
    }

    private int scaleY(int screenY) {
        return screenY * Game.HEIGHT / getHeight();
    }

    public Renderer() {
        setDoubleBuffered(false);
        setBackground(Color.BLACK);
        setFocusable(true);
        requestFocus();
        hud = new HUD();
        camera = new Camera();
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (GameState.isPaused()) {
                    int mx = scaleX(e.getX());
                    int my = scaleY(e.getY());
                    currentMenu.handleClick(
                            new MouseEvent(e.getComponent(), e.getID(),
                                    e.getWhen(), e.getModifiersEx(),
                                    mx, my, e.getClickCount(),
                                    e.isPopupTrigger(), e.getButton())
                    );
                    repaint();
                }
            }
        });

        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (GameState.CURRENT_STATE == GameState.GameStateType.PAUSED) {
                    int mx = scaleX(e.getX());
                    int my = scaleY(e.getY());
                    currentMenu.handleHover(mx, my);
                    repaint();
                }
            }
        });



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
        switch (GameState.CURRENT_STATE)
        {
            case PLAY:
                camera.update();
                World.WORLD.update();
                break;
            case PAUSED:
                // TODO: make the main menu both clickable and navigatable with keyboard
                break;
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

                switch (GameState.CURRENT_STATE)
                {
                    case PLAY:
                        AffineTransform original = g2.getTransform();
                        camera.applyTransform(g2, Game.WIDTH, Game.HEIGHT);

                        World.WORLD.render(g2, camera.getViewBounds(Game.WIDTH, Game.HEIGHT));

                        camera.resetTransform(g2, original);
                        hud.render(g2);
                        break;
                    case PAUSED:
                        currentMenu.render(g2);
                        break;
                    default:
                        String msg = "IMPLEMENT YER GAME STATE!!!";
                        // paused/menu background
                        g2.setColor(Color.WHITE);
                        g2.fillRect(0, 0, Game.WIDTH, Game.HEIGHT);
                        g2.setColor(Color.BLACK);
                        g2.drawString(msg, (Game.WIDTH /2)- g2.getFontMetrics().stringWidth(msg), Game.HEIGHT /2);
                        break;
                }
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
