package meteordevelopment.meteorclient.systems.modules.render;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.meteor.MouseScrollEvent;
import meteordevelopment.meteorclient.events.render.GetFovEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_3532;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/Zoom.class */
public class Zoom extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Double> zoom;
    private final Setting<Double> scrollSensitivity;
    private final Setting<Boolean> smooth;
    private final Setting<Boolean> cinematic;
    private final Setting<Boolean> renderHands;
    private boolean enabled;
    private boolean preCinematic;
    private double preMouseSensitivity;
    private double value;
    private double lastFov;
    private double time;

    public Zoom() {
        super(Categories.Render, "zoom", "Zooms your view.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.zoom = this.sgGeneral.add(new DoubleSetting.Builder().name("zoom").description("How much to zoom.").defaultValue(6.0d).min(1.0d).build());
        this.scrollSensitivity = this.sgGeneral.add(new DoubleSetting.Builder().name("scroll-sensitivity").description("Allows you to change zoom value using scroll wheel. 0 to disable.").defaultValue(1.0d).min(0.0d).build());
        this.smooth = this.sgGeneral.add(new BoolSetting.Builder().name("smooth").description("Smooth transition.").defaultValue(true).build());
        this.cinematic = this.sgGeneral.add(new BoolSetting.Builder().name("cinematic").description("Enables cinematic camera.").defaultValue(false).build());
        this.renderHands = this.sgGeneral.add(new BoolSetting.Builder().name("show-hands").description("Whether or not to render your hands.").defaultValue(false).build());
        this.autoSubscribe = false;
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        if (!this.enabled) {
            this.preCinematic = this.mc.field_1690.field_1914;
            this.preMouseSensitivity = ((Double) this.mc.field_1690.method_42495().method_41753()).doubleValue();
            this.value = this.zoom.get().doubleValue();
            this.lastFov = ((Integer) this.mc.field_1690.method_41808().method_41753()).intValue();
            this.time = 0.001d;
            MeteorClient.EVENT_BUS.subscribe(this);
            this.enabled = true;
        }
    }

    public void onStop() {
        this.mc.field_1690.field_1914 = this.preCinematic;
        this.mc.field_1690.method_42495().method_41748(Double.valueOf(this.preMouseSensitivity));
        this.mc.field_1769.method_3292();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        this.mc.field_1690.field_1914 = this.cinematic.get().booleanValue();
        if (!this.cinematic.get().booleanValue()) {
            this.mc.field_1690.method_42495().method_41748(Double.valueOf(this.preMouseSensitivity / Math.max(getScaling() * 0.5d, 1.0d)));
        }
        if (this.time == 0.0d) {
            MeteorClient.EVENT_BUS.unsubscribe(this);
            this.enabled = false;
            onStop();
        }
    }

    @EventHandler
    private void onMouseScroll(MouseScrollEvent event) {
        if (this.scrollSensitivity.get().doubleValue() > 0.0d && isActive()) {
            this.value += event.value * 0.25d * this.scrollSensitivity.get().doubleValue() * this.value;
            if (this.value < 1.0d) {
                this.value = 1.0d;
            }
            event.cancel();
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (!this.smooth.get().booleanValue()) {
            this.time = isActive() ? 1.0d : 0.0d;
            return;
        }
        if (isActive()) {
            this.time += event.frameTime * 5.0d;
        } else {
            this.time -= event.frameTime * 5.0d;
        }
        this.time = class_3532.method_15350(this.time, 0.0d, 1.0d);
    }

    @EventHandler
    private void onGetFov(GetFovEvent event) {
        event.fov = (float) (((double) event.fov) / getScaling());
        if (this.lastFov != event.fov) {
            this.mc.field_1769.method_3292();
        }
        this.lastFov = event.fov;
    }

    public double getScaling() {
        double delta = this.time < 0.5d ? 4.0d * this.time * this.time * this.time : 1.0d - (Math.pow(((-2.0d) * this.time) + 2.0d, 3.0d) / 2.0d);
        return class_3532.method_16436(delta, 1.0d, this.value);
    }

    public boolean renderHands() {
        return !isActive() || this.renderHands.get().booleanValue();
    }
}
