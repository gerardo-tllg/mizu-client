package meteordevelopment.meteorclient.events.meteor;

import meteordevelopment.meteorclient.events.Cancellable;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/meteor/MouseScrollEvent.class */
public class MouseScrollEvent extends Cancellable {
    private static final MouseScrollEvent INSTANCE = new MouseScrollEvent();
    public double value;

    public static MouseScrollEvent get(double value) {
        INSTANCE.setCancelled(false);
        INSTANCE.value = value;
        return INSTANCE;
    }
}
