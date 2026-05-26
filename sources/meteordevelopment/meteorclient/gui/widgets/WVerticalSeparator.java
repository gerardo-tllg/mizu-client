package meteordevelopment.meteorclient.gui.widgets;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/widgets/WVerticalSeparator.class */
public class WVerticalSeparator extends WWidget {
    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    protected void onCalculateSize() {
        this.width = this.theme.scale(3.0d);
        this.height = 1.0d;
    }
}
