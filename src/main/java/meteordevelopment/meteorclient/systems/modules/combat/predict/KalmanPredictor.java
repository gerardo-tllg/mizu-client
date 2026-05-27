package meteordevelopment.meteorclient.systems.modules.combat.predict;

import net.minecraft.util.math.Vec3d;

/**
 * Per-entity 6D Kalman filter: state = [x, y, z, vx, vy, vz].
 * All intermediate matrices and vectors are pre-allocated as instance fields;
 * no double[] or double[][] allocations occur in predict(), update(), or
 * getPredictedPosition() after construction.
 */
public class KalmanPredictor {

    // --- Static noise constants ---
    private static final double[] Q_DIAG = {0.01, 0.01, 0.01, 0.1, 0.1, 0.1};
    private static final double[] R_DIAG = {0.05, 0.05, 0.05, 0.2, 0.2, 0.2};

    // Walking state-transition matrix F
    private static final double[][] F_WALK = {
        {1, 0, 0, 1,    0,    0   },
        {0, 1, 0, 0,    1,    0   },
        {0, 0, 1, 0,    0,    1   },
        {0, 0, 0, 0.91, 0,    0   },
        {0, 0, 0, 0,    0.98, 0   },
        {0, 0, 0, 0,    0,    0.91}
    };
    private static final double[] U_WALK = {0, 0, 0, 0, -0.0784, 0};

    // Elytra state-transition matrix F
    private static final double[][] F_ELYTRA = {
        {1, 0, 0, 1,    0,    0   },
        {0, 1, 0, 0,    1,    0   },
        {0, 0, 1, 0,    0,    1   },
        {0, 0, 0, 0.99, 0,    0   },
        {0, 0, 0, 0,    0.99, 0   },
        {0, 0, 0, 0,    0,    0.99}
    };
    private static final double[] U_ELYTRA = {0, 0, 0, 0, 0, 0};

    // --- Live state ---
    private final double[]   state = new double[6];
    private final double[][] P     = new double[6][6];
    private boolean initialized = false;

    // --- predict() scratch (pre-allocated, reused every tick) ---
    private final double[]   pWS   = new double[6];
    private final double[]   pNS   = new double[6];
    private final double[][] pFt   = new double[6][6];
    private final double[][] pFP   = new double[6][6];
    private final double[][] pFPFt = new double[6][6];

    // --- update() scratch ---
    private final double[]   uZ    = new double[6];
    private final double[][] uS    = new double[6][6];
    private final double[][] uSinv = new double[6][6];
    private final double[][] uK    = new double[6][6];
    private final double[]   uInn  = new double[6];
    private final double[]   uKinn = new double[6];
    private final double[][] uImK  = new double[6][6];
    private final double[][] uPnew = new double[6][6];

    // --- getPredictedPosition() scratch ---
    private final double[] gPS = new double[6];
    private final double[] gPW = new double[6];

    // --- invert6x6() scratch ---
    private final double[][] invA = new double[6][6];
    private final double[][] invI = new double[6][6];

    // =========================================================================
    // Public API
    // =========================================================================

    public boolean isInitialized() { return initialized; }

    public void init(Vec3d pos, Vec3d vel) {
        state[0] = pos.x; state[1] = pos.y; state[2] = pos.z;
        state[3] = vel.x; state[4] = vel.y; state[5] = vel.z;
        for (int i = 0; i < 6; i++)
            for (int j = 0; j < 6; j++) P[i][j] = (i == j) ? 1.0 : 0.0;
        initialized = true;
    }

    /** Prediction step — advances internal state one tick. Call before update(). */
    public void predict(EntityStateAnalyzer.EntityMovementState movState, float speedMult, float jumpBonus) {
        try {
            boolean elytra = (movState == EntityStateAnalyzer.EntityMovementState.ELYTRA_FLYING);
            double[][] F = elytra ? F_ELYTRA : F_WALK;
            double[]   u = elytra ? U_ELYTRA  : U_WALK;

            System.arraycopy(state, 0, pWS, 0, 6);
            pWS[3] *= speedMult;
            pWS[5] *= speedMult;

            mulVecInto(F, pWS, pNS);
            for (int i = 0; i < 6; i++) pNS[i] += u[i];
            System.arraycopy(pNS, 0, state, 0, 6);

            transposeInto(F, pFt);
            mulMatInto(F,   P,   pFP);
            mulMatInto(pFP, pFt, pFPFt);
            addDiagInto(pFPFt, Q_DIAG, P);
        } catch (Exception ignored) {}
    }

    /** Measurement update — corrects state with observed position and velocity. */
    public void update(Vec3d measuredPos, Vec3d measuredVel) {
        try {
            uZ[0] = measuredPos.x; uZ[1] = measuredPos.y; uZ[2] = measuredPos.z;
            uZ[3] = measuredVel.x; uZ[4] = measuredVel.y; uZ[5] = measuredVel.z;

            addDiagInto(P, R_DIAG, uS);
            invert6x6Into(uS, uSinv);
            mulMatInto(P, uSinv, uK);

            for (int i = 0; i < 6; i++) uInn[i] = uZ[i] - state[i];
            mulVecInto(uK, uInn, uKinn);
            for (int i = 0; i < 6; i++) state[i] += uKinn[i];

            subFromIdentityInto(uK, uImK);
            mulMatInto(uImK, P, uPnew);
            copyMatInto(uPnew, P);
        } catch (Exception ignored) {}
    }

