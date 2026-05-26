package meteordevelopment.meteorclient.events.game;

import meteordevelopment.meteorclient.events.Cancellable;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/game/SendMessageEvent.class */
public class SendMessageEvent extends Cancellable {
    private static final SendMessageEvent INSTANCE = new SendMessageEvent();
    public String message;

    public static SendMessageEvent get(String message) {
        INSTANCE.setCancelled(false);
        INSTANCE.message = message;
        return INSTANCE;
    }
}
