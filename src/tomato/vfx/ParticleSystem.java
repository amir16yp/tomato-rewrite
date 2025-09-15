package tomato.vfx;

import tomato.Game;

import java.awt.*;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class ParticleSystem implements VisualEffect {
    protected CopyOnWriteArrayList<Particle> particles;
    protected final Random random;

    public ParticleSystem() {
        particles = new CopyOnWriteArrayList<>();
        random = new Random();
    }

    @Override
    public void update() {
        float deltaTime = Game.GAME_LOOP.getDeltaTime();
        particles.removeIf(particle -> {
            particle.update(deltaTime);
            return particle.isDead();
        });
    }

    @Override
    public void draw(Graphics2D g) {
        particles.forEach(p -> p.draw(g));
    }

    @Override
    public boolean isFinished() {
        return particles.isEmpty();
    }

    public Rectangle getHitbox() {
        if (particles.isEmpty()) {
            return new Rectangle(0, 0, 0, 0);
        }

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (Particle p : particles) {
            minX = Math.min(minX, (int)p.x);
            minY = Math.min(minY, (int)p.y);
            maxX = Math.max(maxX, (int)p.x + p.size);
            maxY = Math.max(maxY, (int)p.y + p.size);
        }

        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }

    public void emit(int x, int y, Color color, int count) {
        for (int i = 0; i < count; i++) {
            particles.add(new Particle(x, y, color));
        }
    }

    protected class Particle {
        float x, y;
        float vx, vy;
        Color color;
        float life;
        int size;

        public Particle(float x, float y, Color color) {
            this.x = x;
            this.y = y;
            this.color = color;
            this.vx = random.nextFloat() * 2 - 1; // -1 to 1
            this.vy = random.nextFloat() * 2 - 1; // -1 to 1
            this.life = random.nextFloat() * 1.5f + 0.5f; // 0.5 to 2.0 seconds
            this.size = 2; // Default size, can be adjusted
        }

        public void update(float deltaTime) {
            x += vx * deltaTime * 50; // Scale velocity for visible movement
            y += vy * deltaTime * 50;
            life -= deltaTime;
        }

        public void draw(Graphics2D g) {
            g.setColor(color);
            g.fillOval((int)x, (int)y, size, size);
        }

        public boolean isDead() {
            return life <= 0;
        }
    }
}
