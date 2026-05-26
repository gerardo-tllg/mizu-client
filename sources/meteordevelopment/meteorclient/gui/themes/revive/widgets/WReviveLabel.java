package meteordevelopment.meteorclient.gui.themes.revive.widgets;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.revive.ReviveWidget;
import meteordevelopment.meteorclient.gui.widgets.WLabel;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/themes/revive/widgets/WReviveLabel.class */
public class WReviveLabel extends WLabel implements ReviveWidget {
    public WReviveLabel(String text, boolean title) {
        super(text, title);
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (!this.text.isEmpty()) {
            renderer.text(this.text, this.x, this.y, this.color != null ? this.color : this.title ? theme().titleTextColor.get() : theme().textColor.get(), this.title);
        }
    }
}
