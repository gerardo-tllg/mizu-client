package meteordevelopment.meteorclient.gui.renderer;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderOperation;
import meteordevelopment.meteorclient.utils.misc.Pool;
import meteordevelopment.meteorclient.utils.render.color.Color;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/renderer/GuiRenderOperation.class */
public abstract class GuiRenderOperation<T extends GuiRenderOperation<T>> {
    protected double x;
    protected double y;
    protected Color color;

    protected abstract void onRun();

    public void set(double x, double y, Color color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public void run(Pool<T> pool) {
        onRun();
        pool.free(this);
    }
}
