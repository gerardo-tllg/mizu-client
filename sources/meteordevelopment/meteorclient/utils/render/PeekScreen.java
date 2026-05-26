package meteordevelopment.meteorclient.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.BetterTooltips;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.class_1277;
import net.minecraft.class_1733;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_3872;
import net.minecraft.class_495;
import net.minecraft.class_9334;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/render/PeekScreen.class */
public class PeekScreen extends class_495 {
    private final class_2960 TEXTURE;
    private final class_1799[] contents;
    private final class_1799 storageBlock;

    public PeekScreen(class_1799 storageBlock, class_1799[] contents) {
        super(new class_1733(0, MeteorClient.mc.field_1724.method_31548(), new class_1277(contents)), MeteorClient.mc.field_1724.method_31548(), storageBlock.method_7964());
        this.TEXTURE = class_2960.method_60654("textures/gui/container/shulker_box.png");
        this.contents = contents;
        this.storageBlock = storageBlock;
    }

    public boolean method_25402(double mouseX, double mouseY, int button) {
        BetterTooltips tooltips = (BetterTooltips) Modules.get().get(BetterTooltips.class);
        if (button == 2 && this.field_2787 != null && !this.field_2787.method_7677().method_7960() && MeteorClient.mc.field_1724.field_7512.method_34255().method_7960() && tooltips.middleClickOpen()) {
            class_1799 itemStack = this.field_2787.method_7677();
            if (Utils.hasItems(itemStack) || itemStack.method_7909() == class_1802.field_8466) {
                return Utils.openContainer(this.field_2787.method_7677(), this.contents, false);
            }
            if (itemStack.method_58694(class_9334.field_49606) != null || itemStack.method_58694(class_9334.field_49653) != null) {
                method_25419();
                MeteorClient.mc.method_1507(new class_3872(class_3872.class_3931.method_17562(itemStack)));
                return true;
            }
            return false;
        }
        return false;
    }

    public boolean method_25406(double mouseX, double mouseY, int button) {
        return false;
    }

    public boolean method_25404(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256 || MeteorClient.mc.field_1690.field_1822.method_1417(keyCode, scanCode)) {
            method_25419();
            return true;
        }
        return false;
    }

    public boolean method_16803(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            method_25419();
            return true;
        }
        return false;
    }

    protected void method_2389(class_332 context, float delta, int mouseX, int mouseY) {
        Color color = Utils.getShulkerColor(this.storageBlock);
        RenderSystem.setShaderColor(color.r / 255.0f, color.g / 255.0f, color.b / 255.0f, color.a / 255.0f);
        int i = (this.field_22789 - this.field_2792) / 2;
        int j = (this.field_22790 - this.field_2779) / 2;
        context.method_25290(class_1921::method_62277, this.TEXTURE, i, j, 0.0f, 0.0f, this.field_2792, this.field_2779, 256, 256);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
}
