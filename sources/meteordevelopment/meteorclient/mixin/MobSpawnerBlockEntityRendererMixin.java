package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.NoRender;
import net.minecraft.class_2636;
import net.minecraft.class_827;
import net.minecraft.class_839;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/MobSpawnerBlockEntityRendererMixin.class */
@Mixin({class_839.class})
public abstract class MobSpawnerBlockEntityRendererMixin implements class_827<class_2636> {
    @Inject(method = {"render(Lnet/minecraft/block/entity/MobSpawnerBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/util/math/Vec3d;)V"}, at = {@At("HEAD")}, cancellable = true)
    private void onRender(CallbackInfo ci) {
        if (((NoRender) Modules.get().get(NoRender.class)).noMobInSpawner()) {
            ci.cancel();
        }
    }
}
