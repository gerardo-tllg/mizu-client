package meteordevelopment.meteorclient.gui.screens.settings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.class_3545;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/screens/settings/CollectionListSettingScreen.class */
public abstract class CollectionListSettingScreen<T> extends WindowScreen {
    protected final Setting<?> setting;
    protected final Collection<T> collection;
    private final Iterable<T> registry;
    private WTextBox filter;
    private String filterText;
    private WTable table;

    protected abstract WWidget getValueWidget(T t);

    protected abstract String getValueName(T t);

    public CollectionListSettingScreen(GuiTheme theme, String title, Setting<?> setting, Collection<T> collection, Iterable<T> registry) {
        super(theme, title);
        this.filterText = "";
        this.registry = registry;
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
            initWidgets(this.registry);
        };
        this.table = (WTable) add(this.theme.table()).expandX().widget();
        initWidgets(this.registry);
    }

    private void initWidgets(Iterable<T> registry) {
        WTable left = abc(pairs -> {
            registry.forEach(t -> {
                if (skipValue(t) || this.collection.contains(t)) {
                    return;
                }
                int words = Utils.searchInWords(getValueName(t), this.filterText);
                int diff = Utils.searchLevenshteinDefault(getValueName(t), this.filterText, false);
                if (words > 0 || diff <= getValueName(t).length() / 2) {
                    pairs.add(new class_3545(t, Integer.valueOf(-diff)));
                }
            });
        }, true, t -> {
            addValue(registry, t);
            T v = getAdditionalValue(t);
            if (v != null) {
                addValue(registry, v);
            }
        });
        if (!left.cells.isEmpty()) {
            this.table.add(this.theme.verticalSeparator()).expandWidgetY();
        }
        abc(pairs2 -> {
            for (T value : this.collection) {
                if (!skipValue(value)) {
                    int words = Utils.searchInWords(getValueName(value), this.filterText);
                    int diff = Utils.searchLevenshteinDefault(getValueName(value), this.filterText, false);
                    if (words > 0 || diff <= getValueName(value).length() / 2) {
                        pairs2.add(new class_3545(value, Integer.valueOf(-diff)));
                    }
                }
            }
        }, false, t2 -> {
            removeValue(registry, t2);
            T v = getAdditionalValue(t2);
            if (v != null) {
                removeValue(registry, v);
            }
        });
    }

    private void addValue(Iterable<T> registry, T value) {
        if (!this.collection.contains(value)) {
            this.collection.add(value);
            this.setting.onChanged();
            this.table.clear();
            initWidgets(registry);
        }
    }

    private void removeValue(Iterable<T> registry, T value) {
        if (this.collection.remove(value)) {
            this.setting.onChanged();
            this.table.clear();
            initWidgets(registry);
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    private WTable abc(Consumer<List<class_3545<T, Integer>>> addValues, boolean isLeft, Consumer<T> buttonAction) {
        Cell<WTable> cell = this.table.add(this.theme.table()).top();
        WTable table = (WTable) cell.widget();
        Consumer consumer = t -> {
            if (includeValue(t)) {
                table.add(getValueWidget(t));
                WPressable button = (WPressable) table.add(isLeft ? this.theme.plus() : this.theme.minus()).expandCellX().right().widget();
                button.action = () -> {
                    buttonAction.accept(t);
                };
                table.row();
            }
        };
        List<class_3545<T, Integer>> values = new ArrayList<>();
        addValues.accept(values);
        if (!this.filterText.isEmpty()) {
            values.sort(Comparator.comparingInt(value -> {
                return -((Integer) value.method_15441()).intValue();
            }));
        }
        for (class_3545<T, Integer> pair : values) {
            consumer.accept(pair.method_15442());
        }
        if (!table.cells.isEmpty()) {
            cell.expandX();
        }
        return table;
    }

    protected boolean includeValue(T value) {
        return true;
    }

    protected boolean skipValue(T value) {
        return false;
    }

    protected T getAdditionalValue(T value) {
        return null;
    }
}
