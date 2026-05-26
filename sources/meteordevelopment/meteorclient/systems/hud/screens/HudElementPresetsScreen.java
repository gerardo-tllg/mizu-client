package meteordevelopment.meteorclient.systems.hud.screens;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPlus;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.class_332;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/hud/screens/HudElementPresetsScreen.class */
public class HudElementPresetsScreen extends WindowScreen {
    private final HudElementInfo<?> info;
    private final int x;
    private final int y;
    private final WTextBox searchBar;
    private HudElementInfo<?>.Preset firstPreset;

    public HudElementPresetsScreen(GuiTheme theme, HudElementInfo<?> info, int x, int y) {
        super(theme, "Select preset for " + info.title);
        this.info = info;
        this.x = x + 9;
        this.y = y;
        this.searchBar = theme.textBox("");
        this.searchBar.action = () -> {
            clear();
            initWidgets();
        };
        this.enterAction = () -> {
            Hud.get().add(this.firstPreset, x, y);
            method_25419();
        };
    }

    @Override // meteordevelopment.meteorclient.gui.WidgetScreen
    public void initWidgets() {
        this.firstPreset = null;
        add(this.searchBar).expandX();
        this.searchBar.setFocused(true);
        for (HudElementInfo<?>.Preset preset : this.info.presets) {
            if (Utils.searchTextDefault(preset.title, this.searchBar.get(), false)) {
                WHorizontalList l = (WHorizontalList) add(this.theme.horizontalList()).expandX().widget();
                l.add(this.theme.label(preset.title));
                WPlus add = (WPlus) l.add(this.theme.plus()).expandCellX().right().widget();
                add.action = () -> {
                    Hud.get().add((HudElementInfo<?>.Preset) preset, this.x, this.y);
                    method_25419();
                };
                if (this.firstPreset == null) {
                    this.firstPreset = preset;
                }
            }
        }
    }

    @Override // meteordevelopment.meteorclient.gui.WidgetScreen
    protected void onRenderBefore(class_332 drawContext, float delta) {
        HudEditorScreen.renderElements(drawContext);
    }
}
