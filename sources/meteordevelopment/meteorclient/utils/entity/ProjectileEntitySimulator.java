package meteordevelopment.meteorclient.utils.entity;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.SwitchBootstraps;
import java.util.Objects;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.mixin.CrossbowItemAccessor;
import meteordevelopment.meteorclient.mixin.ProjectileInGroundAccessor;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.MissHitResult;
import meteordevelopment.meteorclient.utils.player.Rotations;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1667;
import net.minecraft.class_1670;
import net.minecraft.class_1674;
import net.minecraft.class_1675;
import net.minecraft.class_1680;
import net.minecraft.class_1681;
import net.minecraft.class_1683;
import net.minecraft.class_1684;
import net.minecraft.class_1685;
import net.minecraft.class_1686;
import net.minecraft.class_1687;
import net.minecraft.class_1753;
import net.minecraft.class_1764;
import net.minecraft.class_1771;
import net.minecraft.class_1776;
import net.minecraft.class_1779;
import net.minecraft.class_1787;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1803;
import net.minecraft.class_1823;
import net.minecraft.class_1828;
import net.minecraft.class_1835;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_3610;
import net.minecraft.class_3612;
import net.minecraft.class_3959;
import net.minecraft.class_4076;
import net.minecraft.class_8956;
import net.minecraft.class_9239;
import net.minecraft.class_9278;
import net.minecraft.class_9334;
import org.joml.Quaterniond;
import org.joml.Vector3d;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/entity/ProjectileEntitySimulator.class */
public class ProjectileEntitySimulator {
    private static final class_2338.class_2339 blockPos = new class_2338.class_2339();
    private static final class_243 pos3d = new class_243(0.0d, 0.0d, 0.0d);
    private static final class_243 prevPos3d = new class_243(0.0d, 0.0d, 0.0d);
    public final Vector3d pos = new Vector3d();
    private final Vector3d velocity = new Vector3d();
    private class_1297 simulatingEntity;
    private double gravity;
    private double airDrag;
    private double waterDrag;
    private float height;
    private float width;

    public boolean set(class_1297 user, class_1799 itemStack, double simulated, boolean accurate, float tickDelta) {
        class_1753 class_1753VarMethod_7909 = itemStack.method_7909();
        Objects.requireNonNull(class_1753VarMethod_7909);
        switch ((int) SwitchBootstraps.typeSwitch(MethodHandles.lookup(), "typeSwitch", MethodType.methodType(Integer.TYPE, Object.class, Integer.TYPE), class_1753.class, class_1764.class, class_9239.class, class_1787.class, class_1835.class, class_1823.class, class_1771.class, class_1776.class, class_1779.class, class_1828.class, class_1803.class).dynamicInvoker().invoke(class_1753VarMethod_7909, 0) /* invoke-custom */) {
            case 0:
                double charge = class_1753.method_7722(MeteorClient.mc.field_1724.method_6048());
                if (charge <= 0.1d) {
                    return false;
                }
                set(user, 0.0d, charge * 3.0d, simulated, 0.05d, 0.6d, accurate, tickDelta, class_1299.field_6122);
                return true;
            case 1:
                class_9278 projectilesComponent = (class_9278) itemStack.method_58694(class_9334.field_49649);
                if (projectilesComponent == null) {
                    return false;
                }
                if (projectilesComponent.method_57438(class_1802.field_8639)) {
                    set(user, 0.0d, CrossbowItemAccessor.getSpeed(projectilesComponent), simulated, 0.0d, 0.6d, accurate, tickDelta, class_1299.field_6133);
                    return true;
                }
                set(user, 0.0d, CrossbowItemAccessor.getSpeed(projectilesComponent), simulated, 0.05d, 0.6d, accurate, tickDelta, class_1299.field_6122);
                return true;
            case 2:
                set(user, 0.0d, 1.5d, simulated, 0.0d, 1.0d, accurate, tickDelta, class_1299.field_47243);
                this.airDrag = 1.0d;
                return true;
            case 3:
                setFishingBobber(user, tickDelta);
                return true;
            case 4:
                set(user, 0.0d, 2.5d, simulated, 0.05d, 0.99d, accurate, tickDelta, class_1299.field_6127);
                return true;
            case 5:
                set(user, 0.0d, 1.5d, simulated, 0.03d, 0.8d, accurate, tickDelta, class_1299.field_6068);
                return true;
            case 6:
                set(user, 0.0d, 1.5d, simulated, 0.03d, 0.8d, accurate, tickDelta, class_1299.field_6144);
                return true;
            case 7:
                set(user, 0.0d, 1.5d, simulated, 0.03d, 0.8d, accurate, tickDelta, class_1299.field_6082);
                return true;
            case 8:
                set(user, -20.0d, 0.7d, simulated, 0.07d, 0.8d, accurate, tickDelta, class_1299.field_6064);
                return true;
            case 9:
                set(user, -20.0d, 0.5d, simulated, 0.05d, 0.8d, accurate, tickDelta, class_1299.field_56254);
                return true;
            case 10:
                set(user, -20.0d, 0.5d, simulated, 0.05d, 0.8d, accurate, tickDelta, class_1299.field_56255);
                return true;
            default:
                return false;
        }
    }

