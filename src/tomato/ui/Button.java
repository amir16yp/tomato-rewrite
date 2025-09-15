package tomato.ui;

import java.awt.*;
import java.awt.event.MouseEvent;

public class Button extends UIElement {
    public Runnable onClick;

    public Button(String text, int width, int height, Runnable onClick) {
        super(width, height);
        this.text = text;
        this.onClick = onClick;
        this.backgroundColor = Color.LIGHT_GRAY;
        this.selectedBackgroundColor = Color.DARK_GRAY;
    }

    @Override
    public void handleClick(MouseEvent e) {
        if (contains(e.getX(), e.getY()) && onClick != null) {
            onClick.run();
        }
    }
}
