/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.mixininterface.IClientWorld;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

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
}

