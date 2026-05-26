package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.mixininterface.ISlot;
import net.minecraft.class_1735;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/SlotMixin.class */
@Mixin({class_1735.class})
public abstract class SlotMixin implements ISlot {

    @Shadow
    public int field_7874;

    @Shadow
    @Final
    private int field_7875;

    @Override // meteordevelopment.meteorclient.mixininterface.ISlot
    public int meteor$getId() {
        return this.field_7874;
    }

    @Override // meteordevelopment.meteorclient.mixininterface.ISlot
    public int meteor$getIndex() {
        return this.field_7875;
    }
}
