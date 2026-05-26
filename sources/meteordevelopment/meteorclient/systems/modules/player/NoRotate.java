package meteordevelopment.meteorclient.systems.modules.player;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.mixin.PlayerPositionLookS2CPacketAccessor;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_10182;
import net.minecraft.class_2708;
import net.minecraft.class_2709;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/player/NoRotate.class */
public class NoRotate extends Module {
    public NoRotate() {
        super(Categories.Player, "no-rotate", "Attempts to block rotations sent from server to client.");
    }

    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        float newYaw;
        float newPitch;
        PlayerPositionLookS2CPacketAccessor playerPositionLookS2CPacketAccessor = event.packet;
        if (playerPositionLookS2CPacketAccessor instanceof class_2708) {
            PlayerPositionLookS2CPacketAccessor playerPositionLookS2CPacketAccessor2 = (class_2708) playerPositionLookS2CPacketAccessor;
            class_10182 change = playerPositionLookS2CPacketAccessor2.comp_3228();
            if (playerPositionLookS2CPacketAccessor2.comp_3229().contains(class_2709.field_12401)) {
                newYaw = 0.0f;
            } else {
                newYaw = this.mc.field_1724.method_36454();
            }
            if (playerPositionLookS2CPacketAccessor2.comp_3229().contains(class_2709.field_12397)) {
                newPitch = 0.0f;
            } else {
                newPitch = this.mc.field_1724.method_36455();
            }
            class_10182 newChange = new class_10182(change.comp_3148(), change.comp_3149(), newYaw, newPitch);
            playerPositionLookS2CPacketAccessor2.setChange(newChange);
        }
    }
}
