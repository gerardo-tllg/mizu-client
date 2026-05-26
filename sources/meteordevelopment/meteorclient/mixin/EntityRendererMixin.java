package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.Fullbright;
import meteordevelopment.meteorclient.systems.modules.render.Nametags;
import meteordevelopment.meteorclient.systems.modules.render.NoRender;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.render.postprocess.PostProcessShaders;
import net.minecraft.class_10017;
import net.minecraft.class_1297;
import net.minecraft.class_1540;
import net.minecraft.class_1657;
import net.minecraft.class_1944;
import net.minecraft.class_2561;
import net.minecraft.class_4604;
import net.minecraft.class_897;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/EntityRendererMixin.class */
@Mixin({class_897.class})
public abstract class EntityRendererMixin<T extends class_1297, S extends class_10017> {
    @Inject(method = {"getDisplayName"}, at = {@At("HEAD")}, cancellable = true)
    private void onRenderLabel(T entity, CallbackInfoReturnable<class_2561> cir) {
        if (PostProcessShaders.rendering) {
            cir.setReturnValue((Object) null);
        }
        if (((NoRender) Modules.get().get(NoRender.class)).noNametags()) {
            cir.setReturnValue((Object) null);
        }
        if (entity instanceof class_1657) {
            class_1657 player = (class_1657) entity;
            if (((Nametags) Modules.get().get(Nametags.class)).playerNametags()) {
                if (EntityUtils.getGameMode(player) != null || !((Nametags) Modules.get().get(Nametags.class)).excludeBots()) {
                    cir.setReturnValue((Object) null);
                }
            }
        }
    }

    @Inject(method = {"shouldRender"}, at = {@At("HEAD")}, cancellable = true)
    private void shouldRender(T entity, class_4604 frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        if (((NoRender) Modules.get().get(NoRender.class)).noEntity(entity)) {
            cir.setReturnValue(false);
        }
        if (!((NoRender) Modules.get().get(NoRender.class)).noFallingBlocks() || !(entity instanceof class_1540)) {
            return;
        }
        cir.setReturnValue(false);
    }

    @ModifyReturnValue(method = {"getSkyLight"}, at = {@At("RETURN")})
    private int onGetSkyLight(int original) {
        return Math.max(((Fullbright) Modules.get().get(Fullbright.class)).getLuminance(class_1944.field_9284), original);
    }

    @ModifyReturnValue(method = {"getBlockLight"}, at = {@At("RETURN")})
    private int onGetBlockLight(int original) {
        return Math.max(((Fullbright) Modules.get().get(Fullbright.class)).getLuminance(class_1944.field_9282), original);
    }

    @ModifyExpressionValue(method = {"updateRenderState"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/world/World;getLightLevel(Lnet/minecraft/world/LightType;Lnet/minecraft/util/math/BlockPos;)I")})
    private int onGetLightLevel(int original) {
        return Math.max(((Fullbright) Modules.get().get(Fullbright.class)).getLuminance(class_1944.field_9282), original);
    }
}
