package meteordevelopment.meteorclient.mixin;

import java.util.Optional;
import meteordevelopment.meteorclient.mixininterface.IExplosionS2CPacket;
import net.minecraft.class_243;
import net.minecraft.class_2664;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/ExplosionS2CPacketMixin.class */
@Mixin({class_2664.class})
public abstract class ExplosionS2CPacketMixin implements IExplosionS2CPacket {

    @Shadow
    @Mutable
    @Final
    private Optional<class_243> comp_2884;

    @Override // meteordevelopment.meteorclient.mixininterface.IExplosionS2CPacket
    public void meteor$setVelocityX(float velocity) {
        if (this.comp_2884.isPresent()) {
            class_243 kb = this.comp_2884.get();
            this.comp_2884 = Optional.of(new class_243(velocity, kb.field_1351, kb.field_1350));
        } else {
            this.comp_2884 = Optional.of(new class_243(velocity, 0.0d, 0.0d));
        }
    }

    @Override // meteordevelopment.meteorclient.mixininterface.IExplosionS2CPacket
    public void meteor$setVelocityY(float velocity) {
        if (this.comp_2884.isPresent()) {
            class_243 kb = this.comp_2884.get();
            this.comp_2884 = Optional.of(new class_243(kb.field_1352, velocity, kb.field_1350));
        } else {
            this.comp_2884 = Optional.of(new class_243(0.0d, velocity, 0.0d));
        }
    }

    @Override // meteordevelopment.meteorclient.mixininterface.IExplosionS2CPacket
    public void meteor$setVelocityZ(float velocity) {
        if (this.comp_2884.isPresent()) {
            class_243 kb = this.comp_2884.get();
            this.comp_2884 = Optional.of(new class_243(kb.field_1352, kb.field_1351, velocity));
        } else {
            this.comp_2884 = Optional.of(new class_243(0.0d, 0.0d, velocity));
        }
    }
}
