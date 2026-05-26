package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.mixininterface.IAbstractFurnaceScreenHandler;
import net.minecraft.class_1720;
import net.minecraft.class_1799;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/AbstractFurnaceScreenHandlerMixin.class */
@Mixin({class_1720.class})
public abstract class AbstractFurnaceScreenHandlerMixin implements IAbstractFurnaceScreenHandler {
    @Shadow
    protected abstract boolean method_7640(class_1799 class_1799Var);

    @Override // meteordevelopment.meteorclient.mixininterface.IAbstractFurnaceScreenHandler
    public boolean meteor$isItemSmeltable(class_1799 itemStack) {
        return method_7640(itemStack);
    }
}
