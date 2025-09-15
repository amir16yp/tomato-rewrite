package tomato.ui;

//import potato.Game;

import java.awt.*;

public class Checkbox extends UIElement {
    private final String label;
    public Runnable onTrue;
    public Runnable onFalse;
    private boolean checked;
    private boolean isHovered = false;
    
    // Modern tactical-themed colors
    private static final Color BORDER_COLOR = new Color(210, 60, 50);     // Red border
    private static final Color BORDER_HIGHLIGHT = new Color(250, 90, 80);  // Brighter red border when selected
    private static final Color GLOW_COLOR = new Color(210, 60, 50, 70);   // Red glow with transparency
    private static final Color CHECK_COLOR = new Color(255, 70, 70);      // Red checkmark

    public Checkbox(int x, int y, int width, int height, boolean visible, String label, Runnable onTrue, Runnable onFalse) {
        super(x, y, width, height, visible);
        this.checked = false;
        this.label = label;
        setBackgroundColor(new Color(30, 40, 50));  // Dark blue-gray 
        setHighlightColor(new Color(50, 60, 80));   // Lighter blue-gray for hover/check
        setTextColor(new Color(220, 220, 220));     // Light gray for label text
        this.onFalse = onFalse;
        this.onTrue = onTrue;
        //logger.info("Created Checkbox with x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + ", visible=" + visible);
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String getType() {
        return "Checkbox";
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean toCheck) {
        this.checked = toCheck;
    }

    public void toggleChecked() {
        if (!isVisible()) {
            return;
        }
        //logger.info("Toggle " + !this.checked);
        this.checked = !this.checked;
        if (this.checked) {
//            Game.SOUND_MANAGER.playSoundEffect("CHECK");
            if (onTrue != null) {
                onTrue.run();
            }
        } else {
//            Game.SOUND_MANAGER.playSoundEffect("UNCHECK");
            if (onFalse != null) {
                onFalse.run();
            }
        }
    }

    @Override
    public void draw(Graphics g) {
        super.draw(g);
        if (isVisible()) {
            Graphics2D g2d = (Graphics2D) g;
            
            // Enable anti-aliasing for smoother rendering
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Save original composite
            Composite originalComposite = g2d.getComposite();
            
            // Draw checkbox background
            Color bgColor = isSelected() || isHovered ? getHighlightColor() : getBackgroundColor();
            g2d.setColor(bgColor);
            g2d.fillRoundRect(getX(), getY(), getWidth(), getHeight(), 4, 4);
            
            // Draw subtle gradient overlay for depth
            GradientPaint gradient = new GradientPaint(
                getX(), getY(), new Color(255, 255, 255, 30), 
                getX(), getY() + getHeight(), new Color(0, 0, 0, 30)
            );
            g2d.setPaint(gradient);
            g2d.fillRoundRect(getX(), getY(), getWidth(), getHeight(), 4, 4);
            
            // Draw glow effect when selected, hovered or checked
            if (isSelected() || isHovered || checked) {
                // Set alpha composite for the glow
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                g2d.setColor(GLOW_COLOR);
                
                // Draw outer glow
                int glowSize = 3;
                for (int i = 0; i < glowSize; i++) {
                    g2d.drawRoundRect(
                        getX() - i, 
                        getY() - i, 
                        getWidth() + i * 2, 
                        getHeight() + i * 2, 
                        4 + i, 
                        4 + i
                    );
                }
                
                // Reset composite
                g2d.setComposite(originalComposite);
            }
            
            // Draw checkbox border
            g2d.setColor(checked ? BORDER_HIGHLIGHT : BORDER_COLOR);
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.drawRoundRect(getX(), getY(), getWidth(), getHeight(), 4, 4);
            
            // Draw tactical corner accents
            int cornerSize = 3;
            g2d.setStroke(new BasicStroke(1));
            
            // Top-left corner
            g2d.drawLine(getX(), getY() + cornerSize, getX(), getY());
            g2d.drawLine(getX(), getY(), getX() + cornerSize, getY());
            
            // Bottom-right corner
            g2d.drawLine(getX() + getWidth() - cornerSize, getY() + getHeight(), getX() + getWidth(), getY() + getHeight());
            g2d.drawLine(getX() + getWidth(), getY() + getHeight(), getX() + getWidth(), getY() + getHeight() - cornerSize);
            
            // Draw checkbox check mark if checked
            if (checked) {
                g2d.setColor(CHECK_COLOR);
                g2d.setStroke(new BasicStroke(2));
                
                // Draw a modern checkmark instead of X
                int padding = getWidth() / 4;
                int midX = getX() + getWidth() / 2;
                int midY = getY() + getHeight() / 2;
                
                g2d.drawLine(getX() + padding, midY, midX, getY() + getHeight() - padding);
                g2d.drawLine(midX, getY() + getHeight() - padding, getX() + getWidth() - padding, getY() + padding);
            }
            
            // Draw label with shadow for depth
            Font originalFont = g2d.getFont();
            Font tacticalFont = new Font(Font.SANS_SERIF, Font.BOLD, 12);
            g2d.setFont(tacticalFont);
            
            // Draw text shadow
            g2d.setColor(new Color(0, 0, 0, 100));
            g2d.drawString(label, getX() + getWidth() + 10 + 1, getY() + getHeight() / 2 + g2d.getFontMetrics().getAscent() / 2 + 1);
            
            // Draw actual text
            g2d.setColor(getTextColor());
            g2d.drawString(label, getX() + getWidth() + 10, getY() + getHeight() / 2 + g2d.getFontMetrics().getAscent() / 2);
            
            // Restore original settings
            g2d.setFont(originalFont);
            g2d.setComposite(originalComposite);
        }
    }

    @Override
    public void update() {
        // Update logic for checkbox if needed
    }

    // Implement setSelected method for Checkbox
    public void setSelected(boolean selected) {
        if (selected != this.isSelected()) {
            super.setSelected(selected);
        }
    }

    // Methods to set colors from Button
    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public void setHighlightColor(Color highlightColor) {
        this.highlightColor = highlightColor;
    }

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }

    public void setHovered(boolean hovered) {
        this.isHovered = hovered;
    }
}