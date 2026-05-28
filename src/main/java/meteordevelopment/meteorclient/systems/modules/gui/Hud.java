/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.gui;

import meteordevelopment.meteorclient.gui.newgui.screens.NewHudEditorScreen;
import meteordevelopment.meteorclient.settings.ActionSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.client.MinecraftClient;

/**
 * Clickgui entry for the HUD. Left-click toggles the HUD's active state
 * (via {@link meteordevelopment.meteorclient.systems.hud.Hud#active}); the
 * module row's displayed state mirrors that flag so it stays in sync with
 * the actual HUD. Expanding the module exposes the HUD text scale and an
 * "editor" button that opens the new-theme HUD editor.
 */
public class Hud extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    @SuppressWarnings("unused")
    private final Setting<Double> hudTextScale = sgGeneral.add(new DoubleSetting.Builder()
        .name("hud-text-scale")
        .description("Scale of HUD text (drives the underlying Hud system's text-scale).")
        .defaultValue(0.5)
        .min(0.5)
        .sliderRange(0.5, 3)
        .decimalPlaces(2)
        .onChanged(v -> {
            var hud = meteordevelopment.meteorclient.systems.hud.Hud.get();
            if (hud == null) return;
            Setting<Double> hudScale = hud.settings.get("text-scale", Double.class);
            if (hudScale != null && !hudScale.get().equals(v)) hudScale.set(v);
        })
        .build()
    );

    @SuppressWarnings("unused")
    private final Setting<Boolean> editor = sgGeneral.add(new ActionSetting.Builder()
        .name("editor")
        .description("Open the HUD editor.")
        .buttonLabel("open \u203A")
        .action(() -> MinecraftClient.getInstance().setScreen(new NewHudEditorScreen()))
        .build()
    );

    public Hud() {
        super(Categories.Gui, "hud", "Toggles the HUD. Expand for the editor.");
        serialize = false;
    }

    /** Mirror the underlying Hud system's flag rather than our own. */
    @Override
    public boolean isActive() {
        return meteordevelopment.meteorclient.systems.hud.Hud.get().active;
    }

    /** Flip the Hud system's flag directly; don't touch the module active-list / event bus. */
    @Override
    public void toggle() {
        var hud = meteordevelopment.meteorclient.systems.hud.Hud.get();
        hud.active = !hud.active;
    }
}
