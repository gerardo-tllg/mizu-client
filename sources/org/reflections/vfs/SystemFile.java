package org.reflections.vfs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import org.reflections.vfs.Vfs;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:org/reflections/vfs/SystemFile.class */
public class SystemFile implements Vfs.File {
    private final SystemDir root;
    private final File file;

    public SystemFile(SystemDir root, File file) {
        this.root = root;
        this.file = file;
    }

    @Override // org.reflections.vfs.Vfs.File
    public String getName() {
        return this.file.getName();
    }

    @Override // org.reflections.vfs.Vfs.File
    public String getRelativePath() {
        String filepath = this.file.getPath().replace("\\", "/");
        if (filepath.startsWith(this.root.getPath())) {
            return filepath.substring(this.root.getPath().length() + 1);
        }
        return null;
    }

    @Override // org.reflections.vfs.Vfs.File
    public InputStream openInputStream() {
        try {
            return new FileInputStream(this.file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public String toString() {
        return this.file.toString();
    }
}
