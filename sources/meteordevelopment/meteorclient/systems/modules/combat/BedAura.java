package meteordevelopment.meteorclient.systems.modules.combat;

import java.util.Objects;
import java.util.function.Predicate;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
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
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.DamageUtils;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.meteorclient.utils.world.CardinalDirection;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1268;
import net.minecraft.class_1657;
import net.minecraft.class_1748;
import net.minecraft.class_1799;
import net.minecraft.class_2244;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_2586;
import net.minecraft.class_2587;
import net.minecraft.class_3965;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/BedAura.class */
public class BedAura extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgTargeting;
    private final SettingGroup sgAutoMove;
    private final SettingGroup sgPause;
    private final SettingGroup sgRender;
    private final Setting<Integer> delay;
    private final Setting<Boolean> strictDirection;
    private final Setting<Double> targetRange;
    private final Setting<SortPriority> priority;
    private final Setting<Double> minDamage;
    private final Setting<Double> maxSelfDamage;
    private final Setting<Boolean> antiSuicide;
    private final Setting<Boolean> autoMove;
    private final Setting<Integer> autoMoveSlot;
    private final Setting<Boolean> autoSwitch;
    private final Setting<Boolean> pauseOnEat;
    private final Setting<Boolean> pauseOnDrink;
    private final Setting<Boolean> pauseOnMine;
    private final Setting<Boolean> swing;
    private final Setting<Boolean> render;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    private CardinalDirection direction;
    private class_1657 target;
    private class_2338 placePos;
    private class_2338 breakPos;
    private int timer;

    public BedAura() {
        super(Categories.Combat, "bed-aura", "Automatically places and explodes beds in the Nether and End.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgTargeting = this.settings.createGroup("Targeting");
        this.sgAutoMove = this.settings.createGroup("Inventory");
        this.sgPause = this.settings.createGroup("Pause");
        this.sgRender = this.settings.createGroup("Render");
        this.delay = this.sgGeneral.add(new IntSetting.Builder().name("delay").description("The delay between placing beds in ticks.").defaultValue(9).min(0).sliderMax(20).build());
        this.strictDirection = this.sgGeneral.add(new BoolSetting.Builder().name("strict-direction").description("Only places beds in the direction you are facing.").defaultValue(false).build());
        this.targetRange = this.sgTargeting.add(new DoubleSetting.Builder().name("target-range").description("The range at which players can be targeted.").defaultValue(4.0d).min(0.0d).sliderMax(5.0d).build());
        this.priority = this.sgTargeting.add(new EnumSetting.Builder().name("target-priority").description("How to filter targets within range.").defaultValue(SortPriority.LowestHealth).build());
        this.minDamage = this.sgTargeting.add(new DoubleSetting.Builder().name("min-damage").description("The minimum damage to inflict on your target.").defaultValue(7.0d).range(0.0d, 36.0d).sliderMax(36.0d).build());
        this.maxSelfDamage = this.sgTargeting.add(new DoubleSetting.Builder().name("max-self-damage").description("The maximum damage to inflict on yourself.").defaultValue(7.0d).range(0.0d, 36.0d).sliderMax(36.0d).build());
        this.antiSuicide = this.sgTargeting.add(new BoolSetting.Builder().name("anti-suicide").description("Will not place and break beds if they will kill you.").defaultValue(true).build());
        this.autoMove = this.sgAutoMove.add(new BoolSetting.Builder().name("auto-move").description("Moves beds into a selected hotbar slot.").defaultValue(false).build());
        SettingGroup settingGroup = this.sgAutoMove;
        IntSetting.Builder builderSliderRange = new IntSetting.Builder().name("auto-move-slot").description("The slot auto move moves beds to.").defaultValue(9).range(1, 9).sliderRange(1, 9);
        Setting<Boolean> setting = this.autoMove;
        Objects.requireNonNull(setting);
        this.autoMoveSlot = settingGroup.add(builderSliderRange.visible(setting::get).build());
        this.autoSwitch = this.sgAutoMove.add(new BoolSetting.Builder().name("auto-switch").description("Switches to and from beds automatically.").defaultValue(true).build());
        this.pauseOnEat = this.sgPause.add(new BoolSetting.Builder().name("pause-on-eat").description("Pauses while eating.").defaultValue(true).build());
        this.pauseOnDrink = this.sgPause.add(new BoolSetting.Builder().name("pause-on-drink").description("Pauses while drinking.").defaultValue(true).build());
        this.pauseOnMine = this.sgPause.add(new BoolSetting.Builder().name("pause-on-mine").description("Pauses while mining.").defaultValue(true).build());
        this.swing = this.sgRender.add(new BoolSetting.Builder().name("swing").description("Whether to swing hand client-side.").defaultValue(true).build());
        this.render = this.sgRender.add(new BoolSetting.Builder().name("render").description("Renders the block where it is placing a bed.").defaultValue(true).build());
        this.shapeMode = this.sgRender.add(new EnumSetting.Builder().name("shape-mode").description("How the shapes are rendered.").defaultValue(ShapeMode.Both).build());
        this.sideColor = this.sgRender.add(new ColorSetting.Builder().name("side-color").description("The side color for positions to be placed.").defaultValue(new SettingColor(15, 255, 211, 75)).build());
        this.lineColor = this.sgRender.add(new ColorSetting.Builder().name("line-color").description("The line color for positions to be placed.").defaultValue(new SettingColor(15, 255, 211)).build());
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        this.timer = this.delay.get().intValue();
        this.direction = CardinalDirection.North;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (this.mc.field_1687.method_8597().comp_648()) {
            error("You can't blow up beds in this dimension, disabling.", new Object[0]);
            toggle();
            return;
        }
        if (PlayerUtils.shouldPause(this.pauseOnMine.get().booleanValue(), this.pauseOnEat.get().booleanValue(), this.pauseOnDrink.get().booleanValue())) {
            return;
        }
        this.target = TargetUtils.getPlayerTarget(this.targetRange.get().doubleValue(), this.priority.get());
        if (this.target == null) {
            this.placePos = null;
            this.breakPos = null;
            return;
        }
        if (this.autoMove.get().booleanValue()) {
            FindItemResult bed = InvUtils.find((Predicate<class_1799>) itemStack -> {
                return itemStack.method_7909() instanceof class_1748;
            });
            if (bed.found() && bed.slot() != this.autoMoveSlot.get().intValue() - 1) {
                InvUtils.move().from(bed.slot()).toHotbar(this.autoMoveSlot.get().intValue() - 1);
            }
        }
        if (this.breakPos == null) {
            this.placePos = findPlace(this.target);
        }
        if (this.timer <= 0 && placeBed(this.placePos)) {
            this.timer = this.delay.get().intValue();
        } else {
            this.timer--;
        }
        if (this.breakPos == null) {
            this.breakPos = findBreak();
        }
        breakBed(this.breakPos);
    }

    private class_2338 findPlace(class_1657 target) {
        if (!InvUtils.find((Predicate<class_1799>) itemStack -> {
            return itemStack.method_7909() instanceof class_1748;
        }).found()) {
            return null;
        }
        int index = 0;
        while (index < 3) {
            int i = index == 0 ? 1 : index == 1 ? 0 : 2;
            for (CardinalDirection dir : CardinalDirection.values()) {
                if (!this.strictDirection.get().booleanValue() || dir.toDirection() == this.mc.field_1724.method_5735() || dir.toDirection().method_10153() == this.mc.field_1724.method_5735()) {
                    class_2338 centerPos = target.method_24515().method_10086(i);
                    float headSelfDamage = DamageUtils.bedDamage(this.mc.field_1724, Utils.vec3d(centerPos));
                    float offsetSelfDamage = DamageUtils.bedDamage(this.mc.field_1724, Utils.vec3d(centerPos.method_10093(dir.toDirection())));
                    if (this.mc.field_1687.method_8320(centerPos).method_45474() && BlockUtils.canPlace(centerPos.method_10093(dir.toDirection())) && DamageUtils.bedDamage(target, Utils.vec3d(centerPos)) >= this.minDamage.get().doubleValue() && offsetSelfDamage < this.maxSelfDamage.get().doubleValue() && headSelfDamage < this.maxSelfDamage.get().doubleValue() && ((!this.antiSuicide.get().booleanValue() || PlayerUtils.getTotalHealth() - headSelfDamage > 0.0f) && (!this.antiSuicide.get().booleanValue() || PlayerUtils.getTotalHealth() - offsetSelfDamage > 0.0f))) {
                        this.direction = dir;
                        return centerPos.method_10093(dir.toDirection());
                    }
                }
            }
            index++;
        }
        return null;
    }

    private class_2338 findBreak() {
        for (class_2586 blockEntity : Utils.blockEntities()) {
            if (blockEntity instanceof class_2587) {
                class_2338 bedPos = blockEntity.method_11016();
                class_243 bedVec = Utils.vec3d(bedPos);
                if (PlayerUtils.isWithinReach(bedVec) && DamageUtils.bedDamage(this.target, bedVec) >= this.minDamage.get().doubleValue() && DamageUtils.bedDamage(this.mc.field_1724, bedVec) < this.maxSelfDamage.get().doubleValue() && (!this.antiSuicide.get().booleanValue() || PlayerUtils.getTotalHealth() - DamageUtils.bedDamage(this.mc.field_1724, bedVec) > 0.0f)) {
                    return bedPos;
                }
            }
        }
        return null;
    }

    private boolean placeBed(class_2338 pos) {
        double d;
        if (pos == null) {
            return false;
        }
        FindItemResult bed = InvUtils.findInHotbar((Predicate<class_1799>) itemStack -> {
            return itemStack.method_7909() instanceof class_1748;
        });
        if (bed.getHand() == null && !this.autoSwitch.get().booleanValue()) {
            return false;
        }
        switch (this.direction) {
            case East:
                d = 90.0d;
                break;
            case South:
                d = 180.0d;
                break;
            case West:
                d = -90.0d;
                break;
            default:
                d = 0.0d;
                break;
        }
        double yaw = d;
        Rotations.rotate(yaw, Rotations.getPitch(pos), () -> {
            BlockUtils.place(pos, bed, false, 0, this.swing.get().booleanValue(), true);
            this.breakPos = pos;
        });
        return true;
    }

    private void breakBed(class_2338 pos) {
        if (pos == null) {
            return;
        }
        this.breakPos = null;
        if (this.mc.field_1687.method_8320(pos).method_26204() instanceof class_2244) {
            boolean wasSneaking = this.mc.field_1724.method_5715();
            if (wasSneaking) {
                this.mc.field_1724.method_5660(false);
            }
            this.mc.field_1761.method_2896(this.mc.field_1724, class_1268.field_5810, new class_3965(class_243.method_24953(pos), class_2350.field_11036, pos, false));
            this.mc.field_1724.method_5660(wasSneaking);
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (this.render.get().booleanValue() && this.placePos != null && this.breakPos == null) {
            int x = this.placePos.method_10263();
            int y = this.placePos.method_10264();
            int z = this.placePos.method_10260();
            switch (this.direction) {
                case East:
                    event.renderer.box(x - 1, y, z, x + 1, ((double) y) + 0.6d, z + 1, this.sideColor.get(), this.lineColor.get(), this.shapeMode.get(), 0);
                    break;
                case South:
                    event.renderer.box(x, y, z - 1, x + 1, ((double) y) + 0.6d, z + 1, this.sideColor.get(), this.lineColor.get(), this.shapeMode.get(), 0);
                    break;
                case West:
                    event.renderer.box(x, y, z, x + 2, ((double) y) + 0.6d, z + 1, this.sideColor.get(), this.lineColor.get(), this.shapeMode.get(), 0);
                    break;
                case North:
                    event.renderer.box(x, y, z, x + 1, ((double) y) + 0.6d, z + 2, this.sideColor.get(), this.lineColor.get(), this.shapeMode.get(), 0);
                    break;
            }
        }
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public String getInfoString() {
        return EntityUtils.getName(this.target);
    }
}
