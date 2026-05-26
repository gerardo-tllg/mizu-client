package meteordevelopment.meteorclient.events.entity.player;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/entity/player/BlockBreakingCooldownEvent.class */
public class BlockBreakingCooldownEvent {
    private static final BlockBreakingCooldownEvent INSTANCE = new BlockBreakingCooldownEvent();
    public int cooldown;

    public static BlockBreakingCooldownEvent get(int cooldown) {
        INSTANCE.cooldown = cooldown;
        return INSTANCE;
    }
}
