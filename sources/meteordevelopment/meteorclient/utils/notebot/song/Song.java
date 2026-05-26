package meteordevelopment.meteorclient.utils.notebot.song;

import com.google.common.collect.Multimap;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/notebot/song/Song.class */
public class Song {
    private final Multimap<Integer, Note> notesMap;
    private int lastTick;
    private final String title;
    private final String author;
    private final Set<Note> requirements = new HashSet();
    private boolean finishedLoading = false;

    public Song(Multimap<Integer, Note> notesMap, String title, String author) {
        this.notesMap = notesMap;
        this.title = title;
        this.author = author;
    }

    public void finishLoading() {
        if (this.finishedLoading) {
            throw new IllegalStateException("Song has already finished loading!");
        }
        this.lastTick = ((Integer) Collections.max(this.notesMap.keySet())).intValue();
        Stream streamDistinct = this.notesMap.values().stream().distinct();
        Set<Note> set = this.requirements;
        Objects.requireNonNull(set);
        streamDistinct.forEach((v1) -> {
            r1.add(v1);
        });
        this.finishedLoading = true;
    }

    public Multimap<Integer, Note> getNotesMap() {
        return this.notesMap;
    }

    public Set<Note> getRequirements() {
        if (this.finishedLoading) {
            return this.requirements;
        }
        throw new IllegalStateException("Song is still loading!");
    }

    public int getLastTick() {
        if (this.finishedLoading) {
            return this.lastTick;
        }
        throw new IllegalStateException("Song is still loading!");
    }

    public String getTitle() {
        return this.title;
    }

    public String getAuthor() {
        return this.author;
    }
}
