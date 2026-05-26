package meteordevelopment.meteorclient.gui.titlemenu;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.util.animation.Ascii;
import meteordevelopment.meteorclient.util.animation.TitleAnimation;
import meteordevelopment.meteorclient.util.audio.TitleAudioAnalyzer;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_4185;
import net.minecraft.class_429;
import net.minecraft.class_437;
import net.minecraft.class_500;
import net.minecraft.class_526;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/titlemenu/CustomTitleScreen.class */
public class CustomTitleScreen extends class_437 {
    private static final String CLIENT_NAME = "M  I  Z  U     ";
    private static final String CLIENT_VERSION = "PRE-RELEASE-0.0.1";
    private static final String NEKO_GIF_PATH = "textures/gui/neko.gif";
    private static final Color ACCENT = new Color(Opcode.I2B, 61, 226);
    private float animationTime;
    private final TitleAnimation pulseAnimation;
    private final TitleAnimation bassAnimation;
    private final TitleAnimation trebleAnimation;
    private float bassVizLevel;
    private float trebleVizLevel;
    private final Random random;
    private TitleAnimation bassImpactAnimation;
    private TitleAnimation trebleImpactAnimation;
    private static final int BASS_IMPACT_DURATION = 500;
    private static final int TREBLE_IMPACT_DURATION = 400;
    private final int MAX_WARP_POINTS = 3;
    private final float[] warpX;
    private final float[] warpY;
    private final float[] warpPhase;
    private final float[] warpAmplitude;
    private float chromaticOffset;
    private long lastBassTime;
    private long lastTrebleTime;
    private final List<MusicParticle> particles;
    private final int MAX_PARTICLES = 60;
    private int particleSpawnCounter;
    private final List<DeveloperInfo> developers;
    private float scrollX;
    private final List<Droplet> activeDroplets;
    private Ascii nekoAscii;
    private TitleMusicControls musicControls;

    public CustomTitleScreen() {
        super(class_2561.method_43470(CLIENT_NAME));
        this.animationTime = 0.0f;
        this.pulseAnimation = new TitleAnimation(2000L, 0.0f, 1.0f, TitleAnimation.Easing.LINEAR);
        this.bassAnimation = new TitleAnimation(300L, 0.0f, 1.0f, TitleAnimation.Easing.EASE_OUT_EXPO);
        this.trebleAnimation = new TitleAnimation(200L, 0.0f, 1.0f, TitleAnimation.Easing.EASE_OUT_CUBIC);
        this.bassVizLevel = 0.0f;
        this.trebleVizLevel = 0.0f;
        this.random = new Random();
        this.bassImpactAnimation = null;
        this.trebleImpactAnimation = null;
        this.MAX_WARP_POINTS = 3;
        this.warpX = new float[3];
        this.warpY = new float[3];
        this.warpPhase = new float[3];
        this.warpAmplitude = new float[3];
        this.chromaticOffset = 0.0f;
        this.lastBassTime = 0L;
        this.lastTrebleTime = 0L;
        this.particles = new ArrayList();
        this.MAX_PARTICLES = 60;
        this.particleSpawnCounter = 0;
        this.developers = new ArrayList();
        this.scrollX = 0.0f;
        this.activeDroplets = new ArrayList();
        for (int i = 0; i < 3; i++) {
            this.warpX[i] = this.random.nextFloat();
            this.warpY[i] = this.random.nextFloat();
            this.warpPhase[i] = this.random.nextFloat() * 3.1415927f * 2.0f;
            this.warpAmplitude[i] = 0.1f + (this.random.nextFloat() * 0.3f);
        }
        initDevelopers();
        if (!TitleMusicPlayer.getInstance().isPlaying()) {
            TitleMusicPlayer.getInstance().playMenuMusic();
        }
    }

