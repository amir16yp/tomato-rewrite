package tomato.entity;

@FunctionalInterface
public interface CollisionAction {
    void onCollide(Entity self, Entity other);
}