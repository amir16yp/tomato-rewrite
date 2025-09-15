package tomato.ui;

import tomato.Game;

import java.awt.*;

public class Menu extends UIElement {

    private int padding = 10;
    private int currentY;

    public Menu() {
        super(Game.WIDTH, Game.HEIGHT);
        this.backgroundColor = new Color(0, 0, 0, 100); // translucent
        this.currentY = 80; // initial offset from top
    }

    /**
     * Automatically create a button, size it, center it,
     * and assign its click action.
     */
    public Button addButton(String text, Runnable action) {
        int btnWidth = 200;
        int btnHeight = 40;

        Button btn = new Button(text, btnWidth, btnHeight, action);
        btn.setPosition((width - btnWidth) / 2, currentY);

        elements.add(btn);
        currentY += btnHeight + padding;

        return btn;
    }

    /**
     * Automatically create and add a centered label.
     */
    public Label addLabel(String text, int fontSize) {
        int lblWidth = 300;
        int lblHeight = fontSize + 10;

        Label lbl = new Label(text, lblWidth, lblHeight);
        lbl.setPosition((width - lblWidth) / 2, currentY);

        elements.add(lbl);
        currentY += lblHeight + padding;

        return lbl;
    }

    @Override
    public void render(Graphics2D g) {
        // menu background
        g.setColor(backgroundColor);
        g.fillRect(x, y, width, height);

        // render children
        for (UIElement child : elements) {
            child.render(g);
        }
    }
}
