package org.reflections.vfs;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import org.reflections.Reflections;
import org.reflections.ReflectionsException;
import org.reflections.vfs.Vfs;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:org/reflections/vfs/JarInputDir.class */
public class JarInputDir implements Vfs.Dir {
    private final URL url;
    JarInputStream jarInputStream;
    long cursor = 0;
    long nextCursor = 0;

    public JarInputDir(URL url) {
        this.url = url;
    }

    @Override // org.reflections.vfs.Vfs.Dir
    public String getPath() {
        return this.url.getPath();
    }

    @Override // org.reflections.vfs.Vfs.Dir
    public Iterable<Vfs.File> getFiles() {
        return () -> {
            return new Iterator<Vfs.File>() { // from class: org.reflections.vfs.JarInputDir.1
                Vfs.File entry;

                {
                    try {
                        JarInputDir.this.jarInputStream = new JarInputStream(JarInputDir.this.url.openConnection().getInputStream());
                        this.entry = null;
                    } catch (Exception e) {
                        throw new ReflectionsException("Could not open url connection", e);
                    }
                }

                @Override // java.util.Iterator
                public boolean hasNext() {
                    if (this.entry == null) {
                        Vfs.File fileComputeNext = computeNext();
                        this.entry = fileComputeNext;
                        if (fileComputeNext == null) {
                            return false;
                        }
                    }
                    return true;
                }

                /* JADX WARN: Can't rename method to resolve collision */
                @Override // java.util.Iterator
                public Vfs.File next() {
                    Vfs.File next = this.entry;
                    this.entry = null;
                    return next;
                }

                private Vfs.File computeNext() {
                    ZipEntry entry;
                    do {
                        try {
                            entry = JarInputDir.this.jarInputStream.getNextJarEntry();
                            if (entry == null) {
                                return null;
                            }
                            long size = entry.getSize();
                            if (size < 0) {
                                size = 4294967295L + size;
                            }
                            JarInputDir.this.nextCursor += size;
                        } catch (IOException e) {
                            throw new ReflectionsException("could not get next zip entry", e);
                        }
                    } while (entry.isDirectory());
                    return new JarInputFile(entry, JarInputDir.this, JarInputDir.this.cursor, JarInputDir.this.nextCursor);
                }
            };
        };
    }

    @Override // org.reflections.vfs.Vfs.Dir
    public void close() {
        try {
            if (this.jarInputStream != null) {
                this.jarInputStream.close();
            }
        } catch (IOException e) {
            if (Reflections.log != null) {
                Reflections.log.warn("Could not close InputStream", e);
            }
        }
    }
}
