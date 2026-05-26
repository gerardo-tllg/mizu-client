package meteordevelopment.meteorclient.events.entity.player;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/entity/player/ClipAtLedgeEvent.class */
public class ClipAtLedgeEvent {
    private static final ClipAtLedgeEvent INSTANCE = new ClipAtLedgeEvent();
    private boolean set;
    private boolean clip;

    public void reset() {
        this.set = false;
    }

    public void setClip(boolean clip) {
        this.set = true;
        this.clip = clip;
    }

    public boolean isSet() {
        return this.set;
    }

    public boolean isClip() {
        return this.clip;
    }

    public static ClipAtLedgeEvent get() {
        INSTANCE.reset();
        return INSTANCE;
    }
}
