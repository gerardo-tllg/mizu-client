package meteordevelopment.meteorclient.events.world;

import meteordevelopment.meteorclient.events.Cancellable;
import net.minecraft.class_2394;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/world/ParticleEvent.class */
public class ParticleEvent extends Cancellable {
    private static final ParticleEvent INSTANCE = new ParticleEvent();
    public class_2394 particle;

    public static ParticleEvent get(class_2394 particle) {
        INSTANCE.setCancelled(false);
        INSTANCE.particle = particle;
        return INSTANCE;
    }
}
