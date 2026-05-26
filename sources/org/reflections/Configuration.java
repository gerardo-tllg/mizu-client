package org.reflections;

import java.net.URL;
import java.util.Set;
import java.util.function.Predicate;
import org.reflections.scanners.Scanner;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:org/reflections/Configuration.class */
public interface Configuration {
    Set<Scanner> getScanners();

    Set<URL> getUrls();

    Predicate<String> getInputsFilter();

    boolean isParallel();

    ClassLoader[] getClassLoaders();

    boolean shouldExpandSuperTypes();
}
