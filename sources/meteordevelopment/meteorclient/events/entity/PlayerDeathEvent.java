package meteordevelopment.meteorclient.events.entity;

import net.minecraft.class_1657;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/entity/PlayerDeathEvent.class */
public class PlayerDeathEvent {

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/entity/PlayerDeathEvent$TotemPop.class */
    public static class TotemPop extends PlayerDeathEvent {
        private static final TotemPop INSTANCE = new TotemPop();
        private class_1657 player;
        private int pops;

        public static PlayerDeathEvent get(class_1657 player, int pop) {
            INSTANCE.player = player;
            INSTANCE.pops = pop;
            return INSTANCE;
        }

        public class_1657 getPlayer() {
            return this.player;
        }

        public int getPops() {
            return this.pops;
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/entity/PlayerDeathEvent$Death.class */
    public static class Death extends PlayerDeathEvent {
        private static final Death INSTANCE = new Death();
        private class_1657 player;
        private int pops;

        public static Death get(class_1657 player, int pop) {
            INSTANCE.player = player;
            INSTANCE.pops = pop;
            return INSTANCE;
        }

        public class_1657 getPlayer() {
            return this.player;
        }

        public int getPops() {
            return this.pops;
        }
    }
}
