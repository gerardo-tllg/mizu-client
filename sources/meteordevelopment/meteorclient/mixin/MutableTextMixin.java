package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.mixininterface.IText;
import net.minecraft.class_2477;
import net.minecraft.class_5250;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/MutableTextMixin.class */
@Mixin({class_5250.class})
public abstract class MutableTextMixin implements IText {

    @Shadow
    @Nullable
    private class_2477 field_39009;

    @Override // meteordevelopment.meteorclient.mixininterface.IText
    public void meteor$invalidateCache() {
        this.field_39009 = null;
    }
}
