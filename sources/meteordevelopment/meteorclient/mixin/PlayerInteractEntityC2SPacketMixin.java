package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.mixininterface.IPlayerInteractEntityC2SPacket;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.NoSlow;
import meteordevelopment.meteorclient.systems.modules.movement.Sneak;
import net.minecraft.class_1297;
import net.minecraft.class_2824;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/PlayerInteractEntityC2SPacketMixin.class */
@Mixin({class_2824.class})
public abstract class PlayerInteractEntityC2SPacketMixin implements IPlayerInteractEntityC2SPacket {

    @Shadow
    @Final
    private class_2824.class_5906 field_12871;

    @Shadow
    @Final
    private int field_12870;

    @Override // meteordevelopment.meteorclient.mixininterface.IPlayerInteractEntityC2SPacket
    public class_2824.class_5907 meteor$getType() {
        return this.field_12871.method_34211();
    }

    @Override // meteordevelopment.meteorclient.mixininterface.IPlayerInteractEntityC2SPacket
    public class_1297 meteor$getEntity() {
        return MeteorClient.mc.field_1687.method_8469(this.field_12870);
    }

    @ModifyVariable(method = {"<init>(IZLnet/minecraft/network/packet/c2s/play/PlayerInteractEntityC2SPacket$InteractTypeHandler;)V"}, at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private static boolean setSneaking(boolean sneaking) {
        return ((Sneak) Modules.get().get(Sneak.class)).doPacket() || ((NoSlow) Modules.get().get(NoSlow.class)).airStrict() || sneaking;
    }
}
