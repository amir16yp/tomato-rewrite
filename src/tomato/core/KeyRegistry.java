package tomato.core;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * KeyRegistry handles keyboard input by tracking currently pressed keys
 * and providing a callback mechanism for key press events.
 */
public class KeyRegistry implements KeyListener, MouseListener {

    // Set to track currently pressed keys
    private final CopyOnWriteArraySet<Integer> pressedKeys = new CopyOnWriteArraySet<>();

    // Map for key press event callbacks
    private final ConcurrentHashMap<Integer, Runnable> keyPressedMap = new ConcurrentHashMap<>();

    // Track left mouse button state
    private boolean leftMousePressed = false;

    public KeyRegistry() {
        // Empty constructor
    }

    /**
     * Registers an action to run when a key is pressed.
     *
     * @param keyCode The key code (from KeyEvent)
     * @param action  The action to run
     */
    public void onKeyPressed(int keyCode, Runnable action) {
        keyPressedMap.put(keyCode, action);
    }

    /**
     * Checks if a key is currently pressed.
     *
     * @param keyCode The key code to check
     * @return True if the key is pressed, false otherwise
     */
    public boolean isKeyPressed(int keyCode) {
        return pressedKeys.contains(keyCode);
    }

    /**
     * Checks if the left mouse button is currently pressed.
     *
     * @return True if the left mouse button is pressed, false otherwise
     */
    public boolean isLeftMousePressed() {
        return leftMousePressed;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();

        // Only trigger pressed actions if the key wasn't already pressed
        if (!pressedKeys.contains(keyCode)) {
            // Execute the action if one is registered for this key
            Runnable action = keyPressedMap.get(keyCode);
            if (action != null) {
                action.run();
            }
        }

        // Add to pressed keys set
        pressedKeys.add(keyCode);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Remove from pressed keys set
        pressedKeys.remove(e.getKeyCode());
    }

    // Implement MouseListener methods
    @Override
    public void mouseClicked(MouseEvent e) {
        // Not implemented
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            leftMousePressed = true;

            // Remove initial fire - let handleInput() handle all firing
            // This prevents the double-fire issue where weapon fires once here
            // and again in PlayerEntity.handleInput()
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            leftMousePressed = false;
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // Not implemented
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // Not implemented
    }
}