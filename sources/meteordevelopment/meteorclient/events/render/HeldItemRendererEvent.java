package meteordevelopment.meteorclient.events.render;

import net.minecraft.class_1268;
import net.minecraft.class_4587;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/render/HeldItemRendererEvent.class */
public class HeldItemRendererEvent {
    private static final HeldItemRendererEvent INSTANCE = new HeldItemRendererEvent();
    public class_1268 hand;
    public class_4587 matrix;

    public static HeldItemRendererEvent get(class_1268 hand, class_4587 matrices) {
        INSTANCE.hand = hand;
        INSTANCE.matrix = matrices;
        return INSTANCE;
    }
}
