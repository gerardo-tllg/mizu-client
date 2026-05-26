package meteordevelopment.meteorclient.utils.tooltip;

import javassist.bytecode.Opcode;
import net.minecraft.class_1921;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import net.minecraft.class_5481;
import net.minecraft.class_5684;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/tooltip/BookTooltipComponent.class */
public class BookTooltipComponent implements class_5684, MeteorTooltipData {
    private static final class_2960 TEXTURE_BOOK_BACKGROUND = class_2960.method_60654("textures/gui/book.png");
    private final class_2561 page;

    public BookTooltipComponent(class_2561 page) {
        this.page = page;
    }

    @Override // meteordevelopment.meteorclient.utils.tooltip.MeteorTooltipData
    public class_5684 getComponent() {
        return this;
    }

    public int method_32661(class_327 textRenderer) {
        return Opcode.I2F;
    }

    public int method_32664(class_327 textRenderer) {
        return Opcode.IREM;
    }

    public void method_32666(class_327 textRenderer, int x, int y, int width, int height, class_332 context) {
        context.method_25291(class_1921::method_62277, TEXTURE_BOOK_BACKGROUND, x, y, 0.0f, 12.0f, 0, Opcode.IREM, Opcode.I2F, Opcode.PUTSTATIC, Opcode.PUTSTATIC);
        class_4587 matrices = context.method_51448();
        matrices.method_22903();
        matrices.method_46416(x + 16, y + 12, 1.0f);
        matrices.method_22905(0.7f, 0.7f, 1.0f);
        int offset = 0;
        for (class_5481 line : textRenderer.method_1728(this.page, Opcode.IREM)) {
            context.method_51430(textRenderer, line, 0, offset, 0, false);
            offset += 8;
        }
        matrices.method_22909();
    }
}
