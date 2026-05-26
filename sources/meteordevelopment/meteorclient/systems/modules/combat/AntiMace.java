package meteordevelopment.meteorclient.systems.modules.combat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.MeteorClient;
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
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1657;
import net.minecraft.class_1743;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2767;
import net.minecraft.class_2960;
import net.minecraft.class_3417;
import net.minecraft.class_7923;
import net.minecraft.class_9362;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/AntiMace.class */
public class AntiMace extends Module {
    private static final double GRAVITY = -0.08d;
    private static final double TERMINAL_VELOCITY = -3.92d;
    private static final double DRAG_XZ_FLY = 0.99d;
    private static final double DRAG_Y_FLY = 0.98d;
    private static final double ALIGN_D = 1.5d;
    private static final double ALIGN_E = 0.01d;
    private static final double LOOK_PUSH = 0.1d;
    private static final int BOOST_DURATION_TICKS = 40;
    private static final double ELYTRA_GRAVITY = -0.04d;
    private final SettingGroup sgGeneral;
    private final SettingGroup sgPrediction;
    private final SettingGroup sgRender;
    private final Setting<Material> material;
    private final Setting<Integer> aboveOffset;
    private final Setting<Double> placeRange;
    private final Setting<SortPriority> priority;
    private final Setting<Boolean> onlyAbove;
    private final Setting<Boolean> requireMaceInHand;
    private final Setting<Boolean> pauseEat;
    private final Setting<Boolean> angleDetection;
    private final Setting<Double> steepAngleThreshold;
    private final Setting<Boolean> autoAirPlace;
    private final Setting<Boolean> predictionEnabled;
    private final Setting<Integer> fallPredictionTicks;
    private final Setting<Integer> elytraPredictionTicks;
    private final Setting<Boolean> render;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> predictColor;
    private final Setting<SettingColor> placementColor;
    private class_1657 target;
    private class_243 lastPredictedCenter;
    private final List<class_2338> lastPlacedThisTick;
    private final Map<UUID, class_243> lastPos;
    private final Map<UUID, class_243> estVel;
    private final Map<UUID, Long> lastServerTick;
    private final Map<UUID, Integer> boostingTicks;
    private double lastPredictedDistance;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/AntiMace$Material.class */
    public enum Material {
        Obsidian,
        Cobweb
    }

