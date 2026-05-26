package net.fabricmc.fabric.api.event;

import java.util.function.Function;
import net.fabricmc.fabric.impl.base.event.EventFactoryImpl;
import net.minecraft.class_2960;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:META-INF/jars/fabric-api-base-0.4.62+73a52b4b49.jar:net/fabricmc/fabric/api/event/EventFactory.class */
public final class EventFactory {
    private EventFactory() {
    }

    public static <T> Event<T> createArrayBacked(Class<? super T> type, Function<T[], T> invokerFactory) {
        return EventFactoryImpl.createArrayBacked(type, invokerFactory);
    }

    public static <T> Event<T> createArrayBacked(Class<T> type, T emptyInvoker, Function<T[], T> invokerFactory) {
        return createArrayBacked(type, listeners -> {
            if (listeners.length == 0) {
                return emptyInvoker;
            }
            if (listeners.length == 1) {
                return listeners[0];
            }
            return invokerFactory.apply(listeners);
        });
    }

    public static <T> Event<T> createWithPhases(Class<? super T> type, Function<T[], T> invokerFactory, class_2960... defaultPhases) {
        EventFactoryImpl.ensureContainsDefault(defaultPhases);
        EventFactoryImpl.ensureNoDuplicates(defaultPhases);
        Event<T> event = createArrayBacked(type, invokerFactory);
        for (int i = 1; i < defaultPhases.length; i++) {
            event.addPhaseOrdering(defaultPhases[i - 1], defaultPhases[i]);
        }
        return event;
    }

    @Deprecated
    public static String getHandlerName(Object handler) {
        return handler.getClass().getName();
    }

    @Deprecated
    public static boolean isProfilingEnabled() {
        return false;
    }

    @Deprecated(forRemoval = true)
    public static void invalidate() {
        EventFactoryImpl.invalidate();
    }
}
