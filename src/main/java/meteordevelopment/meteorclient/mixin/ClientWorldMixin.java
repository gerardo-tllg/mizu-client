/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import meteordevelopment.meteorclient.mixininterface.IClientWorld;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Ambience;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientWorld.class)
public class ClientWorldMixin implements IClientWorld {
    @Shadow @Final private PendingUpdateManager pendingUpdateManager;

    @Override
    public int meteor$getSequence() {
        return ((PendingUpdateManagerAccessor) pendingUpdateManager).getSequence();
    }

    @Override
    public int meteor$getAndIncrementSequence() {
        PendingUpdateManager manager = ((PendingUpdateManagerAccessor) pendingUpdateManager).invokeIncrementSequence();
        return ((PendingUpdateManagerAccessor) manager).getSequence();
    }

    @ModifyReturnValue(method = "getSkyColor", at = @At("RETURN"))
    private int modifySkyColor(int original) {
        Ambience ambience = Modules.get().get(Ambience.class);
        if (ambience.isActive() && ambience.customSkyColor.get()) {
            Color color = ambience.skyColor();
            if (color != null) {
                return color.getPacked();
            }
        }
        return original;
    }

    @ModifyReturnValue(method = "getCloudsColor", at = @At("RETURN"))
    private int modifyCloudsColor(int original) {
        Ambience ambience = Modules.get().get(Ambience.class);
        if (ambience.isActive() && ambience.customCloudColor.get()) {
            return ambience.cloudColor.get().getPacked();
        }
        return original;
    }
}
