package meteordevelopment.meteorclient.gui.widgets.containers;

import java.util.function.Consumer;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.utils.WindowConfig;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.pressable.WTriangle;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.class_3532;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/widgets/containers/WWindow.class */
public abstract class WWindow extends WVerticalList {
    public Consumer<WContainer> beforeHeaderInit;
    public String id;
    public final WWidget icon;
    protected final String title;
    protected WHeader header;
    public WView view;
    protected boolean dragging;
    protected boolean dragged;
    protected double movedX;
    protected double movedY;
    private boolean propagateEventsExpanded;
    public double padding = 8.0d;
    protected boolean expanded = true;
    protected double animProgress = 1.0d;
    protected boolean moved = false;

    protected abstract WHeader header(WWidget wWidget);

    public WWindow(WWidget icon, String title) {
        this.icon = icon;
        this.title = title;
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    public void init() {
        this.header = header(this.icon);
        this.header.theme = this.theme;
        super.add(this.header).expandWidgetX().widget();
        this.view = (WView) super.add(this.theme.view()).expandX().pad(this.padding).widget();
        if (this.id != null) {
            this.expanded = this.theme.getWindowConfig(this.id).expanded;
            this.animProgress = this.expanded ? 1.0d : 0.0d;
        }
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.containers.WContainer
    public <T extends WWidget> Cell<T> add(T widget) {
        return this.view.add(widget);
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.containers.WContainer
    public void clear() {
        this.view.clear();
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
        if (this.id != null) {
            WindowConfig config = this.theme.getWindowConfig(this.id);
            config.expanded = expanded;
        }
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList, meteordevelopment.meteorclient.gui.widgets.containers.WContainer, meteordevelopment.meteorclient.gui.widgets.WWidget
    protected void onCalculateWidgetPositions() {
        if (this.id != null) {
            WindowConfig config = this.theme.getWindowConfig(this.id);
            if (config.x != -1.0d) {
                this.x = config.x;
                if (this.x + this.width > Utils.getWindowWidth()) {
                    this.x = ((double) Utils.getWindowWidth()) - this.width;
                }
            }
            if (config.y != -1.0d) {
                this.y = config.y;
                if (this.y + this.height > Utils.getWindowHeight()) {
                    this.y = ((double) Utils.getWindowHeight()) - this.height;
                }
            }
        }
        super.onCalculateWidgetPositions();
        if (this.moved) {
            move(this.movedX - this.x, this.movedY - this.y);
        }
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.containers.WContainer, meteordevelopment.meteorclient.gui.widgets.WWidget
    public boolean render(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (!this.visible) {
            return true;
        }
        boolean scissor = !(this.animProgress == 0.0d || this.animProgress == 1.0d) || (this.expanded && this.animProgress != 1.0d);
        if (scissor) {
            renderer.scissorStart(this.x, this.y, this.width, ((this.height - this.header.height) * this.animProgress) + this.header.height);
        }
        boolean toReturn = super.render(renderer, mouseX, mouseY, delta);
        if (scissor) {
            renderer.scissorEnd();
        }
        return toReturn;
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.containers.WContainer
    protected void renderWidget(WWidget widget, GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (this.expanded || this.animProgress > 0.0d || (widget instanceof WHeader)) {
            widget.render(renderer, mouseX, mouseY, delta);
        }
        this.propagateEventsExpanded = this.expanded;
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.containers.WContainer
    protected boolean propagateEvents(WWidget widget) {
        return (widget instanceof WHeader) || this.propagateEventsExpanded;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/widgets/containers/WWindow$WHeader.class */
    public abstract class WHeader extends WContainer {
        private final WWidget icon;
        private WTriangle triangle;
        private WHorizontalList list;

        public WHeader(WWidget icon) {
            this.icon = icon;
        }

        @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
        public void init() {
            if (this.icon != null) {
                createList();
                add(this.icon).centerY();
            }
            if (WWindow.this.beforeHeaderInit != null) {
                createList();
                WWindow.this.beforeHeaderInit.accept(this);
            }
            add(this.theme.label(WWindow.this.title, true)).expandCellX().center().pad(4.0d);
            this.triangle = (WTriangle) add(this.theme.triangle()).pad(4.0d).right().centerY().widget();
            this.triangle.action = () -> {
                WWindow.this.setExpanded(!WWindow.this.expanded);
            };
        }

        private void createList() {
            this.list = (WHorizontalList) add(this.theme.horizontalList()).expandX().widget();
            this.list.spacing = 0.0d;
        }

        @Override // meteordevelopment.meteorclient.gui.widgets.containers.WContainer
        public <T extends WWidget> Cell<T> add(T widget) {
            return this.list != null ? this.list.add(widget) : super.add(widget);
        }

        @Override // meteordevelopment.meteorclient.gui.widgets.containers.WContainer, meteordevelopment.meteorclient.gui.widgets.WWidget
        protected void onCalculateSize() {
            this.width = 0.0d;
            this.height = 0.0d;
            for (Cell<?> cell : this.cells) {
                double w = cell.padLeft() + cell.widget().width + cell.padRight();
                if (cell.widget() instanceof WTriangle) {
                    w *= 2.0d;
                }
                this.width += w;
                this.height = Math.max(this.height, cell.padTop() + cell.widget().height + cell.padBottom());
            }
        }

        @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
        public boolean onMouseClicked(double mouseX, double mouseY, int button, boolean used) {
            if (this.mouseOver && !used) {
                if (button != 1) {
                    WWindow.this.dragging = true;
                    WWindow.this.dragged = false;
                    return true;
                }
                WWindow.this.setExpanded(!WWindow.this.expanded);
                return true;
            }
            return false;
        }

        @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
        public boolean onMouseReleased(double mouseX, double mouseY, int button) {
            if (WWindow.this.dragging) {
                WWindow.this.dragging = false;
                if (!WWindow.this.dragged) {
                    WWindow.this.setExpanded(!WWindow.this.expanded);
                    return false;
                }
                return false;
            }
            return false;
        }

        @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
        public void onMouseMoved(double mouseX, double mouseY, double lastMouseX, double lastMouseY) {
            if (WWindow.this.dragging) {
                WWindow.this.move(mouseX - lastMouseX, mouseY - lastMouseY);
                WWindow.this.moved = true;
                WWindow.this.movedX = this.x;
                WWindow.this.movedY = this.y;
                if (WWindow.this.id != null) {
                    WindowConfig config = this.theme.getWindowConfig(WWindow.this.id);
                    config.x = this.x;
                    config.y = this.y;
                }
                WWindow.this.dragged = true;
            }
        }

        @Override // meteordevelopment.meteorclient.gui.widgets.containers.WContainer, meteordevelopment.meteorclient.gui.widgets.WWidget
        public boolean render(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            WWindow.this.animProgress += ((double) (WWindow.this.expanded ? 1 : -1)) * delta * 14.0d;
            WWindow.this.animProgress = class_3532.method_15350(WWindow.this.animProgress, 0.0d, 1.0d);
            this.triangle.rotation = (1.0d - WWindow.this.animProgress) * (-90.0d);
            return super.render(renderer, mouseX, mouseY, delta);
        }
    }
}
