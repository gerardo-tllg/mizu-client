package meteordevelopment.meteorclient.systems.modules.hunting;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.GoalXZ;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.ArrayDeque;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1923;
import net.minecraft.class_1937;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2661;
import net.minecraft.class_2818;
import net.minecraft.class_5321;
import xaeroplus.XaeroPlus;
import xaeroplus.event.ChunkDataEvent;
import xaeroplus.module.ModuleManager;
import xaeroplus.module.impl.OldChunks;
import xaeroplus.module.impl.PaletteNewChunks;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/hunting/TrailFollower.class */
public class TrailFollower extends Module {
    private final SettingGroup sgGeneral;
    public final Setting<Integer> maxTrailLength;
    public final Setting<Integer> chunksBeforeStarting;
    public final Setting<Integer> chunkConsiderationWindow;
    public final Setting<TrailEndBehavior> trailEndBehavior;
    public final Setting<Double> trailEndYaw;
    public final Setting<OverworldFlightMode> overworldFlightMode;
    public final Setting<NetherPathMode> netherPathMode;
    public final Setting<Boolean> pitch40Firework;
    public final Setting<Double> rotateScaling;
    public final Setting<Boolean> oppositeDimension;
    public final Setting<Boolean> autoElytra;
    private final SettingGroup sgAdvanced;
    public final Setting<Double> pathDistance;
    public final Setting<FollowMode> flightMethod;
    public final Setting<Double> startDirectionWeighting;
    public final Setting<DirectionWeighting> directionWeighting;
    public final Setting<Integer> directionWeightingMultiplier;
    public final Setting<Boolean> only112;
    public final Setting<Double> chunkFoundTimeout;
    public final Setting<Double> circlingDegPerTick;
    public final Setting<Double> trailTimeout;
    public final Setting<Double> maxTrailDeviation;
    public final Setting<Integer> chunkCacheLength;
    public final Setting<String> webhookLink;
    public final Setting<Integer> baritoneUpdateTicks;
    public final Setting<Boolean> debug;
    private boolean oldAutoFireworkValue;
    private FollowMode followMode;
    private boolean followingTrail;
    private ArrayDeque<class_243> trail;
    private ArrayDeque<class_243> possibleTrail;
    private long lastFoundTrailTime;
    private long lastFoundPossibleTrailTime;
    private double pathDistanceActual;
    private Cache<Long, Byte> seenChunksCache;
    boolean started;
    private double targetYaw;
    private int baritoneSetGoalTicks;
    class_243 posDebug;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/hunting/TrailFollower$DirectionWeighting.class */
    public enum DirectionWeighting {
        LEFT,
        NONE,
        RIGHT
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/hunting/TrailFollower$FollowMode.class */
    public enum FollowMode {
        AUTO,
        BARITONE,
        YAWLOCK
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/hunting/TrailFollower$NetherPathMode.class */
    public enum NetherPathMode {
        AVERAGE,
        OTHER
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/hunting/TrailFollower$OverworldFlightMode.class */
    public enum OverworldFlightMode {
        VANILLA,
        PITCH40,
        OTHER
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/hunting/TrailFollower$TrailEndBehavior.class */
    public enum TrailEndBehavior {
        DISABLE,
        FLY_TOWARDS_YAW,
        DISCONNECT
    }

