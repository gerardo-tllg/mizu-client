package meteordevelopment.meteorclient.systems.modules.render.blockesp;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.utils.IScreenFactory;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BlockDataSetting;
import meteordevelopment.meteorclient.settings.IBlockData;
import meteordevelopment.meteorclient.utils.misc.IChangeable;
import meteordevelopment.meteorclient.utils.misc.ICopyable;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.class_2248;
import net.minecraft.class_2487;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/blockesp/ESPBlockData.class */
public class ESPBlockData implements ICopyable<ESPBlockData>, ISerializable<ESPBlockData>, IChangeable, IBlockData<ESPBlockData>, IScreenFactory {
    public ShapeMode shapeMode;
    public SettingColor lineColor;
    public SettingColor sideColor;
    public boolean tracer;
    public SettingColor tracerColor;
    private boolean changed;

    public ESPBlockData(ShapeMode shapeMode, SettingColor lineColor, SettingColor sideColor, boolean tracer, SettingColor tracerColor) {
        this.shapeMode = shapeMode;
        this.lineColor = lineColor;
        this.sideColor = sideColor;
        this.tracer = tracer;
        this.tracerColor = tracerColor;
    }

    @Override // meteordevelopment.meteorclient.settings.IBlockData
    public WidgetScreen createScreen(GuiTheme theme, class_2248 block, BlockDataSetting<ESPBlockData> setting) {
        return new ESPBlockDataScreen(theme, this, block, setting);
    }

    @Override // meteordevelopment.meteorclient.gui.utils.IScreenFactory
    public WidgetScreen createScreen(GuiTheme theme) {
        return new ESPBlockDataScreen(theme, this, null, null);
    }

    @Override // meteordevelopment.meteorclient.utils.misc.IChangeable
    public boolean isChanged() {
        return this.changed;
    }

    public void changed() {
        this.changed = true;
    }

    public void tickRainbow() {
        this.lineColor.update();
        this.sideColor.update();
        this.tracerColor.update();
    }

    @Override // meteordevelopment.meteorclient.utils.misc.ICopyable
    public ESPBlockData set(ESPBlockData value) {
        this.shapeMode = value.shapeMode;
        this.lineColor.set((Color) value.lineColor);
        this.sideColor.set((Color) value.sideColor);
        this.tracer = value.tracer;
        this.tracerColor.set((Color) value.tracerColor);
        this.changed = value.changed;
        return this;
    }

    @Override // meteordevelopment.meteorclient.utils.misc.ICopyable
    public ESPBlockData copy() {
        return new ESPBlockData(this.shapeMode, new SettingColor(this.lineColor), new SettingColor(this.sideColor), this.tracer, new SettingColor(this.tracerColor));
    }

    @Override // meteordevelopment.meteorclient.utils.misc.ISerializable
    public class_2487 toTag() {
        class_2487 tag = new class_2487();
        tag.method_10582("shapeMode", this.shapeMode.name());
        tag.method_10566("lineColor", this.lineColor.toTag());
        tag.method_10566("sideColor", this.sideColor.toTag());
        tag.method_10556("tracer", this.tracer);
        tag.method_10566("tracerColor", this.tracerColor.toTag());
        tag.method_10556("changed", this.changed);
        return tag;
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // meteordevelopment.meteorclient.utils.misc.ISerializable
    /* JADX INFO: renamed from: fromTag */
    public ESPBlockData fromTag(class_2487 tag) {
        this.shapeMode = ShapeMode.valueOf((String) tag.method_10558("shapeMode").orElse("Both"));
        this.lineColor.fromTag((class_2487) tag.method_10562("lineColor").orElse(new class_2487()));
        this.sideColor.fromTag((class_2487) tag.method_10562("sideColor").orElse(new class_2487()));
        this.tracer = ((Boolean) tag.method_10577("tracer").orElse(false)).booleanValue();
        this.tracerColor.fromTag((class_2487) tag.method_10562("tracerColor").orElse(new class_2487()));
        this.changed = ((Boolean) tag.method_10577("changed").orElse(false)).booleanValue();
        return this;
    }
}
