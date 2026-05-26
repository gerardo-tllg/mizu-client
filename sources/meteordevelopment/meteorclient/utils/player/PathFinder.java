package meteordevelopment.meteorclient.utils.player;

import java.util.ArrayList;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1297;
import net.minecraft.class_2183;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2680;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/player/PathFinder.class */
public class PathFinder {
    private static final int PATH_AHEAD = 3;
    private static final int QUAD_1 = 1;
    private static final int QUAD_2 = 2;
    private static final int SOUTH = 0;
    private static final int NORTH = 180;
    private final ArrayList<PathBlock> path = new ArrayList<>(3);
    private class_1297 target;
    private PathBlock currentPathBlock;

    public PathBlock getNextPathBlock() {
        PathBlock nextBlock = new PathBlock(this, class_2338.method_49638(getNextStraightPos()));
        if (isSolidFloor(nextBlock.blockPos) && isAirAbove(nextBlock.blockPos)) {
            return nextBlock;
        }
        if (!isSolidFloor(nextBlock.blockPos) && isAirAbove(nextBlock.blockPos)) {
            int drop = getDrop(nextBlock.blockPos);
            if (getDrop(nextBlock.blockPos) < 3) {
                nextBlock = new PathBlock(this, new class_2338(nextBlock.blockPos.method_10263(), nextBlock.blockPos.method_10264() - drop, nextBlock.blockPos.method_10260()));
            }
        }
        return nextBlock;
    }

    public int getDrop(class_2338 pos) {
        int drop = 0;
        while (!isSolidFloor(pos) && drop < 3) {
            drop++;
            pos = new class_2338(pos.method_10263(), pos.method_10264() - 1, pos.method_10260());
        }
        return drop;
    }

    public boolean isAirAbove(class_2338 blockPos) {
        if (!getBlockStateAtPos(blockPos.method_10263(), blockPos.method_10264(), blockPos.method_10260()).method_26215()) {
            return false;
        }
        return getBlockStateAtPos(blockPos.method_10263(), blockPos.method_10264() + 1, blockPos.method_10260()).method_26215();
    }

    public class_243 getNextStraightPos() {
        class_243 nextPos = new class_243(MeteorClient.mc.field_1724.method_23317(), MeteorClient.mc.field_1724.method_23318(), MeteorClient.mc.field_1724.method_23321());
        double d = 1.0d;
        while (true) {
            double multiplier = d;
            if (nextPos == MeteorClient.mc.field_1724.method_19538()) {
                nextPos = new class_243((int) (MeteorClient.mc.field_1724.method_23317() + (multiplier * Math.cos(Math.toRadians(MeteorClient.mc.field_1724.method_36454())))), (int) MeteorClient.mc.field_1724.method_23318(), (int) (MeteorClient.mc.field_1724.method_23321() + (multiplier * Math.sin(Math.toRadians(MeteorClient.mc.field_1724.method_36454())))));
                d = multiplier + 0.1d;
            } else {
                return nextPos;
            }
        }
    }

    public int getYawToTarget() {
        int yaw;
        if (this.target == null || MeteorClient.mc.field_1724 == null) {
            return Integer.MAX_VALUE;
        }
        class_243 tPos = this.target.method_19538();
        class_243 pPos = MeteorClient.mc.field_1724.method_19538();
        int direction = getDirection();
        double tan = (tPos.field_1350 - pPos.field_1350) / (tPos.field_1352 - pPos.field_1352);
        if (direction == 1) {
            yaw = (int) (1.5707963267948966d - Math.atan(tan));
        } else if (direction == 2) {
            yaw = (int) ((-1.5707963267948966d) - Math.atan(tan));
        } else {
            return direction;
        }
        return yaw;
    }

    public int getDirection() {
        if (this.target == null || MeteorClient.mc.field_1724 == null) {
            return 0;
        }
        class_243 targetPos = this.target.method_19538();
        class_243 playerPos = MeteorClient.mc.field_1724.method_19538();
        if (targetPos.field_1352 == playerPos.field_1352 && targetPos.field_1350 > playerPos.field_1350) {
            return 0;
        }
        if (targetPos.field_1352 == playerPos.field_1352 && targetPos.field_1350 < playerPos.field_1350) {
            return 180;
        }
        if (targetPos.field_1352 < playerPos.field_1352) {
            return 1;
        }
        if (targetPos.field_1352 > playerPos.field_1352) {
            return 2;
        }
        return 0;
    }

