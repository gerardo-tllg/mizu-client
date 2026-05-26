package meteordevelopment.meteorclient.systems.modules.render;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.input.Input;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_3532;
import net.minecraft.class_5498;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/FreeLook.class */
public class FreeLook extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgArrows;
    public final Setting<Mode> mode;
    public final Setting<Boolean> togglePerspective;
    public final Setting<Double> sensitivity;
    public final Setting<Boolean> arrows;
    private final Setting<Double> arrowSpeed;
    public float cameraYaw;
    public float cameraPitch;
    private class_5498 prePers;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/FreeLook$Mode.class */
    public enum Mode {
        Player,
        Camera
    }

    public FreeLook() {
        super(Categories.Render, "free-look", "Allows more rotation options in third person.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgArrows = this.settings.createGroup("Arrows");
        this.mode = this.sgGeneral.add(new EnumSetting.Builder().name("mode").description("Which entity to rotate.").defaultValue(Mode.Player).build());
        this.togglePerspective = this.sgGeneral.add(new BoolSetting.Builder().name("toggle-perspective").description("Changes your perspective on toggle.").defaultValue(true).build());
        this.sensitivity = this.sgGeneral.add(new DoubleSetting.Builder().name("camera-sensitivity").description("How fast the camera moves in camera mode.").defaultValue(8.0d).min(0.0d).sliderMax(10.0d).build());
        this.arrows = this.sgArrows.add(new BoolSetting.Builder().name("arrows-control-opposite").description("Allows you to control the other entities rotation with the arrow keys.").defaultValue(true).build());
        this.arrowSpeed = this.sgArrows.add(new DoubleSetting.Builder().name("arrow-speed").description("Rotation speed with arrow keys.").defaultValue(4.0d).min(0.0d).build());
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        this.cameraYaw = this.mc.field_1724.method_36454();
        this.cameraPitch = this.mc.field_1724.method_36455();
        this.prePers = this.mc.field_1690.method_31044();
        if (this.prePers == class_5498.field_26665 || !this.togglePerspective.get().booleanValue()) {
            return;
        }
        this.mc.field_1690.method_31043(class_5498.field_26665);
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onDeactivate() {
        if (this.mc.field_1690.method_31044() == this.prePers || !this.togglePerspective.get().booleanValue()) {
            return;
        }
        this.mc.field_1690.method_31043(this.prePers);
    }

    public boolean playerMode() {
        return isActive() && this.mc.field_1690.method_31044() == class_5498.field_26665 && this.mode.get() == Mode.Player;
    }

    public boolean cameraMode() {
        return isActive() && this.mode.get() == Mode.Camera;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (this.arrows.get().booleanValue()) {
            for (int i = 0; i < this.arrowSpeed.get().doubleValue() * 2.0d; i++) {
                switch (this.mode.get()) {
                    case Player:
                        if (Input.isKeyPressed(263)) {
                            this.cameraYaw = (float) (((double) this.cameraYaw) - 0.5d);
                        }
                        if (Input.isKeyPressed(262)) {
                            this.cameraYaw = (float) (((double) this.cameraYaw) + 0.5d);
                        }
                        if (Input.isKeyPressed(265)) {
                            this.cameraPitch = (float) (((double) this.cameraPitch) - 0.5d);
                        }
                        if (Input.isKeyPressed(264)) {
                            this.cameraPitch = (float) (((double) this.cameraPitch) + 0.5d);
                        }
                        break;
                    case Camera:
                        float yaw = this.mc.field_1724.method_36454();
                        float pitch = this.mc.field_1724.method_36455();
                        if (Input.isKeyPressed(263)) {
                            yaw = (float) (((double) yaw) - 0.5d);
                        }
                        if (Input.isKeyPressed(262)) {
                            yaw = (float) (((double) yaw) + 0.5d);
                        }
                        if (Input.isKeyPressed(265)) {
                            pitch = (float) (((double) pitch) - 0.5d);
                        }
                        if (Input.isKeyPressed(264)) {
                            pitch = (float) (((double) pitch) + 0.5d);
                        }
                        this.mc.field_1724.method_36456(yaw);
                        this.mc.field_1724.method_36457(pitch);
                        break;
                }
            }
        }
        this.mc.field_1724.method_36457(class_3532.method_15363(this.mc.field_1724.method_36455(), -90.0f, 90.0f));
        this.cameraPitch = class_3532.method_15363(this.cameraPitch, -90.0f, 90.0f);
    }
}
