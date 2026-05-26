package meteordevelopment.meteorclient.gui.widgets.pressable;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/widgets/pressable/WTriangle.class */
public abstract class WTriangle extends WPressable {
    public double rotation;

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    protected void onCalculateSize() {
        double s = this.theme.textHeight();
        this.width = s;
        this.height = s;
    }
}
