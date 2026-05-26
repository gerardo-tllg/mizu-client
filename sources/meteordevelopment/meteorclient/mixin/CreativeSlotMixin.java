package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.mixininterface.ISlot;
import net.minecraft.class_1735;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/CreativeSlotMixin.class */
@Mixin(targets = {"net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen$CreativeSlot"})
public abstract class CreativeSlotMixin implements ISlot {

    @Shadow
    @Final
    class_1735 field_2898;

    @Override // meteordevelopment.meteorclient.mixininterface.ISlot
    public int meteor$getId() {
        return this.field_2898.field_7874;
    }

    @Override // meteordevelopment.meteorclient.mixininterface.ISlot
    public int meteor$getIndex() {
        return this.field_2898.method_34266();
    }
}
