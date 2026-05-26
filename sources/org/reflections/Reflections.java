package org.reflections;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javassist.bytecode.ClassFile;
import javax.annotation.Nullable;
import org.reflections.scanners.MemberUsageScanner;
import org.reflections.scanners.MethodParameterNamesScanner;
import org.reflections.scanners.Scanner;
import org.reflections.scanners.Scanners;
import org.reflections.serializers.Serializer;
import org.reflections.serializers.XmlSerializer;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.reflections.util.NameHelper;
import org.reflections.util.QueryFunction;
import org.reflections.vfs.Vfs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:org/reflections/Reflections.class */
public class Reflections implements NameHelper {
    public static final Logger log = LoggerFactory.getLogger(Reflections.class);
    protected final transient Configuration configuration;
    protected final Store store;

    public Reflections(Configuration configuration) {
        this.configuration = configuration;
        Map<String, Map<String, Set<String>>> storeMap = scan();
        if (configuration.shouldExpandSuperTypes()) {
            expandSuperTypes(storeMap.get(Scanners.SubTypes.index()), storeMap.get(Scanners.TypesAnnotated.index()));
        }
        this.store = new Store(storeMap);
    }

    public Reflections(Store store) {
        this.configuration = new ConfigurationBuilder();
        this.store = store;
    }

    public Reflections(String prefix, Scanner... scanners) {
        this(prefix, scanners);
    }

    public Reflections(Object... params) {
        this(ConfigurationBuilder.build(params));
    }

    protected Reflections() {
        this.configuration = new ConfigurationBuilder();
        this.store = new Store(new HashMap());
    }

    protected Map<String, Map<String, Set<String>>> scan() {
        long start = System.currentTimeMillis();
        Map<String, Set<Map.Entry<String, String>>> collect = (Map) this.configuration.getScanners().stream().map((v0) -> {
            return v0.index();
        }).distinct().collect(Collectors.toMap(s -> {
            return s;
        }, s2 -> {
            return Collections.synchronizedSet(new HashSet());
        }));
        Set<URL> urls = this.configuration.getUrls();
        (this.configuration.isParallel() ? (Stream) urls.stream().parallel() : urls.stream()).forEach(url -> {
            Vfs.Dir dir = null;
            try {
                try {
                    dir = Vfs.fromURL(url);
                    for (Vfs.File file : dir.getFiles()) {
                        if (doFilter(file, this.configuration.getInputsFilter())) {
                            ClassFile classFile = null;
                            for (Scanner scanner : this.configuration.getScanners()) {
                                try {
                                    scanner.getClass();
                                    if (doFilter(file, scanner::acceptsInput)) {
                                        List<Map.Entry<String, String>> entries = scanner.scan(file);
                                        if (entries == null) {
                                            if (classFile == null) {
                                                classFile = getClassFile(file);
                                            }
                                            entries = scanner.scan(classFile);
                                        }
                                        if (entries != null) {
                                            ((Set) collect.get(scanner.index())).addAll(entries);
                                        }
                                    }
                                } catch (Exception e) {
                                    if (log != null) {
                                        log.trace("could not scan file {} with scanner {}", new Object[]{file.getRelativePath(), scanner.getClass().getSimpleName(), e});
                                    }
                                }
                            }
                        }
                    }
                    if (dir != null) {
                        dir.close();
                    }
                } catch (Exception e2) {
                    if (log != null) {
                        log.warn("could not create Vfs.Dir from url. ignoring the exception and continuing", e2);
                    }
                    if (dir != null) {
                        dir.close();
                    }
                }
            } catch (Throwable th) {
                if (dir != null) {
                    dir.close();
                }
                throw th;
            }
        });
        Map<String, Map<String, Set<String>>> storeMap = (Map) collect.entrySet().stream().collect(Collectors.toMap((v0) -> {
            return v0.getKey();
        }, entry -> {
            return (HashMap) ((Set) entry.getValue()).stream().filter(e -> {
                return e.getKey() != null;
            }).collect(Collectors.groupingBy((v0) -> {
                return v0.getKey();
            }, HashMap::new, Collectors.mapping((v0) -> {
                return v0.getValue();
            }, Collectors.toSet())));
        }));
        if (log != null) {
            int keys = 0;
            int values = 0;
            for (Map<String, Set<String>> map : storeMap.values()) {
                keys += map.size();
                values = (int) (((long) values) + map.values().stream().mapToLong((v0) -> {
                    return v0.size();
                }).sum());
            }
            log.info(String.format("Reflections took %d ms to scan %d urls, producing %d keys and %d values", Long.valueOf(System.currentTimeMillis() - start), Integer.valueOf(urls.size()), Integer.valueOf(keys), Integer.valueOf(values)));
        }
        return storeMap;
    }

