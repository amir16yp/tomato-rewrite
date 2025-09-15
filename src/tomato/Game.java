package tomato;

import tomato.core.GameLoop;
import tomato.core.GameState;
import tomato.core.KeyRegistry;
import tomato.core.Renderer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Game extends JFrame {

    public static final int WIDTH = 640;
    public static final int HEIGHT = 360;
    private final Rectangle windowBounds;
    public static final String GAME_TITLE = "Untitled Tank Game";

    public static Renderer RENDERER;
    public static final KeyRegistry KEY_REGISTRY = new KeyRegistry();
    public static final GameLoop GAME_LOOP = new GameLoop();
    public static Game GAME;

//    static {
//        // Optimal Java2D settings for Linux performance
//        // Disable OpenGL and XRender pipelines, use software rendering
//        // This works best on Linux, especially on Wayland
//        System.setProperty("sun.java2d.opengl", "false");
//        System.setProperty("sun.java2d.xrender", "false");
//
//        // Windows-specific settings (ignored on Linux)
//        System.setProperty("sun.java2d.d3d", "True");
//    }

    public Game() {
        GAME = this;
        KEY_REGISTRY.onKeyPressed(KeyEvent.VK_ESCAPE, () -> {
            if (GameState.CURRENT_STATE == GameState.GameStateType.PLAY)
            {
                GameState.CURRENT_STATE = GameState.GameStateType.PAUSED;
            }
        });
        RENDERER = new Renderer();
        setTitle(GAME_TITLE);
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // maximize

        setResizable(true);
        windowBounds = new Rectangle(100, 100, 800, 600);
        add(RENDERER);
        addKeyListener(KEY_REGISTRY);
        setFocusable(true);
        requestFocus();
        setVisible(true);
        // Start the game loop
        GAME_LOOP.start();
    }

    public static void main(String[] args) {
        new Game();
    }


}