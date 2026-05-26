package meteordevelopment.meteorclient.systems.hud.elements;

import java.util.Objects;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.class_1309;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_490;
import net.minecraft.class_746;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/hud/elements/PlayerModelHud.class */
public class PlayerModelHud extends HudElement {
    public static final HudElementInfo<PlayerModelHud> INFO = new HudElementInfo<>(Hud.GROUP, "player-model", "Displays a model of your player.", PlayerModelHud::new);
    private final SettingGroup sgGeneral;
    private final SettingGroup sgBackground;
    private final Setting<Double> scale;
    private final Setting<Boolean> copyYaw;
    private final Setting<Integer> customYaw;
    private final Setting<Boolean> copyPitch;
    private final Setting<Integer> customPitch;
    private final Setting<CenterOrientation> centerOrientation;
    private final Setting<Boolean> background;
    private final Setting<SettingColor> backgroundColor;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/hud/elements/PlayerModelHud$CenterOrientation.class */
    private enum CenterOrientation {
        North,
        South
    }

    public PlayerModelHud() {
        super(INFO);
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgBackground = this.settings.createGroup("Background");
        this.scale = this.sgGeneral.add(new DoubleSetting.Builder().name("scale").description("The scale.").defaultValue(2.0d).min(1.0d).sliderRange(1.0d, 5.0d).onChanged(aDouble -> {
            calculateSize();
        }).build());
        this.copyYaw = this.sgGeneral.add(new BoolSetting.Builder().name("copy-yaw").description("Makes the player model's yaw equal to yours.").defaultValue(true).build());
        this.customYaw = this.sgGeneral.add(new IntSetting.Builder().name("custom-yaw").description("Custom yaw for when copy yaw is off.").defaultValue(0).range(-180, Opcode.GETFIELD).sliderRange(-180, Opcode.GETFIELD).visible(() -> {
            return !this.copyYaw.get().booleanValue();
        }).build());
        this.copyPitch = this.sgGeneral.add(new BoolSetting.Builder().name("copy-pitch").description("Makes the player model's pitch equal to yours.").defaultValue(true).build());
        this.customPitch = this.sgGeneral.add(new IntSetting.Builder().name("custom-pitch").description("Custom pitch for when copy pitch is off.").defaultValue(0).range(-90, 90).sliderRange(-90, 90).visible(() -> {
            return !this.copyPitch.get().booleanValue();
        }).build());
        this.centerOrientation = this.sgGeneral.add(new EnumSetting.Builder().name("center-orientation").description("Which direction the player faces when the HUD model faces directly forward.").defaultValue(CenterOrientation.South).build());
        this.background = this.sgBackground.add(new BoolSetting.Builder().name("background").description("Displays background.").defaultValue(false).build());
        SettingGroup settingGroup = this.sgBackground;
        ColorSetting.Builder builderDescription = new ColorSetting.Builder().name("background-color").description("Color used for the background.");
        Setting<Boolean> setting = this.background;
        Objects.requireNonNull(setting);
        this.backgroundColor = settingGroup.add(builderDescription.visible(setting::get).defaultValue(new SettingColor(25, 25, 25, 50)).build());
        calculateSize();
    }

    @Override // meteordevelopment.meteorclient.systems.hud.HudElement
    public void render(HudRenderer renderer) {
        renderer.post(() -> {
            class_746 class_746Var = MeteorClient.mc.field_1724;
            if (class_746Var == null) {
                return;
            }
            float offset = this.centerOrientation.get() == CenterOrientation.North ? 180.0f : 0.0f;
            float yaw = this.copyYaw.get().booleanValue() ? class_3532.method_15393(class_746Var.method_36454() + offset) : this.customYaw.get().intValue();
            float pitch = this.copyPitch.get().booleanValue() ? class_746Var.method_36455() : this.customPitch.get().intValue();
            drawEntity(renderer.drawContext, this.x, this.y, (int) (30.0d * this.scale.get().doubleValue()), -yaw, -pitch, class_746Var);
        });
        if (this.background.get().booleanValue()) {
            renderer.quad(this.x, this.y, getWidth(), getHeight(), this.backgroundColor.get());
        } else if (MeteorClient.mc.field_1724 == null) {
            renderer.quad(this.x, this.y, getWidth(), getHeight(), this.backgroundColor.get());
            renderer.line(this.x, this.y, this.x + getWidth(), this.y + getHeight(), Color.GRAY);
            renderer.line(this.x + getWidth(), this.y, this.x, this.y + getHeight(), Color.GRAY);
        }
    }

    private void calculateSize() {
        setSize(50.0d * this.scale.get().doubleValue(), 75.0d * this.scale.get().doubleValue());
    }

    private void drawEntity(class_332 context, int x, int y, int size, float yaw, float pitch, class_1309 entity) {
        float tanYaw = (float) Math.atan(yaw / 40.0f);
        float tanPitch = (float) Math.atan(pitch / 40.0f);
        Quaternionf quaternion = new Quaternionf().rotateZ(3.1415927f);
        float previousBodyYaw = entity.field_6283;
        float previousYaw = entity.method_36454();
        float previousPitch = entity.method_36455();
        float previousPrevHeadYaw = entity.method_5791();
        float prevHeadYaw = entity.field_6241;
        entity.field_6283 = 180.0f + (tanYaw * 20.0f);
        entity.method_36456(180.0f + (tanYaw * 40.0f));
        entity.method_36457((-tanPitch) * 20.0f);
        entity.field_6241 = entity.method_36454();
        entity.method_5847(entity.method_36454());
        class_490.method_48472(context, x + (getWidth() / 2), y + (getHeight() * 0.9f), size, new Vector3f(), quaternion, (Quaternionf) null, entity);
        entity.field_6283 = previousBodyYaw;
        entity.method_36456(previousYaw);
        entity.method_36457(previousPitch);
        entity.method_5847(previousPrevHeadYaw);
        entity.field_6241 = prevHeadYaw;
    }
}
