package meteordevelopment.meteorclient.gui.themes.mizu.widgets.pressable;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.mizu.MizuWidget;
import meteordevelopment.meteorclient.gui.widgets.pressable.WTriangle;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/themes/mizu/widgets/pressable/WMizuTriangle.class */
public class WMizuTriangle extends WTriangle implements MizuWidget {
    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        renderer.rotatedQuad(this.x, this.y, this.width, this.height, this.rotation, GuiRenderer.TRIANGLE, theme().accentColor.get());
    }
}


