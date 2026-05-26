package javassist.bytecode.analysis;

import java.util.NoSuchElementException;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:javassist/bytecode/analysis/IntQueue.class */
class IntQueue {
    private Entry head;
    private Entry tail;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:javassist/bytecode/analysis/IntQueue$Entry.class */
    private static class Entry {
        private Entry next;
        private int value;

        private Entry(int value) {
            this.value = value;
        }
    }

    IntQueue() {
    }

    void add(int value) {
        Entry entry = new Entry(value);
        if (this.tail != null) {
            this.tail.next = entry;
        }
        this.tail = entry;
        if (this.head == null) {
            this.head = entry;
        }
    }

    boolean isEmpty() {
        return this.head == null;
    }

    int take() {
        if (this.head != null) {
            int value = this.head.value;
            this.head = this.head.next;
            if (this.head == null) {
                this.tail = null;
            }
            return value;
        }
        throw new NoSuchElementException();
    }
}
