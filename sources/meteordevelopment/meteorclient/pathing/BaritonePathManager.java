package meteordevelopment.meteorclient.pathing;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalGetToBlock;
import baritone.api.pathing.goals.GoalXZ;
import baritone.api.process.IBaritoneProcess;
import baritone.api.process.PathingCommand;
import baritone.api.process.PathingCommandType;
import baritone.api.utils.Rotation;
import baritone.api.utils.SettingsUtil;
import java.lang.invoke.VarHandle;
import java.util.function.Predicate;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.pathing.IPathManager;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1297;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_3532;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/pathing/BaritonePathManager.class */
public class BaritonePathManager implements IPathManager {
    private final VarHandle rotationField;
    private final BaritoneSettings settings;
    private GoalDirection directionGoal;
    private boolean pathingPaused;

    /* JADX WARN: Code restructure failed: missing block: B:12:0x0063, code lost:
    
        r5.rotationField = r7;
        r5.settings = new meteordevelopment.meteorclient.pathing.BaritoneSettings();
        baritone.api.BaritoneAPI.getProvider().getPrimaryBaritone().getPathingControlManager().registerProcess(new meteordevelopment.meteorclient.pathing.BaritonePathManager.BaritoneProcess(r5));
     */
    /* JADX WARN: Code restructure failed: missing block: B:13:0x008d, code lost:
    
        return;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public BaritonePathManager() {
        /*
            r5 = this;
            r0 = r5
            r0.<init>()
            meteordevelopment.orbit.IEventBus r0 = meteordevelopment.meteorclient.MeteorClient.EVENT_BUS
            r1 = r5
            r0.subscribe(r1)
            baritone.api.IBaritoneProvider r0 = baritone.api.BaritoneAPI.getProvider()
            baritone.api.IBaritone r0 = r0.getPrimaryBaritone()
            baritone.api.behavior.ILookBehavior r0 = r0.getLookBehavior()
            java.lang.Class r0 = r0.getClass()
            r6 = r0
            r0 = 0
            r7 = r0
            r0 = r6
            java.lang.reflect.Field[] r0 = r0.getDeclaredFields()
            r8 = r0
            r0 = r8
            int r0 = r0.length
            r9 = r0
            r0 = 0
            r10 = r0
        L2e:
            r0 = r10
            r1 = r9
            if (r0 >= r1) goto L63
            r0 = r8
            r1 = r10
            r0 = r0[r1]
            r11 = r0
            r0 = r11
            java.lang.Class r0 = r0.getType()
            java.lang.Class<baritone.api.utils.Rotation> r1 = baritone.api.utils.Rotation.class
            if (r0 != r1) goto L5d
            java.lang.invoke.MethodHandles$Lookup r0 = java.lang.invoke.MethodHandles.lookup()     // Catch: java.lang.IllegalAccessException -> L51
            r1 = r11
            java.lang.invoke.VarHandle r0 = r0.unreflectVarHandle(r1)     // Catch: java.lang.IllegalAccessException -> L51
            r7 = r0
            goto L63
        L51:
            r12 = move-exception
            java.lang.RuntimeException r0 = new java.lang.RuntimeException
            r1 = r0
            r2 = r12
            r1.<init>(r2)
            throw r0
        L5d:
            int r10 = r10 + 1
            goto L2e
        L63:
            r0 = r5
            r1 = r7
            r0.rotationField = r1
            r0 = r5
            meteordevelopment.meteorclient.pathing.BaritoneSettings r1 = new meteordevelopment.meteorclient.pathing.BaritoneSettings
            r2 = r1
            r2.<init>()
            r0.settings = r1
            baritone.api.IBaritoneProvider r0 = baritone.api.BaritoneAPI.getProvider()
            baritone.api.IBaritone r0 = r0.getPrimaryBaritone()
            baritone.api.pathing.calc.IPathingControlManager r0 = r0.getPathingControlManager()
            meteordevelopment.meteorclient.pathing.BaritonePathManager$BaritoneProcess r1 = new meteordevelopment.meteorclient.pathing.BaritonePathManager$BaritoneProcess
            r2 = r1
            r3 = r5
            r2.<init>()
            r0.registerProcess(r1)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: meteordevelopment.meteorclient.pathing.BaritonePathManager.<init>():void");
    }

    @Override // meteordevelopment.meteorclient.pathing.IPathManager
    public String getName() {
        return "Baritone";
    }

    @Override // meteordevelopment.meteorclient.pathing.IPathManager
    public boolean isPathing() {
        return BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing();
    }

    @Override // meteordevelopment.meteorclient.pathing.IPathManager
    public void pause() {
        this.pathingPaused = true;
    }

    @Override // meteordevelopment.meteorclient.pathing.IPathManager
    public void resume() {
        this.pathingPaused = false;
    }

    @Override // meteordevelopment.meteorclient.pathing.IPathManager
    public void stop() {
        BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();
    }

    @Override // meteordevelopment.meteorclient.pathing.IPathManager
    public void moveTo(class_2338 pos, boolean ignoreY) {
        if (ignoreY) {
            BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalXZ(pos.method_10263(), pos.method_10260()));
        } else {
            BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalGetToBlock(pos));
        }
    }

    @Override // meteordevelopment.meteorclient.pathing.IPathManager
    public void moveInDirection(float yaw) {
        this.directionGoal = new GoalDirection(yaw);
        BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(this.directionGoal);
    }

    @Override // meteordevelopment.meteorclient.pathing.IPathManager
    public void mine(class_2248... blocks) {
        BaritoneAPI.getProvider().getPrimaryBaritone().getMineProcess().mine(blocks);
    }

    @Override // meteordevelopment.meteorclient.pathing.IPathManager
    public void follow(Predicate<class_1297> entity) {
        BaritoneAPI.getProvider().getPrimaryBaritone().getFollowProcess().follow(entity);
    }

    @Override // meteordevelopment.meteorclient.pathing.IPathManager
    public float getTargetYaw() {
        Rotation rotation = this.rotationField.get(BaritoneAPI.getProvider().getPrimaryBaritone().getLookBehavior());
        if (rotation == null) {
            return 0.0f;
        }
        return rotation.getYaw();
    }

    @Override // meteordevelopment.meteorclient.pathing.IPathManager
    public float getTargetPitch() {
        Rotation rotation = this.rotationField.get(BaritoneAPI.getProvider().getPrimaryBaritone().getLookBehavior());
        if (rotation == null) {
            return 0.0f;
        }
        return rotation.getPitch();
    }

    @Override // meteordevelopment.meteorclient.pathing.IPathManager
    public IPathManager.ISettings getSettings() {
        return this.settings;
    }

    @EventHandler(priority = 200)
    private void onTick(TickEvent.Pre event) {
        if (this.directionGoal == null) {
            return;
        }
        if (this.directionGoal != BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().getGoal()) {
            this.directionGoal = null;
        } else {
            this.directionGoal.tick();
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/pathing/BaritonePathManager$GoalDirection.class */
    private static class GoalDirection implements Goal {
        private static final double SQRT_2 = Math.sqrt(2.0d);
        private final float yaw;
        private int x;
        private int z;
        private int timer;

