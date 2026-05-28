/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.utils.player.TitleScreenCredits;
import meteordevelopment.meteorclient.utils.render.MizuTitleShader;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.LogoDrawer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SplashTextRenderer;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import static meteordevelopment.meteorclient.MeteorClient.mc;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    @Unique private static final int[] PARTICLE_X = new int[20];
    @Unique private static final int[] PARTICLE_Y = new int[20];
    @Unique private static final float[] PARTICLE_SPEED = new float[20];
    @Unique private static boolean particlesInit = false;

    public TitleScreenMixin(Text title) {
        super(title);
    }

    // Cancel panorama — renderBackground() is a no-op in TitleScreen;
    // the panorama lives in renderPanoramaBackground().
    @Inject(method = "renderPanoramaBackground", at = @At("HEAD"), cancellable = true)
    private void cancelPanorama(DrawContext context, float delta, CallbackInfo ci) {
        ci.cancel();
    }

    // Hide the Minecraft logo texture
    @Redirect(method = "render", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/LogoDrawer;draw(Lnet/minecraft/client/gui/DrawContext;IF)V"))
    private void hideVanillaLogo(LogoDrawer drawer, DrawContext context, int screenWidth, float alpha) {}

    // Hide the vanilla yellow splash text
    @Redirect(method = "render", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/screen/SplashTextRenderer;render(Lnet/minecraft/client/gui/DrawContext;ILnet/minecraft/client/font/TextRenderer;I)V"))
    private void hideSplashText(SplashTextRenderer renderer, DrawContext context, int width, TextRenderer tr, int color) {}

    // Dim the vanilla version string — scale 0.5, alpha 15%
    @Redirect(method = "render", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;III)I"))
    private int dimVersionText(DrawContext context, TextRenderer tr, String text, int x, int y, int color) {
        int alpha = (int)(0xFF * 0.15f); // ~15% opacity
        int dimColor = (alpha << 24) | (color & 0x00FFFFFF);
        context.getMatrices().push();
        context.getMatrices().translate(x, y, 0);
        context.getMatrices().scale(0.5f, 0.5f, 1f);
        int result = context.drawTextWithShadow(tr, text, 0, 0, dimColor);
        context.getMatrices().pop();
        return result;
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderHead(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        // Shader provides the atmospheric ocean background (moon, stars, sky gradient)
        float timeSeconds = (System.currentTimeMillis() % 1000000L) / 1000.0f;
        MizuTitleShader.render(timeSeconds, mc.getWindow().getFramebufferWidth(), mc.getWindow().getFramebufferHeight());
        // HD 1px-column wave rendering — submitted to DrawContext before vanilla button
        // fills, so buttons will naturally appear on top of the ocean layers.
        drawHDWaves(context, this.width, this.height);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRenderTail(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        int w = this.width;
        int h = this.height;

        // Solid dark fill covering only the MIZU + subtitle zone (top ~28%).
        // Stops well before buttons (which start at ~35%+), so buttons remain visible.
        // Executes after vanilla buttons in the DrawContext batch, but only covers
        // the top 28% where no buttons exist.
        context.fill(0, 0, w, (int)(h * 0.28f), 0xFF050e1a);

        // MIZU — scale 3 (~60% of previous scale-5), at Y = 15% from top
        int titleScale = 3;
        int titleY = (int)(h * 0.15f);
        String titleText = "MIZU";
        int titleW = textRenderer.getWidth(titleText) * titleScale;
        context.getMatrices().push();
        context.getMatrices().translate(w / 2.0f - titleW / 2.0f, titleY, 0);
        context.getMatrices().scale(titleScale, titleScale, 1.0f);
        context.drawText(textRenderer, titleText, 0, 0, 0xFF1D9E75, false);
        context.getMatrices().pop();

        // Subtitle — scale 0.8 (~40% of previous scale-2), just 2px below MIZU
        float subScale = 0.8f;
        String subtitle = "utility client  ·  1.21.5";
        int subtitleW = (int)(textRenderer.getWidth(subtitle) * subScale);
        int subtitleY = titleY + textRenderer.fontHeight * titleScale + 2;
        context.getMatrices().push();
        context.getMatrices().translate(w / 2.0f - subtitleW / 2.0f, subtitleY, 0);
        context.getMatrices().scale(subScale, subScale, 1.0f);
        context.drawText(textRenderer, subtitle, 0, 0, 0xFF378ADD, false);
        context.getMatrices().pop();

        // Rain particles — only below 75% to clear the button zone
        if (!particlesInit) initParticles(w, h);
        updateAndDrawParticles(context, w, h, delta);

        // "Mizu · by swavez" top-right branding
        net.minecraft.text.MutableText creditText = Text.empty();
        creditText.append(Text.literal("Mizu").setStyle(Style.EMPTY.withColor(0x1D9E75)));
        creditText.append(Text.literal("  ·  by swavez").setStyle(Style.EMPTY.withColor(0x378ADD)));
        int credW = textRenderer.getWidth(creditText);
        context.drawTextWithShadow(textRenderer, creditText, w - 3 - credW, 3, 0xFFFFFFFF);
    }

    // ---- HD 1-pixel-column wave rendering ----

    @Unique
    private static float wavePeak(float x, float cx, float width, float height) {
        float d = (x - cx) / width;
        return height * (float) Math.exp(-d * d * 3.5f);
    }

    @Unique
    private static float waveY(float px, float tm, float scale) {
        float slow = tm * 0.18f;
        float p1 = wavePeak(px, 0.08f + (float)Math.sin(slow*0.7f)*0.04f,  0.14f, 0.22f*scale);
        float p2 = wavePeak(px, 0.28f + (float)Math.sin(slow*0.5f+1.2f)*0.05f, 0.18f, 0.28f*scale);
        float p3 = wavePeak(px, 0.52f + (float)Math.sin(slow*0.6f+0.8f)*0.04f, 0.22f, 0.20f*scale);
        float p4 = wavePeak(px, 0.72f + (float)Math.sin(slow*0.4f+2.1f)*0.05f, 0.16f, 0.25f*scale);
        float p5 = wavePeak(px, 0.92f + (float)Math.sin(slow*0.55f+1.5f)*0.03f, 0.12f, 0.18f*scale);
        return p1 + p2 + p3 + p4 + p5;
    }

    @Unique
    private static void drawHDWaves(DrawContext context, int w, int h) {
        float tm = (System.currentTimeMillis() % 1000000L) / 1000.0f;

        for (int x = 0; x < w; x++) {
            float px = (float) x / w;

            // Compute GL-Y surfaces (0=bottom, 1=top) and convert to screen Y (0=top)
            float s1 = 0.28f + waveY(px, tm, 1.00f);
            float s2 = 0.22f + waveY(px, tm * 1.1f + 0.5f,  0.78f);
            float s3 = 0.17f + waveY(px, tm * 0.9f + 1.2f,  0.58f);
            float s4 = 0.13f + waveY(px, tm * 1.2f + 2.1f,  0.40f);
            float s5 = 0.09f + waveY(px, tm * 0.8f + 3.0f,  0.25f);

            int y1 = h - Math.min(h, Math.max(0, (int)(s1 * h)));
            int y2 = h - Math.min(h, Math.max(0, (int)(s2 * h)));
            int y3 = h - Math.min(h, Math.max(0, (int)(s3 * h)));
            int y4 = h - Math.min(h, Math.max(0, (int)(s4 * h)));
            int y5 = h - Math.min(h, Math.max(0, (int)(s5 * h)));

            // Ensure ascending order (y1 <= y2 <= y3 <= y4 <= y5)
            y2 = Math.max(y1, y2);
            y3 = Math.max(y2, y3);
            y4 = Math.max(y3, y4);
            y5 = Math.max(y4, y5);

            // Layer fills — drawn bottom-up so each layer stacks correctly
            // Layer 1: between wave 1 and wave 2
            if (y2 > y1) context.fill(x, y1, x + 1, y2, 0xFF0a1e30);
            // Layer 2
            if (y3 > y2) context.fill(x, y2, x + 1, y3, 0xFF081828);
            // Layer 3
            if (y4 > y3) context.fill(x, y3, x + 1, y4, 0xFF061220);
            // Layer 4
            if (y5 > y4) context.fill(x, y4, x + 1, y5, 0xFF040e18);
            // Layer 5: from wave 5 to screen bottom
            if (h > y5)  context.fill(x, y5, x + 1, h,  0xFF030a12);

            // Crisp 1×3px crest lines on top of layers
            context.fill(x, y1, x + 1, y1 + 3, 0xFF1D9E75);  // wave 1 — full teal
            context.fill(x, y2, x + 1, y2 + 3, 0xE60F6E56);  // wave 2 — 90%
            context.fill(x, y3, x + 1, y3 + 3, 0xCC085041);  // wave 3 — 80%
            context.fill(x, y4, x + 1, y4 + 3, 0xB204342C);  // wave 4 — 70%
            context.fill(x, y5, x + 1, y5 + 3, 0x9904342C);  // wave 5 — 60%
        }
    }

    // ---- Particles ----

    @Unique
    private static void initParticles(int w, int h) {
        java.util.Random rng = new java.util.Random();
        for (int i = 0; i < 20; i++) {
            PARTICLE_X[i] = rng.nextInt(w);
            PARTICLE_Y[i] = rng.nextInt(h);
            PARTICLE_SPEED[i] = 0.4f + rng.nextFloat() * 0.8f;
        }
        particlesInit = true;
    }

    @Unique
    private void updateAndDrawParticles(DrawContext context, int w, int h, float delta) {
        int waveBase = (int)(h * 0.8f);
        int floorY   = (int)(h * 0.75f);
        for (int i = 0; i < 20; i++) {
            PARTICLE_Y[i] += PARTICLE_SPEED[i] * delta * 20;
            if (PARTICLE_Y[i] >= waveBase) {
                PARTICLE_Y[i] = 0;
                PARTICLE_X[i] = (int)(Math.random() * w);
            }
            if ((int) PARTICLE_Y[i] >= floorY) {
                context.fill(PARTICLE_X[i], (int) PARTICLE_Y[i],
                    PARTICLE_X[i] + 1, (int) PARTICLE_Y[i] + 3, 0x66378ADD);
            }
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> info) {
        if (Config.get().titleScreenCredits.get() && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (TitleScreenCredits.onClicked(mouseX, mouseY)) info.setReturnValue(true);
        }
    }
}
