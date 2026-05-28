package meteordevelopment.meteorclient.systems.modules.player;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.SlotUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;

public class HotbarLock extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> checkDelay = sgGeneral.add(new IntSetting.Builder()
        .name("check-delay")
        .description("The delay in ticks to check for hotbar changes.")
        .defaultValue(5)
        .min(1)
        .sliderRange(1, 20)
        .build()
    );

    private final Setting<Integer> pickaxeSlot = sgGeneral.add(new IntSetting.Builder()
        .name("pickaxe-slot")
        .description("The slot to lock your pickaxe to (1-9).")
        .defaultValue(1)
        .min(1)
        .max(9)
        .sliderRange(1, 9)
        .onChanged(this::onPickaxeSlotChanged)
        .build()
    );

    private final ItemStack[] hotbarSnapshot = new ItemStack[9];
    private final ItemStack[] armorSnapshot = new ItemStack[4];
    private int checkTimer = 0;
    private boolean wasInventoryOpen = false;
    private boolean isEating = false;

    public HotbarLock() {
        super(Categories.Player, "hotbar-lock", "Locks your hotbar to prevent changes.");

        for (int i = 0; i < hotbarSnapshot.length; i++) {
            hotbarSnapshot[i] = new ItemStack(Items.AIR);
        }
        for (int i = 0; i < armorSnapshot.length; i++) {
            armorSnapshot[i] = new ItemStack(Items.AIR);
        }
    }

    @Override
    public void onActivate() {
        saveHotbar();
        checkTimer = 0;
        wasInventoryOpen = false;
        isEating = false;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null) return;

        if (mc.player.isUsingItem()) {
            isEating = true;
            return;
        } else {
            isEating = false;
        }

        if (isEating) return;

        boolean isInventoryOpen = mc.currentScreen != null;

        if (wasInventoryOpen && !isInventoryOpen) {
            saveHotbar();
        }

        wasInventoryOpen = isInventoryOpen;

        if (isInventoryOpen) return;

        if (checkTimer <= 0) {
            checkTimer = checkDelay.get();
            checkHotbar();
        } else {
            checkTimer--;
        }
    }

    private void saveHotbar() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            // Don't snapshot armor items - allow them to be freely equipped/moved
            if (isArmorItem(stack)) {
                hotbarSnapshot[i] = new ItemStack(Items.AIR);
            } else {
                hotbarSnapshot[i] = stack.copy();
            }
        }
        for (int i = 0; i < 4; i++) {
            armorSnapshot[i] = mc.player.getInventory().getStack(36 + i).copy();
        }
    }

    private void checkHotbar() {
        ItemStack pickaxeStack = mc.player.getInventory().getStack(pickaxeSlot.get() - 1);
        boolean isPickaxeInSlot = pickaxeStack.isIn(ItemTags.PICKAXES);

        if (!isPickaxeInSlot) {
            for (int i = 9; i < 36; i++) { // Iterate through the inventory (slots 9-35)
                ItemStack stack = mc.player.getInventory().getStack(i);
                if (stack.isIn(ItemTags.PICKAXES)) {
                    InvUtils.move().from(i).toHotbar(pickaxeSlot.get() - 1);
                    break;
                }
            }
        }

        // If any armor slot changed (e.g. ChestSwap equipped/unequipped elytra), accept the new
        // hotbar state rather than fighting against armor-swapping modules.
        for (int i = 0; i < 4; i++) {
            if (!ItemStack.areItemsAndComponentsEqual(mc.player.getInventory().getStack(36 + i), armorSnapshot[i])) {
                saveHotbar();
                return;
            }
        }

        // Check if any armor items are in the hotbar - if so, skip restoration to avoid interference
        for (int i = 0; i < 9; i++) {
            if (isArmorItem(mc.player.getInventory().getStack(i))) {
                return;
            }
        }

        for (int i = 0; i < 9; i++) {
            ItemStack currentStack = mc.player.getInventory().getStack(i);

            if (ItemStack.areItemsAndComponentsEqual(currentStack, hotbarSnapshot[i])) continue;

            if (currentStack.getItem() == Items.TOTEM_OF_UNDYING) continue;

            mc.player.getInventory().setStack(i, hotbarSnapshot[i].copy());
        }
    }

    private boolean isArmorItem(ItemStack stack) {
        return stack.getItem() == Items.ELYTRA ||
            isChestplate(stack) ||
            isHelmet(stack) ||
            isLeggings(stack) ||
            isBoots(stack);
    }

    private boolean isChestplate(ItemStack stack) {
        return stack.getItem() == Items.LEATHER_CHESTPLATE ||
            stack.getItem() == Items.IRON_CHESTPLATE ||
            stack.getItem() == Items.GOLDEN_CHESTPLATE ||
            stack.getItem() == Items.DIAMOND_CHESTPLATE ||
            stack.getItem() == Items.NETHERITE_CHESTPLATE;
    }

    private boolean isHelmet(ItemStack stack) {
        return stack.getItem() == Items.LEATHER_HELMET ||
            stack.getItem() == Items.IRON_HELMET ||
            stack.getItem() == Items.GOLDEN_HELMET ||
            stack.getItem() == Items.DIAMOND_HELMET ||
            stack.getItem() == Items.NETHERITE_HELMET;
    }

    private boolean isLeggings(ItemStack stack) {
        return stack.getItem() == Items.LEATHER_LEGGINGS ||
            stack.getItem() == Items.IRON_LEGGINGS ||
            stack.getItem() == Items.GOLDEN_LEGGINGS ||
            stack.getItem() == Items.DIAMOND_LEGGINGS ||
            stack.getItem() == Items.NETHERITE_LEGGINGS;
    }

    private boolean isBoots(ItemStack stack) {
        return stack.getItem() == Items.LEATHER_BOOTS ||
            stack.getItem() == Items.IRON_BOOTS ||
            stack.getItem() == Items.GOLDEN_BOOTS ||
            stack.getItem() == Items.DIAMOND_BOOTS ||
            stack.getItem() == Items.NETHERITE_BOOTS;
    }

    private void onPickaxeSlotChanged(int newSlot) {
        if (mc.player == null) return;

        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isIn(ItemTags.PICKAXES)) {
                InvUtils.move().from(i).toHotbar(newSlot - 1);
                break;
            }
        }
    }
}
