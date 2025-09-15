package tomato.ui;

import tomato.Game;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public class RangeSlider extends UIElement {
    // Modern tactical-themed colors
    private static final Color TRACK_BACKGROUND = new Color(30, 40, 50);
    private static final Color TRACK_FILLED = new Color(210, 60, 50);
    private static final Color TRACK_HIGHLIGHT = new Color(255, 70, 70);
    private static final Color THUMB_COLOR = new Color(220, 220, 220);
    private static final Color SHADOW_COLOR = new Color(0, 0, 0, 100);
    private static final Color VALUE_TEXT_COLOR = new Color(220, 220, 220);
    
    private final double minValue;
    private final double maxValue;
    private final int decimalPlaces;
    private double currentValue;
    private Consumer<Double> onValueChange;
    private final double stepSize;

    // Track dimensions
    private int trackStartX;
    private int trackWidth;
    
    // Interaction state
    private boolean isDragging = false;
    private boolean isHovered = false;

    public RangeSlider(int x, int y, int width, int height,
                       double minValue, double maxValue, double initialValue,
                       int decimalPlaces) {
        super(x, y, width, height, true);
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.currentValue = clampValue(initialValue);
        this.decimalPlaces = decimalPlaces;
        
        // Calculate step size
        double range = maxValue - minValue;
        if (decimalPlaces <= 0) {
            this.stepSize = Math.max(1, range / 100.0);
        } else {
            double factor = Math.pow(10, -decimalPlaces);
            this.stepSize = Math.max(factor, range / 100.0);
        }
        
        // Calculate track position (with padding)
        this.trackStartX = x + 10;
        this.trackWidth = width - 20;
    }

    public void setOnValueChange(Consumer<Double> action) {
        this.onValueChange = action;
    }

    private double clampValue(double value) {
        return Math.max(minValue, Math.min(maxValue, value));
    }

    private double getPercentage() {
        return (currentValue - minValue) / (maxValue - minValue);
    }
    
    public void increaseValue() {
        setValue(currentValue + stepSize);
    }
    
    public void decreaseValue() {
        setValue(currentValue - stepSize);
    }
    
    public void setValueByPercentage(double percentage) {
        percentage = Math.max(0.0, Math.min(1.0, percentage));
        setValue(minValue + (percentage * (maxValue - minValue)));
    }

    @Override
    public void update() {
        super.update();
    }

    @Override
    public void draw(Graphics g) {
        super.draw(g);
        if (isVisible()) {
            Graphics2D g2d = (Graphics2D) g;
            
            // Enable anti-aliasing
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            
            // Save original settings
            Composite originalComposite = g2d.getComposite();
            Stroke originalStroke = g2d.getStroke();
            
            // Draw track background
            int trackHeight = 8;
            int trackY = getY() + (getHeight() - trackHeight) / 2;
            g2d.setColor(TRACK_BACKGROUND);
            g2d.fillRoundRect(trackStartX, trackY, trackWidth, trackHeight, 4, 4);
            
            // Draw filled track
            if (getPercentage() > 0) {
                int filledWidth = (int) (trackWidth * getPercentage());
                g2d.setColor(isDragging ? TRACK_HIGHLIGHT : TRACK_FILLED);
                g2d.fillRoundRect(trackStartX, trackY, filledWidth, trackHeight, 4, 4);
            }
            
            // Draw track border
            g2d.setColor(isHovered ? new Color(100, 110, 120, 200) : new Color(70, 80, 90, 150));
            g2d.setStroke(new BasicStroke(isHovered ? 2f : 1f));
            g2d.drawRoundRect(trackStartX, trackY, trackWidth, trackHeight, 4, 4);
            
            // Draw thumb
            int thumbX = trackStartX + (int) (trackWidth * getPercentage());
            int thumbSize = trackHeight + 8;
            int thumbY = getY() + (getHeight() - thumbSize) / 2;
            
            // Thumb shadow
            g2d.setColor(SHADOW_COLOR);
            g2d.fillOval(thumbX - thumbSize/2 + 2, thumbY + 2, thumbSize, thumbSize);
            
            // Thumb fill
            Color thumbColor1 = isDragging ? new Color(250, 250, 250) : new Color(240, 240, 240);
            Color thumbColor2 = isDragging ? new Color(200, 200, 200) : new Color(180, 180, 180);
            GradientPaint thumbGradient = new GradientPaint(
                thumbX - thumbSize/2, thumbY, thumbColor1,
                thumbX - thumbSize/2, thumbY + thumbSize, thumbColor2
            );
            g2d.setPaint(thumbGradient);
            g2d.fillOval(thumbX - thumbSize/2, thumbY, thumbSize, thumbSize);
            g2d.setPaint(null);
            
            // Thumb border
            g2d.setColor(isDragging ? TRACK_HIGHLIGHT : TRACK_FILLED);
            g2d.setStroke(new BasicStroke(isDragging ? 2f : 1.5f));
            g2d.drawOval(thumbX - thumbSize/2, thumbY, thumbSize, thumbSize);
            
            // Value text
            String valueText = formatValue(currentValue);
            Font originalFont = g.getFont();
            Font tacticalFont = new Font(Font.SANS_SERIF, Font.BOLD, 12);
            g2d.setFont(tacticalFont);
            
            FontMetrics metrics = g2d.getFontMetrics();
            int textWidth = metrics.stringWidth(valueText);
            int textX = trackStartX + (trackWidth - textWidth) / 2;
            int textY = trackY + trackHeight + 20;
            
            // Text shadow
            g2d.setColor(SHADOW_COLOR);
            g2d.drawString(valueText, textX + 1, textY + 1);
            
            // Text
            g2d.setColor(VALUE_TEXT_COLOR);
            g2d.drawString(valueText, textX, textY);
            
            // Restore settings
            g2d.setFont(originalFont);
            g2d.setStroke(originalStroke);
            g2d.setComposite(originalComposite);
        }
    }

    private String formatValue(double value) {
        if (decimalPlaces <= 0) {
            return String.valueOf(Math.round(value));
        }
        return String.format("%." + decimalPlaces + "f", value);
    }

    public void handleMouseEvent(MouseEvent e) {
        // Convert screen coordinates to internal coordinates
        double scaleX = (double) Game.RENDERER.getWidth() / Game.WIDTH;
        double scaleY = (double) Game.RENDERER.getHeight() / Game.HEIGHT;
        
        int x = (int) (e.getX() / scaleX);
        int y = (int) (e.getY() / scaleY);
        
        System.out.printf("[DEBUG] RangeSlider.handleMouseEvent: screen=(%d,%d) internal=(%d,%d) event=%d\n", 
                         e.getX(), e.getY(), x, y, e.getID());
        
        // Check if click is within slider bounds
        if (x >= getX() && x <= getX() + getWidth() && 
            y >= getY() && y <= getY() + getHeight()) {
            
            System.out.println("[DEBUG] RangeSlider: click within bounds");
            
            if (e.getID() == MouseEvent.MOUSE_PRESSED) {
                isDragging = true;
                updateValueFromMouseX(x);
            } else if (e.getID() == MouseEvent.MOUSE_RELEASED) {
                isDragging = false;
                updateValueFromMouseX(x);
            } else if (e.getID() == MouseEvent.MOUSE_DRAGGED && isDragging) {
                updateValueFromMouseX(x);
            }
        }
    }
    
    private void updateValueFromMouseX(int mouseX) {
        // Calculate percentage based on mouse position relative to track
        double percentage = (double)(mouseX - trackStartX) / trackWidth;
        percentage = Math.max(0.0, Math.min(1.0, percentage));
        
        System.out.printf("[DEBUG] RangeSlider.updateValueFromMouseX: mouseX=%d, trackStartX=%d, trackWidth=%d, percentage=%.2f\n", 
                         mouseX, trackStartX, trackWidth, percentage);
        
        setValueByPercentage(percentage);
    }
    
    public void handleMouseMove(int x, int y) {
        // Convert screen coordinates to internal coordinates
        double scaleX = (double) Game.RENDERER.getWidth() / Game.WIDTH;
        double scaleY = (double) Game.RENDERER.getHeight() / Game.HEIGHT;
        
        int internalX = (int) (x / scaleX);
        int internalY = (int) (y / scaleY);
        
        // Update hover state
        isHovered = (internalX >= getX() && internalX <= getX() + getWidth() && 
                    internalY >= getY() && internalY <= getY() + getHeight());
        
        // Handle dragging
        if (isDragging) {
            updateValueFromMouseX(internalX);
        }
    }
    
    @Override
    public boolean containsPoint(int x, int y) {
        // Convert screen coordinates to internal coordinates
        double scaleX = (double) Game.RENDERER.getWidth() / Game.WIDTH;
        double scaleY = (double) Game.RENDERER.getHeight() / Game.HEIGHT;
        
        int internalX = (int) (x / scaleX);
        int internalY = (int) (y / scaleY);
        
        boolean hit = (internalX >= getX() && internalX <= getX() + getWidth() && 
                      internalY >= getY() && internalY <= getY() + getHeight());
        
//        if (hit) {
//            System.out.printf("[DEBUG] RangeSlider.containsPoint: screen=(%d,%d) internal=(%d,%d) bounds=(%d,%d,%d,%d)\n",
//                            x, y, internalX, internalY, getX(), getY(), getWidth(), getHeight());
//        }
//
        return hit;
    }

    @Override
    public boolean containsPoint(int x, int y, int scrollOffset) {
        // No scroll offset needed in paginated system
        return containsPoint(x, y);
    }
    
    public double getValue() {
        return currentValue;
    }

    public void setValue(double value) {
        double oldValue = this.currentValue;
        this.currentValue = clampValue(value);
        
        // Only call callback if value actually changed
        if (Math.abs(this.currentValue - oldValue) > 0.001 && onValueChange != null) {
            onValueChange.accept(currentValue);
        }
    }

    public double getMinValue() {
        return minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }
    
    public double getStepSize() {
        return stepSize;
    }
}