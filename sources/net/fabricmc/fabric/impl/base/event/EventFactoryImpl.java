package net.fabricmc.fabric.impl.base.event;

import com.google.common.collect.MapMaker;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.class_2960;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:META-INF/jars/fabric-api-base-0.4.62+73a52b4b49.jar:net/fabricmc/fabric/impl/base/event/EventFactoryImpl.class */
public final class EventFactoryImpl {
    private static final Set<ArrayBackedEvent<?>> ARRAY_BACKED_EVENTS = Collections.newSetFromMap(new MapMaker().weakKeys().makeMap());

    private EventFactoryImpl() {
    }

    public static void invalidate() {
        ARRAY_BACKED_EVENTS.forEach((v0) -> {
            v0.update();
        });
    }

    public static <T> Event<T> createArrayBacked(Class<? super T> type, Function<T[], T> invokerFactory) {
        ArrayBackedEvent<?> arrayBackedEvent = new ArrayBackedEvent<>(type, invokerFactory);
        ARRAY_BACKED_EVENTS.add(arrayBackedEvent);
        return arrayBackedEvent;
    }

    public static void ensureContainsDefault(class_2960[] defaultPhases) {
        for (class_2960 id : defaultPhases) {
            if (id.equals(Event.DEFAULT_PHASE)) {
                return;
            }
        }
        throw new IllegalArgumentException("The event phases must contain Event.DEFAULT_PHASE.");
    }

    public static void ensureNoDuplicates(class_2960[] defaultPhases) {
        for (int i = 0; i < defaultPhases.length; i++) {
            for (int j = i + 1; j < defaultPhases.length; j++) {
                if (defaultPhases[i].equals(defaultPhases[j])) {
                    throw new IllegalArgumentException("Duplicate event phase: " + String.valueOf(defaultPhases[i]));
                }
            }
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    private static <T> T buildEmptyInvoker(Class<T> cls, Function<T[], T> function) {
        Method method = null;
        for (Method method2 : cls.getMethods()) {
            if ((method2.getModifiers() & 2050) == 0) {
                if (method != null) {
                    throw new IllegalStateException("Multiple virtual methods in " + String.valueOf(cls) + "; cannot build empty invoker!");
                }
                method = method2;
            }
        }
        if (method == null) {
            throw new IllegalStateException("No virtual methods in " + String.valueOf(cls) + "; cannot build empty invoker!");
        }
        Object objInvokeWithArguments = null;
        try {
            MethodHandle methodHandleUnreflect = MethodHandles.lookup().unreflect(method);
            MethodType methodTypeDropParameterTypes = methodHandleUnreflect.type().dropParameterTypes(0, 1);
            if (methodTypeDropParameterTypes.returnType() != Void.TYPE) {
                MethodHandle methodHandleExplicitCastArguments = MethodHandles.explicitCastArguments(methodHandleUnreflect, MethodType.genericMethodType(methodTypeDropParameterTypes.parameterCount()).changeReturnType(methodTypeDropParameterTypes.returnType()).insertParameterTypes(0, methodHandleUnreflect.type().parameterType(0)));
                Object[] objArr = new Object[methodHandleUnreflect.type().parameterCount()];
                objArr[0] = function.apply((Object[]) Array.newInstance((Class<?>) cls, 0));
                objInvokeWithArguments = methodHandleExplicitCastArguments.invokeWithArguments(objArr);
            }
            Object obj = objInvokeWithArguments;
            return (T) Proxy.newProxyInstance(EventFactoryImpl.class.getClassLoader(), new Class[]{cls}, (proxy, method3, args) -> {
                return obj;
            });
        } catch (Throwable th) {
            throw new RuntimeException(th);
        }
    }
}
