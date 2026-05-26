package meteordevelopment.meteorclient.mixin;

import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.GpuTexture;
import meteordevelopment.meteorclient.mixininterface.IGpuTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/GpuTextureMixin.class */
@Mixin({GpuTexture.class})
public abstract class GpuTextureMixin implements IGpuTexture {

    @Shadow(remap = false)
    protected AddressMode addressModeU;

    @Shadow(remap = false)
    protected AddressMode addressModeV;

    @Override // meteordevelopment.meteorclient.mixininterface.IGpuTexture
    public AddressMode meteor$getAddressModeU() {
        return this.addressModeU;
    }

    @Override // meteordevelopment.meteorclient.mixininterface.IGpuTexture
    public AddressMode meteor$getAddressModeV() {
        return this.addressModeV;
    }
}
