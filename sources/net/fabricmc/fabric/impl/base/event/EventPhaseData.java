package net.fabricmc.fabric.impl.base.event;

import java.lang.reflect.Array;
import java.util.Arrays;
import net.fabricmc.fabric.impl.base.toposort.SortableNode;
import net.minecraft.class_2960;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:META-INF/jars/fabric-api-base-0.4.62+73a52b4b49.jar:net/fabricmc/fabric/impl/base/event/EventPhaseData.class */
class EventPhaseData<T> extends SortableNode<EventPhaseData<T>> {
    final class_2960 id;
    T[] listeners;

    EventPhaseData(class_2960 class_2960Var, Class<?> cls) {
        this.id = class_2960Var;
        this.listeners = (T[]) ((Object[]) Array.newInstance(cls, 0));
    }

    void addListener(T t) {
        int length = this.listeners.length;
        this.listeners = (T[]) Arrays.copyOf(this.listeners, length + 1);
        this.listeners[length] = t;
    }

    @Override // net.fabricmc.fabric.impl.base.toposort.SortableNode
    protected String getDescription() {
        return this.id.toString();
    }
}
