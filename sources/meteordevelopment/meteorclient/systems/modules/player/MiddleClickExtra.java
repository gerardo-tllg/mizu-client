package meteordevelopment.meteorclient.systems.modules.player;

import meteordevelopment.meteorclient.events.entity.player.FinishUsingItemEvent;
import meteordevelopment.meteorclient.events.entity.player.StoppedUsingItemEvent;
import meteordevelopment.meteorclient.events.meteor.MouseButtonEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friend;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1268;
import net.minecraft.class_1657;
import net.minecraft.class_1753;
import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_2868;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/player/MiddleClickExtra.class */
public class MiddleClickExtra extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Mode> mode;
    private final Setting<Boolean> message;
    private final Setting<Boolean> quickSwap;
    private final Setting<Boolean> swapBack;
    private final Setting<Boolean> notify;
    private final Setting<Boolean> rocketInAir;
    private boolean isUsing;
    private boolean wasHeld;
    private int itemSlot;
    private int selectedSlot;

    public MiddleClickExtra() {
        super(Categories.Player, "middle-click-extra", "Perform various actions when you middle click.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.mode = this.sgGeneral.add(new EnumSetting.Builder().name("mode").description("Which item to use when you middle click.").defaultValue(Mode.Pearl).build());
        this.message = this.sgGeneral.add(new BoolSetting.Builder().name("message").description("Sends a message to the player when you add them as a friend.").defaultValue(false).visible(() -> {
            return this.mode.get() == Mode.AddFriend;
        }).build());
        this.quickSwap = this.sgGeneral.add(new BoolSetting.Builder().name("quick-swap").description("Allows you to use items in your inventory by simulating hotbar key presses. May get flagged by anticheats.").defaultValue(false).visible(() -> {
            return this.mode.get() != Mode.AddFriend;
        }).build());
        this.swapBack = this.sgGeneral.add(new BoolSetting.Builder().name("swap-back").description("Swap back to your original slot when you finish using an item.").defaultValue(false).visible(() -> {
            return (this.mode.get() == Mode.AddFriend || this.quickSwap.get().booleanValue()) ? false : true;
        }).build());
        this.notify = this.sgGeneral.add(new BoolSetting.Builder().name("notify").description("Notifies you when you do not have the specified item in your hotbar.").defaultValue(true).visible(() -> {
            return this.mode.get() != Mode.AddFriend;
        }).build());
        this.rocketInAir = this.sgGeneral.add(new BoolSetting.Builder().name("rocket-in-air").description("Uses a rocket when flying.").defaultValue(true).build());
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onDeactivate() {
        stopIfUsing(false);
    }

    @EventHandler
    private void onMouseButton(MouseButtonEvent event) {
        if (event.action == KeyAction.Press && event.button == 2 && this.mc.field_1755 == null) {
            if (this.rocketInAir.get().booleanValue() && this.mc.field_1724.method_6128()) {
                FindItemResult result = InvUtils.find(class_1802.field_8639);
                if (!result.found() || (!result.isHotbar() && !this.quickSwap.get().booleanValue())) {
                    if (this.notify.get().booleanValue()) {
                        warning("Unable to find specified item.", new Object[0]);
                        return;
                    }
                    return;
                }
                this.selectedSlot = this.mc.field_1724.method_31548().field_7545;
                this.itemSlot = result.slot();
                this.wasHeld = result.isMainHand();
                if (!this.wasHeld) {
                    if (this.quickSwap.get().booleanValue()) {
                        InvUtils.quickSwap().fromId(this.selectedSlot).to(this.itemSlot);
                    } else {
                        InvUtils.swap(result.slot(), this.swapBack.get().booleanValue());
                    }
                }
                if (this.mode.get().immediate) {
                    this.mc.field_1761.method_2919(this.mc.field_1724, class_1268.field_5808);
                    swapBack(false);
                }
                event.cancel();
                return;
            }
            if (this.mode.get() == Mode.AddFriend) {
                if (this.mc.field_1692 != null) {
                    class_1657 class_1657Var = this.mc.field_1692;
                    if (class_1657Var instanceof class_1657) {
                        class_1657 player = class_1657Var;
                        if (!Friends.get().isFriend(player)) {
                            Friends.get().add(new Friend(player));
                            info("Added %s to friends", player.method_5477().getString());
                            if (this.message.get().booleanValue()) {
                                ChatUtils.sendPlayerMsg("/msg " + String.valueOf(player.method_5477()) + " I just friended you on Meteor.");
                            }
                        } else {
                            Friends.get().remove(Friends.get().get(player));
                            info("Removed %s from friends", player.method_5477().getString());
                        }
                        event.cancel();
                        return;
                    }
                    return;
                }
                return;
            }
            FindItemResult result2 = InvUtils.find(this.mode.get().item);
            if (!result2.found() || (!result2.isHotbar() && !this.quickSwap.get().booleanValue())) {
                if (this.notify.get().booleanValue()) {
                    warning("Unable to find specified item.", new Object[0]);
                    return;
                }
                return;
            }
            this.selectedSlot = this.mc.field_1724.method_31548().field_7545;
            this.itemSlot = result2.slot();
            this.wasHeld = result2.isMainHand();
            if (!this.wasHeld) {
                if (this.quickSwap.get().booleanValue()) {
                    InvUtils.quickSwap().fromId(this.selectedSlot).to(this.itemSlot);
                } else {
                    InvUtils.swap(result2.slot(), this.swapBack.get().booleanValue());
                }
            }
            if (this.mode.get().immediate) {
                this.mc.field_1761.method_2919(this.mc.field_1724, class_1268.field_5808);
                swapBack(false);
            } else {
                this.mc.field_1690.field_1904.method_23481(true);
                this.isUsing = true;
            }
            event.cancel();
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.isUsing) {
            boolean pressed = true;
            if (this.mc.field_1724.method_6030().method_7909() instanceof class_1753) {
                pressed = class_1753.method_7722(this.mc.field_1724.method_6048()) < 1.0f;
            }
            this.mc.field_1690.field_1904.method_23481(pressed);
        }
    }

    @EventHandler
    private void onPacketSendEvent(PacketEvent.Send event) {
        if (event.packet instanceof class_2868) {
            stopIfUsing(true);
        }
    }

    @EventHandler
    private void onStoppedUsingItem(StoppedUsingItemEvent event) {
        stopIfUsing(false);
    }

    @EventHandler
    private void onFinishUsingItem(FinishUsingItemEvent event) {
        stopIfUsing(false);
    }

    private void stopIfUsing(boolean wasCancelled) {
        if (this.isUsing) {
            swapBack(wasCancelled);
            this.mc.field_1690.field_1904.method_23481(false);
            this.isUsing = false;
        }
    }

    void swapBack(boolean wasCancelled) {
        if (!this.wasHeld) {
            if (this.quickSwap.get().booleanValue()) {
                InvUtils.quickSwap().fromId(this.selectedSlot).to(this.itemSlot);
            } else {
                if (!this.swapBack.get().booleanValue() || wasCancelled) {
                    return;
                }
                InvUtils.swapBack();
            }
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/player/MiddleClickExtra$Mode.class */
    public enum Mode {
        Pearl(class_1802.field_8634, true),
        XP(class_1802.field_8287, true),
        Rocket(class_1802.field_8639, true),
        Bow(class_1802.field_8102, false),
        Gap(class_1802.field_8463, false),
        EGap(class_1802.field_8367, false),
        Chorus(class_1802.field_8233, false),
        AddFriend(null, true);

        private final class_1792 item;
        private final boolean immediate;

        Mode(class_1792 item, boolean immediate) {
            this.item = item;
            this.immediate = immediate;
        }
    }
}
