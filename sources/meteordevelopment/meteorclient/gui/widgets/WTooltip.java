package meteordevelopment.meteorclient.gui.widgets;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/widgets/WTooltip.class */
public abstract class WTooltip extends WContainer implements WRoot {
    private boolean valid;
    protected String text;

    public WTooltip(String text) {
        this.text = text;
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    public void init() {
        add(this.theme.label(this.text)).pad(4.0d);
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    public void invalidate() {
        this.valid = false;
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.containers.WContainer, meteordevelopment.meteorclient.gui.widgets.WWidget
    public boolean render(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (!this.valid) {
            calculateSize();
            calculateWidgetPositions();
            this.valid = true;
        }
        return super.render(renderer, mouseX, mouseY, delta);
    }
}
