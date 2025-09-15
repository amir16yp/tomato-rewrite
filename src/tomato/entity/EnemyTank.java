package tomato.entity;

import tomato.core.World;
import tomato.Game;

import java.util.Random;

public class EnemyTank extends Tank {

    private static final int THINK_INTERVAL_MS = 1000; // 1s between AI decisions
    private static final int FIRE_COOLDOWN_MS = 1500;  // 1.5s between shots

    private double thinkTimerMs = 0;
    private double fireCooldownMs = 0;
    private Random rng = new Random();

    public EnemyTank(double x, double y) {
        super(x, y);
        this.entityType = EntityType.RED_ENEMY_TANK;
        this.speed = 120;
        this.currentSprite = tomato.core.Utils.loadQOI("/tomato/assets/tank_red.qoi");
    }

    @Override
    public void update() {
        super.update();

        // use deltaTimeMillis from the GameLoop
        float dtMs = Game.GAME_LOOP.getDeltaTimeMillis();

        thinkTimerMs -= dtMs;
        if (fireCooldownMs > 0) fireCooldownMs -= dtMs;

        if (thinkTimerMs <= 0) {
            makeDecision();
            thinkTimerMs = THINK_INTERVAL_MS;
        }

        // Try moving in current direction
        double step = this.speed * Game.GAME_LOOP.getDeltaTime();
        moveForward(step);

        // The collision detection is now handled inside moveForward() method
        // No need for additional collision checking here since moveForward()
        // will automatically prevent movement into colliding positions

        tryShoot();
    }

    private void makeDecision() {
        // 30% chance to track player
        if (rng.nextDouble() < 0.3) {
            Entity player = World.PLAYER_ENTITY;
            if (Math.abs(player.getX() - this.x) > Math.abs(player.getY() - this.y)) {
                this.currentDirection = (player.getX() > this.x) ? Direction.EAST : Direction.WEST;
            } else {
                this.currentDirection = (player.getY() > this.y) ? Direction.SOUTH : Direction.NORTH;
            }
            updateSprite();
        } else {
            pickRandomDirection();
        }
    }

    private void pickRandomDirection() {
        Direction[] dirs = Direction.values();
        this.currentDirection = dirs[rng.nextInt(dirs.length)];
        updateSprite();
    }

    private void tryShoot() {
        if (fireCooldownMs > 0) return;

        Entity player = World.PLAYER_ENTITY;

        // Rough alignment check
        if ((Math.abs(player.getX() - this.x) < 10 &&
                (currentDirection == Direction.NORTH || currentDirection == Direction.SOUTH)) ||
                (Math.abs(player.getY() - this.y) < 10 &&
                        (currentDirection == Direction.EAST || currentDirection == Direction.WEST))) {

            shoot();
            fireCooldownMs = FIRE_COOLDOWN_MS;
        }
    }

    private void updateSprite() {
        super.rotate(currentDirection);
    }
}
