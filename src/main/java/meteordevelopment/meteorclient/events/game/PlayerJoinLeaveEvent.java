/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.events.game;

import net.minecraft.client.network.PlayerListEntry;

public class PlayerJoinLeaveEvent {
    public static class Join {
        private static final Join INSTANCE = new Join();
        public PlayerListEntry entry;

        public static Join get(PlayerListEntry entry) {
            INSTANCE.entry = entry;
            return INSTANCE;
        }
    }

    public static class Leave {
        private static final Leave INSTANCE = new Leave();
        public PlayerListEntry entry;

        public static Leave get(PlayerListEntry entry) {
            INSTANCE.entry = entry;
            return INSTANCE;
        }
    }
}
