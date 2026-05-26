package meteordevelopment.meteorclient.gui.screens.settings;

import java.util.Set;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.utils.misc.Names;
import net.minecraft.class_1887;
import net.minecraft.class_5321;
import net.minecraft.class_7924;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/screens/settings/EnchantmentListSettingScreen.class */
public class EnchantmentListSettingScreen extends DynamicRegistryListSettingScreen<class_1887> {
    public EnchantmentListSettingScreen(GuiTheme theme, Setting<Set<class_5321<class_1887>>> setting) {
        super(theme, "Select Enchantments", setting, setting.get(), class_7924.field_41265);
    }

    @Override // meteordevelopment.meteorclient.gui.screens.settings.DynamicRegistryListSettingScreen
    protected WWidget getValueWidget(class_5321<class_1887> value) {
        return this.theme.label(getValueName(value));
    }

    @Override // meteordevelopment.meteorclient.gui.screens.settings.DynamicRegistryListSettingScreen
    protected String getValueName(class_5321<class_1887> value) {
        return Names.get(value);
    }
}
