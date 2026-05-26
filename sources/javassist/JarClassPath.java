package javassist;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/* JADX INFO: compiled from: ClassPoolTail.java */
/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:javassist/JarClassPath.class */
final class JarClassPath implements ClassPath {
    Set<String> jarfileEntries;
    String jarfileURL;

    JarClassPath(String pathname) throws NotFoundException {
        JarFile jarfile = null;
        try {
            jarfile = new JarFile(pathname);
            this.jarfileEntries = new HashSet();
            for (JarEntry je : Collections.list(jarfile.entries())) {
                if (je.getName().endsWith(".class")) {
                    this.jarfileEntries.add(je.getName());
                }
            }
            this.jarfileURL = new File(pathname).getCanonicalFile().toURI().toURL().toString();
            if (null == jarfile) {
                return;
            }
            try {
                jarfile.close();
            } catch (IOException e) {
            }
        } catch (IOException e2) {
            if (null != jarfile) {
                try {
                    jarfile.close();
                } catch (IOException e3) {
                }
            }
            throw new NotFoundException(pathname);
        } catch (Throwable th) {
            if (null != jarfile) {
                try {
                    jarfile.close();
                } catch (IOException e4) {
                }
            }
            throw th;
        }
    }

    @Override // javassist.ClassPath
    public InputStream openClassfile(String classname) throws NotFoundException {
        URL jarURL = find(classname);
        if (null != jarURL) {
            try {
                if (ClassPool.cacheOpenedJarFile) {
                    return jarURL.openConnection().getInputStream();
                }
                URLConnection con = jarURL.openConnection();
                con.setUseCaches(false);
                return con.getInputStream();
            } catch (IOException e) {
                throw new NotFoundException("broken jar file?: " + classname);
            }
        }
        return null;
    }

    @Override // javassist.ClassPath
    public URL find(String classname) {
        String jarname = classname.replace('.', '/') + ".class";
        if (this.jarfileEntries.contains(jarname)) {
            try {
                return new URL(String.format("jar:%s!/%s", this.jarfileURL, jarname));
            } catch (MalformedURLException e) {
                return null;
            }
        }
        return null;
    }

    public String toString() {
        return this.jarfileURL == null ? "<null>" : this.jarfileURL.toString();
    }
}
