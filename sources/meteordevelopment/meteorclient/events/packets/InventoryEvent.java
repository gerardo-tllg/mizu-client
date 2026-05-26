package meteordevelopment.meteorclient.events.packets;

import net.minecraft.class_2649;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/packets/InventoryEvent.class */
public class InventoryEvent {
    private static final InventoryEvent INSTANCE = new InventoryEvent();
    public class_2649 packet;

    public static InventoryEvent get(class_2649 packet) {
        INSTANCE.packet = packet;
        return INSTANCE;
    }
}
