package meteordevelopment.meteorclient.systems.modules.combat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.MeteorClient;
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
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.managers.RotationManager;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.player.SilentMine;
import meteordevelopment.meteorclient.systems.modules.render.BreakIndicators;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1297;
import net.minecraft.class_1511;
import net.minecraft.class_1657;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_2680;
import net.minecraft.class_2824;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/Surround.class */
public class Surround extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgRender;
    private final Setting<Boolean> pauseEat;
    private final Setting<Boolean> protect;
    private final Setting<Boolean> protectOverrideBlockCooldown;
    private final Setting<Boolean> protectCrystalPlacements;
    private final Setting<Boolean> selfTrapEnabled;
    private final Setting<SelfTrapMode> autoSelfTrapMode;
    private final Setting<Boolean> selfTrapHead;
    private final Setting<Boolean> extendEnabled;
    private final Setting<ExtendMode> extendMode;
    private final Setting<CrawlExtendMode> crawlExtendMode;
    private final Setting<Boolean> enemySilentMineReact;
    private final Setting<Boolean> render;
    private final Setting<Double> fadeTime;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    private final Setting<Boolean> avoidHelpingOpponents;
    private final Setting<SettingColor> skippedSideColor;
    private final Setting<SettingColor> skippedLineColor;
    private final Setting<Boolean> debugProtectShape;
    private final Setting<Double> placeFailCooldown;
    private final Setting<Integer> maxPlacesPerTick;
    private List<class_2338> placePoses;
    private Map<class_2338, Long> renderLastPlacedBlock;
    private Map<class_2338, Long> renderLastSkippedBlock;
    private Map<class_2338, Long> failedPositions;
    private long lastTimeOfCrystalNearHead;
    private long lastTimeOfCrystalNearFeet;
    private long lastTimeOfExtendCrystal;
    private long lastTimeOfCrawlExtendCrystal;
    private long lastAttackTime;
    private class_2338 lastExtendCrystalOffset;
    private class_2338 lastCrawlExtendCrystalOffset;
    private boolean currentFootBlockThreatened;

    public Surround() {
        super(Categories.Combat, "surround", "Surrounds you in blocks to prevent massive crystal damage.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgRender = this.settings.createGroup("Render");
        this.pauseEat = this.sgGeneral.add(new BoolSetting.Builder().name("pause-eat").description("Pauses while eating.").defaultValue(true).build());
        this.protect = this.sgGeneral.add(new BoolSetting.Builder().name("protect").description("Attempts to break crystals around surround positions to prevent surround break.").defaultValue(true).build());
        SettingGroup var10001 = this.sgGeneral;
        BoolSetting.Builder var10002 = new BoolSetting.Builder().name("protect-override-block-cooldown").description("Overrides the cooldown for block placements when you break a crystal. May result in more packet kicks");
        Objects.requireNonNull(this.protect);
        Setting<Boolean> setting = this.protect;
        Objects.requireNonNull(setting);
        this.protectOverrideBlockCooldown = var10001.add(var10002.visible(setting::get).defaultValue(true).build());
        SettingGroup var100012 = this.sgGeneral;
        BoolSetting.Builder var100022 = new BoolSetting.Builder().name("protect-instant-place").description("Attempt to immediately place obsidian at the crystal position right after breaking the crystal.");
        Objects.requireNonNull(this.protect);
        Setting<Boolean> setting2 = this.protect;
        Objects.requireNonNull(setting2);
        this.protectCrystalPlacements = var100012.add(var100022.visible(setting2::get).defaultValue(true).build());
        this.selfTrapEnabled = this.sgGeneral.add(new BoolSetting.Builder().name("self-trap").description("Enables self trap").defaultValue(true).build());
        SettingGroup var100013 = this.sgGeneral;
        EnumSetting.Builder var1 = new EnumSetting.Builder().name("self-trap-mode").description("When to build double high").defaultValue(SelfTrapMode.Smart);
        Objects.requireNonNull(this.selfTrapEnabled);
        Setting<Boolean> setting3 = this.selfTrapEnabled;
        Objects.requireNonNull(setting3);
        this.autoSelfTrapMode = var100013.add(var1.visible(setting3::get).build());
        SettingGroup var100014 = this.sgGeneral;
        BoolSetting.Builder var100023 = new BoolSetting.Builder().name("self-trap-head").description("Places a block above your head to prevent you from velo failing upwards");
        Objects.requireNonNull(this.selfTrapEnabled);
        Setting<Boolean> setting4 = this.selfTrapEnabled;
        Objects.requireNonNull(setting4);
        this.selfTrapHead = var100014.add(var100023.visible(setting4::get).defaultValue(true).build());
        this.extendEnabled = this.sgGeneral.add(new BoolSetting.Builder().name("extend").description("Enables extend placing").defaultValue(true).build());
        SettingGroup var100015 = this.sgGeneral;
        EnumSetting.Builder var12 = new EnumSetting.Builder().name("extend-mode").description("When to place extend blocks").defaultValue(ExtendMode.Smart);
        Objects.requireNonNull(this.extendEnabled);
        Setting<Boolean> setting5 = this.extendEnabled;
        Objects.requireNonNull(setting5);
        this.extendMode = var100015.add(var12.visible(setting5::get).build());
        this.crawlExtendMode = this.sgGeneral.add(new EnumSetting.Builder().name("crawl-extend-mode").description("additional protection layer when crawling.").defaultValue(CrawlExtendMode.Smart).build());
        this.enemySilentMineReact = this.sgGeneral.add(new BoolSetting.Builder().name("enemy-silentmine-react").description("React to enemy breaking (silent mining) blocks near you using BreakIndicators.").defaultValue(true).build());
        this.render = this.sgRender.add(new BoolSetting.Builder().name("render").description("Renders a block overlay when you try to place obsidian.").defaultValue(true).build());
        this.fadeTime = this.sgRender.add(new DoubleSetting.Builder().name("fadeTime").description("How many seconds it takes to fade.").defaultValue(0.2d).min(0.0d).sliderMax(1.0d).build());
        this.shapeMode = this.sgRender.add(new EnumSetting.Builder().name("shape-mode").description("How the shapes are rendered.").defaultValue(ShapeMode.Both).build());
        this.sideColor = this.sgRender.add(new ColorSetting.Builder().name("side-color").description("The side color.").defaultValue(new SettingColor(85, 0, 255, 40)).visible(() -> {
            return this.render.get().booleanValue() && this.shapeMode.get() != ShapeMode.Lines;
        }).build());
        this.lineColor = this.sgRender.add(new ColorSetting.Builder().name("line-color").description("The line color.").defaultValue(new SettingColor(255, 255, 255, 60)).visible(() -> {
            return this.render.get().booleanValue() && this.shapeMode.get() != ShapeMode.Sides;
        }).build());
        this.avoidHelpingOpponents = this.sgGeneral.add(new BoolSetting.Builder().name("avoid-helping-opponents").description("Avoid placing blocks that directly help opponents surround themselves (heuristic).").defaultValue(true).build());
        this.skippedSideColor = this.sgRender.add(new ColorSetting.Builder().name("skipped-side-color").description("Side color for skipped blocks.").defaultValue(new SettingColor(255, 0, 0, 40)).visible(() -> {
            return this.render.get().booleanValue() && this.shapeMode.get() != ShapeMode.Lines;
        }).build());
        this.skippedLineColor = this.sgRender.add(new ColorSetting.Builder().name("skipped-line-color").description("Line color for skipped blocks.").defaultValue(new SettingColor(255, 0, 0, Opcode.ISHL)).visible(() -> {
            return this.render.get().booleanValue() && this.shapeMode.get() != ShapeMode.Sides;
        }).build());
        this.debugProtectShape = this.sgRender.add(new BoolSetting.Builder().name("debug-protect-shape").description("Renders the crystal protect shape positions.").defaultValue(false).build());
        this.placeFailCooldown = this.sgGeneral.add(new DoubleSetting.Builder().name("place-fail-cooldown").description("Seconds to wait before retrying a failed placement position.").defaultValue(0.5d).min(0.0d).sliderMax(2.0d).build());
        this.maxPlacesPerTick = this.sgGeneral.add(new IntSetting.Builder().name("max-places-per-tick").description("Maximum block placements to attempt per tick. Lower = less interference with eating/crystals.").defaultValue(4).min(1).sliderRange(1, 8).build());
        this.placePoses = new ArrayList();
        this.renderLastPlacedBlock = new HashMap();
        this.renderLastSkippedBlock = new HashMap();
        this.failedPositions = new HashMap();
        this.lastTimeOfCrystalNearHead = 0L;
        this.lastTimeOfCrystalNearFeet = 0L;
        this.lastTimeOfExtendCrystal = 0L;
        this.lastTimeOfCrawlExtendCrystal = 0L;
        this.lastAttackTime = 0L;
        this.lastExtendCrystalOffset = null;
        this.lastCrawlExtendCrystalOffset = null;
        this.currentFootBlockThreatened = false;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        class_2338 crystalPos;
        int perpX1;
        int perpZ1;
        int perpX2;
        int perpZ2;
        this.placePoses.clear();
        List<class_2338> feetBlocks = new ArrayList<>();
        List<class_2338> selfTrapBlocks = new ArrayList<>();
        List<class_2338> extendBlocks = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        if (this.mc.field_1724 != null && this.mc.field_1687 != null) {
            if (!this.mc.field_1724.method_5715() && !this.mc.field_1724.method_20448()) {
                class_238 boundingBox = this.mc.field_1724.method_5829().method_1009(0.01d, 0.1d, 0.01d);
                int feetY = this.mc.field_1724.method_24515().method_10264();
                SilentMine silentMine = (SilentMine) Modules.get().get(SilentMine.class);
                int bbMinX = (int) Math.floor(boundingBox.field_1323);
                int bbMaxX = (int) Math.floor(boundingBox.field_1320);
                int playerFeetY = (int) Math.floor(boundingBox.field_1321);
                int bbMaxZ = (int) Math.floor(boundingBox.field_1324);
                boolean lowHealth = bbMinX == bbMaxX && playerFeetY == bbMaxZ;
                boolean extendThreatDetected = false;
                boolean threatFromAbove = false;
                boolean helps = false;
                if (this.selfTrapEnabled.get().booleanValue() || (this.extendEnabled.get().booleanValue() && this.extendMode.get() == ExtendMode.Smart)) {
                    class_238 headCheckBox = this.mc.field_1724.method_5829().method_1012(1.5d, 0.5d, 1.5d).method_989(0.0d, 1.0d, 0.0d);
                    if (EntityUtils.intersectsWithEntity(headCheckBox, e -> {
                        return e instanceof class_1511;
                    })) {
                        threatFromAbove = true;
                        this.lastTimeOfCrystalNearHead = currentTime;
                    }
                    class_238 feetBox = this.mc.field_1724.method_5829().method_1012(1.5d, 0.0d, 1.5d).method_989(0.0d, -1.0d, 0.0d);
                    if (EntityUtils.intersectsWithEntity(feetBox, e2 -> {
                        return e2 instanceof class_1511;
                    })) {
                        helps = true;
                        this.lastTimeOfCrystalNearFeet = currentTime;
                    }
                }
                if (this.extendMode.get() == ExtendMode.Smart && this.extendEnabled.get().booleanValue() && lowHealth) {
                    class_238 headCheckBox2 = this.mc.field_1724.method_5829().method_1012(1.5d, 0.5d, 1.5d).method_989(0.0d, 1.0d, 0.0d);
                    class_1511 closestCrystal = null;
                    double closestDistSq = Double.MAX_VALUE;
                    for (class_1297 entity : this.mc.field_1687.method_8390(class_1511.class, headCheckBox2, e3 -> {
                        return true;
                    })) {
                        double distSq = entity.method_5707(this.mc.field_1724.method_33571());
                        if (distSq < closestDistSq) {
                            closestDistSq = distSq;
                            closestCrystal = (class_1511) entity;
                        }
                    }
                    if (closestCrystal != null) {
                        this.lastExtendCrystalOffset = closestCrystal.method_24515().method_10059(this.mc.field_1724.method_24515());
                        this.lastTimeOfExtendCrystal = currentTime;
                        extendThreatDetected = true;
                    }
                }
                for (int dx = bbMinX; dx <= bbMaxX; dx++) {
                    for (int x = playerFeetY; x <= bbMaxZ; x++) {
                        class_2338 feetPos = new class_2338(dx, feetY, x);
                        for (int offsetX = -1; offsetX <= 1; offsetX++) {
                            for (int perpX12 = -1; perpX12 <= 1; perpX12++) {
                                if (Math.abs(offsetX) + Math.abs(perpX12) == 1) {
                                    class_2338 adjacentPos = feetPos.method_10069(offsetX, 0, perpX12);
                                    if (this.mc.field_1687.method_8320(adjacentPos).method_26215()) {
                                        feetBlocks.add(adjacentPos);
                                    }
                                    if (this.autoSelfTrapMode.get() != SelfTrapMode.None && this.selfTrapEnabled.get().booleanValue()) {
                                        checkSmartDefenses(selfTrapBlocks, adjacentPos, threatFromAbove, helps, currentTime);
                                    }
                                }
                            }
                        }
                        class_2338 t = new class_2338(dx, feetY - 1, x);
                        class_2680 belowFeetState = this.mc.field_1687.method_8320(t);
                        if (!t.equals(silentMine.getRebreakBlockPos()) && !t.equals(silentMine.getDelayedDestroyBlockPos()) && (belowFeetState.method_51176() || belowFeetState.method_26215())) {
                            feetBlocks.add(t);
                        }
                    }
                }
                if (this.extendEnabled.get().booleanValue() && lowHealth) {
                    boolean isNearMyFeet = this.extendMode.get() == ExtendMode.Always || (this.extendMode.get() == ExtendMode.Smart && extendThreatDetected && ((double) (currentTime - this.lastTimeOfExtendCrystal)) / 1000.0d < 1.0d);
                    if (isNearMyFeet) {
                        placeExtendBlocks(extendBlocks, this.mc.field_1724.method_24515(), this.lastExtendCrystalOffset);
                    }
                }
                if (this.selfTrapEnabled.get().booleanValue() && this.selfTrapHead.get().booleanValue() && !this.mc.field_1724.method_5715()) {
                    selfTrapBlocks.add(this.mc.field_1724.method_24515().method_10086(2));
                }
            } else {
                class_238 boundingBox2 = this.mc.field_1724.method_5829().method_1009(0.01d, 0.0d, 0.01d);
                int feetY2 = this.mc.field_1724.method_24515().method_10264();
                SilentMine silentMine2 = (SilentMine) Modules.get().get(SilentMine.class);
                int minX = (int) Math.floor(boundingBox2.field_1323);
                int bbMinX2 = (int) Math.floor(boundingBox2.field_1320);
                int bbMaxX2 = (int) Math.floor(boundingBox2.field_1321);
                int playerFeetY2 = (int) Math.floor(boundingBox2.field_1324);
                boolean amIn1x1 = minX == bbMinX2 && bbMaxX2 == playerFeetY2;
                boolean extendThreatDetected2 = false;
                boolean threatFromAbove2 = false;
                boolean helps2 = false;
                if (this.selfTrapEnabled.get().booleanValue() || (this.extendEnabled.get().booleanValue() && this.crawlExtendMode.get() == CrawlExtendMode.Smart)) {
                    class_238 headCheckBox3 = this.mc.field_1724.method_5829().method_1012(1.5d, 0.5d, 1.5d).method_989(0.0d, 0.5d, 0.0d);
                    if (EntityUtils.intersectsWithEntity(headCheckBox3, e4 -> {
                        return e4 instanceof class_1511;
                    })) {
                        threatFromAbove2 = true;
                        this.lastTimeOfCrystalNearHead = currentTime;
                    }
                    class_238 feetBox2 = this.mc.field_1724.method_5829().method_1012(1.5d, 0.0d, 1.5d).method_989(0.0d, -1.0d, 0.0d);
                    if (EntityUtils.intersectsWithEntity(feetBox2, e5 -> {
                        return e5 instanceof class_1511;
                    })) {
                        helps2 = true;
                        this.lastTimeOfCrystalNearFeet = currentTime;
                    }
                }
                if (this.crawlExtendMode.get() == CrawlExtendMode.Smart && amIn1x1) {
                    class_238 detectionBox = this.mc.field_1724.method_5829().method_1012(2.0d, 1.0d, 2.0d);
                    class_1511 closestCrystal2 = null;
                    double closestDistSq2 = Double.MAX_VALUE;
                    for (class_1297 entity2 : this.mc.field_1687.method_8390(class_1511.class, detectionBox, e6 -> {
                        return true;
                    })) {
                        double distSq2 = entity2.method_5707(this.mc.field_1724.method_19538());
                        if (distSq2 < closestDistSq2) {
                            closestDistSq2 = distSq2;
                            closestCrystal2 = (class_1511) entity2;
                        }
                    }
                    if (closestCrystal2 != null) {
                        this.lastCrawlExtendCrystalOffset = closestCrystal2.method_24515().method_10059(this.mc.field_1724.method_24515());
                        this.lastTimeOfCrawlExtendCrystal = currentTime;
                        extendThreatDetected2 = true;
                    }
                }
                for (int minX2 = minX; minX2 <= bbMinX2; minX2++) {
                    for (int maxX = bbMaxX2; maxX <= playerFeetY2; maxX++) {
                        class_2338 feetPos2 = new class_2338(minX2, feetY2, maxX);
                        for (int offsetX2 = -1; offsetX2 <= 1; offsetX2++) {
                            for (int perpX13 = -1; perpX13 <= 1; perpX13++) {
                                if (Math.abs(offsetX2) + Math.abs(perpX13) == 1) {
                                    class_2338 adjacentPos2 = feetPos2.method_10069(offsetX2, 0, perpX13);
                                    if (this.mc.field_1687.method_8320(adjacentPos2).method_26215()) {
                                        feetBlocks.add(adjacentPos2);
                                    }
                                    if (this.autoSelfTrapMode.get() != SelfTrapMode.None && this.selfTrapEnabled.get().booleanValue()) {
                                        checkSmartDefenses(selfTrapBlocks, adjacentPos2, threatFromAbove2, helps2, currentTime);
                                    }
                                }
                            }
                        }
                        class_2338 t2 = feetPos2.method_10084();
                        if (this.mc.field_1687.method_8320(t2).method_26215()) {
                            feetBlocks.add(t2);
                        }
                        class_2338 t3 = new class_2338(minX2, feetY2 - 1, maxX);
                        class_2680 belowFeetState2 = this.mc.field_1687.method_8320(t3);
                        if (!t3.equals(silentMine2.getRebreakBlockPos()) && !t3.equals(silentMine2.getDelayedDestroyBlockPos()) && (belowFeetState2.method_51176() || belowFeetState2.method_26215())) {
                            feetBlocks.add(t3);
                        }
                    }
                }
                if (this.extendEnabled.get().booleanValue() && amIn1x1) {
                    boolean lowHealth2 = this.crawlExtendMode.get() == CrawlExtendMode.Smart && extendThreatDetected2 && ((double) (currentTime - this.lastTimeOfCrawlExtendCrystal)) / 1000.0d < 1.0d;
                    if (lowHealth2) {
                        placeExtendBlocks(extendBlocks, this.mc.field_1724.method_24515(), this.lastCrawlExtendCrystalOffset);
                    }
                }
                if (this.selfTrapEnabled.get().booleanValue() && this.selfTrapHead.get().booleanValue()) {
                    selfTrapBlocks.add(this.mc.field_1724.method_24515().method_10086(1));
                }
            }
            BreakIndicators breakIndicators = (BreakIndicators) Modules.get().get(BreakIndicators.class);
            this.currentFootBlockThreatened = false;
            if (this.enemySilentMineReact.get().booleanValue() && breakIndicators != null && this.mc.field_1724 != null) {
                class_2338 myFeet = this.mc.field_1724.method_24515();
                class_238 bbFeet = this.mc.field_1724.method_5829().method_1009(0.01d, 0.1d, 0.01d);
                boolean lowHealth3 = ((int) Math.floor(bbFeet.field_1323)) == ((int) Math.floor(bbFeet.field_1320)) && ((int) Math.floor(bbFeet.field_1321)) == ((int) Math.floor(bbFeet.field_1324));
                if (lowHealth3) {
                    Map<class_2338, BreakIndicators.BlockBreak> active = breakIndicators.breakStartTimes;
                    for (BreakIndicators.BlockBreak info : active.values()) {
                        if (info.entity != null) {
                            int dx2 = info.blockPos.method_10263() - myFeet.method_10263();
                            int x2 = info.blockPos.method_10264() - myFeet.method_10264();
                            int dz = info.blockPos.method_10260() - myFeet.method_10260();
                            if (x2 == 0 && Math.abs(dx2) + Math.abs(dz) == 1) {
                                this.currentFootBlockThreatened = true;
                                class_2338 t4 = info.blockPos;
                                if (dx2 != 0) {
                                    perpX1 = dx2;
                                    perpZ1 = 1;
                                    perpX2 = dx2;
                                    perpZ2 = -1;
                                } else {
                                    perpX1 = 1;
                                    perpZ1 = dz;
                                    perpX2 = -1;
                                    perpZ2 = dz;
                                }
                                class_2338 aboveAdj = t4.method_10084();
                                class_2338 belowAdj = t4.method_10074();
                                class_2338 perp1 = myFeet.method_10069(perpX1, 0, perpZ1);
                                class_2338 perp2 = myFeet.method_10069(perpX2, 0, perpZ2);
                                class_2338 outward2 = myFeet.method_10069(dx2 * 2, 0, dz * 2);
                                class_2338[] targets = {aboveAdj, belowAdj, perp1, perp2, outward2};
                                for (class_2338 t22 : targets) {
                                    if (!t22.equals(myFeet) && this.mc.field_1687.method_8320(t22).method_26215()) {
                                        extendBlocks.add(t22);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            this.placePoses.addAll(feetBlocks);
            this.placePoses.addAll(selfTrapBlocks);
            this.placePoses.addAll(extendBlocks);
            Set<class_2338> protectPositions = getProtectPositions();
            if (!this.pauseEat.get().booleanValue() || !this.mc.field_1724.method_6115()) {
                if (this.protect.get().booleanValue()) {
                    SilentMine silentMine3 = (SilentMine) Modules.get().get(SilentMine.class);
                    for (class_2338 protectPos : protectPositions) {
                        class_238 box = new class_238(protectPos.method_10263(), protectPos.method_10264(), protectPos.method_10260(), protectPos.method_10263() + 1, protectPos.method_10264() + 1, protectPos.method_10260() + 1);
                        Predicate<class_1297> entityPredicate = entityx -> {
                            return entityx instanceof class_1511;
                        };
                        class_1297 blocking = (class_1297) this.mc.field_1687.method_8390(class_1511.class, box, entityPredicate).stream().findFirst().orElse(null);
                        if (blocking != null && System.currentTimeMillis() - this.lastAttackTime >= 50 && (crystalPos = blocking.method_24515()) != null && isInsideProtectShape(crystalPos, protectPositions)) {
                            MeteorClient.ROTATION.requestRotation(blocking.method_33571(), 11.0d);
                            if (!MeteorClient.ROTATION.lookingAt(blocking.method_5829()) && RotationManager.lastGround) {
                                MeteorClient.ROTATION.snapAt(blocking.method_33571());
                            }
                            if (MeteorClient.ROTATION.lookingAt(blocking.method_5829())) {
                                this.mc.method_1562().method_52787(class_2824.method_34206(blocking, this.mc.field_1724.method_5715()));
                                this.mc.field_1761.method_2918(this.mc.field_1724, blocking);
                                this.lastAttackTime = System.currentTimeMillis();
                                if (this.protectOverrideBlockCooldown.get().booleanValue()) {
                                    MeteorClient.BLOCK.forceResetPlaceCooldown(protectPos);
                                }
                                if (this.protectCrystalPlacements.get().booleanValue()) {
                                    ArrayList<class_2338> immediateTargets = new ArrayList();
                                    class_2338 rebreak = null;
                                    class_2338 delayed = null;
                                    if (silentMine3 != null && silentMine3.isActive()) {
                                        rebreak = silentMine3.getRebreakBlockPos();
                                        delayed = silentMine3.getDelayedDestroyBlockPos();
                                    }
                                    class_2338 crystalPos2 = blocking.method_24515();
                                    if (crystalPos2 != null && isInsideProtectShape(crystalPos2, protectPositions) && !isInsideEnemyHitboxShell(crystalPos2) && this.mc.field_1687.method_8320(crystalPos2).method_26215() && ((rebreak == null || !crystalPos2.equals(rebreak)) && (delayed == null || !crystalPos2.equals(delayed)))) {
                                        immediateTargets.add(crystalPos2);
                                    }
                                    if (!immediateTargets.isEmpty() && MeteorClient.BLOCK.beginPlacement(immediateTargets, class_1802.field_8281)) {
                                        for (class_2338 t5 : immediateTargets) {
                                            try {
                                                if (!t5.equals(rebreak) && !t5.equals(delayed) && MeteorClient.BLOCK.placeBlock(class_1802.field_8281, t5)) {
                                                    this.renderLastPlacedBlock.put(t5, Long.valueOf(System.currentTimeMillis()));
                                                }
                                            } catch (Exception e7) {
                                            }
                                        }
                                        MeteorClient.BLOCK.endPlacement();
                                    }
                                }
                            }
                        }
                    }
                }
                List<class_2338> filteredPlacePoses = new ArrayList<>();
                Set<class_2338> skippedThisTick = new HashSet<>();
                if (!this.avoidHelpingOpponents.get().booleanValue()) {
                    filteredPlacePoses.addAll(this.placePoses);
                } else {
                    Set<class_2338> playerFootBlocks = new HashSet<>();
                    int playerFeetY3 = this.mc.field_1724.method_24515().method_10264();
                    class_238 bb = this.mc.field_1724.method_5829().method_1009(0.01d, 0.1d, 0.01d);
                    int minX3 = (int) Math.floor(bb.field_1323);
                    int maxX2 = (int) Math.floor(bb.field_1320);
                    int minZ = (int) Math.floor(bb.field_1321);
                    int dx3 = (int) Math.floor(bb.field_1324);
                    boolean amIn1x12 = minX3 == maxX2 && minZ == dx3;
                    for (int x3 = minX3; x3 <= maxX2; x3++) {
                        for (int dz2 = minZ; dz2 <= dx3; dz2++) {
                            playerFootBlocks.add(new class_2338(x3, playerFeetY3, dz2));
                        }
                    }
                    boolean lowHealth4 = isLowHealth(10.0d);
                    for (class_2338 pos : this.placePoses) {
                        if (!wouldHelpOpponent(pos)) {
                            filteredPlacePoses.add(pos);
                        } else {
                            boolean isNearMyFeet2 = isNearMyPerimeter(pos, playerFootBlocks);
                            if (amIn1x12) {
                                filteredPlacePoses.add(pos);
                            } else if (lowHealth4) {
                                filteredPlacePoses.add(pos);
                            } else if (isPlayerPhased() && !this.currentFootBlockThreatened) {
                                skippedThisTick.add(pos);
                            } else if (isNearMyFeet2) {
                                filteredPlacePoses.add(pos);
                            } else {
                                skippedThisTick.add(pos);
                            }
                        }
                    }
                }
                long now = System.currentTimeMillis();
                for (class_2338 p : skippedThisTick) {
                    this.renderLastSkippedBlock.put(p, Long.valueOf(now));
                }
                long failCooldownMs = (long) (this.placeFailCooldown.get().doubleValue() * 1000.0d);
                this.failedPositions.entrySet().removeIf(e8 -> {
                    return now - ((Long) e8.getValue()).longValue() > failCooldownMs;
                });
                class_2338 playerPos = this.mc.field_1724.method_24515();
                filteredPlacePoses.sort((a, b) -> {
                    double distA = a.method_10262(playerPos);
                    double distB = b.method_10262(playerPos);
                    return Double.compare(distA, distB);
                });
                List<class_2338> readyToPlace = new ArrayList<>();
                SilentMine sm = (SilentMine) Modules.get().get(SilentMine.class);
                for (class_2338 blockPos : filteredPlacePoses) {
                    if (!blockPos.equals(sm.getRebreakBlockPos()) && !blockPos.equals(sm.getLastDelayedDestroyBlockPos()) && !this.failedPositions.containsKey(blockPos)) {
                        readyToPlace.add(blockPos);
                        if (readyToPlace.size() >= this.maxPlacesPerTick.get().intValue()) {
                            break;
                        }
                    }
                }
                if (!readyToPlace.isEmpty() && MeteorClient.BLOCK.beginPlacement(readyToPlace, class_1802.field_8281)) {
                    for (class_2338 blockPos2 : readyToPlace) {
                        if (MeteorClient.BLOCK.placeBlock(class_1802.field_8281, blockPos2)) {
                            this.renderLastPlacedBlock.put(blockPos2, Long.valueOf(currentTime));
                        } else {
                            this.failedPositions.put(blockPos2, Long.valueOf(now));
                        }
                    }
                    MeteorClient.BLOCK.endPlacement();
                }
            }
            if (this.debugProtectShape.get().booleanValue() && this.mc.field_1687 != null && this.mc.field_1724 != null) {
                long nowDbg = System.currentTimeMillis();
                for (class_2338 p2 : protectPositions) {
                    this.renderLastSkippedBlock.put(p2, Long.valueOf(nowDbg));
                }
            }
        }
    }

    private boolean isNearMyPerimeter(class_2338 candidatePos, Set<class_2338> playerBlocks) {
        for (class_2338 playerBlock : playerBlocks) {
            if (candidatePos.equals(playerBlock.method_10084()) || candidatePos.equals(playerBlock.method_10074()) || candidatePos.equals(playerBlock.method_10072()) || candidatePos.equals(playerBlock.method_10095()) || candidatePos.equals(playerBlock.method_10078()) || candidatePos.equals(playerBlock.method_10067()) || candidatePos.equals(playerBlock)) {
                return true;
            }
        }
        return false;
    }

    private void checkSmartDefenses(List<class_2338> placePoses, class_2338 adjacentPos, boolean threatFromAbove, boolean threatFromBelow, long currentTime) {
        if (this.mc.field_1687 != null && this.mc.field_1724 != null) {
            boolean shouldBuildUp = this.autoSelfTrapMode.get() == SelfTrapMode.Always || (this.autoSelfTrapMode.get() == SelfTrapMode.Smart && threatFromAbove && ((double) (currentTime - this.lastTimeOfCrystalNearHead)) / 1000.0d < 1.0d);
            if (shouldBuildUp) {
                class_2338 facePlacePos = adjacentPos.method_10084();
                if (this.mc.field_1687.method_8320(facePlacePos).method_26215()) {
                    placePoses.add(facePlacePos);
                }
            }
            boolean shouldBuildDown = this.autoSelfTrapMode.get() == SelfTrapMode.Always || (this.autoSelfTrapMode.get() == SelfTrapMode.Smart && threatFromAbove && ((double) (currentTime - this.lastTimeOfCrystalNearFeet)) / 1000.0d < 1.0d);
            if (shouldBuildDown) {
                class_2338 belowPos = adjacentPos.method_10074();
                if (this.mc.field_1687.method_8320(belowPos).method_26215()) {
                    placePoses.add(belowPos);
                }
            }
            class_2338 feetFloor = this.mc.field_1724.method_24515().method_10087(2);
            if (this.mc.field_1687.method_8320(feetFloor).method_26215()) {
                placePoses.add(feetFloor);
            }
        }
    }

    private void placeExtendBlocks(List<class_2338> placePoses, class_2338 feetPos, class_2338 crystalOffset) {
        class_2338 diagonal1;
        class_2338 diagonal2;
        class_2338 straightBlock;
        if (crystalOffset != null && this.mc.field_1687 != null) {
            int normDx = Integer.signum(crystalOffset.method_10263());
            int normDz = Integer.signum(crystalOffset.method_10260());
            boolean isDiagonal = (normDx == 0 || normDz == 0) ? false : true;
            boolean isCardinal = (normDx != 0) ^ (normDz != 0);
            if (isDiagonal) {
                class_2338 diagonal12 = feetPos.method_10069(normDx, 0, normDz);
                if (this.mc.field_1687.method_8320(diagonal12).method_26215()) {
                    placePoses.add(diagonal12);
                }
                class_2338 diagonal22 = feetPos.method_10069(normDx * 2, 0, 0);
                if (this.mc.field_1687.method_8320(diagonal22).method_26215()) {
                    placePoses.add(diagonal22);
                }
                class_2338 straightBlock2 = feetPos.method_10069(0, 0, normDz * 2);
                if (this.mc.field_1687.method_8320(straightBlock2).method_26215()) {
                    placePoses.add(straightBlock2);
                    return;
                }
                return;
            }
            if (isCardinal) {
                if (normDx != 0) {
                    diagonal1 = feetPos.method_10069(normDx, 0, 1);
                    diagonal2 = feetPos.method_10069(normDx, 0, -1);
                    straightBlock = feetPos.method_10069(normDx * 2, 0, 0);
                } else {
                    diagonal1 = feetPos.method_10069(1, 0, normDz);
                    diagonal2 = feetPos.method_10069(-1, 0, normDz);
                    straightBlock = feetPos.method_10069(0, 0, normDz * 2);
                }
                if (this.mc.field_1687.method_8320(diagonal1).method_26215()) {
                    placePoses.add(diagonal1);
                }
                if (this.mc.field_1687.method_8320(diagonal2).method_26215()) {
                    placePoses.add(diagonal2);
                }
                if (this.mc.field_1687.method_8320(straightBlock).method_26215()) {
                    placePoses.add(straightBlock);
                }
            }
        }
    }

    private boolean isCrystalBlock(class_2338 blockPos) {
        class_2680 blockState = this.mc.field_1687.method_8320(blockPos);
        return blockState.method_27852(class_2246.field_10540) || blockState.method_27852(class_2246.field_9987);
    }

    private boolean wouldHelpOpponent(class_2338 candidatePos) {
        if (this.avoidHelpingOpponents.get().booleanValue() && this.mc.field_1687 != null && this.mc.field_1724 != null) {
            for (class_1657 class_1657Var : this.mc.field_1687.method_18456()) {
                if (class_1657Var != this.mc.field_1724 && (class_1657Var instanceof class_1657)) {
                    class_1657 p = class_1657Var;
                    if (Friends.get().isFriend(p)) {
                        continue;
                    } else {
                        class_2338 oppFeet = p.method_24515();
                        double horiz = Math.sqrt(Math.pow(oppFeet.method_10263() - this.mc.field_1724.method_24515().method_10263(), 2.0d) + Math.pow(oppFeet.method_10260() - this.mc.field_1724.method_24515().method_10260(), 2.0d));
                        if (horiz <= 5.0d) {
                            class_238 pbb = p.method_5829().method_1009(0.01d, 0.1d, 0.01d);
                            int minX = (int) Math.floor(pbb.field_1323);
                            int maxX = (int) Math.floor(pbb.field_1320);
                            int minZ = (int) Math.floor(pbb.field_1321);
                            int maxZ = (int) Math.floor(pbb.field_1324);
                            int sizeX = (maxX - minX) + 1;
                            int sizeZ = (maxZ - minZ) + 1;
                            boolean oppIn1x1 = sizeX == 1 && sizeZ == 1;
                            boolean oppIn2x1 = (sizeX == 2 && sizeZ == 1) || (sizeX == 1 && sizeZ == 2);
                            boolean oppIn2x2 = sizeX == 2 && sizeZ == 2;
                            if (isEntityPhased(p)) {
                                continue;
                            } else {
                                if (oppIn1x1 && (List.of(oppFeet.method_10072(), oppFeet.method_10095(), oppFeet.method_10078(), oppFeet.method_10067()).contains(candidatePos) || candidatePos.equals(oppFeet.method_10074()) || candidatePos.equals(oppFeet))) {
                                    return true;
                                }
                                if (oppIn2x1) {
                                    ArrayList checkSet = new ArrayList();
                                    for (int z = minX; z < minX + sizeX; z++) {
                                        checkSet.add(new class_2338(z, oppFeet.method_10264(), minZ - 1));
                                        checkSet.add(new class_2338(z, oppFeet.method_10264(), minZ + sizeZ));
                                    }
                                    for (int z2 = minZ; z2 < minZ + sizeZ; z2++) {
                                        checkSet.add(new class_2338(minX - 1, oppFeet.method_10264(), z2));
                                        checkSet.add(new class_2338(minX + sizeX, oppFeet.method_10264(), z2));
                                    }
                                    if (checkSet.contains(candidatePos)) {
                                        return true;
                                    }
                                }
                                if (oppIn2x2) {
                                    ArrayList checkSet2 = new ArrayList();
                                    for (int z3 = minX; z3 < minX + 2; z3++) {
                                        checkSet2.add(new class_2338(z3, oppFeet.method_10264(), minZ - 1));
                                        checkSet2.add(new class_2338(z3, oppFeet.method_10264(), minZ + 2));
                                    }
                                    for (int z4 = minZ; z4 < minZ + 2; z4++) {
                                        checkSet2.add(new class_2338(minX - 1, oppFeet.method_10264(), z4));
                                        checkSet2.add(new class_2338(minX + 2, oppFeet.method_10264(), z4));
                                    }
                                    if (checkSet2.contains(candidatePos)) {
                                        return true;
                                    }
                                } else {
                                    continue;
                                }
                            }
                        } else {
                            continue;
                        }
                    }
                }
            }
            return false;
        }
        return false;
    }

    private boolean isEntityPhased(class_1297 e) {
        if (e != null && this.mc.field_1687 != null) {
            class_2338 pos = e.method_24515();
            return !this.mc.field_1687.method_8320(pos).method_26215();
        }
        return false;
    }

    private boolean isInsideEnemyHitboxShell(class_2338 candidatePos) {
        class_1657 enemy;
        if (this.mc.field_1687 != null && this.mc.field_1724 != null) {
            double cx = ((double) candidatePos.method_10263()) + 0.5d;
            double cy = ((double) candidatePos.method_10264()) + 0.5d;
            double cz = ((double) candidatePos.method_10260()) + 0.5d;
            for (class_1657 class_1657Var : this.mc.field_1687.method_18456()) {
                if ((class_1657Var instanceof class_1657) && (enemy = class_1657Var) != this.mc.field_1724 && !Friends.get().isFriend(enemy)) {
                    class_238 bb = enemy.method_5829();
                    class_238 expanded = bb.method_1014(2.0d);
                    if (expanded.method_1008(cx, cy, cz)) {
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }

    private Set<class_2338> getStandingFeetOffsets() {
        Set<class_2338> s = new HashSet<>();
        s.add(new class_2338(0, 0, 0));
        s.add(new class_2338(1, 0, 0));
        s.add(new class_2338(2, 0, 0));
        s.add(new class_2338(3, 0, 0));
        s.add(new class_2338(-1, 0, 0));
        s.add(new class_2338(-2, 0, 0));
        s.add(new class_2338(-3, 0, 0));
        s.add(new class_2338(0, 0, 1));
        s.add(new class_2338(0, 0, 2));
        s.add(new class_2338(0, 0, 3));
        s.add(new class_2338(0, 0, -1));
        s.add(new class_2338(0, 0, -2));
        s.add(new class_2338(0, 0, -3));
        s.add(new class_2338(1, 0, 1));
        s.add(new class_2338(2, 0, 1));
        s.add(new class_2338(1, 0, 2));
        s.add(new class_2338(1, 0, -1));
        s.add(new class_2338(2, 0, -1));
        s.add(new class_2338(1, 0, -2));
        s.add(new class_2338(-1, 0, 1));
        s.add(new class_2338(-2, 0, 1));
        s.add(new class_2338(-1, 0, 2));
        s.add(new class_2338(-1, 0, -1));
        s.add(new class_2338(-2, 0, -1));
        s.add(new class_2338(-1, 0, -2));
        return s;
    }

    private Set<class_2338> getStandingHeadOffsets() {
        Set<class_2338> s = new HashSet<>();
        s.add(new class_2338(0, 1, 0));
        s.add(new class_2338(1, 1, 0));
        s.add(new class_2338(-1, 1, 0));
        s.add(new class_2338(0, 1, 1));
        s.add(new class_2338(0, 1, -1));
        s.add(new class_2338(1, 1, 1));
        s.add(new class_2338(1, 1, -1));
        s.add(new class_2338(-1, 1, 1));
        s.add(new class_2338(-1, 1, -1));
        s.add(new class_2338(2, 1, 0));
        s.add(new class_2338(-2, 1, 0));
        s.add(new class_2338(0, 1, 2));
        s.add(new class_2338(0, 1, -2));
        return s;
    }

    private Set<class_2338> getStandingAboveHeadOffsets() {
        Set<class_2338> s = new HashSet<>();
        s.add(new class_2338(0, 2, 0));
        s.add(new class_2338(0, 3, 0));
        s.add(new class_2338(1, 2, 0));
        s.add(new class_2338(-1, 2, 0));
        s.add(new class_2338(0, 2, 1));
        s.add(new class_2338(0, 2, -1));
        return s;
    }

    private Set<class_2338> getStandingBelowFeetOffsets() {
        Set<class_2338> s = new HashSet<>();
        s.add(new class_2338(0, -1, 0));
        s.add(new class_2338(1, -1, 0));
        s.add(new class_2338(-1, -1, 0));
        s.add(new class_2338(0, -1, 1));
        s.add(new class_2338(0, -1, -1));
        s.add(new class_2338(1, -1, 1));
        s.add(new class_2338(1, -1, -1));
        s.add(new class_2338(-1, -1, 1));
        s.add(new class_2338(-1, -1, -1));
        s.add(new class_2338(0, -2, 0));
        return s;
    }

    private Set<class_2338> getCrawlingBodyOffsets() {
        return getStandingFeetOffsets();
    }

    private Set<class_2338> getCrawlingBelowAndAboveOffsets() {
        Set<class_2338> s = new HashSet<>();
        s.add(new class_2338(0, -1, 0));
        s.add(new class_2338(1, -1, 0));
        s.add(new class_2338(-1, -1, 0));
        s.add(new class_2338(0, -1, 1));
        s.add(new class_2338(0, -1, -1));
        s.add(new class_2338(0, -2, 0));
        s.add(new class_2338(1, -1, 1));
        s.add(new class_2338(1, -1, -1));
        s.add(new class_2338(-1, -1, 1));
        s.add(new class_2338(-1, -1, -1));
        s.add(new class_2338(0, 2, 0));
        s.add(new class_2338(0, 1, 0));
        s.add(new class_2338(1, 1, 0));
        s.add(new class_2338(0, 1, 1));
        s.add(new class_2338(-1, 1, 0));
        s.add(new class_2338(0, 1, -1));
        return s;
    }

    private Set<class_2338> getProtectPositions() {
        Set<class_2338> result = new HashSet<>();
        if (this.mc.field_1724 == null) {
            return result;
        }
        class_2338 base = this.mc.field_1724.method_24515();
        if (!this.mc.field_1724.method_5715() && !this.mc.field_1724.method_20448()) {
            for (class_2338 off : getStandingFeetOffsets()) {
                result.add(base.method_10069(off.method_10263(), off.method_10264(), off.method_10260()));
            }
            for (class_2338 off2 : getStandingHeadOffsets()) {
                result.add(base.method_10069(off2.method_10263(), off2.method_10264(), off2.method_10260()));
            }
            for (class_2338 off3 : getStandingAboveHeadOffsets()) {
                result.add(base.method_10069(off3.method_10263(), off3.method_10264(), off3.method_10260()));
            }
            for (class_2338 off4 : getStandingBelowFeetOffsets()) {
                result.add(base.method_10069(off4.method_10263(), off4.method_10264(), off4.method_10260()));
            }
        } else {
            for (class_2338 off5 : getCrawlingBodyOffsets()) {
                result.add(base.method_10069(off5.method_10263(), off5.method_10264(), off5.method_10260()));
            }
            for (class_2338 off6 : getCrawlingBelowAndAboveOffsets()) {
                result.add(base.method_10069(off6.method_10263(), off6.method_10264(), off6.method_10260()));
            }
        }
        return result;
    }

    private boolean isInsideProtectShape(class_2338 pos, Set<class_2338> protectPositions) {
        return protectPositions.contains(pos);
    }

    private boolean isPlayerPhased() {
        if (this.mc.field_1724 != null && this.mc.field_1687 != null) {
            int playerFeetY = this.mc.field_1724.method_24515().method_10264();
            class_238 bb = this.mc.field_1724.method_5829().method_1009(0.01d, 0.1d, 0.01d);
            int minX = (int) Math.floor(bb.field_1323);
            int maxX = (int) Math.floor(bb.field_1320);
            int minZ = (int) Math.floor(bb.field_1321);
            int maxZ = (int) Math.floor(bb.field_1324);
            int solidBlocks = 0;
            int totalBlocks = 0;
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    class_2338 footBlock = new class_2338(x, playerFeetY, z);
                    if (!this.mc.field_1687.method_8320(footBlock).method_26215()) {
                        solidBlocks++;
                    }
                    totalBlocks++;
                }
            }
            return totalBlocks == 1 ? solidBlocks == 1 : totalBlocks == 2 ? solidBlocks >= 1 : totalBlocks == 4 && solidBlocks >= 2;
        }
        return false;
    }

    private boolean isFootBlockAlmostMined(Set<class_2338> playerFootBlocks) {
        if (this.mc.field_1724 != null && this.mc.field_1687 != null) {
            SilentMine silentMine = (SilentMine) Modules.get().get(SilentMine.class);
            class_2338 rebreakPos = null;
            class_2338 delayedPos = null;
            if (silentMine != null && silentMine.isActive()) {
                rebreakPos = silentMine.getRebreakBlockPos();
                delayedPos = silentMine.getDelayedDestroyBlockPos();
            }
            for (class_2338 feet : playerFootBlocks) {
                class_2680 bs = this.mc.field_1687.method_8320(feet);
                if (bs.method_51176() || bs.method_26215() || feet.equals(rebreakPos) || feet.equals(delayedPos)) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    private boolean isLowHealth(double threshold) {
        return this.mc.field_1724 != null && ((double) this.mc.field_1724.method_6032()) < threshold;
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (this.render.get().booleanValue()) {
            draw(event);
        }
    }

    private void draw(Render3DEvent event) {
        long currentTime = System.currentTimeMillis();
        for (Map.Entry<class_2338, Long> entry : this.renderLastPlacedBlock.entrySet()) {
            if (currentTime - entry.getValue().longValue() <= this.fadeTime.get().doubleValue() * 1000.0d) {
                double time = (currentTime - entry.getValue().longValue()) / 1000.0d;
                double timeCompletion = time / this.fadeTime.get().doubleValue();
                Color fadedSideColor = this.sideColor.get().copy().a((int) (((double) this.sideColor.get().a) * (1.0d - timeCompletion)));
                Color fadedLineColor = this.lineColor.get().copy().a((int) (((double) this.lineColor.get().a) * (1.0d - timeCompletion)));
                event.renderer.box(entry.getKey(), fadedSideColor, fadedLineColor, this.shapeMode.get(), 0);
            }
        }
        for (Map.Entry<class_2338, Long> entry2 : this.renderLastSkippedBlock.entrySet()) {
            if (currentTime - entry2.getValue().longValue() <= this.fadeTime.get().doubleValue() * 1000.0d) {
                double time2 = (currentTime - entry2.getValue().longValue()) / 1000.0d;
                double timeCompletion2 = time2 / this.fadeTime.get().doubleValue();
                Color fadedSideColor2 = this.skippedSideColor.get().copy().a((int) (((double) this.skippedSideColor.get().a) * (1.0d - timeCompletion2)));
                Color fadedLineColor2 = this.skippedLineColor.get().copy().a((int) (((double) this.skippedLineColor.get().a) * (1.0d - timeCompletion2)));
                event.renderer.box(entry2.getKey(), fadedSideColor2, fadedLineColor2, this.shapeMode.get(), 0);
            }
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/Surround$SelfTrapMode.class */
    public enum SelfTrapMode {
        None,
        Smart,
        Always;

        private static SelfTrapMode[] $values() {
            return new SelfTrapMode[]{None, Smart, Always};
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/Surround$ExtendMode.class */
    public enum ExtendMode {
        None,
        Smart,
        Always;

        private static ExtendMode[] $values() {
            return new ExtendMode[]{None, Smart, Always};
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/Surround$CrawlExtendMode.class */
    public enum CrawlExtendMode {
        None,
        Smart;

        private static CrawlExtendMode[] $values() {
            return new CrawlExtendMode[]{None, Smart};
        }
    }
}
