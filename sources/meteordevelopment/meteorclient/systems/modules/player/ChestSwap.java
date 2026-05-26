package meteordevelopment.meteorclient.systems.modules.player;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_10192;
import net.minecraft.class_1304;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_408;
import net.minecraft.class_9334;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/player/ChestSwap.class */
public class ChestSwap extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Keybind> swapBind;
    private boolean keyUnpressed;

    public ChestSwap() {
        super(Categories.Player, "chest-swap", "Automatically swaps between a chestplate and an elytra.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.swapBind = this.sgGeneral.add(new KeybindSetting.Builder().name("swap-bind").description("Swaps on this key press.").build());
        this.keyUnpressed = false;
    }

    @EventHandler(priority = 200)
    private void onRender(Render3DEvent event) {
        update();
    }

    private void update() {
        if (!this.swapBind.get().isPressed()) {
            this.keyUnpressed = true;
        }
        if (this.swapBind.get().isPressed() && this.keyUnpressed && !(this.mc.field_1755 instanceof class_408)) {
            swap();
            this.keyUnpressed = false;
        }
    }

    public void swap() {
        class_1792 currentItem = this.mc.field_1724.method_6118(class_1304.field_6174).method_7909();
        if (currentItem == class_1802.field_8833) {
            PlayerUtils.silentSwapEquipChestplate();
        } else if (isChestplate(this.mc.field_1724.method_6118(class_1304.field_6174))) {
            PlayerUtils.silentSwapEquipElytra();
        } else if (!PlayerUtils.silentSwapEquipChestplate()) {
            PlayerUtils.silentSwapEquipElytra();
        }
    }

    private boolean isChestplate(class_1799 stack) {
        class_10192 equippable = (class_10192) stack.method_58694(class_9334.field_54196);
        return equippable != null && equippable.comp_3174() == class_1304.field_6174;
    }
}
