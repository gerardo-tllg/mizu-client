package meteordevelopment.meteorclient.gui.themes.meteor.widgets.input;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorGuiTheme;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorWidget;
import meteordevelopment.meteorclient.gui.widgets.input.WSlider;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/themes/meteor/widgets/input/WMeteorSlider.class */
public class WMeteorSlider extends WSlider implements MeteorWidget {
    public WMeteorSlider(double value, double min, double max) {
        super(value, min, max);
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        double valueWidth = valueWidth();
        renderBar(renderer, valueWidth);
        renderHandle(renderer, valueWidth);
    }

    private void renderBar(GuiRenderer renderer, double valueWidth) {
        MeteorGuiTheme theme = theme();
        double s = theme.scale(3.0d);
        double handleSize = handleSize();
        double x = this.x + (handleSize / 2.0d);
        double y = (this.y + (this.height / 2.0d)) - (s / 2.0d);
        renderer.quad(x, y, valueWidth, s, theme.sliderLeft.get());
        renderer.quad(x + valueWidth, y, (this.width - valueWidth) - handleSize, s, theme.sliderRight.get());
    }

    private void renderHandle(GuiRenderer renderer, double valueWidth) {
        MeteorGuiTheme theme = theme();
        double s = handleSize();
        renderer.quad(this.x + valueWidth, this.y, s, s, GuiRenderer.CIRCLE, theme.sliderHandle.get(this.dragging, this.handleMouseOver));
    }
}
