package net.fabricmc.fabric.impl.resource.loader;

import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.ObjectMethods;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;
import net.minecraft.class_155;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_3262;
import net.minecraft.class_3264;
import net.minecraft.class_3272;
import net.minecraft.class_3288;
import net.minecraft.class_7367;
import net.minecraft.class_7662;
import net.minecraft.class_7677;
import net.minecraft.class_9224;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:META-INF/jars/fabric-resource-loader-v0-3.1.6+02ca679649.jar:net/fabricmc/fabric/impl/resource/loader/PlaceholderResourcePack.class */
public final class PlaceholderResourcePack extends Record implements class_3262 {
    private final class_3264 type;
    private final class_9224 metadata;
    private static final class_2561 DESCRIPTION_TEXT = class_2561.method_43471("pack.description.modResources");

    public PlaceholderResourcePack(class_3264 type, class_9224 metadata) {
        this.type = type;
        this.metadata = metadata;
    }

    @Override // java.lang.Record
    public final String toString() {
        return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, PlaceholderResourcePack.class), PlaceholderResourcePack.class, "type;metadata", "FIELD:Lnet/fabricmc/fabric/impl/resource/loader/PlaceholderResourcePack;->type:Lnet/minecraft/class_3264;", "FIELD:Lnet/fabricmc/fabric/impl/resource/loader/PlaceholderResourcePack;->metadata:Lnet/minecraft/class_9224;").dynamicInvoker().invoke(this) /* invoke-custom */;
    }

    @Override // java.lang.Record
    public final int hashCode() {
        return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, PlaceholderResourcePack.class), PlaceholderResourcePack.class, "type;metadata", "FIELD:Lnet/fabricmc/fabric/impl/resource/loader/PlaceholderResourcePack;->type:Lnet/minecraft/class_3264;", "FIELD:Lnet/fabricmc/fabric/impl/resource/loader/PlaceholderResourcePack;->metadata:Lnet/minecraft/class_9224;").dynamicInvoker().invoke(this) /* invoke-custom */;
    }

    @Override // java.lang.Record
    public final boolean equals(Object o) {
        return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, PlaceholderResourcePack.class, Object.class), PlaceholderResourcePack.class, "type;metadata", "FIELD:Lnet/fabricmc/fabric/impl/resource/loader/PlaceholderResourcePack;->type:Lnet/minecraft/class_3264;", "FIELD:Lnet/fabricmc/fabric/impl/resource/loader/PlaceholderResourcePack;->metadata:Lnet/minecraft/class_9224;").dynamicInvoker().invoke(this, o) /* invoke-custom */;
    }

    public class_3264 type() {
        return this.type;
    }

    public class_9224 metadata() {
        return this.metadata;
    }

    public class_3272 getMetadata() {
        return ModResourcePackUtil.getMetadataPack(class_155.method_16673().method_48017(this.type), DESCRIPTION_TEXT);
    }

    @Nullable
    public class_7367<InputStream> method_14410(String... segments) {
        if (segments.length > 0) {
            switch (segments[0]) {
                case "pack.mcmeta":
                    return () -> {
                        DataResult<JsonElement> result = class_3272.field_14202.comp_3437().encodeStart(JsonOps.INSTANCE, getMetadata());
                        String metadata = ((JsonElement) result.getOrThrow()).toString();
                        return IOUtils.toInputStream(metadata, StandardCharsets.UTF_8);
                    };
                case "pack.png":
                    return ModResourcePackUtil::getDefaultIcon;
                default:
                    return null;
            }
        }
        return null;
    }

    @Nullable
    public class_7367<InputStream> method_14405(class_3264 type, class_2960 id) {
        return null;
    }

    public void method_14408(class_3264 type, String namespace, String prefix, class_3262.class_7664 consumer) {
    }

    public Set<String> method_14406(class_3264 type) {
        return Collections.emptySet();
    }

    @Nullable
    public <T> T method_14407(class_7677<T> class_7677Var) {
        return (T) class_7662.method_45174(class_3272.field_14202, getMetadata()).method_45173(class_7677Var);
    }

    public class_9224 method_56926() {
        return this.metadata;
    }

    public String method_14409() {
        return ModResourcePackCreator.FABRIC;
    }

    public void close() {
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:META-INF/jars/fabric-resource-loader-v0-3.1.6+02ca679649.jar:net/fabricmc/fabric/impl/resource/loader/PlaceholderResourcePack$Factory.class */
    public static final class Factory extends Record implements class_3288.class_7680 {
        private final class_3264 type;
        private final class_9224 metadata;

        public Factory(class_3264 type, class_9224 metadata) {
            this.type = type;
            this.metadata = metadata;
        }

        @Override // java.lang.Record
        public final String toString() {
            return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, Factory.class), Factory.class, "type;metadata", "FIELD:Lnet/fabricmc/fabric/impl/resource/loader/PlaceholderResourcePack$Factory;->type:Lnet/minecraft/class_3264;", "FIELD:Lnet/fabricmc/fabric/impl/resource/loader/PlaceholderResourcePack$Factory;->metadata:Lnet/minecraft/class_9224;").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final int hashCode() {
            return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, Factory.class), Factory.class, "type;metadata", "FIELD:Lnet/fabricmc/fabric/impl/resource/loader/PlaceholderResourcePack$Factory;->type:Lnet/minecraft/class_3264;", "FIELD:Lnet/fabricmc/fabric/impl/resource/loader/PlaceholderResourcePack$Factory;->metadata:Lnet/minecraft/class_9224;").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final boolean equals(Object o) {
            return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, Factory.class, Object.class), Factory.class, "type;metadata", "FIELD:Lnet/fabricmc/fabric/impl/resource/loader/PlaceholderResourcePack$Factory;->type:Lnet/minecraft/class_3264;", "FIELD:Lnet/fabricmc/fabric/impl/resource/loader/PlaceholderResourcePack$Factory;->metadata:Lnet/minecraft/class_9224;").dynamicInvoker().invoke(this, o) /* invoke-custom */;
        }

        public class_3264 type() {
            return this.type;
        }

        public class_9224 metadata() {
            return this.metadata;
        }

        public class_3262 method_52424(class_9224 var1) {
            return new PlaceholderResourcePack(this.type, this.metadata);
        }

        public class_3262 method_52425(class_9224 var1, class_3288.class_7679 metadata) {
            return method_52424(var1);
        }
    }
}
