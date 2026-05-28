/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.utils.player.TitleScreenCredits;
import meteordevelopment.meteorclient.utils.render.WaveRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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

    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderHead(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        int w = this.width;
        int h = this.height;

        // Solid deep ocean background
        context.fill(0, 0, w, h, 0xFF050e1a);

        // Water kanji watermark — very subtle, centered
        int kanjiScale = 12;
        int kanjiW = textRenderer.getWidth("水") * kanjiScale;
        int kanjiH = textRenderer.fontHeight * kanjiScale;
        context.getMatrices().push();
        context.getMatrices().translate(w / 2.0f - (kanjiW / 2.0f), h / 2.0f - (kanjiH / 2.0f) - 20, 0);
        context.getMatrices().scale(kanjiScale, kanjiScale, 1.0f);
        context.drawText(textRenderer, "水", 0, 0, 0xFF0a1e30, false);
        context.getMatrices().pop();

        // Wave layers at bottom 20%
        WaveRenderer.renderWaves(context, w, h, System.currentTimeMillis());
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRenderTail(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (Config.get().titleScreenCredits.get()) TitleScreenCredits.render(context);

        int w = this.width;
        int h = this.height;
        long now = System.currentTimeMillis();

        // Cover the vanilla Minecraft logo area
        int logoTop = h / 4 - 10;
        int logoH = 44;
        context.fill(0, logoTop - 8, w, logoTop + logoH + 8, 0xFF050e1a);

        // Ripple circles from center
        WaveRenderer.renderContinuousRipples(context, w / 2, h / 3, now);

        // MIZU logo — large, centered, teal
        int titleScale = 5;
        String titleText = "MIZU";
        int titleW = textRenderer.getWidth(titleText) * titleScale;
        int titleY = h / 4;
        context.getMatrices().push();
        context.getMatrices().translate(w / 2.0f - titleW / 2.0f, titleY, 0);
        context.getMatrices().scale(titleScale, titleScale, 1.0f);
        context.drawText(textRenderer, titleText, 0, 0, 0xFF1D9E75, false);
        context.getMatrices().pop();

        // Subtitle
        int subtitleScale = 2;
        String subtitle = "水  ·  utility client  ·  1.21.5";
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

        // Version text bottom right
        String versionText = "Mizu 1.21.5  ·  swavez";
        int versionX = w - textRenderer.getWidth(versionText) - 4;
        int versionY = h - textRenderer.fontHeight - 4;
        context.drawText(textRenderer, versionText, versionX, versionY, 0xFF185FA5, false);
    }

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
            // Rain dot — 2x4 pixels, #378ADD at 40% opacity
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
