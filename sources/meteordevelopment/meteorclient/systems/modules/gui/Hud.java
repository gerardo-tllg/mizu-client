package meteordevelopment.meteorclient.systems.modules.gui;

import meteordevelopment.meteorclient.gui.newgui.screens.NewHudEditorScreen;
import meteordevelopment.meteorclient.settings.ActionSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.class_310;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/gui/Hud.class */
public class Hud extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Double> hudTextScale;
    private final Setting<Boolean> editor;

    public Hud() {
        super(Categories.Gui, "hud", "Toggles the HUD. Expand for the editor.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.hudTextScale = this.sgGeneral.add(new DoubleSetting.Builder().name("hud-text-scale").description("Scale of HUD text (drives the underlying Hud system's text-scale).").defaultValue(0.5d).min(0.5d).sliderRange(0.5d, 3.0d).decimalPlaces(2).onChanged(v -> {
            Setting<Double> hudScale;
            meteordevelopment.meteorclient.systems.hud.Hud hud = meteordevelopment.meteorclient.systems.hud.Hud.get();
            if (hud != null && (hudScale = hud.settings.get("text-scale", Double.class)) != null && !hudScale.get().equals(v)) {
                hudScale.set(v);
            }
        }).build());
        this.editor = this.sgGeneral.add(new ActionSetting.Builder().name("editor").description("Open the HUD editor.").buttonLabel("open ›").action(() -> {
            class_310.method_1551().method_1507(new NewHudEditorScreen());
        }).build());
        this.serialize = false;
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public boolean isActive() {
        return meteordevelopment.meteorclient.systems.hud.Hud.get().active;
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void toggle() {
        meteordevelopment.meteorclient.systems.hud.Hud hud = meteordevelopment.meteorclient.systems.hud.Hud.get();
        hud.active = !hud.active;
    }
}
