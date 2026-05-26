package meteordevelopment.meteorclient.gui.widgets.containers;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.class_3532;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/widgets/containers/WView.class */
public abstract class WView extends WVerticalList {
    public double maxHeight = Double.MAX_VALUE;
    public boolean scrollOnlyWhenMouseOver = true;
    public boolean hasScrollBar = true;
    protected boolean canScroll;
    private double actualHeight;
    private double scroll;
    private double targetScroll;
    private boolean moveAfterPositionWidgets;
    protected boolean handleMouseOver;
    protected boolean handlePressed;

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    public void init() {
        this.maxHeight = ((double) Utils.getWindowHeight()) - this.theme.scale(128.0d);
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList, meteordevelopment.meteorclient.gui.widgets.containers.WContainer, meteordevelopment.meteorclient.gui.widgets.WWidget
    protected void onCalculateSize() {
        boolean couldScroll = this.canScroll;
        this.canScroll = false;
        this.widthRemove = 0.0d;
        super.onCalculateSize();
        if (this.height > this.maxHeight) {
            this.actualHeight = this.height;
            this.height = this.maxHeight;
            this.canScroll = true;
            if (this.hasScrollBar) {
                this.widthRemove = handleWidth() * 2.0d;
                this.width += this.widthRemove;
            }
            if (couldScroll) {
                this.moveAfterPositionWidgets = true;
                return;
            }
            return;
        }
        this.actualHeight = this.height;
        this.scroll = 0.0d;
        this.targetScroll = 0.0d;
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList, meteordevelopment.meteorclient.gui.widgets.containers.WContainer, meteordevelopment.meteorclient.gui.widgets.WWidget
    protected void onCalculateWidgetPositions() {
        super.onCalculateWidgetPositions();
        if (this.moveAfterPositionWidgets) {
            this.scroll = class_3532.method_15350(this.scroll, 0.0d, this.actualHeight - this.height);
            this.targetScroll = this.scroll;
            moveCells(0.0d, -this.scroll);
            this.moveAfterPositionWidgets = false;
        }
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    public boolean onMouseClicked(double mouseX, double mouseY, int button, boolean used) {
        if (this.handleMouseOver && button == 0 && !used) {
            this.handlePressed = true;
            return true;
        }
        return false;
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    public boolean onMouseReleased(double mouseX, double mouseY, int button) {
        if (this.handlePressed) {
            this.handlePressed = false;
            return false;
        }
        return false;
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    public void onMouseMoved(double mouseX, double mouseY, double lastMouseX, double lastMouseY) {
        this.handleMouseOver = false;
        if (this.canScroll && this.hasScrollBar) {
            double x = handleX();
            double y = handleY();
            if (mouseX >= x && mouseX <= x + handleWidth() && mouseY >= y && mouseY <= y + handleHeight()) {
                this.handleMouseOver = true;
            }
        }
        if (this.handlePressed) {
            double preScroll = this.scroll;
            double mouseDelta = mouseY - lastMouseY;
            this.scroll += Math.round(mouseDelta * ((this.actualHeight - (handleHeight() / 2.0d)) / this.height));
            this.scroll = class_3532.method_15350(this.scroll, 0.0d, this.actualHeight - this.height);
            this.targetScroll = this.scroll;
            double delta = this.scroll - preScroll;
            if (delta != 0.0d) {
                moveCells(0.0d, -delta);
            }
        }
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    public boolean onMouseScrolled(double amount) {
        if (!this.scrollOnlyWhenMouseOver || this.mouseOver) {
            this.targetScroll -= Math.round(this.theme.scale(amount * 40.0d));
            this.targetScroll = class_3532.method_15350(this.targetScroll, 0.0d, this.actualHeight - this.height);
            return true;
        }
        return false;
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.containers.WContainer, meteordevelopment.meteorclient.gui.widgets.WWidget
    public boolean render(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        updateScroll(delta);
        if (this.canScroll) {
            renderer.scissorStart(this.x, this.y, this.width, this.height);
        }
        boolean render = super.render(renderer, mouseX, mouseY, delta);
        if (this.canScroll) {
            renderer.scissorEnd();
        }
        return render;
    }

    private void updateScroll(double delta) {
        double preScroll = this.scroll;
        double max = this.actualHeight - this.height;
        if (Math.abs(this.targetScroll - this.scroll) < 1.0d) {
            this.scroll = this.targetScroll;
        } else if (this.targetScroll > this.scroll) {
            this.scroll += Math.round(this.theme.scale((delta * 300.0d) + (delta * 100.0d * (Math.abs(this.targetScroll - this.scroll) / 10.0d))));
            if (this.scroll > this.targetScroll) {
                this.scroll = this.targetScroll;
            }
        } else if (this.targetScroll < this.scroll) {
            this.scroll -= Math.round(this.theme.scale((delta * 300.0d) + ((delta * 100.0d) * (Math.abs(this.targetScroll - this.scroll) / 10.0d))));
            if (this.scroll < this.targetScroll) {
                this.scroll = this.targetScroll;
            }
        }
        this.scroll = class_3532.method_15350(this.scroll, 0.0d, max);
        double change = this.scroll - preScroll;
        if (change != 0.0d) {
            moveCells(0.0d, -change);
        }
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.containers.WContainer
    protected boolean propagateEvents(WWidget widget) {
        return (widget.y >= this.y && widget.y <= this.y + this.height) || (widget.y + widget.height >= this.y && widget.y + widget.height <= this.y + this.height) || ((this.y >= widget.y && this.y <= widget.y + widget.height) || (this.y + this.height >= widget.y && this.y + this.height <= widget.y + widget.height));
    }

    protected double handleWidth() {
        return this.theme.scale(6.0d);
    }

    protected double handleHeight() {
        return (this.height / this.actualHeight) * this.height;
    }

    protected double handleX() {
        return (this.x + this.width) - handleWidth();
    }

    protected double handleY() {
        return this.y + ((this.height - handleHeight()) * (this.scroll / (this.actualHeight - this.height)));
    }
}
