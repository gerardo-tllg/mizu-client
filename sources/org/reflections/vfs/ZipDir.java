package org.reflections.vfs;

import java.io.IOException;
import java.util.jar.JarFile;
import org.reflections.Reflections;
import org.reflections.vfs.Vfs;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:org/reflections/vfs/ZipDir.class */
public class ZipDir implements Vfs.Dir {
    final java.util.zip.ZipFile jarFile;

    public ZipDir(JarFile jarFile) {
        this.jarFile = jarFile;
    }

    @Override // org.reflections.vfs.Vfs.Dir
    public String getPath() {
        return this.jarFile != null ? this.jarFile.getName().replace("\\", "/") : "/NO-SUCH-DIRECTORY/";
    }

    @Override // org.reflections.vfs.Vfs.Dir
    public Iterable<Vfs.File> getFiles() {
        return () -> {
            return this.jarFile.stream().filter(entry -> {
                return !entry.isDirectory();
            }).map(entry2 -> {
                return new ZipFile(this, entry2);
            }).iterator();
        };
    }

    @Override // org.reflections.vfs.Vfs.Dir
    public void close() {
        try {
            this.jarFile.close();
        } catch (IOException e) {
            if (Reflections.log != null) {
                Reflections.log.warn("Could not close JarFile", e);
            }
        }
    }

    public String toString() {
        return this.jarFile.getName();
    }
}