    public void set(class_1297 user, double roll, double speed, double simulated, double gravity, double waterDrag, boolean accurate, float tickDelta, class_1299<?> type) {
        double yaw;
        double pitch;
        double x;
        double y;
        double z;
        Utils.set(this.pos, user, tickDelta).add(0.0d, user.method_18381(user.method_18376()), 0.0d);
        if (user == MeteorClient.mc.field_1724 && Rotations.rotating) {
            yaw = Rotations.serverYaw;
            pitch = Rotations.serverPitch;
        } else {
            yaw = user.method_5705(tickDelta);
            pitch = user.method_5695(tickDelta);
        }
        if (simulated == 0.0d) {
            x = (-Math.sin(yaw * 0.017453292d)) * Math.cos(pitch * 0.017453292d);
            y = -Math.sin((pitch + roll) * 0.017453292d);
            z = Math.cos(yaw * 0.017453292d) * Math.cos(pitch * 0.017453292d);
        } else {
            class_243 vec3d = user.method_18864(1.0f);
            Quaterniond quaternion = new Quaterniond().setAngleAxis(simulated, vec3d.field_1352, vec3d.field_1351, vec3d.field_1350);
            class_243 vec3d2 = user.method_5828(1.0f);
            Vector3d vector3f = new Vector3d(vec3d2.field_1352, vec3d2.field_1351, vec3d2.field_1350);
            vector3f.rotate(quaternion);
            x = vector3f.x;
            y = vector3f.y;
            z = vector3f.z;
        }
        this.velocity.set(x, y, z).normalize().mul(speed);
        if (accurate) {
            class_243 vel = user.method_18798();
            this.velocity.add(vel.field_1352, user.method_24828() ? 0.0d : vel.field_1351, vel.field_1350);
        }
        this.simulatingEntity = user;
        this.gravity = gravity;
        this.airDrag = 0.99d;
        this.waterDrag = waterDrag;
        this.width = type.method_17685();
        this.height = type.method_17686();
    }

    public boolean set(class_1297 entity, boolean accurate) {
        if (entity instanceof ProjectileInGroundAccessor) {
            ProjectileInGroundAccessor ppe = (ProjectileInGroundAccessor) entity;
            if (ppe.invokeIsInGround()) {
                return false;
            }
        }
        if (entity instanceof class_1667) {
            set(entity, 0.05d, 0.6d, accurate);
        } else if (entity instanceof class_1685) {
            set(entity, 0.05d, 0.99d, accurate);
        } else if ((entity instanceof class_1684) || (entity instanceof class_1680) || (entity instanceof class_1681)) {
            set(entity, 0.03d, 0.8d, accurate);
        } else if (entity instanceof class_1683) {
            set(entity, 0.07d, 0.8d, accurate);
        } else if (entity instanceof class_1686) {
            set(entity, 0.05d, 0.8d, accurate);
        } else if ((entity instanceof class_1687) || (entity instanceof class_1674) || (entity instanceof class_1670) || (entity instanceof class_8956)) {
            set(entity, 0.0d, 1.0d, accurate);
            this.airDrag = 1.0d;
        } else {
            return false;
        }
        if (entity.method_5740()) {
            this.gravity = 0.0d;
            return true;
        }
        return true;
    }

    public void set(class_1297 entity, double gravity, double waterDrag, boolean accurate) {
        this.pos.set(entity.method_23317(), entity.method_23318(), entity.method_23321());
        double speed = entity.method_18798().method_1033();
        this.velocity.set(entity.method_18798().field_1352, entity.method_18798().field_1351, entity.method_18798().field_1350).normalize().mul(speed);
        if (accurate) {
            class_243 vel = entity.method_18798();
            this.velocity.add(vel.field_1352, entity.method_24828() ? 0.0d : vel.field_1351, vel.field_1350);
        }
        this.simulatingEntity = entity;
        this.gravity = gravity;
        this.airDrag = 0.99d;
        this.waterDrag = waterDrag;
        this.width = entity.method_17681();
        this.height = entity.method_17682();
    }

