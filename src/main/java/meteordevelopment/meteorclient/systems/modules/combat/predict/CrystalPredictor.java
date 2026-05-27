package meteordevelopment.meteorclient.systems.modules.combat.predict;

import static meteordevelopment.meteorclient.MeteorClient.mc;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Coordinates Kalman + Markov + KNN prediction layers per entity.
 * One instance lives on AutoCrystal.
 *
 * Limits: maximum MAX_PROFILES active target profiles at once.
 * When a new target arrives and we are at capacity, the farthest tracked
 * player is evicted to make room (stale / out-of-world entries are evicted first).
 * Cleanup runs every 20 ticks (~1 s).
 */
public class CrystalPredictor {

    private static final int MAX_PROFILES = 10;

    private final HashMap<UUID, KalmanPredictor>     kalmanMap = new HashMap<>();
    private final HashMap<UUID, MarkovBehaviorModel> markovMap = new HashMap<>();
    private final HashMap<UUID, KNNPatternMatcher>   knnMap    = new HashMap<>();
    private final HashMap<UUID, Long>                lastSeen  = new HashMap<>();
    private int tickCount = 0;

    // Settable from AutoCrystal each tick before calling predict()
    public boolean useKalman           = true;
    public boolean useMarkov           = true;
    public boolean useKNN              = false;
    public boolean accountForTargetPing = true;

    // Ping trackers
    public final PingTracker       myPingTracker     = new PingTracker();
    public final TargetPingTracker targetPingTracker = new TargetPingTracker();

    // Last computed effective prediction ticks — exposed for debug display
    public int lastPredictionTicks = 2;

    // =========================================================================
    // Update pipeline
    // =========================================================================

    /** Call once per tick per tracked entity. Updates all three layers. */
    public void onEntityUpdate(LivingEntity entity) {
        try {
            UUID uuid = entity.getUuid();

            // Enforce profile cap for new entries
            if (!kalmanMap.containsKey(uuid) && kalmanMap.size() >= MAX_PROFILES) {
                if (!evictFarthestFor(entity)) return;
            }

            Vec3d pos = entity.getPos();
            Vec3d vel = entity.getVelocity();

            EntityStateAnalyzer.EntityMovementState state =
                EntityStateAnalyzer.analyzeState(entity);
            float speedMult = EntityStateAnalyzer.getSpeedMultiplier(entity);
            float jumpBonus = EntityStateAnalyzer.getJumpBoostBonus(entity);

            // Kalman
            KalmanPredictor kalman = kalmanMap.computeIfAbsent(uuid, k -> new KalmanPredictor());
            if (!kalman.isInitialized()) {
                kalman.init(pos, vel);
            } else {
                kalman.predict(state, speedMult, jumpBonus);
                kalman.update(pos, vel);
            }
            if (state == EntityStateAnalyzer.EntityMovementState.KNOCKBACK) {
                kalman.increaseUncertainty(2.0);
            }

            // Markov
            markovMap.computeIfAbsent(uuid, k -> new MarkovBehaviorModel()).update(entity, state);

            // KNN
            knnMap.computeIfAbsent(uuid, k -> new KNNPatternMatcher()).addObservation(pos, vel);

            EntityStateAnalyzer.updateHistory(entity);
            lastSeen.put(uuid, System.currentTimeMillis());

        } catch (Exception ignored) {}
    }

    /** Call once per tick. Drives cleanup and ping sampling. */
    public void tick() {
        tickCount++;
        if (tickCount % 20 == 0) cleanup();

        if (mc == null || mc.player == null || mc.getNetworkHandler() == null) return;

        // Sample own RTT
        try {
            var myEntry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            if (myEntry != null) myPingTracker.addSample(myEntry.getLatency());
        } catch (Exception ignored) {}

        // Sample target pings (rate-limited inside TargetPingTracker)
        try {
            for (UUID uuid : lastSeen.keySet()) {
                var entry = mc.getNetworkHandler().getPlayerListEntry(uuid);
                if (entry != null) targetPingTracker.update(uuid, entry.getLatency());
            }
        } catch (Exception ignored) {}
    }

