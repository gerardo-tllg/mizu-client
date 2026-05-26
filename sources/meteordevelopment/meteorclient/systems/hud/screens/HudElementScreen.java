package meteordevelopment.meteorclient.systems.hud.screens;

import java.util.Objects;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WMinus;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.systems.hud.HudBox;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.XAnchor;
import meteordevelopment.meteorclient.systems.hud.YAnchor;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import net.minecraft.class_2487;
import net.minecraft.class_332;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/hud/screens/HudElementScreen.class */
public class HudElementScreen extends WindowScreen {
    private final HudElement element;
    private WContainer settingsC1;
    private WContainer settingsC2;
    private final Settings settings;

    public HudElementScreen(GuiTheme theme, HudElement element) {
        super(theme, element.info.title);
        this.element = element;
        this.settings = new Settings();
        SettingGroup sg = this.settings.createGroup("Anchors");
        sg.add(new BoolSetting.Builder().name("auto-anchors").description("Automatically assigns anchors based on the position.").defaultValue(true).onModuleActivated(booleanSetting -> {
            booleanSetting.set(Boolean.valueOf(element.autoAnchors));
        }).onChanged(aBoolean -> {
            if (aBoolean.booleanValue()) {
                element.box.updateAnchors();
            }
            element.autoAnchors = aBoolean.booleanValue();
        }).build());
        EnumSetting.Builder builderOnModuleActivated = new EnumSetting.Builder().name("x-anchor").description("Horizontal anchor.").defaultValue(XAnchor.Left).visible(() -> {
            return !element.autoAnchors;
        }).onModuleActivated(xAnchorSetting -> {
            xAnchorSetting.set(element.box.xAnchor);
        });
        HudBox hudBox = element.box;
        Objects.requireNonNull(hudBox);
        sg.add(builderOnModuleActivated.onChanged(hudBox::setXAnchor).build());
        EnumSetting.Builder builderOnModuleActivated2 = new EnumSetting.Builder().name("y-anchor").description("Vertical anchor.").defaultValue(YAnchor.Top).visible(() -> {
            return !element.autoAnchors;
        }).onModuleActivated(yAnchorSetting -> {
            yAnchorSetting.set(element.box.yAnchor);
        });
        HudBox hudBox2 = element.box;
        Objects.requireNonNull(hudBox2);
        sg.add(builderOnModuleActivated2.onChanged(hudBox2::setYAnchor).build());
    }

    @Override // meteordevelopment.meteorclient.gui.WidgetScreen
    public void initWidgets() {
        add(this.theme.label(this.element.info.description, ((double) Utils.getWindowWidth()) / 2.0d));
        if (this.element.settings.sizeGroups() > 0) {
            this.element.settings.onActivated();
            this.settingsC1 = (WContainer) add(this.theme.verticalList()).expandX().widget();
            this.settingsC1.add(this.theme.settings(this.element.settings)).expandX();
        }
        this.settings.onActivated();
        this.settingsC2 = (WContainer) add(this.theme.verticalList()).expandX().widget();
        this.settingsC2.add(this.theme.settings(this.settings)).expandX();
        add(this.theme.horizontalSeparator()).expandX();
        WWidget widget = this.element.getWidget(this.theme);
        if (widget != null) {
            Cell<WWidget> cell = add(widget);
            if (widget instanceof WContainer) {
                cell.expandX();
            }
            add(this.theme.horizontalSeparator()).expandX();
        }
        WHorizontalList bottomList = (WHorizontalList) add(this.theme.horizontalList()).expandX().widget();
        bottomList.add(this.theme.label("Active:"));
        WCheckbox active = (WCheckbox) bottomList.add(this.theme.checkbox(this.element.isActive())).widget();
        active.action = () -> {
            if (this.element.isActive() != active.checked) {
                this.element.toggle();
            }
        };
        WMinus remove = (WMinus) bottomList.add(this.theme.minus()).expandCellX().right().widget();
        remove.action = () -> {
            this.element.remove();
            method_25419();
        };
    }

    public void method_25393() {
        super.method_25393();
        if (this.settingsC1 != null) {
            this.element.settings.tick(this.settingsC1, this.theme);
        }
        this.settings.tick(this.settingsC2, this.theme);
    }

    @Override // meteordevelopment.meteorclient.gui.WidgetScreen
    protected void onRenderBefore(class_332 drawContext, float delta) {
        HudEditorScreen.renderElements(drawContext);
    }

    @Override // meteordevelopment.meteorclient.gui.WidgetScreen
    public boolean toClipboard() {
        return NbtUtils.toClipboard(this.element.toTag());
    }

    @Override // meteordevelopment.meteorclient.gui.WidgetScreen
    public boolean fromClipboard() {
        class_2487 clipboard = NbtUtils.fromClipboard();
        if (clipboard != null) {
            this.element.fromTag(clipboard);
            return true;
        }
        return false;
    }
}
