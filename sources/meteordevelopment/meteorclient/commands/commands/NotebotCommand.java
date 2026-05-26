package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.NotebotSongArgumentType;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.Notebot;
import meteordevelopment.meteorclient.utils.notebot.song.Note;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_156;
import net.minecraft.class_2172;
import net.minecraft.class_2561;
import net.minecraft.class_2596;
import net.minecraft.class_2766;
import net.minecraft.class_2767;
import net.minecraft.class_3414;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/commands/commands/NotebotCommand.class */
public class NotebotCommand extends Command {
    private static final SimpleCommandExceptionType INVALID_SONG = new SimpleCommandExceptionType(class_2561.method_43470("Invalid song."));
    private static final DynamicCommandExceptionType INVALID_PATH = new DynamicCommandExceptionType(object -> {
        return class_2561.method_43470("'%s' is not a valid path.".formatted(object));
    });
    int ticks;
    private final Map<Integer, List<Note>> song;

    public NotebotCommand() {
        super("notebot", "Allows you load notebot files", new String[0]);
        this.ticks = -1;
        this.song = new HashMap();
    }

    @Override // meteordevelopment.meteorclient.commands.Command
    public void build(LiteralArgumentBuilder<class_2172> builder) {
        builder.then(literal("help").executes(ctx -> {
            class_156.method_668().method_670("https://github.com/MeteorDevelopment/meteor-client/wiki/Notebot-Guide");
            return 1;
        }));
        builder.then(literal("status").executes(ctx2 -> {
            Notebot notebot = (Notebot) Modules.get().get(Notebot.class);
            info(notebot.getStatus(), new Object[0]);
            return 1;
        }));
        builder.then(literal("pause").executes(ctx3 -> {
            Notebot notebot = (Notebot) Modules.get().get(Notebot.class);
            notebot.pause();
            return 1;
        }));
        builder.then(literal("resume").executes(ctx4 -> {
            Notebot notebot = (Notebot) Modules.get().get(Notebot.class);
            notebot.pause();
            return 1;
        }));
        builder.then(literal("stop").executes(ctx5 -> {
            Notebot notebot = (Notebot) Modules.get().get(Notebot.class);
            notebot.stop();
            return 1;
        }));
        builder.then(literal("randomsong").executes(ctx6 -> {
            Notebot notebot = (Notebot) Modules.get().get(Notebot.class);
            notebot.playRandomSong();
            return 1;
        }));
        builder.then(literal("play").then(argument("song", NotebotSongArgumentType.create()).executes(ctx7 -> {
            Notebot notebot = (Notebot) Modules.get().get(Notebot.class);
            Path songPath = (Path) ctx7.getArgument("song", Path.class);
            if (songPath == null || !songPath.toFile().exists()) {
                throw INVALID_SONG.create();
            }
            notebot.loadSong(songPath.toFile());
            return 1;
        })));
        builder.then(literal("preview").then(argument("song", NotebotSongArgumentType.create()).executes(ctx8 -> {
            Notebot notebot = (Notebot) Modules.get().get(Notebot.class);
            Path songPath = (Path) ctx8.getArgument("song", Path.class);
            if (songPath == null || !songPath.toFile().exists()) {
                throw INVALID_SONG.create();
            }
            notebot.previewSong(songPath.toFile());
            return 1;
        })));
        builder.then(literal("record").then(literal("start").executes(ctx9 -> {
            this.ticks = -1;
            this.song.clear();
            MeteorClient.EVENT_BUS.subscribe(this);
            info("Recording started", new Object[0]);
            return 1;
        })));
        builder.then(literal("record").then(literal("cancel").executes(ctx10 -> {
            MeteorClient.EVENT_BUS.unsubscribe(this);
            info("Recording cancelled", new Object[0]);
            return 1;
        })));
        builder.then(literal("record").then(literal("save").then(argument("name", StringArgumentType.greedyString()).executes(ctx11 -> {
            String name = (String) ctx11.getArgument("name", String.class);
            if (name == null || name.isEmpty()) {
                throw INVALID_PATH.create(name);
            }
            Path notebotFolder = MeteorClient.FOLDER.toPath().resolve("notebot");
            Path path = notebotFolder.resolve(String.format("%s.txt", name)).normalize();
            if (!path.startsWith(notebotFolder)) {
                throw INVALID_PATH.create(path);
            }
            saveRecording(path);
            return 1;
        }))));
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (this.ticks == -1) {
            return;
        }
        this.ticks++;
    }

