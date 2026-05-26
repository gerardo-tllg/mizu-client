package meteordevelopment.meteorclient.mixin;

import net.minecraft.class_6360;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/ResourceReloadLoggerAccessor.class */
@Mixin({class_6360.class})
public interface ResourceReloadLoggerAccessor {
    @Accessor("field_33699")
    class_6360.class_6363 getReloadState();
}
