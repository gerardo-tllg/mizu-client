package meteordevelopment.meteorclient.mixin;

import net.minecraft.class_1299;
import net.minecraft.class_1785;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/EntityBucketItemAccessor.class */
@Mixin({class_1785.class})
public interface EntityBucketItemAccessor {
    @Accessor("entityType")
    class_1299<?> getEntityType();
}
