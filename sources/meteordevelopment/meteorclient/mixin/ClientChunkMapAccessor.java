package meteordevelopment.meteorclient.mixin;

import java.util.concurrent.atomic.AtomicReferenceArray;
import net.minecraft.class_2818;
import net.minecraft.class_631;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/ClientChunkMapAccessor.class */
@Mixin({class_631.class_3681.class})
public interface ClientChunkMapAccessor {
    @Accessor("field_16251")
    AtomicReferenceArray<class_2818> getChunks();
}
