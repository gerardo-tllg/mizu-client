package meteordevelopment.meteorclient.systems.modules.misc;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.notebot.NotebotUtils;
import meteordevelopment.meteorclient.utils.notebot.decoder.SongDecoders;
import meteordevelopment.meteorclient.utils.notebot.instrumentdetect.InstrumentDetectMode;
import meteordevelopment.meteorclient.utils.notebot.song.Note;
import meteordevelopment.meteorclient.utils.notebot.song.Song;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1268;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2428;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_2766;
import net.minecraft.class_2846;
import net.minecraft.class_2885;
import net.minecraft.class_3414;
import net.minecraft.class_3417;
import net.minecraft.class_3965;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/misc/Notebot.class */
public class Notebot extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgNoteMap;
    private final SettingGroup sgRender;
    public final Setting<Integer> tickDelay;
    public final Setting<Integer> concurrentTuneBlocks;
    public final Setting<NotebotUtils.NotebotMode> mode;
    public final Setting<InstrumentDetectMode> instrumentDetectMode;
    public final Setting<Boolean> polyphonic;
    public final Setting<Boolean> autoRotate;
    public final Setting<Boolean> autoPlay;
    public final Setting<Boolean> roundOutOfRange;
    public final Setting<Boolean> swingArm;
    public final Setting<Integer> checkNoteblocksAgainDelay;
    public final Setting<Boolean> renderText;
    public final Setting<Boolean> renderBoxes;
    public final Setting<ShapeMode> shapeMode;
    public final Setting<SettingColor> untunedSideColor;
    public final Setting<SettingColor> untunedLineColor;
    public final Setting<SettingColor> tunedSideColor;
    public final Setting<SettingColor> tunedLineColor;
    public final Setting<SettingColor> tuneHitSideColor;
    private final Setting<SettingColor> tuneHitLineColor;
    public final Setting<SettingColor> scannedNoteblockSideColor;
    private final Setting<SettingColor> scannedNoteblockLineColor;
    public final Setting<Double> noteTextScale;
    public final Setting<Boolean> showScannedNoteblocks;
    private CompletableFuture<Song> loadingSongFuture;
    private Song song;
    private final Map<Note, class_2338> noteBlockPositions;
    private final Multimap<Note, class_2338> scannedNoteblocks;
    private final List<class_2338> clickedBlocks;
    private Stage stage;
    private PlayingMode playingMode;
    private boolean isPlaying;
    private int currentTick;
    private int ticks;
    private WLabel status;
    private boolean anyNoteblockTuned;
    private final Map<class_2338, Integer> tuneHits;
    private int waitTicks;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/misc/Notebot$PlayingMode.class */
    public enum PlayingMode {
        None,
        Preview,
        Noteblocks
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/misc/Notebot$Stage.class */
    public enum Stage {
        None,
        LoadingSong,
        SetUp,
        Tune,
        WaitingToCheckNoteblocks,
        Playing
    }

    public Notebot() {
        super(Categories.Misc, "notebot", "Plays noteblock nicely");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgNoteMap = this.settings.createGroup("Note Map", false);
        this.sgRender = this.settings.createGroup("Render", true);
        this.tickDelay = this.sgGeneral.add(new IntSetting.Builder().name("tick-delay").description("The delay when loading a song.").defaultValue(1).sliderRange(1, 20).min(1).build());
        this.concurrentTuneBlocks = this.sgGeneral.add(new IntSetting.Builder().name("concurrent-tune-blocks").description("How many noteblocks can be tuned at the same time. On Paper it is recommended to set it to 1 to avoid bugs.").defaultValue(1).min(1).sliderRange(1, 20).build());
        this.mode = this.sgGeneral.add(new EnumSetting.Builder().name("mode").description("Select mode of notebot").defaultValue(NotebotUtils.NotebotMode.ExactInstruments).build());
        this.instrumentDetectMode = this.sgGeneral.add(new EnumSetting.Builder().name("instrument-detect-mode").description("Select an instrument detect mode. Can be useful when server has a plugin that modifies noteblock state (e.g ItemsAdder) but noteblock can still play the right note").defaultValue(InstrumentDetectMode.BlockState).build());
        this.polyphonic = this.sgGeneral.add(new BoolSetting.Builder().name("polyphonic").description("Whether or not to allow multiple notes to be played at the same time").defaultValue(true).build());
        this.autoRotate = this.sgGeneral.add(new BoolSetting.Builder().name("auto-rotate").description("Should client look at note block when it wants to hit it").defaultValue(true).build());
        this.autoPlay = this.sgGeneral.add(new BoolSetting.Builder().name("auto-play").description("Auto plays random songs").defaultValue(false).build());
        this.roundOutOfRange = this.sgGeneral.add(new BoolSetting.Builder().name("round-out-of-range").description("Rounds out of range notes").defaultValue(false).build());
        this.swingArm = this.sgGeneral.add(new BoolSetting.Builder().name("swing-arm").description("Should swing arm on hit").defaultValue(true).build());
        this.checkNoteblocksAgainDelay = this.sgGeneral.add(new IntSetting.Builder().name("check-noteblocks-again-delay").description("How much delay should be between end of tuning and checking again").defaultValue(10).min(1).sliderRange(1, 20).build());
        this.renderText = this.sgRender.add(new BoolSetting.Builder().name("render-text").description("Whether or not to render the text above noteblocks.").defaultValue(true).build());
        this.renderBoxes = this.sgRender.add(new BoolSetting.Builder().name("render-boxes").description("Whether or not to render the outline around the noteblocks.").defaultValue(true).build());
        this.shapeMode = this.sgRender.add(new EnumSetting.Builder().name("shape-mode").description("How the shapes are rendered.").defaultValue(ShapeMode.Both).build());
        this.untunedSideColor = this.sgRender.add(new ColorSetting.Builder().name("untuned-side-color").description("The color of the sides of the untuned blocks being rendered.").defaultValue(new SettingColor(204, 0, 0, 10)).build());
        this.untunedLineColor = this.sgRender.add(new ColorSetting.Builder().name("untuned-line-color").description("The color of the lines of the untuned blocks being rendered.").defaultValue(new SettingColor(204, 0, 0, 255)).build());
        this.tunedSideColor = this.sgRender.add(new ColorSetting.Builder().name("tuned-side-color").description("The color of the sides of the tuned blocks being rendered.").defaultValue(new SettingColor(0, 204, 0, 10)).build());
        this.tunedLineColor = this.sgRender.add(new ColorSetting.Builder().name("tuned-line-color").description("The color of the lines of the tuned blocks being rendered.").defaultValue(new SettingColor(0, 204, 0, 255)).build());
        this.tuneHitSideColor = this.sgRender.add(new ColorSetting.Builder().name("hit-side-color").description("The color of the sides being rendered on noteblock tune hit.").defaultValue(new SettingColor(255, Opcode.IFEQ, 0, 10)).build());
        this.tuneHitLineColor = this.sgRender.add(new ColorSetting.Builder().name("hit-line-color").description("The color of the lines being rendered on noteblock tune hit.").defaultValue(new SettingColor(255, Opcode.IFEQ, 0, 255)).build());
        this.scannedNoteblockSideColor = this.sgRender.add(new ColorSetting.Builder().name("scanned-noteblock-side-color").description("The color of the sides of the scanned noteblocks being rendered.").defaultValue(new SettingColor(255, 255, 0, 30)).build());
        this.scannedNoteblockLineColor = this.sgRender.add(new ColorSetting.Builder().name("scanned-noteblock-line-color").description("The color of the lines of the scanned noteblocks being rendered.").defaultValue(new SettingColor(255, 255, 0, 255)).build());
        this.noteTextScale = this.sgRender.add(new DoubleSetting.Builder().name("note-text-scale").description("The scale.").defaultValue(1.5d).min(0.0d).build());
        this.showScannedNoteblocks = this.sgRender.add(new BoolSetting.Builder().name("show-scanned-noteblocks").description("Show scanned Noteblocks").defaultValue(false).build());
        this.loadingSongFuture = null;
        this.noteBlockPositions = new HashMap();
        this.scannedNoteblocks = MultimapBuilder.linkedHashKeys().arrayListValues().build();
        this.clickedBlocks = new ArrayList();
        this.stage = Stage.None;
        this.playingMode = PlayingMode.None;
        this.isPlaying = false;
        this.currentTick = 0;
        this.ticks = 0;
        this.anyNoteblockTuned = false;
        this.tuneHits = new HashMap();
        this.waitTicks = -1;
        for (class_2766 inst : class_2766.values()) {
            NotebotUtils.OptionalInstrument optionalInstrument = NotebotUtils.OptionalInstrument.fromMinecraftInstrument(inst);
            if (optionalInstrument != null) {
                this.sgNoteMap.add(new EnumSetting.Builder().name(beautifyText(inst.name())).defaultValue(optionalInstrument).visible(() -> {
                    return this.mode.get() == NotebotUtils.NotebotMode.ExactInstruments;
                }).build());
            }
        }
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public String getInfoString() {
        if (this.stage == Stage.None) {
            return "None";
        }
        return this.playingMode.toString() + " | " + this.stage.toString();
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        this.ticks = 0;
        resetVariables();
    }

    private void resetVariables() {
        if (this.loadingSongFuture != null) {
            this.loadingSongFuture.cancel(true);
            this.loadingSongFuture = null;
        }
        this.clickedBlocks.clear();
        this.tuneHits.clear();
        this.anyNoteblockTuned = false;
        this.currentTick = 0;
        this.playingMode = PlayingMode.None;
        this.isPlaying = false;
        this.stage = Stage.None;
        this.song = null;
        this.noteBlockPositions.clear();
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        Color sideColor;
        SettingColor settingColor;
        if (this.renderBoxes.get().booleanValue()) {
            if (this.stage == Stage.SetUp || this.stage == Stage.Tune || this.stage == Stage.WaitingToCheckNoteblocks || this.isPlaying) {
                if (this.showScannedNoteblocks.get().booleanValue()) {
                    for (class_2338 blockPos : this.scannedNoteblocks.values()) {
                        double x1 = blockPos.method_10263();
                        double y1 = blockPos.method_10264();
                        double z1 = blockPos.method_10260();
                        double x2 = blockPos.method_10263() + 1;
                        double y2 = blockPos.method_10264() + 1;
                        double z2 = blockPos.method_10260() + 1;
                        event.renderer.box(x1, y1, z1, x2, y2, z2, this.scannedNoteblockSideColor.get(), this.scannedNoteblockLineColor.get(), this.shapeMode.get(), 0);
                    }
                    return;
                }
                for (Map.Entry<Note, class_2338> entry : this.noteBlockPositions.entrySet()) {
                    Note note = entry.getKey();
                    class_2338 blockPos2 = entry.getValue();
                    class_2680 state = this.mc.field_1687.method_8320(blockPos2);
                    if (state.method_26204() == class_2246.field_10179) {
                        int level = ((Integer) state.method_11654(class_2428.field_11324)).intValue();
                        double x12 = blockPos2.method_10263();
                        double y12 = blockPos2.method_10264();
                        double z12 = blockPos2.method_10260();
                        double x22 = blockPos2.method_10263() + 1;
                        double y22 = blockPos2.method_10264() + 1;
                        double z22 = blockPos2.method_10260() + 1;
                        if (this.clickedBlocks.contains(blockPos2)) {
                            sideColor = this.tuneHitSideColor.get();
                            settingColor = this.tuneHitLineColor.get();
                        } else if (note.getNoteLevel() == level) {
                            sideColor = this.tunedSideColor.get();
                            settingColor = this.tunedLineColor.get();
                        } else {
                            sideColor = this.untunedSideColor.get();
                            settingColor = this.untunedLineColor.get();
                        }
                        SettingColor lineColor = settingColor;
                        event.renderer.box(x12, y12, z12, x22, y22, z22, sideColor, lineColor, this.shapeMode.get(), 0);
                    }
                }
            }
        }
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        if (this.renderText.get().booleanValue()) {
            if (this.stage == Stage.SetUp || this.stage == Stage.Tune || this.stage == Stage.WaitingToCheckNoteblocks || this.isPlaying) {
                Vector3d pos = new Vector3d();
                for (class_2338 blockPos : this.noteBlockPositions.values()) {
                    class_2680 state = this.mc.field_1687.method_8320(blockPos);
                    if (state.method_26204() == class_2246.field_10179) {
                        double x = ((double) blockPos.method_10263()) + 0.5d;
                        double y = blockPos.method_10264() + 1;
                        double z = ((double) blockPos.method_10260()) + 0.5d;
                        pos.set(x, y, z);
                        String levelText = String.valueOf(state.method_11654(class_2428.field_11324));
                        String tuneHitsText = null;
                        if (this.tuneHits.containsKey(blockPos)) {
                            tuneHitsText = " -" + String.valueOf(this.tuneHits.get(blockPos));
                        }
                        if (NametagUtils.to2D(pos, this.noteTextScale.get().doubleValue(), true)) {
                            TextRenderer text = TextRenderer.get();
                            NametagUtils.begin(pos);
                            text.beginBig();
                            double xScreen = text.getWidth(levelText) / 2.0d;
                            if (tuneHitsText != null) {
                                xScreen += text.getWidth(tuneHitsText) / 2.0d;
                            }
                            double hX = text.render(levelText, -xScreen, 0.0d, Color.GREEN);
                            if (tuneHitsText != null) {
                                text.render(tuneHitsText, hX, 0.0d, Color.RED);
                            }
                            text.end();
                            NametagUtils.end();
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        this.ticks++;
        this.clickedBlocks.clear();
        if (this.stage == Stage.WaitingToCheckNoteblocks) {
            this.waitTicks--;
            if (this.waitTicks == 0) {
                this.waitTicks = -1;
                info("Checking noteblocks again...", new Object[0]);
                setupTuneHitsMap();
                this.stage = Stage.Tune;
                return;
            }
            return;
        }
        if (this.stage == Stage.SetUp) {
            scanForNoteblocks();
            if (this.scannedNoteblocks.isEmpty()) {
                error("Can't find any nearby noteblock!", new Object[0]);
                stop();
                return;
            }
            setupNoteblocksMap();
            if (this.noteBlockPositions.isEmpty()) {
                error("Can't find any valid noteblock to play song.", new Object[0]);
                stop();
                return;
            } else {
                setupTuneHitsMap();
                this.stage = Stage.Tune;
                return;
            }
        }
        if (this.stage == Stage.Tune) {
            tune();
            return;
        }
        if (this.stage == Stage.Playing && this.isPlaying) {
            if (this.mc.field_1724 == null || this.currentTick > this.song.getLastTick()) {
                onSongEnd();
                return;
            }
            if (this.song.getNotesMap().containsKey(Integer.valueOf(this.currentTick))) {
                if (this.playingMode == PlayingMode.Preview) {
                    onTickPreview();
                } else {
                    if (this.mc.field_1724.method_31549().field_7477) {
                        error("You need to be in survival mode.", new Object[0]);
                        stop();
                        return;
                    }
                    onTickPlay();
                }
            }
            this.currentTick++;
            updateStatus();
        }
    }

    private void setupNoteblocksMap() {
        this.noteBlockPositions.clear();
        List<Note> uniqueNotesToUse = new ArrayList<>(this.song.getRequirements());
        Map<class_2766, List<class_2338>> incorrectNoteBlocks = new HashMap<>();
        for (Map.Entry<Note, Collection<class_2338>> entry : this.scannedNoteblocks.asMap().entrySet()) {
            Note note = entry.getKey();
            List<class_2338> noteblocks = new ArrayList<>((Collection<? extends class_2338>) entry.getValue());
            if (uniqueNotesToUse.contains(note)) {
                this.noteBlockPositions.put(note, (class_2338) noteblocks.removeFirst());
                uniqueNotesToUse.remove(note);
            }
            if (!noteblocks.isEmpty()) {
                if (!incorrectNoteBlocks.containsKey(note.getInstrument())) {
                    incorrectNoteBlocks.put(note.getInstrument(), new ArrayList<>());
                }
                incorrectNoteBlocks.get(note.getInstrument()).addAll(noteblocks);
            }
        }
        for (Map.Entry<class_2766, List<class_2338>> entry2 : incorrectNoteBlocks.entrySet()) {
            List<class_2338> positions = entry2.getValue();
            if (this.mode.get() == NotebotUtils.NotebotMode.ExactInstruments) {
                class_2766 inst = entry2.getKey();
                List<Note> foundNotes = (List) uniqueNotesToUse.stream().filter(note2 -> {
                    return note2.getInstrument() == inst;
                }).collect(Collectors.toList());
                if (!foundNotes.isEmpty()) {
                    for (class_2338 pos : positions) {
                        if (foundNotes.isEmpty()) {
                            break;
                        }
                        Note note3 = (Note) foundNotes.removeFirst();
                        this.noteBlockPositions.put(note3, pos);
                        uniqueNotesToUse.remove(note3);
                    }
                }
            } else {
                for (class_2338 pos2 : positions) {
                    if (uniqueNotesToUse.isEmpty()) {
                        break;
                    }
                    this.noteBlockPositions.put((Note) uniqueNotesToUse.removeFirst(), pos2);
                }
            }
        }
        if (!uniqueNotesToUse.isEmpty()) {
            for (Note note4 : uniqueNotesToUse) {
                warning("Missing note: " + String.valueOf(note4.getInstrument()) + ", " + note4.getNoteLevel(), new Object[0]);
            }
            warning(uniqueNotesToUse.size() + " missing notes!", new Object[0]);
        }
    }

    private void setupTuneHitsMap() {
        int currentLevel;
        this.tuneHits.clear();
        for (Map.Entry<Note, class_2338> entry : this.noteBlockPositions.entrySet()) {
            int targetLevel = entry.getKey().getNoteLevel();
            class_2338 blockPos = entry.getValue();
            class_2680 blockState = this.mc.field_1687.method_8320(blockPos);
            if (blockState.method_26204() == class_2246.field_10179 && targetLevel != (currentLevel = ((Integer) blockState.method_11654(class_2428.field_11324)).intValue())) {
                this.tuneHits.put(blockPos, Integer.valueOf(calcNumberOfHits(currentLevel, targetLevel)));
            }
        }
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public WWidget getWidget(GuiTheme theme) {
        WTable table = theme.table();
        WButton openSongGUI = (WButton) table.add(theme.button("Open Song GUI")).expandX().minWidth(100.0d).widget();
        openSongGUI.action = () -> {
            this.mc.method_1507(theme.notebotSongs());
        };
        table.row();
        WButton alignCenter = (WButton) table.add(theme.button("Align Center")).expandX().minWidth(100.0d).widget();
        alignCenter.action = () -> {
            if (this.mc.field_1724 == null) {
                return;
            }
            class_243 pos = class_243.method_24955(this.mc.field_1724.method_24515());
            this.mc.field_1724.method_5814(pos.field_1352, this.mc.field_1724.method_23318(), pos.field_1350);
        };
        table.row();
        this.status = (WLabel) table.add(theme.label(getStatus())).expandCellX().widget();
        WButton pause = (WButton) table.add(theme.button(this.isPlaying ? "Pause" : "Resume")).right().widget();
        pause.action = () -> {
            pause();
            pause.set(this.isPlaying ? "Pause" : "Resume");
            updateStatus();
        };
        WButton stop = (WButton) table.add(theme.button("Stop")).right().widget();
        stop.action = this::stop;
        return table;
    }

    public String getStatus() {
        return !isActive() ? "Module disabled." : this.song == null ? "No song loaded." : this.isPlaying ? String.format("Playing song. %d/%d", Integer.valueOf(this.currentTick), Integer.valueOf(this.song.getLastTick())) : this.stage == Stage.Playing ? "Ready to play." : (this.stage == Stage.SetUp || this.stage == Stage.Tune || this.stage == Stage.WaitingToCheckNoteblocks) ? "Setting up the noteblocks." : String.format("Stage: %s.", this.stage.toString());
    }

    public void play() {
        if (this.mc.field_1724 == null) {
            return;
        }
        if (this.mc.field_1724.method_31549().field_7477 && this.playingMode != PlayingMode.Preview) {
            error("You need to be in survival mode.", new Object[0]);
        } else if (this.stage == Stage.Playing) {
            this.isPlaying = true;
            info("Playing.", new Object[0]);
        } else {
            error("No song loaded.", new Object[0]);
        }
    }

    public void pause() {
        if (!isActive()) {
            toggle();
        }
        if (this.isPlaying) {
            info("Pausing.", new Object[0]);
            this.isPlaying = false;
        } else {
            info("Resuming.", new Object[0]);
            this.isPlaying = true;
        }
    }

    public void stop() {
        info("Stopping.", new Object[0]);
        disable();
        updateStatus();
    }

    public void onSongEnd() {
        if (this.autoPlay.get().booleanValue() && this.playingMode != PlayingMode.Preview) {
            playRandomSong();
        } else {
            stop();
        }
    }

    public void playRandomSong() {
        File[] files = MeteorClient.FOLDER.toPath().resolve("notebot").toFile().listFiles();
        if (files == null) {
            return;
        }
        File randomSong = files[ThreadLocalRandom.current().nextInt(files.length)];
        if (SongDecoders.hasDecoder(randomSong)) {
            loadSong(randomSong);
        } else {
            playRandomSong();
        }
    }

    public void disable() {
        resetVariables();
        if (!isActive()) {
            toggle();
        }
    }

    public void loadSong(File file) {
        if (!isActive()) {
            toggle();
        }
        resetVariables();
        this.playingMode = PlayingMode.Noteblocks;
        if (!loadFileToMap(file, () -> {
            this.stage = Stage.SetUp;
        })) {
            onSongEnd();
        } else {
            updateStatus();
        }
    }

    public void previewSong(File file) {
        if (!isActive()) {
            toggle();
        }
        resetVariables();
        this.playingMode = PlayingMode.Preview;
        loadFileToMap(file, () -> {
            this.stage = Stage.Playing;
            play();
        });
        updateStatus();
    }

    public boolean loadFileToMap(File file, Runnable callback) {
        if (!file.exists() || !file.isFile()) {
            error("File not found", new Object[0]);
            return false;
        }
        if (!SongDecoders.hasDecoder(file)) {
            error("File is in wrong format. Decoder not found.", new Object[0]);
            return false;
        }
        info("Loading song \"%s\".", FilenameUtils.getBaseName(file.getName()));
        this.loadingSongFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return SongDecoders.parse(file);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        this.loadingSongFuture.completeOnTimeout(null, 60L, TimeUnit.SECONDS);
        this.stage = Stage.LoadingSong;
        long time1 = System.currentTimeMillis();
        this.loadingSongFuture.whenComplete((song, ex) -> {
            if (ex == null) {
                if (song == null) {
                    error("Loading song '" + FilenameUtils.getBaseName(file.getName()) + "' timed out.", new Object[0]);
                    onSongEnd();
                    return;
                }
                this.song = song;
                long time2 = System.currentTimeMillis();
                long diff = time2 - time1;
                info("Song '" + FilenameUtils.getBaseName(file.getName()) + "' has been loaded to the memory! Took " + diff + "ms", new Object[0]);
                callback.run();
                return;
            }
            if (ex instanceof CancellationException) {
                error("Loading song '" + FilenameUtils.getBaseName(file.getName()) + "' was cancelled.", new Object[0]);
                return;
            }
            error("An error occurred while loading song '" + FilenameUtils.getBaseName(file.getName()) + "'. See the logs for more details", new Object[0]);
            MeteorClient.LOG.error("An error occurred while loading song '{}'", FilenameUtils.getBaseName(file.getName()), ex);
            onSongEnd();
        });
        return true;
    }

    private void scanForNoteblocks() {
        if (this.mc.field_1761 == null || this.mc.field_1687 == null || this.mc.field_1724 == null) {
            return;
        }
        this.scannedNoteblocks.clear();
        int min = ((int) (-this.mc.field_1724.method_55754())) - 2;
        int max = ((int) this.mc.field_1724.method_55754()) + 2;
        for (int y = min; y < max; y++) {
            for (int x = min; x < max; x++) {
                for (int z = min; z < max; z++) {
                    class_2338 pos = this.mc.field_1724.method_24515().method_10069(x, y + 1, z);
                    class_2680 blockState = this.mc.field_1687.method_8320(pos);
                    if (blockState.method_26204() == class_2246.field_10179 && this.mc.field_1724.method_56093(pos, 1.0d) && isValidScanSpot(pos)) {
                        Note note = NotebotUtils.getNoteFromNoteBlock(blockState, pos, this.mode.get(), this.instrumentDetectMode.get().getInstrumentDetectFunction());
                        this.scannedNoteblocks.put(note, pos);
                    }
                }
            }
        }
    }

    private void onTickPreview() {
        for (Note note : this.song.getNotesMap().get(Integer.valueOf(this.currentTick))) {
            if (this.mode.get() == NotebotUtils.NotebotMode.ExactInstruments) {
                this.mc.field_1724.method_5783((class_3414) note.getInstrument().method_11886().comp_349(), 2.0f, (float) Math.pow(2.0d, ((double) (note.getNoteLevel() - 12)) / 12.0d));
            } else {
                this.mc.field_1724.method_5783((class_3414) class_3417.field_15114.comp_349(), 2.0f, (float) Math.pow(2.0d, ((double) (note.getNoteLevel() - 12)) / 12.0d));
            }
        }
    }

    private void tune() {
        if (this.tuneHits.isEmpty()) {
            if (this.anyNoteblockTuned) {
                this.anyNoteblockTuned = false;
                this.waitTicks = this.checkNoteblocksAgainDelay.get().intValue();
                this.stage = Stage.WaitingToCheckNoteblocks;
                info("Delaying check for noteblocks", new Object[0]);
                return;
            }
            this.stage = Stage.Playing;
            info("Loading done.", new Object[0]);
            play();
            return;
        }
        if (this.ticks < this.tickDelay.get().intValue()) {
            return;
        }
        tuneBlocks();
        this.ticks = 0;
    }

    private void tuneBlocks() {
        if (this.mc.field_1687 == null || this.mc.field_1724 == null) {
            disable();
        }
        if (this.swingArm.get().booleanValue()) {
            this.mc.field_1724.method_6104(class_1268.field_5808);
        }
        int iterations = 0;
        Iterator<Map.Entry<class_2338, Integer>> iterator = this.tuneHits.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<class_2338, Integer> entry = iterator.next();
            class_2338 pos = entry.getKey();
            int hitsNumber = entry.getValue().intValue();
            if (this.autoRotate.get().booleanValue()) {
                Rotations.rotate(Rotations.getYaw(pos), Rotations.getPitch(pos), 100, () -> {
                    tuneNoteblockWithPackets(pos);
                });
            } else {
                tuneNoteblockWithPackets(pos);
            }
            this.clickedBlocks.add(pos);
            int hitsNumber2 = hitsNumber - 1;
            entry.setValue(Integer.valueOf(hitsNumber2));
            if (hitsNumber2 == 0) {
                iterator.remove();
            }
            iterations++;
            if (iterations == this.concurrentTuneBlocks.get().intValue()) {
                return;
            }
        }
    }

    private void tuneNoteblockWithPackets(class_2338 pos) {
        int sequence = getSequence();
        this.mc.field_1724.field_3944.method_52787(new class_2885(class_1268.field_5808, new class_3965(class_243.method_24953(pos), class_2350.field_11033, pos, false), sequence));
        this.anyNoteblockTuned = true;
    }

    public void updateStatus() {
        if (this.status != null) {
            this.status.set(getStatus());
        }
    }

    private static int calcNumberOfHits(int from, int to) {
        if (from > to) {
            return (25 - from) + to;
        }
        return to - from;
    }

    private void onTickPlay() {
        class_2338 firstPos;
        Collection<Note> notes = this.song.getNotesMap().get(Integer.valueOf(this.currentTick));
        if (!notes.isEmpty()) {
            if (this.autoRotate.get().booleanValue()) {
                Optional<Note> firstNote = notes.stream().findFirst();
                if (firstNote.isPresent() && (firstPos = this.noteBlockPositions.get(firstNote.get())) != null) {
                    Rotations.rotate(Rotations.getYaw(firstPos), Rotations.getPitch(firstPos));
                }
            }
            if (this.swingArm.get().booleanValue()) {
                this.mc.field_1724.method_6104(class_1268.field_5808);
            }
            for (Note note : notes) {
                class_2338 pos = this.noteBlockPositions.get(note);
                if (pos == null) {
                    return;
                }
                if (this.polyphonic.get().booleanValue()) {
                    playRotate(pos);
                } else {
                    playRotate(pos);
                }
            }
        }
    }

    private void playRotate(class_2338 pos) {
        if (this.mc.field_1761 == null) {
            return;
        }
        try {
            this.mc.field_1724.field_3944.method_52787(new class_2846(class_2846.class_2847.field_12968, pos, class_2350.field_11033, 0));
        } catch (NullPointerException e) {
        }
    }

    private boolean isValidScanSpot(class_2338 pos) {
        if (this.mc.field_1687.method_8320(pos).method_26204() != class_2246.field_10179) {
            return false;
        }
        return this.mc.field_1687.method_8320(pos.method_10084()).method_26215();
    }

    @Nullable
    public class_2766 getMappedInstrument(@NotNull class_2766 inst) {
        if (this.mode.get() == NotebotUtils.NotebotMode.ExactInstruments) {
            NotebotUtils.OptionalInstrument optionalInstrument = (NotebotUtils.OptionalInstrument) this.sgNoteMap.getByIndex(inst.ordinal()).get();
            return optionalInstrument.toMinecraftInstrument();
        }
        return inst;
    }

    private String beautifyText(String text) {
        String[] arr = text.toLowerCase(Locale.ROOT).split("_");
        StringBuilder sb = new StringBuilder();
        for (String s : arr) {
            sb.append(Character.toUpperCase(s.charAt(0))).append(s.substring(1));
        }
        return sb.toString().trim();
    }

    private int getSequence() {
        if (this.mc.field_1687 == null) {
            return 0;
        }
        return this.mc.field_1687.meteor$getAndIncrementSequence();
    }
}
