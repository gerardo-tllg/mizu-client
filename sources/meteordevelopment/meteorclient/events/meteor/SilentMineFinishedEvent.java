package meteordevelopment.meteorclient.events.meteor;

import net.minecraft.class_2338;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/meteor/SilentMineFinishedEvent.class */
public class SilentMineFinishedEvent {

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/meteor/SilentMineFinishedEvent$Pre.class */
    public static class Pre {
        private boolean isRebreak;
        private class_2338 blockPos;

        public Pre(class_2338 blockPos, boolean isRebreak) {
            this.blockPos = blockPos;
            this.isRebreak = isRebreak;
        }

        public boolean getIsRebreak() {
            return this.isRebreak;
        }

        public class_2338 getBlockPos() {
            return this.blockPos;
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/meteor/SilentMineFinishedEvent$Post.class */
    public static class Post {
        private boolean isRebreak;
        private class_2338 blockPos;

        public Post(class_2338 blockPos, boolean isRebreak) {
            this.blockPos = blockPos;
            this.isRebreak = isRebreak;
        }

        public boolean getIsRebreak() {
            return this.isRebreak;
        }

        public class_2338 getBlockPos() {
            return this.blockPos;
        }
    }
}
