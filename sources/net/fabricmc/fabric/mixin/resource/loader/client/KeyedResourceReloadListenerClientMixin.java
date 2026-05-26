package net.fabricmc.fabric.mixin.resource.loader.client;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.minecraft.class_1060;
import net.minecraft.class_1076;
import net.minecraft.class_1092;
import net.minecraft.class_1144;
import net.minecraft.class_2960;
import net.minecraft.class_761;
import net.minecraft.class_776;
import net.minecraft.class_918;
import org.spongepowered.asm.mixin.Mixin;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:META-INF/jars/fabric-resource-loader-v0-3.1.6+02ca679649.jar:net/fabricmc/fabric/mixin/resource/loader/client/KeyedResourceReloadListenerClientMixin.class */
@Mixin({class_1144.class, class_1092.class, class_1076.class, class_1060.class, class_761.class, class_776.class, class_918.class})
@Environment(EnvType.CLIENT)
public abstract class KeyedResourceReloadListenerClientMixin implements IdentifiableResourceReloadListener {
    private class_2960 fabric$id;
    private Collection<class_2960> fabric$dependencies;

    @Override // net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener
    public class_2960 getFabricId() {
        if (this.fabric$id == null) {
            if (this instanceof class_1144) {
                this.fabric$id = ResourceReloadListenerKeys.SOUNDS;
            } else if (this instanceof class_1092) {
                this.fabric$id = ResourceReloadListenerKeys.MODELS;
            } else if (this instanceof class_1076) {
                this.fabric$id = ResourceReloadListenerKeys.LANGUAGES;
            } else if (this instanceof class_1060) {
                this.fabric$id = ResourceReloadListenerKeys.TEXTURES;
            } else {
                this.fabric$id = class_2960.method_60656("private/" + getClass().getSimpleName().toLowerCase(Locale.ROOT));
            }
        }
        return this.fabric$id;
    }

    @Override // net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener
    public Collection<class_2960> getFabricDependencies() {
        if (this.fabric$dependencies == null) {
            if ((this instanceof class_1092) || (this instanceof class_761)) {
                this.fabric$dependencies = Collections.singletonList(ResourceReloadListenerKeys.TEXTURES);
            } else if ((this instanceof class_918) || (this instanceof class_776)) {
                this.fabric$dependencies = Collections.singletonList(ResourceReloadListenerKeys.MODELS);
            } else {
                this.fabric$dependencies = Collections.emptyList();
            }
        }
        return this.fabric$dependencies;
    }
}
