package meteordevelopment.meteorclient.events.entity.player;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/entity/player/JumpVelocityMultiplierEvent.class */
public class JumpVelocityMultiplierEvent {
    private static final JumpVelocityMultiplierEvent INSTANCE = new JumpVelocityMultiplierEvent();
    public float multiplier = 1.0f;

    public static JumpVelocityMultiplierEvent get() {
        INSTANCE.multiplier = 1.0f;
        return INSTANCE;
    }
}