    /** Remove profiles not updated in the last 10 s. */
    public void cleanup() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<UUID, Long>> it = lastSeen.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Long> entry = it.next();
            if (now - entry.getValue() > 10_000L) {
                removeProfile(entry.getKey());
                it.remove();
            }
        }
    }

    /** Clear all state. Call on module deactivate/reactivate. */
    public void reset() {
        kalmanMap.clear();
        markovMap.clear();
        knnMap.clear();
        lastSeen.clear();
        targetPingTracker.clear();
        tickCount = 0;
    }

    // =========================================================================
    // Prediction
    // =========================================================================

    /** Never throws — falls back to dead reckoning on any error. */
    public Vec3d predictSafe(LivingEntity entity, int ticks) {
        try {
            return predict(entity, ticks);
        } catch (Exception e) {
            Vec3d pos = entity.getPos();
            Vec3d vel = entity.getVelocity();
            return pos.add(vel.multiply(ticks));
        }
    }

    private Vec3d predict(LivingEntity entity, int ticks) {
        UUID  uuid       = entity.getUuid();
        Vec3d currentPos = entity.getPos();
        Vec3d vel        = entity.getVelocity();

        EntityStateAnalyzer.EntityMovementState state =
            EntityStateAnalyzer.analyzeState(entity);

        if (state == EntityStateAnalyzer.EntityMovementState.CHORUS_TELEPORTING ||
            state == EntityStateAnalyzer.EntityMovementState.LIKELY_LAGGING) {
            return currentPos;
        }

        // Tick count: dynamic Jacobson/Karels or manual fallback
        int effectiveTicks;
        if (accountForTargetPing && myPingTracker.isReady()) {
            double targetOneWay = targetPingTracker.getOneWayMs(uuid);
            effectiveTicks = myPingTracker.getPredictionTicks(targetOneWay);
        } else {
            effectiveTicks = ticks;
        }
        lastPredictionTicks = effectiveTicks;

        float   speedMult = EntityStateAnalyzer.getSpeedMultiplier(entity);
        boolean knockback = (state == EntityStateAnalyzer.EntityMovementState.KNOCKBACK);

        // Layer 1: Kalman
        Vec3d kalmanPred;
        if (useKalman) {
            KalmanPredictor kalman = kalmanMap.get(uuid);
            kalmanPred = (kalman != null && kalman.isInitialized())
                ? kalman.getPredictedPosition(effectiveTicks, state, speedMult)
                : currentPos.add(vel.multiply(effectiveTicks));
        } else {
            kalmanPred = currentPos.add(vel.multiply(effectiveTicks));
        }

        // Layer 2: Markov behavioral bias
        Vec3d biasedPred = kalmanPred;
        if (useMarkov) {
            MarkovBehaviorModel markov = markovMap.get(uuid);
            if (markov != null) biasedPred = markov.applyBehavioralBias(kalmanPred, entity);
        }

        // Layer 3: KNN blend
        Vec3d finalPred = biasedPred;
        if (useKNN) {
            KNNPatternMatcher knn = knnMap.get(uuid);
            if (knn != null) {
                Vec3d knnPred = knn.predictNextPosition(effectiveTicks);
                finalPred = knn.blendWithKalman(biasedPred, knnPred, knn.getHistoryCount());
            }
        }

        // Knockback: reduce confidence to 20% prediction / 80% current
        if (knockback) {
            finalPred = new Vec3d(
                currentPos.x * 0.8 + finalPred.x * 0.2,
                currentPos.y * 0.8 + finalPred.y * 0.2,
                currentPos.z * 0.8 + finalPred.z * 0.2
            );
        }

        // Sanity bound
        double maxDist = Math.max(vel.length() * effectiveTicks * 3.0, 0.5);
        if (finalPred.distanceTo(currentPos) > maxDist) return currentPos;

        return finalPred;
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private void removeProfile(UUID uuid) {
        kalmanMap.remove(uuid);
        markovMap.remove(uuid);
        knnMap.remove(uuid);
        EntityStateAnalyzer.cleanup(uuid);
        targetPingTracker.remove(uuid);
    }

    /**
     * Evicts the farthest tracked player if newEntity is closer (or if a stale entry exists).
     * Returns true if a slot was freed, false if newEntity is farther than all tracked players.
     */
    private boolean evictFarthestFor(LivingEntity newEntity) {
        if (mc == null || mc.world == null || mc.player == null) return false;
        double newDistSq      = newEntity.squaredDistanceTo(mc.player);
        UUID   farthestUuid   = null;
        double farthestDistSq = newDistSq; // only evict if tracked player is farther

        for (UUID tracked : kalmanMap.keySet()) {
            double dist = Double.MAX_VALUE;
            for (PlayerEntity p : mc.world.getPlayers()) {
                if (tracked.equals(p.getUuid())) { dist = p.squaredDistanceTo(mc.player); break; }
            }
            if (dist > farthestDistSq) { farthestDistSq = dist; farthestUuid = tracked; }
        }

        if (farthestUuid != null) {
            removeProfile(farthestUuid);
            lastSeen.remove(farthestUuid);
            return true;
        }
        return false;
    }
}
