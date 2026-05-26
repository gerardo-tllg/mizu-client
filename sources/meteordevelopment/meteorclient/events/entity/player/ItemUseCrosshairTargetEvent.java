package meteordevelopment.meteorclient.events.entity.player;

import net.minecraft.class_239;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/entity/player/ItemUseCrosshairTargetEvent.class */
public class ItemUseCrosshairTargetEvent {
    private static final ItemUseCrosshairTargetEvent INSTANCE = new ItemUseCrosshairTargetEvent();
    public class_239 target;

    public static ItemUseCrosshairTargetEvent get(class_239 target) {
        INSTANCE.target = target;
        return INSTANCE;
    }
}
