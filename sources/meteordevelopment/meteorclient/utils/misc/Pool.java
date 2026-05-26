package meteordevelopment.meteorclient.utils.misc;

import java.util.ArrayDeque;
import java.util.Queue;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/misc/Pool.class */
public class Pool<T> {
    private final Queue<T> items = new ArrayDeque();
    private final Producer<T> producer;

    public Pool(Producer<T> producer) {
        this.producer = producer;
    }

    public synchronized T get() {
        return !this.items.isEmpty() ? this.items.poll() : this.producer.create();
    }

    public synchronized void free(T obj) {
        this.items.offer(obj);
    }
}
