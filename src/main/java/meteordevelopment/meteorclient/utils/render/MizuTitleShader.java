/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.utils.render;

import meteordevelopment.meteorclient.renderer.FullScreenRenderer;
import meteordevelopment.meteorclient.renderer.MeshRenderer;
import meteordevelopment.meteorclient.renderer.MeteorRenderPipelines;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class MizuTitleShader {
    private static boolean failed = false;

    public static void render(float timeSeconds, int screenWidth, int screenHeight) {
        if (failed || FullScreenRenderer.mesh == null) return;

        try {
            MeshRenderer.begin()
                .attachments(mc.getFramebuffer())
                .pipeline(MeteorRenderPipelines.MIZU_TITLE)
                .mesh(FullScreenRenderer.mesh)
                .setupCallback(pass -> {
                    pass.setUniform("t", timeSeconds);
                    pass.setUniform("res", (float) screenWidth, (float) screenHeight);
                })
                .end();
        } catch (Exception e) {
            failed = true;
        }
    }
}