    /**
     * Returns predicted position [ticks] ticks into the future.
     * Operates on a scratch copy — does not mutate the live state.
     */
    public Vec3d getPredictedPosition(int ticks, EntityStateAnalyzer.EntityMovementState movState, float speedMult) {
        try {
            System.arraycopy(state, 0, gPS, 0, 6);
            boolean elytra = (movState == EntityStateAnalyzer.EntityMovementState.ELYTRA_FLYING);
            double[][] F = elytra ? F_ELYTRA : F_WALK;
            double[]   u = elytra ? U_ELYTRA  : U_WALK;

            for (int t = 0; t < ticks; t++) {
                System.arraycopy(gPS, 0, gPW, 0, 6);
                gPW[3] *= speedMult;
                gPW[5] *= speedMult;
                mulVecInto(F, gPW, gPS);
                for (int i = 0; i < 6; i++) gPS[i] += u[i];
            }
            return new Vec3d(gPS[0], gPS[1], gPS[2]);
        } catch (Exception e) {
            return new Vec3d(state[0], state[1], state[2]);
        }
    }

    /** Scale uncertainty on P diagonal — used when knockback is detected. */
    public void increaseUncertainty(double factor) {
        try {
            for (int i = 0; i < 6; i++) P[i][i] *= factor;
        } catch (Exception ignored) {}
    }

    // =========================================================================
    // Private matrix utilities — all write into caller-supplied output arrays
    // =========================================================================

    private static void mulVecInto(double[][] M, double[] v, double[] out) {
        for (int i = 0; i < 6; i++) {
            out[i] = 0;
            for (int j = 0; j < 6; j++) out[i] += M[i][j] * v[j];
        }
    }

    private static void mulMatInto(double[][] A, double[][] B, double[][] out) {
        for (int i = 0; i < 6; i++)
            for (int j = 0; j < 6; j++) {
                out[i][j] = 0;
                for (int k = 0; k < 6; k++) out[i][j] += A[i][k] * B[k][j];
            }
    }

    private static void transposeInto(double[][] A, double[][] out) {
        for (int i = 0; i < 6; i++)
            for (int j = 0; j < 6; j++) out[j][i] = A[i][j];
    }

    private static void addDiagInto(double[][] A, double[] diag, double[][] out) {
        for (int i = 0; i < 6; i++)
            for (int j = 0; j < 6; j++) out[i][j] = A[i][j] + (i == j ? diag[i] : 0.0);
    }

    private static void subFromIdentityInto(double[][] K, double[][] out) {
        for (int i = 0; i < 6; i++)
            for (int j = 0; j < 6; j++) out[i][j] = (i == j ? 1.0 : 0.0) - K[i][j];
    }

    private static void copyMatInto(double[][] src, double[][] dst) {
        for (int i = 0; i < 6; i++) System.arraycopy(src[i], 0, dst[i], 0, 6);
    }

    /** Gaussian elimination with partial pivoting. Writes inverse into out; writes identity on singularity. */
    private void invert6x6Into(double[][] M, double[][] out) {
        for (int i = 0; i < 6; i++) System.arraycopy(M[i], 0, invA[i], 0, 6);
        for (int i = 0; i < 6; i++)
            for (int j = 0; j < 6; j++) invI[i][j] = (i == j) ? 1.0 : 0.0;

        for (int col = 0; col < 6; col++) {
            int maxRow = col;
            for (int row = col + 1; row < 6; row++)
                if (Math.abs(invA[row][col]) > Math.abs(invA[maxRow][col])) maxRow = row;

            double[] tmpA = invA[col]; invA[col] = invA[maxRow]; invA[maxRow] = tmpA;
            double[] tmpI = invI[col]; invI[col] = invI[maxRow]; invI[maxRow] = tmpI;

            double pivot = invA[col][col];
            if (Math.abs(pivot) < 1e-12) {
                for (int i = 0; i < 6; i++)
                    for (int j = 0; j < 6; j++) out[i][j] = (i == j) ? 1.0 : 0.0;
                return;
            }

            for (int j = 0; j < 6; j++) { invA[col][j] /= pivot; invI[col][j] /= pivot; }

            for (int row = 0; row < 6; row++) {
                if (row == col) continue;
                double factor = invA[row][col];
                for (int j = 0; j < 6; j++) {
                    invA[row][j] -= factor * invA[col][j];
                    invI[row][j] -= factor * invI[col][j];
                }
            }
        }
        for (int i = 0; i < 6; i++) System.arraycopy(invI[i], 0, out[i], 0, 6);
    }
}
