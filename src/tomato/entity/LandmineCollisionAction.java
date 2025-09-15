package tomato.entity;

import tomato.core.World;

import java.awt.*;

public class LandmineCollisionAction implements CollisionAction {
    private final int explosionDamage;
    
    public LandmineCollisionAction() {
        this(50); // Default explosion damage
    }
    
    public LandmineCollisionAction(int explosionDamage) {
        this.explosionDamage = explosionDamage;
    }
    
    @Override
    public void onCollide(Entity self, Entity other) {
        // Don't explode on collision with other landmines or projectiles
        if (other instanceof LandmineEntity || other instanceof Projectile) {
            return;
        }
        Rectangle hitbox = self.getHitbox();
        World.WORLD.getVFXManager().createExplosion((int) hitbox.getCenterX(), (int) hitbox.getCenterY(), 50, 2.0f);
        other.takeDamage(explosionDamage);
        
        // Remove the landmine after explosion
        self.markForRemoval();
        
//        System.out.println("Landmine exploded! Dealt " + explosionDamage + " damage to " + other.getClass().getSimpleName());
    }
}
