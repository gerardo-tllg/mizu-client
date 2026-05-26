package org.reflections.vfs;

import java.net.URL;
import java.util.Iterator;
import java.util.Stack;
import java.util.jar.JarFile;
import org.jboss.vfs.VirtualFile;
import org.reflections.vfs.Vfs;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:org/reflections/vfs/JbossDir.class */
public class JbossDir implements Vfs.Dir {
    private final VirtualFile virtualFile;

    private JbossDir(VirtualFile virtualFile) {
        this.virtualFile = virtualFile;
    }

    public static Vfs.Dir createDir(URL url) throws Exception {
        VirtualFile virtualFile = (VirtualFile) url.openConnection().getContent();
        if (virtualFile.isFile()) {
            return new ZipDir(new JarFile(virtualFile.getPhysicalFile()));
        }
        return new JbossDir(virtualFile);
    }

    @Override // org.reflections.vfs.Vfs.Dir
    public String getPath() {
        return this.virtualFile.getPathName();
    }

    @Override // org.reflections.vfs.Vfs.Dir
    public Iterable<Vfs.File> getFiles() {
        return () -> {
            return new Iterator<Vfs.File>() { // from class: org.reflections.vfs.JbossDir.1
                final Stack stack = new Stack();
                Vfs.File entry = null;

                {
                    this.stack.addAll(JbossDir.this.virtualFile.getChildren());
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
                    while (!this.stack.isEmpty()) {
                        VirtualFile file = (VirtualFile) this.stack.pop();
                        if (file.isDirectory()) {
                            this.stack.addAll(file.getChildren());
                        } else {
                            return new JbossFile(JbossDir.this, file);
                        }
                    }
                    return null;
                }
            };
        };
    }
}