    private boolean doFilter(Vfs.File file, @Nullable Predicate<String> predicate) {
        String path = file.getRelativePath();
        String fqn = path.replace('/', '.');
        return predicate == null || predicate.test(path) || predicate.test(fqn);
    }

    private ClassFile getClassFile(Vfs.File file) {
        try {
            DataInputStream dis = new DataInputStream(new BufferedInputStream(file.openInputStream()));
            Throwable th = null;
            try {
                try {
                    ClassFile classFile = new ClassFile(dis);
                    if (dis != null) {
                        if (0 != 0) {
                            try {
                                dis.close();
                            } catch (Throwable th2) {
                                th.addSuppressed(th2);
                            }
                        } else {
                            dis.close();
                        }
                    }
                    return classFile;
                } finally {
                }
            } finally {
            }
        } catch (Exception e) {
            throw new ReflectionsException("could not create class object from file " + file.getRelativePath(), e);
        }
    }

    public static Reflections collect() {
        return collect("META-INF/reflections/", new FilterBuilder().includePattern(".*-reflections\\.xml"));
    }

    public static Reflections collect(String packagePrefix, Predicate<String> resourceNameFilter) {
        return collect(packagePrefix, resourceNameFilter, new XmlSerializer());
    }

    public static Reflections collect(String packagePrefix, Predicate<String> resourceNameFilter, Serializer serializer) {
        Collection<URL> urls = ClasspathHelper.forPackage(packagePrefix, new ClassLoader[0]);
        Iterable<Vfs.File> files = Vfs.findFiles(urls, packagePrefix, resourceNameFilter);
        Reflections reflections = new Reflections();
        StreamSupport.stream(files.spliterator(), false).forEach(file -> {
            try {
                InputStream inputStream = file.openInputStream();
                Throwable th = null;
                try {
                    try {
                        reflections.collect(inputStream, serializer);
                        if (inputStream != null) {
                            if (0 != 0) {
                                try {
                                    inputStream.close();
                                } catch (Throwable th2) {
                                    th.addSuppressed(th2);
                                }
                            } else {
                                inputStream.close();
                            }
                        }
                    } finally {
                    }
                } finally {
                }
            } catch (IOException e) {
                throw new ReflectionsException("could not merge " + file, e);
            }
        });
        return reflections;
    }

    public Reflections collect(InputStream inputStream, Serializer serializer) {
        return merge(serializer.read(inputStream));
    }

    public Reflections collect(File file, Serializer serializer) {
        try {
            FileInputStream inputStream = new FileInputStream(file);
            Throwable th = null;
            try {
                Reflections reflectionsCollect = collect(inputStream, serializer);
                if (inputStream != null) {
                    if (0 != 0) {
                        try {
                            inputStream.close();
                        } catch (Throwable th2) {
                            th.addSuppressed(th2);
                        }
                    } else {
                        inputStream.close();
                    }
                }
                return reflectionsCollect;
            } finally {
            }
        } catch (IOException e) {
            throw new ReflectionsException("could not obtain input stream from file " + file, e);
        }
    }

    public Reflections merge(Reflections reflections) {
        reflections.store.forEach((index, map) -> {
            this.store.merge(index, map, (m1, m2) -> {
                m2.forEach((k, v) -> {
                });
                return m1;
            });
        });
        return this;
    }

