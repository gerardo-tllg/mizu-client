package meteordevelopment.meteorclient.gui.titlemenu;

import meteordevelopment.meteorclient.util.animation.TitleAnimation;
import meteordevelopment.meteorclient.util.animation.TitleAnimation.Easing;
import meteordevelopment.meteorclient.util.audio.TitleAudioAnalyzer;
import meteordevelopment.meteorclient.gui.titlemenu.TitleMusicControls;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import meteordevelopment.meteorclient.util.animation.Ascii;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CustomTitleScreen extends Screen {

    // Change these to your client's name/version
    private static final String CLIENT_NAME = "R  E  V  I  V  E     ";
    private static final String CLIENT_VERSION = "PRE-RELEASE-0.0.1";

    // Accent color — matches the purple in fabric.mod.json: "145,61,226"
    private static final String NEKO_GIF_PATH = "textures/gui/neko.gif";

    // Accent color 2014 matches the purple in fabric.mod.json: "145,61,226"
    private static final Color ACCENT = new Color(145, 61, 226);

    private float animationTime = 0;
    private final TitleAnimation pulseAnimation = new TitleAnimation(2000, 0, 1, Easing.LINEAR);

    private final TitleAnimation bassAnimation = new TitleAnimation(300, 0, 1, Easing.EASE_OUT_EXPO);
    private final TitleAnimation trebleAnimation = new TitleAnimation(200, 0, 1, Easing.EASE_OUT_CUBIC);
    private float bassVizLevel = 0.0f;
    private float trebleVizLevel = 0.0f;
    private final Random random = new Random();

    private TitleAnimation bassImpactAnimation = null;
    private TitleAnimation trebleImpactAnimation = null;
    private static final int BASS_IMPACT_DURATION = 500;
    private static final int TREBLE_IMPACT_DURATION = 400;

    private final int MAX_WARP_POINTS = 3;
    private final float[] warpX = new float[MAX_WARP_POINTS];
    private final float[] warpY = new float[MAX_WARP_POINTS];
    private final float[] warpPhase = new float[MAX_WARP_POINTS];
    private final float[] warpAmplitude = new float[MAX_WARP_POINTS];

    private float chromaticOffset = 0.0f;
    private long lastBassTime = 0;
    private long lastTrebleTime = 0;

    private final List<MusicParticle> particles = new ArrayList<>();
    private final int MAX_PARTICLES = 60;
    private int particleSpawnCounter = 0;

    private final List<DeveloperInfo> developers = new ArrayList<>();
    private float scrollX = 0;

    private final List<Droplet> activeDroplets = new ArrayList<>();

    private Ascii nekoAscii;

    private TitleMusicControls musicControls;

    public CustomTitleScreen() {
        super(Text.literal(CLIENT_NAME));

        for (int i = 0; i < MAX_WARP_POINTS; i++) {
            warpX[i] = random.nextFloat();
            warpY[i] = random.nextFloat();
            warpPhase[i] = random.nextFloat() * (float) Math.PI * 2;
            warpAmplitude[i] = 0.1f + random.nextFloat() * 0.3f;
        }

        initDevelopers();

        if (!TitleMusicPlayer.getInstance().isPlaying()) {
            TitleMusicPlayer.getInstance().playMenuMusic();
        }
    }

    // ---- Edit this list with your own names/colors ----
    private void initDevelopers() {
        developers.add(new DeveloperInfo("Crownizzle", new Color(145, 61, 226)));
        developers.add(new DeveloperInfo("_Synful", new Color(90, 200, 255)));
        developers.add(new DeveloperInfo("Zandax", new Color(255, 215, 0)));
        // Add more devs here if you want
    }

    public class ThemedButton extends ButtonWidget {

        public ThemedButton(int x, int y, int w, int h, Text text, PressAction onPress) {
            super(x, y, w, h, text, onPress, DEFAULT_NARRATION_SUPPLIER);
        }

        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            boolean hovered = isHovered();

            int bgColor = hovered
                ? toARGB(new Color(120, 60, 180, 220)) // hover purple
                : toARGB(new Color(80, 40, 120, 200)); // base purple

            int borderColor = toARGB(new Color(160, 80, 255, 255));

            // background
            context.fill(getX(), getY(), getX() + width, getY() + height, bgColor);

            // border
            context.drawBorder(getX(), getY(), width, height, borderColor);

            // text
            context.drawCenteredTextWithShadow(
                MinecraftClient.getInstance().textRenderer,
                getMessage(),
                getX() + width / 2,
                getY() + (height - 8) / 2,
                0xFFFFFF
            );
        }
    }

    @Override
    protected void init() {
        int buttonWidth = 200;
        int buttonHeight = 20;
        int spacing = 25;
        int startY = this.height / 2 + 4;
        int centerX = (this.width - buttonWidth) / 2;

        addDrawableChild(new ThemedButton(
            centerX, startY, buttonWidth, buttonHeight,
            Text.literal("Singleplayer"),
            btn -> client.setScreen(new SelectWorldScreen(this))
        ));

        addDrawableChild(new ThemedButton(
            centerX, startY + spacing, buttonWidth, buttonHeight,
            Text.literal("Multiplayer"),
            btn -> client.setScreen(new MultiplayerScreen(this))
        ));

        int half = buttonWidth / 2 - 5;

        addDrawableChild(new ThemedButton(
            centerX,
            startY + 2 * spacing,
            half,
            buttonHeight,
            Text.literal("Options"),
            btn -> client.setScreen(new OptionsScreen(this, client.options))
        ));

        addDrawableChild(new ThemedButton(
            centerX + half + 10,
            startY + 2 * spacing,
            half,
            buttonHeight,
            Text.literal("Quit"),
            btn -> client.stop()
        ));
        musicControls = new TitleMusicControls(this);
        nekoAscii = new Ascii(this.width, this.height, NEKO_GIF_PATH, 70);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        animationTime += delta * 0.01f;

        TitleAudioAnalyzer analyzer = TitleAudioAnalyzer.getInstance();
        float bassLevel = analyzer.getBassLevel();
        float trebleLevel = analyzer.getTrebleLevel();

        long now = System.currentTimeMillis();

        if (analyzer.isBassBeat() && (now - lastBassTime > 200)) {
            bassImpactAnimation = new TitleAnimation(BASS_IMPACT_DURATION, 1.0f, 0.0f, Easing.EASE_OUT_EXPO);
            lastBassTime = now;
        }
        if (analyzer.isTrebleBeat() && (now - lastTrebleTime > 150)) {
            trebleImpactAnimation = new TitleAnimation(TREBLE_IMPACT_DURATION, 1.0f, 0.0f, Easing.EASE_OUT_CUBIC);
            chromaticOffset = 3.0f + trebleLevel * 5.0f;
            lastTrebleTime = now;
        }

        if (bassImpactAnimation != null) {
            bassImpactAnimation.update(delta);
            if (bassImpactAnimation.isDone()) bassImpactAnimation = null;
        }
        if (trebleImpactAnimation != null) {
            trebleImpactAnimation.update(delta);
            if (trebleImpactAnimation.isDone()) trebleImpactAnimation = null;
            chromaticOffset *= 0.92f;
        } else {
            chromaticOffset *= 0.8f;
        }

        if (bassLevel > bassVizLevel) {
            bassVizLevel = bassLevel * 0.9f + bassVizLevel * 0.1f;
            bassAnimation.reset(bassVizLevel * 0.7f, bassVizLevel);
        } else {
            bassVizLevel = Math.max(0, bassVizLevel - 0.03f);
        }
        if (trebleLevel > trebleVizLevel) {
            trebleVizLevel = trebleLevel * 0.7f + trebleVizLevel * 0.3f;
            trebleAnimation.reset(trebleVizLevel * 0.7f, trebleVizLevel);
        } else {
            trebleVizLevel = Math.max(0, trebleVizLevel - 0.04f);
        }

        if (pulseAnimation.isDone()) pulseAnimation.reset();

        float pulseValue = (float) (0.5 + 0.5 * Math.sin(pulseAnimation.getValue() * Math.PI * 2));

        drawBackground(context);
        context.getMatrices().push(); // start shake

        float shake = bassLevel * 6.0f; // instead of bassVizLevel// use smoothed value (better than raw)

        context.getMatrices().translate(
            (random.nextFloat() - 0.5f) * shake,
            (random.nextFloat() - 0.5f) * shake,
            0
        );
        drawLogoEffect(context, bassVizLevel, trebleVizLevel, pulseValue);
        updateParticles(delta, bassVizLevel, trebleVizLevel);
        drawParticles(context);
        drawDeveloperScroll(context);
        if (nekoAscii != null) {
            nekoAscii.updatePosition(this.width, this.height);
            nekoAscii.render(context, mouseX, mouseY, delta);
        }
        context.getMatrices().pop(); // stop shake

        super.render(context, mouseX, mouseY, delta);

        drawVersionText(context);
        musicControls.render(context, mouseX, mouseY, delta);

        String credits = "|Made with \u2665| ";
        int cw = client.textRenderer.getWidth(credits);
        context.drawTextWithShadow(client.textRenderer, credits,
            this.width - cw - 5, this.height - 15, toARGB(Color.MAGENTA));
    }

    // ---- Background ----
    private void drawBackground(DrawContext context) {
        context.fill(0, 0, this.width, this.height, toARGB(new Color(20, 20, 22)));

        Color darkened = new Color(
            Math.max(0, ACCENT.getRed() - 180),
            Math.max(0, ACCENT.getGreen() - 180),
            Math.max(0, ACCENT.getBlue() - 180));

        int gradH = this.height / 2;
        for (int y = 0; y < gradH; y++) {
            float alpha = 0.1f * (1 - (float) y / gradH);
            Color grad = new Color(darkened.getRed(), darkened.getGreen(), darkened.getBlue(), (int) (alpha * 255));
            context.fill(0, this.height / 4 + y, this.width, this.height / 4 + y + 1, toARGB(grad));
            context.fill(0, this.height / 4 - y, this.width, this.height / 4 - y + 1, toARGB(grad));
        }
    }

    // ---- Animated logo ----
    private void drawLogoEffect(DrawContext context, float bassLevel, float trebleLevel, float pulseValue) {
        float logoScale = 4.0f + bassLevel * 1.2f;
        if (bassImpactAnimation != null) logoScale += bassImpactAnimation.getValue() * 1.5f;

        int textWidth = (int) (client.textRenderer.getWidth(CLIENT_NAME) * logoScale * 0.9f);
        int x = (this.width - textWidth) / 2;
        int y = this.height / 4;

        float shakeX = 0, shakeY = 0;
        if (bassImpactAnimation != null) {
            float impact = bassImpactAnimation.getValue();
            shakeX = (random.nextFloat() * 2 - 1) * impact * 8.0f;
            shakeY = (random.nextFloat() * 2 - 1) * impact * 5.0f;
        }

        // Glow shadow
        if (trebleLevel > 0.3f || bassLevel > 0.4f) {
            int glowAlpha = (int)(50 + bassLevel * 150);

            Color glow = new Color(
                ACCENT.getRed(),
                ACCENT.getGreen(),
                ACCENT.getBlue(),
                glowAlpha
            );
            drawWarpedText(context, CLIENT_NAME, x + shakeX + 3, y + shakeY + 3, logoScale, toARGB(glow),
                bassLevel * 0.5f, trebleLevel * 0.5f, false);
        }

        Color highlight = new Color(
            Math.min(255, ACCENT.getRed() + 40),
            Math.min(255, ACCENT.getGreen() + 40),
            Math.min(255, ACCENT.getBlue() + 40));

        Color finalColor = new Color(
            clamp((int) (ACCENT.getRed() + (highlight.getRed() - ACCENT.getRed()) * (pulseValue + bassLevel * 0.4f))),
            clamp((int) (ACCENT.getGreen() + (highlight.getGreen() - ACCENT.getGreen()) * (pulseValue + trebleLevel * 0.25f))),
            clamp((int) (ACCENT.getBlue() + (highlight.getBlue() - ACCENT.getBlue()) * (pulseValue + (bassLevel + trebleLevel) * 0.2f))),
            clamp((int) (240 + bassLevel * 15)));

        drawWarpedText(context, CLIENT_NAME, x + shakeX, y + shakeY, logoScale, toARGB(finalColor),
            bassLevel, trebleLevel, chromaticOffset > 0.5f);

        drawSludgeDrips(context, CLIENT_NAME, x + shakeX, y + shakeY, logoScale, finalColor, bassLevel);

        // Subtitle "beta"
        String subtitle = "B E T A";
        int sw = client.textRenderer.getWidth(subtitle);
        float sx = this.width / 2f - sw / 2f + (float) Math.sin(animationTime * 0.8) * 3.0f;
        float sy = y + 30 * logoScale / 4 + 10 + (float) Math.cos(animationTime * 0.5) * 2.0f;

        float strobeSpeed = 5.0f;
        for (int i = 0; i < subtitle.length(); i++) {
            String ch = String.valueOf(subtitle.charAt(i));
            float phase = animationTime * strobeSpeed + i * 0.3f;
            int r = clamp((int) (100 + Math.sin(phase) * 155));
            int g = clamp((int) (100 + Math.sin(phase + 2.0f) * 155));
            int b = clamp((int) (220 + Math.cos(phase) * 35));

            float cx = sx + client.textRenderer.getWidth(subtitle.substring(0, i));
            context.drawText(client.textRenderer, ch, (int) cx, (int) sy, toARGB(new Color(r, g, b)), false);
        }
    }

    // ---- Drips ----
    private void drawSludgeDrips(DrawContext context, String text, float x, float y, float scale, Color color, float bassLevel) {
        if (bassImpactAnimation != null && bassImpactAnimation.getValue() > 0.7f) {
            float charSpacing = client.textRenderer.getWidth("X") * scale * 0.9f;
            int dropsToSpawn = 1 + random.nextInt(2);
            for (int i = 0; i < dropsToSpawn; i++) {
                int ci = random.nextInt(text.length());
                float dx = x + ci * charSpacing + random.nextFloat() * 0.8f * client.textRenderer.getWidth(String.valueOf(text.charAt(ci))) * scale;
                float dy = y + client.textRenderer.fontHeight * scale;
                activeDroplets.add(new Droplet(dx, dy, 2.0f + random.nextFloat() * 2.0f, 1.0f + random.nextFloat() * 3.0f,
                    new Color(color.getRed(), color.getGreen(), color.getBlue(), 180 + random.nextInt(75))));
            }
        }

        for (int i = activeDroplets.size() - 1; i >= 0; i--) {
            Droplet d = activeDroplets.get(i);
            d.y += d.speed;
            if (d.y > this.height) { activeDroplets.remove(i); continue; }

            context.fill((int)(d.x - d.size/2), (int)(d.y - d.size/2), (int)(d.x + d.size/2), (int)(d.y + d.size/2), toARGB(d.color));
            for (int t = 1; t <= 3; t++) {
                float ta = d.color.getAlpha() * (0.5f - t * 0.15f);
                if (ta <= 0) continue;
                float ts = d.size * (0.7f - t * 0.15f);
                context.fill((int)(d.x - ts/2), (int)(d.y - ts/2 - t * 2), (int)(d.x + ts/2), (int)(d.y + ts/2 - t * 2),
                    toARGB(new Color(d.color.getRed(), d.color.getGreen(), d.color.getBlue(), (int) ta)));
            }
        }
    }

    // ---- Scrolling dev bar ----
    private void drawDeveloperScroll(DrawContext context) {
        scrollX -= 0.5f * (1.0f + trebleVizLevel * 0.5f);
        int y = 10;
        int totalWidth = 0;
        for (DeveloperInfo d : developers) totalWidth += client.textRenderer.getWidth(d.name + "   \u2022   ");
        if (scrollX < -totalWidth) scrollX = this.width;

        context.fill(0, y - 2, this.width, y + 12, toARGB(new Color(20, 20, 20, 120)));

        float cx = scrollX;
        for (int i = 0; i < developers.size(); i++) {
            DeveloperInfo d = developers.get(i);
            int w = client.textRenderer.getWidth(d.name);
            if (cx + w > 0 && cx < this.width) {
                float yo = bassVizLevel * 2 * (float) Math.sin(animationTime * 3 + i);
                context.drawTextWithShadow(client.textRenderer, d.name, (int) cx, (int) (y + yo), toARGB(d.color));
            }
            cx += w;
            if (i < developers.size() - 1) {
                String sep = "   \u2022   ";
                int sw = client.textRenderer.getWidth(sep);
                if (cx + sw > 0 && cx < this.width)
                    context.drawTextWithShadow(client.textRenderer, sep, (int) cx, y, toARGB(Color.GRAY));
                cx += sw;
            }
        }
    }

    // ---- Warped text (the wobbly logo render) ----
    private void drawWarpedText(DrawContext context, String text, float x, float y, float scale,
                                int color, float bassLevel, float trebleLevel, boolean chromatic) {
        for (int i = 0; i < MAX_WARP_POINTS; i++) {
            warpPhase[i] += 0.003f + bassLevel * 0.008f + trebleLevel * 0.015f * (i % 2 == 0 ? 1 : -1);
            if (warpPhase[i] > Math.PI * 2) warpPhase[i] -= (float) Math.PI * 2;
            warpAmplitude[i] = 0.15f + bassLevel * 0.5f + trebleLevel * 0.2f;
        }

        if (chromatic && chromaticOffset > 0.5f) {
            int alpha = (color >> 24) & 0xFF;
            drawWarpedTextColor(context, text, x - chromaticOffset, y, scale, toARGB(new Color(255, 50, 50, alpha)), bassLevel, trebleLevel);
            drawWarpedTextColor(context, text, x, y, scale, toARGB(new Color(50, 255, 50, alpha)), bassLevel, trebleLevel);
            drawWarpedTextColor(context, text, x + chromaticOffset, y, scale, toARGB(new Color(50, 50, 255, alpha)), bassLevel, trebleLevel);
        } else {
            drawWarpedTextColor(context, text, x, y, scale, color, bassLevel, trebleLevel);
        }
    }

    private void drawWarpedTextColor(DrawContext context, String text, float x, float y, float scale,
                                     int color, float bassLevel, float trebleLevel) {
        for (int i = 0; i < text.length(); i++) {
            String ch = String.valueOf(text.charAt(i));
            float charX = x + i * (client.textRenderer.getWidth("X") * scale * 0.9f);

            float ox = 0, oy = 0;
            for (int j = 0; j < MAX_WARP_POINTS; j++) {
                float posX = (float) i / text.length();
                float dx = posX - warpX[j], dy = 0.5f - warpY[j];
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                float inf = (1.0f - Math.min(1.0f, dist * 2)) * warpAmplitude[j];

                ox += (float) Math.sin(warpPhase[j] + i * 0.2f) * inf * 3
                    + (float) Math.sin(animationTime * 3 + i * 0.8f) * trebleLevel * 3;
                oy += (float) Math.cos(warpPhase[j] + i * 0.3f) * inf * 3
                    + (float) Math.sin(animationTime * 1.5 + i * 0.15f) * bassLevel * 4;
            }

            int charColor = color;
            if (trebleLevel > 0.5f) {
                float boost = (trebleLevel - 0.5f) * 2.0f;
                int r = Math.min(255, ((color >> 16) & 0xFF) + (int) (boost * 80));
                int g = Math.min(255, ((color >> 8) & 0xFF) + (int) (boost * 80));
                int b = Math.min(255, (color & 0xFF) + (int) (boost * 80));
                charColor = (color & 0xFF000000) | (r << 16) | (g << 8) | b;
            }

            context.getMatrices().push();
            context.getMatrices().translate(charX + ox, y + oy, 0);
            context.getMatrices().scale(scale, scale, 1.0f);
            context.drawText(client.textRenderer, ch, 0, 0, charColor, false);
            context.getMatrices().pop();
        }
    }

    // ---- Particles ----
    private void updateParticles(float delta, float bassLevel, float trebleLevel) {
        particleSpawnCounter++;
        int rate = Math.max(1, (int) (30 - Math.min(25, (bassLevel + trebleLevel) * 20)));

        if (particleSpawnCounter % rate == 0 && particles.size() < MAX_PARTICLES) {
            float speedF = 0.6f + bassLevel * 3.0f; // bass-heavy motion
            float angle = random.nextFloat() * (float) Math.PI * 2;
            float speed = (0.3f + random.nextFloat() * 1.2f) * speedF;

            Color c = (bassLevel > trebleLevel)
                ? new Color(ACCENT.getRed(), (int)(ACCENT.getGreen() * 0.7f), (int)(ACCENT.getBlue() * 0.5f), 120 + (int)(random.nextFloat() * 80))
                : new Color((int)(ACCENT.getRed() * 0.5f), (int)(ACCENT.getGreen() * 0.7f), ACCENT.getBlue(), 120 + (int)(random.nextFloat() * 80));

            MusicParticle p = new MusicParticle(
                this.width / 2f + (random.nextFloat() * 2 - 1) * 120,
                this.height / 4f + (random.nextFloat() * 2 - 1) * 72,
                1 + random.nextFloat() * 2 + bassLevel * 3, c);
            p.vx = (float) Math.cos(angle) * speed;
            p.vy = (float) Math.sin(angle) * speed;
            particles.add(p);
        }

        for (int i = particles.size() - 1; i >= 0; i--) {
            MusicParticle p = particles.get(i);
            p.x += p.vx * delta;
            p.y += p.vy * delta;

            float dx = this.width / 2f - p.x, dy = this.height / 4f - p.y;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            if (dist > 5) {
                p.vx += (dx / dist) * bassLevel * 0.3f * delta * 0.5f;
                p.vy += (dy / dist) * bassLevel * 0.3f * delta * 0.5f;
            }
            p.vx += (random.nextFloat() * 2 - 1) * trebleLevel * 0.2f * delta * 0.5f;
            p.vy += (random.nextFloat() * 2 - 1) * trebleLevel * 0.2f * delta * 0.5f;
            p.vx *= 0.99f; p.vy *= 0.99f;
            p.life -= delta * (0.15f + (bassLevel + trebleLevel) * 0.15f);

            if (p.life <= 0 || p.x < -20 || p.x > this.width + 20 || p.y < -20 || p.y > this.height + 20)
                particles.remove(i);
        }
    }

    private void drawParticles(DrawContext context) {
        for (MusicParticle p : particles) {
            int alpha = (int) (p.color.getAlpha() * p.life);
            float size = p.size * p.life;
            context.fill((int)(p.x - size/2), (int)(p.y - size/2), (int)(p.x + size/2), (int)(p.y + size/2),
                toARGB(new Color(p.color.getRed(), p.color.getGreen(), p.color.getBlue(), alpha)));
        }
    }

    // ---- Version text ----
    private void drawVersionText(DrawContext context) {
        String version = CLIENT_NAME + " " + CLIENT_VERSION;
        context.drawTextWithShadow(client.textRenderer, version, 5, this.height - 15, toARGB(new Color(20, 240, 20)));
    }

    // ---- Input forwarding to music controls ----
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (musicControls != null && musicControls.mouseClicked(mouseX, mouseY, button)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (musicControls != null && musicControls.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) return true;
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (musicControls != null && musicControls.mouseReleased(mouseX, mouseY, button)) return true;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldCloseOnEsc() { return true; }

    // ---- Helpers ----
    private static int toARGB(Color c) {
        return ((c.getAlpha() & 0xFF) << 24) | ((c.getRed() & 0xFF) << 16) | ((c.getGreen() & 0xFF) << 8) | (c.getBlue() & 0xFF);
    }

    private static int clamp(int v) { return Math.max(0, Math.min(255, v)); }

    // ---- Inner classes ----
    private static class MusicParticle {
        float x, y, vx, vy, size, life = 1.0f;
        Color color;
        MusicParticle(float x, float y, float size, Color color) {
            this.x = x; this.y = y; this.size = size; this.color = color;
        }
    }

    private static class Droplet {
        float x, y, size, speed;
        Color color;
        Droplet(float x, float y, float size, float speed, Color color) {
            this.x = x; this.y = y; this.size = size; this.speed = speed; this.color = color;
        }
    }

    private static class DeveloperInfo {
        String name;
        Color color;
        DeveloperInfo(String name, Color color) { this.name = name; this.color = color; }
    }
}
