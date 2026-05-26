package meteordevelopment.meteorclient.gui.tabs;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiTheme;
import net.minecraft.class_437;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/tabs/Tab.class */
public abstract class Tab {
    public final String name;

    public abstract TabScreen createScreen(GuiTheme guiTheme);

    public abstract boolean isScreen(class_437 class_437Var);

    public Tab(String name) {
        this.name = name;
    }

    public void openScreen(GuiTheme theme) {
        TabScreen screen = createScreen(theme);
        screen.addDirect(theme.topBar()).top().centerX();
        MeteorClient.mc.method_1507(screen);
    }
}
