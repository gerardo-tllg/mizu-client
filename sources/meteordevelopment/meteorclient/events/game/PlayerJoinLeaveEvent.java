package meteordevelopment.meteorclient.events.game;

import net.minecraft.class_640;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/game/PlayerJoinLeaveEvent.class */
public class PlayerJoinLeaveEvent {

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/game/PlayerJoinLeaveEvent$Join.class */
    public static class Join {
        private static final Join INSTANCE = new Join();
        public class_640 entry;

        public static Join get(class_640 entry) {
            INSTANCE.entry = entry;
            return INSTANCE;
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/game/PlayerJoinLeaveEvent$Leave.class */
    public static class Leave {
        private static final Leave INSTANCE = new Leave();
        public class_640 entry;

        public static Leave get(class_640 entry) {
            INSTANCE.entry = entry;
            return INSTANCE;
        }
    }
}
