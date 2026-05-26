package meteordevelopment.meteorclient.events.entity.player;

import net.minecraft.class_1799;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/entity/player/StoppedUsingItemEvent.class */
public class StoppedUsingItemEvent {
    private static final StoppedUsingItemEvent INSTANCE = new StoppedUsingItemEvent();
    public class_1799 itemStack;

    public static StoppedUsingItemEvent get(class_1799 itemStack) {
        INSTANCE.itemStack = itemStack;
        return INSTANCE;
    }
}
