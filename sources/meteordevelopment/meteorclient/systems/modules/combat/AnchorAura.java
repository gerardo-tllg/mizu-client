package meteordevelopment.meteorclient.systems.modules.combat;

import java.util.Objects;
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
import meteordevelopment.meteorclient.utils.entity.DamageUtils;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.player.Safety;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1268;
import net.minecraft.class_1657;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_3965;
import org.jetbrains.annotations.Nullable;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/AnchorAura.class */
public class AnchorAura extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgPlace;
    private final SettingGroup sgBreak;
    private final SettingGroup sgPause;
    private final SettingGroup sgRender;
    private final Setting<Double> targetRange;
    private final Setting<SortPriority> targetPriority;
    private final Setting<RotationMode> rotationMode;
    private final Setting<Double> maxDamage;
    private final Setting<Double> minHealth;
    private final Setting<Boolean> place;
    private final Setting<Integer> placeDelay;
    private final Setting<Safety> placeMode;
    private final Setting<Double> placeRange;
    private final Setting<PlaceMode> placePositions;
    private final Setting<Integer> breakDelay;
    private final Setting<Safety> breakMode;
    private final Setting<Double> breakRange;
    private final Setting<Boolean> pauseOnEat;
    private final Setting<Boolean> pauseOnDrink;
    private final Setting<Boolean> pauseOnMine;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<Boolean> renderPlace;
    private final Setting<SettingColor> placeSideColor;
    private final Setting<SettingColor> placeLineColor;
    private final Setting<Boolean> renderBreak;
    private final Setting<SettingColor> breakSideColor;
    private final Setting<SettingColor> breakLineColor;
    private int placeDelayLeft;
    private int breakDelayLeft;
    private class_1657 target;
    private final class_2338.class_2339 mutable;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/AnchorAura$PlaceMode.class */
    public enum PlaceMode {
        Above,
        Around,
        AboveAndBelow,
        All
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/AnchorAura$RotationMode.class */
    public enum RotationMode {
        Place,
        Break,
        Both,
        None
    }

    public AnchorAura() {
        super(Categories.Combat, "anchor-aura", "Automatically places and breaks Respawn Anchors to harm entities.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgPlace = this.settings.createGroup("Place");
        this.sgBreak = this.settings.createGroup("Break");
        this.sgPause = this.settings.createGroup("Pause");
        this.sgRender = this.settings.createGroup("Render");
        this.targetRange = this.sgGeneral.add(new DoubleSetting.Builder().name("target-range").description("The radius in which players get targeted.").defaultValue(4.0d).min(0.0d).sliderMax(5.0d).build());
        this.targetPriority = this.sgGeneral.add(new EnumSetting.Builder().name("target-priority").description("How to select the player to target.").defaultValue(SortPriority.LowestHealth).build());
        this.rotationMode = this.sgGeneral.add(new EnumSetting.Builder().name("rotation-mode").description("The mode to rotate you server-side.").defaultValue(RotationMode.Both).build());
        this.maxDamage = this.sgGeneral.add(new DoubleSetting.Builder().name("max-self-damage").description("The maximum self-damage allowed.").defaultValue(8.0d).build());
        this.minHealth = this.sgGeneral.add(new DoubleSetting.Builder().name("min-health").description("The minimum health you have to be for Anchor Aura to work.").defaultValue(15.0d).build());
        this.place = this.sgPlace.add(new BoolSetting.Builder().name("place").description("Allows Anchor Aura to place anchors.").defaultValue(true).build());
        SettingGroup settingGroup = this.sgPlace;
        IntSetting.Builder builderRange = new IntSetting.Builder().name("place-delay").description("The tick delay between placing anchors.").defaultValue(2).range(0, 10);
        Setting<Boolean> setting = this.place;
        Objects.requireNonNull(setting);
        this.placeDelay = settingGroup.add(builderRange.visible(setting::get).build());
        SettingGroup settingGroup2 = this.sgPlace;
        EnumSetting.Builder builderDefaultValue = new EnumSetting.Builder().name("place-mode").description("The way anchors are allowed to be placed near you.").defaultValue(Safety.Safe);
        Setting<Boolean> setting2 = this.place;
        Objects.requireNonNull(setting2);
        this.placeMode = settingGroup2.add(builderDefaultValue.visible(setting2::get).build());
        SettingGroup settingGroup3 = this.sgPlace;
        DoubleSetting.Builder builderSliderMax = new DoubleSetting.Builder().name("place-range").description("The radius in which anchors are placed in.").defaultValue(5.0d).min(0.0d).sliderMax(5.0d);
        Setting<Boolean> setting3 = this.place;
        Objects.requireNonNull(setting3);
        this.placeRange = settingGroup3.add(builderSliderMax.visible(setting3::get).build());
        SettingGroup settingGroup4 = this.sgPlace;
        EnumSetting.Builder builderDefaultValue2 = new EnumSetting.Builder().name("placement-positions").description("Where the Anchors will be placed on the entity.").defaultValue(PlaceMode.AboveAndBelow);
        Setting<Boolean> setting4 = this.place;
        Objects.requireNonNull(setting4);
        this.placePositions = settingGroup4.add(builderDefaultValue2.visible(setting4::get).build());
        this.breakDelay = this.sgBreak.add(new IntSetting.Builder().name("break-delay").description("The tick delay between breaking anchors.").defaultValue(10).range(0, 10).build());
        this.breakMode = this.sgBreak.add(new EnumSetting.Builder().name("break-mode").description("The way anchors are allowed to be broken near you.").defaultValue(Safety.Safe).build());
        this.breakRange = this.sgBreak.add(new DoubleSetting.Builder().name("break-range").description("The radius in which anchors are broken in.").defaultValue(5.0d).min(0.0d).sliderMax(5.0d).build());
        this.pauseOnEat = this.sgPause.add(new BoolSetting.Builder().name("pause-on-eat").description("Pauses while eating.").defaultValue(false).build());
        this.pauseOnDrink = this.sgPause.add(new BoolSetting.Builder().name("pause-on-drink").description("Pauses while drinking potions.").defaultValue(false).build());
        this.pauseOnMine = this.sgPause.add(new BoolSetting.Builder().name("pause-on-mine").description("Pauses while mining blocks.").defaultValue(false).build());
        this.shapeMode = this.sgRender.add(new EnumSetting.Builder().name("shape-mode").description("How the shapes are rendered.").defaultValue(ShapeMode.Both).build());
        this.renderPlace = this.sgRender.add(new BoolSetting.Builder().name("render-place").description("Renders the block where it is placing an anchor.").defaultValue(true).build());
        SettingGroup settingGroup5 = this.sgRender;
        ColorSetting.Builder builderDefaultValue3 = new ColorSetting.Builder().name("place-side-color").description("The side color for positions to be placed.").defaultValue(new SettingColor(255, 0, 0, 75));
        Setting<Boolean> setting5 = this.renderPlace;
        Objects.requireNonNull(setting5);
        this.placeSideColor = settingGroup5.add(builderDefaultValue3.visible(setting5::get).build());
        SettingGroup settingGroup6 = this.sgRender;
        ColorSetting.Builder builderDefaultValue4 = new ColorSetting.Builder().name("place-line-color").description("The line color for positions to be placed.").defaultValue(new SettingColor(255, 0, 0, 255));
        Setting<Boolean> setting6 = this.renderPlace;
        Objects.requireNonNull(setting6);
        this.placeLineColor = settingGroup6.add(builderDefaultValue4.visible(setting6::get).build());
        this.renderBreak = this.sgRender.add(new BoolSetting.Builder().name("render-break").description("Renders the block where it is breaking an anchor.").defaultValue(true).build());
        SettingGroup settingGroup7 = this.sgRender;
        ColorSetting.Builder builderDefaultValue5 = new ColorSetting.Builder().name("break-side-color").description("The side color for anchors to be broken.").defaultValue(new SettingColor(255, 0, 0, 75));
        Setting<Boolean> setting7 = this.renderBreak;
        Objects.requireNonNull(setting7);
        this.breakSideColor = settingGroup7.add(builderDefaultValue5.visible(setting7::get).build());
        SettingGroup settingGroup8 = this.sgRender;
        ColorSetting.Builder builderDefaultValue6 = new ColorSetting.Builder().name("break-line-color").description("The line color for anchors to be broken.").defaultValue(new SettingColor(255, 0, 0, 255));
        Setting<Boolean> setting8 = this.renderBreak;
        Objects.requireNonNull(setting8);
        this.breakLineColor = settingGroup8.add(builderDefaultValue6.visible(setting8::get).build());
        this.mutable = new class_2338.class_2339();
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        this.placeDelayLeft = 0;
        this.breakDelayLeft = 0;
        this.target = null;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        class_2338 placePos;
        class_2338 breakPos;
        if (this.mc.field_1687.method_8597().comp_649()) {
            error("You are in the Nether... disabling.", new Object[0]);
            toggle();
            return;
        }
        if (!PlayerUtils.shouldPause(this.pauseOnMine.get().booleanValue(), this.pauseOnEat.get().booleanValue(), this.pauseOnDrink.get().booleanValue()) && EntityUtils.getTotalHealth(this.mc.field_1724) > this.minHealth.get().doubleValue()) {
            if (TargetUtils.isBadTarget(this.target, this.targetRange.get().doubleValue())) {
                this.target = TargetUtils.getPlayerTarget(this.targetRange.get().doubleValue(), this.targetPriority.get());
                if (TargetUtils.isBadTarget(this.target, this.targetRange.get().doubleValue())) {
                    return;
                }
            }
            FindItemResult anchor = InvUtils.findInHotbar(class_1802.field_23141);
            FindItemResult glowStone = InvUtils.findInHotbar(class_1802.field_8801);
            if (anchor.found() && glowStone.found()) {
                if (this.breakDelayLeft >= this.breakDelay.get().intValue() && (breakPos = findBreakPos(this.target.method_24515())) != null) {
                    this.breakDelayLeft = 0;
                    if (this.rotationMode.get() != RotationMode.Both && this.rotationMode.get() != RotationMode.Break) {
                        breakAnchor(breakPos, anchor, glowStone);
                    } else {
                        class_2338 immutableBreakPos = breakPos.method_10062();
                        Rotations.rotate(Rotations.getYaw(breakPos), Rotations.getPitch(breakPos), 50, () -> {
                            breakAnchor(immutableBreakPos, anchor, glowStone);
                        });
                    }
                }
                if (this.placeDelayLeft >= this.placeDelay.get().intValue() && this.place.get().booleanValue() && (placePos = findPlacePos(this.target.method_24515())) != null) {
                    this.placeDelayLeft = 0;
                    BlockUtils.place(placePos.method_10062(), anchor, this.rotationMode.get() == RotationMode.Place || this.rotationMode.get() == RotationMode.Both, 50);
                }
                this.placeDelayLeft++;
                this.breakDelayLeft++;
            }
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        class_2338 breakPos;
        if (this.target == null) {
            return;
        }
        if (this.renderPlace.get().booleanValue()) {
            class_2338 placePos = findPlacePos(this.target.method_24515());
            if (placePos == null) {
                return;
            } else {
                event.renderer.box(placePos, this.placeSideColor.get(), this.placeLineColor.get(), this.shapeMode.get(), 0);
            }
        }
        if (!this.renderBreak.get().booleanValue() || (breakPos = findBreakPos(this.target.method_24515())) == null) {
            return;
        }
        event.renderer.box(breakPos, this.breakSideColor.get(), this.breakLineColor.get(), this.shapeMode.get(), 0);
    }

    @Nullable
    private class_2338 findPlacePos(class_2338 targetPlacePos) {
        switch (this.placePositions.get()) {
            case Above:
                if (isValidPlace(targetPlacePos, 0, 2, 0)) {
                }
                break;
            case Around:
                if (!isValidPlace(targetPlacePos, 0, 0, -1) && !isValidPlace(targetPlacePos, 1, 0, 0) && !isValidPlace(targetPlacePos, -1, 0, 0) && !isValidPlace(targetPlacePos, 0, 0, 1)) {
                }
                break;
            case AboveAndBelow:
                if (!isValidPlace(targetPlacePos, 0, -1, 0) && !isValidPlace(targetPlacePos, 0, 2, 0)) {
                }
                break;
            case All:
                if (!isValidPlace(targetPlacePos, 0, -1, 0) && !isValidPlace(targetPlacePos, 0, 2, 0) && !isValidPlace(targetPlacePos, 1, 0, 0) && !isValidPlace(targetPlacePos, -1, 0, 0) && !isValidPlace(targetPlacePos, 0, 0, 1) && !isValidPlace(targetPlacePos, 0, 0, -1) && !isValidPlace(targetPlacePos, 1, 1, 0) && !isValidPlace(targetPlacePos, -1, -1, 0) && !isValidPlace(targetPlacePos, 0, 1, 1) && !isValidPlace(targetPlacePos, 0, 0, -1)) {
                }
                break;
        }
        return this.mutable;
    }

    @Nullable
    private class_2338 findBreakPos(class_2338 targetPos) {
        if (isValidBreak(targetPos, 0, -1, 0) || isValidBreak(targetPos, 0, 2, 0) || isValidBreak(targetPos, 1, 0, 0) || isValidBreak(targetPos, -1, 0, 0) || isValidBreak(targetPos, 0, 0, 1) || isValidBreak(targetPos, 0, 0, -1) || isValidBreak(targetPos, 1, 1, 0) || isValidBreak(targetPos, -1, -1, 0) || isValidBreak(targetPos, 0, 1, 1) || isValidBreak(targetPos, 0, 0, -1)) {
            return this.mutable;
        }
        return null;
    }

    private boolean getDamagePlace(class_2338 pos) {
        return this.placeMode.get() == Safety.Suicide || ((double) DamageUtils.bedDamage(this.mc.field_1724, pos.method_46558())) <= this.maxDamage.get().doubleValue();
    }

    private boolean getDamageBreak(class_2338 pos) {
        return this.breakMode.get() == Safety.Suicide || ((double) DamageUtils.anchorDamage(this.mc.field_1724, pos.method_46558())) <= this.maxDamage.get().doubleValue();
    }

    private boolean isValidPlace(class_2338 origin, int xOffset, int yOffset, int zOffset) {
        BlockUtils.mutateAround(this.mutable, origin, xOffset, yOffset, zOffset);
        return Math.sqrt(this.mc.field_1724.method_24515().method_10262(this.mutable)) <= this.placeRange.get().doubleValue() && getDamagePlace(this.mutable) && BlockUtils.canPlace(this.mutable);
    }

    private boolean isValidBreak(class_2338 origin, int xOffset, int yOffset, int zOffset) {
        BlockUtils.mutateAround(this.mutable, origin, xOffset, yOffset, zOffset);
        return this.mc.field_1687.method_8320(this.mutable).method_26204() == class_2246.field_23152 && Math.sqrt(this.mc.field_1724.method_24515().method_10262(this.mutable)) <= this.breakRange.get().doubleValue() && getDamageBreak(this.mutable);
    }

    private void breakAnchor(class_2338 pos, FindItemResult anchor, FindItemResult glowStone) {
        if (pos == null || this.mc.field_1687.method_8320(pos).method_26204() != class_2246.field_23152) {
            return;
        }
        this.mc.field_1724.method_5660(false);
        if (glowStone.isOffhand()) {
            this.mc.field_1761.method_2896(this.mc.field_1724, class_1268.field_5810, new class_3965(new class_243(((double) pos.method_10263()) + 0.5d, ((double) pos.method_10264()) + 0.5d, ((double) pos.method_10260()) + 0.5d), class_2350.field_11036, pos, true));
        } else {
            InvUtils.swap(glowStone.slot(), true);
            this.mc.field_1761.method_2896(this.mc.field_1724, class_1268.field_5808, new class_3965(new class_243(((double) pos.method_10263()) + 0.5d, ((double) pos.method_10264()) + 0.5d, ((double) pos.method_10260()) + 0.5d), class_2350.field_11036, pos, true));
        }
        if (anchor.isOffhand()) {
            this.mc.field_1761.method_2896(this.mc.field_1724, class_1268.field_5810, new class_3965(new class_243(((double) pos.method_10263()) + 0.5d, ((double) pos.method_10264()) + 0.5d, ((double) pos.method_10260()) + 0.5d), class_2350.field_11036, pos, true));
        } else {
            InvUtils.swap(anchor.slot(), true);
            this.mc.field_1761.method_2896(this.mc.field_1724, class_1268.field_5808, new class_3965(new class_243(((double) pos.method_10263()) + 0.5d, ((double) pos.method_10264()) + 0.5d, ((double) pos.method_10260()) + 0.5d), class_2350.field_11036, pos, true));
        }
        InvUtils.swapBack();
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public String getInfoString() {
        return EntityUtils.getName(this.target);
    }
}
