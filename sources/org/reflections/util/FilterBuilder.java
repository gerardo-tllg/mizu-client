package org.reflections.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javassist.bytecode.Opcode;
import org.reflections.ReflectionsException;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:org/reflections/util/FilterBuilder.class */
public class FilterBuilder implements Predicate<String> {
    private final List<Predicate<String>> chain = new ArrayList();

    public FilterBuilder() {
    }

    private FilterBuilder(Collection<Predicate<String>> filters) {
        this.chain.addAll(filters);
    }

    public FilterBuilder includePackage(String value) {
        return includePattern(prefixPattern(value));
    }

    public FilterBuilder excludePackage(String value) {
        return excludePattern(prefixPattern(value));
    }

    public FilterBuilder includePattern(String regex) {
        return add(new Include(regex));
    }

    public FilterBuilder excludePattern(String regex) {
        return add(new Exclude(regex));
    }

    @Deprecated
    public FilterBuilder include(String regex) {
        return add(new Include(regex));
    }

    @Deprecated
    public FilterBuilder exclude(String regex) {
        add(new Exclude(regex));
        return this;
    }

    public static FilterBuilder parsePackages(String includeExcludeString) {
        List<Predicate<String>> filters = new ArrayList<>();
        for (String string : includeExcludeString.split(",")) {
            String trimmed = string.trim();
            char prefix = trimmed.charAt(0);
            String pattern = prefixPattern(trimmed.substring(1));
            switch (prefix) {
                case Opcode.ALOAD_1 /* 43 */:
                    filters.add(new Include(pattern));
                    break;
                case '-':
                    filters.add(new Exclude(pattern));
                    break;
                default:
                    throw new ReflectionsException("includeExclude should start with either + or -");
            }
        }
        return new FilterBuilder(filters);
    }

    public FilterBuilder add(Predicate<String> filter) {
        this.chain.add(filter);
        return this;
    }

    @Override // java.util.function.Predicate
    public boolean test(String regex) {
        boolean accept = this.chain.isEmpty() || (this.chain.get(0) instanceof Exclude);
        for (Predicate<String> filter : this.chain) {
            if (!accept || !(filter instanceof Include)) {
                if (accept || !(filter instanceof Exclude)) {
                    accept = filter.test(regex);
                    if (!accept && (filter instanceof Exclude)) {
                        break;
                    }
                }
            }
        }
        return accept;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return Objects.equals(this.chain, ((FilterBuilder) o).chain);
    }

    public int hashCode() {
        return Objects.hash(this.chain);
    }

    public String toString() {
        return (String) this.chain.stream().map((v0) -> {
            return v0.toString();
        }).collect(Collectors.joining(", "));
    }

    private static String prefixPattern(String fqn) {
        if (!fqn.endsWith(".")) {
            fqn = fqn + ".";
        }
        return fqn.replace(".", "\\.").replace("$", "\\$") + ".*";
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:org/reflections/util/FilterBuilder$Matcher.class */
    static abstract class Matcher implements Predicate<String> {
        final Pattern pattern;

        Matcher(String regex) {
            this.pattern = Pattern.compile(regex);
        }

        public int hashCode() {
            return Objects.hash(this.pattern);
        }

        public boolean equals(Object o) {
            return this == o || (o != null && getClass() == o.getClass() && Objects.equals(this.pattern.pattern(), ((Matcher) o).pattern.pattern()));
        }

        public String toString() {
            return this.pattern.pattern();
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:org/reflections/util/FilterBuilder$Include.class */
    static class Include extends Matcher {
        Include(String regex) {
            super(regex);
        }

        @Override // java.util.function.Predicate
        public boolean test(String regex) {
            return this.pattern.matcher(regex).matches();
        }

        @Override // org.reflections.util.FilterBuilder.Matcher
        public String toString() {
            return "+" + this.pattern;
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:org/reflections/util/FilterBuilder$Exclude.class */
    static class Exclude extends Matcher {
        Exclude(String regex) {
            super(regex);
        }

        @Override // java.util.function.Predicate
        public boolean test(String regex) {
            return !this.pattern.matcher(regex).matches();
        }

        @Override // org.reflections.util.FilterBuilder.Matcher
        public String toString() {
            return "-" + this.pattern;
        }
    }
}
