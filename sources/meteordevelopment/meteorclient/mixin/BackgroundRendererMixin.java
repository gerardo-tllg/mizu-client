package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.NoRender;
import meteordevelopment.meteorclient.systems.modules.render.Xray;
import meteordevelopment.meteorclient.systems.modules.world.Ambience;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.class_1297;
import net.minecraft.class_4184;
import net.minecraft.class_758;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/BackgroundRendererMixin.class */
@Mixin({class_758.class})
public abstract class BackgroundRendererMixin {
    @ModifyArgs(method = {"applyFog"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Fog;<init>(FFLnet/minecraft/client/render/FogShape;FFFF)V"))
    private static void modifyFogDistance(Args args, class_4184 camera, class_758.class_4596 fogType, Vector4f color, float viewDistance, boolean thickenFog, float tickDelta) {
        if (fogType == class_758.class_4596.field_20946) {
            if (((NoRender) Modules.get().get(NoRender.class)).noFog() || Modules.get().isActive(Xray.class)) {
                args.set(0, Float.valueOf(viewDistance * 4.0f));
                args.set(1, Float.valueOf(viewDistance * 4.25f));
            }
            Ambience ambience = (Ambience) Modules.get().get(Ambience.class);
            if (ambience.isActive() && ambience.customFogColor.get().booleanValue()) {
                Color fogColor = ambience.fogColor.get();
                args.set(3, Float.valueOf(fogColor.r / 255.0f));
                args.set(4, Float.valueOf(fogColor.g / 255.0f));
                args.set(5, Float.valueOf(fogColor.b / 255.0f));
                args.set(6, Float.valueOf(fogColor.a / 255.0f));
            }
        }
    }

    @Inject(method = {"getFogModifier(Lnet/minecraft/entity/Entity;F)Lnet/minecraft/client/render/BackgroundRenderer$StatusEffectFogModifier;"}, at = {@At("HEAD")}, cancellable = true)
    private static void onGetFogModifier(class_1297 entity, float tickDelta, CallbackInfoReturnable<Object> info) {
        if (((NoRender) Modules.get().get(NoRender.class)).noBlindness()) {
            info.setReturnValue((Object) null);
        }
    }
}
