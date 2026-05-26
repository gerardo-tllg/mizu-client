package meteordevelopment.meteorclient.systems.modules.combat;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import meteordevelopment.meteorclient.events.meteor.SilentMineFinishedEvent;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.autocrystal.AutoCrystal;
import meteordevelopment.meteorclient.systems.modules.player.SilentMine;
import meteordevelopment.meteorclient.systems.modules.render.BreakIndicators;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1657;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import org.joml.Vector3d;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/AutoMine.class */
public class AutoMine extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgRender;
    private final double INVALID_SCORE = -1000.0d;
    private final Setting<Double> range;
    private final Setting<Boolean> repositionIfNotAdjacent;
    private final Setting<SortPriority> targetPriority;
    private final Setting<Boolean> ignoreNakeds;
    private final Setting<ExtendBreakMode> extendBreakMode;
    private final Setting<AntiSwimMode> antiSwim;
    private final Setting<AntiSurroundMode> antiSurroundMode;
    private final Setting<Boolean> antiSurroundInnerSnap;
    private final Setting<Boolean> antiSurroundOuterSnap;
    private final Setting<Double> antiSurroundOuterCooldown;
    private final Setting<Boolean> breakIndicatorsSync;
    private final Setting<Boolean> breakIndicatorsSyncOnlyFriends;
    private final Setting<Double> breakIndicatorSyncPenalty;
    private final Setting<Boolean> renderDebugScores;
    private SilentMine silentMine;
    private class_1657 targetPlayer;
    private CityBlock target1;
    private CityBlock target2;
    private class_2338 ignorePos;
    private long lastOuterPlaceTime;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/AutoMine$AntiSurroundMode.class */
    private enum AntiSurroundMode {
        None,
        Inner,
        Outer,
        Auto
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/AutoMine$AntiSwimMode.class */
    private enum AntiSwimMode {
        None,
        Always,
        OnMine,
        OnMineAndSwim
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/AutoMine$CheckPosType.class */
    public enum CheckPosType {
        Feet,
        Surround,
        Extend,
        FacePlace,
        Head,
        Below
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/AutoMine$ExtendBreakMode.class */
    private enum ExtendBreakMode {
        None,
        Long,
        Corner
    }

    public AutoMine() {
        super(Categories.Combat, "auto-mine", "Automatically mines blocks. Requires SilentMine to work.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgRender = this.settings.createGroup("Render");
        this.INVALID_SCORE = -1000.0d;
        this.range = this.sgGeneral.add(new DoubleSetting.Builder().name("range").description("Max range to target").defaultValue(6.5d).min(0.0d).sliderMax(7.0d).build());
        this.repositionIfNotAdjacent = this.sgGeneral.add(new BoolSetting.Builder().name("reposition-if-not-adjacent").description("Cancels mining and repositions if the target block isn't adjacent to or touching the target player.").defaultValue(true).build());
        this.targetPriority = this.sgGeneral.add(new EnumSetting.Builder().name("target-priority").description("How to choose the target").defaultValue(SortPriority.ClosestAngle).build());
        this.ignoreNakeds = this.sgGeneral.add(new BoolSetting.Builder().name("ignore-nakeds").description("Ignore players with no items.").defaultValue(true).build());
        this.extendBreakMode = this.sgGeneral.add(new EnumSetting.Builder().name("extend-break-mode").description("How to mine outside of their surround to place crystals better").defaultValue(ExtendBreakMode.None).build());
        this.antiSwim = this.sgGeneral.add(new EnumSetting.Builder().name("anti-swim-mode").description("Starts mining your head block when the enemy starts mining your feet").defaultValue(AntiSwimMode.OnMineAndSwim).build());
        this.antiSurroundMode = this.sgGeneral.add(new EnumSetting.Builder().name("anti-surround-mode").description("Places crystals in places to prevent surround").defaultValue(AntiSurroundMode.Auto).build());
        this.antiSurroundInnerSnap = this.sgGeneral.add(new BoolSetting.Builder().name("anti-surround-inner-snap").description("Instantly snaps the camera when it needs to for inner place").defaultValue(true).visible(() -> {
            return this.antiSurroundMode.get() == AntiSurroundMode.Auto || this.antiSurroundMode.get() == AntiSurroundMode.Inner;
        }).build());
        this.antiSurroundOuterSnap = this.sgGeneral.add(new BoolSetting.Builder().name("anti-surround-outer-snap").description("Instantly snaps the camera when it needs to for outer place").defaultValue(true).visible(() -> {
            return this.antiSurroundMode.get() == AntiSurroundMode.Auto || this.antiSurroundMode.get() == AntiSurroundMode.Outer;
        }).build());
        this.antiSurroundOuterCooldown = this.sgGeneral.add(new DoubleSetting.Builder().name("anti-surround-outer-cooldown").description("Time to wait between placing crystals").defaultValue(0.1d).min(0.0d).sliderMax(1.0d).visible(() -> {
            return this.antiSurroundMode.get() == AntiSurroundMode.Auto || this.antiSurroundMode.get() == AntiSurroundMode.Outer;
        }).build());
        this.breakIndicatorsSync = this.sgGeneral.add(new BoolSetting.Builder().name("break-indicators-sync").description("Syncs auto-mine scoring with break indicators. Basically leads to quad mine :)").defaultValue(true).build());
        this.breakIndicatorsSyncOnlyFriends = this.sgGeneral.add(new BoolSetting.Builder().name("break-indicators-sync-only-friends").description("Only penalizes blocks friends are mining").defaultValue(false).build());
        this.breakIndicatorSyncPenalty = this.sgGeneral.add(new DoubleSetting.Builder().name("break-indicators-sync-penalty").description("Amount to penalize block for being broken by someone else").defaultValue(8.5d).min(0.0d).sliderMax(25.0d).visible(() -> {
            return this.breakIndicatorsSync.get().booleanValue();
        }).build());
        this.renderDebugScores = this.sgRender.add(new BoolSetting.Builder().name("render-debug-scores").description("Renders scores and their blocks.").defaultValue(false).build());
        this.silentMine = null;
        this.targetPlayer = null;
        this.target1 = null;
        this.target2 = null;
        this.ignorePos = null;
        this.lastOuterPlaceTime = 0L;
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        super.onActivate();
        if (this.silentMine == null) {
            this.silentMine = (SilentMine) Modules.get().get(SilentMine.class);
        }
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onDeactivate() {
        if (this.silentMine != null) {
            this.silentMine.cancelBreaking();
        }
        this.target1 = null;
        this.target2 = null;
        this.targetPlayer = null;
        super.onDeactivate();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.silentMine == null) {
            this.silentMine = (SilentMine) Modules.get().get(SilentMine.class);
        }
        if (this.target1 != null && !isValidTargetBlock(this.target1.blockPos)) {
            this.target1 = null;
        }
        if (this.target2 != null && !isValidTargetBlock(this.target2.blockPos)) {
            this.target2 = null;
        }
        if (this.targetPlayer != null && ((this.target1 != null || this.target2 != null) && this.repositionIfNotAdjacent.get().booleanValue())) {
            class_2338 currentTargetPos = this.target1 != null ? this.target1.blockPos : this.target2.blockPos;
            if (!isBlockAdjacentOrInsidePlayer(this.targetPlayer, currentTargetPos)) {
                this.silentMine.cancelBreaking();
                this.target1 = null;
                this.target2 = null;
            }
        }
        update();
    }

    private boolean isValidTargetBlock(class_2338 blockPos) {
        if (blockPos == null) {
            return false;
        }
        class_2680 state = this.mc.field_1687.method_8320(blockPos);
        boolean isPosGoodRebreak = this.silentMine.canRebreakRebreakBlock() && blockPos.equals(this.silentMine.getRebreakBlockPos());
        return (!state.method_26215() || isPosGoodRebreak) && this.targetPlayer != null && this.targetPlayer.method_5805() && !this.targetPlayer.method_29504() && isStillAroundTarget(blockPos);
    }

    private boolean isBlockAdjacentOrInsidePlayer(class_1657 player, class_2338 blockPos) {
        class_2338 playerPos = player.method_24515();
        if (blockPos.equals(playerPos) || blockPos.equals(playerPos.method_10084()) || blockPos.equals(playerPos.method_10074())) {
            return true;
        }
        for (class_2350 dir : class_2350.class_2353.field_11062) {
            if (blockPos.equals(playerPos.method_10093(dir)) || blockPos.equals(playerPos.method_10084().method_10093(dir))) {
                return true;
            }
        }
        class_238 playerBox = player.method_5829().method_1002(0.01d, 0.01d, 0.01d);
        class_238 blockBox = new class_238(blockPos);
        return playerBox.method_994(blockBox);
    }

    @EventHandler
    private void onSilentMineFinished(SilentMineFinishedEvent.Pre event) {
        if (this.targetPlayer == null || this.antiSurroundMode.get() == AntiSurroundMode.None) {
            return;
        }
        if (this.antiSurroundMode.get() == AntiSurroundMode.Auto || this.antiSurroundMode.get() == AntiSurroundMode.Outer) {
            for (class_2350 dir : class_2350.field_11041) {
                class_2338 playerSurroundBlock = this.targetPlayer.method_24515().method_10093(dir);
                if (event.getBlockPos().equals(playerSurroundBlock)) {
                    class_238 checkBox = class_238.method_30048(playerSurroundBlock.method_46558(), 2.5d, 3.0d, 2.5d);
                    class_238 blockHitbox = new class_238(playerSurroundBlock);
                    boolean outerSpeedCheck = ((double) (System.currentTimeMillis() - this.lastOuterPlaceTime)) > this.antiSurroundOuterCooldown.get().doubleValue() * 1000.0d;
                    if (!outerSpeedCheck) {
                        return;
                    }
                    for (class_2338 blockPos : BlockUtils.iterate(checkBox)) {
                        if (this.mc.field_1687.method_22347(blockPos)) {
                            class_2680 downState = this.mc.field_1687.method_8320(blockPos.method_10074());
                            if (downState.method_27852(class_2246.field_10540) || downState.method_27852(class_2246.field_9987)) {
                                class_238 crystalPlaceHitbox = new class_238(blockPos.method_10263(), blockPos.method_10264(), blockPos.method_10260(), blockPos.method_10263() + 1, blockPos.method_10264() + 2, blockPos.method_10260() + 1);
                                if (EntityUtils.intersectsWithEntity(crystalPlaceHitbox, entity -> {
                                    return !entity.method_7325();
                                })) {
                                    continue;
                                } else {
                                    class_243 crystalPos = new class_243(((double) blockPos.method_10263()) + 0.5d, blockPos.method_10264(), ((double) blockPos.method_10260()) + 0.5d);
                                    class_238 crystalHitbox = new class_238(crystalPos.field_1352 - 1.0d, crystalPos.field_1351, crystalPos.field_1350 - 1.0d, crystalPos.field_1352 + 1.0d, crystalPos.field_1351 + 2.0d, crystalPos.field_1350 + 1.0d);
                                    if (crystalHitbox.method_994(blockHitbox)) {
                                        ((AutoCrystal) Modules.get().get(AutoCrystal.class)).preplaceCrystal(blockPos, this.antiSurroundOuterSnap.get().booleanValue());
                                        this.lastOuterPlaceTime = System.currentTimeMillis();
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (this.antiSurroundMode.get() == AntiSurroundMode.Auto || this.antiSurroundMode.get() == AntiSurroundMode.Inner) {
            for (class_2350 dir2 : class_2350.field_11041) {
                class_2338 playerSurroundBlock2 = this.targetPlayer.method_24515().method_10093(dir2);
                if (playerSurroundBlock2.equals(event.getBlockPos())) {
                    ((AutoCrystal) Modules.get().get(AutoCrystal.class)).preplaceCrystal(playerSurroundBlock2, this.antiSurroundInnerSnap.get().booleanValue());
                }
            }
        }
    }

    private boolean hasAirAroundTarget() {
        if (this.targetPlayer == null) {
            return false;
        }
        class_2338 rebreakingPos = this.silentMine.getRebreakBlockPos();
        for (class_2350 dir : class_2350.class_2353.field_11062) {
            class_2338 surroundPos = this.targetPlayer.method_24515().method_10093(dir);
            if (this.mc.field_1687.method_8320(surroundPos).method_26215() || surroundPos.equals(rebreakingPos)) {
                return true;
            }
        }
        return false;
    }

    private boolean isRebreakingTargetSurround() {
        class_2338 rebreakPos;
        if (this.targetPlayer == null || (rebreakPos = this.silentMine.getRebreakBlockPos()) == null) {
            return false;
        }
        if (rebreakPos.equals(this.targetPlayer.method_24515())) {
            return true;
        }
        for (class_2350 dir : class_2350.class_2353.field_11062) {
            if (rebreakPos.equals(this.targetPlayer.method_24515().method_10093(dir))) {
                return true;
            }
        }
        return false;
    }

    private boolean hasOpeningAroundTarget() {
        if (this.targetPlayer == null) {
            return false;
        }
        class_238 boundingBox = this.targetPlayer.method_5829().method_1002(0.01d, 0.1d, 0.01d);
        double feetY = this.targetPlayer.method_23318();
        class_238 feetBox = new class_238(boundingBox.field_1323, feetY, boundingBox.field_1321, boundingBox.field_1320, feetY + 0.1d, boundingBox.field_1324);
        int blockCount = (int) class_2338.method_29715(feetBox).count();
        if (blockCount > 1 || isPlayerAtBlockCorner(this.targetPlayer)) {
            return false;
        }
        class_2338 rebreakPos = this.silentMine.getRebreakBlockPos();
        boolean hasRebreak = (rebreakPos == null || this.silentMine.canRebreakRebreakBlock()) ? false : true;
        boolean isPhased = false;
        Iterator it = class_2338.method_10094((int) Math.floor(boundingBox.field_1323), (int) Math.floor(boundingBox.field_1322), (int) Math.floor(boundingBox.field_1321), (int) Math.floor(boundingBox.field_1320), (int) Math.floor(boundingBox.field_1322 + 1.6d), (int) Math.floor(boundingBox.field_1324)).iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            class_2338 pos = (class_2338) it.next();
            class_2680 state = this.mc.field_1687.method_8320(pos);
            if (!state.method_26215() && !state.method_26204().equals(class_2246.field_10382) && !state.method_26204().equals(class_2246.field_10164)) {
                isPhased = true;
                break;
            }
        }
        if (isPhased) {
            return false;
        }
        class_2338 playerPos = this.targetPlayer.method_24515();
        for (class_2350 dir : class_2350.class_2353.field_11062) {
            class_2338 surroundPos = playerPos.method_10093(dir);
            if (!this.mc.field_1687.method_8320(surroundPos).method_26215()) {
                if (hasRebreak && surroundPos.equals(rebreakPos)) {
                    return true;
                }
            } else {
                return true;
            }
        }
        return false;
    }

    private boolean isPlayerAtBlockCorner(class_1657 player) {
        double x = player.method_23317();
        double z = player.method_23321();
        double xFraction = Math.abs(x - Math.floor(x));
        double zFraction = Math.abs(z - Math.floor(z));
        return (xFraction < 0.3d || xFraction > 0.7d) && (zFraction < 0.3d || zFraction > 0.7d);
    }

    private void update() {
        if (this.silentMine == null) {
            this.silentMine = (SilentMine) Modules.get().get(SilentMine.class);
        }
        class_2338 selfHeadPos = this.mc.field_1724.method_24515().method_10084();
        class_2680 selfHeadBlock = this.mc.field_1687.method_8320(selfHeadPos);
        class_2680 selfFeetBlock = this.mc.field_1687.method_8320(this.mc.field_1724.method_24515());
        boolean isSwimming = this.mc.field_1724.method_5869() || this.mc.field_1724.method_5771();
        boolean shouldBreakSelfHead = BlockUtils.canBreak(selfHeadPos, selfHeadBlock) && (selfHeadBlock.method_27852(class_2246.field_10540) || selfHeadBlock.method_27852(class_2246.field_22423));
        boolean prioHead = false;
        if (this.antiSwim.get() == AntiSwimMode.Always && shouldBreakSelfHead) {
            this.silentMine.silentBreakBlock(selfHeadPos, class_2350.field_11036, 10.0d);
            prioHead = true;
        }
        if (this.antiSwim.get() == AntiSwimMode.OnMineAndSwim && this.mc.field_1724.method_20448() && shouldBreakSelfHead) {
            this.silentMine.silentBreakBlock(selfHeadPos, class_2350.field_11036, 30.0d);
            prioHead = true;
        }
        if ((this.antiSwim.get() == AntiSwimMode.OnMine || this.antiSwim.get() == AntiSwimMode.OnMineAndSwim) && ((BreakIndicators) Modules.get().get(BreakIndicators.class)).isBlockBeingBroken(this.mc.field_1724.method_24515()) && shouldBreakSelfHead) {
            this.silentMine.silentBreakBlock(selfHeadPos, class_2350.field_11036, 20.0d);
            prioHead = true;
        }
        this.targetPlayer = TargetUtils.get(entity -> {
            if (entity.equals(this.mc.field_1724) || entity.equals(this.mc.field_1719) || !(entity instanceof class_1657)) {
                return false;
            }
            class_1657 player = (class_1657) entity;
            if (!player.method_5805() || player.method_29504() || player.method_68878() || !Friends.get().shouldAttack(player) || entity.method_19538().method_1022(this.mc.field_1724.method_33571()) > this.range.get().doubleValue()) {
                return false;
            }
            return (this.ignoreNakeds.get().booleanValue() && player.method_31548().meteor$getArmor().stream().allMatch(itemStack -> {
                return itemStack.method_7960();
            })) ? false : true;
        }, this.targetPriority.get());
        if (this.targetPlayer == null) {
            if (prioHead) {
                return;
            } else {
                return;
            }
        }
        if (prioHead) {
            return;
        }
        if (this.silentMine.hasDelayedDestroy() && selfHeadBlock.method_27852(class_2246.field_10540) && selfFeetBlock.method_26215() && this.silentMine.getRebreakBlockPos() != null && this.silentMine.getRebreakBlockPos().equals(selfHeadPos)) {
            return;
        }
        if (hasOpeningAroundTarget()) {
            if (isSwimming && shouldBreakSelfHead && this.silentMine.canRebreakRebreakBlock()) {
                this.silentMine.silentBreakBlock(selfHeadPos, class_2350.field_11036, 20.0d);
                return;
            }
            return;
        }
        if (isSwimming && shouldBreakSelfHead && (this.silentMine.canRebreakRebreakBlock() || !this.silentMine.hasRebreakBlock())) {
            this.silentMine.silentBreakBlock(selfHeadPos, class_2350.field_11036, 20.0d);
            if (this.silentMine.getRebreakBlockPos() != null && this.silentMine.getRebreakBlockPos().equals(selfHeadPos)) {
                return;
            }
        }
        findTargetBlocks();
        if (!this.silentMine.hasDelayedDestroy() && this.target1 != null) {
            this.silentMine.silentBreakBlock(this.target1.blockPos, class_2350.field_11036, 10.0d);
        }
        if ((this.silentMine.canRebreakRebreakBlock() || !this.silentMine.hasRebreakBlock()) && this.target2 != null) {
            this.silentMine.silentBreakBlock(this.target2.blockPos, class_2350.field_11036, 10.0d);
        }
    }

    private void findTargetBlocks() {
        this.target1 = findCityBlock(null);
        this.ignorePos = this.target1 != null ? this.target1.blockPos : null;
        this.target2 = findCityBlock(this.target1 != null ? this.target1.blockPos : null);
    }

    private boolean isStillAroundTarget(class_2338 blockPos) {
        if (this.targetPlayer == null) {
            return false;
        }
        if (blockPos.equals(this.targetPlayer.method_24515())) {
            return true;
        }
        for (class_2350 dir : class_2350.class_2353.field_11062) {
            if (blockPos.equals(this.targetPlayer.method_24515().method_10093(dir))) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidSurroundBlock(class_2338 blockPos) {
        if (blockPos == null) {
            return false;
        }
        class_2680 state = this.mc.field_1687.method_8320(blockPos);
        return !state.method_26215() && BlockUtils.canBreak(blockPos, state) && this.silentMine.inBreakRange(blockPos);
    }

    private CityBlock findCityBlock(class_2338 exclude) {
        if (this.targetPlayer == null) {
            return null;
        }
        boolean set = false;
        CityBlock bestBlock = new CityBlock(this);
        Set<CheckPos> checkPos = new HashSet<>();
        class_238 boundingBox = this.targetPlayer.method_5829().method_1002(0.01d, 0.1d, 0.01d);
        double feetY = this.targetPlayer.method_23318();
        class_238 feetBox = new class_238(boundingBox.field_1323, feetY, boundingBox.field_1321, boundingBox.field_1320, feetY + 0.1d, boundingBox.field_1324);
        boolean inBedrock = class_2338.method_29715(feetBox).anyMatch(blockPos -> {
            return this.mc.field_1687.method_8320(blockPos).method_26204() == class_2246.field_9987;
        });
        if (inBedrock) {
            addBedrockCaseCheckPositions(checkPos);
        } else {
            addNormalCaseCheckPositions(checkPos);
        }
        for (CheckPos pos : checkPos) {
            class_2338 blockPos2 = pos.blockPos;
            if (!blockPos2.equals(exclude)) {
                class_2680 block = this.mc.field_1687.method_8320(blockPos2);
                boolean isPosGoodRebreak = this.silentMine.canRebreakRebreakBlock() && blockPos2.equals(this.silentMine.getRebreakBlockPos());
                if (!block.method_26215() || isPosGoodRebreak) {
                    if (BlockUtils.canBreak(blockPos2, block) || isPosGoodRebreak) {
                        if (this.silentMine.inBreakRange(blockPos2)) {
                            double score = inBedrock ? scoreBedrockCityBlock(pos) : scoreNormalCityBlock(pos);
                            if (score != -1000.0d) {
                                double score2 = isPosGoodRebreak ? score + 40.0d : score - getScorePenaltyForSync(pos.blockPos);
                                if (score2 > bestBlock.score) {
                                    bestBlock.score = score2;
                                    bestBlock.blockPos = blockPos2;
                                    bestBlock.isFeetBlock = isBlockInFeet(blockPos2);
                                    set = true;
                                }
                            }
                        }
                    }
                }
            }
        }
        if (set) {
            return bestBlock;
        }
        return null;
    }

    private void addNormalCaseCheckPositions(Set<CheckPos> checkPos) {
        class_238 boundingBox = this.targetPlayer.method_5829().method_1002(0.01d, 0.1d, 0.01d);
        double feetY = this.targetPlayer.method_23318();
        class_238 feetBox = new class_238(boundingBox.field_1323, feetY, boundingBox.field_1321, boundingBox.field_1320, feetY + 0.1d, boundingBox.field_1324);
        for (class_2338 pos : BlockUtils.iterate(feetBox)) {
            checkPos.add(new CheckPos(this, pos, CheckPosType.Feet));
        }
        for (class_2338 pos2 : BlockUtils.iterate(feetBox)) {
            Iterator it = class_2350.class_2353.field_11062.iterator();
            while (it.hasNext()) {
                checkPos.add(new CheckPos(this, pos2.method_10093((class_2350) it.next()), CheckPosType.Surround));
            }
        }
        checkPos.add(new CheckPos(this, this.targetPlayer.method_24515(), CheckPosType.Feet));
        boolean inMultipleBlocks = class_2338.method_29715(feetBox).count() > 1;
        if (!inMultipleBlocks) {
            for (class_2350 dir : class_2350.class_2353.field_11062) {
                switch (this.extendBreakMode.get()) {
                    case Long:
                        checkPos.add(new CheckPos(this, this.targetPlayer.method_24515().method_10079(dir, 2), CheckPosType.Extend));
                        break;
                    case Corner:
                        class_2350 perpDir = getCornerPerpDir(dir);
                        checkPos.add(new CheckPos(this, this.targetPlayer.method_24515().method_10093(dir).method_10093(perpDir), CheckPosType.Extend));
                        break;
                }
            }
        }
    }

    /* JADX INFO: renamed from: meteordevelopment.meteorclient.systems.modules.combat.AutoMine$1, reason: invalid class name */
    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/AutoMine$1.class */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$net$minecraft$util$math$Direction = new int[class_2350.values().length];

        static {
            try {
                $SwitchMap$net$minecraft$util$math$Direction[class_2350.field_11043.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$net$minecraft$util$math$Direction[class_2350.field_11035.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$net$minecraft$util$math$Direction[class_2350.field_11034.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$net$minecraft$util$math$Direction[class_2350.field_11039.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    private class_2350 getCornerPerpDir(class_2350 dir) {
        switch (AnonymousClass1.$SwitchMap$net$minecraft$util$math$Direction[dir.ordinal()]) {
            case 1:
                return class_2350.field_11034;
            case 2:
                return class_2350.field_11039;
            case 3:
                return class_2350.field_11043;
            case 4:
                return class_2350.field_11035;
            default:
                return null;
        }
    }

    private void addBedrockCaseCheckPositions(Set<CheckPos> checkPos) {
        class_238 boundingBox = this.targetPlayer.method_5829().method_1002(0.01d, 0.1d, 0.01d);
        double feetY = this.targetPlayer.method_23318();
        class_238 feetBox = new class_238(boundingBox.field_1323, feetY, boundingBox.field_1321, boundingBox.field_1320, feetY + 0.1d, boundingBox.field_1324);
        boolean canFallDown = class_2338.method_29715(feetBox).allMatch(blockPos -> {
            return this.mc.field_1687.method_8320(blockPos.method_10074()).method_26204() != class_2246.field_9987;
        });
        boolean canBeHitUp = class_2338.method_29715(feetBox).allMatch(blockPos2 -> {
            return this.mc.field_1687.method_8320(blockPos2.method_10086(2)).method_26204() != class_2246.field_9987;
        });
        for (class_2338 pos : BlockUtils.iterate(feetBox)) {
            if (canFallDown) {
                checkPos.add(new CheckPos(this, pos.method_10074(), CheckPosType.Below));
            }
            if (canBeHitUp) {
                checkPos.add(new CheckPos(this, pos.method_10086(2), CheckPosType.Head));
            }
            checkPos.add(new CheckPos(this, pos.method_10084(), CheckPosType.FacePlace));
            for (class_2350 dir : class_2350.class_2353.field_11062) {
                checkPos.add(new CheckPos(this, pos.method_10084().method_10093(dir), CheckPosType.FacePlace));
            }
            checkPos.add(new CheckPos(this, pos, CheckPosType.Surround));
            for (class_2350 dir2 : class_2350.class_2353.field_11062) {
                checkPos.add(new CheckPos(this, pos.method_10093(dir2), CheckPosType.Surround));
            }
        }
    }

    private double scoreNormalCityBlock(CheckPos pos) {
        class_2338 blockPos = pos.blockPos;
        double score = 0.0d;
        class_2680 block = this.mc.field_1687.method_8320(blockPos);
        if (blockPos.equals(this.targetPlayer.method_24515())) {
            class_2680 headBlock = this.mc.field_1687.method_8320(blockPos.method_10084());
            if (headBlock.method_26204().equals(class_2246.field_10540)) {
                score = 0.0d + 100.0d;
            } else {
                if (block.method_26204() == class_2246.field_10343) {
                    return -1000.0d;
                }
                score = 0.0d + 50.0d;
            }
        } else {
            class_2680 selfHeadState = this.mc.field_1687.method_8320(this.mc.field_1724.method_24515().method_10084());
            if (blockPos.equals(this.mc.field_1724.method_24515()) && (selfHeadState.method_26204().equals(class_2246.field_10540) || selfHeadState.method_26204().equals(class_2246.field_9987))) {
                return -1000.0d;
            }
            if (pos.type == CheckPosType.Surround) {
                score = 0.0d + 3.0d;
                boolean isPosAntiSurround = false;
                Iterator it = class_2350.class_2353.field_11062.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    class_2350 dir = (class_2350) it.next();
                    if (this.targetPlayer.method_24515().method_10093(dir).equals(blockPos)) {
                        class_2338 antiSurroundBlockPos = this.targetPlayer.method_24515().method_10079(dir, 2);
                        if (getBlockStateIgnore(antiSurroundBlockPos).method_26215() && isCrystalBlock(antiSurroundBlockPos.method_10074())) {
                            isPosAntiSurround = true;
                            break;
                        }
                        class_2350 perpDir = getCornerPerpDir(dir);
                        class_2338 antiSurroundCornerBlockPos = this.targetPlayer.method_24515().method_10093(dir).method_10093(perpDir);
                        if (getBlockStateIgnore(antiSurroundCornerBlockPos).method_26215() && isCrystalBlock(antiSurroundCornerBlockPos.method_10074())) {
                            isPosAntiSurround = true;
                            break;
                        }
                    }
                }
                if (isPosAntiSurround) {
                    score += 25.0d;
                }
            }
            if (pos.type == CheckPosType.Extend) {
                score += 20.0d;
            }
        }
        double d = this.targetPlayer.method_19538().method_1022(class_243.method_24953(blockPos));
        return score + (10.0d / d);
    }

    private double scoreBedrockCityBlock(CheckPos pos) {
        class_2338 blockPos = pos.blockPos;
        double score = 0.0d;
        if (blockPos.method_10264() == this.targetPlayer.method_31478() + 2 || blockPos.method_10264() == this.targetPlayer.method_31478() - 1) {
            score = 0.0d + 10.0d;
        }
        class_238 boundingBox = this.targetPlayer.method_5829().method_1002(0.01d, 0.1d, 0.01d);
        double feetY = this.targetPlayer.method_23318();
        class_238 feetBox = new class_238(boundingBox.field_1323, feetY, boundingBox.field_1321, boundingBox.field_1320, feetY + 0.1d, boundingBox.field_1324);
        if (class_2338.method_29715(feetBox).count() == 1) {
            boolean canMineFaceBlock = this.mc.field_1687.method_8320(this.targetPlayer.method_24515().method_10084()).method_26204() != class_2246.field_9987;
            if (canMineFaceBlock) {
                if (blockPos.equals(this.targetPlayer.method_24515().method_10084())) {
                    score += 20.0d;
                } else {
                    boolean isSelfTrapBlock = false;
                    class_2350[] class_2350VarArr = class_2350.field_11041;
                    int length = class_2350VarArr.length;
                    int i = 0;
                    while (true) {
                        if (i >= length) {
                            break;
                        }
                        class_2350 dir = class_2350VarArr[i];
                        if (!this.targetPlayer.method_24515().method_10084().method_10093(dir).equals(blockPos)) {
                            i++;
                        } else {
                            isSelfTrapBlock = true;
                            break;
                        }
                    }
                    if (isSelfTrapBlock) {
                        score += 7.5d;
                    }
                }
            }
        }
        double d = this.targetPlayer.method_19538().method_1022(class_243.method_24953(blockPos));
        return score + (10.0d / d);
    }

    private boolean isBlockInFeet(class_2338 blockPos) {
        class_238 boundingBox = this.targetPlayer.method_5829().method_1002(0.01d, 0.1d, 0.01d);
        double feetY = this.targetPlayer.method_23318();
        class_238 feetBox = new class_238(boundingBox.field_1323, feetY, boundingBox.field_1321, boundingBox.field_1320, feetY + 0.1d, boundingBox.field_1324);
        for (class_2338 pos : class_2338.method_10094((int) Math.floor(feetBox.field_1323), (int) Math.floor(feetBox.field_1322), (int) Math.floor(feetBox.field_1321), (int) Math.floor(feetBox.field_1320), (int) Math.floor(feetBox.field_1325), (int) Math.floor(feetBox.field_1324))) {
            if (blockPos.equals(pos)) {
                return true;
            }
        }
        return false;
    }

    private boolean isCrystalBlock(class_2338 blockPos) {
        class_2680 blockState = this.mc.field_1687.method_8320(blockPos);
        return blockState.method_27852(class_2246.field_10540) || blockState.method_27852(class_2246.field_9987);
    }

    public boolean isTargetedPos(class_2338 blockPos) {
        return (this.target1 != null && this.target1.blockPos.equals(blockPos)) || (this.target2 != null && this.target2.blockPos.equals(blockPos));
    }

    private class_2680 getBlockStateIgnore(class_2338 blockPos) {
        return (blockPos == null || blockPos.equals(this.ignorePos)) ? class_2246.field_10124.method_9564() : this.mc.field_1687.method_8320(blockPos);
    }

    private double getScorePenaltyForSync(class_2338 blockPos) {
        BreakIndicators breakIndicators;
        class_1657 doubleminer;
        if (!this.breakIndicatorsSync.get().booleanValue() || (breakIndicators = (BreakIndicators) Modules.get().get(BreakIndicators.class)) == null || !breakIndicators.isActive() || !breakIndicators.isBeingDoublemined(blockPos) || (doubleminer = breakIndicators.getPlayerDoubleminingBlock(blockPos)) == null || doubleminer == this.mc.field_1724) {
            return 0.0d;
        }
        if (!this.breakIndicatorsSyncOnlyFriends.get().booleanValue() || Friends.get().isFriend(doubleminer)) {
            return this.breakIndicatorSyncPenalty.get().doubleValue();
        }
        return 0.0d;
    }

    public boolean isTargetingAnything() {
        return (this.target1 == null && this.target2 == null) ? false : true;
    }

    private void render3d(Render3DEvent event) {
        if (this.targetPlayer == null || !this.renderDebugScores.get().booleanValue()) {
            return;
        }
        double bestScore = 0.0d;
        Set<CheckPos> checkPos = new HashSet<>();
        class_238 boundingBox = this.targetPlayer.method_5829().method_1002(0.01d, 0.1d, 0.01d);
        double feetY = this.targetPlayer.method_23318();
        class_238 feetBox = new class_238(boundingBox.field_1323, feetY, boundingBox.field_1321, boundingBox.field_1320, feetY + 0.1d, boundingBox.field_1324);
        boolean inBedrock = class_2338.method_29715(feetBox).anyMatch(blockPos -> {
            return this.mc.field_1687.method_8320(blockPos).method_26204() == class_2246.field_9987;
        });
        if (inBedrock) {
            addBedrockCaseCheckPositions(checkPos);
        } else {
            addNormalCaseCheckPositions(checkPos);
        }
        for (CheckPos pos : checkPos) {
            class_2338 blockPos2 = pos.blockPos;
            class_2680 block = this.mc.field_1687.method_8320(blockPos2);
            boolean isPosGoodRebreak = this.silentMine.canRebreakRebreakBlock() && blockPos2.equals(this.silentMine.getRebreakBlockPos());
            if (!block.method_26215() || isPosGoodRebreak) {
                if (BlockUtils.canBreak(blockPos2, block) || isPosGoodRebreak) {
                    if (this.silentMine.inBreakRange(blockPos2)) {
                        double score = inBedrock ? scoreBedrockCityBlock(pos) : scoreNormalCityBlock(pos);
                        if (score != -1000.0d) {
                            double score2 = isPosGoodRebreak ? score + 40.0d : score - getScorePenaltyForSync(pos.blockPos);
                            if (score2 > bestScore) {
                                bestScore = score2;
                            }
                        }
                    }
                }
            }
        }
        Color color = Color.RED;
        for (CheckPos pos2 : checkPos) {
            class_2338 blockPos3 = pos2.blockPos;
            class_2680 block2 = this.mc.field_1687.method_8320(blockPos3);
            boolean isPosGoodRebreak2 = this.silentMine.canRebreakRebreakBlock() && blockPos3.equals(this.silentMine.getRebreakBlockPos());
            if (!block2.method_26215() || isPosGoodRebreak2) {
                if (BlockUtils.canBreak(blockPos3, block2) || isPosGoodRebreak2) {
                    if (this.silentMine.inBreakRange(blockPos3)) {
                        double score3 = inBedrock ? scoreBedrockCityBlock(pos2) : scoreNormalCityBlock(pos2);
                        if (score3 != -1000.0d) {
                            double alpha = ((isPosGoodRebreak2 ? score3 + 40.0d : score3 - getScorePenaltyForSync(pos2.blockPos)) / bestScore) / 4.0d;
                            event.renderer.box(blockPos3, color.a((int) (255.0d * alpha)), Color.WHITE, ShapeMode.Sides, 0);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    private void onRender2d(Render2DEvent event) {
        if (this.targetPlayer == null || !this.renderDebugScores.get().booleanValue()) {
            return;
        }
        Vector3d vec3 = new Vector3d();
        Set<CheckPos> checkPos = new HashSet<>();
        class_238 boundingBox = this.targetPlayer.method_5829().method_1002(0.01d, 0.1d, 0.01d);
        double feetY = this.targetPlayer.method_23318();
        class_238 feetBox = new class_238(boundingBox.field_1323, feetY, boundingBox.field_1321, boundingBox.field_1320, feetY + 0.1d, boundingBox.field_1324);
        boolean inBedrock = class_2338.method_29715(feetBox).anyMatch(blockPos -> {
            return this.mc.field_1687.method_8320(blockPos).method_26204() == class_2246.field_9987;
        });
        if (inBedrock) {
            addBedrockCaseCheckPositions(checkPos);
        } else {
            addNormalCaseCheckPositions(checkPos);
        }
        for (CheckPos pos : checkPos) {
            class_2338 blockPos2 = pos.blockPos;
            class_2680 block = this.mc.field_1687.method_8320(blockPos2);
            boolean isPosGoodRebreak = this.silentMine.canRebreakRebreakBlock() && blockPos2.equals(this.silentMine.getRebreakBlockPos());
            if (!block.method_26215() || isPosGoodRebreak) {
                if (BlockUtils.canBreak(blockPos2, block) || isPosGoodRebreak) {
                    if (this.silentMine.inBreakRange(blockPos2)) {
                        double score = inBedrock ? scoreBedrockCityBlock(pos) : scoreNormalCityBlock(pos);
                        if (score != -1000.0d) {
                            double score2 = isPosGoodRebreak ? score + 40.0d : score - getScorePenaltyForSync(pos.blockPos);
                            vec3.set(blockPos2.method_46558().field_1352, blockPos2.method_46558().field_1351, blockPos2.method_46558().field_1350);
                            if (NametagUtils.to2D(vec3, 1.25d)) {
                                NametagUtils.begin(vec3);
                                TextRenderer.get().begin(1.0d, false, true);
                                String text = String.format("%.1f", Double.valueOf(score2));
                                double w = TextRenderer.get().getWidth(text) / 2.0d;
                                TextRenderer.get().render(text, -w, 0.0d, Color.WHITE, true);
                                TextRenderer.get().end();
                                NametagUtils.end();
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) {
            return;
        }
        update();
        render3d(event);
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public String getInfoString() {
        if (this.targetPlayer != null) {
            return EntityUtils.getName(this.targetPlayer);
        }
        return null;
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/AutoMine$CityBlock.class */
    private class CityBlock {
        public class_2338 blockPos;
        public double score;
        public boolean isFeetBlock = false;

        private CityBlock(AutoMine autoMine) {
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/AutoMine$CheckPos.class */
    private class CheckPos {
        public final class_2338 blockPos;
        public final CheckPosType type;

        public CheckPos(AutoMine autoMine, class_2338 blockPos, CheckPosType type) {
            this.blockPos = blockPos;
            this.type = type;
        }

        public int hashCode() {
            return this.blockPos.hashCode();
        }
    }
}
