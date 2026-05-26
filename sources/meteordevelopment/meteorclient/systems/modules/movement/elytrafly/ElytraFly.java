package meteordevelopment.meteorclient.systems.modules.movement.elytrafly;

import java.util.Objects;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.modes.Bounce;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.modes.Packet;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.modes.Pitch40;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.modes.Slide;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.modes.Vanilla;
import meteordevelopment.meteorclient.systems.modules.player.ChestSwap;
import meteordevelopment.meteorclient.systems.modules.player.Rotation;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1304;
import net.minecraft.class_1802;
import net.minecraft.class_2248;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_2828;
import net.minecraft.class_2848;
import net.minecraft.class_3532;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_9334;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/movement/elytrafly/ElytraFly.class */
public class ElytraFly extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgInventory;
    private final SettingGroup sgAutopilot;
    public final Setting<ElytraFlightModes> flightMode;
    public final Setting<Boolean> autoTakeOff;
    public final Setting<Double> fallMultiplier;
    public final Setting<Double> horizontalSpeed;
    public final Setting<Double> verticalSpeed;
    public final Setting<Boolean> acceleration;
    public final Setting<Double> accelerationStep;
    public final Setting<Double> accelerationMin;
    public final Setting<Boolean> stopInWater;
    public final Setting<Boolean> dontGoIntoUnloadedChunks;
    public final Setting<Boolean> autoHover;
    public final Setting<Boolean> noCrash;
    public final Setting<Integer> crashLookAhead;
    private final Setting<Boolean> instaDrop;
    public final Setting<Double> pitch40lowerBounds;
    public final Setting<Double> pitch40upperBounds;
    public final Setting<Double> pitch40rotationSpeed;
    public final Setting<Boolean> autoJump;
    public final Setting<Rotation.LockMode> yawLockMode;
    public final Setting<Double> pitch;
    public final Setting<Double> yaw;
    public final Setting<Boolean> restart;
    public final Setting<Integer> restartDelay;
    public final Setting<Boolean> sprint;
    public final Setting<Double> slideAccel;
    public final Setting<Double> slideMaxSpeed;
    public final Setting<Boolean> replace;
    public final Setting<Integer> replaceDurability;
    public final Setting<ChestSwapMode> chestSwap;
    public final Setting<Boolean> autoReplenish;
    public final Setting<Integer> replenishSlot;
    public final Setting<Boolean> autoPilot;
    public final Setting<Boolean> useFireworks;
    public final Setting<Double> autoPilotFireworkDelay;
    public final Setting<Double> autoPilotMinimumHeight;
    private ElytraFlightMode currentMode;
    private final StaticGroundListener staticGroundListener;
    private final StaticInstaDropListener staticInstadropListener;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/movement/elytrafly/ElytraFly$AutoPilotMode.class */
    public enum AutoPilotMode {
        Vanilla,
        Pitch40
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/movement/elytrafly/ElytraFly$ChestSwapMode.class */
    public enum ChestSwapMode {
        Always,
        Never,
        WaitForGround
    }

    public ElytraFly() {
        super(Categories.Movement, "elytra-fly", "Gives you more control over your elytra.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgInventory = this.settings.createGroup("Inventory");
        this.sgAutopilot = this.settings.createGroup("Autopilot");
        this.flightMode = this.sgGeneral.add(new EnumSetting.Builder().name("mode").description("The mode of flying.").defaultValue(ElytraFlightModes.Vanilla).onModuleActivated(flightModesSetting -> {
            onModeChanged((ElytraFlightModes) flightModesSetting.get());
        }).onChanged(this::onModeChanged).build());
        this.autoTakeOff = this.sgGeneral.add(new BoolSetting.Builder().name("auto-take-off").description("Automatically takes off when you hold jump without needing to double jump.").defaultValue(false).visible(() -> {
            return (this.flightMode.get() == ElytraFlightModes.Pitch40 || this.flightMode.get() == ElytraFlightModes.Bounce || this.flightMode.get() == ElytraFlightModes.Slide) ? false : true;
        }).build());
        this.fallMultiplier = this.sgGeneral.add(new DoubleSetting.Builder().name("fall-multiplier").description("Controls how fast will you go down naturally.").defaultValue(0.01d).min(0.0d).visible(() -> {
            return (this.flightMode.get() == ElytraFlightModes.Pitch40 || this.flightMode.get() == ElytraFlightModes.Bounce || this.flightMode.get() == ElytraFlightModes.Slide) ? false : true;
        }).build());
        this.horizontalSpeed = this.sgGeneral.add(new DoubleSetting.Builder().name("horizontal-speed").description("How fast you go forward and backward.").defaultValue(1.0d).min(0.0d).visible(() -> {
            return (this.flightMode.get() == ElytraFlightModes.Pitch40 || this.flightMode.get() == ElytraFlightModes.Bounce || this.flightMode.get() == ElytraFlightModes.Slide) ? false : true;
        }).build());
        this.verticalSpeed = this.sgGeneral.add(new DoubleSetting.Builder().name("vertical-speed").description("How fast you go up and down.").defaultValue(1.0d).min(0.0d).visible(() -> {
            return (this.flightMode.get() == ElytraFlightModes.Pitch40 || this.flightMode.get() == ElytraFlightModes.Bounce || this.flightMode.get() == ElytraFlightModes.Slide) ? false : true;
        }).build());
        this.acceleration = this.sgGeneral.add(new BoolSetting.Builder().name("acceleration").defaultValue(false).visible(() -> {
            return (this.flightMode.get() == ElytraFlightModes.Pitch40 || this.flightMode.get() == ElytraFlightModes.Bounce || this.flightMode.get() == ElytraFlightModes.Slide) ? false : true;
        }).build());
        this.accelerationStep = this.sgGeneral.add(new DoubleSetting.Builder().name("acceleration-step").min(0.1d).max(5.0d).defaultValue(1.0d).visible(() -> {
            return (this.flightMode.get() == ElytraFlightModes.Pitch40 || !this.acceleration.get().booleanValue() || this.flightMode.get() == ElytraFlightModes.Bounce || this.flightMode.get() == ElytraFlightModes.Slide) ? false : true;
        }).build());
        this.accelerationMin = this.sgGeneral.add(new DoubleSetting.Builder().name("acceleration-start").min(0.1d).defaultValue(0.0d).visible(() -> {
            return (this.flightMode.get() == ElytraFlightModes.Pitch40 || !this.acceleration.get().booleanValue() || this.flightMode.get() == ElytraFlightModes.Bounce || this.flightMode.get() == ElytraFlightModes.Slide) ? false : true;
        }).build());
        this.stopInWater = this.sgGeneral.add(new BoolSetting.Builder().name("stop-in-water").description("Stops flying in water.").defaultValue(true).visible(() -> {
            return (this.flightMode.get() == ElytraFlightModes.Bounce || this.flightMode.get() == ElytraFlightModes.Slide) ? false : true;
        }).build());
        this.dontGoIntoUnloadedChunks = this.sgGeneral.add(new BoolSetting.Builder().name("no-unloaded-chunks").description("Stops you from going into unloaded chunks.").defaultValue(true).build());
        this.autoHover = this.sgGeneral.add(new BoolSetting.Builder().name("auto-hover").description("Automatically hover .3 blocks off ground when holding shift.").defaultValue(false).visible(() -> {
            return (this.flightMode.get() == ElytraFlightModes.Bounce || this.flightMode.get() == ElytraFlightModes.Slide) ? false : true;
        }).build());
        this.noCrash = this.sgGeneral.add(new BoolSetting.Builder().name("no-crash").description("Stops you from going into walls.").defaultValue(false).visible(() -> {
            return (this.flightMode.get() == ElytraFlightModes.Bounce || this.flightMode.get() == ElytraFlightModes.Slide) ? false : true;
        }).build());
        this.crashLookAhead = this.sgGeneral.add(new IntSetting.Builder().name("crash-look-ahead").description("Distance to look ahead when flying.").defaultValue(5).range(1, 15).sliderMin(1).visible(() -> {
            return (!this.noCrash.get().booleanValue() || this.flightMode.get() == ElytraFlightModes.Bounce || this.flightMode.get() == ElytraFlightModes.Slide) ? false : true;
        }).build());
        this.instaDrop = this.sgGeneral.add(new BoolSetting.Builder().name("insta-drop").description("Makes you drop out of flight instantly.").defaultValue(false).visible(() -> {
            return (this.flightMode.get() == ElytraFlightModes.Bounce || this.flightMode.get() == ElytraFlightModes.Slide) ? false : true;
        }).build());
        this.pitch40lowerBounds = this.sgGeneral.add(new DoubleSetting.Builder().name("pitch40-lower-bounds").description("The bottom height boundary for pitch40.").defaultValue(80.0d).min(-128.0d).sliderMax(360.0d).visible(() -> {
            return this.flightMode.get() == ElytraFlightModes.Pitch40;
        }).build());
        this.pitch40upperBounds = this.sgGeneral.add(new DoubleSetting.Builder().name("pitch40-upper-bounds").description("The upper height boundary for pitch40.").defaultValue(120.0d).min(-128.0d).sliderMax(360.0d).visible(() -> {
            return this.flightMode.get() == ElytraFlightModes.Pitch40;
        }).build());
        this.pitch40rotationSpeed = this.sgGeneral.add(new DoubleSetting.Builder().name("pitch40-rotate-speed").description("The speed for pitch rotation (degrees per tick)").defaultValue(4.0d).min(1.0d).sliderMax(6.0d).visible(() -> {
            return this.flightMode.get() == ElytraFlightModes.Pitch40;
        }).build());
        this.autoJump = this.sgGeneral.add(new BoolSetting.Builder().name("auto-jump").description("Automatically jumps for you.").defaultValue(true).visible(() -> {
            return this.flightMode.get() == ElytraFlightModes.Bounce;
        }).build());
        this.yawLockMode = this.sgGeneral.add(new EnumSetting.Builder().name("yaw-lock").description("Whether to enable yaw lock or not").defaultValue(Rotation.LockMode.Smart).visible(() -> {
            return this.flightMode.get() == ElytraFlightModes.Bounce;
        }).build());
        this.pitch = this.sgGeneral.add(new DoubleSetting.Builder().name("pitch").description("The pitch angle to look at when using the bounce mode.").defaultValue(85.0d).range(0.0d, 90.0d).sliderRange(0.0d, 90.0d).visible(() -> {
            return this.flightMode.get() == ElytraFlightModes.Bounce;
        }).build());
        this.yaw = this.sgGeneral.add(new DoubleSetting.Builder().name("yaw").description("The yaw angle to look at when using simple rotation lock in bounce mode.").defaultValue(0.0d).range(0.0d, 360.0d).sliderRange(0.0d, 360.0d).visible(() -> {
            return this.flightMode.get() == ElytraFlightModes.Bounce && this.yawLockMode.get() == Rotation.LockMode.Simple;
        }).build());
        this.restart = this.sgGeneral.add(new BoolSetting.Builder().name("restart").description("Restarts flying with the elytra when rubberbanding.").defaultValue(true).visible(() -> {
            return this.flightMode.get() == ElytraFlightModes.Bounce;
        }).build());
        this.restartDelay = this.sgGeneral.add(new IntSetting.Builder().name("restart-delay").description("How many ticks to wait before restarting the elytra again after rubberbanding.").defaultValue(7).min(0).sliderRange(0, 20).visible(() -> {
            return this.flightMode.get() == ElytraFlightModes.Bounce && this.restart.get().booleanValue();
        }).build());
        this.sprint = this.sgGeneral.add(new BoolSetting.Builder().name("sprint").description("Sprints all the time. If turned off, it will only sprint when the player is touching the ground.").defaultValue(true).visible(() -> {
            return this.flightMode.get() == ElytraFlightModes.Bounce;
        }).build());
        this.slideAccel = this.sgGeneral.add(new DoubleSetting.Builder().name("slide-accel-speed").description("The acceleration of slide.").defaultValue(7.0d).min(0.0d).sliderMax(5.0d).visible(() -> {
            return this.flightMode.get() == ElytraFlightModes.Slide;
        }).build());
        this.slideMaxSpeed = this.sgGeneral.add(new DoubleSetting.Builder().name("slide-max-speed").description("The maximum speed of slide").defaultValue(7.0d).min(0.0d).sliderMax(200.0d).visible(() -> {
            return this.flightMode.get() == ElytraFlightModes.Slide;
        }).build());
        this.replace = this.sgInventory.add(new BoolSetting.Builder().name("elytra-replace").description("Replaces broken elytra with a new elytra.").defaultValue(false).build());
        SettingGroup settingGroup = this.sgInventory;
        IntSetting.Builder builderSliderRange = new IntSetting.Builder().name("replace-durability").description("The durability threshold your elytra will be replaced at.").defaultValue(2).range(1, ((Integer) class_1802.field_8833.method_57347().method_58694(class_9334.field_50072)).intValue() - 1).sliderRange(1, ((Integer) class_1802.field_8833.method_57347().method_58694(class_9334.field_50072)).intValue() - 1);
        Setting<Boolean> setting = this.replace;
        Objects.requireNonNull(setting);
        this.replaceDurability = settingGroup.add(builderSliderRange.visible(setting::get).build());
        this.chestSwap = this.sgInventory.add(new EnumSetting.Builder().name("chest-swap").description("Enables ChestSwap when toggling this module.").defaultValue(ChestSwapMode.Never).build());
        this.autoReplenish = this.sgInventory.add(new BoolSetting.Builder().name("replenish-fireworks").description("Moves fireworks into a selected hotbar slot.").defaultValue(false).build());
        SettingGroup settingGroup2 = this.sgInventory;
        IntSetting.Builder builderSliderRange2 = new IntSetting.Builder().name("replenish-slot").description("The slot auto move moves fireworks to.").defaultValue(9).range(1, 9).sliderRange(1, 9);
        Setting<Boolean> setting2 = this.autoReplenish;
        Objects.requireNonNull(setting2);
        this.replenishSlot = settingGroup2.add(builderSliderRange2.visible(setting2::get).build());
        this.autoPilot = this.sgAutopilot.add(new BoolSetting.Builder().name("auto-pilot").description("Moves forward while elytra flying.").defaultValue(false).visible(() -> {
            return (this.flightMode.get() == ElytraFlightModes.Pitch40 || this.flightMode.get() == ElytraFlightModes.Bounce || this.flightMode.get() == ElytraFlightModes.Slide) ? false : true;
        }).build());
        this.useFireworks = this.sgAutopilot.add(new BoolSetting.Builder().name("use-fireworks").description("Uses firework rockets every second of your choice.").defaultValue(false).visible(() -> {
            return (!this.autoPilot.get().booleanValue() || this.flightMode.get() == ElytraFlightModes.Pitch40 || this.flightMode.get() == ElytraFlightModes.Bounce || this.flightMode.get() == ElytraFlightModes.Slide) ? false : true;
        }).build());
        this.autoPilotFireworkDelay = this.sgAutopilot.add(new DoubleSetting.Builder().name("firework-delay").description("The delay in seconds in between using fireworks if \"Use Fireworks\" is enabled.").min(1.0d).defaultValue(8.0d).sliderMax(20.0d).visible(() -> {
            return (!this.useFireworks.get().booleanValue() || this.flightMode.get() == ElytraFlightModes.Pitch40 || this.flightMode.get() == ElytraFlightModes.Bounce || this.flightMode.get() == ElytraFlightModes.Slide) ? false : true;
        }).build());
        this.autoPilotMinimumHeight = this.sgAutopilot.add(new DoubleSetting.Builder().name("minimum-height").description("The minimum height for autopilot.").defaultValue(120.0d).min(-128.0d).sliderMax(260.0d).visible(() -> {
            return (!this.autoPilot.get().booleanValue() || this.flightMode.get() == ElytraFlightModes.Pitch40 || this.flightMode.get() == ElytraFlightModes.Bounce || this.flightMode.get() == ElytraFlightModes.Slide) ? false : true;
        }).build());
        this.currentMode = new Vanilla();
        this.staticGroundListener = new StaticGroundListener();
        this.staticInstadropListener = new StaticInstaDropListener();
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        this.currentMode.onActivate();
        if ((this.chestSwap.get() == ChestSwapMode.Always || this.chestSwap.get() == ChestSwapMode.WaitForGround) && this.mc.field_1724.method_6118(class_1304.field_6174).method_7909() != class_1802.field_8833 && isActive()) {
            ((ChestSwap) Modules.get().get(ChestSwap.class)).swap();
        }
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onDeactivate() {
        if (this.autoPilot.get().booleanValue()) {
            this.mc.field_1690.field_1894.method_23481(false);
        }
        if (this.chestSwap.get() == ChestSwapMode.Always && this.mc.field_1724.method_6118(class_1304.field_6174).method_7909() == class_1802.field_8833) {
            ((ChestSwap) Modules.get().get(ChestSwap.class)).swap();
        } else if (this.chestSwap.get() == ChestSwapMode.WaitForGround) {
            enableGroundListener();
        }
        if (this.mc.field_1724.method_6128() && this.instaDrop.get().booleanValue()) {
            enableInstaDropListener();
        }
        this.currentMode.onDeactivate();
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) {
        if (this.mc.field_1724.method_6118(class_1304.field_6174).method_7909() != class_1802.field_8833) {
            return;
        }
        this.currentMode.autoTakeoff();
        if (this.mc.field_1724.method_6128()) {
            if (this.flightMode.get() != ElytraFlightModes.Bounce && this.flightMode.get() != ElytraFlightModes.Slide) {
                this.currentMode.velX = 0.0d;
                this.currentMode.velY = event.movement.field_1351;
                this.currentMode.velZ = 0.0d;
                this.currentMode.forward = class_243.method_1030(0.0f, this.mc.field_1724.method_36454()).method_1021(0.1d);
                this.currentMode.right = class_243.method_1030(0.0f, this.mc.field_1724.method_36454() + 90.0f).method_1021(0.1d);
                if (this.mc.field_1724.method_5799() && this.stopInWater.get().booleanValue()) {
                    this.mc.method_1562().method_52787(new class_2848(this.mc.field_1724, class_2848.class_2849.field_12982));
                    return;
                }
                this.currentMode.handleFallMultiplier();
                this.currentMode.handleAutopilot();
                this.currentMode.handleAcceleration();
                this.currentMode.handleHorizontalSpeed(event);
                this.currentMode.handleVerticalSpeed(event);
            }
            int chunkX = (int) ((this.mc.field_1724.method_23317() + this.currentMode.velX) / 16.0d);
            int chunkZ = (int) ((this.mc.field_1724.method_23321() + this.currentMode.velZ) / 16.0d);
            if (this.dontGoIntoUnloadedChunks.get().booleanValue()) {
                if (this.mc.field_1687.method_2935().method_12123(chunkX, chunkZ)) {
                    if (this.flightMode.get() != ElytraFlightModes.Bounce && this.flightMode.get() != ElytraFlightModes.Slide) {
                        event.movement.meteor$set(this.currentMode.velX, this.currentMode.velY, this.currentMode.velZ);
                    }
                } else {
                    this.currentMode.zeroAcceleration();
                    event.movement.meteor$set(0.0d, this.currentMode.velY, 0.0d);
                }
            } else if (this.flightMode.get() != ElytraFlightModes.Bounce && this.flightMode.get() != ElytraFlightModes.Slide) {
                event.movement.meteor$set(this.currentMode.velX, this.currentMode.velY, this.currentMode.velZ);
            }
            if (this.flightMode.get() != ElytraFlightModes.Bounce && this.flightMode.get() != ElytraFlightModes.Slide) {
                this.currentMode.onPlayerMove();
            }
        } else if (this.currentMode.lastForwardPressed && this.flightMode.get() != ElytraFlightModes.Bounce && this.flightMode.get() != ElytraFlightModes.Slide) {
            this.mc.field_1690.field_1894.method_23481(false);
            this.currentMode.lastForwardPressed = false;
        }
        if (this.noCrash.get().booleanValue() && this.mc.field_1724.method_6128() && this.flightMode.get() != ElytraFlightModes.Bounce && this.flightMode.get() != ElytraFlightModes.Slide) {
            class_243 lookAheadPos = this.mc.field_1724.method_19538().method_1019(this.mc.field_1724.method_18798().method_1029().method_1021(this.crashLookAhead.get().intValue()));
            class_3959 raycastContext = new class_3959(this.mc.field_1724.method_19538(), new class_243(lookAheadPos.method_10216(), this.mc.field_1724.method_23318(), lookAheadPos.method_10215()), class_3959.class_3960.field_17558, class_3959.class_242.field_1348, this.mc.field_1724);
            class_3965 hitResult = this.mc.field_1687.method_17742(raycastContext);
            if (hitResult != null && hitResult.method_17783() == class_239.class_240.field_1332) {
                event.movement.meteor$set(0.0d, this.currentMode.velY, 0.0d);
            }
        }
        if (this.autoHover.get().booleanValue() && this.mc.field_1724.method_5715() && !((Freecam) Modules.get().get(Freecam.class)).isActive() && this.mc.field_1724.method_6128() && this.flightMode.get() != ElytraFlightModes.Bounce && this.flightMode.get() != ElytraFlightModes.Slide) {
            class_2680 underState = this.mc.field_1687.method_8320(this.mc.field_1724.method_24515().method_10074());
            class_2248 under = underState.method_26204();
            class_2680 under2State = this.mc.field_1687.method_8320(this.mc.field_1724.method_24515().method_10074().method_10074());
            class_2248 under2 = under2State.method_26204();
            boolean underCollidable = under.field_23159 || !underState.method_26227().method_15769();
            boolean under2Collidable = under2.field_23159 || !under2State.method_26227().method_15769();
            if (!underCollidable && under2Collidable) {
                event.movement.meteor$set(event.movement.field_1352, -0.10000000149011612d, event.movement.field_1350);
                this.mc.field_1724.method_36457(class_3532.method_15363(this.mc.field_1724.method_5695(0.0f), -50.0f, 20.0f));
            }
            if (underCollidable) {
                event.movement.meteor$set(event.movement.field_1352, -0.029999999329447746d, event.movement.field_1350);
                this.mc.field_1724.method_36457(class_3532.method_15363(this.mc.field_1724.method_5695(0.0f), -50.0f, 20.0f));
                if (this.mc.field_1724.method_19538().field_1351 <= this.mc.field_1724.method_24515().method_10074().method_10264() + 1.34f) {
                    event.movement.meteor$set(event.movement.field_1352, 0.0d, event.movement.field_1350);
                    this.mc.field_1724.method_5660(false);
                }
            }
        }
    }

    public boolean canPacketEfly() {
        return isActive() && this.flightMode.get() == ElytraFlightModes.Packet && this.mc.field_1724.method_6118(class_1304.field_6174).method_7909() == class_1802.field_8833 && !this.mc.field_1724.method_24828();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        this.currentMode.onTick();
    }

    @EventHandler
    private void onPreTick(TickEvent.Pre event) {
        this.currentMode.onPreTick();
    }

    @EventHandler
    private void onPacketSend(PacketEvent.Send event) {
        this.currentMode.onPacketSend(event);
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        this.currentMode.onPacketReceive(event);
    }

    private void onModeChanged(ElytraFlightModes mode) {
        switch (mode) {
            case Vanilla:
                this.currentMode = new Vanilla();
                break;
            case Packet:
                this.currentMode = new Packet();
                break;
            case Pitch40:
                this.currentMode = new Pitch40();
                this.autoPilot.set(false);
                break;
            case Bounce:
                this.currentMode = new Bounce();
                break;
            case Slide:
                this.currentMode = new Slide();
                break;
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/movement/elytrafly/ElytraFly$StaticGroundListener.class */
    private class StaticGroundListener {
        private StaticGroundListener() {
        }

        @EventHandler
        private void chestSwapGroundListener(PlayerMoveEvent event) {
            if (ElytraFly.this.mc.field_1724 != null && ElytraFly.this.mc.field_1724.method_24828() && ElytraFly.this.mc.field_1724.method_6118(class_1304.field_6174).method_7909() == class_1802.field_8833) {
                ((ChestSwap) Modules.get().get(ChestSwap.class)).swap();
                ElytraFly.this.disableGroundListener();
            }
        }
    }

    protected void enableGroundListener() {
        MeteorClient.EVENT_BUS.subscribe(this.staticGroundListener);
    }

    protected void disableGroundListener() {
        MeteorClient.EVENT_BUS.unsubscribe(this.staticGroundListener);
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/movement/elytrafly/ElytraFly$StaticInstaDropListener.class */
    private class StaticInstaDropListener {
        private StaticInstaDropListener() {
        }

        @EventHandler
        private void onInstadropTick(TickEvent.Post event) {
            if (ElytraFly.this.mc.field_1724 != null && ElytraFly.this.mc.field_1724.method_6128()) {
                ElytraFly.this.mc.field_1724.method_18800(0.0d, 0.0d, 0.0d);
                ElytraFly.this.mc.field_1724.field_3944.method_52787(new class_2828.class_5911(true, ElytraFly.this.mc.field_1724.field_5976));
            } else {
                ElytraFly.this.disableInstaDropListener();
            }
        }
    }

    protected void enableInstaDropListener() {
        MeteorClient.EVENT_BUS.subscribe(this.staticInstadropListener);
    }

    protected void disableInstaDropListener() {
        MeteorClient.EVENT_BUS.unsubscribe(this.staticInstadropListener);
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public String getInfoString() {
        return this.currentMode.getHudString();
    }
}
