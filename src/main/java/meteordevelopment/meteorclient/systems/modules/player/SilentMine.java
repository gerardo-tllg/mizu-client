package meteordevelopment.meteorclient.systems.modules.player;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.player.StartBreakingBlockEvent;
import meteordevelopment.meteorclient.events.meteor.SilentMineFinishedEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
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
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;

public class SilentMine extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgRender;

    private final Setting<Boolean> pauseOnEat;
    private final Setting<Double> range;
    public final Setting<Boolean> antiRubberband;
    public final Setting<Boolean> preSwitchSinglebreak;
    private final Setting<Integer> singleBreakFailTicks;
    private final Setting<Integer> rebreakSpeed;
    public final Setting<Boolean> rebreakSetBlockBroken;
    public final Setting<Double> speedPercentage;

    private final Setting<Boolean> render;
    private final Setting<Boolean> renderBlock;
    private final Setting<RenderMode> renderMode;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    private final Setting<Double> pulseFrequency;
    private final Setting<Double> pulseAmplitude;
    private final Setting<SettingColor> pulseSideColor;
    private final Setting<SettingColor> pulseLineColor;
    private final Setting<Boolean> debugRenderPrimary;

    private SilentMineBlock rebreakBlock;
    private SilentMineBlock delayedDestroyBlock;
    private BlockPos lastDelayedDestroyBlockPos;
    private double currentGameTickCalculated;
    private boolean needDelayedDestroySwapBack;
    private boolean needRebreakSwapBack;
    private long rebreakWindowStartMs;
    private int rebreakPacketsInWindow;

    public SilentMine() {
        super(Categories.Player, "silent-mine", "Allows you to mine blocks without holding a pickaxe");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgRender = this.settings.createGroup("Render");

        this.pauseOnEat = this.sgGeneral.add(new BoolSetting.Builder()
            .name("pause-on-eat")
            .description("Pauses all mining operations while eating/using items.")
            .defaultValue(true)
            .build());
        this.range = this.sgGeneral.add(new DoubleSetting.Builder()
            .name("range")
            .description("Range to activate use at")
            .defaultValue(5.14)
            .min(0.0)
            .sliderMax(7.0)
            .build());
        this.antiRubberband = this.sgGeneral.add(new BoolSetting.Builder()
            .name("strict-anti-rubberband")
            .description("Attempts to prevent you from rubberbanding extra hard. May result in kicks.")
            .defaultValue(true)
            .build());
        this.preSwitchSinglebreak = this.sgGeneral.add(new BoolSetting.Builder()
            .name("pre-switch-single-break")
            .description("Pre-switches to your pickaxe when the singlebreak block is almost done, for more responsive breaking.")
            .defaultValue(true)
            .build());
        this.singleBreakFailTicks = this.sgGeneral.add(new IntSetting.Builder()
            .name("single-break-fail-ticks")
            .description("Number of ticks to wait before retrying a singlebreak in case of fail.")
            .defaultValue(20)
            .min(5)
            .sliderMax(50)
            .build());
        this.rebreakSpeed = this.sgGeneral.add(new IntSetting.Builder()
            .name("rebreak-speed")
            .description("Rebreak attempts per second.")
            .defaultValue(20)
            .min(1)
            .sliderRange(1, 20)
            .build());
        this.rebreakSetBlockBroken = this.sgGeneral.add(new BoolSetting.Builder()
            .name("set-rebreak-block-broken")
            .description("Breaks the rebreak client side instantly.")
            .defaultValue(true)
            .build());
        this.speedPercentage = this.sgGeneral.add(new DoubleSetting.Builder()
            .name("speed-percentage")
            .description("Percentage of vanilla mining speed (100% = vanilla).")
            .defaultValue(100.0)
            .min(0.0)
            .sliderMax(100.0)
            .build());

        this.render = this.sgRender.add(new BoolSetting.Builder()
            .name("do-render")
            .description("Renders the blocks in queue to be broken.")
            .defaultValue(true)
            .build());
        this.renderBlock = this.sgRender.add(new BoolSetting.Builder()
            .name("render-block")
            .description("Whether to render the block being broken.")
            .defaultValue(true)
            .build());
        this.renderMode = this.sgRender.add(new EnumSetting.Builder<RenderMode>()
            .name("render-mode")
            .description("Render style for the breaking box.")
            .defaultValue(RenderMode.Simple)
            .visible(renderBlock::get)
            .build());
        this.shapeMode = this.sgRender.add(new EnumSetting.Builder<ShapeMode>()
            .name("shape-mode")
            .description("How the shapes are rendered.")
            .defaultValue(ShapeMode.Both)
            .visible(renderBlock::get)
            .build());
        this.sideColor = this.sgRender.add(new ColorSetting.Builder()
            .name("side-color")
            .description("The side color of the rendering.")
            .defaultValue(new SettingColor(255, 180, 255, 15))
            .visible(() -> renderBlock.get() && shapeMode.get().sides())
            .build());
        this.lineColor = this.sgRender.add(new ColorSetting.Builder()
            .name("line-color")
            .description("The line color of the rendering.")
            .defaultValue(new SettingColor(255, 255, 255, 60))
            .visible(() -> renderBlock.get() && shapeMode.get().lines())
            .build());
        this.pulseFrequency = this.sgRender.add(new DoubleSetting.Builder()
            .name("pulse-frequency")
            .description("Pulse frequency (Hz)")
            .defaultValue(2.0)
            .min(0.1)
            .sliderMax(8.0)
            .visible(() -> renderBlock.get() && renderMode.get() == RenderMode.Pulse)
            .build());
        this.pulseAmplitude = this.sgRender.add(new DoubleSetting.Builder()
            .name("pulse-amplitude")
            .description("Pulse size amplitude (0-0.5)")
            .defaultValue(0.2)
            .min(0.0)
            .sliderMax(0.5)
            .visible(() -> renderBlock.get() && renderMode.get() == RenderMode.Pulse)
            .build());
        this.pulseSideColor = this.sgRender.add(new ColorSetting.Builder()
            .name("pulse-side-color")
            .description("Side color during pulse mode")
            .defaultValue(new SettingColor(255, 160, 40, 40))
            .visible(() -> renderBlock.get() && renderMode.get() == RenderMode.Pulse)
            .build());
        this.pulseLineColor = this.sgRender.add(new ColorSetting.Builder()
            .name("pulse-line-color")
            .description("Line color during pulse mode")
            .defaultValue(new SettingColor(255, 160, 40, 80))
            .visible(() -> renderBlock.get() && renderMode.get() == RenderMode.Pulse)
            .build());
        this.debugRenderPrimary = this.sgRender.add(new BoolSetting.Builder()
            .name("debug-render-primary")
            .description("Render the primary block differently for debugging.")
            .defaultValue(false)
            .build());

        this.rebreakBlock = null;
        this.delayedDestroyBlock = null;
        this.lastDelayedDestroyBlockPos = null;
        this.currentGameTickCalculated = 0.0;
        this.needDelayedDestroySwapBack = false;
        this.needRebreakSwapBack = false;
        this.rebreakWindowStartMs = -1L;
        this.rebreakPacketsInWindow = 0;
        this.currentGameTickCalculated = RenderUtils.getCurrentGameTickCalculated();
    }

    @Override
    public void onDeactivate() {
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        currentGameTickCalculated = RenderUtils.getCurrentGameTickCalculated();
        if (pauseOnEat.get() && mc.player != null && mc.player.isUsingItem()) {
            return;
        }

        if (hasDelayedDestroy()) {
            lastDelayedDestroyBlockPos = delayedDestroyBlock.blockPos;
        } else {
            lastDelayedDestroyBlockPos = null;
        }

        if (hasDelayedDestroy() && (mc.world.getBlockState(delayedDestroyBlock.blockPos).isAir() || !BlockUtils.canBreak(delayedDestroyBlock.blockPos))) {
            MeteorClient.EVENT_BUS.post(new SilentMineFinishedEvent.Post(delayedDestroyBlock.blockPos, false));
            delayedDestroyBlock = null;
        }

        if (rebreakBlock != null && (mc.world.getBlockState(rebreakBlock.blockPos).isAir() || !BlockUtils.canBreak(rebreakBlock.blockPos))) {
            rebreakBlock.beenAir = true;
        }

        if (hasRebreakBlock() && rebreakBlock.timesSendBreakPacket > 40 && !canRebreakRebreakBlock()) {
            rebreakBlock.cancelBreaking();
            rebreakBlock = null;
        }

        BlockState blockState;
        FindItemResult result;
        if (hasDelayedDestroy() && delayedDestroyBlock.ticksHeldPickaxe <= singleBreakFailTicks.get()) {
            blockState = mc.world.getBlockState(delayedDestroyBlock.blockPos);
            if (delayedDestroyBlock.isReady()) {
                result = InvUtils.findFastestTool(blockState);
                if (result.found() && mc.player.getInventory().selectedSlot != result.slot() && MeteorClient.SWAP.beginSwap(result, false)) {
                    needDelayedDestroySwapBack = true;
                }

                if (!result.found() || mc.player.getInventory().selectedSlot == result.slot()) {
                    delayedDestroyBlock.ticksHeldPickaxe++;
                }
            }
        }

        if (rebreakBlock != null) {
            blockState = mc.world.getBlockState(rebreakBlock.blockPos);
            if (rebreakBlock.isReady()) {
                if (inBreakRange(rebreakBlock.blockPos)) {
                    if (blockState.isAir()) {
                        result = InvUtils.findInHotbar(stack -> stack.isIn(ItemTags.SWORDS));
                    } else {
                        result = InvUtils.findFastestTool(blockState);
                    }

                    MeteorClient.EVENT_BUS.post(new SilentMineFinishedEvent.Pre(rebreakBlock.blockPos, true));
                    if (result.found() && mc.player.getInventory().selectedSlot != result.slot() && MeteorClient.SWAP.beginSwap(result, true)) {
                        needRebreakSwapBack = true;
                    }

                    long now = System.currentTimeMillis();
                    if (rebreakWindowStartMs == -1L || now - rebreakWindowStartMs >= 1000L) {
                        rebreakWindowStartMs = now;
                        rebreakPacketsInWindow = 0;
                    }

                    int remaining = 30 - rebreakPacketsInWindow;
                    if (remaining < 1) remaining = 1;

                    for (int i = 0; i < remaining; i++) {
                        rebreakBlock.tryBreak();
                        rebreakPacketsInWindow++;
                        if (canRebreakRebreakBlock()) break;
                    }

                    if (needRebreakSwapBack) {
                        MeteorClient.SWAP.endSwap(true);
                        needRebreakSwapBack = false;
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
                delayedDestroyBlock.startBreaking(true);
            } else {
                delayedDestroyBlock.cancelBreaking();
                delayedDestroyBlock = null;
            }
        }

        boolean delayedDestroyFinished = !hasDelayedDestroy() || !delayedDestroyBlock.isReady();
        if (needDelayedDestroySwapBack && delayedDestroyFinished) {
            MeteorClient.SWAP.endSwap(false);
            needDelayedDestroySwapBack = false;
        }
    }

    public void silentBreakBlock(BlockPos pos, double priority) {
        if (!pauseOnEat.get() || mc.player == null || !mc.player.isUsingItem()) {
            silentBreakBlock(pos, Direction.UP, priority);
        }
    }

    public void silentBreakBlock(BlockPos blockPos, Direction direction, double priority) {
        if (!isActive()) return;
        if (pauseOnEat.get() && mc.player != null && mc.player.isUsingItem()) return;
        if (blockPos == null || alreadyBreaking(blockPos)) return;
        if (!BlockUtils.canBreak(blockPos, mc.world.getBlockState(blockPos))) return;
        if (!inBreakRange(blockPos)) return;

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

        if (!alreadyBreaking(blockPos)) {
            if (rebreakBlock != null && delayedDestroyBlock != null && (priority >= rebreakBlock.priority || canRebreakRebreakBlock()) && delayedDestroyBlock.getBreakProgress() <= 0.8) {
                rebreakBlock = null;
            }

            if (rebreakBlock == null) {
                rebreakBlock = new SilentMineBlock(blockPos, direction, priority, true);
                rebreakBlock.startBreaking(false);
            }
        }
    }

    @EventHandler
    public void onStartBreakingBlock(StartBreakingBlockEvent event) {
        if (!pauseOnEat.get() || mc.player == null || !mc.player.isUsingItem()) {
            event.cancel();
            silentBreakBlock(event.blockPos, event.direction, 100.0);
        }
    }

    public boolean canSwapBack() {
        boolean result = needDelayedDestroySwapBack;
        if (hasDelayedDestroy() && delayedDestroyBlock.isReady()) {
            result = false;
        }
        return result;
    }

    public boolean hasDelayedDestroy() {
        return delayedDestroyBlock != null;
    }

    public boolean hasRebreakBlock() {
        return rebreakBlock != null && !rebreakBlock.beenAir;
    }

    public BlockPos getDelayedDestroyBlockPos() {
        return delayedDestroyBlock == null ? null : delayedDestroyBlock.blockPos;
    }

    public BlockPos getLastDelayedDestroyBlockPos() {
        return lastDelayedDestroyBlockPos;
    }

    public double getDelayedDestroyProgress() {
        return delayedDestroyBlock == null ? 0.0 : delayedDestroyBlock.getBreakProgress();
    }

    public BlockPos getRebreakBlockPos() {
        return rebreakBlock == null ? null : rebreakBlock.blockPos;
    }

    public double getRebreakBlockProgress() {
        return rebreakBlock == null ? 0.0 : rebreakBlock.getBreakProgress();
    }

    public boolean canRebreakRebreakBlock() {
        return rebreakBlock != null && rebreakBlock.beenAir;
    }

    public int getRebreaksPerSecond() {
        long now = System.currentTimeMillis();
        return rebreakWindowStartMs != -1L && now - rebreakWindowStartMs <= 1000L ? rebreakPacketsInWindow : 0;
    }

    public boolean inBreakRange(BlockPos blockPos) {
        return !((new Box(blockPos)).squaredMagnitude(mc.player.getEyePos()) > range.get() * range.get());
    }

    public boolean alreadyBreaking(BlockPos blockPos) {
        return (rebreakBlock != null && blockPos.equals(rebreakBlock.blockPos)) || (delayedDestroyBlock != null && blockPos.equals(delayedDestroyBlock.blockPos));
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
        if (needDelayedDestroySwapBack) {
            MeteorClient.SWAP.endSwap(false);
            needDelayedDestroySwapBack = false;
        }
        if (needRebreakSwapBack) {
            MeteorClient.SWAP.endSwap(true);
            needRebreakSwapBack = false;
        }
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
        }
    }

    private int getSeq() {
        if (mc.world == null) return 0;
        return ((meteordevelopment.meteorclient.mixininterface.IClientWorld) mc.world).meteor$getAndIncrementSequence();
    }

    public enum RenderMode {
        Simple,
        BoxIn,
        BoxOut,
        Pulse
    }

    class SilentMineBlock {
        public BlockPos blockPos;
        public Direction direction;
        public boolean started = false;
        public int timesSendBreakPacket = 0;
        public int ticksHeldPickaxe = 0;
        public boolean beenAir = false;
        private double destroyProgressStart = 0.0;
        private double priority = 0.0;
        private boolean isRebreak;
        private final BlockState initialState;

        public SilentMineBlock(BlockPos blockPos, Direction direction, double priority, boolean isRebreak) {
            this.blockPos = blockPos;
            this.direction = direction;
            this.priority = priority;
            this.isRebreak = isRebreak;
            this.initialState = mc.world.getBlockState(blockPos);
        }

        public boolean isReady() {
            if (!BlockUtils.canBreak(blockPos) && !mc.world.getBlockState(blockPos).isAir()) {
                return false;
            }
            double breakProgressSingleTick = getBreakProgressSingleTick();
            double threshold = isRebreak ? 0.7 : 1.0 - (preSwitchSinglebreak.get() ? breakProgressSingleTick / 2.0 : 0.0);
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
            BlockState liveState = mc.world.getBlockState(blockPos);
            BlockState state = liveState.isAir() ? initialState : liveState;
            FindItemResult slot = findBestToolFor(state);
            Box boundingBox = mc.player.getBoundingBox();
            double playerFeetY = boundingBox.minY;
            Box groundBox = new Box(boundingBox.minX, playerFeetY - 0.2, boundingBox.minZ, boundingBox.maxX, playerFeetY, boundingBox.maxZ);
            boolean willBeOnGround = false;

            for (BlockPos pos : BlockUtils.iterate(groundBox)) {
                BlockState blockState = mc.world.getBlockState(pos);
                if (blockState.isSolidBlock(mc.world, pos)) {
                    double blockTopY = (double) pos.getY() + 1.0;
                    double distanceToBlock = playerFeetY - blockTopY;
                    if (distanceToBlock >= 0.0 && distanceToBlock < Math.abs(mc.player.getVelocity().y * 2.0)) {
                        willBeOnGround = true;
                    }
                }
            }

            double breakingSpeed = BlockUtils.getBlockBreakingSpeed(
                slot.found() ? slot.slot() : mc.player.getInventory().selectedSlot,
                state,
                RotationManager.lastGround || (willBeOnGround && !isRebreak));
            double speedMultiplier = speedPercentage.get() / 100.0;
            return Math.min(BlockUtils.getBreakDelta(breakingSpeed / speedMultiplier, state) * (gameTick - destroyProgressStart), 1.0);
        }

        public double getBreakProgressSingleTick() {
            return getBreakProgress(destroyProgressStart + 1.0);
        }

        private FindItemResult findBestToolFor(BlockState state) {
            if (state.isToolRequired()) {
                FindItemResult res = InvUtils.findFastestToolHotbar(state);
                if (res.found()) {
                    return res;
                }
            }

            int bestSlot = -1;
            float bestSpeed = 1.0f;

            for (int i = 0; i < 9; i++) {
                ItemStack stack = mc.player.getInventory().getStack(i);
                float spd = stack.getMiningSpeedMultiplier(state);
                if (spd > bestSpeed) {
                    bestSpeed = spd;
                    bestSlot = i;
                }
            }

            if (bestSlot != -1) {
                ItemStack target = mc.player.getInventory().getStack(bestSlot);
                FindItemResult resx = InvUtils.findInHotbar(s -> s == target);
                if (resx.found()) {
                    return resx;
                }
            }

            return InvUtils.findFastestTool(state);
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
            double shrinkFactor = 1.0 - Math.clamp(isPrimary ? getBreakProgress(renderTick) * 1.4285714285714286 : getBreakProgress(renderTick), 0.0, 1.0);
            BlockPos pos = blockPos;
            Box box = orig.shrink(orig.getLengthX() * shrinkFactor, orig.getLengthY() * shrinkFactor, orig.getLengthZ() * shrinkFactor);
            double xShrink = orig.getLengthX() * shrinkFactor / 2.0;
            double yShrink = orig.getLengthY() * shrinkFactor / 2.0;
            double zShrink = orig.getLengthZ() * shrinkFactor / 2.0;
            double x1 = (double) pos.getX() + box.minX + xShrink;
            double y1 = (double) pos.getY() + box.minY + yShrink;
            double z1 = (double) pos.getZ() + box.minZ + zShrink;
            double x2 = (double) pos.getX() + box.maxX + xShrink;
            double y2 = (double) pos.getY() + box.maxY + yShrink;
            double z2 = (double) pos.getZ() + box.maxZ + zShrink;
            Color color = sideColor.get();
            Color lines = lineColor.get();
            if (debugRenderPrimary.get() && isPrimary) {
                color = Color.ORANGE.a(40);
            }

            switch (renderMode.get()) {
                case Simple:
                case BoxOut:
                    event.renderer.box(x1, y1, z1, x2, y2, z2, color, lines, shapeMode.get(), 0);
                    break;
                case BoxIn: {
                    double progress = Math.clamp(isPrimary ? getBreakProgress(renderTick) * 1.4285714285714286 : getBreakProgress(renderTick), 0.0, 1.0);
                    double lx = orig.getLengthX() * progress / 2.0;
                    double ly = orig.getLengthY() * progress / 2.0;
                    double lz = orig.getLengthZ() * progress / 2.0;
                    double bx1 = (double) pos.getX() + orig.minX + lx;
                    double by1 = (double) pos.getY() + orig.minY + ly;
                    double bz1 = (double) pos.getZ() + orig.minZ + lz;
                    double bx2 = (double) pos.getX() + orig.maxX - lx;
                    double by2 = (double) pos.getY() + orig.maxY - ly;
                    double bz2 = (double) pos.getZ() + orig.maxZ - lz;
                    event.renderer.box(bx1, by1, bz1, bx2, by2, bz2, color, lines, shapeMode.get(), 0);
                    break;
                }
                case Pulse: {
                    double elapsedTicks = renderTick - destroyProgressStart;
                    double seconds = Math.max(0.0, elapsedTicks / 20.0);
                    double phase = seconds * pulseFrequency.get() * Math.PI * 2.0;
                    double add = Math.sin(phase) * pulseAmplitude.get();
                    double shrinkAdj = Math.max(0.0, Math.min(1.0, shrinkFactor + add));
                    double xShrink2 = orig.getLengthX() * shrinkAdj / 2.0;
                    double yShrink2 = orig.getLengthY() * shrinkAdj / 2.0;
                    double zShrink2 = orig.getLengthZ() * shrinkAdj / 2.0;
                    double x1p = (double) pos.getX() + box.minX + xShrink2;
                    double y1p = (double) pos.getY() + box.minY + yShrink2;
                    double z1p = (double) pos.getZ() + box.minZ + zShrink2;
                    double x2p = (double) pos.getX() + box.maxX + xShrink2;
                    double y2p = (double) pos.getY() + box.maxY + yShrink2;
                    double z2p = (double) pos.getZ() + box.maxZ + zShrink2;
                    int sideA = (int) Math.round((double) pulseSideColor.get().a * (0.6 + 0.4 * Math.sin(phase)));
                    int lineA = (int) Math.round((double) pulseLineColor.get().a * (0.6 + 0.4 * Math.sin(phase)));
                    Color sidePulse = pulseSideColor.get().copy().a(Math.max(0, Math.min(255, sideA)));
                    Color linePulse = pulseLineColor.get().copy().a(Math.max(0, Math.min(255, lineA)));
                    event.renderer.box(x1p, y1p, z1p, x2p, y2p, z2p, sidePulse, linePulse, ShapeMode.Both, 0);
                    break;
                }
            }
        }
    }
}
