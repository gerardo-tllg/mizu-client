package meteordevelopment.meteorclient.mixin;

import net.minecraft.class_5568;
import net.minecraft.class_5573;
import net.minecraft.class_5578;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/SimpleEntityLookupAccessor.class */
@Mixin({class_5578.class})
public interface SimpleEntityLookupAccessor {
    @Accessor("field_27259")
    <T extends class_5568> class_5573<T> getCache();
}
