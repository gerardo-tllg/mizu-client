package meteordevelopment.meteorclient.mixin.sodium;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.Xray;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockOcclusionCache;
import net.minecraft.class_1922;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2680;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/sodium/SodiumBlockOcclusionCacheMixin.class */
@Mixin(value = {BlockOcclusionCache.class}, remap = false)
public abstract class SodiumBlockOcclusionCacheMixin {

    @Unique
    private Xray xray;

    @Inject(method = {"<init>"}, at = {@At("TAIL")})
    private void onInit(CallbackInfo info) {
        this.xray = (Xray) Modules.get().get(Xray.class);
    }

    @ModifyReturnValue(method = {"shouldDrawSide"}, at = {@At("RETURN")})
    private boolean shouldDrawSide(boolean original, class_2680 state, class_1922 view, class_2338 pos, class_2350 facing) {
        if (this.xray.isActive()) {
            return this.xray.modifyDrawSide(state, view, pos, facing, original);
        }
        return original;
    }
}
