package meteordevelopment.meteorclient.systems.modules.player;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/player/Rotation.class */
public class Rotation extends Module {
    private final SettingGroup sgYaw;
    private final SettingGroup sgPitch;
    private final Setting<LockMode> yawLockMode;
    private final Setting<Double> yawAngle;
    private final Setting<LockMode> pitchLockMode;
    private final Setting<Double> pitchAngle;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/player/Rotation$LockMode.class */
    public enum LockMode {
        Smart,
        Simple,
        None
    }

    public Rotation() {
        super(Categories.Player, "rotation", "Changes/locks your yaw and pitch.");
        this.sgYaw = this.settings.createGroup("Yaw");
        this.sgPitch = this.settings.createGroup("Pitch");
        this.yawLockMode = this.sgYaw.add(new EnumSetting.Builder().name("yaw-lock-mode").description("The way in which your yaw is locked.").defaultValue(LockMode.Simple).build());
        this.yawAngle = this.sgYaw.add(new DoubleSetting.Builder().name("yaw-angle").description("Yaw angle in degrees.").defaultValue(0.0d).sliderMax(360.0d).max(360.0d).visible(() -> {
            return this.yawLockMode.get() == LockMode.Simple;
        }).build());
        this.pitchLockMode = this.sgPitch.add(new EnumSetting.Builder().name("pitch-lock-mode").description("The way in which your pitch is locked.").defaultValue(LockMode.Simple).build());
        this.pitchAngle = this.sgPitch.add(new DoubleSetting.Builder().name("pitch-angle").description("Pitch angle in degrees.").defaultValue(0.0d).range(-90.0d, 90.0d).sliderRange(-90.0d, 90.0d).visible(() -> {
            return this.pitchLockMode.get() == LockMode.Simple;
        }).build());
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        onTick(null);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        switch (this.yawLockMode.get()) {
            case Smart:
                setYawAngle(getSmartYawDirection());
                break;
            case Simple:
                setYawAngle(this.yawAngle.get().floatValue());
                break;
        }
        switch (this.pitchLockMode.get()) {
            case Smart:
                this.mc.field_1724.method_36457(getSmartPitchDirection());
                break;
            case Simple:
                this.mc.field_1724.method_36457(this.pitchAngle.get().floatValue());
                break;
        }
    }

    private float getSmartYawDirection() {
        return Math.round((this.mc.field_1724.method_36454() + 1.0f) / 45.0f) * 45.0f;
    }

    private float getSmartPitchDirection() {
        return Math.round((this.mc.field_1724.method_36455() + 1.0f) / 30.0f) * 30.0f;
    }

    private void setYawAngle(float yawAngle) {
        this.mc.field_1724.method_36456(yawAngle);
        this.mc.field_1724.field_6241 = yawAngle;
        this.mc.field_1724.field_6283 = yawAngle;
    }
}
