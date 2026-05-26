package meteordevelopment.meteorclient.systems.modules.render;

import java.util.List;
import meteordevelopment.meteorclient.MixinPlugin;
import meteordevelopment.meteorclient.events.render.RenderBlockEntityEvent;
import meteordevelopment.meteorclient.events.world.AmbientOcclusionEvent;
import meteordevelopment.meteorclient.events.world.ChunkOcclusionEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.class_1922;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_259;
import net.minecraft.class_2680;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/Xray.class */
public class Xray extends Module {
    private final SettingGroup sgGeneral;
    public static final List<class_2248> ORES = List.of((Object[]) new class_2248[]{class_2246.field_10418, class_2246.field_29219, class_2246.field_10212, class_2246.field_29027, class_2246.field_10571, class_2246.field_29026, class_2246.field_10090, class_2246.field_29028, class_2246.field_10080, class_2246.field_29030, class_2246.field_10442, class_2246.field_29029, class_2246.field_10013, class_2246.field_29220, class_2246.field_27120, class_2246.field_29221, class_2246.field_23077, class_2246.field_10213, class_2246.field_22109});
    private final Setting<List<class_2248>> blocks;
    public final Setting<Integer> opacity;
    private final Setting<Boolean> exposedOnly;

    public Xray() {
        super(Categories.Render, "xray", "Only renders specified blocks. Good for mining.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.blocks = this.sgGeneral.add(new BlockListSetting.Builder().name("whitelist").description("Which blocks to show x-rayed.").defaultValue(ORES).onChanged(v -> {
            if (isActive()) {
                this.mc.field_1769.method_3279();
            }
        }).build());
        this.opacity = this.sgGeneral.add(new IntSetting.Builder().name("opacity").description("The opacity for all other blocks.").defaultValue(25).range(0, 255).sliderMax(255).onChanged(onChanged -> {
            if (isActive()) {
                this.mc.field_1769.method_3279();
            }
        }).build());
        this.exposedOnly = this.sgGeneral.add(new BoolSetting.Builder().name("exposed-only").description("Show only exposed ores.").defaultValue(false).onChanged(onChanged2 -> {
            if (isActive()) {
                this.mc.field_1769.method_3279();
            }
        }).build());
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        this.mc.field_1769.method_3279();
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onDeactivate() {
        this.mc.field_1769.method_3279();
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public WWidget getWidget(GuiTheme theme) {
        if (MixinPlugin.isSodiumPresent) {
            return theme.label("Warning: Due to Sodium in use, opacity is overridden to 0.");
        }
        if (MixinPlugin.isIrisPresent && IrisApi.getInstance().isShaderPackInUse()) {
            return theme.label("Warning: Due to shaders in use, opacity is overridden to 0.");
        }
        return null;
    }

    @EventHandler
    private void onRenderBlockEntity(RenderBlockEntityEvent event) {
        if (isBlocked(event.blockEntity.method_11010().method_26204(), event.blockEntity.method_11016())) {
            event.cancel();
        }
    }

    @EventHandler
    private void onChunkOcclusion(ChunkOcclusionEvent event) {
        event.cancel();
    }

    @EventHandler
    private void onAmbientOcclusion(AmbientOcclusionEvent event) {
        event.lightLevel = 1.0f;
    }

    public boolean modifyDrawSide(class_2680 state, class_1922 view, class_2338 pos, class_2350 facing, boolean returns) {
        if (!returns && !isBlocked(state.method_26204(), pos)) {
            class_2338 adjPos = pos.method_10093(facing);
            class_2680 adjState = view.method_8320(adjPos);
            return (adjState.method_26173(facing.method_10153()) == class_259.method_1077() && adjState.method_26204() == state.method_26204() && !BlockUtils.isExposed(adjPos)) ? false : true;
        }
        return returns;
    }

    public boolean isBlocked(class_2248 block, class_2338 blockPos) {
        return (this.blocks.get().contains(block) && (!this.exposedOnly.get().booleanValue() || blockPos == null || BlockUtils.isExposed(blockPos))) ? false : true;
    }

    public static int getAlpha(class_2680 state, class_2338 pos) {
        Xray xray = (Xray) Modules.get().get(Xray.class);
        if (xray.isActive() && xray.isBlocked(state.method_26204(), pos)) {
            if (MixinPlugin.isSodiumPresent || (MixinPlugin.isIrisPresent && IrisApi.getInstance().isShaderPackInUse())) {
                return 0;
            }
            return xray.opacity.get().intValue();
        }
        return -1;
    }
}
