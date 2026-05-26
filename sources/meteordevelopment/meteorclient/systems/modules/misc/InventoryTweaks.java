package meteordevelopment.meteorclient.systems.modules.misc;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import meteordevelopment.meteorclient.events.entity.DropItemsEvent;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.meteorclient.events.meteor.MouseButtonEvent;
import meteordevelopment.meteorclient.events.packets.InventoryEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.HandledScreenAccessor;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ItemListSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.ScreenHandlerListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.InventorySorter;
import meteordevelopment.meteorclient.utils.player.SlotUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1703;
import net.minecraft.class_1735;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_2815;
import net.minecraft.class_3917;
import net.minecraft.class_465;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/misc/InventoryTweaks.class */
public class InventoryTweaks extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgSorting;
    private final SettingGroup sgAutoDrop;
    private final SettingGroup sgStealDump;
    private final SettingGroup sgAutoSteal;
    private final Setting<Boolean> mouseDragItemMove;
    private final Setting<List<class_1792>> antiDropItems;
    private final Setting<Boolean> xCarry;
    private final Setting<Boolean> sortingEnabled;
    private final Setting<Keybind> sortingKey;
    private final Setting<Integer> sortingDelay;
    private final Setting<Boolean> disableInCreative;
    private final Setting<List<class_1792>> autoDropItems;
    private final Setting<Boolean> autoDropExcludeEquipped;
    private final Setting<Boolean> autoDropExcludeHotbar;
    private final Setting<Boolean> autoDropOnlyFullStacks;
    public final Setting<List<class_3917<?>>> stealScreens;
    private final Setting<Boolean> buttons;
    private final Setting<Boolean> stealDrop;
    private final Setting<Boolean> dropBackwards;
    private final Setting<ListMode> dumpFilter;
    private final Setting<List<class_1792>> dumpItems;
    private final Setting<ListMode> stealFilter;
    private final Setting<List<class_1792>> stealItems;
    private final Setting<Boolean> autoSteal;
    private final Setting<Boolean> autoDump;
    private final Setting<Integer> autoStealDelay;
    private final Setting<Integer> autoStealInitDelay;
    private final Setting<Integer> autoStealRandomDelay;
    private InventorySorter sorter;
    private boolean invOpened;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/misc/InventoryTweaks$ListMode.class */
    public enum ListMode {
        Whitelist,
        Blacklist,
        None
    }

    public InventoryTweaks() {
        super(Categories.Misc, "inventory-tweaks", "Various inventory related utilities.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgSorting = this.settings.createGroup("Sorting");
        this.sgAutoDrop = this.settings.createGroup("Auto Drop");
        this.sgStealDump = this.settings.createGroup("Steal and Dump");
        this.sgAutoSteal = this.settings.createGroup("Auto Steal");
        this.mouseDragItemMove = this.sgGeneral.add(new BoolSetting.Builder().name("mouse-drag-item-move").description("Moving mouse over items while holding shift will transfer it to the other container.").defaultValue(true).build());
        this.antiDropItems = this.sgGeneral.add(new ItemListSetting.Builder().name("anti-drop-items").description("Items to prevent dropping. Doesn't work in creative inventory screen.").build());
        this.xCarry = this.sgGeneral.add(new BoolSetting.Builder().name("xcarry").description("Allows you to store four extra item stacks in your crafting grid.").defaultValue(true).onChanged(v -> {
            if (v.booleanValue() || !Utils.canUpdate()) {
                return;
            }
            this.mc.field_1724.field_3944.method_52787(new class_2815(this.mc.field_1724.field_7498.field_7763));
            this.invOpened = false;
        }).build());
        this.sortingEnabled = this.sgSorting.add(new BoolSetting.Builder().name("sorting-enabled").description("Automatically sorts stacks in inventory.").defaultValue(true).build());
        SettingGroup settingGroup = this.sgSorting;
        KeybindSetting.Builder builderDescription = new KeybindSetting.Builder().name("sorting-key").description("Key to trigger the sort.");
        Setting<Boolean> setting = this.sortingEnabled;
        Objects.requireNonNull(setting);
        this.sortingKey = settingGroup.add(builderDescription.visible(setting::get).defaultValue(Keybind.fromButton(2)).build());
        SettingGroup settingGroup2 = this.sgSorting;
        IntSetting.Builder builderDescription2 = new IntSetting.Builder().name("sorting-delay").description("Delay in ticks between moving items when sorting.");
        Setting<Boolean> setting2 = this.sortingEnabled;
        Objects.requireNonNull(setting2);
        this.sortingDelay = settingGroup2.add(builderDescription2.visible(setting2::get).defaultValue(1).min(0).build());
        SettingGroup settingGroup3 = this.sgSorting;
        BoolSetting.Builder builderDefaultValue = new BoolSetting.Builder().name("disable-in-creative").description("Disables the inventory sorter when in creative mode.").defaultValue(true);
        Setting<Boolean> setting3 = this.sortingEnabled;
        Objects.requireNonNull(setting3);
        this.disableInCreative = settingGroup3.add(builderDefaultValue.visible(setting3::get).build());
        this.autoDropItems = this.sgAutoDrop.add(new ItemListSetting.Builder().name("auto-drop-items").description("Items to drop.").build());
        this.autoDropExcludeEquipped = this.sgAutoDrop.add(new BoolSetting.Builder().name("exclude-equipped").description("Whether or not to drop items equipped in armor slots.").defaultValue(true).build());
        this.autoDropExcludeHotbar = this.sgAutoDrop.add(new BoolSetting.Builder().name("exclude-hotbar").description("Whether or not to drop items from your hotbar.").defaultValue(false).build());
        this.autoDropOnlyFullStacks = this.sgAutoDrop.add(new BoolSetting.Builder().name("only-full-stacks").description("Only drops the items if the stack is full.").defaultValue(false).build());
        this.stealScreens = this.sgStealDump.add(new ScreenHandlerListSetting.Builder().name("steal-screens").description("Select the screens to display buttons and auto steal.").defaultValue(List.of(class_3917.field_17326, class_3917.field_17327)).build());
        this.buttons = this.sgStealDump.add(new BoolSetting.Builder().name("inventory-buttons").description("Shows steal and dump buttons in container guis.").defaultValue(true).build());
        this.stealDrop = this.sgStealDump.add(new BoolSetting.Builder().name("steal-drop").description("Drop items to the ground instead of stealing them.").defaultValue(false).build());
        SettingGroup settingGroup4 = this.sgStealDump;
        BoolSetting.Builder builderDefaultValue2 = new BoolSetting.Builder().name("drop-backwards").description("Drop items behind you.").defaultValue(false);
        Setting<Boolean> setting4 = this.stealDrop;
        Objects.requireNonNull(setting4);
        this.dropBackwards = settingGroup4.add(builderDefaultValue2.visible(setting4::get).build());
        this.dumpFilter = this.sgStealDump.add(new EnumSetting.Builder().name("dump-filter").description("Dump mode.").defaultValue(ListMode.None).build());
        this.dumpItems = this.sgStealDump.add(new ItemListSetting.Builder().name("dump-items").description("Items to dump.").build());
        this.stealFilter = this.sgStealDump.add(new EnumSetting.Builder().name("steal-filter").description("Steal mode.").defaultValue(ListMode.None).build());
        this.stealItems = this.sgStealDump.add(new ItemListSetting.Builder().name("steal-items").description("Items to steal.").build());
        this.autoSteal = this.sgAutoSteal.add(new BoolSetting.Builder().name("auto-steal").description("Automatically removes all possible items when you open a container.").defaultValue(false).onChanged(val -> {
            checkAutoStealSettings();
        }).build());
        this.autoDump = this.sgAutoSteal.add(new BoolSetting.Builder().name("auto-dump").description("Automatically dumps all possible items when you open a container.").defaultValue(false).onChanged(val2 -> {
            checkAutoStealSettings();
        }).build());
        this.autoStealDelay = this.sgAutoSteal.add(new IntSetting.Builder().name("delay").description("The minimum delay between stealing the next stack in milliseconds.").defaultValue(20).sliderMax(1000).build());
        this.autoStealInitDelay = this.sgAutoSteal.add(new IntSetting.Builder().name("initial-delay").description("The initial delay before stealing in milliseconds. 0 to use normal delay instead.").defaultValue(50).sliderMax(1000).build());
        this.autoStealRandomDelay = this.sgAutoSteal.add(new IntSetting.Builder().name("random").description("Randomly adds a delay of up to the specified time in milliseconds.").min(0).sliderMax(1000).defaultValue(50).build());
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        this.invOpened = false;
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onDeactivate() {
        this.sorter = null;
        if (this.invOpened) {
            this.mc.field_1724.field_3944.method_52787(new class_2815(this.mc.field_1724.field_7498.field_7763));
        }
    }

    @EventHandler
    private void onKey(KeyEvent event) {
        if (event.action == KeyAction.Press && this.sortingKey.get().matches(true, event.key, event.modifiers) && sort()) {
            event.cancel();
        }
    }

    @EventHandler
    private void onMouseButton(MouseButtonEvent event) {
        if (event.action == KeyAction.Press && this.sortingKey.get().matches(false, event.button, 0) && sort()) {
            event.cancel();
        }
    }

    private boolean sort() {
        if (!this.sortingEnabled.get().booleanValue()) {
            return false;
        }
        HandledScreenAccessor handledScreenAccessor = this.mc.field_1755;
        if (!(handledScreenAccessor instanceof class_465)) {
            return false;
        }
        HandledScreenAccessor handledScreenAccessor2 = (class_465) handledScreenAccessor;
        if (this.sorter != null) {
            return false;
        }
        if (this.mc.field_1724.method_68878() && this.disableInCreative.get().booleanValue()) {
            return false;
        }
        if (!this.mc.field_1724.field_7512.method_34255().method_7960()) {
            FindItemResult empty = InvUtils.findEmpty();
            if (empty.found()) {
                InvUtils.click().slot(empty.slot());
            } else {
                InvUtils.click().slot(-999);
            }
        }
        class_1735 focusedSlot = handledScreenAccessor2.getFocusedSlot();
        if (focusedSlot == null) {
            return false;
        }
        this.sorter = new InventorySorter(handledScreenAccessor2, focusedSlot);
        return true;
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        this.sorter = null;
    }

    @EventHandler
    private void onTickPre(TickEvent.Pre event) {
        if (this.sorter == null || !this.sorter.tick(this.sortingDelay.get().intValue())) {
            return;
        }
        this.sorter = null;
    }

    @EventHandler
    private void onTickPost(TickEvent.Post event) {
        if (!Utils.canUpdate() || (this.mc.field_1755 instanceof class_465) || this.autoDropItems.get().isEmpty()) {
            return;
        }
        for (int i = this.autoDropExcludeHotbar.get().booleanValue() ? 9 : 0; i < this.mc.field_1724.method_31548().method_5439(); i++) {
            class_1799 itemStack = this.mc.field_1724.method_31548().method_5438(i);
            if (this.autoDropItems.get().contains(itemStack.method_7909()) && ((!this.autoDropOnlyFullStacks.get().booleanValue() || itemStack.method_7947() == itemStack.method_7914()) && (!this.autoDropExcludeEquipped.get().booleanValue() || !SlotUtils.isArmor(i)))) {
                InvUtils.drop().slot(i);
            }
        }
    }

    @EventHandler
    private void onDropItems(DropItemsEvent event) {
        if (this.antiDropItems.get().contains(event.itemStack.method_7909())) {
            event.cancel();
        }
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (this.xCarry.get().booleanValue()) {
            class_2815 class_2815Var = event.packet;
            if (class_2815Var instanceof class_2815) {
                class_2815 packet = class_2815Var;
                if (packet.method_36168() == this.mc.field_1724.field_7498.field_7763) {
                    this.invOpened = true;
                    event.cancel();
                }
            }
        }
    }

    private void checkAutoStealSettings() {
        if (this.autoSteal.get().booleanValue() && this.autoDump.get().booleanValue()) {
            error("You can't enable Auto Steal and Auto Dump at the same time!", new Object[0]);
            this.autoDump.set(false);
        }
    }

    private int getSleepTime() {
        return this.autoStealDelay.get().intValue() + (this.autoStealRandomDelay.get().intValue() > 0 ? ThreadLocalRandom.current().nextInt(0, this.autoStealRandomDelay.get().intValue()) : 0);
    }

    /* JADX WARN: Removed duplicated region for block: B:48:0x0122  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private void moveSlots(net.minecraft.class_1703 r7, int r8, int r9, boolean r10) {
        /*
            Method dump skipped, instruction units count: 385
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: meteordevelopment.meteorclient.systems.modules.misc.InventoryTweaks.moveSlots(net.minecraft.class_1703, int, int, boolean):void");
    }

    public void steal(class_1703 handler) {
        MeteorExecutor.execute(() -> {
            moveSlots(handler, 0, SlotUtils.indexToId(9), true);
        });
    }

    public void dump(class_1703 handler) {
        int playerInvOffset = SlotUtils.indexToId(9);
        MeteorExecutor.execute(() -> {
            moveSlots(handler, playerInvOffset, playerInvOffset + 36, false);
        });
    }

    public boolean showButtons() {
        return isActive() && this.buttons.get().booleanValue();
    }

    public boolean mouseDragItemMove() {
        return isActive() && this.mouseDragItemMove.get().booleanValue();
    }

    public boolean canSteal(class_1703 handler) {
        try {
            return this.stealScreens.get().contains(handler.method_17358());
        } catch (UnsupportedOperationException e) {
            return false;
        }
    }

    @EventHandler
    private void onInventory(InventoryEvent event) {
        class_1703 handler = this.mc.field_1724.field_7512;
        if (canSteal(handler) && event.packet.comp_3837() == handler.field_7763) {
            if (this.autoSteal.get().booleanValue()) {
                steal(handler);
            } else if (this.autoDump.get().booleanValue()) {
                dump(handler);
            }
        }
    }
}
