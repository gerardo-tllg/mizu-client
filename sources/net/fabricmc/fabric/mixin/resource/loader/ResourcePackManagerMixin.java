package net.fabricmc.fabric.mixin.resource.loader;

import com.llamalad7.mixinextras.sugar.Local;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.fabricmc.fabric.impl.resource.loader.FabricResourcePackProfile;
import net.fabricmc.fabric.impl.resource.loader.ModResourcePackCreator;
import net.fabricmc.fabric.impl.resource.loader.ModResourcePackUtil;
import net.minecraft.class_3264;
import net.minecraft.class_3279;
import net.minecraft.class_3283;
import net.minecraft.class_3285;
import net.minecraft.class_3288;
import net.minecraft.class_5352;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:META-INF/jars/fabric-resource-loader-v0-3.1.6+02ca679649.jar:net/fabricmc/fabric/mixin/resource/loader/ResourcePackManagerMixin.class */
@Mixin({class_3283.class})
public abstract class ResourcePackManagerMixin {

    @Unique
    private static final Logger LOGGER = LoggerFactory.getLogger("ResourcePackManagerMixin");

    @Shadow
    @Mutable
    @Final
    public Set<class_3285> field_14227;

    @Shadow
    private Map<String, class_3288> field_14226;

    @Inject(method = {"<init>([Lnet/minecraft/class_3285;)V"}, at = {@At("RETURN")})
    public void construct(class_3285[] resourcePackProviders, CallbackInfo info) {
        this.field_14227 = new LinkedHashSet(this.field_14227);
        boolean shouldAddServerProvider = false;
        Iterator<class_3285> it = this.field_14227.iterator();
        while (it.hasNext()) {
            class_3279 class_3279Var = (class_3285) it.next();
            if ((class_3279Var instanceof class_3279) && (class_3279Var.field_25345 == class_5352.field_25349 || class_3279Var.field_25345 == class_5352.field_25350)) {
                shouldAddServerProvider = true;
                break;
            }
        }
        if (shouldAddServerProvider) {
            this.field_14227.add(new ModResourcePackCreator(class_3264.field_14190));
        }
    }

    @Inject(method = {"method_29208(Ljava/util/Collection;)Ljava/util/List;"}, at = {@At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableList;copyOf(Ljava/util/Collection;)Lcom/google/common/collect/ImmutableList;", shift = At.Shift.BEFORE)})
    private void handleAutoEnableDisable(Collection<String> enabledNames, CallbackInfoReturnable<List<class_3288>> cir, @Local List<class_3288> enabledAfterFirstRun) {
        ModResourcePackUtil.refreshAutoEnabledPacks(enabledAfterFirstRun, this.field_14226);
    }

    @Inject(method = {"method_49427(Ljava/lang/String;)Z"}, at = {@At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", shift = At.Shift.AFTER)})
    private void handleAutoEnable(String profile, CallbackInfoReturnable<Boolean> cir, @Local List<class_3288> newlyEnabled) {
        if (ModResourcePackCreator.POST_CHANGE_HANDLE_REQUIRED.contains(profile)) {
            ModResourcePackUtil.refreshAutoEnabledPacks(newlyEnabled, this.field_14226);
        }
    }

    @Inject(method = {"method_49428(Ljava/lang/String;)Z"}, at = {@At(value = "INVOKE", target = "Ljava/util/List;remove(Ljava/lang/Object;)Z")})
    private void handleAutoDisable(String profile, CallbackInfoReturnable<Boolean> cir, @Local List<class_3288> enabled) {
        if (ModResourcePackCreator.POST_CHANGE_HANDLE_REQUIRED.contains(profile)) {
            Set<String> currentlyEnabled = (Set) enabled.stream().map((v0) -> {
                return v0.method_14463();
            }).collect(Collectors.toSet());
            enabled.removeIf(p -> {
                return !((FabricResourcePackProfile) p).fabric_parentsEnabled(currentlyEnabled);
            });
            LOGGER.debug("[Fabric] Internal pack auto-removed upon disabling {}, result: {}", profile, enabled.stream().map((v0) -> {
                return v0.method_14463();
            }).toList());
        }
    }
}
