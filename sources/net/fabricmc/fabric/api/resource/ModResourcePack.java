package net.fabricmc.fabric.api.resource;

import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.class_3262;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:META-INF/jars/fabric-resource-loader-v0-3.1.6+02ca679649.jar:net/fabricmc/fabric/api/resource/ModResourcePack.class */
public interface ModResourcePack extends class_3262 {
    ModMetadata getFabricModMetadata();

    ModResourcePack createOverlay(String str);
}
