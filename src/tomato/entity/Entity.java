package tomato.entity;

import tomato.core.Mathf;
import tomato.core.SpriteCache;
import tomato.core.World;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Entity {
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
    protected int health = 50;
    protected EntityType entityType;
    protected boolean rotatable = true;
    protected CollisionAction collisionAction;
    protected int maxHealth = 50;
    // TODO: enemy tanks should not exit their spawn chunks
    // TODO: projectiles should be marked for removal after it traveled through an entire chunk without hitting anything
    // TODO: use GameState to show different screens (death screen, pause)
    // TODO: add different enemy types and sprite variants (damage, etc)
    // TODO: explosion VFX
    // TODO: more mechanics (armor, ammo?)

    public void setCollisionAction(CollisionAction action) {
        this.collisionAction = action;
    }

    public CollisionAction getCollisionAction() {
        return this.collisionAction;
    }

    public void takeDamage(int damage) {
        this.health -= damage;
        if (this.health <= 0) {
            markForRemoval();
        }
    }

    public World.Chunk getChunk() {
        return World.WORLD.getChunkAtWorld(x, y);
    }

    public Rectangle getSpawnCage() {
        World.Chunk chunk = getChunk();
        if (chunk == null)
        {
            return null;
        }
        return getChunk().getBounds();
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public void markForRemoval() {
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
        this.rotatedSprites = SpriteCache.getRotations(this.entityType, startingSprite);
    }

    public void update() {
        if (rotatedSprites == null && rotatable) {
            createRotatedSprites(this.currentSprite);
            updateCurrentSprite();
        }
        // Handle collisions using the collision action system
        handleCollisions();
    }

    public boolean isInUnloadedChunk() {
        return !World.WORLD.isEntityInLoadedChunk(this);
    }

    public boolean intersectsEntity(Entity otherEntity) {
        if (otherEntity == this) return false;
        return otherEntity.getHitbox().intersects(this.getHitbox());
    }


    public Rectangle getHitbox() {
        if (currentSprite == null) {
            return new Rectangle((int) x, (int) y, 0, 0);
        }

        // Use cached hitbox if available and sprite hasn't changed
        if (cachedHitbox != null && !hitboxNeedsUpdate) {
            // Return hitbox with current position + cached relative offset
            return new Rectangle(
                (int) x + cachedHitbox.x,
                (int) y + cachedHitbox.y,
                cachedHitbox.width,
                cachedHitbox.height
            );
        }

        // Try to get pre-calculated hitbox from SpriteCache first
        Rectangle spriteHitbox = null;
        if (entityType != null && currentDirection != null) {
            Rectangle preCalculated = tomato.core.SpriteCache.queryHitboxCache(entityType, currentDirection);
            if (preCalculated != null) {
                spriteHitbox = new Rectangle(
                    (int) x + preCalculated.x,
                    (int) y + preCalculated.y,
                    preCalculated.width,
                    preCalculated.height
                );
                
                // Cache the relative offset for future use
                cachedHitbox = new Rectangle(
                    preCalculated.x,  // Store relative offset
                    preCalculated.y,  // Store relative offset
                    preCalculated.width,
                    preCalculated.height
                );
            }
        }
        
        // Fallback to expensive pixel-by-pixel calculation if no pre-calculated hitbox
        if (spriteHitbox == null) {
            spriteHitbox = calculateSpriteHitbox();
            
            // Cache the relative hitbox dimensions
            if (spriteHitbox.width > 0 && spriteHitbox.height > 0) {
                cachedHitbox = new Rectangle(
                    spriteHitbox.x - (int) x,  // Store relative offset
                    spriteHitbox.y - (int) y,  // Store relative offset
                    spriteHitbox.width,
                    spriteHitbox.height
                );
            }
        }
        
        hitboxNeedsUpdate = false;
        return spriteHitbox;
    }

    private Rectangle calculateSpriteHitbox() {
        if (currentSprite == null) {
            return new Rectangle((int) x, (int) y, 0, 0);
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
            return new Rectangle((int) x, (int) y, 0, 0);
        }

        int rectWidth = (maxX - minX) + 1;
        int rectHeight = (maxY - minY) + 1;

        return new Rectangle(
                (int) x + minX,
                (int) y + minY,
                rectWidth,
                rectHeight
        );
    }

    private Point getCenteroid() {
        Rectangle hitbox = getHitbox();
        return new Point((int) hitbox.getCenterX(), (int) hitbox.getCenterY());
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

    public void rotate(Direction direction) {
        this.currentDirection = direction;
        updateCurrentSprite();
    }

    private void drawHitbox(Graphics g) {
        Color originColor = g.getColor();
        g.setColor(Color.RED);
        Rectangle hitbox = getHitbox();
        g.drawRect(hitbox.x, hitbox.y, hitbox.width, hitbox.height);
        g.setColor(originColor);
    }

    public void render(Graphics2D g) {
        if (currentSprite != null) {
            g.drawImage(currentSprite, (int) x, (int) y, null);
            if (shouldDrawHitbox) {
                drawHitbox(g);
            }
        } else {
            // Draw a simple colored rectangle as placeholder
            g.setColor(Color.RED);
            g.fillRect((int) x, (int) y, 10, 10);
        }
    }

    /**
     * Get entities that intersect with this entity - optimized using spatial grid
     */
    public ArrayList<Entity> getEntitiesIntersect() {
        return new ArrayList<>(World.WORLD.getSpatialGrid().getActualCollisions(this));
    }

    public void handleCollisions() {
        Entity hit = getFirstEntityHit();
        if (hit != null && collisionAction != null) {
            collisionAction.onCollide(this, hit);
        }
    }


    /**
     * Fast collision check - returns first entity hit (optimized using spatial grid)
     */
    public Entity getFirstEntityHit() {
        return World.WORLD.getSpatialGrid().getFirstCollision(this);
    }


    /**
     * Check if this entity intersects with any non-projectile entity
     * Used for collision prevention in movement - optimized using spatial grid
     */
    public boolean hasCollisionWithNonProjectiles() {
        return World.WORLD.getSpatialGrid().hasCollisionWithNonProjectiles(this) ||
               (this != World.PLAYER_ENTITY && 
                !(World.PLAYER_ENTITY instanceof Projectile) && 
                this.getHitbox().intersects(World.PLAYER_ENTITY.getHitbox()));
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setX(double x) {
        this.x = x;
        // Position changes don't require hitbox recalculation, just offset update
    }

    public void setY(double y) {
        this.y = y;
        // Position changes don't require hitbox recalculation, just offset update
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
        // Position changes don't require hitbox recalculation, just offset update
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

        System.out.println(sb);
    }
}
