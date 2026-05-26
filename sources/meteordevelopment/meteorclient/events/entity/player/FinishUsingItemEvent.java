package meteordevelopment.meteorclient.events.entity.player;

import net.minecraft.class_1799;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/entity/player/FinishUsingItemEvent.class */
public class FinishUsingItemEvent {
    private static final FinishUsingItemEvent INSTANCE = new FinishUsingItemEvent();
    public class_1799 itemStack;

    public static FinishUsingItemEvent get(class_1799 itemStack) {
        INSTANCE.itemStack = itemStack;
        return INSTANCE;
    }
}
