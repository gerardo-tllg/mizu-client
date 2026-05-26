package meteordevelopment.orbit.listeners;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/orbit/listeners/IListener.class */
public interface IListener {
    void call(Object obj);

    Class<?> getTarget();

    int getPriority();

    @Deprecated
    boolean isStatic();
}
