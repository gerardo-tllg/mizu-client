package meteordevelopment.meteorclient.mixin;

import net.minecraft.class_631;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/ClientChunkManagerAccessor.class */
@Mixin({class_631.class})
public interface ClientChunkManagerAccessor {
    @Accessor("field_16246")
    class_631.class_3681 getChunks();
}
