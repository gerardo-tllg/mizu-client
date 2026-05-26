package meteordevelopment.meteorclient.mixin.sodium;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import meteordevelopment.meteorclient.utils.render.MeshBuilderVertexConsumerProvider;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.minecraft.class_4588;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/sodium/MeshVertexConsumerMixin.class */
@Mixin(value = {MeshBuilderVertexConsumerProvider.MeshBuilderVertexConsumer.class}, remap = false)
public abstract class MeshVertexConsumerMixin implements class_4588, VertexBufferWriter {
    public void push(MemoryStack stack, long ptr, int count, VertexFormat format) {
        int positionOffset = format.getOffset(VertexFormatElement.POSITION);
        if (positionOffset == -1) {
            return;
        }
        for (int i = 0; i < count; i++) {
            long positionPtr = ptr + (((long) format.getVertexSize()) * ((long) i)) + ((long) positionOffset);
            float x = MemoryUtil.memGetFloat(positionPtr);
            float y = MemoryUtil.memGetFloat(positionPtr + 4);
            float z = MemoryUtil.memGetFloat(positionPtr + 8);
            method_22912(x, y, z);
        }
    }
}
