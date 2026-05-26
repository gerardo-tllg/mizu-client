package meteordevelopment.meteorclient.events.meteor;

import meteordevelopment.meteorclient.events.Cancellable;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/meteor/CharTypedEvent.class */
public class CharTypedEvent extends Cancellable {
    private static final CharTypedEvent INSTANCE = new CharTypedEvent();
    public char c;

    public static CharTypedEvent get(char c) {
        INSTANCE.setCancelled(false);
        INSTANCE.c = c;
        return INSTANCE;
    }
}
