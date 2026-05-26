package meteordevelopment.meteorclient.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.renderer.GuiDebugRenderer;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WRoot;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.CursorStyle;
import meteordevelopment.meteorclient.utils.misc.input.Input;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_437;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/WidgetScreen.class */
public abstract class WidgetScreen extends class_437 {
    private static final GuiRenderer RENDERER = new GuiRenderer();
    private static final GuiDebugRenderer DEBUG_RENDERER = new GuiDebugRenderer();
    public Runnable taskAfterRender;
    protected Runnable enterAction;
    public class_437 parent;
    private final WContainer root;
    protected final GuiTheme theme;
    public boolean locked;
    public boolean lockedAllowClose;
    private boolean closed;
    private boolean onClose;
    private boolean debug;
    private double lastMouseX;
    private double lastMouseY;
    public double animProgress;
    private List<Runnable> onClosed;
    protected boolean firstInit;

    public abstract void initWidgets();

    public WidgetScreen(GuiTheme theme, String title) {
        super(class_2561.method_43470(title));
        this.firstInit = true;
        this.parent = MeteorClient.mc.field_1755;
        this.root = new WFullScreenRoot();
        this.theme = theme;
        this.root.theme = theme;
        if (this.parent != null) {
            this.animProgress = 1.0d;
            if ((this instanceof TabScreen) && (this.parent instanceof TabScreen)) {
                this.parent = ((TabScreen) this.parent).parent;
            }
        }
    }

    public <W extends WWidget> Cell<W> add(W widget) {
        return this.root.add(widget);
    }

    public void clear() {
        this.root.clear();
    }

    public void invalidate() {
        this.root.invalidate();
    }

    protected void method_25426() {
        MeteorClient.EVENT_BUS.subscribe(this);
        this.closed = false;
        if (this.firstInit) {
            this.firstInit = false;
            initWidgets();
        }
    }

    public void reload() {
        clear();
        initWidgets();
    }

    public void onClosed(Runnable action) {
        if (this.onClosed == null) {
            this.onClosed = new ArrayList(2);
        }
        this.onClosed.add(action);
    }

    public boolean method_25402(double mouseX, double mouseY, int button) {
        if (this.locked) {
            return false;
        }
        double s = MeteorClient.mc.method_22683().method_4495();
        return this.root.mouseClicked(mouseX * s, mouseY * s, button, false);
    }

    public boolean method_25406(double mouseX, double mouseY, int button) {
        if (this.locked) {
            return false;
        }
        double s = MeteorClient.mc.method_22683().method_4495();
        return this.root.mouseReleased(mouseX * s, mouseY * s, button);
    }

    public void method_16014(double mouseX, double mouseY) {
        if (this.locked) {
            return;
        }
        double s = MeteorClient.mc.method_22683().method_4495();
        double mouseX2 = mouseX * s;
        double mouseY2 = mouseY * s;
        this.root.mouseMoved(mouseX2, mouseY2, this.lastMouseX, this.lastMouseY);
        this.lastMouseX = mouseX2;
        this.lastMouseY = mouseY2;
    }

    public boolean method_25401(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.locked) {
            return false;
        }
        this.root.mouseScrolled(verticalAmount);
        return super.method_25401(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    public boolean method_16803(int keyCode, int scanCode, int modifiers) {
        if (this.locked) {
            return false;
        }
        if ((modifiers == 2 || modifiers == 8) && keyCode == 57) {
            this.debug = !this.debug;
            return true;
        }
        if ((keyCode == 257 || keyCode == 335) && this.enterAction != null) {
            this.enterAction.run();
            return true;
        }
        return super.method_16803(keyCode, scanCode, modifiers);
    }

    public boolean method_25404(int keyCode, int scanCode, int modifiers) {
        if (this.locked) {
            return false;
        }
        boolean shouldReturn = this.root.keyPressed(keyCode, modifiers) || super.method_25404(keyCode, scanCode, modifiers);
        if (shouldReturn) {
            return true;
        }
        if (keyCode == 258) {
            AtomicReference<WTextBox> firstTextBox = new AtomicReference<>(null);
            AtomicBoolean done = new AtomicBoolean(false);
            AtomicBoolean foundFocused = new AtomicBoolean(false);
            loopWidgets(this.root, wWidget -> {
                if (done.get() || !(wWidget instanceof WTextBox)) {
                    return;
                }
                WTextBox textBox = (WTextBox) wWidget;
                if (foundFocused.get()) {
                    textBox.setFocused(true);
                    textBox.setCursorMax();
                    done.set(true);
                } else if (textBox.isFocused()) {
                    textBox.setFocused(false);
                    foundFocused.set(true);
                }
                if (firstTextBox.get() == null) {
                    firstTextBox.set(textBox);
                }
            });
            if (!done.get() && firstTextBox.get() != null) {
                firstTextBox.get().setFocused(true);
                firstTextBox.get().setCursorMax();
                return true;
            }
            return true;
        }
        boolean control = class_310.field_1703 ? modifiers == 8 : modifiers == 2;
        return (control && keyCode == 67 && toClipboard()) || (control && keyCode == 86 && fromClipboard());
    }

    public void keyRepeated(int key, int modifiers) {
        if (this.locked) {
            return;
        }
        this.root.keyRepeated(key, modifiers);
    }

    public boolean method_25400(char chr, int keyCode) {
        if (this.locked) {
            return false;
        }
        return this.root.charTyped(chr);
    }

    public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
        if (!Utils.canUpdate()) {
            method_25420(context, mouseX, mouseY, delta);
        }
        double s = MeteorClient.mc.method_22683().method_4495();
        int mouseX2 = (int) (((double) mouseX) * s);
        int mouseY2 = (int) (((double) mouseY) * s);
        this.animProgress += (double) ((delta / 20.0f) * 14.0f);
        this.animProgress = class_3532.method_15350(this.animProgress, 0.0d, 1.0d);
        GuiKeyEvents.canUseKeys = true;
        context.method_51452();
        Utils.unscaledProjection();
        onRenderBefore(context, delta);
        RENDERER.theme = this.theme;
        this.theme.beforeRender();
        RENDERER.begin(context);
        RENDERER.setAlpha(this.animProgress);
        this.root.render(RENDERER, mouseX2, mouseY2, delta / 20.0f);
        RENDERER.setAlpha(1.0d);
        RENDERER.end();
        boolean tooltip = RENDERER.renderTooltip(context, mouseX2, mouseY2, delta / 20.0f);
        if (this.debug) {
            DEBUG_RENDERER.render(this.root);
            if (tooltip) {
                DEBUG_RENDERER.render(RENDERER.tooltipWidget);
            }
        }
        context.method_51452();
        Utils.scaledProjection();
        runAfterRenderTasks();
    }

