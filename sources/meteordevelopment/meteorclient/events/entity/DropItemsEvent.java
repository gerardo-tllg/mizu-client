package meteordevelopment.meteorclient.events.entity;

import meteordevelopment.meteorclient.events.Cancellable;
import net.minecraft.class_1799;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/entity/DropItemsEvent.class */
public class DropItemsEvent extends Cancellable {
    private static final DropItemsEvent INSTANCE = new DropItemsEvent();
    public class_1799 itemStack;

    public static DropItemsEvent get(class_1799 itemStack) {
        INSTANCE.setCancelled(false);
        INSTANCE.itemStack = itemStack;
        return INSTANCE;
    }
}
