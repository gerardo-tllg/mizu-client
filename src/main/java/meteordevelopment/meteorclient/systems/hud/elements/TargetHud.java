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
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.entity.player.PlayerEntity;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class TargetHud extends HudElement {
    public static final HudElementInfo<TargetHud> INFO = new HudElementInfo<>(Hud.GROUP, "target", "Displays information about your current target.", TargetHud::new);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("The range to target players.")
        .defaultValue(100)
        .min(1)
        .sliderMax(200)
        .build()
    );

    private final Setting<Boolean> showHealth = sgGeneral.add(new BoolSetting.Builder()
        .name("show-health")
        .description("Shows the target's health.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showDistance = sgGeneral.add(new BoolSetting.Builder()
        .name("show-distance")
        .description("Shows the distance to the target.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showPing = sgGeneral.add(new BoolSetting.Builder()
        .name("show-ping")
        .description("Shows the target's ping.")
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

    public TargetHud() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        PlayerEntity target = TargetUtils.getPlayerTarget(range.get(), SortPriority.LowestDistance);

        if (target == null && !isInEditor()) {
            String text = "Target: None";
            renderer.text(text, x, y, titleColor.get(), textShadow.get());
            setSize(renderer.textWidth(text), renderer.textHeight());
            return;
        }

        if (isInEditor()) target = mc.player;

        double y = this.y;
        double width = 0;

        // Name
        String nameText = "Target: " + (target != null ? target.getName().getString() : "None");
        renderer.text(nameText, x, y, titleColor.get(), textShadow.get());
        width = Math.max(width, renderer.textWidth(nameText));
        y += renderer.textHeight() + 2;

        if (target != null) {
            // Health
            if (showHealth.get()) {
                double health = Math.round(target.getHealth() * 10.0) / 10.0;
                double maxHealth = Math.round(target.getMaxHealth() * 10.0) / 10.0;
                String healthText = "Health: ";
                double x1 = renderer.text(healthText, x, y, titleColor.get(), textShadow.get());
                String healthValue = health + "/" + maxHealth;
                renderer.text(healthValue, x1, y, valueColor.get(), textShadow.get());
                width = Math.max(width, renderer.textWidth(healthText + healthValue));
                y += renderer.textHeight() + 2;
            }

            // Distance
            if (showDistance.get() && !isInEditor()) {
                double dist = Math.round(mc.player.distanceTo(target) * 10.0) / 10.0;
                String distText = "Distance: ";
                double x1 = renderer.text(distText, x, y, titleColor.get(), textShadow.get());
                String distValue = dist + "m";
                renderer.text(distValue, x1, y, valueColor.get(), textShadow.get());
                width = Math.max(width, renderer.textWidth(distText + distValue));
                y += renderer.textHeight() + 2;
            }

            // Ping
            if (showPing.get()) {
                int ping = EntityUtils.getPing(target);
                String pingText = "Ping: ";
                double x1 = renderer.text(pingText, x, y, titleColor.get(), textShadow.get());
                String pingValue = ping + "ms";
                renderer.text(pingValue, x1, y, valueColor.get(), textShadow.get());
                width = Math.max(width, renderer.textWidth(pingText + pingValue));
                y += renderer.textHeight() + 2;
            }
        }

        setSize(width, y - this.y);
    }
}
