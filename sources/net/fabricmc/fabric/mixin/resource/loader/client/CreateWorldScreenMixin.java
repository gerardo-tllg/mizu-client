package net.fabricmc.fabric.mixin.resource.loader.client;

import com.mojang.datafixers.util.Pair;
import java.io.File;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.impl.resource.loader.ModResourcePackCreator;
import net.fabricmc.fabric.impl.resource.loader.ModResourcePackUtil;
import net.minecraft.class_2561;
import net.minecraft.class_3264;
import net.minecraft.class_3283;
import net.minecraft.class_437;
import net.minecraft.class_525;
import net.minecraft.class_7712;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:META-INF/jars/fabric-resource-loader-v0-3.1.6+02ca679649.jar:net/fabricmc/fabric/mixin/resource/loader/client/CreateWorldScreenMixin.class */
@Mixin({class_525.class})
@Environment(EnvType.CLIENT)
public abstract class CreateWorldScreenMixin extends class_437 {

    @Shadow
    private class_3283 field_25792;

    private CreateWorldScreenMixin() {
        super((class_2561) null);
    }

    @ModifyVariable(method = {"method_64244(Lnet/minecraft/class_310;Lnet/minecraft/class_437;Ljava/util/function/Function;Lnet/minecraft/class_10221;Lnet/minecraft/class_5321;Lnet/minecraft/class_10241;)V"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/class_525;method_41849(Lnet/minecraft/class_3283;Lnet/minecraft/class_7712;)Lnet/minecraft/class_7237$class_6906;"))
    private static class_3283 onCreateResManagerInit(class_3283 manager) {
        manager.field_14227.add(new ModResourcePackCreator(class_3264.field_14190));
        return manager;
    }

    @Redirect(method = {"method_64244(Lnet/minecraft/class_310;Lnet/minecraft/class_437;Ljava/util/function/Function;Lnet/minecraft/class_10221;Lnet/minecraft/class_5321;Lnet/minecraft/class_10241;)V"}, at = @At(value = "FIELD", target = "Lnet/minecraft/class_7712;field_40260:Lnet/minecraft/class_7712;", ordinal = 0))
    private static class_7712 replaceDefaultSettings() {
        return ModResourcePackUtil.createDefaultDataConfiguration();
    }

    @Inject(method = {"method_30296(Lnet/minecraft/class_7712;)Lcom/mojang/datafixers/util/Pair;"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/class_3283;method_14445()V", shift = At.Shift.BEFORE)})
    private void onScanPacks(CallbackInfoReturnable<Pair<File, class_3283>> cir) {
        this.field_25792.field_14227.add(new ModResourcePackCreator(class_3264.field_14190));
    }
}
