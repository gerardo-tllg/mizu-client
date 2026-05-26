package meteordevelopment.meteorclient.gui.screens.settings;

import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.input.WIntEdit;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.utils.misc.Names;
import net.minecraft.class_1291;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1844;
import net.minecraft.class_9334;
import org.apache.commons.lang3.StringUtils;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/screens/settings/StatusEffectAmplifierMapSettingScreen.class */
public class StatusEffectAmplifierMapSettingScreen extends WindowScreen {
    private final Setting<Reference2IntMap<class_1291>> setting;
    private WTable table;
    private String filterText;

    public StatusEffectAmplifierMapSettingScreen(GuiTheme theme, Setting<Reference2IntMap<class_1291>> setting) {
        super(theme, "Modify Amplifiers");
        this.filterText = "";
        this.setting = setting;
    }

    @Override // meteordevelopment.meteorclient.gui.WidgetScreen
    public void initWidgets() {
        WTextBox filter = (WTextBox) add(this.theme.textBox("")).minWidth(400.0d).expandX().widget();
        filter.setFocused(true);
        filter.action = () -> {
            this.filterText = filter.get().trim();
            this.table.clear();
            initTable();
        };
        this.table = (WTable) add(this.theme.table()).expandX().widget();
        initTable();
    }

    private void initTable() {
        List<class_1291> statusEffects = new ArrayList<>((Collection<? extends class_1291>) this.setting.get().keySet());
        statusEffects.sort(Comparator.comparing(Names::get));
        for (class_1291 statusEffect : statusEffects) {
            String name = Names.get(statusEffect);
            if (StringUtils.containsIgnoreCase(name, this.filterText)) {
                this.table.add(this.theme.itemWithLabel(getPotionStack(statusEffect), name)).expandCellX();
                WIntEdit level = this.theme.intEdit(this.setting.get().getInt(statusEffect), 0, Integer.MAX_VALUE, true);
                level.action = () -> {
                    this.setting.get().put(statusEffect, level.get());
                    this.setting.onChanged();
                };
                this.table.add(level).minWidth(50.0d);
                this.table.row();
            }
        }
    }

    private class_1799 getPotionStack(class_1291 effect) {
        class_1799 potion = class_1802.field_8574.method_7854();
        potion.method_57379(class_9334.field_49651, new class_1844(((class_1844) potion.method_58694(class_9334.field_49651)).comp_2378(), Optional.of(Integer.valueOf(effect.method_5556())), ((class_1844) potion.method_58694(class_9334.field_49651)).comp_2380(), Optional.empty()));
        return potion;
    }
}
