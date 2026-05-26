package meteordevelopment.meteorclient.gui.screens;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.Settings;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/screens/EditSystemScreen.class */
public abstract class EditSystemScreen<T> extends WindowScreen {
    private WContainer settingsContainer;
    protected final T value;
    protected final boolean isNew;
    private final Runnable reload;

    public abstract T create();

    public abstract boolean save();

    public abstract Settings getSettings();

    public EditSystemScreen(GuiTheme theme, T value, Runnable reload) {
        super(theme, value == null ? "New" : "Edit");
        this.isNew = value == null;
        this.value = this.isNew ? create() : value;
        this.reload = reload;
    }

    @Override // meteordevelopment.meteorclient.gui.WidgetScreen
    public void initWidgets() {
        this.settingsContainer = (WContainer) add(this.theme.verticalList()).expandX().minWidth(400.0d).widget();
        this.settingsContainer.add(this.theme.settings(getSettings())).expandX();
        add(this.theme.horizontalSeparator()).expandX();
        WButton done = (WButton) add(this.theme.button(this.isNew ? "Create" : "Save")).expandX().widget();
        done.action = () -> {
            if (save()) {
                method_25419();
            }
        };
        this.enterAction = done.action;
    }

    public void method_25393() {
        getSettings().tick(this.settingsContainer, this.theme);
    }

    @Override // meteordevelopment.meteorclient.gui.WidgetScreen
    protected void onClosed() {
        if (this.reload != null) {
            this.reload.run();
        }
    }
}
