package org.reflections.util;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.reflections.ReflectionsException;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:org/reflections/util/NameHelper.class */
public interface NameHelper {
    public static final List<String> primitiveNames = Arrays.asList("boolean", "char", "byte", "short", "int", "long", "float", "double", "void");
    public static final List<Class<?>> primitiveTypes = Arrays.asList(Boolean.TYPE, Character.TYPE, Byte.TYPE, Short.TYPE, Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE, Void.TYPE);
    public static final List<String> primitiveDescriptors = Arrays.asList("Z", "C", "B", "S", "I", "J", "F", "D", "V");

    default String toName(AnnotatedElement element) {
        if (element.getClass().equals(Class.class)) {
            return toName((Class<?>) element);
        }
        if (element.getClass().equals(Constructor.class)) {
            return toName((Constructor<?>) element);
        }
        if (element.getClass().equals(Method.class)) {
            return toName((Method) element);
        }
        if (element.getClass().equals(Field.class)) {
            return toName((Field) element);
        }
        return null;
    }

    default String toName(Class<?> type) {
        int dim = 0;
        while (type.isArray()) {
            dim++;
            type = type.getComponentType();
        }
        return type.getName() + String.join("", Collections.nCopies(dim, "[]"));
    }

    default String toName(Constructor<?> constructor) {
        return String.format("%s.<init>(%s)", constructor.getName(), String.join(", ", toNames(constructor.getParameterTypes())));
    }

    default String toName(Method method) {
        return String.format("%s.%s(%s)", method.getDeclaringClass().getName(), method.getName(), String.join(", ", toNames(method.getParameterTypes())));
    }

    default String toName(Field field) {
        return String.format("%s.%s", field.getDeclaringClass().getName(), field.getName());
    }

    default Collection<String> toNames(Collection<? extends AnnotatedElement> elements) {
        return (Collection) elements.stream().map(this::toName).filter((v0) -> {
            return Objects.nonNull(v0);
        }).collect(Collectors.toList());
    }

    default Collection<String> toNames(AnnotatedElement... elements) {
        return toNames(Arrays.asList(elements));
    }

    default <T> T forName(String str, Class<T> cls, ClassLoader... classLoaderArr) {
        if (cls.equals(Class.class)) {
            return (T) forClass(str, classLoaderArr);
        }
        if (cls.equals(Constructor.class)) {
            return (T) forConstructor(str, classLoaderArr);
        }
        if (cls.equals(Method.class)) {
            return (T) forMethod(str, classLoaderArr);
        }
        if (cls.equals(Field.class)) {
            return (T) forField(str, classLoaderArr);
        }
        if (cls.equals(Member.class)) {
            return (T) forMember(str, classLoaderArr);
        }
        return null;
    }

    default Class<?> forClass(String typeName, ClassLoader... loaders) {
        String type;
        String type2;
        if (primitiveNames.contains(typeName)) {
            return primitiveTypes.get(primitiveNames.indexOf(typeName));
        }
        if (typeName.contains("[")) {
            int i = typeName.indexOf("[");
            String type3 = typeName.substring(0, i);
            String array = typeName.substring(i).replace("]", "");
            if (primitiveNames.contains(type3)) {
                type2 = primitiveDescriptors.get(primitiveNames.indexOf(type3));
            } else {
                type2 = "L" + type3 + ";";
            }
            type = array + type2;
        } else {
            type = typeName;
        }
        for (ClassLoader classLoader : ClasspathHelper.classLoaders(loaders)) {
            if (type.contains("[")) {
                try {
                    return Class.forName(type, false, classLoader);
                } catch (Throwable th) {
                }
            }
            try {
                return classLoader.loadClass(type);
            } catch (Throwable th2) {
            }
        }
        return null;
    }

    default Member forMember(String descriptor, ClassLoader... loaders) throws ReflectionsException {
        int p0 = descriptor.lastIndexOf(40);
        String memberKey = p0 != -1 ? descriptor.substring(0, p0) : descriptor;
        String methodParameters = p0 != -1 ? descriptor.substring(p0 + 1, descriptor.lastIndexOf(41)) : "";
        int p1 = Math.max(memberKey.lastIndexOf(46), memberKey.lastIndexOf("$"));
        String className = memberKey.substring(0, p1);
        String memberName = memberKey.substring(p1 + 1);
        Class<?>[] parameterTypes = null;
        if (!methodParameters.isEmpty()) {
            String[] parameterNames = methodParameters.split(",");
            parameterTypes = (Class[]) Arrays.stream(parameterNames).map(name -> {
                return forClass(name.trim(), loaders);
            }).toArray(x$0 -> {
                return new Class[x$0];
            });
        }
        try {
            for (Class<?> aClass = forClass(className, loaders); aClass != null; aClass = aClass.getSuperclass()) {
                try {
                    return !descriptor.contains("(") ? aClass.isInterface() ? aClass.getField(memberName) : aClass.getDeclaredField(memberName) : descriptor.contains("init>") ? aClass.isInterface() ? aClass.getConstructor(parameterTypes) : aClass.getDeclaredConstructor(parameterTypes) : aClass.isInterface() ? aClass.getMethod(memberName, parameterTypes) : aClass.getDeclaredMethod(memberName, parameterTypes);
                } catch (Exception e) {
                }
            }
            return null;
        } catch (Exception e2) {
            return null;
        }
    }

    @Nullable
    default <T extends AnnotatedElement> T forElement(String descriptor, Class<T> resultType, ClassLoader[] loaders) {
        Member member = forMember(descriptor, loaders);
        if (member == null || !member.getClass().equals(resultType)) {
            return null;
        }
        return (T) member;
    }

    @Nullable
    default Method forMethod(String descriptor, ClassLoader... loaders) throws ReflectionsException {
        return (Method) forElement(descriptor, Method.class, loaders);
    }

    default Constructor<?> forConstructor(String descriptor, ClassLoader... loaders) throws ReflectionsException {
        return (Constructor) forElement(descriptor, Constructor.class, loaders);
    }

    @Nullable
    default Field forField(String descriptor, ClassLoader... loaders) {
        return (Field) forElement(descriptor, Field.class, loaders);
    }

    default <T> Collection<T> forNames(Collection<String> names, Class<T> resultType, ClassLoader... loaders) {
        return (Collection) names.stream().map(name -> {
            return forName(name, resultType, loaders);
        }).filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    default Collection<Class<?>> forNames(Collection<String> names, ClassLoader... loaders) {
        return forNames(names, Class.class, loaders);
    }
}