    public void expandSuperTypes(Map<String, Set<String>> subTypesStore, Map<String, Set<String>> typesAnnotatedStore) {
        if (subTypesStore == null || subTypesStore.isEmpty()) {
            return;
        }
        Set<String> keys = new LinkedHashSet<>(subTypesStore.keySet());
        keys.removeAll((Collection) subTypesStore.values().stream().flatMap((v0) -> {
            return v0.stream();
        }).collect(Collectors.toSet()));
        keys.remove("java.lang.Object");
        for (String key : keys) {
            Class<?> type = forClass(key, loaders());
            if (type != null) {
                expandSupertypes(subTypesStore, typesAnnotatedStore, key, type);
            }
        }
    }

    private void expandSupertypes(Map<String, Set<String>> subTypesStore, Map<String, Set<String>> typesAnnotatedStore, String key, Class<?> type) {
        Set<Annotation> typeAnnotations = ReflectionUtils.getAnnotations(type, new Predicate[0]);
        if (typesAnnotatedStore != null && !typeAnnotations.isEmpty()) {
            String typeName = type.getName();
            for (Annotation typeAnnotation : typeAnnotations) {
                String annotationName = typeAnnotation.annotationType().getName();
                typesAnnotatedStore.computeIfAbsent(annotationName, s -> {
                    return new HashSet();
                }).add(typeName);
            }
        }
        for (Class<?> supertype : ReflectionUtils.getSuperTypes(type)) {
            String supertypeName = supertype.getName();
            if (subTypesStore.containsKey(supertypeName)) {
                subTypesStore.get(supertypeName).add(key);
            } else {
                subTypesStore.computeIfAbsent(supertypeName, s2 -> {
                    return new HashSet();
                }).add(key);
                expandSupertypes(subTypesStore, typesAnnotatedStore, supertypeName, supertype);
            }
        }
    }

    public <T> Set<T> get(QueryFunction<Store, T> query) {
        return query.apply(this.store);
    }

    public <T> Set<Class<? extends T>> getSubTypesOf(Class<T> type) {
        return get(Scanners.SubTypes.of(type).as(Class.class, loaders()));
    }

    public Set<Class<?>> getTypesAnnotatedWith(Class<? extends Annotation> annotation) {
        return get(Scanners.SubTypes.of(Scanners.TypesAnnotated.with(annotation)).asClass(loaders()));
    }

    public Set<Class<?>> getTypesAnnotatedWith(Class<? extends Annotation> annotation, boolean honorInherited) {
        if (!honorInherited) {
            return getTypesAnnotatedWith(annotation);
        }
        if (annotation.isAnnotationPresent(Inherited.class)) {
            return get(Scanners.TypesAnnotated.get(annotation).add(Scanners.SubTypes.of(Scanners.TypesAnnotated.get(annotation).filter(c -> {
                return !forClass(c, loaders()).isInterface();
            }))).asClass(loaders()));
        }
        return get(Scanners.TypesAnnotated.get(annotation).asClass(loaders()));
    }

    public Set<Class<?>> getTypesAnnotatedWith(Annotation annotation) {
        return get(Scanners.SubTypes.of(Scanners.TypesAnnotated.of(Scanners.TypesAnnotated.get(annotation.annotationType()).filter(c -> {
            return ReflectionUtils.withAnnotation(annotation).test(forClass(c, loaders()));
        }))).asClass(loaders()));
    }

    public Set<Class<?>> getTypesAnnotatedWith(Annotation annotation, boolean honorInherited) {
        if (!honorInherited) {
            return getTypesAnnotatedWith(annotation);
        }
        Class<? extends Annotation> type = annotation.annotationType();
        if (type.isAnnotationPresent(Inherited.class)) {
            return get(Scanners.TypesAnnotated.with(type).asClass(loaders()).filter(ReflectionUtils.withAnnotation(annotation)).add(Scanners.SubTypes.of(Scanners.TypesAnnotated.with(type).asClass(loaders()).filter(c -> {
                return !c.isInterface();
            }))));
        }
        return get(Scanners.TypesAnnotated.with(type).asClass(loaders()).filter(ReflectionUtils.withAnnotation(annotation)));
    }

