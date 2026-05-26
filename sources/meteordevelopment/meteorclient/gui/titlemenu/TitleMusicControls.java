package meteordevelopment.meteorclient.gui.titlemenu;

import java.awt.Color;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.util.animation.TitleAnimation;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/titlemenu/TitleMusicControls.class */
public class TitleMusicControls {
    private final class_437 parentScreen;
    private int posY;
    private int skipButtonY;
    private final PixelButton muteButton;
    private final PixelButton skipButton;
    private final VolumeSlider volumeSlider;
    private static final class_310 mc = class_310.method_1551();
    private static final Color ACCENT = new Color(Opcode.I2B, 61, 226);
    private final int posX = 10;
    private final int width = 24;
    private final int height = 40;
    private final int skipButtonX = 10;
    private final TitleAnimation hoverAnimation = new TitleAnimation(150, 0.0f, 1.0f, TitleAnimation.Easing.EASE_OUT_CUBIC);
    private boolean isHovered = false;
    private float alpha = 0.7f;
    private boolean isMuted = false;
    private float volume = 0.6f;
    private final String[] musicTracks = {"assets/meteor-client/music/menu_music3.wav", "assets/meteor-client/music/menu_music.wav", "assets/meteor-client/music/menu_music2.wav"};
    private int currentTrackIndex = 0;

    public TitleMusicControls(class_437 parentScreen) {
        this.parentScreen = parentScreen;
        this.posY = (parentScreen.field_22790 / 2) - 20;
        this.skipButtonY = parentScreen.field_22790 - 50;
        this.muteButton = new PixelButton(10, this.posY, 24, 24, "MUTE");
        this.skipButton = new PixelButton(10, this.skipButtonY, 24, 24, "SKIP");
        this.volumeSlider = new VolumeSlider(10, this.posY + 24 + 5, 24, 6);
        this.volumeSlider.setValue(this.volume);
    }

