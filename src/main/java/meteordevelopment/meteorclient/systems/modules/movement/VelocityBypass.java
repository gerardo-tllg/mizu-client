/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.movement;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.CommonPongC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;

import java.util.LinkedList;
import java.util.Queue;

public class VelocityBypass extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Boolean> onVelo = sgGeneral.add(new BoolSetting.Builder()
        .name("on-velo")
        .description("Delay pong packets triggered by entity velocity updates.")
        .defaultValue(true)
        .build()
    );

    public final Setting<Boolean> onExplosion = sgGeneral.add(new BoolSetting.Builder()
        .name("on-explosion")
        .description("Delay pong packets triggered by explosion packets.")
        .defaultValue(false)
        .build()
    );

    public final Setting<Integer> cancelNextHowMany = sgGeneral.add(new IntSetting.Builder()
        .name("cancel-next-how-many")
        .description("How many pong packets to delay per velocity/explosion trigger.")
        .defaultValue(4)
        .min(1)
        .sliderMax(20)
        .build()
    );

    public final Setting<Boolean> send = sgGeneral.add(new BoolSetting.Builder()
        .name("send")
        .description("Flush all queued pong packets immediately once the delay counter reaches zero.")
        .defaultValue(false)
        .build()
    );

    private int nextHowMany = 0;
    private final Queue<Packet<?>> packetQueue = new LinkedList<>();

    public VelocityBypass() {
        super(Categories.Movement, "velocity-bypass",
              "Delays CommonPong C2S packets after velocity/explosion hits to bypass anti-cheat velocity checks.");
    }

    @Override
    public void onDeactivate() {
        sendAllPacketsInQueue();
    }

    @EventHandler(priority = EventPriority.HIGHEST + 1000)
    private void onReceivePacket(PacketEvent.Receive event) {
        if (onVelo.get()
                && event.packet instanceof EntityVelocityUpdateS2CPacket p
                && p.getEntityId() == mc.player.getId()) {
            nextHowMany += cancelNextHowMany.get();
        } else if (onExplosion.get() && event.packet instanceof ExplosionS2CPacket) {
            nextHowMany += cancelNextHowMany.get();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST + 1000)
    private void onSendPacket(PacketEvent.Send event) {
        if (!(event.packet instanceof CommonPongC2SPacket)) return;

        if (nextHowMany > 0) {
            event.cancel();
            synchronized (packetQueue) {
                packetQueue.add(event.packet);
            }
            nextHowMany--;
        } else if (send.get()) {
            sendAllPacketsInQueue();
        }
    }

    private void sendAllPacketsInQueue() {
        synchronized (packetQueue) {
            while (!packetQueue.isEmpty()) {
                Packet<?> packet = packetQueue.poll();
                if (mc.player != null && mc.player.networkHandler != null) {
                    mc.player.networkHandler.sendPacket(packet);
                }
            }
        }
        nextHowMany = 0;
    }
}
