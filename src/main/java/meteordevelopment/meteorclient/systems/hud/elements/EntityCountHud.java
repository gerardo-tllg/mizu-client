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
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class EntityCountHud extends HudElement {
    public static final HudElementInfo<EntityCountHud> INFO = new HudElementInfo<>(Hud.GROUP, "entity-count", "Displays entity counts around you.", EntityCountHud::new);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> showPlayers = sgGeneral.add(new BoolSetting.Builder()
        .name("show-players")
        .description("Shows player count.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showHostile = sgGeneral.add(new BoolSetting.Builder()
        .name("show-hostile")
        .description("Shows hostile mob count.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showPassive = sgGeneral.add(new BoolSetting.Builder()
        .name("show-passive")
        .description("Shows passive mob count.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showTotal = sgGeneral.add(new BoolSetting.Builder()
        .name("show-total")
        .description("Shows total entity count.")
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

    public EntityCountHud() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        if (mc.world == null) {
            String text = "Entities: 0";
            renderer.text(text, x, y, titleColor.get(), textShadow.get());
            setSize(renderer.textWidth(text), renderer.textHeight());
            return;
        }

        int players = 0;
        int hostile = 0;
        int passive = 0;
        int total = 0;

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            
            if (entity instanceof PlayerEntity) players++;
            else if (entity instanceof HostileEntity) hostile++;
            else if (entity instanceof PassiveEntity) passive++;
            
            total++;
        }

        double y = this.y;
        double width = 0;

        if (showPlayers.get()) {
            String titleText = "Players: ";
            double x1 = renderer.text(titleText, x, y, titleColor.get(), textShadow.get());
            String valueText = String.valueOf(players);
            renderer.text(valueText, x1, y, valueColor.get(), textShadow.get());
            width = Math.max(width, renderer.textWidth(titleText + valueText));
            y += renderer.textHeight() + 2;
        }

        if (showHostile.get()) {
            String titleText = "Hostile: ";
            double x1 = renderer.text(titleText, x, y, titleColor.get(), textShadow.get());
            String valueText = String.valueOf(hostile);
            renderer.text(valueText, x1, y, valueColor.get(), textShadow.get());
            width = Math.max(width, renderer.textWidth(titleText + valueText));
            y += renderer.textHeight() + 2;
        }

        if (showPassive.get()) {
            String titleText = "Passive: ";
            double x1 = renderer.text(titleText, x, y, titleColor.get(), textShadow.get());
            String valueText = String.valueOf(passive);
            renderer.text(valueText, x1, y, valueColor.get(), textShadow.get());
            width = Math.max(width, renderer.textWidth(titleText + valueText));
            y += renderer.textHeight() + 2;
        }

        if (showTotal.get()) {
            String titleText = "Total: ";
            double x1 = renderer.text(titleText, x, y, titleColor.get(), textShadow.get());
            String valueText = String.valueOf(total);
            renderer.text(valueText, x1, y, valueColor.get(), textShadow.get());
            width = Math.max(width, renderer.textWidth(titleText + valueText));
            y += renderer.textHeight() + 2;
        }

        setSize(width, y - this.y);
    }
}
