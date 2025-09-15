package tomato.entity;

import tomato.Game;
import tomato.core.Utils;
import tomato.core.World;

import java.awt.*;

public class Projectile extends Entity {

    private final Direction shootDirection;
    private final Entity shooter;
    private final int damage;

    public Projectile(double x, double y, Entity shooter, Direction direction) {
        this(x, y, shooter, direction, 10); // Default damage amount
    }

    public Projectile(double x, double y, Entity shooter, Direction direction, int damage) {
        super(x, y);
        this.entityType = EntityType.REGULAR_PROJECTILE;
        this.speed = 500;
        this.shooter = shooter;
        this.damage = damage;
        this.shouldDrawHitbox = false;
        this.currentSprite = Utils.loadQOI("/tomato/assets/projectile.qoi");
        shootDirection = direction;
    }

    public static void shootProjectile(double x, double y, Entity shooter, Direction direction) {
        Projectile projectile = new Projectile(x, y, shooter, direction);
        World.WORLD.getWorldEntities().add(projectile);
    }

    public static void shootProjectile(double x, double y, Entity shooter, Direction direction, int damage) {
        Projectile projectile = new Projectile(x, y, shooter, direction, damage);
        World.WORLD.getWorldEntities().add(projectile);
    }

    public int getDamage() {
        return damage;
    }


    @Override
    public void update() {
        super.update();
        this.currentDirection = shootDirection;
        rotate(currentDirection);
        double adjustedSpeed = this.speed * Game.GAME_LOOP.getDeltaTime();

        // Move projectile
        switch (currentDirection) {
            case NORTH:
                setY(this.y - adjustedSpeed);
                break;
            case SOUTH:
                setY(this.y + adjustedSpeed);
                break;
            case EAST:
                setX(this.x + adjustedSpeed);
                break;
            case WEST:
                setX(this.x - adjustedSpeed);
                break;
        }

        if (isInUnloadedChunk()) {
            markForRemoval();
        }

        // Check for collision and deal damage if hit
        Entity hitEntity = getFirstEntityHit();
        if (hitEntity != null && hitEntity != shooter) {
            // Deal damage to the hit entity
            hitEntity.takeDamage(this.damage);
            this.markForRemoval();
            return;
        }

        // Also check collision with player entity (stored separately)
        if (World.PLAYER_ENTITY != null && World.PLAYER_ENTITY != shooter &&
                this.getHitbox().intersects(World.PLAYER_ENTITY.getHitbox())) {
            // Deal damage to the player
            World.PLAYER_ENTITY.takeDamage(this.damage);
            this.markForRemoval();
        }


//        // Remove projectile if it goes off screen (optional bounds check)
//        if (isProjectileOutOfBounds()) {
//            this.markForRemoval();
//        }
    }

//    /**
//     * Check if projectile is out of world bounds
//     * Projectiles are allowed to go slightly beyond world boundaries before removal
//     */
//    private boolean isProjectileOutOfBounds() {
//        // Get world dimensions from the chunk system
//        final int WORLD_WIDTH = World.WORLD.getWorldWidth();
//        final int WORLD_HEIGHT = World.WORLD.getWorldHeight();
//
//        // Allow projectiles to go slightly beyond world boundaries before removal
//        final int BUFFER = 50;
//        return x < -BUFFER || x > WORLD_WIDTH + BUFFER ||
//               y < -BUFFER || y > WORLD_HEIGHT + BUFFER;
//    }


}
