package meteordevelopment.meteorclient.utils.tooltip;

import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.class_1088;
import net.minecraft.class_1767;
import net.minecraft.class_1799;
import net.minecraft.class_308;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_4608;
import net.minecraft.class_5602;
import net.minecraft.class_5684;
import net.minecraft.class_630;
import net.minecraft.class_823;
import net.minecraft.class_9307;
import net.minecraft.class_9334;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/tooltip/BannerTooltipComponent.class */
public class BannerTooltipComponent implements MeteorTooltipData, class_5684 {
    private final class_1767 color;
    private final class_9307 patterns;
    private final class_630 bannerField = MeteorClient.mc.method_31974().method_32072(class_5602.field_55122).method_32086("flag");

    public BannerTooltipComponent(class_1799 banner) {
        this.color = banner.method_7909().method_7706();
        this.patterns = (class_9307) banner.method_58695(class_9334.field_49619, class_9307.field_49404);
    }

    public BannerTooltipComponent(class_1767 color, class_9307 patterns) {
        this.color = color;
        this.patterns = patterns;
    }

    @Override // meteordevelopment.meteorclient.utils.tooltip.MeteorTooltipData
    public class_5684 getComponent() {
        return this;
    }

    public int method_32661(class_327 textRenderer) {
        return Opcode.IFLE;
    }

    public int method_32664(class_327 textRenderer) {
        return 80;
    }

    public void method_32666(class_327 textRenderer, int x, int y, int width, int height, class_332 context) {
        class_308.method_24210();
        class_4587 matrices = context.method_51448();
        matrices.method_22903();
        matrices.method_46416(x + 8, y + 8, 0.0f);
        matrices.method_22903();
        matrices.method_22904(0.5d, 16.0d, 0.0d);
        matrices.method_22905(6.0f, -6.0f, 1.0f);
        matrices.method_22905(2.0f, -2.0f, -2.0f);
        matrices.method_22903();
        matrices.method_22904(2.5d, 8.5d, 0.0d);
        matrices.method_22905(5.0f, 5.0f, 5.0f);
        class_4597.class_4598 immediate = MeteorClient.mc.method_22940().method_23000();
        this.bannerField.field_3654 = 0.0f;
        this.bannerField.field_3656 = -32.0f;
        class_823.method_29999(matrices, immediate, 15728880, class_4608.field_21444, this.bannerField, class_1088.field_20847, true, this.color, this.patterns);
        matrices.method_22909();
        matrices.method_22909();
        immediate.method_22993();
        matrices.method_22909();
        class_308.method_24211();
    }
}
