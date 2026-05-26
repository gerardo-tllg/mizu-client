package org.reflections.vfs;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import org.reflections.vfs.Vfs;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:org/reflections/vfs/JarInputFile.class */
public class JarInputFile implements Vfs.File {
    private final ZipEntry entry;
    private final JarInputDir jarInputDir;
    private final long fromIndex;
    private final long endIndex;

    public JarInputFile(ZipEntry entry, JarInputDir jarInputDir, long cursor, long nextCursor) {
        this.entry = entry;
        this.jarInputDir = jarInputDir;
        this.fromIndex = cursor;
        this.endIndex = nextCursor;
    }

    @Override // org.reflections.vfs.Vfs.File
    public String getName() {
        String name = this.entry.getName();
        return name.substring(name.lastIndexOf("/") + 1);
    }

    @Override // org.reflections.vfs.Vfs.File
    public String getRelativePath() {
        return this.entry.getName();
    }

    @Override // org.reflections.vfs.Vfs.File
    public InputStream openInputStream() {
        return new InputStream() { // from class: org.reflections.vfs.JarInputFile.1
            @Override // java.io.InputStream
            public int read() throws IOException {
                if (JarInputFile.this.jarInputDir.cursor >= JarInputFile.this.fromIndex && JarInputFile.this.jarInputDir.cursor <= JarInputFile.this.endIndex) {
                    int read = JarInputFile.this.jarInputDir.jarInputStream.read();
                    JarInputFile.this.jarInputDir.cursor++;
                    return read;
                }
                return -1;
            }
        };
    }
}
