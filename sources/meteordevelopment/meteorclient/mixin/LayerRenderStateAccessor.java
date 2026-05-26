package meteordevelopment.meteorclient.mixin;

import net.minecraft.class_10444;
import net.minecraft.class_804;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/LayerRenderStateAccessor.class */
@Mixin({class_10444.class_10446.class})
public interface LayerRenderStateAccessor {
    @Accessor("field_56967")
    class_804 getTransform();
}
