package tomato.vfx;

import tomato.Game;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

public class ExplosionEffect extends ParticleSystem implements DynamicLightSource {
    private static final int RING_COUNT = 5;
    private static final int PARTICLES_PER_RING = 30;
    private static final int RANDOM_PARTICLE_COUNT = 100;

    private final int x, y;
    private final int maxRadius;
    private final float duration;
    private final ArrayList<Color> shuffledRingColors;
    private final ArrayList<Color> shuffledParticleColors;
    private float currentTime;

    public ExplosionEffect(int x, int y, int maxRadius, float durationSeconds, Color[] ringColors, Color[] particleColors) {
        super();
        this.x = x;
        this.y = y;
        this.maxRadius = maxRadius;
        this.duration = durationSeconds;
        this.shuffledRingColors = shuffleColors(ringColors);
        this.shuffledParticleColors = shuffleColors(particleColors);
        this.currentTime = 0;
        createExplosion();
    }
    
    private ArrayList<Color> shuffleColors(Color[] colors) {
        ArrayList<Color> colorList = new ArrayList<>();
        Collections.addAll(colorList, colors);
        Collections.shuffle(colorList);
        return colorList;
    }

    private void createExplosion() {
        createRings();
        createRandomParticles();
    }

    private void createRings() {
        for (int ring = 0; ring < RING_COUNT; ring++) {
            float ringRadius = (ring + 1) * (maxRadius / (float)RING_COUNT);
            Color ringColor = shuffledRingColors.get(ring % shuffledRingColors.size());

            for (int i = 0; i < PARTICLES_PER_RING; i++) {
                float angle = (float) (i * 2 * Math.PI / PARTICLES_PER_RING);
                float particleX = x + (float)(Math.cos(angle) * ringRadius);
                float particleY = y + (float)(Math.sin(angle) * ringRadius);

                int size = random.nextInt(2) + 2; // Size between 2 and 3
                float life = duration * (0.5f + random.nextFloat() * 0.5f); // 50% to 100% of duration

                particles.add(new RingParticle(particleX, particleY, ringRadius, angle, ringColor, size, life));
            }
        }
    }

    private void createRandomParticles() {
        for (int i = 0; i < RANDOM_PARTICLE_COUNT; i++) {
            float angle = random.nextFloat() * 2 * (float) Math.PI;
            float radius = random.nextFloat() * maxRadius;
            float particleX = x + (float)(Math.cos(angle) * radius);
            float particleY = y + (float)(Math.sin(angle) * radius);

            Color particleColor = shuffledParticleColors.get(random.nextInt(shuffledParticleColors.size()));
            int size = random.nextInt(3) + 2; // Size between 2 and 4
            float life = duration * (0.3f + random.nextFloat() * 0.7f); // 30% to 100% of duration

            particles.add(new RandomParticle(particleX, particleY, particleColor, size, life));
        }
    }

    @Override
    public void update() {
        float deltaTime = Game.GAME_LOOP.getDeltaTime();
        currentTime += deltaTime;

        particles.removeIf(particle -> {
            particle.update(deltaTime);
            return particle.isDead();
        });
    }

    @Override
    public boolean isFinished() {
        return currentTime >= duration || particles.isEmpty();
    }

    @Override
    public int getLightX() {
        return x;
    }

    @Override
    public int getLightY() {
        return y;
    }

    @Override
    public int getLightRadius() {
        return (int) (maxRadius * Math.max(0, 1 - currentTime / duration));
    }

    @Override
    public float getLightStrength() {
        return Math.max(0, 1f - (currentTime / duration) * 2); // Light fades twice as fast
    }

    private class RingParticle extends Particle {
        private float radius;
        private float angle;
        private float initialLife;

        public RingParticle(float startX, float startY, float radius, float angle, Color color, int size, float life) {
            super(startX, startY, color);
            this.radius = radius;
            this.angle = angle;
            this.size = size;
            this.life = life;
            this.initialLife = life;
        }

        @Override
        public void update(float deltaTime) {
            // Expand the radius faster
            radius += (maxRadius / (duration * 0.75f)) * deltaTime;

            // Update position based on new radius
            x = ExplosionEffect.this.x + (float)(Math.cos(angle) * radius);
            y = ExplosionEffect.this.y + (float)(Math.sin(angle) * radius);

            // Increase angular velocity for faster rotation
            angle += deltaTime * 2f;

            life -= deltaTime;
        }

        @Override
        public void draw(Graphics2D g) {
            float alpha = Math.max(0, Math.min(1, life / initialLife));
            Color adjustedColor = new Color(
                    color.getRed(),
                    color.getGreen(),
                    color.getBlue(),
                    (int)(255 * alpha)
            );
            g.setColor(adjustedColor);
            g.fillOval((int)x - size/2, (int)y - size/2, size, size);
        }

        @Override
        public boolean isDead() {
            return life <= 0 || radius > maxRadius;
        }
    }

    private class RandomParticle extends Particle {
        private float initialLife;

        public RandomParticle(float x, float y, Color color, int size, float life) {
            super(x, y, color);
            float speed = random.nextFloat() * maxRadius / (duration * 0.75f); // Faster initial speed
            float angle = random.nextFloat() * 2 * (float) Math.PI;
            this.vx = (float) Math.cos(angle) * speed;
            this.vy = (float) Math.sin(angle) * speed;
            this.size = size;
            this.life = life;
            this.initialLife = life;
        }

        @Override
        public void update(float deltaTime) {
            x += vx * deltaTime;
            y += vy * deltaTime;

            // Slow down particles more quickly
            vx *= 0.95f;
            vy *= 0.95f;

            life -= deltaTime;
        }

        @Override
        public void draw(Graphics2D g) {
            float alpha = Math.max(0, Math.min(1, life / initialLife));
            Color adjustedColor = new Color(
                    color.getRed(),
                    color.getGreen(),
                    color.getBlue(),
                    (int)(255 * alpha)
            );
            g.setColor(adjustedColor);
            g.fillOval((int)x - size/2, (int)y - size/2, size, size);
        }
    }
}
