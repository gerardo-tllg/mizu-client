package meteordevelopment.meteorclient.gui.screens.settings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPlus;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.class_151;
import net.minecraft.class_2378;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3545;
import net.minecraft.class_5321;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/screens/settings/DynamicRegistryListSettingScreen.class */
public abstract class DynamicRegistryListSettingScreen<E> extends WindowScreen {
    protected final Setting<?> setting;
    protected final Collection<class_5321<E>> collection;
    private final class_5321<class_2378<E>> registryKey;
    private final Optional<class_2378<E>> registry;
    private WTextBox filter;
    private String filterText;
    private WTable table;

    protected abstract WWidget getValueWidget(class_5321<E> class_5321Var);

    protected abstract String getValueName(class_5321<E> class_5321Var);

    public DynamicRegistryListSettingScreen(GuiTheme theme, String title, Setting<?> setting, Collection<class_5321<E>> collection, class_5321<class_2378<E>> registryKey) {
        super(theme, title);
        this.filterText = "";
        this.registryKey = registryKey;
        this.registry = Optional.ofNullable(class_310.method_1551().method_1562()).flatMap(networkHandler -> {
            return networkHandler.method_29091().method_46759(registryKey);
        });
        this.setting = setting;
        this.collection = collection;
    }

    @Override // meteordevelopment.meteorclient.gui.WidgetScreen
    public void initWidgets() {
        this.filter = (WTextBox) add(this.theme.textBox("")).minWidth(400.0d).expandX().widget();
        this.filter.setFocused(true);
        this.filter.action = () -> {
            this.filterText = this.filter.get().trim();
            this.table.clear();
            generateWidgets();
        };
        this.table = (WTable) add(this.theme.table()).expandX().widget();
        generateWidgets();
    }

    private void generateWidgets() {
        WTable left = abc(pairs -> {
            this.registry.ifPresent(registry -> {
                registry.method_42017().map((v0) -> {
                    return v0.method_40230();
                }).filter((v0) -> {
                    return v0.isPresent();
                }).map((v0) -> {
                    return v0.get();
                }).forEach(t -> {
                    if (skipValue(t) || this.collection.contains(t)) {
                        return;
                    }
                    int words = Utils.searchInWords(getValueName(t), this.filterText);
                    int diff = Utils.searchLevenshteinDefault(getValueName(t), this.filterText, false);
                    if (words > 0 || diff <= getValueName(t).length() / 2) {
                        pairs.add(new class_3545(t, Integer.valueOf(-diff)));
                    }
                });
            });
        }, true, t -> {
            addValue(t);
            class_5321<E> v = getAdditionalValue(t);
            if (v != null) {
                addValue(v);
            }
        });
        if (!left.cells.isEmpty()) {
            left.add(this.theme.horizontalSeparator()).expandX();
            left.row();
        }
        WHorizontalList manualEntry = (WHorizontalList) left.add(this.theme.horizontalList()).expandX().widget();
        WTextBox textBox = (WTextBox) manualEntry.add(this.theme.textBox("minecraft:")).expandX().minWidth(120.0d).widget();
        ((WPlus) manualEntry.add(this.theme.plus()).expandCellX().right().widget()).action = () -> {
            String entry = textBox.get().trim();
            try {
                class_2960 id = entry.contains(":") ? class_2960.method_60654(entry) : class_2960.method_60656(entry);
                addValue(class_5321.method_29179(this.registryKey, id));
            } catch (class_151 e) {
            }
        };
        this.table.add(this.theme.verticalSeparator()).expandWidgetY();
        abc(pairs2 -> {
            for (class_5321<E> value : this.collection) {
                if (!skipValue(value)) {
                    int words = Utils.searchInWords(getValueName(value), this.filterText);
                    int diff = Utils.searchLevenshteinDefault(getValueName(value), this.filterText, false);
                    if (words > 0 || diff <= getValueName(value).length() / 2) {
                        pairs2.add(new class_3545(value, Integer.valueOf(-diff)));
                    }
                }
            }
        }, false, t2 -> {
            removeValue(t2);
            class_5321<E> v = getAdditionalValue(t2);
            if (v != null) {
                removeValue(v);
            }
        });
    }

    private void addValue(class_5321<E> value) {
        if (!this.collection.contains(value)) {
            this.collection.add(value);
            this.setting.onChanged();
            this.table.clear();
            generateWidgets();
        }
    }

    private void removeValue(class_5321<E> value) {
        if (this.collection.remove(value)) {
            this.setting.onChanged();
            this.table.clear();
            generateWidgets();
        }
    }

    private WTable abc(Consumer<List<class_3545<class_5321<E>, Integer>>> addValues, boolean isLeft, Consumer<class_5321<E>> buttonAction) {
        Cell<WTable> cell = this.table.add(this.theme.table()).top();
        WTable table = (WTable) cell.widget();
        Consumer<class_5321<E>> forEach = t -> {
            if (includeValue(t)) {
                table.add(getValueWidget(t));
                WPressable button = (WPressable) table.add(isLeft ? this.theme.plus() : this.theme.minus()).expandCellX().right().widget();
                button.action = () -> {
                    buttonAction.accept(t);
                };
                table.row();
            }
        };
        List<class_3545<class_5321<E>, Integer>> values = new ArrayList<>();
        addValues.accept(values);
        if (!this.filterText.isEmpty()) {
            values.sort(Comparator.comparingInt(value -> {
                return -((Integer) value.method_15441()).intValue();
            }));
        }
        for (class_3545<class_5321<E>, Integer> pair : values) {
            forEach.accept((class_5321) pair.method_15442());
        }
        if (!table.cells.isEmpty()) {
            cell.expandX();
        }
        return table;
    }

    protected boolean includeValue(class_5321<E> value) {
        return true;
    }

    protected boolean skipValue(class_5321<E> value) {
        return false;
    }

    protected class_5321<E> getAdditionalValue(class_5321<E> value) {
        return null;
    }
}
