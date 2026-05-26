package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.Scaffold;
import net.minecraft.class_1299;
import net.minecraft.class_1309;
import net.minecraft.class_1937;
import net.minecraft.class_3222;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/ServerPlayerEntityMixin.class */
@Mixin({class_3222.class})
public abstract class ServerPlayerEntityMixin extends class_1309 {
    protected ServerPlayerEntityMixin(class_1299<? extends class_1309> entityType, class_1937 world) {
        super(entityType, world);
    }

    @Inject(method = {"jump"}, at = {@At("HEAD")}, cancellable = true)
    public void dontJump(CallbackInfo ci) {
        if (method_37908().field_9236 && ((Scaffold) Modules.get().get(Scaffold.class)).towering()) {
            ci.cancel();
        }
    }
}
