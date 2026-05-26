package meteordevelopment.meteorclient.gui.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.utils.Utils;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/renderer/Scissor.class */
public class Scissor {
    public int x;
    public int y;
    public int width;
    public int height;
    public final List<Runnable> postTasks = new ArrayList();

    public Scissor set(double x, double y, double width, double height) {
        if (width < 0.0d) {
            width = 0.0d;
        }
        if (height < 0.0d) {
            height = 0.0d;
        }
        this.x = (int) Math.round(x);
        this.y = (int) Math.round(y);
        this.width = (int) Math.round(width);
        this.height = (int) Math.round(height);
        this.postTasks.clear();
        return this;
    }

    public void push() {
        RenderSystem.getDevice().meteor$pushScissor(this.x, (Utils.getWindowHeight() - this.y) - this.height, this.width, this.height);
    }

    public void pop() {
        RenderSystem.getDevice().meteor$popScissor();
    }
}
