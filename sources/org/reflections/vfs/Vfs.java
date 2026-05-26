package org.reflections.vfs;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.reflections.Reflections;
import org.reflections.ReflectionsException;
import org.reflections.util.ClasspathHelper;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:org/reflections/vfs/Vfs.class */
public abstract class Vfs {
    private static List<UrlType> defaultUrlTypes = new ArrayList(Arrays.asList(DefaultUrlTypes.values()));

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:org/reflections/vfs/Vfs$DefaultUrlTypes.class */
    public enum DefaultUrlTypes implements UrlType {
        jarFile { // from class: org.reflections.vfs.Vfs.DefaultUrlTypes.1
            @Override // org.reflections.vfs.Vfs.UrlType
            public boolean matches(URL url) {
                return url.getProtocol().equals("file") && Vfs.hasJarFileInPath(url);
            }

            @Override // org.reflections.vfs.Vfs.UrlType
            public Dir createDir(URL url) throws Exception {
                return new ZipDir(new JarFile(Vfs.getFile(url)));
            }
        },
        jarUrl { // from class: org.reflections.vfs.Vfs.DefaultUrlTypes.2
            @Override // org.reflections.vfs.Vfs.UrlType
            public boolean matches(URL url) {
                return ("jar".equals(url.getProtocol()) || "zip".equals(url.getProtocol()) || "wsjar".equals(url.getProtocol())) && !Vfs.hasInnerJarFileInPath(url);
            }

            @Override // org.reflections.vfs.Vfs.UrlType
            public Dir createDir(URL url) throws Exception {
                try {
                    URLConnection urlConnection = url.openConnection();
                    if (urlConnection instanceof JarURLConnection) {
                        urlConnection.setUseCaches(false);
                        return new ZipDir(((JarURLConnection) urlConnection).getJarFile());
                    }
                } catch (Throwable th) {
                }
                java.io.File file = Vfs.getFile(url);
                if (file != null) {
                    return new ZipDir(new JarFile(file));
                }
                return null;
            }
        },
        directory { // from class: org.reflections.vfs.Vfs.DefaultUrlTypes.3
            @Override // org.reflections.vfs.Vfs.UrlType
            public boolean matches(URL url) {
                java.io.File file;
                return url.getProtocol().equals("file") && !Vfs.hasJarFileInPath(url) && (file = Vfs.getFile(url)) != null && file.isDirectory();
            }

            @Override // org.reflections.vfs.Vfs.UrlType
            public Dir createDir(URL url) throws Exception {
                return new SystemDir(Vfs.getFile(url));
            }
        },
        jboss_vfs { // from class: org.reflections.vfs.Vfs.DefaultUrlTypes.4
            @Override // org.reflections.vfs.Vfs.UrlType
            public boolean matches(URL url) {
                return url.getProtocol().equals("vfs");
            }

            @Override // org.reflections.vfs.Vfs.UrlType
            public Dir createDir(URL url) throws Exception {
                return JbossDir.createDir(url);
            }
        },
        jboss_vfsfile { // from class: org.reflections.vfs.Vfs.DefaultUrlTypes.5
            @Override // org.reflections.vfs.Vfs.UrlType
            public boolean matches(URL url) throws Exception {
                return "vfszip".equals(url.getProtocol()) || "vfsfile".equals(url.getProtocol());
            }

            @Override // org.reflections.vfs.Vfs.UrlType
            public Dir createDir(URL url) throws Exception {
                return new UrlTypeVFS().createDir(url);
            }
        },
        bundle { // from class: org.reflections.vfs.Vfs.DefaultUrlTypes.6
            @Override // org.reflections.vfs.Vfs.UrlType
            public boolean matches(URL url) throws Exception {
                return url.getProtocol().startsWith("bundle");
            }

            @Override // org.reflections.vfs.Vfs.UrlType
            public Dir createDir(URL url) throws Exception {
                return Vfs.fromURL((URL) ClasspathHelper.contextClassLoader().loadClass("org.eclipse.core.runtime.FileLocator").getMethod("resolve", URL.class).invoke(null, url));
            }
        },
        jarInputStream { // from class: org.reflections.vfs.Vfs.DefaultUrlTypes.7
            @Override // org.reflections.vfs.Vfs.UrlType
            public boolean matches(URL url) throws Exception {
                return url.toExternalForm().contains(".jar");
            }

            @Override // org.reflections.vfs.Vfs.UrlType
            public Dir createDir(URL url) throws Exception {
                return new JarInputDir(url);
            }
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:org/reflections/vfs/Vfs$File.class */
    public interface File {
        String getName();

        String getRelativePath();

        InputStream openInputStream() throws IOException;
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:org/reflections/vfs/Vfs$UrlType.class */
    public interface UrlType {
        boolean matches(URL url) throws Exception;

        Dir createDir(URL url) throws Exception;
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:org/reflections/vfs/Vfs$Dir.class */
    public interface Dir {
        String getPath();

        Iterable<File> getFiles();

        default void close() {
        }
    }

    public static List<UrlType> getDefaultUrlTypes() {
        return defaultUrlTypes;
    }

    public static void setDefaultURLTypes(List<UrlType> urlTypes) {
        defaultUrlTypes = urlTypes;
    }

    public static void addDefaultURLTypes(UrlType urlType) {
        defaultUrlTypes.add(0, urlType);
    }

    public static Dir fromURL(URL url) {
        return fromURL(url, defaultUrlTypes);
    }

    public static Dir fromURL(URL url, List<UrlType> urlTypes) {
        Dir dir;
        for (UrlType type : urlTypes) {
            try {
            } catch (Throwable e) {
                if (Reflections.log != null) {
                    Reflections.log.warn("could not create Dir using " + type + " from url " + url.toExternalForm() + ". skipping.", e);
                }
            }
            if (type.matches(url) && (dir = type.createDir(url)) != null) {
                return dir;
            }
        }
        throw new ReflectionsException("could not create Vfs.Dir from url, no matching UrlType was found [" + url.toExternalForm() + "]\neither use fromURL(final URL url, final List<UrlType> urlTypes) or use the static setDefaultURLTypes(final List<UrlType> urlTypes) or addDefaultURLTypes(UrlType urlType) with your specialized UrlType.");
    }

    public static Dir fromURL(URL url, UrlType... urlTypes) {
        return fromURL(url, (List<UrlType>) Arrays.asList(urlTypes));
    }

    public static Iterable<File> findFiles(Collection<URL> inUrls, String packagePrefix, Predicate<String> nameFilter) {
        Predicate<File> fileNamePredicate = file -> {
            String path = file.getRelativePath();
            if (path.startsWith(packagePrefix)) {
                String filename = path.substring(path.indexOf(packagePrefix) + packagePrefix.length());
                return !filename.isEmpty() && nameFilter.test(filename.substring(1));
            }
            return false;
        };
        return findFiles(inUrls, fileNamePredicate);
    }

    public static Iterable<File> findFiles(Collection<URL> urls, Predicate<File> filePredicate) {
        return () -> {
            return urls.stream().flatMap(url -> {
                try {
                    return StreamSupport.stream(fromURL(url).getFiles().spliterator(), false);
                } catch (Throwable e) {
                    if (Reflections.log != null) {
                        Reflections.log.error("could not findFiles for url. continuing. [" + url + "]", e);
                    }
                    return Stream.of((Object[]) new File[0]);
                }
            }).filter(filePredicate).iterator();
        };
    }

    public static java.io.File getFile(URL url) {
        try {
            java.io.File file = new java.io.File(url.toURI().getSchemeSpecificPart());
            if (file.exists()) {
                return file;
            }
        } catch (URISyntaxException e) {
        }
        try {
            String path = URLDecoder.decode(url.getPath(), "UTF-8");
            if (path.contains(".jar!")) {
                path = path.substring(0, path.lastIndexOf(".jar!") + ".jar".length());
            }
            java.io.File file2 = new java.io.File(path);
            if (file2.exists()) {
                return file2;
            }
        } catch (UnsupportedEncodingException e2) {
        }
        try {
            String path2 = url.toExternalForm();
            if (path2.startsWith("jar:")) {
                path2 = path2.substring("jar:".length());
            }
            if (path2.startsWith("wsjar:")) {
                path2 = path2.substring("wsjar:".length());
            }
            if (path2.startsWith("file:")) {
                path2 = path2.substring("file:".length());
            }
            if (path2.contains(".jar!")) {
                path2 = path2.substring(0, path2.indexOf(".jar!") + ".jar".length());
            }
            if (path2.contains(".war!")) {
                path2 = path2.substring(0, path2.indexOf(".war!") + ".war".length());
            }
            java.io.File file3 = new java.io.File(path2);
            if (file3.exists()) {
                return file3;
            }
            java.io.File file4 = new java.io.File(path2.replace("%20", " "));
            if (file4.exists()) {
                return file4;
            }
            return null;
        } catch (Exception e3) {
            return null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static boolean hasJarFileInPath(URL url) {
        return url.toExternalForm().matches(".*\\.jar(!.*|$)");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static boolean hasInnerJarFileInPath(URL url) {
        return url.toExternalForm().matches(".+\\.jar!/.+");
    }
}
