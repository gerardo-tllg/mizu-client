package meteordevelopment.meteorclient.systems.modules.misc;

import java.util.Set;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.PacketListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.network.PacketUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_2596;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/misc/PacketCanceller.class */
public class PacketCanceller extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Set<Class<? extends class_2596<?>>>> s2cPackets;
    private final Setting<Set<Class<? extends class_2596<?>>>> c2sPackets;

    public PacketCanceller() {
        super(Categories.Misc, "packet-canceller", "Allows you to cancel certain packets.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.s2cPackets = this.sgGeneral.add(new PacketListSetting.Builder().name("S2C-packets").description("Server-to-client packets to cancel.").filter(aClass -> {
            return PacketUtils.getS2CPackets().contains(aClass);
        }).build());
        this.c2sPackets = this.sgGeneral.add(new PacketListSetting.Builder().name("C2S-packets").description("Client-to-server packets to cancel.").filter(aClass2 -> {
            return PacketUtils.getC2SPackets().contains(aClass2);
        }).build());
        this.runInMainMenu = true;
    }

    @EventHandler(priority = Opcode.JSR_W)
    private void onReceivePacket(PacketEvent.Receive event) {
        if (this.s2cPackets.get().contains(event.packet.getClass())) {
            event.cancel();
        }
    }

    @EventHandler(priority = Opcode.JSR_W)
    private void onSendPacket(PacketEvent.Send event) {
        if (this.c2sPackets.get().contains(event.packet.getClass())) {
            event.cancel();
        }
    }
}
