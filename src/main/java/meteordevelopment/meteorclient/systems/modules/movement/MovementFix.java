package meteordevelopment.meteorclient.systems.modules.movement;

import meteordevelopment.meteorclient.events.entity.player.PlayerJumpEvent;
import meteordevelopment.meteorclient.events.entity.player.PlayerTravelEvent;
import meteordevelopment.meteorclient.events.entity.player.UpdatePlayerVelocity;
import meteordevelopment.meteorclient.events.input.KeyboardInputEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.util.math.MathHelper;

public class MovementFix extends Module {
    public static MovementFix MOVE_FIX;

    private final SettingGroup sgGeneral;
    private final Setting<Boolean> grimStrict;
    private final Setting<Boolean> grimCobwebSprintJump;
    private final Setting<Boolean> travel;
    public final Setting<MovementFix.UpdateMode> updateMode;

    public static boolean inWebs = false;
    public static boolean realInWebs = false;

    public static float fixYaw;
    public static float fixPitch;

    public static float prevYaw;
    public static float prevPitch;

    public static boolean setRot = false;

    private boolean preJumpSprint;

    public MovementFix() {
        super(Categories.Movement, "movement-fix", "Fixes movement for rotations");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.grimStrict = this.sgGeneral.add(new BoolSetting.Builder()
            .name("grim-strict")
            .description("Strict mode for Grim. Should be off for 2b2t.org and on for other Grim servers.")
            .defaultValue(false)
            .build());
        this.grimCobwebSprintJump = this.sgGeneral.add(new BoolSetting.Builder()
            .name("grim-cobweb-sprint-jump-fix")
            .description("Fixes rubberbanding when sprint jumping in cobwebs with no slow.")
            .defaultValue(true)
            .build());
        this.travel = this.sgGeneral.add(new BoolSetting.Builder()
            .name("travel")
            .description("Fixes rotation for travel events.")
            .defaultValue(true)
            .build());
        this.updateMode = this.sgGeneral.add(new EnumSetting.Builder<MovementFix.UpdateMode>()
            .name("update-mode")
            .description("When to fix movement.")
            .defaultValue(MovementFix.UpdateMode.Packet)
            .build());
        this.preJumpSprint = false;
        MOVE_FIX = this;
    }

    @EventHandler
    public void onTick(TickEvent.Post event) {
        realInWebs = inWebs;
        inWebs = false;
    }

    @EventHandler
    public void onPreJump(PlayerJumpEvent.Pre e) {
        if (!mc.player.isRiding() && !Modules.get().get(GrimDisabler.class).shouldSetYawOverflowRotation()) {
            prevYaw = mc.player.getYaw();
            prevPitch = mc.player.getPitch();
            mc.player.setYaw(fixYaw);
            mc.player.setPitch(fixPitch);
            setRot = true;

            if (realInWebs && mc.player.isSprinting() && grimCobwebSprintJump.get()) {
                preJumpSprint = mc.player.isSprinting();
                mc.player.setSprinting(false);
            }
        }
    }

    @EventHandler
    public void onPostJump(PlayerJumpEvent.Post e) {
        if (!mc.player.isRiding() && !Modules.get().get(GrimDisabler.class).shouldSetYawOverflowRotation()) {
            mc.player.setYaw(prevYaw);
            mc.player.setPitch(prevPitch);
            setRot = false;

            if (realInWebs && grimCobwebSprintJump.get()) {
                mc.player.setSprinting(preJumpSprint);
            }
        }
    }

    @EventHandler
    public void onPreTravel(PlayerTravelEvent.Pre e) {
        if (travel.get() && !mc.player.isRiding() && !Modules.get().get(GrimDisabler.class).shouldSetYawOverflowRotation()) {
            prevYaw = mc.player.getYaw();
            prevPitch = mc.player.getPitch();
            mc.player.setYaw(fixYaw);
            mc.player.setPitch(fixPitch);
            setRot = true;
        }
    }

    @EventHandler
    public void onPostTravel(PlayerTravelEvent.Post e) {
        if (travel.get() && !mc.player.isRiding() && !Modules.get().get(GrimDisabler.class).shouldSetYawOverflowRotation()) {
            mc.player.setYaw(prevYaw);
            mc.player.setPitch(prevPitch);
            setRot = false;
        }
    }

    @EventHandler
    public void onPlayerMove(UpdatePlayerVelocity event) {
        if (!mc.player.isRiding() && !Modules.get().get(GrimDisabler.class).shouldSetYawOverflowRotation()) {
            event.cancel();
            event.setVelocity(PlayerUtils.movementInputToVelocity(event.getMovementInput(), event.getSpeed(), fixYaw));
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onKeyInput(KeyboardInputEvent e) {
        if (!mc.player.isRiding() && !Modules.get().get(Freecam.class).isActive() && !mc.player.isGliding() && !Modules.get().get(GrimDisabler.class).shouldSetYawOverflowRotation()) {
            float mF = mc.player.forwardSpeed;
            float mS = mc.player.sidewaysSpeed;
            float delta = (mc.player.getYaw() - fixYaw) * MathHelper.RADIANS_PER_DEGREE;
            float cos = MathHelper.cos(delta);
            float sin = MathHelper.sin(delta);

            if (grimStrict.get()) {
                mc.player.sidewaysSpeed = (float) Math.round(mS * cos - mF * sin);
                mc.player.forwardSpeed = (float) Math.round(mF * cos + mS * sin);
            } else {
                mc.player.sidewaysSpeed = mS * cos - mF * sin;
                mc.player.forwardSpeed = mF * cos + mS * sin;
            }
        }
    }

    public enum UpdateMode {
        Packet,
        Mouse,
        Both
    }
}
