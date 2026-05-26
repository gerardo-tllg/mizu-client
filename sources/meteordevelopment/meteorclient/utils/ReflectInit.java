package meteordevelopment.meteorclient.utils;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import meteordevelopment.meteorclient.addons.AddonManager;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/ReflectInit.class */
public class ReflectInit {
    private static final List<Reflections> reflections = new ArrayList();

    private ReflectInit() {
    }

    public static void registerPackages() {
        for (MeteorAddon addon : AddonManager.ADDONS) {
            try {
                add(addon);
            } catch (AbstractMethodError e) {
                throw new RuntimeException("Addon \"%s\" is too old and cannot be ran.".formatted(addon.name), e);
            }
        }
    }

    private static void add(MeteorAddon addon) {
        String pkg = addon.getPackage();
        if (pkg == null || pkg.isBlank()) {
            return;
        }
        reflections.add(new Reflections(pkg, Scanners.MethodsAnnotated));
    }

    public static void init(Class<? extends Annotation> annotation) {
        for (Reflections reflection : reflections) {
            Set<Method> initTasks = reflection.getMethodsAnnotatedWith(annotation);
            if (initTasks == null) {
                return;
            }
            Map<Class<?>, List<Method>> byClass = (Map) initTasks.stream().collect(Collectors.groupingBy((v0) -> {
                return v0.getDeclaringClass();
            }));
            Set<Method> left = new HashSet<>(initTasks);
            while (true) {
                Method m = left.stream().findAny().orElse(null);
                if (m != null) {
                    reflectInit(m, annotation, left, byClass);
                }
            }
        }
    }

    private static <T extends Annotation> void reflectInit(Method task, Class<T> annotation, Set<Method> left, Map<Class<?>, List<Method>> byClass) {
        left.remove(task);
        for (Class<?> clazz : getDependencies(task, annotation)) {
            for (Method m : byClass.getOrDefault(clazz, Collections.emptyList())) {
                if (left.contains(m)) {
                    reflectInit(m, annotation, left, byClass);
                }
            }
        }
        try {
            task.invoke(null, new Object[0]);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Error running @%s task '%s.%s'".formatted(annotation.getSimpleName(), task.getDeclaringClass().getSimpleName(), task.getName()), e);
        } catch (NullPointerException e2) {
            throw new RuntimeException("Method \"%s\" using Init annotations from non-static context".formatted(task.getName()), e2);
        }
    }

    private static <T extends Annotation> Class<?>[] getDependencies(Method task, Class<T> annotation) {
        Annotation annotation2 = task.getAnnotation(annotation);
        Objects.requireNonNull(annotation2);
        switch ((int) SwitchBootstraps.typeSwitch(MethodHandles.lookup(), "typeSwitch", MethodType.methodType(Integer.TYPE, Object.class, Integer.TYPE), PreInit.class, PostInit.class).dynamicInvoker().invoke(annotation2, 0) /* invoke-custom */) {
            case 0:
                PreInit pre = (PreInit) annotation2;
                return pre.dependencies();
            case 1:
                PostInit post = (PostInit) annotation2;
                return post.dependencies();
            default:
                return new Class[0];
        }
    }
}
