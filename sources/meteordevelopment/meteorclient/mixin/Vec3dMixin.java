package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.mixininterface.IVec3d;
import net.minecraft.class_243;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/Vec3dMixin.class */
@Mixin({class_243.class})
public abstract class Vec3dMixin implements IVec3d {

    @Shadow
    @Mutable
    @Final
    public double field_1352;

    @Shadow
    @Mutable
    @Final
    public double field_1351;

    @Shadow
    @Mutable
    @Final
    public double field_1350;

    @Override // meteordevelopment.meteorclient.mixininterface.IVec3d
    public void meteor$set(double x, double y, double z) {
        this.field_1352 = x;
        this.field_1351 = y;
        this.field_1350 = z;
    }

    @Override // meteordevelopment.meteorclient.mixininterface.IVec3d
    public void meteor$setXZ(double x, double z) {
        this.field_1352 = x;
        this.field_1350 = z;
    }

    @Override // meteordevelopment.meteorclient.mixininterface.IVec3d
    public void meteor$setY(double y) {
        this.field_1351 = y;
    }
}
