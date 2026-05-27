package meteordevelopment.meteorclient.systems.managers;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IClientPlayerInteractionManager;
import meteordevelopment.meteorclient.systems.config.AntiCheatConfig;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.Item;
import net.minecraft.util.Hand;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class SwapManager {
    private final AntiCheatConfig antiCheatConfig = AntiCheatConfig.get();
    private final Object swapLock = new Object();
    private SwapState multiTickSwapState = new SwapState();
    private SwapState instantSwapState = new SwapState();

    public SwapManager() {
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    public boolean beginSwap(Item item, boolean instant) {
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

        return !result.found() ? false : beginSwap(result, instant);
    }

    public boolean beginSwap(FindItemResult result, boolean instant) {
        if (!result.found()) {
            return false;
        }

        if (getItemSwapMode() == SwapMode.None && !result.isMainHand()) {
            return false;
        }

        if (!instant && mc.player.isUsingItem() && mc.player.getActiveHand() == Hand.MAIN_HAND) {
            return false;
        }

        synchronized (swapLock) {
            if (instantSwapState.isSwapped) {
                return false;
            }

            if (multiTickSwapState.isSwapped && !instant) {
                return false;
            }

            getSwapState(instant).isSwapped = true;
        }

        SwapState swapState = getSwapState(instant);

        switch (getItemSwapMode()) {
            case None -> {
            }
            case Auto -> {
                boolean shouldSilentSwap = !result.isHotbar()
                        || (multiTickSwapState.isSwapped && instant)
                        || (mc.player.isUsingItem() && mc.player.getActiveHand() == Hand.MAIN_HAND);

                if (shouldSilentSwap) {
                    if (antiCheatConfig.swapAntiScreenClose.get() && mc.currentScreen instanceof HandledScreen<?>) {
                        getSwapState(instant).isSwapped = false;
                        return false;
                    }

                    swapState.silentSwapInventorySlot = result.slot();
                    swapState.silentSwapSelectedSlot = mc.player.getInventory().selectedSlot;
                    swapState.didSilentSwap = true;

                    InvUtils.quickSwap().fromId(mc.player.getInventory().selectedSlot).to(result.slot());
                } else {
                    swapState.hotbarSelectedSlot = mc.player.getInventory().selectedSlot;
                    swapState.hotbarItemSlot = result.slot();
                    swapState.didSilentSwap = false;

                    mc.player.getInventory().selectedSlot = result.slot();
                    ((IClientPlayerInteractionManager) mc.interactionManager).meteor$syncSelected();
                }
            }
            case SilentHotbar -> {
                swapState.hotbarSelectedSlot = mc.player.getInventory().selectedSlot;
                swapState.hotbarItemSlot = result.slot();
                swapState.didSilentSwap = false;

                mc.player.getInventory().selectedSlot = result.slot();
                ((IClientPlayerInteractionManager) mc.interactionManager).meteor$syncSelected();
            }
            case SilentSwap -> {
                if (antiCheatConfig.swapAntiScreenClose.get() && mc.currentScreen instanceof HandledScreen<?>) {
                    getSwapState(instant).isSwapped = false;
                    return false;
                }

                swapState.silentSwapInventorySlot = result.slot();
                swapState.silentSwapSelectedSlot = mc.player.getInventory().selectedSlot;
                swapState.didSilentSwap = true;

                InvUtils.quickSwap().fromId(mc.player.getInventory().selectedSlot).to(result.slot());
            }
        }

        return true;
    }

    public boolean canSwap(Item item) {
        return getSlot(item).found();
    }

    public FindItemResult getSlot(Item item) {
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
        synchronized (swapLock) {
            if (instantSwap && !getSwapState(instantSwap).isSwapped) {
                return;
            }
        }

        SwapState swapState = getSwapState(instantSwap);

        if (swapState.didSilentSwap) {
            InvUtils.quickSwap().fromId(swapState.silentSwapSelectedSlot).to(swapState.silentSwapInventorySlot);
        } else {
            mc.player.getInventory().selectedSlot = swapState.hotbarSelectedSlot;
            ((IClientPlayerInteractionManager) mc.interactionManager).meteor$syncSelected();
        }

        swapState.isSwapped = false;
    }

    public SwapMode getItemSwapMode() {
        return antiCheatConfig.swapMode.get();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.world == null || mc.player == null) {
            return;
        }

        if (multiTickSwapState.isSwapped) {
            if (multiTickSwapState.didSilentSwap) {
                if (multiTickSwapState.silentSwapSelectedSlot != mc.player.getInventory().selectedSlot) {
                    mc.player.getInventory().selectedSlot = multiTickSwapState.silentSwapSelectedSlot;
                    ((IClientPlayerInteractionManager) mc.interactionManager).meteor$syncSelected();
                }
            } else {
                if (multiTickSwapState.hotbarItemSlot != mc.player.getInventory().selectedSlot) {
                    mc.player.getInventory().selectedSlot = multiTickSwapState.hotbarItemSlot;
                    ((IClientPlayerInteractionManager) mc.interactionManager).meteor$syncSelected();
                }
            }
        }
    }

    private SwapState getSwapState(boolean instantSwap) {
        return instantSwap ? instantSwapState : multiTickSwapState;
    }

    public enum SwapMode {
        None, Auto, SilentHotbar, SilentSwap
    }

    private class SwapState {
        public boolean isSwapped = false;
        public boolean didSilentSwap = false;
        public int hotbarSelectedSlot = 0;
        public int hotbarItemSlot = 0;
        public int silentSwapSelectedSlot = 0;
        public int silentSwapInventorySlot = 0;
    }
}
