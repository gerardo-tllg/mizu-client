package meteordevelopment.meteorclient.systems.managers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1309;
import net.minecraft.class_1493;
import net.minecraft.class_1560;
import net.minecraft.class_1590;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_238;
import net.minecraft.class_243;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/managers/TargetManager.class */
public class TargetManager {
    private final Settings settings = new Settings();
    private final SettingGroup sgTargets = this.settings.createGroup("Targets");
    private final Setting<Double> range = this.sgTargets.add(new DoubleSetting.Builder().name("range").description("Max range to target.").defaultValue(6.5d).min(0.0d).sliderMax(7.0d).build());
    private final Setting<TargetMode> targetMode = this.sgTargets.add(new EnumSetting.Builder().name("target-mode").description("How many targets to choose.").defaultValue(TargetMode.Single).build());
    private final Setting<TargetSortMode> targetSortMode = this.sgTargets.add(new EnumSetting.Builder().name("target-sort-mode").description("How to sort the targets.").defaultValue(TargetSortMode.ClosestAngle).build());
    private final Setting<Integer> numTargets = this.sgTargets.add(new IntSetting.Builder().name("num-targets").description("Max range to target.").defaultValue(2).min(1).sliderMax(5).visible(() -> {
        return this.targetMode.get() == TargetMode.Multi;
    }).build());
    private final Setting<Boolean> ignoreNakeds = this.sgTargets.add(new BoolSetting.Builder().name("ignore-nakeds").description("Ignore players with no items.").defaultValue(true).build());
    private final Setting<Boolean> ignorePassive = this.sgTargets.add(new BoolSetting.Builder().name("ignore-passive").description("Does not attack passive mobs.").defaultValue(false).build());
    private Setting<Set<class_1299<?>>> validEntities;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/managers/TargetManager$TargetMode.class */
    public enum TargetMode {
        Single,
        Multi,
        All
    }

    public TargetManager(Module module, boolean entityListFilter) {
        this.validEntities = null;
        module.settings.groups.addAll(this.settings.groups);
        this.validEntities = this.sgTargets.add(new EntityTypeListSetting.Builder().name("entities").description("Entities to target.").onlyAttackable().defaultValue(class_1299.field_6097).build());
    }

    public List<class_1657> getPlayerTargets() {
        return getPlayerTargets(entity -> {
            return true;
        });
    }

    public List<class_1657> getPlayerTargets(Predicate<class_1657> isGood) {
        List<class_1657> entities = new ArrayList();
        class_243 pos = MeteorClient.mc.field_1724.method_19538();
        class_238 box = new class_238(pos.field_1352 - this.range.get().doubleValue(), pos.field_1351 - this.range.get().doubleValue(), pos.field_1350 - this.range.get().doubleValue(), pos.field_1352 + this.range.get().doubleValue(), pos.field_1351 + this.range.get().doubleValue(), pos.field_1350 + this.range.get().doubleValue());
        double rangeSqr = this.range.get().doubleValue() * this.range.get().doubleValue();
        for (class_1657 entity : MeteorClient.mc.field_1687.method_8390(class_1657.class, box, e -> {
            return !e.method_31481();
        })) {
            if (entity != null && entity.method_5829().method_49271(pos) < rangeSqr && isGood.test(entity) && (!this.ignoreNakeds.get().booleanValue() || !((class_1799) entity.method_31548().meteor$getArmor().get(0)).method_7960() || !((class_1799) entity.method_31548().meteor$getArmor().get(1)).method_7960() || !((class_1799) entity.method_31548().meteor$getArmor().get(2)).method_7960() || !((class_1799) entity.method_31548().meteor$getArmor().get(3)).method_7960())) {
                if (!entity.method_68878() && Friends.get().shouldAttack(entity) && !entity.equals(MeteorClient.mc.field_1724) && !entity.equals(MeteorClient.mc.field_1719) && !entity.method_29504()) {
                    entities.add(entity);
                }
            }
        }
        entities.sort(this.targetSortMode.get());
        switch (this.targetMode.get()) {
            case Single:
                if (entities.size() >= 1) {
                    entities = List.of(entities.get(0));
                }
                break;
            case Multi:
                if (entities.size() > this.numTargets.get().intValue()) {
                    entities.subList(this.numTargets.get().intValue(), entities.size()).clear();
                }
                break;
        }
        return entities;
    }

