package meteordevelopment.meteorclient.events.render;

import meteordevelopment.meteorclient.events.Cancellable;
import net.minecraft.class_2586;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/render/RenderBlockEntityEvent.class */
public class RenderBlockEntityEvent extends Cancellable {
    private static final RenderBlockEntityEvent INSTANCE = new RenderBlockEntityEvent();
    public class_2586 blockEntity;

    public static RenderBlockEntityEvent get(class_2586 blockEntity) {
        INSTANCE.setCancelled(false);
        INSTANCE.blockEntity = blockEntity;
        return INSTANCE;
    }
}
