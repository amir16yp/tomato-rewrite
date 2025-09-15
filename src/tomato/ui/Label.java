package tomato.ui;

import java.awt.*;

public class Label extends UIElement {
    public Label(String text, int width, int height) {
        super(width, height);
        this.text = text;
        this.textColor = Color.GREEN;
        this.backgroundColor = new Color(0, 0, 0, 0); // transparent
    }

    @Override
    public void render(Graphics2D g) {
        g.setColor(textColor);
        FontMetrics fm = g.getFontMetrics();
        int tx = x + (width - fm.stringWidth(text)) / 2;
        int ty = y + (height + fm.getAscent()) / 2 - 2;
        g.drawString(text, tx, ty);
    }
}
