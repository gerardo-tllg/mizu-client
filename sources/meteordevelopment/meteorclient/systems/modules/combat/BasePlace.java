package meteordevelopment.meteorclient.systems.modules.combat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BlockListSetting;
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
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.autocrystal.AutoCrystal;
import meteordevelopment.meteorclient.systems.modules.player.SilentMine;
import meteordevelopment.meteorclient.utils.entity.DamageUtils;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1657;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2626;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/BasePlace.class */
public class BasePlace extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgPlace;
    private final SettingGroup sgDamage;
    private final SettingGroup sgRender;
    private final Setting<List<class_2248>> blocks;
    private final Setting<Integer> targetRange;
    private final Setting<Double> placeRange;
    private final Setting<SortPriority> priority;
    private final Setting<Boolean> pauseEat;
    private final Setting<Boolean> smartPlace;
    private final Setting<Boolean> antiSurroundBreak;
    private final Setting<Double> placeDelay;
    private final Setting<Integer> placesPerTick;
    private final Setting<Double> minPlace;
    private final Setting<Double> maxSelfDamage;
    private final Setting<Double> minDamageImprovement;
    private final Setting<Boolean> render;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    private class_1657 target;
    private class_2338 bestPos;
    private Map<class_2338, Double> possiblePlacements;
    private long lastPlacedTime;
    private double maxCurrentCrystalDamage;
    private int placeCooldown;

    public BasePlace() {
        super(Categories.Combat, "base-place", "Places blocks next to enemies to allow for crystal placement.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgPlace = this.settings.createGroup("Place");
        this.sgDamage = this.settings.createGroup("Damage");
        this.sgRender = this.settings.createGroup("Render");
        this.blocks = this.sgGeneral.add(new BlockListSetting.Builder().name("whitelist").description("Which blocks to use.").defaultValue(class_2246.field_10540).build());
        this.targetRange = this.sgGeneral.add(new IntSetting.Builder().name("target-range").description("The range players can be targeted.").defaultValue(4).build());
        this.placeRange = this.sgGeneral.add(new DoubleSetting.Builder().name("place-range").description("Maximum range for obsidian placement.").defaultValue(4.5d).min(0.0d).sliderMax(6.0d).build());
        this.priority = this.sgGeneral.add(new EnumSetting.Builder().name("target-priority").description("How to select the player to target.").defaultValue(SortPriority.LowestHealth).build());
        this.pauseEat = this.sgGeneral.add(new BoolSetting.Builder().name("pause-eat").description("Pauses while eating.").defaultValue(true).build());
        this.smartPlace = this.sgGeneral.add(new BoolSetting.Builder().name("smart-place").description("Only places obsidian when necessary for crystal damage.").defaultValue(true).build());
        this.antiSurroundBreak = this.sgGeneral.add(new BoolSetting.Builder().name("anti-surround-break").description("Places obsidian to prevent surround breaking.").defaultValue(true).build());
        this.placeDelay = this.sgPlace.add(new DoubleSetting.Builder().name("place-delay").description("Delay between obsidian placements in seconds.").defaultValue(0.1d).min(0.0d).sliderMax(1.0d).build());
        this.placesPerTick = this.sgPlace.add(new IntSetting.Builder().name("places-per-tick").description("Maximum obsidian placements per tick.").defaultValue(1).min(1).max(10).build());
        this.minPlace = this.sgDamage.add(new DoubleSetting.Builder().name("min-place-damage").description("Minimum enemy damage to place obsidian.").defaultValue(8.0d).min(0.0d).sliderRange(0.0d, 20.0d).build());
        this.maxSelfDamage = this.sgDamage.add(new DoubleSetting.Builder().name("max-self-damage").description("Maximum self damage from potential crystal placements.").defaultValue(4.0d).min(0.0d).sliderRange(0.0d, 20.0d).build());
        this.minDamageImprovement = this.sgDamage.add(new DoubleSetting.Builder().name("min-damage-improvement").description("Minimum damage improvement to trigger obsidian placement.").defaultValue(2.0d).min(0.0d).sliderRange(0.0d, 10.0d).build());
        this.render = this.sgRender.add(new BoolSetting.Builder().name("render").description("Renders an overlay where blocks will be placed.").defaultValue(true).build());
        this.shapeMode = this.sgRender.add(new EnumSetting.Builder().name("shape-mode").description("How the shapes are rendered.").defaultValue(ShapeMode.Both).build());
        this.sideColor = this.sgRender.add(new ColorSetting.Builder().name("side-color").description("The side color of the target block rendering.").defaultValue(new SettingColor(Opcode.MULTIANEWARRAY, Opcode.L2F, 232, 10)).build());
        this.lineColor = this.sgRender.add(new ColorSetting.Builder().name("line-color").description("The line color of the target block rendering.").defaultValue(new SettingColor(Opcode.MULTIANEWARRAY, Opcode.L2F, 232)).build());
        this.possiblePlacements = new HashMap();
        this.maxCurrentCrystalDamage = 0.0d;
        this.placeCooldown = 0;
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        this.target = null;
        this.bestPos = null;
        this.possiblePlacements.clear();
        this.lastPlacedTime = 0L;
        this.maxCurrentCrystalDamage = 0.0d;
        this.placeCooldown = 0;
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onDeactivate() {
        this.possiblePlacements.clear();
    }

    @EventHandler
    private void onBlockUpdate(PacketEvent.Receive event) {
        class_2626 class_2626Var = event.packet;
        if (class_2626Var instanceof class_2626) {
            class_2626 packet = class_2626Var;
            if (packet.method_11308().method_26215()) {
                class_2338 pos = packet.method_11309();
                if (this.possiblePlacements.containsKey(pos)) {
                    this.possiblePlacements.remove(pos);
                }
                if (this.target != null) {
                    class_2338 targetPos = this.target.method_24515();
                    if (pos.equals(targetPos.method_10095()) || pos.equals(targetPos.method_10072()) || pos.equals(targetPos.method_10078()) || pos.equals(targetPos.method_10067())) {
                        this.maxCurrentCrystalDamage = 0.0d;
                    }
                }
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        class_1792 useItem;
        if (this.placeCooldown > 0) {
            this.placeCooldown--;
            return;
        }
        if (this.target == null || TargetUtils.isBadTarget(this.target, this.targetRange.get().intValue())) {
            this.target = TargetUtils.getPlayerTarget(this.targetRange.get().intValue(), this.priority.get());
            if (TargetUtils.isBadTarget(this.target, this.targetRange.get().intValue())) {
                this.bestPos = null;
                return;
            }
        }
        updateCurrentMaxDamage();
        calculateOptimalPlacements();
        this.bestPos = findBestPlacement();
        if (this.bestPos == null) {
            return;
        }
        if (this.smartPlace.get().booleanValue() && this.maxCurrentCrystalDamage >= this.minPlace.get().doubleValue()) {
            double bestPosDamage = this.possiblePlacements.getOrDefault(this.bestPos, Double.valueOf(0.0d)).doubleValue();
            if (bestPosDamage - this.maxCurrentCrystalDamage < this.minDamageImprovement.get().doubleValue()) {
                return;
            }
        }
        if (System.currentTimeMillis() - this.lastPlacedTime >= this.placeDelay.get().doubleValue() * 1000.0d && (useItem = findUseItem()) != null) {
            if (this.pauseEat.get().booleanValue() && this.mc.field_1724.method_6115()) {
                return;
            }
            SilentMine silentMine = (SilentMine) Modules.get().get(SilentMine.class);
            if (silentMine != null && silentMine.isActive()) {
                if (silentMine.getDelayedDestroyBlockPos() == null || !this.bestPos.equals(silentMine.getDelayedDestroyBlockPos())) {
                    if (silentMine.getRebreakBlockPos() != null && this.bestPos.equals(silentMine.getRebreakBlockPos())) {
                        return;
                    }
                } else {
                    return;
                }
            }
            class_243 centerPos = this.bestPos.method_46558();
            class_238 boundingBox = new class_238(centerPos.method_1023(0.5d, 0.5d, 0.5d), centerPos.method_1031(0.5d, 0.5d, 0.5d));
            MeteorClient.ROTATION.requestRotation(centerPos, 11.0d);
            if (!MeteorClient.ROTATION.lookingAt(boundingBox) && RotationManager.lastGround) {
                MeteorClient.ROTATION.snapAt(centerPos);
            }
            if (MeteorClient.ROTATION.lookingAt(boundingBox) && MeteorClient.BLOCK.beginPlacement(this.bestPos, this.mc.field_1687.method_8320(this.bestPos), useItem)) {
                MeteorClient.BLOCK.placeBlock(useItem, this.bestPos, this.mc.field_1687.method_8320(this.bestPos));
                MeteorClient.BLOCK.endPlacement();
                this.lastPlacedTime = System.currentTimeMillis();
                this.placeCooldown = 1;
            }
        }
    }

    private void updateCurrentMaxDamage() {
        class_2248 block;
        this.maxCurrentCrystalDamage = 0.0d;
        if (this.target == null || this.mc.field_1687 == null) {
            return;
        }
        this.target.method_24515();
        class_2338 playerPos = this.mc.field_1724.method_24515();
        Set<class_2338> validCrystalPositions = new HashSet<>();
        AutoCrystal autoCrystal = (AutoCrystal) Modules.get().get(AutoCrystal.class);
        double crystalPlaceRange = autoCrystal != null ? autoCrystal.placeRange.get().doubleValue() : 4.0d;
        for (int x = -8; x <= 8; x++) {
            for (int y = (-8) / 2; y <= 8 / 2; y++) {
                for (int z = -8; z <= 8; z++) {
                    class_2338 checkPos = playerPos.method_10069(x, y, z);
                    if (this.mc.field_1724.method_33571().method_1022(checkPos.method_10084().method_46558()) <= crystalPlaceRange && (((block = this.mc.field_1687.method_8320(checkPos).method_26204()) == class_2246.field_10540 || block == class_2246.field_9987) && this.mc.field_1687.method_8320(checkPos.method_10084()).method_26215() && this.mc.field_1687.method_8320(checkPos.method_10086(2)).method_26215())) {
                        validCrystalPositions.add(checkPos);
                    }
                }
            }
        }
        for (class_2338 pos : validCrystalPositions) {
            class_243 crystalPos = new class_243(((double) pos.method_10263()) + 0.5d, pos.method_10264() + 1, ((double) pos.method_10260()) + 0.5d);
            double targetDamage = DamageUtils.crystalDamage(this.target, crystalPos);
            double selfDamage = DamageUtils.crystalDamage(this.mc.field_1724, crystalPos);
            if (selfDamage <= this.maxSelfDamage.get().doubleValue() && targetDamage > this.maxCurrentCrystalDamage) {
                this.maxCurrentCrystalDamage = targetDamage;
            }
        }
    }

    private void calculateOptimalPlacements() {
        this.possiblePlacements.clear();
        if (this.target == null) {
            return;
        }
        class_2338 targetPos = this.target.method_24515();
        class_2338 playerPos = this.mc.field_1724.method_24515();
        Set<class_2338> candidatePositions = new HashSet<>();
        AutoCrystal autoCrystal = (AutoCrystal) Modules.get().get(AutoCrystal.class);
        double crystalPlaceRange = autoCrystal != null ? autoCrystal.placeRange.get().doubleValue() : 4.0d;
        int horizontalRange = (int) Math.ceil(this.placeRange.get().doubleValue());
        for (int x = -horizontalRange; x <= horizontalRange; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -horizontalRange; z <= horizontalRange; z++) {
                    class_2338 checkPos = playerPos.method_10069(x, y, z);
                    if (this.mc.field_1724.method_33571().method_1022(checkPos.method_46558()) <= this.placeRange.get().doubleValue() && this.mc.field_1687.method_8320(checkPos).method_26215() && this.mc.field_1687.method_8320(checkPos.method_10084()).method_26215()) {
                        double crystalDistToPlayer = this.mc.field_1724.method_33571().method_1022(checkPos.method_10084().method_46558());
                        if (crystalDistToPlayer <= crystalPlaceRange) {
                            candidatePositions.add(checkPos);
                        }
                    }
                }
            }
        }
        for (class_2350 direction : class_2350.values()) {
            if (direction != class_2350.field_11036 && direction != class_2350.field_11033) {
                class_2338 adjacentPos = targetPos.method_10093(direction);
                if (this.mc.field_1687.method_8320(adjacentPos).method_26215() && this.mc.field_1687.method_8320(adjacentPos.method_10084()).method_26215() && this.mc.field_1724.method_33571().method_1022(adjacentPos.method_46558()) <= this.placeRange.get().doubleValue()) {
                    candidatePositions.add(adjacentPos);
                }
            }
        }
        class_2338[] diagonals = {targetPos.method_10069(1, 0, 1), targetPos.method_10069(1, 0, -1), targetPos.method_10069(-1, 0, 1), targetPos.method_10069(-1, 0, -1)};
        for (class_2338 pos : diagonals) {
            if (this.mc.field_1687.method_8320(pos).method_26215() && this.mc.field_1687.method_8320(pos.method_10084()).method_26215() && this.mc.field_1724.method_33571().method_1022(pos.method_46558()) <= this.placeRange.get().doubleValue()) {
                candidatePositions.add(pos);
            }
        }
        for (class_2350 direction2 : class_2350.values()) {
            if (direction2 != class_2350.field_11036 && direction2 != class_2350.field_11033) {
                class_2338 belowAdjacent = targetPos.method_10074().method_10093(direction2);
                if (this.mc.field_1687.method_8320(belowAdjacent).method_26215() && this.mc.field_1687.method_8320(belowAdjacent.method_10084()).method_26215() && this.mc.field_1687.method_8320(belowAdjacent.method_10086(2)).method_26215() && this.mc.field_1724.method_33571().method_1022(belowAdjacent.method_46558()) <= this.placeRange.get().doubleValue()) {
                    candidatePositions.add(belowAdjacent);
                }
            }
        }
        for (class_2338 pos2 : candidatePositions) {
            if (!EntityUtils.intersectsWithEntity(new class_238(pos2), entity -> {
                return !entity.method_7325() && entity.method_5829().method_994(new class_238(pos2));
            })) {
                class_243 crystalPos = new class_243(((double) pos2.method_10263()) + 0.5d, pos2.method_10264() + 1, ((double) pos2.method_10260()) + 0.5d);
                double targetDamage = DamageUtils.crystalDamage(this.target, crystalPos);
                double selfDamage = DamageUtils.crystalDamage(this.mc.field_1724, crystalPos);
                boolean isValidCrystalPos = true;
                if (autoCrystal != null) {
                    double crystalDistToPlayer2 = this.mc.field_1724.method_33571().method_1022(crystalPos);
                    if (crystalDistToPlayer2 > autoCrystal.placeRange.get().doubleValue()) {
                        isValidCrystalPos = false;
                    }
                }
                if (isValidCrystalPos && selfDamage <= this.maxSelfDamage.get().doubleValue() && targetDamage >= this.minPlace.get().doubleValue()) {
                    this.possiblePlacements.put(pos2, Double.valueOf(targetDamage));
                }
            }
        }
        if (this.antiSurroundBreak.get().booleanValue()) {
            for (class_2350 direction3 : class_2350.values()) {
                if (direction3 != class_2350.field_11036 && direction3 != class_2350.field_11033) {
                    class_2338 surroundPos = targetPos.method_10093(direction3);
                    class_2248 surroundBlock = this.mc.field_1687.method_8320(surroundPos).method_26204();
                    if (surroundBlock == class_2246.field_10540) {
                        SilentMine silentMine = (SilentMine) Modules.get().get(SilentMine.class);
                        boolean isBeingMined = silentMine != null && silentMine.isActive() && (surroundPos.equals(silentMine.getDelayedDestroyBlockPos()) || surroundPos.equals(silentMine.getRebreakBlockPos()));
                        if (isBeingMined) {
                            for (class_2350 dir2 : class_2350.values()) {
                                if (dir2 != class_2350.field_11036 && dir2 != class_2350.field_11033 && dir2 != direction3.method_10153()) {
                                    class_2338 supportPos = surroundPos.method_10093(dir2);
                                    if (this.mc.field_1687.method_8320(supportPos).method_26215() && this.mc.field_1687.method_8320(supportPos.method_10084()).method_26215() && !EntityUtils.intersectsWithEntity(new class_238(supportPos), entity2 -> {
                                        return !entity2.method_7325() && entity2.method_5829().method_994(new class_238(supportPos));
                                    })) {
                                        class_243 crystalPos2 = new class_243(((double) supportPos.method_10263()) + 0.5d, supportPos.method_10264() + 1, ((double) supportPos.method_10260()) + 0.5d);
                                        double targetDamage2 = DamageUtils.crystalDamage(this.target, crystalPos2);
                                        double selfDamage2 = DamageUtils.crystalDamage(this.mc.field_1724, crystalPos2);
                                        if (selfDamage2 <= this.maxSelfDamage.get().doubleValue()) {
                                            this.possiblePlacements.put(supportPos, Double.valueOf(Math.max(targetDamage2, this.minPlace.get().doubleValue())));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private class_2338 findBestPlacement() {
        if (this.possiblePlacements.isEmpty()) {
            return null;
        }
        class_2338 bestPos = null;
        double bestDamage = 0.0d;
        for (Map.Entry<class_2338, Double> entry : this.possiblePlacements.entrySet()) {
            if (entry.getValue().doubleValue() > bestDamage) {
                class_2338 pos = entry.getKey();
                if (this.mc.field_1687.method_8320(pos).method_26215() && !EntityUtils.intersectsWithEntity(new class_238(pos), entity -> {
                    return !entity.method_7325() && entity.method_5829().method_994(new class_238(pos));
                })) {
                    bestDamage = entry.getValue().doubleValue();
                    bestPos = pos;
                }
            }
        }
        return bestPos;
    }

    private class_1792 findUseItem() {
        FindItemResult result = InvUtils.findInHotbar((Predicate<class_1799>) itemStack -> {
            for (class_2248 block : this.blocks.get()) {
                if (block.method_8389() == itemStack.method_7909()) {
                    return true;
                }
            }
            return false;
        });
        if (result.found()) {
            return this.mc.field_1724.method_31548().method_5438(result.slot()).method_7909();
        }
        return null;
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (!this.render.get().booleanValue() || this.bestPos == null) {
            return;
        }
        event.renderer.box(this.bestPos, this.sideColor.get(), this.lineColor.get(), this.shapeMode.get(), 0);
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public String getInfoString() {
        return EntityUtils.getName(this.target);
    }
}
