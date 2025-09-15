package tomato.core;

import java.util.Random;

/**
 * OpenSimplex noise in pure Java.
 * Original algorithm by Kurt Spencer, simplified for 2D use here.
 */
public class OpenSimplexNoise {
    private static final double STRETCH_CONSTANT_2D = -0.211324865405187;    // (1/√2+1 - 1)/2
    private static final double SQUISH_CONSTANT_2D = 0.366025403784439;      // (√2+1 - 1)/2

    private static final long DEFAULT_SEED = 0;
    private final short[] perm;
    private final short[] permGradIndex2D;

    private static final byte[] gradients2D = new byte[]{
            5, 2, 2, 5,
            -5, 2, -2, 5,
            5, -2, 2, -5,
            -5, -2, -2, -5,
    };

    public OpenSimplexNoise() {
        this(DEFAULT_SEED);
    }

    public OpenSimplexNoise(long seed) {
        perm = new short[256];
        permGradIndex2D = new short[256];
        short[] source = new short[256];
        for (short i = 0; i < 256; i++)
            source[i] = i;
        Random random = new Random(seed);
        for (int i = 255; i >= 0; i--) {
            int r = random.nextInt(i + 1);
            perm[i] = source[r];
            permGradIndex2D[i] = (short) ((perm[i] % (gradients2D.length / 2)) * 2);
            source[r] = source[i];
        }
    }

    /**
     * 2D OpenSimplex noise: returns in range [-1, 1]
     */
    public double eval(double x, double y) {
        // Place input coordinates onto grid.
        double stretchOffset = (x + y) * STRETCH_CONSTANT_2D;
        double xs = x + stretchOffset;
        double ys = y + stretchOffset;

        // Floor to get grid coordinates of rhombus (stretched square) super-cell origin.
        int xsb = fastFloor(xs);
        int ysb = fastFloor(ys);

        // Unstretch to get actual coordinates of rhombus origin.
        double squishOffset = (xsb + ysb) * SQUISH_CONSTANT_2D;
        double xb = xsb + squishOffset;
        double yb = ysb + squishOffset;

        // Compute grid coordinates relative to rhombus origin.
        double xins = xs - xsb;
        double yins = ys - ysb;

        // Sum those together to get a value that determines which region we're in.
        double inSum = xins + yins;

        // Positions relative to origin point.
        double dx0 = x - xb;
        double dy0 = y - yb;

        // We'll be defining these inside the next block and using them afterwards.
        double dx_ext, dy_ext;
        int xsv_ext, ysv_ext;

        double value = 0;

        // Contribution (1,0)
        double dx1 = dx0 - 1 - SQUISH_CONSTANT_2D;
        double dy1 = dy0 - 0 - SQUISH_CONSTANT_2D;
        double attn1 = 2 - dx1 * dx1 - dy1 * dy1;
        if (attn1 > 0) {
            int gi = permGradIndex2D[(perm[(xsb + 1) & 0xFF] + ysb) & 0xFF];
            double extrapolation = gradients2D[gi] * dx1 + gradients2D[gi + 1] * dy1;
            attn1 *= attn1;
            value += attn1 * attn1 * extrapolation;
        }

        // Contribution (0,1)
        double dx2 = dx0 - 0 - SQUISH_CONSTANT_2D;
        double dy2 = dy0 - 1 - SQUISH_CONSTANT_2D;
        double attn2 = 2 - dx2 * dx2 - dy2 * dy2;
        if (attn2 > 0) {
            int gi = permGradIndex2D[(perm[xsb & 0xFF] + ysb + 1) & 0xFF];
            double extrapolation = gradients2D[gi] * dx2 + gradients2D[gi + 1] * dy2;
            attn2 *= attn2;
            value += attn2 * attn2 * extrapolation;
        }

        // Contribution (0,0)
        double dx0b = dx0 - 0 - 0 * SQUISH_CONSTANT_2D;
        double dy0b = dy0 - 0 - 0 * SQUISH_CONSTANT_2D;
        double attn0 = 2 - dx0b * dx0b - dy0b * dy0b;
        if (attn0 > 0) {
            int gi = permGradIndex2D[(perm[xsb & 0xFF] + ysb) & 0xFF];
            double extrapolation = gradients2D[gi] * dx0b + gradients2D[gi + 1] * dy0b;
            attn0 *= attn0;
            value += attn0 * attn0 * extrapolation;
        }

        return value / 47; // scale to [-1,1]
    }

    private static int fastFloor(double x) {
        int xi = (int) x;
        return x < xi ? xi - 1 : xi;
    }
}
