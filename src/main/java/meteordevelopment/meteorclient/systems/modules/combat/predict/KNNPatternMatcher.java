package meteordevelopment.meteorclient.systems.modules.combat.predict;

import net.minecraft.util.math.Vec3d;

/**
 * Per-entity KNN pattern matcher for movement prediction.
 * Maintains a 40-tick circular buffer of [x,y,z,vx,vy,vz] observations.
 * All working arrays (distances, pattern buffers) are pre-allocated as fields;
 * no double[] allocations occur inside predictNextPosition() after construction.
 */
public class KNNPatternMatcher {

    private static final int HISTORY_SIZE   = 40;
    private static final int PATTERN_LENGTH = 5;
    private static final int K              = 3;
    private static final int DIMS           = 6;
    private static final int FLAT_LEN       = PATTERN_LENGTH * DIMS; // 30

    private final double[][] history = new double[HISTORY_SIZE][DIMS];
    private int historyHead  = 0;
    private int historyCount = 0;

    // Pre-allocated working arrays — reused every tick, zero allocations in hot path
    private final double[] distances    = new double[HISTORY_SIZE];
    private final int[]    indices      = new int[HISTORY_SIZE];
    private final double[] queryPattern = new double[FLAT_LEN];
    private final double[] candPattern  = new double[FLAT_LEN];

    // =========================================================================
    // Public API
    // =========================================================================

    public void addObservation(Vec3d pos, Vec3d vel) {
        history[historyHead][0] = pos.x;
        history[historyHead][1] = pos.y;
        history[historyHead][2] = pos.z;
        history[historyHead][3] = vel.x;
        history[historyHead][4] = vel.y;
        history[historyHead][5] = vel.z;
        historyHead = (historyHead + 1) % HISTORY_SIZE;
        if (historyCount < HISTORY_SIZE) historyCount++;
    }

    public int getHistoryCount() { return historyCount; }

    /**
     * Predict position [ticks] ahead using KNN pattern matching.
     * Returns null if fewer than 10 observations are available or no valid matches found.
     */
    public Vec3d predictNextPosition(int ticks) {
        if (historyCount < 10) return null;
        if (historyCount < PATTERN_LENGTH + ticks + 1) return null;

        if (!fillLastNTicksFlat(queryPattern, PATTERN_LENGTH)) return null;

        int maxIndex = historyCount - PATTERN_LENGTH - ticks;
        if (maxIndex <= 0) return null;

        int validCount = 0;
        for (int i = 0; i < maxIndex; i++) {
            if (!fillNTicksAtFlat(i, PATTERN_LENGTH, candPattern)) continue;
            distances[validCount] = patternDistanceFlat(queryPattern, candPattern);
            indices[validCount]   = i;
            validCount++;
        }
        if (validCount == 0) return null;

        int kActual = Math.min(K, validCount);
        for (int k = 0; k < kActual; k++) {
            int minIdx = k;
            for (int j = k + 1; j < validCount; j++)
                if (distances[j] < distances[minIdx]) minIdx = j;
            double tmpD = distances[k]; distances[k] = distances[minIdx]; distances[minIdx] = tmpD;
            int    tmpI = indices[k];   indices[k]   = indices[minIdx];   indices[minIdx]   = tmpI;
        }

        double totalWeight = 0, wx = 0, wy = 0, wz = 0;
        for (int k = 0; k < kActual; k++) {
            double   weight      = 1.0 / (distances[k] + 0.001);
            int      futureIndex = indices[k] + PATTERN_LENGTH + (ticks - 1);
            double[] futureObs   = getObsAt(futureIndex);
            if (futureObs == null) continue;
            wx += futureObs[0] * weight;
            wy += futureObs[1] * weight;
            wz += futureObs[2] * weight;
            totalWeight += weight;
        }

        if (totalWeight == 0) return null;
        return new Vec3d(wx / totalWeight, wy / totalWeight, wz / totalWeight);
    }

    /** Blend KNN prediction with Kalman prediction. KNN gains weight as history grows. */
    public Vec3d blendWithKalman(Vec3d kalmanPred, Vec3d knnPred, int historyLen) {
        if (knnPred == null || historyLen < 15) return kalmanPred;
        return new Vec3d(
            kalmanPred.x * 0.6 + knnPred.x * 0.4,
            kalmanPred.y * 0.6 + knnPred.y * 0.4,
            kalmanPred.z * 0.6 + knnPred.z * 0.4
        );
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    /** Fill [out] with last n ticks flattened as [t*DIMS + d]. Returns false if not enough data. */
    private boolean fillLastNTicksFlat(double[] out, int n) {
        if (historyCount < n) return false;
        for (int i = 0; i < n; i++) {
            int idx = ((historyHead - n + i) % HISTORY_SIZE + HISTORY_SIZE) % HISTORY_SIZE;
            System.arraycopy(history[idx], 0, out, i * DIMS, DIMS);
        }
        return true;
    }

    /** Fill [out] with n ticks starting at logical index startLogical, flattened. */
    private boolean fillNTicksAtFlat(int startLogical, int n, double[] out) {
        if (startLogical + n > historyCount) return false;
        for (int i = 0; i < n; i++) {
            double[] obs = getObsAt(startLogical + i);
            if (obs == null) return false;
            System.arraycopy(obs, 0, out, i * DIMS, DIMS);
        }
        return true;
    }

    /** Euclidean distance between two flat PATTERN_LENGTH*DIMS vectors. */
    private double patternDistanceFlat(double[] a, double[] b) {
        double dist = 0;
        for (int i = 0; i < FLAT_LEN; i++) {
            double diff = a[i] - b[i];
            dist += diff * diff;
        }
        return Math.sqrt(dist);
    }

    /** Returns reference to the raw history row at logical index (0 = oldest). Never copies. */
    private double[] getObsAt(int logicalIndex) {
        if (logicalIndex < 0 || logicalIndex >= historyCount) return null;
        int physIdx = ((historyHead - historyCount + logicalIndex) % HISTORY_SIZE + HISTORY_SIZE) % HISTORY_SIZE;
        return history[physIdx];
    }
}
