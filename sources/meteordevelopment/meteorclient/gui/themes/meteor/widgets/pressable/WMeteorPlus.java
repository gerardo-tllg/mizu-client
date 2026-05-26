package meteordevelopment.meteorclient.gui.themes.meteor.widgets.pressable;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorGuiTheme;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorWidget;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPlus;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/themes/meteor/widgets/pressable/WMeteorPlus.class */
public class WMeteorPlus extends WPlus implements MeteorWidget {
    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        MeteorGuiTheme theme = theme();
        double pad = pad();
        double s = theme.scale(3.0d);
        renderBackground(renderer, this, this.pressed, this.mouseOver);
        renderer.quad(this.x + pad, (this.y + (this.height / 2.0d)) - (s / 2.0d), this.width - (pad * 2.0d), s, theme.plusColor.get());
        renderer.quad((this.x + (this.width / 2.0d)) - (s / 2.0d), this.y + pad, s, this.height - (pad * 2.0d), theme.plusColor.get());
    }
}
