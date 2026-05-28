/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.systems.config.Config;
import net.minecraft.client.gui.screen.SplashTextRenderer;
import net.minecraft.client.resource.SplashTextResourceSupplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Random;

@Mixin(SplashTextResourceSupplier.class)
public abstract class SplashTextResourceSupplierMixin {
    @Unique
    private static final Random random = new Random();
    @Unique
    private final List<String> mizuSplashes = getMizuSplashes();

    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    private void onApply(CallbackInfoReturnable<SplashTextRenderer> cir) {
        if (Config.get() == null || !Config.get().titleScreenSplashes.get()) return;
        cir.setReturnValue(new SplashTextRenderer(mizuSplashes.get(random.nextInt(mizuSplashes.size()))));
    }

    @Unique
    private static List<String> getMizuSplashes() {
        return List.of(
            "swavez",
            "mizu on top",
            "water runs deep",
            "built different",
            "hunting season",
            "pitch 40",
            "new chunks",
            "stay hydrated"
        );
    }
}
