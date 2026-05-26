package meteordevelopment.meteorclient.utils.tooltip;

import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.class_1799;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_5684;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/tooltip/ContainerTooltipComponent.class */
public class ContainerTooltipComponent implements class_5684, MeteorTooltipData {
    private static final class_2960 TEXTURE_CONTAINER_BACKGROUND = MeteorClient.identifier("textures/container.png");
    private final class_1799[] items;
    private final Color color;

    public ContainerTooltipComponent(class_1799[] items, Color color) {
        this.items = items;
        this.color = color;
    }

    @Override // meteordevelopment.meteorclient.utils.tooltip.MeteorTooltipData
    public class_5684 getComponent() {
        return this;
    }

    public int method_32661(class_327 textRenderer) {
        return 67;
    }

    public int method_32664(class_327 textRenderer) {
        return Opcode.ARETURN;
    }

    public void method_32666(class_327 textRenderer, int x, int y, int width, int height, class_332 context) {
        context.method_25291(class_1921::method_62277, TEXTURE_CONTAINER_BACKGROUND, x, y, 0.0f, 0.0f, Opcode.ARETURN, 67, Opcode.ARETURN, 67, this.color.getPacked());
        int row = 0;
        int i = 0;
        for (class_1799 itemStack : this.items) {
            RenderUtils.drawItem(context, itemStack, x + 8 + (i * 18), y + 7 + (row * 18), 1.0f, true);
            i++;
            if (i >= 9) {
                i = 0;
                row++;
            }
        }
    }
}