    public class_2680 getBlockStateAtPos(class_2338 pos) {
        if (MeteorClient.mc.field_1687 != null) {
            return MeteorClient.mc.field_1687.method_8320(pos);
        }
        return null;
    }

    public class_2680 getBlockStateAtPos(int x, int y, int z) {
        if (MeteorClient.mc.field_1687 != null) {
            return MeteorClient.mc.field_1687.method_8320(new class_2338(x, y, z));
        }
        return null;
    }

    public class_2248 getBlockAtPos(class_2338 pos) {
        if (MeteorClient.mc.field_1687 != null) {
            return MeteorClient.mc.field_1687.method_8320(pos).method_26204();
        }
        return null;
    }

    public boolean isSolidFloor(class_2338 blockPos) {
        return isAir(getBlockAtPos(blockPos));
    }

    public boolean isAir(class_2248 block) {
        return block == class_2246.field_10124;
    }

    public boolean isWater(class_2248 block) {
        return block == class_2246.field_10382;
    }

    public void lookAtDestination(PathBlock pathBlock) {
        if (MeteorClient.mc.field_1724 != null) {
            MeteorClient.mc.field_1724.method_5702(class_2183.class_2184.field_9851, new class_243(pathBlock.blockPos.method_10263(), pathBlock.blockPos.method_10264() + MeteorClient.mc.field_1724.method_5751(), pathBlock.blockPos.method_10260()));
        }
    }

    @EventHandler
    private void moveEventListener(PlayerMoveEvent event) {
        if (this.target != null && MeteorClient.mc.field_1724 != null) {
            if (!PlayerUtils.isWithin(this.target, 3.0d)) {
                if (this.currentPathBlock == null) {
                    this.currentPathBlock = getNextPathBlock();
                }
                if (MeteorClient.mc.field_1724.method_19538().method_1025(new class_243(this.currentPathBlock.blockPos.method_10263(), this.currentPathBlock.blockPos.method_10264(), this.currentPathBlock.blockPos.method_10260())) < 0.01d) {
                    this.currentPathBlock = getNextPathBlock();
                }
                lookAtDestination(this.currentPathBlock);
                if (!MeteorClient.mc.field_1690.field_1894.method_1434()) {
                    MeteorClient.mc.field_1690.field_1894.method_23481(true);
                    return;
                }
                return;
            }
            if (MeteorClient.mc.field_1690.field_1894.method_1434()) {
                MeteorClient.mc.field_1690.field_1894.method_23481(false);
            }
            this.path.clear();
            this.currentPathBlock = null;
        }
    }

    public void initiate(class_1297 entity) {
        this.target = entity;
        if (this.target != null) {
            this.currentPathBlock = getNextPathBlock();
        }
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    public void disable() {
        this.target = null;
        this.path.clear();
        if (MeteorClient.mc.field_1690.field_1894.method_1434()) {
            MeteorClient.mc.field_1690.field_1894.method_23481(false);
        }
        MeteorClient.EVENT_BUS.unsubscribe(this);
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/player/PathFinder$PathBlock.class */
    public class PathBlock {
        public final class_2248 block;
        public final class_2338 blockPos;
        public final class_2680 blockState;
        public double yaw;

        public PathBlock(final PathFinder this$0, class_2248 b, class_2338 pos, class_2680 state) {
            this.block = b;
            this.blockPos = pos;
            this.blockState = state;
        }

        public PathBlock(final PathFinder this$0, class_2248 b, class_2338 pos) {
            this.block = b;
            this.blockPos = pos;
            this.blockState = this$0.getBlockStateAtPos(this.blockPos);
        }

        public PathBlock(final PathFinder this$0, class_2338 pos) {
            this.blockPos = pos;
            this.block = this$0.getBlockAtPos(pos);
            this.blockState = this$0.getBlockStateAtPos(this.blockPos);
        }
    }
}
