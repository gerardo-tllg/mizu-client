package meteordevelopment.meteorclient.events.packets;

import net.minecraft.class_2653;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/packets/ContainerSlotUpdateEvent.class */
public class ContainerSlotUpdateEvent {
    private static final ContainerSlotUpdateEvent INSTANCE = new ContainerSlotUpdateEvent();
    public class_2653 packet;

    public static ContainerSlotUpdateEvent get(class_2653 packet) {
        INSTANCE.packet = packet;
        return INSTANCE;
    }
}
