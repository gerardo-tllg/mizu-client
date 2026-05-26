package meteordevelopment.meteorclient.systems.modules.combat.autocrystal;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.EntityAddedEvent;
import meteordevelopment.meteorclient.events.entity.PlayerDeathEvent;
import meteordevelopment.meteorclient.events.entity.player.RotateEvent;
import meteordevelopment.meteorclient.events.entity.player.SendMovementPacketsEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IBox;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.AutoMine;
import meteordevelopment.meteorclient.systems.modules.combat.PearlPhase;
import meteordevelopment.meteorclient.systems.modules.combat.Surround;
import meteordevelopment.meteorclient.systems.modules.player.SilentMine;
import meteordevelopment.meteorclient.systems.modules.render.BreakIndicators;
import meteordevelopment.meteorclient.utils.CrystalUtils;
import meteordevelopment.meteorclient.utils.entity.DamageUtils;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.fakeplayer.FakePlayerManager;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.misc.Pool;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1511;
import net.minecraft.class_1542;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_2824;
import net.minecraft.class_2879;
import net.minecraft.class_2885;
import net.minecraft.class_3965;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/autocrystal/AutoCrystal.class */
public class AutoCrystal extends Module {
    private final SettingGroup sgGeneral;
    public final SettingGroup sgPlace;
    private final SettingGroup sgFacePlace;
    private final SettingGroup sgBreak;
    private final SettingGroup sgRotate;
    private final SettingGroup sgSwing;
    private final SettingGroup sgRange;
    private final AutoCrystalRenderer renderer;
    private final Setting<Boolean> placeCrystals;
    private final Setting<Boolean> pauseEatPlace;
    private final Setting<Boolean> breakCrystals;
    private final Setting<Boolean> pauseEatBreak;
    private final Setting<Boolean> ignoreNakeds;
    private final Setting<Boolean> setPlayerDead;
    private final Setting<Boolean> predictBlock;
    private final Setting<Double> predictBlockThreshold;
    private final Setting<Boolean> cevHead;
    private final Setting<Boolean> ignoreCalc;
    private final Setting<Double> placeSpeedLimit;
    private final Setting<Double> minPlace;
    private final Setting<Double> maxPlace;
    private final Setting<Boolean> antiSurroundPlace;
    private final Setting<Double> placeDelay;
    private final Setting<Boolean> ignoreItems;
    private final Setting<Boolean> ignoreTrapdoors;
    private final Setting<Boolean> shorelineMode;
    private final Setting<Boolean> facePlace;
    private final Setting<Boolean> facePlaceMissingArmor;
    private final Setting<Integer> facePlaceHealth;
    private final Setting<Integer> facePlaceArmor;
    private final Setting<Keybind> forceFacePlaceKeybind;
    private final Setting<Double> breakSpeedLimit;
    private final Setting<Boolean> packetBreak;
    private final Setting<Double> minBreak;
    private final Setting<Double> maxBreak;
    private final Setting<Double> breakDelay;
    private final Setting<Boolean> rotatePlace;
    private final Setting<Boolean> rotateBreak;
    private final Setting<RotationTiming> rotationTiming;
    private final Setting<SwingMode> breakSwingMode;
    private final Setting<SwingMode> placeSwingMode;
    public final Setting<Double> placeRange;
    private final Setting<Double> breakRange;
    private static final Set<class_2248> TRAPDOOR_BLOCKS = new HashSet<class_2248>() { // from class: meteordevelopment.meteorclient.systems.modules.combat.autocrystal.AutoCrystal.1
        {
            add(class_2246.field_10137);
            add(class_2246.field_10323);
            add(class_2246.field_10486);
            add(class_2246.field_10017);
            add(class_2246.field_10608);
            add(class_2246.field_10246);
            add(class_2246.field_37555);
            add(class_2246.field_42740);
            add(class_2246.field_40285);
            add(class_2246.field_22094);
            add(class_2246.field_22095);
        }
    };
    private class_243 currentTarget;
    private long lastTargetTime;
    private static final long TARGET_EXPIRY_TIME = 400;
    private class_1657 currentTargetPlayer;
    private class_2338 bestPlacePosition;
    private double bestPlaceDamage;
    private long lastTargetSelectionTime;
    private static final long TARGET_SELECTION_COOLDOWN = 300;
    public final List<class_1297> forceBreakCrystals;
    private final Pool<PlacePosition> placePositionPool;
    private final List<PlacePosition> _placePositions;
    private final class_2338.class_2339 mutablePos;
    private final IntSet brokenCrystals;
    private final Map<Integer, Long> crystalBreakDelays;
    private final Map<class_2338, Long> crystalPlaceDelays;
    public final Set<class_2338> cachedValidSpots;
    private long lastPlaceTimeMS;
    private long lastBreakTimeMS;
    private AutoMine autoMine;
    private class_2338 lastChosenPos;
    private class_2338 lastShorelinePos;
    private int placeAttempts;
    private class_2338 lastFurtherPos;
    private long lastHighPriorityRotationTime;
    private static final long HIGH_PRIORITY_ROTATION_COOLDOWN = 100;
    private boolean rotationActive;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/autocrystal/AutoCrystal$SwingMode.class */
    public enum SwingMode {
        Packet,
        Client,
        None
    }

    public boolean isValidSpot(int x, int y, int z) {
        class_2338 eyePos = class_2338.method_49638(this.mc.field_1724.method_33571());
        class_2338 pos = new class_2338(eyePos.method_10263() + x, eyePos.method_10264() + y, eyePos.method_10260() + z);
        return this.cachedValidSpots.contains(pos);
    }

