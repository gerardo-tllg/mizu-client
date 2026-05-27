/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.hud.elements;

import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

import java.util.ArrayList;
import java.util.List;

public class BindsHud extends HudElement {
    public static final HudElementInfo<BindsHud> INFO = new HudElementInfo<>(Hud.GROUP, "binds", "Displays modules with keybinds.", BindsHud::new);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> limit = sgGeneral.add(new IntSetting.Builder()
        .name("limit")
        .description("Maximum number of binds to display.")
        .defaultValue(10)
        .min(1)
        .sliderRange(1, 20)
        .build()
    );

    private final Setting<Boolean> activeOnly = sgGeneral.add(new BoolSetting.Builder()
        .name("active-only")
        .description("Only shows binds for active modules.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> textShadow = sgGeneral.add(new BoolSetting.Builder()
        .name("text-shadow")
        .description("Renders shadow behind text.")
        .defaultValue(true)
        .build()
    );

    private final Setting<SettingColor> titleColor = sgGeneral.add(new ColorSetting.Builder()
        .name("title-color")
        .description("Title color.")
        .defaultValue(new SettingColor())
        .build()
    );

    private final Setting<SettingColor> bindColor = sgGeneral.add(new ColorSetting.Builder()
        .name("bind-color")
        .description("Bind color.")
        .defaultValue(new SettingColor(175, 175, 175))
        .build()
    );

    public BindsHud() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        List<Module> modules = new ArrayList<>();

        for (Module module : Modules.get().getAll()) {
            if (module.keybind.isSet() && (!activeOnly.get() || module.isActive())) {
                modules.add(module);
            }
        }

        if (modules.isEmpty()) {
            String text = "No binds";
            renderer.text(text, x, y, titleColor.get(), textShadow.get());
            setSize(renderer.textWidth(text), renderer.textHeight());
            return;
        }

        double y = this.y;
        double width = 0;

        int count = 0;
        for (Module module : modules) {
            if (count >= limit.get()) break;

            String titleText = module.title + ": ";
            String bindText = module.keybind.toString();

            double lineWidth = renderer.textWidth(titleText + bindText);
            double x1 = renderer.text(titleText, x, y, titleColor.get(), textShadow.get());
            renderer.text(bindText, x1, y, bindColor.get(), textShadow.get());

            width = Math.max(width, lineWidth);
            y += renderer.textHeight() + 2;
            count++;
        }

        setSize(width, y - this.y);
    }
}
