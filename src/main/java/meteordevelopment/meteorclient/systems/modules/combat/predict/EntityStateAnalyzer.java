package meteordevelopment.meteorclient.systems.modules.combat.predict;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class EntityStateAnalyzer {
    private EntityStateAnalyzer() {}

    public enum EntityMovementState {
        NORMAL_GROUND, NORMAL_AIR, SWIMMING, FLUID_TRAPPED,
        VEHICLE_BOAT, VEHICLE_HORSE, VEHICLE_MINECART, VEHICLE_OTHER,
        PHASED_IN_BLOCK, KNOCKBACK, LEVITATING, SLOW_FALLING,
        LIKELY_LAGGING, ELYTRA_FLYING, CHORUS_TELEPORTING
    }

    // Per-entity last known velocity for knockback/teleport detection
    private static final Map<UUID, Vec3d> lastVelocities = new HashMap<>();
    private static final Map<UUID, Vec3d> lastPositions  = new HashMap<>();

    /**
     * Call once per tick per entity to keep velocity/position history fresh.
     */
    public static void updateHistory(LivingEntity entity) {
        lastVelocities.put(entity.getUuid(), entity.getVelocity());
        lastPositions.put(entity.getUuid(), entity.getPos());
    }

    /** Analyze entity movement state. Priority order determines which state wins. */
    public static EntityMovementState analyzeState(LivingEntity entity) {
        if (mc.world == null) return EntityMovementState.NORMAL_GROUND;

        // 1. Vehicle check (highest priority)
        if (entity.getVehicle() != null) {
            if (entity.getVehicle() instanceof BoatEntity)            return EntityMovementState.VEHICLE_BOAT;
            if (entity.getVehicle() instanceof AbstractHorseEntity)   return EntityMovementState.VEHICLE_HORSE;
            if (entity.getVehicle() instanceof AbstractMinecartEntity) return EntityMovementState.VEHICLE_MINECART;
            return EntityMovementState.VEHICLE_OTHER;
        }

        // 2. Elytra
        if (entity.isGliding()) return EntityMovementState.ELYTRA_FLYING;

        Vec3d vel = entity.getVelocity();
        UUID uuid = entity.getUuid();

        // 3. Knockback: velocity magnitude increased by > 0.4 since last tick
        Vec3d lastVel = lastVelocities.get(uuid);
        if (lastVel != null) {
            double velDelta = vel.length() - lastVel.length();
            if (velDelta > 0.4) return EntityMovementState.KNOCKBACK;
        }

        // 4. Levitating
        if (entity.hasStatusEffect(StatusEffects.LEVITATION)) return EntityMovementState.LEVITATING;

        // 5. Slow falling
        if (entity.hasStatusEffect(StatusEffects.SLOW_FALLING)) return EntityMovementState.SLOW_FALLING;

        // 6. Chorus teleport: detect large unexpected position jump
        Vec3d lastPos = lastPositions.get(uuid);
        if (lastPos != null && entity.getPos().distanceTo(lastPos) > 8.0 && vel.length() < 1.0) {
            return EntityMovementState.CHORUS_TELEPORTING;
        }

        // 7. Phased in block
        try {
            var blockState = mc.world.getBlockState(entity.getBlockPos());
            if (blockState.isSolidBlock(mc.world, entity.getBlockPos())) {
                return EntityMovementState.PHASED_IN_BLOCK;
            }
        } catch (Exception ignored) {}

        // 8. Swimming
        if (entity.isSwimming()) return EntityMovementState.SWIMMING;

        // 9. Fluid trapped: in fluid, low speed, not swimming
        double hSpeed = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
        if ((entity.isTouchingWater() || entity.isInLava()) && hSpeed < 0.05) {
            return EntityMovementState.FLUID_TRAPPED;
        }

        // 10. Normal states
        if (entity.isOnGround()) return EntityMovementState.NORMAL_GROUND;
        return EntityMovementState.NORMAL_AIR;
    }

    /**
     * Speed/Slowness horizontal velocity multiplier from status effects.
     * Applied to vx/vz before physics step.
     */
    public static float getSpeedMultiplier(LivingEntity entity) {
        StatusEffectInstance speed = entity.getStatusEffect(StatusEffects.SPEED);
        if (speed != null) {
            int amp = speed.getAmplifier(); // 0=SpeedI, 1=SpeedII, 2=SpeedIII
            if (amp == 0) return 1.2f;
            if (amp == 1) return 1.4f;
            return 1.6f;
        }
        StatusEffectInstance slowness = entity.getStatusEffect(StatusEffects.SLOWNESS);
        if (slowness != null) {
            int amp = slowness.getAmplifier();
            if (amp == 0) return 0.85f;
            if (amp == 1) return 0.70f;
            return 0.55f;
        }
        return 1.0f;
    }

    /**
     * Jump boost additive bonus (added to vy when JUMPING state detected).
     */
    public static float getJumpBoostBonus(LivingEntity entity) {
        StatusEffectInstance jumpBoost = entity.getStatusEffect(StatusEffects.JUMP_BOOST);
        if (jumpBoost == null) return 0f;
        int amp = jumpBoost.getAmplifier();
        return amp == 0 ? 0.1f : 0.2f;
    }

    /** Clean up stale history entries (call periodically). */
    public static void cleanup(UUID uuid) {
        lastVelocities.remove(uuid);
        lastPositions.remove(uuid);
    }
}
