package meteordevelopment.meteorclient.utils.world;

import java.util.Iterator;
import java.util.Map;
import net.minecraft.class_2338;
import net.minecraft.class_2586;
import net.minecraft.class_2791;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/world/BlockEntityIterator.class */
public class BlockEntityIterator implements Iterator<class_2586> {
    private final Iterator<class_2791> chunks = new ChunkIterator(false);
    private Iterator<class_2586> blockEntities;

    public BlockEntityIterator() {
        nextChunk();
    }

    private void nextChunk() {
        while (this.chunks.hasNext()) {
            Map<class_2338, class_2586> blockEntityMap = this.chunks.next().getBlockEntities();
            if (!blockEntityMap.isEmpty()) {
                this.blockEntities = blockEntityMap.values().iterator();
                return;
            }
        }
    }

    @Override // java.util.Iterator
    public boolean hasNext() {
        if (this.blockEntities == null) {
            return false;
        }
        if (this.blockEntities.hasNext()) {
            return true;
        }
        nextChunk();
        return this.blockEntities.hasNext();
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // java.util.Iterator
    public class_2586 next() {
        return this.blockEntities.next();
    }
}
