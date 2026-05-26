package meteordevelopment.meteorclient.gui.widgets.pressable;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/widgets/pressable/WCheckbox.class */
public abstract class WCheckbox extends WPressable {
    public boolean checked;

    public WCheckbox(boolean checked) {
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
}
