package meteordevelopment.meteorclient.gui.themes.mizu.widgets.pressable;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.mizu.MizuWidget;
import meteordevelopment.meteorclient.gui.widgets.pressable.WMinus;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/themes/mizu/widgets/pressable/WMizuMinus.class */
public class WMizuMinus extends WMinus implements MizuWidget {
    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        double pad = pad();
        double s = this.theme.scale(3.0d);
        renderBackground(renderer, this, this.pressed, this.mouseOver);
        renderer.quad(this.x + pad, (this.y + (this.height / 2.0d)) - (s / 2.0d), this.width - (pad * 2.0d), s, theme().minusColor.get());
    }
}


