package meteordevelopment.meteorclient.events.world;

import net.minecraft.class_2680;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/world/BlockActivateEvent.class */
public class BlockActivateEvent {
    private static final BlockActivateEvent INSTANCE = new BlockActivateEvent();
    public class_2680 blockState;

    public static BlockActivateEvent get(class_2680 blockState) {
        INSTANCE.blockState = blockState;
        return INSTANCE;
    }
}
