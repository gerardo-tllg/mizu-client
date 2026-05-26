package meteordevelopment.meteorclient.systems.modules.movement;

import com.google.common.collect.Streams;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import javassist.bytecode.Opcode;
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
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1747;
import net.minecraft.class_1799;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2346;
import net.minecraft.class_238;
import net.minecraft.class_2382;
import net.minecraft.class_243;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/movement/Scaffold.class */
public class Scaffold extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgRender;
    private final Setting<List<class_2248>> blocks;
    private final Setting<ListMode> blocksFilter;
    private final Setting<Boolean> fastTower;
    private final Setting<Double> towerSpeed;
    private final Setting<Boolean> whileMoving;
    private final Setting<Boolean> onlyOnClick;
    private final Setting<Boolean> renderSwing;
    private final Setting<Boolean> autoSwitch;
    private final Setting<Boolean> rotate;
    private final Setting<Boolean> airPlace;
    private final Setting<Double> aheadDistance;
    private final Setting<Double> placeRange;
    private final Setting<Double> radius;
    private final Setting<Integer> blocksPerTick;
    private final Setting<Boolean> render;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    private final class_2338.class_2339 bp;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/movement/Scaffold$ListMode.class */
    public enum ListMode {
        Whitelist,
        Blacklist
    }

    public Scaffold() {
        super(Categories.Movement, "scaffold", "Automatically places blocks under you.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgRender = this.settings.createGroup("Render");
        this.blocks = this.sgGeneral.add(new BlockListSetting.Builder().name("blocks").description("Selected blocks.").build());
        this.blocksFilter = this.sgGeneral.add(new EnumSetting.Builder().name("blocks-filter").description("How to use the block list setting").defaultValue(ListMode.Blacklist).build());
        this.fastTower = this.sgGeneral.add(new BoolSetting.Builder().name("fast-tower").description("Whether or not to scaffold upwards faster.").defaultValue(false).build());
        SettingGroup settingGroup = this.sgGeneral;
        DoubleSetting.Builder builderSliderMax = new DoubleSetting.Builder().name("tower-speed").description("The speed at which to tower.").defaultValue(0.5d).min(0.0d).sliderMax(1.0d);
        Setting<Boolean> setting = this.fastTower;
        Objects.requireNonNull(setting);
        this.towerSpeed = settingGroup.add(builderSliderMax.visible(setting::get).build());
        SettingGroup settingGroup2 = this.sgGeneral;
        BoolSetting.Builder builderDefaultValue = new BoolSetting.Builder().name("while-moving").description("Allows you to tower while moving.").defaultValue(false);
        Setting<Boolean> setting2 = this.fastTower;
        Objects.requireNonNull(setting2);
        this.whileMoving = settingGroup2.add(builderDefaultValue.visible(setting2::get).build());
        this.onlyOnClick = this.sgGeneral.add(new BoolSetting.Builder().name("only-on-click").description("Only places blocks when holding right click.").defaultValue(false).build());
        this.renderSwing = this.sgGeneral.add(new BoolSetting.Builder().name("swing").description("Renders your client-side swing.").defaultValue(false).build());
        this.autoSwitch = this.sgGeneral.add(new BoolSetting.Builder().name("auto-switch").description("Automatically swaps to a block before placing.").defaultValue(true).build());
        this.rotate = this.sgGeneral.add(new BoolSetting.Builder().name("rotate").description("Rotates towards the blocks being placed.").defaultValue(true).build());
        this.airPlace = this.sgGeneral.add(new BoolSetting.Builder().name("air-place").description("Allow air place. This also allows you to modify scaffold radius.").defaultValue(false).build());
        this.aheadDistance = this.sgGeneral.add(new DoubleSetting.Builder().name("ahead-distance").description("How far ahead to place blocks.").defaultValue(0.0d).min(0.0d).sliderMax(1.0d).visible(() -> {
            return !this.airPlace.get().booleanValue();
        }).build());
        this.placeRange = this.sgGeneral.add(new DoubleSetting.Builder().name("closest-block-range").description("How far can scaffold place blocks when you are in air.").defaultValue(4.0d).min(0.0d).sliderMax(8.0d).visible(() -> {
            return !this.airPlace.get().booleanValue();
        }).build());
        SettingGroup settingGroup3 = this.sgGeneral;
        DoubleSetting.Builder builderMax = new DoubleSetting.Builder().name("radius").description("Scaffold radius.").defaultValue(0.0d).min(0.0d).max(6.0d);
        Setting<Boolean> setting3 = this.airPlace;
        Objects.requireNonNull(setting3);
        this.radius = settingGroup3.add(builderMax.visible(setting3::get).build());
        SettingGroup settingGroup4 = this.sgGeneral;
        IntSetting.Builder builderMin = new IntSetting.Builder().name("blocks-per-tick").description("How many blocks to place in one tick.").defaultValue(3).min(1);
        Setting<Boolean> setting4 = this.airPlace;
        Objects.requireNonNull(setting4);
        this.blocksPerTick = settingGroup4.add(builderMin.visible(setting4::get).build());
        this.render = this.sgRender.add(new BoolSetting.Builder().name("render").description("Whether to render blocks that have been placed.").defaultValue(true).build());
        SettingGroup settingGroup5 = this.sgRender;
        EnumSetting.Builder builderDefaultValue2 = new EnumSetting.Builder().name("shape-mode").description("How the shapes are rendered.").defaultValue(ShapeMode.Both);
        Setting<Boolean> setting5 = this.render;
        Objects.requireNonNull(setting5);
        this.shapeMode = settingGroup5.add(builderDefaultValue2.visible(setting5::get).build());
        SettingGroup settingGroup6 = this.sgRender;
        ColorSetting.Builder builderDefaultValue3 = new ColorSetting.Builder().name("side-color").description("The side color of the target block rendering.").defaultValue(new SettingColor(Opcode.MULTIANEWARRAY, Opcode.L2F, 232, 10));
        Setting<Boolean> setting6 = this.render;
        Objects.requireNonNull(setting6);
        this.sideColor = settingGroup6.add(builderDefaultValue3.visible(setting6::get).build());
        SettingGroup settingGroup7 = this.sgRender;
        ColorSetting.Builder builderDefaultValue4 = new ColorSetting.Builder().name("line-color").description("The line color of the target block rendering.").defaultValue(new SettingColor(Opcode.MULTIANEWARRAY, Opcode.L2F, 232));
        Setting<Boolean> setting7 = this.render;
        Objects.requireNonNull(setting7);
        this.lineColor = settingGroup7.add(builderDefaultValue4.visible(setting7::get).build());
        this.bp = new class_2338.class_2339();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!this.onlyOnClick.get().booleanValue() || this.mc.field_1690.field_1904.method_1434()) {
            class_243 vec = this.mc.field_1724.method_19538().method_1019(this.mc.field_1724.method_18798()).method_1031(0.0d, -0.75d, 0.0d);
            if (this.airPlace.get().booleanValue()) {
                this.bp.method_10102(vec.method_10216(), vec.method_10214(), vec.method_10215());
            } else {
                class_243 pos = this.mc.field_1724.method_19538();
                if (this.aheadDistance.get().doubleValue() != 0.0d && !towering() && !this.mc.field_1687.method_8320(this.mc.field_1724.method_24515().method_10074()).method_26220(this.mc.field_1687, this.mc.field_1724.method_24515()).method_1110()) {
                    class_243 dir = class_243.method_1030(0.0f, this.mc.field_1724.method_36454()).method_18805(this.aheadDistance.get().doubleValue(), 0.0d, this.aheadDistance.get().doubleValue());
                    if (this.mc.field_1690.field_1894.method_1434()) {
                        pos = pos.method_1031(dir.field_1352, 0.0d, dir.field_1350);
                    }
                    if (this.mc.field_1690.field_1881.method_1434()) {
                        pos = pos.method_1031(-dir.field_1352, 0.0d, -dir.field_1350);
                    }
                    if (this.mc.field_1690.field_1913.method_1434()) {
                        pos = pos.method_1031(dir.field_1350, 0.0d, -dir.field_1352);
                    }
                    if (this.mc.field_1690.field_1849.method_1434()) {
                        pos = pos.method_1031(-dir.field_1350, 0.0d, dir.field_1352);
                    }
                }
                this.bp.method_10102(pos.field_1352, vec.field_1351, pos.field_1350);
            }
            if (this.mc.field_1690.field_1832.method_1434() && !this.mc.field_1690.field_1903.method_1434() && this.mc.field_1724.method_23318() + vec.field_1351 > -1.0d) {
                this.bp.method_33098(this.bp.method_10264() - 1);
            }
            if (this.bp.method_10264() >= this.mc.field_1724.method_24515().method_10264()) {
                this.bp.method_33098(this.mc.field_1724.method_24515().method_10264() - 1);
            }
            class_2338 targetBlock = this.bp.method_10062();
            if (!this.airPlace.get().booleanValue() && BlockUtils.getPlaceSide(this.bp) == null) {
                this.mc.field_1724.method_19538().method_1031(0.0d, -0.9800000190734863d, 0.0d).method_1019(this.mc.field_1724.method_18798());
                List<class_2338> blockPosArray = new ArrayList<>();
                for (int x = (int) (this.mc.field_1724.method_23317() - this.placeRange.get().doubleValue()); x < this.mc.field_1724.method_23317() + this.placeRange.get().doubleValue(); x++) {
                    for (int z = (int) (this.mc.field_1724.method_23321() - this.placeRange.get().doubleValue()); z < this.mc.field_1724.method_23321() + this.placeRange.get().doubleValue(); z++) {
                        for (int y = (int) Math.max(this.mc.field_1687.method_31607(), this.mc.field_1724.method_23318() - this.placeRange.get().doubleValue()); y < Math.min(320.0d, this.mc.field_1724.method_23318() + this.placeRange.get().doubleValue()); y++) {
                            this.bp.method_10103(x, y, z);
                            if (BlockUtils.getPlaceSide(this.bp) != null && BlockUtils.canPlace(this.bp) && this.mc.field_1724.method_33571().method_1025(class_243.method_24953(this.bp.method_10093(BlockUtils.getClosestPlaceSide(this.bp)))) <= 36.0d) {
                                blockPosArray.add(new class_2338(this.bp));
                            }
                        }
                    }
                }
                if (blockPosArray.isEmpty()) {
                    return;
                }
                blockPosArray.sort(Comparator.comparingDouble(blockPos -> {
                    return blockPos.method_10262(targetBlock);
                }));
                this.bp.method_10101((class_2382) blockPosArray.getFirst());
            }
            if (this.airPlace.get().booleanValue()) {
                List<class_2338> blocks = new ArrayList<>();
                for (int x2 = (int) (((double) this.bp.method_10263()) - this.radius.get().doubleValue()); x2 <= ((double) this.bp.method_10263()) + this.radius.get().doubleValue(); x2++) {
                    for (int z2 = (int) (((double) this.bp.method_10260()) - this.radius.get().doubleValue()); z2 <= ((double) this.bp.method_10260()) + this.radius.get().doubleValue(); z2++) {
                        class_2338 blockPos2 = class_2338.method_49637(x2, this.bp.method_10264(), z2);
                        if (this.mc.field_1724.method_19538().method_1022(class_243.method_24953(blockPos2)) <= this.radius.get().doubleValue() || (x2 == this.bp.method_10263() && z2 == this.bp.method_10260())) {
                            blocks.add(blockPos2);
                        }
                    }
                }
                if (!blocks.isEmpty()) {
                    blocks.sort(Comparator.comparingDouble(PlayerUtils::squaredDistanceTo));
                    int counter = 0;
                    for (class_2338 block : blocks) {
                        if (place(block)) {
                            counter++;
                        }
                        if (counter >= this.blocksPerTick.get().intValue()) {
                            break;
                        }
                    }
                }
            } else {
                place(this.bp);
            }
            FindItemResult result = InvUtils.findInHotbar((Predicate<class_1799>) itemStack -> {
                return validItem(itemStack, this.bp);
            });
            if (this.fastTower.get().booleanValue() && this.mc.field_1690.field_1903.method_1434() && !this.mc.field_1690.field_1832.method_1434() && result.found()) {
                if (this.autoSwitch.get().booleanValue() || result.getHand() != null) {
                    class_243 velocity = this.mc.field_1724.method_18798();
                    class_238 playerBox = this.mc.field_1724.method_5829();
                    if (Streams.stream(this.mc.field_1687.method_20812(this.mc.field_1724, playerBox.method_989(0.0d, 1.0d, 0.0d))).toList().isEmpty()) {
                        if (this.whileMoving.get().booleanValue() || !PlayerUtils.isMoving()) {
                            velocity = new class_243(velocity.field_1352, this.towerSpeed.get().doubleValue(), velocity.field_1350);
                        }
                        this.mc.field_1724.method_18799(velocity);
                        return;
                    }
                    this.mc.field_1724.method_18800(velocity.field_1352, Math.ceil(this.mc.field_1724.method_23318()) - this.mc.field_1724.method_23318(), velocity.field_1350);
                    this.mc.field_1724.method_24830(true);
                }
            }
        }
    }

    public boolean scaffolding() {
        return isActive() && (!this.onlyOnClick.get().booleanValue() || (this.onlyOnClick.get().booleanValue() && this.mc.field_1690.field_1904.method_1434()));
    }

    public boolean towering() {
        FindItemResult result = InvUtils.findInHotbar((Predicate<class_1799>) itemStack -> {
            return validItem(itemStack, this.bp);
        });
        return scaffolding() && this.fastTower.get().booleanValue() && this.mc.field_1690.field_1903.method_1434() && !this.mc.field_1690.field_1832.method_1434() && (this.whileMoving.get().booleanValue() || !PlayerUtils.isMoving()) && result.found() && (this.autoSwitch.get().booleanValue() || result.getHand() != null);
    }

    private boolean validItem(class_1799 itemStack, class_2338 pos) {
        if (!(itemStack.method_7909() instanceof class_1747)) {
            return false;
        }
        class_2248 block = itemStack.method_7909().method_7711();
        if (this.blocksFilter.get() == ListMode.Blacklist && this.blocks.get().contains(block)) {
            return false;
        }
        if ((this.blocksFilter.get() != ListMode.Whitelist || this.blocks.get().contains(block)) && class_2248.method_9614(block.method_9564().method_26220(this.mc.field_1687, pos))) {
            return ((block instanceof class_2346) && class_2346.method_10128(this.mc.field_1687.method_8320(pos))) ? false : true;
        }
        return false;
    }

    private boolean place(class_2338 bp) {
        FindItemResult item = InvUtils.findInHotbar((Predicate<class_1799>) itemStack -> {
            return validItem(itemStack, bp);
        });
        if (!item.found()) {
            return false;
        }
        if ((item.getHand() != null || this.autoSwitch.get().booleanValue()) && BlockUtils.place(bp, item, this.rotate.get().booleanValue(), 50, this.renderSwing.get().booleanValue(), true)) {
            if (this.render.get().booleanValue()) {
                RenderUtils.renderTickingBlock(bp.method_10062(), this.sideColor.get(), this.lineColor.get(), this.shapeMode.get(), 0, 8, true, false);
                return true;
            }
            return true;
        }
        return false;
    }
}
