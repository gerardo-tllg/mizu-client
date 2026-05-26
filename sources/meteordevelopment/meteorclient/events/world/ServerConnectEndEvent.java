package meteordevelopment.meteorclient.events.world;

import java.net.InetSocketAddress;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/world/ServerConnectEndEvent.class */
public class ServerConnectEndEvent {
    private static final ServerConnectEndEvent INSTANCE = new ServerConnectEndEvent();
    public InetSocketAddress address;

    public static ServerConnectEndEvent get(InetSocketAddress address) {
        INSTANCE.address = address;
        return INSTANCE;
    }
}