    public List<class_1297> getEntityTargets() {
        return getEntityTargets(entity -> {
            return true;
        });
    }

    public List<class_1297> getEntityTargets(Predicate<class_1297> isGood) {
        List<class_1297> entities = new ArrayList();
        class_243 pos = MeteorClient.mc.field_1724.method_19538();
        class_238 box = new class_238(pos.field_1352 - this.range.get().doubleValue(), pos.field_1351 - this.range.get().doubleValue(), pos.field_1350 - this.range.get().doubleValue(), pos.field_1352 + this.range.get().doubleValue(), pos.field_1351 + this.range.get().doubleValue(), pos.field_1350 + this.range.get().doubleValue());
        double rangeSqr = this.range.get().doubleValue() * this.range.get().doubleValue();
        for (class_1309 class_1309Var : MeteorClient.mc.field_1687.method_8390(class_1297.class, box, e -> {
            return !e.method_31481();
        })) {
            if (class_1309Var != null && class_1309Var.method_5829().method_49271(pos) < rangeSqr && isGood.test(class_1309Var) && !class_1309Var.equals(MeteorClient.mc.field_1724) && !class_1309Var.equals(MeteorClient.mc.field_1719)) {
                if (class_1309Var instanceof class_1309) {
                    class_1309 livingEntity = class_1309Var;
                    if (livingEntity.method_29504()) {
                    }
                }
                if (class_1309Var.method_5805() && (this.validEntities == null || this.validEntities.get().contains(class_1309Var.method_5864()))) {
                    if (this.ignorePassive.get().booleanValue()) {
                        if (class_1309Var instanceof class_1560) {
                            class_1560 enderman = (class_1560) class_1309Var;
                            if (!enderman.method_7028()) {
                            }
                        }
                        if (class_1309Var instanceof class_1590) {
                            class_1590 piglin = (class_1590) class_1309Var;
                            if (piglin.method_29507() <= 0) {
                            }
                        }
                        if (class_1309Var instanceof class_1493) {
                            class_1493 wolf = (class_1493) class_1309Var;
                            if (!wolf.method_6510()) {
                            }
                        }
                    }
                    if (class_1309Var instanceof class_1657) {
                        class_1657 player = (class_1657) class_1309Var;
                        if (player.method_68878() || !Friends.get().shouldAttack(player)) {
                        }
                    }
                    entities.add(class_1309Var);
                }
            }
        }
        entities.sort(this.targetSortMode.get());
        switch (this.targetMode.get()) {
            case Single:
                if (entities.size() >= 1) {
                    entities = List.of(entities.get(0));
                }
                break;
            case Multi:
                if (entities.size() > this.numTargets.get().intValue()) {
                    entities.subList(this.numTargets.get().intValue(), entities.size()).clear();
                }
                break;
        }
        return entities;
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/managers/TargetManager$TargetSortMode.class */
    public enum TargetSortMode implements Comparator<class_1297> {
        LowestDistance(Comparator.comparingDouble(entity -> {
            return entity.method_33571().method_1025(MeteorClient.mc.field_1724.method_33571());
        })),
        HighestDistance((e1, e2) -> {
            return Double.compare(e2.method_33571().method_1025(MeteorClient.mc.field_1724.method_33571()), e1.method_33571().method_1025(MeteorClient.mc.field_1724.method_33571()));
        }),
        ClosestAngle(TargetSortMode::sortAngle);

        private final Comparator<class_1297> comparator;

        TargetSortMode(Comparator comparator) {
            this.comparator = comparator;
        }

        @Override // java.util.Comparator
        public int compare(class_1297 o1, class_1297 o2) {
            return this.comparator.compare(o1, o2);
        }

        private static int sortAngle(class_1297 e1, class_1297 e2) {
            float[] angle1 = MeteorClient.ROTATION.getRotation(e1.method_33571());
            float[] angle2 = MeteorClient.ROTATION.getRotation(e1.method_33571());
            double e1yaw = Math.abs(angle1[0] - MeteorClient.mc.field_1724.method_36454());
            double e2yaw = Math.abs(angle2[0] - MeteorClient.mc.field_1724.method_36454());
            return Double.compare(e1yaw * e1yaw, e2yaw * e2yaw);
        }
    }
}
