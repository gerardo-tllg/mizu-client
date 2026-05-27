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
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.autocrystal.AutoCrystal;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class AutoCrystalHud extends HudElement {
    public static final HudElementInfo<AutoCrystalHud> INFO = new HudElementInfo<>(Hud.GROUP, "auto-crystal", "Displays AutoCrystal module information.", AutoCrystalHud::new);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

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

    private final Setting<SettingColor> valueColor = sgGeneral.add(new ColorSetting.Builder()
        .name("value-color")
        .description("Value color.")
        .defaultValue(new SettingColor(175, 175, 175))
        .build()
    );

    public AutoCrystalHud() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        AutoCrystal autoCrystal = Modules.get().get(AutoCrystal.class);

        if (autoCrystal == null || !autoCrystal.isActive()) {
            String text = "AutoCrystal: OFF";
            renderer.text(text, x, y, titleColor.get(), textShadow.get());
            setSize(renderer.textWidth(text), renderer.textHeight());
            return;
        }

        double y = this.y;
        double width = 0;

        // Title
        String titleText = "AutoCrystal";
        double titleWidth = renderer.textWidth(titleText);
        renderer.text(titleText, x, y, titleColor.get(), textShadow.get());
        y += renderer.textHeight() + 2;
        width = Math.max(width, titleWidth);

        // Info String
        String info = autoCrystal.getInfoString();
        if (info != null && !info.isEmpty()) {
            double infoWidth = renderer.textWidth("Info: ") + renderer.textWidth(info);
            double x1 = renderer.text("Info: ", x, y, titleColor.get(), textShadow.get());
            renderer.text(info, x1, y, valueColor.get(), textShadow.get());
            y += renderer.textHeight() + 2;
            width = Math.max(width, infoWidth);
        }

        setSize(width, y - this.y);
    }
}
