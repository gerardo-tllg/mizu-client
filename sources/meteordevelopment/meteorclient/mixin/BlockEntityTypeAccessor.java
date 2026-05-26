package meteordevelopment.meteorclient.mixin;

import java.util.Set;
import net.minecraft.class_2248;
import net.minecraft.class_2591;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/BlockEntityTypeAccessor.class */
@Mixin({class_2591.class})
public interface BlockEntityTypeAccessor {
    @Accessor("field_19315")
    Set<class_2248> getBlocks();
}
