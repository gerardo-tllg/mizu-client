package meteordevelopment.meteorclient.gui.themes.meteor.widgets;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorGuiTheme;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorWidget;
import meteordevelopment.meteorclient.gui.utils.AlignmentX;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.class_3532;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/themes/meteor/widgets/WMeteorModule.class */
public class WMeteorModule extends WPressable implements MeteorWidget {
    private final Module module;
    private double titleWidth;
    private double animationProgress1;
    private double animationProgress2;

    public WMeteorModule(Module module) {
        this.module = module;
        this.tooltip = module.description;
        if (module.isActive()) {
            this.animationProgress1 = 1.0d;
            this.animationProgress2 = 1.0d;
        } else {
            this.animationProgress1 = 0.0d;
            this.animationProgress2 = 0.0d;
        }
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    public double pad() {
        return this.theme.scale(4.0d);
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    protected void onCalculateSize() {
        double pad = pad();
        if (this.titleWidth == 0.0d) {
            this.titleWidth = this.theme.textWidth(this.module.title);
        }
        this.width = pad + this.titleWidth + pad;
        this.height = pad + this.theme.textHeight() + pad;
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.pressable.WPressable
    protected void onPressed(int button) {
        if (button != 0) {
            if (button == 1) {
                MeteorClient.mc.method_1507(this.theme.moduleScreen(this.module));
                return;
            }
            return;
        }
        this.module.toggle();
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        MeteorGuiTheme theme = theme();
        double pad = pad();
        this.animationProgress1 += delta * 4.0d * ((double) ((this.module.isActive() || this.mouseOver) ? 1 : -1));
        this.animationProgress1 = class_3532.method_15350(this.animationProgress1, 0.0d, 1.0d);
        this.animationProgress2 += delta * 6.0d * ((double) (this.module.isActive() ? 1 : -1));
        this.animationProgress2 = class_3532.method_15350(this.animationProgress2, 0.0d, 1.0d);
        if (this.animationProgress1 > 0.0d) {
            renderer.quad(this.x, this.y, this.width * this.animationProgress1, this.height, theme.moduleBackground.get());
        }
        if (this.animationProgress2 > 0.0d) {
            renderer.quad(this.x, this.y + (this.height * (1.0d - this.animationProgress2)), theme.scale(2.0d), this.height * this.animationProgress2, theme.accentColor.get());
        }
        double x = this.x + pad;
        double w = this.width - (pad * 2.0d);
        if (theme.moduleAlignment.get() == AlignmentX.Center) {
            x += (w / 2.0d) - (this.titleWidth / 2.0d);
        } else if (theme.moduleAlignment.get() == AlignmentX.Right) {
            x += w - this.titleWidth;
        }
        renderer.text(this.module.title, x, this.y + pad, theme.textColor.get(), false);
    }
}
