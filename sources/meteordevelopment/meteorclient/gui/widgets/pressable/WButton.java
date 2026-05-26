package meteordevelopment.meteorclient.gui.widgets.pressable;

import meteordevelopment.meteorclient.gui.renderer.packer.GuiTexture;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/widgets/pressable/WButton.class */
public abstract class WButton extends WPressable {
    protected String text;
    protected double textWidth;
    protected GuiTexture texture;

    public WButton(String text, GuiTexture texture) {
        this.text = text;
        this.texture = texture;
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    protected void onCalculateSize() {
        double pad = pad();
        if (this.text != null) {
            this.textWidth = this.theme.textWidth(this.text);
            this.width = pad + this.textWidth + pad;
            this.height = pad + this.theme.textHeight() + pad;
        } else {
            double s = this.theme.textHeight();
            this.width = pad + s + pad;
            this.height = pad + s + pad;
        }
    }

    public void set(String text) {
        if (this.text == null || Math.round(this.theme.textWidth(text)) != this.textWidth) {
            invalidate();
        }
        this.text = text;
    }

    public String getText() {
        return this.text;
    }
}
