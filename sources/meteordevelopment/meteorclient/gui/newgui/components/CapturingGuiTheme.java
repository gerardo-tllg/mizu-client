package meteordevelopment.meteorclient.gui.newgui.components;

import meteordevelopment.meteorclient.gui.themes.meteor.MeteorGuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.settings.Settings;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/newgui/components/CapturingGuiTheme.class */
public class CapturingGuiTheme extends MeteorGuiTheme {
    public Settings captured;

    @Override // meteordevelopment.meteorclient.gui.GuiTheme
    public WWidget settings(Settings settings) {
        if (this.captured == null) {
            this.captured = settings;
        }
        return super.settings(settings);
    }

    @Override // meteordevelopment.meteorclient.gui.GuiTheme
    public WWidget settings(Settings settings, String filter) {
        if (this.captured == null) {
            this.captured = settings;
        }
        return super.settings(settings, filter);
    }
}
