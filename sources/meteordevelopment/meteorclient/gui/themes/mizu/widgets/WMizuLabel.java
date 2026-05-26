package meteordevelopment.meteorclient.gui.themes.mizu.widgets;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.mizu.MizuWidget;
import meteordevelopment.meteorclient.gui.widgets.WLabel;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/themes/mizu/widgets/WMizuLabel.class */
public class WMizuLabel extends WLabel implements MizuWidget {
    public WMizuLabel(String text, boolean title) {
        super(text, title);
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (!this.text.isEmpty()) {
            renderer.text(this.text, this.x, this.y, this.color != null ? this.color : this.title ? theme().titleTextColor.get() : theme().textColor.get(), this.title);
        }
    }
}


