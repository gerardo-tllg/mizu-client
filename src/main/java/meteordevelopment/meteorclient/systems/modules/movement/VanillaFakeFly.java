package meteordevelopment.meteorclient.systems.modules.movement;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.input.Input;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.Hand;

public class VanillaFakeFly extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
            .name("mode").description("How to mode")
            .defaultValue(Mode.Fly).build());

    private boolean wantFirework = false;

    public VanillaFakeFly() {
        super(Categories.Movement, "vanilla-fakefly", "Fakes your fly.");
    }

    @Override
    public void onActivate() {
        // In 1.21.5, stopFallFlying() renamed to stopGliding()
        mc.player.stopGliding();
    }

    @Override
    public void onDeactivate() {
        if (isBounce()) {
            mc.options.jumpKey.setPressed(false);
            Input.setKeyState(mc.options.jumpKey, false);

            mc.options.forwardKey.setPressed(false);
            Input.setKeyState(mc.options.forwardKey, false);

            mc.player.setSprinting(false);
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!isFlying()) {
            return;
        }

        if (isBounce()) {
            mc.options.jumpKey.setPressed(true);
            Input.setKeyState(mc.options.jumpKey, true);

            mc.options.forwardKey.setPressed(true);
            Input.setKeyState(mc.options.forwardKey, true);

            mc.player.setSprinting(true);
        }

        PlayerUtils.silentSwapEquipElytra();

        mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player,
                ClientCommandC2SPacket.Mode.START_FALL_FLYING));

        if (wantFirework) {
            if (MeteorClient.SWAP.beginSwap(Items.FIREWORK_ROCKET, true)) {
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                MeteorClient.SWAP.endSwap(true);
            }

            wantFirework = false;
        }

        PlayerUtils.silentSwapEquipChestplate();
    }

    public boolean isFlying() {
        if (mc.player.isOnGround() && !isBounce()) {
            return false;
        }

        return isActive();
    }

    public boolean isBounce() {
        return mode.get() == Mode.Bounce;
    }

    public void requestFirework() {
        wantFirework = true;
    }

    private enum Mode {
        Fly, Control, Bounce
    }
}