    protected void runAfterRenderTasks() {
        if (this.taskAfterRender != null) {
            this.taskAfterRender.run();
            this.taskAfterRender = null;
        }
    }

    protected void onRenderBefore(class_332 drawContext, float delta) {
    }

    public void method_25410(class_310 client, int width, int height) {
        super.method_25410(client, width, height);
        this.root.invalidate();
    }

    public void method_25419() {
        if (!this.locked || this.lockedAllowClose) {
            boolean preOnClose = this.onClose;
            this.onClose = true;
            method_25432();
            this.onClose = preOnClose;
        }
    }

    public void method_25432() {
        if (!this.closed || this.lockedAllowClose) {
            this.closed = true;
            onClosed();
            Input.setCursorStyle(CursorStyle.Default);
            loopWidgets(this.root, widget -> {
                if (widget instanceof WTextBox) {
                    WTextBox textBox = (WTextBox) widget;
                    if (textBox.isFocused()) {
                        textBox.setFocused(false);
                    }
                }
            });
            MeteorClient.EVENT_BUS.unsubscribe(this);
            GuiKeyEvents.canUseKeys = true;
            if (this.onClosed != null) {
                for (Runnable action : this.onClosed) {
                    action.run();
                }
            }
            if (this.onClose) {
                this.taskAfterRender = () -> {
                    this.locked = true;
                    MeteorClient.mc.method_1507(this.parent);
                };
            }
        }
    }

    private void loopWidgets(WWidget widget, Consumer<WWidget> action) {
        action.accept(widget);
        if (widget instanceof WContainer) {
            for (Cell<?> cell : ((WContainer) widget).cells) {
                loopWidgets(cell.widget(), action);
            }
        }
    }

    protected void onClosed() {
    }

    public boolean toClipboard() {
        return false;
    }

    public boolean fromClipboard() {
        return false;
    }

    public boolean method_25422() {
        return !this.locked || this.lockedAllowClose;
    }

    public boolean method_25421() {
        return false;
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/WidgetScreen$WFullScreenRoot.class */
    private static class WFullScreenRoot extends WContainer implements WRoot {
        private boolean valid;

        private WFullScreenRoot() {
        }

        @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
        public void invalidate() {
            this.valid = false;
        }

        @Override // meteordevelopment.meteorclient.gui.widgets.containers.WContainer, meteordevelopment.meteorclient.gui.widgets.WWidget
        protected void onCalculateSize() {
            this.width = Utils.getWindowWidth();
            this.height = Utils.getWindowHeight();
        }

        @Override // meteordevelopment.meteorclient.gui.widgets.containers.WContainer, meteordevelopment.meteorclient.gui.widgets.WWidget
        protected void onCalculateWidgetPositions() {
            for (Cell<?> cell : this.cells) {
                cell.x = 0.0d;
                cell.y = 0.0d;
                cell.width = this.width;
                cell.height = this.height;
                cell.alignWidget();
            }
        }

        @Override // meteordevelopment.meteorclient.gui.widgets.containers.WContainer, meteordevelopment.meteorclient.gui.widgets.WWidget
        public boolean render(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            if (!this.valid) {
                calculateSize();
                calculateWidgetPositions();
                this.valid = true;
                mouseMoved(MeteorClient.mc.field_1729.method_1603(), MeteorClient.mc.field_1729.method_1604(), MeteorClient.mc.field_1729.method_1603(), MeteorClient.mc.field_1729.method_1604());
            }
            return super.render(renderer, mouseX, mouseY, delta);
        }
    }
}