    public AntiMace() {
        super(Categories.Combat, "anti-mace", "Places a 7-block pattern above you to block a falling/elytra mace attacker.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgPrediction = this.settings.createGroup("Prediction");
        this.sgRender = this.settings.createGroup("Render");
        this.material = this.sgGeneral.add(new EnumSetting.Builder().name("material").description("Material to counter the mace with.").defaultValue(Material.Obsidian).build());
        this.aboveOffset = this.sgGeneral.add(new IntSetting.Builder().name("above-offset").description("How many blocks above your head to place the pattern so mace dive/aura cannot reach you.").defaultValue(3).min(1).sliderRange(1, 6).build());
        this.placeRange = this.sgGeneral.add(new DoubleSetting.Builder().name("place-range").description("Max distance to the predicted point to activate.").defaultValue(4.5d).min(1.0d).sliderMax(8.0d).build());
        this.priority = this.sgGeneral.add(new EnumSetting.Builder().name("target-priority").description("How to select the player to target.").defaultValue(SortPriority.ClosestAngle).build());
        this.onlyAbove = this.sgGeneral.add(new BoolSetting.Builder().name("only-above").description("Only target players above you.").defaultValue(true).build());
        this.requireMaceInHand = this.sgGeneral.add(new BoolSetting.Builder().name("require-holding-mace").description("Require the target to be holding a mace (toggle off for silent swap).").defaultValue(true).build());
        this.pauseEat = this.sgGeneral.add(new BoolSetting.Builder().name("pause-eat").description("Pause while using an item (e.g., eating).").defaultValue(true).build());
        this.angleDetection = this.sgGeneral.add(new BoolSetting.Builder().name("angle-detection").description("(Obsidian) Auto-detect the attacker's mace dive/aura angle and shape the obsidian pattern to intercept it.").defaultValue(true).visible(() -> {
            return this.material.get() == Material.Obsidian;
        }).build());
        this.steepAngleThreshold = this.sgGeneral.add(new DoubleSetting.Builder().name("steep-angle-threshold").description("Pitch (degrees) above which the attack is treated as a vertical dive. Below it, a wedge is built toward the attacker.").defaultValue(60.0d).min(20.0d).max(89.0d).sliderRange(20.0d, 89.0d).visible(() -> {
            return this.material.get() == Material.Obsidian && this.angleDetection.get().booleanValue();
        }).build());
        this.autoAirPlace = this.sgGeneral.add(new BoolSetting.Builder().name("auto-air-place").description("(Obsidian) Order placements so blocks adjacent to existing supports go first; the seed block is air-placed when needed and the rest chain off it.").defaultValue(true).visible(() -> {
            return this.material.get() == Material.Obsidian;
        }).build());
        this.predictionEnabled = this.sgPrediction.add(new BoolSetting.Builder().name("prediction").description("Predict with vanilla-like physics (no ground raycast).").defaultValue(true).build());
        SettingGroup settingGroup = this.sgPrediction;
        IntSetting.Builder builderSliderMax = new IntSetting.Builder().name("fall-prediction-ticks").description("Ticks to simulate ahead while falling.").defaultValue(7).min(0).sliderMax(20);
        Setting<Boolean> setting = this.predictionEnabled;
        Objects.requireNonNull(setting);
        this.fallPredictionTicks = settingGroup.add(builderSliderMax.visible(setting::get).build());
        SettingGroup settingGroup2 = this.sgPrediction;
        IntSetting.Builder builderSliderMax2 = new IntSetting.Builder().name("elytra-prediction-ticks").description("Ticks to simulate ahead while elytra flying.").defaultValue(7).min(0).sliderMax(20);
        Setting<Boolean> setting2 = this.predictionEnabled;
        Objects.requireNonNull(setting2);
        this.elytraPredictionTicks = settingGroup2.add(builderSliderMax2.visible(setting2::get).build());
        this.render = this.sgRender.add(new BoolSetting.Builder().name("render").description("Render predicted place position and last placed blocks.").defaultValue(true).build());
        this.shapeMode = this.sgRender.add(new EnumSetting.Builder().name("shape-mode").description("How shapes are rendered.").defaultValue(ShapeMode.Both).build());
        this.predictColor = this.sgRender.add(new ColorSetting.Builder().name("predict-color").description("Predicted attacker point color.").defaultValue(new SettingColor(255, Opcode.F2L, 0, 35)).build());
        this.placementColor = this.sgRender.add(new ColorSetting.Builder().name("placement-color").description("Color of the placed blocks.").defaultValue(new SettingColor(0, 200, 255, 35)).build());
        this.lastPlacedThisTick = new ArrayList();
        this.lastPos = new HashMap();
        this.estVel = new HashMap();
        this.lastServerTick = new HashMap();
        this.boostingTicks = new HashMap();
        this.lastPredictedDistance = 0.0d;
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        this.target = null;
        this.lastPredictedCenter = null;
        this.lastPlacedThisTick.clear();
        this.lastPos.clear();
        this.estVel.clear();
        this.lastServerTick.clear();
        this.boostingTicks.clear();
        this.lastPredictedDistance = 0.0d;
    }

    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        if (this.mc.field_1687 != null && !this.mc.field_1687.method_18456().isEmpty()) {
            class_2767 class_2767Var = event.packet;
            if (class_2767Var instanceof class_2767) {
                class_2767 packet = class_2767Var;
                if (packet.method_11894().comp_349() == class_3417.field_49785) {
                    class_243 soundPos = new class_243(packet.method_11890(), packet.method_11889(), packet.method_11893());
                    for (class_1657 player : this.mc.field_1687.method_18456()) {
                        if (player.method_19538().method_1025(soundPos) < 9.0d) {
                            this.boostingTicks.put(player.method_5667(), 40);
                            return;
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!this.boostingTicks.isEmpty()) {
            this.boostingTicks.entrySet().removeIf(entry -> {
                int ticks = ((Integer) entry.getValue()).intValue() - 1;
                if (ticks <= 0) {
                    return true;
                }
                entry.setValue(Integer.valueOf(ticks));
                return false;
            });
        }
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: java.lang.MatchException */
    @EventHandler
    private void onTick(TickEvent.Post event) throws MatchException {
        class_243 future;
        List<class_2338> pattern;
        this.lastPredictedCenter = null;
        this.lastPlacedThisTick.clear();
        this.lastPredictedDistance = 0.0d;
        if (this.mc.field_1687 == null || this.mc.field_1724 == null) {
            return;
        }
        if (this.pauseEat.get().booleanValue() && this.mc.field_1724.method_6115()) {
            return;
        }
        if (this.target == null || TargetUtils.isBadTarget(this.target, 14.0d)) {
            this.target = TargetUtils.getPlayerTarget(14.0d, this.priority.get());
            if (this.target == null) {
                return;
            }
        }
        if (!this.onlyAbove.get().booleanValue() || this.target.method_23318() > this.mc.field_1724.method_23318() + 1.0d) {
            if (!this.requireMaceInHand.get().booleanValue() || isHoldingMace(this.target)) {
                UUID id = this.target.method_5667();
                long svTime = this.mc.field_1687.method_8510();
                class_243 currPos = this.target.method_19538();
                class_243 prevPos = this.lastPos.get(id);
                Long lastSv = this.lastServerTick.get(id);
                if (prevPos != null && lastSv != null) {
                    long dt = Math.max(1L, svTime - lastSv.longValue());
                    class_243 delta = currPos.method_1020(prevPos);
                    this.estVel.put(id, new class_243(delta.field_1352 / dt, delta.field_1351 / dt, delta.field_1350 / dt));
                } else {
                    this.estVel.put(id, this.target.method_18798());
                }
                this.lastPos.put(id, currPos);
                this.lastServerTick.put(id, Long.valueOf(svTime));
                boolean elytra = this.target.method_6128();
                boolean falling = (elytra || this.target.method_24828()) ? false : true;
                if (falling || elytra) {
                    if (this.predictionEnabled.get().booleanValue()) {
                        if (elytra) {
                            if (this.boostingTicks.containsKey(id)) {
                                future = simulateBoostedElytraFuturePos(this.target, this.elytraPredictionTicks.get().intValue());
                            } else {
                                future = simulateElytraFuturePos(this.target, this.elytraPredictionTicks.get().intValue());
                            }
                        } else {
                            future = simulateFallFrom(currPos, this.estVel.getOrDefault(id, this.target.method_18798()), this.fallPredictionTicks.get().intValue());
                        }
                    } else {
                        future = currPos;
                    }
                    class_243 eyes = this.mc.field_1724.method_33571();
                    double distanceToRawFuture = eyes.method_1022(future);
                    this.lastPredictedDistance = distanceToRawFuture;
                    this.lastPredictedCenter = class_243.method_24953(class_2338.method_49638(future));
                    if (distanceToRawFuture > this.placeRange.get().doubleValue()) {
                        return;
                    }
                    class_2338 centerPos = this.mc.field_1724.method_24515().method_10086(this.aboveOffset.get().intValue());
                    boolean obsidianAngle = this.material.get() == Material.Obsidian && this.angleDetection.get().booleanValue();
                    if (obsidianAngle) {
                        pattern = buildAttackAnglePattern(centerPos, future);
                    } else {
                        pattern = new ArrayList(7);
                        pattern.add(centerPos);
                        pattern.add(centerPos.method_10084());
                        pattern.add(centerPos.method_10074());
                        pattern.add(centerPos.method_10095());
                        pattern.add(centerPos.method_10072());
                        pattern.add(centerPos.method_10078());
                        pattern.add(centerPos.method_10067());
                    }
                    class_2248 desiredBlock = this.material.get() == Material.Obsidian ? class_2246.field_10540 : class_2246.field_10343;
                    pattern.removeIf(pos -> {
                        return this.mc.field_1687.method_8320(pos).method_26204() == desiredBlock;
                    });
                    if (pattern.isEmpty()) {
                        return;
                    }
                    if (this.material.get() == Material.Obsidian && this.autoAirPlace.get().booleanValue()) {
                        pattern = orderForCascade(pattern);
                    } else {
                        pattern.sort(Comparator.comparingInt((v0) -> {
                            return v0.method_10264();
                        }).reversed().thenComparingDouble(p -> {
                            return eyes.method_1025(class_243.method_24953(p));
                        }));
                    }
                    class_1792 useItem = findUseItem();
                    if (useItem != null && MeteorClient.BLOCK.beginPlacement(pattern, useItem)) {
                        for (class_2338 pos2 : pattern) {
                            if (MeteorClient.BLOCK.placeBlock(useItem, pos2)) {
                                this.lastPlacedThisTick.add(pos2);
                            }
                        }
                        MeteorClient.BLOCK.endPlacement();
                    }
                }
            }
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (this.render.get().booleanValue()) {
            if (this.lastPredictedCenter != null) {
                event.renderer.box(class_238.method_30048(this.lastPredictedCenter, LOOK_PUSH, LOOK_PUSH, LOOK_PUSH), this.predictColor.get(), this.predictColor.get(), ShapeMode.Both, 0);
            }
            if (!this.lastPlacedThisTick.isEmpty()) {
                for (class_2338 pos : this.lastPlacedThisTick) {
                    event.renderer.box(pos, this.placementColor.get(), this.placementColor.get(), this.shapeMode.get(), 0);
                }
            }
        }
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public String getInfoString() {
        if (this.target == null) {
            return null;
        }
        return String.format("%s [%.1f]", EntityUtils.getName(this.target), Double.valueOf(this.lastPredictedDistance));
    }

    private boolean isHoldingMace(class_1657 p) {
        return isMace(p.method_6047()) || isMace(p.method_6079());
    }

    private boolean isMace(class_1799 stack) {
        class_2960 id;
        if (stack == null || stack.method_7960()) {
            return false;
        }
        class_1792 item = stack.method_7909();
        if (item == class_1802.field_49814 || (item instanceof class_9362) || item == class_1802.field_49098) {
            return true;
        }
        if ((item instanceof class_1743) && (id = class_7923.field_41178.method_10221(item)) != null && ("netherite_axe".equals(id.method_12832()) || "minecraft:netherite_axe".equals(id.toString()))) {
            return true;
        }
        class_2960 id2 = class_7923.field_41178.method_10221(item);
        return id2 != null && ("mace".equals(id2.method_12832()) || "minecraft:mace".equals(id2.toString()));
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: java.lang.MatchException */
    private class_1792 findUseItem() throws MatchException {
        class_1792 class_1792Var;
        switch (this.material.get()) {
            case Obsidian:
                class_1792Var = class_1802.field_8281;
                break;
            case Cobweb:
                class_1792Var = class_1802.field_8786;
                break;
            default:
                throw new MatchException((String) null, (Throwable) null);
        }
        class_1792 targetItem = class_1792Var;
        FindItemResult result = InvUtils.findInHotbar(targetItem);
        if (result.found()) {
            return targetItem;
        }
        return null;
    }

    private class_243 simulateFallFrom(class_243 pos, class_243 velPerTick, int ticks) {
        class_243 p = pos;
        class_243 v = velPerTick;
        for (int i = 0; i < ticks; i++) {
            p = p.method_1019(v);
            double vy = v.field_1351 + GRAVITY;
            double vx = v.field_1352 * DRAG_Y_FLY;
            double vy2 = vy * DRAG_Y_FLY;
            double vz = v.field_1350 * DRAG_Y_FLY;
            if (vy2 < TERMINAL_VELOCITY) {
                vy2 = -3.92d;
            }
            v = new class_243(vx, vy2, vz);
        }
        return p;
    }

    private class_243 simulateElytraFuturePos(class_1657 player, int ticks) {
        class_243 pos = player.method_19538();
        class_243 vel = player.method_18798();
        float pitchRad = (float) Math.toRadians(player.method_36455());
        class_243 look = player.method_5720();
        double cos = Math.cos(pitchRad);
        for (int i = 0; i < ticks; i++) {
            pos = pos.method_1019(vel);
            double horizSpeed = Math.hypot(vel.field_1352, vel.field_1350);
            double len = vel.method_1033();
            double liftFactor = cos * cos * Math.min(1.0d, len / 0.4d);
            double vy = vel.field_1351 + ELYTRA_GRAVITY + (liftFactor * 0.06d);
            if (vel.field_1351 < 0.0d && horizSpeed > 0.0d) {
                vy += (-0.1d) * vel.field_1351 * liftFactor;
            }
            class_243 vAfterLift = new class_243(vel.field_1352, vy, vel.field_1350);
            class_243 align = new class_243((look.field_1352 * LOOK_PUSH) + (((look.field_1352 * ALIGN_D) - vAfterLift.field_1352) * ALIGN_E), (look.field_1351 * LOOK_PUSH) + (((look.field_1351 * ALIGN_D) - vAfterLift.field_1351) * ALIGN_E), (look.field_1350 * LOOK_PUSH) + (((look.field_1350 * ALIGN_D) - vAfterLift.field_1350) * ALIGN_E));
            class_243 vAligned = vAfterLift.method_1019(align);
            vel = new class_243(vAligned.field_1352 * DRAG_XZ_FLY, vAligned.field_1351 * DRAG_Y_FLY, vAligned.field_1350 * DRAG_XZ_FLY);
        }
        return pos;
    }

    private List<class_2338> buildAttackAnglePattern(class_2338 centerPos, class_243 futurePos) {
        class_243 playerCenter = this.mc.field_1724.method_19538().method_1031(0.0d, ((double) this.mc.field_1724.method_17682()) / 2.0d, 0.0d);
        class_243 toAttacker = futurePos.method_1020(playerCenter);
        double horizLen = Math.hypot(toAttacker.field_1352, toAttacker.field_1350);
        double pitchDeg = Math.toDegrees(Math.atan2(Math.abs(toAttacker.field_1351), Math.max(horizLen, 1.0E-4d)));
        List<class_2338> p = new ArrayList<>(7);
        p.add(centerPos);
        if (pitchDeg >= this.steepAngleThreshold.get().doubleValue()) {
            p.add(centerPos.method_10084());
            p.add(centerPos.method_10095());
            p.add(centerPos.method_10072());
            p.add(centerPos.method_10078());
            p.add(centerPos.method_10067());
        } else {
            class_2350 primary = dominantHorizontal(toAttacker.field_1352, toAttacker.field_1350);
            class_2350 secondary = secondaryHorizontal(toAttacker.field_1352, toAttacker.field_1350, primary);
            p.add(centerPos.method_10084());
            class_2338 side = centerPos.method_10093(primary);
            p.add(side);
            p.add(side.method_10084());
            if (secondary != null && secondary != primary) {
                p.add(centerPos.method_10093(secondary));
            }
        }
        return p;
    }

    private class_2350 dominantHorizontal(double dx, double dz) {
        return Math.abs(dx) >= Math.abs(dz) ? dx >= 0.0d ? class_2350.field_11034 : class_2350.field_11039 : dz >= 0.0d ? class_2350.field_11035 : class_2350.field_11043;
    }

    private class_2350 secondaryHorizontal(double dx, double dz, class_2350 primary) {
        double ax = Math.abs(dx);
        double az = Math.abs(dz);
        double ratio = Math.min(ax, az) / Math.max(Math.max(ax, az), 1.0E-4d);
        if (ratio < 0.45d) {
            return null;
        }
        return (primary == class_2350.field_11034 || primary == class_2350.field_11039) ? dz >= 0.0d ? class_2350.field_11035 : class_2350.field_11043 : dx >= 0.0d ? class_2350.field_11034 : class_2350.field_11039;
    }

    private List<class_2338> orderForCascade(List<class_2338> pattern) {
        List<class_2338> remaining = new ArrayList<>(pattern);
        List<class_2338> ordered = new ArrayList<>(pattern.size());
        Set<class_2338> planned = new HashSet<>();
        while (!remaining.isEmpty()) {
            class_2338 best = null;
            int bestScore = -1;
            for (class_2338 pos : remaining) {
                int score = supportScore(pos, planned);
                if (score > bestScore) {
                    bestScore = score;
                    best = pos;
                }
            }
            ordered.add(best);
            planned.add(best);
            remaining.remove(best);
        }
        return ordered;
    }

    private int supportScore(class_2338 pos, Set<class_2338> planned) {
        int score = 0;
        for (class_2350 d : class_2350.values()) {
            class_2338 n = pos.method_10093(d);
            if (!this.mc.field_1687.method_8320(n).method_26215()) {
                score += 3;
            } else if (planned.contains(n)) {
                score += 2;
            }
        }
        return score;
    }

    private class_243 simulateBoostedElytraFuturePos(class_1657 player, int ticks) {
        class_243 pos = player.method_19538();
        class_243 vel = player.method_18798();
        for (int i = 0; i < ticks; i++) {
            pos = pos.method_1019(vel);
            double vy = vel.field_1351;
            if (vy < TERMINAL_VELOCITY) {
                vy = -3.92d;
            }
            vel = new class_243(vel.field_1352 * 0.991d, vy, vel.field_1350 * 0.991d);
        }
        return pos;
    }
}
