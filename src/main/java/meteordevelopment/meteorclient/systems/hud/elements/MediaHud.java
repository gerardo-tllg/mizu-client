package meteordevelopment.meteorclient.systems.hud.elements;

import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.other.WindowsMediaController;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MediaHud extends HudElement {
    public static final HudElementInfo<MediaHud> INFO = new HudElementInfo<>(
        Hud.GROUP, "Media HUD", "Displays currently playing media and allows playback control.", MediaHud::new
    );

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgControls = settings.createGroup("Controls");
    private final SettingGroup sgBackground = settings.createGroup("Background");

    private String currentMedia = "Not Playing";
    private boolean recalculateSize;
    private boolean firstTick = true;
    private float opacity = 0f;

    private float lastWidth = 100;
    private float lastHeight = 10;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private final Setting<SettingColor> textColor = sgGeneral.add(new ColorSetting.Builder()
        .name("text-color")
        .description("Color of the media text.")
        .defaultValue(new SettingColor(255, 255, 255))
        .build()
    );

    // ✅ Proper Keybind Settings
    private final Setting<Keybind> playPauseKey = sgControls.add(new KeybindSetting.Builder()
        .name("play-pause")
        .description("Keybind to play/pause media.")
        .defaultValue(Keybind.fromKey(80)) // Default: 'P'
        .build()
    );

    private final Setting<Keybind> nextTrackKey = sgControls.add(new KeybindSetting.Builder()
        .name("next-track")
        .description("Keybind to skip to next track.")
        .defaultValue(Keybind.fromKey(78)) // Default: 'N'
        .build()
    );

    private final Setting<Keybind> prevTrackKey = sgControls.add(new KeybindSetting.Builder()
        .name("previous-track")
        .description("Keybind to go back to the previous track.")
        .defaultValue(Keybind.fromKey(66)) // Default: 'B'
        .build()
    );

    private final Setting<Boolean> autoHide = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-hide")
        .description("Hides the HUD when nothing is playing.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> enableFade = sgGeneral.add(new BoolSetting.Builder()
        .name("fade-animation")
        .description("Enables fade-in/fade-out animation when song changes.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> background = sgBackground.add(new BoolSetting.Builder()
        .name("background")
        .description("Displays a background behind the text.")
        .defaultValue(true)
        .build()
    );

    private final Setting<SettingColor> backgroundColor = sgBackground.add(new ColorSetting.Builder()
        .name("background-color")
        .description("Color of the background.")
        .visible(background::get)
        .defaultValue(new SettingColor(25, 25, 25, 150))
        .build()
    );

    // ✅ Track Key States (Fixing Keybind Handling)
    private final Map<Keybind, Boolean> keyPressedState = new HashMap<>();

    public MediaHud() {
        super(INFO);
        scheduler.scheduleAtFixedRate(this::updateMediaInfo, 0, 2, TimeUnit.SECONDS);
    }

    @Override
    public void tick(HudRenderer renderer) {
        if (recalculateSize) {
            calculateSize(renderer);
            recalculateSize = false;
        }

        if (firstTick) {
            calculateSize(renderer);
            firstTick = false;
        }

        // ✅ Improved Auto-Hide Detection (Only Shows if Music is Playing)
        boolean isPlaying = currentMedia.contains(" - ");
        float targetOpacity = (isPlaying || !autoHide.get()) ? 1f : 0f;
        opacity += (targetOpacity - opacity) * 0.2f;

        // ✅ Detect Key Presses (Only On First Press, No Repeat)
        checkKeyPress(playPauseKey.get(), WindowsMediaController::playPause);
        checkKeyPress(nextTrackKey.get(), WindowsMediaController::nextTrack);
        checkKeyPress(prevTrackKey.get(), WindowsMediaController::previousTrack);
    }

    // ✅ Helper Method to Handle Key Presses Without Holding
    private void checkKeyPress(Keybind keybind, Runnable action) {
        boolean isPressed = keybind.isPressed();

        if (isPressed && !keyPressedState.getOrDefault(keybind, false)) {
            action.run();
        }

        keyPressedState.put(keybind, isPressed);
    }

    @Override
    public void render(HudRenderer renderer) {
        if (opacity <= 0f) return; // Auto-hide when no song is playing

        double xPos = x + 4;
        double yPos = y + 4;

        // ✅ Background Fade Fix
        if (background.get()) {
            SettingColor bgC = backgroundColor.get();
            Color bg = new Color(bgC.r, bgC.g, bgC.b, (int) (opacity * bgC.a));
            renderer.quad(x, y, getWidth(), getHeight(), bg);
        }

        // ✅ Render the text with fade effect
        SettingColor textC = textColor.get();
        Color textCol = new Color(textC.r, textC.g, textC.b, (int) (opacity * 255));
        renderer.text(currentMedia, xPos, yPos, textCol, true, 1.0);
    }

    private void calculateSize(HudRenderer renderer) {
        lastWidth = (float) renderer.textWidth(currentMedia, true, 1.0) + 8;
        lastHeight = (float) renderer.textHeight(true, 1.0) + 8;
        setSize(lastWidth, lastHeight);
    }

    private void updateMediaInfo() {
        String newMedia = WindowsMediaController.getCurrentMedia();
        if (!newMedia.equals(currentMedia)) {
            currentMedia = newMedia;
            recalculateSize = true;
        }
    }
}
