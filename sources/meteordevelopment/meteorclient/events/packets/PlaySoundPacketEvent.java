package meteordevelopment.meteorclient.events.packets;

import net.minecraft.class_2767;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/packets/PlaySoundPacketEvent.class */
public class PlaySoundPacketEvent {
    private static final PlaySoundPacketEvent INSTANCE = new PlaySoundPacketEvent();
    public class_2767 packet;

    public static PlaySoundPacketEvent get(class_2767 packet) {
        INSTANCE.packet = packet;
        return INSTANCE;
    }
}
