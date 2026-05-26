package meteordevelopment.meteorclient.events.world;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.ObjectMethods;
import net.minecraft.class_2818;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/world/ChunkDataEvent.class */
public final class ChunkDataEvent extends Record {
    private final class_2818 chunk;

    public ChunkDataEvent(class_2818 chunk) {
        this.chunk = chunk;
    }

    @Override // java.lang.Record
    public final String toString() {
        return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, ChunkDataEvent.class), ChunkDataEvent.class, "chunk", "FIELD:Lmeteordevelopment/meteorclient/events/world/ChunkDataEvent;->chunk:Lnet/minecraft/class_2818;").dynamicInvoker().invoke(this) /* invoke-custom */;
    }

    @Override // java.lang.Record
    public final int hashCode() {
        return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, ChunkDataEvent.class), ChunkDataEvent.class, "chunk", "FIELD:Lmeteordevelopment/meteorclient/events/world/ChunkDataEvent;->chunk:Lnet/minecraft/class_2818;").dynamicInvoker().invoke(this) /* invoke-custom */;
    }

    @Override // java.lang.Record
    public final boolean equals(Object o) {
        return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, ChunkDataEvent.class, Object.class), ChunkDataEvent.class, "chunk", "FIELD:Lmeteordevelopment/meteorclient/events/world/ChunkDataEvent;->chunk:Lnet/minecraft/class_2818;").dynamicInvoker().invoke(this, o) /* invoke-custom */;
    }

    public class_2818 chunk() {
        return this.chunk;
    }
}
