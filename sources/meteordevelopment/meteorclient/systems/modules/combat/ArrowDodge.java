package meteordevelopment.meteorclient.systems.modules.combat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import javassist.compiler.TokenId;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.ProjectileEntityAccessor;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.ProjectileEntitySimulator;
import meteordevelopment.meteorclient.utils.misc.Pool;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1667;
import net.minecraft.class_1676;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2828;
import org.joml.Vector3d;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/ArrowDodge.class */
public class ArrowDodge extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgMovement;
    private final Setting<MoveType> moveType;
    private final Setting<Double> moveSpeed;
    private final Setting<Double> distanceCheck;
    private final Setting<Boolean> accurate;
    private final Setting<Boolean> groundCheck;
    private final Setting<Boolean> allProjectiles;
    private final Setting<Boolean> ignoreOwn;
    public final Setting<Integer> simulationSteps;
    private final List<class_243> possibleMoveDirections;
    private final ProjectileEntitySimulator simulator;
    private final Pool<Vector3d> vec3s;
    private final List<Vector3d> points;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/ArrowDodge$MoveType.class */
    public enum MoveType {
        Velocity,
        Packet
    }

    public ArrowDodge() {
        super(Categories.Combat, "arrow-dodge", "Tries to dodge arrows coming at you.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgMovement = this.settings.createGroup("Movement");
        this.moveType = this.sgMovement.add(new EnumSetting.Builder().name("move-type").description("The way you are moved by this module.").defaultValue(MoveType.Velocity).build());
        this.moveSpeed = this.sgMovement.add(new DoubleSetting.Builder().name("move-speed").description("How fast should you be when dodging arrow.").defaultValue(1.0d).min(0.01d).sliderRange(0.01d, 5.0d).build());
        this.distanceCheck = this.sgMovement.add(new DoubleSetting.Builder().name("distance-check").description("How far should an arrow be from the player to be considered not hitting.").defaultValue(1.0d).min(0.01d).sliderRange(0.01d, 5.0d).build());
        this.accurate = this.sgGeneral.add(new BoolSetting.Builder().name("accurate").description("Whether or not to calculate more accurate.").defaultValue(false).build());
        this.groundCheck = this.sgGeneral.add(new BoolSetting.Builder().name("ground-check").description("Tries to prevent you from falling to your death.").defaultValue(true).build());
        this.allProjectiles = this.sgGeneral.add(new BoolSetting.Builder().name("all-projectiles").description("Dodge all projectiles, not only arrows.").defaultValue(false).build());
        this.ignoreOwn = this.sgGeneral.add(new BoolSetting.Builder().name("ignore-own").description("Ignore your own projectiles.").defaultValue(false).build());
        this.simulationSteps = this.sgGeneral.add(new IntSetting.Builder().name("simulation-steps").description("How many steps to simulate projectiles. Zero for no limit.").defaultValue(Integer.valueOf(TokenId.BadToken)).sliderMax(5000).build());
        this.possibleMoveDirections = Arrays.asList(new class_243(1.0d, 0.0d, 1.0d), new class_243(0.0d, 0.0d, 1.0d), new class_243(-1.0d, 0.0d, 1.0d), new class_243(1.0d, 0.0d, 0.0d), new class_243(-1.0d, 0.0d, 0.0d), new class_243(1.0d, 0.0d, -1.0d), new class_243(0.0d, 0.0d, -1.0d), new class_243(-1.0d, 0.0d, -1.0d));
        this.simulator = new ProjectileEntitySimulator();
        this.vec3s = new Pool<>(Vector3d::new);
        this.points = new ArrayList();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        UUID owner;
        for (Vector3d point : this.points) {
            this.vec3s.free(point);
        }
        this.points.clear();
        for (ProjectileEntityAccessor projectileEntityAccessor : this.mc.field_1687.method_18112()) {
            if ((projectileEntityAccessor instanceof class_1676) && (this.allProjectiles.get().booleanValue() || (projectileEntityAccessor instanceof class_1667))) {
                if (!this.ignoreOwn.get().booleanValue() || (owner = projectileEntityAccessor.getOwnerUuid()) == null || !owner.equals(this.mc.field_1724.method_5667())) {
                    if (this.simulator.set(projectileEntityAccessor, this.accurate.get().booleanValue())) {
                        int i = 0;
                        while (true) {
                            if (i < (this.simulationSteps.get().intValue() > 0 ? this.simulationSteps.get().intValue() : Integer.MAX_VALUE)) {
                                this.points.add(this.vec3s.get().set(this.simulator.pos));
                                if (this.simulator.tick() != null) {
                                    break;
                                } else {
                                    i++;
                                }
                            }
                        }
                    }
                }
            }
        }
        if (isValid(class_243.field_1353, false)) {
            return;
        }
        double speed = this.moveSpeed.get().doubleValue();
        for (int i2 = 0; i2 < 500; i2++) {
            boolean didMove = false;
            Collections.shuffle(this.possibleMoveDirections);
            Iterator<class_243> it = this.possibleMoveDirections.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                class_243 direction = it.next();
                class_243 velocity = direction.method_1021(speed);
                if (isValid(velocity, true)) {
                    move(velocity);
                    didMove = true;
                    break;
                }
            }
            if (!didMove) {
                speed += this.moveSpeed.get().doubleValue();
            } else {
                return;
            }
        }
    }

    private void move(class_243 vel) {
        move(vel.field_1352, vel.field_1351, vel.field_1350);
    }

    private void move(double velX, double velY, double velZ) {
        switch (this.moveType.get()) {
            case Velocity:
                this.mc.field_1724.method_18800(velX, velY, velZ);
                break;
            case Packet:
                class_243 newPos = this.mc.field_1724.method_19538().method_1031(velX, velY, velZ);
                this.mc.field_1724.field_3944.method_52787(new class_2828.class_2829(newPos.field_1352, newPos.field_1351, newPos.field_1350, false, this.mc.field_1724.field_5976));
                this.mc.field_1724.field_3944.method_52787(new class_2828.class_2829(newPos.field_1352, newPos.field_1351 - 0.01d, newPos.field_1350, true, this.mc.field_1724.field_5976));
                break;
        }
    }

    private boolean isValid(class_243 velocity, boolean checkGround) {
        class_243 playerPos = this.mc.field_1724.method_19538().method_1019(velocity);
        class_243 headPos = playerPos.method_1031(0.0d, 1.0d, 0.0d);
        for (Vector3d pos : this.points) {
            class_243 projectilePos = new class_243(pos.x, pos.y, pos.z);
            if (projectilePos.method_24802(playerPos, this.distanceCheck.get().doubleValue()) || projectilePos.method_24802(headPos, this.distanceCheck.get().doubleValue())) {
                return false;
            }
        }
        if (checkGround) {
            class_2338 blockPos = this.mc.field_1724.method_24515().method_10081(class_2338.method_49637(velocity.field_1352, velocity.field_1351, velocity.field_1350));
            if (this.mc.field_1687.method_8320(blockPos).method_26220(this.mc.field_1687, blockPos).method_1110() && this.mc.field_1687.method_8320(blockPos.method_10084()).method_26220(this.mc.field_1687, blockPos.method_10084()).method_1110()) {
                return (this.groundCheck.get().booleanValue() && this.mc.field_1687.method_8320(blockPos.method_10074()).method_26220(this.mc.field_1687, blockPos.method_10074()).method_1110()) ? false : true;
            }
            return false;
        }
        return true;
    }
}
