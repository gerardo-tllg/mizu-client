package meteordevelopment.meteorclient.events.world;

import meteordevelopment.meteorclient.events.Cancellable;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/world/ChunkOcclusionEvent.class */
public class ChunkOcclusionEvent extends Cancellable {
    private static final ChunkOcclusionEvent INSTANCE = new ChunkOcclusionEvent();

    public static ChunkOcclusionEvent get() {
        INSTANCE.setCancelled(false);
        return INSTANCE;
    }
}
