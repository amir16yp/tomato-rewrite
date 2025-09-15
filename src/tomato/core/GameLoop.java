package tomato.core;

import tomato.Game;

import java.util.concurrent.locks.LockSupport;


public class GameLoop implements Runnable {
    private static final float NANOS_PER_SECOND = 1_000_000_000.0f;
    private static final float MILLIS_PER_SECOND = 1000.0f;
    private static final int FPS_SAMPLE_SIZE = 60; // Rolling average window size
    private static final int MENU_FPS = 60; // Fixed FPS for menus
    // Improved FPS tracking
    private final long[] frameTimes = new long[FPS_SAMPLE_SIZE];
    private final Thread thread = new Thread(this);
    private boolean running = false;
    private boolean paused = false;
    private long lastFrameTime;
    private float deltaTime; // In seconds
    private float deltaTimeMillis; // In milliseconds
    private int frameTimeIndex = 0;
    private int frameCount = 0;
    private long fps = 0;
    private long lastFPSUpdateTime = 0;
    private float timeScale = 1.0f;


    public GameLoop() {
        // Initialize frame times array
        for (int i = 0; i < FPS_SAMPLE_SIZE; i++) {
            frameTimes[i] = 0;
        }
    }

    public int getTargetFPS() {
        return 0;
    }

    public float getTimeScale() {
        return timeScale;
    }

    public void setTimeScale(float timeScale) {
        this.timeScale = timeScale;
    }

    public float getDeltaTime() {
        return deltaTime * timeScale;
    }

    public float getDeltaTimeMillis() {
        return deltaTimeMillis * timeScale;
    }

    public long getFPS() {
        return fps;
    }

    public void start() {
        if (!running) {
            running = true;
            lastFrameTime = System.nanoTime();
            if (!thread.isAlive()) {
                thread.start();
            }
        }
    }

    public void stop() {
        running = false;
    }

    /**
     * Check if the game loop is currently running
     *
     * @return True if the game loop is running
     */
    public boolean isRunning() {
        return running;
    }

    public void togglePause() {
        paused = !paused;
        if (!paused) {
            lastFrameTime = System.nanoTime(); // Reset lastFrameTime when unpausing
        }
    }

    /**
     * More precise sleep method using hybrid approach:
     * - Spin for high precision with small durations
     * - Use parkNanos for most waiting which is more efficient than sleep()
     *
     * @param nanos time to wait in nanoseconds
     */
    private void preciseSleep(long nanos) {
        if (nanos <= 0) return;

        final long targetTime = System.nanoTime() + nanos;

        long now;
        while ((now = System.nanoTime()) < targetTime) {
            long timeLeft = targetTime - now;

            if (timeLeft > 2_000_000L) {
                LockSupport.parkNanos(1_000_000L); // Sleep 1ms
            } else if (timeLeft > 100_000L) {
                Thread.yield(); // Yield for short time
            } else {
                // Final spin
                // If on Java 9+, use: Thread.onSpinWait();
            }
        }
    }


    @Override
    public void run() {
        final long targetFrameTime = (long) (NANOS_PER_SECOND / getTargetFPS());

        while (running) {
            long frameStartTime = System.nanoTime();

            deltaTime = (frameStartTime - lastFrameTime) / NANOS_PER_SECOND;
            deltaTimeMillis = deltaTime * MILLIS_PER_SECOND;
            lastFrameTime = frameStartTime;

            // Update game state
            Game.RENDERER.update();

            // Repaint on EDT
            Game.RENDERER.repaint();

            updateFPS(frameStartTime);

            // Frame rate limiting
            long frameTime = System.nanoTime() - frameStartTime;
            long sleepTime = targetFrameTime - frameTime;

            if (sleepTime > 0) {
                preciseSleep(sleepTime);
            }
        }
    }

    private void updateFPS(long currentTime) {
        frameTimes[frameTimeIndex] = currentTime;
        frameTimeIndex = (frameTimeIndex + 1) % FPS_SAMPLE_SIZE;
        frameCount++;

        if (frameCount >= FPS_SAMPLE_SIZE) {
            long oldestFrame = frameTimes[(frameTimeIndex + 1) % FPS_SAMPLE_SIZE];
            if (oldestFrame > 0) {
                double timeElapsed = (currentTime - oldestFrame) / (double) NANOS_PER_SECOND;

                // Update FPS once per second
                if (currentTime - lastFPSUpdateTime >= NANOS_PER_SECOND) {
                    fps = Math.round(FPS_SAMPLE_SIZE / timeElapsed);
                    lastFPSUpdateTime = currentTime;
                }
            }
        }
    }
}