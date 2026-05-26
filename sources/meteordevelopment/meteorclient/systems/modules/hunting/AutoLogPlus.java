package meteordevelopment.meteorclient.systems.modules.hunting;

import java.util.Objects;
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
import meteordevelopment.meteorclient.systems.modules.misc.AutoReconnect;
import meteordevelopment.meteorclient.utils.world.TickRate;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2338;
import net.minecraft.class_2561;
import net.minecraft.class_2661;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/hunting/AutoLogPlus.class */
public class AutoLogPlus extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Boolean> logOnY;
    private final Setting<Double> yLevel;
    private final Setting<Boolean> logArmor;
    private final Setting<Boolean> ignoreElytra;
    private final Setting<Double> armorPercent;
    private final Setting<Boolean> logPortal;
    private final Setting<Integer> portalTicks;
    private final Setting<Boolean> logPosition;
    private final Setting<class_2338> position;
    private final Setting<Double> distance;
    private final Setting<Boolean> serverNotResponding;
    private final Setting<Double> serverNotRespondingSecs;
    private final Setting<Boolean> reconnectAfterNotResponding;
    private final Setting<Double> secondsToReconnect;
    private final Setting<Boolean> illegalDisconnect;
    private int currPortalTicks;
    private double oldDelay;
    private boolean autoReconnectEnabled;
    private boolean waitingForReconnection;

    public AutoLogPlus() {
        super(Categories.Hunting, "auto-log-plus", "Provides some additional triggers to log out.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.logOnY = this.sgGeneral.add(new BoolSetting.Builder().name("log-on-y").description("Logs out if you are below a certain Y level.").defaultValue(false).build());
        SettingGroup settingGroup = this.sgGeneral;
        DoubleSetting.Builder builderSliderRange = new DoubleSetting.Builder().name("y-level").defaultValue(256.0d).min(-128.0d).sliderRange(-128.0d, 320.0d);
        Setting<Boolean> setting = this.logOnY;
        Objects.requireNonNull(setting);
        this.yLevel = settingGroup.add(builderSliderRange.visible(setting::get).build());
        this.logArmor = this.sgGeneral.add(new BoolSetting.Builder().name("log-armor").description("Logs out if your armor goes below a certain durability amount.").defaultValue(false).build());
        SettingGroup settingGroup2 = this.sgGeneral;
        BoolSetting.Builder builderDefaultValue = new BoolSetting.Builder().name("ignore-elytra").description("Ignores the elytra when checking armor durability.").defaultValue(false);
        Setting<Boolean> setting2 = this.logArmor;
        Objects.requireNonNull(setting2);
        this.ignoreElytra = settingGroup2.add(builderDefaultValue.visible(setting2::get).build());
        SettingGroup settingGroup3 = this.sgGeneral;
        DoubleSetting.Builder builderSliderRange2 = new DoubleSetting.Builder().name("armor-percent").defaultValue(5.0d).min(0.0d).sliderRange(0.0d, 100.0d);
        Setting<Boolean> setting3 = this.logArmor;
        Objects.requireNonNull(setting3);
        this.armorPercent = settingGroup3.add(builderSliderRange2.visible(setting3::get).build());
        this.logPortal = this.sgGeneral.add(new BoolSetting.Builder().name("log-on-portal").description("Logs out if you are in a portal for too long.").defaultValue(false).build());
        SettingGroup settingGroup4 = this.sgGeneral;
        IntSetting.Builder builderSliderMax = new IntSetting.Builder().name("portal-ticks").description("The amount of ticks in a portal before you get kicked (It takes 80 ticks to go through a portal).").defaultValue(30).min(1).sliderMax(70);
        Setting<Boolean> setting4 = this.logPortal;
        Objects.requireNonNull(setting4);
        this.portalTicks = settingGroup4.add(builderSliderMax.visible(setting4::get).build());
        this.logPosition = this.sgGeneral.add(new BoolSetting.Builder().name("log-position").description("Logs out if you are within x blocks of this position. Y Position is not included").defaultValue(false).build());
        SettingGroup settingGroup5 = this.sgGeneral;
        BlockPosSetting.Builder builderDefaultValue2 = new BlockPosSetting.Builder().name("position").description("The position to log out at. Y position is ignored.").defaultValue(new class_2338(0, 0, 0));
        Setting<Boolean> setting5 = this.logPosition;
        Objects.requireNonNull(setting5);
        this.position = settingGroup5.add(builderDefaultValue2.visible(setting5::get).build());
        SettingGroup settingGroup6 = this.sgGeneral;
        DoubleSetting.Builder builderSliderRange3 = new DoubleSetting.Builder().name("distance").description("The distance from the position to log out at.").defaultValue(100.0d).sliderRange(0.0d, 1000.0d);
        Setting<Boolean> setting6 = this.logPosition;
        Objects.requireNonNull(setting6);
        this.distance = settingGroup6.add(builderSliderRange3.visible(setting6::get).build());
        this.serverNotResponding = this.sgGeneral.add(new BoolSetting.Builder().name("server-not-responding").description("Logs out if the server is not responding.").defaultValue(false).build());
        SettingGroup settingGroup7 = this.sgGeneral;
        DoubleSetting.Builder builderSliderMax2 = new DoubleSetting.Builder().name("seconds-not-responding").description("The amount of seconds the server is not responding before you log out.").defaultValue(10.0d).min(1.0d).sliderMax(60.0d);
        Setting<Boolean> setting7 = this.serverNotResponding;
        Objects.requireNonNull(setting7);
        this.serverNotRespondingSecs = settingGroup7.add(builderSliderMax2.visible(setting7::get).build());
        SettingGroup settingGroup8 = this.sgGeneral;
        BoolSetting.Builder builderDefaultValue3 = new BoolSetting.Builder().name("reconnect-after-not-responding").description("Reconnects after the server is not responding.").defaultValue(false);
        Setting<Boolean> setting8 = this.serverNotResponding;
        Objects.requireNonNull(setting8);
        this.reconnectAfterNotResponding = settingGroup8.add(builderDefaultValue3.visible(setting8::get).build());
        this.secondsToReconnect = this.sgGeneral.add(new DoubleSetting.Builder().name("reconnect-seconds").description("The amount of seconds to wait before reconnecting (Will temporarily overwrite Meteor's AutoReconnect.").defaultValue(60.0d).min(10.0d).sliderMax(300.0d).visible(() -> {
            return this.reconnectAfterNotResponding.get().booleanValue() && this.serverNotResponding.get().booleanValue();
        }).build());
        this.illegalDisconnect = this.sgGeneral.add(new BoolSetting.Builder().name("illegal-disconnect").description("Disconnects from the server using the slot method.").defaultValue(false).build());
        this.currPortalTicks = 0;
        this.waitingForReconnection = false;
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        this.currPortalTicks = 0;
        if (this.waitingForReconnection) {
            this.waitingForReconnection = false;
            AutoReconnect autoReconnect = (AutoReconnect) Modules.get().get(AutoReconnect.class);
            autoReconnect.settings.get("delay").set(Double.valueOf(this.oldDelay));
            if (!this.autoReconnectEnabled && autoReconnect.isActive()) {
                autoReconnect.toggle();
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (this.mc.field_1724 == null || this.mc.field_1724.method_31549().field_7478) {
            return;
        }
        if (this.serverNotResponding.get().booleanValue() && !this.waitingForReconnection && TickRate.INSTANCE.getTimeSinceLastTick() > this.serverNotRespondingSecs.get().doubleValue()) {
            if (this.reconnectAfterNotResponding.get().booleanValue()) {
                AutoReconnect autoReconnect = (AutoReconnect) Modules.get().get(AutoReconnect.class);
                this.autoReconnectEnabled = autoReconnect.isActive();
                Setting<?> setting = autoReconnect.settings.get("delay");
                this.oldDelay = ((Double) setting.get()).doubleValue();
                setting.set(this.secondsToReconnect.get());
                if (!this.autoReconnectEnabled) {
                    autoReconnect.toggle();
                }
                this.waitingForReconnection = true;
            }
            logOut("Server was not responding for " + String.valueOf(this.serverNotRespondingSecs.get()) + " seconds.", !this.reconnectAfterNotResponding.get().booleanValue());
            return;
        }
        if (this.logPortal.get().booleanValue() && this.mc.field_1724.field_51994 != null) {
            if (this.mc.field_1724.field_51994.method_60709()) {
                this.currPortalTicks++;
                if (this.currPortalTicks > this.portalTicks.get().intValue()) {
                    logOut("Player was in a portal for " + this.currPortalTicks + " ticks.", true);
                    return;
                }
            } else {
                this.currPortalTicks = 0;
            }
        }
        if (this.logOnY.get().booleanValue() && this.mc.field_1724.method_23318() < this.yLevel.get().doubleValue()) {
            double dMethod_23318 = this.mc.field_1724.method_23318();
            String.valueOf(this.yLevel.get());
            logOut("Player was at Y=" + dMethod_23318 + " which is below your limit of Y=" + this, true);
            return;
        }
        if (this.logArmor.get().booleanValue()) {
            for (int i = 0; i < 4; i++) {
                class_1799 armorPiece = this.mc.field_1724.method_31548().method_5438(36 + i);
                if ((!this.ignoreElytra.get().booleanValue() || armorPiece.method_7909() != class_1802.field_8833) && armorPiece.method_7963()) {
                    int max = armorPiece.method_7936();
                    int current = armorPiece.method_7919();
                    double percentUndamaged = 100.0d - ((((double) current) / ((double) max)) * 100.0d);
                    if (percentUndamaged < this.armorPercent.get().doubleValue()) {
                        logOut("You had low armor", true);
                        return;
                    }
                }
            }
        }
        if (this.logPosition.get().booleanValue()) {
            double distanceToTarget = this.mc.field_1724.method_19538().method_18805(1.0d, 0.0d, 1.0d).method_1022(this.position.get().method_46558().method_18805(1.0d, 0.0d, 1.0d));
            if (distanceToTarget < this.distance.get().doubleValue()) {
                logOut("Player was within " + distanceToTarget + " blocks of the target position.", true);
            }
        }
    }

    private void logOut(String reason, boolean turnOffReconnect) {
        if (this.mc.field_1724 == null) {
            return;
        }
        if (turnOffReconnect && ((AutoReconnect) Modules.get().get(AutoReconnect.class)).isActive()) {
            ((AutoReconnect) Modules.get().get(AutoReconnect.class)).toggle();
        }
        if (this.illegalDisconnect.get().booleanValue()) {
            this.mc.field_1724.field_3944.method_45729(String.valueOf((char) 0));
        } else {
            this.mc.field_1724.field_3944.method_52781(new class_2661(class_2561.method_43470("[AutoLogPlus] " + reason)));
        }
    }
}
