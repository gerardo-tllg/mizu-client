package meteordevelopment.meteorclient.gui.themes.mizu.widgets.input;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.mizu.MizuGuiTheme;
import meteordevelopment.meteorclient.gui.themes.mizu.MizuWidget;
import meteordevelopment.meteorclient.gui.widgets.input.WDropdown;
import meteordevelopment.meteorclient.utils.render.color.Color;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/themes/mizu/widgets/input/WMizuDropdown.class */
public class WMizuDropdown<T> extends WDropdown<T> implements MizuWidget {
    public WMizuDropdown(T[] values, T value) {
        super(values, value);
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.input.WDropdown
    protected WDropdown.WDropdownRoot createRootWidget() {
        return new WRoot();
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.input.WDropdown
    protected WDropdown<T>.WDropdownValue createValueWidget() {
        return new WValue(this);
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        MizuGuiTheme theme = theme();
        double pad = pad();
        double s = theme.textHeight();
        renderBackground(renderer, this, this.pressed, this.mouseOver);
        String text = get().toString();
        double w = theme.textWidth(text);
        renderer.text(text, ((this.x + pad) + (this.maxValueWidth / 2.0d)) - (w / 2.0d), this.y + pad, theme.textColor.get(), false);
        renderer.rotatedQuad(this.x + pad + this.maxValueWidth + pad, this.y + pad, s, s, 0.0d, GuiRenderer.TRIANGLE, theme.accentColor.get());
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/themes/mizu/widgets/input/WMizuDropdown$WRoot.class */
    private static class WRoot extends WDropdown.WDropdownRoot implements MizuWidget {
        private WRoot() {
        }

        @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            MizuGuiTheme theme = theme();
            double s = theme.scale(1.0d);
            Color c = theme.outlineColor.get();
            renderer.quad(this.x, (this.y + this.height) - s, this.width, s, c);
            renderer.quad(this.x, this.y, s, this.height - s, c);
            renderer.quad((this.x + this.width) - s, this.y, s, this.height - s, c);
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/themes/mizu/widgets/input/WMizuDropdown$WValue.class */
    private class WValue extends WDropdown<T>.WDropdownValue implements MizuWidget {
        private WValue(WMizuDropdown wMizuDropdown) {
            super();
        }

        @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
        protected void onCalculateSize() {
            double pad = pad();
            this.width = pad + this.theme.textWidth(this.value.toString()) + pad;
            this.height = pad + this.theme.textHeight() + pad;
        }

        @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            MizuGuiTheme theme = theme();
            Color color = theme.backgroundColor.get(this.pressed, this.mouseOver, true);
            int preA = color.a;
            color.a += color.a / 2;
            color.validate();
            renderer.quad(this, color);
            color.a = preA;
            String text = this.value.toString();
            renderer.text(text, (this.x + (this.width / 2.0d)) - (theme.textWidth(text) / 2.0d), this.y + pad(), theme.textColor.get(), false);
        }
    }
}