    public Set<Method> getMethodsAnnotatedWith(Class<? extends Annotation> annotation) {
        return get(Scanners.MethodsAnnotated.with(annotation).as(Method.class, loaders()));
    }

    public Set<Method> getMethodsAnnotatedWith(Annotation annotation) {
        return get(Scanners.MethodsAnnotated.with(annotation.annotationType()).as(Method.class, loaders()).filter(ReflectionUtils.withAnnotation(annotation)));
    }

    public Set<Method> getMethodsWithSignature(Class<?>... types) {
        return get(Scanners.MethodsSignature.with(types).as(Method.class, loaders()));
    }

    public Set<Method> getMethodsWithParameter(AnnotatedElement type) {
        return get(Scanners.MethodsParameter.with(type).as(Method.class, loaders()));
    }

    public Set<Method> getMethodsReturn(Class<?> type) {
        return get(Scanners.MethodsReturn.of(type).as(Method.class, loaders()));
    }

    public Set<Constructor> getConstructorsAnnotatedWith(Class<? extends Annotation> annotation) {
        return get(Scanners.ConstructorsAnnotated.with(annotation).as(Constructor.class, loaders()));
    }

    public Set<Constructor> getConstructorsAnnotatedWith(Annotation annotation) {
        return get(Scanners.ConstructorsAnnotated.with(annotation.annotationType()).as(Constructor.class, loaders()).filter(ReflectionUtils.withAnyParameterAnnotation(annotation)));
    }

    public Set<Constructor> getConstructorsWithSignature(Class<?>... types) {
        return get(Scanners.ConstructorsSignature.with(types).as(Constructor.class, loaders()));
    }

    public Set<Constructor> getConstructorsWithParameter(AnnotatedElement type) {
        return get(Scanners.ConstructorsParameter.of(type).as(Constructor.class, loaders()));
    }

    public Set<Field> getFieldsAnnotatedWith(Class<? extends Annotation> annotation) {
        return get(Scanners.FieldsAnnotated.with(annotation).as(Field.class, loaders()));
    }

    public Set<Field> getFieldsAnnotatedWith(Annotation annotation) {
        return get(Scanners.FieldsAnnotated.with(annotation.annotationType()).as(Field.class, loaders()).filter(ReflectionUtils.withAnnotation(annotation)));
    }

    public Set<String> getResources(String pattern) {
        return get(Scanners.Resources.with(pattern));
    }

    public Set<String> getResources(Pattern pattern) {
        return getResources(pattern.pattern());
    }

    public List<String> getMemberParameterNames(Member member) {
        return (List) this.store.getOrDefault(MethodParameterNamesScanner.class.getSimpleName(), Collections.emptyMap()).getOrDefault(toName((AnnotatedElement) member), Collections.emptySet()).stream().flatMap(s -> {
            return Stream.of((Object[]) s.split(", "));
        }).collect(Collectors.toList());
    }

    public Collection<Member> getMemberUsage(Member member) {
        Set<String> usages = this.store.getOrDefault(MemberUsageScanner.class.getSimpleName(), Collections.emptyMap()).getOrDefault(toName((AnnotatedElement) member), Collections.emptySet());
        return forNames(usages, Member.class, loaders());
    }

    @Deprecated
    public Set<String> getAllTypes() {
        return getAll(Scanners.SubTypes);
    }

    public Set<String> getAll(Scanner scanner) {
        Map<String, Set<String>> map = this.store.getOrDefault(scanner.index(), Collections.emptyMap());
        return (Set) Stream.concat(map.keySet().stream(), map.values().stream().flatMap((v0) -> {
            return v0.stream();
        })).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Store getStore() {
        return this.store;
    }

    public Configuration getConfiguration() {
        return this.configuration;
    }

    public File save(String filename) {
        return save(filename, new XmlSerializer());
    }

    public File save(String filename, Serializer serializer) {
        return serializer.save(this, filename);
    }

    ClassLoader[] loaders() {
        return this.configuration.getClassLoaders();
    }
}
