/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.combat;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IPlayerInteractEntityC2SPacket;
import meteordevelopment.meteorclient.mixininterface.IPlayerMoveC2SPacket;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.MaceItem;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

import java.util.Objects;

public class Criticals extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgMace;

    private final Setting<Mode> mode;
    private final Setting<Boolean> ka;
    private final Setting<Boolean> mace;
    private final Setting<Double> extraHeight;

    private PlayerInteractEntityC2SPacket attackPacket;
    private HandSwingC2SPacket swingPacket;
    private boolean sendPackets;
    private int sendTimer;

    public Criticals() {
        super(Categories.Combat, "criticals", "Performs critical attacks when you hit your target.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgMace = this.settings.createGroup("Mace");

        this.mode = this.sgGeneral.add(new EnumSetting.Builder<Mode>()
            .name("mode")
            .description("The mode on how Criticals will function.")
            .defaultValue(Mode.Packet)
            .build()
        );

        this.ka = this.sgGeneral.add(new BoolSetting.Builder()
            .name("only-killaura")
            .description("Only performs crits when using killaura.")
            .defaultValue(false)
            .visible(() -> this.mode.get() != Mode.None)
            .build()
        );

        this.mace = this.sgMace.add(new BoolSetting.Builder()
            .name("smash-attack")
            .description("Will always perform smash attacks when using a mace.")
            .defaultValue(true)
            .build()
        );

        Setting<Boolean> maceRef = this.mace;
        Objects.requireNonNull(maceRef);
        this.extraHeight = this.sgMace.add(new DoubleSetting.Builder()
            .name("additional-height")
            .description("The amount of additional height to spoof. More height means more damage.")
            .defaultValue(0.0)
            .min(0.0)
            .sliderRange(0.0, 100.0)
            .visible(maceRef::get)
            .build()
        );
    }

    @Override
    public void onActivate() {
        this.attackPacket = null;
        this.swingPacket = null;
        this.sendPackets = false;
        this.sendTimer = 0;
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (event.packet instanceof IPlayerInteractEntityC2SPacket packet) {
            if (packet.meteor$getType() == PlayerInteractEntityC2SPacket.InteractType.ATTACK) {
                if (this.mace.get() && mc.player.getMainHandStack().getItem() instanceof MaceItem) {
                    if (mc.player.isGliding()) return;

                    this.sendPacket(0.0);
                    this.sendPacket(1.501 + this.extraHeight.get());
                    this.sendPacket(0.0);
                    return;
                }

                if (this.skipCrit()) return;

                Entity entity = packet.meteor$getEntity();
                if (!(entity instanceof LivingEntity) ||
                    (entity != Modules.get().get(KillAura.class).getTarget() && this.ka.get())) {
                    return;
                }

                switch (this.mode.get().ordinal()) {
                    case 1: // Packet
                        this.sendPacket(0.0625);
                        this.sendPacket(0.0);
                        return;
                    case 2: // Bypass
                        this.sendPacket(0.11);
                        this.sendPacket(0.1100013579);
                        this.sendPacket(1.3579E-6);
                        return;
                    case 3: // Jump
                    case 4: // MiniJump
                        if (!this.sendPackets) {
                            this.sendPackets = true;
                            this.sendTimer = this.mode.get() == Mode.Jump ? 6 : 4;
                            this.attackPacket = (PlayerInteractEntityC2SPacket) event.packet;

                            if (this.mode.get() == Mode.Jump) {
                                mc.player.jump();
                            } else {
                                ((IVec3d) mc.player.getVelocity()).meteor$setY(0.25);
                            }

                            event.cancel();
                        }
                        return;
                    default:
                        return;
                }
            }
        }

        if (event.packet instanceof HandSwingC2SPacket && this.mode.get() != Mode.Packet) {
            if (this.skipCrit()) return;

            if (this.sendPackets && this.swingPacket == null) {
                this.swingPacket = (HandSwingC2SPacket) event.packet;
                event.cancel();
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.sendPackets) {
            if (this.sendTimer <= 0) {
                this.sendPackets = false;

                if (this.attackPacket == null || this.swingPacket == null) return;

                mc.getNetworkHandler().sendPacket(this.attackPacket);
                mc.getNetworkHandler().sendPacket(this.swingPacket);

                this.attackPacket = null;
                this.swingPacket = null;
            } else {
                --this.sendTimer;
            }
        }
    }

    private void sendPacket(double height) {
        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();

        PlayerMoveC2SPacket packet = new PlayerMoveC2SPacket.PositionAndOnGround(x, y + height, z, false, mc.player.horizontalCollision);
        ((IPlayerMoveC2SPacket) packet).meteor$setTag(1337);

        mc.player.networkHandler.sendPacket(packet);
    }

    private boolean skipCrit() {
        return !mc.player.isOnGround() || mc.player.isSubmergedInWater() || mc.player.isInLava() || mc.player.isClimbing();
    }

    @Override
    public String getInfoString() {
        return this.mode.get().name();
    }

    public enum Mode {
        None,
        Packet,
        Bypass,
        Jump,
        MiniJump
    }
}
