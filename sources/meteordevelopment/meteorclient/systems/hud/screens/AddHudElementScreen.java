package meteordevelopment.meteorclient.systems.hud.screens;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WSection;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPlus;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.class_332;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/hud/screens/AddHudElementScreen.class */
public class AddHudElementScreen extends WindowScreen {
    private final int x;
    private final int y;
    private final WTextBox searchBar;
    private Object firstObject;

    public AddHudElementScreen(GuiTheme theme, int x, int y) {
        super(theme, "Add Hud element");
        this.x = x;
        this.y = y;
        this.searchBar = theme.textBox("");
        this.searchBar.action = () -> {
            clear();
            initWidgets();
        };
        this.enterAction = () -> {
            runObject(this.firstObject);
        };
    }

    @Override // meteordevelopment.meteorclient.gui.WidgetScreen
    public void initWidgets() {
        this.firstObject = null;
        add(this.searchBar).expandX();
        this.searchBar.setFocused(true);
        Hud hud = Hud.get();
        Map<HudGroup, List<Item>> grouped = new HashMap<>();
        for (HudElementInfo<?> info : hud.infos.values()) {
            if (info.hasPresets() && !this.searchBar.get().isEmpty()) {
                for (HudElementInfo<?>.Preset preset : info.presets) {
                    String title = info.title + "  -  " + preset.title;
                    if (Utils.searchTextDefault(title, this.searchBar.get(), false)) {
                        grouped.computeIfAbsent(info.group, hudGroup -> {
                            return new ArrayList();
                        }).add(new Item(title, info.description, preset));
                    }
                }
            } else if (Utils.searchTextDefault(info.title, this.searchBar.get(), false)) {
                grouped.computeIfAbsent(info.group, hudGroup2 -> {
                    return new ArrayList();
                }).add(new Item(info.title, info.description, info));
            }
        }
        for (HudGroup group : grouped.keySet()) {
            WSection section = (WSection) add(this.theme.section(group.title())).expandX().widget();
            for (Item item : grouped.get(group)) {
                WHorizontalList l = (WHorizontalList) section.add(this.theme.horizontalList()).expandX().widget();
                ((WLabel) l.add(this.theme.label(item.title)).widget()).tooltip = item.description;
                Object obj = item.object;
                if (obj instanceof HudElementInfo.Preset) {
                    HudElementInfo.Preset preset2 = (HudElementInfo.Preset) obj;
                    WPlus add = (WPlus) l.add(this.theme.plus()).expandCellX().right().widget();
                    add.action = () -> {
                        runObject(preset2);
                    };
                    if (this.firstObject == null) {
                        this.firstObject = preset2;
                    }
                } else {
                    HudElementInfo<?> info2 = (HudElementInfo) item.object;
                    if (info2.hasPresets()) {
                        WButton open = (WButton) l.add(this.theme.button(" > ")).expandCellX().right().widget();
                        open.action = () -> {
                            runObject(info2);
                        };
                    } else {
                        WPlus add2 = (WPlus) l.add(this.theme.plus()).expandCellX().right().widget();
                        add2.action = () -> {
                            runObject(info2);
                        };
                    }
                    if (this.firstObject == null) {
                        this.firstObject = info2;
                    }
                }
            }
        }
    }

    private void runObject(Object object) {
        if (object == null) {
            return;
        }
        if (object instanceof HudElementInfo.Preset) {
            Hud.get().add((HudElementInfo<?>.Preset) object, this.x, this.y);
            method_25419();
            return;
        }
        HudElementInfo<?> info = (HudElementInfo) object;
        if (info.hasPresets()) {
            HudElementPresetsScreen screen = new HudElementPresetsScreen(this.theme, info, this.x, this.y);
            screen.parent = this.parent;
            MeteorClient.mc.method_1507(screen);
        } else {
            Hud.get().add(info, this.x, this.y);
            method_25419();
        }
    }

    @Override // meteordevelopment.meteorclient.gui.WidgetScreen
    protected void onRenderBefore(class_332 drawContext, float delta) {
        HudEditorScreen.renderElements(drawContext);
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/hud/screens/AddHudElementScreen$Item.class */
    private record Item(String title, String description, Object object) {
    }
}