    public TrailFollower() {
        super(Categories.Hunting, "TrailFollower", "Automatically follows trails in all dimensions.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.maxTrailLength = this.sgGeneral.add(new IntSetting.Builder().name("max-trail-length").description("The number of trail points to keep for the average. Adjust to change how quickly the average will change. More does not necessarily equal better because if the list is too long it will contain chunks behind you.").defaultValue(20).sliderRange(1, 100).build());
        this.chunksBeforeStarting = this.sgGeneral.add(new IntSetting.Builder().name("chunks-before-starting").description("Useful for afking looking for a trail. The amount of chunks before it gets detected as a trail.").defaultValue(10).sliderRange(1, 50).build());
        this.chunkConsiderationWindow = this.sgGeneral.add(new IntSetting.Builder().name("chunk-timeframe").description("The amount of time in seconds that the chunks must be found in before starting.").defaultValue(5).sliderRange(1, 20).build());
        this.trailEndBehavior = this.sgGeneral.add(new EnumSetting.Builder().name("trail-end-behavior").description("What to do when the trail ends.").defaultValue(TrailEndBehavior.DISABLE).build());
        this.trailEndYaw = this.sgGeneral.add(new DoubleSetting.Builder().name("trail-end-yaw").description("The direction to go after the trail is abandoned.").defaultValue(0.0d).sliderRange(0.0d, 359.9d).visible(() -> {
            return this.trailEndBehavior.get() == TrailEndBehavior.FLY_TOWARDS_YAW;
        }).build());
        this.overworldFlightMode = this.sgGeneral.add(new EnumSetting.Builder().name("overworld-flight-mode").description("Choose how TrailFollower flies in Overworld. If other is selected then nothing will be automatically enabled, instead just your yaw will be changed to point towards the trail.").defaultValue(OverworldFlightMode.PITCH40).build());
        this.netherPathMode = this.sgGeneral.add(new EnumSetting.Builder().name("nether-path-mode").description("Choose how TrailFollower does baritone pathing in Nether. If other is selected then nothing will be automatically enabled, instead just your yaw will be changed to point towards the trail.").defaultValue(NetherPathMode.AVERAGE).build());
        this.pitch40Firework = this.sgGeneral.add(new BoolSetting.Builder().name("auto-firework").description("Uses a firework automatically if your velocity is too low.").defaultValue(true).visible(() -> {
            return this.overworldFlightMode.get() == OverworldFlightMode.PITCH40;
        }).build());
        this.rotateScaling = this.sgGeneral.add(new DoubleSetting.Builder().name("rotate-scaling").description("Scaling of how fast the yaw changes. 1 = instant, 0 = doesn't change").defaultValue(0.1d).sliderRange(0.0d, 1.0d).build());
        this.oppositeDimension = this.sgGeneral.add(new BoolSetting.Builder().name("opposite-dimension").description("Follows trails from the opposite dimension (Requires that you've already loaded the other dimension with XP).").defaultValue(false).build());
        this.autoElytra = this.sgGeneral.add(new BoolSetting.Builder().name("auto-start-baritone-elytra").description("Starts baritone elytra for you.").defaultValue(false).build());
        this.sgAdvanced = this.settings.createGroup("Advanced", false);
        this.pathDistance = this.sgAdvanced.add(new DoubleSetting.Builder().name("path-distance").description("The distance to add trail positions in the direction the player is facing. (Ignored when following overworld from nether)").defaultValue(500.0d).sliderRange(100.0d, 2000.0d).onChanged(value -> {
            this.pathDistanceActual = value.doubleValue();
        }).build());
        this.flightMethod = this.sgAdvanced.add(new EnumSetting.Builder().name("flight-method").description("Decided how the goals will be used. Leave this on AUTO unless you want to use yaw lock in the nether for example.").defaultValue(FollowMode.AUTO).build());
        this.startDirectionWeighting = this.sgAdvanced.add(new DoubleSetting.Builder().name("start-direction-weight").description("The weighting of the direction the player is facing when starting the trail. 0 for no weighting (not recommended) 1 for max weighting (will take a bit for direction to change)").defaultValue(0.5d).min(0.0d).sliderMax(1.0d).build());
        this.directionWeighting = this.sgAdvanced.add(new EnumSetting.Builder().name("direction-weighting").description("How the chunks found should be weighted. Useful for path splits. Left will weight chunks to the left of the player higher, right will weigh chunks to the right higher, and none will be in the middle/random. ").defaultValue(DirectionWeighting.NONE).build());
        this.directionWeightingMultiplier = this.sgAdvanced.add(new IntSetting.Builder().name("direction-weighting-multiplier").description("The multiplier for how much weight should be given to chunks in the direction specified. Values are capped to be in the range [2, maxTrailLength].").defaultValue(2).min(2).sliderMax(10).visible(() -> {
            return this.directionWeighting.get() != DirectionWeighting.NONE;
        }).build());
        this.only112 = this.sgAdvanced.add(new BoolSetting.Builder().name("follow-only-1.12").description("Will only follow 1.12 chunks and will ignore other ones.").defaultValue(false).build());
        this.chunkFoundTimeout = this.sgAdvanced.add(new DoubleSetting.Builder().name("chunk-found-timeout").description("The amount of MS without a chunk found to trigger circling.").defaultValue(5000.0d).min(1000.0d).sliderMax(10000.0d).build());
        this.circlingDegPerTick = this.sgAdvanced.add(new DoubleSetting.Builder().name("Circling-degrees-per-tick").description("The amount of degrees to change per tick while circling.").defaultValue(2.0d).min(1.0d).sliderMax(20.0d).build());
        this.trailTimeout = this.sgAdvanced.add(new DoubleSetting.Builder().name("trail-timeout").description("The amount of MS without a chunk found to stop following the trail.").defaultValue(30000.0d).min(10000.0d).sliderMax(60000.0d).build());
        this.maxTrailDeviation = this.sgAdvanced.add(new DoubleSetting.Builder().name("max-trail-deviation").description("Maximum allowed angle (in degrees) from the original trail direction. Helps avoid switching to intersecting trails.").defaultValue(180.0d).min(1.0d).sliderMax(270.0d).build());
        this.chunkCacheLength = this.sgAdvanced.add(new IntSetting.Builder().name("chunk-cache-length").description("The amount of chunks to keep in the cache. (Won't be applied until deactivating)").defaultValue(100000).sliderRange(0, 10000000).build());
        this.webhookLink = this.sgGeneral.add(new StringSetting.Builder().name("webhook-link").description("Will send all updates to the webhook link. Leave blank to disable.").defaultValue("").build());
        this.baritoneUpdateTicks = this.sgAdvanced.add(new IntSetting.Builder().name("baritone-path-update-ticks").description("The amount of ticks between updates to the baritone goal. Low values may cause high instability.").defaultValue(100).sliderRange(20, 600).build());
        this.debug = this.sgAdvanced.add(new BoolSetting.Builder().name("debug").description("Debug mode.").defaultValue(false).build());
        this.followingTrail = false;
        this.trail = new ArrayDeque<>();
        this.possibleTrail = new ArrayDeque<>();
        this.pathDistanceActual = this.pathDistance.get().doubleValue();
        this.seenChunksCache = Caffeine.newBuilder().maximumSize(this.chunkCacheLength.get().intValue()).expireAfterWrite(Duration.ofMinutes(5L)).build();
        this.started = false;
        this.baritoneSetGoalTicks = 0;
    }

    void resetTrail() {
        this.baritoneSetGoalTicks = 0;
        this.followingTrail = false;
        this.trail = new ArrayDeque<>();
        this.possibleTrail = new ArrayDeque<>();
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        resetTrail();
        XaeroPlus.EVENT_BUS.register(this);
        if (this.started) {
            if (this.mc.field_1724 != null && this.mc.field_1687 != null) {
                class_5321<class_1937> currentDimension = this.mc.field_1687.method_27983();
                if (this.oppositeDimension.get().booleanValue()) {
                    if (currentDimension.equals(class_1937.field_25181)) {
                        info("There is no opposite dimension to the end. Disabling TrailFollower", new Object[0]);
                        toggle();
                        return;
                    } else if (currentDimension.equals(class_1937.field_25180)) {
                        info("Following overworld trails from the nether is not supported yet, sorry. Disabling TrailFollower", new Object[0]);
                        toggle();
                        return;
                    }
                }
                if (this.flightMethod.get() != FollowMode.AUTO) {
                    this.followMode = this.flightMethod.get();
                } else if (!currentDimension.equals(class_1937.field_25180)) {
                    this.followMode = FollowMode.YAWLOCK;
                    info("You are in the overworld or end, basic yaw mode will be used.", new Object[0]);
                } else {
                    try {
                        Class.forName("baritone.api.BaritoneAPI");
                        this.followMode = FollowMode.BARITONE;
                        info("You are in the nether, baritone mode will be used.", new Object[0]);
                    } catch (ClassNotFoundException e) {
                        info("Baritone is required to trail follow in the nether. Disabling TrailFollower", new Object[0]);
                        toggle();
                        return;
                    }
                }
                if (this.followMode == FollowMode.YAWLOCK && !this.mc.field_1687.method_27983().equals(class_1937.field_25180)) {
                    if (this.overworldFlightMode.get() == OverworldFlightMode.PITCH40) {
                        info("Enable Movement ? ElytraFly (Mode: Pitch40) for pitch 40 flight.", new Object[0]);
                    } else if (this.overworldFlightMode.get() == OverworldFlightMode.VANILLA) {
                        AFKVanillaFly afkVanillaFly = (AFKVanillaFly) Modules.get().get(AFKVanillaFly.class);
                        if (!afkVanillaFly.isActive()) {
                            afkVanillaFly.toggle();
                        }
                    }
                }
                class_243 offset = new class_243(Math.sin((((double) (-this.mc.field_1724.method_36454())) * 3.141592653589793d) / 180.0d), 0.0d, Math.cos((((double) (-this.mc.field_1724.method_36454())) * 3.141592653589793d) / 180.0d)).method_1029().method_1021(this.pathDistance.get().doubleValue());
                class_243 targetPos = this.mc.field_1724.method_19538().method_1019(offset);
                for (int i = 0; i < ((double) this.maxTrailLength.get().intValue()) * this.startDirectionWeighting.get().doubleValue(); i++) {
                    this.trail.add(targetPos);
                }
                this.targetYaw = getActualYaw(this.mc.field_1724.method_36454());
            } else {
                toggle();
            }
            this.started = true;
        }
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onDeactivate() {
        Pitch40Util pitch40Util;
        this.started = false;
        this.seenChunksCache = Caffeine.newBuilder().maximumSize(this.chunkCacheLength.get().intValue()).expireAfterWrite(Duration.ofMinutes(5L)).build();
        XaeroPlus.EVENT_BUS.unregister(this);
        this.trail.clear();
        if (this.followMode == null) {
        }
        switch (this.followMode.ordinal()) {
            case 1:
                BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("cancel");
                break;
            case 2:
                if (this.mc.field_1687 != null && !this.mc.field_1687.method_27983().equals(class_1937.field_25180)) {
                    if (this.overworldFlightMode.get() == OverworldFlightMode.VANILLA) {
                        AFKVanillaFly afkVanillaFly = (AFKVanillaFly) Modules.get().get(AFKVanillaFly.class);
                        if (afkVanillaFly != null) {
                            afkVanillaFly.resetYLock();
                            if (afkVanillaFly.isActive()) {
                                afkVanillaFly.toggle();
                            }
                        }
                        break;
                    } else if (this.overworldFlightMode.get() == OverworldFlightMode.PITCH40 && (pitch40Util = (Pitch40Util) Modules.get().get(Pitch40Util.class)) != null && pitch40Util.isActive()) {
                        pitch40Util.toggle();
                        break;
                    }
                }
                break;
        }
    }

    private void circle() {
        if (this.followMode == FollowMode.BARITONE) {
            return;
        }
        this.mc.field_1724.method_36456(getActualYaw((float) (((double) this.mc.field_1724.method_36454()) + this.circlingDegPerTick.get().doubleValue())));
        if (this.mc.field_1724.field_6012 % 100 == 0) {
            log("Circling to look for new chunks, abandoning trail in " + ((this.trailTimeout.get().doubleValue() - (System.currentTimeMillis() - this.lastFoundTrailTime)) / 1000.0d) + " seconds.");
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        class_243 baritoneTarget;
        if (!this.started) {
            this.started = true;
            onActivate();
        }
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) {
            return;
        }
        if (this.followingTrail && System.currentTimeMillis() - this.lastFoundTrailTime > this.trailTimeout.get().doubleValue()) {
            resetTrail();
            log("Trail timed out, stopping.");
            switch (this.trailEndBehavior.get()) {
                case DISABLE:
                    toggle();
                    break;
                case FLY_TOWARDS_YAW:
                    this.targetYaw = this.trailEndYaw.get().doubleValue();
                    break;
                case DISCONNECT:
                    this.mc.field_1724.field_3944.method_52781(new class_2661(class_2561.method_43470("[TrailFollower] Trail timed out.")));
                    break;
            }
        }
        if (this.followingTrail && System.currentTimeMillis() - this.lastFoundTrailTime > this.chunkFoundTimeout.get().doubleValue()) {
            circle();
        }
        switch (this.followMode.ordinal()) {
            case 1:
                if (this.baritoneSetGoalTicks > 0) {
                    this.baritoneSetGoalTicks--;
                    break;
                } else if (this.baritoneSetGoalTicks == 0) {
                    this.baritoneSetGoalTicks = this.baritoneUpdateTicks.get().intValue();
                    if (this.mc.field_1687.method_27983().equals(class_1937.field_25180)) {
                        if (!this.trail.isEmpty()) {
                            if (this.netherPathMode.get() == NetherPathMode.AVERAGE) {
                                class_243 averagePos = calculateAveragePosition(this.trail);
                                class_243 directionVec = averagePos.method_1020(this.mc.field_1724.method_19538()).method_1029();
                                class_243 predictedPos = this.mc.field_1724.method_19538().method_1019(directionVec.method_1021(10.0d));
                                this.targetYaw = Rotations.getYaw(predictedPos);
                                baritoneTarget = HuntingUtils.positionInDirection(this.mc.field_1724.method_19538(), this.targetYaw, this.pathDistanceActual);
                            } else {
                                class_243 lastPos = this.trail.getLast();
                                baritoneTarget = lastPos;
                            }
                            BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalXZ((int) baritoneTarget.field_1352, (int) baritoneTarget.field_1350));
                        }
                    } else {
                        class_243 targetPos = HuntingUtils.positionInDirection(this.mc.field_1724.method_19538(), this.targetYaw, this.pathDistanceActual);
                        BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalXZ((int) targetPos.field_1352, (int) targetPos.field_1350));
                        this.targetYaw = Rotations.getYaw(targetPos);
                    }
                    if (this.autoElytra.get().booleanValue() && BaritoneAPI.getProvider().getPrimaryBaritone().getElytraProcess().currentDestination() == null) {
                        BaritoneAPI.getSettings().elytraTermsAccepted.value = true;
                        BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("elytra");
                        break;
                    }
                }
                break;
            case 2:
                this.mc.field_1724.method_36456(HuntingUtils.smoothRotation(getActualYaw(this.mc.field_1724.method_36454()), this.targetYaw, this.rotateScaling.get().doubleValue()));
                break;
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (this.debug.get().booleanValue()) {
            class_243 targetPos = HuntingUtils.positionInDirection(this.mc.field_1724.method_19538(), this.targetYaw, 10.0d);
            event.renderer.line(this.mc.field_1724.method_23317(), this.mc.field_1724.method_23318(), this.mc.field_1724.method_23321(), targetPos.field_1352, targetPos.field_1351, targetPos.field_1350, new Color(255, 0, 0));
            if (this.posDebug != null) {
                event.renderer.line(this.mc.field_1724.method_23317(), this.mc.field_1724.method_23318(), this.mc.field_1724.method_23321(), this.posDebug.field_1352, targetPos.field_1351, this.posDebug.field_1350, new Color(0, 0, 255));
            }
        }
    }

    @net.lenni0451.lambdaevents.EventHandler(priority = -1)
    public void onChunkData(ChunkDataEvent event) {
        if (event.seenChunk()) {
            return;
        }
        class_5321<class_1937> currentDimension = this.mc.field_1687.method_27983();
        class_2818 chunk = event.chunk();
        class_1923 chunkPos = chunk.method_12004();
        long chunkLong = chunkPos.method_8324();
        if (this.seenChunksCache.getIfPresent(Long.valueOf(chunkLong)) != null) {
            return;
        }
        class_1923 chunkDelta = new class_1923(chunkPos.field_9181 - this.mc.field_1724.method_31476().field_9181, chunkPos.field_9180 - this.mc.field_1724.method_31476().field_9180);
        if (this.oppositeDimension.get().booleanValue()) {
            if (currentDimension.equals(class_1937.field_25179)) {
                chunkPos = new class_1923((this.mc.field_1724.method_31476().field_9181 / 8) + chunkDelta.field_9181, (this.mc.field_1724.method_31476().field_9180 / 8) + chunkDelta.field_9180);
                currentDimension = class_1937.field_25180;
            } else if (currentDimension.equals(class_1937.field_25180)) {
                chunkPos = new class_1923((this.mc.field_1724.method_31476().field_9181 * 8) + chunkDelta.field_9181, (this.mc.field_1724.method_31476().field_9180 * 8) + chunkDelta.field_9180);
                currentDimension = class_1937.field_25179;
            }
        }
        if (isValidChunk(chunkPos, currentDimension)) {
            this.seenChunksCache.put(Long.valueOf(chunkLong), (byte) 127);
            class_243 pos = chunk.method_12004().method_33943(0).method_46558();
            this.posDebug = pos;
            if (!this.followingTrail) {
                if (System.currentTimeMillis() - this.lastFoundPossibleTrailTime > this.chunkConsiderationWindow.get().intValue() * 1000) {
                    this.possibleTrail.clear();
                }
                this.possibleTrail.add(pos);
                this.lastFoundPossibleTrailTime = System.currentTimeMillis();
                if (this.possibleTrail.size() > this.chunksBeforeStarting.get().intValue()) {
                    log("Trail found, starting to follow.");
                    this.followingTrail = true;
                    this.lastFoundTrailTime = System.currentTimeMillis();
                    this.trail.addAll(this.possibleTrail);
                    this.possibleTrail.clear();
                    return;
                }
                return;
            }
            double chunkAngle = Rotations.getYaw(pos);
            double angleDiff = HuntingUtils.angleDifference(this.targetYaw, chunkAngle);
            if (this.followingTrail && Math.abs(angleDiff) > this.maxTrailDeviation.get().doubleValue()) {
                return;
            }
            this.lastFoundTrailTime = System.currentTimeMillis();
            while (this.trail.size() >= this.maxTrailLength.get().intValue()) {
                this.trail.pollFirst();
            }
            if (angleDiff > 0.0d && angleDiff < 90.0d && this.directionWeighting.get() == DirectionWeighting.LEFT) {
                for (int i = 0; i < this.directionWeightingMultiplier.get().intValue() - 1; i++) {
                    this.trail.pollFirst();
                    this.trail.add(pos);
                }
                this.trail.add(pos);
            } else if (angleDiff < 0.0d && angleDiff > -90.0d && this.directionWeighting.get() == DirectionWeighting.RIGHT) {
                for (int i2 = 0; i2 < this.directionWeightingMultiplier.get().intValue() - 1; i2++) {
                    this.trail.pollFirst();
                    this.trail.add(pos);
                }
                this.trail.add(pos);
            } else {
                this.trail.add(pos);
            }
            if (!this.trail.isEmpty()) {
                if (this.followMode == FollowMode.YAWLOCK) {
                    class_243 averagePos = calculateAveragePosition(this.trail);
                    class_243 positionVec = averagePos.method_1020(this.mc.field_1724.method_19538()).method_1029();
                    class_243 targetPos = this.mc.field_1724.method_19538().method_1019(positionVec.method_1021(10.0d));
                    this.targetYaw = Rotations.getYaw(targetPos);
                    return;
                }
                class_243 lastTrailPoint = this.trail.getLast();
                this.targetYaw = Rotations.getYaw(lastTrailPoint);
            }
        }
    }

    private boolean isValidChunk(class_1923 chunkPos, class_5321<class_1937> currentDimension) {
        PaletteNewChunks paletteNewChunks = ModuleManager.getModule(PaletteNewChunks.class);
        boolean is119NewChunk = paletteNewChunks.isNewChunk(chunkPos.field_9181, chunkPos.field_9180, currentDimension);
        boolean is112OldChunk = ModuleManager.getModule(OldChunks.class).isOldChunk(chunkPos.field_9181, chunkPos.field_9180, currentDimension);
        boolean isHighlighted = is119NewChunk || paletteNewChunks.isInverseNewChunk(chunkPos.field_9181, chunkPos.field_9180, currentDimension);
        return isHighlighted && (!(is119NewChunk || this.only112.get().booleanValue()) || is112OldChunk);
    }

    private class_243 calculateAveragePosition(ArrayDeque<class_243> positions) {
        double sumX = 0.0d;
        double sumZ = 0.0d;
        for (class_243 pos : positions) {
            sumX += pos.field_1352;
            sumZ += pos.field_1350;
        }
        return new class_243(sumX / ((double) positions.size()), 0.0d, sumZ / ((double) positions.size()));
    }

    private float getActualYaw(float yaw) {
        return ((yaw % 360.0f) + 360.0f) % 360.0f;
    }

    private void log(String message) {
        info(message, new Object[0]);
        if (!this.webhookLink.get().isEmpty()) {
            HuntingUtils.sendWebhook(this.webhookLink.get(), "TrailFollower", message, null, this.mc.field_1724.method_7334().getName());
        }
    }
}