    public void setFishingBobber(class_1297 user, float tickDelta) {
        double yaw;
        double pitch;
        if (user == MeteorClient.mc.field_1724 && Rotations.rotating) {
            yaw = Rotations.serverYaw;
            pitch = Rotations.serverPitch;
        } else {
            yaw = user.method_5705(tickDelta);
            pitch = user.method_5695(tickDelta);
        }
        double h = Math.cos(((-yaw) * 0.01745329238474369d) - 3.1415927410125732d);
        double i = Math.sin(((-yaw) * 0.01745329238474369d) - 3.1415927410125732d);
        double j = -Math.cos((-pitch) * 0.01745329238474369d);
        double k = Math.sin((-pitch) * 0.01745329238474369d);
        Utils.set(this.pos, user, tickDelta).sub(i * 0.3d, 0.0d, h * 0.3d).add(0.0d, user.method_18381(user.method_18376()), 0.0d);
        this.velocity.set(-i, class_3532.method_15350(-(k / j), -5.0d, 5.0d), -h);
        double l = this.velocity.length();
        this.velocity.mul((0.6d / l) + 0.5d, (0.6d / l) + 0.5d, (0.6d / l) + 0.5d);
        this.simulatingEntity = user;
        this.gravity = 0.03d;
        this.airDrag = 0.92d;
        this.waterDrag = 0.0d;
        this.width = class_1299.field_6103.method_17685();
        this.height = class_1299.field_6103.method_17686();
    }

    public class_239 tick() {
        prevPos3d.meteor$set(this.pos);
        this.pos.add(this.velocity);
        this.velocity.mul(isTouchingWater() ? this.waterDrag : this.airDrag);
        this.velocity.sub(0.0d, this.gravity, 0.0d);
        if (this.pos.y < MeteorClient.mc.field_1687.method_31607()) {
            return MissHitResult.INSTANCE;
        }
        int chunkX = class_4076.method_32204(this.pos.x);
        int chunkZ = class_4076.method_32204(this.pos.z);
        if (!MeteorClient.mc.field_1687.method_2935().method_12123(chunkX, chunkZ)) {
            return MissHitResult.INSTANCE;
        }
        pos3d.meteor$set(this.pos);
        if (pos3d.equals(prevPos3d)) {
            return MissHitResult.INSTANCE;
        }
        class_239 hitResult = getCollision();
        if (hitResult.method_17783() == class_239.class_240.field_1333) {
            return null;
        }
        return hitResult;
    }

    private boolean isTouchingWater() {
        blockPos.method_10102(this.pos.x, this.pos.y, this.pos.z);
        class_3610 fluidState = MeteorClient.mc.field_1687.method_8316(blockPos);
        return (fluidState.method_15772() == class_3612.field_15910 || fluidState.method_15772() == class_3612.field_15909) && this.pos.y - ((double) ((int) this.pos.y)) <= ((double) fluidState.method_20785());
    }

    private class_239 getCollision() {
        class_239 hitResult = MeteorClient.mc.field_1687.method_17742(new class_3959(prevPos3d, pos3d, class_3959.class_3960.field_17558, this.waterDrag == 0.0d ? class_3959.class_242.field_1347 : class_3959.class_242.field_1348, this.simulatingEntity));
        if (hitResult.method_17783() != class_239.class_240.field_1333) {
            pos3d.meteor$set(hitResult.method_17784().field_1352, hitResult.method_17784().field_1351, hitResult.method_17784().field_1350);
        }
        class_238 box = new class_238(prevPos3d.field_1352 - ((double) (this.width / 2.0f)), prevPos3d.field_1351, prevPos3d.field_1350 - ((double) (this.width / 2.0f)), prevPos3d.field_1352 + ((double) (this.width / 2.0f)), prevPos3d.field_1351 + ((double) this.height), prevPos3d.field_1350 + ((double) (this.width / 2.0f))).method_1012(this.velocity.x, this.velocity.y, this.velocity.z).method_1014(1.0d);
        class_239 hitResult2 = class_1675.method_18077(MeteorClient.mc.field_1687, this.simulatingEntity == MeteorClient.mc.field_1724 ? null : this.simulatingEntity, prevPos3d, pos3d, box, entity -> {
            return !entity.method_7325() && entity.method_5805() && entity.method_5863();
        });
        if (hitResult2 != null) {
            hitResult = hitResult2;
        }
        return hitResult;
    }
}
