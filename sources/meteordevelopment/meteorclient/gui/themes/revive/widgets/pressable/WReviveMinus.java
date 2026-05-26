package meteordevelopment.meteorclient.gui.themes.revive.widgets.pressable;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.revive.ReviveWidget;
import meteordevelopment.meteorclient.gui.widgets.pressable.WMinus;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/themes/revive/widgets/pressable/WReviveMinus.class */
public class WReviveMinus extends WMinus implements ReviveWidget {
    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        double pad = pad();
        double s = this.theme.scale(3.0d);
        renderBackground(renderer, this, this.pressed, this.mouseOver);
        renderer.quad(this.x + pad, (this.y + (this.height / 2.0d)) - (s / 2.0d), this.width - (pad * 2.0d), s, theme().minusColor.get());
    }
}
