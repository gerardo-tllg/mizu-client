package meteordevelopment.meteorclient.systems.modules.player;

import java.util.Objects;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.player.StartBreakingBlockEvent;
import meteordevelopment.meteorclient.events.meteor.SilentMineFinishedEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
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
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_2846;
import net.minecraft.class_2868;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/player/SilentMine.class */
public class SilentMine extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgNoBlockDrop;
    private final SettingGroup sgRender;
    private final Setting<Double> range;
    public final Setting<Boolean> antiRubberband;
    public final Setting<Boolean> preSwitchSinglebreak;
    private final Setting<Integer> singleBreakFailTicks;
    public final Setting<Boolean> rebreakSetBlockBroken;
    public final Setting<Double> speedPercentage;
    public final Setting<Boolean> noBlockDrop;
    public final Setting<Double> swapThreshold;
    public final Setting<Boolean> onlyForObsidian;
    private final Setting<Boolean> render;
    private final Setting<Boolean> renderBlock;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    private final Setting<Boolean> debugRenderPrimary;
    private SilentMineBlock rebreakBlock;
    private SilentMineBlock delayedDestroyBlock;
    private class_2338 lastDelayedDestroyBlockPos;
    private double currentGameTickCalculated;
    private boolean needDelayedDestroySwapBack;
    private boolean needRebreakSwapBack;
    private boolean hasSwapped;
    private int originalSlot;
    private int totemPopTicks;
    private boolean isBeingTotemPopped;

    public SilentMine() {
        super(Categories.Player, "silent-mine", "Allows you to mine blocks without holding a pickaxe");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgNoBlockDrop = this.settings.createGroup("No Block Drop");
        this.sgRender = this.settings.createGroup("Render");
        this.range = this.sgGeneral.add(new DoubleSetting.Builder().name("range").defaultValue(5.14d).min(0.0d).sliderMax(7.0d).build());
        this.antiRubberband = this.sgGeneral.add(new BoolSetting.Builder().name("strict-anti-rubberband").defaultValue(true).build());
        this.preSwitchSinglebreak = this.sgGeneral.add(new BoolSetting.Builder().name("pre-switch-single-break").defaultValue(true).build());
        this.singleBreakFailTicks = this.sgGeneral.add(new IntSetting.Builder().name("single-break-fail-ticks").defaultValue(20).min(5).sliderMax(50).build());
        this.rebreakSetBlockBroken = this.sgGeneral.add(new BoolSetting.Builder().name("set-rebreak-block-broken").defaultValue(true).build());
        this.speedPercentage = this.sgGeneral.add(new DoubleSetting.Builder().name("speed-percentage").description("Percentage of vanilla mining speed (100% = vanilla, 70% = 70% of vanilla time).").defaultValue(100.0d).min(0.0d).sliderMax(100.0d).build());
        this.noBlockDrop = this.sgNoBlockDrop.add(new BoolSetting.Builder().name("no-block-drop").description("Prevents blocks from dropping by mining with gold pickaxe.").defaultValue(false).build());
        SettingGroup settingGroup = this.sgNoBlockDrop;
        DoubleSetting.Builder builderSliderMax = new DoubleSetting.Builder().name("swap-threshold").description("When to swap to gold pickaxe (higher = later).").defaultValue(0.7d).min(0.5d).max(0.95d).sliderMin(0.5d).sliderMax(0.95d);
        Setting<Boolean> setting = this.noBlockDrop;
        Objects.requireNonNull(setting);
        this.swapThreshold = settingGroup.add(builderSliderMax.visible(setting::get).build());
        SettingGroup settingGroup2 = this.sgNoBlockDrop;
        BoolSetting.Builder builderDefaultValue = new BoolSetting.Builder().name("only-for-obsidian").description("Only prevents drops for obsidian blocks.").defaultValue(true);
        Setting<Boolean> setting2 = this.noBlockDrop;
        Objects.requireNonNull(setting2);
        this.onlyForObsidian = settingGroup2.add(builderDefaultValue.visible(setting2::get).build());
        this.render = this.sgRender.add(new BoolSetting.Builder().name("do-render").defaultValue(true).build());
        this.renderBlock = this.sgRender.add(new BoolSetting.Builder().name("render-block").defaultValue(true).build());
        SettingGroup settingGroup3 = this.sgRender;
        EnumSetting.Builder builderDefaultValue2 = new EnumSetting.Builder().name("shape-mode").defaultValue(ShapeMode.Both);
        Setting<Boolean> setting3 = this.renderBlock;
        Objects.requireNonNull(setting3);
        this.shapeMode = settingGroup3.add(builderDefaultValue2.visible(setting3::get).build());
        this.sideColor = this.sgRender.add(new ColorSetting.Builder().name("side-color").defaultValue(new SettingColor(255, Opcode.GETFIELD, 255, 15)).visible(() -> {
            return this.renderBlock.get().booleanValue() && this.shapeMode.get().sides();
        }).build());
        this.lineColor = this.sgRender.add(new ColorSetting.Builder().name("line-color").defaultValue(new SettingColor(255, 255, 255, 60)).visible(() -> {
            return this.renderBlock.get().booleanValue() && this.shapeMode.get().lines();
        }).build());
        this.debugRenderPrimary = this.sgRender.add(new BoolSetting.Builder().name("debug-render-primary").defaultValue(false).build());
        this.rebreakBlock = null;
        this.delayedDestroyBlock = null;
        this.lastDelayedDestroyBlockPos = null;
        this.currentGameTickCalculated = 0.0d;
        this.needDelayedDestroySwapBack = false;
        this.needRebreakSwapBack = false;
        this.hasSwapped = false;
        this.originalSlot = -1;
        this.totemPopTicks = 0;
        this.isBeingTotemPopped = false;
        this.currentGameTickCalculated = RenderUtils.getCurrentGameTickCalculated();
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onDeactivate() {
        if (this.hasSwapped && this.mc.field_1724 != null && this.originalSlot != -1) {
            this.mc.field_1724.method_31548().field_7545 = this.originalSlot;
            this.mc.field_1724.field_3944.method_52787(new class_2868(this.originalSlot));
            this.hasSwapped = false;
            this.originalSlot = -1;
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        this.currentGameTickCalculated = RenderUtils.getCurrentGameTickCalculated();
        if (this.mc.field_1724 != null && this.mc.field_1724.method_6115() && this.mc.field_1724.method_6030().method_7909() == class_1802.field_8288) {
            this.isBeingTotemPopped = true;
            this.totemPopTicks = 0;
        } else {
            this.totemPopTicks++;
        }
        if (this.totemPopTicks > 30) {
            this.isBeingTotemPopped = false;
        }
        if (hasDelayedDestroy()) {
            this.lastDelayedDestroyBlockPos = this.delayedDestroyBlock.blockPos;
        } else {
            this.lastDelayedDestroyBlockPos = null;
        }
        if (hasDelayedDestroy() && (this.mc.field_1687.method_8320(this.delayedDestroyBlock.blockPos).method_26215() || !BlockUtils.canBreak(this.delayedDestroyBlock.blockPos))) {
            MeteorClient.EVENT_BUS.post(new SilentMineFinishedEvent.Post(this.delayedDestroyBlock.blockPos, false));
            this.delayedDestroyBlock = null;
            swapBackToNetherite();
        }
        if (this.rebreakBlock != null && (this.mc.field_1687.method_8320(this.rebreakBlock.blockPos).method_26215() || !BlockUtils.canBreak(this.rebreakBlock.blockPos))) {
            this.rebreakBlock.beenAir = true;
            swapBackToNetherite();
        }
        if (hasRebreakBlock() && this.rebreakBlock.timesSendBreakPacket > 10 && !canRebreakRebreakBlock()) {
            this.rebreakBlock.cancelBreaking();
            this.rebreakBlock = null;
        }
        if (hasDelayedDestroy() && this.delayedDestroyBlock.ticksHeldPickaxe <= this.singleBreakFailTicks.get().intValue()) {
            class_2680 blockState = this.mc.field_1687.method_8320(this.delayedDestroyBlock.blockPos);
            if (this.delayedDestroyBlock.isReady() && !this.isBeingTotemPopped) {
                if (this.noBlockDrop.get().booleanValue() && shouldPreventBlockDrop(blockState)) {
                    FindItemResult goldPickaxe = InvUtils.find(class_1802.field_8335);
                    if (goldPickaxe.found() && goldPickaxe.slot() >= 0 && goldPickaxe.slot() <= 8) {
                        if (this.originalSlot == -1) {
                            this.originalSlot = this.mc.field_1724.method_31548().field_7545;
                        }
                        this.mc.field_1724.method_31548().field_7545 = goldPickaxe.slot();
                        this.mc.field_1724.field_3944.method_52787(new class_2868(goldPickaxe.slot()));
                        this.hasSwapped = true;
                        this.delayedDestroyBlock.ticksHeldPickaxe++;
                        return;
                    }
                }
                FindItemResult result = InvUtils.findFastestTool(blockState);
                if (result.found() && this.mc.field_1724.method_31548().field_7545 != result.slot() && MeteorClient.SWAP.beginSwap(result, false)) {
                    this.needDelayedDestroySwapBack = true;
                }
                if (!result.found() || this.mc.field_1724.method_31548().field_7545 == result.slot()) {
                    this.delayedDestroyBlock.ticksHeldPickaxe++;
                }
            }
        }
        if (this.rebreakBlock != null && !this.isBeingTotemPopped) {
            class_2680 blockState2 = this.mc.field_1687.method_8320(this.rebreakBlock.blockPos);
            if (this.rebreakBlock.isReady()) {
                if (inBreakRange(this.rebreakBlock.blockPos)) {
                    if (this.noBlockDrop.get().booleanValue() && shouldPreventBlockDrop(blockState2)) {
                        FindItemResult goldPickaxe2 = InvUtils.find(class_1802.field_8335);
                        if (goldPickaxe2.found() && goldPickaxe2.slot() >= 0 && goldPickaxe2.slot() <= 8) {
                            if (this.originalSlot == -1) {
                                this.originalSlot = this.mc.field_1724.method_31548().field_7545;
                            }
                            this.mc.field_1724.method_31548().field_7545 = goldPickaxe2.slot();
                            this.mc.field_1724.field_3944.method_52787(new class_2868(goldPickaxe2.slot()));
                            this.hasSwapped = true;
                            this.rebreakBlock.tryBreak();
                            if (this.rebreakSetBlockBroken.get().booleanValue() && canRebreakRebreakBlock()) {
                                this.mc.field_1687.method_8501(this.rebreakBlock.blockPos, class_2246.field_10124.method_9564());
                                return;
                            }
                            return;
                        }
                    }
                    FindItemResult result2 = InvUtils.findFastestTool(blockState2);
                    MeteorClient.EVENT_BUS.post(new SilentMineFinishedEvent.Pre(this.rebreakBlock.blockPos, true));
                    if (result2.found() && this.mc.field_1724.method_31548().field_7545 != result2.slot() && MeteorClient.SWAP.beginSwap(result2, true)) {
                        this.needRebreakSwapBack = true;
                        if (this.originalSlot == -1 && this.noBlockDrop.get().booleanValue() && shouldPreventBlockDrop(blockState2)) {
                            this.originalSlot = result2.slot();
                        }
                    }
                    if (this.noBlockDrop.get().booleanValue() && !this.hasSwapped && shouldPreventBlockDrop(blockState2) && this.rebreakBlock.getBreakProgress() >= this.swapThreshold.get().doubleValue()) {
                        swapToGoldenPickaxe();
                    }
                    this.rebreakBlock.tryBreak();
                    if (this.needRebreakSwapBack) {
                        MeteorClient.SWAP.endSwap(true);
                    }
                    if (this.rebreakSetBlockBroken.get().booleanValue() && canRebreakRebreakBlock()) {
                        this.mc.field_1687.method_8501(this.rebreakBlock.blockPos, class_2246.field_10124.method_9564());
                    }
                } else {
                    this.rebreakBlock = null;
                }
            }
        }
        if (hasDelayedDestroy() && this.delayedDestroyBlock.ticksHeldPickaxe > this.singleBreakFailTicks.get().intValue()) {
            if (inBreakRange(this.delayedDestroyBlock.blockPos)) {
                class_2680 state = this.mc.field_1687.method_8320(this.delayedDestroyBlock.blockPos);
                if (this.noBlockDrop.get().booleanValue() && shouldPreventBlockDrop(state)) {
                    FindItemResult goldPickaxe3 = InvUtils.findInHotbar(class_1802.field_8335);
                    if (goldPickaxe3.found()) {
                        if (this.originalSlot == -1) {
                            this.originalSlot = this.mc.field_1724.method_31548().field_7545;
                        }
                        this.mc.field_1724.method_31548().field_7545 = goldPickaxe3.slot();
                        this.mc.field_1724.field_3944.method_52787(new class_2868(goldPickaxe3.slot()));
                        this.hasSwapped = true;
                    }
                }
                this.delayedDestroyBlock.startBreaking(true);
            } else {
                this.delayedDestroyBlock.cancelBreaking();
                this.delayedDestroyBlock = null;
            }
        }
        boolean delayedDestroyFinished = (hasDelayedDestroy() && this.delayedDestroyBlock.isReady()) ? false : true;
        if (this.needDelayedDestroySwapBack && delayedDestroyFinished) {
            MeteorClient.SWAP.endSwap(false);
            this.needDelayedDestroySwapBack = false;
        }
    }

    private void swapToGoldenPickaxe() {
        if (this.mc.field_1724 == null) {
            return;
        }
        FindItemResult goldenPickaxe = InvUtils.findInHotbar(class_1802.field_8335);
        if (goldenPickaxe.found()) {
            if (this.originalSlot == -1) {
                this.originalSlot = this.mc.field_1724.method_31548().field_7545;
            }
            this.mc.field_1724.method_31548().field_7545 = goldenPickaxe.slot();
            this.mc.field_1724.field_3944.method_52787(new class_2868(goldenPickaxe.slot()));
            this.hasSwapped = true;
        }
    }

    private void swapBackToNetherite() {
        if (this.mc.field_1724 == null || !this.hasSwapped || this.originalSlot == -1) {
            return;
        }
        this.mc.field_1724.method_31548().field_7545 = this.originalSlot;
        this.mc.field_1724.field_3944.method_52787(new class_2868(this.originalSlot));
        this.hasSwapped = false;
        this.originalSlot = -1;
    }

    private boolean shouldPreventBlockDrop(class_2680 state) {
        return !this.onlyForObsidian.get().booleanValue() || state.method_26204() == class_2246.field_10540 || state.method_26204() == class_2246.field_22423;
    }

    public void silentBreakBlock(class_2338 blockPos, class_2350 direction, double priority) {
        if (!isActive() || blockPos == null || alreadyBreaking(blockPos) || !BlockUtils.canBreak(blockPos, this.mc.field_1687.method_8320(blockPos)) || !inBreakRange(blockPos)) {
            return;
        }
        boolean isAntiSwimBlock = blockPos.equals(this.mc.field_1724.method_24515().method_10084());
        if (!hasDelayedDestroy()) {
            boolean willResetPrimary = (this.rebreakBlock == null || canRebreakRebreakBlock()) ? false : true;
            if (willResetPrimary && this.rebreakBlock.priority < priority) {
                return;
            }
            this.currentGameTickCalculated -= 0.1d;
            this.delayedDestroyBlock = new SilentMineBlock(blockPos, direction, priority, false);
            this.delayedDestroyBlock.startBreaking(true);
            if (willResetPrimary) {
                this.rebreakBlock.startBreaking(false);
            }
        }
        if (alreadyBreaking(blockPos)) {
            return;
        }
        if (this.rebreakBlock != null && this.delayedDestroyBlock != null && ((priority >= this.rebreakBlock.priority || canRebreakRebreakBlock()) && this.delayedDestroyBlock.getBreakProgress() <= 0.8d)) {
            this.rebreakBlock = null;
        }
        if (this.rebreakBlock == null || isAntiSwimBlock) {
            this.rebreakBlock = new SilentMineBlock(blockPos, direction, priority, true);
            this.rebreakBlock.startBreaking(false);
        }
    }

    @EventHandler
    public void onStartBreakingBlock(StartBreakingBlockEvent event) {
        event.cancel();
        silentBreakBlock(event.blockPos, event.direction, 100.0d);
    }

    public boolean canSwapBack() {
        return this.needDelayedDestroySwapBack && !(hasDelayedDestroy() && this.delayedDestroyBlock.isReady());
    }

    public boolean hasDelayedDestroy() {
        return this.delayedDestroyBlock != null;
    }

    public boolean hasRebreakBlock() {
        return (this.rebreakBlock == null || this.rebreakBlock.beenAir) ? false : true;
    }

    public class_2338 getDelayedDestroyBlockPos() {
        if (this.delayedDestroyBlock != null) {
            return this.delayedDestroyBlock.blockPos;
        }
        return null;
    }

    public void cancelBreaking() {
        if (this.rebreakBlock != null) {
            this.rebreakBlock.cancelBreaking();
            this.rebreakBlock = null;
        }
        if (this.delayedDestroyBlock != null) {
            this.delayedDestroyBlock.cancelBreaking();
            this.delayedDestroyBlock = null;
        }
        swapBackToNetherite();
    }

    public class_2338 getLastDelayedDestroyBlockPos() {
        return this.lastDelayedDestroyBlockPos;
    }

    public double getDelayedDestroyProgress() {
        if (this.delayedDestroyBlock != null) {
            return this.delayedDestroyBlock.getBreakProgress();
        }
        return 0.0d;
    }

    public class_2338 getRebreakBlockPos() {
        if (this.rebreakBlock != null) {
            return this.rebreakBlock.blockPos;
        }
        return null;
    }

    public double getRebreakBlockProgress() {
        if (this.rebreakBlock != null) {
            return this.rebreakBlock.getBreakProgress();
        }
        return 0.0d;
    }

    public boolean canRebreakRebreakBlock() {
        return this.rebreakBlock != null && this.rebreakBlock.beenAir;
    }

    public boolean inBreakRange(class_2338 blockPos) {
        return new class_238(blockPos).method_49271(this.mc.field_1724.method_33571()) <= this.range.get().doubleValue() * this.range.get().doubleValue();
    }

    public boolean alreadyBreaking(class_2338 blockPos) {
        return (this.rebreakBlock != null && blockPos.equals(this.rebreakBlock.blockPos)) || (this.delayedDestroyBlock != null && blockPos.equals(this.delayedDestroyBlock.blockPos));
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (this.render.get().booleanValue()) {
            double calculatedDrawGameTick = RenderUtils.getCurrentGameTickCalculated();
            if (this.rebreakBlock != null) {
                this.rebreakBlock.render(event, calculatedDrawGameTick, true);
            }
            if (this.delayedDestroyBlock != null) {
                this.delayedDestroyBlock.render(event, calculatedDrawGameTick, false);
            }
        }
    }

    @EventHandler
    private void onPacket(PacketEvent.Send event) {
        class_2846 class_2846Var = event.packet;
        if (class_2846Var instanceof class_2846) {
            class_2846 packet = class_2846Var;
            if (packet.method_12363() == class_2846.class_2847.field_12973 && this.antiRubberband.get().booleanValue() && (packet.method_12362().equals(getRebreakBlockPos()) || packet.method_12362().equals(getDelayedDestroyBlockPos()))) {
                this.mc.method_1562().method_52787(new class_2846(class_2846.class_2847.field_12971, packet.method_12362(), packet.method_12360()));
            }
            if (this.noBlockDrop.get().booleanValue()) {
                if (packet.method_12363() == class_2846.class_2847.field_12968 || packet.method_12363() == class_2846.class_2847.field_12973) {
                    class_2680 state = this.mc.field_1687.method_8320(packet.method_12362());
                    if (shouldPreventBlockDrop(state) && this.mc.field_1724 != null) {
                        FindItemResult goldenPickaxe = InvUtils.findInHotbar(class_1802.field_8335);
                        if (goldenPickaxe.found()) {
                            this.mc.field_1724.method_31548().field_7545 = goldenPickaxe.slot();
                            this.mc.field_1724.field_3944.method_52787(new class_2868(goldenPickaxe.slot()));
                            this.hasSwapped = true;
                        }
                    }
                }
            }
        }
    }

    private int getSeq() {
        return this.mc.field_1687.meteor$getAndIncrementSequence();
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/player/SilentMine$SilentMineBlock.class */
    class SilentMineBlock {
        public class_2338 blockPos;
        public class_2350 direction;
        public boolean started = false;
        public int timesSendBreakPacket = 0;
        public int ticksHeldPickaxe = 0;
        public boolean beenAir = false;
        private double destroyProgressStart = 0.0d;
        private double priority;
        private boolean isRebreak;

        public SilentMineBlock(class_2338 blockPos, class_2350 direction, double priority, boolean isRebreak) {
            this.priority = 0.0d;
            this.blockPos = blockPos;
            this.direction = direction;
            this.priority = priority;
            this.isRebreak = isRebreak;
        }

        public boolean isReady() {
            if (!BlockUtils.canBreak(this.blockPos)) {
                return false;
            }
            double breakProgressSingleTick = getBreakProgressSingleTick();
            double speedMultiplier = SilentMine.this.speedPercentage.get().doubleValue() / 100.0d;
            double baseThreshold = this.isRebreak ? 0.7d : 1.0d;
            double threshold = baseThreshold - ((!SilentMine.this.preSwitchSinglebreak.get().booleanValue() || this.isRebreak) ? 0.0d : (breakProgressSingleTick / speedMultiplier) / 2.0d);
            return getBreakProgress() >= threshold || this.timesSendBreakPacket > 0;
        }

        public void startBreaking(boolean isDelayedDestroy) {
            this.ticksHeldPickaxe = 0;
            this.timesSendBreakPacket = 0;
            this.destroyProgressStart = SilentMine.this.currentGameTickCalculated;
            if (isDelayedDestroy && SilentMine.this.canRebreakRebreakBlock()) {
                SilentMine.this.rebreakBlock = null;
            }
            SilentMine.this.mc.method_1562().method_52787(new class_2846(class_2846.class_2847.field_12973, this.blockPos, this.direction, SilentMine.this.getSeq()));
            SilentMine.this.mc.method_1562().method_52787(new class_2846(class_2846.class_2847.field_12968, this.blockPos, this.direction, SilentMine.this.getSeq()));
            SilentMine.this.mc.method_1562().method_52787(new class_2846(class_2846.class_2847.field_12973, this.blockPos, this.direction, SilentMine.this.getSeq()));
            SilentMine.this.mc.method_1562().method_52787(new class_2846(class_2846.class_2847.field_12973, this.blockPos, this.direction, SilentMine.this.getSeq()));
            SilentMine.this.mc.method_1562().method_52787(new class_2846(class_2846.class_2847.field_12968, this.blockPos, this.direction, SilentMine.this.getSeq()));
            SilentMine.this.mc.method_1562().method_52787(new class_2846(class_2846.class_2847.field_12973, this.blockPos, this.direction, SilentMine.this.getSeq()));
            if (!SilentMine.this.antiRubberband.get().booleanValue()) {
                SilentMine.this.mc.method_1562().method_52787(new class_2846(class_2846.class_2847.field_12971, this.blockPos, this.direction));
                SilentMine.this.mc.method_1562().method_52787(new class_2846(class_2846.class_2847.field_12971, this.blockPos, this.direction));
            }
            this.started = true;
        }

        public void tryBreak() {
            SilentMine.this.mc.method_1562().method_52787(new class_2846(class_2846.class_2847.field_12973, this.blockPos, this.direction, SilentMine.this.getSeq()));
            if (!SilentMine.this.antiRubberband.get().booleanValue()) {
                SilentMine.this.mc.method_1562().method_52787(new class_2846(class_2846.class_2847.field_12971, this.blockPos, this.direction));
            }
            this.timesSendBreakPacket++;
        }

        public void cancelBreaking() {
            SilentMine.this.mc.method_1562().method_52787(new class_2846(class_2846.class_2847.field_12971, this.blockPos, this.direction));
        }

        public double getBreakProgress() {
            return getBreakProgress(SilentMine.this.currentGameTickCalculated);
        }

        public double getBreakProgress(double gameTick) {
            class_2680 state = SilentMine.this.mc.field_1687.method_8320(this.blockPos);
            FindItemResult bestTool = InvUtils.findFastestToolHotbar(state);
            int toolSlot = bestTool.found() ? bestTool.slot() : SilentMine.this.mc.field_1724.method_31548().field_7545;
            class_238 boundingBox = SilentMine.this.mc.field_1724.method_5829();
            double playerFeetY = boundingBox.field_1322;
            class_238 groundBox = new class_238(boundingBox.field_1323, playerFeetY - 0.2d, boundingBox.field_1321, boundingBox.field_1320, playerFeetY, boundingBox.field_1324);
            boolean willBeOnGround = false;
            for (class_2338 pos : BlockUtils.iterate(groundBox)) {
                class_2680 blockState = SilentMine.this.mc.field_1687.method_8320(pos);
                if (blockState.method_26212(SilentMine.this.mc.field_1687, pos)) {
                    double blockTopY = ((double) pos.method_10264()) + 1.0d;
                    double distanceToBlock = playerFeetY - blockTopY;
                    if (distanceToBlock >= 0.0d && distanceToBlock < Math.abs(SilentMine.this.mc.field_1724.method_18798().field_1351 * 2.0d)) {
                        willBeOnGround = true;
                    }
                }
            }
            double baseBreakingSpeed = BlockUtils.getBlockBreakingSpeed(toolSlot, state, RotationManager.lastGround || (willBeOnGround && !this.isRebreak));
            double speedMultiplier = SilentMine.this.speedPercentage.get().doubleValue() / 100.0d;
            double adjustedBreakingSpeed = baseBreakingSpeed / speedMultiplier;
            return Math.min(BlockUtils.getBreakDelta(adjustedBreakingSpeed, state) * (gameTick - this.destroyProgressStart), 1.0d);
        }

        public double getBreakProgressSingleTick() {
            return getBreakProgress(this.destroyProgressStart + 1.0d);
        }

        public double getPriority() {
            return this.priority;
        }

        public void render(Render3DEvent event, double renderTick, boolean isPrimary) {
            class_265 shape = SilentMine.this.mc.field_1687.method_8320(this.blockPos).method_26218(SilentMine.this.mc.field_1687, this.blockPos);
            if (shape == null || shape.method_1110()) {
                event.renderer.box(this.blockPos, SilentMine.this.sideColor.get(), SilentMine.this.lineColor.get(), SilentMine.this.shapeMode.get(), 0);
                return;
            }
            class_238 orig = shape.method_1107();
            double shrinkFactor = 1.0d - Math.clamp(isPrimary ? getBreakProgress(renderTick) * 1.4285714285714286d : getBreakProgress(renderTick), 0.0d, 1.0d);
            class_2338 pos = this.blockPos;
            class_238 box = orig.method_1002(orig.method_17939() * shrinkFactor, orig.method_17940() * shrinkFactor, orig.method_17941() * shrinkFactor);
            double xShrink = (orig.method_17939() * shrinkFactor) / 2.0d;
            double yShrink = (orig.method_17940() * shrinkFactor) / 2.0d;
            double zShrink = (orig.method_17941() * shrinkFactor) / 2.0d;
            double x1 = ((double) pos.method_10263()) + box.field_1323 + xShrink;
            double y1 = ((double) pos.method_10264()) + box.field_1322 + yShrink;
            double z1 = ((double) pos.method_10260()) + box.field_1321 + zShrink;
            double x2 = ((double) pos.method_10263()) + box.field_1320 + xShrink;
            double y2 = ((double) pos.method_10264()) + box.field_1325 + yShrink;
            double z2 = ((double) pos.method_10260()) + box.field_1324 + zShrink;
            Color color = SilentMine.this.sideColor.get();
            if (SilentMine.this.debugRenderPrimary.get().booleanValue() && isPrimary) {
                color = Color.ORANGE.a(40);
            }
            event.renderer.box(x1, y1, z1, x2, y2, z2, color, SilentMine.this.lineColor.get(), SilentMine.this.shapeMode.get(), 0);
        }
    }
}
