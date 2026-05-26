package meteordevelopment.meteorclient.gui.tabs.builtin;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.screens.EditSystemScreen;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.WindowTabScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WMinus;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.systems.macros.Macro;
import meteordevelopment.meteorclient.systems.macros.Macros;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import net.minecraft.class_437;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/tabs/builtin/MacrosTab.class */
public class MacrosTab extends Tab {
    public MacrosTab() {
        super("Macros");
    }

    @Override // meteordevelopment.meteorclient.gui.tabs.Tab
    public TabScreen createScreen(GuiTheme theme) {
        return new MacrosScreen(theme, this);
    }

    @Override // meteordevelopment.meteorclient.gui.tabs.Tab
    public boolean isScreen(class_437 screen) {
        return screen instanceof MacrosScreen;
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/tabs/builtin/MacrosTab$MacrosScreen.class */
    private static class MacrosScreen extends WindowTabScreen {
        public MacrosScreen(GuiTheme theme, Tab tab) {
            super(theme, tab);
        }

        @Override // meteordevelopment.meteorclient.gui.WidgetScreen
        public void initWidgets() {
            WTable table = (WTable) add(this.theme.table()).expandX().minWidth(400.0d).widget();
            initTable(table);
            add(this.theme.horizontalSeparator()).expandX();
            WButton create = (WButton) add(this.theme.button("Create")).expandX().widget();
            create.action = () -> {
                MeteorClient.mc.method_1507(new EditMacroScreen(this.theme, null, this::reload));
            };
        }

        private void initTable(WTable table) {
            table.clear();
            if (Macros.get().isEmpty()) {
                return;
            }
            for (Macro macro : Macros.get()) {
                table.add(this.theme.label(macro.name.get() + " (" + String.valueOf(macro.keybind.get()) + ")"));
                WButton edit = (WButton) table.add(this.theme.button(GuiRenderer.EDIT)).expandCellX().right().widget();
                edit.action = () -> {
                    MeteorClient.mc.method_1507(new EditMacroScreen(this.theme, macro, this::reload));
                };
                WMinus remove = (WMinus) table.add(this.theme.minus()).widget();
                remove.action = () -> {
                    Macros.get().remove(macro);
                    reload();
                };
                table.row();
            }
        }

        @Override // meteordevelopment.meteorclient.gui.WidgetScreen
        public boolean toClipboard() {
            return NbtUtils.toClipboard(Macros.get());
        }

        @Override // meteordevelopment.meteorclient.gui.WidgetScreen
        public boolean fromClipboard() {
            return NbtUtils.fromClipboard(Macros.get());
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/tabs/builtin/MacrosTab$EditMacroScreen.class */
    private static class EditMacroScreen extends EditSystemScreen<Macro> {
        public EditMacroScreen(GuiTheme theme, Macro value, Runnable reload) {
            super(theme, value, reload);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // meteordevelopment.meteorclient.gui.screens.EditSystemScreen
        public Macro create() {
            return new Macro();
        }

        /* JADX WARN: Multi-variable type inference failed */
        @Override // meteordevelopment.meteorclient.gui.screens.EditSystemScreen
        public boolean save() {
            if (((Macro) this.value).name.get().isBlank() || ((Macro) this.value).messages.get().isEmpty() || !((Macro) this.value).keybind.get().isSet()) {
                return false;
            }
            if (this.isNew) {
                for (Macro m : Macros.get()) {
                    if (((Macro) this.value).equals(m)) {
                        return false;
                    }
                }
            }
            if (!this.isNew) {
                Macros.get().save();
                return true;
            }
            Macros.get().add((Macro) this.value);
            return true;
        }

        /* JADX WARN: Multi-variable type inference failed */
        @Override // meteordevelopment.meteorclient.gui.screens.EditSystemScreen
        public Settings getSettings() {
            return ((Macro) this.value).settings;
        }
    }
}
