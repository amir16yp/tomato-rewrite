package tomato.vfx;

import java.awt.*;

public interface VisualEffect {
    void update();
    void draw(Graphics2D g);
    boolean isFinished();
}
