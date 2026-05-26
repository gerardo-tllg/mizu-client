package meteordevelopment.meteorclient.mixininterface;

import javax.annotation.Nullable;
import net.minecraft.class_2535;
import net.minecraft.class_2596;
import net.minecraft.class_7648;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixininterface/ClientConnectionAccessor.class */
@Mixin({class_2535.class})
public interface ClientConnectionAccessor {
    @Invoker("sendImmediately")
    void invokeSendImmediately(class_2596<?> class_2596Var, @Nullable class_7648 class_7648Var, boolean z);
}
