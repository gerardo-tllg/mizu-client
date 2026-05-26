package meteordevelopment.meteorclient.renderer.text;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Objects;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.class_327;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_9799;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/renderer/text/VanillaTextRenderer.class */
public class VanillaTextRenderer implements TextRenderer {
    public static final VanillaTextRenderer INSTANCE = new VanillaTextRenderer();
    public boolean scaleIndividually;
    private boolean building;
    private final class_9799 buffer = new class_9799(2048);
    private final class_4597.class_4598 immediate = class_4597.method_22991(this.buffer);
    private final class_4587 matrices = new class_4587();
    private final Matrix4f emptyMatrix = new Matrix4f();
    public double scale = 2.0d;
    private double alpha = 1.0d;

    private VanillaTextRenderer() {
    }

    @Override // meteordevelopment.meteorclient.renderer.text.TextRenderer
    public void setAlpha(double a) {
        this.alpha = a;
    }

    @Override // meteordevelopment.meteorclient.renderer.text.TextRenderer
    public double getWidth(String text, int length, boolean shadow) {
        if (text.isEmpty()) {
            return 0.0d;
        }
        if (length != text.length()) {
            text = text.substring(0, length);
        }
        return ((double) (MeteorClient.mc.field_1772.method_1727(text) + (shadow ? 1 : 0))) * this.scale;
    }

    @Override // meteordevelopment.meteorclient.renderer.text.TextRenderer
    public double getHeight(boolean shadow) {
        Objects.requireNonNull(MeteorClient.mc.field_1772);
        return ((double) (9 + (shadow ? 1 : 0))) * this.scale;
    }

    @Override // meteordevelopment.meteorclient.renderer.text.TextRenderer
    public void begin(double scale, boolean scaleOnly, boolean big) {
        if (this.building) {
            throw new RuntimeException("VanillaTextRenderer.begin() called twice");
        }
        this.scale = scale * 2.0d;
        this.building = true;
    }

    @Override // meteordevelopment.meteorclient.renderer.text.TextRenderer
    public double render(String text, double x, double y, Color color, boolean shadow) {
        boolean wasBuilding = this.building;
        if (!wasBuilding) {
            begin();
        }
        double x2 = x + (0.5d * this.scale);
        double y2 = y + (0.5d * this.scale);
        int preA = color.a;
        color.a = (int) ((((double) color.a) / 255.0d) * this.alpha * 255.0d);
        Matrix4f matrix = this.emptyMatrix;
        if (this.scaleIndividually) {
            this.matrices.method_22903();
            this.matrices.method_22905((float) this.scale, (float) this.scale, 1.0f);
            matrix = this.matrices.method_23760().method_23761();
        }
        double x22 = MeteorClient.mc.field_1772.method_27521(text, (float) (x2 / this.scale), (float) (y2 / this.scale), color.getPacked(), shadow, matrix, this.immediate, class_327.class_6415.field_33993, 0, 15728880);
        if (this.scaleIndividually) {
            this.matrices.method_22909();
        }
        color.a = preA;
        if (!wasBuilding) {
            end();
        }
        return (x22 - 1.0d) * this.scale;
    }

    @Override // meteordevelopment.meteorclient.renderer.text.TextRenderer
    public boolean isBuilding() {
        return this.building;
    }

    @Override // meteordevelopment.meteorclient.renderer.text.TextRenderer
    public void end() {
        if (!this.building) {
            throw new RuntimeException("VanillaTextRenderer.end() called without calling begin()");
        }
        Matrix4fStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.pushMatrix();
        if (!this.scaleIndividually) {
            matrixStack.scale((float) this.scale, (float) this.scale, 1.0f);
        }
        this.immediate.method_22993();
        matrixStack.popMatrix();
        this.scale = 2.0d;
        this.building = false;
    }
}
