package meteordevelopment.meteorclient.systems.managers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.mixininterface.IPlayerInventory;
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
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class TargetManager {
    private final Settings settings = new Settings();
    private final SettingGroup sgTargets;
    private final Setting<Double> range;
    private final Setting<TargetMode> targetMode;
    private final Setting<TargetSortMode> targetSortMode;
    private final Setting<Integer> numTargets;
    private final Setting<Boolean> ignoreNakeds;
    private final Setting<Boolean> ignorePassive;
    private Setting<Set<EntityType<?>>> validEntities;

    public TargetManager(Module module, boolean entityListFilter) {
        this.sgTargets = this.settings.createGroup("Targets");
        this.range = this.sgTargets.add(new DoubleSetting.Builder()
                .name("range")
                .description("Max range to target.")
                .defaultValue(6.5)
                .min(0.0)
                .sliderMax(7.0)
                .build());
        this.targetMode = this.sgTargets.add(new EnumSetting.Builder<TargetMode>()
                .name("target-mode")
                .description("How many targets to choose.")
                .defaultValue(TargetMode.Single)
                .build());
        this.targetSortMode = this.sgTargets.add(new EnumSetting.Builder<TargetSortMode>()
                .name("target-sort-mode")
                .description("How to sort the targets.")
                .defaultValue(TargetSortMode.ClosestAngle)
                .build());
        this.numTargets = this.sgTargets.add(new IntSetting.Builder()
                .name("num-targets")
                .description("Max range to target.")
                .defaultValue(2)
                .min(1)
                .sliderMax(5)
                .visible(() -> this.targetMode.get() == TargetMode.Multi)
                .build());
        this.ignoreNakeds = this.sgTargets.add(new BoolSetting.Builder()
                .name("ignore-nakeds")
                .description("Ignore players with no items.")
                .defaultValue(true)
                .build());
        this.ignorePassive = this.sgTargets.add(new BoolSetting.Builder()
                .name("ignore-passive")
                .description("Does not attack passive mobs.")
                .defaultValue(false)
                .build());
        this.validEntities = null;
        module.settings.groups.addAll(this.settings.groups);
        this.validEntities = this.sgTargets.add(new EntityTypeListSetting.Builder()
                .name("entities")
                .description("Entities to target.")
                .onlyAttackable()
                .defaultValue(EntityType.PLAYER)
                .build());
    }

    public List<PlayerEntity> getPlayerTargets() {
        return this.getPlayerTargets(entity -> true);
    }

    public List<PlayerEntity> getPlayerTargets(Predicate<PlayerEntity> isGood) {
        List<PlayerEntity> entities = new ArrayList<>();

        Vec3d pos = mc.player.getPos();
        Box box = new Box(
                pos.x - range.get(), pos.y - range.get(), pos.z - range.get(),
                pos.x + range.get(), pos.y + range.get(), pos.z + range.get());

        double rangeSqr = range.get() * range.get();

        for (PlayerEntity entity : mc.world.getEntitiesByClass(PlayerEntity.class, box, e -> !e.isRemoved())) {
            if (entity == null) continue;
            if (!(entity.getBoundingBox().squaredMagnitude(pos) < rangeSqr)) continue;
            if (!isGood.test(entity)) continue;
            if (ignoreNakeds.get()
                    && ((IPlayerInventory) entity.getInventory()).meteor$getArmor().get(0).isEmpty()
                    && ((IPlayerInventory) entity.getInventory()).meteor$getArmor().get(1).isEmpty()
                    && ((IPlayerInventory) entity.getInventory()).meteor$getArmor().get(2).isEmpty()
                    && ((IPlayerInventory) entity.getInventory()).meteor$getArmor().get(3).isEmpty())
                continue;

            if (!entity.isCreative()
                    && Friends.get().shouldAttack(entity)
                    && !entity.equals(mc.player)
                    && !entity.equals(mc.cameraEntity)
                    && !entity.isDead()) {
                entities.add(entity);
            }
        }

        entities.sort(targetSortMode.get());

        switch (targetMode.get()) {
            case Single -> {
                if (entities.size() >= 1) {
                    entities = List.of(entities.get(0));
                }
            }
            case Multi -> {
                if (entities.size() > numTargets.get()) {
                    entities.subList(numTargets.get(), entities.size()).clear();
                }
            }
            case All -> {}
        }

        return entities;
    }

    public List<Entity> getEntityTargets() {
        return this.getEntityTargets(entity -> true);
    }

    public List<Entity> getEntityTargets(Predicate<Entity> isGood) {
        List<Entity> entities = new ArrayList<>();

        Vec3d pos = mc.player.getPos();
        Box box = new Box(
                pos.x - range.get(), pos.y - range.get(), pos.z - range.get(),
                pos.x + range.get(), pos.y + range.get(), pos.z + range.get());

        double rangeSqr = range.get() * range.get();

        for (Entity entity : mc.world.getEntitiesByClass(Entity.class, box, e -> !e.isRemoved())) {
            if (entity == null) continue;
            if (!(entity.getBoundingBox().squaredMagnitude(pos) < rangeSqr)) continue;
            if (!isGood.test(entity)) continue;
            if (entity.equals(mc.player) || entity.equals(mc.cameraEntity)) continue;

            if (entity instanceof LivingEntity livingEntity) {
                if (livingEntity.isDead()) continue;
            }
            if (!entity.isAlive()) continue;

            if (validEntities != null && !validEntities.get().contains(entity.getType())) continue;

            if (ignorePassive.get()) {
                if (entity instanceof EndermanEntity enderman && !enderman.isAngry()) continue;
                // In 1.21.5, isAngryAt() needs ServerWorld - use anger time instead
                if (entity instanceof ZombifiedPiglinEntity piglin && piglin.getAngerTime() <= 0) continue;
                if (entity instanceof WolfEntity wolf && !wolf.isAttacking()) continue;
            }

            if (entity instanceof PlayerEntity player) {
                if (player.isCreative() || !Friends.get().shouldAttack(player)) continue;
            }

            entities.add(entity);
        }

        entities.sort(targetSortMode.get());

        switch (targetMode.get()) {
            case Single -> {
                if (entities.size() >= 1) {
                    entities = List.of(entities.get(0));
                }
            }
            case Multi -> {
                if (entities.size() > numTargets.get()) {
                    entities.subList(numTargets.get(), entities.size()).clear();
                }
            }
            case All -> {}
        }

        return entities;
    }

    public enum TargetMode {
        Single, Multi, All
    }

    public enum TargetSortMode implements Comparator<Entity> {
        LowestDistance(Comparator.comparingDouble(entity ->
                entity.getEyePos().squaredDistanceTo(mc.player.getEyePos()))),

        HighestDistance((e1, e2) -> Double.compare(
                e2.getEyePos().squaredDistanceTo(mc.player.getEyePos()),
                e1.getEyePos().squaredDistanceTo(mc.player.getEyePos()))),

        ClosestAngle(TargetSortMode::sortAngle);

        private final Comparator<Entity> comparator;

        TargetSortMode(Comparator<Entity> comparator) {
            this.comparator = comparator;
        }

        @Override
        public int compare(Entity o1, Entity o2) {
            return comparator.compare(o1, o2);
        }

        private static int sortAngle(Entity e1, Entity e2) {
            float[] angle1 = MeteorClient.ROTATION.getRotation(e1.getEyePos());
            float[] angle2 = MeteorClient.ROTATION.getRotation(e1.getEyePos());

            double e1yaw = Math.abs(angle1[0] - mc.player.getYaw());
            double e2yaw = Math.abs(angle2[0] - mc.player.getYaw());

            return Double.compare(e1yaw * e1yaw, e2yaw * e2yaw);
        }
    }
}
