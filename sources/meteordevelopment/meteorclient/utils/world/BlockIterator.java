package meteordevelopment.meteorclient.utils.world;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.PreInit;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.Pool;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_2338;
import net.minecraft.class_2680;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/world/BlockIterator.class */
public class BlockIterator {
    private static final Pool<Callback> callbackPool = new Pool<>(Callback::new);
    private static final List<Callback> callbacks = new ArrayList();
    private static final List<Runnable> afterCallbacks = new ArrayList();
    private static final class_2338.class_2339 blockPos = new class_2338.class_2339();
    private static int hRadius;
    private static int vRadius;
    private static boolean disableCurrent;

    private BlockIterator() {
    }

    @PreInit
    public static void init() {
        MeteorClient.EVENT_BUS.subscribe(BlockIterator.class);
    }

    @EventHandler(priority = -201)
    private static void onTick(TickEvent.Pre event) {
        if (Utils.canUpdate()) {
            int px = MeteorClient.mc.field_1724.method_31477();
            int py = MeteorClient.mc.field_1724.method_31478();
            int pz = MeteorClient.mc.field_1724.method_31479();
            for (int x = px - hRadius; x <= px + hRadius; x++) {
                for (int z = pz - hRadius; z <= pz + hRadius; z++) {
                    for (int y = Math.max(MeteorClient.mc.field_1687.method_31607(), py - vRadius); y <= py + vRadius && y <= MeteorClient.mc.field_1687.method_31605(); y++) {
                        blockPos.method_10103(x, y, z);
                        class_2680 blockState = MeteorClient.mc.field_1687.method_8320(blockPos);
                        int dx = Math.abs(x - px);
                        int dy = Math.abs(y - py);
                        int dz = Math.abs(z - pz);
                        Iterator<Callback> it = callbacks.iterator();
                        while (it.hasNext()) {
                            Callback callback = it.next();
                            if (dx <= callback.hRadius && dy <= callback.vRadius && dz <= callback.hRadius) {
                                disableCurrent = false;
                                callback.function.accept(blockPos, blockState);
                                if (disableCurrent) {
                                    it.remove();
                                }
                            }
                        }
                    }
                }
            }
            hRadius = 0;
            vRadius = 0;
            for (Callback callback2 : callbacks) {
                callbackPool.free(callback2);
            }
            callbacks.clear();
            for (Runnable callback3 : afterCallbacks) {
                callback3.run();
            }
            afterCallbacks.clear();
        }
    }

    public static void register(int horizontalRadius, int verticalRadius, BiConsumer<class_2338, class_2680> function) {
        hRadius = Math.max(hRadius, horizontalRadius);
        vRadius = Math.max(vRadius, verticalRadius);
        Callback callback = callbackPool.get();
        callback.function = function;
        callback.hRadius = horizontalRadius;
        callback.vRadius = verticalRadius;
        callbacks.add(callback);
    }

    public static void disableCurrent() {
        disableCurrent = true;
    }

    public static void after(Runnable callback) {
        afterCallbacks.add(callback);
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/world/BlockIterator$Callback.class */
    private static class Callback {
        public BiConsumer<class_2338, class_2680> function;
        public int hRadius;
        public int vRadius;

        private Callback() {
        }
    }
}
