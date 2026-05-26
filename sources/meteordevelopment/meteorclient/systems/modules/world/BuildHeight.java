package meteordevelopment.meteorclient.systems.modules.world;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_2350;
import net.minecraft.class_2885;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/world/BuildHeight.class */
public class BuildHeight extends Module {
    public BuildHeight() {
        super(Categories.World, "build-height", "Allows you to interact with objects at the build limit.");
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        class_2885 class_2885Var = event.packet;
        if (class_2885Var instanceof class_2885) {
            class_2885 p = class_2885Var;
            if (this.mc.field_1687 != null && p.method_12543().method_17784().field_1351 >= this.mc.field_1687.method_31605() && p.method_12543().method_17780() == class_2350.field_11036) {
                p.method_12543().setSide(class_2350.field_11033);
            }
        }
    }
}
