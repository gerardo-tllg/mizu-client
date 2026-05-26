package meteordevelopment.meteorclient.mixin;

import net.minecraft.class_1297;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/EntityAccessor.class */
@Mixin({class_1297.class})
public interface EntityAccessor {
    @Accessor("touchingWater")
    void setInWater(boolean z);
}
