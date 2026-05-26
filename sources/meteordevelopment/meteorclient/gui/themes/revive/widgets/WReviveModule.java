package meteordevelopment.meteorclient.gui.themes.revive.widgets;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.revive.ReviveGuiTheme;
import meteordevelopment.meteorclient.gui.themes.revive.ReviveWidget;
import meteordevelopment.meteorclient.gui.utils.AlignmentX;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.class_3532;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/themes/revive/widgets/WReviveModule.class */
public class WReviveModule extends WPressable implements ReviveWidget {
    private final Module module;
    private double titleWidth;
    private double animProgress1;
    private double animProgress2;

    public WReviveModule(Module module) {
        this.module = module;
        this.tooltip = module.description;
        this.animProgress1 = module.isActive() ? 1.0d : 0.0d;
        this.animProgress2 = module.isActive() ? 1.0d : 0.0d;
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    public double pad() {
        return this.theme.scale(7.0d);
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
        ReviveGuiTheme theme = theme();
        double pad = pad();
        this.animProgress1 += delta * 5.0d * ((double) ((this.module.isActive() || this.mouseOver) ? 1 : -1));
        this.animProgress1 = class_3532.method_15350(this.animProgress1, 0.0d, 1.0d);
        this.animProgress2 += delta * 8.0d * ((double) (this.module.isActive() ? 1 : -1));
        this.animProgress2 = class_3532.method_15350(this.animProgress2, 0.0d, 1.0d);
        if (this.animProgress1 > 0.0d) {
            renderer.quad(this.x, this.y, this.width * this.animProgress1, this.height, theme.moduleBackground.get());
        }
        if (this.animProgress2 > 0.0d) {
            double barW = theme.scale(3.0d);
            renderer.quad(this.x, this.y + (this.height * (1.0d - this.animProgress2)), barW, this.height * this.animProgress2, theme.accentColor.get());
        }
        renderer.quad(this.x, (this.y + this.height) - theme.scale(1.0d), this.width, theme.scale(1.0d), theme.outlineColor.get());
        double tx = this.x + pad;
        double w = this.width - (pad * 2.0d);
        if (theme.moduleAlignment.get() == AlignmentX.Center) {
            tx += (w / 2.0d) - (this.titleWidth / 2.0d);
        } else if (theme.moduleAlignment.get() == AlignmentX.Right) {
            tx += w - this.titleWidth;
        }
        renderer.text(this.module.title, tx, this.y + pad, theme.textColor.get(), false);
    }
}
