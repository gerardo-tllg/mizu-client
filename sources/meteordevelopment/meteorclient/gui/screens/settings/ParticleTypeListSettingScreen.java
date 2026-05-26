package meteordevelopment.meteorclient.gui.screens.settings;

import java.util.List;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.utils.misc.Names;
import net.minecraft.class_2394;
import net.minecraft.class_2396;
import net.minecraft.class_7923;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/screens/settings/ParticleTypeListSettingScreen.class */
public class ParticleTypeListSettingScreen extends CollectionListSettingScreen<class_2396<?>> {
    public ParticleTypeListSettingScreen(GuiTheme theme, Setting<List<class_2396<?>>> setting) {
        super(theme, "Select Particles", setting, setting.get(), class_7923.field_41180);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // meteordevelopment.meteorclient.gui.screens.settings.CollectionListSettingScreen
    public WWidget getValueWidget(class_2396<?> value) {
        return this.theme.label(getValueName(value));
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // meteordevelopment.meteorclient.gui.screens.settings.CollectionListSettingScreen
    public String getValueName(class_2396<?> value) {
        return Names.get(value);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // meteordevelopment.meteorclient.gui.screens.settings.CollectionListSettingScreen
    public boolean skipValue(class_2396<?> value) {
        return !(value instanceof class_2394);
    }
}
