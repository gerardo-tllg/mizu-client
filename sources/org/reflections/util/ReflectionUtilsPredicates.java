package org.reflections.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.reflections.ReflectionUtils;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:org/reflections/util/ReflectionUtilsPredicates.class */
public class ReflectionUtilsPredicates {
    public static <T extends Member> Predicate<T> withName(String name) {
        return input -> {
            return input != null && input.getName().equals(name);
        };
    }

    public static <T extends Member> Predicate<T> withPrefix(String prefix) {
        return input -> {
            return input != null && input.getName().startsWith(prefix);
        };
    }

    public static <T> Predicate<T> withNamePrefix(String prefix) {
        return input -> {
            return toName(input).startsWith(prefix);
        };
    }

    public static <T extends AnnotatedElement> Predicate<T> withPattern(String regex) {
        return input -> {
            return Pattern.matches(regex, input.toString());
        };
    }

    public static <T extends AnnotatedElement> Predicate<T> withAnnotation(Class<? extends Annotation> annotation) {
        return input -> {
            return input != null && input.isAnnotationPresent(annotation);
        };
    }

    public static <T extends AnnotatedElement> Predicate<T> withAnnotations(Class<? extends Annotation>... annotations) {
        return input -> {
            return input != null && Arrays.equals(annotations, annotationTypes(input.getAnnotations()));
        };
    }

    public static <T extends AnnotatedElement> Predicate<T> withAnnotation(Annotation annotation) {
        return input -> {
            return input != null && input.isAnnotationPresent(annotation.annotationType()) && areAnnotationMembersMatching(input.getAnnotation(annotation.annotationType()), annotation);
        };
    }

    public static <T extends AnnotatedElement> Predicate<T> withAnnotations(Annotation... annotations) {
        return input -> {
            if (input != null) {
                Annotation[] inputAnnotations = input.getAnnotations();
                if (inputAnnotations.length == annotations.length) {
                    return IntStream.range(0, inputAnnotations.length).allMatch(i -> {
                        return areAnnotationMembersMatching(inputAnnotations[i], annotations[i]);
                    });
                }
                return true;
            }
            return true;
        };
    }

    public static Predicate<Member> withParameters(Class<?>... types) {
        return input -> {
            return Arrays.equals(parameterTypes(input), types);
        };
    }

    public static Predicate<Member> withParametersAssignableTo(Class... types) {
        return input -> {
            return isAssignable(types, parameterTypes(input));
        };
    }

    public static Predicate<Member> withParametersAssignableFrom(Class... types) {
        return input -> {
            return isAssignable(parameterTypes(input), types);
        };
    }

    public static Predicate<Member> withParametersCount(int count) {
        return input -> {
            return input != null && parameterTypes(input).length == count;
        };
    }

    public static Predicate<Member> withAnyParameterAnnotation(Class<? extends Annotation> annotationClass) {
        return input -> {
            return input != null && annotationTypes(parameterAnnotations(input)).stream().anyMatch(input1 -> {
                return input1.equals(annotationClass);
            });
        };
    }

    public static Predicate<Member> withAnyParameterAnnotation(Annotation annotation) {
        return input -> {
            return input != null && parameterAnnotations(input).stream().anyMatch(input1 -> {
                return areAnnotationMembersMatching(annotation, input1);
            });
        };
    }

    public static <T> Predicate<Field> withType(Class<T> type) {
        return input -> {
            return input != null && input.getType().equals(type);
        };
    }

    public static <T> Predicate<Field> withTypeAssignableTo(Class<T> type) {
        return input -> {
            return input != null && type.isAssignableFrom(input.getType());
        };
    }

    public static <T> Predicate<Method> withReturnType(Class<T> type) {
        return input -> {
            return input != null && input.getReturnType().equals(type);
        };
    }

    public static <T> Predicate<Method> withReturnTypeAssignableFrom(Class<T> type) {
        return input -> {
            return input != null && type.isAssignableFrom(input.getReturnType());
        };
    }

    public static <T extends Member> Predicate<T> withModifier(int mod) {
        return input -> {
            return (input == null || (input.getModifiers() & mod) == 0) ? false : true;
        };
    }

    public static <T extends Member> Predicate<T> withPublic() {
        return withModifier(1);
    }

    public static <T extends Member> Predicate<T> withStatic() {
        return withModifier(8);
    }

    public static <T extends Member> Predicate<T> withInterface() {
        return withModifier(512);
    }

    public static Predicate<Class<?>> withClassModifier(int mod) {
        return input -> {
            return (input == null || (input.getModifiers() & mod) == 0) ? false : true;
        };
    }

    public static boolean isAssignable(Class[] childClasses, Class[] parentClasses) {
        if (childClasses == null || childClasses.length == 0) {
            return parentClasses == null || parentClasses.length == 0;
        }
        if (childClasses.length != parentClasses.length) {
            return false;
        }
        return IntStream.range(0, childClasses.length).noneMatch(i -> {
            return !parentClasses[i].isAssignableFrom(childClasses[i]) || (parentClasses[i] == Object.class && childClasses[i] != Object.class);
        });
    }

    private static String toName(Object input) {
        if (input == null) {
            return "";
        }
        if (input.getClass().equals(Class.class)) {
            return ((Class) input).getName();
        }
        if (input instanceof Member) {
            return ((Member) input).getName();
        }
        if (input instanceof Annotation) {
            return ((Annotation) input).annotationType().getName();
        }
        return input.toString();
    }

    private static Class[] parameterTypes(Member member) {
        if (member == null) {
            return null;
        }
        if (member.getClass() == Method.class) {
            return ((Method) member).getParameterTypes();
        }
        if (member.getClass() == Constructor.class) {
            return ((Constructor) member).getParameterTypes();
        }
        return null;
    }

    private static Set<Annotation> parameterAnnotations(Member member) {
        Annotation[][] parameterAnnotations;
        if (member instanceof Method) {
            parameterAnnotations = ((Method) member).getParameterAnnotations();
        } else {
            parameterAnnotations = member instanceof Constructor ? ((Constructor) member).getParameterAnnotations() : (Annotation[][]) null;
        }
        Annotation[][] annotations = parameterAnnotations;
        return (Set) Arrays.stream(annotations != null ? annotations : new Annotation[0]).flatMap((v0) -> {
            return Arrays.stream(v0);
        }).collect(Collectors.toSet());
    }

    private static Set<Class<? extends Annotation>> annotationTypes(Collection<Annotation> annotations) {
        return (Set) annotations.stream().map((v0) -> {
            return v0.annotationType();
        }).collect(Collectors.toSet());
    }

    private static Class<? extends Annotation>[] annotationTypes(Annotation[] annotations) {
        return (Class[]) Arrays.stream(annotations).map((v0) -> {
            return v0.annotationType();
        }).toArray(x$0 -> {
            return new Class[x$0];
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static boolean areAnnotationMembersMatching(Annotation annotation1, Annotation annotation2) {
        if (annotation2 != null && annotation1.annotationType() == annotation2.annotationType()) {
            for (Method method : annotation1.annotationType().getDeclaredMethods()) {
                if (!ReflectionUtils.invoke(method, annotation1, new Object[0]).equals(ReflectionUtils.invoke(method, annotation2, new Object[0]))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
