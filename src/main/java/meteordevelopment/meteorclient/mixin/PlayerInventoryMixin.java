/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.mixininterface.IPlayerInventory;
import net.minecraft.entity.EntityEquipment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin implements IPlayerInventory {
    // In 1.21.5, armor moved to EntityEquipment - access via equipment field
    @Shadow @Final private EntityEquipment equipment;
    @Shadow public abstract DefaultedList<ItemStack> getMainStacks();

    @Override
    public DefaultedList<ItemStack> meteor$getArmor() {
        // In 1.21.5, armor is stored in EntityEquipment, not as a list
        // Create a DefaultedList for backwards compatibility
        DefaultedList<ItemStack> armor = DefaultedList.ofSize(4, ItemStack.EMPTY);
        armor.set(0, equipment.get(EquipmentSlot.FEET));
        armor.set(1, equipment.get(EquipmentSlot.LEGS));
        armor.set(2, equipment.get(EquipmentSlot.CHEST));
        armor.set(3, equipment.get(EquipmentSlot.HEAD));
        return armor;
    }

    @Override
    public DefaultedList<ItemStack> meteor$getMain() {
        return getMainStacks();
    }
}
