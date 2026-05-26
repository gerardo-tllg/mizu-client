package meteordevelopment.meteorclient.gui.themes.revive.widgets;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.revive.ReviveGuiTheme;
import meteordevelopment.meteorclient.gui.themes.revive.ReviveWidget;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/themes/revive/widgets/WReviveWindow.class */
public class WReviveWindow extends WWindow implements ReviveWidget {
    public WReviveWindow(WWidget icon, String title) {
        super(icon, title);
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.containers.WWindow
    protected WWindow.WHeader header(WWidget icon) {
        return new WReviveHeader(icon);
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (this.expanded || this.animProgress > 0.0d) {
            ReviveGuiTheme theme = theme();
            renderer.quad(this.x, this.y + this.header.height, this.width, this.height - this.header.height, theme.backgroundColor.get());
            renderer.quad(this.x, this.y + this.header.height, theme.scale(2.0d), this.height - this.header.height, theme.accentColor.get());
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/themes/revive/widgets/WReviveWindow$WReviveHeader.class */
    private class WReviveHeader extends WWindow.WHeader {
        public WReviveHeader(WWidget icon) {
            super(icon);
        }

        @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            ReviveGuiTheme theme = WReviveWindow.this.theme();
            renderer.quad(this, theme.backgroundColor.get());
            renderer.quad(this.x, (this.y + this.height) - theme.scale(2.0d), this.width, theme.scale(2.0d), theme.accentColor.get());
        }
    }
}
