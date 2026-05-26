package javassist;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/* JADX INFO: compiled from: ClassPoolTail.java */
/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:javassist/DirClassPath.class */
final class DirClassPath implements ClassPath {
    String directory;

    DirClassPath(String dirName) {
        this.directory = dirName;
    }

    @Override // javassist.ClassPath
    public InputStream openClassfile(String classname) {
        try {
            char sep = File.separatorChar;
            String filename = this.directory + sep + classname.replace('.', sep) + ".class";
            return new FileInputStream(filename.toString());
        } catch (FileNotFoundException | SecurityException e) {
            return null;
        }
    }

    @Override // javassist.ClassPath
    public URL find(String classname) {
        char sep = File.separatorChar;
        String filename = this.directory + sep + classname.replace('.', sep) + ".class";
        File f = new File(filename);
        if (f.exists()) {
            try {
                return f.getCanonicalFile().toURI().toURL();
            } catch (MalformedURLException e) {
                return null;
            } catch (IOException e2) {
                return null;
            }
        }
        return null;
    }

    public String toString() {
        return this.directory;
    }
}
