package net.fabricmc.fabric.mixin.resource.loader.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.minecraft.class_2960;
import net.minecraft.class_378;
import org.spongepowered.asm.mixin.Mixin;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:META-INF/jars/fabric-resource-loader-v0-3.1.6+02ca679649.jar:net/fabricmc/fabric/mixin/resource/loader/client/FontManagerMixin.class */
@Mixin({class_378.class})
@Environment(EnvType.CLIENT)
public abstract class FontManagerMixin implements IdentifiableResourceReloadListener {
    @Override // net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener
    public class_2960 getFabricId() {
        return ResourceReloadListenerKeys.FONTS;
    }
}
