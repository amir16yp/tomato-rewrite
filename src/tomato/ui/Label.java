package tomato.ui;


import tomato.Game;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Label extends UIElement {
    private String text;
    private boolean centered;
    private List<String> lines = new ArrayList<>();
    
    // Text wrapping flag
    private boolean wrapText = false;
    
    // Style options
    private boolean useTextShadow = true;
    private boolean useTacticalStyle = true;
    private boolean useTextGlow = false;
    
    // Default font for metrics calculation
    private static final Font DEFAULT_FONT = new Font("SansSerif", Font.PLAIN, 12);
    private static final Font TACTICAL_FONT = new Font("SansSerif", Font.BOLD, 12);
    
    // Tactical style colors
    private static final Color SHADOW_COLOR = new Color(0, 0, 0, 100);
    private static final Color GLOW_COLOR = new Color(255, 70, 70, 80);
    private static final Color TAG_COLOR = new Color(255, 70, 70, 180);
    
    // Hover and click functionality
    private Color hoverColor = null;
    private Runnable clickAction = null;
    private boolean isHovered = false;
    private int preferredHeight = -1;

    public void setHoverColor(Color hoverColor) {
        this.hoverColor = hoverColor;
    }
    
    public void setClickAction(Runnable clickAction) {
        this.clickAction = clickAction;
    }
    
    public void setHovered(boolean hovered) {
        this.isHovered = hovered;
    }
    
    public boolean isHovered() {
        return isHovered;
    }
    
    public void onClick() {
        if (clickAction != null) {
            clickAction.run();
        }
    }
    
    /**
     * Updates the hover state based on mouse position
     * @param mouseX The current mouse X coordinate
     * @param mouseY The current mouse Y coordinate
     */
    public void updateHoverState(int mouseX, int mouseY) {
        boolean wasHovered = isHovered;
        boolean nowHovered = containsPoint(mouseX, mouseY);
        
        if (wasHovered != nowHovered) {
            setHovered(nowHovered);
        }
    }
    
    @Override
    public void update() {
        super.update();
        // Note: Mouse position updates should be handled by the parent container (Menu)
        // This method is here for consistency with UIElement interface
    }

    public void setPreferredHeight(int height) {
        this.preferredHeight = height;
    }

    @Override
    public int getHeight() {
        return preferredHeight > 0 ? preferredHeight : super.getHeight();
    }

    public Label(int x, int y, int width, int height, String text) {
        super(x, y, width, height, true);
        this.text = text;
        this.centered = false;
        setBackgroundColor(new Color(0, 0, 0, 0));  // Transparent background
        setTextColor(new Color(220, 220, 220));  // Light gray text
        setShouldDrawBackgroundColor(false);
        
        // Process text to handle newlines and wrapping
        processText();
    }

    public Label(int x, int y, String text) {
        this(x, y, 0, 0, text); // Width and height will be set based on text dimensions
    }
    
    public void setUseTacticalStyle(boolean useTacticalStyle) {
        this.useTacticalStyle = useTacticalStyle;
    }
    
    public void setUseTextShadow(boolean useTextShadow) {
        this.useTextShadow = useTextShadow;
    }
    
    public void setUseTextGlow(boolean useTextGlow) {
        this.useTextGlow = useTextGlow;
    }
    
    private void processText() {
        lines.clear();
        if (text == null || text.length() == 0) {
            return;
        }
        
        // First split by explicit newlines
        String[] splitByNewline = text.split("\n");
        
        // If wrapping is enabled and width is set, break lines by width
        if (wrapText && getWidth() > 0) {
            for (String line : splitByNewline) {
                wrapLine(line);
            }
        } else {
            // Just add the lines split by newline
            for (String line : splitByNewline) {
                lines.add(line);
            }
        }
    }
    
    private void wrapLine(String line) {
        if (line.length() == 0) {
            lines.add("");
            return;
        }
        
        // Calculate maximum width - either the label width if set, or the game width
        int maxWidth = getWidth() > 0 ? getWidth() : Game.WIDTH;
        
        // Create a temporary graphics object for metrics calculation if possible
        FontMetrics metrics = null;
        BufferedImage tempImg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = tempImg.createGraphics();
        g2d.setFont(DEFAULT_FONT);
        metrics = g2d.getFontMetrics();
        
        StringBuilder currentLine = new StringBuilder();
        String[] words = line.split(" ");
        
        for (String word : words) {
            String testLine = currentLine.toString();
            if (testLine.length() != 0) {
                testLine += " ";
            }
            testLine += word;
            
            int width = metrics.stringWidth(testLine);
            
            if (width <= maxWidth) {
                if (!currentLine.equals("")) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            } else {
                // Add the current line
                if (!currentLine.equals("")) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder();
                }
                
                // If the word alone is too long, we need to break it
                if (metrics.stringWidth(word) > maxWidth) {
                    // Add character by character until we reach width limit
                    for (int i = 0; i < word.length(); i++) {
                        String testWord = currentLine.toString() + word.charAt(i);
                        if (metrics.stringWidth(testWord) <= maxWidth) {
                            currentLine.append(word.charAt(i));
                        } else {
                            lines.add(currentLine.toString());
                            currentLine = new StringBuilder();
                            currentLine.append(word.charAt(i));
                        }
                    }
                } else {
                    currentLine.append(word);
                }
            }
        }
        
        // Add the last line if anything is left
        if (!currentLine.equals("")) {
            lines.add(currentLine.toString());
        }
        
        // Clean up the graphics object
        g2d.dispose();
    }

    @Override
    public void draw(Graphics g) {
        super.draw(g);
        if (isVisible()) {
            Graphics2D g2d = (Graphics2D) g;
            
            // Enable anti-aliasing for smoother text
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            
            // Save original font
            Font originalFont = g2d.getFont();
            
            // Use tactical font if enabled
            if (useTacticalStyle) {
                g2d.setFont(TACTICAL_FONT);
            }
            
            FontMetrics metrics = g2d.getFontMetrics();
            
            // If we're word wrapping and we haven't processed the text with metrics yet,
            // or the width changed, reprocess the text
            if (wrapText && getWidth() > 0 && lines.size() == 0) {
                processText();
            }
            
            // If no explicit wrapping but we need to do auto-sizing
            if (lines.size() == 0) {
                lines.add(text);
            }
            
            // Calculate total height needed for all lines
            int lineHeight = metrics.getHeight();
            int totalTextHeight = lineHeight * lines.size();
            
            // Auto-size width if not set
            if (getWidth() == 0) {
                int maxWidth = 0;
                for (String line : lines) {
                    int lineWidth = metrics.stringWidth(line);
                    maxWidth = Math.max(maxWidth, lineWidth);
                }
                setWidth(maxWidth);
            }
            
            // Auto-size height if not set
            if (getHeight() == 0) {
                setHeight(totalTextHeight);
            }
            
            // Draw HUD tag style if tactical style is enabled (for title labels)
            if (useTacticalStyle && lines.size() == 1 && text.equals(text.toUpperCase()) && text.length() < 25) {
                drawTacticalLabel(g2d, metrics);
            } else {
                // Draw regular text for each line
                drawRegularText(g2d, metrics);
            }
            
            // Restore original font
            g2d.setFont(originalFont);
        }
    }
    
    private void drawTacticalLabel(Graphics2D g2d, FontMetrics metrics) {
        // Draw a tactical HUD-style label with a colored tag
        
        // Get the first line (we already checked there's only one for this style)
        String line = lines.get(0);
        int textWidth = metrics.stringWidth(line);
        
        // Calculate horizontal position based on alignment
        int textX;
        if (centered) {
            textX = getX() + (getWidth() - textWidth) / 2;
        } else {
            textX = getX();
        }
        
        // Calculate vertical position
        int textY = getY() + metrics.getAscent();
        
        // Draw tag rectangle
        g2d.setColor(TAG_COLOR);
        int tagPadding = 6;
        g2d.fillRect(textX - tagPadding, getY() - 1, textWidth + tagPadding * 2, getHeight());
        
        // Draw diagonal lines at the corners for tactical effect
        int lineSize = 5;
        g2d.drawLine(textX - tagPadding, getY() - 1, textX - tagPadding - lineSize, getY() - 1 - lineSize);
        g2d.drawLine(textX - tagPadding, getY() - 1 + getHeight(), textX - tagPadding - lineSize, getY() - 1 + getHeight() + lineSize);
        g2d.drawLine(textX - tagPadding + textWidth + tagPadding * 2, getY() - 1, 
                     textX - tagPadding + textWidth + tagPadding * 2 + lineSize, getY() - 1 - lineSize);
        g2d.drawLine(textX - tagPadding + textWidth + tagPadding * 2, getY() - 1 + getHeight(),
                     textX - tagPadding + textWidth + tagPadding * 2 + lineSize, getY() - 1 + getHeight() + lineSize);
        
        // Draw text with shadow
        if (useTextShadow) {
            g2d.setColor(SHADOW_COLOR);
            g2d.drawString(line, textX + 1, textY + 1);
        }
        
        // Draw actual text with hover color if hovered
        Color currentTextColor = (isHovered && hoverColor != null) ? hoverColor : getTextColor();
        g2d.setColor(currentTextColor);
        g2d.drawString(line, textX, textY);
    }
    
    private void drawRegularText(Graphics2D g2d, FontMetrics metrics) {
        // Draw each line
        int y = getY();
        
        // Draw text glow first if enabled
        if (useTextGlow) {
            Composite originalComposite = g2d.getComposite();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            g2d.setColor(GLOW_COLOR);
            
            for (String line : lines) {
                int textWidth = metrics.stringWidth(line);
                int textX = centered ? getX() + (getWidth() - textWidth) / 2 : getX();
                int textY = y + metrics.getAscent();
                
                // Draw glow by drawing the text multiple times with slight offset
                for (int i = 0; i < 3; i++) {
                    g2d.drawString(line, textX - i, textY);
                    g2d.drawString(line, textX + i, textY);
                    g2d.drawString(line, textX, textY - i);
                    g2d.drawString(line, textX, textY + i);
                }
                
                y += metrics.getHeight();
            }
            
            g2d.setComposite(originalComposite);
            y = getY(); // Reset y position for actual text
        }
        
        for (String line : lines) {
            // Skip lines that would render outside the component bounds
            if (y + metrics.getHeight() < getY() || y > getY() + getHeight()) {
                y += metrics.getHeight();
                continue;
            }
            
            int textWidth = metrics.stringWidth(line);
            
            // Calculate horizontal position based on alignment
            int textX;
            if (centered) {
                textX = getX() + (getWidth() - textWidth) / 2;
            } else {
                textX = getX();
            }
            
            // Calculate vertical position (baseline)
            int textY = y + metrics.getAscent();
            
            // Draw text shadow if enabled
            if (useTextShadow) {
                g2d.setColor(SHADOW_COLOR);
                g2d.drawString(line, textX + 1, textY + 1);
            }
            
            // Draw the text with hover color if hovered
            Color currentTextColor = (isHovered && hoverColor != null) ? hoverColor : getTextColor();
            g2d.setColor(currentTextColor);
            g2d.drawString(line, textX, textY);
            
            // Move to next line
            y += metrics.getHeight();
        }
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        processText();
    }

    public boolean isCentered() {
        return centered;
    }

    public void setCentered(boolean centered) {
        this.centered = centered;
    }
    
    public boolean isTextWrapping() {
        return wrapText;
    }
    
    public void setTextWrapping(boolean wrap) {
        if (this.wrapText != wrap) {
            this.wrapText = wrap;
            processText();
        }
    }
    
    @Override
    public void setWidth(int width) {
        super.setWidth(width);
        if (wrapText && width > 0) {
            processText();
        }
    }

    @Override
    public String getType() {
        return "Label";
    }
}