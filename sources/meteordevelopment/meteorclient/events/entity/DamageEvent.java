package meteordevelopment.meteorclient.events.entity;

import net.minecraft.class_1282;
import net.minecraft.class_1309;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/entity/DamageEvent.class */
public class DamageEvent {
    private static final DamageEvent INSTANCE = new DamageEvent();
    public class_1309 entity;
    public class_1282 source;

    public static DamageEvent get(class_1309 entity, class_1282 source) {
        INSTANCE.entity = entity;
        INSTANCE.source = source;
        return INSTANCE;
    }
}
