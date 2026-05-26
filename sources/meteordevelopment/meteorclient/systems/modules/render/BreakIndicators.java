package meteordevelopment.meteorclient.systems.modules.render;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.player.SilentMine;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_2620;
import net.minecraft.class_265;
import net.minecraft.class_2680;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/BreakIndicators.class */
public class BreakIndicators extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgRender;
    private final Setting<Double> rebreakCompletionAmount;
    private final Setting<Double> completionAmount;
    private final Setting<Double> removeCompletionAmount;
    private final Setting<Boolean> ignoreFriends;
    private final Setting<Boolean> render;
    private final Setting<Boolean> useDoubleminePrediction;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    private final Queue<BlockBreak> _breakPackets;
    public final Map<class_2338, BlockBreak> breakStartTimes;
    private final Map<class_2338, BlockBreak> predictedDoublemine;

    public BreakIndicators() {
        super(Categories.Render, "break-indicators", "Renders the progress of a block being broken.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgRender = this.settings.createGroup("Render");
        this.rebreakCompletionAmount = this.sgGeneral.add(new DoubleSetting.Builder().name("rebreak-completion-amount").description("Determines how fast rendering increases of a suspected rebreak block. Smaller is faster.").defaultValue(0.7d).min(0.0d).sliderMax(1.5d).build());
        this.completionAmount = this.sgGeneral.add(new DoubleSetting.Builder().name("full-completion-amount").description("Determines how fast rendering increases. Smaller is faster.").defaultValue(1.0d).min(0.0d).sliderMax(1.5d).build());
        this.removeCompletionAmount = this.sgGeneral.add(new DoubleSetting.Builder().name("force-remove-completion-amount").description("Determines how long it takes to forcibly remove a block from being rendered.").defaultValue(1.3d).min(0.0d).sliderMax(1.5d).build());
        this.ignoreFriends = this.sgGeneral.add(new BoolSetting.Builder().name("ignore-friends").description("Doesn't render blocks that friends are breaking.").defaultValue(false).build());
        this.render = this.sgRender.add(new BoolSetting.Builder().name("do-render").description("Renders the blocks in queue to be broken.").defaultValue(true).build());
        this.useDoubleminePrediction = this.sgRender.add(new BoolSetting.Builder().name("use-doublemine-predicition").description("Does some fancy stuff to make indicators more accurate.").defaultValue(false).build());
        SettingGroup settingGroup = this.sgRender;
        EnumSetting.Builder builderDefaultValue = new EnumSetting.Builder().name("shape-mode").description("How the shapes are rendered.").defaultValue(ShapeMode.Both);
        Setting<Boolean> setting = this.render;
        Objects.requireNonNull(setting);
        this.shapeMode = settingGroup.add(builderDefaultValue.visible(setting::get).build());
        this.sideColor = this.sgRender.add(new ColorSetting.Builder().name("side-color").description("The side color of the rendering.").defaultValue(new SettingColor(255, 0, 80, 10)).visible(() -> {
            return this.render.get().booleanValue() && this.shapeMode.get().sides();
        }).build());
        this.lineColor = this.sgRender.add(new ColorSetting.Builder().name("line-color").description("The line color of the rendering.").defaultValue(new SettingColor(255, 255, 255, 40)).visible(() -> {
            return this.render.get().booleanValue() && this.shapeMode.get().lines();
        }).build());
        this._breakPackets = new ConcurrentLinkedQueue();
        this.breakStartTimes = new HashMap();
        this.predictedDoublemine = new HashMap();
    }

    @EventHandler
    private void onPacket(PacketEvent.Receive event) {
        class_2620 class_2620Var = event.packet;
        if (class_2620Var instanceof class_2620) {
            class_2620 packet = class_2620Var;
            class_1297 entity = this.mc.field_1687.method_8469(packet.method_11280());
            this._breakPackets.add(new BlockBreak(packet.method_11277(), RenderUtils.getCurrentGameTickCalculated(), entity));
        }
    }

    public boolean isBlockBeingBroken(class_2338 blockPos) {
        return this.breakStartTimes.containsKey(blockPos);
    }

    public boolean isBeingDoublemined(class_2338 blockPos) {
        return this.predictedDoublemine.containsKey(blockPos);
    }

    public class_1657 getPlayerDoubleminingBlock(class_2338 blockPos) {
        return this.predictedDoublemine.get(blockPos).entity;
    }

    public double getBlockProgress(class_2338 blockPos) {
        if (!this.breakStartTimes.containsKey(blockPos)) {
            return 0.0d;
        }
        return this.breakStartTimes.get(blockPos).getBreakProgress(RenderUtils.getCurrentGameTickCalculated());
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        double currentGameTickCalculated = RenderUtils.getCurrentGameTickCalculated();
        while (!this._breakPackets.isEmpty()) {
            BlockBreak breakEvent = this._breakPackets.remove();
            if (breakEvent.entity != null && (breakEvent.entity instanceof class_1657)) {
                List<BlockBreak> playerBreakingBlocks = this.breakStartTimes.values().stream().filter(x -> {
                    return x.entity == breakEvent.entity && !x.blockPos.equals(breakEvent.blockPos);
                }).sorted((block1, block2) -> {
                    return Double.compare(block1.startTick, block2.startTick);
                }).toList();
                if (playerBreakingBlocks.size() >= 3) {
                    BlockBreak oldestBlock = playerBreakingBlocks.get(0);
                    double oldestProgress = oldestBlock.getBreakProgress(currentGameTickCalculated);
                    if (oldestProgress > 0.8d) {
                        this.predictedDoublemine.remove(oldestBlock.blockPos);
                    }
                }
            }
            if (!this.breakStartTimes.containsKey(breakEvent.blockPos)) {
                this.breakStartTimes.put(breakEvent.blockPos, breakEvent);
            }
            if (!this.predictedDoublemine.containsKey(breakEvent.blockPos)) {
                this.predictedDoublemine.put(breakEvent.blockPos, breakEvent);
            }
        }
        Iterator<Map.Entry<class_2338, BlockBreak>> iterator = this.breakStartTimes.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<class_2338, BlockBreak> entry = iterator.next();
            if (this.mc.field_1687.method_8320(entry.getKey()).method_26215() || entry.getValue().getBreakProgress(currentGameTickCalculated) > this.removeCompletionAmount.get().doubleValue() || !BlockUtils.canBreak(entry.getKey())) {
                iterator.remove();
            }
        }
        Iterator<Map.Entry<class_2338, BlockBreak>> iterator2 = this.predictedDoublemine.entrySet().iterator();
        while (iterator2.hasNext()) {
            Map.Entry<class_2338, BlockBreak> entry2 = iterator2.next();
            if (this.mc.field_1687.method_8320(entry2.getKey()).method_26215() || entry2.getValue().getBreakProgress(currentGameTickCalculated) > this.removeCompletionAmount.get().doubleValue() || !BlockUtils.canBreak(entry2.getKey())) {
                iterator2.remove();
            }
        }
        if (this.useDoubleminePrediction.get().booleanValue()) {
            for (Map.Entry<class_2338, BlockBreak> entry3 : this.predictedDoublemine.entrySet()) {
                if (this.ignoreFriends.get().booleanValue() && entry3.getValue().entity != null) {
                    class_1657 class_1657Var = entry3.getValue().entity;
                    if (class_1657Var instanceof class_1657) {
                        class_1657 player = class_1657Var;
                        if (Friends.get().isFriend(player)) {
                        }
                    }
                }
                entry3.getValue().renderBlock(event, currentGameTickCalculated);
            }
        } else {
            for (Map.Entry<class_2338, BlockBreak> entry4 : this.breakStartTimes.entrySet()) {
                if (this.ignoreFriends.get().booleanValue() && entry4.getValue().entity != null) {
                    class_1657 class_1657Var2 = entry4.getValue().entity;
                    if (class_1657Var2 instanceof class_1657) {
                        class_1657 player2 = class_1657Var2;
                        if (Friends.get().isFriend(player2)) {
                        }
                    }
                }
                entry4.getValue().renderBlock(event, currentGameTickCalculated);
            }
        }
        Map<class_1657, List<BlockBreak>> doublemineBreakingBlocks = (Map) this.predictedDoublemine.values().stream().sorted(Comparator.comparingDouble(blockBreak -> {
            return blockBreak.startTick;
        })).filter(blockBreak2 -> {
            return blockBreak2.entity instanceof class_1657;
        }).collect(Collectors.groupingBy(blockBreak3 -> {
            return blockBreak3.entity;
        }, Collectors.toList()));
        for (Map.Entry<class_1657, List<BlockBreak>> entry5 : doublemineBreakingBlocks.entrySet()) {
            entry5.getValue().forEach(x2 -> {
                x2.isRebreak = false;
            });
            if (entry5.getValue().size() >= 2) {
                ((BlockBreak) entry5.getValue().getLast()).isRebreak = true;
            }
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/BreakIndicators$BlockBreak.class */
    public class BlockBreak {
        public class_2338 blockPos;
        public double startTick;
        public class_1297 entity;
        public boolean isRebreak = false;

        public BlockBreak(class_2338 blockPos, double startTick, class_1297 entity) {
            this.blockPos = blockPos;
            this.startTick = startTick;
            this.entity = entity;
        }

        public void renderBlock(Render3DEvent event, double currentTick) {
            class_265 shape = BreakIndicators.this.mc.field_1687.method_8320(this.blockPos).method_26218(BreakIndicators.this.mc.field_1687, this.blockPos);
            if (shape == null || shape.method_1110()) {
                event.renderer.box(this.blockPos, BreakIndicators.this.sideColor.get(), BreakIndicators.this.lineColor.get(), BreakIndicators.this.shapeMode.get(), 0);
                return;
            }
            class_238 orig = shape.method_1107();
            double completion = (this.isRebreak ? BreakIndicators.this.rebreakCompletionAmount.get() : BreakIndicators.this.completionAmount.get()).doubleValue();
            double shrinkFactor = Math.clamp(1.0d - (getBreakProgress(currentTick) * (1.0d / completion)), 0.0d, 1.0d);
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
            Color color = BreakIndicators.this.sideColor.get();
            event.renderer.box(x1, y1, z1, x2, y2, z2, color, BreakIndicators.this.lineColor.get(), BreakIndicators.this.shapeMode.get(), 0);
        }

        public double getBreakProgress(double currentTick) {
            SilentMine silentMine;
            class_2680 state = BreakIndicators.this.mc.field_1687.method_8320(this.blockPos);
            FindItemResult slot = InvUtils.findFastestToolHotbar(BreakIndicators.this.mc.field_1687.method_8320(this.blockPos));
            double breakingSpeed = BlockUtils.getBlockBreakingSpeed(slot.found() ? slot.slot() : BreakIndicators.this.mc.field_1724.method_31548().method_67532(), state, true);
            if (this.entity == BreakIndicators.this.mc.field_1724 && (silentMine = (SilentMine) Modules.get().get(SilentMine.class)) != null && silentMine.isActive()) {
                double speedMultiplier = silentMine.speedPercentage.get().doubleValue() / 100.0d;
                breakingSpeed /= speedMultiplier;
            }
            return BlockUtils.getBreakDelta(breakingSpeed, state) * (currentTick - this.startTick);
        }
    }
}
