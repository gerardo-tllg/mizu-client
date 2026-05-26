package meteordevelopment.meteorclient.systems.managers;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.player.LookAtEvent;
import meteordevelopment.meteorclient.events.entity.player.PlayerJumpEvent;
import meteordevelopment.meteorclient.events.entity.player.PlayerTravelEvent;
import meteordevelopment.meteorclient.events.entity.player.RotateEvent;
import meteordevelopment.meteorclient.events.entity.player.SendMovementPacketsEvent;
import meteordevelopment.meteorclient.events.entity.player.UpdatePlayerVelocity;
import meteordevelopment.meteorclient.events.input.KeyboardInputEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.systems.config.AntiCheatConfig;
import meteordevelopment.meteorclient.systems.modules.movement.MovementFix;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.class_10182;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2708;
import net.minecraft.class_2709;
import net.minecraft.class_2828;
import net.minecraft.class_3532;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/managers/RotationManager.class */
public class RotationManager {
    public float nextYaw;
    public float nextPitch;
    private static float renderPitch;
    private static float renderYawOffset;
    private static float prevPitch;
    private static float prevRenderYawOffset;
    private static float prevRotationYawHead;
    private static float rotationYawHead;
    private int ticksExisted;
    public static boolean lastGround;
    private static final long ROTATION_COOLDOWN = 15;
    public static boolean sendDisablerPacket = false;
    public static float lastActualYaw = 0.0f;
    public static class_243 targetVec = null;
    private static final RotationRequest request = new RotationRequest();
    public float rotationYaw = 0.0f;
    public float rotationPitch = 0.0f;
    public float lastYaw = 0.0f;
    public float lastPitch = 0.0f;
    public double lastX = 0.0d;
    public double lastY = 0.0d;
    public double lastZ = 0.0d;
    private boolean shouldFulfilRequest = false;
    private boolean rotationLocked = false;
    private long lastRotationTime = 0;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/managers/RotationManager$RotationRequest.class */
    public static class RotationRequest {
        public double priority;
        public float yaw;
        public float pitch;
        public boolean fulfilled = false;
        public Runnable callback = null;
        public long timestamp = 0;
    }

