package meteordevelopment.meteorclient.gui.screens.settings;

import java.util.List;
import java.util.function.Predicate;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.utils.misc.Names;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2960;
import net.minecraft.class_7923;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/screens/settings/BlockListSettingScreen.class */
public class BlockListSettingScreen extends CollectionListSettingScreen<class_2248> {
    private static final class_2960 ID = class_2960.method_60655("minecraft", "");

    public BlockListSettingScreen(GuiTheme theme, Setting<List<class_2248>> setting) {
        super(theme, "Select Blocks", setting, setting.get(), class_7923.field_41175);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // meteordevelopment.meteorclient.gui.screens.settings.CollectionListSettingScreen
    public boolean includeValue(class_2248 value) {
        Predicate<class_2248> filter = ((BlockListSetting) this.setting).filter;
        if (filter == null) {
            return value != class_2246.field_10124;
        }
        return filter.test(value);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // meteordevelopment.meteorclient.gui.screens.settings.CollectionListSettingScreen
    public WWidget getValueWidget(class_2248 value) {
        return this.theme.itemWithLabel(value.method_8389().method_7854(), getValueName(value));
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // meteordevelopment.meteorclient.gui.screens.settings.CollectionListSettingScreen
    public String getValueName(class_2248 value) {
        return Names.get(value);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // meteordevelopment.meteorclient.gui.screens.settings.CollectionListSettingScreen
    public boolean skipValue(class_2248 value) {
        return class_7923.field_41175.method_10221(value).method_12832().endsWith("_wall_banner");
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // meteordevelopment.meteorclient.gui.screens.settings.CollectionListSettingScreen
    public class_2248 getAdditionalValue(class_2248 value) {
        String path = class_7923.field_41175.method_10221(value).method_12832();
        if (!path.endsWith("_banner")) {
            return null;
        }
        ID.setPath(path.substring(0, path.length() - 6) + "wall_banner");
        return (class_2248) class_7923.field_41175.method_63535(ID);
    }
}
