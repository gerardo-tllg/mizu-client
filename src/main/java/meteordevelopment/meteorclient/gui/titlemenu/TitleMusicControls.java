package meteordevelopment.meteorclient.gui.titlemenu;

import meteordevelopment.meteorclient.util.animation.TitleAnimation;
import meteordevelopment.meteorclient.util.animation.TitleAnimation.Easing;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;

import java.awt.Color;

public class TitleMusicControls {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    private final Screen parentScreen;

    private final int posX = 10;
    private int posY;
    private final int width = 24;
    private final int height = 40;
    private final int skipButtonX = 10;
    private int skipButtonY;

    private final PixelButton muteButton;
    private final PixelButton skipButton;
    private final VolumeSlider volumeSlider;

    private final TitleAnimation hoverAnimation = new TitleAnimation(150, 0, 1, Easing.EASE_OUT_CUBIC);
    private boolean isHovered = false;
    private float alpha = 0.7f;

    private boolean isMuted = false;
    private float volume = 0.6f;


    // Accent color — purple by default to match ReviveClient
    private static final Color ACCENT = new Color(145, 61, 226);

    // Tracks bundled in your resources at assets/meteor-client/music/
    private final String[] musicTracks = {
        "assets/meteor-client/music/menu_music3.wav",
        "assets/meteor-client/music/menu_music.wav",
        "assets/meteor-client/music/menu_music2.wav"
    };
    private int currentTrackIndex = 0;

