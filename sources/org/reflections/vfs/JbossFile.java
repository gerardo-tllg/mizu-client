package org.reflections.vfs;

import java.io.IOException;
import java.io.InputStream;
import org.jboss.vfs.VirtualFile;
import org.reflections.vfs.Vfs;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:org/reflections/vfs/JbossFile.class */
public class JbossFile implements Vfs.File {
    private final JbossDir root;
    private final VirtualFile virtualFile;

    public JbossFile(JbossDir root, VirtualFile virtualFile) {
        this.root = root;
        this.virtualFile = virtualFile;
    }

    @Override // org.reflections.vfs.Vfs.File
    public String getName() {
        return this.virtualFile.getName();
    }

    @Override // org.reflections.vfs.Vfs.File
    public String getRelativePath() {
        String filepath = this.virtualFile.getPathName();
        if (filepath.startsWith(this.root.getPath())) {
            return filepath.substring(this.root.getPath().length() + 1);
        }
        return null;
    }

    @Override // org.reflections.vfs.Vfs.File
    public InputStream openInputStream() throws IOException {
        return this.virtualFile.openStream();
    }
}
