package meteordevelopment.meteorclient.systems.hud.elements;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.other.WindowsMediaController;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/hud/elements/MediaHud.class */
public class MediaHud extends HudElement {
    public static final HudElementInfo<MediaHud> INFO = new HudElementInfo<>(Hud.GROUP, "Media HUD", "Displays currently playing media and allows playback control.", MediaHud::new);
    private final SettingGroup sgGeneral;
    private final SettingGroup sgControls;
    private final SettingGroup sgBackground;
    private String currentMedia;
    private boolean recalculateSize;
    private boolean firstTick;
    private float opacity;
    private float lastWidth;
    private float lastHeight;
    private final ScheduledExecutorService scheduler;
    private final Setting<SettingColor> textColor;
    private final Setting<Keybind> playPauseKey;
    private final Setting<Keybind> nextTrackKey;
    private final Setting<Keybind> prevTrackKey;
    private final Setting<Boolean> autoHide;
    private final Setting<Boolean> enableFade;
    private final Setting<Boolean> background;
    private final Setting<SettingColor> backgroundColor;
    private final Map<Keybind, Boolean> keyPressedState;

    public MediaHud() {
        super(INFO);
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgControls = this.settings.createGroup("Controls");
        this.sgBackground = this.settings.createGroup("Background");
        this.currentMedia = "Not Playing";
        this.firstTick = true;
        this.opacity = 0.0f;
        this.lastWidth = 100.0f;
        this.lastHeight = 10.0f;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.textColor = this.sgGeneral.add(new ColorSetting.Builder().name("text-color").description("Color of the media text.").defaultValue(new SettingColor(255, 255, 255)).build());
        this.playPauseKey = this.sgControls.add(new KeybindSetting.Builder().name("play-pause").description("Keybind to play/pause media.").defaultValue(Keybind.fromKey(80)).build());
        this.nextTrackKey = this.sgControls.add(new KeybindSetting.Builder().name("next-track").description("Keybind to skip to next track.").defaultValue(Keybind.fromKey(78)).build());
        this.prevTrackKey = this.sgControls.add(new KeybindSetting.Builder().name("previous-track").description("Keybind to go back to the previous track.").defaultValue(Keybind.fromKey(66)).build());
        this.autoHide = this.sgGeneral.add(new BoolSetting.Builder().name("auto-hide").description("Hides the HUD when nothing is playing.").defaultValue(true).build());
        this.enableFade = this.sgGeneral.add(new BoolSetting.Builder().name("fade-animation").description("Enables fade-in/fade-out animation when song changes.").defaultValue(true).build());
        this.background = this.sgBackground.add(new BoolSetting.Builder().name("background").description("Displays a background behind the text.").defaultValue(true).build());
        SettingGroup settingGroup = this.sgBackground;
        ColorSetting.Builder builderDescription = new ColorSetting.Builder().name("background-color").description("Color of the background.");
        Setting<Boolean> setting = this.background;
        Objects.requireNonNull(setting);
        this.backgroundColor = settingGroup.add(builderDescription.visible(setting::get).defaultValue(new SettingColor(25, 25, 25, Opcode.FCMPG)).build());
        this.keyPressedState = new HashMap();
        this.scheduler.scheduleAtFixedRate(this::updateMediaInfo, 0L, 2L, TimeUnit.SECONDS);
    }

    @Override // meteordevelopment.meteorclient.systems.hud.HudElement
    public void tick(HudRenderer renderer) {
        if (this.recalculateSize) {
            calculateSize(renderer);
            this.recalculateSize = false;
        }
        if (this.firstTick) {
            calculateSize(renderer);
            this.firstTick = false;
        }
        boolean isPlaying = this.currentMedia.contains(" - ");
        float targetOpacity = (isPlaying || !this.autoHide.get().booleanValue()) ? 1.0f : 0.0f;
        this.opacity += (targetOpacity - this.opacity) * 0.2f;
        checkKeyPress(this.playPauseKey.get(), WindowsMediaController::playPause);
        checkKeyPress(this.nextTrackKey.get(), WindowsMediaController::nextTrack);
        checkKeyPress(this.prevTrackKey.get(), WindowsMediaController::previousTrack);
    }

    private void checkKeyPress(Keybind keybind, Runnable action) {
        boolean isPressed = keybind.isPressed();
        if (isPressed && !this.keyPressedState.getOrDefault(keybind, false).booleanValue()) {
            action.run();
        }
        this.keyPressedState.put(keybind, Boolean.valueOf(isPressed));
    }

    @Override // meteordevelopment.meteorclient.systems.hud.HudElement
    public void render(HudRenderer renderer) {
        if (this.opacity <= 0.0f) {
            return;
        }
        double xPos = this.x + 4;
        double yPos = this.y + 4;
        if (this.background.get().booleanValue()) {
            SettingColor bgC = this.backgroundColor.get();
            Color bg = new Color(bgC.r, bgC.g, bgC.b, (int) (this.opacity * bgC.a));
            renderer.quad(this.x, this.y, getWidth(), getHeight(), bg);
        }
        SettingColor textC = this.textColor.get();
        Color textCol = new Color(textC.r, textC.g, textC.b, (int) (this.opacity * 255.0f));
        renderer.text(this.currentMedia, xPos, yPos, textCol, true, 1.0d);
    }

    private void calculateSize(HudRenderer renderer) {
        this.lastWidth = ((float) renderer.textWidth(this.currentMedia, true, 1.0d)) + 8.0f;
        this.lastHeight = ((float) renderer.textHeight(true, 1.0d)) + 8.0f;
        setSize(this.lastWidth, this.lastHeight);
    }

    private void updateMediaInfo() {
        String newMedia = WindowsMediaController.getCurrentMedia();
        if (!newMedia.equals(this.currentMedia)) {
            this.currentMedia = newMedia;
            this.recalculateSize = true;
        }
    }
}
