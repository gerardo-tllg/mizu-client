package meteordevelopment.meteorclient.events.world;

import net.minecraft.class_2338;
import net.minecraft.class_2680;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/world/BlockUpdateEvent.class */
public class BlockUpdateEvent {
    private static final BlockUpdateEvent INSTANCE = new BlockUpdateEvent();
    public class_2338 pos;
    public class_2680 oldState;
    public class_2680 newState;

    public static BlockUpdateEvent get(class_2338 pos, class_2680 oldState, class_2680 newState) {
        INSTANCE.pos = pos;
        INSTANCE.oldState = oldState;
        INSTANCE.newState = newState;
        return INSTANCE;
    }
}
