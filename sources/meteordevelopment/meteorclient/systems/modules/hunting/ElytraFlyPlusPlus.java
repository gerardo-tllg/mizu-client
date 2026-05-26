package meteordevelopment.meteorclient.systems.modules.hunting;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalBlock;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.List;
import java.util.Objects;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.ChunkDataEvent;
import meteordevelopment.meteorclient.events.world.PlaySoundEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BlockPosSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.player.ChestSwap;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1304;
import net.minecraft.class_1313;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1923;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2645;
import net.minecraft.class_2708;
import net.minecraft.class_2848;
import net.minecraft.class_2960;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/hunting/ElytraFlyPlusPlus.class */
public class ElytraFlyPlusPlus extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgObstaclePasser;
    private final Setting<Boolean> bounce;
    private final Setting<Boolean> motionYBoost;
    private final Setting<Boolean> onlyWhileColliding;
    private final Setting<Boolean> tunnelBounce;
    private final Setting<Double> speed;
    private final Setting<Boolean> lockPitch;
    private final Setting<Double> pitch;
    private final Setting<Boolean> lockYaw;
    private final Setting<Boolean> useCustomYaw;
    private final Setting<Double> yaw;
    private final Setting<Boolean> highwayObstaclePasser;
    private final Setting<Boolean> useCustomStartPos;
    private final Setting<class_2338> startPos;
    private final Setting<Boolean> awayFromStartPos;
    private final Setting<Double> distance;
    private final Setting<Integer> targetY;
    private final Setting<Boolean> avoidPortalTraps;
    private final Setting<Double> portalAvoidDistance;
    private final Setting<Integer> portalScanWidth;
    private final Setting<Boolean> fakeFly;
    private final Setting<Boolean> toggleElytra;
    private boolean startSprinting;
    private class_2338 portalTrap;
    private boolean paused;
    private boolean elytraToggled;
    private class_243 lastUnstuckPos;
    private int stuckTimer;
    private class_243 lastPos;
    private final double maxDistance = 80.0d;
    private class_2338 tempPath;
    private boolean waitingForChunksToLoad;

    public ElytraFlyPlusPlus() {
        super(Categories.Hunting, "ElytraFlyPlusPlus", "Elytra fly with some more features.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgObstaclePasser = this.settings.createGroup("Obstacle Passer");
        this.bounce = this.sgGeneral.add(new BoolSetting.Builder().name("bounce").description("Automatically does bounce efly.").defaultValue(false).build());
        SettingGroup settingGroup = this.sgGeneral;
        BoolSetting.Builder builderDefaultValue = new BoolSetting.Builder().name("motion-y-boost").description("Greatly increases speed by cancelling Y momentum.").defaultValue(false);
        Setting<Boolean> setting = this.bounce;
        Objects.requireNonNull(setting);
        this.motionYBoost = settingGroup.add(builderDefaultValue.visible(setting::get).build());
        this.onlyWhileColliding = this.sgGeneral.add(new BoolSetting.Builder().name("only-while-colliding").description("Only enables motion y boost if colliding with a wall.").defaultValue(true).visible(() -> {
            return this.bounce.get().booleanValue() && this.motionYBoost.get().booleanValue();
        }).build());
        this.tunnelBounce = this.sgGeneral.add(new BoolSetting.Builder().name("tunnel-bounce").description("Allows you to bounce in 1x2 tunnels. This should not be on if you are not in a tunnel.").defaultValue(false).visible(() -> {
            return this.bounce.get().booleanValue() && this.motionYBoost.get().booleanValue();
        }).build());
        this.speed = this.sgGeneral.add(new DoubleSetting.Builder().name("speed").description("The speed in blocks per second to keep you at.").defaultValue(100.0d).sliderRange(20.0d, 250.0d).visible(() -> {
            return this.bounce.get().booleanValue() && this.motionYBoost.get().booleanValue();
        }).build());
        SettingGroup settingGroup2 = this.sgGeneral;
        BoolSetting.Builder builderDefaultValue2 = new BoolSetting.Builder().name("lock-pitch").description("Whether to lock your pitch when bounce is enabled.").defaultValue(true);
        Setting<Boolean> setting2 = this.bounce;
        Objects.requireNonNull(setting2);
        this.lockPitch = settingGroup2.add(builderDefaultValue2.visible(setting2::get).build());
        this.pitch = this.sgGeneral.add(new DoubleSetting.Builder().name("pitch").description("The pitch to set when bounce is enabled.").defaultValue(90.0d).sliderRange(-90.0d, 90.0d).visible(() -> {
            return this.bounce.get().booleanValue() && this.lockPitch.get().booleanValue();
        }).build());
        SettingGroup settingGroup3 = this.sgGeneral;
        BoolSetting.Builder builderDefaultValue3 = new BoolSetting.Builder().name("lock-yaw").description("Whether to lock your yaw when bounce is enabled.").defaultValue(false);
        Setting<Boolean> setting3 = this.bounce;
        Objects.requireNonNull(setting3);
        this.lockYaw = settingGroup3.add(builderDefaultValue3.visible(setting3::get).build());
        SettingGroup settingGroup4 = this.sgGeneral;
        BoolSetting.Builder builderDefaultValue4 = new BoolSetting.Builder().name("use-custom-yaw").description("Enable this if you want to use a yaw that isn't a factor of 45. WARNING: This effects the baritone goal for obstacle passer, use the default Rotations module if you only want a different yawlock.").defaultValue(false);
        Setting<Boolean> setting4 = this.bounce;
        Objects.requireNonNull(setting4);
        this.useCustomYaw = settingGroup4.add(builderDefaultValue4.visible(setting4::get).build());
        this.yaw = this.sgGeneral.add(new DoubleSetting.Builder().name("yaw").description("The yaw to set when bounce is enabled. This is auto set to the closest 45 deg angle to you unless Use Custom Yaw is enabled. WARNING: This effects the baritone goal for obstacle passer, use the default Rotations module if you only want a different yawlock.").defaultValue(0.0d).sliderRange(0.0d, 359.0d).visible(() -> {
            return this.bounce.get().booleanValue() && this.useCustomYaw.get().booleanValue();
        }).build());
        SettingGroup settingGroup5 = this.sgObstaclePasser;
        BoolSetting.Builder builderDefaultValue5 = new BoolSetting.Builder().name("highway-obstacle-passer").description("Uses baritone to pass obstacles.").defaultValue(false);
        Setting<Boolean> setting5 = this.bounce;
        Objects.requireNonNull(setting5);
        this.highwayObstaclePasser = settingGroup5.add(builderDefaultValue5.visible(setting5::get).build());
        this.useCustomStartPos = this.sgObstaclePasser.add(new BoolSetting.Builder().name("use-custom-start-position").description("Enable and set this ONLY if you are on a ringroad or don't want to be locked to a highway. Otherwise (0, 0) is the start position and will be automatically used.").defaultValue(false).visible(() -> {
            return this.bounce.get().booleanValue() && this.highwayObstaclePasser.get().booleanValue();
        }).build());
        this.startPos = this.sgObstaclePasser.add(new BlockPosSetting.Builder().name("start-position").description("The start position to use when using a custom start position.").defaultValue(new class_2338(0, 0, 0)).visible(() -> {
            return this.bounce.get().booleanValue() && this.highwayObstaclePasser.get().booleanValue() && this.useCustomStartPos.get().booleanValue();
        }).build());
        this.awayFromStartPos = this.sgObstaclePasser.add(new BoolSetting.Builder().name("away-from-start-position").description("If true, will go away from the start position instead of towards it. The start pos is (0,0) if it is not set to a custom start pos.").defaultValue(true).visible(() -> {
            return this.bounce.get().booleanValue() && this.highwayObstaclePasser.get().booleanValue();
        }).build());
        this.distance = this.sgObstaclePasser.add(new DoubleSetting.Builder().name("distance").description("The distance to set the baritone goal for path realignment.").defaultValue(10.0d).visible(() -> {
            return this.bounce.get().booleanValue() && this.highwayObstaclePasser.get().booleanValue();
        }).build());
        this.targetY = this.sgObstaclePasser.add(new IntSetting.Builder().name("y-level").description("The Y level to bounce at. This must be correct or bounce will not start properly.").defaultValue(Integer.valueOf(Opcode.ISHL)).visible(() -> {
            return this.bounce.get().booleanValue() && this.highwayObstaclePasser.get().booleanValue();
        }).build());
        this.avoidPortalTraps = this.sgObstaclePasser.add(new BoolSetting.Builder().name("avoid-portal-traps").description("Will attempt to detect portal traps on chunk load and avoid them.").defaultValue(false).visible(() -> {
            return this.bounce.get().booleanValue() && this.highwayObstaclePasser.get().booleanValue();
        }).build());
        this.portalAvoidDistance = this.sgObstaclePasser.add(new DoubleSetting.Builder().name("portal-avoid-distance").description("The distance to a portal trap where the obstacle passer will takeover and go around it.").defaultValue(20.0d).min(0.0d).sliderMax(50.0d).visible(() -> {
            return this.bounce.get().booleanValue() && this.highwayObstaclePasser.get().booleanValue() && this.avoidPortalTraps.get().booleanValue();
        }).build());
        this.portalScanWidth = this.sgObstaclePasser.add(new IntSetting.Builder().name("portal-scan-width").description("The width on the axis of the highway that will be scanned for portal traps.").defaultValue(5).min(3).sliderMax(10).visible(() -> {
            return this.bounce.get().booleanValue() && this.highwayObstaclePasser.get().booleanValue() && this.avoidPortalTraps.get().booleanValue();
        }).build());
        this.fakeFly = this.sgGeneral.add(new BoolSetting.Builder().name("chestplate-fakefly").description("Lets you fly using a chestplate to use almost 0 elytra durability. Must have elytra in hotbar.").defaultValue(false).build());
        this.toggleElytra = this.sgGeneral.add(new BoolSetting.Builder().name("toggle-elytra").description("Equips an elytra on activate, and a chestplate on deactivate.").defaultValue(false).visible(() -> {
            return !this.fakeFly.get().booleanValue();
        }).build());
        this.portalTrap = null;
        this.paused = false;
        this.elytraToggled = false;
        this.stuckTimer = 0;
        this.maxDistance = 80.0d;
        this.tempPath = null;
    }

    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        if (!(event.packet instanceof class_2708) && (event.packet instanceof class_2645)) {
            event.cancel();
        }
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        if (this.mc.field_1724 == null || this.mc.field_1724.method_31549().field_7478) {
            return;
        }
        this.startSprinting = this.mc.field_1724.method_5624();
        this.tempPath = null;
        this.portalTrap = null;
        this.paused = false;
        this.waitingForChunksToLoad = false;
        this.elytraToggled = false;
        this.lastPos = this.mc.field_1724.method_19538();
        this.lastUnstuckPos = this.mc.field_1724.method_19538();
        this.stuckTimer = 0;
        if (this.bounce.get().booleanValue() && this.mc.field_1724.method_19538().method_18805(1.0d, 0.0d, 1.0d).method_1033() >= 100.0d) {
            if (BaritoneAPI.getProvider().getPrimaryBaritone().getElytraProcess().currentDestination() == null) {
                BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoal((Goal) null);
            }
            if (!this.useCustomStartPos.get().booleanValue()) {
                this.startPos.set(new class_2338(0, 0, 0));
            }
            if (this.useCustomYaw.get().booleanValue()) {
                return;
            }
            if (this.mc.field_1724.method_24515().method_10262(this.startPos.get()) < 10000.0d || !this.highwayObstaclePasser.get().booleanValue()) {
                double playerAngleNormalized = HuntingUtils.angleOnAxis(this.mc.field_1724.method_36454());
                this.yaw.set(Double.valueOf(playerAngleNormalized));
                return;
            }
            class_2338 directionVec = this.mc.field_1724.method_24515().method_10059(this.startPos.get());
            double angle = Math.toDegrees(Math.atan2(-directionVec.method_10263(), directionVec.method_10260()));
            double angleNormalized = HuntingUtils.angleOnAxis(angle);
            if (!this.awayFromStartPos.get().booleanValue()) {
                angleNormalized += 180.0d;
            }
            this.yaw.set(Double.valueOf(angleNormalized));
        }
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) {
        if (this.mc.field_1724 != null && event.type == class_1313.field_6308 && enabled() && this.motionYBoost.get().booleanValue() && this.bounce.get().booleanValue()) {
            if (!this.onlyWhileColliding.get().booleanValue() || this.mc.field_1724.field_5976) {
                if (this.lastPos != null) {
                    double speedBps = this.mc.field_1724.method_19538().method_1020(this.lastPos).method_18805(20.0d, 0.0d, 20.0d).method_1033();
                    Timer timer = (Timer) Modules.get().get(Timer.class);
                    if (timer.isActive()) {
                        speedBps *= timer.getMultiplier();
                    }
                    if (this.mc.field_1724.method_24828() && this.mc.field_1724.method_5624() && speedBps < this.speed.get().doubleValue()) {
                        if (speedBps > 20.0d || this.tunnelBounce.get().booleanValue()) {
                            event.movement.meteor$setY(0.0d);
                        }
                        this.mc.field_1724.method_18800(this.mc.field_1724.method_18798().field_1352, 0.0d, this.mc.field_1724.method_18798().field_1350);
                    }
                }
                this.lastPos = this.mc.field_1724.method_19538();
            }
        }
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onDeactivate() {
        if (this.mc.field_1724 == null) {
            return;
        }
        if (this.bounce.get().booleanValue() && BaritoneAPI.getProvider().getPrimaryBaritone().getElytraProcess().currentDestination() == null) {
            BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoal((Goal) null);
        }
        this.mc.field_1724.method_5728(this.startSprinting);
        if (this.toggleElytra.get().booleanValue() && !this.fakeFly.get().booleanValue() && !this.mc.field_1724.method_6118(class_1304.field_6174).method_7909().toString().contains("chestplate")) {
            ((ChestSwap) Modules.get().get(ChestSwap.class)).swap();
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.mc.field_1724 == null || this.mc.field_1724.method_31549().field_7478) {
            return;
        }
        if (this.toggleElytra.get().booleanValue() && !this.fakeFly.get().booleanValue() && !this.elytraToggled) {
            if (!this.mc.field_1724.method_6118(class_1304.field_6174).method_7909().equals(class_1802.field_8833)) {
                ((ChestSwap) Modules.get().get(ChestSwap.class)).swap();
            } else {
                this.elytraToggled = true;
            }
        }
        if (enabled()) {
            this.mc.field_1724.method_5728(true);
        }
        if (this.bounce.get().booleanValue()) {
            if (this.tempPath != null && this.mc.field_1724.method_24515().method_10262(this.tempPath) < 500.0d) {
                this.tempPath = null;
                BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoal((Goal) null);
            } else if (this.tempPath != null) {
                BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalBlock(this.tempPath));
                return;
            }
            if (this.highwayObstaclePasser.get().booleanValue() && BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().getGoal() != null) {
                return;
            }
            if (this.mc.field_1724.method_5707(this.lastUnstuckPos) < 25.0d) {
                this.stuckTimer++;
            } else {
                this.stuckTimer = 0;
                this.lastUnstuckPos = this.mc.field_1724.method_19538();
            }
            if (this.highwayObstaclePasser.get().booleanValue() && this.mc.field_1724.method_19538().method_1033() > 100.0d && (this.mc.field_1724.method_23318() < this.targetY.get().intValue() || this.mc.field_1724.method_23318() > this.targetY.get().intValue() + 2 || ((this.mc.field_1724.field_5976 && !this.mc.field_1724.field_34927) || ((this.portalTrap != null && this.portalTrap.method_10262(this.mc.field_1724.method_24515()) < this.portalAvoidDistance.get().doubleValue() * this.portalAvoidDistance.get().doubleValue()) || this.waitingForChunksToLoad || this.stuckTimer > 50)))) {
                this.waitingForChunksToLoad = false;
                this.paused = true;
                class_2338 goal = this.mc.field_1724.method_24515();
                double currDistance = this.distance.get().doubleValue();
                if (this.portalTrap != null) {
                    currDistance += this.mc.field_1724.method_19538().method_1022(this.portalTrap.method_46558());
                    this.portalTrap = null;
                    info("Pathing around portal.", new Object[0]);
                }
                while (currDistance <= 80.0d) {
                    class_243 unitYawVec = HuntingUtils.yawToDirection(this.yaw.get().doubleValue());
                    class_243 travelVec = this.mc.field_1724.method_19538().method_1020(this.startPos.get().method_46558());
                    double parallelCurrPosDot = travelVec.method_18806(new class_243(1.0d, 0.0d, 1.0d)).method_1026(unitYawVec);
                    class_243 parallelCurrPosComponent = unitYawVec.method_1021(parallelCurrPosDot);
                    class_243 pos = HuntingUtils.positionInDirection(this.startPos.get().method_46558().method_1019(parallelCurrPosComponent), this.yaw.get().doubleValue(), currDistance);
                    goal = new class_2338((int) Math.floor(pos.field_1352), this.targetY.get().intValue(), (int) Math.floor(pos.field_1350));
                    currDistance += 1.0d;
                    if (this.mc.field_1687.method_8320(goal).method_26204() == class_2246.field_10243) {
                        this.waitingForChunksToLoad = true;
                        return;
                    } else if (this.mc.field_1687.method_8320(goal.method_10074()).method_26212(this.mc.field_1687, goal.method_10074()) && this.mc.field_1687.method_8320(goal).method_26204() != class_2246.field_10316 && this.mc.field_1687.method_8320(goal).method_26215()) {
                        BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalBlock(goal));
                    }
                }
                this.tempPath = goal;
                BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalBlock(goal));
                return;
            }
            this.paused = false;
            if (!enabled()) {
                return;
            }
            if (!this.fakeFly.get().booleanValue() && this.mc.field_1724.method_24828() && (!this.motionYBoost.get().booleanValue() || Utils.getPlayerSpeed().method_18805(1.0d, 0.0d, 1.0d).method_1033() < this.speed.get().doubleValue())) {
                this.mc.field_1724.method_6043();
            }
            if (this.lockYaw.get().booleanValue()) {
                this.mc.field_1724.method_36456(this.yaw.get().floatValue());
            }
            if (this.lockPitch.get().booleanValue()) {
                this.mc.field_1724.method_36457(this.pitch.get().floatValue());
            }
        }
        if (enabled()) {
            if (this.fakeFly.get().booleanValue()) {
                doGrimEflyStuff();
            } else {
                sendStartFlyingPacket();
            }
        }
    }

    public boolean enabled() {
        return isActive() && !this.paused && this.mc.field_1724 != null && (this.fakeFly.get().booleanValue() || this.mc.field_1724.method_6118(class_1304.field_6174).method_7909().equals(class_1802.field_8833));
    }

    private void doGrimEflyStuff() {
        FindItemResult itemResult = InvUtils.findInHotbar(class_1802.field_8833);
        if (itemResult.found()) {
            swapToItem(itemResult.slot());
            sendStartFlyingPacket();
            if (this.bounce.get().booleanValue() && this.mc.field_1724.method_24828() && (!this.motionYBoost.get().booleanValue() || Utils.getPlayerSpeed().method_18805(1.0d, 0.0d, 1.0d).method_1033() < this.speed.get().doubleValue())) {
                this.mc.field_1724.method_6043();
            }
            swapToItem(itemResult.slot());
        }
    }

    @EventHandler
    private void onPlaySound(PlaySoundEvent event) {
        if (this.fakeFly.get().booleanValue()) {
            List<class_2960> armorEquipSounds = List.of(class_2960.method_60654("minecraft:item.armor.equip_generic"), class_2960.method_60654("minecraft:item.armor.equip_netherite"), class_2960.method_60654("minecraft:item.armor.equip_elytra"), class_2960.method_60654("minecraft:item.armor.equip_diamond"), class_2960.method_60654("minecraft:item.armor.equip_gold"), class_2960.method_60654("minecraft:item.armor.equip_iron"), class_2960.method_60654("minecraft:item.armor.equip_chain"), class_2960.method_60654("minecraft:item.armor.equip_leather"), class_2960.method_60654("minecraft:item.elytra.flying"));
            for (class_2960 identifier : armorEquipSounds) {
                if (identifier.equals(event.sound.method_4775())) {
                    event.cancel();
                    return;
                }
            }
        }
    }

    private void swapToItem(int slot) {
        class_1799 chestItem = this.mc.field_1724.method_31548().method_5438(38);
        class_1799 hotbarSwapItem = this.mc.field_1724.method_31548().method_5438(slot);
        Int2ObjectOpenHashMap int2ObjectOpenHashMap = new Int2ObjectOpenHashMap();
        int2ObjectOpenHashMap.put(6, hotbarSwapItem);
        int2ObjectOpenHashMap.put(slot + 36, chestItem);
        sendSwapPacket(int2ObjectOpenHashMap, slot);
    }

    private void sendStartFlyingPacket() {
        if (this.mc.field_1724 == null) {
            return;
        }
        this.mc.field_1724.field_3944.method_52787(new class_2848(this.mc.field_1724, class_2848.class_2849.field_12982));
    }

    private void sendSwapPacket(Int2ObjectMap<class_1799> changedSlots, int buttonNum) {
        warning("Fake fly feature is currently disabled in 1.21.5", new Object[0]);
    }

    @EventHandler
    private void onChunkData(ChunkDataEvent event) {
        if (this.avoidPortalTraps.get().booleanValue() && this.highwayObstaclePasser.get().booleanValue()) {
            class_1923 pos = event.chunk().method_12004();
            class_2338 centerPos = pos.method_33943(this.targetY.get().intValue());
            class_243 moveDir = HuntingUtils.yawToDirection(this.yaw.get().doubleValue());
            double distanceToHighway = HuntingUtils.distancePointToDirection(class_243.method_24954(centerPos), moveDir, this.mc.field_1724.method_19538());
            if (distanceToHighway > 21.0d) {
                return;
            }
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = this.targetY.get().intValue(); y < this.targetY.get().intValue() + 3; y++) {
                        class_2338 position = new class_2338((pos.field_9181 * 16) + x, y, (pos.field_9180 * 16) + z);
                        if (HuntingUtils.distancePointToDirection(class_243.method_24954(position), moveDir, this.mc.field_1724.method_19538()) <= this.portalScanWidth.get().intValue() && this.mc.field_1687.method_8320(position).method_26204().equals(class_2246.field_10316)) {
                            class_2338 posBehind = new class_2338((int) Math.floor(((double) position.method_10263()) + moveDir.field_1352), position.method_10264(), (int) Math.floor(((double) position.method_10260()) + moveDir.field_1350));
                            if ((this.mc.field_1687.method_8320(posBehind).method_26212(this.mc.field_1687, posBehind) || this.mc.field_1687.method_8320(posBehind).method_26204() == class_2246.field_10316) && (this.portalTrap == null || (this.portalTrap.method_10262(posBehind) > 100.0d && this.mc.field_1724.method_24515().method_10262(posBehind) < this.mc.field_1724.method_24515().method_10262(this.portalTrap)))) {
                                this.portalTrap = posBehind;
                            }
                        }
                    }
                }
            }
        }
    }
}
