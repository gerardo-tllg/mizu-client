package meteordevelopment.meteorclient.mixin;

import net.minecraft.class_4970;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/AbstractBlockAccessor.class */
@Mixin({class_4970.class})
public interface AbstractBlockAccessor {
    @Accessor("collidable")
    boolean isCollidable();
}
