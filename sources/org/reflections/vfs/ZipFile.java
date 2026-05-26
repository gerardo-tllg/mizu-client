package org.reflections.vfs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import org.reflections.vfs.Vfs;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:org/reflections/vfs/ZipFile.class */
public class ZipFile implements Vfs.File {
    private final ZipDir root;
    private final ZipEntry entry;

    public ZipFile(ZipDir root, ZipEntry entry) {
        this.root = root;
        this.entry = entry;
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
    public InputStream openInputStream() throws IOException {
        return this.root.jarFile.getInputStream(this.entry);
    }

    public String toString() {
        return this.root.getPath() + "!" + File.separatorChar + this.entry.toString();
    }
}
