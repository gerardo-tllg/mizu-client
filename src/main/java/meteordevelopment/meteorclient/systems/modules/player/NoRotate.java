/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.player;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.mixin.PlayerPositionLookS2CPacketAccessor;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerPosition;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;

public class NoRotate extends Module {
    public NoRotate() {
        super(Categories.Player, "no-rotate", "Attempts to block rotations sent from server to client.");
    }

    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        if (event.packet instanceof PlayerPositionLookS2CPacket packet) {
            PlayerPosition change = packet.change();

            float newYaw;
            float newPitch;

            if (packet.relatives().contains(PositionFlag.Y_ROT)) {
                newYaw = 0.0F;
            } else {
                newYaw = mc.player.getYaw();
            }

            if (packet.relatives().contains(PositionFlag.X_ROT)) {
                newPitch = 0.0F;
            } else {
                newPitch = mc.player.getPitch();
            }

            PlayerPosition newChange = new PlayerPosition(
                change.position(),
                change.deltaMovement(),
                newYaw,
                newPitch
            );
            ((PlayerPositionLookS2CPacketAccessor) (Object) packet).setChange(newChange);
        }
    }
}
