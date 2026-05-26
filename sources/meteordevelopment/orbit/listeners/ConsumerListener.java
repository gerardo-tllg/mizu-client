package meteordevelopment.orbit.listeners;

import java.util.function.Consumer;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/orbit/listeners/ConsumerListener.class */
public class ConsumerListener<T> implements IListener {
    private final Class<T> target;
    private final int priority;
    private final Consumer<T> executor;

    public ConsumerListener(Class<T> target, int priority, Consumer<T> executor) {
        this.target = target;
        this.priority = priority;
        this.executor = executor;
    }

    public ConsumerListener(Class<T> target, Consumer<T> executor) {
        this(target, 0, executor);
    }

    @Override // meteordevelopment.orbit.listeners.IListener
    public void call(Object event) {
        this.executor.accept(event);
    }

    @Override // meteordevelopment.orbit.listeners.IListener
    public Class<T> getTarget() {
        return this.target;
    }

    @Override // meteordevelopment.orbit.listeners.IListener
    public int getPriority() {
        return this.priority;
    }

    @Override // meteordevelopment.orbit.listeners.IListener
    public boolean isStatic() {
        return false;
    }
}
