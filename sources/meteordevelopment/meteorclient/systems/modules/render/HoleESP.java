package meteordevelopment.meteorclient.systems.modules.render;

import java.util.ArrayList;
import java.util.List;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.AbstractBlockAccessor;
import meteordevelopment.meteorclient.renderer.Renderer3D;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.misc.Pool;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockIterator;
import meteordevelopment.meteorclient.utils.world.Dir;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2680;
import net.minecraft.class_2818;
import net.minecraft.class_4076;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/HoleESP.class */
public class HoleESP extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgRender;
    private final Setting<Integer> horizontalRadius;
    private final Setting<Integer> verticalRadius;
    private final Setting<Integer> holeHeight;
    private final Setting<Boolean> doubles;
    private final Setting<Boolean> ignoreOwn;
    private final Setting<Boolean> webs;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<Double> height;
    private final Setting<Boolean> topQuad;
    private final Setting<Boolean> bottomQuad;
    private final Setting<SettingColor> bedrockColorTop;
    private final Setting<SettingColor> bedrockColorBottom;
    private final Setting<SettingColor> obsidianColorTop;
    private final Setting<SettingColor> obsidianColorBottom;
    private final Setting<SettingColor> mixedColorTop;
    private final Setting<SettingColor> mixedColorBottom;
    private final Pool<Hole> holePool;
    private final List<Hole> holes;
    private final byte NULL = 0;

    public HoleESP() {
        super(Categories.Render, "hole-esp", "Displays holes that you will take less damage in.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgRender = this.settings.createGroup("Render");
        this.horizontalRadius = this.sgGeneral.add(new IntSetting.Builder().name("horizontal-radius").description("Horizontal radius in which to search for holes.").defaultValue(10).min(0).sliderMax(32).build());
        this.verticalRadius = this.sgGeneral.add(new IntSetting.Builder().name("vertical-radius").description("Vertical radius in which to search for holes.").defaultValue(5).min(0).sliderMax(32).build());
        this.holeHeight = this.sgGeneral.add(new IntSetting.Builder().name("min-height").description("Minimum hole height required to be rendered.").defaultValue(3).min(1).sliderMin(1).build());
        this.doubles = this.sgGeneral.add(new BoolSetting.Builder().name("doubles").description("Highlights double holes that can be stood across.").defaultValue(true).build());
        this.ignoreOwn = this.sgGeneral.add(new BoolSetting.Builder().name("ignore-own").description("Ignores rendering the hole you are currently standing in.").defaultValue(false).build());
        this.webs = this.sgGeneral.add(new BoolSetting.Builder().name("webs").description("Whether to show holes that have webs inside of them.").defaultValue(false).build());
        this.shapeMode = this.sgRender.add(new EnumSetting.Builder().name("shape-mode").description("How the shapes are rendered.").defaultValue(ShapeMode.Both).build());
        this.height = this.sgRender.add(new DoubleSetting.Builder().name("height").description("The height of rendering.").defaultValue(0.2d).min(0.0d).build());
        this.topQuad = this.sgRender.add(new BoolSetting.Builder().name("top-quad").description("Whether to render a quad at the top of the hole.").defaultValue(true).build());
        this.bottomQuad = this.sgRender.add(new BoolSetting.Builder().name("bottom-quad").description("Whether to render a quad at the bottom of the hole.").defaultValue(false).build());
        this.bedrockColorTop = this.sgRender.add(new ColorSetting.Builder().name("bedrock-top").description("The top color for holes that are completely bedrock.").defaultValue(new SettingColor(100, 255, 0, 200)).build());
        this.bedrockColorBottom = this.sgRender.add(new ColorSetting.Builder().name("bedrock-bottom").description("The bottom color for holes that are completely bedrock.").defaultValue(new SettingColor(100, 255, 0, 0)).build());
        this.obsidianColorTop = this.sgRender.add(new ColorSetting.Builder().name("obsidian-top").description("The top color for holes that are completely obsidian.").defaultValue(new SettingColor(255, 0, 0, 200)).build());
        this.obsidianColorBottom = this.sgRender.add(new ColorSetting.Builder().name("obsidian-bottom").description("The bottom color for holes that are completely obsidian.").defaultValue(new SettingColor(255, 0, 0, 0)).build());
        this.mixedColorTop = this.sgRender.add(new ColorSetting.Builder().name("mixed-top").description("The top color for holes that have mixed bedrock and obsidian.").defaultValue(new SettingColor(255, Opcode.LAND, 0, 200)).build());
        this.mixedColorBottom = this.sgRender.add(new ColorSetting.Builder().name("mixed-bottom").description("The bottom color for holes that have mixed bedrock and obsidian.").defaultValue(new SettingColor(255, Opcode.LAND, 0, 0)).build());
        this.holePool = new Pool<>(Hole::new);
        this.holes = new ArrayList();
        this.NULL = (byte) 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        for (Hole hole : this.holes) {
            this.holePool.free(hole);
        }
        this.holes.clear();
        BlockIterator.register(this.horizontalRadius.get().intValue(), this.verticalRadius.get().intValue(), (blockPos, blockState) -> {
            if (validHole(blockPos)) {
                int bedrock = 0;
                int obsidian = 0;
                class_2350 air = null;
                for (class_2350 direction : class_2350.values()) {
                    if (direction != class_2350.field_11036) {
                        class_2338 offsetPos = blockPos.method_10093(direction);
                        class_2680 state = this.mc.field_1687.method_8320(offsetPos);
                        if (state.method_26204() == class_2246.field_9987) {
                            bedrock++;
                        } else if (state.method_26204() == class_2246.field_10540) {
                            obsidian++;
                        } else {
                            if (direction == class_2350.field_11033) {
                                return;
                            }
                            if (this.doubles.get().booleanValue() && air == null && validHole(offsetPos)) {
                                for (class_2350 dir : class_2350.values()) {
                                    if (dir != direction.method_10153() && dir != class_2350.field_11036) {
                                        class_2680 blockState1 = this.mc.field_1687.method_8320(offsetPos.method_10093(dir));
                                        if (blockState1.method_26204() == class_2246.field_9987) {
                                            bedrock++;
                                        } else if (blockState1.method_26204() != class_2246.field_10540) {
                                            return;
                                        } else {
                                            obsidian++;
                                        }
                                    }
                                }
                                air = direction;
                            }
                        }
                    }
                }
                if (obsidian + bedrock == 5 && air == null) {
                    this.holes.add(this.holePool.get().set(blockPos, obsidian == 5 ? Hole.Type.Obsidian : bedrock == 5 ? Hole.Type.Bedrock : Hole.Type.Mixed, (byte) 0));
                } else if (obsidian + bedrock == 8 && this.doubles.get().booleanValue() && air != null) {
                    this.holes.add(this.holePool.get().set(blockPos, obsidian == 8 ? Hole.Type.Obsidian : bedrock == 8 ? Hole.Type.Bedrock : Hole.Type.Mixed, Dir.get(air)));
                }
            }
        });
    }

    private boolean validHole(class_2338 pos) {
        if (this.ignoreOwn.get().booleanValue() && this.mc.field_1724.method_24515().equals(pos)) {
            return false;
        }
        class_2818 chunk = this.mc.field_1687.method_8497(class_4076.method_18675(pos.method_10263()), class_4076.method_18675(pos.method_10260()));
        class_2248 block = chunk.method_8320(pos).method_26204();
        if ((!this.webs.get().booleanValue() && block == class_2246.field_10343) || ((AbstractBlockAccessor) block).isCollidable()) {
            return false;
        }
        for (int i = 0; i < this.holeHeight.get().intValue(); i++) {
            if (chunk.method_8320(pos.method_10086(i)).method_26204().isCollidable()) {
                return false;
            }
        }
        return true;
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        for (Hole hole : this.holes) {
            hole.render(event.renderer, this.shapeMode.get(), this.height.get().doubleValue(), this.topQuad.get().booleanValue(), this.bottomQuad.get().booleanValue());
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/HoleESP$Hole.class */
    private static class Hole {
        public class_2338.class_2339 blockPos = new class_2338.class_2339();
        public byte exclude;
        public Type type;

        /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/HoleESP$Hole$Type.class */
        public enum Type {
            Bedrock,
            Obsidian,
            Mixed
        }

        private Hole() {
        }

        public Hole set(class_2338 blockPos, Type type, byte exclude) {
            this.blockPos.method_10101(blockPos);
            this.exclude = exclude;
            this.type = type;
            return this;
        }

        public Color getTopColor() {
            switch (this.type) {
                case Bedrock:
                    return ((HoleESP) Modules.get().get(HoleESP.class)).bedrockColorTop.get();
                case Obsidian:
                    return ((HoleESP) Modules.get().get(HoleESP.class)).obsidianColorTop.get();
                default:
                    return ((HoleESP) Modules.get().get(HoleESP.class)).mixedColorTop.get();
            }
        }

        public Color getBottomColor() {
            switch (this.type) {
                case Bedrock:
                    return ((HoleESP) Modules.get().get(HoleESP.class)).bedrockColorBottom.get();
                case Obsidian:
                    return ((HoleESP) Modules.get().get(HoleESP.class)).obsidianColorBottom.get();
                default:
                    return ((HoleESP) Modules.get().get(HoleESP.class)).mixedColorBottom.get();
            }
        }

        public void render(Renderer3D renderer, ShapeMode mode, double height, boolean topQuad, boolean bottomQuad) {
            int x = this.blockPos.method_10263();
            int y = this.blockPos.method_10264();
            int z = this.blockPos.method_10260();
            Color top = getTopColor();
            Color bottom = getBottomColor();
            int originalTopA = top.a;
            int originalBottompA = bottom.a;
            if (mode.lines()) {
                if (Dir.isNot(this.exclude, (byte) 32) && Dir.isNot(this.exclude, (byte) 8)) {
                    renderer.line(x, y, z, x, ((double) y) + height, z, bottom, top);
                }
                if (Dir.isNot(this.exclude, (byte) 32) && Dir.isNot(this.exclude, (byte) 16)) {
                    renderer.line(x, y, z + 1, x, ((double) y) + height, z + 1, bottom, top);
                }
                if (Dir.isNot(this.exclude, (byte) 64) && Dir.isNot(this.exclude, (byte) 8)) {
                    renderer.line(x + 1, y, z, x + 1, ((double) y) + height, z, bottom, top);
                }
                if (Dir.isNot(this.exclude, (byte) 64) && Dir.isNot(this.exclude, (byte) 16)) {
                    renderer.line(x + 1, y, z + 1, x + 1, ((double) y) + height, z + 1, bottom, top);
                }
                if (Dir.isNot(this.exclude, (byte) 8)) {
                    renderer.line(x, y, z, x + 1, y, z, bottom);
                }
                if (Dir.isNot(this.exclude, (byte) 8)) {
                    renderer.line(x, ((double) y) + height, z, x + 1, ((double) y) + height, z, top);
                }
                if (Dir.isNot(this.exclude, (byte) 16)) {
                    renderer.line(x, y, z + 1, x + 1, y, z + 1, bottom);
                }
                if (Dir.isNot(this.exclude, (byte) 16)) {
                    renderer.line(x, ((double) y) + height, z + 1, x + 1, ((double) y) + height, z + 1, top);
                }
                if (Dir.isNot(this.exclude, (byte) 32)) {
                    renderer.line(x, y, z, x, y, z + 1, bottom);
                }
                if (Dir.isNot(this.exclude, (byte) 32)) {
                    renderer.line(x, ((double) y) + height, z, x, ((double) y) + height, z + 1, top);
                }
                if (Dir.isNot(this.exclude, (byte) 64)) {
                    renderer.line(x + 1, y, z, x + 1, y, z + 1, bottom);
                }
                if (Dir.isNot(this.exclude, (byte) 64)) {
                    renderer.line(x + 1, ((double) y) + height, z, x + 1, ((double) y) + height, z + 1, top);
                }
            }
            if (mode.sides()) {
                top.a = originalTopA / 2;
                bottom.a = originalBottompA / 2;
                if (Dir.isNot(this.exclude, (byte) 2) && topQuad) {
                    renderer.quad(x, ((double) y) + height, z, x, ((double) y) + height, z + 1, x + 1, ((double) y) + height, z + 1, x + 1, ((double) y) + height, z, top);
                }
                if (Dir.isNot(this.exclude, (byte) 4) && bottomQuad) {
                    renderer.quad(x, y, z, x, y, z + 1, x + 1, y, z + 1, x + 1, y, z, bottom);
                }
                if (Dir.isNot(this.exclude, (byte) 8)) {
                    renderer.gradientQuadVertical(x, y, z, x + 1, ((double) y) + height, z, top, bottom);
                }
                if (Dir.isNot(this.exclude, (byte) 16)) {
                    renderer.gradientQuadVertical(x, y, z + 1, x + 1, ((double) y) + height, z + 1, top, bottom);
                }
                if (Dir.isNot(this.exclude, (byte) 32)) {
                    renderer.gradientQuadVertical(x, y, z, x, ((double) y) + height, z + 1, top, bottom);
                }
                if (Dir.isNot(this.exclude, (byte) 64)) {
                    renderer.gradientQuadVertical(x + 1, y, z, x + 1, ((double) y) + height, z + 1, top, bottom);
                }
                top.a = originalTopA;
                bottom.a = originalBottompA;
            }
        }
    }
}
