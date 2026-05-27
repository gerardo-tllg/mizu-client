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
import meteordevelopment.meteorclient.systems.modules.player.Reach;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

public class ReachDisplayHud extends HudElement {
    public static final HudElementInfo<ReachDisplayHud> INFO = new HudElementInfo<>(Hud.GROUP, "reach-display", "Displays your current reach distance.", ReachDisplayHud::new);

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

    public ReachDisplayHud() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        Reach reach = Modules.get().get(Reach.class);

        double y = this.y;
        double width = 0;

        if (reach == null || !reach.isActive()) {
            String text = "Reach: Default";
            renderer.text(text, x, y, titleColor.get(), textShadow.get());
            setSize(renderer.textWidth(text), renderer.textHeight());
            return;
        }

        // Block Reach
        String blockTitleText = "Block: ";
        double blockReach = 4.5 + reach.blockReach();
        String blockValueText = String.format("%.1f", blockReach);
        double x1 = renderer.text(blockTitleText, x, y, titleColor.get(), textShadow.get());
        renderer.text(blockValueText, x1, y, valueColor.get(), textShadow.get());
        width = Math.max(width, renderer.textWidth(blockTitleText + blockValueText));
        y += renderer.textHeight() + 2;

        // Entity Reach
        String entityTitleText = "Entity: ";
        double entityReach = 3.0 + reach.entityReach();
        String entityValueText = String.format("%.1f", entityReach);
        x1 = renderer.text(entityTitleText, x, y, titleColor.get(), textShadow.get());
        renderer.text(entityValueText, x1, y, valueColor.get(), textShadow.get());
        width = Math.max(width, renderer.textWidth(entityTitleText + entityValueText));

        setSize(width, y - this.y + renderer.textHeight());
    }
}
