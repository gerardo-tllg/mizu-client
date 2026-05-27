package meteordevelopment.meteorclient.systems.modules.combat.predict;

import java.util.HashMap;
import java.util.UUID;

/**
 * Tracks smoothed per-target ping, updated at most once per second from the
 * server player list. Smoothing coefficient: 70% old / 30% new sample.
 */
public class TargetPingTracker {

    private final HashMap<UUID, Double> smoothedTargetPing = new HashMap<>();
    private final HashMap<UUID, Long>   lastUpdateTime     = new HashMap<>();

    /** Feed a raw latency sample for the given player. Rate-limited to ~1 Hz. */
    public void update(UUID uuid, int rawPingMs) {
        Long lastUpdate = lastUpdateTime.get(uuid);
        long now = System.currentTimeMillis();
        if (lastUpdate != null && (now - lastUpdate) < 800) return;
        double current = smoothedTargetPing.getOrDefault(uuid, (double) rawPingMs);
        smoothedTargetPing.put(uuid, 0.7 * current + 0.3 * rawPingMs);
        lastUpdateTime.put(uuid, now);
    }

    /** Returns estimated one-way latency (half of round-trip). Defaults to 100 ms if unknown. */
    public double getOneWayMs(UUID uuid) {
        return smoothedTargetPing.getOrDefault(uuid, 100.0) / 2.0;
    }

    public void remove(UUID uuid) {
        smoothedTargetPing.remove(uuid);
        lastUpdateTime.remove(uuid);
    }

    public void clear() {
        smoothedTargetPing.clear();
        lastUpdateTime.clear();
    }
}
