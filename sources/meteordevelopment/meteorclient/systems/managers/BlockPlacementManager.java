package meteordevelopment.meteorclient.systems.managers;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.systems.config.AntiCheatConfig;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.meteorclient.utils.world.TickRate;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1268;
import net.minecraft.class_1792;
import net.minecraft.class_1937;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_2626;
import net.minecraft.class_2680;
import net.minecraft.class_2846;
import net.minecraft.class_2868;
import net.minecraft.class_2885;
import net.minecraft.class_3726;
import net.minecraft.class_3965;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/managers/BlockPlacementManager.class */
public class BlockPlacementManager {
    private int packetsSent;
    private final AntiCheatConfig antiCheatConfig = AntiCheatConfig.get();
    private final Map<class_2338, Long> placeCooldowns = new ConcurrentHashMap();
    private boolean locked = false;
    private long lastSentPacketTimestamp = -1;

    public BlockPlacementManager() {
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    public boolean beginPlacement(class_2338 position, class_2680 state, class_1792 item) {
        if (!checkLimit(System.currentTimeMillis(), false) || this.locked || !checkPlacement(item, position, state) || !MeteorClient.SWAP.beginSwap(item, true)) {
            return false;
        }
        this.locked = true;
        return true;
    }

    public boolean beginPlacement(List<class_2338> positions, class_1792 item) {
        if (!checkLimit(System.currentTimeMillis(), false) || this.locked || positions.stream().filter(x -> {
            return checkPlacement(item, x);
        }).findAny().isEmpty() || !MeteorClient.SWAP.beginSwap(item, true)) {
            return false;
        }
        this.locked = true;
        return true;
    }

    public boolean placeBlock(class_1792 item, class_2338 blockPos) {
        return placeBlock(item, blockPos, MeteorClient.mc.field_1687.method_8320(blockPos));
    }

    public boolean placeBlock(class_1792 item, class_2338 blockPos, class_2680 state) {
        class_2338 neighbour;
        double dMethod_55754;
        long currentTime = System.currentTimeMillis();
        if (this.placeCooldowns.values().stream().filter(x -> {
            return currentTime - x.longValue() <= 1000;
        }).count() >= this.antiCheatConfig.blocksPerSecondCap.get().doubleValue()) {
            return false;
        }
        if (MeteorClient.mc.field_1724 != null) {
            if (this.antiCheatConfig.blockPlaceRange.get().doubleValue() > 0.0d) {
                dMethod_55754 = this.antiCheatConfig.blockPlaceRange.get().doubleValue();
            } else {
                dMethod_55754 = MeteorClient.mc.field_1724.method_55754();
            }
            double maxRange = dMethod_55754;
            if (Math.sqrt(blockPos.method_19770(MeteorClient.mc.field_1724.method_33571())) > maxRange) {
                return false;
            }
        }
        if (!checkPlacement(item, blockPos, state)) {
            return false;
        }
        class_2350 dir = BlockUtils.getPlaceSide(blockPos);
        class_243 hitPos = blockPos.method_46558();
        class_2350 airPlaceDir = class_2350.field_11036;
        if (dir == null) {
            double closestDist = Double.MAX_VALUE;
            for (int range = 1; range <= 3; range++) {
                for (class_2350 searchDir : class_2350.values()) {
                    class_2338 candidate = blockPos.method_10079(searchDir, range);
                    if (!MeteorClient.mc.field_1687.method_8320(candidate).method_26215()) {
                        double dist = candidate.method_19770(MeteorClient.mc.field_1724.method_33571());
                        if (dist < closestDist) {
                            closestDist = dist;
                            if (range == 1) {
                                dir = searchDir;
                            }
                            airPlaceDir = searchDir;
                        }
                    }
                }
                if (dir != null) {
                    break;
                }
            }
            if (dir != null) {
                neighbour = blockPos.method_10093(dir);
                hitPos = hitPos.method_1031(((double) dir.method_10148()) * 0.5d, ((double) dir.method_10164()) * 0.5d, ((double) dir.method_10165()) * 0.5d);
            } else {
                neighbour = blockPos;
            }
        } else {
            neighbour = blockPos.method_10093(dir);
            hitPos = hitPos.method_1031(((double) dir.method_10148()) * 0.5d, ((double) dir.method_10164()) * 0.5d, ((double) dir.method_10165()) * 0.5d);
        }
        if (this.antiCheatConfig.blockRotatePlace.get().booleanValue()) {
            MeteorClient.ROTATION.snapAt(hitPos);
        }
        Long lastPlaceTime = this.placeCooldowns.get(blockPos);
        if ((lastPlaceTime != null && currentTime - lastPlaceTime.longValue() < this.antiCheatConfig.blockPlacePerBlockCooldown.get().doubleValue() * 1000.0d) || !checkLimit(currentTime, true)) {
            return false;
        }
        this.placeCooldowns.put(blockPos, Long.valueOf(currentTime));
        boolean shouldAirPlace = dir == null || this.antiCheatConfig.forceAirPlace.get().booleanValue();
        boolean useGrimAirPlace = this.antiCheatConfig.blockPlaceAirPlace.get().booleanValue() && shouldAirPlace;
        boolean useMainHandAirPlace = !useGrimAirPlace && this.antiCheatConfig.mainHandAirPlace.get().booleanValue() && shouldAirPlace;
        if (!useGrimAirPlace) {
            if (useMainHandAirPlace) {
                MeteorClient.mc.method_1562().method_52787(new class_2885(class_1268.field_5808, new class_3965(hitPos, dir == null ? airPlaceDir : dir.method_10153(), neighbour, false), getSequence()));
                return true;
            }
            MeteorClient.mc.method_1562().method_52787(new class_2885(class_1268.field_5808, new class_3965(hitPos, dir == null ? airPlaceDir : dir.method_10153(), neighbour, false), getSequence()));
            return true;
        }
        int origSlot = MeteorClient.mc.field_1724.method_31548().field_7545;
        FindItemResult obsidianResult = InvUtils.findInHotbar(item);
        if (obsidianResult.found() && obsidianResult.isHotbar()) {
            MeteorClient.mc.field_1724.method_31548().field_7545 = obsidianResult.slot();
            MeteorClient.mc.method_1562().method_52787(new class_2868(obsidianResult.slot()));
        }
        MeteorClient.mc.method_1562().method_52787(new class_2846(class_2846.class_2847.field_12969, class_2338.field_10980, class_2350.field_11033));
        MeteorClient.mc.method_1562().method_52787(new class_2885(class_1268.field_5810, new class_3965(hitPos, dir == null ? airPlaceDir : dir.method_10153(), neighbour, false), getSequence()));
        MeteorClient.mc.method_1562().method_52787(new class_2846(class_2846.class_2847.field_12969, class_2338.field_10980, class_2350.field_11033));
        MeteorClient.mc.field_1724.method_31548().field_7545 = origSlot;
        MeteorClient.mc.method_1562().method_52787(new class_2868(origSlot));
        return true;
    }

    public boolean checkPlacement(class_1792 item, class_2338 blockPos) {
        return checkPlacement(item, blockPos, MeteorClient.mc.field_1687.method_8320(blockPos));
    }

    public boolean checkPlacement(class_1792 item, class_2338 blockPos, class_2680 state) {
        if ((!this.antiCheatConfig.blockPlaceAirPlace.get().booleanValue() && !this.antiCheatConfig.mainHandAirPlace.get().booleanValue() && getPlaceOnDirection(blockPos) == null) || !state.method_45474() || !class_1937.method_25953(blockPos)) {
            return false;
        }
        return MeteorClient.mc.field_1687.method_8628(class_2248.method_9503(item).method_9564(), blockPos, class_3726.method_16194());
    }

    public void endPlacement() {
        if (this.locked) {
            this.locked = false;
            MeteorClient.SWAP.endSwap(true);
        }
    }

    public void forceResetPlaceCooldown(class_2338 blockPos) {
        this.placeCooldowns.remove(blockPos);
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        class_2626 class_2626Var = event.packet;
        if (class_2626Var instanceof class_2626) {
            class_2626 packet = class_2626Var;
            if (!packet.method_11308().method_26215()) {
                this.placeCooldowns.remove(packet.method_11309());
            }
        }
    }

    public static class_2350 getPlaceOnDirection(class_2338 pos) {
        if (pos == null) {
            return null;
        }
        class_2350 best = null;
        if (MeteorClient.mc.field_1687 != null && MeteorClient.mc.field_1724 != null) {
            double cDist = -1.0d;
            for (class_2350 dir : class_2350.values()) {
                if (!MeteorClient.mc.field_1687.method_8320(pos.method_10093(dir)).method_26215()) {
                    double dist = getDistanceForDir(pos, dir);
                    if (dist >= 0.0d && (cDist < 0.0d || dist < cDist)) {
                        best = dir;
                        cDist = dist;
                    }
                }
            }
        }
        return best;
    }

    private static double getDistanceForDir(class_2338 pos, class_2350 dir) {
        if (MeteorClient.mc.field_1724 == null) {
            return 0.0d;
        }
        class_243 vec = new class_243(pos.method_10263() + (dir.method_10148() / 2.0f), pos.method_10264() + (dir.method_10164() / 2.0f), pos.method_10260() + (dir.method_10165() / 2.0f));
        class_243 dist = MeteorClient.mc.field_1724.method_33571().method_1031(-vec.field_1352, -vec.field_1351, -vec.field_1350);
        return dist.method_1027();
    }

    private boolean checkLimit(long timestamp, boolean incrementLimit) {
        long packetLimitMs = this.antiCheatConfig.blockPacketLimit.get().intValue();
        if (this.antiCheatConfig.blockPlaceTpsSync.get().booleanValue()) {
            double tps = TickRate.INSTANCE.getTickRate();
            if (tps < 19.0d) {
                packetLimitMs = (long) (packetLimitMs * (20.0d / tps));
            }
        }
        if (this.lastSentPacketTimestamp != -1 && timestamp - this.lastSentPacketTimestamp < packetLimitMs && this.packetsSent >= 9) {
            return false;
        }
        if (incrementLimit) {
            this.packetsSent++;
        }
        if (this.lastSentPacketTimestamp != -1 && timestamp - this.lastSentPacketTimestamp < packetLimitMs) {
            return true;
        }
        this.lastSentPacketTimestamp = timestamp;
        this.packetsSent = 0;
        return true;
    }

    private static int getSequence() {
        if (MeteorClient.mc.field_1687 == null) {
            return 0;
        }
        return MeteorClient.mc.field_1687.meteor$getAndIncrementSequence();
    }
}
