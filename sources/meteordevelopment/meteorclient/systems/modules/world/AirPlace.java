package meteordevelopment.meteorclient.systems.modules.world;

import java.util.Objects;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1268;
import net.minecraft.class_1747;
import net.minecraft.class_1826;
import net.minecraft.class_2338;
import net.minecraft.class_239;
import net.minecraft.class_2680;
import net.minecraft.class_3965;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/world/AirPlace.class */
public class AirPlace extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgRange;
    private final Setting<Boolean> render;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    private final Setting<Boolean> customRange;
    private final Setting<Double> range;
    private class_239 hitResult;

    public AirPlace() {
        super(Categories.Player, "air-place", "Places a block where your crosshair is pointing at.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgRange = this.settings.createGroup("Range");
        this.render = this.sgGeneral.add(new BoolSetting.Builder().name("render").description("Renders a block overlay where the block will be placed.").defaultValue(true).build());
        this.shapeMode = this.sgGeneral.add(new EnumSetting.Builder().name("shape-mode").description("How the shapes are rendered.").defaultValue(ShapeMode.Both).build());
        this.sideColor = this.sgGeneral.add(new ColorSetting.Builder().name("side-color").description("The color of the sides of the blocks being rendered.").defaultValue(new SettingColor(204, 0, 0, 10)).build());
        this.lineColor = this.sgGeneral.add(new ColorSetting.Builder().name("line-color").description("The color of the lines of the blocks being rendered.").defaultValue(new SettingColor(204, 0, 0, 255)).build());
        this.customRange = this.sgRange.add(new BoolSetting.Builder().name("custom-range").description("Use custom range for air place.").defaultValue(false).build());
        SettingGroup settingGroup = this.sgRange;
        DoubleSetting.Builder builderDescription = new DoubleSetting.Builder().name("range").description("Custom range to place at.");
        Setting<Boolean> setting = this.customRange;
        Objects.requireNonNull(setting);
        this.range = settingGroup.add(builderDescription.visible(setting::get).defaultValue(5.0d).min(0.0d).sliderMax(6.0d).build());
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) {
            return;
        }
        double r = this.customRange.get().booleanValue() ? this.range.get().doubleValue() : this.mc.field_1724.method_55754();
        this.hitResult = this.mc.method_1560().method_5745(r, 0.0f, false);
        class_3965 class_3965Var = this.hitResult;
        if (class_3965Var instanceof class_3965) {
            class_3965 blockHitResult = class_3965Var;
            if (((this.mc.field_1724.method_6047().method_7909() instanceof class_1747) || (this.mc.field_1724.method_6047().method_7909() instanceof class_1826)) && this.mc.field_1690.field_1904.method_1434()) {
                BlockUtils.place(blockHitResult.method_17777(), class_1268.field_5808, this.mc.field_1724.method_31548().method_67532(), false, 0, true, true, false);
            }
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        class_2338 class_2338VarMethod_10093;
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) {
            return;
        }
        class_3965 class_3965Var = this.hitResult;
        if (class_3965Var instanceof class_3965) {
            class_3965 blockHitResult = class_3965Var;
            if (((this.mc.field_1724.method_6047().method_7909() instanceof class_1747) || (this.mc.field_1724.method_6047().method_7909() instanceof class_1826)) && this.render.get().booleanValue()) {
                class_2680 hitState = this.mc.field_1687.method_8320(blockHitResult.method_17777());
                if (hitState.method_45474()) {
                    class_2338VarMethod_10093 = blockHitResult.method_17777();
                } else {
                    class_2338VarMethod_10093 = blockHitResult.method_17777().method_10093(blockHitResult.method_17780());
                }
                class_2338 renderPos = class_2338VarMethod_10093;
                if (this.mc.field_1687.method_8320(renderPos).method_45474()) {
                    event.renderer.box(renderPos, this.sideColor.get(), this.lineColor.get(), this.shapeMode.get(), 0);
                }
            }
        }
    }
}
