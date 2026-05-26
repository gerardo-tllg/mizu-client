package meteordevelopment.meteorclient.gui.screens.settings;

import java.util.Set;
import java.util.function.Predicate;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.settings.PacketListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.utils.network.PacketUtils;
import net.minecraft.class_2596;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/screens/settings/PacketBoolSettingScreen.class */
public class PacketBoolSettingScreen extends CollectionListSettingScreen<Class<? extends class_2596<?>>> {
    public PacketBoolSettingScreen(GuiTheme theme, Setting<Set<Class<? extends class_2596<?>>>> setting) {
        super(theme, "Select Packets", setting, setting.get(), PacketUtils.PACKETS);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // meteordevelopment.meteorclient.gui.screens.settings.CollectionListSettingScreen
    public boolean includeValue(Class<? extends class_2596<?>> value) {
        Predicate<Class<? extends class_2596<?>>> filter = ((PacketListSetting) this.setting).filter;
        if (filter == null) {
            return true;
        }
        return filter.test(value);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // meteordevelopment.meteorclient.gui.screens.settings.CollectionListSettingScreen
    public WWidget getValueWidget(Class<? extends class_2596<?>> value) {
        return this.theme.label(getValueName(value));
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // meteordevelopment.meteorclient.gui.screens.settings.CollectionListSettingScreen
    public String getValueName(Class<? extends class_2596<?>> value) {
        return PacketUtils.getName(value);
    }
}