    public TitleMusicControls(Screen parentScreen) {
        this.parentScreen = parentScreen;
        this.posY = parentScreen.height / 2 - height / 2;
        this.skipButtonY = parentScreen.height - 50;

        this.muteButton = new PixelButton(posX, posY, width, width, "MUTE");
        this.skipButton = new PixelButton(skipButtonX, skipButtonY, width, width, "SKIP");
        this.volumeSlider = new VolumeSlider(posX, posY + width + 5, width, height - width - 10);
        this.volumeSlider.setValue(volume);
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {

        int baseX = 20; // left side position
        int baseY = parentScreen.height / 2 - 40; // vertical center

        int spacing = 28; // space between elements

        muteButton.x = baseX + 60;
        muteButton.y = baseY + 108;

        volumeSlider.x = baseX + 30;
        volumeSlider.y = baseY + spacing + 90;

        skipButton.x = baseX - 10;
        skipButton.y = baseY + spacing + 40 * 2;

        boolean newHovered = isMouseOver(mouseX, mouseY);
        if (newHovered != isHovered) { isHovered = newHovered; hoverAnimation.reset(); }

        hoverAnimation.update(delta);
        float hp = hoverAnimation.getValue();
        alpha = isHovered ? 0.7f + 0.3f * hp : 0.5f - 0.2f * hp;

        muteButton.render(context, mouseX, mouseY, delta);
        volumeSlider.render(context, mouseX, mouseY, delta);
        skipButton.render(context, mouseX, mouseY, delta);
    }
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {

            // MUTE BUTTON
            if (muteButton.isMouseOver((int) mouseX, (int) mouseY)) {
                toggleMute();
                return true;
            }

            // SKIP BUTTON
            if (skipButton.isMouseOver((int) mouseX, (int) mouseY)) {
                skipTrack();
                return true;
            }

            // SLIDER
            if (volumeSlider.mouseClicked(mouseX, mouseY)) {
                return true;
            }
        }

        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == 0 && volumeSlider.mouseDragged(mouseX, mouseY)) {
            volume = volumeSlider.getValue();
            updateVolume();
            return true;
        }
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) volumeSlider.mouseReleased();
        return false;
    }

    private boolean isMouseOver(int mouseX, int mouseY) {
        boolean main = mouseX >= posX - 3 && mouseX <= posX + width + 3 &&
            mouseY >= posY - 3 && mouseY <= posY + height + 3;
        boolean skip = mouseX >= skipButtonX - 3 && mouseX <= skipButtonX + width + 3 &&
            mouseY >= skipButtonY - 3 && mouseY <= skipButtonY + width + 3;
        return main || skip;
    }

    private void toggleMute() {
        isMuted = !isMuted;
        TitleMusicPlayer.getInstance().setVolume(isMuted ? 0 : volume);
        muteButton.setText(isMuted ? "UNMUTE" : "MUTE");
    }

    private void updateVolume() {
        if (!isMuted) TitleMusicPlayer.getInstance().setVolume(volume);
    }

    private void skipTrack() {
        currentTrackIndex = (currentTrackIndex + 1) % musicTracks.length;
        TitleMusicPlayer.getInstance().playFromResource(musicTracks[currentTrackIndex]);
    }

    private static int toARGB(Color c) {
        return ((c.getAlpha() & 0xFF) << 24) | ((c.getRed() & 0xFF) << 16) | ((c.getGreen() & 0xFF) << 8) | (c.getBlue() & 0xFF);
    }

    // ---- Inner: PixelButton ----
    private class PixelButton {
        int x, y, width, height;
        String text;
        boolean isHovered = false;
        TitleAnimation pressAnimation = new TitleAnimation(150, 0, 1, Easing.EASE_OUT_CUBIC);

        PixelButton(int x, int y, int width, int height, String text) {
            this.x = x; this.y = y; this.width = width; this.height = height; this.text = text;
        }

        void render(DrawContext context, int mouseX, int mouseY, float delta) {
            boolean wasHovered = isHovered;
            isHovered = isMouseOver(mouseX, mouseY);

            if (wasHovered != isHovered) pressAnimation.reset();

            pressAnimation.update(delta);
            float progress = pressAnimation.getValue();

            Color base = ACCENT;
            Color hover = new Color(
                Math.min(255, base.getRed() + 40),
                Math.min(255, base.getGreen() + 40),
                Math.min(255, base.getBlue() + 40)
            );

            Color btnColor = isHovered
                ? new Color(
                base.getRed() + (int)((hover.getRed() - base.getRed()) * progress),
                base.getGreen() + (int)((hover.getGreen() - base.getGreen()) * progress),
                base.getBlue() + (int)((hover.getBlue() - base.getBlue()) * progress),
                (int)(255 * alpha))
                : new Color(base.getRed(), base.getGreen(), base.getBlue(), (int)(180 * alpha));

            // smooth button background
            context.fill(x, y, x + width, y + height,
                toARGB(new Color(
                    25, 25, 35,
                    (int)(180 * alpha)
                ))
            );

// glow on hover
            if (isHovered) {
                context.fill(x - 1, y - 1, x + width + 1, y,
                    toARGB(ACCENT));
                context.fill(x - 1, y + height, x + width + 1, y + height + 1,
                    toARGB(ACCENT));
            }

            if (text.equals("MUTE") || text.equals("UNMUTE")) drawMuteIcon(context, isMuted);
            else if (text.equals("SKIP")) drawSkipIcon(context);
        }

        void drawPixelatedButton(DrawContext context, Color color) {
            context.fill(x, y, x + width, y + height, toARGB(color));
            Color light = new Color(Math.min(255, color.getRed() + 70), Math.min(255, color.getGreen() + 70), Math.min(255, color.getBlue() + 90), color.getAlpha());
            Color dark = new Color(Math.max(0, color.getRed() - 70), Math.max(0, color.getGreen() - 70), Math.max(0, color.getBlue() - 50), color.getAlpha());
            context.fill(x, y, x + width, y + 1, toARGB(light));
            context.fill(x, y + 1, x + 1, y + height - 1, toARGB(light));
            context.fill(x, y + height - 1, x + width, y + height, toARGB(dark));
            context.fill(x + width - 1, y + 1, x + width, y + height - 1, toARGB(dark));
        }

        void drawMuteIcon(DrawContext context, boolean muted) {
            int is = 14, sx = x + (width - is) / 2, sy = y + (height - is) / 2;
            context.fill(sx + 2, sy + 4, sx + 5, sy + 10, toARGB(new Color(220, 220, 220)));
            context.fill(sx + 3, sy + 5, sx + 4, sy + 9, toARGB(new Color(150, 150, 150)));
            context.fill(sx + 3, sy + 6, sx + 4, sy + 8, toARGB(new Color(50, 50, 50)));
            for (int i = 0; i < 3; i++) context.fill(sx + 5, sy + 4 + i, sx + 8 - i, sy + 5 + i, toARGB(new Color(200, 200, 200)));
            for (int i = 0; i < 3; i++) context.fill(sx + 5, sy + 9 - i, sx + 8 - i, sy + 10 - i, toARGB(new Color(200, 200, 200)));
            if (!muted) {
                for (int i = 0; i < 4; i++) context.fill(sx + 8 + i, sy + 5 + i/2, sx + 9 + i, sy + 9 - i/2, toARGB(new Color(170, 100, 220, (int)(255 * (1f - i/7f)))));
                for (int i = 0; i < 4; i++) context.fill(sx + 10 + i, sy + 4 + i/2, sx + 11 + i, sy + 10 - i/2, toARGB(new Color(100, 170, 255, (int)(200 * (1f - i/7f)))));
            } else {
                for (int i = 0; i < 7; i++) context.fill(sx + 8 + i, sy + 3 + i, sx + 9 + i, sy + 4 + i, toARGB(new Color(255, 70, 70)));
                for (int i = 0; i < 7; i++) context.fill(sx + 8 + i, sy + 10 - i, sx + 9 + i, sy + 11 - i, toARGB(new Color(255, 50, 50)));
            }
        }

        void drawSkipIcon(DrawContext context) {
            int is = 14, sx = x + (width - is) / 2, sy = y + (height - is) / 2;
            context.fill(sx + 2, sy + 3, sx + 3, sy + 11, 0xFFFFFFFF);
            context.fill(sx + 2, sy + 3, sx + 7, sy + 7, 0xFFFFFFFF);
            context.fill(sx + 2, sy + 11, sx + 7, sy + 7, 0xFFFFFFFF);
            context.fill(sx + 7, sy + 3, sx + 8, sy + 11, 0xFFFFFFFF);
            context.fill(sx + 7, sy + 3, sx + 12, sy + 7, 0xFFFFFFFF);
            context.fill(sx + 7, sy + 11, sx + 12, sy + 7, 0xFFFFFFFF);
            context.fill(sx + 12, sy + 3, sx + 13, sy + 11, 0xFFFFFFFF);
        }

        boolean isMouseOver(int mx, int my) {
            return mx >= x && mx <= x + width && my >= y && my <= y + height;
        }

        void setText(String t) { this.text = t; }
    }

    // ---- Inner: VolumeSlider ----
    private class VolumeSlider {
        int x, y, width, height;
        float value = 0.6f;
        boolean isDragging = false;
        TitleAnimation hoverAnim = new TitleAnimation(150, 0, 1, Easing.EASE_OUT_CUBIC);
        boolean isHovered = false;

        VolumeSlider(int x, int y, int width, int height) {
            this.x = x; this.y = y; this.width = width; this.height = height;
        }

        void render(DrawContext context, int mouseX, int mouseY, float delta) {
            boolean wasHovered = isHovered;
            isHovered = isMouseOver(mouseX, mouseY) || isDragging;
            int panelX = x - 10;
            int panelY = y - 20;
            int panelW = width + 20;
            int panelH = height + 40;


            int barY = y + height / 2;

// background bar
            context.fill(x, barY - 2, x + width, barY + 2,
                toARGB(new Color(20, 20, 30, (int)(200 * alpha)))
            );

// filled part
            int fillX = x + (int)(value * width);

            context.fill(x, barY - 2, fillX, barY + 2,
                toARGB(ACCENT)
            );

            if (wasHovered != isHovered) hoverAnim.reset();
            hoverAnim.update(delta);
            float p = hoverAnim.getValue();

            int centerX = x + width / 2;

            // Background track
            context.fill(centerX - 1, y, centerX + 1, y + height,
                toARGB(new Color(255, 255, 255, (int)(80 * alpha)))
            );

            // Fill
            int fillY = y + height - (int)(value * height);

            context.fill(centerX - 1, fillY, centerX + 1, y + height,
                toARGB(new Color(
                    ACCENT.getRed(),
                    ACCENT.getGreen(),
                    ACCENT.getBlue(),
                    (int)(220 * alpha)
                ))
            );

            // Glow
            if (isHovered) {
                Color glow = new Color(
                    ACCENT.getRed(),
                    ACCENT.getGreen(),
                    ACCENT.getBlue(),
                    (int)(80 * p)
                );
                context.fill(centerX - 4, fillY - 2, centerX + 4, fillY + 2, toARGB(glow));
            }

            // Knob
            // Knob
            int knobX = x + (int)(value * width);

// outer border
            context.fill(knobX - 4, barY - 4, knobX + 4, barY + 4,
                toARGB(new Color(20, 20, 20, 255))
            );

// inner square
            context.fill(knobX - 3, barY - 3, knobX + 3, barY + 3,
                toARGB(new Color(200, 200, 200, 255))
            );
        }

        boolean mouseClicked(double mx, double my) {
            if (isMouseOver((int) mx, (int) my)) {
                isDragging = true;
                updateFromMouse(mx);
                return true;
            }
            return false;
        }

        boolean mouseDragged(double mx, double my) {
            if (isDragging) {
                updateFromMouse(mx);
                return true;
            }
            return false;
        }

        void mouseReleased() {
            isDragging = false;
        }

        private void updateFromMouse(double mx) {
            value = (float) Math.max(0, Math.min(1, (mx - x) / width));
        }

        boolean isMouseOver(int mx, int my) {
            return mx >= x && mx <= x + width && my >= y && my <= y + height;
        }

        float getValue() { return value; }
        void setValue(float v) { this.value = Math.max(0, Math.min(1, v)); }
    }
}
