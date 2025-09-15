package tomato.entity;

import tomato.Game;
import tomato.core.Utils;
import tomato.core.World;
import tomato.vfx.DynamicLightSource;

import java.awt.*;

public class Projectile extends Entity implements DynamicLightSource {

    private final Direction shootDirection;
    private final Entity shooter;
    private final int damage;

    public Projectile(double x, double y, Entity shooter, Direction direction) {
        this(x, y, shooter, direction, 10); // Default damage amount
    }

    public Projectile(double x, double y, Entity shooter, Direction direction, int damage) {
        super(x, y);
        World.WORLD.getVFXManager().getLighting().addLightSource(this);
        this.entityType = EntityType.REGULAR_PROJECTILE;
        this.speed = 500;
        this.shooter = shooter;
        this.damage = damage;
        this.shouldDrawHitbox = false;
        this.currentSprite = Utils.loadQOI("/tomato/assets/projectile.qoi");
        shootDirection = direction;
        
        // Set up collision action for damage dealing
        this.setCollisionAction(new ProjectileDamageAction(damage, shooter));
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
                if (isInUnloadedChunk()) {
                    markForRemoval();
                }
                setX(this.x - adjustedSpeed);
                break;
        }



        // Also check collision with player entity (stored separately)
        if (World.PLAYER_ENTITY != null && World.PLAYER_ENTITY != shooter &&
                this.getHitbox().intersects(World.PLAYER_ENTITY.getHitbox())) {
            // Use collision action to handle player collision
            if (collisionAction != null) {
                collisionAction.onCollide(this, World.PLAYER_ENTITY);
            }
        }
    }

    @Override
    public void markForRemoval() {
        World.WORLD.getVFXManager().getLighting().removeLightSource(this);
        super.markForRemoval();
    }

    @Override
    public int getLightX() {
        return (int) this.getHitbox().getCenterX();
    }

    @Override
    public int getLightY() {
        return (int) this.getHitbox().getCenterY();
    }

    @Override
    public int getLightRadius() {
        return 15;
    }

    @Override
    public float getLightStrength() {
        return 1.0f;
    }
}
