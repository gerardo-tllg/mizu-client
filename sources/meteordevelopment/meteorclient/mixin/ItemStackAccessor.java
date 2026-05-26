package meteordevelopment.meteorclient.mixin;

import net.minecraft.class_1792;
import net.minecraft.class_1799;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/ItemStackAccessor.class */
@Mixin({class_1799.class})
public interface ItemStackAccessor {
    @Accessor("item")
    @Mutable
    void setItem(class_1792 class_1792Var);
}
