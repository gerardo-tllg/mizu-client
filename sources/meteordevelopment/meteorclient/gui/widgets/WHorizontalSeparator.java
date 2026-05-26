package meteordevelopment.meteorclient.gui.widgets;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/widgets/WHorizontalSeparator.class */
public abstract class WHorizontalSeparator extends WWidget {
    protected String text;
    protected double textWidth;

    public WHorizontalSeparator(String text) {
        this.text = text;
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    protected void onCalculateSize() {
        if (this.text != null) {
            this.textWidth = this.theme.textWidth(this.text);
        }
        this.width = 1.0d;
        this.height = this.text != null ? this.theme.textHeight() : this.theme.scale(3.0d);
    }
}
