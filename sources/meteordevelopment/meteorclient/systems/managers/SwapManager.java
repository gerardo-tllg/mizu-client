package meteordevelopment.meteorclient.systems.managers;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.config.AntiCheatConfig;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1268;
import net.minecraft.class_1792;
import net.minecraft.class_465;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/managers/SwapManager.class */
public class SwapManager {
    private final AntiCheatConfig antiCheatConfig = AntiCheatConfig.get();
    private final Object swapLock = new Object();
    private SwapState multiTickSwapState = new SwapState(this);
    private SwapState instantSwapState = new SwapState(this);

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/managers/SwapManager$SwapMode.class */
    public enum SwapMode {
        None,
        Auto,
        SilentHotbar,
        SilentSwap
    }

    public SwapManager() {
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    public boolean beginSwap(class_1792 item, boolean instant) {
        FindItemResult result = InvUtils.findInHotbar(item);
        if (getItemSwapMode() == SwapMode.None && !result.isMainHand()) {
            return false;
        }
        if (getItemSwapMode() == SwapMode.SilentHotbar && !result.found()) {
            return false;
        }
        if (!result.found()) {
            result = InvUtils.find(item);
        }
        if (result.found()) {
            return beginSwap(result, instant);
        }
        return false;
    }

    public boolean beginSwap(FindItemResult result, boolean instant) {
        if (!result.found()) {
            return false;
        }
        if (getItemSwapMode() == SwapMode.None && !result.isMainHand()) {
            return false;
        }
        if (!instant && MeteorClient.mc.field_1724.method_6115() && MeteorClient.mc.field_1724.method_6058() == class_1268.field_5808) {
            return false;
        }
        synchronized (this.swapLock) {
            if (this.instantSwapState.isSwapped) {
                return false;
            }
            if (this.multiTickSwapState.isSwapped && !instant) {
                return false;
            }
            getSwapState(instant).isSwapped = true;
            SwapState swapState = getSwapState(instant);
            switch (getItemSwapMode()) {
                case None:
                default:
                    return true;
                case Auto:
                    boolean shouldSilentSwap = !result.isHotbar() || (this.multiTickSwapState.isSwapped && instant) || (MeteorClient.mc.field_1724.method_6115() && MeteorClient.mc.field_1724.method_6058() == class_1268.field_5808);
                    if (shouldSilentSwap) {
                        if (this.antiCheatConfig.swapAntiScreenClose.get().booleanValue() && (MeteorClient.mc.field_1755 instanceof class_465)) {
                            getSwapState(instant).isSwapped = false;
                            return false;
                        }
                        swapState.silentSwapInventorySlot = result.slot();
                        swapState.silentSwapSelectedSlot = MeteorClient.mc.field_1724.method_31548().field_7545;
                        swapState.didSilentSwap = true;
                        InvUtils.quickSwap().fromId(MeteorClient.mc.field_1724.method_31548().field_7545).to(result.slot());
                        return true;
                    }
                    swapState.hotbarSelectedSlot = MeteorClient.mc.field_1724.method_31548().field_7545;
                    swapState.hotbarItemSlot = result.slot();
                    swapState.didSilentSwap = false;
                    MeteorClient.mc.field_1724.method_31548().field_7545 = result.slot();
                    MeteorClient.mc.field_1761.meteor$syncSelected();
                    return true;
                case SilentHotbar:
                    swapState.hotbarSelectedSlot = MeteorClient.mc.field_1724.method_31548().field_7545;
                    swapState.hotbarItemSlot = result.slot();
                    swapState.didSilentSwap = false;
                    MeteorClient.mc.field_1724.method_31548().field_7545 = result.slot();
                    MeteorClient.mc.field_1761.meteor$syncSelected();
                    return true;
                case SilentSwap:
                    if (this.antiCheatConfig.swapAntiScreenClose.get().booleanValue() && (MeteorClient.mc.field_1755 instanceof class_465)) {
                        getSwapState(instant).isSwapped = false;
                        return false;
                    }
                    swapState.silentSwapInventorySlot = result.slot();
                    swapState.silentSwapSelectedSlot = MeteorClient.mc.field_1724.method_31548().field_7545;
                    swapState.didSilentSwap = true;
                    InvUtils.quickSwap().fromId(MeteorClient.mc.field_1724.method_31548().field_7545).to(result.slot());
                    return true;
            }
        }
    }

    public boolean canSwap(class_1792 item) {
        return getSlot(item).found();
    }

    public FindItemResult getSlot(class_1792 item) {
        FindItemResult result = InvUtils.findInHotbar(item);
        if (getItemSwapMode() == SwapMode.None && !result.isMainHand()) {
            return new FindItemResult(-1, 0);
        }
        if (getItemSwapMode() == SwapMode.SilentHotbar && !result.found()) {
            return new FindItemResult(-1, 0);
        }
        if (!result.found()) {
            result = InvUtils.find(item);
        }
        return !result.found() ? new FindItemResult(-1, 0) : result;
    }

    public void endSwap(boolean instantSwap) {
        synchronized (this.swapLock) {
            if (instantSwap) {
                if (!getSwapState(instantSwap).isSwapped) {
                    return;
                }
            }
            SwapState swapState = getSwapState(instantSwap);
            if (swapState.didSilentSwap) {
                InvUtils.quickSwap().fromId(swapState.silentSwapSelectedSlot).to(swapState.silentSwapInventorySlot);
            } else {
                MeteorClient.mc.field_1724.method_31548().field_7545 = swapState.hotbarSelectedSlot;
                MeteorClient.mc.field_1761.meteor$syncSelected();
            }
            swapState.isSwapped = false;
        }
    }

    public SwapMode getItemSwapMode() {
        return this.antiCheatConfig.swapMode.get();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (MeteorClient.mc.field_1687 != null && MeteorClient.mc.field_1724 != null && this.multiTickSwapState.isSwapped) {
            if (this.multiTickSwapState.didSilentSwap) {
                if (this.multiTickSwapState.silentSwapSelectedSlot != MeteorClient.mc.field_1724.method_31548().field_7545) {
                    MeteorClient.mc.field_1724.method_31548().field_7545 = this.multiTickSwapState.silentSwapSelectedSlot;
                    MeteorClient.mc.field_1761.meteor$syncSelected();
                    return;
                }
                return;
            }
            if (this.multiTickSwapState.hotbarItemSlot != MeteorClient.mc.field_1724.method_31548().field_7545) {
                MeteorClient.mc.field_1724.method_31548().field_7545 = this.multiTickSwapState.hotbarItemSlot;
                MeteorClient.mc.field_1761.meteor$syncSelected();
            }
        }
    }

    private SwapState getSwapState(boolean instantSwap) {
        return instantSwap ? this.instantSwapState : this.multiTickSwapState;
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/managers/SwapManager$SwapState.class */
    private class SwapState {
        public boolean isSwapped = false;
        public boolean didSilentSwap = false;
        public int hotbarSelectedSlot = 0;
        public int hotbarItemSlot = 0;
        public int silentSwapSelectedSlot = 0;
        public int silentSwapInventorySlot = 0;

        private SwapState(SwapManager swapManager) {
        }
    }
}
