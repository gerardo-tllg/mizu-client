package meteordevelopment.meteorclient.gui.widgets.pressable;

import meteordevelopment.meteorclient.gui.widgets.WWidget;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/widgets/pressable/WPressable.class */
public abstract class WPressable extends WWidget {
    public Runnable action;
    protected boolean pressed;

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    public boolean onMouseClicked(double mouseX, double mouseY, int button, boolean used) {
        if (this.mouseOver && ((button == 0 || button == 1) && !used)) {
            this.pressed = true;
        }
        return this.pressed;
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    public boolean onMouseReleased(double mouseX, double mouseY, int button) {
        if (this.pressed) {
            onPressed(button);
            if (this.action != null) {
                this.action.run();
            }
            this.pressed = false;
            return false;
        }
        return false;
    }

    protected void onPressed(int button) {
    }
}
