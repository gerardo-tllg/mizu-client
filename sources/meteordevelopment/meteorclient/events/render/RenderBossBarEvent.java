package meteordevelopment.meteorclient.events.render;

import java.util.Iterator;
import net.minecraft.class_2561;
import net.minecraft.class_345;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/render/RenderBossBarEvent.class */
public class RenderBossBarEvent {

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/render/RenderBossBarEvent$BossText.class */
    public static class BossText {
        private static final BossText INSTANCE = new BossText();
        public class_345 bossBar;
        public class_2561 name;

        public static BossText get(class_345 bossBar, class_2561 name) {
            INSTANCE.bossBar = bossBar;
            INSTANCE.name = name;
            return INSTANCE;
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/render/RenderBossBarEvent$BossSpacing.class */
    public static class BossSpacing {
        private static final BossSpacing INSTANCE = new BossSpacing();
        public int spacing;

        public static BossSpacing get(int spacing) {
            INSTANCE.spacing = spacing;
            return INSTANCE;
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/render/RenderBossBarEvent$BossIterator.class */
    public static class BossIterator {
        private static final BossIterator INSTANCE = new BossIterator();
        public Iterator<class_345> iterator;

        public static BossIterator get(Iterator<class_345> iterator) {
            INSTANCE.iterator = iterator;
            return INSTANCE;
        }
    }
}
