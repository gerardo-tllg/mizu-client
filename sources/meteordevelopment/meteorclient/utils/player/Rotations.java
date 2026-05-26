package meteordevelopment.meteorclient.utils.player;

import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.player.SendMovementPacketsEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.utils.PreInit;
import meteordevelopment.meteorclient.utils.entity.Target;
import meteordevelopment.meteorclient.utils.misc.Pool;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1297;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2828;
import net.minecraft.class_3532;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/player/Rotations.class */
public class Rotations {
    public static float serverYaw;
    public static float serverPitch;
    public static int rotationTimer;
    private static float preYaw;
    private static float prePitch;
    private static Rotation lastRotation;
    private static int lastRotationTimer;
    private static boolean sentLastRotation;
    private static final Pool<Rotation> rotationPool = new Pool<>(Rotation::new);
    private static final List<Rotation> rotations = new ArrayList();
    private static int i = 0;
    public static boolean rotating = false;

    private Rotations() {
    }

    @PreInit
    public static void init() {
        MeteorClient.EVENT_BUS.subscribe(Rotations.class);
    }

    public static void rotate(double yaw, double pitch, int priority, boolean clientSide, Runnable callback) {
        Rotation rotation = rotationPool.get();
        rotation.set(yaw, pitch, priority, clientSide, callback);
        int i2 = 0;
        while (i2 < rotations.size() && priority <= rotations.get(i2).priority) {
            i2++;
        }
        rotations.add(i2, rotation);
    }

    public static void rotate(double yaw, double pitch, int priority, Runnable callback) {
        rotate(yaw, pitch, priority, false, callback);
    }

    public static void rotate(double yaw, double pitch, Runnable callback) {
        rotate(yaw, pitch, 0, callback);
    }

    public static void rotate(double yaw, double pitch, int priority) {
        rotate(yaw, pitch, priority, null);
    }

    public static void rotate(double yaw, double pitch) {
        rotate(yaw, pitch, 0, null);
    }

    private static void resetLastRotation() {
        if (lastRotation != null) {
            rotationPool.free(lastRotation);
            lastRotation = null;
            lastRotationTimer = 0;
        }
    }

    @EventHandler
    private static void onSendMovementPacketsPre(SendMovementPacketsEvent.Pre event) {
        if (MeteorClient.mc.field_1719 != MeteorClient.mc.field_1724) {
            return;
        }
        sentLastRotation = false;
        if (!rotations.isEmpty()) {
            rotating = true;
            resetLastRotation();
            Rotation rotation = rotations.get(i);
            setupMovementPacketRotation(rotation);
            if (rotations.size() > 1) {
                rotationPool.free(rotation);
            }
            i++;
            return;
        }
        if (lastRotation != null) {
            if (lastRotationTimer >= Config.get().rotationHoldTicks.get().intValue()) {
                resetLastRotation();
                rotating = false;
            } else {
                setupMovementPacketRotation(lastRotation);
                sentLastRotation = true;
                lastRotationTimer++;
            }
        }
    }

    private static void setupMovementPacketRotation(Rotation rotation) {
        setClientRotation(rotation);
        setCamRotation(rotation.yaw, rotation.pitch);
    }

    private static void setClientRotation(Rotation rotation) {
        preYaw = MeteorClient.mc.field_1724.method_36454();
        prePitch = MeteorClient.mc.field_1724.method_36455();
        MeteorClient.mc.field_1724.method_36456((float) rotation.yaw);
        MeteorClient.mc.field_1724.method_36457((float) rotation.pitch);
    }

    @EventHandler
    private static void onSendMovementPacketsPost(SendMovementPacketsEvent.Post event) {
        if (!rotations.isEmpty()) {
            if (MeteorClient.mc.field_1719 == MeteorClient.mc.field_1724) {
                rotations.get(i - 1).runCallback();
                if (rotations.size() == 1) {
                    lastRotation = rotations.get(i - 1);
                }
                resetPreRotation();
            }
            while (i < rotations.size()) {
                Rotation rotation = rotations.get(i);
                setCamRotation(rotation.yaw, rotation.pitch);
                if (rotation.clientSide) {
                    setClientRotation(rotation);
                }
                rotation.sendPacket();
                if (rotation.clientSide) {
                    resetPreRotation();
                }
                if (i == rotations.size() - 1) {
                    lastRotation = rotation;
                } else {
                    rotationPool.free(rotation);
                }
                i++;
            }
            rotations.clear();
            i = 0;
            return;
        }
        if (sentLastRotation) {
            resetPreRotation();
        }
    }

    private static void resetPreRotation() {
        MeteorClient.mc.field_1724.method_36456(preYaw);
        MeteorClient.mc.field_1724.method_36457(prePitch);
    }

    @EventHandler
    private static void onTick(TickEvent.Pre event) {
        rotationTimer++;
    }

