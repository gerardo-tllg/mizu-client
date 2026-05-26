package meteordevelopment.meteorclient.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.class_3298;
import org.apache.commons.io.IOUtils;
import org.joml.Matrix4f;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/renderer/Shader.class */
public class Shader {
    public static Shader BOUND;
    private final int id;
    private final Object2IntMap<String> uniformLocations = new Object2IntOpenHashMap();

    public Shader(String vertPath, String fragPath) {
        int vert = GL.createShader(35633);
        GL.shaderSource(vert, read(vertPath));
        String vertError = GL.compileShader(vert);
        if (vertError != null) {
            MeteorClient.LOG.error("Failed to compile vertex shader (" + vertPath + "): " + vertError);
            throw new RuntimeException("Failed to compile vertex shader (" + vertPath + "): " + vertError);
        }
        int frag = GL.createShader(35632);
        GL.shaderSource(frag, read(fragPath));
        String fragError = GL.compileShader(frag);
        if (fragError != null) {
            MeteorClient.LOG.error("Failed to compile fragment shader (" + fragPath + "): " + fragError);
            throw new RuntimeException("Failed to compile fragment shader (" + fragPath + "): " + fragError);
        }
        this.id = GL.createProgram();
        String programError = GL.linkProgram(this.id, vert, frag);
        if (programError != null) {
            MeteorClient.LOG.error("Failed to link program: " + programError);
            throw new RuntimeException("Failed to link program: " + programError);
        }
        GL.deleteShader(vert);
        GL.deleteShader(frag);
    }

    private String read(String path) {
        try {
            return IOUtils.toString(((class_3298) MeteorClient.mc.method_1478().method_14486(MeteorClient.identifier("shaders/" + path)).get()).method_14482(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read shader '" + path + "'", e);
        }
    }

    public void bind() {
        GL.useProgram(this.id);
        BOUND = this;
    }

    private int getLocation(String name) {
        if (this.uniformLocations.containsKey(name)) {
            return this.uniformLocations.getInt(name);
        }
        int location = GL.getUniformLocation(this.id, name);
        this.uniformLocations.put(name, location);
        return location;
    }

    public void set(String name, boolean v) {
        GL.uniformInt(getLocation(name), v ? 1 : 0);
    }

    public void set(String name, int v) {
        GL.uniformInt(getLocation(name), v);
    }

    public void set(String name, double v) {
        GL.uniformFloat(getLocation(name), (float) v);
    }

    public void set(String name, double v1, double v2) {
        GL.uniformFloat2(getLocation(name), (float) v1, (float) v2);
    }

    public void set(String name, Color color) {
        GL.uniformFloat4(getLocation(name), color.r / 255.0f, color.g / 255.0f, color.b / 255.0f, color.a / 255.0f);
    }

    public void set(String name, Matrix4f mat) {
        GL.uniformMatrix(getLocation(name), mat);
    }

    public void setDefaults() {
        set("u_Proj", RenderSystem.getProjectionMatrix());
        set("u_ModelView", (Matrix4f) RenderSystem.getModelViewStack());
    }
}
