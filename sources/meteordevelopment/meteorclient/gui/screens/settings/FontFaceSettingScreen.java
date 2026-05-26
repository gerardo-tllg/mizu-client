package meteordevelopment.meteorclient.gui.screens.settings;

import java.util.List;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.containers.WView;
import meteordevelopment.meteorclient.gui.widgets.input.WDropdown;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.renderer.Fonts;
import meteordevelopment.meteorclient.renderer.text.FontFamily;
import meteordevelopment.meteorclient.renderer.text.FontInfo;
import meteordevelopment.meteorclient.settings.FontFaceSetting;
import org.apache.commons.lang3.StringUtils;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/screens/settings/FontFaceSettingScreen.class */
public class FontFaceSettingScreen extends WindowScreen {
    private final FontFaceSetting setting;
    private WTable table;
    private WTextBox filter;
    private String filterText;

    public FontFaceSettingScreen(GuiTheme theme, FontFaceSetting setting) {
        super(theme, "Select Font");
        this.filterText = "";
        this.setting = setting;
    }

    @Override // meteordevelopment.meteorclient.gui.WidgetScreen
    public void initWidgets() {
        this.filter = (WTextBox) add(this.theme.textBox("")).expandX().widget();
        this.filter.setFocused(true);
        this.filter.action = () -> {
            this.filterText = this.filter.get().trim();
            this.table.clear();
            initTable();
        };
        this.window.view.hasScrollBar = false;
        this.enterAction = () -> {
            List<Cell<?>> row = this.table.getRow(0);
            if (row == null) {
                return;
            }
            WWidget widget = row.get(2).widget();
            if (widget instanceof WButton) {
                WButton button = (WButton) widget;
                button.action.run();
            }
        };
        WView view = (WView) add(this.theme.view()).expandX().widget();
        view.scrollOnlyWhenMouseOver = false;
        this.table = (WTable) view.add(this.theme.table()).expandX().widget();
        initTable();
    }

    private void initTable() {
        for (FontFamily fontFamily : Fonts.FONT_FAMILIES) {
            String name = fontFamily.getName();
            WLabel item = this.theme.label(name);
            if (this.filterText.isEmpty() || StringUtils.containsIgnoreCase(name, this.filterText)) {
                this.table.add(item);
                WDropdown<FontInfo.Type> dropdown = (WDropdown) this.table.add(this.theme.dropdown(FontInfo.Type.Regular)).right().widget();
                WButton select = (WButton) this.table.add(this.theme.button("Select")).expandCellX().right().widget();
                select.action = () -> {
                    this.setting.set(fontFamily.get((FontInfo.Type) dropdown.get()));
                    method_25419();
                };
                this.table.row();
            }
        }
    }
}
