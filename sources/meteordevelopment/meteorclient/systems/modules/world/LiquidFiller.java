package meteordevelopment.meteorclient.systems.modules.world;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.world.BlockIterator;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1747;
import net.minecraft.class_1799;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_3609;
import net.minecraft.class_3612;
import net.minecraft.class_3959;
import net.minecraft.class_3965;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/world/LiquidFiller.class */
public class LiquidFiller extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgWhitelist;
    private final Setting<PlaceIn> placeInLiquids;
    private final Setting<Shape> shape;
    private final Setting<Double> placeRange;
    private final Setting<Double> placeWallsRange;
    private final Setting<Integer> delay;
    private final Setting<Integer> maxBlocksPerTick;
    private final Setting<SortMode> sortMode;
    private final Setting<Boolean> rotate;
    private final Setting<ListMode> listMode;
    private final Setting<List<class_2248>> whitelist;
    private final Setting<List<class_2248>> blacklist;
    private final List<class_2338.class_2339> blocks;
    private int timer;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/world/LiquidFiller$ListMode.class */
    public enum ListMode {
        Whitelist,
        Blacklist
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/world/LiquidFiller$PlaceIn.class */
    public enum PlaceIn {
        Both,
        Water,
        Lava
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/world/LiquidFiller$Shape.class */
    public enum Shape {
        Sphere,
        UniformCube
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/world/LiquidFiller$SortMode.class */
    public enum SortMode {
        None,
        Closest,
        Furthest,
        TopDown,
        BottomUp
    }

    public LiquidFiller() {
        super(Categories.World, "liquid-filler", "Places blocks inside of liquid source blocks within range of you.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgWhitelist = this.settings.createGroup("Whitelist");
        this.placeInLiquids = this.sgGeneral.add(new EnumSetting.Builder().name("place-in").description("What type of liquids to place in.").defaultValue(PlaceIn.Both).build());
        this.shape = this.sgGeneral.add(new EnumSetting.Builder().name("shape").description("The shape of placing algorithm.").defaultValue(Shape.Sphere).build());
        this.placeRange = this.sgGeneral.add(new DoubleSetting.Builder().name("place-range").description("The range at which blocks can be placed.").defaultValue(4.5d).min(0.0d).sliderMax(6.0d).build());
        this.placeWallsRange = this.sgGeneral.add(new DoubleSetting.Builder().name("walls-range").description("Range in which to place when behind blocks.").defaultValue(4.5d).min(0.0d).sliderMax(6.0d).build());
        this.delay = this.sgGeneral.add(new IntSetting.Builder().name("delay").description("Delay between actions in ticks.").defaultValue(0).min(0).build());
        this.maxBlocksPerTick = this.sgGeneral.add(new IntSetting.Builder().name("max-blocks-per-tick").description("Maximum blocks to try to place per tick.").defaultValue(1).min(1).sliderRange(1, 10).build());
        this.sortMode = this.sgGeneral.add(new EnumSetting.Builder().name("sort-mode").description("The blocks you want to place first.").defaultValue(SortMode.Furthest).build());
        this.rotate = this.sgGeneral.add(new BoolSetting.Builder().name("rotate").description("Automatically rotates towards the space targeted for filling.").defaultValue(true).build());
        this.listMode = this.sgWhitelist.add(new EnumSetting.Builder().name("list-mode").description("Selection mode.").defaultValue(ListMode.Whitelist).build());
        this.whitelist = this.sgWhitelist.add(new BlockListSetting.Builder().name("whitelist").description("The allowed blocks that it will use to fill up the liquid.").defaultValue(class_2246.field_10566, class_2246.field_10445, class_2246.field_10340, class_2246.field_10515, class_2246.field_10508, class_2246.field_10474, class_2246.field_10115).visible(() -> {
            return this.listMode.get() == ListMode.Whitelist;
        }).build());
        this.blacklist = this.sgWhitelist.add(new BlockListSetting.Builder().name("blacklist").description("The denied blocks that it not will use to fill up the liquid.").visible(() -> {
            return this.listMode.get() == ListMode.Blacklist;
        }).build());
        this.blocks = new ArrayList();
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        this.timer = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        FindItemResult item;
        if (this.timer < this.delay.get().intValue()) {
            this.timer++;
            return;
        }
        this.timer = 0;
        double pX = this.mc.field_1724.method_23317();
        double pY = this.mc.field_1724.method_23318();
        double pZ = this.mc.field_1724.method_23321();
        if (this.listMode.get() == ListMode.Whitelist) {
            item = InvUtils.findInHotbar((Predicate<class_1799>) itemStack -> {
                return (itemStack.method_7909() instanceof class_1747) && this.whitelist.get().contains(class_2248.method_9503(itemStack.method_7909()));
            });
        } else {
            item = InvUtils.findInHotbar((Predicate<class_1799>) itemStack2 -> {
                return (itemStack2.method_7909() instanceof class_1747) && !this.blacklist.get().contains(class_2248.method_9503(itemStack2.method_7909()));
            });
        }
        if (item.found()) {
            BlockIterator.register((int) Math.ceil(this.placeRange.get().doubleValue() + 1.0d), (int) Math.ceil(this.placeRange.get().doubleValue()), (blockPos, blockState) -> {
                if (isOutOfRange(blockPos)) {
                    return;
                }
                class_3609 class_3609VarMethod_15772 = blockState.method_26227().method_15772();
                if (this.placeInLiquids.get() != PlaceIn.Both || class_3609VarMethod_15772 == class_3612.field_15910 || class_3609VarMethod_15772 == class_3612.field_15908) {
                    if (this.placeInLiquids.get() != PlaceIn.Water || class_3609VarMethod_15772 == class_3612.field_15910) {
                        if ((this.placeInLiquids.get() != PlaceIn.Lava || class_3609VarMethod_15772 == class_3612.field_15908) && BlockUtils.canPlace(blockPos)) {
                            this.blocks.add(blockPos.method_25503());
                        }
                    }
                }
            });
            FindItemResult findItemResult = item;
            BlockIterator.after(() -> {
                if (this.sortMode.get() == SortMode.TopDown || this.sortMode.get() == SortMode.BottomUp) {
                    this.blocks.sort(Comparator.comparingDouble(value -> {
                        return value.method_10264() * (this.sortMode.get() == SortMode.BottomUp ? 1 : -1);
                    }));
                } else if (this.sortMode.get() != SortMode.None) {
                    this.blocks.sort(Comparator.comparingDouble(value2 -> {
                        return Utils.squaredDistance(pX, pY, pZ, ((double) value2.method_10263()) + 0.5d, ((double) value2.method_10264()) + 0.5d, ((double) value2.method_10260()) + 0.5d) * ((double) (this.sortMode.get() == SortMode.Closest ? 1 : -1));
                    }));
                }
                int count = 0;
                for (class_2338 pos : this.blocks) {
                    if (count >= this.maxBlocksPerTick.get().intValue()) {
                        break;
                    }
                    BlockUtils.place(pos, findItemResult, this.rotate.get().booleanValue(), 0, true);
                    count++;
                }
                this.blocks.clear();
            });
        }
    }

    private boolean isOutOfRange(class_2338 blockPos) {
        if (!isWithinShape(blockPos, this.placeRange.get().doubleValue())) {
            return true;
        }
        class_3959 raycastContext = new class_3959(this.mc.field_1724.method_33571(), blockPos.method_46558(), class_3959.class_3960.field_17558, class_3959.class_242.field_1348, this.mc.field_1724);
        class_3965 result = this.mc.field_1687.method_17742(raycastContext);
        return (result == null || !result.method_17777().equals(blockPos)) && !isWithinShape(blockPos, this.placeWallsRange.get().doubleValue());
    }

    private boolean isWithinShape(class_2338 blockPos, double range) {
        if (this.shape.get() == Shape.UniformCube) {
            class_2338 playerBlockPos = this.mc.field_1724.method_24515();
            double dX = Math.abs(blockPos.method_10263() - playerBlockPos.method_10263());
            double dY = Math.abs(blockPos.method_10264() - playerBlockPos.method_10264());
            double dZ = Math.abs(blockPos.method_10260() - playerBlockPos.method_10260());
            double maxDist = Math.max(Math.max(dX, dY), dZ);
            return maxDist <= Math.floor(range);
        }
        return PlayerUtils.isWithin(blockPos.method_46558(), range);
    }
}
