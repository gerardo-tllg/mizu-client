package meteordevelopment.meteorclient.mixin;

import net.minecraft.class_1735;
import net.minecraft.class_465;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/HandledScreenAccessor.class */
@Mixin({class_465.class})
public interface HandledScreenAccessor {
    @Accessor("field_2787")
    class_1735 getFocusedSlot();
}
