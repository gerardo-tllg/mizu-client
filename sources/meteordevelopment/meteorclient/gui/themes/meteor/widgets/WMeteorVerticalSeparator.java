package meteordevelopment.meteorclient.gui.themes.meteor.widgets;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorGuiTheme;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorWidget;
import meteordevelopment.meteorclient.gui.widgets.WVerticalSeparator;
import meteordevelopment.meteorclient.utils.render.color.Color;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/themes/meteor/widgets/WMeteorVerticalSeparator.class */
public class WMeteorVerticalSeparator extends WVerticalSeparator implements MeteorWidget {
    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        MeteorGuiTheme theme = theme();
        Color colorEdges = theme.separatorEdges.get();
        Color colorCenter = theme.separatorCenter.get();
        double s = theme.scale(1.0d);
        double offsetX = Math.round(this.width / 2.0d);
        renderer.quad(this.x + offsetX, this.y, s, this.height / 2.0d, colorEdges, colorEdges, colorCenter, colorCenter);
        renderer.quad(this.x + offsetX, this.y + (this.height / 2.0d), s, this.height / 2.0d, colorCenter, colorCenter, colorEdges, colorEdges);
    }
}
