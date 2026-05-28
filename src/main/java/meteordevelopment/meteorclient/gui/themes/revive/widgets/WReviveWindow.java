package meteordevelopment.meteorclient.gui.themes.revive.widgets;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.revive.ReviveGuiTheme;
import meteordevelopment.meteorclient.gui.themes.revive.ReviveWidget;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;

public class WReviveWindow extends WWindow implements ReviveWidget {
    public WReviveWindow(WWidget icon, String title) {
        super(icon, title);
    }

    @Override
    protected WHeader header(WWidget icon) {
        return new WReviveHeader(icon);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (expanded || animProgress > 0) {
            ReviveGuiTheme theme = theme();
            // Body background
            renderer.quad(x, y + header.height, width, height - header.height,
                theme.backgroundColor.get());
            // Left accent border on body
            renderer.quad(x, y + header.height, theme.scale(2), height - header.height,
                theme.accentColor.get());
        }
    }

    private class WReviveHeader extends WHeader {
        public WReviveHeader(WWidget icon) {
            super(icon);
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            ReviveGuiTheme theme = theme();
            // Dark header background
            renderer.quad(this, theme.backgroundColor.get());
            // Bottom border line in accent color
            renderer.quad(x, y + height - theme.scale(2), width, theme.scale(2),
                theme.accentColor.get());
        }
    }
}
