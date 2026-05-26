package meteordevelopment.meteorclient.gui.widgets;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.utils.BaseWidget;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/widgets/WWidget.class */
public abstract class WWidget implements BaseWidget {
    public boolean visible = true;
    public GuiTheme theme;
    public double x;
    public double y;
    public double width;
    public double height;
    public double minWidth;
    public WWidget parent;
    public String tooltip;
    public boolean mouseOver;
    protected double mouseOverTimer;

    public void init() {
    }

    public void move(double deltaX, double deltaY) {
        this.x = Math.round(this.x + deltaX);
        this.y = Math.round(this.y + deltaY);
    }

    @Override // meteordevelopment.meteorclient.gui.utils.BaseWidget
    public GuiTheme getTheme() {
        return this.theme;
    }

    public double pad() {
        return this.theme.pad();
    }

    public void calculateSize() {
        onCalculateSize();
        double minWidth = this.theme.scale(this.minWidth);
        if (this.width < minWidth) {
            this.width = minWidth;
        }
        this.width = Math.round(this.width);
        this.height = Math.round(this.height);
    }

    protected void onCalculateSize() {
    }

    public void calculateWidgetPositions() {
        this.x = Math.round(this.x);
        this.y = Math.round(this.y);
        onCalculateWidgetPositions();
    }

    protected void onCalculateWidgetPositions() {
    }

    public boolean render(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (!this.visible) {
            return true;
        }
        if (isOver(mouseX, mouseY)) {
            this.mouseOverTimer += delta;
            if (this.mouseOverTimer >= 1.0d && this.tooltip != null) {
                renderer.tooltip(this.tooltip);
            }
        } else {
            this.mouseOverTimer = 0.0d;
        }
        onRender(renderer, mouseX, mouseY, delta);
        return false;
    }

    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, boolean used) {
        return onMouseClicked(mouseX, mouseY, button, used);
    }

    public boolean onMouseClicked(double mouseX, double mouseY, int button, boolean used) {
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return onMouseReleased(mouseX, mouseY, button);
    }

    public boolean onMouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    public void mouseMoved(double mouseX, double mouseY, double lastMouseX, double lastMouseY) {
        this.mouseOver = isOver(mouseX, mouseY);
        onMouseMoved(mouseX, mouseY, lastMouseX, lastMouseY);
    }

    public void onMouseMoved(double mouseX, double mouseY, double lastMouseX, double lastMouseY) {
    }

    public boolean mouseScrolled(double amount) {
        return onMouseScrolled(amount);
    }

    public boolean onMouseScrolled(double amount) {
        return false;
    }

    public boolean keyPressed(int key, int mods) {
        return onKeyPressed(key, mods);
    }

    public boolean onKeyPressed(int key, int mods) {
        return false;
    }

    public boolean keyRepeated(int key, int mods) {
        return onKeyRepeated(key, mods);
    }

    public boolean onKeyRepeated(int key, int mods) {
        return false;
    }

    public boolean charTyped(char c) {
        return onCharTyped(c);
    }

    public boolean onCharTyped(char c) {
        return false;
    }

    public void invalidate() {
        WWidget root = getRoot();
        if (root != null) {
            root.invalidate();
        }
    }

    protected WWidget getRoot() {
        if (this.parent != null) {
            return this.parent.getRoot();
        }
        if (this instanceof WRoot) {
            return this;
        }
        return null;
    }

    public boolean isOver(double x, double y) {
        return x >= this.x && x <= this.x + this.width && y >= this.y && y <= this.y + this.height;
    }
}