    public void render(class_332 context, int mouseX, int mouseY, float delta) {
        int baseY = (this.parentScreen.field_22790 / 2) - 40;
        this.muteButton.x = 20 + 60;
        this.muteButton.y = baseY + Opcode.IDIV;
        this.volumeSlider.x = 20 + 30;
        this.volumeSlider.y = baseY + 28 + 90;
        this.skipButton.x = 20 - 10;
        this.skipButton.y = baseY + 28 + 80;
        boolean newHovered = isMouseOver(mouseX, mouseY);
        if (newHovered != this.isHovered) {
            this.isHovered = newHovered;
            this.hoverAnimation.reset();
        }
        this.hoverAnimation.update(delta);
        float hp = this.hoverAnimation.getValue();
        this.alpha = this.isHovered ? 0.7f + (0.3f * hp) : 0.5f - (0.2f * hp);
        this.muteButton.render(context, mouseX, mouseY, delta);
        this.volumeSlider.render(context, mouseX, mouseY, delta);
        this.skipButton.render(context, mouseX, mouseY, delta);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (this.muteButton.isMouseOver((int) mouseX, (int) mouseY)) {
                toggleMute();
                return true;
            }
            if (this.skipButton.isMouseOver((int) mouseX, (int) mouseY)) {
                skipTrack();
                return true;
            }
            if (this.volumeSlider.mouseClicked(mouseX, mouseY)) {
                return true;
            }
            return false;
        }
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == 0 && this.volumeSlider.mouseDragged(mouseX, mouseY)) {
            this.volume = this.volumeSlider.getValue();
            updateVolume();
            return true;
        }
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            this.volumeSlider.mouseReleased();
            return false;
        }
        return false;
    }

    private boolean isMouseOver(int mouseX, int mouseY) {
        boolean main = mouseX >= 7 && mouseX <= 37 && mouseY >= this.posY - 3 && mouseY <= (this.posY + 40) + 3;
        boolean skip = mouseX >= 7 && mouseX <= 37 && mouseY >= this.skipButtonY - 3 && mouseY <= (this.skipButtonY + 24) + 3;
        return main || skip;
    }

    private void toggleMute() {
        this.isMuted = !this.isMuted;
        TitleMusicPlayer.getInstance().setVolume(this.isMuted ? 0.0f : this.volume);
        this.muteButton.setText(this.isMuted ? "UNMUTE" : "MUTE");
    }

    private void updateVolume() {
        if (!this.isMuted) {
            TitleMusicPlayer.getInstance().setVolume(this.volume);
        }
    }

    private void skipTrack() {
        this.currentTrackIndex = (this.currentTrackIndex + 1) % this.musicTracks.length;
        TitleMusicPlayer.getInstance().playFromResource(this.musicTracks[this.currentTrackIndex]);
    }

    private static int toARGB(Color c) {
        return ((c.getAlpha() & 255) << 24) | ((c.getRed() & 255) << 16) | ((c.getGreen() & 255) << 8) | (c.getBlue() & 255);
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/titlemenu/TitleMusicControls$PixelButton.class */
    private class PixelButton {
        int x;
        int y;
        int width;
        int height;
        String text;
        boolean isHovered = false;
        TitleAnimation pressAnimation = new TitleAnimation(150, 0.0f, 1.0f, TitleAnimation.Easing.EASE_OUT_CUBIC);

        PixelButton(int x, int y, int width, int height, String text) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.text = text;
        }

        void render(class_332 context, int mouseX, int mouseY, float delta) {
            Color color;
            boolean wasHovered = this.isHovered;
            this.isHovered = isMouseOver(mouseX, mouseY);
            if (wasHovered != this.isHovered) {
                this.pressAnimation.reset();
            }
            this.pressAnimation.update(delta);
            float progress = this.pressAnimation.getValue();
            Color base = TitleMusicControls.ACCENT;
            Color hover = new Color(Math.min(255, base.getRed() + 40), Math.min(255, base.getGreen() + 40), Math.min(255, base.getBlue() + 40));
            if (this.isHovered) {
                color = new Color(base.getRed() + ((int) ((hover.getRed() - base.getRed()) * progress)), base.getGreen() + ((int) ((hover.getGreen() - base.getGreen()) * progress)), base.getBlue() + ((int) ((hover.getBlue() - base.getBlue()) * progress)), (int) (255.0f * TitleMusicControls.this.alpha));
            } else {
                color = new Color(base.getRed(), base.getGreen(), base.getBlue(), (int) (180.0f * TitleMusicControls.this.alpha));
            }
            context.method_25294(this.x, this.y, this.x + this.width, this.y + this.height, TitleMusicControls.toARGB(new Color(25, 25, 35, (int) (180.0f * TitleMusicControls.this.alpha))));
            if (this.isHovered) {
                context.method_25294(this.x - 1, this.y - 1, this.x + this.width + 1, this.y, TitleMusicControls.toARGB(TitleMusicControls.ACCENT));
                context.method_25294(this.x - 1, this.y + this.height, this.x + this.width + 1, this.y + this.height + 1, TitleMusicControls.toARGB(TitleMusicControls.ACCENT));
            }
            if (!this.text.equals("MUTE") && !this.text.equals("UNMUTE")) {
                if (this.text.equals("SKIP")) {
                    drawSkipIcon(context);
                    return;
                }
                return;
            }
            drawMuteIcon(context, TitleMusicControls.this.isMuted);
        }

        void drawPixelatedButton(class_332 context, Color color) {
            context.method_25294(this.x, this.y, this.x + this.width, this.y + this.height, TitleMusicControls.toARGB(color));
            Color light = new Color(Math.min(255, color.getRed() + 70), Math.min(255, color.getGreen() + 70), Math.min(255, color.getBlue() + 90), color.getAlpha());
            Color dark = new Color(Math.max(0, color.getRed() - 70), Math.max(0, color.getGreen() - 70), Math.max(0, color.getBlue() - 50), color.getAlpha());
            context.method_25294(this.x, this.y, this.x + this.width, this.y + 1, TitleMusicControls.toARGB(light));
            context.method_25294(this.x, this.y + 1, this.x + 1, (this.y + this.height) - 1, TitleMusicControls.toARGB(light));
            context.method_25294(this.x, (this.y + this.height) - 1, this.x + this.width, this.y + this.height, TitleMusicControls.toARGB(dark));
            context.method_25294((this.x + this.width) - 1, this.y + 1, this.x + this.width, (this.y + this.height) - 1, TitleMusicControls.toARGB(dark));
        }

        void drawMuteIcon(class_332 context, boolean muted) {
            int sx = this.x + ((this.width - 14) / 2);
            int sy = this.y + ((this.height - 14) / 2);
            context.method_25294(sx + 2, sy + 4, sx + 5, sy + 10, TitleMusicControls.toARGB(new Color(220, 220, 220)));
            context.method_25294(sx + 3, sy + 5, sx + 4, sy + 9, TitleMusicControls.toARGB(new Color(Opcode.FCMPG, Opcode.FCMPG, Opcode.FCMPG)));
            context.method_25294(sx + 3, sy + 6, sx + 4, sy + 8, TitleMusicControls.toARGB(new Color(50, 50, 50)));
            for (int i = 0; i < 3; i++) {
                context.method_25294(sx + 5, sy + 4 + i, (sx + 8) - i, sy + 5 + i, TitleMusicControls.toARGB(new Color(200, 200, 200)));
            }
            for (int i2 = 0; i2 < 3; i2++) {
                context.method_25294(sx + 5, (sy + 9) - i2, (sx + 8) - i2, (sy + 10) - i2, TitleMusicControls.toARGB(new Color(200, 200, 200)));
            }
            if (!muted) {
                for (int i3 = 0; i3 < 4; i3++) {
                    context.method_25294(sx + 8 + i3, sy + 5 + (i3 / 2), sx + 9 + i3, (sy + 9) - (i3 / 2), TitleMusicControls.toARGB(new Color(Opcode.TABLESWITCH, 100, 220, (int) (255.0f * (1.0f - (i3 / 7.0f))))));
                }
                for (int i4 = 0; i4 < 4; i4++) {
                    context.method_25294(sx + 10 + i4, sy + 4 + (i4 / 2), sx + 11 + i4, (sy + 10) - (i4 / 2), TitleMusicControls.toARGB(new Color(100, Opcode.TABLESWITCH, 255, (int) (200.0f * (1.0f - (i4 / 7.0f))))));
                }
                return;
            }
            for (int i5 = 0; i5 < 7; i5++) {
                context.method_25294(sx + 8 + i5, sy + 3 + i5, sx + 9 + i5, sy + 4 + i5, TitleMusicControls.toARGB(new Color(255, 70, 70)));
            }
            for (int i6 = 0; i6 < 7; i6++) {
                context.method_25294(sx + 8 + i6, (sy + 10) - i6, sx + 9 + i6, (sy + 11) - i6, TitleMusicControls.toARGB(new Color(255, 50, 50)));
            }
        }

        void drawSkipIcon(class_332 context) {
            int sx = this.x + ((this.width - 14) / 2);
            int sy = this.y + ((this.height - 14) / 2);
            context.method_25294(sx + 2, sy + 3, sx + 3, sy + 11, -1);
            context.method_25294(sx + 2, sy + 3, sx + 7, sy + 7, -1);
            context.method_25294(sx + 2, sy + 11, sx + 7, sy + 7, -1);
            context.method_25294(sx + 7, sy + 3, sx + 8, sy + 11, -1);
            context.method_25294(sx + 7, sy + 3, sx + 12, sy + 7, -1);
            context.method_25294(sx + 7, sy + 11, sx + 12, sy + 7, -1);
            context.method_25294(sx + 12, sy + 3, sx + 13, sy + 11, -1);
        }

        boolean isMouseOver(int mx, int my) {
            return mx >= this.x && mx <= this.x + this.width && my >= this.y && my <= this.y + this.height;
        }

        void setText(String t) {
            this.text = t;
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/titlemenu/TitleMusicControls$VolumeSlider.class */
    private class VolumeSlider {
        int x;
        int y;
        int width;
        int height;
        float value = 0.6f;
        boolean isDragging = false;
        TitleAnimation hoverAnim = new TitleAnimation(150, 0.0f, 1.0f, TitleAnimation.Easing.EASE_OUT_CUBIC);
        boolean isHovered = false;

        VolumeSlider(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        void render(class_332 context, int mouseX, int mouseY, float delta) {
            boolean wasHovered = this.isHovered;
            this.isHovered = isMouseOver(mouseX, mouseY) || this.isDragging;
            int i = this.x - 10;
            int i2 = this.y - 20;
            int i3 = this.width + 20;
            int i4 = this.height + 40;
            int barY = this.y + (this.height / 2);
            context.method_25294(this.x, barY - 2, this.x + this.width, barY + 2, TitleMusicControls.toARGB(new Color(20, 20, 30, (int) (200.0f * TitleMusicControls.this.alpha))));
            int fillX = this.x + ((int) (this.value * this.width));
            context.method_25294(this.x, barY - 2, fillX, barY + 2, TitleMusicControls.toARGB(TitleMusicControls.ACCENT));
            if (wasHovered != this.isHovered) {
                this.hoverAnim.reset();
            }
            this.hoverAnim.update(delta);
            float p = this.hoverAnim.getValue();
            int centerX = this.x + (this.width / 2);
            context.method_25294(centerX - 1, this.y, centerX + 1, this.y + this.height, TitleMusicControls.toARGB(new Color(255, 255, 255, (int) (80.0f * TitleMusicControls.this.alpha))));
            int fillY = (this.y + this.height) - ((int) (this.value * this.height));
            context.method_25294(centerX - 1, fillY, centerX + 1, this.y + this.height, TitleMusicControls.toARGB(new Color(TitleMusicControls.ACCENT.getRed(), TitleMusicControls.ACCENT.getGreen(), TitleMusicControls.ACCENT.getBlue(), (int) (220.0f * TitleMusicControls.this.alpha))));
            if (this.isHovered) {
                Color glow = new Color(TitleMusicControls.ACCENT.getRed(), TitleMusicControls.ACCENT.getGreen(), TitleMusicControls.ACCENT.getBlue(), (int) (80.0f * p));
                context.method_25294(centerX - 4, fillY - 2, centerX + 4, fillY + 2, TitleMusicControls.toARGB(glow));
            }
            int knobX = this.x + ((int) (this.value * this.width));
            context.method_25294(knobX - 4, barY - 4, knobX + 4, barY + 4, TitleMusicControls.toARGB(new Color(20, 20, 20, 255)));
            context.method_25294(knobX - 3, barY - 3, knobX + 3, barY + 3, TitleMusicControls.toARGB(new Color(200, 200, 200, 255)));
        }

        boolean mouseClicked(double mx, double my) {
            if (isMouseOver((int) mx, (int) my)) {
                this.isDragging = true;
                updateFromMouse(mx);
                return true;
            }
            return false;
        }

        boolean mouseDragged(double mx, double my) {
            if (this.isDragging) {
                updateFromMouse(mx);
                return true;
            }
            return false;
        }

        void mouseReleased() {
            this.isDragging = false;
        }

        private void updateFromMouse(double mx) {
            this.value = (float) Math.max(0.0d, Math.min(1.0d, (mx - ((double) this.x)) / ((double) this.width)));
        }

        boolean isMouseOver(int mx, int my) {
            return mx >= this.x && mx <= this.x + this.width && my >= this.y && my <= this.y + this.height;
        }

        float getValue() {
            return this.value;
        }

        void setValue(float v) {
            this.value = Math.max(0.0f, Math.min(1.0f, v));
        }
    }
}
