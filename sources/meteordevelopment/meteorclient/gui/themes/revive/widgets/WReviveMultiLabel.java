package meteordevelopment.meteorclient.gui.themes.revive.widgets;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.revive.ReviveWidget;
import meteordevelopment.meteorclient.gui.widgets.WMultiLabel;
import meteordevelopment.meteorclient.utils.render.color.Color;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/themes/revive/widgets/WReviveMultiLabel.class */
public class WReviveMultiLabel extends WMultiLabel implements ReviveWidget {
    public WReviveMultiLabel(String text, boolean title, double maxWidth) {
        super(text, title, maxWidth);
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        double h = this.theme.textHeight(this.title);
        Color defaultColor = theme().textColor.get();
        for (int i = 0; i < this.lines.size(); i++) {
            renderer.text(this.lines.get(i), this.x, this.y + (h * ((double) i)), this.color != null ? this.color : defaultColor, false);
        }
    }
}
