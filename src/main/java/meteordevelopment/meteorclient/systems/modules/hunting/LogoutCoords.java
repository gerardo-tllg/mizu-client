/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.hunting;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

/**
 * LogoutCoords — disconnects when the player reaches target XZ (±Y) coordinates.
 *
 * Useful for long AFK journeys: set your target before going to bed and the
 * client will disconnect automatically on arrival so you are not standing still
 * in the open.
 */
public class LogoutCoords extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // ── Target ───────────────────────────────────────────────────────────────
    private final Setting<Double> targetX = sgGeneral.add(new DoubleSetting.Builder()
        .name("target-x")
        .description("Target X coordinate.")
        .defaultValue(0.0)
        .build()
    );

    private final Setting<Double> targetZ = sgGeneral.add(new DoubleSetting.Builder()
        .name("target-z")
        .description("Target Z coordinate.")
        .defaultValue(0.0)
        .build()
    );

    private final Setting<Double> radius = sgGeneral.add(new DoubleSetting.Builder()
        .name("radius")
        .description("Disconnect when horizontal distance to target is within this many blocks.")
        .defaultValue(50.0)
        .min(1.0)
        .sliderRange(1.0, 500.0)
        .build()
    );

    private final Setting<Boolean> checkY = sgGeneral.add(new BoolSetting.Builder()
        .name("check-y")
        .description("Also require the player to be within Y radius of the target Y before disconnecting.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Double> targetY = sgGeneral.add(new DoubleSetting.Builder()
        .name("target-y")
        .description("Target Y coordinate.")
        .defaultValue(64.0)
        .visible(checkY::get)
        .build()
    );

    private final Setting<Double> yRadius = sgGeneral.add(new DoubleSetting.Builder()
        .name("y-radius")
        .description("Y distance threshold.")
        .defaultValue(10.0)
        .min(1.0)
        .sliderRange(1.0, 64.0)
        .visible(checkY::get)
        .build()
    );

    // ── Alert ────────────────────────────────────────────────────────────────
    private final Setting<Boolean> soundAlert = sgGeneral.add(new BoolSetting.Builder()
        .name("sound-alert")
        .description("Play a sound immediately before disconnecting.")
        .defaultValue(true)
        .build()
    );

    private final Setting<String> message = sgGeneral.add(new StringSetting.Builder()
        .name("message")
        .description("Client-side message to display before disconnecting.")
        .defaultValue("Reached destination")
        .build()
    );

    public LogoutCoords() {
        super(Categories.Hunting,
            "logout-coords",
            "Disconnects when you reach the configured XZ coordinates. Set it and go AFK.");
    }

    @Override
    public void onActivate() {
        if (mc.player == null) return;
        double dist = horizontalDistance();
        info("Monitoring — target (§b%.0f§r, §b%.0f§r), current distance §e%.0f§r blocks.",
            targetX.get(), targetZ.get(), dist);
    }

    // ── Tick ──────────────────────────────────────────────────────────────────

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.world == null) return;

        double hDist = horizontalDistance();
        if (hDist > radius.get()) return;

        // Optional Y check
        if (checkY.get()) {
            double yDist = Math.abs(mc.player.getY() - targetY.get());
            if (yDist > yRadius.get()) return;
        }

        // Arrived — alert, notify, disconnect
        if (soundAlert.get()) {
            mc.world.playSoundFromEntity(
                mc.player, mc.player,
                SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(),
                SoundCategory.BLOCKS, 2.0f, 1.0f
            );
        }

        info("§a" + message.get() + "§r — disconnecting.");
        toggle(); // disable first so the module does not re-fire on reconnect

        mc.player.networkHandler.onDisconnect(
            new DisconnectS2CPacket(Text.literal("[LogoutCoords] " + message.get()))
        );
    }

    // ── Info string ───────────────────────────────────────────────────────────

    @Override
    public String getInfoString() {
        if (mc.player == null) return null;
        double dist = horizontalDistance();
        return dist >= 1000.0
            ? String.format("%.1fk", dist / 1000.0)
            : String.format("%.0f", dist);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private double horizontalDistance() {
        if (mc.player == null) return Double.MAX_VALUE;
        double dx = mc.player.getX() - targetX.get();
        double dz = mc.player.getZ() - targetZ.get();
        return Math.sqrt(dx * dx + dz * dz);
    }
}
