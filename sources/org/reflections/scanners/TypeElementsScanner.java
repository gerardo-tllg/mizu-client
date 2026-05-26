package org.reflections.scanners;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import javassist.bytecode.ClassFile;
import org.reflections.util.JavassistHelper;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:org/reflections/scanners/TypeElementsScanner.class */
public class TypeElementsScanner implements Scanner {
    private boolean includeFields = true;
    private boolean includeMethods = true;
    private boolean includeAnnotations = true;
    private boolean publicOnly = true;
    private Predicate<String> resultFilter = s -> {
        return true;
    };

    @Override // org.reflections.scanners.Scanner
    public List<Map.Entry<String, String>> scan(ClassFile classFile) {
        List<Map.Entry<String, String>> entries = new ArrayList<>();
        String className = classFile.getName();
        if (this.resultFilter.test(className) && isPublic(classFile)) {
            entries.add(entry(className, ""));
            if (this.includeFields) {
                classFile.getFields().forEach(field -> {
                    entries.add(entry(className, field.getName()));
                });
            }
            if (this.includeMethods) {
                classFile.getMethods().stream().filter((v1) -> {
                    return isPublic(v1);
                }).forEach(method -> {
                    entries.add(entry(className, method.getName() + "(" + String.join(", ", JavassistHelper.getParameters(method)) + ")"));
                });
            }
            if (this.includeAnnotations) {
                classFile.getClass();
                JavassistHelper.getAnnotations(classFile::getAttribute).stream().filter(this.resultFilter).forEach(annotation -> {
                    entries.add(entry(className, "@" + annotation));
                });
            }
        }
        return entries;
    }

    private boolean isPublic(Object object) {
        return !this.publicOnly || JavassistHelper.isPublic(object);
    }

    public TypeElementsScanner filterResultsBy(Predicate<String> filter) {
        this.resultFilter = filter;
        return this;
    }

    public TypeElementsScanner includeFields() {
        return includeFields(true);
    }

    public TypeElementsScanner includeFields(boolean include) {
        this.includeFields = include;
        return this;
    }

    public TypeElementsScanner includeMethods() {
        return includeMethods(true);
    }

    public TypeElementsScanner includeMethods(boolean include) {
        this.includeMethods = include;
        return this;
    }

    public TypeElementsScanner includeAnnotations() {
        return includeAnnotations(true);
    }

    public TypeElementsScanner includeAnnotations(boolean include) {
        this.includeAnnotations = include;
        return this;
    }

    public TypeElementsScanner publicOnly(boolean only) {
        this.publicOnly = only;
        return this;
    }

    public TypeElementsScanner publicOnly() {
        return publicOnly(true);
    }
}
