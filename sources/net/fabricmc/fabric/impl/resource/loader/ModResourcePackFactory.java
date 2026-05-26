package net.fabricmc.fabric.impl.resource.loader;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.resource.ModResourcePack;
import net.minecraft.class_3262;
import net.minecraft.class_3288;
import net.minecraft.class_8614;
import net.minecraft.class_9224;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:META-INF/jars/fabric-resource-loader-v0-3.1.6+02ca679649.jar:net/fabricmc/fabric/impl/resource/loader/ModResourcePackFactory.class */
public final class ModResourcePackFactory extends Record implements class_3288.class_7680 {
    private final ModResourcePack pack;

    public ModResourcePackFactory(ModResourcePack pack) {
        this.pack = pack;
    }

    @Override // java.lang.Record
    public final String toString() {
        return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, ModResourcePackFactory.class), ModResourcePackFactory.class, "pack", "FIELD:Lnet/fabricmc/fabric/impl/resource/loader/ModResourcePackFactory;->pack:Lnet/fabricmc/fabric/api/resource/ModResourcePack;").dynamicInvoker().invoke(this) /* invoke-custom */;
    }

    @Override // java.lang.Record
    public final int hashCode() {
        return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, ModResourcePackFactory.class), ModResourcePackFactory.class, "pack", "FIELD:Lnet/fabricmc/fabric/impl/resource/loader/ModResourcePackFactory;->pack:Lnet/fabricmc/fabric/api/resource/ModResourcePack;").dynamicInvoker().invoke(this) /* invoke-custom */;
    }

    @Override // java.lang.Record
    public final boolean equals(Object o) {
        return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, ModResourcePackFactory.class, Object.class), ModResourcePackFactory.class, "pack", "FIELD:Lnet/fabricmc/fabric/impl/resource/loader/ModResourcePackFactory;->pack:Lnet/fabricmc/fabric/api/resource/ModResourcePack;").dynamicInvoker().invoke(this, o) /* invoke-custom */;
    }

    public ModResourcePack pack() {
        return this.pack;
    }

    public class_3262 method_52424(class_9224 var1) {
        return this.pack;
    }

    public class_3262 method_52425(class_9224 var1, class_3288.class_7679 metadata) {
        if (metadata.comp_1584().isEmpty()) {
            return this.pack;
        }
        List<class_3262> overlays = new ArrayList<>(metadata.comp_1584().size());
        for (String overlay : metadata.comp_1584()) {
            overlays.add(this.pack.createOverlay(overlay));
        }
        return new class_8614(this.pack, overlays);
    }
}
