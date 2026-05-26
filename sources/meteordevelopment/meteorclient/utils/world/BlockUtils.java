package meteordevelopment.meteorclient.utils.world;

import javassist.compiler.TokenId;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.player.InstantRebreak;
import meteordevelopment.meteorclient.systems.modules.player.SpeedMine;
import meteordevelopment.meteorclient.utils.PreInit;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1292;
import net.minecraft.class_1294;
import net.minecraft.class_1747;
import net.minecraft.class_1799;
import net.minecraft.class_1887;
import net.minecraft.class_1893;
import net.minecraft.class_1922;
import net.minecraft.class_1937;
import net.minecraft.class_1944;
import net.minecraft.class_2189;
import net.minecraft.class_2199;
import net.minecraft.class_2231;
import net.minecraft.class_2237;
import net.minecraft.class_2244;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2269;
import net.minecraft.class_2304;
import net.minecraft.class_2323;
import net.minecraft.class_2338;
import net.minecraft.class_2349;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_2406;
import net.minecraft.class_2428;
import net.minecraft.class_243;
import net.minecraft.class_2482;
import net.minecraft.class_2510;
import net.minecraft.class_2533;
import net.minecraft.class_259;
import net.minecraft.class_2680;
import net.minecraft.class_2760;
import net.minecraft.class_2771;
import net.minecraft.class_2879;
import net.minecraft.class_3486;
import net.minecraft.class_3532;
import net.minecraft.class_3711;
import net.minecraft.class_3713;
import net.minecraft.class_3718;
import net.minecraft.class_3726;
import net.minecraft.class_3965;
import net.minecraft.class_5134;
import net.minecraft.class_5321;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/world/BlockUtils.class */
public class BlockUtils {
    public static boolean breaking;
    private static boolean breakingThisTick;
    private static final ThreadLocal<class_2338.class_2339> EXPOSED_POS = ThreadLocal.withInitial(class_2338.class_2339::new);

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/world/BlockUtils$MobSpawn.class */
    public enum MobSpawn {
        Never,
        Potential,
        Always
    }

    private BlockUtils() {
    }

    @PreInit
    public static void init() {
        MeteorClient.EVENT_BUS.subscribe(BlockUtils.class);
    }

    public static boolean place(class_2338 blockPos, FindItemResult findItemResult, int rotationPriority) {
        return place(blockPos, findItemResult, rotationPriority, true);
    }

    public static boolean place(class_2338 blockPos, FindItemResult findItemResult, boolean rotate, int rotationPriority) {
        return place(blockPos, findItemResult, rotate, rotationPriority, true);
    }

    public static boolean place(class_2338 blockPos, FindItemResult findItemResult, boolean rotate, int rotationPriority, boolean checkEntities) {
        return place(blockPos, findItemResult, rotate, rotationPriority, true, checkEntities);
    }

    public static boolean place(class_2338 blockPos, FindItemResult findItemResult, int rotationPriority, boolean checkEntities) {
        return place(blockPos, findItemResult, true, rotationPriority, true, checkEntities);
    }

    public static boolean place(class_2338 blockPos, FindItemResult findItemResult, boolean rotate, int rotationPriority, boolean swingHand, boolean checkEntities) {
        return place(blockPos, findItemResult, rotate, rotationPriority, swingHand, checkEntities, true);
    }

    public static boolean place(class_2338 blockPos, FindItemResult findItemResult, boolean rotate, int rotationPriority, boolean swingHand, boolean checkEntities, boolean swapBack) {
        if (findItemResult.isOffhand()) {
            return place(blockPos, class_1268.field_5810, MeteorClient.mc.field_1724.method_31548().field_7545, rotate, rotationPriority, swingHand, checkEntities, swapBack);
        }
        if (findItemResult.isHotbar()) {
            return place(blockPos, class_1268.field_5808, findItemResult.slot(), rotate, rotationPriority, swingHand, checkEntities, swapBack);
        }
        return false;
    }

