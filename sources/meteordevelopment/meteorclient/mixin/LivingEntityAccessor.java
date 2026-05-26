package meteordevelopment.meteorclient.mixin;

import net.minecraft.class_1309;
import net.minecraft.class_3611;
import net.minecraft.class_6862;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/LivingEntityAccessor.class */
@Mixin({class_1309.class})
public interface LivingEntityAccessor {
    @Invoker("method_6010")
    void swimUpwards(class_6862<class_3611> class_6862Var);

    @Accessor("field_6282")
    boolean isJumping();

    @Accessor("field_6228")
    int getJumpCooldown();

    @Accessor("field_6228")
    void setJumpCooldown(int i);
}
