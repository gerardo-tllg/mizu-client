package meteordevelopment.meteorclient.gui.tabs.builtin;

import java.util.Objects;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.WindowTabScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.screens.HudEditorScreen;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import net.minecraft.class_332;
import net.minecraft.class_437;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/tabs/builtin/HudTab.class */
public class HudTab extends Tab {
    public HudTab() {
        super("HUD");
    }

    @Override // meteordevelopment.meteorclient.gui.tabs.Tab
    public TabScreen createScreen(GuiTheme theme) {
        return new HudScreen(theme, this);
    }

    @Override // meteordevelopment.meteorclient.gui.tabs.Tab
    public boolean isScreen(class_437 screen) {
        return screen instanceof HudScreen;
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/tabs/builtin/HudTab$HudScreen.class */
    public static class HudScreen extends WindowTabScreen {
        private final Hud hud;

        public HudScreen(GuiTheme theme, Tab tab) {
            super(theme, tab);
            this.hud = Hud.get();
            this.hud.settings.onActivated();
        }

        @Override // meteordevelopment.meteorclient.gui.WidgetScreen
        public void initWidgets() {
            add(this.theme.settings(this.hud.settings)).expandX();
            add(this.theme.horizontalSeparator()).expandX();
            WButton openEditor = (WButton) add(this.theme.button("Edit")).expandX().widget();
            openEditor.action = () -> {
                MeteorClient.mc.method_1507(new HudEditorScreen(this.theme));
            };
            WHorizontalList buttons = (WHorizontalList) add(this.theme.horizontalList()).expandX().widget();
            WButton wButton = (WButton) buttons.add(this.theme.button("Clear")).expandX().widget();
            Hud hud = this.hud;
            Objects.requireNonNull(hud);
            wButton.action = hud::clear;
            WButton wButton2 = (WButton) buttons.add(this.theme.button("Reset to default elements")).expandX().widget();
            Hud hud2 = this.hud;
            Objects.requireNonNull(hud2);
            wButton2.action = hud2::resetToDefaultElements;
            add(this.theme.horizontalSeparator()).expandX();
            WHorizontalList bottom = (WHorizontalList) add(this.theme.horizontalList()).expandX().widget();
            bottom.add(this.theme.label("Active: "));
            WCheckbox active = (WCheckbox) bottom.add(this.theme.checkbox(this.hud.active)).expandCellX().widget();
            active.action = () -> {
                this.hud.active = active.checked;
            };
            WButton resetSettings = (WButton) bottom.add(this.theme.button(GuiRenderer.RESET)).widget();
            Settings settings = this.hud.settings;
            Objects.requireNonNull(settings);
            resetSettings.action = settings::reset;
        }

        @Override // meteordevelopment.meteorclient.gui.WidgetScreen
        protected void onRenderBefore(class_332 drawContext, float delta) {
            HudEditorScreen.renderElements(drawContext);
        }

        @Override // meteordevelopment.meteorclient.gui.WidgetScreen
        public boolean toClipboard() {
            return NbtUtils.toClipboard(this.hud);
        }

        @Override // meteordevelopment.meteorclient.gui.WidgetScreen
        public boolean fromClipboard() {
            return NbtUtils.fromClipboard(this.hud);
        }
    }
}
