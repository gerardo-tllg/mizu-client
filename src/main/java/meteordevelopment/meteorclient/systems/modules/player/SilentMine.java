package meteordevelopment.meteorclient.systems.modules.player;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.player.StartBreakingBlockEvent;
import meteordevelopment.meteorclient.events.meteor.SilentMineFinishedEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IClientWorld;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.managers.RotationManager;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;

public class SilentMine extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgNoBlockDrop = settings.createGroup("No Block Drop");
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder().name("range").defaultValue(5.14).min(0.0).sliderMax(7.0).build());
    public final Setting<Boolean> antiRubberband = sgGeneral.add(new BoolSetting.Builder().name("strict-anti-rubberband").defaultValue(true).build());
    public final Setting<Boolean> preSwitchSinglebreak = sgGeneral.add(new BoolSetting.Builder().name("pre-switch-single-break").defaultValue(true).build());
    private final Setting<Integer> singleBreakFailTicks = sgGeneral.add(new IntSetting.Builder().name("single-break-fail-ticks").defaultValue(20).min(5).sliderMax(50).build());
    public final Setting<Boolean> rebreakSetBlockBroken = sgGeneral.add(new BoolSetting.Builder().name("set-rebreak-block-broken").defaultValue(true).build());
    public final Setting<Double> speedPercentage = sgGeneral.add(new DoubleSetting.Builder()
        .name("speed-percentage")
        .description("Percentage of vanilla mining speed (100% = vanilla, 70% = 70% of vanilla time).")
        .defaultValue(100.0)
        .min(0.0)
        .sliderMax(100.0)
        .build());

    public final Setting<Boolean> noBlockDrop = sgNoBlockDrop.add(new BoolSetting.Builder().name("no-block-drop").description("Prevents blocks from dropping by mining with gold pickaxe.").defaultValue(false).build());
    public final Setting<Double> swapThreshold = sgNoBlockDrop.add(new DoubleSetting.Builder().name("swap-threshold").description("When to swap to gold pickaxe (higher = later).").defaultValue(0.7).min(0.5).max(0.95).sliderMin(0.5).sliderMax(0.95).visible(noBlockDrop::get).build());
    public final Setting<Boolean> onlyForObsidian = sgNoBlockDrop.add(new BoolSetting.Builder().name("only-for-obsidian").description("Only prevents drops for obsidian blocks.").defaultValue(true).visible(noBlockDrop::get).build());

    private final Setting<Boolean> render = sgRender.add(new BoolSetting.Builder().name("do-render").defaultValue(true).build());
    private final Setting<Boolean> renderBlock = sgRender.add(new BoolSetting.Builder().name("render-block").defaultValue(true).build());
    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>().name("shape-mode").defaultValue(ShapeMode.Both).visible(renderBlock::get).build());
    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder().name("side-color").defaultValue(new SettingColor(255, 180, 255, 15)).visible(() -> renderBlock.get() && shapeMode.get().sides()).build());
    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder().name("line-color").defaultValue(new SettingColor(255, 255, 255, 60)).visible(() -> renderBlock.get() && shapeMode.get().lines()).build());
    private final Setting<Boolean> debugRenderPrimary = sgRender.add(new BoolSetting.Builder().name("debug-render-primary").defaultValue(false).build());

    private SilentMineBlock rebreakBlock = null;
    private SilentMineBlock delayedDestroyBlock = null;
    private BlockPos lastDelayedDestroyBlockPos = null;
    private double currentGameTickCalculated = 0;
    private boolean needDelayedDestroySwapBack = false;
    private boolean needRebreakSwapBack = false;

    private boolean hasSwapped = false;
    private int originalSlot = -1;

    public SilentMine() {
        super(Categories.Player, "silent-mine", "Allows you to mine blocks without holding a pickaxe");
        currentGameTickCalculated = RenderUtils.getCurrentGameTickCalculated();
    }

    @Override
    public void onDeactivate() {

        if (hasSwapped && mc.player != null && originalSlot != -1) {
            mc.player.getInventory().selectedSlot = originalSlot;
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(originalSlot));
            hasSwapped = false;
            originalSlot = -1;
        }
    }

    private int totemPopTicks = 0;
    private boolean isBeingTotemPopped = false;

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        currentGameTickCalculated = RenderUtils.getCurrentGameTickCalculated();

        if (mc.player != null && mc.player.isUsingItem() && mc.player.getActiveItem().getItem() == Items.TOTEM_OF_UNDYING) {
            isBeingTotemPopped = true;
            totemPopTicks = 0;
        } else {
            totemPopTicks++;
        }

        if (totemPopTicks > 30) {
            isBeingTotemPopped = false;
        }

        if (hasDelayedDestroy()) {
            lastDelayedDestroyBlockPos = delayedDestroyBlock.blockPos;
        } else {
            lastDelayedDestroyBlockPos = null;
        }

        if (hasDelayedDestroy() && (mc.world.getBlockState(delayedDestroyBlock.blockPos).isAir() || !BlockUtils.canBreak(delayedDestroyBlock.blockPos))) {
            MeteorClient.EVENT_BUS.post(new SilentMineFinishedEvent.Post(delayedDestroyBlock.blockPos, false));
            delayedDestroyBlock = null;
            swapBackToNetherite();
        }

        if (rebreakBlock != null && (mc.world.getBlockState(rebreakBlock.blockPos).isAir() || !BlockUtils.canBreak(rebreakBlock.blockPos))) {
            rebreakBlock.beenAir = true;
            swapBackToNetherite();
        }

        if (hasRebreakBlock() && rebreakBlock.timesSendBreakPacket > 10 && !canRebreakRebreakBlock()) {
            rebreakBlock.cancelBreaking();
            rebreakBlock = null;
        }

        if (hasDelayedDestroy() && delayedDestroyBlock.ticksHeldPickaxe <= singleBreakFailTicks.get()) {
            BlockState blockState = mc.world.getBlockState(delayedDestroyBlock.blockPos);
            if (delayedDestroyBlock.isReady() && !isBeingTotemPopped) {

                if (noBlockDrop.get() && shouldPreventBlockDrop(blockState)) {
                    FindItemResult goldPickaxe = InvUtils.find(Items.GOLDEN_PICKAXE);
                    if (goldPickaxe.found() && goldPickaxe.slot() >= 0 && goldPickaxe.slot() <= 8) {
                        if (originalSlot == -1) {
                            originalSlot = mc.player.getInventory().selectedSlot;
                        }
                        mc.player.getInventory().selectedSlot = goldPickaxe.slot();
                        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(goldPickaxe.slot()));
                        hasSwapped = true;
                        delayedDestroyBlock.ticksHeldPickaxe++;
                        return;
                    }
                }

                FindItemResult result = InvUtils.findFastestTool(blockState);
                if (result.found() && mc.player.getInventory().selectedSlot != result.slot()) {
                    if (MeteorClient.SWAP.beginSwap(result, false)) {
                        needDelayedDestroySwapBack = true;
                    }
                }
                if (!result.found() || mc.player.getInventory().selectedSlot == result.slot()) {
                    delayedDestroyBlock.ticksHeldPickaxe++;
                }
            }
        }

        if (rebreakBlock != null && !isBeingTotemPopped) {
            BlockState blockState = mc.world.getBlockState(rebreakBlock.blockPos);
            if (rebreakBlock.isReady()) {
                if (inBreakRange(rebreakBlock.blockPos)) {

                    if (noBlockDrop.get() && shouldPreventBlockDrop(blockState)) {
                        FindItemResult goldPickaxe = InvUtils.find(Items.GOLDEN_PICKAXE);
                        if (goldPickaxe.found() && goldPickaxe.slot() >= 0 && goldPickaxe.slot() <= 8) {
                            if (originalSlot == -1) {
                                originalSlot = mc.player.getInventory().selectedSlot;
                            }
                            mc.player.getInventory().selectedSlot = goldPickaxe.slot();
                            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(goldPickaxe.slot()));
                            hasSwapped = true;
                            rebreakBlock.tryBreak();

                            if (rebreakSetBlockBroken.get() && canRebreakRebreakBlock()) {
                                mc.world.setBlockState(rebreakBlock.blockPos, Blocks.AIR.getDefaultState());
                            }
                            return;
                        }
                    }

                    FindItemResult result = InvUtils.findFastestTool(blockState);
                    MeteorClient.EVENT_BUS.post(new SilentMineFinishedEvent.Pre(rebreakBlock.blockPos, true));
                    if (result.found() && mc.player.getInventory().selectedSlot != result.slot()) {
                        if (MeteorClient.SWAP.beginSwap(result, true)) {
                            needRebreakSwapBack = true;

                            if (originalSlot == -1 && noBlockDrop.get() && shouldPreventBlockDrop(blockState)) {
                                originalSlot = result.slot();
                            }
                        }
                    }

                    if (noBlockDrop.get() && !hasSwapped && shouldPreventBlockDrop(blockState)) {
                        if (rebreakBlock.getBreakProgress() >= swapThreshold.get()) {
                            swapToGoldenPickaxe();
                        }
                    }

                    rebreakBlock.tryBreak();

                    if (needRebreakSwapBack) {
                        MeteorClient.SWAP.endSwap(true);
                    }

                    if (rebreakSetBlockBroken.get() && canRebreakRebreakBlock()) {
                        mc.world.setBlockState(rebreakBlock.blockPos, Blocks.AIR.getDefaultState());
                    }
                } else {
                    rebreakBlock = null;
                }
            }
        }

        if (hasDelayedDestroy() && delayedDestroyBlock.ticksHeldPickaxe > singleBreakFailTicks.get()) {
            if (inBreakRange(delayedDestroyBlock.blockPos)) {
                BlockState state = mc.world.getBlockState(delayedDestroyBlock.blockPos);

                if (noBlockDrop.get() && shouldPreventBlockDrop(state)) {
                    FindItemResult goldPickaxe = InvUtils.findInHotbar(Items.GOLDEN_PICKAXE);
                    if (goldPickaxe.found()) {

                        if (originalSlot == -1) {
                            originalSlot = mc.player.getInventory().selectedSlot;
                        }

                        mc.player.getInventory().selectedSlot = goldPickaxe.slot();
                        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(goldPickaxe.slot()));
                        hasSwapped = true;
                    }
                }

                delayedDestroyBlock.startBreaking(true);
            } else {
                delayedDestroyBlock.cancelBreaking();
                delayedDestroyBlock = null;
            }
        }

        boolean delayedDestroyFinished = !(hasDelayedDestroy() && delayedDestroyBlock.isReady());
        if (needDelayedDestroySwapBack && delayedDestroyFinished) {
            MeteorClient.SWAP.endSwap(false);
            needDelayedDestroySwapBack = false;
        }
    }

    private void swapToGoldenPickaxe() {
        if (mc.player == null) return;

        FindItemResult goldenPickaxe = InvUtils.findInHotbar(Items.GOLDEN_PICKAXE);
        if (goldenPickaxe.found()) {

            if (originalSlot == -1) {
                originalSlot = mc.player.getInventory().selectedSlot;
            }

            mc.player.getInventory().selectedSlot = goldenPickaxe.slot();
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(goldenPickaxe.slot()));
            hasSwapped = true;
        }
    }

    private void swapBackToNetherite() {
        if (mc.player == null || !hasSwapped || originalSlot == -1) return;

        mc.player.getInventory().selectedSlot = originalSlot;
        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(originalSlot));
        hasSwapped = false;
        originalSlot = -1;
    }

    private boolean shouldPreventBlockDrop(BlockState state) {
        if (onlyForObsidian.get()) {
            return state.getBlock() == Blocks.OBSIDIAN || state.getBlock() == Blocks.CRYING_OBSIDIAN;
        }
        return true;
    }

    public void silentBreakBlock(BlockPos blockPos, Direction direction, double priority) {
        if (!isActive() || blockPos == null || alreadyBreaking(blockPos) || !BlockUtils.canBreak(blockPos, mc.world.getBlockState(blockPos)) || !inBreakRange(blockPos)) {
            return;
        }

        boolean isAntiSwimBlock = blockPos.equals(mc.player.getBlockPos().up());
        if (!hasDelayedDestroy()) {
            boolean willResetPrimary = rebreakBlock != null && !canRebreakRebreakBlock();
            if (willResetPrimary && rebreakBlock.priority < priority) {
                return;
            }
            currentGameTickCalculated -= 0.1;
            delayedDestroyBlock = new SilentMineBlock(blockPos, direction, priority, false);
            delayedDestroyBlock.startBreaking(true);
            if (willResetPrimary) {
                rebreakBlock.startBreaking(false);
            }
        }

        if (alreadyBreaking(blockPos)) {
            return;
        }

        if (rebreakBlock != null && delayedDestroyBlock != null && (priority >= rebreakBlock.priority || canRebreakRebreakBlock())) {
            if (delayedDestroyBlock.getBreakProgress() <= 0.8) {
                rebreakBlock = null;
            }
        }

        if (rebreakBlock == null || isAntiSwimBlock) {
            rebreakBlock = new SilentMineBlock(blockPos, direction, priority, true);
            rebreakBlock.startBreaking(false);
        }
    }

    @EventHandler
    public void onStartBreakingBlock(StartBreakingBlockEvent event) {
        event.cancel();
        silentBreakBlock(event.blockPos, event.direction, 100f);
    }

    public boolean canSwapBack() {
        return needDelayedDestroySwapBack && !(hasDelayedDestroy() && delayedDestroyBlock.isReady());
    }

    public boolean hasDelayedDestroy() {
        return delayedDestroyBlock != null;
    }

    public boolean hasRebreakBlock() {
        return rebreakBlock != null && !rebreakBlock.beenAir;
    }

    public BlockPos getDelayedDestroyBlockPos() {
        return delayedDestroyBlock != null ? delayedDestroyBlock.blockPos : null;
    }

    public void cancelBreaking() {
        if (rebreakBlock != null) {
            rebreakBlock.cancelBreaking();
            rebreakBlock = null;
        }
        if (delayedDestroyBlock != null) {
            delayedDestroyBlock.cancelBreaking();
            delayedDestroyBlock = null;
        }

        swapBackToNetherite();
    }

    public BlockPos getLastDelayedDestroyBlockPos() {
        return lastDelayedDestroyBlockPos;
    }

    public double getDelayedDestroyProgress() {
        return delayedDestroyBlock != null ? delayedDestroyBlock.getBreakProgress() : 0;
    }

    public BlockPos getRebreakBlockPos() {
        return rebreakBlock != null ? rebreakBlock.blockPos : null;
    }

    public double getRebreakBlockProgress() {
        return rebreakBlock != null ? rebreakBlock.getBreakProgress() : 0;
    }

    public boolean canRebreakRebreakBlock() {
        return rebreakBlock != null && rebreakBlock.beenAir;
    }

    public boolean inBreakRange(BlockPos blockPos) {
        return (new Box(blockPos)).squaredMagnitude(mc.player.getEyePos()) <= range.get() * range.get();
    }

    public boolean alreadyBreaking(BlockPos blockPos) {
        return (rebreakBlock != null && blockPos.equals(rebreakBlock.blockPos)) || (delayedDestroyBlock != null && blockPos.equals(delayedDestroyBlock.blockPos));
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (render.get()) {
            double calculatedDrawGameTick = RenderUtils.getCurrentGameTickCalculated();
            if (rebreakBlock != null) {
                rebreakBlock.render(event, calculatedDrawGameTick, true);
            }
            if (delayedDestroyBlock != null) {
                delayedDestroyBlock.render(event, calculatedDrawGameTick, false);
            }
        }
    }

    @EventHandler
    private void onPacket(PacketEvent.Send event) {
        if (event.packet instanceof PlayerActionC2SPacket packet) {

            if (packet.getAction() == PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK && antiRubberband.get() && (packet.getPos().equals(getRebreakBlockPos()) || packet.getPos().equals(getDelayedDestroyBlockPos()))) {
                mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, packet.getPos(), packet.getDirection()));
            }

            if (noBlockDrop.get() && (packet.getAction() == PlayerActionC2SPacket.Action.START_DESTROY_BLOCK ||
                packet.getAction() == PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK)) {
                BlockState state = mc.world.getBlockState(packet.getPos());
                if (shouldPreventBlockDrop(state) && mc.player != null) {

                    FindItemResult goldenPickaxe = InvUtils.findInHotbar(Items.GOLDEN_PICKAXE);
                    if (goldenPickaxe.found()) {
                        mc.player.getInventory().selectedSlot = goldenPickaxe.slot();
                        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(goldenPickaxe.slot()));
                        hasSwapped = true;
                    }
                }
            }
        }
    }

    private int getSeq() {
        return ((IClientWorld) mc.world).meteor$getAndIncrementSequence();
    }

    class SilentMineBlock {
        public BlockPos blockPos;
        public Direction direction;
        public boolean started = false;
        public int timesSendBreakPacket = 0;
        public int ticksHeldPickaxe = 0;
        public boolean beenAir = false;
        private double destroyProgressStart = 0;
        private double priority = 0;
        private boolean isRebreak;

        public SilentMineBlock(BlockPos blockPos, Direction direction, double priority, boolean isRebreak) {
            this.blockPos = blockPos;
            this.direction = direction;
            this.priority = priority;
            this.isRebreak = isRebreak;
        }

        public boolean isReady() {
            if (!BlockUtils.canBreak(blockPos)) return false;
            double breakProgressSingleTick = getBreakProgressSingleTick();
            double speedMultiplier = speedPercentage.get() / 100.0;
            double baseThreshold = isRebreak ? 0.7 : 1.0;
            double threshold = baseThreshold - (preSwitchSinglebreak.get() && !isRebreak ? (breakProgressSingleTick / speedMultiplier) / 2.0 : 0.0);
            return getBreakProgress() >= threshold || timesSendBreakPacket > 0;
        }

        public void startBreaking(boolean isDelayedDestroy) {
            ticksHeldPickaxe = 0;
            timesSendBreakPacket = 0;
            this.destroyProgressStart = currentGameTickCalculated;
            if (isDelayedDestroy && canRebreakRebreakBlock()) {
                rebreakBlock = null;
            }
            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, direction, getSeq()));
            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockPos, direction, getSeq()));
            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, direction, getSeq()));
            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, direction, getSeq()));
            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockPos, direction, getSeq()));
            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, direction, getSeq()));
            if (!antiRubberband.get()) {
                mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, blockPos, direction));
                mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, blockPos, direction));
            }
            started = true;
        }

        public void tryBreak() {
            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, direction, getSeq()));
            if (!antiRubberband.get()) {
                mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, blockPos, direction));
            }
            timesSendBreakPacket++;
        }

        public void cancelBreaking() {
            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, blockPos, direction));
        }

        public double getBreakProgress() {
            return getBreakProgress(currentGameTickCalculated);
        }

        public double getBreakProgress(double gameTick) {
            BlockState state = mc.world.getBlockState(blockPos);

            FindItemResult bestTool = InvUtils.findFastestToolHotbar(state);
            int toolSlot = bestTool.found() ? bestTool.slot() : mc.player.getInventory().selectedSlot;

            Box boundingBox = mc.player.getBoundingBox();
            double playerFeetY = boundingBox.minY;
            Box groundBox = new Box(boundingBox.minX, playerFeetY - 0.2, boundingBox.minZ, boundingBox.maxX, playerFeetY, boundingBox.maxZ);
            boolean willBeOnGround = false;
            for (BlockPos pos : BlockUtils.iterate(groundBox)) {
                BlockState blockState = mc.world.getBlockState(pos);
                if (!blockState.isSolidBlock(mc.world, pos)) continue;
                double blockTopY = pos.getY() + 1.0;
                double distanceToBlock = playerFeetY - blockTopY;
                if (distanceToBlock >= 0 && distanceToBlock < Math.abs(mc.player.getVelocity().y * 2)) {
                    willBeOnGround = true;
                }
            }

            double baseBreakingSpeed = BlockUtils.getBlockBreakingSpeed(toolSlot, state, RotationManager.lastGround || (willBeOnGround && !isRebreak));
            double speedMultiplier = speedPercentage.get() / 100.0;
            double adjustedBreakingSpeed = baseBreakingSpeed / speedMultiplier;
            return Math.min(BlockUtils.getBreakDelta(adjustedBreakingSpeed, state) * (gameTick - destroyProgressStart), 1.0);
        }

        public double getBreakProgressSingleTick() {
            return getBreakProgress(destroyProgressStart + 1);
        }

        public double getPriority() {
            return priority;
        }

        public void render(Render3DEvent event, double renderTick, boolean isPrimary) {
            VoxelShape shape = mc.world.getBlockState(blockPos).getOutlineShape(mc.world, blockPos);
            if (shape == null || shape.isEmpty()) {
                event.renderer.box(blockPos, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
                return;
            }
            Box orig = shape.getBoundingBox();
            double shrinkFactor = 1d - Math.clamp(isPrimary ? getBreakProgress(renderTick) * (1 / 0.7) : getBreakProgress(renderTick), 0, 1);
            BlockPos pos = blockPos;
            Box box = orig.shrink(orig.getLengthX() * shrinkFactor, orig.getLengthY() * shrinkFactor, orig.getLengthZ() * shrinkFactor);
            double xShrink = (orig.getLengthX() * shrinkFactor) / 2;
            double yShrink = (orig.getLengthY() * shrinkFactor) / 2;
            double zShrink = (orig.getLengthZ() * shrinkFactor) / 2;
            double x1 = pos.getX() + box.minX + xShrink;
            double y1 = pos.getY() + box.minY + yShrink;
            double z1 = pos.getZ() + box.minZ + zShrink;
            double x2 = pos.getX() + box.maxX + xShrink;
            double y2 = pos.getY() + box.maxY + yShrink;
            double z2 = pos.getZ() + box.maxZ + zShrink;
            Color color = sideColor.get();
            if (debugRenderPrimary.get() && isPrimary) {
                color = Color.ORANGE.a(40);
            }
            event.renderer.box(x1, y1, z1, x2, y2, z2, color, lineColor.get(), shapeMode.get(), 0);
        }
    }
}
