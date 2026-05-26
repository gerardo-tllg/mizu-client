package meteordevelopment.meteorclient.mixin;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/ShapeIndexBufferAccessor.class */
@Mixin({RenderSystem.class_5590.class})
public interface ShapeIndexBufferAccessor {
    @Accessor("indexBuffer")
    GpuBuffer getBuffer();
}