    public static boolean place(class_2338 blockPos, class_1268 hand, int slot, boolean rotate, int rotationPriority, boolean swingHand, boolean checkEntities, boolean swapBack) {
        class_2338 neighbour;
        if (slot < 0 || slot > 8) {
            return false;
        }
        class_2248 toPlace = class_2246.field_10540;
        class_1799 i = hand == class_1268.field_5808 ? MeteorClient.mc.field_1724.method_31548().method_5438(slot) : MeteorClient.mc.field_1724.method_31548().method_5438(45);
        class_1747 class_1747VarMethod_7909 = i.method_7909();
        if (class_1747VarMethod_7909 instanceof class_1747) {
            class_1747 blockItem = class_1747VarMethod_7909;
            toPlace = blockItem.method_7711();
        }
        if (!canPlaceBlock(blockPos, checkEntities, toPlace)) {
            return false;
        }
        class_243 hitPos = class_243.method_24953(blockPos);
        class_2350 side = getPlaceSide(blockPos);
        if (side == null) {
            side = class_2350.field_11036;
            neighbour = blockPos;
        } else {
            neighbour = blockPos.method_10093(side);
            hitPos = hitPos.method_1031(((double) side.method_10148()) * 0.5d, ((double) side.method_10164()) * 0.5d, ((double) side.method_10165()) * 0.5d);
        }
        class_3965 bhr = new class_3965(hitPos, side.method_10153(), neighbour, false);
        if (rotate) {
            Rotations.rotate(Rotations.getYaw(hitPos), Rotations.getPitch(hitPos), rotationPriority, () -> {
                InvUtils.swap(slot, swapBack);
                interact(bhr, hand, swingHand);
                if (swapBack) {
                    InvUtils.swapBack();
                }
            });
            return true;
        }
        InvUtils.swap(slot, swapBack);
        interact(bhr, hand, swingHand);
        if (swapBack) {
            InvUtils.swapBack();
            return true;
        }
        return true;
    }

    public static void interact(class_3965 blockHitResult, class_1268 hand, boolean swing) {
        boolean wasSneaking = MeteorClient.mc.field_1724.method_5715();
        MeteorClient.mc.field_1724.method_5660(false);
        class_1269 result = MeteorClient.mc.field_1761.method_2896(MeteorClient.mc.field_1724, hand, blockHitResult);
        if (result.method_23665()) {
            if (swing) {
                MeteorClient.mc.field_1724.method_6104(hand);
            } else {
                MeteorClient.mc.method_1562().method_52787(new class_2879(hand));
            }
        }
        MeteorClient.mc.field_1724.method_5660(wasSneaking);
    }

    public static boolean canPlaceBlock(class_2338 blockPos, boolean checkEntities, class_2248 block) {
        if (blockPos != null && class_1937.method_25953(blockPos) && MeteorClient.mc.field_1687.method_8320(blockPos).method_45474()) {
            return !checkEntities || MeteorClient.mc.field_1687.method_8628(block.method_9564(), blockPos, class_3726.method_16194());
        }
        return false;
    }

    public static boolean canPlace(class_2338 blockPos, boolean checkEntities) {
        return canPlaceBlock(blockPos, checkEntities, class_2246.field_10540);
    }

    public static boolean canPlace(class_2338 blockPos) {
        return canPlace(blockPos, true);
    }

    public static class_2350 getPlaceSide(class_2338 blockPos) {
        class_243 lookVec = blockPos.method_46558().method_1020(MeteorClient.mc.field_1724.method_33571());
        double bestRelevancy = -1.7976931348623157E308d;
        class_2350 bestSide = null;
        for (class_2350 side : class_2350.values()) {
            class_2338 neighbor = blockPos.method_10093(side);
            class_2680 state = MeteorClient.mc.field_1687.method_8320(neighbor);
            if (!state.method_26215() && !isClickable(state.method_26204()) && state.method_26227().method_15769()) {
                double relevancy = side.method_10166().method_10172(lookVec.method_10216(), lookVec.method_10214(), lookVec.method_10215()) * ((double) side.method_10171().method_10181());
                if (relevancy > bestRelevancy) {
                    bestRelevancy = relevancy;
                    bestSide = side;
                }
            }
        }
        return bestSide;
    }

    public static class_2350 getClosestPlaceSide(class_2338 blockPos) {
        return getClosestPlaceSide(blockPos, MeteorClient.mc.field_1724.method_33571());
    }

