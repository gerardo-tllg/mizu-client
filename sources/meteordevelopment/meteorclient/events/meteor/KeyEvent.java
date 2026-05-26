package meteordevelopment.meteorclient.events.meteor;

import meteordevelopment.meteorclient.events.Cancellable;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/meteor/KeyEvent.class */
public class KeyEvent extends Cancellable {
    private static final KeyEvent INSTANCE = new KeyEvent();
    public int key;
    public int modifiers;
    public KeyAction action;

    public static KeyEvent get(int key, int modifiers, KeyAction action) {
        INSTANCE.setCancelled(false);
        INSTANCE.key = key;
        INSTANCE.modifiers = modifiers;
        INSTANCE.action = action;
        return INSTANCE;
    }
}
