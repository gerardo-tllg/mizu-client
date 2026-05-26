package meteordevelopment.meteorclient.gui.widgets.pressable;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/widgets/pressable/WFavorite.class */
public abstract class WFavorite extends WPressable {
    public boolean checked;

    protected abstract Color getColor();

    public WFavorite(boolean checked) {
        this.checked = checked;
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    protected void onCalculateSize() {
        double pad = pad();
        double s = this.theme.textHeight();
        this.width = pad + s + pad;
        this.height = pad + s + pad;
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.pressable.WPressable
    protected void onPressed(int button) {
        this.checked = !this.checked;
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        double pad = pad();
        double s = this.theme.textHeight();
        renderer.quad(this.x + pad, this.y + pad, s, s, this.checked ? GuiRenderer.FAVORITE_YES : GuiRenderer.FAVORITE_NO, getColor());
    }
}
