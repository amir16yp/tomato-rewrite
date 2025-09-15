package tomato.entity;

import tomato.core.World;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Entity
{
    protected double x;
    protected double y;
    protected BufferedImage currentSprite;
    protected Direction currentDirection;
    protected double speed = 25.0;
    protected BufferedImage[] rotatedSprites;
    protected boolean shouldDrawHitbox = false;
    protected boolean markedForRemoval = false;
    protected Rectangle cachedHitbox = null;
    protected boolean hitboxNeedsUpdate = true;
    protected int spriteWidth = 0;
    protected int spriteHeight = 0;
    protected int health = 20;

    // TODO: enemy tanks should not exit their spawn chunks
    // TODO: projectiles should be marked for removal after it traveled through an entire chunk without hitting anything
    // TODO: use GameState to show different screens (death screen, pause)
    // TODO: add different enemy types and sprite variants (damage, etc)

    public void takeDamage(int damage) {
        this.health -= damage;
        if (this.health <= 0) {
            markForRemoval();
        }
    }
    
    public int getHealth() {
        return health;
    }
    
    public void setHealth(int health) {
        this.health = health;
    }

    public void markForRemoval()
    {
        this.markedForRemoval = true;
    }

    public boolean isMarkedForRemoval() {
        return markedForRemoval;
    }

    public Entity(double x, double y) {
        this.x = x;
        this.y = y;
        this.currentDirection = Direction.SOUTH;
    }


    protected void createRotatedSprites(BufferedImage startingSprite) {
        rotatedSprites = new BufferedImage[4];

        // SOUTH (0 degrees) - original sprite
        rotatedSprites[0] = startingSprite;

        // EAST (90 degrees clockwise)
        rotatedSprites[1] = rotateImage(startingSprite, Math.PI / 2);

        // NORTH (180 degrees)
        rotatedSprites[2] = rotateImage(startingSprite, Math.PI);

        // WEST (270 degrees clockwise)s
        rotatedSprites[3] = rotateImage(startingSprite, 3 * Math.PI / 2);
    }

    public void update() {
        if (rotatedSprites == null)
        {
            createRotatedSprites(this.currentSprite);
            updateCurrentSprite();
        }
    }

    public boolean intersectsEntity(Entity otherEntity)
    {
        if (otherEntity == this) return false;
        return otherEntity.getHitbox().intersects(this.getHitbox());
    }
    
    /**
     * Fast distance-based collision check before expensive hitbox intersection
     */
    public boolean couldIntersectEntity(Entity otherEntity)
    {
        if (otherEntity == this) return false;
        
        // Quick distance check first
        double dx = this.x - otherEntity.x;
        double dy = this.y - otherEntity.y;
        double maxDistance = 100; // Adjust based on your largest entity size
        
        if (dx * dx + dy * dy > maxDistance * maxDistance) {
            return false;
        }
        
        return intersectsEntity(otherEntity);
    }
//
//    public Rectangle getHitbox() {
//        if (currentSprite == null) {
//            return new Rectangle((int)x, (int)y, 0, 0);
//        }
//
//        // Use cached hitbox if position hasn't changed and hitbox is valid
//        if (cachedHitbox != null && !hitboxNeedsUpdate) {
//            cachedHitbox.x = (int)x;
//            cachedHitbox.y = (int)y;
//            return cachedHitbox;
//        }
//
//        // For performance, use simple bounding box for most entities
//        // Override this method in specific entities that need pixel-perfect collision
//        int width = currentSprite.getWidth();
//        int height = currentSprite.getHeight();
//
//        cachedHitbox = new Rectangle((int)x, (int)y, width, height);
//        hitboxNeedsUpdate = false;
//
//        return cachedHitbox;
//    }
    
    // Pixel-perfect hitbox calculation - only use when necessary
    protected Rectangle getHitbox() {
        if (currentSprite == null) {
            return new Rectangle((int)x, (int)y, 0, 0);
        }

        int width = currentSprite.getWidth();
        int height = currentSprite.getHeight();

        int minX = width;
        int minY = height;
        int maxX = -1;
        int maxY = -1;

        for (int yy = 0; yy < height; yy++) {
            for (int xx = 0; xx < width; xx++) {
                int pixel = currentSprite.getRGB(xx, yy);
                int alpha = (pixel >> 24) & 0xff;
                if (alpha > 0) { // non-transparent pixel
                    if (xx < minX) minX = xx;
                    if (yy < minY) minY = yy;
                    if (xx > maxX) maxX = xx;
                    if (yy > maxY) maxY = yy;
                }
            }
        }

        if (maxX < minX || maxY < minY) {
            return new Rectangle((int)x, (int)y, 0, 0);
        }

        int rectWidth = (maxX - minX) + 1;
        int rectHeight = (maxY - minY) + 1;

        return new Rectangle(
                (int)x + minX,
                (int)y + minY,
                rectWidth,
                rectHeight
        );
    }

    private Point getCenteroid()
    {
        Rectangle hitbox = getHitbox();
        return new Point((int) hitbox.getCenterX(), (int) hitbox.getCenterY());
    }


    private BufferedImage rotateImage(BufferedImage image, double angle) {
        int width = image.getWidth();
        int height = image.getHeight();

        // Create a new image with the same dimensions
        BufferedImage rotated = new BufferedImage(width, height, image.getType());
        Graphics2D g2d = rotated.createGraphics();

        // Set rendering hints for better quality
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Create transform for rotation around center
        AffineTransform transform = new AffineTransform();
        transform.translate(width / 2.0, height / 2.0);
        transform.rotate(angle);
        transform.translate(-width / 2.0, -height / 2.0);

        g2d.setTransform(transform);
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        return rotated;
    }

    private void updateCurrentSprite() {
        BufferedImage oldSprite = this.currentSprite;
        
        switch (currentDirection) {
            case SOUTH:
                this.currentSprite = rotatedSprites[0];
                break;
            case WEST:
                this.currentSprite = rotatedSprites[1];
                break;
            case NORTH:
                this.currentSprite = rotatedSprites[2];
                break;
            case EAST:
                this.currentSprite = rotatedSprites[3];
                break;
        }
        
        // Mark hitbox for update if sprite changed
        if (oldSprite != this.currentSprite) {
            hitboxNeedsUpdate = true;
        }
    }

    public void rotate(Direction direction)
    {
        this.currentDirection = direction;
        updateCurrentSprite();
    }

    private void drawHitbox(Graphics g)
    {
        Color originColor = g.getColor();
        g.setColor(Color.RED);
        Rectangle hitbox = getHitbox();
        g.drawRect(hitbox.x, hitbox.y, hitbox.width, hitbox.height);
        g.setColor(originColor);
    }

    public void render(Graphics2D g) {
        if (currentSprite != null) {
            g.drawImage(currentSprite, (int)x, (int)y, null);
            if (shouldDrawHitbox)
            {
                drawHitbox(g);
            }
        } else {
            // Draw a simple colored rectangle as placeholder
            g.setColor(Color.RED);
            g.fillRect((int)x, (int)y, 10, 10);
        }
    }

    /**
     * Get entities that intersect with this entity - optimized for projectiles
     */
    public ArrayList<Entity> getEntitiesIntersect()
    {
        ArrayList<Entity> entitiesIntersected = new ArrayList<>();
        Rectangle hitbox = this.getHitbox();
        
        for (Entity entity : World.WORLD.getWorldEntities())
        {
            if (entity != this && entity.getHitbox().intersects(hitbox))
            {
                entitiesIntersected.add(entity);
            }
        }
        return entitiesIntersected;
    }
    
    /**
     * Fast collision check - returns first entity hit (optimized for projectiles)
     */
    public Entity getFirstEntityHit()
    {
        Rectangle hitbox = this.getHitbox();
        
        for (Entity entity : World.WORLD.getWorldEntities())
        {
            if (entity != this && entity.getHitbox().intersects(hitbox))
            {
                return entity;
            }
        }
        return null;
    }

    
    /**
     * Check if this entity intersects with any non-projectile entity
     * Used for collision prevention in movement
     */
    public boolean hasCollisionWithNonProjectiles()
    {
        Rectangle hitbox = this.getHitbox();
        for (Entity entity : World.WORLD.getWorldEntities())
        {
            if (entity != this && !(entity instanceof Projectile) && entity.getHitbox().intersects(hitbox))
            {
                return true;
            }
        }
        
        // Also check collision with player entity if this is not the player
        if (this != World.PLAYER_ENTITY && !(World.PLAYER_ENTITY instanceof Projectile))
        {
            if (World.PLAYER_ENTITY.getHitbox().intersects(hitbox))
            {
                return true;
            }
        }
        
        return false;
    }
    
//    /**
//     * Check if this entity would be out of world bounds at the given position
//     * Projectiles are exempt from bounds checking
//     */
//    public boolean wouldBeOutOfBounds(double x, double y) {
//        // Projectiles can go out of bounds
//        if (this instanceof Projectile) {
//            return false;
//        }
//
//        // Get world dimensions from the chunk system
//        final int WORLD_WIDTH = World.WORLD.getWorldWidth();
//        final int WORLD_HEIGHT = World.WORLD.getWorldHeight();
//
//        Rectangle hitbox = getHitbox();
//        int entityWidth = hitbox.width;
//        int entityHeight = hitbox.height;
//
//        // Check if entity would be outside world bounds
//        return x < 0 || y < 0 ||
//               x + entityWidth > WORLD_WIDTH ||
//               y + entityHeight > WORLD_HEIGHT;
//    }
//


    public double getX() { return x; }
    public double getY() { return y; }
    public void setX(double x) { 
        this.x = x; 
        hitboxNeedsUpdate = true;
    }
    
    public void setY(double y) { 
        this.y = y; 
        hitboxNeedsUpdate = true;
    }
    
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
        hitboxNeedsUpdate = true;
    }

    protected void logInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Entity Info ===\n");
        sb.append("Class: ").append(this.getClass().getSimpleName()).append("\n");
        sb.append("Position: (").append(x).append(", ").append(y).append(")\n");
        sb.append("Direction: ").append(currentDirection).append("\n");
        sb.append("Speed: ").append(speed).append("\n");

        if (currentSprite != null) {
            sb.append("Sprite Size: ")
                    .append(currentSprite.getWidth())
                    .append("x")
                    .append(currentSprite.getHeight())
                    .append("\n");
        } else {
            sb.append("Sprite: null\n");
        }

        Rectangle hb = getHitbox();
        sb.append("Hitbox: [x=")
                .append(hb.x).append(", y=")
                .append(hb.y).append(", w=")
                .append(hb.width).append(", h=")
                .append(hb.height).append("]\n");

//        sb.append("Marked for removal: ").append(markedForRemoval).append("\n");
        sb.append("===================\n");

        System.out.println(sb.toString());
    }
}
