package meteordevelopment.meteorclient.systems.modules.combat.predict;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

/**
 * Per-entity Markov chain behavioral predictor.
 * Tracks movement state transitions to predict next behavior.
 */
public class MarkovBehaviorModel {

    public enum BehaviorState {
        SPRINTING_FORWARD,
        STRAFING_LEFT,
        STRAFING_RIGHT,
        STANDING_STILL,
        JUMPING,
        FALLING,
        EATING,
        PLACING_BLOCKS,
        ELYTRA_FLYING,
        TAKING_KNOCKBACK;

        public static final int COUNT = values().length; // 10
    }

    private static final int N = BehaviorState.COUNT; // 10
    private static final double BIAS_MAGNITUDE = 0.1; // blocks per tick, conservative

    private final double[][] transitionMatrix = new double[N][N];
    private BehaviorState currentState = BehaviorState.STANDING_STILL;
    private BehaviorState prevState    = BehaviorState.STANDING_STILL;
    private Vec3d lastVelocity = Vec3d.ZERO;

    public MarkovBehaviorModel() {
        // Uniform prior: 1/N each
        double uniform = 1.0 / N;
        for (int i = 0; i < N; i++)
            for (int j = 0; j < N; j++)
                transitionMatrix[i][j] = uniform;
    }

    /**
     * Update state from entity's current observed data.
     * Call once per tick per entity.
     */
    public void update(LivingEntity entity, EntityStateAnalyzer.EntityMovementState movState) {
        prevState = currentState;
        currentState = detectBehaviorState(entity, movState);

        // Increment transition count and renormalize row
        int from = prevState.ordinal();
        int to   = currentState.ordinal();
        transitionMatrix[from][to] += 1.0;

        // Normalize row so it sums to 1.0
        double rowSum = 0;
        for (int j = 0; j < N; j++) rowSum += transitionMatrix[from][j];
        if (rowSum > 0) for (int j = 0; j < N; j++) transitionMatrix[from][j] /= rowSum;

        lastVelocity = entity.getVelocity();
    }

    /** Returns the most likely next behavior state. */
    public BehaviorState predictNextState() {
        int cur = currentState.ordinal();
        int bestJ = 0;
        double bestP = transitionMatrix[cur][0];
        for (int j = 1; j < N; j++) {
            if (transitionMatrix[cur][j] > bestP) {
                bestP = transitionMatrix[cur][j];
                bestJ = j;
            }
        }
        return BehaviorState.values()[bestJ];
    }

    /**
     * Apply a behavioral bias to a Kalman prediction.
     * Shifts the prediction toward where the entity is likely to move.
     */
    public Vec3d applyBehavioralBias(Vec3d kalmanPrediction, LivingEntity entity) {
        BehaviorState nextState = predictNextState();

        // No bias for low-confidence states
        if (nextState == BehaviorState.TAKING_KNOCKBACK) return kalmanPrediction;

        double bx = 0, by = 0, bz = 0;
        float yawRad = (float) Math.toRadians(entity.getYaw());

        switch (nextState) {
            case STRAFING_LEFT -> {
                // Perpendicular left relative to entity yaw
                bx = -Math.cos(yawRad) * BIAS_MAGNITUDE;
                bz =  Math.sin(yawRad) * BIAS_MAGNITUDE;
            }
            case STRAFING_RIGHT -> {
                bx =  Math.cos(yawRad) * BIAS_MAGNITUDE;
                bz = -Math.sin(yawRad) * BIAS_MAGNITUDE;
            }
            case JUMPING -> {
                by = BIAS_MAGNITUDE;
            }
            case EATING, STANDING_STILL -> {
                // Reduce horizontal component — entity is likely slowing down
                return new Vec3d(
                    kalmanPrediction.x * 0.5,
                    kalmanPrediction.y,
                    kalmanPrediction.z * 0.5
                );
            }
            default -> { /* No bias */ }
        }

        return kalmanPrediction.add(bx, by, bz);
    }

    // === PRIVATE ===

    private BehaviorState detectBehaviorState(LivingEntity entity, EntityStateAnalyzer.EntityMovementState movState) {
        Vec3d vel = entity.getVelocity();
        double hSpeed = Math.sqrt(vel.x * vel.x + vel.z * vel.z);

        // Elytra
        if (movState == EntityStateAnalyzer.EntityMovementState.ELYTRA_FLYING) return BehaviorState.ELYTRA_FLYING;

        // Knockback: horizontal speed spiked up compared to last tick
        double lastH = Math.sqrt(lastVelocity.x * lastVelocity.x + lastVelocity.z * lastVelocity.z);
        if (vel.length() - lastVelocity.length() > 0.3) return BehaviorState.TAKING_KNOCKBACK;

        // Eating / using item
        if (entity.isUsingItem()) return BehaviorState.EATING;

        // Jumping / Falling
        if (vel.y > 0.1 && !entity.isOnGround()) return BehaviorState.JUMPING;
        if (vel.y < -0.1 && !entity.isOnGround()) return BehaviorState.FALLING;

        // Standing still
        if (hSpeed < 0.05) return BehaviorState.STANDING_STILL;

        // Strafing vs sprinting: compare velocity direction to entity facing direction
        float yawRad = (float) Math.toRadians(entity.getYaw());
        // Forward unit vector (Minecraft yaw: 0=south, 90=west)
        double fwdX = -Math.sin(yawRad);
        double fwdZ =  Math.cos(yawRad);
        // Dot product of velocity with forward
        double dot = (vel.x * fwdX + vel.z * fwdZ);
        // Cross product Z component (positive = turning left)
        double cross = (fwdX * vel.z - fwdZ * vel.x);

        if (Math.abs(dot) > hSpeed * 0.7) return BehaviorState.SPRINTING_FORWARD;
        if (cross > 0) return BehaviorState.STRAFING_LEFT;
        return BehaviorState.STRAFING_RIGHT;
    }
}
