package meteordevelopment.meteorclient.gui.widgets.pressable;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/widgets/pressable/WPlus.class */
public abstract class WPlus extends WPressable {
    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    protected void onCalculateSize() {
        double pad = pad();
        double s = this.theme.textHeight();
        this.width = pad + s + pad;
        this.height = pad + s + pad;
    }
}
