package meteordevelopment.meteorclient.pathing;

import java.lang.reflect.InvocationTargetException;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.PreInit;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/pathing/PathManagers.class */
public class PathManagers {
    private static IPathManager INSTANCE = new NopPathManager();

    public static IPathManager get() {
        return INSTANCE;
    }

    @PreInit
    public static void init() {
        if (exists("meteordevelopment.voyager.PathManager")) {
            try {
                INSTANCE = (IPathManager) Class.forName("meteordevelopment.voyager.PathManager").getConstructor(new Class[0]).newInstance(new Object[0]);
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        if (exists("baritone.api.BaritoneAPI")) {
            BaritoneUtils.IS_AVAILABLE = true;
            if (INSTANCE instanceof NopPathManager) {
                INSTANCE = new BaritonePathManager();
            }
        }
        MeteorClient.LOG.info("Path Manager: {}", INSTANCE.getName());
    }

    private static boolean exists(String name) {
        try {
            Class.forName(name);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