    @EventHandler
    private void onReadPacket(PacketEvent.Receive event) {
        class_2596<?> class_2596Var = event.packet;
        if (class_2596Var instanceof class_2767) {
            class_2767 sound = (class_2767) class_2596Var;
            if (((class_3414) sound.method_11894().comp_349()).comp_3319().method_12832().contains("note_block")) {
                if (this.ticks == -1) {
                    this.ticks = 0;
                }
                List<Note> notes = this.song.computeIfAbsent(Integer.valueOf(this.ticks), tick -> {
                    return new ArrayList();
                });
                Note note = getNote(sound);
                if (note != null) {
                    notes.add(note);
                }
            }
        }
    }

    private void saveRecording(Path path) {
        if (this.song.isEmpty()) {
            MeteorClient.EVENT_BUS.unsubscribe(this);
            return;
        }
        try {
            MeteorClient.EVENT_BUS.unsubscribe(this);
            FileWriter file = new FileWriter(path.toFile());
            for (Map.Entry<Integer, List<Note>> entry : this.song.entrySet()) {
                int tick = entry.getKey().intValue();
                List<Note> notes = entry.getValue();
                for (Note note : notes) {
                    class_2766 instrument = note.getInstrument();
                    int noteLevel = note.getNoteLevel();
                    file.write(String.format("%d:%d:%d\n", Integer.valueOf(tick), Integer.valueOf(noteLevel), Integer.valueOf(instrument.ordinal())));
                }
            }
            file.close();
            info("Song saved.", new Object[0]);
        } catch (IOException e) {
            info("Couldn't create the file.", new Object[0]);
            MeteorClient.EVENT_BUS.unsubscribe(this);
        }
    }

    private Note getNote(class_2767 soundPacket) {
        float pitch = soundPacket.method_11892();
        int noteLevel = -1;
        int n = 0;
        while (true) {
            if (n < 25) {
                if (((double) ((float) Math.pow(2.0d, ((double) (n - 12)) / 12.0d))) - 0.01d >= pitch || ((double) ((float) Math.pow(2.0d, ((double) (n - 12)) / 12.0d))) + 0.01d <= pitch) {
                    n++;
                } else {
                    noteLevel = n;
                    break;
                }
            } else {
                break;
            }
        }
        if (noteLevel == -1) {
            error("Error while bruteforcing a note level! Sound: " + String.valueOf(soundPacket.method_11894().comp_349()) + " Pitch: " + pitch, new Object[0]);
            return null;
        }
        class_2766 instrument = getInstrumentFromSound((class_3414) soundPacket.method_11894().comp_349());
        if (instrument == null) {
            error("Can't find the instrument from sound! Sound: " + String.valueOf(soundPacket.method_11894().comp_349()), new Object[0]);
            return null;
        }
        return new Note(instrument, noteLevel);
    }

    private class_2766 getInstrumentFromSound(class_3414 sound) {
        String path = sound.comp_3319().method_12832();
        if (path.contains("harp")) {
            return class_2766.field_12648;
        }
        if (path.contains("basedrum")) {
            return class_2766.field_12653;
        }
        if (path.contains("snare")) {
            return class_2766.field_12643;
        }
        if (path.contains("hat")) {
            return class_2766.field_12645;
        }
        if (path.contains("bass")) {
            return class_2766.field_12651;
        }
        if (path.contains("flute")) {
            return class_2766.field_12650;
        }
        if (path.contains("bell")) {
            return class_2766.field_12644;
        }
        if (path.contains("guitar")) {
            return class_2766.field_12654;
        }
        if (path.contains("chime")) {
            return class_2766.field_12647;
        }
        if (path.contains("xylophone")) {
            return class_2766.field_12655;
        }
        if (path.contains("iron_xylophone")) {
            return class_2766.field_18284;
        }
        if (path.contains("cow_bell")) {
            return class_2766.field_18285;
        }
        if (path.contains("didgeridoo")) {
            return class_2766.field_18286;
        }
        if (path.contains("bit")) {
            return class_2766.field_18287;
        }
        if (path.contains("banjo")) {
            return class_2766.field_18288;
        }
        if (path.contains("pling")) {
            return class_2766.field_18289;
        }
        return null;
    }
}
