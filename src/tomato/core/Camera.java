package tomato.core;

import tomato.Game;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

public class Camera {
    private float x, y;
    private float zoom;
    private float targetX, targetY;
    private float targetZoom;
    private float lerpSpeed;

    public Camera() {
        this.x = 0;
        this.y = 0;
        this.zoom = 1f;
        this.targetX = 0;
        this.targetY = 0;
        this.targetZoom = 1.0f;
        this.lerpSpeed = 0.1f; // Smooth camera movement
    }

    public Camera(float x, float y, float zoom) {
        this.x = x;
        this.y = y;
        this.zoom = zoom;
        this.targetX = x;
        this.targetY = y;
        this.targetZoom = zoom;
        this.lerpSpeed = 0.1f;
    }

    /**
     * Set the camera position immediately
     */
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        this.targetX = x;
        this.targetY = y;
    }

    public Rectangle getViewBounds(int screenWidth, int screenHeight) {
        int viewW = (int) (screenWidth / zoom);
        int viewH = (int) (screenHeight / zoom);

        int left = (int) (x - viewW / 2);
        int top = (int) (y - viewH / 2);

        return new Rectangle(left, top, viewW, viewH);
    }

    /**
     * Set the target position for smooth camera movement
     */
    public void setTarget(float x, float y) {
        this.targetX = x;
        this.targetY = y;
    }

    /**
     * Set the zoom level immediately
     */
    public void setZoom(float zoom) {
        this.zoom = Math.max(0.1f, Math.min(10.0f, zoom)); // Clamp zoom
        this.targetZoom = this.zoom;
    }

    /**
     * Set the target zoom for smooth zooming
     */
    public void setTargetZoom(float zoom) {
        this.targetZoom = Math.max(0.1f, Math.min(10.0f, zoom)); // Clamp zoom
    }

    /**
     * Update camera interpolation (call this each frame)
     */
    public void update() {
        // Smooth interpolation to target position
        float lerpFactor = 1.0f - (float) Math.pow(1.0f - lerpSpeed, Game.GAME_LOOP.getDeltaTime() * 60.0f);

        x = lerp(x, targetX, lerpFactor);
        y = lerp(y, targetY, lerpFactor);
        zoom = lerp(zoom, targetZoom, lerpFactor);
    }

    private float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    /**
     * Apply camera transform to Graphics2D
     */
    public void applyTransform(Graphics2D g, int screenWidth, int screenHeight) {
        // Create camera transform
        AffineTransform cameraTransform = new AffineTransform();

        // Translate to center of screen
        cameraTransform.translate(screenWidth / 2.0, screenHeight / 2.0);

        // Apply zoom
        cameraTransform.scale(zoom, zoom);

        // Translate by camera position (negative to move world opposite to camera)
        cameraTransform.translate(-x, -y);

        // Apply the transform
        g.setTransform(cameraTransform);
    }

    /**
     * Reset Graphics2D transform to original state
     */
    public void resetTransform(Graphics2D g, AffineTransform originalTransform) {
        g.setTransform(originalTransform);
    }

    /**
     * Convert screen coordinates to world coordinates
     */
    public Point2D.Float screenToWorld(int screenX, int screenY, int screenWidth, int screenHeight) {
        // Account for screen center offset
        float worldX = (screenX - screenWidth / 2.0f) / zoom + x;
        float worldY = (screenY - screenHeight / 2.0f) / zoom + y;
        return new Point2D.Float(worldX, worldY);
    }

    /**
     * Convert world coordinates to screen coordinates
     */
    public Point worldToScreen(float worldX, float worldY, int screenWidth, int screenHeight) {
        int screenX = (int) ((worldX - x) * zoom + screenWidth / 2.0f);
        int screenY = (int) ((worldY - y) * zoom + screenHeight / 2.0f);
        return new Point(screenX, screenY);
    }

    /**
     * Focus camera on a specific entity or position
     */
    public void focusOn(float targetX, float targetY) {
        setTarget(targetX, targetY);
    }

    /**
     * Move camera by offset
     */
    public void move(float deltaX, float deltaY) {
        setTarget(targetX + deltaX, targetY + deltaY);
    }

    /**
     * Zoom in/out by factor
     */
    public void zoomBy(float factor) {
        setTargetZoom(targetZoom * factor);
    }

    // Getters
    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZoom() {
        return zoom;
    }

    public float getTargetX() {
        return targetX;
    }

    public float getTargetY() {
        return targetY;
    }

    public float getTargetZoom() {
        return targetZoom;
    }

    public void setLerpSpeed(float lerpSpeed) {
        this.lerpSpeed = Math.max(0.01f, Math.min(1.0f, lerpSpeed));
    }

    public float getLerpSpeed() {
        return lerpSpeed;
    }
}
