package meteordevelopment.meteorclient.events.entity.player;

import meteordevelopment.meteorclient.events.Cancellable;
import net.minecraft.class_2338;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/entity/player/BreakBlockEvent.class */
public class BreakBlockEvent extends Cancellable {
    private static final BreakBlockEvent INSTANCE = new BreakBlockEvent();
    public class_2338 blockPos;

    public static BreakBlockEvent get(class_2338 blockPos) {
        INSTANCE.setCancelled(false);
        INSTANCE.blockPos = blockPos;
        return INSTANCE;
    }
}
