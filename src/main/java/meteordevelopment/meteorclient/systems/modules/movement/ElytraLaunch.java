package meteordevelopment.meteorclient.systems.modules.movement;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

import java.util.function.Predicate;

public class ElytraLaunch extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> autoFireRockets = sgGeneral.add(
        new BoolSetting.Builder()
            .name("auto-fire-rockets")
            .description("Automatically fires rockets while flying.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> keepElytraOn = sgGeneral.add(
        new BoolSetting.Builder()
            .name("keep-elytra-on")
            .description("Keeps elytra equipped after launch.")
            .defaultValue(true)
            .build()
    );

    private LaunchState launchState = LaunchState.NONE;

    private int spacePressTimer;
    private int rocketTimer;
    private boolean wasSpacePressed;
    private boolean wasElytraEquipped;
    private int equipDelay;

    public ElytraLaunch() {
        super(Categories.Movement, "elytra-launch", "Double space to launch with an elytra.");
    }

    @Override
    public void onActivate() {
        resetState();
    }

    @Override
    public void onDeactivate() {
        stopLaunch();
        resetState();
    }

    private void resetState() {
        spacePressTimer = 0;
        rocketTimer = 0;
        wasSpacePressed = false;
        launchState = LaunchState.NONE;
        wasElytraEquipped = false;
        equipDelay = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        ClientPlayerEntity player = mc.player;
        if (player == null || mc.world == null) return;

        if (rocketTimer > 0) rocketTimer--;

        boolean spacePressed = mc.options.jumpKey.isPressed();

        if (launchState == LaunchState.NONE) {
            if (spacePressTimer > 0) spacePressTimer--;

            if (spacePressed && !wasSpacePressed && !player.isGliding()) {
                if (spacePressTimer > 0) {
                    launch();
                } else {
                    spacePressTimer = 7;
                }
            }
        } else {
            if (!spacePressed) {
                stopLaunch();
            }

            if (equipDelay > 0) equipDelay--;

            switch (launchState) {
                case START_FLYING -> {
                    if (equipDelay <= 0) {
                        player.startGliding();
                        launchState = LaunchState.START_ROCKET;
                    }
                }

                case START_ROCKET -> {
                    if (fireRocket()) {
                        rocketTimer = 40;
                        launchState = LaunchState.FLYING;
                    } else {
                        stopLaunch();
                    }
                }

                case FLYING -> {
                    if (autoFireRockets.get() && player.isGliding() && rocketTimer <= 0) {
                        if (fireRocket()) rocketTimer = 40;
                    }
                }
            }
        }

        wasSpacePressed = spacePressed;
    }

    private void launch() {
        spacePressTimer = 0;

        wasElytraEquipped =
            mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() == Items.ELYTRA;

        if (!wasElytraEquipped) {
            if (!PlayerUtils.silentSwapEquipElytra()) {
                error("No elytra found.");
                resetState();
                return;
            }
            equipDelay = 2;
        }

        launchState = LaunchState.START_FLYING;
    }

    private void stopLaunch() {
        if ((!keepElytraOn.get() || !wasElytraEquipped)) {
            PlayerUtils.silentSwapEquipChestplate();
        }
        resetState();
    }

    private boolean fireRocket() {
        Predicate<ItemStack> rocket = stack ->
            stack.getItem() == Items.FIREWORK_ROCKET;

        FindItemResult result = InvUtils.findInHotbar(rocket);

        if (!result.found()) {
            FindItemResult inventoryResult = InvUtils.find(rocket);
            if (inventoryResult.found() && !inventoryResult.isHotbar()) {
                FindItemResult hotbarSlot = InvUtils.findInHotbar(x ->
                    x.getItem() != Items.TOTEM_OF_UNDYING
                );

                int targetSlot = hotbarSlot.found() ? hotbarSlot.slot() : mc.player.getInventory().selectedSlot;
                InvUtils.move().from(inventoryResult.slot()).toHotbar(targetSlot);
                result = InvUtils.findInHotbar(rocket);
            }
        }

        if (result.found()) {
            if (result.isOffhand()) {
                mc.interactionManager.interactItem(mc.player, Hand.OFF_HAND);
                return true;
            }

            InvUtils.swap(result.slot(), true);
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            InvUtils.swapBack();
            return true;
        }

        return false;
    }

    private enum LaunchState {
        NONE,
        START_FLYING,
        START_ROCKET,
        FLYING
    }
}
