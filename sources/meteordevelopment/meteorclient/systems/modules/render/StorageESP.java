package meteordevelopment.meteorclient.systems.modules.render;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.events.entity.player.InteractBlockEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.renderer.DrawMode;
import meteordevelopment.meteorclient.renderer.Mesh;
import meteordevelopment.meteorclient.renderer.ShaderMesh;
import meteordevelopment.meteorclient.renderer.Shaders;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StorageBlockListSetting;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.MeshVertexConsumerProvider;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.SimpleBlockRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.render.postprocess.PostProcessShaders;
import meteordevelopment.meteorclient.utils.world.Dir;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_2246;
import net.minecraft.class_2281;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2586;
import net.minecraft.class_2589;
import net.minecraft.class_2591;
import net.minecraft.class_2595;
import net.minecraft.class_2601;
import net.minecraft.class_2609;
import net.minecraft.class_2611;
import net.minecraft.class_2614;
import net.minecraft.class_2627;
import net.minecraft.class_2646;
import net.minecraft.class_2680;
import net.minecraft.class_2745;
import net.minecraft.class_3719;
import net.minecraft.class_7716;
import net.minecraft.class_8172;
import net.minecraft.class_8887;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/StorageESP.class */
public class StorageESP extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgOpened;
    private final Set<class_2338> interactedBlocks;
    public final Setting<Mode> mode;
    private final Setting<List<class_2591<?>>> storageBlocks;
    private final Setting<Boolean> tracers;
    public final Setting<ShapeMode> shapeMode;
    public final Setting<Integer> fillOpacity;
    public final Setting<Integer> outlineWidth;
    public final Setting<Double> glowMultiplier;
    private final Setting<SettingColor> chest;
    private final Setting<SettingColor> trappedChest;
    private final Setting<SettingColor> barrel;
    private final Setting<SettingColor> shulker;
    private final Setting<SettingColor> enderChest;
    private final Setting<SettingColor> other;
    private final Setting<Double> fadeDistance;
    private final Setting<Boolean> hideOpened;
    private final Setting<SettingColor> openedColor;
    private final Color lineColor;
    private final Color sideColor;
    private boolean render;
    private int count;
    private final ShaderMesh mesh;
    private final MeshVertexConsumerProvider vertexConsumerProvider;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/StorageESP$Mode.class */
    public enum Mode {
        Box,
        Shader
    }

    public StorageESP() {
        super(Categories.Render, "storage-esp", "Renders all specified storage blocks.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgOpened = this.settings.createGroup("Opened Rendering");
        this.interactedBlocks = new HashSet();
        this.mode = this.sgGeneral.add(new EnumSetting.Builder().name("mode").description("Rendering mode.").defaultValue(Mode.Shader).build());
        this.storageBlocks = this.sgGeneral.add(new StorageBlockListSetting.Builder().name("storage-blocks").description("Select the storage blocks to display.").defaultValue(StorageBlockListSetting.STORAGE_BLOCKS).build());
        this.tracers = this.sgGeneral.add(new BoolSetting.Builder().name("tracers").description("Draws tracers to storage blocks.").defaultValue(false).build());
        this.shapeMode = this.sgGeneral.add(new EnumSetting.Builder().name("shape-mode").description("How the shapes are rendered.").defaultValue(ShapeMode.Both).build());
        this.fillOpacity = this.sgGeneral.add(new IntSetting.Builder().name("fill-opacity").description("The opacity of the shape fill.").visible(() -> {
            return this.shapeMode.get() != ShapeMode.Lines;
        }).defaultValue(50).range(0, 255).sliderMax(255).build());
        this.outlineWidth = this.sgGeneral.add(new IntSetting.Builder().name("width").description("The width of the shader outline.").visible(() -> {
            return this.mode.get() == Mode.Shader;
        }).defaultValue(1).range(1, 10).sliderRange(1, 5).build());
        this.glowMultiplier = this.sgGeneral.add(new DoubleSetting.Builder().name("glow-multiplier").description("Multiplier for glow effect").visible(() -> {
            return this.mode.get() == Mode.Shader;
        }).decimalPlaces(3).defaultValue(3.5d).min(0.0d).sliderMax(10.0d).build());
        this.chest = this.sgGeneral.add(new ColorSetting.Builder().name("chest").description("The color of chests.").defaultValue(new SettingColor(255, Opcode.IF_ICMPNE, 0, 255)).build());
        this.trappedChest = this.sgGeneral.add(new ColorSetting.Builder().name("trapped-chest").description("The color of trapped chests.").defaultValue(new SettingColor(255, 0, 0, 255)).build());
        this.barrel = this.sgGeneral.add(new ColorSetting.Builder().name("barrel").description("The color of barrels.").defaultValue(new SettingColor(255, Opcode.IF_ICMPNE, 0, 255)).build());
        this.shulker = this.sgGeneral.add(new ColorSetting.Builder().name("shulker").description("The color of Shulker Boxes.").defaultValue(new SettingColor(255, Opcode.IF_ICMPNE, 0, 255)).build());
        this.enderChest = this.sgGeneral.add(new ColorSetting.Builder().name("ender-chest").description("The color of Ender Chests.").defaultValue(new SettingColor(Opcode.ISHL, 0, 255, 255)).build());
        this.other = this.sgGeneral.add(new ColorSetting.Builder().name("other").description("The color of furnaces, dispenders, droppers and hoppers.").defaultValue(new SettingColor(Opcode.F2L, Opcode.F2L, Opcode.F2L, 255)).build());
        this.fadeDistance = this.sgGeneral.add(new DoubleSetting.Builder().name("fade-distance").description("The distance at which the color will fade.").defaultValue(6.0d).min(0.0d).sliderMax(12.0d).build());
        this.hideOpened = this.sgOpened.add(new BoolSetting.Builder().name("hide-opened").description("Hides opened containers.").defaultValue(false).build());
        this.openedColor = this.sgOpened.add(new ColorSetting.Builder().name("opened-color").description("Optional setting to change colors of opened chests, as opposed to not rendering. Disabled at zero opacity.").defaultValue(new SettingColor(203, 90, 203, 0)).build());
        this.lineColor = new Color(0, 0, 0, 0);
        this.sideColor = new Color(0, 0, 0, 0);
        this.mesh = new ShaderMesh(Shaders.POS_COLOR, DrawMode.Triangles, Mesh.Attrib.Vec3, Mesh.Attrib.Color);
        this.vertexConsumerProvider = new MeshVertexConsumerProvider(this.mesh);
    }

    private void getBlockEntityColor(class_2586 blockEntity) {
        this.render = false;
        if (this.storageBlocks.get().contains(blockEntity.method_11017())) {
            if (blockEntity instanceof class_2646) {
                this.lineColor.set((Color) this.trappedChest.get());
            } else if (blockEntity instanceof class_2595) {
                this.lineColor.set((Color) this.chest.get());
            } else if (blockEntity instanceof class_3719) {
                this.lineColor.set((Color) this.barrel.get());
            } else if (blockEntity instanceof class_2627) {
                this.lineColor.set((Color) this.shulker.get());
            } else if (blockEntity instanceof class_2611) {
                this.lineColor.set((Color) this.enderChest.get());
            } else if (!(blockEntity instanceof class_2609) && !(blockEntity instanceof class_2589) && !(blockEntity instanceof class_7716) && !(blockEntity instanceof class_8887) && !(blockEntity instanceof class_2601) && !(blockEntity instanceof class_8172) && !(blockEntity instanceof class_2614)) {
                return;
            } else {
                this.lineColor.set((Color) this.other.get());
            }
            this.render = true;
            if (this.shapeMode.get() == ShapeMode.Sides || this.shapeMode.get() == ShapeMode.Both) {
                this.sideColor.set(this.lineColor);
                this.sideColor.a = this.fillOpacity.get().intValue();
            }
        }
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public WWidget getWidget(GuiTheme theme) {
        WVerticalList list = theme.verticalList();
        WButton clear = (WButton) list.add(theme.button("Clear Rendering Cache")).expandX().widget();
        clear.action = () -> {
            this.interactedBlocks.clear();
        };
        return list;
    }

    @EventHandler
    private void onBlockInteract(InteractBlockEvent event) {
        class_2338 pos = event.result.method_17777();
        class_2595 class_2595VarMethod_8321 = this.mc.field_1687.method_8321(pos);
        if (class_2595VarMethod_8321 == null) {
            return;
        }
        this.interactedBlocks.add(pos);
        if (class_2595VarMethod_8321 instanceof class_2595) {
            class_2595 chestBlockEntity = class_2595VarMethod_8321;
            class_2680 state = chestBlockEntity.method_11010();
            class_2745 chestType = state.method_11654(class_2281.field_10770);
            if (chestType == class_2745.field_12574 || chestType == class_2745.field_12571) {
                class_2350 facing = state.method_11654(class_2281.field_10768);
                class_2338 otherPartPos = pos.method_10093(chestType == class_2745.field_12574 ? facing.method_10170() : facing.method_10160());
                this.interactedBlocks.add(otherPartPos);
            }
        }
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: java.lang.MatchException */
    @EventHandler
    private void onRender(Render3DEvent event) throws MatchException {
        this.count = 0;
        if (this.mode.get() == Mode.Shader) {
            this.mesh.begin();
        }
        for (class_2586 blockEntity : Utils.blockEntities()) {
            boolean interacted = this.interactedBlocks.contains(blockEntity.method_11016());
            if (!interacted || !this.hideOpened.get().booleanValue()) {
                getBlockEntityColor(blockEntity);
                if (interacted && this.openedColor.get().a > 0) {
                    this.lineColor.set((Color) this.openedColor.get());
                    this.sideColor.set((Color) this.openedColor.get());
                    this.sideColor.a = this.fillOpacity.get().intValue();
                }
                if (this.render) {
                    double dist = PlayerUtils.squaredDistanceTo(((double) blockEntity.method_11016().method_10263()) + 0.5d, ((double) blockEntity.method_11016().method_10264()) + 0.5d, ((double) blockEntity.method_11016().method_10260()) + 0.5d);
                    double a = dist <= this.fadeDistance.get().doubleValue() * this.fadeDistance.get().doubleValue() ? dist / (this.fadeDistance.get().doubleValue() * this.fadeDistance.get().doubleValue()) : 1.0d;
                    int prevLineA = this.lineColor.a;
                    int prevSideA = this.sideColor.a;
                    Color color = this.lineColor;
                    color.a = (int) (((double) color.a) * a);
                    Color color2 = this.sideColor;
                    color2.a = (int) (((double) color2.a) * a);
                    if (this.tracers.get().booleanValue() && a >= 0.075d) {
                        event.renderer.line(RenderUtils.center.field_1352, RenderUtils.center.field_1351, RenderUtils.center.field_1350, ((double) blockEntity.method_11016().method_10263()) + 0.5d, ((double) blockEntity.method_11016().method_10264()) + 0.5d, ((double) blockEntity.method_11016().method_10260()) + 0.5d, this.lineColor);
                    }
                    if (this.mode.get() == Mode.Box && a >= 0.075d) {
                        renderBox(event, blockEntity);
                    }
                    this.lineColor.a = prevLineA;
                    this.sideColor.a = prevSideA;
                    if (this.mode.get() == Mode.Shader) {
                        renderShader(event, blockEntity);
                    }
                    this.count++;
                }
            }
        }
        if (this.mode.get() == Mode.Shader) {
            PostProcessShaders.STORAGE_OUTLINE.endRender(() -> {
                this.mesh.render(event.matrices);
            });
        }
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: java.lang.MatchException */
    private void renderBox(Render3DEvent event, class_2586 blockEntity) throws MatchException {
        double x1 = blockEntity.method_11016().method_10263();
        double y1 = blockEntity.method_11016().method_10264();
        double z1 = blockEntity.method_11016().method_10260();
        double x2 = blockEntity.method_11016().method_10263() + 1;
        double y2 = blockEntity.method_11016().method_10264() + 1;
        double z2 = blockEntity.method_11016().method_10260() + 1;
        int excludeDir = 0;
        if (blockEntity instanceof class_2595) {
            class_2680 state = this.mc.field_1687.method_8320(blockEntity.method_11016());
            if ((state.method_26204() == class_2246.field_10034 || state.method_26204() == class_2246.field_10380) && state.method_11654(class_2281.field_10770) != class_2745.field_12569) {
                excludeDir = Dir.get(class_2281.method_9758(state));
            }
        }
        if ((blockEntity instanceof class_2595) || (blockEntity instanceof class_2611)) {
            if (Dir.isNot(excludeDir, (byte) 32)) {
                x1 += 0.0625d;
            }
            if (Dir.isNot(excludeDir, (byte) 8)) {
                z1 += 0.0625d;
            }
            if (Dir.isNot(excludeDir, (byte) 64)) {
                x2 -= 0.0625d;
            }
            y2 -= 0.0625d * 2.0d;
            if (Dir.isNot(excludeDir, (byte) 16)) {
                z2 -= 0.0625d;
            }
        }
        event.renderer.box(x1, y1, z1, x2, y2, z2, this.sideColor, this.lineColor, this.shapeMode.get(), excludeDir);
    }

    private void renderShader(Render3DEvent event, class_2586 blockEntity) {
        this.vertexConsumerProvider.setColor(this.lineColor);
        SimpleBlockRenderer.renderWithBlockEntity(blockEntity, event.tickDelta, this.vertexConsumerProvider);
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public String getInfoString() {
        return Integer.toString(this.count);
    }

    public boolean isShader() {
        return isActive() && this.mode.get() == Mode.Shader;
    }
}
