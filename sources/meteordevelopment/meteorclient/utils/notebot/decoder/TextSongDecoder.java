package meteordevelopment.meteorclient.utils.notebot.decoder;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import meteordevelopment.meteorclient.utils.notebot.song.Note;
import meteordevelopment.meteorclient.utils.notebot.song.Song;
import net.minecraft.class_2766;
import org.apache.commons.io.FilenameUtils;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/notebot/decoder/TextSongDecoder.class */
public class TextSongDecoder extends SongDecoder {
    @Override // meteordevelopment.meteorclient.utils.notebot.decoder.SongDecoder
    public Song parse(File file) throws Exception {
        List<String> data = Files.readAllLines(file.toPath());
        ListMultimap listMultimapBuild = MultimapBuilder.linkedHashKeys().arrayListValues().build();
        String title = FilenameUtils.getBaseName(file.getName());
        String author = "Unknown";
        for (int lineNumber = 0; lineNumber < data.size(); lineNumber++) {
            String line = data.get(lineNumber);
            if (line.startsWith("// Name: ")) {
                title = line.substring(9);
            } else if (line.startsWith("// Author: ")) {
                author = line.substring(11);
            } else if (!line.isEmpty()) {
                String[] parts = data.get(lineNumber).split(":");
                if (parts.length < 2) {
                    this.notebot.warning("Malformed line %d", Integer.valueOf(lineNumber));
                } else {
                    int type = 0;
                    try {
                        int key = Integer.parseInt(parts[0]);
                        int val = Integer.parseInt(parts[1]);
                        if (parts.length > 2) {
                            type = Integer.parseInt(parts[2]);
                        }
                        Note note = new Note(class_2766.values()[type], val);
                        listMultimapBuild.put(Integer.valueOf(key), note);
                    } catch (NumberFormatException e) {
                        this.notebot.warning("Invalid character at line %d", Integer.valueOf(lineNumber));
                    }
                }
            }
        }
        return new Song(listMultimapBuild, title, author);
    }
}
