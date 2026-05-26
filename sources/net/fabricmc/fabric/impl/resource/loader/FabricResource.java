package net.fabricmc.fabric.impl.resource.loader;

import net.minecraft.class_5352;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:META-INF/jars/fabric-resource-loader-v0-3.1.6+02ca679649.jar:net/fabricmc/fabric/impl/resource/loader/FabricResource.class */
public interface FabricResource {
    default class_5352 getFabricPackSource() {
        LoggerFactory.getLogger(FabricResource.class).error("Unknown Resource implementation {}, returning PACK_SOURCE_NONE as the source", getClass().getName());
        return class_5352.field_25347;
    }
}
