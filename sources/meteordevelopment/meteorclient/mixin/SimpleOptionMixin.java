package meteordevelopment.meteorclient.mixin;

import java.util.Objects;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.mixininterface.ISimpleOption;
import net.minecraft.class_310;
import net.minecraft.class_7172;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/SimpleOptionMixin.class */
@Mixin({class_7172.class})
public abstract class SimpleOptionMixin implements ISimpleOption {

    @Shadow
    Object field_37868;

    @Shadow
    @Final
    private Consumer<Object> field_37867;

    @Override // meteordevelopment.meteorclient.mixininterface.ISimpleOption
    public void setValue(Object value) {
        if (!class_310.method_1551().method_22108()) {
            this.field_37868 = value;
        } else if (!Objects.equals(this.field_37868, value)) {
            this.field_37868 = value;
            this.field_37867.accept(this.field_37868);
        }
    }
}
