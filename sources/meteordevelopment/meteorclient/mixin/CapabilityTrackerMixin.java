package meteordevelopment.meteorclient.mixin;

import com.mojang.blaze3d.opengl.GlStateManager;
import meteordevelopment.meteorclient.mixininterface.ICapabilityTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/CapabilityTrackerMixin.class */
@Mixin({GlStateManager.class_1018.class})
public abstract class CapabilityTrackerMixin implements ICapabilityTracker {

    @Shadow
    private boolean field_5051;

    @Shadow
    public abstract void method_4470(boolean z);

    @Override // meteordevelopment.meteorclient.mixininterface.ICapabilityTracker
    public boolean meteor$get() {
        return this.field_5051;
    }

    @Override // meteordevelopment.meteorclient.mixininterface.ICapabilityTracker
    public void meteor$set(boolean state) {
        method_4470(state);
    }
}
