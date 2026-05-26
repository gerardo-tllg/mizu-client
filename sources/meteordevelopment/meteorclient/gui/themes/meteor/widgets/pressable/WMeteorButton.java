package meteordevelopment.meteorclient.gui.themes.meteor.widgets.pressable;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.renderer.packer.GuiTexture;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorGuiTheme;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorWidget;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/themes/meteor/widgets/pressable/WMeteorButton.class */
public class WMeteorButton extends WButton implements MeteorWidget {
    public WMeteorButton(String text, GuiTexture texture) {
        super(text, texture);
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        MeteorGuiTheme theme = theme();
        double pad = pad();
        renderBackground(renderer, this, this.pressed, this.mouseOver);
        if (this.text != null) {
            renderer.text(this.text, (this.x + (this.width / 2.0d)) - (this.textWidth / 2.0d), this.y + pad, theme.textColor.get(), false);
        } else {
            double ts = theme.textHeight();
            renderer.quad((this.x + (this.width / 2.0d)) - (ts / 2.0d), this.y + pad, ts, ts, this.texture, theme.textColor.get());
        }
    }
}
