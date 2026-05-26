package meteordevelopment.meteorclient.events.world;

import net.minecraft.class_639;
import net.minecraft.class_642;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/world/ServerConnectBeginEvent.class */
public class ServerConnectBeginEvent {
    private static final ServerConnectBeginEvent INSTANCE = new ServerConnectBeginEvent();
    public class_639 address;
    public class_642 info;

    public static ServerConnectBeginEvent get(class_639 address, class_642 info) {
        INSTANCE.address = address;
        INSTANCE.info = info;
        return INSTANCE;
    }
}
