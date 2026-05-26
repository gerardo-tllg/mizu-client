package meteordevelopment.meteorclient.gui.widgets;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.renderer.Texture;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/widgets/WTexture.class */
public class WTexture extends WWidget {
    private final double width;
    private final double height;
    private final double rotation;
    private final Texture texture;

    public WTexture(double width, double height, double rotation, Texture texture) {
        this.width = width;
        this.height = height;
        this.rotation = rotation;
        this.texture = texture;
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    protected void onCalculateSize() {
        super.width = this.theme.scale(this.width);
        super.height = this.theme.scale(this.height);
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        renderer.texture(this.x, this.y, super.width, super.height, this.rotation, this.texture);
    }
}
