package meteordevelopment.meteorclient.events.render;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/render/GetFovEvent.class */
public class GetFovEvent {
    private static final GetFovEvent INSTANCE = new GetFovEvent();
    public float fov;

    public static GetFovEvent get(float fov) {
        INSTANCE.fov = fov;
        return INSTANCE;
    }
}
