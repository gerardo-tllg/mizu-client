package meteordevelopment.meteorclient.systems.modules.world;

import java.util.Objects;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1887;
import net.minecraft.class_1893;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_5321;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/world/EChestFarmer.class */
public class EChestFarmer extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgRender;
    private final Setting<Boolean> selfToggle;
    private final Setting<Boolean> ignoreExisting;
    private final Setting<Integer> amount;
    private final Setting<Boolean> swingHand;
    private final Setting<Boolean> render;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    private final class_265 SHAPE;
    private class_2338 target;
    private int startCount;

    public EChestFarmer() {
        super(Categories.World, "echest-farmer", "Places and breaks EChests to farm obsidian.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgRender = this.settings.createGroup("Render");
        this.selfToggle = this.sgGeneral.add(new BoolSetting.Builder().name("self-toggle").description("Disables when you reach the desired amount of obsidian.").defaultValue(false).build());
        SettingGroup settingGroup = this.sgGeneral;
        BoolSetting.Builder builderDefaultValue = new BoolSetting.Builder().name("ignore-existing").description("Ignores existing obsidian in your inventory and mines the total target amount.").defaultValue(true);
        Setting<Boolean> setting = this.selfToggle;
        Objects.requireNonNull(setting);
        this.ignoreExisting = settingGroup.add(builderDefaultValue.visible(setting::get).build());
        SettingGroup settingGroup2 = this.sgGeneral;
        IntSetting.Builder builderSliderRange = new IntSetting.Builder().name("amount").description("The amount of obsidian to farm.").defaultValue(64).sliderMax(128).range(8, 512).sliderRange(8, 128);
        Setting<Boolean> setting2 = this.selfToggle;
        Objects.requireNonNull(setting2);
        this.amount = settingGroup2.add(builderSliderRange.visible(setting2::get).build());
        this.swingHand = this.sgRender.add(new BoolSetting.Builder().name("swing-hand").description("Swing hand client-side.").defaultValue(true).build());
        this.render = this.sgRender.add(new BoolSetting.Builder().name("render").description("Renders a block overlay where the obsidian will be placed.").defaultValue(true).build());
        this.shapeMode = this.sgRender.add(new EnumSetting.Builder().name("shape-mode").description("How the shapes are rendered.").defaultValue(ShapeMode.Both).build());
        this.sideColor = this.sgRender.add(new ColorSetting.Builder().name("side-color").description("The color of the sides of the blocks being rendered.").defaultValue(new SettingColor(204, 0, 0, 50)).build());
        this.lineColor = this.sgRender.add(new ColorSetting.Builder().name("line-color").description("The color of the lines of the blocks being rendered.").defaultValue(new SettingColor(204, 0, 0, 255)).build());
        this.SHAPE = class_2248.method_9541(1.0d, 0.0d, 1.0d, 15.0d, 14.0d, 15.0d);
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        this.target = null;
        this.startCount = InvUtils.find(class_1802.field_8281).count();
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onDeactivate() {
        InvUtils.swapBack();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.target == null) {
            if (this.mc.field_1765 == null || this.mc.field_1765.method_17783() != class_239.class_240.field_1332) {
                return;
            }
            class_2338 pos = this.mc.field_1765.method_17777().method_10084();
            class_2680 state = this.mc.field_1687.method_8320(pos);
            if (state.method_45474() || state.method_26204() == class_2246.field_10443) {
                this.target = this.mc.field_1765.method_17777().method_10084();
            } else {
                return;
            }
        }
        if (!PlayerUtils.isWithinReach(this.target)) {
            error("Target block pos out of reach.", new Object[0]);
            this.target = null;
            return;
        }
        if (this.selfToggle.get().booleanValue()) {
            if (InvUtils.find(class_1802.field_8281).count() - (this.ignoreExisting.get().booleanValue() ? this.startCount : 0) >= this.amount.get().intValue()) {
                InvUtils.swapBack();
                toggle();
                return;
            }
        }
        if (this.mc.field_1687.method_8320(this.target).method_26204() == class_2246.field_10443) {
            double bestScore = -1.0d;
            int bestSlot = -1;
            for (int i = 0; i < 9; i++) {
                class_1799 itemStack = this.mc.field_1724.method_31548().method_5438(i);
                if (!Utils.hasEnchantment(itemStack, (class_5321<class_1887>) class_1893.field_9099)) {
                    double score = itemStack.method_7924(class_2246.field_10443.method_9564());
                    if (score > bestScore) {
                        bestScore = score;
                        bestSlot = i;
                    }
                }
            }
            if (bestSlot == -1) {
                return;
            }
            InvUtils.swap(bestSlot, true);
            BlockUtils.breakBlock(this.target, this.swingHand.get().booleanValue());
        }
        if (this.mc.field_1687.method_8320(this.target).method_45474()) {
            FindItemResult echest = InvUtils.findInHotbar(class_1802.field_8466);
            if (!echest.found()) {
                error("No Echests in hotbar, disabling", new Object[0]);
                toggle();
            } else {
                BlockUtils.place(this.target, echest, true, 0, true);
            }
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (this.target == null || !this.render.get().booleanValue() || ((PacketMine) Modules.get().get(PacketMine.class)).isMiningBlock(this.target)) {
            return;
        }
        class_238 box = (class_238) this.SHAPE.method_1090().getFirst();
        event.renderer.box(((double) this.target.method_10263()) + box.field_1323, ((double) this.target.method_10264()) + box.field_1322, ((double) this.target.method_10260()) + box.field_1321, ((double) this.target.method_10263()) + box.field_1320, ((double) this.target.method_10264()) + box.field_1325, ((double) this.target.method_10260()) + box.field_1324, this.sideColor.get(), this.lineColor.get(), this.shapeMode.get(), 0);
    }
}
