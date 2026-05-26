package meteordevelopment.meteorclient.systems.modules.hunting;

import java.util.ArrayList;
import java.util.List;
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
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1268;
import net.minecraft.class_1747;
import net.minecraft.class_1802;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_2846;
import net.minecraft.class_2885;
import net.minecraft.class_3965;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/hunting/AutoPortal.class */
public class AutoPortal extends Module {
    private final SettingGroup sgGeneral;
    private final List<class_2338> waitingForBreak;
    private final Setting<Integer> placeDelay;
    private final Setting<Integer> blocksPerTick;
    private final Setting<Boolean> render;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    private final List<class_2338> portalBlocks;
    private int delay;
    private int index;

    public AutoPortal() {
        super(Categories.Hunting, "auto-portal", "For the Base Hunter who has places to be.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.waitingForBreak = new ArrayList();
        this.placeDelay = this.sgGeneral.add(new IntSetting.Builder().name("place-delay").description("Ticks between each obsidian placement.").defaultValue(1).sliderRange(1, 20).build());
        this.blocksPerTick = this.sgGeneral.add(new IntSetting.Builder().name("blocks-per-tick").description("How many blocks to place each tick.").defaultValue(1).sliderRange(1, 5).build());
        this.render = this.sgGeneral.add(new BoolSetting.Builder().name("render").description("Renders the portal frame as it's being placed.").defaultValue(true).build());
        this.shapeMode = this.sgGeneral.add(new EnumSetting.Builder().name("shape-mode").description("How the box is rendered.").defaultValue(ShapeMode.Both).build());
        this.sideColor = this.sgGeneral.add(new ColorSetting.Builder().name("side-color").defaultValue(new SettingColor(100, 100, 255, 10)).build());
        this.lineColor = this.sgGeneral.add(new ColorSetting.Builder().name("line-color").defaultValue(new SettingColor(100, 100, 255, 255)).build());
        this.portalBlocks = new ArrayList();
        this.delay = 0;
        this.index = 0;
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) {
            toggle();
            return;
        }
        int obsidianCount = 0;
        for (int i = 0; i < 36; i++) {
            if (this.mc.field_1724.method_31548().method_5438(i).method_7909() == class_1802.field_8281) {
                obsidianCount += this.mc.field_1724.method_31548().method_5438(i).method_7947();
            }
        }
        if (obsidianCount < 10) {
            error("Not enough obsidian to build the portal (need at least 10)!", new Object[0]);
            toggle();
            return;
        }
        this.portalBlocks.clear();
        this.index = 0;
        this.delay = 0;
        class_2350 forward = this.mc.field_1724.method_5735();
        class_2350 right = forward.method_10170();
        class_2338 standingPos = this.mc.field_1724.method_24515();
        class_2338 blockBelow = standingPos.method_10074();
        double blockHeight = this.mc.field_1687.method_8320(blockBelow).method_26220(this.mc.field_1687, blockBelow).method_1105(class_2350.class_2351.field_11052);
        if (blockHeight < 1.0d) {
            standingPos = standingPos.method_10084();
        }
        class_2338 base = standingPos.method_10079(forward, 2).method_10079(right, -1);
        int obsidianCheck = 0;
        List<class_2338> checkPositions = List.of(base.method_10079(right, 1), base.method_10079(right, 2), base.method_10079(right, 0).method_10086(1), base.method_10079(right, 0).method_10086(2), base.method_10079(right, 0).method_10086(3), base.method_10079(right, 3).method_10086(1), base.method_10079(right, 3).method_10086(2), base.method_10079(right, 3).method_10086(3), base.method_10079(right, 1).method_10086(4), base.method_10079(right, 2).method_10086(4));
        boolean obstructed = checkPositions.stream().anyMatch(pos -> {
            return !this.mc.field_1687.method_8320(pos).method_45474();
        });
        if (obstructed) {
            error("Portal area obstructed. Move and try again.", new Object[0]);
            this.portalBlocks.clear();
            this.portalBlocks.addAll(checkPositions);
            this.index = checkPositions.size();
            return;
        }
        for (class_2338 checkPos : checkPositions) {
            if (this.mc.field_1687.method_8320(checkPos).method_26204().method_8389() == class_1802.field_8281) {
                obsidianCheck++;
            }
        }
        if (obsidianCheck >= checkPositions.size()) {
            error("A portal already exists here!", new Object[0]);
            toggle();
            return;
        }
        this.portalBlocks.add(base.method_10079(right, 1));
        this.portalBlocks.add(base.method_10079(right, 2));
        for (int i2 = 1; i2 <= 3; i2++) {
            this.portalBlocks.add(base.method_10079(right, 0).method_10086(i2));
        }
        for (int i3 = 1; i3 <= 3; i3++) {
            this.portalBlocks.add(base.method_10079(right, 3).method_10086(i3));
        }
        this.portalBlocks.add(base.method_10079(right, 1).method_10086(4));
        this.portalBlocks.add(base.method_10079(right, 2).method_10086(4));
        for (int i4 = 0; i4 < 9; i4++) {
            if (this.mc.field_1724.method_31548().method_5438(i4).method_7909() == class_1802.field_8281) {
                this.mc.field_1724.method_31548().field_7545 = i4;
                return;
            }
        }
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onDeactivate() {
        this.portalBlocks.clear();
        this.index = 0;
        this.delay = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) {
            return;
        }
        class_1747 class_1747VarMethod_7909 = this.mc.field_1724.method_6047().method_7909();
        if (class_1747VarMethod_7909 instanceof class_1747) {
            class_1747 blockItem = class_1747VarMethod_7909;
            if (blockItem.method_7711().method_8389() != class_1802.field_8281) {
                return;
            }
            if (this.index >= this.portalBlocks.size()) {
                toggle();
                return;
            }
            this.delay++;
            if (this.delay < this.placeDelay.get().intValue()) {
                return;
            }
            int i = 0;
            while (i < this.blocksPerTick.get().intValue() && this.index < this.portalBlocks.size()) {
                class_2338 pos = this.portalBlocks.get(this.index);
                if (!this.mc.field_1687.method_8320(pos).method_45474()) {
                    if (!this.waitingForBreak.contains(pos) && this.mc.field_1687.method_8320(pos).method_26204().method_8389() != class_1802.field_8281 && this.mc.field_1761 != null) {
                        this.mc.field_1761.method_2910(pos, class_2350.field_11036);
                        this.mc.field_1724.method_6104(class_1268.field_5808);
                        this.waitingForBreak.add(pos);
                    }
                    this.index--;
                    return;
                }
                this.waitingForBreak.remove(pos);
                class_3965 bhr = new class_3965(class_243.method_24953(pos), class_2350.field_11036, pos, false);
                int sequence = getSequence();
                this.mc.field_1724.field_3944.method_52787(new class_2846(class_2846.class_2847.field_12969, class_2338.field_10980, class_2350.field_11033));
                this.mc.field_1724.field_3944.method_52787(new class_2885(class_1268.field_5810, bhr, sequence));
                this.mc.field_1724.field_3944.method_52787(new class_2846(class_2846.class_2847.field_12969, class_2338.field_10980, class_2350.field_11033));
                this.mc.field_1724.method_6104(class_1268.field_5808);
                i++;
                this.index++;
            }
            this.delay = 0;
            if (this.index >= this.portalBlocks.size()) {
                int i2 = 0;
                while (true) {
                    if (i2 >= 9) {
                        break;
                    }
                    if (this.mc.field_1724.method_31548().method_5438(i2).method_7909() != class_1802.field_8884) {
                        i2++;
                    } else {
                        this.mc.field_1724.method_31548().field_7545 = i2;
                        class_2338 firePos = this.portalBlocks.get(0).method_10084();
                        class_3965 fireHit = new class_3965(class_243.method_24953(firePos), class_2350.field_11036, firePos, false);
                        this.mc.field_1761.method_2896(this.mc.field_1724, class_1268.field_5808, fireHit);
                        this.mc.field_1724.method_6104(class_1268.field_5808);
                        break;
                    }
                }
                info("Portal complete. AutoPortal disabled.", new Object[0]);
                toggle();
            }
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (this.render.get().booleanValue()) {
            for (int i = this.index; i < this.portalBlocks.size(); i++) {
                class_2338 pos = this.portalBlocks.get(i);
                event.renderer.box(pos, this.sideColor.get(), this.lineColor.get(), this.shapeMode.get(), 0);
            }
        }
    }

    private int getSequence() {
        if (this.mc.field_1687 == null) {
            return 0;
        }
        return this.mc.field_1687.meteor$getAndIncrementSequence();
    }
}
