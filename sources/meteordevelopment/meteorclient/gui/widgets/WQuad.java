package meteordevelopment.meteorclient.gui.widgets;

import meteordevelopment.meteorclient.utils.render.color.Color;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/widgets/WQuad.class */
public abstract class WQuad extends WWidget {
    public Color color;

    public WQuad(Color color) {
        this.color = color;
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    protected void onCalculateSize() {
        double s = this.theme.scale(32.0d);
        this.width = s;
        this.height = s;
    }
}
