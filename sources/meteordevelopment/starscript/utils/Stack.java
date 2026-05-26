package meteordevelopment.starscript.utils;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/starscript/utils/Stack.class */
public class Stack<T> {
    private T[] items = (T[]) new Object[8];
    private int size;

    public void clear() {
        for (int i = 0; i < this.size; i++) {
            this.items[i] = null;
        }
        this.size = 0;
    }

    public void push(T t) {
        if (this.size >= this.items.length) {
            T[] tArr = (T[]) new Object[this.items.length * 2];
            System.arraycopy(this.items, 0, tArr, 0, this.items.length);
            this.items = tArr;
        }
        T[] tArr2 = this.items;
        int i = this.size;
        this.size = i + 1;
        tArr2[i] = t;
    }

    public T pop() {
        T[] tArr = this.items;
        int i = this.size - 1;
        this.size = i;
        T item = tArr[i];
        this.items[this.size] = null;
        return item;
    }

    public T peek() {
        return this.items[this.size - 1];
    }

    public T peek(int offset) {
        return this.items[(this.size - 1) - offset];
    }
}
