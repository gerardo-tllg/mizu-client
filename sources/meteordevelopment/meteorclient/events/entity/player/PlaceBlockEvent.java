package meteordevelopment.meteorclient.events.entity.player;

import meteordevelopment.meteorclient.events.Cancellable;
import net.minecraft.class_2248;
import net.minecraft.class_2338;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/entity/player/PlaceBlockEvent.class */
public class PlaceBlockEvent extends Cancellable {
    private static final PlaceBlockEvent INSTANCE = new PlaceBlockEvent();
    public class_2338 blockPos;
    public class_2248 block;

    public static PlaceBlockEvent get(class_2338 blockPos, class_2248 block) {
        INSTANCE.setCancelled(false);
        INSTANCE.blockPos = blockPos;
        INSTANCE.block = block;
        return INSTANCE;
    }
}