    public AutoCrystal() {
        super(Categories.Combat, "auto-crystal", "Automatically places and attacks crystals.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgPlace = this.settings.createGroup("Place");
        this.sgFacePlace = this.settings.createGroup("Face Place");
        this.sgBreak = this.settings.createGroup("Break");
        this.sgRotate = this.settings.createGroup("Rotate");
        this.sgSwing = this.settings.createGroup("Swing");
        this.sgRange = this.settings.createGroup("Range");
        this.renderer = new AutoCrystalRenderer(this);
        this.placeCrystals = this.sgGeneral.add(new BoolSetting.Builder().name("place").description("Places crystals.").defaultValue(true).build());
        this.pauseEatPlace = this.sgGeneral.add(new BoolSetting.Builder().name("pause-eat-place").description("Pauses placing when eating").defaultValue(true).build());
        this.breakCrystals = this.sgGeneral.add(new BoolSetting.Builder().name("break").description("Breaks crystals.").defaultValue(true).build());
        this.pauseEatBreak = this.sgGeneral.add(new BoolSetting.Builder().name("pause-eat-break").description("Pauses placing when breaking").defaultValue(false).build());
        this.ignoreNakeds = this.sgGeneral.add(new BoolSetting.Builder().name("ignore-nakeds").description("Ignore players with no items.").defaultValue(true).build());
        this.setPlayerDead = this.sgGeneral.add(new BoolSetting.Builder().name("set-player-dead-instantly").description("Tries to not blow up loot by instantly killing the player in the packet they die.").defaultValue(true).build());
        this.predictBlock = this.sgGeneral.add(new BoolSetting.Builder().name("predict-block").description("Predicts block damage").defaultValue(false).build());
        this.predictBlockThreshold = this.sgGeneral.add(new DoubleSetting.Builder().name("predict-block-threshold").description("Block break progress threshold for pre-placing crystals.").defaultValue(95.0d).min(80.0d).max(99.0d).sliderRange(80.0d, 99.0d).visible(() -> {
            return this.predictBlock.get().booleanValue();
        }).build());
        this.cevHead = this.sgGeneral.add(new BoolSetting.Builder().name("cev-head").description("So you wanna be a 1.12 player?").defaultValue(false).build());
        this.ignoreCalc = this.sgGeneral.add(new BoolSetting.Builder().name("Laser").description("It BEAMS people.").defaultValue(true).build());
        this.placeSpeedLimit = this.sgPlace.add(new DoubleSetting.Builder().name("place-speed-limit").description("Maximum number of crystals to place every second.").defaultValue(40.0d).min(0.0d).sliderRange(0.0d, 40.0d).build());
        this.minPlace = this.sgPlace.add(new DoubleSetting.Builder().name("min-place").description("Minimum enemy damage to place.").defaultValue(8.0d).min(0.0d).sliderRange(0.0d, 20.0d).build());
        this.maxPlace = this.sgPlace.add(new DoubleSetting.Builder().name("max-place").description("Max self damage to place.").defaultValue(20.0d).min(0.0d).sliderRange(0.0d, 20.0d).build());
        this.antiSurroundPlace = this.sgPlace.add(new BoolSetting.Builder().name("anti-surround").description("Ignores auto-mine blocks from calculations to place outside of their surround.").defaultValue(true).build());
        this.placeDelay = this.sgPlace.add(new DoubleSetting.Builder().name("place-delay").description("The number of seconds to wait to retry placing a crystal at a position.").defaultValue(0.05d).min(0.0d).sliderMax(0.6d).build());
        this.ignoreItems = this.sgPlace.add(new BoolSetting.Builder().name("ignore-items").description("Places on items. Good for low ping.").defaultValue(true).build());
        this.ignoreTrapdoors = this.sgPlace.add(new BoolSetting.Builder().name("ignore-trapdoors").description("Places crystals even if a trapdoor is in the way.").defaultValue(true).build());
        this.shorelineMode = this.sgPlace.add(new BoolSetting.Builder().name("shoreline-mode").description("Places in the corners of the targets surrounding blocks.").defaultValue(false).build());
        this.facePlace = this.sgFacePlace.add(new BoolSetting.Builder().name("face-place").description("Face places when conditions are met.").defaultValue(true).build());
        this.facePlaceMissingArmor = this.sgFacePlace.add(new BoolSetting.Builder().name("face-place-missing-armor").description("Face places on missing armor").defaultValue(true).build());
        this.facePlaceHealth = this.sgFacePlace.add(new IntSetting.Builder().name("face-place-health").description("Face places when target health is below this value.").defaultValue(8).min(1).max(36).sliderRange(1, 36).visible(() -> {
            return this.facePlace.get().booleanValue();
        }).build());
        this.facePlaceArmor = this.sgFacePlace.add(new IntSetting.Builder().name("face-place-armor").description("Face places when target armor durability percentage is below this value.").defaultValue(20).min(1).max(100).sliderRange(1, 100).visible(() -> {
            return this.facePlace.get().booleanValue();
        }).build());
        this.forceFacePlaceKeybind = this.sgFacePlace.add(new KeybindSetting.Builder().name("force-face-place").description("Keybind to force face place").build());
        this.breakSpeedLimit = this.sgBreak.add(new DoubleSetting.Builder().name("break-speed-limit").description("Maximum number of crystals to break every second.").defaultValue(60.0d).min(0.0d).sliderRange(0.0d, 60.0d).build());
        this.packetBreak = this.sgBreak.add(new BoolSetting.Builder().name("packet-break").description("Breaks when the crystal packet arrives").defaultValue(true).build());
        this.minBreak = this.sgBreak.add(new DoubleSetting.Builder().name("min-break").description("Minimum enemy damage to break.").defaultValue(3.0d).min(0.0d).sliderRange(0.0d, 20.0d).build());
        this.maxBreak = this.sgBreak.add(new DoubleSetting.Builder().name("max-break").description("Max self damage to break.").defaultValue(20.0d).min(0.0d).sliderRange(0.0d, 20.0d).build());
        this.breakDelay = this.sgBreak.add(new DoubleSetting.Builder().name("break-delay").description("The number of seconds to wait to retry breaking a crystal.").defaultValue(0.05d).min(0.0d).sliderMax(0.6d).build());
        this.rotatePlace = this.sgRotate.add(new BoolSetting.Builder().name("rotate-place").description("Rotates server-side towards the crystals when placed.").defaultValue(false).build());
        this.rotateBreak = this.sgRotate.add(new BoolSetting.Builder().name("rotate-break").description("Rotates server-side towards the crystals when broken.").defaultValue(true).build());
        this.rotationTiming = this.sgRotate.add(new EnumSetting.Builder().name("timing").description("When to rotate to target (Pre = before action, Post = after action).").defaultValue(RotationTiming.Pre).build());
        this.breakSwingMode = this.sgSwing.add(new EnumSetting.Builder().name("break-swing-mode").description("Mode for swinging your hand when breaking").defaultValue(SwingMode.None).build());
        this.placeSwingMode = this.sgSwing.add(new EnumSetting.Builder().name("place-swing-mode").description("Mode for swinging your hand when placing").defaultValue(SwingMode.None).build());
        this.placeRange = this.sgRange.add(new DoubleSetting.Builder().name("place-range").description("Maximum distance to place crystals for").defaultValue(4.0d).build());
        this.breakRange = this.sgRange.add(new DoubleSetting.Builder().name("break-range").description("Maximum distance to break crystals for").defaultValue(4.0d).build());
        this.currentTarget = null;
        this.lastTargetTime = 0L;
        this.currentTargetPlayer = null;
        this.bestPlacePosition = null;
        this.bestPlaceDamage = 0.0d;
        this.lastTargetSelectionTime = 0L;
        this.forceBreakCrystals = new ArrayList();
        this.placePositionPool = new Pool<>(() -> {
            return new PlacePosition(this);
        });
        this._placePositions = new ArrayList();
        this.mutablePos = new class_2338.class_2339();
        this.brokenCrystals = new IntOpenHashSet();
        this.crystalBreakDelays = new HashMap();
        this.crystalPlaceDelays = new HashMap();
        this.cachedValidSpots = new HashSet();
        this.lastPlaceTimeMS = 0L;
        this.lastBreakTimeMS = 0L;
        this.lastChosenPos = null;
        this.lastShorelinePos = null;
        this.placeAttempts = 0;
        this.lastFurtherPos = null;
        this.lastHighPriorityRotationTime = 0L;
        this.rotationActive = false;
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/autocrystal/AutoCrystal$RotationTiming.class */
    public enum RotationTiming {
        Pre("Pre"),
        Post("Post");

        private final String title;

        RotationTiming(String title) {
            this.title = title;
        }

        @Override // java.lang.Enum
        public String toString() {
            return this.title;
        }
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        if (this.autoMine == null) {
            this.autoMine = (AutoMine) Modules.get().get(AutoMine.class);
        }
        this.crystalBreakDelays.clear();
        this.crystalPlaceDelays.clear();
        this.lastFurtherPos = null;
        this.lastShorelinePos = null;
        this.currentTarget = null;
        this.lastTargetTime = 0L;
        this.currentTargetPlayer = null;
        this.bestPlacePosition = null;
        this.bestPlaceDamage = 0.0d;
        this.lastTargetSelectionTime = 0L;
        this.cachedValidSpots.clear();
        this.renderer.onActivate();
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onDeactivate() {
        this.currentTarget = null;
        this.lastTargetTime = 0L;
        this.lastShorelinePos = null;
        this.currentTargetPlayer = null;
        this.bestPlacePosition = null;
        this.bestPlaceDamage = 0.0d;
        this.lastTargetSelectionTime = 0L;
        this.rotationActive = false;
        this.cachedValidSpots.clear();
    }

    private boolean moduleIsActivelyRotating() {
        boolean highPriorityActive = MeteorClient.ROTATION.isHighPriorityRotationActive(950.0d);
        if (!highPriorityActive) {
            return System.currentTimeMillis() - this.lastHighPriorityRotationTime < HIGH_PRIORITY_ROTATION_COOLDOWN;
        }
        this.lastHighPriorityRotationTime = System.currentTimeMillis();
        return true;
    }

    private void maintainRotation() {
        class_243 class_243VarMethod_33571;
        if (this.mc.field_1724 == null || !isActive()) {
            return;
        }
        long currentTime = System.currentTimeMillis();
        boolean shouldReleaseLock = this.currentTarget == null || currentTime - this.lastTargetTime > TARGET_EXPIRY_TIME || moduleIsActivelyRotating();
        if (shouldReleaseLock) {
            this.rotationActive = false;
            return;
        }
        if (this.currentTarget != null) {
            if (this.rotatePlace.get().booleanValue() || this.rotateBreak.get().booleanValue()) {
                this.rotationActive = true;
                if (this.currentTargetPlayer != null && !this.currentTargetPlayer.method_29504()) {
                    class_243VarMethod_33571 = this.currentTargetPlayer.method_33571();
                } else {
                    class_243VarMethod_33571 = this.currentTarget;
                }
                class_243 rotationTarget = class_243VarMethod_33571;
                MeteorClient.ROTATION.requestRotation(rotationTarget, 850.0d);
            }
        }
    }

    private void selectBestTarget() {
        List<class_1657> validTargets = new ArrayList<>();
        for (class_1657 player : this.mc.field_1687.method_18456()) {
            if (player != this.mc.field_1724 && !Friends.get().isFriend(player) && !player.method_29504() && (!this.ignoreNakeds.get().booleanValue() || !isPlayerNaked(player))) {
                validTargets.add(player);
            }
        }
        FakePlayerManager.forEach(fp -> {
            if (fp == null || fp.method_29504()) {
                return;
            }
            validTargets.add(fp);
        });
        this.currentTargetPlayer = getNearestEnemyPlayer();
        this.bestPlacePosition = null;
        this.bestPlaceDamage = 0.0d;
        PlacePosition bestPlacePos = null;
        for (class_1657 player2 : validTargets) {
            double distanceSq = player2.method_5707(this.mc.field_1724.method_33571());
            if (distanceSq <= 625.0d) {
                boolean doFacePlace = shouldFacePlace(player2);
                PlacePosition testPos = findBestPlacePosition(player2, doFacePlace);
                if (testPos != null && (bestPlacePos == null || testPos.damage > bestPlacePos.damage)) {
                    bestPlacePos = testPos;
                    this.currentTargetPlayer = player2;
                }
            }
        }
        if (bestPlacePos != null) {
            this.bestPlacePosition = bestPlacePos.blockPos;
            this.bestPlaceDamage = bestPlacePos.damage;
        }
    }

    private void update() {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) {
            return;
        }
        long currentTime = System.currentTimeMillis();
        maintainRotation();
        if (this.autoMine == null) {
            this.autoMine = (AutoMine) Modules.get().get(AutoMine.class);
        }
        if (this.currentTargetPlayer == null || this.currentTargetPlayer.method_29504() || this.currentTargetPlayer.method_31481()) {
            this.currentTargetPlayer = getNearestEnemyPlayer();
        }
        boolean canBreak = this.breakCrystals.get().booleanValue() && !(this.pauseEatBreak.get().booleanValue() && this.mc.field_1724.method_6115());
        if (canBreak) {
            for (class_1297 entity : this.mc.field_1687.method_18112()) {
                if ((entity instanceof class_1511) && inBreakRange(entity.method_19538()) && shouldBreakCrystal(entity)) {
                    if (!breakSpeedCheck()) {
                        break;
                    }
                    class_1657 crystalTarget = getCrystalTargetPlayer(entity);
                    if (crystalTarget != null) {
                        this.currentTargetPlayer = crystalTarget;
                    }
                    this.currentTarget = entity.method_19538();
                    this.lastTargetTime = currentTime;
                    breakCrystal(entity);
                }
            }
        }
        for (PlacePosition p : this._placePositions) {
            this.placePositionPool.free(p);
        }
        this._placePositions.clear();
        if (this.placeCrystals.get().booleanValue()) {
            if (this.pauseEatPlace.get().booleanValue() && this.mc.field_1724.method_6115()) {
                return;
            }
            updateCachedValidSpots();
            boolean placed = tryPlacementWithPriority();
            if (!placed) {
                handleBlockPrediction();
            }
        }
    }

    private boolean tryPlacementWithPriority() {
        class_2338 predictedPos;
        long currentTime = System.currentTimeMillis();
        class_1657 target = getNearestEnemyPlayer();
        if (this.cevHead.get().booleanValue() && target != null && target.method_5707(this.mc.field_1724.method_33571()) <= 25.0d) {
            tryCevHead(target);
        }
        if (target != null && target.method_5707(this.mc.field_1724.method_33571()) <= 25.0d && (predictedPos = getPredictivePlacePosition(target)) != null && placeCrystal(predictedPos)) {
            this.lastPlaceTimeMS = currentTime;
            return true;
        }
        boolean shouldSelectNewTarget = this.currentTargetPlayer == null || this.bestPlacePosition == null || currentTime - this.lastTargetSelectionTime > TARGET_SELECTION_COOLDOWN;
        if (shouldSelectNewTarget) {
            selectBestTarget();
            this.lastTargetSelectionTime = currentTime;
        }
        if (this.bestPlacePosition != null && placeSpeedCheck()) {
            this.currentTarget = this.bestPlacePosition.method_46558();
            this.lastTargetTime = currentTime;
            if (placeCrystal(this.bestPlacePosition)) {
                this.lastPlaceTimeMS = currentTime;
                return true;
            }
        }
        if (this.shorelineMode.get().booleanValue() && this.ignoreCalc.get().booleanValue() && target != null && tryShorelinePlacement(target)) {
            return true;
        }
        if (target != null && shouldFacePlace(target) && tryFacePlacement(target)) {
            return true;
        }
        return false;
    }

    private class_2338 getPredictivePlacePosition(class_1657 target) {
        if (target == null || target.method_29504() || !inPlaceRange(class_2338.method_49638(target.method_19538()))) {
            this.lastChosenPos = null;
            this.lastFurtherPos = null;
            this.lastShorelinePos = null;
            this.placeAttempts = 0;
            return null;
        }
        if (PlayerUtils.isPlayerPhased(target) && this.mc.field_1687.method_8320(class_2338.method_49638(target.method_19538())).method_26204() == class_2246.field_9987) {
            return null;
        }
        class_2338 enemyPos = class_2338.method_49638(target.method_19538());
        class_2338[] ignorePositions = {class_2338.method_49638(this.mc.field_1724.method_19538()), class_2338.method_49638(this.mc.field_1724.method_19538()).method_10074(), class_2338.method_49638(this.mc.field_1724.method_19538()).method_10087(2)};
        class_2338[] adjacentPositions = {enemyPos.method_10095(), enemyPos.method_10072(), enemyPos.method_10078(), enemyPos.method_10067()};
        class_2338[] furtherPositions = {enemyPos.method_10076(2).method_10074(), enemyPos.method_10077(2).method_10074(), enemyPos.method_10089(2).method_10074(), enemyPos.method_10088(2).method_10074()};
        SilentMine silentMine = (SilentMine) Modules.get().get(SilentMine.class);
        class_2338 rebreakPos = (silentMine == null || !silentMine.isActive()) ? null : silentMine.getRebreakBlockPos();
        if (this.lastFurtherPos != null) {
            int index = -1;
            int i = 0;
            while (true) {
                if (i >= furtherPositions.length) {
                    break;
                }
                if (!furtherPositions[i].equals(this.lastFurtherPos)) {
                    i++;
                } else {
                    index = i;
                    break;
                }
            }
            if (index != -1 && isValidPosition(this.lastFurtherPos) && ((this.mc.field_1687.method_22347(this.lastFurtherPos.method_10084()) || (this.ignoreTrapdoors.get().booleanValue() && TRAPDOOR_BLOCKS.contains(this.mc.field_1687.method_8320(this.lastFurtherPos.method_10084()).method_26204()))) && rebreakPos != null && rebreakPos.equals(adjacentPositions[index]))) {
                if (placeSpeedCheck()) {
                    return this.lastFurtherPos;
                }
            } else {
                this.lastFurtherPos = null;
            }
        }
        if (rebreakPos != null) {
            for (int i2 = 0; i2 < furtherPositions.length; i2++) {
                class_2338 furtherPos = furtherPositions[i2];
                class_2338 adjacentPos = adjacentPositions[i2];
                class_2680 aboveState = this.mc.field_1687.method_8320(furtherPos.method_10084());
                boolean airAbove = this.mc.field_1687.method_22347(furtherPos.method_10084()) || (this.ignoreTrapdoors.get().booleanValue() && TRAPDOOR_BLOCKS.contains(aboveState.method_26204()));
                boolean validPos = isValidPosition(furtherPos);
                boolean rebreakMatch = rebreakPos.equals(adjacentPos);
                if (airAbove && validPos && rebreakMatch) {
                    this.lastFurtherPos = furtherPos;
                    if (placeSpeedCheck()) {
                        return furtherPos;
                    }
                }
            }
        }
        class_2338[] facePlacePositions = {enemyPos.method_10095().method_10074(), enemyPos.method_10072().method_10074(), enemyPos.method_10078().method_10074(), enemyPos.method_10067().method_10074()};
        for (class_2338 pos : facePlacePositions) {
            if (isValidPosition(pos) && !Arrays.asList(ignorePositions).contains(pos)) {
                return pos;
            }
        }
        return null;
    }

    private boolean tryShorelinePlacement(class_1657 target) {
        if (target == null) {
            return false;
        }
        long currentTime = System.currentTimeMillis();
        class_2338 enemyPos = class_2338.method_49638(target.method_19538());
        class_2338[] ignorePositions = {class_2338.method_49638(this.mc.field_1724.method_19538()), class_2338.method_49638(this.mc.field_1724.method_19538()).method_10074(), class_2338.method_49638(this.mc.field_1724.method_19538()).method_10087(2)};
        class_2338[] cornerPositions = {enemyPos.method_10095().method_10078().method_10074(), enemyPos.method_10095().method_10067().method_10074(), enemyPos.method_10072().method_10078().method_10074(), enemyPos.method_10072().method_10067().method_10074()};
        if (this.lastShorelinePos != null && isValidPosition(this.lastShorelinePos) && !Arrays.asList(ignorePositions).contains(this.lastShorelinePos) && isCornerAdjacentToMinedSurround(this.lastShorelinePos, enemyPos) && placeSpeedCheck()) {
            if (this.rotatePlace.get().booleanValue()) {
                this.currentTarget = this.lastShorelinePos.method_46558();
                this.lastTargetTime = currentTime;
            }
            if (placeCrystal(this.lastShorelinePos)) {
                this.lastChosenPos = this.lastShorelinePos;
                this.lastFurtherPos = null;
                this.placeAttempts = 0;
                return true;
            }
        }
        for (class_2338 cornerPos : cornerPositions) {
            if (isValidPosition(cornerPos) && !Arrays.asList(ignorePositions).contains(cornerPos) && isCornerAdjacentToMinedSurround(cornerPos, enemyPos) && placeSpeedCheck()) {
                if (this.rotatePlace.get().booleanValue()) {
                    this.currentTarget = cornerPos.method_46558();
                    this.lastTargetTime = currentTime;
                }
                if (placeCrystal(cornerPos)) {
                    this.lastShorelinePos = cornerPos;
                    this.lastChosenPos = cornerPos;
                    this.lastFurtherPos = null;
                    this.placeAttempts = 0;
                    return true;
                }
            }
        }
        this.lastShorelinePos = null;
        return false;
    }

    private boolean tryFacePlacement(class_1657 target) {
        if (target == null) {
            return false;
        }
        class_2338 enemyPos = class_2338.method_49638(target.method_19538());
        class_2338[] ignorePositions = {class_2338.method_49638(this.mc.field_1724.method_19538()), class_2338.method_49638(this.mc.field_1724.method_19538()).method_10074(), class_2338.method_49638(this.mc.field_1724.method_19538()).method_10087(2)};
        class_2338[] facePlacePositions = {enemyPos.method_10095().method_10074(), enemyPos.method_10072().method_10074(), enemyPos.method_10078().method_10074(), enemyPos.method_10067().method_10074()};
        for (class_2338 facePlacePos : facePlacePositions) {
            if (isValidPosition(facePlacePos) && !Arrays.asList(ignorePositions).contains(facePlacePos) && placeSpeedCheck() && placeCrystal(facePlacePos)) {
                this.lastChosenPos = facePlacePos;
                this.lastFurtherPos = null;
                this.lastShorelinePos = null;
                this.placeAttempts = 0;
                return true;
            }
        }
        return false;
    }

    private boolean isPlayerNaked(class_1657 player) {
        return ((class_1799) player.method_31548().meteor$getArmor().get(0)).method_7960() && ((class_1799) player.method_31548().meteor$getArmor().get(1)).method_7960() && ((class_1799) player.method_31548().meteor$getArmor().get(2)).method_7960() && ((class_1799) player.method_31548().meteor$getArmor().get(3)).method_7960();
    }

    private boolean shouldTryPreplace() {
        class_1657 nearest = getNearestEnemyPlayer();
        if (nearest == null) {
            return false;
        }
        double distance = nearest.method_5707(this.mc.field_1724.method_33571());
        return distance <= 25.0d;
    }

    private void tryCevHead(class_1657 player) {
        BreakIndicators breakIndicators;
        class_2338 headBlockPos = class_2338.method_49638(player.method_19538()).method_10084();
        class_2680 headBlockState = this.mc.field_1687.method_8320(headBlockPos);
        if (headBlockState.method_26214(this.mc.field_1687, headBlockPos) >= 0.0f && inPlaceRange(headBlockPos)) {
            class_2338 crystalPos = headBlockPos.method_10084();
            class_2680 crystalBlockState = this.mc.field_1687.method_8320(crystalPos);
            boolean validBlock = crystalBlockState.method_26215() || (this.ignoreTrapdoors.get().booleanValue() && TRAPDOOR_BLOCKS.contains(crystalBlockState.method_26204()));
            if (validBlock && !this.crystalPlaceDelays.containsKey(crystalPos.method_10074()) && (breakIndicators = (BreakIndicators) Modules.get().get(BreakIndicators.class)) != null && breakIndicators.isBlockBeingBroken(headBlockPos)) {
                double breakProgress = breakIndicators.breakStartTimes.get(headBlockPos).getBreakProgress(RenderUtils.getCurrentGameTickCalculated());
                if (breakProgress >= this.predictBlockThreshold.get().doubleValue() / 100.0d) {
                    placeCrystal(crystalPos.method_10074());
                }
            }
        }
    }

    private void handleBlockPrediction() {
        BreakIndicators breakIndicators;
        if (this.predictBlock.get().booleanValue() && (breakIndicators = (BreakIndicators) Modules.get().get(BreakIndicators.class)) != null) {
            for (Map.Entry<class_2338, BreakIndicators.BlockBreak> entry : breakIndicators.breakStartTimes.entrySet()) {
                class_2338 blockPos = entry.getKey();
                if (breakIndicators.isBlockBeingBroken(blockPos)) {
                    double breakProgress = entry.getValue().getBreakProgress(RenderUtils.getCurrentGameTickCalculated());
                    if (breakProgress >= this.predictBlockThreshold.get().doubleValue() / 100.0d) {
                        class_2338 crystalPos = blockPos.method_10084();
                        class_2680 blockState = this.mc.field_1687.method_8320(crystalPos);
                        boolean canPlace = (this.mc.field_1687.method_22347(crystalPos) || (this.ignoreTrapdoors.get().booleanValue() && TRAPDOOR_BLOCKS.contains(blockState.method_26204()))) && inPlaceRange(crystalPos);
                        if (canPlace) {
                            placeCrystal(crystalPos.method_10074());
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = 250)
    private void onTickPre(TickEvent.Pre event) {
        maintainRotation();
    }

    @EventHandler(priority = 200)
    private void onSendMovementPacket(SendMovementPacketsEvent.Pre event) {
        maintainRotation();
    }

    @EventHandler(priority = -190)
    private void onRotationEvent(RotateEvent event) {
        class_243 class_243VarMethod_33571;
        boolean pearlPhaseEnabled = ((PearlPhase) Modules.get().get(PearlPhase.class)).isActive();
        boolean surroundEnabled = ((Surround) Modules.get().get(Surround.class)).isActive();
        if (moduleIsActivelyRotating() || !this.rotationActive || pearlPhaseEnabled || surroundEnabled || this.currentTarget == null) {
            return;
        }
        if (this.rotatePlace.get().booleanValue() || this.rotateBreak.get().booleanValue()) {
            if (this.currentTargetPlayer != null && !this.currentTargetPlayer.method_29504()) {
                class_243VarMethod_33571 = this.currentTargetPlayer.method_33571();
            } else {
                class_243VarMethod_33571 = this.currentTarget;
            }
            class_243 rotationTarget = class_243VarMethod_33571;
            float[] angles = MeteorClient.ROTATION.getRotation(rotationTarget);
            event.setYaw(angles[0]);
            event.setPitch(angles[1]);
        }
    }

    private boolean isCornerAdjacentToMinedSurround(class_2338 cornerPos, class_2338 enemyPos) {
        if (this.autoMine == null) {
            return false;
        }
        if (cornerPos.equals(enemyPos.method_10095().method_10078().method_10074())) {
            return this.autoMine.isTargetedPos(enemyPos.method_10095()) || this.autoMine.isTargetedPos(enemyPos.method_10078());
        }
        if (cornerPos.equals(enemyPos.method_10095().method_10067().method_10074())) {
            return this.autoMine.isTargetedPos(enemyPos.method_10095()) || this.autoMine.isTargetedPos(enemyPos.method_10067());
        }
        if (cornerPos.equals(enemyPos.method_10072().method_10078().method_10074())) {
            return this.autoMine.isTargetedPos(enemyPos.method_10072()) || this.autoMine.isTargetedPos(enemyPos.method_10078());
        }
        if (cornerPos.equals(enemyPos.method_10072().method_10067().method_10074())) {
            return this.autoMine.isTargetedPos(enemyPos.method_10072()) || this.autoMine.isTargetedPos(enemyPos.method_10067());
        }
        return false;
    }

    public void preplaceCrystal(class_2338 crystalBlockPos, boolean snapAt) {
        if (this.pauseEatPlace.get().booleanValue() && this.mc.field_1724.method_6115()) {
            return;
        }
        class_1657 target = getNearestEnemyPlayer();
        if (target == null) {
            return;
        }
        if (crystalBlockPos != null && isValidPosition(crystalBlockPos.method_10074())) {
            if (tryPlaceCrystalAtPos(crystalBlockPos.method_10074(), snapAt)) {
                this.lastChosenPos = crystalBlockPos.method_10074();
                this.lastFurtherPos = null;
                this.lastShorelinePos = null;
                this.placeAttempts = 0;
                return;
            }
            this.placeAttempts++;
            if (this.placeAttempts >= 1) {
                placeCrystal(crystalBlockPos.method_10074());
                this.placeAttempts = 0;
                this.lastChosenPos = crystalBlockPos.method_10074();
                this.lastFurtherPos = null;
                this.lastShorelinePos = null;
                return;
            }
            return;
        }
        tryPlacementWithPriority();
    }

    public boolean placeCrystal(class_2338 blockPos) {
        if (blockPos == null || this.mc.field_1724 == null) {
            return false;
        }
        class_2248 block = this.mc.field_1687.method_8320(blockPos).method_26204();
        if (block != class_2246.field_10540 && block != class_2246.field_9987) {
            return false;
        }
        class_2338 crystalPos = blockPos.method_10084();
        class_2680 crystalState = this.mc.field_1687.method_8320(crystalPos);
        boolean validSpace = this.mc.field_1687.method_22347(crystalPos) || (this.ignoreTrapdoors.get().booleanValue() && TRAPDOOR_BLOCKS.contains(crystalState.method_26204()));
        if (!validSpace) {
            return false;
        }
        class_238 box = new class_238(blockPos.method_10263(), blockPos.method_10264() + 1, blockPos.method_10260(), blockPos.method_10263() + 1, blockPos.method_10264() + 3, blockPos.method_10260() + 1);
        if (intersectsWithEntities(box)) {
            return false;
        }
        FindItemResult result = InvUtils.find(class_1802.field_8301);
        if (!result.found()) {
            return false;
        }
        boolean isHoldingCrystal = this.mc.field_1724.method_6047().method_7909() == class_1802.field_8301 || this.mc.field_1724.method_6079().method_7909() == class_1802.field_8301;
        boolean didSwap = false;
        this.currentTarget = blockPos.method_46558();
        this.lastTargetTime = System.currentTimeMillis();
        long currentTime = System.currentTimeMillis();
        if (this.crystalPlaceDelays.containsKey(blockPos) && (currentTime - this.crystalPlaceDelays.get(blockPos).longValue()) / 1000.0d < this.placeDelay.get().doubleValue()) {
            return false;
        }
        if (this.rotatePlace.get().booleanValue() && this.rotationTiming.get() == RotationTiming.Pre) {
            MeteorClient.ROTATION.snapAt(this.currentTarget);
        }
        if (!isHoldingCrystal) {
            try {
                didSwap = MeteorClient.SWAP.beginSwap(result, true);
                if (!didSwap) {
                    if (didSwap) {
                        MeteorClient.SWAP.endSwap(true);
                    }
                    return false;
                }
            } finally {
                if (didSwap) {
                    MeteorClient.SWAP.endSwap(true);
                }
            }
        }
        this.crystalPlaceDelays.put(blockPos, Long.valueOf(currentTime));
        this.renderer.onPlaceCrystal(blockPos);
        class_3965 calculatedHitResult = AutoCrystalUtil.getPlaceBlockHitResult(blockPos);
        class_1268 hand = this.mc.field_1724.method_6047().method_7909() == class_1802.field_8301 ? class_1268.field_5808 : class_1268.field_5810;
        int s = this.mc.field_1687.meteor$getAndIncrementSequence();
        this.mc.field_1724.field_3944.method_52787(new class_2885(hand, calculatedHitResult, s));
        if (this.placeSwingMode.get() == SwingMode.Client) {
            this.mc.field_1724.method_6104(hand);
        }
        if (this.placeSwingMode.get() == SwingMode.Packet) {
            this.mc.method_1562().method_52787(new class_2879(hand));
        }
        if (this.rotatePlace.get().booleanValue() && this.rotationTiming.get() == RotationTiming.Post) {
            MeteorClient.ROTATION.snapAt(this.currentTarget);
        }
        return true;
    }

    public boolean breakCrystal(class_1297 entity) {
        if ((this.pauseEatBreak.get().booleanValue() && this.mc.field_1724.method_6115()) || this.mc.field_1724 == null) {
            return false;
        }
        this.currentTarget = entity.method_19538();
        this.lastTargetTime = System.currentTimeMillis();
        long currentTime = System.currentTimeMillis();
        if (this.crystalBreakDelays.containsKey(Integer.valueOf(entity.method_5628())) && (currentTime - this.crystalBreakDelays.get(Integer.valueOf(entity.method_5628())).longValue()) / 1000.0d < this.breakDelay.get().doubleValue()) {
            return false;
        }
        if (this.rotateBreak.get().booleanValue() && this.rotationTiming.get() == RotationTiming.Pre) {
            MeteorClient.ROTATION.snapAt(this.currentTarget);
        }
        this.crystalBreakDelays.put(Integer.valueOf(entity.method_5628()), Long.valueOf(currentTime));
        this.brokenCrystals.add(entity.method_5628());
        this.renderer.onBreakCrystal(entity);
        class_2824 packet = class_2824.method_34206(entity, this.mc.field_1724.method_5715());
        this.mc.method_1562().method_52787(packet);
        if (this.breakSwingMode.get() == SwingMode.Client) {
            this.mc.field_1724.method_6104(class_1268.field_5808);
        }
        if (this.breakSwingMode.get() == SwingMode.Packet) {
            this.mc.method_1562().method_52787(new class_2879(class_1268.field_5808));
        }
        if (this.rotateBreak.get().booleanValue() && this.rotationTiming.get() == RotationTiming.Post) {
            MeteorClient.ROTATION.snapAt(this.currentTarget);
        }
        this.lastBreakTimeMS = System.currentTimeMillis();
        return true;
    }

    private boolean tryPlaceCrystalAtPos(class_2338 pos, boolean snapAt) {
        if (!inPlaceRange(pos)) {
            return false;
        }
        class_2680 downState = this.mc.field_1687.method_8320(pos);
        class_2248 downBlock = downState.method_26204();
        if (downBlock != class_2246.field_10540 && downBlock != class_2246.field_9987) {
            return false;
        }
        class_2680 aboveState = this.mc.field_1687.method_8320(pos.method_10084());
        if (!this.mc.field_1687.method_22347(pos.method_10084()) && (!this.ignoreTrapdoors.get().booleanValue() || !TRAPDOOR_BLOCKS.contains(aboveState.method_26204()))) {
            return false;
        }
        class_238 box = new class_238(pos.method_10263(), pos.method_10264() + 1, pos.method_10260(), pos.method_10263() + 1, pos.method_10264() + 3, pos.method_10260() + 1);
        if (intersectsWithEntities(box)) {
            return false;
        }
        if (this.rotatePlace.get().booleanValue() && snapAt && !MeteorClient.ROTATION.lookingAt(new class_238(pos))) {
            MeteorClient.ROTATION.snapAt(pos.method_46558());
        }
        return placeCrystal(pos);
    }

    private class_1657 getNearestEnemyPlayer() {
        class_1657 nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (class_1657 player : this.mc.field_1687.method_18456()) {
            if (player != this.mc.field_1724 && !Friends.get().isFriend(player) && !player.method_29504()) {
                double distance = player.method_5707(this.mc.field_1724.method_33571());
                if (distance < nearestDistance) {
                    nearest = player;
                    nearestDistance = distance;
                }
            }
        }
        return nearest;
    }

    public boolean inPlaceRange(class_2338 blockPos) {
        class_243 from = this.mc.field_1724.method_33571();
        double distance = blockPos.method_46558().method_1022(from);
        return distance <= this.placeRange.get().doubleValue();
    }

    public boolean inBreakRange(class_243 pos) {
        class_243 from = this.mc.field_1724.method_33571();
        return pos.method_1022(from) <= this.breakRange.get().doubleValue();
    }

    public boolean shouldBreakCrystal(class_1297 entity) {
        boolean damageCheck = false;
        double selfDamage = DamageUtils.crystalDamage(this.mc.field_1724, entity.method_19538());
        if (selfDamage > this.maxBreak.get().doubleValue()) {
            return false;
        }
        class_2338 crystalPos = class_2338.method_49638(entity.method_19538());
        class_2338 floorPos = crystalPos.method_10074();
        Iterator it = this.mc.field_1687.method_18456().iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            class_1657 player = (class_1657) it.next();
            if (player != this.mc.field_1724 && !player.method_29504() && !Friends.get().isFriend(player)) {
                class_2338 playerFloorPos = class_2338.method_49638(player.method_19538()).method_10074();
                if (playerFloorPos.equals(floorPos)) {
                    return true;
                }
                if (this.ignoreCalc.get().booleanValue()) {
                    damageCheck = true;
                    break;
                }
                double targetDamage = DamageUtils.crystalDamage(player, entity.method_19538());
                if (targetDamage >= this.minBreak.get().doubleValue()) {
                    damageCheck = true;
                    break;
                }
            }
        }
        return damageCheck;
    }

    private class_1657 getCrystalTargetPlayer(class_1297 crystal) {
        class_1657 bestTarget = null;
        double bestDamage = 0.0d;
        for (class_1657 player : this.mc.field_1687.method_18456()) {
            if (player != this.mc.field_1724 && !player.method_29504() && !Friends.get().isFriend(player)) {
                double damage = DamageUtils.crystalDamage(player, crystal.method_19538());
                if (damage > bestDamage) {
                    bestDamage = damage;
                    bestTarget = player;
                }
            }
        }
        return bestTarget;
    }

    @EventHandler(priority = 200)
    private void onEntity(EntityAddedEvent event) {
        class_1297 entity = event.entity;
        if (entity instanceof class_1511) {
            class_2338 blockPos = class_2338.method_49638(entity.method_19538()).method_10074();
            if (this.crystalPlaceDelays.containsKey(blockPos)) {
                this.crystalPlaceDelays.remove(blockPos);
            }
            if (this.currentTarget != null && blockPos.method_46558().method_1022(this.currentTarget) < 1.0d) {
                this.currentTarget = entity.method_19538();
                this.lastTargetTime = System.currentTimeMillis();
            }
            if (this.breakCrystals.get().booleanValue() && this.packetBreak.get().booleanValue() && inBreakRange(entity.method_19538()) && shouldBreakCrystal(entity) && breakSpeedCheck()) {
                class_1657 crystalTarget = getCrystalTargetPlayer(entity);
                if (crystalTarget != null) {
                    this.currentTargetPlayer = crystalTarget;
                }
                breakCrystal(entity);
            }
        }
    }

    private Set<class_2338> getIgnoredPositions() {
        SilentMine silentMine;
        Set<class_2338> ignored = new HashSet<>();
        if (this.antiSurroundPlace.get().booleanValue() && (silentMine = (SilentMine) Modules.get().get(SilentMine.class)) != null && silentMine.isActive()) {
            if (silentMine.getDelayedDestroyBlockPos() != null) {
                ignored.add(silentMine.getDelayedDestroyBlockPos());
            }
            if (silentMine.getRebreakBlockPos() != null) {
                ignored.add(silentMine.getRebreakBlockPos());
            }
        }
        return ignored;
    }

    private PlacePosition findBestPlacePosition(class_1657 target, boolean doFacePlace) {
        if (target == null) {
            return null;
        }
        PlacePosition bestPos = this.placePositionPool.get();
        this._placePositions.add(bestPos);
        bestPos.damage = 0.0d;
        bestPos.blockPos = null;
        bestPos.facePlace = false;
        getIgnoredPositions();
        class_243 targetPos = target.method_19538();
        for (class_2338 pos : this.cachedValidSpots) {
            double distance = pos.method_19770(targetPos);
            if (distance <= 144.0d) {
                class_243 crystalPos = new class_243(((double) pos.method_10263()) + 0.5d, pos.method_10264() + 1, ((double) pos.method_10260()) + 0.5d);
                double damage = CrystalUtils.calculateCrystalDamage(crystalPos, target);
                if (damage < this.minPlace.get().doubleValue()) {
                    damage = DamageUtils.crystalDamage(target, crystalPos);
                }
                if (damage >= this.minPlace.get().doubleValue() && damage > bestPos.damage) {
                    bestPos.blockPos = pos;
                    bestPos.damage = damage;
                    bestPos.facePlace = false;
                }
            }
        }
        if (bestPos.blockPos == null && doFacePlace) {
            class_2338 targetFloorPos = class_2338.method_49638(target.method_19538()).method_10074();
            class_2338[] facePlacePositions = {targetFloorPos.method_10095(), targetFloorPos.method_10072(), targetFloorPos.method_10078(), targetFloorPos.method_10067()};
            for (class_2338 pos2 : facePlacePositions) {
                if (this.cachedValidSpots.contains(pos2)) {
                    double damage2 = CrystalUtils.calculateCrystalDamage(new class_243(((double) pos2.method_10263()) + 0.5d, pos2.method_10264() + 1, ((double) pos2.method_10260()) + 0.5d), target);
                    if (damage2 >= 1.0d && damage2 > bestPos.damage) {
                        bestPos.blockPos = pos2;
                        bestPos.damage = damage2;
                        bestPos.facePlace = true;
                    }
                }
            }
        }
        if (bestPos.blockPos != null) {
            return bestPos;
        }
        return null;
    }

    private boolean shouldFacePlace(class_1657 target) {
        if (!this.facePlace.get().booleanValue()) {
            return false;
        }
        if (this.forceFacePlaceKeybind.get().isPressed()) {
            return true;
        }
        if (this.facePlaceMissingArmor.get().booleanValue()) {
            for (int i = 0; i < 4; i++) {
                if (((class_1799) target.method_31548().meteor$getArmor().get(i)).method_7960()) {
                    return true;
                }
            }
        }
        return target.method_6032() <= ((float) this.facePlaceHealth.get().intValue()) || checkArmorDurability(target);
    }

    private boolean checkArmorDurability(class_1657 target) {
        int maxDurability;
        for (int i = 0; i < 4; i++) {
            class_1799 armorPiece = (class_1799) target.method_31548().meteor$getArmor().get(i);
            if (!armorPiece.method_7960() && (maxDurability = armorPiece.method_7936()) > 0) {
                int currentDurability = maxDurability - armorPiece.method_7919();
                int durabilityPercentage = (int) ((currentDurability / maxDurability) * 100.0f);
                if (durabilityPercentage <= this.facePlaceArmor.get().intValue()) {
                    return true;
                }
            }
        }
        return false;
    }

    private void updateCachedValidSpots() {
        this.cachedValidSpots.clear();
        int r = (int) Math.ceil(this.placeRange.get().doubleValue());
        class_2338 eyePos = class_2338.method_49638(this.mc.field_1724.method_33571());
        IBox class_238Var = new class_238(0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d);
        SilentMine silentMine = (SilentMine) Modules.get().get(SilentMine.class);
        class_2338 rebreakBlockPos = (silentMine == null || !silentMine.isActive()) ? null : silentMine.getRebreakBlockPos();
        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    class_2338 pos = new class_2338(eyePos.method_10263() + x, eyePos.method_10264() + y, eyePos.method_10260() + z);
                    if (this.mc.field_1724.method_33571().method_1022(pos.method_46558()) <= this.placeRange.get().doubleValue()) {
                        class_2338 crystalPos = pos.method_10084();
                        class_2680 posState = this.mc.field_1687.method_8320(pos);
                        class_2248 block = posState.method_26204();
                        if (block == class_2246.field_10540 || block == class_2246.field_9987) {
                            class_2680 aboveState = this.mc.field_1687.method_8320(crystalPos);
                            boolean isRebreakBlock = rebreakBlockPos != null && rebreakBlockPos.equals(pos);
                            boolean validAbove = this.mc.field_1687.method_22347(crystalPos) || isRebreakBlock || (this.ignoreTrapdoors.get().booleanValue() && TRAPDOOR_BLOCKS.contains(aboveState.method_26204()));
                            if (validAbove) {
                                class_238Var.meteor$set(pos.method_10263(), pos.method_10264() + 1, pos.method_10260(), pos.method_10263() + 1, pos.method_10264() + 3, pos.method_10260() + 1);
                                if (!intersectsWithEntities(class_238Var)) {
                                    double selfDamage = DamageUtils.crystalDamage(this.mc.field_1724, new class_243(((double) pos.method_10263()) + 0.5d, pos.method_10264() + 1, ((double) pos.method_10260()) + 0.5d));
                                    if (selfDamage <= this.maxPlace.get().doubleValue()) {
                                        this.cachedValidSpots.add(pos);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = Opcode.JSR_W)
    private void onRender3D(Render3DEvent event) {
        if (isActive()) {
            update();
            maintainRotation();
            this.renderer.onRender3D(event);
        }
    }

    @EventHandler
    private void onPlayerDeath(PlayerDeathEvent.Death event) {
        if (event.getPlayer() == null) {
            return;
        }
        if (event.getPlayer() == this.mc.field_1724) {
            this.rotationActive = false;
            this.currentTarget = null;
            this.lastTargetTime = 0L;
        } else if (this.setPlayerDead.get().booleanValue()) {
            event.getPlayer().method_6033(0.0f);
        }
    }

    private boolean intersectsWithEntities(class_238 box) {
        return EntityUtils.intersectsWithEntity(box, entity -> {
            if (entity.method_7325() || this.brokenCrystals.contains(entity.method_5628())) {
                return false;
            }
            if (!this.ignoreItems.get().booleanValue() || !(entity instanceof class_1542)) {
                return true;
            }
            class_1542 item = (class_1542) entity;
            return item.field_6012 >= 10;
        });
    }

    private boolean isValidPosition(class_2338 pos) {
        if (!inPlaceRange(pos)) {
            return false;
        }
        class_2680 downState = this.mc.field_1687.method_8320(pos);
        class_2248 downBlock = downState.method_26204();
        if (downBlock != class_2246.field_10540 && downBlock != class_2246.field_9987) {
            return false;
        }
        class_2680 aboveState = this.mc.field_1687.method_8320(pos.method_10084());
        if (!this.mc.field_1687.method_22347(pos.method_10084()) && (!this.ignoreTrapdoors.get().booleanValue() || !TRAPDOOR_BLOCKS.contains(aboveState.method_26204()))) {
            return false;
        }
        class_238 box = new class_238(pos.method_10263(), pos.method_10264() + 1, pos.method_10260(), pos.method_10263() + 1, pos.method_10264() + 3, pos.method_10260() + 1);
        return !intersectsWithEntities(box);
    }

    private boolean breakSpeedCheck() {
        long currentTime = System.currentTimeMillis();
        return this.breakSpeedLimit.get().doubleValue() == 0.0d || ((double) (currentTime - this.lastBreakTimeMS)) / 1000.0d > 1.0d / this.breakSpeedLimit.get().doubleValue();
    }

    private boolean placeSpeedCheck() {
        long currentTime = System.currentTimeMillis();
        return this.placeSpeedLimit.get().doubleValue() == 0.0d || ((double) (currentTime - this.lastPlaceTimeMS)) / 1000.0d > 1.0d / this.placeSpeedLimit.get().doubleValue();
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public String getInfoString() {
        long currentTime = System.currentTimeMillis();
        return String.format("%d", Long.valueOf(this.crystalBreakDelays.values().stream().filter(x -> {
            return currentTime - x.longValue() <= 1000;
        }).count()));
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/autocrystal/AutoCrystal$PlacePosition.class */
    private class PlacePosition {
        public class_2338 blockPos;
        public double damage = 0.0d;
        public boolean facePlace = false;

        private PlacePosition(AutoCrystal autoCrystal) {
        }
    }
}
