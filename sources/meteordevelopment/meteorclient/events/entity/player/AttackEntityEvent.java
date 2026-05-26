package meteordevelopment.meteorclient.events.entity.player;

import meteordevelopment.meteorclient.events.Cancellable;
import net.minecraft.class_1297;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/entity/player/AttackEntityEvent.class */
public class AttackEntityEvent extends Cancellable {
    private static final AttackEntityEvent INSTANCE = new AttackEntityEvent();
    public class_1297 entity;

    public static AttackEntityEvent get(class_1297 entity) {
        INSTANCE.setCancelled(false);
        INSTANCE.entity = entity;
        return INSTANCE;
    }
}
