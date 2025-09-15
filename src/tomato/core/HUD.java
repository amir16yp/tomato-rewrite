package tomato.core;

import tomato.Game;
import tomato.entity.Entity;
import tomato.entity.Tank;

import java.awt.*;
import java.awt.image.BufferedImage;

public class HUD
{
    private static final BufferedImage HEART_SPRITE = Utils.loadQOI("/tomato/assets/heart.qoi");
    private Entity playerEntity = World.PLAYER_ENTITY;
    public HUD()
    {

    }

    public void render(Graphics2D g)
    {
        renderPlayerHP(g, 32, Game.HEIGHT - 32);
    }

    private void renderPlayerHP(Graphics2D g, int x, int y)
    {
        int health = playerEntity.getHealth();
        int maxHearts = (int)Math.ceil(20 / 2.0); // 20 hp = 10 hearts

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
