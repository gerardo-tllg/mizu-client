package net.fabricmc.fabric.impl.base.event;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.impl.base.toposort.NodeSorting;
import net.minecraft.class_2960;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:META-INF/jars/fabric-api-base-0.4.62+73a52b4b49.jar:net/fabricmc/fabric/impl/base/event/ArrayBackedEvent.class */
class ArrayBackedEvent<T> extends Event<T> {
    private final Function<T[], T> invokerFactory;
    private T[] handlers;
    private final Object lock = new Object();
    private final Map<class_2960, EventPhaseData<T>> phases = new LinkedHashMap();
    private final List<EventPhaseData<T>> sortedPhases = new ArrayList();

    ArrayBackedEvent(Class<? super T> cls, Function<T[], T> function) {
        this.invokerFactory = function;
        this.handlers = (T[]) ((Object[]) Array.newInstance(cls, 0));
        update();
    }

    void update() {
        this.invoker = this.invokerFactory.apply(this.handlers);
    }

    @Override // net.fabricmc.fabric.api.event.Event
    public void register(T listener) {
        register(DEFAULT_PHASE, listener);
    }

    @Override // net.fabricmc.fabric.api.event.Event
    public void register(class_2960 phaseIdentifier, T listener) {
        Objects.requireNonNull(phaseIdentifier, "Tried to register a listener for a null phase!");
        Objects.requireNonNull(listener, "Tried to register a null listener!");
        synchronized (this.lock) {
            getOrCreatePhase(phaseIdentifier, true).addListener(listener);
            rebuildInvoker(this.handlers.length + 1);
        }
    }

    private EventPhaseData<T> getOrCreatePhase(class_2960 id, boolean sortIfCreate) {
        EventPhaseData<T> phase = this.phases.get(id);
        if (phase == null) {
            phase = new EventPhaseData<>(id, this.handlers.getClass().getComponentType());
            this.phases.put(id, phase);
            this.sortedPhases.add(phase);
            if (sortIfCreate) {
                NodeSorting.sort(this.sortedPhases, "event phases", Comparator.comparing(data -> {
                    return data.id;
                }));
            }
        }
        return phase;
    }

    private void rebuildInvoker(int i) {
        if (this.sortedPhases.size() == 1) {
            this.handlers = this.sortedPhases.get(0).listeners;
        } else {
            T[] tArr = (T[]) ((Object[]) Array.newInstance(this.handlers.getClass().getComponentType(), i));
            int i2 = 0;
            for (EventPhaseData<T> eventPhaseData : this.sortedPhases) {
                int length = eventPhaseData.listeners.length;
                System.arraycopy(eventPhaseData.listeners, 0, tArr, i2, length);
                i2 += length;
            }
            this.handlers = tArr;
        }
        update();
    }

    @Override // net.fabricmc.fabric.api.event.Event
    public void addPhaseOrdering(class_2960 firstPhase, class_2960 secondPhase) {
        Objects.requireNonNull(firstPhase, "Tried to add an ordering for a null phase.");
        Objects.requireNonNull(secondPhase, "Tried to add an ordering for a null phase.");
        if (firstPhase.equals(secondPhase)) {
            throw new IllegalArgumentException("Tried to add a phase that depends on itself.");
        }
        synchronized (this.lock) {
            EventPhaseData<T> first = getOrCreatePhase(firstPhase, false);
            EventPhaseData<T> second = getOrCreatePhase(secondPhase, false);
            EventPhaseData.link(first, second);
            NodeSorting.sort(this.sortedPhases, "event phases", Comparator.comparing(data -> {
                return data.id;
            }));
            rebuildInvoker(this.handlers.length);
        }
    }
}
