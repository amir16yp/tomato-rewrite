package tomato.core;

import java.util.logging.Logger;

/**
 * Mathf - Fast math utility class with precached trigonometric values
 * Improves performance by storing precomputed values of sin, cos, and tan
 * for commonly used angles.
 */
public class Mathf {
    private static final Logger logger = Logger.getLogger(Mathf.class.getName());

    // Size of the lookup tables (higher values give more precision)
    private static final int TABLE_SIZE = 4096;

    // Maximum angle in radians (2π)
    private static final double MAX_ANGLE = Math.PI * 2.0;

    // Lookup tables for trigonometric functions
    private static final double[] SIN_TABLE = new double[TABLE_SIZE];
    private static final double[] COS_TABLE = new double[TABLE_SIZE];
    private static final double[] TAN_TABLE = new double[TABLE_SIZE];

    // Conversion factor from radians to table index
    private static final double TABLE_FACTOR = TABLE_SIZE / MAX_ANGLE;

    public static double normalizeAngle(double angle) {
        // Use modulo to bring the angle within a 2π range
        angle = angle % (2 * Math.PI);

        // Adjust to the range [-π, π]
        if (angle > Math.PI) {
            angle -= 2 * Math.PI;
        } else if (angle < -Math.PI) {
            angle += 2 * Math.PI;
        }

        return angle;
    }

    // Static initializer to populate the tables
    static {
        for (int i = 0; i < TABLE_SIZE; i++) {
            double angle = i * MAX_ANGLE / TABLE_SIZE;
            SIN_TABLE[i] = Math.sin(angle);
            COS_TABLE[i] = Math.cos(angle);
            TAN_TABLE[i] = Math.tan(angle);
        }
        logger.info("Mathf: Trigonometric tables initialized with " + TABLE_SIZE + " entries");
    }

    /**
     * Fast sine calculation using table lookup
     *
     * @param angle Angle in radians
     * @return Approximate sine value
     */
    public static double sin(double angle) {
        // Normalize angle to [0, 2π)
        angle = angle % MAX_ANGLE;
        if (angle < 0) angle += MAX_ANGLE;

        // Convert to table index
        int index = (int) (angle * TABLE_FACTOR) & (TABLE_SIZE - 1);
        return SIN_TABLE[index];
    }

    /**
     * Fast cosine calculation using table lookup
     *
     * @param angle Angle in radians
     * @return Approximate cosine value
     */
    public static double cos(double angle) {
        // Normalize angle to [0, 2π)
        angle = angle % MAX_ANGLE;
        if (angle < 0) angle += MAX_ANGLE;

        // Convert to table index
        int index = (int) (angle * TABLE_FACTOR) & (TABLE_SIZE - 1);
        return COS_TABLE[index];
    }

    /**
     * Fast tangent calculation using table lookup
     *
     * @param angle Angle in radians
     * @return Approximate tangent value
     */
    public static double tan(double angle) {
        // Normalize angle to [0, 2π)
        angle = angle % MAX_ANGLE;
        if (angle < 0) angle += MAX_ANGLE;

        // Convert to table index
        int index = (int) (angle * TABLE_FACTOR) & (TABLE_SIZE - 1);
        return TAN_TABLE[index];
    }

    /**
     * Fast inverse tangent approximation
     *
     * @param y Y coordinate
     * @param x X coordinate
     * @return Approximate arctangent in radians
     */
    public static double atan2(double y, double x) {
        if (x == 0.0) return (y > 0) ? Math.PI / 2 : (y < 0 ? -Math.PI / 2 : 0);

        double absZ = Math.abs(y / x);
        double angle;

        if (absZ < 1.0) {
            angle = absZ / (1.0 + 0.28 * absZ * absZ);
        } else {
            angle = Math.PI / 2 - absZ / (absZ * absZ + 0.28);
        }

        if (x < 0) {
            angle = Math.PI - angle;
        }
        return (y < 0) ? -angle : angle;
    }

    /**
     * Converts degrees to radians
     *
     * @param degrees Angle in degrees
     * @return Angle in radians
     */
    public static double toRadians(double degrees) {
        // Using multiplication is faster than division
        // PI/180 = 0.017453292519943295
        return degrees * 0.017453292519943295;
    }

    /**
     * Converts radians to degrees
     *
     * @param radians Angle in radians
     * @return Angle in degrees
     */
    public static double toDegrees(double radians) {
        // Using multiplication is faster than division
        // 180/PI = 57.29577951308232
        return radians * 57.29577951308232;
    }

    /**
     * Fast approximation of square root for distance calculations
     *
     * @param x Value to calculate square root of
     * @return Approximated square root
     */
    public static double fastSqrt(double x) {
        // Fast inverse square root approximation (similar to Quake III algorithm)
        // but using Java's built-in Double.longBitsToDouble for portability
        if (x <= 0) return 0;

        double xhalf = 0.5d * x;
        long i = Double.doubleToLongBits(x);
        i = 0x5fe6eb50c7b537a9L - (i >> 1); // Magic number for initial guess
        double y = Double.longBitsToDouble(i);
        // One iteration of Newton's method
        y = y * (1.5d - xhalf * y * y);

        return x * y; // x * 1/sqrt(x) = sqrt(x)
    }

    /**
     * Calculate the distance between two points using fast approximation
     *
     * @param x1 First point x
     * @param y1 First point y
     * @param x2 Second point x
     * @param y2 Second point y
     * @return Distance between the points
     */
    public static double distance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;

        // Use built-in hypot for higher precision needs
        // return Math.hypot(dx, dy);

        // For performance, use approximation
        return fastSqrt(dx * dx + dy * dy);
    }
}