package meteordevelopment.meteorclient.systems.hud.elements;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.other.SpotifyGrabber;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/hud/elements/SpotifyHud.class */
public class SpotifyHud extends HudElement {
    public static final HudElementInfo<SpotifyHud> INFO = new HudElementInfo<>(Hud.GROUP, "Spotify HUD", "Displays the currently playing song on Spotify with animations and customization.", SpotifyHud::new);
    private final SettingGroup sgGeneral;
    private final SettingGroup sgScale;
    private final SettingGroup sgBackground;
    private boolean recalculateSize;
    private boolean firstTick;
    private float opacity;
    private float lastWidth;
    private float lastHeight;
    private final Setting<SettingColor> songColor;
    private final Setting<SettingColor> artistColor;
    private final Setting<Boolean> shadow;
    private final Setting<Integer> border;
    private final Setting<Boolean> autoHide;
    private final Setting<Boolean> enableFade;
    private final Setting<Boolean> customFont;
    private String currentSong;
    private final ScheduledExecutorService scheduler;
    public final Setting<Boolean> customScale;
    public final Setting<Double> scale;
    private final Setting<Boolean> background;
    private final Setting<SettingColor> backgroundColor;

    public SpotifyHud() {
        super(INFO);
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgScale = this.settings.createGroup("Scale");
        this.sgBackground = this.settings.createGroup("Background");
        this.firstTick = true;
        this.opacity = 0.0f;
        this.lastWidth = 100.0f;
        this.lastHeight = 10.0f;
        this.songColor = this.sgGeneral.add(new ColorSetting.Builder().name("song-color").description("Color of the song title.").defaultValue(new SettingColor(255, 255, 255)).build());
        this.artistColor = this.sgGeneral.add(new ColorSetting.Builder().name("artist-color").description("Color of the artist name.").defaultValue(new SettingColor(200, 200, 200)).build());
        this.shadow = this.sgGeneral.add(new BoolSetting.Builder().name("shadow").description("Renders a shadow behind text.").defaultValue(true).onChanged(aBoolean -> {
            this.recalculateSize = true;
        }).build());
        this.border = this.sgGeneral.add(new IntSetting.Builder().name("border").description("How much space to add around the element.").defaultValue(2).onChanged(integer -> {
            this.recalculateSize = true;
        }).build());
        this.autoHide = this.sgGeneral.add(new BoolSetting.Builder().name("auto-hide").description("Hides the HUD when nothing is playing.").defaultValue(true).build());
        this.enableFade = this.sgGeneral.add(new BoolSetting.Builder().name("fade-animation").description("Enables fade-in/fade-out animation when song changes.").defaultValue(true).build());
        this.customFont = this.sgGeneral.add(new BoolSetting.Builder().name("custom-font").description("Use a different font style for text.").defaultValue(false).build());
        this.currentSong = "Not Playing";
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.customScale = this.sgScale.add(new BoolSetting.Builder().name("custom-scale").description("Applies custom text scale rather than the global one.").defaultValue(false).onChanged(val -> {
            this.recalculateSize = true;
        }).build());
        SettingGroup settingGroup = this.sgScale;
        DoubleSetting.Builder builderDescription = new DoubleSetting.Builder().name("scale").description("Custom text scale.");
        Setting<Boolean> setting = this.customScale;
        Objects.requireNonNull(setting);
        this.scale = settingGroup.add(builderDescription.visible(setting::get).defaultValue(1.0d).onChanged(val2 -> {
            this.recalculateSize = true;
        }).min(0.5d).sliderRange(0.5d, 3.0d).build());
        this.background = this.sgBackground.add(new BoolSetting.Builder().name("background").description("Displays a background behind the text.").defaultValue(false).build());
        SettingGroup settingGroup2 = this.sgBackground;
        ColorSetting.Builder builderDescription2 = new ColorSetting.Builder().name("background-color").description("Color of the background.");
        Setting<Boolean> setting2 = this.background;
        Objects.requireNonNull(setting2);
        this.backgroundColor = settingGroup2.add(builderDescription2.visible(setting2::get).defaultValue(new SettingColor(25, 25, 25, 80)).build());
        this.scheduler.scheduleAtFixedRate(this::updateSpotifyTitle, 0L, 2L, TimeUnit.SECONDS);
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
        if (this.enableFade.get().booleanValue()) {
            float targetOpacity = (this.currentSong.equals("Not Playing") && this.autoHide.get().booleanValue()) ? 0.0f : 1.0f;
            this.opacity += (targetOpacity - this.opacity) * 0.2f;
        } else {
            this.opacity = (this.currentSong.equals("Not Playing") && this.autoHide.get().booleanValue()) ? 0.0f : 1.0f;
        }
    }

    @Override // meteordevelopment.meteorclient.systems.hud.HudElement
    public void render(HudRenderer renderer) {
        if (this.opacity <= 0.0f) {
            return;
        }
        double xPos = this.x + this.border.get().intValue();
        double yPos = this.y + this.border.get().intValue();
        if (this.background.get().booleanValue()) {
            SettingColor bgC = this.backgroundColor.get();
            Color bg = new Color(bgC.r, bgC.g, bgC.b, (int) (this.opacity * bgC.a));
            renderer.quad(this.x, this.y, getWidth(), getHeight(), bg);
        }
        String[] parts = this.currentSong.split(" - ", 2);
        String song = parts.length > 0 ? parts[0] : "Unknown";
        String artist = parts.length > 1 ? parts[1] : "";
        SettingColor songC = this.songColor.get();
        SettingColor artistC = this.artistColor.get();
        Color finalSongColor = new Color(songC.r, songC.g, songC.b, (int) (this.opacity * 255.0f));
        new Color(artistC.r, artistC.g, artistC.b, (int) (this.opacity * 255.0f));
        String fullText = song + " - " + artist;
        renderer.text(fullText, xPos, yPos, finalSongColor, this.shadow.get().booleanValue(), getScale());
    }

    private void updateSpotifyTitle() {
        String newSong = SpotifyGrabber.getSpotifySongTitle();
        if (!newSong.equals(this.currentSong)) {
            this.currentSong = newSong;
            this.recalculateSize = true;
        }
    }

    private void calculateSize(HudRenderer renderer) {
        this.lastWidth = ((float) renderer.textWidth(this.currentSong, this.shadow.get().booleanValue(), getScale())) + (this.border.get().intValue() * 2);
        this.lastHeight = ((float) renderer.textHeight(this.shadow.get().booleanValue(), getScale())) + (this.border.get().intValue() * 2);
        setSize(this.lastWidth, this.lastHeight);
    }

    private float getScale() {
        if (this.customScale.get().booleanValue()) {
            return this.scale.get().floatValue();
        }
        return -1.0f;
    }
}
