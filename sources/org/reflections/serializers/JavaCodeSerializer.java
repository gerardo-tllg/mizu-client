package org.reflections.serializers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.reflections.Reflections;
import org.reflections.scanners.TypeElementsScanner;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:org/reflections/serializers/JavaCodeSerializer.class */
public class JavaCodeSerializer implements Serializer {
    private static final String pathSeparator = "_";
    private static final String doubleSeparator = "__";
    private static final String dotSeparator = ".";
    private static final String arrayDescriptor = "$$";
    private static final String tokenSeparator = "_";
    private StringBuilder sb;
    private List<String> prevPaths;
    private int indent;

    @Override // org.reflections.serializers.Serializer
    public Reflections read(InputStream inputStream) {
        throw new UnsupportedOperationException("read is not implemented on JavaCodeSerializer");
    }

    @Override // org.reflections.serializers.Serializer
    public File save(Reflections reflections, String name) {
        String packageName;
        String className;
        if (name.endsWith("/")) {
            name = name.substring(0, name.length() - 1);
        }
        String filename = name.replace('.', '/').concat(".java");
        File file = Serializer.prepareFile(filename);
        int lastDot = name.lastIndexOf(46);
        if (lastDot == -1) {
            packageName = "";
            className = name.substring(name.lastIndexOf(47) + 1);
        } else {
            packageName = name.substring(name.lastIndexOf(47) + 1, lastDot);
            className = name.substring(lastDot + 1);
        }
        try {
            this.sb = new StringBuilder();
            this.sb.append("//generated using Reflections JavaCodeSerializer").append(" [").append(new Date()).append("]").append("\n");
            if (packageName.length() != 0) {
                this.sb.append("package ").append(packageName).append(";\n");
                this.sb.append("\n");
            }
            this.sb.append("public interface ").append(className).append(" {\n\n");
            toString(reflections);
            this.sb.append("}\n");
            Files.write(new File(filename).toPath(), this.sb.toString().getBytes(Charset.defaultCharset()), new OpenOption[0]);
            return file;
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    private void toString(Reflections reflections) {
        Map<String, Set<String>> map = reflections.getStore().get(TypeElementsScanner.class.getSimpleName());
        this.prevPaths = new ArrayList();
        this.indent = 1;
        map.keySet().stream().sorted().forEach(fqn -> {
            List<String> typePaths = Arrays.asList(fqn.split("\\."));
            String className = fqn.substring(fqn.lastIndexOf(46) + 1);
            List<String> fields = new ArrayList<>();
            List<String> methods = new ArrayList<>();
            List<String> annotations = new ArrayList<>();
            ((Set) map.get(fqn)).stream().sorted().forEach(element -> {
                if (element.startsWith("@")) {
                    annotations.add(element.substring(1));
                    return;
                }
                if (element.contains("(")) {
                    if (!element.startsWith("<")) {
                        int i = element.indexOf(40);
                        String name = element.substring(0, i);
                        String params = element.substring(i + 1, element.indexOf(")"));
                        String paramsDescriptor = params.length() != 0 ? "_" + params.replace(dotSeparator, "_").replace(", ", doubleSeparator).replace("[]", arrayDescriptor) : "";
                        methods.add(!methods.contains(name) ? name : name + paramsDescriptor);
                        return;
                    }
                    return;
                }
                if (!element.isEmpty()) {
                    fields.add(element);
                }
            });
            int i = indentOpen(typePaths, this.prevPaths);
            addPackages(typePaths, i);
            addClass(typePaths, className);
            addFields(typePaths, fields);
            addMethods(typePaths, fields, methods);
            addAnnotations(typePaths, annotations);
            this.prevPaths = typePaths;
        });
        indentClose(this.prevPaths);
    }

    protected int indentOpen(List<String> typePaths, List<String> prevPaths) {
        int i = 0;
        while (i < Math.min(typePaths.size(), prevPaths.size()) && typePaths.get(i).equals(prevPaths.get(i))) {
            i++;
        }
        for (int j = prevPaths.size(); j > i; j--) {
            StringBuilder sb = this.sb;
            int i2 = this.indent - 1;
            this.indent = i2;
            sb.append(indent(i2)).append("}\n");
        }
        return i;
    }

    protected void indentClose(List<String> prevPaths) {
        for (int j = prevPaths.size(); j >= 1; j--) {
            this.sb.append(indent(j)).append("}\n");
        }
    }

    protected void addPackages(List<String> typePaths, int i) {
        for (int j = i; j < typePaths.size() - 1; j++) {
            StringBuilder sb = this.sb;
            int i2 = this.indent;
            this.indent = i2 + 1;
            sb.append(indent(i2)).append("interface ").append(uniqueName(typePaths.get(j), typePaths, j)).append(" {\n");
        }
    }

    protected void addClass(List<String> typePaths, String className) {
        StringBuilder sb = this.sb;
        int i = this.indent;
        this.indent = i + 1;
        sb.append(indent(i)).append("interface ").append(uniqueName(className, typePaths, typePaths.size() - 1)).append(" {\n");
    }

    protected void addFields(List<String> typePaths, List<String> fields) {
        if (!fields.isEmpty()) {
            StringBuilder sb = this.sb;
            int i = this.indent;
            this.indent = i + 1;
            sb.append(indent(i)).append("interface fields {\n");
            for (String field : fields) {
                this.sb.append(indent(this.indent)).append("interface ").append(uniqueName(field, typePaths)).append(" {}\n");
            }
            StringBuilder sb2 = this.sb;
            int i2 = this.indent - 1;
            this.indent = i2;
            sb2.append(indent(i2)).append("}\n");
        }
    }

    protected void addMethods(List<String> typePaths, List<String> fields, List<String> methods) {
        if (!methods.isEmpty()) {
            StringBuilder sb = this.sb;
            int i = this.indent;
            this.indent = i + 1;
            sb.append(indent(i)).append("interface methods {\n");
            for (String method : methods) {
                String methodName = uniqueName(method, fields);
                this.sb.append(indent(this.indent)).append("interface ").append(uniqueName(methodName, typePaths)).append(" {}\n");
            }
            StringBuilder sb2 = this.sb;
            int i2 = this.indent - 1;
            this.indent = i2;
            sb2.append(indent(i2)).append("}\n");
        }
    }

    protected void addAnnotations(List<String> typePaths, List<String> annotations) {
        if (!annotations.isEmpty()) {
            StringBuilder sb = this.sb;
            int i = this.indent;
            this.indent = i + 1;
            sb.append(indent(i)).append("interface annotations {\n");
            for (String annotation : annotations) {
                this.sb.append(indent(this.indent)).append("interface ").append(uniqueName(annotation, typePaths)).append(" {}\n");
            }
            StringBuilder sb2 = this.sb;
            int i2 = this.indent - 1;
            this.indent = i2;
            sb2.append(indent(i2)).append("}\n");
        }
    }

    private String uniqueName(String candidate, List<String> prev, int offset) {
        String normalized = normalize(candidate);
        for (int i = 0; i < offset; i++) {
            if (normalized.equals(prev.get(i))) {
                return uniqueName(normalized + "_", prev, offset);
            }
        }
        return normalized;
    }

    private String normalize(String candidate) {
        return candidate.replace(dotSeparator, "_");
    }

    private String uniqueName(String candidate, List<String> prev) {
        return uniqueName(candidate, prev, prev.size());
    }

    private String indent(int times) {
        return (String) IntStream.range(0, times).mapToObj(i -> {
            return "  ";
        }).collect(Collectors.joining());
    }
}
