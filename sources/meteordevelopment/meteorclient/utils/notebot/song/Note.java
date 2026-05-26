package meteordevelopment.meteorclient.utils.notebot.song;

import java.util.Objects;
import net.minecraft.class_2766;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/notebot/song/Note.class */
public class Note {
    private class_2766 instrument;
    private int noteLevel;

    public Note(class_2766 instrument, int noteLevel) {
        this.instrument = instrument;
        this.noteLevel = noteLevel;
    }

    public class_2766 getInstrument() {
        return this.instrument;
    }

    public void setInstrument(class_2766 instrument) {
        this.instrument = instrument;
    }

    public int getNoteLevel() {
        return this.noteLevel;
    }

    public void setNoteLevel(int noteLevel) {
        this.noteLevel = noteLevel;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Note note = (Note) o;
        return this.instrument == note.instrument && this.noteLevel == note.noteLevel;
    }

    public int hashCode() {
        return Objects.hash(this.instrument, Integer.valueOf(this.noteLevel));
    }

    public String toString() {
        return "Note{instrument=" + String.valueOf(getInstrument()) + ", noteLevel=" + getNoteLevel() + "}";
    }
}
