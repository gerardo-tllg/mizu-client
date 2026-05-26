package meteordevelopment.meteorclient.systems.modules.movement;

import java.util.Objects;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.EntityVelocityUpdateS2CPacketAccessor;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.managers.RotationManager;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_2743;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/movement/Velocity.class */
public class Velocity extends Module {
    private final SettingGroup sgGeneral;
    public final Setting<Boolean> knockback;
    public final Setting<Boolean> knockbackPhaseOnly;
    public final Setting<Boolean> knockbackPhaseInAir;
    public final Setting<Double> knockbackHorizontal;
    public final Setting<Double> knockbackVertical;
    public final Setting<Boolean> explosions;
    public final Setting<Double> explosionsHorizontal;
    public final Setting<Double> explosionsVertical;
    public final Setting<Boolean> liquids;
    public final Setting<Double> liquidsHorizontal;
    public final Setting<Double> liquidsVertical;
    public final Setting<Boolean> entityPush;
    public final Setting<Double> entityPushAmount;
    public final Setting<Boolean> blocks;
    public final Setting<Boolean> sinking;
    public final Setting<Boolean> fishing;
    public final Setting<Boolean> livingEntityKnockback;

    public Velocity() {
        super(Categories.Movement, "velocity", "Prevents you from being moved by external forces.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.knockback = this.sgGeneral.add(new BoolSetting.Builder().name("knockback").description("Modifies the amount of knockback you take from attacks.").defaultValue(true).build());
        SettingGroup settingGroup = this.sgGeneral;
        BoolSetting.Builder builderDefaultValue = new BoolSetting.Builder().name("knockback-phase-only").description("Only modifies knockback when phased into a wall.").defaultValue(true);
        Setting<Boolean> setting = this.knockback;
        Objects.requireNonNull(setting);
        this.knockbackPhaseOnly = settingGroup.add(builderDefaultValue.visible(setting::get).build());
        this.knockbackPhaseInAir = this.sgGeneral.add(new BoolSetting.Builder().name("knockback-phase-disable-in-air").description("Doesn't modify knockback in a phase when you're in the air (like jumping).").defaultValue(true).visible(() -> {
            return this.knockback.get().booleanValue() && this.knockbackPhaseOnly.get().booleanValue();
        }).build());
        SettingGroup settingGroup2 = this.sgGeneral;
        DoubleSetting.Builder builderSliderMax = new DoubleSetting.Builder().name("knockback-horizontal").description("How much horizontal knockback you will take.").defaultValue(0.0d).sliderMax(1.0d);
        Setting<Boolean> setting2 = this.knockback;
        Objects.requireNonNull(setting2);
        this.knockbackHorizontal = settingGroup2.add(builderSliderMax.visible(setting2::get).build());
        SettingGroup settingGroup3 = this.sgGeneral;
        DoubleSetting.Builder builderSliderMax2 = new DoubleSetting.Builder().name("knockback-vertical").description("How much vertical knockback you will take.").defaultValue(0.0d).sliderMax(1.0d);
        Setting<Boolean> setting3 = this.knockback;
        Objects.requireNonNull(setting3);
        this.knockbackVertical = settingGroup3.add(builderSliderMax2.visible(setting3::get).build());
        this.explosions = this.sgGeneral.add(new BoolSetting.Builder().name("explosions").description("Modifies your knockback from explosions.").defaultValue(true).build());
        SettingGroup settingGroup4 = this.sgGeneral;
        DoubleSetting.Builder builderSliderMax3 = new DoubleSetting.Builder().name("explosions-horizontal").description("How much velocity you will take from explosions horizontally.").defaultValue(0.0d).sliderMax(1.0d);
        Setting<Boolean> setting4 = this.explosions;
        Objects.requireNonNull(setting4);
        this.explosionsHorizontal = settingGroup4.add(builderSliderMax3.visible(setting4::get).build());
        SettingGroup settingGroup5 = this.sgGeneral;
        DoubleSetting.Builder builderSliderMax4 = new DoubleSetting.Builder().name("explosions-vertical").description("How much velocity you will take from explosions vertically.").defaultValue(0.0d).sliderMax(1.0d);
        Setting<Boolean> setting5 = this.explosions;
        Objects.requireNonNull(setting5);
        this.explosionsVertical = settingGroup5.add(builderSliderMax4.visible(setting5::get).build());
        this.liquids = this.sgGeneral.add(new BoolSetting.Builder().name("liquids").description("Modifies the amount you are pushed by flowing liquids.").defaultValue(true).build());
        SettingGroup settingGroup6 = this.sgGeneral;
        DoubleSetting.Builder builderSliderMax5 = new DoubleSetting.Builder().name("liquids-horizontal").description("How much velocity you will take from liquids horizontally.").defaultValue(0.0d).sliderMax(1.0d);
        Setting<Boolean> setting6 = this.liquids;
        Objects.requireNonNull(setting6);
        this.liquidsHorizontal = settingGroup6.add(builderSliderMax5.visible(setting6::get).build());
        SettingGroup settingGroup7 = this.sgGeneral;
        DoubleSetting.Builder builderSliderMax6 = new DoubleSetting.Builder().name("liquids-vertical").description("How much velocity you will take from liquids vertically.").defaultValue(0.0d).sliderMax(1.0d);
        Setting<Boolean> setting7 = this.liquids;
        Objects.requireNonNull(setting7);
        this.liquidsVertical = settingGroup7.add(builderSliderMax6.visible(setting7::get).build());
        this.entityPush = this.sgGeneral.add(new BoolSetting.Builder().name("entity-push").description("Modifies the amount you are pushed by entities.").defaultValue(true).build());
        SettingGroup settingGroup8 = this.sgGeneral;
        DoubleSetting.Builder builderSliderMax7 = new DoubleSetting.Builder().name("entity-push-amount").description("How much you will be pushed.").defaultValue(0.0d).sliderMax(1.0d);
        Setting<Boolean> setting8 = this.entityPush;
        Objects.requireNonNull(setting8);
        this.entityPushAmount = settingGroup8.add(builderSliderMax7.visible(setting8::get).build());
        this.blocks = this.sgGeneral.add(new BoolSetting.Builder().name("blocks").description("Prevents you from being pushed out of blocks.").defaultValue(true).build());
        this.sinking = this.sgGeneral.add(new BoolSetting.Builder().name("sinking").description("Prevents you from sinking in liquids.").defaultValue(false).build());
        this.fishing = this.sgGeneral.add(new BoolSetting.Builder().name("fishing").description("Prevents you from being pulled by fishing rods.").defaultValue(false).build());
        this.livingEntityKnockback = this.sgGeneral.add(new BoolSetting.Builder().name("living-entity-knockback").description("Prevents you from being moved by knockback.").defaultValue(true).build());
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (this.sinking.get().booleanValue() && !this.mc.field_1690.field_1903.method_1434() && !this.mc.field_1690.field_1832.method_1434()) {
            if ((this.mc.field_1724.method_5799() || this.mc.field_1724.method_5771()) && this.mc.field_1724.method_18798().field_1351 < 0.0d) {
                this.mc.field_1724.method_18798().meteor$setY(0.0d);
            }
        }
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (this.knockback.get().booleanValue()) {
            EntityVelocityUpdateS2CPacketAccessor entityVelocityUpdateS2CPacketAccessor = event.packet;
            if (entityVelocityUpdateS2CPacketAccessor instanceof class_2743) {
                EntityVelocityUpdateS2CPacketAccessor entityVelocityUpdateS2CPacketAccessor2 = (class_2743) entityVelocityUpdateS2CPacketAccessor;
                if (entityVelocityUpdateS2CPacketAccessor2.method_11818() == this.mc.field_1724.method_5628()) {
                    if (!this.knockbackPhaseOnly.get().booleanValue() || ((!this.knockbackPhaseInAir.get().booleanValue() || RotationManager.lastGround) && PlayerUtils.isPlayerPhased())) {
                        double velX = ((entityVelocityUpdateS2CPacketAccessor2.method_11815() / 8000.0d) - this.mc.field_1724.method_18798().field_1352) * this.knockbackHorizontal.get().doubleValue();
                        double velY = ((entityVelocityUpdateS2CPacketAccessor2.method_11816() / 8000.0d) - this.mc.field_1724.method_18798().field_1351) * this.knockbackVertical.get().doubleValue();
                        double velZ = ((entityVelocityUpdateS2CPacketAccessor2.method_11819() / 8000.0d) - this.mc.field_1724.method_18798().field_1350) * this.knockbackHorizontal.get().doubleValue();
                        entityVelocityUpdateS2CPacketAccessor2.setX((int) ((velX * 8000.0d) + (this.mc.field_1724.method_18798().field_1352 * 8000.0d)));
                        entityVelocityUpdateS2CPacketAccessor2.setY((int) ((velY * 8000.0d) + (this.mc.field_1724.method_18798().field_1351 * 8000.0d)));
                        entityVelocityUpdateS2CPacketAccessor2.setZ((int) ((velZ * 8000.0d) + (this.mc.field_1724.method_18798().field_1350 * 8000.0d)));
                    }
                }
            }
        }
    }

    public double getHorizontal(Setting<Double> setting) {
        if (isActive()) {
            return setting.get().doubleValue();
        }
        return 1.0d;
    }

    public double getVertical(Setting<Double> setting) {
        if (isActive()) {
            return setting.get().doubleValue();
        }
        return 1.0d;
    }
}
