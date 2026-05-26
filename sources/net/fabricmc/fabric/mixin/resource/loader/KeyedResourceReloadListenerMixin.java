package net.fabricmc.fabric.mixin.resource.loader;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.minecraft.class_1863;
import net.minecraft.class_2960;
import net.minecraft.class_2989;
import net.minecraft.class_5349;
import org.spongepowered.asm.mixin.Mixin;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:META-INF/jars/fabric-resource-loader-v0-3.1.6+02ca679649.jar:net/fabricmc/fabric/mixin/resource/loader/KeyedResourceReloadListenerMixin.class */
@Mixin({class_1863.class, class_2989.class, class_5349.class})
public abstract class KeyedResourceReloadListenerMixin implements IdentifiableResourceReloadListener {
    private class_2960 fabric$id;
    private Collection<class_2960> fabric$dependencies;

    @Override // net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener
    public class_2960 getFabricId() {
        if (this.fabric$id == null) {
            if (this instanceof class_1863) {
                this.fabric$id = ResourceReloadListenerKeys.RECIPES;
            } else if (this instanceof class_2989) {
                this.fabric$id = ResourceReloadListenerKeys.ADVANCEMENTS;
            } else if (this instanceof class_5349) {
                this.fabric$id = ResourceReloadListenerKeys.FUNCTIONS;
            } else {
                this.fabric$id = class_2960.method_60656("private/" + getClass().getSimpleName().toLowerCase(Locale.ROOT));
            }
        }
        return this.fabric$id;
    }

    @Override // net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener
    public Collection<class_2960> getFabricDependencies() {
        if (this.fabric$dependencies == null) {
            this.fabric$dependencies = Collections.emptyList();
        }
        return this.fabric$dependencies;
    }
}