    private void initDevelopers() {
        this.developers.add(new DeveloperInfo("swavez", new Color(Opcode.I2B, 61, 226)));
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/titlemenu/CustomTitleScreen$ThemedButton.class */
    public class ThemedButton extends class_4185 {
        public ThemedButton(final CustomTitleScreen this$0, int x, int y, int w, int h, class_2561 text, class_4185.class_4241 onPress) {
            super(x, y, w, h, text, onPress, field_40754);
        }

        public void method_48579(class_332 context, int mouseX, int mouseY, float delta) {
            int argb;
            boolean hovered = method_49606();
            if (hovered) {
                argb = CustomTitleScreen.toARGB(new Color(Opcode.ISHL, 60, Opcode.GETFIELD, 220));
            } else {
                argb = CustomTitleScreen.toARGB(new Color(80, 40, Opcode.ISHL, 200));
            }
            int bgColor = argb;
            int borderColor = CustomTitleScreen.toARGB(new Color(Opcode.IF_ICMPNE, 80, 255, 255));
            context.method_25294(method_46426(), method_46427(), method_46426() + this.field_22758, method_46427() + this.field_22759, bgColor);
            context.method_49601(method_46426(), method_46427(), this.field_22758, this.field_22759, borderColor);
            context.method_27534(class_310.method_1551().field_1772, method_25369(), method_46426() + (this.field_22758 / 2), method_46427() + ((this.field_22759 - 8) / 2), 16777215);
        }
    }

    protected void method_25426() {
        int startY = (this.field_22790 / 2) + 4;
        int centerX = (this.field_22789 - 200) / 2;
        method_37063(new ThemedButton(this, centerX, startY, 200, 20, class_2561.method_43470("Singleplayer"), btn -> {
            this.field_22787.method_1507(new class_526(this));
        }));
        method_37063(new ThemedButton(this, centerX, startY + 25, 200, 20, class_2561.method_43470("Multiplayer"), btn2 -> {
            this.field_22787.method_1507(new class_500(this));
        }));
        int half = (200 / 2) - 5;
        method_37063(new ThemedButton(this, centerX, startY + (2 * 25), half, 20, class_2561.method_43470("Options"), btn3 -> {
            this.field_22787.method_1507(new class_429(this, this.field_22787.field_1690));
        }));
        method_37063(new ThemedButton(this, centerX + half + 10, startY + (2 * 25), half, 20, class_2561.method_43470("Quit"), btn4 -> {
            this.field_22787.method_1490();
        }));
        this.musicControls = new TitleMusicControls(this);
        this.nekoAscii = new Ascii(this.field_22789, this.field_22790, NEKO_GIF_PATH, 70);
    }

    public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
        this.animationTime += delta * 0.01f;
        TitleAudioAnalyzer analyzer = TitleAudioAnalyzer.getInstance();
        float bassLevel = analyzer.getBassLevel();
        float trebleLevel = analyzer.getTrebleLevel();
        long now = System.currentTimeMillis();
        if (analyzer.isBassBeat() && now - this.lastBassTime > 200) {
            this.bassImpactAnimation = new TitleAnimation(500L, 1.0f, 0.0f, TitleAnimation.Easing.EASE_OUT_EXPO);
            this.lastBassTime = now;
        }
        if (analyzer.isTrebleBeat() && now - this.lastTrebleTime > 150) {
            this.trebleImpactAnimation = new TitleAnimation(400L, 1.0f, 0.0f, TitleAnimation.Easing.EASE_OUT_CUBIC);
            this.chromaticOffset = 3.0f + (trebleLevel * 5.0f);
            this.lastTrebleTime = now;
        }
        if (this.bassImpactAnimation != null) {
            this.bassImpactAnimation.update(delta);
            if (this.bassImpactAnimation.isDone()) {
                this.bassImpactAnimation = null;
            }
        }
        if (this.trebleImpactAnimation != null) {
            this.trebleImpactAnimation.update(delta);
            if (this.trebleImpactAnimation.isDone()) {
                this.trebleImpactAnimation = null;
            }
            this.chromaticOffset *= 0.92f;
        } else {
            this.chromaticOffset *= 0.8f;
        }
        if (bassLevel > this.bassVizLevel) {
            this.bassVizLevel = (bassLevel * 0.9f) + (this.bassVizLevel * 0.1f);
            this.bassAnimation.reset(this.bassVizLevel * 0.7f, this.bassVizLevel);
        } else {
            this.bassVizLevel = Math.max(0.0f, this.bassVizLevel - 0.03f);
        }
        if (trebleLevel > this.trebleVizLevel) {
            this.trebleVizLevel = (trebleLevel * 0.7f) + (this.trebleVizLevel * 0.3f);
            this.trebleAnimation.reset(this.trebleVizLevel * 0.7f, this.trebleVizLevel);
        } else {
            this.trebleVizLevel = Math.max(0.0f, this.trebleVizLevel - 0.04f);
        }
        if (this.pulseAnimation.isDone()) {
            this.pulseAnimation.reset();
        }
        float pulseValue = (float) (0.5d + (0.5d * Math.sin(((double) this.pulseAnimation.getValue()) * 3.141592653589793d * 2.0d)));
        drawBackground(context);
        context.method_51448().method_22903();
        float shake = bassLevel * 6.0f;
        context.method_51448().method_46416((this.random.nextFloat() - 0.5f) * shake, (this.random.nextFloat() - 0.5f) * shake, 0.0f);
        drawLogoEffect(context, this.bassVizLevel, this.trebleVizLevel, pulseValue);
        updateParticles(delta, this.bassVizLevel, this.trebleVizLevel);
        drawParticles(context);
        drawDeveloperScroll(context);
        if (this.nekoAscii != null) {
            this.nekoAscii.updatePosition(this.field_22789, this.field_22790);
            this.nekoAscii.render(context, mouseX, mouseY, delta);
        }
        context.method_51448().method_22909();
        super.method_25394(context, mouseX, mouseY, delta);
        drawVersionText(context);
        this.musicControls.render(context, mouseX, mouseY, delta);
        int cw = this.field_22787.field_1772.method_1727("|Made with ♥| ");
        context.method_25303(this.field_22787.field_1772, "|Made with ♥| ", (this.field_22789 - cw) - 5, this.field_22790 - 15, toARGB(Color.MAGENTA));
    }

