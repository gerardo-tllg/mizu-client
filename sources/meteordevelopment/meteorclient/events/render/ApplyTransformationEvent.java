package meteordevelopment.meteorclient.events.render;

import meteordevelopment.meteorclient.events.Cancellable;
import net.minecraft.class_804;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/render/ApplyTransformationEvent.class */
public class ApplyTransformationEvent extends Cancellable {
    private static final ApplyTransformationEvent INSTANCE = new ApplyTransformationEvent();
    public class_804 transformation;
    public boolean leftHanded;

    public static ApplyTransformationEvent get(class_804 transformation, boolean leftHanded) {
        INSTANCE.setCancelled(false);
        INSTANCE.transformation = transformation;
        INSTANCE.leftHanded = leftHanded;
        return INSTANCE;
    }
}
