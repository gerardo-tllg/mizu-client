package meteordevelopment.meteorclient.events.world;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/world/AmbientOcclusionEvent.class */
public class AmbientOcclusionEvent {
    private static final AmbientOcclusionEvent INSTANCE = new AmbientOcclusionEvent();
    public float lightLevel = -1.0f;

    public static AmbientOcclusionEvent get() {
        INSTANCE.lightLevel = -1.0f;
        return INSTANCE;
    }
}
