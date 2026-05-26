package meteordevelopment.meteorclient.pathing;

import baritone.api.BaritoneAPI;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/pathing/BaritoneUtils.class */
public class BaritoneUtils {
    public static boolean IS_AVAILABLE = false;

    private BaritoneUtils() {
    }

    public static String getPrefix() {
        if (IS_AVAILABLE) {
            return (String) BaritoneAPI.getSettings().prefix.value;
        }
        return "";
    }
}
