# Chams Image Shader Crash Fix for 1.21.5

## Problem
When using the Chams module with the "Image" shader mode in Minecraft 1.21.5, the game would crash with:
```
java.lang.NullPointerException: Cannot invoke "net.minecraft.class_276.method_30277()" because "this.framebuffer" is null
    at meteordevelopment.meteorclient.utils.render.postprocess.EntityShader.beginRender(EntityShader.java:14)
```

## Root Cause
The `ChamsShader` class was missing the `init()` call in its constructor. Unlike the other post-process shaders (`EntityOutlineShader` and `StorageOutlineShader`), `ChamsShader` never initialized its framebuffer, leaving it as `null`.

When the rendering system tried to use the framebuffer in `EntityShader.beginRender()`, it would crash because the framebuffer was never created.

## The Fix

### 1. Added init() call to ChamsShader constructor
**File:** `src/main/java/meteordevelopment/meteorclient/utils/render/postprocess/ChamsShader.java`

```java
public ChamsShader() {
    init(MeteorRenderPipelines.POST_IMAGE);  // <-- ADDED THIS LINE
    MeteorClient.EVENT_BUS.subscribe(ChamsShader.class);
}
```

This initializes:
- The `framebuffer` field (creates a new SimpleFramebuffer)
- The `vertexConsumerProvider` field
- The `pipeline` field with `POST_IMAGE` render pipeline

### 2. Added proper import
Added import for `MeteorRenderPipelines` to the ChamsShader class.

### 3. Cleaned up obsolete code
**File:** `src/main/java/meteordevelopment/meteorclient/systems/modules/render/Chams.java`

Removed commented-out code from the `updateShader()` method and added a comment explaining that the render pipeline is now set during initialization and cannot be changed dynamically.

## Why POST_IMAGE Pipeline?

The `MeteorRenderPipelines.POST_IMAGE` pipeline was chosen because:

1. **It has the correct uniforms for Chams:**
   - `u_Texture` - The entity texture
   - `u_TextureI` - The custom image texture
   - `u_Color` - The shader color
   - `u_Size` - Screen size
   - `u_Time` - Animation time

2. **It's specifically designed for image-based post-processing effects**, which is exactly what the Chams "Image" shader mode does.

3. **It matches the commented-out code** that was trying to use an image-based shader.

## How It Works Now

### Initialization Flow:
1. `PostProcessShaders.init()` creates new `ChamsShader()`
2. `ChamsShader()` constructor calls `init(MeteorRenderPipelines.POST_IMAGE)`
3. `PostProcessShader.init()` creates the framebuffer and sets up the rendering pipeline
4. Framebuffer is now ready for use

### Rendering Flow:
1. `PostProcessShaders.beginRender()` calls `ChamsShader.beginRender()`
2. `EntityShader.beginRender()` checks `shouldDraw()` and clears the framebuffer
3. Entities are rendered to the framebuffer
4. `ChamsShader.endRender()` composites the framebuffer with the custom shader

## Comparison with Other Shaders

### EntityOutlineShader (ESP)
```java
public EntityOutlineShader() {
    init(MeteorRenderPipelines.POST_OUTLINE);  // Uses outline pipeline
}
```

### StorageOutlineShader (Storage ESP)
```java
public StorageOutlineShader() {
    init(MeteorRenderPipelines.POST_OUTLINE);  // Uses outline pipeline
}
```

### ChamsShader (NOW FIXED)
```java
public ChamsShader() {
    init(MeteorRenderPipelines.POST_IMAGE);  // Uses image pipeline
    MeteorClient.EVENT_BUS.subscribe(ChamsShader.class);
}
```

All three shaders now properly initialize their framebuffers in the constructor.

## Testing
- ? Code compiles successfully
- ? No more NullPointerException when using Chams with Image shader
- ? Framebuffer is properly initialized before rendering
- ? Follows the same pattern as other post-process shaders

## Additional Notes

### Why the old code was commented out
The old code in `Chams.updateShader()` was trying to dynamically change the shader at runtime:
```java
// OLD (commented out):
PostProcessShaders.CHAMS.init(Utils.titleToName(value.name()));
```

This doesn't work in the new 1.21.5 rendering system because:
1. The `init()` method now takes a `RenderPipeline` object, not a string
2. Render pipelines should be set once during initialization, not changed dynamically
3. The shader behavior is now controlled by `shouldDraw()` and `setupPass()` methods

The new approach is cleaner and more aligned with Minecraft 1.21.5's rendering architecture.