    public static double getYaw(class_1297 entity) {
        return MeteorClient.mc.field_1724.method_36454() + class_3532.method_15393((((float) Math.toDegrees(Math.atan2(entity.method_23321() - MeteorClient.mc.field_1724.method_23321(), entity.method_23317() - MeteorClient.mc.field_1724.method_23317()))) - 90.0f) - MeteorClient.mc.field_1724.method_36454());
    }

    public static double getYaw(class_243 pos) {
        return MeteorClient.mc.field_1724.method_36454() + class_3532.method_15393((((float) Math.toDegrees(Math.atan2(pos.method_10215() - MeteorClient.mc.field_1724.method_23321(), pos.method_10216() - MeteorClient.mc.field_1724.method_23317()))) - 90.0f) - MeteorClient.mc.field_1724.method_36454());
    }

    public static double getPitch(class_243 pos) {
        double diffX = pos.method_10216() - MeteorClient.mc.field_1724.method_23317();
        double diffY = pos.method_10214() - (MeteorClient.mc.field_1724.method_23318() + ((double) MeteorClient.mc.field_1724.method_18381(MeteorClient.mc.field_1724.method_18376())));
        double diffZ = pos.method_10215() - MeteorClient.mc.field_1724.method_23321();
        double diffXZ = Math.sqrt((diffX * diffX) + (diffZ * diffZ));
        return MeteorClient.mc.field_1724.method_36455() + class_3532.method_15393(((float) (-Math.toDegrees(Math.atan2(diffY, diffXZ)))) - MeteorClient.mc.field_1724.method_36455());
    }

    public static double getPitch(class_1297 entity, Target target) {
        double y;
        if (target == Target.Head) {
            y = entity.method_23320();
        } else {
            y = target == Target.Body ? entity.method_23318() + ((double) (entity.method_17682() / 2.0f)) : entity.method_23318();
        }
        double diffX = entity.method_23317() - MeteorClient.mc.field_1724.method_23317();
        double diffY = y - (MeteorClient.mc.field_1724.method_23318() + ((double) MeteorClient.mc.field_1724.method_18381(MeteorClient.mc.field_1724.method_18376())));
        double diffZ = entity.method_23321() - MeteorClient.mc.field_1724.method_23321();
        double diffXZ = Math.sqrt((diffX * diffX) + (diffZ * diffZ));
        return MeteorClient.mc.field_1724.method_36455() + class_3532.method_15393(((float) (-Math.toDegrees(Math.atan2(diffY, diffXZ)))) - MeteorClient.mc.field_1724.method_36455());
    }

    public static double getPitch(class_1297 entity) {
        return getPitch(entity, Target.Body);
    }

    public static double getYaw(class_2338 pos) {
        return MeteorClient.mc.field_1724.method_36454() + class_3532.method_15393((((float) Math.toDegrees(Math.atan2((((double) pos.method_10260()) + 0.5d) - MeteorClient.mc.field_1724.method_23321(), (((double) pos.method_10263()) + 0.5d) - MeteorClient.mc.field_1724.method_23317()))) - 90.0f) - MeteorClient.mc.field_1724.method_36454());
    }

    public static double getPitch(class_2338 pos) {
        double diffX = (((double) pos.method_10263()) + 0.5d) - MeteorClient.mc.field_1724.method_23317();
        double diffY = (((double) pos.method_10264()) + 0.5d) - (MeteorClient.mc.field_1724.method_23318() + ((double) MeteorClient.mc.field_1724.method_18381(MeteorClient.mc.field_1724.method_18376())));
        double diffZ = (((double) pos.method_10260()) + 0.5d) - MeteorClient.mc.field_1724.method_23321();
        double diffXZ = Math.sqrt((diffX * diffX) + (diffZ * diffZ));
        return MeteorClient.mc.field_1724.method_36455() + class_3532.method_15393(((float) (-Math.toDegrees(Math.atan2(diffY, diffXZ)))) - MeteorClient.mc.field_1724.method_36455());
    }

    public static void setCamRotation(double yaw, double pitch) {
        serverYaw = (float) yaw;
        serverPitch = (float) pitch;
        rotationTimer = 0;
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/player/Rotations$Rotation.class */
    private static class Rotation {
        public double yaw;
        public double pitch;
        public int priority;
        public boolean clientSide;
        public Runnable callback;

        private Rotation() {
        }

        public void set(double yaw, double pitch, int priority, boolean clientSide, Runnable callback) {
            this.yaw = yaw;
            this.pitch = pitch;
            this.priority = priority;
            this.clientSide = clientSide;
            this.callback = callback;
        }

        public void sendPacket() {
            MeteorClient.mc.method_1562().method_52787(new class_2828.class_2831((float) this.yaw, (float) this.pitch, MeteorClient.mc.field_1724.method_24828(), MeteorClient.mc.field_1724.field_5976));
            runCallback();
        }

        public void runCallback() {
            if (this.callback != null) {
                this.callback.run();
            }
        }
    }
}
