package tomato.entity;

import tomato.core.World;

import java.awt.*;

/**
 * Collision action for projectiles that deals damage to other entities
 */
public class ProjectileDamageAction implements CollisionAction {
    private final int damage;
    private final Entity shooter;

    public ProjectileDamageAction(int damage, Entity shooter) {
        this.damage = damage;
        this.shooter = shooter;
    }

    @Override
    public void onCollide(Entity self, Entity other) {
        // Don't damage the shooter
        if (other == shooter) {
            return;
        }

        // Don't damage other projectiles
        if (other instanceof Projectile) {
            return;
        }

        Rectangle hitbox = self.getHitbox();
        // Deal damage to the hit entity
        World.WORLD.getVFXManager().createExplosion((int) hitbox.getCenterX(), (int) hitbox.getCenterY(), 25, 1.5f);
        other.takeDamage(damage);
        
        // Remove the projectile after hitting something
        self.markForRemoval();
    }
}
