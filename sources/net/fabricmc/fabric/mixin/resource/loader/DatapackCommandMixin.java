package net.fabricmc.fabric.mixin.resource.loader;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.fabricmc.fabric.impl.resource.loader.FabricResourcePackProfile;
import net.minecraft.class_2168;
import net.minecraft.class_2561;
import net.minecraft.class_3027;
import net.minecraft.class_3283;
import net.minecraft.class_3288;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:META-INF/jars/fabric-resource-loader-v0-3.1.6+02ca679649.jar:net/fabricmc/fabric/mixin/resource/loader/DatapackCommandMixin.class */
@Mixin({class_3027.class})
public class DatapackCommandMixin {

    @Unique
    private static final DynamicCommandExceptionType INTERNAL_PACK_EXCEPTION = new DynamicCommandExceptionType(packName -> {
        return class_2561.method_54159("commands.datapack.fabric.internal", new Object[]{packName});
    });

    @Redirect(method = {"method_13136(Lcom/mojang/brigadier/context/CommandContext;Lcom/mojang/brigadier/suggestion/SuggestionsBuilder;)Ljava/util/concurrent/CompletableFuture;"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/class_3283;method_29210()Ljava/util/Collection;"))
    private static Collection<String> filterEnabledPackSuggestions(class_3283 dataPackManager) {
        return dataPackManager.method_14444().stream().filter(profile -> {
            return !((FabricResourcePackProfile) profile).fabric_isHidden();
        }).map((v0) -> {
            return v0.method_14463();
        }).toList();
    }

    @WrapOperation(method = {"method_13120(Lcom/mojang/brigadier/context/CommandContext;Lcom/mojang/brigadier/suggestion/SuggestionsBuilder;)Ljava/util/concurrent/CompletableFuture;"}, at = {@At(value = "INVOKE", target = "Ljava/util/stream/Stream;filter(Ljava/util/function/Predicate;)Ljava/util/stream/Stream;", ordinal = 0)})
    private static Stream<class_3288> filterDisabledPackSuggestions(Stream<class_3288> instance, Predicate<? super class_3288> predicate, Operation<Stream<class_3288>> original) {
        return ((Stream) original.call(new Object[]{instance, predicate})).filter(profile -> {
            return !((FabricResourcePackProfile) profile).fabric_isHidden();
        });
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: com.mojang.brigadier.exceptions.CommandSyntaxException */
    @Inject(method = {"method_13127(Lcom/mojang/brigadier/context/CommandContext;Ljava/lang/String;Z)Lnet/minecraft/class_3288;"}, at = {@At(value = "INVOKE", target = "Ljava/util/Collection;contains(Ljava/lang/Object;)Z", shift = At.Shift.BEFORE)})
    private static void errorOnInternalPack(CommandContext<class_2168> context, String name, boolean enable, CallbackInfoReturnable<class_3288> cir, @Local class_3288 profile) throws CommandSyntaxException {
        if (((FabricResourcePackProfile) profile).fabric_isHidden()) {
            throw INTERNAL_PACK_EXCEPTION.create(profile.method_14463());
        }
    }
}
