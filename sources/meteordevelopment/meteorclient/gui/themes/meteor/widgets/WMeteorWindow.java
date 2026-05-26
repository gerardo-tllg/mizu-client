package meteordevelopment.meteorclient.gui.themes.meteor.widgets;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorWidget;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/themes/meteor/widgets/WMeteorWindow.class */
public class WMeteorWindow extends WWindow implements MeteorWidget {
    public WMeteorWindow(WWidget icon, String title) {
        super(icon, title);
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.containers.WWindow
    protected WWindow.WHeader header(WWidget icon) {
        return new WMeteorHeader(icon);
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (this.expanded || this.animProgress > 0.0d) {
            renderer.quad(this.x, this.y + this.header.height, this.width, this.height - this.header.height, theme().backgroundColor.get());
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/themes/meteor/widgets/WMeteorWindow$WMeteorHeader.class */
    private class WMeteorHeader extends WWindow.WHeader {
        public WMeteorHeader(WWidget icon) {
            super(icon);
        }

        @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            renderer.quad(this, WMeteorWindow.this.theme().accentColor.get());
        }
    }
}
