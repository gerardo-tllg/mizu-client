package meteordevelopment.meteorclient.systems.modules.combat.predict;

/**
 * Jacobson/Karels RTT estimation — the same algorithm used by TCP since 1988,
 * adapted for Minecraft ping compensation.
 */
public class PingTracker {

    private static final double ALPHA              = 0.125;
    private static final double BETA               = 0.25;
    private static final double PHI                = 4.0;
    private static final double OUTLIER_THRESHOLD  = 3.0;

    public double  smoothedRTT  = 0;
    public double  deviation    = 0;
    private boolean initialized = false;

    public void addSample(double rttMs) {
        if (!initialized) {
            smoothedRTT = rttMs;
            deviation   = rttMs / 2.0;
            initialized = true;
            return;
        }
        double zScore = Math.abs(rttMs - smoothedRTT) / (deviation + 0.001);
        if (zScore > OUTLIER_THRESHOLD) return;
        double difference = rttMs - smoothedRTT;
        smoothedRTT = smoothedRTT + ALPHA * difference;
        deviation   = deviation   + BETA  * (Math.abs(difference) - deviation);
    }

    /** Conservative one-way latency estimate including jitter margin. */
    public double getSmoothedOneWayMs() {
        return (smoothedRTT + PHI * deviation) / 2.0;
    }

    /** How many 50 ms ticks to predict ahead given target's one-way latency. Clamped [1, 4]. */
    public int getPredictionTicks(double targetOneWayMs) {
        double totalMs = getSmoothedOneWayMs() + targetOneWayMs;
        return Math.max(1, Math.min(4, Math.round((float) (totalMs / 50.0))));
    }

    public boolean isReady() {
        return initialized;
    }
}
