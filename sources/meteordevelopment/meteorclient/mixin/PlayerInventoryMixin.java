package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.mixininterface.IPlayerInventory;
import net.minecraft.class_10630;
import net.minecraft.class_1304;
import net.minecraft.class_1661;
import net.minecraft.class_1799;
import net.minecraft.class_2371;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/PlayerInventoryMixin.class */
@Mixin({class_1661.class})
public abstract class PlayerInventoryMixin implements IPlayerInventory {

    @Shadow
    @Final
    private class_10630 field_56552;

    @Shadow
    public abstract class_2371<class_1799> method_67533();

    @Override // meteordevelopment.meteorclient.mixininterface.IPlayerInventory
    public class_2371<class_1799> meteor$getArmor() {
        class_2371<class_1799> armor = class_2371.method_10213(4, class_1799.field_8037);
        armor.set(0, this.field_56552.method_66659(class_1304.field_6166));
        armor.set(1, this.field_56552.method_66659(class_1304.field_6172));
        armor.set(2, this.field_56552.method_66659(class_1304.field_6174));
        armor.set(3, this.field_56552.method_66659(class_1304.field_6169));
        return armor;
    }

    @Override // meteordevelopment.meteorclient.mixininterface.IPlayerInventory
    public class_2371<class_1799> meteor$getMain() {
        return method_67533();
    }
}
