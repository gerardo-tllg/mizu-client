package meteordevelopment.meteorclient.mixin;

import net.minecraft.class_1263;
import net.minecraft.class_1733;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/ShulkerBoxScreenHandlerAccessor.class */
@Mixin({class_1733.class})
public interface ShulkerBoxScreenHandlerAccessor {
    @Accessor("field_7867")
    class_1263 meteor$getInventory();
}