        public GoalDirection(float yaw) {
            this.yaw = yaw;
            tick();
        }

        public static double calculate(double xDiff, double zDiff) {
            double straight;
            double diagonal;
            double x = Math.abs(xDiff);
            double z = Math.abs(zDiff);
            if (x < z) {
                straight = z - x;
                diagonal = x;
            } else {
                straight = x - z;
                diagonal = z;
            }
            return ((diagonal * SQRT_2) + straight) * ((Double) BaritoneAPI.getSettings().costHeuristic.value).doubleValue();
        }

        public void tick() {
            if (this.timer <= 0) {
                this.timer = 20;
                class_243 pos = MeteorClient.mc.field_1724.method_19538();
                float theta = (float) Math.toRadians(this.yaw);
                this.x = (int) Math.floor(pos.field_1352 - (((double) class_3532.method_15374(theta)) * 100.0d));
                this.z = (int) Math.floor(pos.field_1350 + (((double) class_3532.method_15362(theta)) * 100.0d));
            }
            this.timer--;
        }

        public boolean isInGoal(int x, int y, int z) {
            return x == this.x && z == this.z;
        }

        public double heuristic(int x, int y, int z) {
            int xDiff = x - this.x;
            int zDiff = z - this.z;
            return calculate(xDiff, zDiff);
        }

        public String toString() {
            return String.format("GoalXZ{x=%s,z=%s}", SettingsUtil.maybeCensor(this.x), SettingsUtil.maybeCensor(this.z));
        }

        public int getX() {
            return this.x;
        }

        public int getZ() {
            return this.z;
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/pathing/BaritonePathManager$BaritoneProcess.class */
    private class BaritoneProcess implements IBaritoneProcess {
        private BaritoneProcess() {
        }

        public boolean isActive() {
            return BaritonePathManager.this.pathingPaused;
        }

        public PathingCommand onTick(boolean b, boolean b1) {
            BaritoneAPI.getProvider().getPrimaryBaritone().getInputOverrideHandler().clearAllKeys();
            return new PathingCommand((Goal) null, PathingCommandType.REQUEST_PAUSE);
        }

        public boolean isTemporary() {
            return true;
        }

        public void onLostControl() {
        }

        public double priority() {
            return 0.0d;
        }

        public String displayName0() {
            return "MasterClient";
        }
    }
}
