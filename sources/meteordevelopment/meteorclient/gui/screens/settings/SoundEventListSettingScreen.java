package meteordevelopment.meteorclient.gui.screens.settings;

import java.util.List;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.class_3414;
import net.minecraft.class_7923;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/screens/settings/SoundEventListSettingScreen.class */
public class SoundEventListSettingScreen extends CollectionListSettingScreen<class_3414> {
    public SoundEventListSettingScreen(GuiTheme theme, Setting<List<class_3414>> setting) {
        super(theme, "Select Sounds", setting, setting.get(), class_7923.field_41172);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // meteordevelopment.meteorclient.gui.screens.settings.CollectionListSettingScreen
    public WWidget getValueWidget(class_3414 value) {
        return this.theme.label(getValueName(value));
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // meteordevelopment.meteorclient.gui.screens.settings.CollectionListSettingScreen
    public String getValueName(class_3414 value) {
        return value.comp_3319().method_12832();
    }
}
