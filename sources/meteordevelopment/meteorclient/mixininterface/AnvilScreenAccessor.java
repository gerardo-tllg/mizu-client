package meteordevelopment.meteorclient.mixininterface;

import net.minecraft.class_342;
import net.minecraft.class_471;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixininterface/AnvilScreenAccessor.class */
@Mixin({class_471.class})
public interface AnvilScreenAccessor {
    @Accessor
    class_342 getNameField();
}
