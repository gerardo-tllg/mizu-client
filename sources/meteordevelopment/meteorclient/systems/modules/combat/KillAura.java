package meteordevelopment.meteorclient.systems.modules.combat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.pathing.PathManagers;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.Target;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.world.TickRate;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1309;
import net.minecraft.class_1429;
import net.minecraft.class_1493;
import net.minecraft.class_1560;
import net.minecraft.class_1590;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1934;
import net.minecraft.class_238;
import net.minecraft.class_2868;
import net.minecraft.class_3489;
import net.minecraft.class_3532;
import net.minecraft.class_6025;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/KillAura.class */
public class KillAura extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgTargeting;
    private final SettingGroup sgTiming;
    private final Setting<Weapon> weapon;
    private final Setting<RotationMode> rotation;
    private final Setting<Boolean> autoSwitch;
    private final Setting<Boolean> onlyOnClick;
    private final Setting<Boolean> onlyOnLook;
    private final Setting<Boolean> pauseOnCombat;
    private final Setting<ShieldMode> shieldMode;
    private final Setting<Set<class_1299<?>>> entities;
    private final Setting<SortPriority> priority;
    private final Setting<Integer> maxTargets;
    private final Setting<Double> range;
    private final Setting<Double> wallsRange;
    private final Setting<EntityAge> mobAgeFilter;
    private final Setting<Boolean> ignoreNamed;
    private final Setting<Boolean> ignorePassive;
    private final Setting<Boolean> ignoreTamed;
    private final Setting<Boolean> pauseOnLag;
    private final Setting<Boolean> pauseOnUse;
    private final Setting<Boolean> tpsSync;
    private final Setting<Boolean> customDelay;
    private final Setting<Integer> hitDelay;
    private final Setting<Integer> switchDelay;
    private final List<class_1297> targets;
    private int switchTimer;
    private int hitTimer;
    private boolean wasPathing;
    public boolean attacking;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/KillAura$EntityAge.class */
    public enum EntityAge {
        Baby,
        Adult,
        Both
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/KillAura$RotationMode.class */
    public enum RotationMode {
        Always,
        OnHit,
        None
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/KillAura$ShieldMode.class */
    public enum ShieldMode {
        Ignore,
        Break,
        None
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/KillAura$Weapon.class */
    public enum Weapon {
        Sword,
        Axe,
        Mace,
        Trident,
        All,
        Any
    }

    public KillAura() {
        super(Categories.Combat, "kill-aura", "Attacks specified entities around you.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgTargeting = this.settings.createGroup("Targeting");
        this.sgTiming = this.settings.createGroup("Timing");
        this.weapon = this.sgGeneral.add(new EnumSetting.Builder().name("weapon").description("Only attacks an entity when a specified weapon is in your hand.").defaultValue(Weapon.All).build());
        this.rotation = this.sgGeneral.add(new EnumSetting.Builder().name("rotate").description("Determines when you should rotate towards the target.").defaultValue(RotationMode.Always).build());
        this.autoSwitch = this.sgGeneral.add(new BoolSetting.Builder().name("auto-switch").description("Switches to your selected weapon when attacking the target.").defaultValue(false).build());
        this.onlyOnClick = this.sgGeneral.add(new BoolSetting.Builder().name("only-on-click").description("Only attacks when holding left click.").defaultValue(false).build());
        this.onlyOnLook = this.sgGeneral.add(new BoolSetting.Builder().name("only-on-look").description("Only attacks when looking at an entity.").defaultValue(false).build());
        this.pauseOnCombat = this.sgGeneral.add(new BoolSetting.Builder().name("pause-baritone").description("Freezes Baritone temporarily until you are finished attacking the entity.").defaultValue(true).build());
        this.shieldMode = this.sgGeneral.add(new EnumSetting.Builder().name("shield-mode").description("Will try and use an axe to break target shields.").defaultValue(ShieldMode.Break).visible(() -> {
            return this.autoSwitch.get().booleanValue() && this.weapon.get() != Weapon.Axe;
        }).build());
        this.entities = this.sgTargeting.add(new EntityTypeListSetting.Builder().name("entities").description("Entities to attack.").onlyAttackable().defaultValue(class_1299.field_6097).build());
        this.priority = this.sgTargeting.add(new EnumSetting.Builder().name("priority").description("How to filter targets within range.").defaultValue(SortPriority.ClosestAngle).build());
        this.maxTargets = this.sgTargeting.add(new IntSetting.Builder().name("max-targets").description("How many entities to target at once.").defaultValue(1).min(1).sliderRange(1, 5).visible(() -> {
            return !this.onlyOnLook.get().booleanValue();
        }).build());
        this.range = this.sgTargeting.add(new DoubleSetting.Builder().name("range").description("The maximum range the entity can be to attack it.").defaultValue(4.5d).min(0.0d).sliderMax(6.0d).build());
        this.wallsRange = this.sgTargeting.add(new DoubleSetting.Builder().name("walls-range").description("The maximum range the entity can be attacked through walls.").defaultValue(3.5d).min(0.0d).sliderMax(6.0d).build());
        this.mobAgeFilter = this.sgTargeting.add(new EnumSetting.Builder().name("mob-age-filter").description("Determines the age of the mobs to target (baby, adult, or both).").defaultValue(EntityAge.Adult).build());
        this.ignoreNamed = this.sgTargeting.add(new BoolSetting.Builder().name("ignore-named").description("Whether or not to attack mobs with a name.").defaultValue(false).build());
        this.ignorePassive = this.sgTargeting.add(new BoolSetting.Builder().name("ignore-passive").description("Will only attack sometimes passive mobs if they are targeting you.").defaultValue(true).build());
        this.ignoreTamed = this.sgTargeting.add(new BoolSetting.Builder().name("ignore-tamed").description("Will avoid attacking mobs you tamed.").defaultValue(false).build());
        this.pauseOnLag = this.sgTiming.add(new BoolSetting.Builder().name("pause-on-lag").description("Pauses if the server is lagging.").defaultValue(true).build());
        this.pauseOnUse = this.sgTiming.add(new BoolSetting.Builder().name("pause-on-use").description("Does not attack while using an item.").defaultValue(false).build());
        this.tpsSync = this.sgTiming.add(new BoolSetting.Builder().name("TPS-sync").description("Tries to sync attack delay with the server's TPS.").defaultValue(true).build());
        this.customDelay = this.sgTiming.add(new BoolSetting.Builder().name("custom-delay").description("Use a custom delay instead of the vanilla cooldown.").defaultValue(false).build());
        SettingGroup settingGroup = this.sgTiming;
        IntSetting.Builder builderSliderMax = new IntSetting.Builder().name("hit-delay").description("How fast you hit the entity in ticks.").defaultValue(11).min(0).sliderMax(60);
        Setting<Boolean> setting = this.customDelay;
        Objects.requireNonNull(setting);
        this.hitDelay = settingGroup.add(builderSliderMax.visible(setting::get).build());
        this.switchDelay = this.sgTiming.add(new IntSetting.Builder().name("switch-delay").description("How many ticks to wait before hitting an entity after switching hotbar slots.").defaultValue(0).min(0).sliderMax(10).build());
        this.targets = new ArrayList();
        this.wasPathing = false;
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onDeactivate() {
        this.targets.clear();
        this.attacking = false;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        Predicate<class_1799> predicate;
        if (!this.mc.field_1724.method_5805() || PlayerUtils.getGameMode() == class_1934.field_9219) {
            return;
        }
        if (this.pauseOnUse.get().booleanValue() && (this.mc.field_1761.method_2923() || this.mc.field_1724.method_6115())) {
            return;
        }
        if (!this.onlyOnClick.get().booleanValue() || this.mc.field_1690.field_1886.method_1434()) {
            if (TickRate.INSTANCE.getTimeSinceLastTick() < 1.0f || !this.pauseOnLag.get().booleanValue()) {
                if (this.onlyOnLook.get().booleanValue()) {
                    class_1297 targeted = this.mc.field_1692;
                    if (targeted == null || !entityCheck(targeted)) {
                        return;
                    }
                    this.targets.clear();
                    this.targets.add(this.mc.field_1692);
                } else {
                    this.targets.clear();
                    TargetUtils.getList(this.targets, this::entityCheck, this.priority.get(), this.maxTargets.get().intValue());
                }
                if (this.targets.isEmpty()) {
                    this.attacking = false;
                    if (this.wasPathing) {
                        PathManagers.get().resume();
                        this.wasPathing = false;
                        return;
                    }
                    return;
                }
                class_1297 primary = (class_1297) this.targets.getFirst();
                if (this.autoSwitch.get().booleanValue()) {
                    switch (this.weapon.get()) {
                        case Sword:
                            predicate = stack -> {
                                return stack.method_31573(class_3489.field_42611);
                            };
                            break;
                        case Axe:
                            predicate = stack2 -> {
                                return stack2.method_31573(class_3489.field_42612);
                            };
                            break;
                        case Mace:
                            predicate = stack3 -> {
                                return stack3.method_7909() == class_1802.field_49814;
                            };
                            break;
                        case Trident:
                            predicate = stack4 -> {
                                return stack4.method_7909() == class_1802.field_8547;
                            };
                            break;
                        case All:
                            predicate = stack5 -> {
                                return stack5.method_31573(class_3489.field_42612) || stack5.method_31573(class_3489.field_42611) || stack5.method_7909() == class_1802.field_49814 || stack5.method_7909() == class_1802.field_8547;
                            };
                            break;
                        default:
                            predicate = o -> {
                                return true;
                            };
                            break;
                    }
                    Predicate<class_1799> predicate2 = predicate;
                    FindItemResult weaponResult = InvUtils.findInHotbar(predicate2);
                    if (shouldShieldBreak()) {
                        FindItemResult axeResult = InvUtils.findInHotbar((Predicate<class_1799>) itemStack -> {
                            return itemStack.method_31573(class_3489.field_42612);
                        });
                        if (axeResult.found()) {
                            weaponResult = axeResult;
                        }
                    }
                    InvUtils.swap(weaponResult.slot(), false);
                }
                if (itemInHand()) {
                    this.attacking = true;
                    if (this.rotation.get() == RotationMode.Always) {
                        Rotations.rotate(Rotations.getYaw(primary), Rotations.getPitch(primary, Target.Body));
                    }
                    if (this.pauseOnCombat.get().booleanValue() && PathManagers.get().isPathing() && !this.wasPathing) {
                        PathManagers.get().pause();
                        this.wasPathing = true;
                    }
                    if (delayCheck()) {
                        this.targets.forEach(this::attack);
                    }
                }
            }
        }
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (event.packet instanceof class_2868) {
            this.switchTimer = this.switchDelay.get().intValue();
        }
    }

    private boolean shouldShieldBreak() {
        Iterator<class_1297> it = this.targets.iterator();
        while (it.hasNext()) {
            if (((class_1297) it.next()) instanceof class_1657) {
            }
        }
        return false;
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: java.lang.MatchException */
    private boolean entityCheck(class_1297 entity) throws MatchException {
        if (entity.equals(this.mc.field_1724) || entity.equals(this.mc.field_1719)) {
            return false;
        }
        if (entity instanceof class_1309) {
            class_1309 livingEntity = (class_1309) entity;
            if (livingEntity.method_29504()) {
                return false;
            }
        }
        if (!entity.method_5805()) {
            return false;
        }
        class_238 hitbox = entity.method_5829();
        if (!PlayerUtils.isWithin(class_3532.method_15350(this.mc.field_1724.method_23317(), hitbox.field_1323, hitbox.field_1320), class_3532.method_15350(this.mc.field_1724.method_23318(), hitbox.field_1322, hitbox.field_1325), class_3532.method_15350(this.mc.field_1724.method_23321(), hitbox.field_1321, hitbox.field_1324), this.range.get().doubleValue()) || !this.entities.get().contains(entity.method_5864())) {
            return false;
        }
        if (this.ignoreNamed.get().booleanValue() && entity.method_16914()) {
            return false;
        }
        if (!PlayerUtils.canSeeEntity(entity) && !PlayerUtils.isWithin(entity, this.wallsRange.get().doubleValue())) {
            return false;
        }
        if (this.ignoreTamed.get().booleanValue() && (entity instanceof class_6025)) {
            class_6025 tameable = (class_6025) entity;
            if (tameable.method_35057() != null && tameable.method_35057().equals(this.mc.field_1724.method_5667())) {
                return false;
            }
        }
        if (this.ignorePassive.get().booleanValue()) {
            if (entity instanceof class_1560) {
                class_1560 enderman = (class_1560) entity;
                if (!enderman.method_7028()) {
                    return false;
                }
            }
            if (entity instanceof class_1590) {
                class_1590 piglin = (class_1590) entity;
                if (!piglin.method_6510()) {
                    return false;
                }
            }
            if (entity instanceof class_1493) {
                class_1493 wolf = (class_1493) entity;
                if (!wolf.method_6510()) {
                    return false;
                }
            }
        }
        if (entity instanceof class_1657) {
            class_1657 player = (class_1657) entity;
            if (player.method_68878() || !Friends.get().shouldAttack(player)) {
                return false;
            }
        }
        if (entity instanceof class_1429) {
            class_1429 animal = (class_1429) entity;
            switch (this.mobAgeFilter.get()) {
                case Baby:
                    return animal.method_6109();
                case Adult:
                    return !animal.method_6109();
                case Both:
                    return true;
                default:
                    throw new MatchException((String) null, (Throwable) null);
            }
        }
        return true;
    }

    private boolean delayCheck() {
        if (this.switchTimer > 0) {
            this.switchTimer--;
            return false;
        }
        float delay = this.customDelay.get().booleanValue() ? this.hitDelay.get().intValue() : 0.5f;
        if (this.tpsSync.get().booleanValue()) {
            delay /= TickRate.INSTANCE.getTickRate() / 20.0f;
        }
        if (!this.customDelay.get().booleanValue()) {
            return this.mc.field_1724.method_7261(delay) >= 1.0f;
        }
        if (this.hitTimer < delay) {
            this.hitTimer++;
            return false;
        }
        return true;
    }

    private void attack(class_1297 target) {
        if (this.rotation.get() == RotationMode.OnHit) {
            Rotations.rotate(Rotations.getYaw(target), Rotations.getPitch(target, Target.Body));
        }
        this.mc.field_1761.method_2918(this.mc.field_1724, target);
        this.mc.field_1724.method_6104(class_1268.field_5808);
        this.hitTimer = 0;
    }

    private boolean itemInHand() {
        if (shouldShieldBreak()) {
            return this.mc.field_1724.method_6047().method_7909().toString().contains("axe");
        }
        switch (this.weapon.get()) {
            case Sword:
                return this.mc.field_1724.method_6047().method_31573(class_3489.field_42611);
            case Axe:
                return this.mc.field_1724.method_6047().method_31573(class_3489.field_42612);
            case Mace:
                return this.mc.field_1724.method_6047().method_7909() == class_1802.field_49814;
            case Trident:
                return this.mc.field_1724.method_6047().method_7909() == class_1802.field_8547;
            case All:
                return this.mc.field_1724.method_6047().method_31573(class_3489.field_42612) || this.mc.field_1724.method_6047().method_31573(class_3489.field_42611) || this.mc.field_1724.method_6047().method_7909() == class_1802.field_49814 || this.mc.field_1724.method_6047().method_7909() == class_1802.field_8547;
            default:
                return true;
        }
    }

    public class_1297 getTarget() {
        if (this.targets.isEmpty()) {
            return null;
        }
        return (class_1297) this.targets.getFirst();
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public String getInfoString() {
        if (this.targets.isEmpty()) {
            return null;
        }
        return EntityUtils.getName(getTarget());
    }
}
