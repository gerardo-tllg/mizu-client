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
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

public class TimerHud extends HudElement {
    public static final HudElementInfo<TimerHud> INFO = new HudElementInfo<>(Hud.GROUP, "timer", "Displays the timer multiplier.", TimerHud::new);

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

    public TimerHud() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        Timer timer = Modules.get().get(Timer.class);

        if (timer == null || !timer.isActive()) {
            String text = "Timer: OFF";
            renderer.text(text, x, y, titleColor.get(), textShadow.get());
            setSize(renderer.textWidth(text), renderer.textHeight());
            return;
        }

        double multiplier = timer.getMultiplier();
        String titleText = "Timer: ";
        String valueText = String.format("%.2fx", multiplier);

        double width = renderer.textWidth(titleText + valueText);
        double x1 = renderer.text(titleText, x, y, titleColor.get(), textShadow.get());
        renderer.text(valueText, x1, y, valueColor.get(), textShadow.get());

        setSize(width, renderer.textHeight());
    }
}
