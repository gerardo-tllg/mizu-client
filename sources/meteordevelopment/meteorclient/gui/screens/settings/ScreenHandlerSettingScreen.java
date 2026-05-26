package meteordevelopment.meteorclient.gui.screens.settings;

import java.util.List;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.class_3917;
import net.minecraft.class_7923;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/screens/settings/ScreenHandlerSettingScreen.class */
public class ScreenHandlerSettingScreen extends CollectionListSettingScreen<class_3917<?>> {
    public ScreenHandlerSettingScreen(GuiTheme theme, Setting<List<class_3917<?>>> setting) {
        super(theme, "Select Screen Handlers", setting, setting.get(), class_7923.field_41187);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // meteordevelopment.meteorclient.gui.screens.settings.CollectionListSettingScreen
    public WWidget getValueWidget(class_3917<?> value) {
        return this.theme.label(getValueName(value));
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // meteordevelopment.meteorclient.gui.screens.settings.CollectionListSettingScreen
    public String getValueName(class_3917<?> type) {
        return class_7923.field_41187.method_10221(type).toString();
    }
}
