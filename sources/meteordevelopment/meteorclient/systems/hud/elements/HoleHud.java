package meteordevelopment.meteorclient.systems.hud.elements;

import java.util.List;
import java.util.Objects;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2350;
import net.minecraft.class_3532;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/hud/elements/HoleHud.class */
public class HoleHud extends HudElement {
    public static final HudElementInfo<HoleHud> INFO = new HudElementInfo<>(Hud.GROUP, "hole", "Displays information about the hole you are standing in.", HoleHud::new);
    private final SettingGroup sgGeneral;
    private final SettingGroup sgBackground;
    public final Setting<List<class_2248>> safe;
    private final Setting<Double> scale;
    private final Setting<Integer> border;
    private final Setting<Boolean> background;
    private final Setting<SettingColor> backgroundColor;
    private final Color BG_COLOR;
    private final Color OL_COLOR;

    public HoleHud() {
        super(INFO);
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgBackground = this.settings.createGroup("Background");
        this.safe = this.sgGeneral.add(new BlockListSetting.Builder().name("safe-blocks").description("Which blocks to consider safe.").defaultValue(class_2246.field_10540, class_2246.field_9987, class_2246.field_22423, class_2246.field_22108).build());
        this.scale = this.sgGeneral.add(new DoubleSetting.Builder().name("scale").description("The scale.").defaultValue(2.0d).onChanged(aDouble -> {
            calculateSize();
        }).min(1.0d).sliderRange(1.0d, 5.0d).build());
        this.border = this.sgGeneral.add(new IntSetting.Builder().name("border").description("How much space to add around the element.").defaultValue(0).onChanged(integer -> {
            calculateSize();
        }).build());
        this.background = this.sgBackground.add(new BoolSetting.Builder().name("background").description("Displays background.").defaultValue(false).build());
        SettingGroup settingGroup = this.sgBackground;
        ColorSetting.Builder builderDescription = new ColorSetting.Builder().name("background-color").description("Color used for the background.");
        Setting<Boolean> setting = this.background;
        Objects.requireNonNull(setting);
        this.backgroundColor = settingGroup.add(builderDescription.visible(setting::get).defaultValue(new SettingColor(25, 25, 25, 50)).build());
        this.BG_COLOR = new Color(255, 25, 25, 100);
        this.OL_COLOR = new Color(255, 25, 25, 255);
        calculateSize();
    }

    @Override // meteordevelopment.meteorclient.systems.hud.HudElement
    public void setSize(double width, double height) {
        super.setSize(width + ((double) (this.border.get().intValue() * 2)), height + ((double) (this.border.get().intValue() * 2)));
    }

    private void calculateSize() {
        setSize(48.0d * this.scale.get().doubleValue(), 48.0d * this.scale.get().doubleValue());
    }

    @Override // meteordevelopment.meteorclient.systems.hud.HudElement
    public void render(HudRenderer renderer) {
        renderer.post(() -> {
            double x = this.x + this.border.get().intValue();
            double y = this.y + this.border.get().intValue();
            drawBlock(renderer, get(Facing.Left), x, y + (16.0d * this.scale.get().doubleValue()));
            drawBlock(renderer, get(Facing.Front), x + (16.0d * this.scale.get().doubleValue()), y);
            drawBlock(renderer, get(Facing.Right), x + (32.0d * this.scale.get().doubleValue()), y + (16.0d * this.scale.get().doubleValue()));
            drawBlock(renderer, get(Facing.Back), x + (16.0d * this.scale.get().doubleValue()), y + (32.0d * this.scale.get().doubleValue()));
        });
        if (this.background.get().booleanValue()) {
            renderer.quad(this.x, this.y, getWidth(), getHeight(), this.backgroundColor.get());
        }
    }

    private class_2350 get(Facing dir) {
        if (isInEditor()) {
            return class_2350.field_11033;
        }
        float yaw = class_3532.method_15393(MeteorClient.mc.field_1724.method_36454() + dir.offset);
        return class_2350.method_10150(yaw).method_10153();
    }

    private void drawBlock(HudRenderer renderer, class_2350 dir, double x, double y) {
        class_2248 block = dir == class_2350.field_11033 ? class_2246.field_10540 : MeteorClient.mc.field_1687.method_8320(MeteorClient.mc.field_1724.method_24515().method_10093(dir)).method_26204();
        if (this.safe.get().contains(block)) {
            renderer.item(block.method_8389().method_7854(), (int) x, (int) y, this.scale.get().floatValue(), false);
            if (dir == class_2350.field_11033) {
                return;
            }
            MeteorClient.mc.field_1769.getBlockBreakingInfos().values().forEach(info -> {
                if (info.method_13991().equals(MeteorClient.mc.field_1724.method_24515().method_10093(dir))) {
                    renderBreaking(renderer, x, y, info.method_13988() / 9.0f);
                }
            });
        }
    }

    private void renderBreaking(HudRenderer renderer, double x, double y, double percent) {
        renderer.quad(x, y, 16.0d * percent * this.scale.get().doubleValue(), 16.0d * this.scale.get().doubleValue(), this.BG_COLOR);
        renderer.quad(x, y, 16.0d * this.scale.get().doubleValue(), 1.0d * this.scale.get().doubleValue(), this.OL_COLOR);
        renderer.quad(x, y + (15.0d * this.scale.get().doubleValue()), 16.0d * this.scale.get().doubleValue(), 1.0d * this.scale.get().doubleValue(), this.OL_COLOR);
        renderer.quad(x, y, 1.0d * this.scale.get().doubleValue(), 16.0d * this.scale.get().doubleValue(), this.OL_COLOR);
        renderer.quad(x + (15.0d * this.scale.get().doubleValue()), y, 1.0d * this.scale.get().doubleValue(), 16.0d * this.scale.get().doubleValue(), this.OL_COLOR);
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/hud/elements/HoleHud$Facing.class */
    private enum Facing {
        Left(-90),
        Right(90),
        Front(0),
        Back(Opcode.GETFIELD);

        public final int offset;

        Facing(int offset) {
            this.offset = offset;
        }
    }
}
