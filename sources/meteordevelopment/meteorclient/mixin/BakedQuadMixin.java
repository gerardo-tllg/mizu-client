package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.mixininterface.IBakedQuad;
import net.minecraft.class_777;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/BakedQuadMixin.class */
@Mixin({class_777.class})
public abstract class BakedQuadMixin implements IBakedQuad {

    @Shadow
    @Final
    private int[] comp_3721;

    @Override // meteordevelopment.meteorclient.mixininterface.IBakedQuad
    public float meteor$getX(int vertexI) {
        return Float.intBitsToFloat(this.comp_3721[vertexI * 8]);
    }

    @Override // meteordevelopment.meteorclient.mixininterface.IBakedQuad
    public float meteor$getY(int vertexI) {
        return Float.intBitsToFloat(this.comp_3721[(vertexI * 8) + 1]);
    }

    @Override // meteordevelopment.meteorclient.mixininterface.IBakedQuad
    public float meteor$getZ(int vertexI) {
        return Float.intBitsToFloat(this.comp_3721[(vertexI * 8) + 2]);
    }
}
