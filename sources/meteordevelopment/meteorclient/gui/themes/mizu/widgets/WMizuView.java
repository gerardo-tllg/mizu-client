package meteordevelopment.meteorclient.gui.themes.mizu.widgets;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.mizu.MizuWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WView;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/themes/mizu/widgets/WMizuView.class */
public class WMizuView extends WView implements MizuWidget {
    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (this.canScroll && this.hasScrollBar) {
            renderer.quad(handleX(), handleY(), handleWidth(), handleHeight(), theme().scrollbarColor.get(this.handlePressed, this.handleMouseOver));
        }
    }
}


