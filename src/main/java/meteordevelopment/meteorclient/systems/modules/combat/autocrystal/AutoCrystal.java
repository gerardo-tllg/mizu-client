package meteordevelopment.meteorclient.systems.modules.combat.autocrystal;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.EntityAddedEvent;
import meteordevelopment.meteorclient.events.entity.PlayerDeathEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.mixininterface.IBox;
import meteordevelopment.meteorclient.mixininterface.IClientWorld;
import meteordevelopment.meteorclient.mixininterface.IPlayerInventory;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.AutoMine;
import meteordevelopment.meteorclient.systems.modules.player.SilentMine;
import meteordevelopment.meteorclient.utils.entity.DamageUtils;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.misc.Pool;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import meteordevelopment.meteorclient.systems.modules.combat.predict.CrystalPredictor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class AutoCrystal extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgPlace;
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

    private final Setting<Double> placeSpeedLimit;
    private final Setting<Double> minPlace;
    private final Setting<Double> maxPlace;
    private final Setting<Boolean> antiSurroundPlace;
    private final Setting<Double> placeDelay;

    private final Setting<Boolean> facePlaceMissingArmor;
    private final Setting<Keybind> forceFacePlaceKeybind;
    private final Setting<Boolean> slowPlace;
    private final Setting<Double> slowPlaceMinDamage;
    private final Setting<Double> slowPlaceMaxDamage;
    private final Setting<Double> slowPlaceSpeed;

    private final Setting<Double> breakSpeedLimit;
    private final Setting<Boolean> packetBreak;
    private final Setting<Double> minBreak;
    private final Setting<Double> maxBreak;
    private final Setting<Double> breakDelay;

    private final Setting<Boolean> rotatePlace;
    private final Setting<Boolean> rotateBreak;

    private final Setting<SwingMode> breakSwingMode;
    private final Setting<SwingMode> placeSwingMode;

    public final Setting<Double> placeRange;
    private final Setting<Double> breakRange;

    public final List<Entity> forceBreakCrystals;
    private final Pool<PlacePosition> placePositionPool;
    private final List<PlacePosition> _placePositions;
    private final BlockPos.Mutable mutablePos;
    private final IntSet explodedCrystals;
    private final Map<Integer, Long> crystalBreakDelays;
    private final Map<BlockPos, Long> crystalPlaceDelays;
    public final List<Boolean> cachedValidSpots;
    private long lastPlaceTimeMS;
    private long lastBreakTimeMS;
    private AutoMine autoMine;
    private Set<BlockPos> _calcIgnoreSet;
    private int tickCounter = 0;
    private boolean placeThisTick = false;
    private boolean breakThisTick = false;
    private Vec3d lastCachePlayerPos = null;
    private boolean cacheIsDirty = true;
    private final Setting<Boolean> prediction;
    private final Setting<Integer> predictionTicks;
    private final Setting<Boolean> useKalman;
    private final Setting<Boolean> useMarkov;
    private final Setting<Boolean> useKNN;
    private final Setting<Boolean> showPrediction;
    private final Setting<Boolean> dynamicPingCompensation;
    private final Setting<Double> predictBlockThreshold;
    private final Setting<Boolean> showPingDebug;
    private final CrystalPredictor crystalPredictor;
    public Vec3d lastPredictedPos = null;

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

        this.placeCrystals = this.sgGeneral.add(new BoolSetting.Builder()
            .name("place").description("Places crystals.").defaultValue(true).build());
        this.pauseEatPlace = this.sgGeneral.add(new BoolSetting.Builder()
            .name("pause-eat-place").description("Pauses placing when eating").defaultValue(true).build());
        this.breakCrystals = this.sgGeneral.add(new BoolSetting.Builder()
            .name("break").description("Breaks crystals.").defaultValue(true).build());
        this.pauseEatBreak = this.sgGeneral.add(new BoolSetting.Builder()
            .name("pause-eat-break").description("Pauses placing when breaking").defaultValue(false).build());
        this.ignoreNakeds = this.sgGeneral.add(new BoolSetting.Builder()
            .name("ignore-nakeds").description("Ignore players with no items.").defaultValue(true).build());
        this.setPlayerDead = this.sgGeneral.add(new BoolSetting.Builder()
            .name("set-player-dead-instantly").description("Tries to not blow up loot by instantly killing the player in the packet they die.").defaultValue(true).build());

        this.placeSpeedLimit = this.sgPlace.add(new DoubleSetting.Builder()
            .name("place-speed-limit").description("Maximum number of crystals to place every second.").defaultValue(40.0).min(0.0).sliderRange(0.0, 40.0).build());
        this.minPlace = this.sgPlace.add(new DoubleSetting.Builder()
            .name("min-place").description("Minimum enemy damage to place.").defaultValue(8.0).min(0.0).sliderRange(0.0, 20.0).build());
        this.maxPlace = this.sgPlace.add(new DoubleSetting.Builder()
            .name("max-place").description("Max self damage to place.").defaultValue(20.0).min(0.0).sliderRange(0.0, 20.0).build());
        this.antiSurroundPlace = this.sgPlace.add(new BoolSetting.Builder()
            .name("anti-surround").description("Ignores auto-mine blocks from calculations to place outside of their surround.").defaultValue(true).build());
        this.placeDelay = this.sgPlace.add(new DoubleSetting.Builder()
            .name("place-delay").description("The number of seconds to wait to retry placing a crystal at a position.").defaultValue(0.05).min(0.0).sliderMax(0.6).build());
        this.prediction = this.sgPlace.add(new BoolSetting.Builder()
            .name("prediction").description("Predicts target movement when selecting place positions.").defaultValue(true).build());
        this.predictionTicks = this.sgPlace.add(new IntSetting.Builder()
            .name("prediction-ticks").description("Ticks ahead to predict target position. Default 2 is correct for ~80ms ping.").defaultValue(2).min(1).sliderRange(1, 4)
            .visible(() -> this.prediction.get()).build());
        this.useKalman = this.sgPlace.add(new BoolSetting.Builder()
            .name("use-kalman").description("Use Kalman filter prediction layer.").defaultValue(true)
            .visible(() -> this.prediction.get()).build());
        this.useMarkov = this.sgPlace.add(new BoolSetting.Builder()
            .name("use-markov").description("Use Markov behavior model layer.").defaultValue(true)
            .visible(() -> this.prediction.get()).build());
        this.useKNN = this.sgPlace.add(new BoolSetting.Builder()
            .name("use-knn").description("Use KNN pattern matching layer (needs warmup).").defaultValue(false)
            .visible(() -> this.prediction.get()).build());
        this.showPrediction = this.sgPlace.add(new BoolSetting.Builder()
            .name("show-prediction").description("Render predicted target position.").defaultValue(false)
            .visible(() -> this.prediction.get()).build());
        this.dynamicPingCompensation = this.sgPlace.add(new BoolSetting.Builder()
            .name("dynamic-ping").description("Use Jacobson/Karels RTT estimation to auto-calculate prediction ticks from smoothed latency with jitter resistance.").defaultValue(true)
            .visible(() -> this.prediction.get()).build());
        this.predictBlockThreshold = this.sgPlace.add(new DoubleSetting.Builder()
            .name("predict-threshold").description("Extra radius (blocks) around the explosion zone accepted when placing at a predicted position.").defaultValue(0.5).min(0.1).sliderRange(0.1, 2.0)
            .visible(() -> this.prediction.get()).build());
        this.showPingDebug = this.sgPlace.add(new BoolSetting.Builder()
            .name("show-ping-debug").description("Show smoothed RTT, deviation, and prediction ticks in the module info string.").defaultValue(false)
            .visible(() -> this.prediction.get()).build());

        this.facePlaceMissingArmor = this.sgFacePlace.add(new BoolSetting.Builder()
            .name("face-place-missing-armor").description("Face places on missing armor").defaultValue(true).build());
        this.forceFacePlaceKeybind = this.sgFacePlace.add(new KeybindSetting.Builder()
            .name("force-face-place").description("Keybind to force face place").build());
        this.slowPlace = this.sgFacePlace.add(new BoolSetting.Builder()
            .name("slow-place").description("Slowly places crystals at lower damages.").defaultValue(true).build());
        this.slowPlaceMinDamage = this.sgFacePlace.add(new DoubleSetting.Builder()
            .name("slow-place-min-place").description("Minimum damage to slow place.").defaultValue(4.0).min(0.0).sliderRange(0.0, 20.0)
            .visible(() -> this.slowPlace.get()).build());
        this.slowPlaceMaxDamage = this.sgFacePlace.add(new DoubleSetting.Builder()
            .name("slow-place-max-place").description("Maximum damage to slow place.").defaultValue(8.0).min(0.0).sliderRange(0.0, 20.0)
            .visible(() -> this.slowPlace.get()).build());
        this.slowPlaceSpeed = this.sgFacePlace.add(new DoubleSetting.Builder()
            .name("slow-place-speed").description("Speed at which to slow place.").defaultValue(2.0).min(0.0).sliderRange(0.0, 20.0)
            .visible(() -> this.slowPlace.get()).build());

        this.breakSpeedLimit = this.sgBreak.add(new DoubleSetting.Builder()
            .name("break-speed-limit").description("Maximum number of crystals to break every second.").defaultValue(60.0).min(0.0).sliderRange(0.0, 60.0).build());
        this.packetBreak = this.sgBreak.add(new BoolSetting.Builder()
            .name("packet-break").description("Breaks when the crystal packet arrives").defaultValue(true).build());
        this.minBreak = this.sgBreak.add(new DoubleSetting.Builder()
            .name("min-break").description("Minimum enemy damage to break.").defaultValue(3.0).min(0.0).sliderRange(0.0, 20.0).build());
        this.maxBreak = this.sgBreak.add(new DoubleSetting.Builder()
            .name("max-break").description("Max self damage to break.").defaultValue(20.0).min(0.0).sliderRange(0.0, 20.0).build());
        this.breakDelay = this.sgBreak.add(new DoubleSetting.Builder()
            .name("break-delay").description("The number of seconds to wait to retry breaking a crystal.").defaultValue(0.05).min(0.0).sliderMax(0.6).build());

        this.rotatePlace = this.sgRotate.add(new BoolSetting.Builder()
            .name("rotate-place").description("Rotates server-side towards the crystals when placed.").defaultValue(false).build());
        this.rotateBreak = this.sgRotate.add(new BoolSetting.Builder()
            .name("rotate-break").description("Rotates server-side towards the crystals when broken.").defaultValue(true).build());

        this.breakSwingMode = this.sgSwing.add(new EnumSetting.Builder<SwingMode>()
            .name("break-swing-mode").description("Mode for swinging your hand when breaking").defaultValue(SwingMode.None).build());
        this.placeSwingMode = this.sgSwing.add(new EnumSetting.Builder<SwingMode>()
            .name("place-swing-mode").description("Mode for swinging your hand when placing").defaultValue(SwingMode.None).build());

        this.placeRange = this.sgRange.add(new DoubleSetting.Builder()
            .name("place-range").description("Maximum distance to place crystals for").defaultValue(4.0).build());
        this.breakRange = this.sgRange.add(new DoubleSetting.Builder()
            .name("break-range").description("Maximum distance to break crystals for").defaultValue(4.0).build());

        this.forceBreakCrystals = new ArrayList<>();
        this.placePositionPool = new Pool<>(PlacePosition::new);
        this._placePositions = new ArrayList<>();
        this.mutablePos = new BlockPos.Mutable();
        this.explodedCrystals = new IntOpenHashSet();
        this.crystalBreakDelays = new HashMap<>();
        this.crystalPlaceDelays = new HashMap<>();
        this.cachedValidSpots = new ArrayList<>();
        this.lastPlaceTimeMS = 0L;
        this.lastBreakTimeMS = 0L;
        this._calcIgnoreSet = new HashSet<>();
        this.crystalPredictor = new CrystalPredictor();
    }

    @Override
    public void onActivate() {
        if (autoMine == null) {
            autoMine = Modules.get().get(AutoMine.class);
        }
        explodedCrystals.clear();
        crystalBreakDelays.clear();
        crystalPlaceDelays.clear();
        renderer.onActivate();
        crystalPredictor.reset();
        lastPredictedPos = null;
        cacheIsDirty = true;
        lastCachePlayerPos = null;
        tickCounter = 0;
    }

    private void update() {
        if (mc.player == null || mc.world == null || mc.world.getPlayers().isEmpty()) return;

        if (autoMine == null) {
            autoMine = Modules.get().get(AutoMine.class);
        }

        for (PlacePosition p : _placePositions) placePositionPool.free(p);
        _placePositions.clear();

        PlacePosition bestPlacePos = null;

        if (placeCrystals.get() && !(pauseEatPlace.get() && mc.player.isUsingItem())) {
            cachedValidPlaceSpots();

            for (PlayerEntity player : mc.world.getPlayers()) {
                if (player == mc.player) continue;
                if (Friends.get().isFriend(player)) continue;
                if (player.isSpectator()) continue;
                if (ignoreNakeds.get() && isPlayerNaked(player)) continue;
                if (player.squaredDistanceTo(mc.player.getEyePos()) > 144.0) continue;

                PlacePosition testPos = findBestPlacePosition(player);
                if (testPos != null && (bestPlacePos == null || testPos.damage > bestPlacePos.damage)) {
                    bestPlacePos = testPos;
                }
            }

            long currentTime = System.currentTimeMillis();
            if (bestPlacePos != null && placeSpeedCheck(bestPlacePos.isSlowPlace) && placeCrystal(bestPlacePos.blockPos.down())) {
                lastPlaceTimeMS = currentTime;
            }
        }

        if (breakCrystals.get() && !(pauseEatBreak.get() && mc.player.isUsingItem())) {
            List<Entity> breakCandidates = new ArrayList<>();
            for (Entity entity : mc.world.getEntities()) {
                if (!(entity instanceof EndCrystalEntity)) continue;
                if (!inBreakRange(entity.getPos())) continue;
                if (!shouldBreakCrystal(entity)) continue;
                breakCandidates.add(entity);
            }
            breakCandidates.sort((a, b) -> Double.compare(getMaxTargetDamage(b), getMaxTargetDamage(a)));

            for (Entity entity : breakCandidates) {
                if (breakThisTick) break;
                if (!breakSpeedCheck()) continue;
                if (rotateBreak.get() && !MeteorClient.ROTATION.lookingAt(entity.getBoundingBox())) {
                    MeteorClient.ROTATION.requestRotation(entity.getPos(), 10.0);
                    continue;
                }
                breakCrystal(entity);
            }
        }
    }

    public boolean placeCrystal(BlockPos blockPos) {
        if (placeThisTick || CrystalTickBudget.placeUsed) return false;
        if (blockPos == null || mc.player == null) return false;

        BlockPos crystalBlockPos = blockPos.up();
        Box box = new Box(
            crystalBlockPos.getX(), crystalBlockPos.getY(), crystalBlockPos.getZ(),
            crystalBlockPos.getX() + 1, crystalBlockPos.getY() + 2, crystalBlockPos.getZ() + 1);
        if (intersectsWithEntities(box)) return false;

        FindItemResult result = InvUtils.find(Items.END_CRYSTAL);
        if (!result.found()) return false;

        if (rotatePlace.get()) {
            MeteorClient.ROTATION.requestRotation(blockPos.toCenterPos(), 10.0);
            if (!MeteorClient.ROTATION.lookingAt(new Box(
                blockPos.getX(), blockPos.getY(), blockPos.getZ(),
                blockPos.getX() + 1, blockPos.getY() + 1, blockPos.getZ() + 1))) {
                return false;
            }
        }

        long currentTime = System.currentTimeMillis();
        if (crystalPlaceDelays.containsKey(blockPos) &&
            (double)(currentTime - crystalPlaceDelays.get(blockPos)) / 1000.0 < placeDelay.get()) {
            return false;
        }

        if (!MeteorClient.SWAP.beginSwap(result, true)) return false;

        crystalPlaceDelays.put(blockPos, currentTime);
        renderer.onPlaceCrystal(blockPos);

        BlockHitResult calculatedHitResult = AutoCrystalUtil.getPlaceBlockHitResult(blockPos);
        Hand hand = Hand.MAIN_HAND;
        int sequence = getSequence();
        mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(hand, calculatedHitResult, sequence));

        if (placeSwingMode.get() == SwingMode.Client) mc.player.swingHand(hand);
        if (placeSwingMode.get() == SwingMode.Packet) mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(hand));

        MeteorClient.SWAP.endSwap(true);
        placeThisTick = true;
        CrystalTickBudget.placeUsed = true;
        return true;
    }

    public boolean breakCrystal(Entity entity) {
        if (breakThisTick || CrystalTickBudget.breakUsed) return false;
        if (mc.player == null) return false;

        if (rotateBreak.get()) {
            MeteorClient.ROTATION.requestRotation(entity.getPos(), 10.0);
            if (!MeteorClient.ROTATION.lookingAt(entity.getBoundingBox())) return false;
        }

        long currentTime = System.currentTimeMillis();
        if (crystalBreakDelays.containsKey(entity.getId()) &&
            (double)(currentTime - crystalBreakDelays.get(entity.getId())) / 1000.0 < breakDelay.get()) {
            return false;
        }

        crystalBreakDelays.put(entity.getId(), currentTime);
        renderer.onBreakCrystal(entity);

        PlayerInteractEntityC2SPacket packet = PlayerInteractEntityC2SPacket.attack(entity, mc.player.isSneaking());
        mc.getNetworkHandler().sendPacket(packet);

        if (breakSwingMode.get() == SwingMode.Client) mc.player.swingHand(Hand.MAIN_HAND);
        if (breakSwingMode.get() == SwingMode.Packet) mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));

        explodedCrystals.add(entity.getId());
        lastBreakTimeMS = System.currentTimeMillis();
        breakThisTick = true;
        CrystalTickBudget.breakUsed = true;
        return true;
    }

    private PlacePosition findBestPlacePosition(PlayerEntity target) {
        PlacePosition bestPos = placePositionPool.get();
        _placePositions.add(bestPos);
        bestPos.damage = 0.0;
        bestPos.blockPos = null;
        bestPos.isSlowPlace = false;

        int r = (int) Math.floor(placeRange.get());
        BlockPos eyePos = BlockPos.ofFloored(mc.player.getEyePos());
        int ex = eyePos.getX();
        int ey = eyePos.getY();
        int ez = eyePos.getZ();

        boolean set = false;

        _calcIgnoreSet.clear();
        if (antiSurroundPlace.get()) {
            SilentMine silentMine = Modules.get().get(SilentMine.class);
            if (silentMine != null && silentMine.isActive()) {
                if (silentMine.getDelayedDestroyBlockPos() != null) _calcIgnoreSet.add(silentMine.getDelayedDestroyBlockPos());
                if (silentMine.getRebreakBlockPos() != null) _calcIgnoreSet.add(silentMine.getRebreakBlockPos());
            }
        }

        boolean shouldFacePlace = false;
        if (facePlaceMissingArmor.get()) {
            IPlayerInventory inv = (IPlayerInventory) target.getInventory();
            if (inv.meteor$getArmor().get(0).isEmpty() || inv.meteor$getArmor().get(1).isEmpty() ||
                inv.meteor$getArmor().get(2).isEmpty() || inv.meteor$getArmor().get(3).isEmpty()) {
                shouldFacePlace = true;
            }
        }
        if (forceFacePlaceKeybind.get().isPressed()) shouldFacePlace = true;

        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    int index = (x + r) * (2 * r + 1) * (2 * r + 1) + (y + r) * (2 * r + 1) + (z + r);
                    if (index < 0 || index >= cachedValidSpots.size()) continue;
                    if (!cachedValidSpots.get(index)) continue;

                    BlockPos pos = mutablePos.set(ex + x, ey + y, ez + z);
                    // pos is the air block; explosion center is at (pos.x+0.5, pos.y, pos.z+0.5)
                    Vec3d crystalCenter = new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                    double targetDamage;
                    if (prediction.get()) {
                        Vec3d predictedPos = crystalPredictor.predictSafe(target, predictionTicks.get());
                        lastPredictedPos = predictedPos;
                        Vec3d posOffset = predictedPos.subtract(target.getPos());
                        float damageAtPredicted = DamageUtils.crystalDamage(target, predictedPos, target.getBoundingBox().offset(posOffset), crystalCenter, DamageUtils.HIT_FACTORY);
                        float damageAtCurrent = DamageUtils.crystalDamage(target, crystalCenter);
                        double distToPredicted = crystalCenter.distanceTo(predictedPos);
                        double effectiveThreshold = predictBlockThreshold.get();
                        if (dynamicPingCompensation.get() && crystalPredictor.myPingTracker.isReady()) {
                            double myOneWay     = crystalPredictor.myPingTracker.getSmoothedOneWayMs();
                            double targetOneWay = crystalPredictor.targetPingTracker.getOneWayMs(target.getUuid());
                            effectiveThreshold += (myOneWay + targetOneWay) / 1000.0;
                        }
                        if (distToPredicted <= 12.0 + effectiveThreshold) {
                            targetDamage = Math.max(damageAtPredicted, damageAtCurrent);
                        } else {
                            targetDamage = damageAtCurrent;
                        }
                    } else {
                        targetDamage = DamageUtils.crystalDamage(target, crystalCenter);
                    }

                    boolean shouldSet = targetDamage >= (shouldFacePlace ? 1.0 : minPlace.get()) && targetDamage > bestPos.damage;
                    boolean isSlowPlacePos = false;

                    if (slowPlace.get() && targetDamage > bestPos.damage
                        && targetDamage <= slowPlaceMaxDamage.get()
                        && targetDamage >= slowPlaceMinDamage.get()) {
                        shouldSet = true;
                        isSlowPlacePos = true;
                    }

                    if (shouldSet) {
                        bestPos.blockPos = pos.toImmutable();
                        bestPos.damage = targetDamage;
                        bestPos.isSlowPlace = isSlowPlacePos;
                        set = true;
                    }
                }
            }
        }

        return set ? bestPos : null;
    }

    private void cachedValidPlaceSpots() {
        // Always refresh _calcIgnoreSet (fast, needed even on cache hit)
        _calcIgnoreSet.clear();
        if (antiSurroundPlace.get()) {
            SilentMine silentMine = Modules.get().get(SilentMine.class);
            if (silentMine != null && silentMine.isActive()) {
                if (silentMine.getDelayedDestroyBlockPos() != null) _calcIgnoreSet.add(silentMine.getDelayedDestroyBlockPos());
                if (silentMine.getRebreakBlockPos() != null) _calcIgnoreSet.add(silentMine.getRebreakBlockPos());
            }
        }

        // Dirty flag — skip full rebuild if world unchanged and player hasn't moved significantly
        if (!cacheIsDirty && lastCachePlayerPos != null && mc.player.getPos().distanceTo(lastCachePlayerPos) < 0.5) {
            return;
        }

        int r = (int) Math.floor(placeRange.get());
        BlockPos eyePos = BlockPos.ofFloored(mc.player.getEyePos());
        int ex = eyePos.getX();
        int ey = eyePos.getY();
        int ez = eyePos.getZ();
        Box box = new Box(0, 0, 0, 0, 0, 0);

        cachedValidSpots.clear();
        int totalSize = (2 * r + 1) * (2 * r + 1) * (2 * r + 1);
        while (cachedValidSpots.size() < totalSize) cachedValidSpots.add(false);

        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    if (mc.world.isAir(mutablePos.set(ex + x, ey + y, ez + z))) {
                        BlockPos downPos = mutablePos.set(ex + x, ey + y - 1, ez + z);
                        BlockState downState = mc.world.getBlockState(downPos);
                        Block downBlock = downState.getBlock();
                        if (!downState.isAir()
                            && (downBlock == Blocks.OBSIDIAN || downBlock == Blocks.BEDROCK)
                            && !_calcIgnoreSet.contains(downPos.toImmutable())
                            && inPlaceRange(downPos)) {
                            ((IBox) box).meteor$set(
                                downPos.getX(), downPos.getY() + 1, downPos.getZ(),
                                downPos.getX() + 1, downPos.getY() + 3, downPos.getZ() + 1);
                            if (!intersectsWithEntities(box)) {
                                Vec3d crystalCenter = new Vec3d(downPos.getX() + 0.5, downPos.getY() + 1, downPos.getZ() + 0.5);
                                double selfDamage = DamageUtils.crystalDamage(mc.player, crystalCenter);
                                if (selfDamage <= maxPlace.get()) {
                                    int index = (x + r) * (2 * r + 1) * (2 * r + 1) + (y + r) * (2 * r + 1) + (z + r);
                                    cachedValidSpots.set(index, true);
                                }
                            }
                        }
                    }
                }
            }
        }
        lastCachePlayerPos = mc.player.getPos();
        cacheIsDirty = false;
    }

    public void preplaceCrystal(BlockPos crystalBlockPos, boolean snapAt) {
        BlockPos blockPos = crystalBlockPos.down();
        crystalPlaceDelays.remove(blockPos);
        Box box = new Box(
            crystalBlockPos.getX(), crystalBlockPos.getY(), crystalBlockPos.getZ(),
            crystalBlockPos.getX() + 1, crystalBlockPos.getY() + 2, crystalBlockPos.getZ() + 1);
        if (!intersectsWithEntities(box)) {
            if (rotatePlace.get() && snapAt && !MeteorClient.ROTATION.lookingAt(new Box(
                blockPos.getX(), blockPos.getY(), blockPos.getZ(),
                blockPos.getX() + 1, blockPos.getY() + 1, blockPos.getZ() + 1))) {
                MeteorClient.ROTATION.snapAt(blockPos.toCenterPos());
            }
            placeCrystal(blockPos);
        }
    }

    public boolean inPlaceRange(BlockPos blockPos) {
        Vec3d from = mc.player.getEyePos();
        return blockPos.toCenterPos().distanceTo(from) <= placeRange.get();
    }

    public boolean inBreakRange(Vec3d pos) {
        Vec3d from = mc.player.getEyePos();
        return pos.distanceTo(from) <= breakRange.get();
    }

    public boolean shouldBreakCrystal(Entity entity) {
        boolean damageCheck = false;
        double selfDamage = DamageUtils.crystalDamage(mc.player, entity.getPos());
        if (selfDamage > maxBreak.get()) return false;

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;
            if (player.isSpectator()) continue;
            if (player.isDead()) continue;
            if (Friends.get().isFriend(player)) continue;
            if (player.squaredDistanceTo(mc.player.getEyePos()) > 196.0) continue;
            double targetDamage = DamageUtils.crystalDamage(player, entity.getPos());
            if (targetDamage >= minBreak.get()) {
                damageCheck = true;
                break;
            }
        }

        return damageCheck;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onEntity(EntityAddedEvent event) {
        Entity entity = event.entity;

        // Initialize predictor for new living entities (players)
        if (entity instanceof LivingEntity livingEntity && !(entity instanceof EndCrystalEntity)) {
            crystalPredictor.onEntityUpdate(livingEntity);
            return;
        }

        if (!(entity instanceof EndCrystalEntity)) return;

        BlockPos blockPos = entity.getBlockPos().down();
        if (crystalPlaceDelays.containsKey(blockPos)) crystalPlaceDelays.remove(blockPos);

        if (breakCrystals.get() && packetBreak.get()) {
            if (!inBreakRange(entity.getPos())) return;
            if (!shouldBreakCrystal(entity)) return;
            if (!breakSpeedCheck()) return;
            breakCrystal(entity);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onTick(TickEvent.Pre event) {
        if (!isActive() || mc.player == null || mc.world == null) return;
        tickCounter++;
        placeThisTick = false;
        breakThisTick = false;
        update();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onTickBudgetReset(TickEvent.Pre event) {
        CrystalTickBudget.reset();
        if (!isActive() || mc.player == null || mc.world == null) return;
        crystalPredictor.useKalman            = useKalman.get();
        crystalPredictor.useMarkov            = useMarkov.get();
        crystalPredictor.useKNN               = useKNN.get();
        crystalPredictor.accountForTargetPing = dynamicPingCompensation.get();
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;
            crystalPredictor.onEntityUpdate(player);
        }
        crystalPredictor.tick();
    }

    @EventHandler(priority = EventPriority.HIGHEST + 1)
    private void onRender3D(Render3DEvent event) {
        if (!isActive()) return;
        renderer.onRender3D(event);
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (event.packet instanceof BlockUpdateS2CPacket || event.packet instanceof ChunkDeltaUpdateS2CPacket) {
            cacheIsDirty = true;
        }
    }

    @EventHandler
    private void onPlayerDeath(PlayerDeathEvent.Death event) {
        if (event.getPlayer() == null || event.getPlayer() == mc.player) return;
        if (setPlayerDead.get()) {
            event.getPlayer().setHealth(0.0F);
        }
    }

    private boolean intersectsWithEntities(Box box) {
        return EntityUtils.intersectsWithEntity(box, entity ->
            !entity.isRemoved() && !explodedCrystals.contains(entity.getId())
        );
    }

    private boolean breakSpeedCheck() {
        long currentTime = System.currentTimeMillis();
        return breakSpeedLimit.get() == 0.0 ||
            (double)(currentTime - lastBreakTimeMS) / 1000.0 > 1.0 / breakSpeedLimit.get();
    }

    private boolean placeSpeedCheck(boolean isSlowPlace) {
        long currentTime = System.currentTimeMillis();
        double speed = isSlowPlace ? slowPlaceSpeed.get() : placeSpeedLimit.get();
        return speed == 0.0 || (double)(currentTime - lastPlaceTimeMS) / 1000.0 > 1.0 / speed;
    }

    public boolean isValidSpot(int x, int y, int z) {
        int r = (int) Math.floor(placeRange.get());
        int stride = 2 * r + 1;
        int index = (x + r) * stride * stride + (y + r) * stride + (z + r);
        if (index < 0 || index >= cachedValidSpots.size()) return false;
        return cachedValidSpots.get(index);
    }

    @Override
    public String getInfoString() {
        if (showPingDebug.get() && crystalPredictor.myPingTracker.isReady()) {
            return String.format("P:%d±%d T:%d",
                (int) crystalPredictor.myPingTracker.smoothedRTT,
                (int) crystalPredictor.myPingTracker.deviation,
                crystalPredictor.lastPredictionTicks);
        }
        long currentTime = System.currentTimeMillis();
        return String.format("%d", crystalBreakDelays.values().stream()
            .filter(x -> currentTime - x <= 1000L).count());
    }

    private boolean isPlayerNaked(PlayerEntity player) {
        IPlayerInventory inv = (IPlayerInventory) player.getInventory();
        return inv.meteor$getArmor().get(0).isEmpty()
            && inv.meteor$getArmor().get(1).isEmpty()
            && inv.meteor$getArmor().get(2).isEmpty()
            && inv.meteor$getArmor().get(3).isEmpty();
    }

    private int getSequence() {
        if (mc.world == null) return 0;
        return ((IClientWorld) mc.world).meteor$getAndIncrementSequence();
    }

    private double getMaxTargetDamage(Entity crystal) {
        double max = 0;
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player || player.isSpectator() || player.isDead() || Friends.get().isFriend(player)) continue;
            if (player.squaredDistanceTo(mc.player.getEyePos()) > 196.0) continue;
            double dmg = DamageUtils.crystalDamage(player, crystal.getPos());
            if (dmg > max) max = dmg;
        }
        return max;
    }

    public enum SwingMode {
        Packet, Client, None
    }

    private class PlacePosition {
        public BlockPos blockPos;
        public double damage = 0.0;
        public boolean isSlowPlace = false;
    }
}
