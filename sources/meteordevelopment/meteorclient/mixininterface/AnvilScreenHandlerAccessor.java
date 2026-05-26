package meteordevelopment.meteorclient.mixininterface;

import net.minecraft.class_1706;
import net.minecraft.class_3915;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixininterface/AnvilScreenHandlerAccessor.class */
@Mixin({class_1706.class})
public interface AnvilScreenHandlerAccessor {
    @Accessor("field_7770")
    class_3915 getLevelCost();
}
