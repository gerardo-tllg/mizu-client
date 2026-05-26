package meteordevelopment.meteorclient.utils.network;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/network/OnlinePlayers.class */
public class OnlinePlayers {
    private static long lastPingTime;

    private OnlinePlayers() {
    }

    public static void update() {
        long time = System.currentTimeMillis();
        if (time - lastPingTime > 300000) {
            MeteorExecutor.execute(() -> {
                Http.post("https://meteorclient.com/api/online/ping").ignoreExceptions().send();
            });
            lastPingTime = time;
        }
    }

    public static void leave() {
        MeteorExecutor.execute(() -> {
            Http.post("https://meteorclient.com/api/online/leave").ignoreExceptions().send();
        });
    }
}
