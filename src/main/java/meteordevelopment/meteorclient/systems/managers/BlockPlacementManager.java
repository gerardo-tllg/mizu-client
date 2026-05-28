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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.Item;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class BlockPlacementManager {
    private final AntiCheatConfig antiCheatConfig = AntiCheatConfig.get();
    private final Map<BlockPos, Long> placeCooldowns = new ConcurrentHashMap<>();
    private boolean locked = false;
    private int packetsSent;
    private long lastSentPacketTimestamp = -1L;

    public BlockPlacementManager() {
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    public boolean beginPlacement(BlockPos position, BlockState state, Item item) {
        if (!checkLimit(System.currentTimeMillis(), false)) {
            return false;
        }
        if (locked) {
            return false;
        }
        if (!checkPlacement(item, position, state)) {
            return false;
        }
        if (!MeteorClient.SWAP.beginSwap(item, true)) {
            return false;
        }
        locked = true;
        return true;
    }

    public boolean beginPlacement(List<BlockPos> positions, Item item) {
        if (!checkLimit(System.currentTimeMillis(), false)) {
            return false;
        }
        if (locked) {
            return false;
        }
        if (positions.stream().filter(x -> checkPlacement(item, x)).findAny().isEmpty()) {
            return false;
        }
        if (!MeteorClient.SWAP.beginSwap(item, true)) {
            return false;
        }
        locked = true;
        return true;
    }

    public boolean placeBlock(Item item, BlockPos blockPos) {
        return placeBlock(item, blockPos, mc.world.getBlockState(blockPos));
    }

    public boolean placeBlock(Item item, BlockPos blockPos, BlockState state) {
        long currentTime = System.currentTimeMillis();

        if ((double) placeCooldowns.values().stream().filter(x -> currentTime - x <= 1000L).count()
            >= (Double) antiCheatConfig.blocksPerSecondCap.get()) {
            return false;
        }

        // Range check
        if (mc.player != null) {
            double maxRange = antiCheatConfig.blockPlaceRange.get() > 0
                ? antiCheatConfig.blockPlaceRange.get()
                : mc.player.getBlockInteractionRange();
            double dist = Math.sqrt(blockPos.getSquaredDistance(mc.player.getEyePos()));
            if (dist > maxRange) {
                return false;
            }
        }

        if (!checkPlacement(item, blockPos, state)) {
            return false;
        }

        Direction dir = BlockUtils.getPlaceSide(blockPos);
        Vec3d hitPos = blockPos.toCenterPos();
        BlockPos neighbour;
        Direction airPlaceDir = Direction.UP; // fallback

        if (dir == null) {
            // No adjacent solid block - find nearest solid block for best direction
            double closestDist = Double.MAX_VALUE;
            for (int range = 1; range <= 3; range++) {
                for (Direction searchDir : Direction.values()) {
                    BlockPos candidate = blockPos.offset(searchDir, range);
                    if (!mc.world.getBlockState(candidate).isAir()) {
                        double dist = candidate.getSquaredDistance(mc.player.getEyePos());
                        if (dist < closestDist) {
                            closestDist = dist;
                            // If directly adjacent (range 1), use as real placement face
                            if (range == 1) {
                                dir = searchDir;
                            }
                            airPlaceDir = searchDir;
                        }
                    }
                }
                if (dir != null) break; // found adjacent block, use normal placement
            }

            if (dir != null) {
                // Found adjacent block on extended search
                neighbour = blockPos.offset(dir);
                hitPos = hitPos.add(dir.getOffsetX() * 0.5, dir.getOffsetY() * 0.5, dir.getOffsetZ() * 0.5);
            } else {
                // True air place - use direction toward nearest solid block
                neighbour = blockPos;
            }
        } else {
            neighbour = blockPos.offset(dir);
            hitPos = hitPos.add(dir.getOffsetX() * 0.5, dir.getOffsetY() * 0.5, dir.getOffsetZ() * 0.5);
        }

        // Rotate toward placement target for all placement modes
        if (antiCheatConfig.blockRotatePlace.get()) {
            MeteorClient.ROTATION.snapAt(hitPos);
        }

        Long lastPlaceTime = placeCooldowns.get(blockPos);
        if (lastPlaceTime != null && (double)(currentTime - lastPlaceTime) < antiCheatConfig.blockPlacePerBlockCooldown.get() * 1000.0) {
            return false;
        }

        if (!checkLimit(currentTime, true)) {
            return false;
        }

        placeCooldowns.put(blockPos, currentTime);

        boolean shouldAirPlace = dir == null || antiCheatConfig.forceAirPlace.get();
        boolean useGrimAirPlace = antiCheatConfig.blockPlaceAirPlace.get() && shouldAirPlace;
        boolean useMainHandAirPlace = !useGrimAirPlace && antiCheatConfig.mainHandAirPlace.get() && shouldAirPlace;

        if (useGrimAirPlace) {
            // Offhand swap exploit for Grim servers - bypass SwapManager, handle swap directly
            // Find obsidian in hotbar and switch to it via raw packet
            int origSlot = mc.player.getInventory().selectedSlot;
            FindItemResult obsidianResult = InvUtils.findInHotbar(item);
            if (obsidianResult.found() && obsidianResult.isHotbar()) {
                mc.player.getInventory().selectedSlot = obsidianResult.slot();
                mc.getNetworkHandler().sendPacket(new net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket(obsidianResult.slot()));
            }

            // Swap mainhand to offhand
            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));

            // Place from offhand
            mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(
                Hand.OFF_HAND,
                new BlockHitResult(hitPos, dir == null ? airPlaceDir : dir.getOpposite(), neighbour, false),
                getSequence()));

            // Swap offhand back to mainhand
            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));

            // Restore original slot
            mc.player.getInventory().selectedSlot = origSlot;
            mc.getNetworkHandler().sendPacket(new net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket(origSlot));
        } else if (useMainHandAirPlace) {
            // Raw packet air placement - bypasses client-side LOS check
            mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(
                Hand.MAIN_HAND,
                new BlockHitResult(hitPos, dir == null ? airPlaceDir : dir.getOpposite(), neighbour, false),
                getSequence()));
        } else {
            // Normal raw packet placement against adjacent block
            mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(
                Hand.MAIN_HAND,
                new BlockHitResult(hitPos, dir == null ? airPlaceDir : dir.getOpposite(), neighbour, false),
                getSequence()));
        }

        return true;
    }

    public boolean checkPlacement(Item item, BlockPos blockPos) {
        return checkPlacement(item, blockPos, mc.world.getBlockState(blockPos));
    }

    public boolean checkPlacement(Item item, BlockPos blockPos, BlockState state) {
        if (!antiCheatConfig.blockPlaceAirPlace.get() && !antiCheatConfig.mainHandAirPlace.get() && getPlaceOnDirection(blockPos) == null) {
            return false;
        }
        if (!state.isReplaceable()) {
            return false;
        }
        if (!World.isValid(blockPos)) {
            return false;
        }
        return mc.world.canPlace(Block.getBlockFromItem(item).getDefaultState(), blockPos, ShapeContext.absent());
    }

    public void endPlacement() {
        if (locked) {
            locked = false;
            MeteorClient.SWAP.endSwap(true);
        }
    }

    public void forceResetPlaceCooldown(BlockPos blockPos) {
        placeCooldowns.remove(blockPos);
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (event.packet instanceof BlockUpdateS2CPacket packet) {
            if (!packet.getState().isAir()) {
                placeCooldowns.remove(packet.getPos());
            }
        }
    }

    public static Direction getPlaceOnDirection(BlockPos pos) {
        if (pos == null) {
            return null;
        }

        Direction best = null;
        if (MeteorClient.mc.world != null && MeteorClient.mc.player != null) {
            double cDist = -1.0;
            for (Direction dir : Direction.values()) {
                if (!MeteorClient.mc.world.getBlockState(pos.offset(dir)).isAir()) {
                    double dist = getDistanceForDir(pos, dir);
                    if (dist >= 0.0 && (cDist < 0.0 || dist < cDist)) {
                        best = dir;
                        cDist = dist;
                    }
                }
            }
        }
        return best;
    }

    private static double getDistanceForDir(BlockPos pos, Direction dir) {
        if (MeteorClient.mc.player == null) {
            return 0.0;
        }

        Vec3d vec = new Vec3d(
            (double)((float) pos.getX() + (float) dir.getOffsetX() / 2.0F),
            (double)((float) pos.getY() + (float) dir.getOffsetY() / 2.0F),
            (double)((float) pos.getZ() + (float) dir.getOffsetZ() / 2.0F));
        Vec3d dist = MeteorClient.mc.player.getEyePos().add(-vec.x, -vec.y, -vec.z);
        return dist.lengthSquared();
    }

    private boolean checkLimit(long timestamp, boolean incrementLimit) {
        long packetLimitMs = (long)(int) antiCheatConfig.blockPacketLimit.get();
        if (antiCheatConfig.blockPlaceTpsSync.get()) {
            double tps = (double) TickRate.INSTANCE.getTickRate();
            if (tps < 19.0) {
                packetLimitMs = (long)((double) packetLimitMs * (20.0 / tps));
            }
        }

        if (lastSentPacketTimestamp != -1L && timestamp - lastSentPacketTimestamp < packetLimitMs && packetsSent >= 9) {
            return false;
        }

        if (incrementLimit) {
            packetsSent++;
        }

        if (lastSentPacketTimestamp != -1L && timestamp - lastSentPacketTimestamp < packetLimitMs) {
            return true;
        } else {
            lastSentPacketTimestamp = timestamp;
            packetsSent = 0;
            return true;
        }
    }

    private static int getSequence() {
        if (mc.world == null) return 0;
        return ((meteordevelopment.meteorclient.mixininterface.IClientWorld) mc.world).meteor$getAndIncrementSequence();
    }
}
