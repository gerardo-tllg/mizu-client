package meteordevelopment.meteorclient.systems.modules.combat.autocrystal;

import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoCrystalUtil {
    public static BlockHitResult getPlaceBlockHitResult(BlockPos blockPos) {
        Direction dir = getPlaceOnDirection(blockPos);
        Vec3d pos = getPosForDir(blockPos, dir);
        return new BlockHitResult(pos, dir, blockPos, true);
    }

    private static Direction getPlaceOnDirection(BlockPos blockPos) {
        if (blockPos != null && MeteorClient.mc.world != null && MeteorClient.mc.player != null) {
            Direction bestdir = null;
            double bestDist = -1.0;

            for (Direction dir : Direction.values()) {
                Vec3d pos = getPosForDir(blockPos, dir);
                double dist = MeteorClient.mc.player.getEyePos().squaredDistanceTo(pos);
                if (dist >= 0.0 && (bestDist < 0.0 || dist < bestDist)) {
                    bestdir = dir;
                    bestDist = dist;
                }
            }

            return bestdir;
        } else {
            return null;
        }
    }

    private static Vec3d getPosForDir(BlockPos blockPos, Direction dir) {
        Vec3d offset = new Vec3d((double) dir.getOffsetX() / 2.0, (double) dir.getOffsetY() / 2.0, (double) dir.getOffsetZ() / 2.0);
        return blockPos.toCenterPos().add(offset);
    }
}
