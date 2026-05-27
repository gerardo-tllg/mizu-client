/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.player;

import java.util.List;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.ClientPlayerInteractionManagerAccessor;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;

import static net.minecraft.entity.effect.StatusEffects.HASTE;

public class SpeedMine extends Module {
    private final SettingGroup sgGeneral;
    public final Setting<Mode> mode;
    private final Setting<List<Block>> blocks;
    private final Setting<ListMode> blocksFilter;
    public final Setting<Double> modifier;
    private final Setting<Integer> hasteAmplifier;
    private final Setting<Boolean> instamine;
    private final Setting<Boolean> grimBypass;

    public SpeedMine() {
        super(Categories.Player, "speed-mine", "Allows you to quickly mine blocks.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.mode = this.sgGeneral.add(new EnumSetting.Builder<Mode>()
            .name("mode")
            .defaultValue(Mode.Damage)
            .onChanged(mode -> this.removeHaste())
            .build()
        );
        this.blocks = this.sgGeneral.add(new BlockListSetting.Builder()
            .name("blocks")
            .description("Selected blocks.")
            .filter(block -> block.getHardness() > 0.0F)
            .visible(() -> this.mode.get() != Mode.Haste)
            .build()
        );
        this.blocksFilter = this.sgGeneral.add(new EnumSetting.Builder<ListMode>()
            .name("blocks-filter")
            .description("How to use the blocks setting.")
            .defaultValue(ListMode.Blacklist)
            .visible(() -> this.mode.get() != Mode.Haste)
            .build()
        );
        this.modifier = this.sgGeneral.add(new DoubleSetting.Builder()
            .name("modifier")
            .description("Mining speed modifier. An additional value of 0.2 is equivalent to one haste level (1.2 = haste 1).")
            .defaultValue(1.4)
            .visible(() -> this.mode.get() == Mode.Normal)
            .min(0.0)
            .build()
        );
        this.hasteAmplifier = this.sgGeneral.add(new IntSetting.Builder()
            .name("haste-amplifier")
            .description("What value of haste to give you. Above 2 not recommended.")
            .defaultValue(2)
            .min(1)
            .visible(() -> this.mode.get() == Mode.Haste)
            .onChanged(i -> this.removeHaste())
            .build()
        );
        this.instamine = this.sgGeneral.add(new BoolSetting.Builder()
            .name("instamine")
            .description("Whether or not to instantly mine blocks under certain conditions.")
            .defaultValue(true)
            .visible(() -> this.mode.get() == Mode.Damage)
            .build()
        );
        this.grimBypass = this.sgGeneral.add(new BoolSetting.Builder()
            .name("grim-bypass")
            .description("Bypasses Grim's fastbreak check, working as of 2.3.58")
            .defaultValue(false)
            .visible(() -> this.mode.get() == Mode.Damage)
            .build()
        );
    }

    @Override
    public void onDeactivate() {
        this.removeHaste();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate()) return;

        if (this.mode.get() == Mode.Haste) {
            StatusEffectInstance haste = mc.player.getStatusEffect(HASTE);
            if (haste == null || haste.getAmplifier() <= this.hasteAmplifier.get() - 1) {
                mc.player.setStatusEffect(new StatusEffectInstance(HASTE, -1, this.hasteAmplifier.get() - 1, false, false, false), null);
            }
        } else if (this.mode.get() == Mode.Damage) {
            ClientPlayerInteractionManagerAccessor im = (ClientPlayerInteractionManagerAccessor) mc.interactionManager;
            float progress = im.getBreakingProgress();
            BlockPos pos = im.getCurrentBreakingBlockPos();

            if (pos == null || progress <= 0.0F) return;

            if (progress + mc.world.getBlockState(pos).calcBlockBreakingDelta(mc.player, mc.world, pos) >= 0.7F) {
                im.setCurrentBreakingProgress(1.0F);
            }
        }
    }

    @EventHandler
    private void onPacket(PacketEvent.Send event) {
        if (this.mode.get() == Mode.Damage && this.grimBypass.get()) {
            if (event.packet instanceof PlayerActionC2SPacket packet) {
                if (packet.getAction() == PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK) {
                    mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(
                        PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK,
                        packet.getPos().up(),
                        packet.getDirection()
                    ));
                }
            }
        }
    }

    private void removeHaste() {
        if (!Utils.canUpdate()) return;

        StatusEffectInstance haste = mc.player.getStatusEffect(HASTE);
        if (haste != null && !haste.shouldShowIcon()) {
            mc.player.removeStatusEffect(HASTE);
        }
    }

    public boolean filter(Block block) {
        if (this.blocksFilter.get() == ListMode.Blacklist && !this.blocks.get().contains(block)) return true;
        return this.blocksFilter.get() == ListMode.Whitelist && this.blocks.get().contains(block);
    }

    public boolean instamine() {
        return this.isActive() && this.mode.get() == Mode.Damage && this.instamine.get();
    }

    public enum Mode {
        Normal,
        Haste,
        Damage
    }

    public enum ListMode {
        Whitelist,
        Blacklist
    }
}
