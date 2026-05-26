package meteordevelopment.meteorclient.events.entity;

import net.minecraft.class_1297;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/entity/EntityRemovedEvent.class */
public class EntityRemovedEvent {
    private static final EntityRemovedEvent INSTANCE = new EntityRemovedEvent();
    public class_1297 entity;

    public static EntityRemovedEvent get(class_1297 entity) {
        INSTANCE.entity = entity;
        return INSTANCE;
    }
}
