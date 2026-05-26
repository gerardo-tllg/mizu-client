package meteordevelopment.meteorclient.mixininterface;

import com.mojang.blaze3d.systems.RenderPass;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixininterface/IGpuDevice.class */
public interface IGpuDevice {
    void meteor$pushScissor(int i, int i2, int i3, int i4);

    void meteor$popScissor();

    @Deprecated
    void meteor$onCreateRenderPass(RenderPass renderPass);
}