    public RotationManager() {
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    public void snapAt(class_243 target) {
        float[] angle = getRotation(target);
        if (AntiCheatConfig.get().grimSnapRotation.get().booleanValue()) {
            MeteorClient.mc.method_1562().method_52787(new class_2828.class_2830(this.lastX, this.lastY, this.lastZ, angle[0], angle[1], lastGround, MeteorClient.mc.field_1724.field_5976));
        } else {
            MeteorClient.mc.method_1562().method_52787(new class_2828.class_2831(angle[0], angle[1], lastGround, MeteorClient.mc.field_1724.field_5976));
        }
    }

    public void snapAt(float yaw, float pitch) {
        if (AntiCheatConfig.get().grimSnapRotation.get().booleanValue()) {
            MeteorClient.mc.method_1562().method_52787(new class_2828.class_2830(this.lastX, this.lastY, this.lastZ, yaw, pitch, lastGround, MeteorClient.mc.field_1724.field_5976));
        } else {
            MeteorClient.mc.method_1562().method_52787(new class_2828.class_2831(yaw, pitch, lastGround, MeteorClient.mc.field_1724.field_5976));
        }
    }

    public void requestRotation(class_243 target, double priority) {
        if (!isRotationLocked() || priority >= 900.0d) {
            float[] angle = getRotation(target);
            requestRotation(angle[0], angle[1], priority, null);
        }
    }

    public void requestRotation(class_243 target, double priority, Runnable callback) {
        if (!isRotationLocked() || priority >= 900.0d) {
            float[] angle = getRotation(target);
            requestRotation(angle[0], angle[1], priority, callback);
        }
    }

    public void requestRotation(float yaw, float pitch, double priority) {
        if (!isRotationLocked() || priority >= 900.0d) {
            requestRotation(yaw, pitch, priority, null);
        }
    }

    public void requestRotation(float yaw, float pitch, double priority, Runnable callback) {
        if (request.priority <= priority || request.fulfilled || priority >= 900.0d) {
            boolean smallRotationChange = Math.abs(class_3532.method_15393(yaw - request.yaw)) < 1.0f && Math.abs(class_3532.method_15393(pitch - request.pitch)) < 1.0f;
            if (request.priority < priority || request.fulfilled || !smallRotationChange) {
                request.fulfilled = false;
                request.yaw = yaw;
                request.pitch = pitch;
                request.priority = priority;
                request.callback = callback;
                request.timestamp = System.currentTimeMillis();
                this.rotationLocked = true;
                this.lastRotationTime = System.currentTimeMillis();
            }
        }
    }

    private boolean isRotationLocked() {
        long currentTime = System.currentTimeMillis();
        if (this.rotationLocked && currentTime - this.lastRotationTime > ROTATION_COOLDOWN) {
            this.rotationLocked = false;
        }
        return this.rotationLocked;
    }

    public float[] getRotation(class_243 eyesPos, class_243 vec) {
        double diffX = vec.field_1352 - eyesPos.field_1352;
        double diffY = vec.field_1351 - eyesPos.field_1351;
        double diffZ = vec.field_1350 - eyesPos.field_1350;
        double diffXZ = Math.sqrt((diffX * diffX) + (diffZ * diffZ));
        float yaw = ((float) Math.toDegrees(Math.atan2(diffZ, diffX))) - 90.0f;
        float pitch = (float) (-Math.toDegrees(Math.atan2(diffY, diffXZ)));
        return new float[]{class_3532.method_15393(yaw), class_3532.method_15393(pitch)};
    }

    public float[] getRotation(class_243 vec) {
        class_243 eyesPos = MeteorClient.mc.field_1724.method_33571();
        return getRotation(eyesPos, vec);
    }

    public boolean lookingAt(class_238 box) {
        return lookingAt(this.lastYaw, this.lastPitch, box);
    }

    public boolean lookingAt(float yaw, float pitch, class_238 box) {
        return raytraceCheck(MeteorClient.mc.field_1724.method_33571(), yaw, pitch, box);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLastRotation(RotateEvent event) {
        LookAtEvent lookAtEvent = new LookAtEvent();
        MeteorClient.EVENT_BUS.post(lookAtEvent);
        this.shouldFulfilRequest = false;
        if (request != null && !request.fulfilled && request.priority > lookAtEvent.priority) {
            event.setYaw(request.yaw);
            event.setPitch(request.pitch);
            this.shouldFulfilRequest = true;
        } else if (lookAtEvent.getRotation()) {
            event.setYaw(lookAtEvent.getYaw());
            event.setPitch(lookAtEvent.getPitch());
        } else if (lookAtEvent.getTarget() != null) {
            float[] newAngle = getRotation(lookAtEvent.getTarget());
            event.setYaw(newAngle[0]);
            event.setPitch(newAngle[1]);
        }
    }

    public boolean isHighPriorityRotationActive(double threshold) {
        if (this.rotationLocked && request != null && !request.fulfilled && request.priority >= threshold) {
            long age = System.currentTimeMillis() - request.timestamp;
            if (age < 500) {
                return true;
            }
            return false;
        }
        return false;
    }

    @EventHandler(priority = -999)
    public void onPacketSend(PacketEvent.Send event) {
        if (MeteorClient.mc.field_1724 == null || event.isCancelled()) {
            return;
        }
        class_2828 class_2828Var = event.packet;
        if (class_2828Var instanceof class_2828) {
            class_2828 packet = class_2828Var;
            if (packet.method_36172()) {
                this.lastYaw = packet.method_12271(this.lastYaw);
                if (sendDisablerPacket) {
                    sendDisablerPacket = false;
                    this.lastYaw = lastActualYaw;
                }
                this.lastPitch = packet.method_12270(this.lastPitch);
                setRenderRotation(this.lastYaw, this.lastPitch, false);
            }
            if (packet.method_36171()) {
                this.lastX = packet.method_12269(this.lastX);
                this.lastY = packet.method_12268(this.lastY);
                this.lastZ = packet.method_12274(this.lastZ);
            }
            lastGround = packet.method_12273();
        }
    }

    @EventHandler(priority = 100)
    public void onReceivePacket(PacketEvent.Receive event) {
        if (MeteorClient.mc.field_1724 == null) {
            return;
        }
        class_2708 class_2708Var = event.packet;
        if (class_2708Var instanceof class_2708) {
            class_2708 packet = class_2708Var;
            class_10182 change = packet.comp_3228();
            if (packet.comp_3229().contains(class_2709.field_12401)) {
                this.lastYaw += change.comp_3150();
            } else {
                this.lastYaw = change.comp_3150();
            }
            if (packet.comp_3229().contains(class_2709.field_12397)) {
                this.lastPitch += change.comp_3151();
            } else {
                this.lastPitch = change.comp_3151();
            }
            if (packet.comp_3229().contains(class_2709.field_12400)) {
                this.lastX += change.comp_3148().field_1352;
            } else {
                this.lastX = change.comp_3148().field_1352;
            }
            if (packet.comp_3229().contains(class_2709.field_12398)) {
                this.lastY += change.comp_3148().field_1351;
            } else {
                this.lastY = change.comp_3148().field_1351;
            }
            if (packet.comp_3229().contains(class_2709.field_12403)) {
                this.lastZ += change.comp_3148().field_1350;
            } else {
                this.lastZ = change.comp_3148().field_1350;
            }
            setRenderRotation(this.lastYaw, this.lastPitch, true);
        }
    }

    @EventHandler
    public void onUpdateWalkingPost(SendMovementPacketsEvent.Post event) {
        setRenderRotation(this.lastYaw, this.lastPitch, false);
        this.lastYaw = MeteorClient.mc.field_1724.method_36454();
        this.lastPitch = MeteorClient.mc.field_1724.method_36455();
        this.lastX = MeteorClient.mc.field_1724.method_23317();
        this.lastY = MeteorClient.mc.field_1724.method_23318();
        this.lastZ = MeteorClient.mc.field_1724.method_23321();
        lastGround = MeteorClient.mc.field_1724.method_24828();
    }

    @EventHandler
    public void onMovementPacket(SendMovementPacketsEvent.Rotation event) {
        if (AntiCheatConfig.get().tickSync.get().booleanValue()) {
            if (this.shouldFulfilRequest && !request.fulfilled) {
                request.fulfilled = true;
                this.shouldFulfilRequest = false;
            }
            if (MovementFix.MOVE_FIX.isActive()) {
                event.yaw = this.nextYaw;
                event.pitch = this.nextPitch;
            } else {
                RotateEvent rotateEvent = new RotateEvent(event.yaw, event.pitch);
                MeteorClient.EVENT_BUS.post(rotateEvent);
                event.yaw = rotateEvent.getYaw();
                event.pitch = rotateEvent.getPitch();
            }
            if (AntiCheatConfig.get().grimSync.get().booleanValue()) {
                event.forceFull = true;
            }
            if (AntiCheatConfig.get().grimRotation.get().booleanValue()) {
                event.forceFullOnRotate = true;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onUpdatePlayerVelocity(UpdatePlayerVelocity event) {
        if (MovementFix.MOVE_FIX.isActive() && MovementFix.MOVE_FIX.updateMode.get() != MovementFix.UpdateMode.Mouse) {
            moveFixRotation();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreJump(PlayerJumpEvent.Pre event) {
        if (MovementFix.MOVE_FIX.isActive() && MovementFix.MOVE_FIX.updateMode.get() != MovementFix.UpdateMode.Mouse) {
            moveFixRotation();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onTravel(PlayerTravelEvent.Pre event) {
        if (MovementFix.MOVE_FIX.isActive() && MovementFix.MOVE_FIX.updateMode.get() != MovementFix.UpdateMode.Mouse) {
            moveFixRotation();
        }
    }

    @EventHandler(priority = 200)
    public void onKeyInput(KeyboardInputEvent event) {
        if (MovementFix.MOVE_FIX.isActive() && MovementFix.MOVE_FIX.updateMode.get() != MovementFix.UpdateMode.Mouse) {
            moveFixRotation();
        }
    }

    private void moveFixRotation() {
        if (MovementFix.setRot) {
            MeteorClient.mc.field_1724.method_36456(MovementFix.prevYaw);
            MeteorClient.mc.field_1724.method_36457(MovementFix.prevPitch);
        }
        RotateEvent rotateEvent = new RotateEvent(MeteorClient.mc.field_1724.method_36454(), MeteorClient.mc.field_1724.method_36455());
        MeteorClient.EVENT_BUS.post(rotateEvent);
        this.nextYaw = rotateEvent.getYaw();
        this.nextPitch = rotateEvent.getPitch();
        MovementFix.fixYaw = this.nextYaw;
        MovementFix.fixPitch = this.nextPitch;
        if (MovementFix.setRot) {
            MeteorClient.mc.field_1724.method_36456(MovementFix.fixYaw);
            MeteorClient.mc.field_1724.method_36457(MovementFix.fixPitch);
        }
    }

    public boolean raytraceCheck(class_243 pos, double yaw, double pitch, class_238 box) {
        class_243 vec = new class_243(Math.cos(Math.toRadians(yaw + 90.0d)) * Math.abs(Math.cos(Math.toRadians(pitch))), -Math.sin(Math.toRadians(pitch)), Math.sin(Math.toRadians(yaw + 90.0d)) * Math.abs(Math.cos(Math.toRadians(pitch))));
        double rayX = pos.field_1352;
        double rayY = pos.field_1351;
        double rayZ = pos.field_1350;
        double dirX = vec.field_1352;
        double dirY = vec.field_1351;
        double dirZ = vec.field_1350;
        double minX = box.field_1323;
        double minY = box.field_1322;
        double minZ = box.field_1321;
        double maxX = box.field_1320;
        double maxY = box.field_1325;
        double maxZ = box.field_1324;
        double invDirX = dirX != 0.0d ? 1.0d / dirX : 1.0E10d;
        double invDirY = dirY != 0.0d ? 1.0d / dirY : 1.0E10d;
        double invDirZ = dirZ != 0.0d ? 1.0d / dirZ : 1.0E10d;
        double tMinX = (minX - rayX) * invDirX;
        double tMaxX = (maxX - rayX) * invDirX;
        if (tMinX > tMaxX) {
            tMinX = tMaxX;
            tMaxX = tMinX;
        }
        double tMinY = (minY - rayY) * invDirY;
        double tMaxY = (maxY - rayY) * invDirY;
        if (tMinY > tMaxY) {
            tMinY = tMaxY;
            tMaxY = tMinY;
        }
        double tMinZ = (minZ - rayZ) * invDirZ;
        double tMaxZ = (maxZ - rayZ) * invDirZ;
        if (tMinZ > tMaxZ) {
            tMinZ = tMaxZ;
            tMaxZ = tMinZ;
        }
        double tMin = Math.max(Math.max(tMinX, tMinY), tMinZ);
        double tMax = Math.min(Math.min(tMaxX, tMaxY), tMaxZ);
        return tMax >= 0.0d && tMin <= tMax;
    }

    public void setRenderRotation(float yaw, float pitch, boolean force) {
        if (MeteorClient.mc.field_1724 != null) {
            if (MeteorClient.mc.field_1724.field_6012 != this.ticksExisted || force) {
                this.ticksExisted = MeteorClient.mc.field_1724.field_6012;
                prevPitch = renderPitch;
                prevRenderYawOffset = renderYawOffset;
                renderYawOffset = getRenderYawOffset(yaw, prevRenderYawOffset);
                prevRotationYawHead = rotationYawHead;
                rotationYawHead = yaw;
                renderPitch = pitch;
            }
        }
    }

    public static float getRenderPitch() {
        return renderPitch;
    }

    public static float getRotationYawHead() {
        return rotationYawHead;
    }

    public static float getRenderYawOffset() {
        return renderYawOffset;
    }

    public static float getPrevPitch() {
        return prevPitch;
    }

    public static float getPrevRotationYawHead() {
        return prevRotationYawHead;
    }

    public static float getPrevRenderYawOffset() {
        return prevRenderYawOffset;
    }

    private float getRenderYawOffset(float yaw, float offsetIn) {
        float result = offsetIn;
        double xDif = MeteorClient.mc.field_1724.method_23317() - MeteorClient.mc.field_1724.field_6014;
        double zDif = MeteorClient.mc.field_1724.method_23321() - MeteorClient.mc.field_1724.field_5969;
        if ((xDif * xDif) + (zDif * zDif) > 0.002500000176951289d) {
            float offset = (((float) class_3532.method_15349(zDif, xDif)) * 57.295776f) - 90.0f;
            float wrap = class_3532.method_15379(class_3532.method_15393(yaw) - offset);
            if (95.0f < wrap && wrap < 265.0f) {
                result = offset - 180.0f;
            } else {
                result = offset;
            }
        }
        if (MeteorClient.mc.field_1724.field_6251 > 0.0f) {
            result = yaw;
        }
        float offset2 = class_3532.method_15393(yaw - (offsetIn + (class_3532.method_15393(result - offsetIn) * 0.3f)));
        if (offset2 < -75.0f) {
            offset2 = -75.0f;
        } else if (offset2 >= 75.0f) {
            offset2 = 75.0f;
        }
        float result2 = yaw - offset2;
        if (offset2 * offset2 > 2500.0f) {
            result2 += offset2 * 0.2f;
        }
        return result2;
    }
}
