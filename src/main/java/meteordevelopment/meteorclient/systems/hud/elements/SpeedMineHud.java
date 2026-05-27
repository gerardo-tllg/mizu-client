/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.hud.elements;

import meteordevelopment.meteorclient.mixin.ClientPlayerInteractionManagerAccessor;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.player.SpeedMine;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class SpeedMineHud extends HudElement {
    public static final HudElementInfo<SpeedMineHud> INFO = new HudElementInfo<>(Hud.GROUP, "speed-mine", "Displays SpeedMine module information.", SpeedMineHud::new);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> showMode = sgGeneral.add(new BoolSetting.Builder()
        .name("show-mode")
        .description("Shows the current mode.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showProgress = sgGeneral.add(new BoolSetting.Builder()
        .name("show-progress")
        .description("Shows mining progress.")
        .defaultValue(true)
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

    private final Setting<SettingColor> valueColor = sgGeneral.add(new ColorSetting.Builder()
        .name("value-color")
        .description("Value color.")
        .defaultValue(new SettingColor(175, 175, 175))
        .build()
    );

    public SpeedMineHud() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        SpeedMine speedMine = Modules.get().get(SpeedMine.class);

        if (speedMine == null || !speedMine.isActive()) {
            String text = "SpeedMine: OFF";
            renderer.text(text, x, y, titleColor.get(), textShadow.get());
            setSize(renderer.textWidth(text), renderer.textHeight());
            return;
        }

        double y = this.y;
        double width = 0;

        // Title
        String titleText = "SpeedMine";
        renderer.text(titleText, x, y, titleColor.get(), textShadow.get());
        width = Math.max(width, renderer.textWidth(titleText));
        y += renderer.textHeight() + 2;

        // Mode
        if (showMode.get()) {
            String modeText = "Mode: ";
            double x1 = renderer.text(modeText, x, y, titleColor.get(), textShadow.get());
            String modeValue = speedMine.mode.get().toString();
            renderer.text(modeValue, x1, y, valueColor.get(), textShadow.get());
            width = Math.max(width, renderer.textWidth(modeText + modeValue));
            y += renderer.textHeight() + 2;
        }

        // Progress
        if (showProgress.get() && mc.interactionManager != null) {
            float progress = ((ClientPlayerInteractionManagerAccessor) mc.interactionManager).getBreakingProgress();
            if (progress > 0) {
                String progressText = "Progress: ";
                double x1 = renderer.text(progressText, x, y, titleColor.get(), textShadow.get());
                String progressValue = String.format("%.1f%%", progress * 100);
                renderer.text(progressValue, x1, y, valueColor.get(), textShadow.get());
                width = Math.max(width, renderer.textWidth(progressText + progressValue));
                y += renderer.textHeight() + 2;
            }
        }

        setSize(width, y - this.y);
    }
}
