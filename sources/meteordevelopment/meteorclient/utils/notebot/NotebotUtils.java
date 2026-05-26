package meteordevelopment.meteorclient.utils.notebot;

import java.util.HashMap;
import java.util.Map;
import meteordevelopment.meteorclient.utils.notebot.instrumentdetect.InstrumentDetectFunction;
import meteordevelopment.meteorclient.utils.notebot.song.Note;
import net.minecraft.class_2338;
import net.minecraft.class_2428;
import net.minecraft.class_2680;
import net.minecraft.class_2766;
import org.jetbrains.annotations.Nullable;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/notebot/NotebotUtils.class */
public class NotebotUtils {

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/notebot/NotebotUtils$NotebotMode.class */
    public enum NotebotMode {
        AnyInstrument,
        ExactInstruments
    }

    public static Note getNoteFromNoteBlock(class_2680 noteBlock, class_2338 blockPos, NotebotMode mode, InstrumentDetectFunction instrumentDetectFunction) {
        class_2766 instrument = null;
        int level = ((Integer) noteBlock.method_11654(class_2428.field_11324)).intValue();
        if (mode == NotebotMode.ExactInstruments) {
            instrument = instrumentDetectFunction.detectInstrument(noteBlock, blockPos);
        }
        return new Note(instrument, level);
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/notebot/NotebotUtils$OptionalInstrument.class */
    public enum OptionalInstrument {
        None(null),
        Harp(class_2766.field_12648),
        Basedrum(class_2766.field_12653),
        Snare(class_2766.field_12643),
        Hat(class_2766.field_12645),
        Bass(class_2766.field_12651),
        Flute(class_2766.field_12650),
        Bell(class_2766.field_12644),
        Guitar(class_2766.field_12654),
        Chime(class_2766.field_12647),
        Xylophone(class_2766.field_12655),
        IronXylophone(class_2766.field_18284),
        CowBell(class_2766.field_18285),
        Didgeridoo(class_2766.field_18286),
        Bit(class_2766.field_18287),
        Banjo(class_2766.field_18288),
        Pling(class_2766.field_18289);

        public static final Map<class_2766, OptionalInstrument> BY_MINECRAFT_INSTRUMENT = new HashMap();
        private final class_2766 minecraftInstrument;

        static {
            for (OptionalInstrument optionalInstrument : values()) {
                BY_MINECRAFT_INSTRUMENT.put(optionalInstrument.minecraftInstrument, optionalInstrument);
            }
        }

        OptionalInstrument(@Nullable class_2766 minecraftInstrument) {
            this.minecraftInstrument = minecraftInstrument;
        }

        public class_2766 toMinecraftInstrument() {
            return this.minecraftInstrument;
        }

        public static OptionalInstrument fromMinecraftInstrument(class_2766 instrument) {
            if (instrument != null) {
                return BY_MINECRAFT_INSTRUMENT.get(instrument);
            }
            return null;
        }
    }
}
