package meteordevelopment.meteorclient.mixin;

import net.minecraft.class_2960;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/IdentifierAccessor.class */
@Mixin({class_2960.class})
public interface IdentifierAccessor {
    @Accessor("field_13355")
    @Mutable
    void setPath(String str);
}
