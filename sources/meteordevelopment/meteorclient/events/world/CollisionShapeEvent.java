package meteordevelopment.meteorclient.events.world;

import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.meteorclient.events.Cancellable;
import net.minecraft.class_2338;
import net.minecraft.class_265;
import net.minecraft.class_2680;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/world/CollisionShapeEvent.class */
public class CollisionShapeEvent extends Cancellable {
    private static final CollisionShapeEvent INSTANCE = new CollisionShapeEvent();
    public class_2680 state;
    public class_2338 pos;
    public class_265 shape;

    public static CollisionShapeEvent get(class_2680 state, class_2338 pos, class_265 shape) {
        CollisionShapeEvent event = INSTANCE;
        if (!RenderSystem.isOnRenderThread()) {
            event = new CollisionShapeEvent();
        }
        event.setCancelled(false);
        event.state = state;
        event.pos = pos;
        event.shape = shape;
        return event;
    }
}
