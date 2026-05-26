package meteordevelopment.meteorclient.gui.widgets.containers;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.class_312;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/widgets/containers/WContainer.class */
public abstract class WContainer extends WWidget {
    public final List<Cell<?>> cells = new ArrayList();

    public <T extends WWidget> Cell<T> add(T widget) {
        widget.parent = this;
        widget.theme = this.theme;
        Cell<T> cell = new Cell(widget).centerY();
        this.cells.add(cell);
        widget.init();
        invalidate();
        return cell;
    }

    public void clear() {
        if (!this.cells.isEmpty()) {
            this.cells.clear();
            invalidate();
        }
    }

    public void remove(Cell<?> cell) {
        if (this.cells.remove(cell)) {
            invalidate();
        }
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    public void move(double deltaX, double deltaY) {
        super.move(deltaX, deltaY);
        for (Cell<?> cell : this.cells) {
            cell.move(deltaX, deltaY);
        }
    }

    public void moveCells(double deltaX, double deltaY) {
        for (Cell<?> cell : this.cells) {
            cell.move(deltaX, deltaY);
            class_312 mouse = MeteorClient.mc.field_1729;
            cell.widget().mouseMoved(mouse.method_1603(), mouse.method_1604(), mouse.method_1603(), mouse.method_1604());
        }
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    public void calculateSize() {
        for (Cell<?> cell : this.cells) {
            cell.widget().calculateSize();
        }
        super.calculateSize();
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    protected void onCalculateSize() {
        this.width = 0.0d;
        this.height = 0.0d;
        for (Cell<?> cell : this.cells) {
            this.width = Math.max(this.width, cell.padLeft() + cell.widget().width + cell.padRight());
            this.height = Math.max(this.height, cell.padTop() + cell.widget().height + cell.padBottom());
        }
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    public void calculateWidgetPositions() {
        super.calculateWidgetPositions();
        for (Cell<?> cell : this.cells) {
            cell.widget().calculateWidgetPositions();
        }
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    protected void onCalculateWidgetPositions() {
        for (Cell<?> cell : this.cells) {
            cell.x = this.x + cell.padLeft();
            cell.y = this.y + cell.padTop();
            cell.width = (this.width - cell.padLeft()) - cell.padRight();
            cell.height = (this.height - cell.padTop()) - cell.padBottom();
            cell.alignWidget();
        }
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    public boolean render(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (super.render(renderer, mouseX, mouseY, delta)) {
            return true;
        }
        for (Cell<?> cell : this.cells) {
            double y = cell.widget().y;
            if (y > Utils.getWindowHeight()) {
                return false;
            }
            if (y + cell.widget().height > 0.0d) {
                renderWidget(cell.widget(), renderer, mouseX, mouseY, delta);
            }
        }
        return false;
    }

    protected void renderWidget(WWidget widget, GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        widget.render(renderer, mouseX, mouseY, delta);
    }

    protected boolean propagateEvents(WWidget widget) {
        return true;
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    public boolean mouseClicked(double mouseX, double mouseY, int button, boolean used) {
        try {
            for (Cell<?> cell : this.cells) {
                if (propagateEvents(cell.widget()) && cell.widget().mouseClicked(mouseX, mouseY, button, used)) {
                    used = true;
                }
            }
        } catch (ConcurrentModificationException e) {
        }
        return super.mouseClicked(mouseX, mouseY, button, used) || used;
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        try {
            for (Cell<?> cell : this.cells) {
                if (propagateEvents(cell.widget()) && cell.widget().mouseReleased(mouseX, mouseY, button)) {
                    return true;
                }
            }
        } catch (ConcurrentModificationException e) {
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    public void mouseMoved(double mouseX, double mouseY, double lastMouseX, double lastMouseY) {
        try {
            for (Cell<?> cell : this.cells) {
                if (propagateEvents(cell.widget())) {
                    cell.widget().mouseMoved(mouseX, mouseY, lastMouseX, lastMouseY);
                }
            }
        } catch (ConcurrentModificationException e) {
        }
        super.mouseMoved(mouseX, mouseY, lastMouseX, lastMouseY);
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    public boolean mouseScrolled(double amount) {
        try {
            for (Cell<?> cell : this.cells) {
                if (propagateEvents(cell.widget()) && cell.widget().mouseScrolled(amount)) {
                    return true;
                }
            }
        } catch (ConcurrentModificationException e) {
        }
        return super.mouseScrolled(amount);
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    public boolean keyPressed(int key, int modifiers) {
        try {
            for (Cell<?> cell : this.cells) {
                if (propagateEvents(cell.widget()) && cell.widget().keyPressed(key, modifiers)) {
                    return true;
                }
            }
        } catch (ConcurrentModificationException e) {
        }
        return onKeyPressed(key, modifiers);
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    public boolean keyRepeated(int key, int modifiers) {
        try {
            for (Cell<?> cell : this.cells) {
                if (propagateEvents(cell.widget()) && cell.widget().keyRepeated(key, modifiers)) {
                    return true;
                }
            }
        } catch (ConcurrentModificationException e) {
        }
        return onKeyRepeated(key, modifiers);
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    public boolean charTyped(char c) {
        try {
            for (Cell<?> cell : this.cells) {
                if (propagateEvents(cell.widget()) && cell.widget().charTyped(c)) {
                    return true;
                }
            }
        } catch (ConcurrentModificationException e) {
        }
        return super.charTyped(c);
    }
}
