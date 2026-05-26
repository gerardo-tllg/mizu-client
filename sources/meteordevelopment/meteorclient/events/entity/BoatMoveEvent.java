package meteordevelopment.meteorclient.events.entity;

import net.minecraft.class_10255;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/entity/BoatMoveEvent.class */
public class BoatMoveEvent {
    private static final BoatMoveEvent INSTANCE = new BoatMoveEvent();
    public class_10255 boat;

    public static BoatMoveEvent get(class_10255 entity) {
        INSTANCE.boat = entity;
        return INSTANCE;
    }
}
