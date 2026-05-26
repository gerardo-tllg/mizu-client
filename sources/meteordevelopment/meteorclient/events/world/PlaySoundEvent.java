package meteordevelopment.meteorclient.events.world;

import meteordevelopment.meteorclient.events.Cancellable;
import net.minecraft.class_1113;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/world/PlaySoundEvent.class */
public class PlaySoundEvent extends Cancellable {
    private static final PlaySoundEvent INSTANCE = new PlaySoundEvent();
    public class_1113 sound;

    public static PlaySoundEvent get(class_1113 sound) {
        INSTANCE.setCancelled(false);
        INSTANCE.sound = sound;
        return INSTANCE;
    }
}
