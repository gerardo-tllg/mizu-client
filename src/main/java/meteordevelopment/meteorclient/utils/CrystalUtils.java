package meteordevelopment.meteorclient.utils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class CrystalUtils {
    public static double calculateCrystalDamage(Vec3d crystalPos, PlayerEntity player) {
        return calculateCrystalDamage(crystalPos, player, false);
    }

    public static double calculateCrystalDamage(Vec3d crystalPos, PlayerEntity player, boolean ignoreWalls) {
        if (mc.world == null || player == null) return 0.0;

        double distance = Math.sqrt(player.squaredDistanceTo(crystalPos));
        if (distance > 12.0) return 0.0;

        double explosionPower = 6.0;
        double impact = (1.0 - (distance / 12.0)) * getExposure(crystalPos, player, ignoreWalls);
        double damage = ((impact * impact + impact) / 2.0 * 7.0 * explosionPower + 1.0);

        damage = MathHelper.clamp(damage, 0.0, 36.0);
        return applyDamageReduction(damage, player);
    }

    private static float getExposure(Vec3d source, PlayerEntity player, boolean ignoreWalls) {
        // If ignoring walls, return maximum exposure
        if (ignoreWalls) return 1.0f;

        Box box = player.getBoundingBox();
        double xDiff = box.maxX - box.minX;
        double yDiff = box.maxY - box.minY;
        double zDiff = box.maxZ - box.minZ;

        double xStep = 1.0 / (xDiff * 2.0 + 1.0);
        double yStep = 1.0 / (yDiff * 2.0 + 1.0);
        double zStep = 1.0 / (zDiff * 2.0 + 1.0);

        if (xStep <= 0 || yStep <= 0 || zStep <= 0) return 0.0f;

        int misses = 0;
        int hits = 0;

        // Apply offsets to center the sampling grid
        double xOffset = (1.0 - Math.floor(1.0 / xStep) * xStep) * 0.5;
        double zOffset = (1.0 - Math.floor(1.0 / zStep) * zStep) * 0.5;

        // Scale steps by box dimensions
        xStep = xStep * xDiff;
        yStep = yStep * yDiff;
        zStep = zStep * zDiff;

        double startX = box.minX + xOffset;
        double startY = box.minY;
        double startZ = box.minZ + zOffset;
        double endX = box.maxX + xOffset;
        double endY = box.maxY;
        double endZ = box.maxZ + zOffset;

        for (double x = startX; x <= endX; x += xStep) {
            for (double y = startY; y <= endY; y += yStep) {
                for (double z = startZ; z <= endZ; z += zStep) {
                    Vec3d point = new Vec3d(x, y, z);
                    if (!isOccluded(source, point)) misses++;

                    hits++;
                }
            }
        }

        return (float) misses / hits;
    }

    private static boolean isOccluded(Vec3d start, Vec3d end) {
        BlockPos startPos = new BlockPos((int)start.x, (int)start.y, (int)start.z);
        BlockPos endPos = new BlockPos((int)end.x, (int)end.y, (int)end.z);

        if (!startPos.equals(endPos)) {
            Vec3d direction = end.subtract(start).normalize();
            double distance = start.distanceTo(end);
            double step = 0.1;

            for (double d = 0; d <= distance; d += step) {
                Vec3d point = start.add(direction.multiply(d));
                BlockPos pos = new BlockPos((int)point.x, (int)point.y, (int)point.z);

                if (mc.world.getBlockState(pos).isOpaque()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static double applyDamageReduction(double damage, PlayerEntity player) {
        double armorValue = player.getArmor();
        double toughness = 2.0;
        double reductionFactor = armorValue * (1.0 - damage / (damage + toughness + 8.0));
        return Math.max(damage - reductionFactor / 2.0, damage * 0.2);
    }
}
