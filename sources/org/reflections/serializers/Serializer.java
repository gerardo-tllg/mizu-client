package org.reflections.serializers;

import java.io.File;
import java.io.InputStream;
import org.reflections.Reflections;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:org/reflections/serializers/Serializer.class */
public interface Serializer {
    Reflections read(InputStream inputStream);

    File save(Reflections reflections, String str);

    static File prepareFile(String filename) {
        File file = new File(filename);
        File parent = file.getAbsoluteFile().getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        return file;
    }
}
