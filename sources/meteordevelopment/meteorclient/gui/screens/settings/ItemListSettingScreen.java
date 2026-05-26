package meteordevelopment.meteorclient.gui.screens.settings;

import java.util.function.Predicate;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.settings.ItemListSetting;
import meteordevelopment.meteorclient.utils.misc.Names;
import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_7923;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/screens/settings/ItemListSettingScreen.class */
public class ItemListSettingScreen extends CollectionListSettingScreen<class_1792> {
    public ItemListSettingScreen(GuiTheme theme, ItemListSetting setting) {
        super(theme, "Select Items", setting, setting.get(), class_7923.field_41178);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // meteordevelopment.meteorclient.gui.screens.settings.CollectionListSettingScreen
    public boolean includeValue(class_1792 value) {
        Predicate<class_1792> filter = ((ItemListSetting) this.setting).filter;
        return (filter == null || filter.test(value)) && value != class_1802.field_8162;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // meteordevelopment.meteorclient.gui.screens.settings.CollectionListSettingScreen
    public WWidget getValueWidget(class_1792 value) {
        return this.theme.itemWithLabel(value.method_7854());
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // meteordevelopment.meteorclient.gui.screens.settings.CollectionListSettingScreen
    public String getValueName(class_1792 value) {
        return Names.get(value);
    }
}
