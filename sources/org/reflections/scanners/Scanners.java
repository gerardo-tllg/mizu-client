package org.reflections.scanners;

import java.lang.annotation.Inherited;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javassist.bytecode.ClassFile;
import org.reflections.Store;
import org.reflections.util.JavassistHelper;
import org.reflections.util.NameHelper;
import org.reflections.util.QueryBuilder;
import org.reflections.util.QueryFunction;
import org.reflections.vfs.Vfs;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:org/reflections/scanners/Scanners.class */
public enum Scanners implements Scanner, QueryBuilder, NameHelper {
    SubTypes { // from class: org.reflections.scanners.Scanners.1
        @Override // org.reflections.scanners.Scanners
        public void scan(ClassFile classFile, List<Map.Entry<String, String>> entries) {
            entries.add(entry(classFile.getSuperclass(), classFile.getName()));
            entries.addAll(entries(Arrays.asList(classFile.getInterfaces()), classFile.getName()));
        }
    },
    TypesAnnotated { // from class: org.reflections.scanners.Scanners.2
        @Override // org.reflections.scanners.Scanners
        public boolean acceptResult(String annotation) {
            return super.acceptResult(annotation) || annotation.equals(Inherited.class.getName());
        }

        @Override // org.reflections.scanners.Scanners
        public void scan(ClassFile classFile, List<Map.Entry<String, String>> entries) {
            classFile.getClass();
            entries.addAll(entries(JavassistHelper.getAnnotations(classFile::getAttribute), classFile.getName()));
        }
    },
    MethodsAnnotated { // from class: org.reflections.scanners.Scanners.3
        @Override // org.reflections.scanners.Scanners
        public void scan(ClassFile classFile, List<Map.Entry<String, String>> entries) {
            JavassistHelper.getMethods(classFile).forEach(method -> {
                method.getClass();
                entries.addAll(entries(JavassistHelper.getAnnotations(method::getAttribute), JavassistHelper.methodName(classFile, method)));
            });
        }
    },
    ConstructorsAnnotated { // from class: org.reflections.scanners.Scanners.4
        @Override // org.reflections.scanners.Scanners
        public void scan(ClassFile classFile, List<Map.Entry<String, String>> entries) {
            JavassistHelper.getConstructors(classFile).forEach(constructor -> {
                constructor.getClass();
                entries.addAll(entries(JavassistHelper.getAnnotations(constructor::getAttribute), JavassistHelper.methodName(classFile, constructor)));
            });
        }
    },
    FieldsAnnotated { // from class: org.reflections.scanners.Scanners.5
        @Override // org.reflections.scanners.Scanners
        public void scan(ClassFile classFile, List<Map.Entry<String, String>> entries) {
            classFile.getFields().forEach(field -> {
                field.getClass();
                entries.addAll(entries(JavassistHelper.getAnnotations(field::getAttribute), JavassistHelper.fieldName(classFile, field)));
            });
        }
    },
    Resources { // from class: org.reflections.scanners.Scanners.6
        @Override // org.reflections.scanners.Scanner
        public boolean acceptsInput(String file) {
            return !file.endsWith(".class");
        }

        @Override // org.reflections.scanners.Scanner
        public List<Map.Entry<String, String>> scan(Vfs.File file) {
            return Collections.singletonList(entry(file.getName(), file.getRelativePath()));
        }

        @Override // org.reflections.scanners.Scanners
        public void scan(ClassFile classFile, List<Map.Entry<String, String>> entries) {
            throw new IllegalStateException();
        }

        @Override // org.reflections.util.QueryBuilder
        public QueryFunction<Store, String> with(String pattern) {
            return store -> {
                return (LinkedHashSet) store.getOrDefault(index(), Collections.emptyMap()).entrySet().stream().filter(entry -> {
                    return ((String) entry.getKey()).matches(pattern);
                }).flatMap(entry2 -> {
                    return ((Set) entry2.getValue()).stream();
                }).collect(Collectors.toCollection(LinkedHashSet::new));
            };
        }
    },
    MethodsParameter { // from class: org.reflections.scanners.Scanners.7
        @Override // org.reflections.scanners.Scanners
        public void scan(ClassFile classFile, List<Map.Entry<String, String>> entries) {
            JavassistHelper.getMethods(classFile).forEach(method -> {
                String value = JavassistHelper.methodName(classFile, method);
                entries.addAll(entries(JavassistHelper.getParameters(method), value));
                JavassistHelper.getParametersAnnotations(method).forEach(annotations -> {
                    entries.addAll(entries(annotations, value));
                });
            });
        }
    },
    ConstructorsParameter { // from class: org.reflections.scanners.Scanners.8
        @Override // org.reflections.scanners.Scanners
        public void scan(ClassFile classFile, List<Map.Entry<String, String>> entries) {
            JavassistHelper.getConstructors(classFile).forEach(constructor -> {
                String value = JavassistHelper.methodName(classFile, constructor);
                entries.addAll(entries(JavassistHelper.getParameters(constructor), value));
                JavassistHelper.getParametersAnnotations(constructor).forEach(annotations -> {
                    entries.addAll(entries(annotations, value));
                });
            });
        }
    },
    MethodsSignature { // from class: org.reflections.scanners.Scanners.9
        @Override // org.reflections.scanners.Scanners
        public void scan(ClassFile classFile, List<Map.Entry<String, String>> entries) {
            JavassistHelper.getMethods(classFile).forEach(method -> {
                entries.add(entry(JavassistHelper.getParameters(method).toString(), JavassistHelper.methodName(classFile, method)));
            });
        }

        @Override // org.reflections.util.QueryBuilder
        public QueryFunction<Store, String> with(AnnotatedElement... keys) {
            return QueryFunction.single(toNames(keys).toString()).getAll(this::get);
        }
    },
    ConstructorsSignature { // from class: org.reflections.scanners.Scanners.10
        @Override // org.reflections.scanners.Scanners
        public void scan(ClassFile classFile, List<Map.Entry<String, String>> entries) {
            JavassistHelper.getConstructors(classFile).forEach(constructor -> {
                entries.add(entry(JavassistHelper.getParameters(constructor).toString(), JavassistHelper.methodName(classFile, constructor)));
            });
        }

        @Override // org.reflections.util.QueryBuilder
        public QueryFunction<Store, String> with(AnnotatedElement... keys) {
            return QueryFunction.single(toNames(keys).toString()).getAll(this::get);
        }
    },
    MethodsReturn { // from class: org.reflections.scanners.Scanners.11
        @Override // org.reflections.scanners.Scanners
        public void scan(ClassFile classFile, List<Map.Entry<String, String>> entries) {
            JavassistHelper.getMethods(classFile).forEach(method -> {
                entries.add(entry(JavassistHelper.getReturnType(method), JavassistHelper.methodName(classFile, method)));
            });
        }
    };

    private Predicate<String> resultFilter;

    abstract void scan(ClassFile classFile, List<Map.Entry<String, String>> list);

    Scanners() {
        this.resultFilter = s -> {
            return true;
        };
    }

    @Override // org.reflections.scanners.Scanner
    public String index() {
        return name();
    }

    public Scanners filterResultsBy(Predicate<String> filter) {
        this.resultFilter = filter;
        return this;
    }

    @Override // org.reflections.scanners.Scanner
    public final List<Map.Entry<String, String>> scan(ClassFile classFile) {
        List<Map.Entry<String, String>> entries = new ArrayList<>();
        scan(classFile, entries);
        return (List) entries.stream().filter(a -> {
            return acceptResult((String) a.getKey());
        }).collect(Collectors.toList());
    }

    protected boolean acceptResult(String fqn) {
        return fqn != null && this.resultFilter.test(fqn);
    }
}
