package tomato.ui;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class UIElement {
    protected int width;
    protected int height;
    protected int x = 0;
    protected int y = 0;
    protected List<UIElement> elements = new ArrayList<>();

    protected Color backgroundColor = Color.WHITE;
    protected Color textColor = Color.BLACK;

    protected Color selectedBackgroundColor = Color.GRAY;
    protected Color selectedTextColor = Color.RED;

    protected boolean selected = false;
    protected String text = "";

    public UIElement(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void addElement(UIElement element) {
        elements.add(element);
    }

    public void render(Graphics2D g) {
        // background
        g.setColor(selected ? selectedBackgroundColor : backgroundColor);
        g.fillRect(x, y, width, height);

        // border
        g.setColor(Color.BLACK);
        g.drawRect(x, y, width, height);

        // text
        if (text != null && !text.isEmpty()) {
            g.setColor(selected ? selectedTextColor : textColor);
            FontMetrics fm = g.getFontMetrics();
            int tx = x + (width - fm.stringWidth(text)) / 2;
            int ty = y + (height + fm.getAscent()) / 2 - 2;
            g.drawString(text, tx, ty);
        }

        // children
        for (UIElement child : elements) {
            child.render(g);
        }
    }

    public boolean contains(int mx, int my) {
        return (mx >= x && mx <= x + width && my >= y && my <= y + height);
    }

    public void handleClick(MouseEvent e) {
        for (UIElement child : elements) {
            child.handleClick(e);
        }
    }


    public void handleHover(int mx, int my) {
        boolean inside = contains(mx, my);
        this.selected = inside;

        // propagate to children
        for (UIElement child : elements) {
            child.handleHover(mx, my);
        }
    }
}
