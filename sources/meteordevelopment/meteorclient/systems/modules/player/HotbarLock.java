package meteordevelopment.meteorclient.systems.modules.player;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_3489;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/player/HotbarLock.class */
public class HotbarLock extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Integer> checkDelay;
    private final Setting<Integer> pickaxeSlot;
    private final class_1799[] hotbarSnapshot;
    private final class_1799[] armorSnapshot;
    private int checkTimer;
    private boolean wasInventoryOpen;
    private boolean isEating;

    public HotbarLock() {
        super(Categories.Player, "hotbar-lock", "Locks your hotbar to prevent changes.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.checkDelay = this.sgGeneral.add(new IntSetting.Builder().name("check-delay").description("The delay in ticks to check for hotbar changes.").defaultValue(5).min(1).sliderRange(1, 20).build());
        this.pickaxeSlot = this.sgGeneral.add(new IntSetting.Builder().name("pickaxe-slot").description("The slot to lock your pickaxe to (1-9).").defaultValue(1).min(1).max(9).sliderRange(1, 9).onChanged((v1) -> {
            onPickaxeSlotChanged(v1);
        }).build());
        this.hotbarSnapshot = new class_1799[9];
        this.armorSnapshot = new class_1799[4];
        this.checkTimer = 0;
        this.wasInventoryOpen = false;
        this.isEating = false;
        for (int i = 0; i < this.hotbarSnapshot.length; i++) {
            this.hotbarSnapshot[i] = new class_1799(class_1802.field_8162);
        }
        for (int i2 = 0; i2 < this.armorSnapshot.length; i2++) {
            this.armorSnapshot[i2] = new class_1799(class_1802.field_8162);
        }
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        saveHotbar();
        this.checkTimer = 0;
        this.wasInventoryOpen = false;
        this.isEating = false;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.mc.field_1724 == null) {
            return;
        }
        if (this.mc.field_1724.method_6115()) {
            this.isEating = true;
            return;
        }
        this.isEating = false;
        if (this.isEating) {
            return;
        }
        boolean isInventoryOpen = this.mc.field_1755 != null;
        if (this.wasInventoryOpen && !isInventoryOpen) {
            saveHotbar();
        }
        this.wasInventoryOpen = isInventoryOpen;
        if (isInventoryOpen) {
            return;
        }
        if (this.checkTimer <= 0) {
            this.checkTimer = this.checkDelay.get().intValue();
            checkHotbar();
        } else {
            this.checkTimer--;
        }
    }

    private void saveHotbar() {
        for (int i = 0; i < 9; i++) {
            class_1799 stack = this.mc.field_1724.method_31548().method_5438(i);
            if (isArmorItem(stack)) {
                this.hotbarSnapshot[i] = new class_1799(class_1802.field_8162);
            } else {
                this.hotbarSnapshot[i] = stack.method_7972();
            }
        }
        for (int i2 = 0; i2 < 4; i2++) {
            this.armorSnapshot[i2] = this.mc.field_1724.method_31548().method_5438(36 + i2).method_7972();
        }
    }

    private void checkHotbar() {
        class_1799 pickaxeStack = this.mc.field_1724.method_31548().method_5438(this.pickaxeSlot.get().intValue() - 1);
        boolean isPickaxeInSlot = pickaxeStack.method_31573(class_3489.field_42614);
        if (!isPickaxeInSlot) {
            int i = 9;
            while (true) {
                if (i >= 36) {
                    break;
                }
                class_1799 stack = this.mc.field_1724.method_31548().method_5438(i);
                if (!stack.method_31573(class_3489.field_42614)) {
                    i++;
                } else {
                    InvUtils.move().from(i).toHotbar(this.pickaxeSlot.get().intValue() - 1);
                    break;
                }
            }
        }
        for (int i2 = 0; i2 < 4; i2++) {
            if (!class_1799.method_31577(this.mc.field_1724.method_31548().method_5438(36 + i2), this.armorSnapshot[i2])) {
                saveHotbar();
                return;
            }
        }
        for (int i3 = 0; i3 < 9; i3++) {
            if (isArmorItem(this.mc.field_1724.method_31548().method_5438(i3))) {
                return;
            }
        }
        for (int i4 = 0; i4 < 9; i4++) {
            class_1799 currentStack = this.mc.field_1724.method_31548().method_5438(i4);
            if (!class_1799.method_31577(currentStack, this.hotbarSnapshot[i4]) && currentStack.method_7909() != class_1802.field_8288) {
                this.mc.field_1724.method_31548().method_5447(i4, this.hotbarSnapshot[i4].method_7972());
            }
        }
    }

    private boolean isArmorItem(class_1799 stack) {
        return stack.method_7909() == class_1802.field_8833 || isChestplate(stack) || isHelmet(stack) || isLeggings(stack) || isBoots(stack);
    }

    private boolean isChestplate(class_1799 stack) {
        return stack.method_7909() == class_1802.field_8577 || stack.method_7909() == class_1802.field_8523 || stack.method_7909() == class_1802.field_8678 || stack.method_7909() == class_1802.field_8058 || stack.method_7909() == class_1802.field_22028;
    }

    private boolean isHelmet(class_1799 stack) {
        return stack.method_7909() == class_1802.field_8267 || stack.method_7909() == class_1802.field_8743 || stack.method_7909() == class_1802.field_8862 || stack.method_7909() == class_1802.field_8805 || stack.method_7909() == class_1802.field_22027;
    }

    private boolean isLeggings(class_1799 stack) {
        return stack.method_7909() == class_1802.field_8570 || stack.method_7909() == class_1802.field_8396 || stack.method_7909() == class_1802.field_8416 || stack.method_7909() == class_1802.field_8348 || stack.method_7909() == class_1802.field_22029;
    }

    private boolean isBoots(class_1799 stack) {
        return stack.method_7909() == class_1802.field_8370 || stack.method_7909() == class_1802.field_8660 || stack.method_7909() == class_1802.field_8753 || stack.method_7909() == class_1802.field_8285 || stack.method_7909() == class_1802.field_22030;
    }

    private void onPickaxeSlotChanged(int newSlot) {
        if (this.mc.field_1724 == null) {
            return;
        }
        for (int i = 0; i < 36; i++) {
            class_1799 stack = this.mc.field_1724.method_31548().method_5438(i);
            if (stack.method_31573(class_3489.field_42614)) {
                InvUtils.move().from(i).toHotbar(newSlot - 1);
                return;
            }
        }
    }
}
