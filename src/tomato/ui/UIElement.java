package tomato.ui;


import tomato.Game;

import java.awt.*;
import java.awt.image.BufferedImage;

public class UIElement {
    private static int totalElementCount = 0;
    protected Font font;
    protected Color backgroundColor;
    protected Color highlightColor;
    protected Color textColor;
    private int x;
    private int y;
    private int width;
    private int height;
    private boolean visible;
    private boolean selected = false;
    private boolean shouldDrawBackgroundColor = true;

    // New fields for background image
    private BufferedImage backgroundImage = null;
    private boolean stretchImage = true;
    private boolean tileImage = false;
    private boolean blurBackground = false;
    private int blurRadius = 5; // Default blur radius

    // Cache for blurred images to avoid recalculating every frame
    private BufferedImage cachedBlurredImage = null;
    private int cachedBlurRadius = -1;
    private BufferedImage cachedOriginalImage = null;

    public UIElement(int x, int y, int width, int height, boolean visible) {
        //logger.addPrefix("E" + totalElementCount);
        totalElementCount++;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.visible = visible;
        //logger.info("Created UIElement with x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + ", visible=" + visible);
    }

    public String getType() {
        return "UIElement";
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setVisible(boolean visible) {
        //logger.Log("Setting visible to " + visible);
        this.visible = visible;
    }

    public void update() {

    }

    public void draw(Graphics g) {
        if (isVisible()) {
            g.setFont(font);
            
            // Draw background image if set
            if (backgroundImage != null) {
                drawBackgroundImage(g);
            }
            // Otherwise draw background color if enabled
            else if (shouldDrawBackgroundColor()) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(getBackgroundColor());
                g2d.fillRect(getX(), getY(), getWidth(), getHeight());
            }
        }
    }

