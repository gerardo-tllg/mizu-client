package meteordevelopment.meteorclient.gui.widgets;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import net.minecraft.class_1799;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/widgets/WItem.class */
public class WItem extends WWidget {
    protected class_1799 itemStack;

    public WItem(class_1799 itemStack) {
        this.itemStack = itemStack;
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    protected void onCalculateSize() {
        double s = this.theme.scale(32.0d);
        this.width = s;
        this.height = s;
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (!this.itemStack.method_7960()) {
            renderer.post(() -> {
                double s = this.theme.scale(2.0d);
                renderer.item(this.itemStack, (int) this.x, (int) this.y, (float) s, true);
            });
        }
    }

    public void set(class_1799 itemStack) {
        this.itemStack = itemStack;
    }
}
