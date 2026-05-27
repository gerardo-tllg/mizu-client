/*
 * This file is part of the Meteor Client distribution
 * (https://github.com/MeteorDevelopment/meteor-client). Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.combat;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.SlotUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.*;
import net.minecraft.block.entity.Hopper;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import net.minecraft.client.gui.screen.ingame.BookEditScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

import static meteordevelopment.orbit.EventPriority.HIGHEST;

public class Offhand extends Module {
    private final SettingGroup sgTotem;
    private final SettingGroup sgCombat;
    private final Setting<Integer> totemOffhandHealth;
    private final Setting<Boolean> antiGhost;
    private final Setting<Boolean> mainHandTotem;
    private final Setting<Integer> mainHandTotemSlot;
    private final Setting<Boolean> swordGapple;

    public Offhand() {
        super(Categories.Combat, "offhand", "Allows you to hold specified items in your offhand.");
        this.sgTotem = this.settings.createGroup("Totem");
        this.sgCombat = this.settings.createGroup("Combat");
        this.totemOffhandHealth = this.sgTotem.add(new IntSetting.Builder()
            .name("offhand-totem-health")
            .description("The health to force hold a totem at.")
            .defaultValue(10).range(0, 36).sliderMax(36).build());
        this.antiGhost = this.sgTotem.add(new BoolSetting.Builder()
            .name("anti-ghost")
            .description("Deletes your totem client side when you pop.")
            .defaultValue(true).build());
        this.mainHandTotem = this.sgTotem.add(new BoolSetting.Builder()
            .name("main-hand-totem")
            .description("Whether or not to hold a totem in your main hand.")
            .defaultValue(true).build());
        this.mainHandTotemSlot = this.sgTotem.add(new IntSetting.Builder()
            .name("main-hand-totem-slot")
            .description("The slot in your hotbar to hold your main hand totem.")
            .defaultValue(3).range(1, 9).visible(() -> mainHandTotem.get()).build());
        this.swordGapple = this.sgCombat.add(new BoolSetting.Builder()
            .name("sword-gapple")
            .description("Lets you right click while holding a sword to eat a golden apple.")
            .defaultValue(true).build());
    }

    @Override
    public void onActivate() {
    }

    @EventHandler(priority = HIGHEST + 999)
    private void onTick(TickEvent.Pre event) {
        if (mc.currentScreen == null || mc.currentScreen instanceof ChatScreen
                || mc.currentScreen instanceof AbstractSignEditScreen
                || mc.currentScreen instanceof BookEditScreen) {
            if (mainHandTotem.get()) {
                updateMainHandTotem();
            }
            updateOffhandSlot();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onReceivePacket(PacketEvent.Receive event) {
        if (!(event.packet instanceof EntityStatusS2CPacket p))
            return;
        if (p.getStatus() != 35)
            return;

        Entity entity = p.getEntity(mc.world);
        if (entity == null || !entity.equals(mc.player))
            return;

        if (antiGhost.get()) {
            mc.player.getInventory().removeStack(45);
            updateOffhandSlot();
        }
    }

    private void updateMainHandTotem() {
        FindItemResult totemResult = findTotem();
        if (!totemResult.found() || totemResult.isOffhand()) return;

        if (mc.player.getInventory().getStack(mainHandTotemSlot.get() - 1).getItem() != Items.TOTEM_OF_UNDYING) {
            mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId,
                SlotUtils.indexToId(totemResult.slot()), mainHandTotemSlot.get() - 1,
                SlotActionType.SWAP, mc.player);
        }
    }

    private void updateOffhandSlot() {
        boolean isLowHealth = mc.player.getHealth() + mc.player.getAbsorptionAmount()
            - PlayerUtils.possibleHealthReductions(true, true) <= totemOffhandHealth.get();
        boolean flying = mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() == Items.ELYTRA
            && mc.player.isGliding();

        if (!isLowHealth && !flying) {
            if (swordGapple.get() && mc.player.getMainHandStack().isIn(ItemTags.SWORDS)
                    && mc.options.useKey.isPressed()) {
                moveGappleToOffhand();
            } else {
                moveTotemToOffhand();
            }
        } else {
            moveTotemToOffhand();
        }
    }

    private void moveTotemToOffhand() {
        if (mc.player.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING) {
            FindItemResult totemResult = findTotem();
            if (totemResult.isHotbar()) {
                mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, SlotUtils.OFFHAND,
                    totemResult.slot(), SlotActionType.SWAP, mc.player);
                updateMainHandTotem();
            } else if (totemResult.found()) {
                int selectedSlot = mc.player.getInventory().selectedSlot;
                mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId,
                    totemResult.slot(), selectedSlot, SlotActionType.SWAP, mc.player);
                mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, SlotUtils.OFFHAND,
                    selectedSlot, SlotActionType.SWAP, mc.player);
                mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId,
                    totemResult.slot(), selectedSlot, SlotActionType.SWAP, mc.player);
            }
        }
    }

    private void moveGappleToOffhand() {
        if (mc.player.getOffHandStack().getItem() != Items.ENCHANTED_GOLDEN_APPLE) {
            if (!willInteractWithChestBlock()) {
                FindItemResult inventoryGappleResult = InvUtils.find(Items.ENCHANTED_GOLDEN_APPLE);
                if (inventoryGappleResult.found()) {
                    if (inventoryGappleResult.isHotbar()) {
                        mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId,
                            SlotUtils.OFFHAND, inventoryGappleResult.slot(), SlotActionType.SWAP, mc.player);
                    } else {
                        int selectedSlot = mc.player.getInventory().selectedSlot;
                        mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId,
                            inventoryGappleResult.slot(), selectedSlot, SlotActionType.SWAP, mc.player);
                        mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId,
                            SlotUtils.OFFHAND, selectedSlot, SlotActionType.SWAP, mc.player);
                        mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId,
                            inventoryGappleResult.slot(), selectedSlot, SlotActionType.SWAP, mc.player);
                    }
                }
            }
        }
    }

    private boolean willInteractWithChestBlock() {
        if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult) mc.crosshairTarget;
            BlockState blockState = mc.world.getBlockState(blockHitResult.getBlockPos());
            Block block = blockState.getBlock();
            if (block instanceof ShulkerBoxBlock || block instanceof AbstractChestBlock
                    || block instanceof EnderChestBlock || block instanceof TrappedChestBlock
                    || block instanceof FurnaceBlock || block instanceof AbstractFurnaceBlock
                    || block instanceof BlastFurnaceBlock || block instanceof DropperBlock
                    || block instanceof Hopper) {
                return true;
            }
        }
        return false;
    }

    private FindItemResult findTotem() {
        return InvUtils.find(x -> x.getItem().equals(Items.TOTEM_OF_UNDYING), 0, 35);
    }
}
