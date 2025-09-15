package tomato.entity;

import tomato.Game;
import tomato.core.World;

import java.awt.event.KeyEvent;

public class PlayerTank extends Tank {

    public PlayerTank() {
        super(160, 160);
        this.speed = 100.0;
        Game.KEY_REGISTRY.onKeyPressed(KeyEvent.VK_A, () -> {turnRight();});
        Game.KEY_REGISTRY.onKeyPressed(KeyEvent.VK_D, () -> turnLeft());
        Game.KEY_REGISTRY.onKeyPressed(KeyEvent.VK_SPACE, () -> {
            // Calculate center of the tank sprite
            double centerX = x + (currentSprite != null ? currentSprite.getWidth() / 4.0 : 0);
            double centerY = y + (currentSprite != null ? currentSprite.getHeight() / 4.0 : 0);
            Projectile.shootProjectile(centerX, centerY, this, currentDirection);
        });


        Game.KEY_REGISTRY.onKeyPressed(KeyEvent.VK_X, () -> {
            // debug key
            World.WORLD.spawnRedEnemy(120, 120);
        });

    }

    @Override
    public void update() {
        super.update();

        double adjustedSpeed = speed * Game.GAME_LOOP.getDeltaTime();

        // Forward/back movement (relative to facing direction)
        if (Game.KEY_REGISTRY.isKeyPressed(KeyEvent.VK_W)) {
            moveForward(adjustedSpeed);
        }
        if (Game.KEY_REGISTRY.isKeyPressed(KeyEvent.VK_S)) {
            moveForward(-adjustedSpeed);
        }

        // Camera follows
        Game.RENDERER.getCamera().setPosition((float) getX(), (float) getY());
    }

    private void moveForward(double distance) {
        switch (currentDirection) {
            case NORTH: setY(this.y - distance); break;
            case SOUTH: setY(this.y + distance); break;
            case EAST:  setX(this.x + distance); break;
            case WEST:  setX(this.x - distance); break;
        }
    }

    private void turnLeft() {
        switch (currentDirection) {
            case NORTH: rotate(Direction.WEST); break;
            case WEST:  rotate(Direction.SOUTH); break;
            case SOUTH: rotate(Direction.EAST); break;
            case EAST:  rotate(Direction.NORTH); break;
        }
    }

    private void turnRight() {
        switch (currentDirection) {
            case NORTH: rotate(Direction.EAST); break;
            case EAST:  rotate(Direction.SOUTH); break;
            case SOUTH: rotate(Direction.WEST); break;
            case WEST:  rotate(Direction.NORTH); break;
        }
    }
}
