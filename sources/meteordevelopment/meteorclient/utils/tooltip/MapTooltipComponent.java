package meteordevelopment.meteorclient.utils.tooltip;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.BetterTooltips;
import net.minecraft.class_10090;
import net.minecraft.class_1806;
import net.minecraft.class_1921;
import net.minecraft.class_22;
import net.minecraft.class_2960;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_5684;
import net.minecraft.class_9209;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/tooltip/MapTooltipComponent.class */
public class MapTooltipComponent implements class_5684, MeteorTooltipData {
    private static final class_2960 TEXTURE_MAP_BACKGROUND = class_2960.method_60654("textures/map/map_background.png");
    private final int mapId;
    private final class_10090 mapRenderState = new class_10090();

    public MapTooltipComponent(int mapId) {
        this.mapId = mapId;
    }

    public int method_32661(class_327 textRenderer) {
        double scale = ((BetterTooltips) Modules.get().get(BetterTooltips.class)).mapsScale.get().doubleValue();
        return ((int) (144.0d * scale)) + 2;
    }

    public int method_32664(class_327 textRenderer) {
        double scale = ((BetterTooltips) Modules.get().get(BetterTooltips.class)).mapsScale.get().doubleValue();
        return (int) (144.0d * scale);
    }

    @Override // meteordevelopment.meteorclient.utils.tooltip.MeteorTooltipData
    public class_5684 getComponent() {
        return this;
    }

    public void method_32666(class_327 textRenderer, int x, int y, int width, int height, class_332 context) {
        double scale = ((BetterTooltips) Modules.get().get(BetterTooltips.class)).mapsScale.get().doubleValue();
        class_4587 matrices = context.method_51448();
        matrices.method_22903();
        matrices.method_46416(x, y, 0.0f);
        matrices.method_22905(((float) scale) * 2.0f, ((float) scale) * 2.0f, 0.0f);
        matrices.method_22905(1.125f, 1.125f, 0.0f);
        context.method_25291(class_1921::method_62277, TEXTURE_MAP_BACKGROUND, 0, 0, 0.0f, 0.0f, 0, 64, 64, 64, 64);
        matrices.method_22909();
        class_4597.class_4598 consumer = MeteorClient.mc.method_22940().method_23000();
        class_22 mapState = class_1806.method_7997(new class_9209(this.mapId), MeteorClient.mc.field_1687);
        if (mapState == null) {
            return;
        }
        matrices.method_22903();
        matrices.method_46416(x, y, 0.0f);
        matrices.method_22905((float) scale, (float) scale, 0.0f);
        matrices.method_46416(8.0f, 8.0f, 0.0f);
        MeteorClient.mc.method_61965().method_62230(new class_9209(this.mapId), mapState, this.mapRenderState);
        MeteorClient.mc.method_61965().method_1773(this.mapRenderState, matrices, consumer, false, 15728880);
        consumer.method_22993();
        matrices.method_22909();
    }
}
