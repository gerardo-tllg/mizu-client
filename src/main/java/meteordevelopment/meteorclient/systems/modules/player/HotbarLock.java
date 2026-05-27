package meteordevelopment.meteorclient.systems.modules.player;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
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
        .onChanged(this::onPickaxeSlotChanged) // Add listener for changes
        .build()
    );

    private final ItemStack[] hotbarSnapshot = new ItemStack[9];
    private int checkTimer = 0;
    private boolean wasInventoryOpen = false;
    private boolean isEating = false;

    public HotbarLock() {
        super(Categories.Player, "hotbar-lock", "Locks your hotbar to prevent changes.");

        for (int i = 0; i < hotbarSnapshot.length; i++) {
            hotbarSnapshot[i] = new ItemStack(Items.AIR);
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
            hotbarSnapshot[i] = mc.player.getInventory().getStack(i).copy();
        }
    }

    private void checkHotbar() {

        ItemStack pickaxeStack = mc.player.getInventory().getStack(pickaxeSlot.get() - 1);
        boolean isPickaxeInSlot = isPickaxe(pickaxeStack);


        if (!isPickaxeInSlot) {
            for (int i = 9; i < 36; i++) { // Iterate through the inventory (slots 9-35)
                ItemStack stack = mc.player.getInventory().getStack(i);
                if (isPickaxe(stack)) {
                    InvUtils.move().from(i).toHotbar(pickaxeSlot.get() - 1);
                    break;
                }
            }
        }


        for (int i = 0; i < 9; i++) {
            ItemStack currentStack = mc.player.getInventory().getStack(i);

            if (ItemStack.areItemsAndComponentsEqual(currentStack, hotbarSnapshot[i])) continue;

            if (currentStack.getItem() == Items.TOTEM_OF_UNDYING || i >= 36 && i <= 39) continue;

            InvUtils.move().from(SlotUtils.indexToId(i)).to(SlotUtils.indexToId(i));
            mc.player.getInventory().setStack(i, hotbarSnapshot[i].copy());
        }
    }

    private void onPickaxeSlotChanged(int newSlot) {
        if (mc.player == null) return;

        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (isPickaxe(stack)) {

                InvUtils.move().from(i).toHotbar(newSlot - 1);
                break;
            }
        }
    }

    private boolean isPickaxe(ItemStack stack) {
        return stack.isIn(ItemTags.PICKAXES);
    }
}