    private void drawBackground(class_332 context) {
        context.method_25294(0, 0, this.field_22789, this.field_22790, toARGB(new Color(20, 20, 22)));
        Color darkened = new Color(Math.max(0, ACCENT.getRed() - Opcode.GETFIELD), Math.max(0, ACCENT.getGreen() - Opcode.GETFIELD), Math.max(0, ACCENT.getBlue() - Opcode.GETFIELD));
        int gradH = this.field_22790 / 2;
        for (int y = 0; y < gradH; y++) {
            float alpha = 0.1f * (1.0f - (y / gradH));
            Color grad = new Color(darkened.getRed(), darkened.getGreen(), darkened.getBlue(), (int) (alpha * 255.0f));
            context.method_25294(0, (this.field_22790 / 4) + y, this.field_22789, (this.field_22790 / 4) + y + 1, toARGB(grad));
            context.method_25294(0, (this.field_22790 / 4) - y, this.field_22789, ((this.field_22790 / 4) - y) + 1, toARGB(grad));
        }
    }

    private void drawLogoEffect(class_332 context, float bassLevel, float trebleLevel, float pulseValue) {
        float logoScale = 4.0f + (bassLevel * 1.2f);
        if (this.bassImpactAnimation != null) {
            logoScale += this.bassImpactAnimation.getValue() * 1.5f;
        }
        int textWidth = (int) (this.field_22787.field_1772.method_1727(CLIENT_NAME) * logoScale * 0.9f);
        int x = (this.field_22789 - textWidth) / 2;
        int y = this.field_22790 / 4;
        float shakeX = 0.0f;
        float shakeY = 0.0f;
        if (this.bassImpactAnimation != null) {
            float impact = this.bassImpactAnimation.getValue();
            shakeX = ((this.random.nextFloat() * 2.0f) - 1.0f) * impact * 8.0f;
            shakeY = ((this.random.nextFloat() * 2.0f) - 1.0f) * impact * 5.0f;
        }
        if (trebleLevel > 0.3f || bassLevel > 0.4f) {
            int glowAlpha = (int) (50.0f + (bassLevel * 150.0f));
            Color glow = new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), glowAlpha);
            drawWarpedText(context, CLIENT_NAME, x + shakeX + 3.0f, y + shakeY + 3.0f, logoScale, toARGB(glow), bassLevel * 0.5f, trebleLevel * 0.5f, false);
        }
        Color highlight = new Color(Math.min(255, ACCENT.getRed() + 40), Math.min(255, ACCENT.getGreen() + 40), Math.min(255, ACCENT.getBlue() + 40));
        Color finalColor = new Color(clamp((int) (ACCENT.getRed() + ((highlight.getRed() - ACCENT.getRed()) * (pulseValue + (bassLevel * 0.4f))))), clamp((int) (ACCENT.getGreen() + ((highlight.getGreen() - ACCENT.getGreen()) * (pulseValue + (trebleLevel * 0.25f))))), clamp((int) (ACCENT.getBlue() + ((highlight.getBlue() - ACCENT.getBlue()) * (pulseValue + ((bassLevel + trebleLevel) * 0.2f))))), clamp((int) (240.0f + (bassLevel * 15.0f))));
        drawWarpedText(context, CLIENT_NAME, x + shakeX, y + shakeY, logoScale, toARGB(finalColor), bassLevel, trebleLevel, this.chromaticOffset > 0.5f);
        drawSludgeDrips(context, CLIENT_NAME, x + shakeX, y + shakeY, logoScale, finalColor, bassLevel);
        int sw = this.field_22787.field_1772.method_1727("B E T A");
        float sx = ((this.field_22789 / 2.0f) - (sw / 2.0f)) + (((float) Math.sin(((double) this.animationTime) * 0.8d)) * 3.0f);
        float sy = y + ((30.0f * logoScale) / 4.0f) + 10.0f + (((float) Math.cos(((double) this.animationTime) * 0.5d)) * 2.0f);
        for (int i = 0; i < "B E T A".length(); i++) {
            String ch = String.valueOf("B E T A".charAt(i));
            float phase = (this.animationTime * 5.0f) + (i * 0.3f);
            int r = clamp((int) (100.0d + (Math.sin(phase) * 155.0d)));
            int g = clamp((int) (100.0d + (Math.sin(phase + 2.0f) * 155.0d)));
            int b = clamp((int) (220.0d + (Math.cos(phase) * 35.0d)));
            float cx = sx + this.field_22787.field_1772.method_1727("B E T A".substring(0, i));
            context.method_51433(this.field_22787.field_1772, ch, (int) cx, (int) sy, toARGB(new Color(r, g, b)), false);
        }
    }

    private void drawSludgeDrips(class_332 context, String text, float x, float y, float scale, Color color, float bassLevel) {
        if (this.bassImpactAnimation != null && this.bassImpactAnimation.getValue() > 0.7f) {
            float charSpacing = this.field_22787.field_1772.method_1727("X") * scale * 0.9f;
            int dropsToSpawn = 1 + this.random.nextInt(2);
            for (int i = 0; i < dropsToSpawn; i++) {
                int ci = this.random.nextInt(text.length());
                float dx = x + (ci * charSpacing) + (this.random.nextFloat() * 0.8f * this.field_22787.field_1772.method_1727(String.valueOf(text.charAt(ci))) * scale);
                Objects.requireNonNull(this.field_22787.field_1772);
                float dy = y + (9.0f * scale);
                this.activeDroplets.add(new Droplet(dx, dy, 2.0f + (this.random.nextFloat() * 2.0f), 1.0f + (this.random.nextFloat() * 3.0f), new Color(color.getRed(), color.getGreen(), color.getBlue(), Opcode.GETFIELD + this.random.nextInt(75))));
            }
        }
        for (int i2 = this.activeDroplets.size() - 1; i2 >= 0; i2--) {
            Droplet d = this.activeDroplets.get(i2);
            d.y += d.speed;
            if (d.y > this.field_22790) {
                this.activeDroplets.remove(i2);
            } else {
                context.method_25294((int) (d.x - (d.size / 2.0f)), (int) (d.y - (d.size / 2.0f)), (int) (d.x + (d.size / 2.0f)), (int) (d.y + (d.size / 2.0f)), toARGB(d.color));
                for (int t = 1; t <= 3; t++) {
                    float ta = d.color.getAlpha() * (0.5f - (t * 0.15f));
                    if (ta > 0.0f) {
                        float ts = d.size * (0.7f - (t * 0.15f));
                        context.method_25294((int) (d.x - (ts / 2.0f)), (int) ((d.y - (ts / 2.0f)) - (t * 2)), (int) (d.x + (ts / 2.0f)), (int) ((d.y + (ts / 2.0f)) - (t * 2)), toARGB(new Color(d.color.getRed(), d.color.getGreen(), d.color.getBlue(), (int) ta)));
                    }
                }
            }
        }
    }

    private void drawDeveloperScroll(class_332 context) {
        this.scrollX -= 0.5f * (1.0f + (this.trebleVizLevel * 0.5f));
        int totalWidth = 0;
        Iterator<DeveloperInfo> it = this.developers.iterator();
        while (it.hasNext()) {
            totalWidth += this.field_22787.field_1772.method_1727(it.next().name + "   •   ");
        }
        if (this.scrollX < (-totalWidth)) {
            this.scrollX = this.field_22789;
        }
        context.method_25294(0, 10 - 2, this.field_22789, 10 + 12, toARGB(new Color(20, 20, 20, Opcode.ISHL)));
        float cx = this.scrollX;
        for (int i = 0; i < this.developers.size(); i++) {
            DeveloperInfo d = this.developers.get(i);
            int w = this.field_22787.field_1772.method_1727(d.name);
            if (cx + w > 0.0f && cx < this.field_22789) {
                float yo = this.bassVizLevel * 2.0f * ((float) Math.sin((this.animationTime * 3.0f) + i));
                context.method_25303(this.field_22787.field_1772, d.name, (int) cx, (int) (10 + yo), toARGB(d.color));
            }
            cx += w;
            if (i < this.developers.size() - 1) {
                int sw = this.field_22787.field_1772.method_1727("   •   ");
                if (cx + sw > 0.0f && cx < this.field_22789) {
                    context.method_25303(this.field_22787.field_1772, "   •   ", (int) cx, 10, toARGB(Color.GRAY));
                }
                cx += sw;
            }
        }
    }

    private void drawWarpedText(class_332 context, String text, float x, float y, float scale, int color, float bassLevel, float trebleLevel, boolean chromatic) {
        for (int i = 0; i < 3; i++) {
            float[] fArr = this.warpPhase;
            int i2 = i;
            fArr[i2] = fArr[i2] + 0.003f + (bassLevel * 0.008f) + (trebleLevel * 0.015f * (i % 2 == 0 ? 1 : -1));
            if (this.warpPhase[i] > 6.283185307179586d) {
                float[] fArr2 = this.warpPhase;
                int i3 = i;
                fArr2[i3] = fArr2[i3] - 6.2831855f;
            }
            this.warpAmplitude[i] = 0.15f + (bassLevel * 0.5f) + (trebleLevel * 0.2f);
        }
        if (chromatic && this.chromaticOffset > 0.5f) {
            int alpha = (color >> 24) & 255;
            drawWarpedTextColor(context, text, x - this.chromaticOffset, y, scale, toARGB(new Color(255, 50, 50, alpha)), bassLevel, trebleLevel);
            drawWarpedTextColor(context, text, x, y, scale, toARGB(new Color(50, 255, 50, alpha)), bassLevel, trebleLevel);
            drawWarpedTextColor(context, text, x + this.chromaticOffset, y, scale, toARGB(new Color(50, 50, 255, alpha)), bassLevel, trebleLevel);
            return;
        }
        drawWarpedTextColor(context, text, x, y, scale, color, bassLevel, trebleLevel);
    }

    private void drawWarpedTextColor(class_332 context, String text, float x, float y, float scale, int color, float bassLevel, float trebleLevel) {
        for (int i = 0; i < text.length(); i++) {
            String ch = String.valueOf(text.charAt(i));
            float charX = x + (i * this.field_22787.field_1772.method_1727("X") * scale * 0.9f);
            float ox = 0.0f;
            float oy = 0.0f;
            for (int j = 0; j < 3; j++) {
                float posX = i / text.length();
                float dx = posX - this.warpX[j];
                float dy = 0.5f - this.warpY[j];
                float dist = (float) Math.sqrt((dx * dx) + (dy * dy));
                float inf = (1.0f - Math.min(1.0f, dist * 2.0f)) * this.warpAmplitude[j];
                ox += (((float) Math.sin(this.warpPhase[j] + (i * 0.2f))) * inf * 3.0f) + (((float) Math.sin((this.animationTime * 3.0f) + (i * 0.8f))) * trebleLevel * 3.0f);
                oy += (((float) Math.cos(this.warpPhase[j] + (i * 0.3f))) * inf * 3.0f) + (((float) Math.sin((((double) this.animationTime) * 1.5d) + ((double) (i * 0.15f)))) * bassLevel * 4.0f);
            }
            int charColor = color;
            if (trebleLevel > 0.5f) {
                float boost = (trebleLevel - 0.5f) * 2.0f;
                int r = Math.min(255, ((color >> 16) & 255) + ((int) (boost * 80.0f)));
                int g = Math.min(255, ((color >> 8) & 255) + ((int) (boost * 80.0f)));
                int b = Math.min(255, (color & 255) + ((int) (boost * 80.0f)));
                charColor = (color & (-16777216)) | (r << 16) | (g << 8) | b;
            }
            context.method_51448().method_22903();
            context.method_51448().method_46416(charX + ox, y + oy, 0.0f);
            context.method_51448().method_22905(scale, scale, 1.0f);
            context.method_51433(this.field_22787.field_1772, ch, 0, 0, charColor, false);
            context.method_51448().method_22909();
        }
    }

    private void updateParticles(float delta, float bassLevel, float trebleLevel) {
        Color color;
        this.particleSpawnCounter++;
        int rate = Math.max(1, (int) (30.0f - Math.min(25.0f, (bassLevel + trebleLevel) * 20.0f)));
        if (this.particleSpawnCounter % rate == 0 && this.particles.size() < 60) {
            float speedF = 0.6f + (bassLevel * 3.0f);
            float angle = this.random.nextFloat() * 3.1415927f * 2.0f;
            float speed = (0.3f + (this.random.nextFloat() * 1.2f)) * speedF;
            if (bassLevel > trebleLevel) {
                color = new Color(ACCENT.getRed(), (int) (ACCENT.getGreen() * 0.7f), (int) (ACCENT.getBlue() * 0.5f), Opcode.ISHL + ((int) (this.random.nextFloat() * 80.0f)));
            } else {
                color = new Color((int) (ACCENT.getRed() * 0.5f), (int) (ACCENT.getGreen() * 0.7f), ACCENT.getBlue(), Opcode.ISHL + ((int) (this.random.nextFloat() * 80.0f)));
            }
            Color c = color;
            MusicParticle p = new MusicParticle((this.field_22789 / 2.0f) + (((this.random.nextFloat() * 2.0f) - 1.0f) * 120.0f), (this.field_22790 / 4.0f) + (((this.random.nextFloat() * 2.0f) - 1.0f) * 72.0f), 1.0f + (this.random.nextFloat() * 2.0f) + (bassLevel * 3.0f), c);
            p.vx = ((float) Math.cos(angle)) * speed;
            p.vy = ((float) Math.sin(angle)) * speed;
            this.particles.add(p);
        }
        for (int i = this.particles.size() - 1; i >= 0; i--) {
            MusicParticle p2 = this.particles.get(i);
            p2.x += p2.vx * delta;
            p2.y += p2.vy * delta;
            float dx = (this.field_22789 / 2.0f) - p2.x;
            float dy = (this.field_22790 / 4.0f) - p2.y;
            float dist = (float) Math.sqrt((dx * dx) + (dy * dy));
            if (dist > 5.0f) {
                p2.vx += (dx / dist) * bassLevel * 0.3f * delta * 0.5f;
                p2.vy += (dy / dist) * bassLevel * 0.3f * delta * 0.5f;
            }
            p2.vx += ((this.random.nextFloat() * 2.0f) - 1.0f) * trebleLevel * 0.2f * delta * 0.5f;
            p2.vy += ((this.random.nextFloat() * 2.0f) - 1.0f) * trebleLevel * 0.2f * delta * 0.5f;
            p2.vx *= 0.99f;
            p2.vy *= 0.99f;
            p2.life -= delta * (0.15f + ((bassLevel + trebleLevel) * 0.15f));
            if (p2.life <= 0.0f || p2.x < -20.0f || p2.x > this.field_22789 + 20 || p2.y < -20.0f || p2.y > this.field_22790 + 20) {
                this.particles.remove(i);
            }
        }
    }

    private void drawParticles(class_332 context) {
        for (MusicParticle p : this.particles) {
            int alpha = (int) (p.color.getAlpha() * p.life);
            float size = p.size * p.life;
            context.method_25294((int) (p.x - (size / 2.0f)), (int) (p.y - (size / 2.0f)), (int) (p.x + (size / 2.0f)), (int) (p.y + (size / 2.0f)), toARGB(new Color(p.color.getRed(), p.color.getGreen(), p.color.getBlue(), alpha)));
        }
    }

    private void drawVersionText(class_332 context) {
        context.method_25303(this.field_22787.field_1772, "M  I  Z  U      PRE-RELEASE-0.0.1", 5, this.field_22790 - 15, toARGB(new Color(20, 240, 20)));
    }

    public boolean method_25402(double mouseX, double mouseY, int button) {
        if (this.musicControls == null || !this.musicControls.mouseClicked(mouseX, mouseY, button)) {
            return super.method_25402(mouseX, mouseY, button);
        }
        return true;
    }

    public boolean method_25403(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.musicControls == null || !this.musicControls.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
            return super.method_25403(mouseX, mouseY, button, deltaX, deltaY);
        }
        return true;
    }

    public boolean method_25406(double mouseX, double mouseY, int button) {
        if (this.musicControls == null || !this.musicControls.mouseReleased(mouseX, mouseY, button)) {
            return super.method_25406(mouseX, mouseY, button);
        }
        return true;
    }

    public boolean method_25422() {
        return true;
    }

    private static int toARGB(Color c) {
        return ((c.getAlpha() & 255) << 24) | ((c.getRed() & 255) << 16) | ((c.getGreen() & 255) << 8) | (c.getBlue() & 255);
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/titlemenu/CustomTitleScreen$MusicParticle.class */
    private static class MusicParticle {
        float x;
        float y;
        float vx;
        float vy;
        float size;
        float life = 1.0f;
        Color color;

        MusicParticle(float x, float y, float size, Color color) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.color = color;
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/titlemenu/CustomTitleScreen$Droplet.class */
    private static class Droplet {
        float x;
        float y;
        float size;
        float speed;
        Color color;

        Droplet(float x, float y, float size, float speed, Color color) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.speed = speed;
            this.color = color;
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/titlemenu/CustomTitleScreen$DeveloperInfo.class */
    private static class DeveloperInfo {
        String name;
        Color color;

        DeveloperInfo(String name, Color color) {
            this.name = name;
            this.color = color;
        }
    }
}
