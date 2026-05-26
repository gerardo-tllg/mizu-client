package meteordevelopment.meteorclient.gui.themes.mizu.widgets;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.mizu.MizuWidget;
import meteordevelopment.meteorclient.gui.widgets.WTooltip;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/themes/mizu/widgets/WMizuTooltip.class */
public class WMizuTooltip extends WTooltip implements MizuWidget {
    public WMizuTooltip(String text) {
        super(text);
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        renderer.quad(this, theme().backgroundColor.get());
        renderer.quad(this.x, this.y, this.width, theme().scale(1.0d), theme().accentColor.get());
    }
}


