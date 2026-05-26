package org.reflections.scanners;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javassist.bytecode.ClassFile;
import javax.annotation.Nullable;
import org.reflections.vfs.Vfs;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:org/reflections/scanners/Scanner.class */
public interface Scanner {
    List<Map.Entry<String, String>> scan(ClassFile classFile);

    @Nullable
    default List<Map.Entry<String, String>> scan(Vfs.File file) {
        return null;
    }

    default String index() {
        return getClass().getSimpleName();
    }

    default boolean acceptsInput(String file) {
        return file.endsWith(".class");
    }

    default Map.Entry<String, String> entry(String key, String value) {
        return new AbstractMap.SimpleEntry(key, value);
    }

    default List<Map.Entry<String, String>> entries(Collection<String> keys, String value) {
        return (List) keys.stream().map(key -> {
            return entry(key, value);
        }).collect(Collectors.toList());
    }

    default List<Map.Entry<String, String>> entries(String key, String value) {
        return Collections.singletonList(entry(key, value));
    }

    default List<Map.Entry<String, String>> entries(String key, Collection<String> values) {
        return (List) values.stream().map(value -> {
            return entry(key, value);
        }).collect(Collectors.toList());
    }
}
