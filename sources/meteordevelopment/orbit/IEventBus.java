package meteordevelopment.orbit;

import meteordevelopment.orbit.listeners.IListener;
import meteordevelopment.orbit.listeners.LambdaListener;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/orbit/IEventBus.class */
public interface IEventBus {
    void registerLambdaFactory(String str, LambdaListener.Factory factory);

    boolean isListening(Class<?> cls);

    <T> T post(T t);

    <T extends ICancellable> T post(T t);

    void subscribe(Object obj);

    void subscribe(Class<?> cls);

    void subscribe(IListener iListener);

    void unsubscribe(Object obj);

    void unsubscribe(Class<?> cls);

    void unsubscribe(IListener iListener);
}
