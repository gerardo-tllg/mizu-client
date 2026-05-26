package net.fabricmc.fabric.mixin.resource.loader;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.class_2535;
import net.minecraft.class_8609;
import net.minecraft.class_8610;
import net.minecraft.class_8792;
import net.minecraft.class_9226;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:META-INF/jars/fabric-resource-loader-v0-3.1.6+02ca679649.jar:net/fabricmc/fabric/mixin/resource/loader/ServerConfigurationNetworkHandlerMixin.class */
@Mixin({class_8610.class})
public abstract class ServerConfigurationNetworkHandlerMixin extends class_8609 {
    public ServerConfigurationNetworkHandlerMixin(MinecraftServer server, class_2535 connection, class_8792 clientData) {
        super(server, connection, clientData);
    }

    @ModifyArg(method = {"method_52409()V"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/class_9223;<init>(Ljava/util/List;Lnet/minecraft/class_7780;)V", ordinal = 0))
    public List<class_9226> filterKnownPacks(List<class_9226> currentKnownPacks) {
        Stream<class_9226> stream = this.field_45012.fabric_getOriginalKnownPacks().stream();
        Objects.requireNonNull(currentKnownPacks);
        return stream.filter((v1) -> {
            return r1.contains(v1);
        }).toList();
    }
}
