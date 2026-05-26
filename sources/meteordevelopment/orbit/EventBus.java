package meteordevelopment.orbit;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import meteordevelopment.orbit.listeners.IListener;
import meteordevelopment.orbit.listeners.LambdaListener;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/orbit/EventBus.class */
public class EventBus implements IEventBus {
    private final Map<Object, List<IListener>> listenerCache = new ConcurrentHashMap();
    private final Map<Class<?>, List<IListener>> staticListenerCache = new ConcurrentHashMap();
    private final Map<Class<?>, List<IListener>> listenerMap = new ConcurrentHashMap();
    private final List<LambdaFactoryInfo> lambdaFactoryInfos = new ArrayList();

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/orbit/EventBus$LambdaFactoryInfo.class */
    private static class LambdaFactoryInfo {
        public final String packagePrefix;
        public final LambdaListener.Factory factory;

        public LambdaFactoryInfo(String packagePrefix, LambdaListener.Factory factory) {
            this.packagePrefix = packagePrefix;
            this.factory = factory;
        }
    }

    @Override // meteordevelopment.orbit.IEventBus
    public void registerLambdaFactory(String packagePrefix, LambdaListener.Factory factory) {
        synchronized (this.lambdaFactoryInfos) {
            this.lambdaFactoryInfos.add(new LambdaFactoryInfo(packagePrefix, factory));
        }
    }

    @Override // meteordevelopment.orbit.IEventBus
    public boolean isListening(Class<?> eventKlass) {
        List<IListener> listeners = this.listenerMap.get(eventKlass);
        return (listeners == null || listeners.isEmpty()) ? false : true;
    }

    @Override // meteordevelopment.orbit.IEventBus
    public <T> T post(T event) {
        List<IListener> listeners = this.listenerMap.get(event.getClass());
        if (listeners != null) {
            for (IListener listener : listeners) {
                listener.call(event);
            }
        }
        return event;
    }

    @Override // meteordevelopment.orbit.IEventBus
    public <T extends ICancellable> T post(T event) {
        List<IListener> listeners = this.listenerMap.get(event.getClass());
        if (listeners != null) {
            event.setCancelled(false);
            for (IListener listener : listeners) {
                listener.call(event);
                if (event.isCancelled()) {
                    break;
                }
            }
        }
        return event;
    }

    @Override // meteordevelopment.orbit.IEventBus
    public void subscribe(Object object) {
        subscribe(getListeners(object.getClass(), object), false);
    }

    @Override // meteordevelopment.orbit.IEventBus
    public void subscribe(Class<?> klass) {
        subscribe(getListeners(klass, null), true);
    }

    @Override // meteordevelopment.orbit.IEventBus
    public void subscribe(IListener listener) {
        subscribe(listener, false);
    }

    private void subscribe(List<IListener> listeners, boolean onlyStatic) {
        for (IListener listener : listeners) {
            subscribe(listener, onlyStatic);
        }
    }

    private void subscribe(IListener listener, boolean onlyStatic) {
        if (onlyStatic) {
            if (listener.isStatic()) {
                insert(this.listenerMap.computeIfAbsent(listener.getTarget(), aClass -> {
                    return new CopyOnWriteArrayList();
                }), listener);
                return;
            }
            return;
        }
        insert(this.listenerMap.computeIfAbsent(listener.getTarget(), aClass2 -> {
            return new CopyOnWriteArrayList();
        }), listener);
    }

    private void insert(List<IListener> listeners, IListener listener) {
        int i = 0;
        while (i < listeners.size() && listener.getPriority() <= listeners.get(i).getPriority()) {
            i++;
        }
        listeners.add(i, listener);
    }

    @Override // meteordevelopment.orbit.IEventBus
    public void unsubscribe(Object object) {
        unsubscribe(getListeners(object.getClass(), object), false);
    }

    @Override // meteordevelopment.orbit.IEventBus
    public void unsubscribe(Class<?> klass) {
        unsubscribe(getListeners(klass, null), true);
    }

    @Override // meteordevelopment.orbit.IEventBus
    public void unsubscribe(IListener listener) {
        unsubscribe(listener, false);
    }

    private void unsubscribe(List<IListener> listeners, boolean staticOnly) {
        for (IListener listener : listeners) {
            unsubscribe(listener, staticOnly);
        }
    }

    private void unsubscribe(IListener listener, boolean staticOnly) {
        List<IListener> l = this.listenerMap.get(listener.getTarget());
        if (l != null) {
            if (staticOnly) {
                if (listener.isStatic()) {
                    l.remove(listener);
                    return;
                }
                return;
            }
            l.remove(listener);
        }
    }

    private List<IListener> getListeners(Class<?> klass, Object object) {
        Function<? super Class<?>, ? extends List<IListener>> function = o -> {
            List<IListener> listeners = new CopyOnWriteArrayList<>();
            getListeners(listeners, klass, object);
            return listeners;
        };
        if (object == null) {
            return this.staticListenerCache.computeIfAbsent(klass, function);
        }
        for (Object key : this.listenerCache.keySet()) {
            if (key == object) {
                return this.listenerCache.get(object);
            }
        }
        List<IListener> listeners = (List) function.apply(object);
        this.listenerCache.put(object, listeners);
        return listeners;
    }

    private void getListeners(List<IListener> listeners, Class<?> klass, Object object) {
        for (Method method : klass.getDeclaredMethods()) {
            if (isValid(method)) {
                listeners.add(new LambdaListener(getLambdaFactory(klass), klass, object, method));
            }
        }
        if (klass.getSuperclass() != null) {
            getListeners(listeners, klass.getSuperclass(), object);
        }
    }

    private boolean isValid(Method method) {
        return method.isAnnotationPresent(EventHandler.class) && method.getReturnType() == Void.TYPE && method.getParameterCount() == 1 && !method.getParameters()[0].getType().isPrimitive();
    }

    private LambdaListener.Factory getLambdaFactory(Class<?> klass) {
        synchronized (this.lambdaFactoryInfos) {
            for (LambdaFactoryInfo info : this.lambdaFactoryInfos) {
                if (klass.getName().startsWith(info.packagePrefix)) {
                    return info.factory;
                }
            }
            throw new NoLambdaFactoryException(klass);
        }
    }
}
