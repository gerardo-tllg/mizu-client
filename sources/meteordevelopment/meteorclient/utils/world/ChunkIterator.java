package meteordevelopment.meteorclient.utils.world;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReferenceArray;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.mixin.ClientChunkMapAccessor;
import net.minecraft.class_2791;
import net.minecraft.class_2818;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/world/ChunkIterator.class */
public class ChunkIterator implements Iterator<class_2791> {
    private final boolean onlyWithLoadedNeighbours;
    private class_2791 chunk;
    private final ClientChunkMapAccessor map = MeteorClient.mc.field_1687.method_2935().getChunks();
    private int i = 0;

    public ChunkIterator(boolean onlyWithLoadedNeighbours) {
        this.onlyWithLoadedNeighbours = onlyWithLoadedNeighbours;
        getNext();
    }

    private class_2791 getNext() {
        class_2791 prev = this.chunk;
        this.chunk = null;
        while (this.i < this.map.getChunks().length()) {
            AtomicReferenceArray<class_2818> chunks = this.map.getChunks();
            int i = this.i;
            this.i = i + 1;
            this.chunk = chunks.get(i);
            if (this.chunk != null && (!this.onlyWithLoadedNeighbours || isInRadius(this.chunk))) {
                break;
            }
        }
        return prev;
    }

    private boolean isInRadius(class_2791 chunk) {
        int x = chunk.method_12004().field_9181;
        int z = chunk.method_12004().field_9180;
        return MeteorClient.mc.field_1687.method_2935().method_12123(x + 1, z) && MeteorClient.mc.field_1687.method_2935().method_12123(x - 1, z) && MeteorClient.mc.field_1687.method_2935().method_12123(x, z + 1) && MeteorClient.mc.field_1687.method_2935().method_12123(x, z - 1);
    }

    @Override // java.util.Iterator
    public boolean hasNext() {
        return this.chunk != null;
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // java.util.Iterator
    public class_2791 next() {
        return getNext();
    }
}
