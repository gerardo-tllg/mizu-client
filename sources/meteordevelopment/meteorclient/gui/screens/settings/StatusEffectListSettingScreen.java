package meteordevelopment.meteorclient.gui.screens.settings;

import java.util.List;
import java.util.Optional;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.utils.misc.Names;
import net.minecraft.class_1291;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1844;
import net.minecraft.class_7923;
import net.minecraft.class_9334;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/screens/settings/StatusEffectListSettingScreen.class */
public class StatusEffectListSettingScreen extends CollectionListSettingScreen<class_1291> {
    public StatusEffectListSettingScreen(GuiTheme theme, Setting<List<class_1291>> setting) {
        super(theme, "Select Effects", setting, setting.get(), class_7923.field_41174);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // meteordevelopment.meteorclient.gui.screens.settings.CollectionListSettingScreen
    public WWidget getValueWidget(class_1291 value) {
        return this.theme.itemWithLabel(getPotionStack(value), getValueName(value));
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // meteordevelopment.meteorclient.gui.screens.settings.CollectionListSettingScreen
    public String getValueName(class_1291 value) {
        return Names.get(value);
    }

    private class_1799 getPotionStack(class_1291 effect) {
        class_1799 potion = class_1802.field_8574.method_7854();
        potion.method_57379(class_9334.field_49651, new class_1844(((class_1844) potion.method_58694(class_9334.field_49651)).comp_2378(), Optional.of(Integer.valueOf(effect.method_5556())), ((class_1844) potion.method_58694(class_9334.field_49651)).comp_2380(), Optional.empty()));
        return potion;
    }
}
