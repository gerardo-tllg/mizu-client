package meteordevelopment.meteorclient.gui.themes.mizu.widgets;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.mizu.MizuGuiTheme;
import meteordevelopment.meteorclient.gui.themes.mizu.MizuWidget;
import meteordevelopment.meteorclient.gui.widgets.WHorizontalSeparator;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/themes/mizu/widgets/WMizuHorizontalSeparator.class */
public class WMizuHorizontalSeparator extends WHorizontalSeparator implements MizuWidget {
    public WMizuHorizontalSeparator(String text) {
        super(text);
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (this.text != null) {
            renderWithText(renderer);
        } else {
            renderWithoutText(renderer);
        }
    }

    private void renderWithoutText(GuiRenderer renderer) {
        MizuGuiTheme theme = theme();
        double s = theme.scale(1.0d);
        double w = this.width / 2.0d;
        renderer.quad(this.x, this.y + s, w, s, theme.separatorEdges.get(), theme.separatorCenter.get());
        renderer.quad(this.x + w, this.y + s, w, s, theme.separatorCenter.get(), theme.separatorEdges.get());
    }

    private void renderWithText(GuiRenderer renderer) {
        MizuGuiTheme theme = theme();
        double s = theme.scale(2.0d);
        double h = theme.scale(1.0d);
        double textStart = Math.round(((this.width / 2.0d) - (this.textWidth / 2.0d)) - s);
        double textEnd = s + textStart + this.textWidth + s;
        double offsetY = Math.round(this.height / 2.0d);
        renderer.quad(this.x, this.y + offsetY, textStart, h, theme.separatorEdges.get(), theme.separatorCenter.get());
        renderer.text(this.text, this.x + textStart + s, this.y, theme.separatorText.get(), false);
        renderer.quad(this.x + textEnd, this.y + offsetY, this.width - textEnd, h, theme.separatorCenter.get(), theme.separatorEdges.get());
    }
}


