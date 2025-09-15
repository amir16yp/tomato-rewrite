package tomato.entity;

import tomato.Game;
import tomato.core.Utils;
import tomato.core.World;

import java.awt.Rectangle;

public class Projectile extends Entity{

    private Direction shootDirection;
    private Entity shooter;
    public Projectile(double x, double y, Entity shooter, Direction direction) {
        super(x, y);
        this.speed = 200;
        this.shooter = shooter;
        this.shouldDrawHitbox = false;
        this.currentSprite = Utils.loadQOI("/tomato/assets/projectile.qoi");
        shootDirection = direction;
    }

    public static void shootProjectile(double x, double y, Entity shooter, Direction direction)
    {
        Projectile projectile = new Projectile(x, y, shooter, direction);
        World.WORLD.getWorldEntities().add(projectile);
    }


    @Override
    public void update() {
        super.update();
        this.currentDirection = shootDirection;
        rotate(currentDirection);
        double adjustedSpeed = this.speed * Game.GAME_LOOP.getDeltaTime();
        
        // Move projectile
        switch (currentDirection) {
            case NORTH: setY(this.y - adjustedSpeed); break;
            case SOUTH: setY(this.y + adjustedSpeed); break;
            case EAST:  setX(this.x + adjustedSpeed); break;
            case WEST:  setX(this.x - adjustedSpeed); break;
        }

        // Check for collision and mark for removal if hit
        Entity hitEntity = getFirstEntityHit();
        if (hitEntity != null && hitEntity != shooter) {
            // Handle collision (you can add damage logic here)
            this.markForRemoval();
        }
        
        // Remove projectile if it goes off screen (optional bounds check)
        if (isOutOfBounds()) {
            this.markForRemoval();
        }
    }
    
    /**
     * Check if projectile is out of world bounds
     */
    private boolean isOutOfBounds() {
        // Adjust these bounds based on your world size
        return x < -50 || x > 1100 || y < -50 || y > 1100;
    }
    
    /**
     * Override getHitbox for smaller, more precise projectile collision
     */
    @Override
    public Rectangle getHitbox() {
        if (currentSprite == null) {
            return new Rectangle((int)x, (int)y, 4, 4); // Small default hitbox
        }
        
        // Use cached hitbox if available
        if (cachedHitbox != null && !hitboxNeedsUpdate) {
            cachedHitbox.x = (int)x;
            cachedHitbox.y = (int)y;
            return cachedHitbox;
        }
        
        // Projectiles use smaller hitbox for better gameplay
        int width = Math.max(4, currentSprite.getWidth() / 2);
        int height = Math.max(4, currentSprite.getHeight() / 2);
        
        // Center the smaller hitbox
        int offsetX = (currentSprite.getWidth() - width) / 2;
        int offsetY = (currentSprite.getHeight() - height) / 2;
        
        cachedHitbox = new Rectangle((int)x + offsetX, (int)y + offsetY, width, height);
        hitboxNeedsUpdate = false;
        
        return cachedHitbox;
    }
}
