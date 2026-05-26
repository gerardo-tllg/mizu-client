package meteordevelopment.meteorclient.gui.widgets.input;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WRoot;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable;
import net.minecraft.class_3532;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/widgets/input/WDropdown.class */
public abstract class WDropdown<T> extends WPressable {
    public Runnable action;
    protected T[] values;
    protected T value;
    protected double maxValueWidth;
    protected WDropdownRoot root;
    protected boolean expanded;
    protected double animProgress;

    protected abstract WDropdownRoot createRootWidget();

    protected abstract WDropdown<T>.WDropdownValue createValueWidget();

    public WDropdown(T[] values, T value) {
        this.values = values;
        set(value);
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    public void init() {
        this.root = createRootWidget();
        this.root.theme = this.theme;
        this.root.spacing = 0.0d;
        for (int i = 0; i < this.values.length; i++) {
            WDropdown<T>.WDropdownValue widget = createValueWidget();
            widget.theme = this.theme;
            widget.value = this.values[i];
            Cell<?> cell = this.root.add(widget).padHorizontal(2.0d).expandWidgetX();
            if (i >= this.values.length - 1) {
                cell.padBottom(2.0d);
            }
        }
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    protected void onCalculateSize() {
        double pad = pad();
        this.maxValueWidth = 0.0d;
        for (T value : this.values) {
            double valueWidth = this.theme.textWidth(value.toString());
            this.maxValueWidth = Math.max(this.maxValueWidth, valueWidth);
        }
        this.root.calculateSize();
        this.width = pad + this.maxValueWidth + pad + this.theme.textHeight() + pad;
        this.height = pad + this.theme.textHeight() + pad;
        this.root.width = this.width;
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    protected void onCalculateWidgetPositions() {
        super.onCalculateWidgetPositions();
        this.root.x = this.x;
        this.root.y = this.y + this.height;
        this.root.calculateWidgetPositions();
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.pressable.WPressable
    protected void onPressed(int button) {
        this.expanded = !this.expanded;
    }

    public T get() {
        return this.value;
    }

    public void set(T value) {
        this.value = value;
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    public void move(double deltaX, double deltaY) {
        super.move(deltaX, deltaY);
        this.root.move(deltaX, deltaY);
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    public boolean render(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        boolean render = super.render(renderer, mouseX, mouseY, delta);
        this.animProgress += ((double) (this.expanded ? 1 : -1)) * delta * 14.0d;
        this.animProgress = class_3532.method_15350(this.animProgress, 0.0d, 1.0d);
        if (!render && this.animProgress > 0.0d) {
            renderer.absolutePost(() -> {
                renderer.scissorStart(this.x, this.y + this.height, this.width, this.root.height * this.animProgress);
                this.root.render(renderer, mouseX, mouseY, delta);
                renderer.scissorEnd();
            });
        }
        if (this.expanded && this.root.mouseOver) {
            this.theme.disableHoverColor = true;
        }
        return render;
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.pressable.WPressable, meteordevelopment.meteorclient.gui.widgets.WWidget
    public boolean onMouseClicked(double mouseX, double mouseY, int button, boolean used) {
        if (!this.mouseOver && !this.root.mouseOver) {
            this.expanded = false;
        }
        if (super.onMouseClicked(mouseX, mouseY, button, used)) {
            used = true;
        }
        if (this.expanded && this.root.mouseClicked(mouseX, mouseY, button, used)) {
            used = true;
        }
        return used;
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.pressable.WPressable, meteordevelopment.meteorclient.gui.widgets.WWidget
    public boolean onMouseReleased(double mouseX, double mouseY, int button) {
        if (super.onMouseReleased(mouseX, mouseY, button)) {
            return true;
        }
        return this.expanded && this.root.mouseReleased(mouseX, mouseY, button);
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    public void onMouseMoved(double mouseX, double mouseY, double lastMouseX, double lastMouseY) {
        super.onMouseMoved(mouseX, mouseY, lastMouseX, lastMouseY);
        if (this.expanded) {
            this.root.mouseMoved(mouseX, mouseY, lastMouseX, lastMouseY);
        }
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    public boolean onMouseScrolled(double amount) {
        if (super.onMouseScrolled(amount)) {
            return true;
        }
        if (this.expanded) {
            return this.root.mouseScrolled(amount);
        }
        return false;
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    public boolean onKeyPressed(int key, int mods) {
        if (super.onKeyPressed(key, mods)) {
            return true;
        }
        return this.expanded && this.root.keyPressed(key, mods);
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    public boolean onKeyRepeated(int key, int mods) {
        if (super.onKeyRepeated(key, mods)) {
            return true;
        }
        return this.expanded && this.root.keyRepeated(key, mods);
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    public boolean onCharTyped(char c) {
        if (super.onCharTyped(c)) {
            return true;
        }
        return this.expanded && this.root.charTyped(c);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/widgets/input/WDropdown$WDropdownRoot.class */
    public static abstract class WDropdownRoot extends WVerticalList implements WRoot {
        protected WDropdownRoot() {
        }

        @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
        public void invalidate() {
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/widgets/input/WDropdown$WDropdownValue.class */
    public abstract class WDropdownValue extends WPressable {
        protected T value;

        protected WDropdownValue() {
        }

        @Override // meteordevelopment.meteorclient.gui.widgets.pressable.WPressable
        protected void onPressed(int button) {
            boolean isNew = !WDropdown.this.value.equals(this.value);
            WDropdown.this.value = this.value;
            WDropdown.this.expanded = false;
            if (!isNew || WDropdown.this.action == null) {
                return;
            }
            WDropdown.this.action.run();
        }
    }
}
