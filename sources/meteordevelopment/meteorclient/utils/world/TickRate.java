package meteordevelopment.meteorclient.utils.world;

import java.util.Arrays;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_2761;
import net.minecraft.class_3532;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/world/TickRate.class */
public class TickRate {
    public static TickRate INSTANCE = new TickRate();
    private final float[] tickRates = new float[20];
    private int nextIndex = 0;
    private long timeLastTimeUpdate = -1;
    private long timeGameJoined;

    private TickRate() {
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        if (event.packet instanceof class_2761) {
            long now = System.currentTimeMillis();
            float timeElapsed = (now - this.timeLastTimeUpdate) / 1000.0f;
            this.tickRates[this.nextIndex] = class_3532.method_15363(20.0f / timeElapsed, 0.0f, 20.0f);
            this.nextIndex = (this.nextIndex + 1) % this.tickRates.length;
            this.timeLastTimeUpdate = now;
        }
    }

    @EventHandler
    private void onGameJoined(GameJoinedEvent event) {
        Arrays.fill(this.tickRates, 0.0f);
        this.nextIndex = 0;
        long jCurrentTimeMillis = System.currentTimeMillis();
        this.timeLastTimeUpdate = jCurrentTimeMillis;
        this.timeGameJoined = jCurrentTimeMillis;
    }

    public float getTickRate() {
        if (!Utils.canUpdate()) {
            return 0.0f;
        }
        if (System.currentTimeMillis() - this.timeGameJoined < 4000) {
            return 20.0f;
        }
        int numTicks = 0;
        float sumTickRates = 0.0f;
        for (float tickRate : this.tickRates) {
            if (tickRate > 0.0f) {
                sumTickRates += tickRate;
                numTicks++;
            }
        }
        return sumTickRates / numTicks;
    }

    public float getTimeSinceLastTick() {
        long now = System.currentTimeMillis();
        if (now - this.timeGameJoined < 4000) {
            return 0.0f;
        }
        return (now - this.timeLastTimeUpdate) / 1000.0f;
    }
}
