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
        // NOTE: do NOT call context.fill() here — DrawContext fills are batched and
        // execute AFTER MeshRenderer shader passes, which would paint over the waves.
        // The shader itself provides the full dark ocean background.
        float timeSeconds = (System.currentTimeMillis() % 1000000L) / 1000.0f;
        MizuTitleShader.render(timeSeconds, mc.getWindow().getFramebufferWidth(), mc.getWindow().getFramebufferHeight());
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRenderTail(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        int w = this.width;
        int h = this.height;

        // Cover vanilla logo/splash remnants — solid dark band across top region.
        // DrawContext fills execute after the shader so they paint over the sky area
        // cleanly while the shader's wave area (below ~44% from top) shows through.
        context.fill(0, 0, w, h / 4 + 70, 0xFF050e1a);

        // CPU wave crests — reliable DrawContext rendering that always shows.
        // Draws in the lower portion of the screen where the shader ocean is.
        drawCPUWaves(context, w, h);

        // 水 kanji watermark — large, centered, behind MIZU text
        int titleScale = 5;
        int kanjiScale = 14;
        int kanjiW = textRenderer.getWidth("水") * kanjiScale;
        int kanjiH = textRenderer.fontHeight * kanjiScale;
        int titleY = h / 6;
        int kanjiCenterY = titleY + (textRenderer.fontHeight * titleScale) / 2;
        context.getMatrices().push();
        context.getMatrices().translate(w / 2.0f - kanjiW / 2.0f, kanjiCenterY - kanjiH / 2.0f, 0);
        context.getMatrices().scale(kanjiScale, kanjiScale, 1.0f);
        context.drawText(textRenderer, "水", 0, 0, 0xFF0D3048, false);
        context.getMatrices().pop();

        // MIZU — large teal, at Y = ~16.7% (h/6), safely above all vanilla buttons
        String titleText = "MIZU";
        int titleW = textRenderer.getWidth(titleText) * titleScale;
        context.getMatrices().push();
        context.getMatrices().translate(w / 2.0f - titleW / 2.0f, titleY, 0);
        context.getMatrices().scale(titleScale, titleScale, 1.0f);
        context.drawText(textRenderer, titleText, 0, 0, 0xFF1D9E75, false);
        context.getMatrices().pop();

        // Subtitle — just below MIZU
        int subtitleScale = 2;
        String subtitle = "utility client  ·  1.21.5";
        int subtitleW = textRenderer.getWidth(subtitle) * subtitleScale;
        int subtitleY = titleY + textRenderer.fontHeight * titleScale + 4;
        context.getMatrices().push();
        context.getMatrices().translate(w / 2.0f - subtitleW / 2.0f, subtitleY, 0);
        context.getMatrices().scale(subtitleScale, subtitleScale, 1.0f);
        context.drawText(textRenderer, subtitle, 0, 0, 0xFF378ADD, false);
        context.getMatrices().pop();

        // Rain particles
        if (!particlesInit) initParticles(w, h);
        updateAndDrawParticles(context, w, h, delta);

        // "Mizu · by swavez" — top right only, no bottom-right duplicate
        net.minecraft.text.MutableText creditText = Text.empty();
        creditText.append(Text.literal("Mizu").setStyle(Style.EMPTY.withColor(0x1D9E75)));
        creditText.append(Text.literal("  ·  by swavez").setStyle(Style.EMPTY.withColor(0x378ADD)));
        int credW = textRenderer.getWidth(creditText);
        context.drawTextWithShadow(textRenderer, creditText, w - 3 - credW, 3, 0xFFFFFFFF);
    }

    // ---- CPU wave crests ----

    @Unique
    private static float cpuPeak(float x, float cx, float width, float height) {
        float d = (x - cx) / width;
        return height * (float) Math.exp(-d * d * 3.5);
    }

    @Unique
    private static float cpuWaveSurf(float px, float tm, float scale) {
        float slow = tm * 0.18f;
        float p1 = cpuPeak(px, 0.08f + (float)Math.sin(slow*0.7f)*0.04f,  0.14f, 0.22f*scale);
        float p2 = cpuPeak(px, 0.28f + (float)Math.sin(slow*0.5f+1.2f)*0.05f, 0.18f, 0.28f*scale);
        float p3 = cpuPeak(px, 0.52f + (float)Math.sin(slow*0.6f+0.8f)*0.04f, 0.22f, 0.20f*scale);
        float p4 = cpuPeak(px, 0.72f + (float)Math.sin(slow*0.4f+2.1f)*0.05f, 0.16f, 0.25f*scale);
        float p5 = cpuPeak(px, 0.92f + (float)Math.sin(slow*0.55f+1.5f)*0.03f, 0.12f, 0.18f*scale);
        return p1+p2+p3+p4+p5;
    }

    @Unique
    private static void drawCPUWaves(DrawContext context, int w, int h) {
        float tm = (System.currentTimeMillis() % 1000000L) / 1000.0f;
        int safeTop = h / 4 + 75; // don't draw over the UI fill area
        int step = 3;

        for (int xi = 0; xi < w; xi += step) {
            float px = (float) xi / w;

            // surf1 — primary wave (brightest crest)
            float s1 = 0.28f + cpuWaveSurf(px, tm, 1.0f);
            int y1 = h - (int)(s1 * h); // convert GL Y to screen Y
            if (y1 >= safeTop && y1 < h - 4) {
                context.fill(xi, y1, xi + step, y1 + 2, 0xCC1D9E75);
            }

            // surf2 — secondary wave
            float s2 = 0.22f + cpuWaveSurf(px, tm * 1.1f + 0.5f, 0.78f);
            int y2 = h - (int)(s2 * h);
            if (y2 >= safeTop && y2 < h - 4) {
                context.fill(xi, y2, xi + step, y2 + 2, 0x990D7058);
            }

            // surf3 — tertiary wave (subtlest)
            float s3 = 0.17f + cpuWaveSurf(px, tm * 0.9f + 1.2f, 0.58f);
            int y3 = h - (int)(s3 * h);
            if (y3 >= safeTop && y3 < h - 4) {
                context.fill(xi, y3, xi + step, y3 + 1, 0x660A4A38);
            }
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
        int waveBase = (int)(h * 0.8);
        for (int i = 0; i < 20; i++) {
            PARTICLE_Y[i] += PARTICLE_SPEED[i] * delta * 20;
            if (PARTICLE_Y[i] >= waveBase) {
                PARTICLE_Y[i] = 0;
                PARTICLE_X[i] = (int)(Math.random() * w);
            }
            context.fill(PARTICLE_X[i], (int) PARTICLE_Y[i], PARTICLE_X[i] + 1, (int) PARTICLE_Y[i] + 3, 0x66378ADD);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> info) {
        if (Config.get().titleScreenCredits.get() && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (TitleScreenCredits.onClicked(mouseX, mouseY)) info.setReturnValue(true);
        }
    }
}
