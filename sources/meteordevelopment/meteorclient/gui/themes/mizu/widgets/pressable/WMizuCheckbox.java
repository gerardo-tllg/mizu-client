package meteordevelopment.meteorclient.gui.themes.mizu.widgets.pressable;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.mizu.MizuGuiTheme;
import meteordevelopment.meteorclient.gui.themes.mizu.MizuWidget;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;
import net.minecraft.class_3532;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/themes/mizu/widgets/pressable/WMizuCheckbox.class */
public class WMizuCheckbox extends WCheckbox implements MizuWidget {
    private double animProgress;

    public WMizuCheckbox(boolean checked) {
        super(checked);
        this.animProgress = checked ? 1.0d : 0.0d;
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        MizuGuiTheme theme = theme();
        this.animProgress += ((double) (this.checked ? 1 : -1)) * delta * 14.0d;
        this.animProgress = class_3532.method_15350(this.animProgress, 0.0d, 1.0d);
        renderBackground(renderer, this, this.pressed, this.mouseOver);
        if (this.animProgress > 0.0d) {
            double cs = ((this.width - theme.scale(2.0d)) / 1.75d) * this.animProgress;
            renderer.quad(this.x + ((this.width - cs) / 2.0d), this.y + ((this.height - cs) / 2.0d), cs, cs, theme.checkboxColor.get());
        }
    }
}


