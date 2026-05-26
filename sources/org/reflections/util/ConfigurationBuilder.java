package org.reflections.util;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.reflections.Configuration;
import org.reflections.ReflectionsException;
import org.reflections.scanners.Scanner;
import org.reflections.scanners.Scanners;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:org/reflections/util/ConfigurationBuilder.class */
public class ConfigurationBuilder implements Configuration {
    public static final Set<Scanner> DEFAULT_SCANNERS = new HashSet(Arrays.asList(Scanners.TypesAnnotated, Scanners.SubTypes));
    public static final Predicate<String> DEFAULT_INPUTS_FILTER = t -> {
        return true;
    };
    private Set<Scanner> scanners;
    private Predicate<String> inputsFilter;
    private ClassLoader[] classLoaders;
    private boolean isParallel = true;
    private boolean expandSuperTypes = true;
    private Set<URL> urls = new HashSet();

    public static ConfigurationBuilder build(Object... params) {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        List<Object> parameters = new ArrayList<>();
        for (Object param : params) {
            if (param.getClass().isArray()) {
                for (Object p : (Object[]) param) {
                    parameters.add(p);
                }
            } else if (param instanceof Iterable) {
                for (Object p2 : (Iterable) param) {
                    parameters.add(p2);
                }
            } else {
                parameters.add(param);
            }
        }
        ClassLoader[] loaders = (ClassLoader[]) Stream.of(params).filter(p3 -> {
            return p3 instanceof ClassLoader;
        }).distinct().toArray(x$0 -> {
            return new ClassLoader[x$0];
        });
        if (loaders.length != 0) {
            builder.addClassLoaders(loaders);
        }
        FilterBuilder inputsFilter = new FilterBuilder();
        builder.filterInputsBy(inputsFilter);
        for (Object param2 : parameters) {
            if ((param2 instanceof String) && !((String) param2).isEmpty()) {
                builder.forPackage((String) param2, loaders);
                inputsFilter.includePackage((String) param2);
            } else if ((param2 instanceof Class) && !Scanner.class.isAssignableFrom((Class) param2)) {
                builder.addUrls(ClasspathHelper.forClass((Class) param2, loaders));
                inputsFilter.includePackage(((Class) param2).getPackage().getName());
            } else if (param2 instanceof URL) {
                builder.addUrls((URL) param2);
            } else if (param2 instanceof Scanner) {
                builder.addScanners((Scanner) param2);
            } else if ((param2 instanceof Class) && Scanner.class.isAssignableFrom((Class) param2)) {
                try {
                    builder.addScanners((Scanner) ((Class) param2).getDeclaredConstructor(new Class[0]).newInstance(new Object[0]));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else if (param2 instanceof Predicate) {
                builder.filterInputsBy((Predicate) param2);
            } else {
                throw new ReflectionsException("could not use param '" + param2 + "'");
            }
        }
        if (builder.getUrls().isEmpty()) {
            builder.addUrls(ClasspathHelper.forClassLoader(loaders));
        }
        return builder;
    }

    public ConfigurationBuilder forPackage(String pkg, ClassLoader... classLoaders) {
        return addUrls(ClasspathHelper.forPackage(pkg, classLoaders));
    }

    public ConfigurationBuilder forPackages(String... packages) {
        for (String pkg : packages) {
            forPackage(pkg, new ClassLoader[0]);
        }
        return this;
    }

    @Override // org.reflections.Configuration
    public Set<Scanner> getScanners() {
        return this.scanners != null ? this.scanners : DEFAULT_SCANNERS;
    }

    public ConfigurationBuilder setScanners(Scanner... scanners) {
        this.scanners = new HashSet(Arrays.asList(scanners));
        return this;
    }

    public ConfigurationBuilder addScanners(Scanner... scanners) {
        if (this.scanners == null) {
            setScanners(scanners);
        } else {
            this.scanners.addAll(Arrays.asList(scanners));
        }
        return this;
    }

    @Override // org.reflections.Configuration
    public Set<URL> getUrls() {
        return this.urls;
    }

    public ConfigurationBuilder setUrls(Collection<URL> urls) {
        this.urls = new HashSet(urls);
        return this;
    }

    public ConfigurationBuilder setUrls(URL... urls) {
        return setUrls(Arrays.asList(urls));
    }

    public ConfigurationBuilder addUrls(Collection<URL> urls) {
        this.urls.addAll(urls);
        return this;
    }

    public ConfigurationBuilder addUrls(URL... urls) {
        return addUrls(Arrays.asList(urls));
    }

    @Override // org.reflections.Configuration
    public Predicate<String> getInputsFilter() {
        return this.inputsFilter != null ? this.inputsFilter : DEFAULT_INPUTS_FILTER;
    }

    public ConfigurationBuilder setInputsFilter(Predicate<String> inputsFilter) {
        this.inputsFilter = inputsFilter;
        return this;
    }

    public ConfigurationBuilder filterInputsBy(Predicate<String> inputsFilter) {
        return setInputsFilter(inputsFilter);
    }

    @Override // org.reflections.Configuration
    public boolean isParallel() {
        return this.isParallel;
    }

    public ConfigurationBuilder setParallel(boolean parallel) {
        this.isParallel = parallel;
        return this;
    }

    @Override // org.reflections.Configuration
    public ClassLoader[] getClassLoaders() {
        return this.classLoaders;
    }

    public ConfigurationBuilder setClassLoaders(ClassLoader[] classLoaders) {
        this.classLoaders = classLoaders;
        return this;
    }

    public ConfigurationBuilder addClassLoaders(ClassLoader... classLoaders) {
        this.classLoaders = this.classLoaders == null ? classLoaders : (ClassLoader[]) Stream.concat(Arrays.stream(this.classLoaders), Arrays.stream(classLoaders)).distinct().toArray(x$0 -> {
            return new ClassLoader[x$0];
        });
        return this;
    }

    @Override // org.reflections.Configuration
    public boolean shouldExpandSuperTypes() {
        return this.expandSuperTypes;
    }

    public ConfigurationBuilder setExpandSuperTypes(boolean expandSuperTypes) {
        this.expandSuperTypes = expandSuperTypes;
        return this;
    }
}
