package meteordevelopment.meteorclient.gui.widgets.containers;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import net.minecraft.class_3532;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/widgets/containers/WSection.class */
public abstract class WSection extends WVerticalList {
    public Runnable action;
    protected String title;
    protected boolean expanded;
    protected double animProgress;
    private WHeader header;
    protected final WWidget headerWidget;
    private double actualWidth;
    private double actualHeight;
    private double forcedHeight = -1.0d;
    private boolean firstTime = true;

    protected abstract WHeader createHeader();

    public WSection(String title, boolean expanded, WWidget headerWidget) {
        this.title = title;
        this.expanded = expanded;
        this.headerWidget = headerWidget;
        this.animProgress = expanded ? 1.0d : 0.0d;
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    public void init() {
        this.header = createHeader();
        this.header.theme = this.theme;
        super.add(this.header).expandX();
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.containers.WContainer
    public <T extends WWidget> Cell<T> add(T widget) {
        return super.add(widget).padHorizontal(6.0d);
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public boolean isExpanded() {
        return this.expanded;
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList, meteordevelopment.meteorclient.gui.widgets.containers.WContainer, meteordevelopment.meteorclient.gui.widgets.WWidget
    protected void onCalculateSize() {
        if (this.forcedHeight == -1.0d) {
            super.onCalculateSize();
            this.actualWidth = this.width;
            this.actualHeight = this.height;
        } else {
            this.width = this.actualWidth;
            this.height = this.forcedHeight;
            if (this.animProgress == 1.0d) {
                this.forcedHeight = -1.0d;
            }
        }
        if (this.firstTime) {
            this.firstTime = false;
            this.forcedHeight = ((this.actualHeight - this.header.height) * this.animProgress) + this.header.height;
            onCalculateSize();
        }
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.containers.WContainer, meteordevelopment.meteorclient.gui.widgets.WWidget
    public boolean render(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (!this.visible) {
            return true;
        }
        double preProgress = this.animProgress;
        this.animProgress += ((double) (this.expanded ? 1 : -1)) * delta * 14.0d;
        this.animProgress = class_3532.method_15350(this.animProgress, 0.0d, 1.0d);
        if (this.animProgress != preProgress) {
            this.forcedHeight = ((this.actualHeight - this.header.height) * this.animProgress) + this.header.height;
            invalidate();
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
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.containers.WContainer
    protected boolean propagateEvents(WWidget widget) {
        return this.expanded || (widget instanceof WHeader);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/widgets/containers/WSection$WHeader.class */
    public abstract class WHeader extends WHorizontalList {
        protected String title;

        public WHeader(String title) {
            this.title = title;
        }

        @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
        public boolean onMouseClicked(double mouseX, double mouseY, int button, boolean used) {
            if (this.mouseOver && button == 0 && !used) {
                onClick();
                return true;
            }
            return false;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        public void onClick() {
            WSection.this.setExpanded(!WSection.this.expanded);
            if (WSection.this.action != null) {
                WSection.this.action.run();
            }
        }
    }
}
