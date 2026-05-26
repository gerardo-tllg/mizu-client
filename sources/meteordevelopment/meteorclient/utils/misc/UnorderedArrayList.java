package meteordevelopment.meteorclient.utils.misc;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/misc/UnorderedArrayList.class */
public class UnorderedArrayList<T> extends AbstractList<T> {
    private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = new Object[0];
    private static final int DEFAULT_CAPACITY = 10;
    private static final int MAX_ARRAY_SIZE = 2147483639;
    private transient T[] items = (T[]) DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
    private int size;

    @Override // java.util.AbstractList, java.util.AbstractCollection, java.util.Collection, java.util.List
    public boolean add(T t) {
        if (this.size == this.items.length) {
            grow(this.size + 1);
        }
        T[] tArr = this.items;
        int i = this.size;
        this.size = i + 1;
        tArr[i] = t;
        this.modCount++;
        return true;
    }

    @Override // java.util.AbstractList, java.util.List
    public T set(int index, T element) {
        T old = this.items[index];
        this.items[index] = element;
        return old;
    }

    @Override // java.util.AbstractList, java.util.List
    public T get(int index) {
        return this.items[index];
    }

    @Override // java.util.AbstractList, java.util.AbstractCollection, java.util.Collection, java.util.List
    public void clear() {
        this.modCount++;
        for (int i = 0; i < this.size; i++) {
            this.items[i] = null;
        }
        this.size = 0;
    }

    @Override // java.util.AbstractList, java.util.List
    public int indexOf(Object o) {
        for (int i = 0; i < this.size; i++) {
            if (Objects.equals(this.items[i], o)) {
                return i;
            }
        }
        return -1;
    }

    @Override // java.util.AbstractList, java.util.List
    public int lastIndexOf(Object o) {
        T[] elements = this.items;
        for (int i = this.size - 1; i >= 0; i--) {
            if (Objects.equals(elements[i], o)) {
                return i;
            }
        }
        return -1;
    }

    @Override // java.util.AbstractCollection, java.util.Collection, java.util.List
    public boolean remove(Object o) {
        int i = indexOf(o);
        if (i == -1) {
            return false;
        }
        this.items[i] = null;
        T[] tArr = this.items;
        T[] tArr2 = this.items;
        int i2 = this.size - 1;
        this.size = i2;
        tArr[i] = tArr2[i2];
        this.modCount++;
        return true;
    }

    @Override // java.util.AbstractList, java.util.List
    public T remove(int index) {
        T old = this.items[index];
        this.items[index] = null;
        T[] tArr = this.items;
        T[] tArr2 = this.items;
        int i = this.size - 1;
        this.size = i;
        tArr[index] = tArr2[i];
        this.modCount++;
        return old;
    }

    @Override // java.util.Collection
    public boolean removeIf(Predicate<? super T> predicate) {
        int i = this.size;
        int i2 = 0;
        for (int i3 = 0; i3 < this.size; i3++) {
            Object obj = (Object) ((T[]) this.items)[i3];
            if (!predicate.test(obj)) {
                if (i2 < i3) {
                    ((T[]) this.items)[i2] = obj;
                }
                i2++;
            }
        }
        this.size = i2;
        return this.size != i;
    }

    @Override // java.util.AbstractCollection, java.util.Collection, java.util.List
    public int size() {
        return this.size;
    }

    public void ensureCapacity(int minCapacity) {
        if (minCapacity > this.items.length) {
            if (this.items != DEFAULTCAPACITY_EMPTY_ELEMENTDATA || minCapacity > 10) {
                this.modCount++;
                grow(minCapacity);
            }
        }
    }

    private void grow(int i) {
        this.items = (T[]) Arrays.copyOf(this.items, newCapacity(i));
    }

    private int newCapacity(int minCapacity) {
        int oldCapacity = this.items.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity <= 0) {
            if (this.items == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
                return Math.max(10, minCapacity);
            }
            if (minCapacity < 0) {
                throw new OutOfMemoryError();
            }
            return minCapacity;
        }
        if (newCapacity - MAX_ARRAY_SIZE <= 0) {
            return newCapacity;
        }
        return hugeCapacity(minCapacity);
    }

    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) {
            throw new OutOfMemoryError();
        }
        if (minCapacity > MAX_ARRAY_SIZE) {
            return Integer.MAX_VALUE;
        }
        return MAX_ARRAY_SIZE;
    }
}
