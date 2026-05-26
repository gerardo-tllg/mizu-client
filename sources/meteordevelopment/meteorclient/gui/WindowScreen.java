package meteordevelopment.meteorclient.gui;

import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/WindowScreen.class */
public abstract class WindowScreen extends WidgetScreen {
    protected final WWindow window;

    public WindowScreen(GuiTheme theme, WWidget icon, String title) {
        super(theme, title);
        this.window = (WWindow) super.add(theme.window(icon, title)).center().widget();
        this.window.view.scrollOnlyWhenMouseOver = false;
    }

    public WindowScreen(GuiTheme theme, String title) {
        this(theme, null, title);
    }

    @Override // meteordevelopment.meteorclient.gui.WidgetScreen
    public <W extends WWidget> Cell<W> add(W widget) {
        return this.window.add(widget);
    }

    @Override // meteordevelopment.meteorclient.gui.WidgetScreen
    public void clear() {
        this.window.clear();
    }
}
