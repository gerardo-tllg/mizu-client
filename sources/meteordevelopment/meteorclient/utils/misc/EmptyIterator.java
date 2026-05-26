package meteordevelopment.meteorclient.utils.misc;

import java.util.Iterator;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/misc/EmptyIterator.class */
public class EmptyIterator<T> implements Iterator<T> {
    @Override // java.util.Iterator
    public boolean hasNext() {
        return false;
    }

    @Override // java.util.Iterator
    public T next() {
        return null;
    }
}
