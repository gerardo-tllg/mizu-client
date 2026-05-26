package meteordevelopment.meteorclient.systems.modules.movement;

import java.util.LinkedList;
import java.util.Queue;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_2596;
import net.minecraft.class_2664;
import net.minecraft.class_2743;
import net.minecraft.class_6374;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/movement/VelocityBypass.class */
public class VelocityBypass extends Module {
    private final SettingGroup sgGeneral;
    public final Setting<Boolean> onVelo;
    public final Setting<Boolean> onExplosion;
    public final Setting<Integer> cancelNextHowMany;
    public final Setting<Boolean> send;
    private int nextHowMany;
    private final Queue<class_2596<?>> packetQueue;

    public VelocityBypass() {
        super(Categories.Movement, "velocity-bypass", "Delays CommonPong C2S packets after velocity/explosion hits to bypass anti-cheat velocity checks.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.onVelo = this.sgGeneral.add(new BoolSetting.Builder().name("on-velo").description("Delay pong packets triggered by entity velocity updates.").defaultValue(true).build());
        this.onExplosion = this.sgGeneral.add(new BoolSetting.Builder().name("on-explosion").description("Delay pong packets triggered by explosion packets.").defaultValue(false).build());
        this.cancelNextHowMany = this.sgGeneral.add(new IntSetting.Builder().name("cancel-next-how-many").description("How many pong packets to delay per velocity/explosion trigger.").defaultValue(4).min(1).sliderMax(20).build());
        this.send = this.sgGeneral.add(new BoolSetting.Builder().name("send").description("Flush all queued pong packets immediately once the delay counter reaches zero.").defaultValue(false).build());
        this.nextHowMany = 0;
        this.packetQueue = new LinkedList();
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onDeactivate() {
        sendAllPacketsInQueue();
    }

    @EventHandler(priority = 1200)
    private void onReceivePacket(PacketEvent.Receive event) {
        if (this.onVelo.get().booleanValue()) {
            class_2743 class_2743Var = event.packet;
            if (class_2743Var instanceof class_2743) {
                class_2743 p = class_2743Var;
                if (p.method_11818() == this.mc.field_1724.method_5628()) {
                    this.nextHowMany += this.cancelNextHowMany.get().intValue();
                    return;
                }
            }
        }
        if (this.onExplosion.get().booleanValue() && (event.packet instanceof class_2664)) {
            this.nextHowMany += this.cancelNextHowMany.get().intValue();
        }
    }

    @EventHandler(priority = 1200)
    private void onSendPacket(PacketEvent.Send event) {
        if (event.packet instanceof class_6374) {
            if (this.nextHowMany > 0) {
                event.cancel();
                synchronized (this.packetQueue) {
                    this.packetQueue.add(event.packet);
                }
                this.nextHowMany--;
                return;
            }
            if (this.send.get().booleanValue()) {
                sendAllPacketsInQueue();
            }
        }
    }

    private void sendAllPacketsInQueue() {
        synchronized (this.packetQueue) {
            while (!this.packetQueue.isEmpty()) {
                class_2596<?> packet = this.packetQueue.poll();
                if (this.mc.field_1724 != null && this.mc.field_1724.field_3944 != null) {
                    this.mc.field_1724.field_3944.method_52787(packet);
                }
            }
        }
        this.nextHowMany = 0;
    }
}
