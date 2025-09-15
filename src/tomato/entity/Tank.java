package tomato.entity;

import tomato.core.Mathf;
import tomato.core.Utils;
import tomato.core.World;
import java.awt.Rectangle;

public class Tank extends Entity {

    public Tank(double x, double y) {
        super(x, y);
        this.currentDirection = Direction.SOUTH;
        this.currentSprite = Utils.loadQOI("/tomato/assets/tank.qoi");
//        this.shouldDrawHitbox = true;
        
        // Set up collision action for tanks
    }

    protected void moveForward(double distance) {
        // Store original position
        double originalX = this.x;
        double originalY = this.y;

        // Calculate new position
        switch (currentDirection) {
            case NORTH:
                setY(this.y - distance);
                break;
            case SOUTH:
                setY(this.y + distance);
                break;
            case EAST:
                setX(this.x + distance);
                break;
            case WEST:
                setX(this.x - distance);
                break;
        }

        // Check for collision with other entities (excluding projectiles) OR out of bounds
        if (hasCollisionWithNonProjectiles()) {
            // Find the entity we collided with and trigger its collision action
            Rectangle hitbox = this.getHitbox();
            for (Entity entity : World.WORLD.getWorldEntities()) {
                if (entity != this && !(entity instanceof Projectile) && entity.getHitbox().intersects(hitbox)) {
                    if (entity.getCollisionAction() != null) {
                        entity.getCollisionAction().onCollide(entity, this);
                    }
                    break; // Only handle the first collision
                }
            }
            
            // Revert to original position if collision detected
            setX(originalX);
            setY(originalY);
        }
    }

    protected void shoot() {
        // Calculate center of the tank sprite using more precise positioning
        double centerX = x + (currentSprite != null ? currentSprite.getWidth() / 4.0 : 0);
        double centerY = y + (currentSprite != null ? currentSprite.getHeight() / 4.0 : 0);
        
        // Offset projectile spawn position based on direction for more realistic shooting
        double offsetDistance = 20.0; // Distance from tank center to gun barrel
        switch (currentDirection) {
            case NORTH:
                centerY -= offsetDistance;
                break;
            case SOUTH:
                centerY += offsetDistance;
                break;
            case EAST:
                centerX += offsetDistance;
                break;
            case WEST:
                centerX -= offsetDistance;
                break;
        }
        
        Projectile.shootProjectile(centerX, centerY, this, currentDirection);
    }

}
