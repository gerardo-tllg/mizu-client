package meteordevelopment.meteorclient.utils.notebot.decoder;

import java.io.File;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.Notebot;
import meteordevelopment.meteorclient.utils.notebot.song.Song;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/notebot/decoder/SongDecoder.class */
public abstract class SongDecoder {
    protected Notebot notebot = (Notebot) Modules.get().get(Notebot.class);

    public abstract Song parse(File file) throws Exception;
}
