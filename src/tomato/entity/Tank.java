package tomato.entity;

import tomato.core.Utils;

public class Tank extends Entity{

    public Tank(double x, double y) {
        super(x, y);
        this.currentDirection = Direction.SOUTH;
        this.currentSprite = Utils.loadQOI("/tomato/assets/tank.qoi");
//        this.shouldDrawHitbox = true;
    }

    protected void moveForward(double distance) {
        // Store original position
        double originalX = this.x;
        double originalY = this.y;
        
        // Calculate new position
        switch (currentDirection) {
            case NORTH: setY(this.y - distance); break;
            case SOUTH: setY(this.y + distance); break;
            case EAST:  setX(this.x + distance); break;
            case WEST:  setX(this.x - distance); break;
        }
        
        // Check for collision with other entities (excluding projectiles) OR out of bounds
        if (hasCollisionWithNonProjectiles()) {
            // Revert to original position if collision or bounds violation detected
            setX(originalX);
            setY(originalY);
        }
    }

    protected void shoot()
    {
        // Calculate center of the tank sprite
        double centerX = x + (currentSprite != null ? currentSprite.getWidth() / 4.0 : 0);
        double centerY = y + (currentSprite != null ? currentSprite.getHeight() / 4.0 : 0);
        Projectile.shootProjectile(centerX, centerY, this, currentDirection);
    }

}
