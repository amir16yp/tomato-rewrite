package tomato.core;

import tomato.Game;
import tomato.entity.Entity;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class HUD {
    private static final BufferedImage HEART_SPRITE = Utils.loadQOI("/tomato/assets/heart.qoi");
    private static final Font MONO_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 14);

    // TODO add a static field of a monospaced font instance

    public HUD() {

    }

    public void render(Graphics2D g) {
        if (World.PLAYER_ENTITY != null) {
            renderPlayerHP(g, 32, Game.HEIGHT - 32);
        }
        renderStats(g);
//        renderHitboxes(g);
    }

    private void renderHitboxes(Graphics2D g) {
        AffineTransform original = g.getTransform();
        Game.RENDERER.getCamera().applyTransform(g, Game.WIDTH, Game.HEIGHT);

        g.setColor(Color.RED);
        for (Entity entity : World.WORLD.getWorldEntities()) {
            Rectangle cage = entity.getHitbox();
            if (cage != null) {
                g.drawRect(cage.x, cage.y, cage.width, cage.height);
            }
        }

        g.setTransform(original);
    }

    private void drawMonospace(Graphics2D g, int x, int y, String str, Color color) {
        g.setFont(MONO_FONT);
        g.setColor(color);
        g.drawString(str, x, y);
    }

    private void renderStats(Graphics2D g) {
        String sb = "FPS: " + Game.GAME_LOOP.getFPS() +
                " | Δt: " + String.format("%.4f", Game.GAME_LOOP.getDeltaTime()) +
                " | Entities: " + World.WORLD.getWorldEntities().size() +
                " | Chunks: " + World.WORLD.getLoadedChunkCount();

        drawMonospace(g, 16, 24, sb, Color.WHITE);
    }


    private void renderPlayerHP(Graphics2D g, int x, int y) {

        int health = World.PLAYER_ENTITY.getHealth();
        int maxHearts = (int) Math.ceil(20 / 2.0); // 20 hp = 10 hearts

        int fullHearts = health / 2;
        boolean hasHalf = (health % 2 == 1);

        for (int i = 0; i < maxHearts; i++) {
            int drawX = x + (i * 20);

            if (i < fullHearts) {
                // Full heart
                g.drawImage(HEART_SPRITE, drawX, y, null);

            } else if (i == fullHearts && hasHalf) {
                // Half heart (draw left half of heart)
                int halfW = HEART_SPRITE.getWidth() / 2;
                g.drawImage(
                        HEART_SPRITE,
                        drawX, y, drawX + halfW, y + HEART_SPRITE.getHeight(),
                        0, 0, halfW, HEART_SPRITE.getHeight(),
                        null
                );

            } else {
                // Empty heart: faded heart using transparency
                Composite old = g.getComposite();
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
                g.drawImage(HEART_SPRITE, drawX, y, null);
                g.setComposite(old);
            }
        }
    }

}
