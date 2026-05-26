package meteordevelopment.meteorclient.gui.themes.mizu.widgets;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.mizu.MizuGuiTheme;
import meteordevelopment.meteorclient.gui.themes.mizu.MizuWidget;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/themes/mizu/widgets/WMizuWindow.class */
public class WMizuWindow extends WWindow implements MizuWidget {
    public WMizuWindow(WWidget icon, String title) {
        super(icon, title);
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.containers.WWindow
    protected WWindow.WHeader header(WWidget icon) {
        return new WMizuHeader(icon);
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (this.expanded || this.animProgress > 0.0d) {
            MizuGuiTheme theme = theme();
            renderer.quad(this.x, this.y + this.header.height, this.width, this.height - this.header.height, theme.backgroundColor.get());
            renderer.quad(this.x, this.y + this.header.height, theme.scale(2.0d), this.height - this.header.height, theme.accentColor.get());
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/themes/mizu/widgets/WMizuWindow$WMizuHeader.class */
    private class WMizuHeader extends WWindow.WHeader {
        public WMizuHeader(WWidget icon) {
            super(icon);
        }

        @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            MizuGuiTheme theme = WMizuWindow.this.theme();
            renderer.quad(this, theme.backgroundColor.get());
            renderer.quad(this.x, (this.y + this.height) - theme.scale(2.0d), this.width, theme.scale(2.0d), theme.accentColor.get());
        }
    }
}


