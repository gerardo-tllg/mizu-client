package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Ambience;
import net.minecraft.class_1163;
import net.minecraft.class_1920;
import net.minecraft.class_2338;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/BiomeColorsMixin.class */
@Mixin({class_1163.class})
public abstract class BiomeColorsMixin {
    @Inject(method = {"getWaterColor"}, at = {@At("HEAD")}, cancellable = true)
    private static void onGetWaterColor(class_1920 world, class_2338 pos, CallbackInfoReturnable<Integer> info) {
        Ambience ambience = (Ambience) Modules.get().get(Ambience.class);
        if (ambience.isActive() && ambience.customWaterColor.get().booleanValue()) {
            info.setReturnValue(Integer.valueOf(ambience.waterColor.get().getPacked()));
        }
    }

    @Inject(method = {"getFoliageColor"}, at = {@At("HEAD")}, cancellable = true)
    private static void onGetFoliageColor(class_1920 world, class_2338 pos, CallbackInfoReturnable<Integer> info) {
        Ambience ambience = (Ambience) Modules.get().get(Ambience.class);
        if (ambience.isActive() && ambience.customFoliageColor.get().booleanValue()) {
            info.setReturnValue(Integer.valueOf(ambience.foliageColor.get().getPacked()));
        }
    }

    @Inject(method = {"getGrassColor"}, at = {@At("HEAD")}, cancellable = true)
    private static void onGetGrassColor(class_1920 world, class_2338 pos, CallbackInfoReturnable<Integer> info) {
        Ambience ambience = (Ambience) Modules.get().get(Ambience.class);
        if (ambience.isActive() && ambience.customGrassColor.get().booleanValue()) {
            info.setReturnValue(Integer.valueOf(ambience.grassColor.get().getPacked()));
        }
    }
}
