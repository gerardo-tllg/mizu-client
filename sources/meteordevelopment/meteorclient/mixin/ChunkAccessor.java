package meteordevelopment.meteorclient.mixin;

import java.util.Map;
import net.minecraft.class_2338;
import net.minecraft.class_2586;
import net.minecraft.class_2791;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/ChunkAccessor.class */
@Mixin({class_2791.class})
public interface ChunkAccessor {
    @Accessor("field_34543")
    Map<class_2338, class_2586> getBlockEntities();
}
