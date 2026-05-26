package net.fabricmc.fabric.impl.resource.loader;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.ObjectMethods;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_5352;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:META-INF/jars/fabric-resource-loader-v0-3.1.6+02ca679649.jar:net/fabricmc/fabric/impl/resource/loader/BuiltinModResourcePackSource.class */
public final class BuiltinModResourcePackSource extends Record implements class_5352 {
    private final String modId;

    public BuiltinModResourcePackSource(String modId) {
        this.modId = modId;
    }

    @Override // java.lang.Record
    public final String toString() {
        return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, BuiltinModResourcePackSource.class), BuiltinModResourcePackSource.class, "modId", "FIELD:Lnet/fabricmc/fabric/impl/resource/loader/BuiltinModResourcePackSource;->modId:Ljava/lang/String;").dynamicInvoker().invoke(this) /* invoke-custom */;
    }

    @Override // java.lang.Record
    public final int hashCode() {
        return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, BuiltinModResourcePackSource.class), BuiltinModResourcePackSource.class, "modId", "FIELD:Lnet/fabricmc/fabric/impl/resource/loader/BuiltinModResourcePackSource;->modId:Ljava/lang/String;").dynamicInvoker().invoke(this) /* invoke-custom */;
    }

    @Override // java.lang.Record
    public final boolean equals(Object o) {
        return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, BuiltinModResourcePackSource.class, Object.class), BuiltinModResourcePackSource.class, "modId", "FIELD:Lnet/fabricmc/fabric/impl/resource/loader/BuiltinModResourcePackSource;->modId:Ljava/lang/String;").dynamicInvoker().invoke(this, o) /* invoke-custom */;
    }

    public String modId() {
        return this.modId;
    }

    public boolean method_45279() {
        return true;
    }

    public class_2561 method_45282(class_2561 packName) {
        return class_2561.method_43469("pack.nameAndSource", new Object[]{packName, class_2561.method_43469("pack.source.builtinMod", new Object[]{this.modId})}).method_27692(class_124.field_1080);
    }
}
