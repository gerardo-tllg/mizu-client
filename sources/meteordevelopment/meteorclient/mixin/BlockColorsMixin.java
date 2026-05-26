package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Ambience;
import net.minecraft.class_322;
import net.minecraft.class_324;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/BlockColorsMixin.class */
@Mixin({class_324.class})
public abstract class BlockColorsMixin {
    @ModifyArg(method = {"create"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/color/block/BlockColors;registerColorProvider(Lnet/minecraft/client/color/block/BlockColorProvider;[Lnet/minecraft/block/Block;)V", ordinal = 3), index = 0)
    private static class_322 modifySpruceLeavesColor(class_322 provider) {
        return (state, world, pos, tintIndex) -> {
            return getModifiedColor(-10380959);
        };
    }

    @ModifyArg(method = {"create"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/color/block/BlockColors;registerColorProvider(Lnet/minecraft/client/color/block/BlockColorProvider;[Lnet/minecraft/block/Block;)V", ordinal = 4), index = 0)
    private static class_322 modifyBirchLeavesColor(class_322 provider) {
        return (state, world, pos, tintIndex) -> {
            return getModifiedColor(-8345771);
        };
    }

    /* JADX INFO: Access modifiers changed from: private */
    @Unique
    public static int getModifiedColor(int original) {
        if (Modules.get() == null) {
            return original;
        }
        Ambience ambience = (Ambience) Modules.get().get(Ambience.class);
        if (ambience.isActive() && ambience.customFoliageColor.get().booleanValue()) {
            return ambience.foliageColor.get().getPacked();
        }
        return original;
    }
}
