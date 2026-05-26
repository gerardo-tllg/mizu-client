package meteordevelopment.meteorclient.systems.modules.world;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.events.entity.player.BlockBreakingCooldownEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockIterator;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1268;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_2382;
import net.minecraft.class_243;
import net.minecraft.class_2846;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/world/Nuker.class */
public class Nuker extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgWhitelist;
    private final SettingGroup sgRender;
    private final Setting<Shape> shape;
    private final Setting<Mode> mode;
    private final Setting<Double> range;
    private final Setting<Integer> range_up;
    private final Setting<Integer> range_down;
    private final Setting<Integer> range_left;
    private final Setting<Integer> range_right;
    private final Setting<Integer> range_forward;
    private final Setting<Integer> range_back;
    private final Setting<Integer> delay;
    private final Setting<Integer> maxBlocksPerTick;
    private final Setting<SortMode> sortMode;
    private final Setting<Boolean> swingHand;
    private final Setting<Boolean> packetMine;
    private final Setting<Boolean> rotate;
    private final Setting<ListMode> listMode;
    private final Setting<List<class_2248>> blacklist;
    private final Setting<List<class_2248>> whitelist;
    private final Setting<Boolean> enableRenderBounding;
    private final Setting<ShapeMode> shapeModeBox;
    private final Setting<SettingColor> sideColorBox;
    private final Setting<SettingColor> lineColorBox;
    private final Setting<Boolean> enableRenderBreaking;
    private final Setting<ShapeMode> shapeModeBreak;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    private final List<class_2338> blocks;
    private boolean firstBlock;
    private final class_2338.class_2339 lastBlockPos;
    private int timer;
    private int noBlockTimer;
    private final class_2338.class_2339 pos1;
    private final class_2338.class_2339 pos2;
    int maxh;
    int maxv;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/world/Nuker$ListMode.class */
    public enum ListMode {
        Whitelist,
        Blacklist
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/world/Nuker$Mode.class */
    public enum Mode {
        All,
        Flatten,
        Smash
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/world/Nuker$Shape.class */
    public enum Shape {
        Cube,
        UniformCube,
        Sphere
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/world/Nuker$SortMode.class */
    public enum SortMode {
        None,
        Closest,
        Furthest,
        TopDown
    }

    public Nuker() {
        super(Categories.World, "nuker", "Breaks blocks around you.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgWhitelist = this.settings.createGroup("Whitelist");
        this.sgRender = this.settings.createGroup("Render");
        this.shape = this.sgGeneral.add(new EnumSetting.Builder().name("shape").description("The shape of nuking algorithm.").defaultValue(Shape.Sphere).build());
        this.mode = this.sgGeneral.add(new EnumSetting.Builder().name("mode").description("The way the blocks are broken.").defaultValue(Mode.Flatten).build());
        this.range = this.sgGeneral.add(new DoubleSetting.Builder().name("range").description("The break range.").defaultValue(4.0d).min(0.0d).visible(() -> {
            return this.shape.get() != Shape.Cube;
        }).build());
        this.range_up = this.sgGeneral.add(new IntSetting.Builder().name("up").description("The break range.").defaultValue(1).min(0).visible(() -> {
            return this.shape.get() == Shape.Cube;
        }).build());
        this.range_down = this.sgGeneral.add(new IntSetting.Builder().name("down").description("The break range.").defaultValue(1).min(0).visible(() -> {
            return this.shape.get() == Shape.Cube;
        }).build());
        this.range_left = this.sgGeneral.add(new IntSetting.Builder().name("left").description("The break range.").defaultValue(1).min(0).visible(() -> {
            return this.shape.get() == Shape.Cube;
        }).build());
        this.range_right = this.sgGeneral.add(new IntSetting.Builder().name("right").description("The break range.").defaultValue(1).min(0).visible(() -> {
            return this.shape.get() == Shape.Cube;
        }).build());
        this.range_forward = this.sgGeneral.add(new IntSetting.Builder().name("forward").description("The break range.").defaultValue(1).min(0).visible(() -> {
            return this.shape.get() == Shape.Cube;
        }).build());
        this.range_back = this.sgGeneral.add(new IntSetting.Builder().name("back").description("The break range.").defaultValue(1).min(0).visible(() -> {
            return this.shape.get() == Shape.Cube;
        }).build());
        this.delay = this.sgGeneral.add(new IntSetting.Builder().name("delay").description("Delay in ticks between breaking blocks.").defaultValue(0).build());
        this.maxBlocksPerTick = this.sgGeneral.add(new IntSetting.Builder().name("max-blocks-per-tick").description("Maximum blocks to try to break per tick. Useful when insta mining.").defaultValue(1).min(1).sliderRange(1, 6).build());
        this.sortMode = this.sgGeneral.add(new EnumSetting.Builder().name("sort-mode").description("The blocks you want to mine first.").defaultValue(SortMode.Closest).build());
        this.swingHand = this.sgGeneral.add(new BoolSetting.Builder().name("swing-hand").description("Swing hand client side.").defaultValue(true).build());
        this.packetMine = this.sgGeneral.add(new BoolSetting.Builder().name("packet-mine").description("Attempt to instamine everything at once.").defaultValue(false).build());
        this.rotate = this.sgGeneral.add(new BoolSetting.Builder().name("rotate").description("Rotates server-side to the block being mined.").defaultValue(true).build());
        this.listMode = this.sgWhitelist.add(new EnumSetting.Builder().name("list-mode").description("Selection mode.").defaultValue(ListMode.Blacklist).build());
        this.blacklist = this.sgWhitelist.add(new BlockListSetting.Builder().name("blacklist").description("The blocks you don't want to mine.").visible(() -> {
            return this.listMode.get() == ListMode.Blacklist;
        }).build());
        this.whitelist = this.sgWhitelist.add(new BlockListSetting.Builder().name("whitelist").description("The blocks you want to mine.").visible(() -> {
            return this.listMode.get() == ListMode.Whitelist;
        }).build());
        this.enableRenderBounding = this.sgRender.add(new BoolSetting.Builder().name("bounding-box").description("Enable rendering bounding box for Cube and Uniform Cube.").defaultValue(true).build());
        this.shapeModeBox = this.sgRender.add(new EnumSetting.Builder().name("nuke-box-mode").description("How the shape for the bounding box is rendered.").defaultValue(ShapeMode.Both).build());
        this.sideColorBox = this.sgRender.add(new ColorSetting.Builder().name("side-color").description("The side color of the bounding box.").defaultValue(new SettingColor(16, Opcode.FMUL, Opcode.D2F, 100)).build());
        this.lineColorBox = this.sgRender.add(new ColorSetting.Builder().name("line-color").description("The line color of the bounding box.").defaultValue(new SettingColor(16, Opcode.FMUL, Opcode.D2F, 255)).build());
        this.enableRenderBreaking = this.sgRender.add(new BoolSetting.Builder().name("broken-blocks").description("Enable rendering bounding box for Cube and Uniform Cube.").defaultValue(true).build());
        SettingGroup settingGroup = this.sgRender;
        EnumSetting.Builder builderDefaultValue = new EnumSetting.Builder().name("nuke-block-mode").description("How the shapes for broken blocks are rendered.").defaultValue(ShapeMode.Both);
        Setting<Boolean> setting = this.enableRenderBreaking;
        Objects.requireNonNull(setting);
        this.shapeModeBreak = settingGroup.add(builderDefaultValue.visible(setting::get).build());
        SettingGroup settingGroup2 = this.sgRender;
        ColorSetting.Builder builderDefaultValue2 = new ColorSetting.Builder().name("side-color").description("The side color of the target block rendering.").defaultValue(new SettingColor(255, 0, 0, 80));
        Setting<Boolean> setting2 = this.enableRenderBreaking;
        Objects.requireNonNull(setting2);
        this.sideColor = settingGroup2.add(builderDefaultValue2.visible(setting2::get).build());
        SettingGroup settingGroup3 = this.sgRender;
        ColorSetting.Builder builderDefaultValue3 = new ColorSetting.Builder().name("line-color").description("The line color of the target block rendering.").defaultValue(new SettingColor(255, 0, 0, 255));
        Setting<Boolean> setting3 = this.enableRenderBreaking;
        Objects.requireNonNull(setting3);
        this.lineColor = settingGroup3.add(builderDefaultValue3.visible(setting3::get).build());
        this.blocks = new ArrayList();
        this.lastBlockPos = new class_2338.class_2339();
        this.pos1 = new class_2338.class_2339();
        this.pos2 = new class_2338.class_2339();
        this.maxh = 0;
        this.maxv = 0;
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        this.firstBlock = true;
        this.timer = 0;
        this.noBlockTimer = 0;
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (this.enableRenderBounding.get().booleanValue() && this.shape.get() != Shape.Sphere && this.mode.get() != Mode.Smash) {
            int minX = Math.min(this.pos1.method_10263(), this.pos2.method_10263());
            int minY = Math.min(this.pos1.method_10264(), this.pos2.method_10264());
            int minZ = Math.min(this.pos1.method_10260(), this.pos2.method_10260());
            int maxX = Math.max(this.pos1.method_10263(), this.pos2.method_10263());
            int maxY = Math.max(this.pos1.method_10264(), this.pos2.method_10264());
            int maxZ = Math.max(this.pos1.method_10260(), this.pos2.method_10260());
            event.renderer.box(minX, minY, minZ, maxX, maxY, maxZ, this.sideColorBox.get(), this.lineColorBox.get(), this.shapeModeBox.get(), 0);
        }
    }

    @EventHandler
    private void onTickPre(TickEvent.Pre event) {
        if (this.timer > 0) {
            this.timer--;
            return;
        }
        double pX = this.mc.field_1724.method_23317();
        double pY = this.mc.field_1724.method_23318();
        double pZ = this.mc.field_1724.method_23321();
        double rangeSq = Math.pow(this.range.get().doubleValue(), 2.0d);
        if (this.shape.get() == Shape.UniformCube) {
            this.range.set(Double.valueOf(Math.round(this.range.get().doubleValue())));
        }
        int r = (int) Math.round(this.range.get().doubleValue());
        if (this.shape.get() == Shape.UniformCube) {
            double pX_ = pX + 1.0d;
            this.pos1.method_10102(pX_ - ((double) r), (pY - ((double) r)) + 1.0d, (pZ - ((double) r)) + 1.0d);
            this.pos2.method_10102((pX_ + ((double) r)) - 1.0d, pY + ((double) r), pZ + ((double) r));
        } else {
            int direction = Math.round((this.mc.field_1724.method_5802().field_1342 % 360.0f) / 90.0f);
            int direction2 = Math.floorMod(direction, 4);
            this.pos1.method_10102(pX - ((double) this.range_forward.get().intValue()), Math.ceil(pY) - ((double) this.range_down.get().intValue()), pZ - ((double) this.range_right.get().intValue()));
            this.pos2.method_10102(pX + ((double) this.range_back.get().intValue()) + 1.0d, Math.ceil(pY + ((double) this.range_up.get().intValue()) + 1.0d), pZ + ((double) this.range_left.get().intValue()) + 1.0d);
            switch (direction2) {
                case 0:
                    double pZ_ = pZ + 1.0d;
                    double pX_2 = pX + 1.0d;
                    this.pos1.method_10102(pX_2 - ((double) (this.range_right.get().intValue() + 1)), Math.ceil(pY) - ((double) this.range_down.get().intValue()), pZ_ - ((double) (this.range_back.get().intValue() + 1)));
                    this.pos2.method_10102(pX_2 + ((double) this.range_left.get().intValue()), Math.ceil(pY + ((double) this.range_up.get().intValue()) + 1.0d), pZ_ + ((double) this.range_forward.get().intValue()));
                    break;
                case 2:
                    double pX_3 = pX + 1.0d;
                    double pZ_2 = pZ + 1.0d;
                    this.pos1.method_10102(pX_3 - ((double) (this.range_left.get().intValue() + 1)), Math.ceil(pY) - ((double) this.range_down.get().intValue()), pZ_2 - ((double) (this.range_forward.get().intValue() + 1)));
                    this.pos2.method_10102(pX_3 + ((double) this.range_right.get().intValue()), Math.ceil(pY + ((double) this.range_up.get().intValue()) + 1.0d), pZ_2 + ((double) this.range_back.get().intValue()));
                    break;
                case 3:
                    double pX_4 = pX + 1.0d;
                    this.pos1.method_10102(pX_4 - ((double) (this.range_back.get().intValue() + 1)), Math.ceil(pY) - ((double) this.range_down.get().intValue()), pZ - ((double) this.range_left.get().intValue()));
                    this.pos2.method_10102(pX_4 + ((double) this.range_forward.get().intValue()), Math.ceil(pY + ((double) this.range_up.get().intValue()) + 1.0d), pZ + ((double) this.range_right.get().intValue()) + 1.0d);
                    break;
            }
            this.maxh = 1 + Math.max(Math.max(Math.max(this.range_back.get().intValue(), this.range_right.get().intValue()), this.range_forward.get().intValue()), this.range_left.get().intValue());
            this.maxv = 1 + Math.max(this.range_up.get().intValue(), this.range_down.get().intValue());
        }
        if (this.mode.get() == Mode.Flatten) {
            this.pos1.method_33098((int) Math.floor(pY));
        }
        class_238 box = new class_238(this.pos1.method_46558(), this.pos2.method_46558());
        BlockIterator.register(Math.max((int) Math.ceil(this.range.get().doubleValue() + 1.0d), this.maxh), Math.max((int) Math.ceil(this.range.get().doubleValue()), this.maxv), (blockPos, blockState) -> {
            switch (this.shape.get()) {
                case Cube:
                    if (!box.method_1006(class_243.method_24953(blockPos))) {
                        return;
                    }
                    break;
                case UniformCube:
                    if (chebyshevDist(this.mc.field_1724.method_24515().method_10263(), this.mc.field_1724.method_24515().method_10264(), this.mc.field_1724.method_24515().method_10260(), blockPos.method_10263(), blockPos.method_10264(), blockPos.method_10260()) >= this.range.get().doubleValue()) {
                        return;
                    }
                    break;
                case Sphere:
                    if (Utils.squaredDistance(pX, pY, pZ, ((double) blockPos.method_10263()) + 0.5d, ((double) blockPos.method_10264()) + 0.5d, ((double) blockPos.method_10260()) + 0.5d) > rangeSq) {
                        return;
                    }
                    break;
            }
            if (BlockUtils.canBreak(blockPos, blockState)) {
                if (this.mode.get() != Mode.Flatten || blockPos.method_10264() >= Math.floor(this.mc.field_1724.method_23318())) {
                    if (this.mode.get() != Mode.Smash || blockState.method_26214(this.mc.field_1687, blockPos) == 0.0f) {
                        if (this.listMode.get() != ListMode.Whitelist || this.whitelist.get().contains(blockState.method_26204())) {
                            if (this.listMode.get() == ListMode.Blacklist && this.blacklist.get().contains(blockState.method_26204())) {
                                return;
                            }
                            this.blocks.add(blockPos.method_10062());
                        }
                    }
                }
            }
        });
        BlockIterator.after(() -> {
            if (this.sortMode.get() == SortMode.TopDown) {
                this.blocks.sort(Comparator.comparingDouble(value -> {
                    return -value.method_10264();
                }));
            } else if (this.sortMode.get() != SortMode.None) {
                this.blocks.sort(Comparator.comparingDouble(value2 -> {
                    return Utils.squaredDistance(pX, pY, pZ, ((double) value2.method_10263()) + 0.5d, ((double) value2.method_10264()) + 0.5d, ((double) value2.method_10260()) + 0.5d) * ((double) (this.sortMode.get() == SortMode.Closest ? 1 : -1));
                }));
            }
            if (this.blocks.isEmpty()) {
                int i = this.noBlockTimer;
                this.noBlockTimer = i + 1;
                if (i >= this.delay.get().intValue()) {
                    this.firstBlock = true;
                    return;
                }
                return;
            }
            this.noBlockTimer = 0;
            if (!this.firstBlock && !this.lastBlockPos.equals(this.blocks.getFirst())) {
                this.timer = this.delay.get().intValue();
                this.firstBlock = false;
                this.lastBlockPos.method_10101((class_2382) this.blocks.getFirst());
                if (this.timer > 0) {
                    return;
                }
            }
            int count = 0;
            for (class_2338 block : this.blocks) {
                if (count >= this.maxBlocksPerTick.get().intValue()) {
                    break;
                }
                boolean canInstaMine = BlockUtils.canInstaBreak(block);
                if (this.rotate.get().booleanValue()) {
                    Rotations.rotate(Rotations.getYaw(block), Rotations.getPitch(block), () -> {
                        breakBlock(block);
                    });
                } else {
                    breakBlock(block);
                }
                if (this.enableRenderBreaking.get().booleanValue()) {
                    RenderUtils.renderTickingBlock(block, this.sideColor.get(), this.lineColor.get(), this.shapeModeBreak.get(), 0, 8, true, false);
                }
                this.lastBlockPos.method_10101(block);
                count++;
                if (!canInstaMine && !this.packetMine.get().booleanValue()) {
                    break;
                }
            }
            this.firstBlock = false;
            this.blocks.clear();
        });
    }

    private void breakBlock(class_2338 blockPos) {
        if (this.packetMine.get().booleanValue()) {
            this.mc.method_1562().method_52787(new class_2846(class_2846.class_2847.field_12968, blockPos, BlockUtils.getDirection(blockPos)));
            this.mc.field_1724.method_6104(class_1268.field_5808);
            this.mc.method_1562().method_52787(new class_2846(class_2846.class_2847.field_12973, blockPos, BlockUtils.getDirection(blockPos)));
            return;
        }
        BlockUtils.breakBlock(blockPos, this.swingHand.get().booleanValue());
    }

    @EventHandler(priority = 200)
    private void onBlockBreakingCooldown(BlockBreakingCooldownEvent event) {
        event.cooldown = 0;
    }

    public static int chebyshevDist(int x1, int y1, int z1, int x2, int y2, int z2) {
        int dX = Math.abs(x2 - x1);
        int dY = Math.abs(y2 - y1);
        int dZ = Math.abs(z2 - z1);
        return Math.max(Math.max(dX, dY), dZ);
    }
}