    public static class_2350 getClosestPlaceSide(class_2338 blockPos, class_243 pos) {
        class_2350 closestSide = null;
        double closestDistance = Double.MAX_VALUE;
        for (class_2350 side : class_2350.values()) {
            class_2338 neighbor = blockPos.method_10093(side);
            class_2680 state = MeteorClient.mc.field_1687.method_8320(neighbor);
            if (!state.method_26215() && !isClickable(state.method_26204()) && state.method_26227().method_15769()) {
                double distance = pos.method_1028(neighbor.method_10263(), neighbor.method_10264(), neighbor.method_10260());
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestSide = side;
                }
            }
        }
        return closestSide;
    }

    @EventHandler(priority = TokenId.ABSTRACT)
    private static void onTickPre(TickEvent.Pre event) {
        breakingThisTick = false;
    }

    @EventHandler(priority = -300)
    private static void onTickPost(TickEvent.Post event) {
        if (!breakingThisTick && breaking) {
            breaking = false;
            if (MeteorClient.mc.field_1761 != null) {
                MeteorClient.mc.field_1761.method_2925();
            }
        }
    }

    public static boolean breakBlock(class_2338 blockPos, boolean swing) {
        if (!canBreak(blockPos, MeteorClient.mc.field_1687.method_8320(blockPos))) {
            return false;
        }
        class_2338 pos = blockPos instanceof class_2338.class_2339 ? new class_2338(blockPos) : blockPos;
        InstantRebreak ir = (InstantRebreak) Modules.get().get(InstantRebreak.class);
        if (ir != null && ir.isActive() && ir.blockPos.equals(pos) && ir.shouldMine()) {
            ir.sendPacket();
            return true;
        }
        if (MeteorClient.mc.field_1761.method_2923()) {
            MeteorClient.mc.field_1761.method_2902(pos, getDirection(blockPos));
        } else {
            MeteorClient.mc.field_1761.method_2910(pos, getDirection(blockPos));
        }
        if (swing) {
            MeteorClient.mc.field_1724.method_6104(class_1268.field_5808);
        } else {
            MeteorClient.mc.method_1562().method_52787(new class_2879(class_1268.field_5808));
        }
        breaking = true;
        breakingThisTick = true;
        return true;
    }

    public static boolean canBreak(class_2338 blockPos, class_2680 state) {
        return (MeteorClient.mc.field_1724.method_68878() || state.method_26214(MeteorClient.mc.field_1687, blockPos) >= 0.0f) && state.method_26218(MeteorClient.mc.field_1687, blockPos) != class_259.method_1073();
    }

    public static boolean canBreak(class_2338 blockPos) {
        return canBreak(blockPos, MeteorClient.mc.field_1687.method_8320(blockPos));
    }

    public static boolean canInstaBreak(class_2338 blockPos, float breakSpeed) {
        return MeteorClient.mc.field_1724.method_68878() || calcBlockBreakingDelta2(blockPos, breakSpeed) >= 1.0f;
    }

    public static boolean canInstaBreak(class_2338 blockPos) {
        class_2680 state = MeteorClient.mc.field_1687.method_8320(blockPos);
        return canInstaBreak(blockPos, MeteorClient.mc.field_1724.method_7351(state));
    }

    public static float calcBlockBreakingDelta2(class_2338 blockPos, float breakSpeed) {
        class_2680 state = MeteorClient.mc.field_1687.method_8320(blockPos);
        float f = state.method_26214(MeteorClient.mc.field_1687, blockPos);
        if (f == -1.0f) {
            return 0.0f;
        }
        int i = MeteorClient.mc.field_1724.method_7305(state) ? 30 : 100;
        return (breakSpeed / f) / i;
    }

    public static boolean isClickable(class_2248 block) {
        return (block instanceof class_2304) || (block instanceof class_2199) || (block instanceof class_2406) || (block instanceof class_3711) || (block instanceof class_3713) || (block instanceof class_3718) || (block instanceof class_2269) || (block instanceof class_2231) || (block instanceof class_2237) || (block instanceof class_2244) || (block instanceof class_2349) || (block instanceof class_2323) || (block instanceof class_2428) || (block instanceof class_2533);
    }

    public static MobSpawn isValidMobSpawn(class_2338 blockPos, boolean newMobSpawnLightLevel) {
        return isValidMobSpawn(blockPos, MeteorClient.mc.field_1687.method_8320(blockPos), newMobSpawnLightLevel ? 0 : 7);
    }

    public static MobSpawn isValidMobSpawn(class_2338 blockPos, class_2680 blockState, int spawnLightLimit) {
        if (!(blockState.method_26204() instanceof class_2189)) {
            return MobSpawn.Never;
        }
        class_2338 down = blockPos.method_10074();
        class_2680 downState = MeteorClient.mc.field_1687.method_8320(down);
        if (downState.method_26204() == class_2246.field_9987) {
            return MobSpawn.Never;
        }
        if (!topSurface(downState)) {
            if (downState.method_26220(MeteorClient.mc.field_1687, down) != class_259.method_1077()) {
                return MobSpawn.Never;
            }
            if (downState.method_26167()) {
                return MobSpawn.Never;
            }
        }
        return MeteorClient.mc.field_1687.method_8314(class_1944.field_9282, blockPos) > spawnLightLimit ? MobSpawn.Never : MeteorClient.mc.field_1687.method_8314(class_1944.field_9284, blockPos) > spawnLightLimit ? MobSpawn.Potential : MobSpawn.Always;
    }

    public static boolean topSurface(class_2680 blockState) {
        if ((blockState.method_26204() instanceof class_2482) && blockState.method_11654(class_2482.field_11501) == class_2771.field_12679) {
            return true;
        }
        return (blockState.method_26204() instanceof class_2510) && blockState.method_11654(class_2510.field_11572) == class_2760.field_12619;
    }

    public static class_2350 getDirection(class_2338 pos) {
        class_243 eyesPos = new class_243(MeteorClient.mc.field_1724.method_23317(), MeteorClient.mc.field_1724.method_23318() + ((double) MeteorClient.mc.field_1724.method_18381(MeteorClient.mc.field_1724.method_18376())), MeteorClient.mc.field_1724.method_23321());
        return ((double) pos.method_10264()) > eyesPos.field_1351 ? MeteorClient.mc.field_1687.method_8320(pos.method_10069(0, -1, 0)).method_45474() ? class_2350.field_11033 : MeteorClient.mc.field_1724.method_5735().method_10153() : !MeteorClient.mc.field_1687.method_8320(pos.method_10069(0, 1, 0)).method_45474() ? MeteorClient.mc.field_1724.method_5735().method_10153() : class_2350.field_11036;
    }

    public static boolean isExposed(class_2338 blockPos) {
        for (class_2350 direction : class_2350.values()) {
            if (!MeteorClient.mc.field_1687.method_8320(EXPOSED_POS.get().method_25505(blockPos, direction)).method_26225()) {
                return true;
            }
        }
        return false;
    }

    public static double getBreakDelta(int slot, class_2680 state) {
        float hardness = state.method_26214((class_1922) null, (class_2338) null);
        if (hardness == -1.0f) {
            return 0.0d;
        }
        class_1799 stack = (class_1799) MeteorClient.mc.field_1724.method_31548().meteor$getMain().get(slot);
        return (getBlockBreakingSpeed(slot, state, MeteorClient.mc.field_1724.method_24828()) / ((double) hardness)) / ((double) ((!state.method_29291() || stack.method_7924(state) > 1.0f) ? 30 : 100));
    }

    public static double getBreakDelta(double breakingSpeed, class_2680 state) {
        float hardness = state.method_26214((class_1922) null, (class_2338) null);
        if (hardness == -1.0f) {
            return 0.0d;
        }
        return (breakingSpeed / ((double) hardness)) / 30.0d;
    }

    public static double getBlockBreakingSpeed(int slot, class_2680 block, boolean isOnGround) {
        float f;
        class_1799 tool;
        int efficiency;
        double speed = ((class_1799) MeteorClient.mc.field_1724.method_31548().meteor$getMain().get(slot)).method_7924(block);
        if (speed > 1.0d && (efficiency = Utils.getEnchantmentLevel((tool = MeteorClient.mc.field_1724.method_31548().method_5438(slot)), (class_5321<class_1887>) class_1893.field_9131)) > 0 && !tool.method_7960()) {
            speed += (double) ((efficiency * efficiency) + 1);
        }
        if (class_1292.method_5576(MeteorClient.mc.field_1724)) {
            speed *= (double) (1.0f + ((class_1292.method_5575(MeteorClient.mc.field_1724) + 1) * 0.2f));
        }
        if (MeteorClient.mc.field_1724.method_6059(class_1294.field_5901)) {
            switch (MeteorClient.mc.field_1724.method_6112(class_1294.field_5901).method_5578()) {
                case 0:
                    f = 0.3f;
                    break;
                case 1:
                    f = 0.09f;
                    break;
                case 2:
                    f = 0.0027f;
                    break;
                default:
                    f = 8.1E-4f;
                    break;
            }
            float k = f;
            speed *= (double) k;
        }
        if (MeteorClient.mc.field_1724.method_5777(class_3486.field_15517)) {
            speed *= MeteorClient.mc.field_1724.method_45325(class_5134.field_51576);
        }
        if (!isOnGround) {
            speed /= 5.0d;
        }
        SpeedMine speedMine = (SpeedMine) Modules.get().get(SpeedMine.class);
        if (speedMine.isActive() && speedMine.mode.get() == SpeedMine.Mode.Normal && speedMine.filter(block.method_26204())) {
            speed *= speedMine.modifier.get().doubleValue();
        }
        return speed;
    }

    public static class_2338.class_2339 mutateAround(class_2338.class_2339 mutable, class_2338 origin, int xOffset, int yOffset, int zOffset) {
        return mutable.method_10103(origin.method_10263() + xOffset, origin.method_10264() + yOffset, origin.method_10260() + zOffset);
    }

    public static Iterable<class_2338> iterate(class_238 box) {
        return class_2338.method_10094(class_3532.method_15357(box.field_1323), class_3532.method_15357(box.field_1322), class_3532.method_15357(box.field_1321), class_3532.method_15357(box.field_1320), class_3532.method_15357(box.field_1325), class_3532.method_15357(box.field_1324));
    }
}
