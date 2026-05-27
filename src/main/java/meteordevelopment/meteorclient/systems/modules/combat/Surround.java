package meteordevelopment.meteorclient.systems.modules.combat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Predicate;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
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
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;

public class Surround extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgRender;
    private final Setting<Boolean> pauseEat;
    private final Setting<Boolean> protect;
    private final Setting<Boolean> protectOverrideBlockCooldown;
    private final Setting<Boolean> protectCrystalPlacements;
    private final Setting<Boolean> selfTrapEnabled;
    private final Setting<Surround.SelfTrapMode> autoSelfTrapMode;
    private final Setting<Boolean> selfTrapHead;
    private final Setting<Boolean> extendEnabled;
    private final Setting<Surround.ExtendMode> extendMode;
    private final Setting<Surround.CrawlExtendMode> crawlExtendMode;
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
    private List<BlockPos> placePoses;
    private Map<BlockPos, Long> renderLastPlacedBlock;
    private Map<BlockPos, Long> renderLastSkippedBlock;
    private long lastTimeOfCrystalNearHead;
    private long lastTimeOfCrystalNearFeet;
    private long lastTimeOfExtendCrystal;
    private long lastTimeOfCrawlExtendCrystal;
    private long lastAttackTime;
    private BlockPos lastExtendCrystalOffset;
    private BlockPos lastCrawlExtendCrystalOffset;
    private boolean currentFootBlockThreatened;

    public Surround() {
        super(Categories.Combat, "surround", "Surrounds you in blocks to prevent massive crystal damage.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgRender = this.settings.createGroup("Render");
        this.pauseEat = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("pause-eat")).description("Pauses while eating.")).defaultValue(true)).build());
        this.protect = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("protect")).description("Attempts to break crystals around surround positions to prevent surround break.")).defaultValue(true)).build());
        SettingGroup var10001 = this.sgGeneral;
        BoolSetting.Builder var10002 = (BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("protect-override-block-cooldown")).description("Overrides the cooldown for block placements when you break a crystal. May result in more packet kicks");
        Setting var10003 = this.protect;
        Objects.requireNonNull(var10003);
        this.protectOverrideBlockCooldown = var10001.add(((BoolSetting.Builder)((BoolSetting.Builder)var10002.visible(this.protect::get)).defaultValue(true)).build());
        var10001 = this.sgGeneral;
        var10002 = (BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("protect-instant-place")).description("Attempt to immediately place obsidian at the crystal position right after breaking the crystal.");
        var10003 = this.protect;
        Objects.requireNonNull(var10003);
        this.protectCrystalPlacements = var10001.add(((BoolSetting.Builder)((BoolSetting.Builder)var10002.visible(this.protect::get)).defaultValue(true)).build());
        this.selfTrapEnabled = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("self-trap")).description("Enables self trap")).defaultValue(true)).build());
        var10001 = this.sgGeneral;
        EnumSetting.Builder var1 = (EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("self-trap-mode")).description("When to build double high")).defaultValue(Surround.SelfTrapMode.Smart);
        var10003 = this.selfTrapEnabled;
        Objects.requireNonNull(var10003);
        this.autoSelfTrapMode = var10001.add(((EnumSetting.Builder)var1.visible(this.selfTrapEnabled::get)).build());
        var10001 = this.sgGeneral;
        var10002 = (BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("self-trap-head")).description("Places a block above your head to prevent you from velo failing upwards");
        var10003 = this.selfTrapEnabled;
        Objects.requireNonNull(var10003);
        this.selfTrapHead = var10001.add(((BoolSetting.Builder)((BoolSetting.Builder)var10002.visible(this.selfTrapEnabled::get)).defaultValue(true)).build());
        this.extendEnabled = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("extend")).description("Enables extend placing")).defaultValue(true)).build());
        var10001 = this.sgGeneral;
        var1 = (EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("extend-mode")).description("When to place extend blocks")).defaultValue(Surround.ExtendMode.Smart);
        var10003 = this.extendEnabled;
        Objects.requireNonNull(var10003);
        this.extendMode = var10001.add(((EnumSetting.Builder)var1.visible(this.extendEnabled::get)).build());
        this.crawlExtendMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("crawl-extend-mode")).description("additional protection layer when crawling.")).defaultValue(Surround.CrawlExtendMode.Smart)).build());
        this.enemySilentMineReact = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("enemy-silentmine-react")).description("React to enemy breaking (silent mining) blocks near you using BreakIndicators.")).defaultValue(true)).build());
        this.render = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("render")).description("Renders a block overlay when you try to place obsidian.")).defaultValue(true)).build());
        this.fadeTime = this.sgRender.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("fadeTime")).description("How many seconds it takes to fade.")).defaultValue(0.2D).min(0.0D).sliderMax(1.0D).build());
        this.shapeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("shape-mode")).description("How the shapes are rendered.")).defaultValue(ShapeMode.Both)).build());
        this.sideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("side-color")).description("The side color.")).defaultValue(new SettingColor(85, 0, 255, 40)).visible(() -> {
            return (Boolean)this.render.get() && this.shapeMode.get() != ShapeMode.Lines;
        })).build());
        this.lineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("line-color")).description("The line color.")).defaultValue(new SettingColor(255, 255, 255, 60)).visible(() -> {
            return (Boolean)this.render.get() && this.shapeMode.get() != ShapeMode.Sides;
        })).build());
        this.avoidHelpingOpponents = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("avoid-helping-opponents")).description("Avoid placing blocks that directly help opponents surround themselves (heuristic).")).defaultValue(true)).build());
        this.skippedSideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("skipped-side-color")).description("Side color for skipped blocks.")).defaultValue(new SettingColor(255, 0, 0, 40)).visible(() -> {
            return (Boolean)this.render.get() && this.shapeMode.get() != ShapeMode.Lines;
        })).build());
        this.skippedLineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("skipped-line-color")).description("Line color for skipped blocks.")).defaultValue(new SettingColor(255, 0, 0, 120)).visible(() -> {
            return (Boolean)this.render.get() && this.shapeMode.get() != ShapeMode.Sides;
        })).build());
        this.debugProtectShape = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("debug-protect-shape")).description("Renders the crystal protect shape positions.")).defaultValue(false)).build());
        this.placePoses = new ArrayList();
        this.renderLastPlacedBlock = new HashMap();
        this.renderLastSkippedBlock = new HashMap();
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
        this.placePoses.clear();
        List<BlockPos> feetBlocks = new ArrayList();
        List<BlockPos> selfTrapBlocks = new ArrayList();
        List<BlockPos> extendBlocks = new ArrayList();
        long currentTime = System.currentTimeMillis();
        if (this.mc.player != null && this.mc.world != null) {
            Box boundingBox;
            int feetY;
            int bbMinX;
            int bbMaxX;
            int playerFeetY;
            boolean amIn1x1;
            boolean lowHealth;
            SilentMine silentMine;
            int bbMaxZ;
            int minX;
            boolean extendThreatDetected;
            int maxX;
            BlockPos pos;
            BlockPos rebreak;
            boolean helps;
            int dx;
            boolean isNearMyFeet;
            BlockPos feetPos;
            int x;
            BlockPos t;
            int perpX1;
            if (!this.mc.player.isSneaking() && !this.mc.player.isCrawling()) {
                boundingBox = this.mc.player.getBoundingBox().expand(0.01D, 0.1D, 0.01D);
                feetY = this.mc.player.getBlockPos().getY();
                silentMine = (SilentMine)Modules.get().get(SilentMine.class);
                bbMinX = (int)Math.floor(boundingBox.minX);
                bbMaxX = (int)Math.floor(boundingBox.maxX);
                playerFeetY = (int)Math.floor(boundingBox.minZ);
                bbMaxZ = (int)Math.floor(boundingBox.maxZ);
                lowHealth = bbMinX == bbMaxX && playerFeetY == bbMaxZ;
                extendThreatDetected = false;
                boolean threatFromAbove = false;
                helps = false;
                Box headCheckBox;
                if ((Boolean)this.selfTrapEnabled.get() || (Boolean)this.extendEnabled.get() && this.extendMode.get() == Surround.ExtendMode.Smart) {
                    headCheckBox = this.mc.player.getBoundingBox().stretch(1.5D, 0.5D, 1.5D).offset(0.0D, 1.0D, 0.0D);
                    if (EntityUtils.intersectsWithEntity(headCheckBox, (e) -> {
                        return e instanceof EndCrystalEntity;
                    })) {
                        threatFromAbove = true;
                        this.lastTimeOfCrystalNearHead = currentTime;
                    }

                    Box feetBox = this.mc.player.getBoundingBox().stretch(1.5D, 0.0D, 1.5D).offset(0.0D, -1.0D, 0.0D);
                    if (EntityUtils.intersectsWithEntity(feetBox, (e) -> {
                        return e instanceof EndCrystalEntity;
                    })) {
                        helps = true;
                        this.lastTimeOfCrystalNearFeet = currentTime;
                    }
                }

                if (this.extendMode.get() == Surround.ExtendMode.Smart && (Boolean)this.extendEnabled.get() && lowHealth) {
                    headCheckBox = this.mc.player.getBoundingBox().stretch(1.5D, 0.5D, 1.5D).offset(0.0D, 1.0D, 0.0D);
                    EndCrystalEntity closestCrystal = null;
                    double closestDistSq = Double.MAX_VALUE;
                    Iterator var22 = this.mc.world.getEntitiesByClass(EndCrystalEntity.class, headCheckBox, (e) -> {
                        return true;
                    }).iterator();

                    while(var22.hasNext()) {
                        Entity entity = (Entity)var22.next();
                        double distSq = entity.squaredDistanceTo(this.mc.player.getEyePos());
                        if (distSq < closestDistSq) {
                            closestDistSq = distSq;
                            closestCrystal = (EndCrystalEntity)entity;
                        }
                    }

                    if (closestCrystal != null) {
                        this.lastExtendCrystalOffset = closestCrystal.getBlockPos().subtract(this.mc.player.getBlockPos());
                        this.lastTimeOfExtendCrystal = currentTime;
                        extendThreatDetected = true;
                    }
                }

                for(dx = bbMinX; dx <= bbMaxX; ++dx) {
                    for(x = playerFeetY; x <= bbMaxZ; ++x) {
                        feetPos = new BlockPos(dx, feetY, x);

                        for(int offsetX = -1; offsetX <= 1; ++offsetX) {
                            for(perpX1 = -1; perpX1 <= 1; ++perpX1) {
                                if (Math.abs(offsetX) + Math.abs(perpX1) == 1) {
                                    BlockPos adjacentPos = feetPos.add(offsetX, 0, perpX1);
                                    if (this.mc.world.getBlockState(adjacentPos).isAir()) {
                                        feetBlocks.add(adjacentPos);
                                    }

                                    if (this.autoSelfTrapMode.get() != Surround.SelfTrapMode.None && (Boolean)this.selfTrapEnabled.get()) {
                                        this.checkSmartDefenses(selfTrapBlocks, adjacentPos, threatFromAbove, helps, currentTime);
                                    }
                                }
                            }
                        }

                        t = new BlockPos(dx, feetY - 1, x);
                        BlockState belowFeetState = this.mc.world.getBlockState(t);
                        if (!t.equals(silentMine.getRebreakBlockPos()) && !t.equals(silentMine.getDelayedDestroyBlockPos()) && (belowFeetState.isLiquid() || belowFeetState.isAir())) {
                            feetBlocks.add(t);
                        }
                    }
                }

                if ((Boolean)this.extendEnabled.get() && lowHealth) {
                    isNearMyFeet = this.extendMode.get() == Surround.ExtendMode.Always || this.extendMode.get() == Surround.ExtendMode.Smart && extendThreatDetected && (double)(currentTime - this.lastTimeOfExtendCrystal) / 1000.0D < 1.0D;
                    if (isNearMyFeet) {
                        this.placeExtendBlocks(extendBlocks, this.mc.player.getBlockPos(), this.lastExtendCrystalOffset);
                    }
                }

                if ((Boolean)this.selfTrapEnabled.get() && (Boolean)this.selfTrapHead.get() && !this.mc.player.isSneaking()) {
                    selfTrapBlocks.add(this.mc.player.getBlockPos().up(2));
                }
            } else {
                boundingBox = this.mc.player.getBoundingBox().expand(0.01D, 0.0D, 0.01D);
                feetY = this.mc.player.getBlockPos().getY();
                minX = (int)Math.floor(boundingBox.minX);
                bbMinX = (int)Math.floor(boundingBox.maxX);
                bbMaxX = (int)Math.floor(boundingBox.minY);
                playerFeetY = (int)Math.floor(boundingBox.maxZ);
                amIn1x1 = minX == bbMinX && bbMaxX == playerFeetY;
                lowHealth = false;
                Iterator var19;
                if (this.crawlExtendMode.get() == Surround.CrawlExtendMode.Smart && amIn1x1) {
                    Box detectionBox = this.mc.player.getBoundingBox().stretch(2.0D, 1.0D, 2.0D);
                    EndCrystalEntity closestCrystal = null;
                    double closestDistSq = Double.MAX_VALUE;
                    var19 = this.mc.world.getEntitiesByClass(EndCrystalEntity.class, detectionBox, (e) -> {
                        return true;
                    }).iterator();

                    while(var19.hasNext()) {
                        Entity entity = (Entity)var19.next();
                        double distSq = entity.squaredDistanceTo(this.mc.player.getPos());
                        if (distSq < closestDistSq) {
                            closestDistSq = distSq;
                            closestCrystal = (EndCrystalEntity)entity;
                        }
                    }

                    if (closestCrystal != null) {
                        this.lastCrawlExtendCrystalOffset = closestCrystal.getBlockPos().subtract(this.mc.player.getBlockPos());
                        this.lastTimeOfCrawlExtendCrystal = currentTime;
                        lowHealth = true;
                    }
                }

                for(minX = minX; minX <= bbMinX; ++minX) {
                    for(maxX = bbMaxX; maxX <= playerFeetY; ++maxX) {
                        rebreak = new BlockPos(minX, feetY, maxX);
                        List<BlockPos> criticalOffsets = List.of(new BlockPos(0, 1, 0), new BlockPos(0, -1, 0), new BlockPos(1, 0, 0), new BlockPos(-1, 0, 0), new BlockPos(0, 0, 1), new BlockPos(0, 0, -1));
                        var19 = criticalOffsets.iterator();

                        while(var19.hasNext()) {
                            feetPos = (BlockPos)var19.next();
                            t = rebreak.add(feetPos.getX(), feetPos.getY(), feetPos.getZ());
                            if (this.mc.world.getBlockState(t).isAir()) {
                                feetBlocks.add(t);
                            }
                        }
                    }
                }

                extendThreatDetected = amIn1x1 && this.crawlExtendMode.get() == Surround.CrawlExtendMode.Smart && lowHealth && (double)(currentTime - this.lastTimeOfCrawlExtendCrystal) / 1000.0D < 1.0D;
                if (extendThreatDetected) {
                    pos = new BlockPos(minX, feetY, bbMaxX);
                    this.placeExtendBlocks(extendBlocks, pos, this.lastCrawlExtendCrystalOffset);
                }
            }

            BreakIndicators breakIndicators = (BreakIndicators)Modules.get().get(BreakIndicators.class);
            this.currentFootBlockThreatened = false;
            int dz;
            if ((Boolean)this.enemySilentMineReact.get() && breakIndicators != null && this.mc.player != null) {
                BlockPos myFeet = this.mc.player.getBlockPos();
                Box bbFeet = this.mc.player.getBoundingBox().expand(0.01D, 0.1D, 0.01D);
                bbMinX = (int)Math.floor(bbFeet.minX);
                bbMaxX = (int)Math.floor(bbFeet.maxX);
                playerFeetY = (int)Math.floor(bbFeet.minZ);
                bbMaxZ = (int)Math.floor(bbFeet.maxZ);
                lowHealth = bbMinX == bbMaxX && playerFeetY == bbMaxZ;
                if (lowHealth) {
                    Map<BlockPos, BreakIndicators.BlockBreak> active = breakIndicators.breakStartTimes;
                    Iterator var69 = active.values().iterator();

                    label389:
                    while(true) {
                        BreakIndicators.BlockBreak info;
                        do {
                            do {
                                do {
                                    if (!var69.hasNext()) {
                                        break label389;
                                    }

                                    info = (BreakIndicators.BlockBreak)var69.next();
                                } while(info.entity == null);

                                dx = info.blockPos.getX() - myFeet.getX();
                                x = info.blockPos.getY() - myFeet.getY();
                                dz = info.blockPos.getZ() - myFeet.getZ();
                            } while(x != 0);
                        } while(Math.abs(dx) + Math.abs(dz) != 1);

                        this.currentFootBlockThreatened = true;
                        t = info.blockPos;
                        int perpZ2;
                        int perpZ1;
                        int perpX2;
                        if (dx != 0) {
                            perpX1 = dx;
                            perpZ1 = 1;
                            perpX2 = dx;
                            perpZ2 = -1;
                        } else {
                            perpX1 = 1;
                            perpZ1 = dz;
                            perpX2 = -1;
                            perpZ2 = dz;
                        }

                        BlockPos aboveAdj = t.up();
                        BlockPos belowAdj = t.down();
                        BlockPos perp1 = myFeet.add(perpX1, 0, perpZ1);
                        BlockPos perp2 = myFeet.add(perpX2, 0, perpZ2);
                        BlockPos outward2 = myFeet.add(dx * 2, 0, dz * 2);
                        BlockPos[] targets = new BlockPos[]{aboveAdj, belowAdj, perp1, perp2, outward2};
                        BlockPos[] var32 = targets;
                        int var33 = targets.length;

                        for(int var34 = 0; var34 < var33; ++var34) {
                            BlockPos t2 = var32[var34];
                            if (!t2.equals(myFeet) && this.mc.world.getBlockState(t2).isAir()) {
                                extendBlocks.add(t2);
                            }
                        }
                    }
                }
            }

            this.placePoses.addAll(feetBlocks);
            this.placePoses.addAll(selfTrapBlocks);
            this.placePoses.addAll(extendBlocks);
            Set<BlockPos> protectPositions = this.getProtectPositions();
            if (!(Boolean)this.pauseEat.get() || !this.mc.player.isUsingItem()) {
                if ((Boolean)this.protect.get()) {
                    silentMine = (SilentMine)Modules.get().get(SilentMine.class);
                    Iterator var44 = protectPositions.iterator();

                    label361:
                    while(true) {
                        ArrayList immediateTargets;
                        BlockPos delayed;
                        do {
                            do {
                                Entity blocking;
                                do {
                                    BlockPos protectPos;
                                    do {
                                        BlockPos crystalPos;
                                        do {
                                            do {
                                                do {
                                                    do {
                                                        if (!var44.hasNext()) {
                                                            break label361;
                                                        }

                                                        protectPos = (BlockPos)var44.next();
                                                        Box box = new Box((double)protectPos.getX(), (double)protectPos.getY(), (double)protectPos.getZ(), (double)(protectPos.getX() + 1), (double)(protectPos.getY() + 1), (double)(protectPos.getZ() + 1));
                                                        Predicate<Entity> entityPredicate = (entityx) -> {
                                                            return entityx instanceof EndCrystalEntity;
                                                        };
                                                        blocking = (Entity)this.mc.world.getEntitiesByClass(EndCrystalEntity.class, box, entityPredicate).stream().findFirst().orElse(null);
                                                    } while(blocking == null);
                                                } while(System.currentTimeMillis() - this.lastAttackTime < 50L);

                                                crystalPos = blocking.getBlockPos();
                                            } while(crystalPos == null);
                                        } while(!this.isInsideProtectShape(crystalPos, protectPositions));

                                        MeteorClient.ROTATION.requestRotation(blocking.getEyePos(), 11.0D);
                                        if (!MeteorClient.ROTATION.lookingAt(blocking.getBoundingBox()) && RotationManager.lastGround) {
                                            MeteorClient.ROTATION.snapAt(blocking.getEyePos());
                                        }
                                    } while(!MeteorClient.ROTATION.lookingAt(blocking.getBoundingBox()));

                                    this.mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(blocking, this.mc.player.isSneaking()));
                                    this.mc.interactionManager.attackEntity(this.mc.player, blocking);
                                    this.lastAttackTime = System.currentTimeMillis();
                                    if ((Boolean)this.protectOverrideBlockCooldown.get()) {
                                        MeteorClient.BLOCK.forceResetPlaceCooldown(protectPos);
                                    }
                                } while(!(Boolean)this.protectCrystalPlacements.get());

                                immediateTargets = new ArrayList();
                                rebreak = null;
                                delayed = null;
                                if (silentMine != null && silentMine.isActive()) {
                                    rebreak = silentMine.getRebreakBlockPos();
                                    delayed = silentMine.getDelayedDestroyBlockPos();
                                }

                                BlockPos crystalPos2 = blocking.getBlockPos();
                                if (crystalPos2 != null && this.isInsideProtectShape(crystalPos2, protectPositions) && !this.isInsideEnemyHitboxShell(crystalPos2) && this.mc.world.getBlockState(crystalPos2).isAir() && (rebreak == null || !crystalPos2.equals(rebreak)) && (delayed == null || !crystalPos2.equals(delayed))) {
                                    immediateTargets.add(crystalPos2);
                                }
                            } while(immediateTargets.isEmpty());
                        } while(!MeteorClient.BLOCK.beginPlacement(immediateTargets, Items.OBSIDIAN));

                        Iterator var85 = immediateTargets.iterator();

                        while(var85.hasNext()) {
                            t = (BlockPos)var85.next();

                            try {
                                if (!t.equals(rebreak) && !t.equals(delayed) && MeteorClient.BLOCK.placeBlock(Items.OBSIDIAN, t)) {
                                    this.renderLastPlacedBlock.put(t, System.currentTimeMillis());
                                }
                            } catch (Exception var36) {
                            }
                        }

                        MeteorClient.BLOCK.endPlacement();
                    }
                }

                List<BlockPos> filteredPlacePoses = new ArrayList();
                Set<BlockPos> skippedThisTick = new HashSet();
                if (!(Boolean)this.avoidHelpingOpponents.get()) {
                    filteredPlacePoses.addAll(this.placePoses);
                } else {
                    Set<BlockPos> playerFootBlocks = new HashSet();
                    playerFeetY = this.mc.player.getBlockPos().getY();
                    amIn1x1 = false;
                    Box bb = this.mc.player.getBoundingBox().expand(0.01D, 0.1D, 0.01D);
                    minX = (int)Math.floor(bb.minX);
                    maxX = (int)Math.floor(bb.maxX);
                    int minZ = (int)Math.floor(bb.minZ);
                    dx = (int)Math.floor(bb.maxZ);
                    amIn1x1 = minX == maxX && minZ == dx;
                    x = minX;

                    label290:
                    while(true) {
                        if (x > maxX) {
                            lowHealth = this.isLowHealth(10.0D);
                            Iterator var68 = this.placePoses.iterator();

                            while(true) {
                                while(true) {
                                    if (!var68.hasNext()) {
                                        break label290;
                                    }

                                    pos = (BlockPos)var68.next();
                                    helps = this.wouldHelpOpponent(pos);
                                    if (!helps) {
                                        filteredPlacePoses.add(pos);
                                    } else {
                                        isNearMyFeet = this.isNearMyPerimeter(pos, playerFootBlocks);
                                        if (amIn1x1) {
                                            filteredPlacePoses.add(pos);
                                        } else if (lowHealth) {
                                            filteredPlacePoses.add(pos);
                                        } else if (this.isPlayerPhased() && !this.currentFootBlockThreatened) {
                                            skippedThisTick.add(pos);
                                        } else if (isNearMyFeet) {
                                            filteredPlacePoses.add(pos);
                                        } else {
                                            skippedThisTick.add(pos);
                                        }
                                    }
                                }
                            }
                        }

                        for(dz = minZ; dz <= dx; ++dz) {
                            playerFootBlocks.add(new BlockPos(x, playerFeetY, dz));
                        }

                        ++x;
                    }
                }

                long now = System.currentTimeMillis();
                Iterator var54 = skippedThisTick.iterator();

                while(var54.hasNext()) {
                    BlockPos p = (BlockPos)var54.next();
                    this.renderLastSkippedBlock.put(p, now);
                }

                if (MeteorClient.BLOCK.beginPlacement(filteredPlacePoses, Items.OBSIDIAN)) {
                    filteredPlacePoses.forEach((blockPos) -> {
                        if (!blockPos.equals(((SilentMine)Modules.get().get(SilentMine.class)).getRebreakBlockPos()) && !blockPos.equals(((SilentMine)Modules.get().get(SilentMine.class)).getLastDelayedDestroyBlockPos()) && MeteorClient.BLOCK.placeBlock(Items.OBSIDIAN, blockPos)) {
                            this.renderLastPlacedBlock.put(blockPos, currentTime);
                        }

                    });
                    MeteorClient.BLOCK.endPlacement();
                }
            }

            if ((Boolean)this.debugProtectShape.get() && this.mc.world != null && this.mc.player != null) {
                long nowDbg = System.currentTimeMillis();
                Iterator var49 = protectPositions.iterator();

                while(var49.hasNext()) {
                    BlockPos p = (BlockPos)var49.next();
                    this.renderLastSkippedBlock.put(p, nowDbg);
                }
            }

        }
    }

    private boolean isNearMyPerimeter(BlockPos candidatePos, Set<BlockPos> playerBlocks) {
        Iterator var3 = playerBlocks.iterator();

        BlockPos playerBlock;
        do {
            if (!var3.hasNext()) {
                return false;
            }

            playerBlock = (BlockPos)var3.next();
        } while(!candidatePos.equals(playerBlock.up()) && !candidatePos.equals(playerBlock.down()) && !candidatePos.equals(playerBlock.south()) && !candidatePos.equals(playerBlock.north()) && !candidatePos.equals(playerBlock.east()) && !candidatePos.equals(playerBlock.west()) && !candidatePos.equals(playerBlock));

        return true;
    }

    private void checkSmartDefenses(List<BlockPos> placePoses, BlockPos adjacentPos, boolean threatFromAbove, boolean threatFromBelow, long currentTime) {
        if (this.mc.world != null && this.mc.player != null) {
            boolean shouldBuildUp = this.autoSelfTrapMode.get() == Surround.SelfTrapMode.Always || this.autoSelfTrapMode.get() == Surround.SelfTrapMode.Smart && threatFromAbove && (double)(currentTime - this.lastTimeOfCrystalNearHead) / 1000.0D < 1.0D;
            if (shouldBuildUp) {
                BlockPos facePlacePos = adjacentPos.up();
                if (this.mc.world.getBlockState(facePlacePos).isAir()) {
                    placePoses.add(facePlacePos);
                }
            }

            boolean shouldBuildDown = this.autoSelfTrapMode.get() == Surround.SelfTrapMode.Always || this.autoSelfTrapMode.get() == Surround.SelfTrapMode.Smart && threatFromAbove && (double)(currentTime - this.lastTimeOfCrystalNearFeet) / 1000.0D < 1.0D;
            if (shouldBuildDown) {
                BlockPos belowPos = adjacentPos.down();
                if (this.mc.world.getBlockState(belowPos).isAir()) {
                    placePoses.add(belowPos);
                }
            }

            BlockPos feetFloor = this.mc.player.getBlockPos().down(2);
            if (this.mc.world.getBlockState(feetFloor).isAir()) {
                placePoses.add(feetFloor);
            }

        }
    }

    private void placeExtendBlocks(List<BlockPos> placePoses, BlockPos feetPos, BlockPos crystalOffset) {
        if (crystalOffset != null && this.mc.world != null) {
            int normDx = Integer.signum(crystalOffset.getX());
            int normDz = Integer.signum(crystalOffset.getZ());
            boolean isDiagonal = normDx != 0 && normDz != 0;
            boolean isCardinal = normDx != 0 ^ normDz != 0;
            BlockPos diagonal1;
            BlockPos diagonal2;
            BlockPos straightBlock;
            if (isDiagonal) {
                diagonal1 = feetPos.add(normDx, 0, normDz);
                if (this.mc.world.getBlockState(diagonal1).isAir()) {
                    placePoses.add(diagonal1);
                }

                diagonal2 = feetPos.add(normDx * 2, 0, 0);
                if (this.mc.world.getBlockState(diagonal2).isAir()) {
                    placePoses.add(diagonal2);
                }

                straightBlock = feetPos.add(0, 0, normDz * 2);
                if (this.mc.world.getBlockState(straightBlock).isAir()) {
                    placePoses.add(straightBlock);
                }
            } else if (isCardinal) {
                if (normDx != 0) {
                    diagonal1 = feetPos.add(normDx, 0, 1);
                    diagonal2 = feetPos.add(normDx, 0, -1);
                    straightBlock = feetPos.add(normDx * 2, 0, 0);
                } else {
                    diagonal1 = feetPos.add(1, 0, normDz);
                    diagonal2 = feetPos.add(-1, 0, normDz);
                    straightBlock = feetPos.add(0, 0, normDz * 2);
                }

                if (this.mc.world.getBlockState(diagonal1).isAir()) {
                    placePoses.add(diagonal1);
                }

                if (this.mc.world.getBlockState(diagonal2).isAir()) {
                    placePoses.add(diagonal2);
                }

                if (this.mc.world.getBlockState(straightBlock).isAir()) {
                    placePoses.add(straightBlock);
                }
            }

        }
    }

    private boolean isCrystalBlock(BlockPos blockPos) {
        BlockState blockState = this.mc.world.getBlockState(blockPos);
        return blockState.isOf(Blocks.OBSIDIAN) || blockState.isOf(Blocks.BEDROCK);
    }

    private boolean wouldHelpOpponent(BlockPos candidatePos) {
        if (!(Boolean)this.avoidHelpingOpponents.get()) {
            return false;
        } else if (this.mc.world != null && this.mc.player != null) {
            BlockPos cand = candidatePos;
            Iterator var3 = this.mc.world.getPlayers().iterator();

            while(true) {
                PlayerEntity p;
                BlockPos oppFeet;
                int minX;
                int minZ;
                int sizeX;
                int sizeZ;
                boolean oppIn1x1;
                boolean oppIn2x1;
                boolean oppIn2x2;
                do {
                    double horiz;
                    do {
                        do {
                            Entity e;
                            do {
                                do {
                                    if (!var3.hasNext()) {
                                        return false;
                                    }

                                    e = (Entity)var3.next();
                                } while(e == this.mc.player);
                            } while(!(e instanceof PlayerEntity));

                            p = (PlayerEntity)e;
                        } while(Friends.get().isFriend(p));

                        oppFeet = p.getBlockPos();
                        horiz = Math.sqrt(Math.pow((double)(oppFeet.getX() - this.mc.player.getBlockPos().getX()), 2.0D) + Math.pow((double)(oppFeet.getZ() - this.mc.player.getBlockPos().getZ()), 2.0D));
                    } while(horiz > 5.0D);

                    Box pbb = p.getBoundingBox().expand(0.01D, 0.1D, 0.01D);
                    minX = (int)Math.floor(pbb.minX);
                    int maxX = (int)Math.floor(pbb.maxX);
                    minZ = (int)Math.floor(pbb.minZ);
                    int maxZ = (int)Math.floor(pbb.maxZ);
                    sizeX = maxX - minX + 1;
                    sizeZ = maxZ - minZ + 1;
                    oppIn1x1 = sizeX == 1 && sizeZ == 1;
                    oppIn2x1 = sizeX == 2 && sizeZ == 1 || sizeX == 1 && sizeZ == 2;
                    oppIn2x2 = sizeX == 2 && sizeZ == 2;
                } while(this.isEntityPhased(p));

                if (oppIn1x1) {
                    List<BlockPos> checkSet = List.of(oppFeet.south(), oppFeet.north(), oppFeet.east(), oppFeet.west());
                    if (checkSet.contains(cand)) {
                        return true;
                    }

                    if (cand.equals(oppFeet.down()) || cand.equals(oppFeet)) {
                        return true;
                    }
                }

                int startX;
                int startZ;
                int z;
                ArrayList checkSet;
                if (oppIn2x1) {
                    checkSet = new ArrayList();
                    startX = minX;
                    startZ = minZ;

                    for(z = minX; z < startX + sizeX; ++z) {
                        checkSet.add(new BlockPos(z, oppFeet.getY(), startZ - 1));
                        checkSet.add(new BlockPos(z, oppFeet.getY(), startZ + sizeZ));
                    }

                    for(z = startZ; z < startZ + sizeZ; ++z) {
                        checkSet.add(new BlockPos(startX - 1, oppFeet.getY(), z));
                        checkSet.add(new BlockPos(startX + sizeX, oppFeet.getY(), z));
                    }

                    if (checkSet.contains(cand)) {
                        return true;
                    }
                }

                if (oppIn2x2) {
                    checkSet = new ArrayList();
                    startX = minX;
                    startZ = minZ;

                    for(z = minX; z < startX + 2; ++z) {
                        checkSet.add(new BlockPos(z, oppFeet.getY(), startZ - 1));
                        checkSet.add(new BlockPos(z, oppFeet.getY(), startZ + 2));
                    }

                    for(z = startZ; z < startZ + 2; ++z) {
                        checkSet.add(new BlockPos(startX - 1, oppFeet.getY(), z));
                        checkSet.add(new BlockPos(startX + 2, oppFeet.getY(), z));
                    }

                    if (checkSet.contains(cand)) {
                        return true;
                    }
                }
            }
        } else {
            return false;
        }
    }

    private boolean isEntityPhased(Entity e) {
        if (e != null && this.mc.world != null) {
            BlockPos pos = e.getBlockPos();
            return !this.mc.world.getBlockState(pos).isAir();
        } else {
            return false;
        }
    }

    private boolean isInsideEnemyHitboxShell(BlockPos candidatePos) {
        if (this.mc.world != null && this.mc.player != null) {
            double cx = (double)candidatePos.getX() + 0.5D;
            double cy = (double)candidatePos.getY() + 0.5D;
            double cz = (double)candidatePos.getZ() + 0.5D;
            Iterator var8 = this.mc.world.getPlayers().iterator();

            while(var8.hasNext()) {
                Entity e = (Entity)var8.next();
                if (e instanceof PlayerEntity) {
                    PlayerEntity enemy = (PlayerEntity)e;
                    if (enemy != this.mc.player && !Friends.get().isFriend(enemy)) {
                        Box bb = enemy.getBoundingBox();
                        Box expanded = bb.expand(2.0D);
                        if (expanded.contains(cx, cy, cz)) {
                            return true;
                        }
                    }
                }
            }

            return false;
        } else {
            return false;
        }
    }

    private Set<BlockPos> getStandingFeetOffsets() {
        Set<BlockPos> s = new HashSet();
        s.add(new BlockPos(0, 0, 0));
        s.add(new BlockPos(1, 0, 0));
        s.add(new BlockPos(2, 0, 0));
        s.add(new BlockPos(3, 0, 0));
        s.add(new BlockPos(-1, 0, 0));
        s.add(new BlockPos(-2, 0, 0));
        s.add(new BlockPos(-3, 0, 0));
        s.add(new BlockPos(0, 0, 1));
        s.add(new BlockPos(0, 0, 2));
        s.add(new BlockPos(0, 0, 3));
        s.add(new BlockPos(0, 0, -1));
        s.add(new BlockPos(0, 0, -2));
        s.add(new BlockPos(0, 0, -3));
        s.add(new BlockPos(1, 0, 1));
        s.add(new BlockPos(2, 0, 1));
        s.add(new BlockPos(1, 0, 2));
        s.add(new BlockPos(1, 0, -1));
        s.add(new BlockPos(2, 0, -1));
        s.add(new BlockPos(1, 0, -2));
        s.add(new BlockPos(-1, 0, 1));
        s.add(new BlockPos(-2, 0, 1));
        s.add(new BlockPos(-1, 0, 2));
        s.add(new BlockPos(-1, 0, -1));
        s.add(new BlockPos(-2, 0, -1));
        s.add(new BlockPos(-1, 0, -2));
        return s;
    }

    private Set<BlockPos> getStandingHeadOffsets() {
        Set<BlockPos> s = new HashSet();
        s.add(new BlockPos(0, 1, 0));
        s.add(new BlockPos(1, 1, 0));
        s.add(new BlockPos(-1, 1, 0));
        s.add(new BlockPos(0, 1, 1));
        s.add(new BlockPos(0, 1, -1));
        s.add(new BlockPos(1, 1, 1));
        s.add(new BlockPos(1, 1, -1));
        s.add(new BlockPos(-1, 1, 1));
        s.add(new BlockPos(-1, 1, -1));
        s.add(new BlockPos(2, 1, 0));
        s.add(new BlockPos(-2, 1, 0));
        s.add(new BlockPos(0, 1, 2));
        s.add(new BlockPos(0, 1, -2));
        return s;
    }

    private Set<BlockPos> getStandingAboveHeadOffsets() {
        Set<BlockPos> s = new HashSet();
        s.add(new BlockPos(0, 2, 0));
        s.add(new BlockPos(0, 3, 0));
        s.add(new BlockPos(1, 2, 0));
        s.add(new BlockPos(-1, 2, 0));
        s.add(new BlockPos(0, 2, 1));
        s.add(new BlockPos(0, 2, -1));
        return s;
    }

    private Set<BlockPos> getStandingBelowFeetOffsets() {
        Set<BlockPos> s = new HashSet();
        s.add(new BlockPos(0, -1, 0));
        s.add(new BlockPos(1, -1, 0));
        s.add(new BlockPos(-1, -1, 0));
        s.add(new BlockPos(0, -1, 1));
        s.add(new BlockPos(0, -1, -1));
        s.add(new BlockPos(1, -1, 1));
        s.add(new BlockPos(1, -1, -1));
        s.add(new BlockPos(-1, -1, 1));
        s.add(new BlockPos(-1, -1, -1));
        s.add(new BlockPos(0, -2, 0));
        return s;
    }

    private Set<BlockPos> getCrawlingBodyOffsets() {
        return this.getStandingFeetOffsets();
    }

    private Set<BlockPos> getCrawlingBelowAndAboveOffsets() {
        Set<BlockPos> s = new HashSet();
        s.add(new BlockPos(0, -1, 0));
        s.add(new BlockPos(1, -1, 0));
        s.add(new BlockPos(-1, -1, 0));
        s.add(new BlockPos(0, -1, 1));
        s.add(new BlockPos(0, -1, -1));
        s.add(new BlockPos(0, -2, 0));
        s.add(new BlockPos(1, -1, 1));
        s.add(new BlockPos(1, -1, -1));
        s.add(new BlockPos(-1, -1, 1));
        s.add(new BlockPos(-1, -1, -1));
        s.add(new BlockPos(0, 2, 0));
        s.add(new BlockPos(0, 1, 0));
        s.add(new BlockPos(1, 1, 0));
        s.add(new BlockPos(0, 1, 1));
        s.add(new BlockPos(-1, 1, 0));
        s.add(new BlockPos(0, 1, -1));
        return s;
    }

    private Set<BlockPos> getProtectPositions() {
        Set<BlockPos> result = new HashSet();
        if (this.mc.player == null) {
            return result;
        } else {
            BlockPos base = this.mc.player.getBlockPos();
            Iterator var3;
            BlockPos off;
            if (!this.mc.player.isSneaking() && !this.mc.player.isCrawling()) {
                var3 = this.getStandingFeetOffsets().iterator();

                while(var3.hasNext()) {
                    off = (BlockPos)var3.next();
                    result.add(base.add(off.getX(), off.getY(), off.getZ()));
                }

                var3 = this.getStandingHeadOffsets().iterator();

                while(var3.hasNext()) {
                    off = (BlockPos)var3.next();
                    result.add(base.add(off.getX(), off.getY(), off.getZ()));
                }

                var3 = this.getStandingAboveHeadOffsets().iterator();

                while(var3.hasNext()) {
                    off = (BlockPos)var3.next();
                    result.add(base.add(off.getX(), off.getY(), off.getZ()));
                }

                var3 = this.getStandingBelowFeetOffsets().iterator();

                while(var3.hasNext()) {
                    off = (BlockPos)var3.next();
                    result.add(base.add(off.getX(), off.getY(), off.getZ()));
                }
            } else {
                var3 = this.getCrawlingBodyOffsets().iterator();

                while(var3.hasNext()) {
                    off = (BlockPos)var3.next();
                    result.add(base.add(off.getX(), off.getY(), off.getZ()));
                }

                var3 = this.getCrawlingBelowAndAboveOffsets().iterator();

                while(var3.hasNext()) {
                    off = (BlockPos)var3.next();
                    result.add(base.add(off.getX(), off.getY(), off.getZ()));
                }
            }

            return result;
        }
    }

    private boolean isInsideProtectShape(BlockPos pos, Set<BlockPos> protectPositions) {
        return protectPositions.contains(pos);
    }

    private boolean isPlayerPhased() {
        if (this.mc.player != null && this.mc.world != null) {
            int playerFeetY = this.mc.player.getBlockPos().getY();
            Box bb = this.mc.player.getBoundingBox().expand(0.01D, 0.1D, 0.01D);
            int minX = (int)Math.floor(bb.minX);
            int maxX = (int)Math.floor(bb.maxX);
            int minZ = (int)Math.floor(bb.minZ);
            int maxZ = (int)Math.floor(bb.maxZ);
            int solidBlocks = 0;
            int totalBlocks = 0;

            for(int x = minX; x <= maxX; ++x) {
                for(int z = minZ; z <= maxZ; ++z) {
                    BlockPos footBlock = new BlockPos(x, playerFeetY, z);
                    if (!this.mc.world.getBlockState(footBlock).isAir()) {
                        ++solidBlocks;
                    }

                    ++totalBlocks;
                }
            }

            if (totalBlocks == 1) {
                return solidBlocks == 1;
            } else if (totalBlocks == 2) {
                return solidBlocks >= 1;
            } else if (totalBlocks == 4) {
                return solidBlocks >= 2;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean isFootBlockAlmostMined(Set<BlockPos> playerFootBlocks) {
        if (this.mc.player != null && this.mc.world != null) {
            SilentMine silentMine = (SilentMine)Modules.get().get(SilentMine.class);
            BlockPos rebreakPos = null;
            BlockPos delayedPos = null;
            if (silentMine != null && silentMine.isActive()) {
                rebreakPos = silentMine.getRebreakBlockPos();
                delayedPos = silentMine.getDelayedDestroyBlockPos();
            }

            Iterator var5 = playerFootBlocks.iterator();

            BlockPos feet;
            do {
                if (!var5.hasNext()) {
                    return false;
                }

                feet = (BlockPos)var5.next();
                BlockState bs = this.mc.world.getBlockState(feet);
                if (bs.isLiquid() || bs.isAir()) {
                    return true;
                }

                if (feet.equals(rebreakPos)) {
                    return true;
                }
            } while(!feet.equals(delayedPos));

            return true;
        } else {
            return false;
        }
    }

    private boolean isLowHealth(double threshold) {
        if (this.mc.player == null) {
            return false;
        } else {
            return (double)this.mc.player.getHealth() < threshold;
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if ((Boolean)this.render.get()) {
            this.draw(event);
        }

    }

    private void draw(Render3DEvent event) {
        long currentTime = System.currentTimeMillis();
        Iterator var4 = this.renderLastPlacedBlock.entrySet().iterator();

        Entry entry;
        double time;
        double timeCompletion;
        Color fadedSideColor;
        Color fadedLineColor;
        while(var4.hasNext()) {
            entry = (Entry)var4.next();
            if (!((double)(currentTime - (Long)entry.getValue()) > (Double)this.fadeTime.get() * 1000.0D)) {
                time = (double)(currentTime - (Long)entry.getValue()) / 1000.0D;
                timeCompletion = time / (Double)this.fadeTime.get();
                fadedSideColor = ((SettingColor)this.sideColor.get()).copy().a((int)((double)((SettingColor)this.sideColor.get()).a * (1.0D - timeCompletion)));
                fadedLineColor = ((SettingColor)this.lineColor.get()).copy().a((int)((double)((SettingColor)this.lineColor.get()).a * (1.0D - timeCompletion)));
                event.renderer.box((BlockPos)((BlockPos)entry.getKey()), fadedSideColor, fadedLineColor, (ShapeMode)this.shapeMode.get(), 0);
            }
        }

        var4 = this.renderLastSkippedBlock.entrySet().iterator();

        while(var4.hasNext()) {
            entry = (Entry)var4.next();
            if (!((double)(currentTime - (Long)entry.getValue()) > (Double)this.fadeTime.get() * 1000.0D)) {
                time = (double)(currentTime - (Long)entry.getValue()) / 1000.0D;
                timeCompletion = time / (Double)this.fadeTime.get();
                fadedSideColor = ((SettingColor)this.skippedSideColor.get()).copy().a((int)((double)((SettingColor)this.skippedSideColor.get()).a * (1.0D - timeCompletion)));
                fadedLineColor = ((SettingColor)this.skippedLineColor.get()).copy().a((int)((double)((SettingColor)this.skippedLineColor.get()).a * (1.0D - timeCompletion)));
                event.renderer.box((BlockPos)((BlockPos)entry.getKey()), fadedSideColor, fadedLineColor, (ShapeMode)this.shapeMode.get(), 0);
            }
        }

    }

    public static enum SelfTrapMode {
        None,
        Smart,
        Always;

        // $FF: synthetic method
        private static Surround.SelfTrapMode[] $values() {
            return new Surround.SelfTrapMode[]{None, Smart, Always};
        }
    }

    public static enum ExtendMode {
        None,
        Smart,
        Always;

        // $FF: synthetic method
        private static Surround.ExtendMode[] $values() {
            return new Surround.ExtendMode[]{None, Smart, Always};
        }
    }

    public static enum CrawlExtendMode {
        None,
        Smart;

        // $FF: synthetic method
        private static Surround.CrawlExtendMode[] $values() {
            return new Surround.CrawlExtendMode[]{None, Smart};
        }
    }
}