    /**
     * Draws the background image according to the current settings
     */
    protected void drawBackgroundImage(Graphics g) {
        if (backgroundImage == null) return;
        
        // Create a copy of the image if we need to apply blur
        BufferedImage imageToDraw = backgroundImage;
        if (blurBackground) {
            // Check if we can use cached blurred image
            if (cachedBlurredImage != null && 
                cachedBlurRadius == blurRadius && 
                cachedOriginalImage == backgroundImage) {
                imageToDraw = cachedBlurredImage;
            } else {
                // Need to create new blurred image
                imageToDraw = applyBlur(backgroundImage, blurRadius);
                // Cache the result
                cachedBlurredImage = imageToDraw;
                cachedBlurRadius = blurRadius;
                cachedOriginalImage = backgroundImage;
            }
        }
        
        // Save original rendering hints
        Object originalInterpolation = null;
        if (g instanceof Graphics2D) {
            Graphics2D g2d = (Graphics2D) g;
            originalInterpolation = g2d.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
            // Set nearest neighbor interpolation for pixel-perfect scaling
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        }
        
        if (stretchImage) {
            // Stretch the image to fill the element
            g.drawImage(imageToDraw, getX(), getY(), getWidth(), getHeight(), null);
        } else if (tileImage) {
            // Tile the image to fill the element
            int imgWidth = imageToDraw.getWidth();
            int imgHeight = imageToDraw.getHeight();
            
            for (int y = getY(); y < getY() + getHeight(); y += imgHeight) {
                for (int x = getX(); x < getX() + getWidth(); x += imgWidth) {
                    g.drawImage(imageToDraw, x, y, null);
                }
            }
        } else {
            // Center the image
            int x = getX() + (getWidth() - imageToDraw.getWidth()) / 2;
            int y = getY() + (getHeight() - imageToDraw.getHeight()) / 2;
            g.drawImage(imageToDraw, x, y, null);
        }
        
        // Restore original rendering hints
        if (g instanceof Graphics2D) {
            Graphics2D g2d = (Graphics2D) g;
            if (originalInterpolation != null) {
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, originalInterpolation);
            } else {
                g2d.getRenderingHints().remove(RenderingHints.KEY_INTERPOLATION);
            }
        }
    }

    /**
     * Applies an optimized blur to the given image using sliding window technique
     * @param image The image to blur
     * @param radius The blur radius
     * @return A new blurred image
     */
    private BufferedImage applyBlur(BufferedImage image, int radius) {
        if (radius <= 0) return image;
        
        // Clamp radius to reasonable values for performance
        radius = Math.min(radius, 20);
        
        int width = image.getWidth();
        int height = image.getHeight();
        
        // Get source pixels as array for faster access
        int[] sourcePixels = new int[width * height];
        image.getRGB(0, 0, width, height, sourcePixels, 0, width);
        
        // Create arrays for intermediate and final results
        int[] tempPixels = new int[width * height];
        int[] resultPixels = new int[width * height];
        
        // Pre-calculate kernel size
        int kernelSize = radius * 2 + 1;
        
        // Horizontal pass with sliding window optimization
        for (int y = 0; y < height; y++) {
            int rowOffset = y * width;
            
            // Initialize sliding window for first pixel
            int sumA = 0, sumR = 0, sumG = 0, sumB = 0;
            
            // Fill initial window
            for (int i = -radius; i <= radius; i++) {
                int px = Math.max(0, Math.min(width - 1, i));
                int pixel = sourcePixels[rowOffset + px];
                
                sumA += (pixel >> 24) & 0xff;
                sumR += (pixel >> 16) & 0xff;
                sumG += (pixel >> 8) & 0xff;
                sumB += pixel & 0xff;
            }
            
            // Process first pixel
            tempPixels[rowOffset] = ((sumA / kernelSize) << 24) | 
                                   ((sumR / kernelSize) << 16) | 
                                   ((sumG / kernelSize) << 8) | 
                                   (sumB / kernelSize);
            
            // Slide window across remaining pixels in row
            for (int x = 1; x < width; x++) {
                // Remove leftmost pixel from window
                int leftPx = Math.max(0, x - radius - 1);
                int leftPixel = sourcePixels[rowOffset + leftPx];
                sumA -= (leftPixel >> 24) & 0xff;
                sumR -= (leftPixel >> 16) & 0xff;
                sumG -= (leftPixel >> 8) & 0xff;
                sumB -= leftPixel & 0xff;
                
                // Add rightmost pixel to window
                int rightPx = Math.min(width - 1, x + radius);
                int rightPixel = sourcePixels[rowOffset + rightPx];
                sumA += (rightPixel >> 24) & 0xff;
                sumR += (rightPixel >> 16) & 0xff;
                sumG += (rightPixel >> 8) & 0xff;
                sumB += rightPixel & 0xff;
                
                // Store averaged result
                tempPixels[rowOffset + x] = ((sumA / kernelSize) << 24) | 
                                          ((sumR / kernelSize) << 16) | 
                                          ((sumG / kernelSize) << 8) | 
                                          (sumB / kernelSize);
            }
        }
        
        // Vertical pass with sliding window optimization
        for (int x = 0; x < width; x++) {
            // Initialize sliding window for first pixel
            int sumA = 0, sumR = 0, sumG = 0, sumB = 0;
            
            // Fill initial window
            for (int i = -radius; i <= radius; i++) {
                int py = Math.max(0, Math.min(height - 1, i));
                int pixel = tempPixels[py * width + x];
                
                sumA += (pixel >> 24) & 0xff;
                sumR += (pixel >> 16) & 0xff;
                sumG += (pixel >> 8) & 0xff;
                sumB += pixel & 0xff;
            }
            
            // Process first pixel
            resultPixels[x] = ((sumA / kernelSize) << 24) | 
                             ((sumR / kernelSize) << 16) | 
                             ((sumG / kernelSize) << 8) | 
                             (sumB / kernelSize);
            
            // Slide window down remaining pixels in column
            for (int y = 1; y < height; y++) {
                // Remove topmost pixel from window
                int topPy = Math.max(0, y - radius - 1);
                int topPixel = tempPixels[topPy * width + x];
                sumA -= (topPixel >> 24) & 0xff;
                sumR -= (topPixel >> 16) & 0xff;
                sumG -= (topPixel >> 8) & 0xff;
                sumB -= topPixel & 0xff;
                
                // Add bottommost pixel to window
                int bottomPy = Math.min(height - 1, y + radius);
                int bottomPixel = tempPixels[bottomPy * width + x];
                sumA += (bottomPixel >> 24) & 0xff;
                sumR += (bottomPixel >> 16) & 0xff;
                sumG += (bottomPixel >> 8) & 0xff;
                sumB += bottomPixel & 0xff;
                
                // Store averaged result
                resultPixels[y * width + x] = ((sumA / kernelSize) << 24) | 
                                            ((sumR / kernelSize) << 16) | 
                                            ((sumG / kernelSize) << 8) | 
                                            (sumB / kernelSize);
            }
        }
        
        // Create result image and set pixels
        BufferedImage blurredImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        blurredImage.setRGB(0, 0, width, height, resultPixels, 0, width);
        
        return blurredImage;
    }

    public boolean containsPoint(int x, int y) {
        // Calculate scale factors
        double scaleX = (double) Game.RENDERER.getWidth() / Game.WIDTH;
        double scaleY = (double) Game.RENDERER.getHeight() / Game.HEIGHT;

        // Adjust mouse coordinates for scale
        int scaledX = (int) (x / scaleX);
        int scaledY = (int) (y / scaleY);

        boolean hit = scaledX >= getX() && scaledX <= getX() + getWidth() &&
                scaledY >= getY() && scaledY <= getY() + getHeight();
        return hit;
    }

    public boolean containsPoint(int x, int y, int scrollOffset) {
        // Calculate scale factors
        double scaleX = (double) Game.RENDERER.getWidth() / Game.WIDTH;
        double scaleY = (double) Game.RENDERER.getHeight() / Game.HEIGHT;

        // Adjust mouse coordinates for scale
        int scaledX = (int) (x / scaleX);
        int scaledY = (int) (y / scaleY);

        // Check if the scaled coordinates are within the button bounds, accounting for scroll offset
        return scaledX >= getX() && scaledX <= getX() + getWidth() &&
                scaledY >= getY() - scrollOffset && scaledY <= getY() - scrollOffset + getHeight();
    }


    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean toSetSelected) {
        this.selected = toSetSelected;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public Color getHighlightColor() {
        return highlightColor;
    }

    public void setHighlightColor(Color highlightColor) {
        this.highlightColor = highlightColor;
    }

    public Color getTextColor() {
        return textColor;
    }

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }

    public boolean shouldDrawBackgroundColor() {
        return shouldDrawBackgroundColor;
    }

    public void setShouldDrawBackgroundColor(boolean shouldDrawBackgroundColor) {
        this.shouldDrawBackgroundColor = shouldDrawBackgroundColor;
    }

    /**
     * Sets a background image for this UI element
     * @param image The image to use as background
     */
    public void setBackgroundImage(BufferedImage image) {
        this.backgroundImage = image;
        // Clear blur cache when background image changes
        clearBlurCache();
    }

    /**
     * Gets the current background image
     * @return The background image or null if none is set
     */
    public BufferedImage getBackgroundImage() {
        return backgroundImage;
    }

    /**
     * Sets whether the background image should be stretched to fit the element
     * @param stretch True to stretch, false to maintain aspect ratio
     */
    public void setStretchBackgroundImage(boolean stretch) {
        this.stretchImage = stretch;
    }

    /**
     * Sets whether the background image should be tiled if it's smaller than the element
     * @param tile True to tile, false to draw once
     */
    public void setTileBackgroundImage(boolean tile) {
        this.tileImage = tile;
        if (tile) {
            // If we're tiling, we shouldn't stretch
            this.stretchImage = false;
        }
    }

    /**
     * Sets whether to apply a blur effect to the background image
     * @param blur True to apply blur, false for no blur
     * @param radius The blur radius (higher = more blur)
     */
    public void setBlurBackgroundImage(boolean blur, int radius) {
        this.blurBackground = blur;
        this.blurRadius = Math.max(1, radius); // Ensure radius is at least 1
        // Clear blur cache when blur settings change
        clearBlurCache();
    }

    /**
     * Sets whether to apply a blur effect with default radius
     * @param blur True to apply blur, false for no blur
     */
    public void setBlurBackgroundImage(boolean blur) {
        setBlurBackgroundImage(blur, this.blurRadius);
    }
    
    /**
     * Clears the cached blurred image, forcing it to be recalculated next time
     */
    private void clearBlurCache() {
        cachedBlurredImage = null;
        cachedBlurRadius = -1;
        cachedOriginalImage = null;
    }
}