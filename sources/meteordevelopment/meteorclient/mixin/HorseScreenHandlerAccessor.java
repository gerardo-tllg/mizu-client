package meteordevelopment.meteorclient.mixin;

import net.minecraft.class_1496;
import net.minecraft.class_1724;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/HorseScreenHandlerAccessor.class */
@Mixin({class_1724.class})
public interface HorseScreenHandlerAccessor {
    @Accessor("field_7837")
    class_1496 getEntity();
}
