package meteordevelopment.meteorclient.systems.modules.player;

import java.util.List;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.ItemStackAccessor;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ItemListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/player/AutoReplenish.class */
public class AutoReplenish extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Integer> threshold;
    private final Setting<Integer> tickDelay;
    private final Setting<Boolean> offhand;
    private final Setting<Boolean> unstackable;
    private final Setting<Boolean> searchHotbar;
    private final Setting<List<class_1792>> excludedItems;
    private final class_1799[] items;
    private boolean prevHadOpenScreen;
    private int tickDelayLeft;

    public AutoReplenish() {
        super(Categories.Player, "auto-replenish", "Automatically refills items in your hotbar, main hand, or offhand.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.threshold = this.sgGeneral.add(new IntSetting.Builder().name("threshold").description("The threshold of items left this actives at.").defaultValue(8).min(1).sliderRange(1, 63).build());
        this.tickDelay = this.sgGeneral.add(new IntSetting.Builder().name("delay").description("The tick delay to replenish your hotbar.").defaultValue(1).min(0).build());
        this.offhand = this.sgGeneral.add(new BoolSetting.Builder().name("offhand").description("Whether or not to refill your offhand with items.").defaultValue(true).build());
        this.unstackable = this.sgGeneral.add(new BoolSetting.Builder().name("unstackable").description("Replenishes unstackable items.").defaultValue(true).build());
        this.searchHotbar = this.sgGeneral.add(new BoolSetting.Builder().name("search-hotbar").description("Uses items in your hotbar to replenish if they are the only ones left.").defaultValue(true).build());
        this.excludedItems = this.sgGeneral.add(new ItemListSetting.Builder().name("excluded-items").description("Items that WILL NOT replenish.").build());
        this.items = new class_1799[10];
        for (int i = 0; i < this.items.length; i++) {
            this.items[i] = new class_1799(class_1802.field_8162);
        }
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        fillItems();
        this.tickDelayLeft = this.tickDelay.get().intValue();
        this.prevHadOpenScreen = this.mc.field_1755 != null;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.mc.field_1755 == null && this.prevHadOpenScreen) {
            fillItems();
        }
        this.prevHadOpenScreen = this.mc.field_1755 != null;
        if (this.mc.field_1724.field_7512.method_7602().size() == 46 && this.mc.field_1755 == null) {
            if (this.tickDelayLeft <= 0) {
                this.tickDelayLeft = this.tickDelay.get().intValue();
                for (int i = 0; i < 9; i++) {
                    class_1799 stack = this.mc.field_1724.method_31548().method_5438(i);
                    checkSlot(i, stack);
                }
                if (this.offhand.get().booleanValue()) {
                    class_1799 stack2 = this.mc.field_1724.method_6079();
                    checkSlot(45, stack2);
                    return;
                }
                return;
            }
            this.tickDelayLeft--;
        }
    }

    private void checkSlot(int slot, class_1799 stack) {
        class_1799 prevStack = getItem(slot);
        if (!stack.method_7960() && stack.method_7946() && !this.excludedItems.get().contains(stack.method_7909()) && stack.method_7947() <= this.threshold.get().intValue()) {
            addSlots(slot, findItem(stack, slot, (this.threshold.get().intValue() - stack.method_7947()) + 1));
        }
        if (stack.method_7960() && !prevStack.method_7960() && !this.excludedItems.get().contains(prevStack.method_7909())) {
            if (prevStack.method_7946()) {
                addSlots(slot, findItem(prevStack, slot, (this.threshold.get().intValue() - stack.method_7947()) + 1));
            } else if (this.unstackable.get().booleanValue()) {
                addSlots(slot, findItem(prevStack, slot, 1));
            }
        }
        setItem(slot, stack);
    }

    private int findItem(class_1799 itemStack, int excludedSlot, int goodEnoughCount) {
        int slot = -1;
        int count = 0;
        int i = 35;
        while (true) {
            if (i < (this.searchHotbar.get().booleanValue() ? 0 : 9)) {
                break;
            }
            class_1799 stack = this.mc.field_1724.method_31548().method_5438(i);
            if (i != excludedSlot && stack.method_7909() == itemStack.method_7909() && class_1799.method_31577(itemStack, stack) && stack.method_7947() > count) {
                slot = i;
                count = stack.method_7947();
                if (count >= goodEnoughCount) {
                    break;
                }
            }
            i--;
        }
        return slot;
    }

    private void addSlots(int to, int from) {
        InvUtils.move().from(from).to(to);
    }

    private void fillItems() {
        for (int i = 0; i < 9; i++) {
            setItem(i, this.mc.field_1724.method_31548().method_5438(i));
        }
        setItem(45, this.mc.field_1724.method_6079());
    }

    private class_1799 getItem(int slot) {
        if (slot == 45) {
            slot = 9;
        }
        return this.items[slot];
    }

    private void setItem(int slot, class_1799 stack) {
        if (slot == 45) {
            slot = 9;
        }
        ItemStackAccessor itemStackAccessor = this.items[slot];
        itemStackAccessor.setItem(stack.method_7909());
        itemStackAccessor.method_7939(stack.method_7947());
        itemStackAccessor.method_57365(stack.method_57353());
        if (stack.method_7960()) {
            itemStackAccessor.setItem(class_1802.field_8162);
        }
    }
}
