package tomato.ui;

import tomato.Game;
import tomato.core.GameState;

import java.awt.*;

public class Button extends UIElement {
    public boolean selected;
    private String text;
    private Color textColor;
    private Color backgroundColor;
    private Color highlightColor;
    private Runnable onSelectedAction;
    private boolean enabled = true;
    private boolean isHovered = false;
    
    // Modern tactical-themed colors
    private static final Color BUTTON_BORDER = new Color(210, 60, 50);     // Red border
    private static final Color BUTTON_BORDER_HIGHLIGHT = new Color(250, 90, 80);  // Brighter red border when selected
    private static final Color BUTTON_GLOW = new Color(210, 60, 50, 70);   // Red glow with transparency

    public Button(int x, int y, int width, int height, String text) {
        super(x, y, width, height, true);
        this.text = text;
        this.textColor = new Color(220, 220, 220);  // Light gray text
        this.backgroundColor = new Color(30, 40, 50);  // Dark blue-gray 
        this.highlightColor = new Color(50, 60, 80);   // Lighter blue-gray for hover/select
        this.selected = false;
    }

    public void setHighlightColor(Color highlightColor) {
        this.highlightColor = highlightColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }

    public void setOnSelectedAction(Runnable action) {
        this.onSelectedAction = action;
    }

    public void onOptionSelected() {
        if (onSelectedAction != null && this.isVisible() && GameState.CURRENT_STATE == GameState.GameStateType.PAUSED && enabled) {
//            Game.SOUND_MANAGER.playSoundEffect("CLICK");
            onSelectedAction.run();
        }
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
    
    public void setHovered(boolean hovered) {
        this.isHovered = hovered;
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        
        // Save the original composite
        Composite originalComposite = g2d.getComposite();
        
        // Use anti-aliasing for smoother edges
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Determine the appropriate color based on state
        Color bgColor = enabled ? (selected ? highlightColor : backgroundColor) : new Color(50, 50, 60);
        
        // Draw button base
        g2d.setColor(bgColor);
        g2d.fillRoundRect(getX(), getY(), getWidth(), getHeight(), 8, 8);
        
        // Draw subtle gradient overlay
        GradientPaint gradient = new GradientPaint(
            getX(), getY(), new Color(255, 255, 255, 30), 
            getX(), getY() + getHeight(), new Color(0, 0, 0, 30)
        );
        g2d.setPaint(gradient);
        g2d.fillRoundRect(getX(), getY(), getWidth(), getHeight(), 8, 8);
        
        // Draw glow effect when selected or hovered
        if (selected || isHovered) {
            // Set alpha composite for the glow
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
            g2d.setColor(BUTTON_GLOW);
            
            // Draw outer glow
            int glowSize = 4;
            for (int i = 0; i < glowSize; i++) {
                g2d.drawRoundRect(
                    getX() - i, 
                    getY() - i, 
                    getWidth() + i * 2, 
                    getHeight() + i * 2, 
                    8 + i, 
                    8 + i
                );
            }
            
            // Reset composite
            g2d.setComposite(originalComposite);
        }
        
        // Draw border with tactical red color
        g2d.setColor(selected ? BUTTON_BORDER_HIGHLIGHT : BUTTON_BORDER);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(getX(), getY(), getWidth(), getHeight(), 8, 8);
        
        // Draw decorative corner accents for a tactical HUD look
        int cornerSize = 5;
        g2d.setStroke(new BasicStroke(1));
        
        // Top-left corner
        g2d.drawLine(getX(), getY() + cornerSize, getX(), getY());
        g2d.drawLine(getX(), getY(), getX() + cornerSize, getY());
        
        // Top-right corner
        g2d.drawLine(getX() + getWidth() - cornerSize, getY(), getX() + getWidth(), getY());
        g2d.drawLine(getX() + getWidth(), getY(), getX() + getWidth(), getY() + cornerSize);
        
        // Bottom-left corner
        g2d.drawLine(getX(), getY() + getHeight() - cornerSize, getX(), getY() + getHeight());
        g2d.drawLine(getX(), getY() + getHeight(), getX() + cornerSize, getY() + getHeight());
        
        // Bottom-right corner
        g2d.drawLine(getX() + getWidth() - cornerSize, getY() + getHeight(), getX() + getWidth(), getY() + getHeight());
        g2d.drawLine(getX() + getWidth(), getY() + getHeight(), getX() + getWidth(), getY() + getHeight() - cornerSize);
        
        // Choose text color based on enabled state
        Color txtColor = enabled ? textColor : new Color(120, 120, 140);
        g2d.setColor(txtColor);
        
        // Set font to a more modern, tactical look
        Font originalFont = g.getFont();
        Font tacticalFont = new Font(Font.SANS_SERIF, Font.BOLD, 12);
        g2d.setFont(tacticalFont);
        
        // Draw the text with a subtle shadow for depth
        int stringWidth = g2d.getFontMetrics().stringWidth(text);
        int stringHeight = g2d.getFontMetrics().getHeight();
        int x = getX() + (getWidth() - stringWidth) / 2;
        int y = getY() + (getHeight() - stringHeight) / 2 + g2d.getFontMetrics().getAscent();
        
        // Draw text shadow
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.drawString(text, x + 1, y + 1);
        
        // Draw actual text
        g2d.setColor(txtColor);
        g2d.drawString(text, x, y);
        
        // Restore the original font
        g2d.setFont(originalFont);
        
        // Restore the original composite
        g2d.setComposite(originalComposite);
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean setSelected) {
        selected = setSelected;
    }
    
    /**
     * Checks if the button is enabled
     * @return true if the button is enabled, false otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Sets whether the button is enabled
     * @param enabled true to enable the button, false to disable it
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}