package meteordevelopment.meteorclient.events.entity;

import net.minecraft.class_1297;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/entity/EntityDestroyEvent.class */
public class EntityDestroyEvent {
    private static final EntityDestroyEvent INSTANCE = new EntityDestroyEvent();
    public class_1297 entity;

    public static EntityDestroyEvent get(class_1297 entity) {
        INSTANCE.entity = entity;
        return INSTANCE;
    }
}
