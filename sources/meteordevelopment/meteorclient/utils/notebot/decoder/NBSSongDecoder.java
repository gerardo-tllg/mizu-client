package meteordevelopment.meteorclient.utils.notebot.decoder;

import com.google.common.collect.Multimap;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.utils.notebot.song.Note;
import meteordevelopment.meteorclient.utils.notebot.song.Song;
import net.minecraft.class_2766;
import org.jetbrains.annotations.NotNull;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/notebot/decoder/NBSSongDecoder.class */
public class NBSSongDecoder extends SongDecoder {
    public static final int NOTE_OFFSET = 33;

    @Override // meteordevelopment.meteorclient.utils.notebot.decoder.SongDecoder
    @NotNull
    public Song parse(File songFile) throws Exception {
        return parse(new FileInputStream(songFile));
    }

    /* JADX WARN: Incorrect condition in loop: B:16:0x00c9 */
    @org.jetbrains.annotations.NotNull
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private meteordevelopment.meteorclient.utils.notebot.song.Song parse(java.io.InputStream r7) throws java.lang.Exception {
        /*
            Method dump skipped, instruction units count: 302
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: meteordevelopment.meteorclient.utils.notebot.decoder.NBSSongDecoder.parse(java.io.InputStream):meteordevelopment.meteorclient.utils.notebot.song.Song");
    }

    private static void setNote(int ticks, Note note, Multimap<Integer, Note> notesMap) {
        notesMap.put(Integer.valueOf(ticks), note);
    }

    private static short readShort(DataInputStream dataInputStream) throws IOException {
        int byte1 = dataInputStream.readUnsignedByte();
        int byte2 = dataInputStream.readUnsignedByte();
        return (short) (byte1 + (byte2 << 8));
    }

    private static int readInt(DataInputStream dataInputStream) throws IOException {
        int byte1 = dataInputStream.readUnsignedByte();
        int byte2 = dataInputStream.readUnsignedByte();
        int byte3 = dataInputStream.readUnsignedByte();
        int byte4 = dataInputStream.readUnsignedByte();
        return byte1 + (byte2 << 8) + (byte3 << 16) + (byte4 << 24);
    }

    private static String readString(DataInputStream dataInputStream) throws IOException {
        int length = readInt(dataInputStream);
        if (length < 0) {
            throw new EOFException("Length can't be negative! Length: " + length);
        }
        if (length > dataInputStream.available()) {
            throw new EOFException("Can't read string that is larger than a buffer! Length: " + length + " Readable Bytes Length: " + dataInputStream.available());
        }
        StringBuilder builder = new StringBuilder(length);
        while (length > 0) {
            char c = (char) dataInputStream.readByte();
            if (c == '\r') {
                c = ' ';
            }
            builder.append(c);
            length--;
        }
        return builder.toString();
    }

    private static class_2766 fromNBSInstrument(int instrument) {
        switch (instrument) {
            case 0:
                return class_2766.field_12648;
            case 1:
                return class_2766.field_12651;
            case 2:
                return class_2766.field_12653;
            case 3:
                return class_2766.field_12643;
            case 4:
                return class_2766.field_12645;
            case 5:
                return class_2766.field_12654;
            case 6:
                return class_2766.field_12650;
            case 7:
                return class_2766.field_12644;
            case 8:
                return class_2766.field_12647;
            case 9:
                return class_2766.field_12655;
            case 10:
                return class_2766.field_18284;
            case 11:
                return class_2766.field_18285;
            case 12:
                return class_2766.field_18286;
            case Opcode.FCONST_2 /* 13 */:
                return class_2766.field_18287;
            case Opcode.DCONST_0 /* 14 */:
                return class_2766.field_18288;
            case 15:
                return class_2766.field_18289;
            default:
                return null;
        }
    }
}
