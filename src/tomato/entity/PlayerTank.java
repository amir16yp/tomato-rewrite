package tomato.entity;

import tomato.Game;
import tomato.core.GameState;
import tomato.core.SpriteCache;
import tomato.core.Utils;
import tomato.core.World;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Random;

public class PlayerTank extends Tank {

    public PlayerTank() {
        super(200, 200);
        this.currentSprite = SpriteCache.queryCache(EntityType.PLAYER_TANK, Direction.SOUTH);
        this.health = 50;
        this.entityType = EntityType.PLAYER_TANK;
        this.speed = 100.0;
        // Game.KEY_REGISTRY.onKeyPressed(KeyEvent.VK_ESCAPE, () -> {
        //     if (GameState.CURRENT_STATE == GameState.GameStateType.PLAY)
        //     {
        //         GameState.CURRENT_STATE = GameState.GameStateType.PAUSED;
        //     }
        // });
        Game.KEY_REGISTRY.onKeyPressed(KeyEvent.VK_A, () -> {
            turnLeft();
        });
        Game.KEY_REGISTRY.onKeyPressed(KeyEvent.VK_D, () -> {
            turnRight();
        });
        Game.KEY_REGISTRY.onKeyPressed(KeyEvent.VK_SPACE, () -> {
            shoot(EntityType.GOLD_PROJECTILE);
        });

        Game.KEY_REGISTRY.onKeyPressed(KeyEvent.VK_ESCAPE, () -> {
            GameState.Pause();
        });

        Game.KEY_REGISTRY.onKeyPressed(KeyEvent.VK_X, () -> {
            // debug key
            Point spawnPoint = this.getChunk().getRandomWorldCoordinate();
            if (new Random().nextBoolean())
            {
                World.WORLD.spawnRedEnemy(spawnPoint.x, spawnPoint.y);
            } else {
                World.WORLD.spawnLandmine(spawnPoint.x, spawnPoint.y);
            }
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

    private void turnLeft() {
        switch (currentDirection) {
            case NORTH:
                rotate(Direction.WEST);
                break;
            case WEST:
                rotate(Direction.SOUTH);
                break;
            case SOUTH:
                rotate(Direction.EAST);
                break;
            case EAST:
                rotate(Direction.NORTH);
                break;
        }
    }

    private void turnRight() {
        switch (currentDirection) {
            case NORTH:
                rotate(Direction.EAST);
                break;
            case EAST:
                rotate(Direction.SOUTH);
                break;
            case SOUTH:
                rotate(Direction.WEST);
                break;
            case WEST:
                rotate(Direction.NORTH);
                break;
        }
    }
}
