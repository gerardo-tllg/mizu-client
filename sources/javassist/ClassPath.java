package javassist;

import java.io.InputStream;
import java.net.URL;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:javassist/ClassPath.class */
public interface ClassPath {
    InputStream openClassfile(String str) throws NotFoundException;

    URL find(String str);
}
