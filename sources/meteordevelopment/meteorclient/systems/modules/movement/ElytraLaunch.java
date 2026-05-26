package meteordevelopment.meteorclient.systems.modules.movement;

import java.util.function.Predicate;
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
import net.minecraft.class_1268;
import net.minecraft.class_1304;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_746;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/movement/ElytraLaunch.class */
public class ElytraLaunch extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Boolean> autoFireRockets;
    private final Setting<Boolean> keepElytraOn;
    private LaunchState launchState;
    private int spacePressTimer;
    private int rocketTimer;
    private boolean wasSpacePressed;
    private boolean wasElytraEquipped;
    private int equipDelay;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/movement/ElytraLaunch$LaunchState.class */
    private enum LaunchState {
        NONE,
        START_FLYING,
        START_ROCKET,
        FLYING
    }

    public ElytraLaunch() {
        super(Categories.Movement, "elytra-launch", "Double space to launch with an elytra.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.autoFireRockets = this.sgGeneral.add(new BoolSetting.Builder().name("auto-fire-rockets").description("Automatically fires rockets while flying.").defaultValue(true).build());
        this.keepElytraOn = this.sgGeneral.add(new BoolSetting.Builder().name("keep-elytra-on").description("Keeps elytra equipped after launch.").defaultValue(true).build());
        this.launchState = LaunchState.NONE;
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        resetState();
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onDeactivate() {
        stopLaunch();
        resetState();
    }

    private void resetState() {
        this.spacePressTimer = 0;
        this.rocketTimer = 0;
        this.wasSpacePressed = false;
        this.launchState = LaunchState.NONE;
        this.wasElytraEquipped = false;
        this.equipDelay = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        class_746 player = this.mc.field_1724;
        if (player == null || this.mc.field_1687 == null) {
            return;
        }
        if (this.rocketTimer > 0) {
            this.rocketTimer--;
        }
        boolean spacePressed = this.mc.field_1690.field_1903.method_1434();
        if (this.launchState == LaunchState.NONE) {
            if (this.spacePressTimer > 0) {
                this.spacePressTimer--;
            }
            if (spacePressed && !this.wasSpacePressed && !player.method_6128()) {
                if (this.spacePressTimer > 0) {
                    launch();
                } else {
                    this.spacePressTimer = 7;
                }
            }
        } else {
            if (!spacePressed) {
                stopLaunch();
            }
            if (this.equipDelay > 0) {
                this.equipDelay--;
            }
            switch (this.launchState.ordinal()) {
                case 1:
                    if (this.equipDelay <= 0) {
                        player.method_23669();
                        this.launchState = LaunchState.START_ROCKET;
                    }
                    break;
                case 2:
                    if (fireRocket()) {
                        this.rocketTimer = 40;
                        this.launchState = LaunchState.FLYING;
                    } else {
                        stopLaunch();
                    }
                    break;
                case 3:
                    if (this.autoFireRockets.get().booleanValue() && player.method_6128() && this.rocketTimer <= 0 && fireRocket()) {
                        this.rocketTimer = 40;
                    }
                    break;
            }
        }
        this.wasSpacePressed = spacePressed;
    }

    private void launch() {
        this.spacePressTimer = 0;
        this.wasElytraEquipped = this.mc.field_1724.method_6118(class_1304.field_6174).method_7909() == class_1802.field_8833;
        if (!this.wasElytraEquipped) {
            if (!PlayerUtils.silentSwapEquipElytra()) {
                error("No elytra found.", new Object[0]);
                resetState();
                return;
            }
            this.equipDelay = 2;
        }
        this.launchState = LaunchState.START_FLYING;
    }

    private void stopLaunch() {
        if (!this.keepElytraOn.get().booleanValue() || !this.wasElytraEquipped) {
            PlayerUtils.silentSwapEquipChestplate();
        }
        resetState();
    }

    private boolean fireRocket() {
        Predicate<class_1799> rocket = stack -> {
            return stack.method_7909() == class_1802.field_8639;
        };
        FindItemResult result = InvUtils.findInHotbar(rocket);
        if (!result.found()) {
            FindItemResult inventoryResult = InvUtils.find(rocket);
            if (inventoryResult.found() && !inventoryResult.isHotbar()) {
                FindItemResult hotbarSlot = InvUtils.findInHotbar((Predicate<class_1799>) x -> {
                    return x.method_7909() != class_1802.field_8288;
                });
                int targetSlot = hotbarSlot.found() ? hotbarSlot.slot() : this.mc.field_1724.method_31548().field_7545;
                InvUtils.move().from(inventoryResult.slot()).toHotbar(targetSlot);
                result = InvUtils.findInHotbar(rocket);
            }
        }
        if (result.found()) {
            if (result.isOffhand()) {
                this.mc.field_1761.method_2919(this.mc.field_1724, class_1268.field_5810);
                return true;
            }
            InvUtils.swap(result.slot(), true);
            this.mc.field_1761.method_2919(this.mc.field_1724, class_1268.field_5808);
            InvUtils.swapBack();
            return true;
        }
        return false;
    }
}
