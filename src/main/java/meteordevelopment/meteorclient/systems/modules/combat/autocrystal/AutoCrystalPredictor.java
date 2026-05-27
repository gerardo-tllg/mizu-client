package meteordevelopment.meteorclient.systems.modules.combat.autocrystal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

public class AutoCrystalPredictor {
    private AutoCrystalPredictor() {}

    /**
     * Predicts entity position [ticks] game ticks into the future.
     * Uses simplified physics matching vanilla client movement.
     * Falls back to current position if prediction exceeds a sanity bound.
     */
    public static Vec3d predictPosition(LivingEntity entity, int ticks) {
        Vec3d startPos = entity.getPos();
        Vec3d vel = entity.getVelocity();

        double velX = vel.x;
        double velY = vel.y;
        double velZ = vel.z;
        double posX = startPos.x;
        double posY = startPos.y;
        double posZ = startPos.z;

        if (entity.isGliding()) {
            // Elytra simulation
            float pitchRad = (float) Math.toRadians(entity.getPitch());
            float yawRad   = (float) Math.toRadians(entity.getYaw());
            double lookX = -Math.sin(yawRad) * Math.cos(pitchRad);
            double lookY = -Math.sin(pitchRad);
            double lookZ =  Math.cos(yawRad) * Math.cos(pitchRad);

            for (int i = 0; i < ticks; i++) {
                double speed = Math.sqrt(velX * velX + velY * velY + velZ * velZ);
                double liftFactor = Math.cos(pitchRad) * Math.cos(pitchRad) * Math.min(1.0, speed / 0.4);
                velY += -0.08 + liftFactor * 0.06;
                velX *= 0.99;
                velY *= 0.99;
                velZ *= 0.99;
                posX += velX;
                posY += velY;
                posZ += velZ;
            }
        } else {
            // Walking / sprinting simulation
            for (int i = 0; i < ticks; i++) {
                velX *= 0.91;
                velZ *= 0.91;
                velY = (velY - 0.08) * 0.98;
                posX += velX;
                posY += velY;
                posZ += velZ;
            }
        }

        Vec3d predicted = new Vec3d(posX, posY, posZ);

        // Sanity check — if prediction is unreasonably far, fall back silently
        double maxReasonableDistance = entity.getVelocity().length() * ticks * 2.0;
        if (predicted.distanceTo(startPos) > maxReasonableDistance) {
            return startPos;
        }

        return predicted;
    }
}
