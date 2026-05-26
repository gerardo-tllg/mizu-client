package meteordevelopment.meteorclient.systems.modules.combat;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockIterator;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.meteorclient.utils.world.Dir;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1297;
import net.minecraft.class_1511;
import net.minecraft.class_1541;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2680;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/HoleFiller.class */
public class HoleFiller extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgSmart;
    private final SettingGroup sgRender;
    private final Setting<List<class_2248>> blocks;
    private final Setting<Integer> searchRadius;
    private final Setting<Double> placeRange;
    private final Setting<Boolean> doubles;
    private final Setting<Boolean> rotate;
    private final Setting<Integer> placeDelay;
    private final Setting<Integer> blocksPerTick;
    private final Setting<Boolean> smart;
    public final Setting<Keybind> forceFill;
    private final Setting<Boolean> predict;
    private final Setting<Boolean> ignoreSafe;
    private final Setting<Boolean> onlyMoving;
    private final Setting<Double> targetRange;
    private final Setting<Double> feetRange;
    private final Setting<Boolean> swing;
    private final Setting<Boolean> render;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    private final Setting<SettingColor> nextSideColor;
    private final Setting<SettingColor> nextLineColor;
    private final List<class_1657> targets;
    private final List<Hole> holes;
    private final class_2338.class_2339 testPos;
    private final class_238 box;
    private int timer;

    public HoleFiller() {
        super(Categories.Combat, "hole-filler", "Fills holes with specified blocks.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgSmart = this.settings.createGroup("Smart");
        this.sgRender = this.settings.createGroup("Render");
        this.blocks = this.sgGeneral.add(new BlockListSetting.Builder().name("blocks").description("Which blocks can be used to fill holes.").defaultValue(class_2246.field_10540, class_2246.field_22423, class_2246.field_22108, class_2246.field_23152, class_2246.field_10343).build());
        this.searchRadius = this.sgGeneral.add(new IntSetting.Builder().name("search-radius").description("Horizontal radius in which to search for holes.").defaultValue(5).min(0).sliderMax(6).build());
        this.placeRange = this.sgGeneral.add(new DoubleSetting.Builder().name("place-range").description("How far away from the player you can place a block.").defaultValue(4.5d).min(0.0d).sliderMax(6.0d).build());
        this.doubles = this.sgGeneral.add(new BoolSetting.Builder().name("doubles").description("Fills double holes.").defaultValue(true).build());
        this.rotate = this.sgGeneral.add(new BoolSetting.Builder().name("rotate").description("Automatically rotates towards the holes being filled.").defaultValue(false).build());
        this.placeDelay = this.sgGeneral.add(new IntSetting.Builder().name("place-delay").description("The ticks delay between placement.").defaultValue(1).min(0).build());
        this.blocksPerTick = this.sgGeneral.add(new IntSetting.Builder().name("blocks-per-tick").description("How many blocks to place in one tick.").defaultValue(3).min(1).build());
        this.smart = this.sgSmart.add(new BoolSetting.Builder().name("smart").description("Take more factors into account before filling a hole.").defaultValue(true).build());
        SettingGroup settingGroup = this.sgSmart;
        KeybindSetting.Builder builderDefaultValue = new KeybindSetting.Builder().name("force-fill").description("Fills all holes around you regardless of target checks.").defaultValue(Keybind.none());
        Setting<Boolean> setting = this.smart;
        Objects.requireNonNull(setting);
        this.forceFill = settingGroup.add(builderDefaultValue.visible(setting::get).build());
        SettingGroup settingGroup2 = this.sgSmart;
        BoolSetting.Builder builderDefaultValue2 = new BoolSetting.Builder().name("predict").description("Predict target movement to account for ping.").defaultValue(true);
        Setting<Boolean> setting2 = this.smart;
        Objects.requireNonNull(setting2);
        this.predict = settingGroup2.add(builderDefaultValue2.visible(setting2::get).build());
        SettingGroup settingGroup3 = this.sgSmart;
        BoolSetting.Builder builderDefaultValue3 = new BoolSetting.Builder().name("ignore-safe").description("Ignore players in safe holes.").defaultValue(true);
        Setting<Boolean> setting3 = this.smart;
        Objects.requireNonNull(setting3);
        this.ignoreSafe = settingGroup3.add(builderDefaultValue3.visible(setting3::get).build());
        SettingGroup settingGroup4 = this.sgSmart;
        BoolSetting.Builder builderDefaultValue4 = new BoolSetting.Builder().name("only-moving").description("Ignore players if they're standing still.").defaultValue(true);
        Setting<Boolean> setting4 = this.smart;
        Objects.requireNonNull(setting4);
        this.onlyMoving = settingGroup4.add(builderDefaultValue4.visible(setting4::get).build());
        SettingGroup settingGroup5 = this.sgSmart;
        DoubleSetting.Builder builderSliderMax = new DoubleSetting.Builder().name("target-range").description("How far away to target players.").defaultValue(7.0d).min(0.0d).sliderMin(1.0d).sliderMax(10.0d);
        Setting<Boolean> setting5 = this.smart;
        Objects.requireNonNull(setting5);
        this.targetRange = settingGroup5.add(builderSliderMax.visible(setting5::get).build());
        SettingGroup settingGroup6 = this.sgSmart;
        DoubleSetting.Builder builderSliderMax2 = new DoubleSetting.Builder().name("feet-range").description("How far from a hole a player's feet must be to fill it.").defaultValue(1.5d).min(0.0d).sliderMax(4.0d);
        Setting<Boolean> setting6 = this.smart;
        Objects.requireNonNull(setting6);
        this.feetRange = settingGroup6.add(builderSliderMax2.visible(setting6::get).build());
        this.swing = this.sgRender.add(new BoolSetting.Builder().name("swing").description("Swing the player's hand when placing.").defaultValue(true).build());
        this.render = this.sgRender.add(new BoolSetting.Builder().name("render").description("Renders an overlay where blocks will be placed.").defaultValue(true).build());
        SettingGroup settingGroup7 = this.sgRender;
        EnumSetting.Builder builderDefaultValue5 = new EnumSetting.Builder().name("shape-mode").description("How the shapes are rendered.").defaultValue(ShapeMode.Both);
        Setting<Boolean> setting7 = this.render;
        Objects.requireNonNull(setting7);
        this.shapeMode = settingGroup7.add(builderDefaultValue5.visible(setting7::get).build());
        this.sideColor = this.sgRender.add(new ColorSetting.Builder().name("side-color").description("The side color of the target block rendering.").defaultValue(new SettingColor(Opcode.MULTIANEWARRAY, Opcode.L2F, 232, 10)).visible(() -> {
            return this.render.get().booleanValue() && this.shapeMode.get().sides();
        }).build());
        this.lineColor = this.sgRender.add(new ColorSetting.Builder().name("line-color").description("The line color of the target block rendering.").defaultValue(new SettingColor(Opcode.MULTIANEWARRAY, Opcode.L2F, 232)).visible(() -> {
            return this.render.get().booleanValue() && this.shapeMode.get().lines();
        }).build());
        this.nextSideColor = this.sgRender.add(new ColorSetting.Builder().name("next-side-color").description("The side color of the next block to be placed.").defaultValue(new SettingColor(227, Opcode.WIDE, 245, 10)).visible(() -> {
            return this.render.get().booleanValue() && this.shapeMode.get().sides();
        }).build());
        this.nextLineColor = this.sgRender.add(new ColorSetting.Builder().name("next-line-color").description("The line color of the next block to be placed.").defaultValue(new SettingColor(227, Opcode.WIDE, 245)).visible(() -> {
            return this.render.get().booleanValue() && this.shapeMode.get().lines();
        }).build());
        this.targets = new ArrayList();
        this.holes = new ArrayList();
        this.testPos = new class_2338.class_2339();
        this.box = new class_238(0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d);
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        this.timer = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.smart.get().booleanValue()) {
            setTargets();
        }
        this.holes.clear();
        FindItemResult block = InvUtils.findInHotbar((Predicate<class_1799>) itemStack -> {
            return this.blocks.get().contains(class_2248.method_9503(itemStack.method_7909()));
        });
        if (block.found()) {
            BlockIterator.register(this.searchRadius.get().intValue(), this.searchRadius.get().intValue(), (blockPos, blockState) -> {
                if (validHole(blockPos)) {
                    int bedrock = 0;
                    int obsidian = 0;
                    class_2350 air = null;
                    for (class_2350 direction : class_2350.values()) {
                        if (direction != class_2350.field_11036) {
                            class_2680 state = this.mc.field_1687.method_8320(blockPos.method_10093(direction));
                            if (state.method_26204() == class_2246.field_9987) {
                                bedrock++;
                            } else if (state.method_26204() == class_2246.field_10540) {
                                obsidian++;
                            } else {
                                if (direction == class_2350.field_11033) {
                                    return;
                                }
                                if (validHole(blockPos.method_10093(direction)) && air == null) {
                                    for (class_2350 dir : class_2350.values()) {
                                        if (dir != direction.method_10153() && dir != class_2350.field_11036) {
                                            class_2680 blockState1 = this.mc.field_1687.method_8320(blockPos.method_10093(direction).method_10093(dir));
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
                            if (obsidian + bedrock == 5 && air == null) {
                                this.holes.add(new Hole(blockPos, (byte) 0));
                            } else if (obsidian + bedrock == 8 && this.doubles.get().booleanValue() && air != null) {
                                this.holes.add(new Hole(blockPos, Dir.get(air)));
                            }
                        }
                    }
                }
            });
            BlockIterator.after(() -> {
                if (this.timer > 0 || this.holes.isEmpty()) {
                    return;
                }
                int bpt = 0;
                for (Hole hole : this.holes) {
                    if (bpt < this.blocksPerTick.get().intValue() && BlockUtils.place(hole.blockPos, block, this.rotate.get().booleanValue(), 10, this.swing.get().booleanValue(), true)) {
                        bpt++;
                    }
                }
                this.timer = this.placeDelay.get().intValue();
            });
            this.timer--;
        }
    }

    @EventHandler(priority = 100)
    private void onRender(Render3DEvent event) {
        if (!this.render.get().booleanValue() || this.holes.isEmpty()) {
            return;
        }
        for (Hole hole : this.holes) {
            boolean isNext = false;
            for (int i = 0; i < this.holes.size(); i++) {
                if (this.holes.get(i).equals(hole) && i < this.blocksPerTick.get().intValue()) {
                    isNext = true;
                }
            }
            Color side = isNext ? this.nextSideColor.get() : this.sideColor.get();
            SettingColor line = isNext ? this.nextLineColor.get() : this.lineColor.get();
            event.renderer.box((class_2338) hole.blockPos, side, (Color) line, this.shapeMode.get(), (int) hole.exclude);
        }
    }

    private boolean validHole(class_2338 pos) {
        this.testPos.method_10101(pos);
        if (this.mc.field_1724.method_24515().equals(this.testPos) || distance(this.mc.field_1724, this.testPos, false) > this.placeRange.get().doubleValue() || this.mc.field_1687.method_8320(this.testPos).method_26204() == class_2246.field_10343 || this.mc.field_1687.method_8320(this.testPos).method_26204().isCollidable()) {
            return false;
        }
        this.testPos.method_10069(0, 1, 0);
        if (this.mc.field_1687.method_8320(this.testPos).method_26204().isCollidable()) {
            return false;
        }
        this.testPos.method_10069(0, -1, 0);
        this.box.meteor$set(pos);
        if (!this.mc.field_1687.method_8333((class_1297) null, this.box, entity -> {
            return (entity instanceof class_1657) || (entity instanceof class_1541) || (entity instanceof class_1511);
        }).isEmpty()) {
            return false;
        }
        if (!this.smart.get().booleanValue() || this.forceFill.get().isPressed()) {
            return true;
        }
        return this.targets.stream().anyMatch(target -> {
            return target.method_23318() > ((double) this.testPos.method_10264()) && distance(target, this.testPos, true) < this.feetRange.get().doubleValue();
        });
    }

    private void setTargets() {
        this.targets.clear();
        for (class_1657 player : this.mc.field_1687.method_18456()) {
            if (player.method_5858(this.mc.field_1724) <= Math.pow(this.targetRange.get().doubleValue(), 2.0d) && !player.method_68878() && player != this.mc.field_1724 && !player.method_29504() && Friends.get().shouldAttack(player) && (!this.ignoreSafe.get().booleanValue() || !isSurrounded(player))) {
                if (!this.onlyMoving.get().booleanValue() || (player.method_18798().field_1352 == 0.0d && player.method_18798().field_1351 == 0.0d && player.method_18798().field_1350 == 0.0d)) {
                    this.targets.add(player);
                }
            }
        }
    }

    private boolean isSurrounded(class_1657 target) {
        for (class_2350 dir : class_2350.values()) {
            if (dir != class_2350.field_11036 && dir != class_2350.field_11033) {
                this.testPos.method_10101(target.method_24515().method_10093(dir));
                class_2248 block = this.mc.field_1687.method_8320(this.testPos).method_26204();
                if (block != class_2246.field_10540 && block != class_2246.field_9987 && block != class_2246.field_23152 && block != class_2246.field_22423 && block != class_2246.field_22108) {
                    return false;
                }
            }
        }
        return true;
    }

    private double distance(class_1657 player, class_2338 pos, boolean feet) {
        class_243 testVec = player.method_19538();
        if (!feet) {
            testVec.method_1031(0.0d, player.method_18381(this.mc.field_1724.method_18376()), 0.0d);
        } else if (this.predict.get().booleanValue()) {
            testVec.method_1031(player.method_18798().field_1352, player.method_18798().field_1351, player.method_18798().field_1350);
        }
        double i = testVec.field_1352 - (((double) pos.method_10263()) + 0.5d);
        double j = testVec.field_1351 - (((double) pos.method_10264()) + (feet ? 1.0d : 0.5d));
        double k = testVec.field_1350 - (((double) pos.method_10260()) + 0.5d);
        return Math.sqrt((i * i) + (j * j) + (k * k));
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/HoleFiller$Hole.class */
    private static class Hole {
        private final class_2338.class_2339 blockPos = new class_2338.class_2339();
        private final byte exclude;

        public Hole(class_2338 blockPos, byte exclude) {
            this.blockPos.method_10101(blockPos);
            this.exclude = exclude;
        }
    }
}
