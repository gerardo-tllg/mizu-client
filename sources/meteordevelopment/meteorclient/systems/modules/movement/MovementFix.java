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
import net.minecraft.class_3532;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/movement/MovementFix.class */
public class MovementFix extends Module {
    public static MovementFix MOVE_FIX;
    private final SettingGroup sgGeneral;
    private final Setting<Boolean> grimStrict;
    private final Setting<Boolean> grimCobwebSprintJump;
    private final Setting<Boolean> travel;
    public final Setting<UpdateMode> updateMode;
    public static float fixYaw;
    public static float fixPitch;
    public static float prevYaw;
    public static float prevPitch;
    private boolean preJumpSprint;
    public static boolean inWebs = false;
    public static boolean realInWebs = false;
    public static boolean setRot = false;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/movement/MovementFix$UpdateMode.class */
    public enum UpdateMode {
        Packet,
        Mouse,
        Both
    }

    public MovementFix() {
        super(Categories.Movement, "movement-fix", "Fixes movement for rotations");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.grimStrict = this.sgGeneral.add(new BoolSetting.Builder().name("grim-strict").description("Strict mode for Grim. Should be off for 2b2t.org and on for other Grim servers.").defaultValue(false).build());
        this.grimCobwebSprintJump = this.sgGeneral.add(new BoolSetting.Builder().name("grim-cobweb-sprint-jump-fix").description("Fixes rubberbanding when sprint jumping in cobwebs with no slow.").defaultValue(true).build());
        this.travel = this.sgGeneral.add(new BoolSetting.Builder().name("travel").description("Fixes rotation for travel events.").defaultValue(true).build());
        this.updateMode = this.sgGeneral.add(new EnumSetting.Builder().name("update-mode").description("When to fix movement.").defaultValue(UpdateMode.Packet).build());
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
        if (!this.mc.field_1724.method_3144() && !((GrimDisabler) Modules.get().get(GrimDisabler.class)).shouldSetYawOverflowRotation()) {
            prevYaw = this.mc.field_1724.method_36454();
            prevPitch = this.mc.field_1724.method_36455();
            this.mc.field_1724.method_36456(fixYaw);
            this.mc.field_1724.method_36457(fixPitch);
            setRot = true;
            if (realInWebs && this.mc.field_1724.method_5624() && this.grimCobwebSprintJump.get().booleanValue()) {
                this.preJumpSprint = this.mc.field_1724.method_5624();
                this.mc.field_1724.method_5728(false);
            }
        }
    }

    @EventHandler
    public void onPostJump(PlayerJumpEvent.Post e) {
        if (!this.mc.field_1724.method_3144() && !((GrimDisabler) Modules.get().get(GrimDisabler.class)).shouldSetYawOverflowRotation()) {
            this.mc.field_1724.method_36456(prevYaw);
            this.mc.field_1724.method_36457(prevPitch);
            setRot = false;
            if (realInWebs && this.grimCobwebSprintJump.get().booleanValue()) {
                this.mc.field_1724.method_5728(this.preJumpSprint);
            }
        }
    }

    @EventHandler
    public void onPreTravel(PlayerTravelEvent.Pre e) {
        if (this.travel.get().booleanValue() && !this.mc.field_1724.method_3144() && !((GrimDisabler) Modules.get().get(GrimDisabler.class)).shouldSetYawOverflowRotation()) {
            prevYaw = this.mc.field_1724.method_36454();
            prevPitch = this.mc.field_1724.method_36455();
            this.mc.field_1724.method_36456(fixYaw);
            this.mc.field_1724.method_36457(fixPitch);
            setRot = true;
        }
    }

    @EventHandler
    public void onPostTravel(PlayerTravelEvent.Post e) {
        if (this.travel.get().booleanValue() && !this.mc.field_1724.method_3144() && !((GrimDisabler) Modules.get().get(GrimDisabler.class)).shouldSetYawOverflowRotation()) {
            this.mc.field_1724.method_36456(prevYaw);
            this.mc.field_1724.method_36457(prevPitch);
            setRot = false;
        }
    }

    @EventHandler
    public void onPlayerMove(UpdatePlayerVelocity event) {
        if (!this.mc.field_1724.method_3144() && !((GrimDisabler) Modules.get().get(GrimDisabler.class)).shouldSetYawOverflowRotation()) {
            event.cancel();
            event.setVelocity(PlayerUtils.movementInputToVelocity(event.getMovementInput(), event.getSpeed(), fixYaw));
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onKeyInput(KeyboardInputEvent e) {
        if (!this.mc.field_1724.method_3144() && !((Freecam) Modules.get().get(Freecam.class)).isActive() && !this.mc.field_1724.method_6128() && !((GrimDisabler) Modules.get().get(GrimDisabler.class)).shouldSetYawOverflowRotation()) {
            float mF = this.mc.field_1724.field_6250;
            float mS = this.mc.field_1724.field_6212;
            float delta = (this.mc.field_1724.method_36454() - fixYaw) * 0.017453292f;
            float cos = class_3532.method_15362(delta);
            float sin = class_3532.method_15374(delta);
            if (this.grimStrict.get().booleanValue()) {
                this.mc.field_1724.field_6212 = Math.round((mS * cos) - (mF * sin));
                this.mc.field_1724.field_6250 = Math.round((mF * cos) + (mS * sin));
                return;
            }
            this.mc.field_1724.field_6212 = (mS * cos) - (mF * sin);
            this.mc.field_1724.field_6250 = (mF * cos) + (mS * sin);
        }
    }
}
