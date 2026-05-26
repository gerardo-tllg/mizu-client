package meteordevelopment.meteorclient.systems.modules.movement;

import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.Vector3dSetting;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_124;
import net.minecraft.class_1304;
import net.minecraft.class_1657;
import net.minecraft.class_1802;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2661;
import net.minecraft.class_2848;
import org.joml.Vector3d;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/movement/AutoWasp.class */
public class AutoWasp extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Double> horizontalSpeed;
    private final Setting<Double> verticalSpeed;
    private final Setting<Boolean> avoidLanding;
    private final Setting<Boolean> predictMovement;
    private final Setting<Boolean> onlyFriends;
    private final Setting<Action> action;
    private final Setting<Vector3d> offset;
    public class_1657 target;
    private int jumpTimer;
    private boolean incrementJumpTimer;

    public AutoWasp() {
        super(Categories.Movement, "auto-wasp", "Wasps for you. Unable to traverse around blocks, assumes a clear straight line to the target.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.horizontalSpeed = this.sgGeneral.add(new DoubleSetting.Builder().name("horizontal-speed").description("Horizontal elytra speed.").defaultValue(2.0d).build());
        this.verticalSpeed = this.sgGeneral.add(new DoubleSetting.Builder().name("vertical-speed").description("Vertical elytra speed.").defaultValue(3.0d).build());
        this.avoidLanding = this.sgGeneral.add(new BoolSetting.Builder().name("avoid-landing").description("Will try to avoid landing if your target is on the ground.").defaultValue(true).build());
        this.predictMovement = this.sgGeneral.add(new BoolSetting.Builder().name("predict-movement").description("Tries to predict the targets position according to their movement.").defaultValue(true).build());
        this.onlyFriends = this.sgGeneral.add(new BoolSetting.Builder().name("only-friends").description("Will only follow friends.").defaultValue(false).build());
        this.action = this.sgGeneral.add(new EnumSetting.Builder().name("action-on-target-loss").description("What to do if you lose the target.").defaultValue(Action.TOGGLE).build());
        this.offset = this.sgGeneral.add(new Vector3dSetting.Builder().name("offset").description("How many blocks offset to wasp at from the target.").defaultValue(0.0d, 0.0d, 0.0d).build());
        this.jumpTimer = 0;
        this.incrementJumpTimer = false;
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        if (this.target == null || this.target.method_31481()) {
            this.target = TargetUtils.get(entity -> {
                if (!(entity instanceof class_1657) || entity == this.mc.field_1724 || ((class_1657) entity).method_29504() || ((class_1657) entity).method_6032() <= 0.0f) {
                    return false;
                }
                return (this.onlyFriends.get().booleanValue() && Friends.get().get((class_1657) entity) == null) ? false : true;
            }, SortPriority.LowestDistance);
            if (this.target == null) {
                error("No valid targets.", new Object[0]);
                toggle();
                return;
            }
            info(this.target.method_5477().getString() + " set as target.", new Object[0]);
        }
        this.jumpTimer = 0;
        this.incrementJumpTimer = false;
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onDeactivate() {
        this.target = null;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.target.method_31481()) {
            warning("Lost target!", new Object[0]);
            switch (this.action.get()) {
                case TOGGLE:
                    toggle();
                    break;
                case CHOOSE_NEW_TARGET:
                    onActivate();
                    break;
                case DISCONNECT:
                    this.mc.field_1724.field_3944.method_52781(new class_2661(class_2561.method_43470("%s[%sAuto Wasp%s] Lost target.".formatted(class_124.field_1080, class_124.field_1078, class_124.field_1080))));
                    break;
            }
            if (!isActive()) {
                return;
            }
        }
        if (this.mc.field_1724.method_6118(class_1304.field_6174).method_7909() != class_1802.field_8833) {
            return;
        }
        if (this.incrementJumpTimer) {
            this.jumpTimer++;
        }
        if (!this.mc.field_1724.method_6128()) {
            if (!this.incrementJumpTimer) {
                this.incrementJumpTimer = true;
            }
            if (this.mc.field_1724.method_24828() && this.incrementJumpTimer) {
                this.mc.field_1724.method_6043();
                return;
            } else {
                if (this.jumpTimer >= 4) {
                    this.jumpTimer = 0;
                    this.mc.field_1724.method_6100(false);
                    this.mc.field_1724.method_5728(true);
                    this.mc.method_1562().method_52787(new class_2848(this.mc.field_1724, class_2848.class_2849.field_12982));
                    return;
                }
                return;
            }
        }
        this.incrementJumpTimer = false;
        this.jumpTimer = 0;
    }

    @EventHandler
    private void onMove(PlayerMoveEvent event) {
        if (this.mc.field_1724.method_6118(class_1304.field_6174).method_7909() == class_1802.field_8833 && this.mc.field_1724.method_6128()) {
            double xVel = 0.0d;
            double yVel = 0.0d;
            double zVel = 0.0d;
            class_243 targetPos = this.target.method_19538().method_1031(this.offset.get().x, this.offset.get().y, this.offset.get().z);
            if (this.predictMovement.get().booleanValue()) {
                targetPos.method_1019(class_1657.method_20736(this.target, this.target.method_18798(), this.target.method_5829(), this.mc.field_1687, this.mc.field_1687.method_20743(this.target, this.target.method_5829().method_18804(this.target.method_18798()))));
            }
            if (this.avoidLanding.get().booleanValue()) {
                double d = this.target.method_5829().method_17939() / 2.0d;
                class_2350[] class_2350VarArr = class_2350.field_11041;
                int length = class_2350VarArr.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        break;
                    }
                    class_2350 dir = class_2350VarArr[i];
                    class_2338 pos = class_2338.method_49638(targetPos.method_43206(dir, d).method_43206(dir.method_10170(), d)).method_10074();
                    if (!this.mc.field_1687.method_8320(pos).method_26204().field_23159 || Math.abs(targetPos.method_10214() - ((double) (pos.method_10264() + 1))) > 0.25d) {
                        i++;
                    } else {
                        targetPos = new class_243(targetPos.field_1352, ((double) pos.method_10264()) + 1.25d, targetPos.field_1350);
                        break;
                    }
                }
            }
            double xDist = targetPos.method_10216() - this.mc.field_1724.method_23317();
            double zDist = targetPos.method_10215() - this.mc.field_1724.method_23321();
            double absX = Math.abs(xDist);
            double absZ = Math.abs(zDist);
            double diag = 0.0d;
            if (absX > 9.999999747378752E-6d && absZ > 9.999999747378752E-6d) {
                diag = 1.0d / Math.sqrt((absX * absX) + (absZ * absZ));
            }
            if (absX > 9.999999747378752E-6d) {
                xVel = absX < this.horizontalSpeed.get().doubleValue() ? xDist : this.horizontalSpeed.get().doubleValue() * Math.signum(xDist);
                if (diag != 0.0d) {
                    xVel *= absX * diag;
                }
            }
            if (absZ > 9.999999747378752E-6d) {
                zVel = absZ < this.horizontalSpeed.get().doubleValue() ? zDist : this.horizontalSpeed.get().doubleValue() * Math.signum(zDist);
                if (diag != 0.0d) {
                    zVel *= absZ * diag;
                }
            }
            double yDist = targetPos.method_10214() - this.mc.field_1724.method_23318();
            if (Math.abs(yDist) > 9.999999747378752E-6d) {
                yVel = Math.abs(yDist) < this.verticalSpeed.get().doubleValue() ? yDist : this.verticalSpeed.get().doubleValue() * Math.signum(yDist);
            }
            event.movement.meteor$set(xVel, yVel, zVel);
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/movement/AutoWasp$Action.class */
    public enum Action {
        TOGGLE,
        CHOOSE_NEW_TARGET,
        DISCONNECT;

        /* JADX INFO: Thrown type has an unknown type hierarchy: java.lang.MatchException */
        @Override // java.lang.Enum
        public String toString() throws MatchException {
            switch (this) {
                case TOGGLE:
                    return "Toggle module";
                case CHOOSE_NEW_TARGET:
                    return "Choose new target";
                case DISCONNECT:
                    return "Disconnect";
                default:
                    throw new MatchException((String) null, (Throwable) null);
            }
        }
    }
}
