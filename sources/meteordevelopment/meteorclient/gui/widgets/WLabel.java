package meteordevelopment.meteorclient.gui.widgets;

import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable;
import meteordevelopment.meteorclient.utils.render.color.Color;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/widgets/WLabel.class */
public abstract class WLabel extends WPressable {
    public Color color;
    protected String text;
    protected boolean title;

    public WLabel(String text, boolean title) {
        this.text = text;
        this.title = title;
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    protected void onCalculateSize() {
        this.width = this.theme.textWidth(this.text, this.text.length(), this.title);
        this.height = this.theme.textHeight(this.title);
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.pressable.WPressable, meteordevelopment.meteorclient.gui.widgets.WWidget
    public boolean onMouseClicked(double mouseX, double mouseY, int button, boolean used) {
        if (this.action != null) {
            return super.onMouseClicked(mouseX, mouseY, button, used);
        }
        return false;
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.pressable.WPressable, meteordevelopment.meteorclient.gui.widgets.WWidget
    public boolean onMouseReleased(double mouseX, double mouseY, int button) {
        if (this.action != null) {
            return super.onMouseReleased(mouseX, mouseY, button);
        }
        return false;
    }

    public void set(String text) {
        if (Math.round(this.theme.textWidth(text, text.length(), this.title)) != this.width) {
            invalidate();
        }
        this.text = text;
    }

    public String get() {
        return this.text;
    }

    public WLabel color(Color color) {
        this.color = color;
        return this;
    }
}
