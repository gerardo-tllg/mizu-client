package meteordevelopment.meteorclient.events.render;

import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.class_332;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/render/Render2DEvent.class */
public class Render2DEvent {
    private static final Render2DEvent INSTANCE = new Render2DEvent();
    public class_332 drawContext;
    public int screenWidth;
    public int screenHeight;
    public double frameTime;
    public float tickDelta;

    public static Render2DEvent get(class_332 drawContext, int screenWidth, int screenHeight, float tickDelta) {
        INSTANCE.drawContext = drawContext;
        INSTANCE.screenWidth = screenWidth;
        INSTANCE.screenHeight = screenHeight;
        INSTANCE.frameTime = Utils.frameTime;
        INSTANCE.tickDelta = tickDelta;
        return INSTANCE;
    }
}
