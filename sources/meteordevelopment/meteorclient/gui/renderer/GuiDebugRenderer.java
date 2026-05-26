package meteordevelopment.meteorclient.gui.renderer;

import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.renderer.MeshBuilder;
import meteordevelopment.meteorclient.renderer.MeshRenderer;
import meteordevelopment.meteorclient.renderer.MeteorRenderPipelines;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.class_310;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/renderer/GuiDebugRenderer.class */
public class GuiDebugRenderer {
    private static final Color CELL_COLOR = new Color(25, 225, 25);
    private static final Color WIDGET_COLOR = new Color(25, 25, 225);
    private final MeshBuilder mesh = new MeshBuilder(MeteorRenderPipelines.WORLD_COLORED_LINES);

    public void render(WWidget widget) {
        if (widget == null) {
            return;
        }
        this.mesh.begin();
        renderWidget(widget);
        this.mesh.end();
        MeshRenderer.begin().attachments(class_310.method_1551().method_1522()).pipeline(MeteorRenderPipelines.WORLD_COLORED_LINES).mesh(this.mesh).end();
    }

    private void renderWidget(WWidget widget) {
        lineBox(widget.x, widget.y, widget.width, widget.height, WIDGET_COLOR);
        if (widget instanceof WContainer) {
            for (Cell<?> cell : ((WContainer) widget).cells) {
                lineBox(cell.x, cell.y, cell.width, cell.height, CELL_COLOR);
                renderWidget(cell.widget());
            }
        }
    }

    private void lineBox(double x, double y, double width, double height, Color color) {
        line(x, y, x + width, y, color);
        line(x + width, y, x + width, y + height, color);
        line(x, y, x, y + height, color);
        line(x, y + height, x + width, y + height, color);
    }

    private void line(double x1, double y1, double x2, double y2, Color color) {
        this.mesh.ensureLineCapacity();
        this.mesh.line(this.mesh.vec3(x1, y1, 0.0d).color(color).next(), this.mesh.vec3(x2, y2, 0.0d).color(color).next());
    }
}
