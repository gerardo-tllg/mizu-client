package meteordevelopment.meteorclient.systems.managers;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.SwitchBootstraps;
import java.util.Objects;
import java.util.UUID;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.PlayerDeathEvent;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.game.PlayerJoinLeaveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_2663;
import net.minecraft.class_2703;
import net.minecraft.class_640;
import net.minecraft.class_7828;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/managers/InformationManager.class */
public class InformationManager {
    private final Object2IntMap<UUID> totemPopMap = new Object2IntOpenHashMap();
    private boolean isLoginPacket = true;

    public InformationManager() {
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        int pops;
        if (MeteorClient.mc.field_1687 == null || MeteorClient.mc.field_1724 == null) {
            return;
        }
        class_2703 class_2703Var = event.packet;
        Objects.requireNonNull(class_2703Var);
        int i = 0;
        while (true) {
            switch ((int) SwitchBootstraps.typeSwitch(MethodHandles.lookup(), "typeSwitch", MethodType.methodType(Integer.TYPE, Object.class, Integer.TYPE), class_2663.class, class_2703.class, class_7828.class, class_2663.class).dynamicInvoker().invoke(class_2703Var, i) /* invoke-custom */) {
                case 0:
                    class_2663 packet = (class_2663) class_2703Var;
                    if (packet.method_11470() == 35) {
                        class_1657 class_1657VarMethod_11469 = packet.method_11469(MeteorClient.mc.field_1687);
                        if (class_1657VarMethod_11469 instanceof class_1657) {
                            class_1657 entity = class_1657VarMethod_11469;
                            synchronized (this.totemPopMap) {
                                pops = this.totemPopMap.getOrDefault(entity.method_5667(), 0) + 1;
                                this.totemPopMap.put(entity.method_5667(), pops);
                                break;
                            }
                            MeteorClient.EVENT_BUS.post(PlayerDeathEvent.TotemPop.get(entity, pops));
                            return;
                        }
                        return;
                    }
                    i = 1;
                    break;
                    break;
                case 1:
                    class_2703 packet2 = class_2703Var;
                    if (this.isLoginPacket) {
                        this.isLoginPacket = false;
                        return;
                    }
                    if (packet2.method_46327().contains(class_2703.class_5893.field_29136)) {
                        for (class_2703.class_2705 entry : packet2.method_46330()) {
                            class_640 playerListEntry = new class_640(entry.comp_1107(), false);
                            MeteorClient.EVENT_BUS.post(PlayerJoinLeaveEvent.Join.get(playerListEntry));
                        }
                        return;
                    }
                    return;
                case 2:
                    class_7828 packet3 = (class_7828) class_2703Var;
                    if (MeteorClient.mc.method_1562() == null) {
                        return;
                    }
                    for (UUID uuid : packet3.comp_1105()) {
                        class_640 toRemove = MeteorClient.mc.method_1562().method_2871(uuid);
                        if (toRemove != null) {
                            MeteorClient.EVENT_BUS.post(PlayerJoinLeaveEvent.Leave.get(toRemove));
                        }
                    }
                    return;
                case 3:
                    class_2663 packet4 = (class_2663) class_2703Var;
                    if (packet4.method_11470() == 3) {
                        class_1657 class_1657VarMethod_114692 = packet4.method_11469(MeteorClient.mc.field_1687);
                        if (class_1657VarMethod_114692 instanceof class_1657) {
                            class_1657 entity2 = class_1657VarMethod_114692;
                            int pops2 = 0;
                            if (this.totemPopMap.containsKey(entity2.method_5667())) {
                                pops2 = this.totemPopMap.removeInt(entity2.method_5667());
                            }
                            MeteorClient.EVENT_BUS.post(PlayerDeathEvent.Death.get(entity2, pops2));
                            return;
                        }
                        return;
                    }
                    i = 4;
                    break;
                    break;
                default:
                    return;
            }
        }
    }

    @EventHandler
    private void onGameLeave(GameLeftEvent event) {
        this.isLoginPacket = true;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (MeteorClient.mc.field_1687 == null || MeteorClient.mc.field_1724 == null) {
        }
    }

    public int getPops(class_1297 entity) {
        return this.totemPopMap.getOrDefault(entity.method_5667(), 0);
    }

    public int getPops(UUID uuid) {
        return this.totemPopMap.getOrDefault(uuid, 0);
    }
}
