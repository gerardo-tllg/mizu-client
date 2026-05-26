package meteordevelopment.meteorclient.events.packets;

import meteordevelopment.meteorclient.events.Cancellable;
import net.minecraft.class_2535;
import net.minecraft.class_2596;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/packets/PacketEvent.class */
public class PacketEvent {

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/packets/PacketEvent$Receive.class */
    public static class Receive extends Cancellable {
        public class_2596<?> packet;
        public class_2535 connection;

        public Receive(class_2596<?> packet, class_2535 connection) {
            setCancelled(false);
            this.packet = packet;
            this.connection = connection;
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/packets/PacketEvent$Send.class */
    public static class Send extends Cancellable {
        public class_2596<?> packet;
        public class_2535 connection;

        public Send(class_2596<?> packet, class_2535 connection) {
            setCancelled(false);
            this.packet = packet;
            this.connection = connection;
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/packets/PacketEvent$Sent.class */
    public static class Sent {
        public class_2596<?> packet;
        public class_2535 connection;

        public Sent(class_2596<?> packet, class_2535 connection) {
            this.packet = packet;
            this.connection = connection;
        }
    }
}
