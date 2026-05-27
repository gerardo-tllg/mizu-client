package meteordevelopment.meteorclient.systems.hud.elements;

import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.other.SpotifyGrabber;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SpotifyHud extends HudElement {
    public static final HudElementInfo<SpotifyHud> INFO = new HudElementInfo<>(
        Hud.GROUP, "Spotify HUD", "Displays the currently playing song on Spotify with animations and customization.", SpotifyHud::new
    );

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgScale = settings.createGroup("Scale");
    private final SettingGroup sgBackground = settings.createGroup("Background");

    private boolean recalculateSize;
    private boolean firstTick = true;
    private float opacity = 0f; // Fade-in/fade-out animation speed

    // Cached text size
    private float lastWidth = 100;
    private float lastHeight = 10;

    // Color settings
    private final Setting<SettingColor> songColor = sgGeneral.add(new ColorSetting.Builder()
        .name("song-color")
        .description("Color of the song title.")
        .defaultValue(new SettingColor(255, 255, 255)) // Default: White
        .build()
    );

    private final Setting<SettingColor> artistColor = sgGeneral.add(new ColorSetting.Builder()
        .name("artist-color")
        .description("Color of the artist name.")
        .defaultValue(new SettingColor(200, 200, 200)) // Slightly dimmed
        .build()
    );

    private final Setting<Boolean> shadow = sgGeneral.add(new BoolSetting.Builder()
        .name("shadow")
        .description("Renders a shadow behind text.")
        .defaultValue(true)
        .onChanged(aBoolean -> recalculateSize = true)
        .build()
    );

    private final Setting<Integer> border = sgGeneral.add(new IntSetting.Builder()
        .name("border")
        .description("How much space to add around the element.")
        .defaultValue(2)
        .onChanged(integer -> recalculateSize = true)
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

    private final Setting<Boolean> customFont = sgGeneral.add(new BoolSetting.Builder()
        .name("custom-font")
        .description("Use a different font style for text.")
        .defaultValue(false)
        .build()
    );

    private String currentSong = "Not Playing";


    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public SpotifyHud() {
        super(INFO);
        scheduler.scheduleAtFixedRate(this::updateSpotifyTitle, 0, 2, TimeUnit.SECONDS);
    }

    public final Setting<Boolean> customScale = sgScale.add(new BoolSetting.Builder()
        .name("custom-scale")
        .description("Applies custom text scale rather than the global one.")
        .defaultValue(false)
        .onChanged(val -> recalculateSize = true)
        .build()
    );

    public final Setting<Double> scale = sgScale.add(new DoubleSetting.Builder()
        .name("scale")
        .description("Custom text scale.")
        .visible(customScale::get)
        .defaultValue(1.0)
        .onChanged(val -> recalculateSize = true)
        .min(0.5)
        .sliderRange(0.5, 3.0)
        .build()
    );

    private final Setting<Boolean> background = sgBackground.add(new BoolSetting.Builder()
        .name("background")
        .description("Displays a background behind the text.")
        .defaultValue(false)
        .build()
    );

    private final Setting<SettingColor> backgroundColor = sgBackground.add(new ColorSetting.Builder()
        .name("background-color")
        .description("Color of the background.")
        .visible(background::get)
        .defaultValue(new SettingColor(25, 25, 25, 80))
        .build()
    );

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


        if (enableFade.get()) {
            float targetOpacity = (currentSong.equals("Not Playing") && autoHide.get()) ? 0f : 1f;
            opacity += (targetOpacity - opacity) * 0.2f; // 🔥 Increased fade speed
        } else {
            opacity = (currentSong.equals("Not Playing") && autoHide.get()) ? 0f : 1f;
        }
    }

    @Override
    public void render(HudRenderer renderer) {
        if (opacity <= 0f) return; // Auto-hide

        double xPos = x + border.get();
        double yPos = y + border.get();

        // ✅ Fade Background Properly
        if (background.get()) {
            SettingColor bgC = backgroundColor.get();
            Color bg = new Color(bgC.r, bgC.g, bgC.b, (int) (opacity * bgC.a));
            renderer.quad(x, y, getWidth(), getHeight(), bg);
        }


        String[] parts = currentSong.split(" - ", 2);
        String song = parts.length > 0 ? parts[0] : "Unknown";
        String artist = parts.length > 1 ? parts[1] : "";


        SettingColor songC = songColor.get();
        SettingColor artistC = artistColor.get();

        Color finalSongColor = new Color(songC.r, songC.g, songC.b, (int) (opacity * 255));
        Color finalArtistColor = new Color(artistC.r, artistC.g, artistC.b, (int) (opacity * 255));


        String fullText = song + " - " + artist;
        renderer.text(fullText, xPos, yPos, finalSongColor, shadow.get(), getScale());
    }

    private void updateSpotifyTitle() {
        String newSong = SpotifyGrabber.getSpotifySongTitle();

        if (!newSong.equals(currentSong)) {
            currentSong = newSong;
            recalculateSize = true;
        }
    }

    private void calculateSize(HudRenderer renderer) {
        lastWidth = (float) renderer.textWidth(currentSong, shadow.get(), getScale()) + border.get() * 2;
        lastHeight = (float) renderer.textHeight(shadow.get(), getScale()) + border.get() * 2;

        setSize(lastWidth, lastHeight);
    }

    private float getScale() {
        return customScale.get() ? scale.get().floatValue() : -1f;
    }

}
