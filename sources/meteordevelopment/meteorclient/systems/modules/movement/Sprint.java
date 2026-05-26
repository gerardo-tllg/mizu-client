package meteordevelopment.meteorclient.systems.modules.movement;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IPlayerInteractEntityC2SPacket;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_2824;
import net.minecraft.class_2848;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/movement/Sprint.class */
public class Sprint extends Module {
    private final SettingGroup sgGeneral;
    public final Setting<Mode> mode;
    public final Setting<Boolean> jumpFix;
    private final Setting<Boolean> keepSprint;
    private final Setting<Boolean> unsprintOnHit;
    private final Setting<Boolean> unsprintInWater;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/movement/Sprint$Mode.class */
    public enum Mode {
        Strict,
        Rage
    }

    public Sprint() {
        super(Categories.Movement, "sprint", "Automatically sprints.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.mode = this.sgGeneral.add(new EnumSetting.Builder().name("speed-mode").description("What mode of sprinting.").defaultValue(Mode.Strict).build());
        this.jumpFix = this.sgGeneral.add(new BoolSetting.Builder().name("jump-fix").description("Whether to correct jumping directions.").defaultValue(true).visible(() -> {
            return this.mode.get() == Mode.Rage;
        }).build());
        this.keepSprint = this.sgGeneral.add(new BoolSetting.Builder().name("keep-sprint").description("Whether to keep sprinting after attacking an entity.").defaultValue(false).build());
        this.unsprintOnHit = this.sgGeneral.add(new BoolSetting.Builder().name("unsprint-on-hit").description("Whether to stop sprinting when attacking, to ensure you get crits and sweep attacks.").defaultValue(false).build());
        this.unsprintInWater = this.sgGeneral.add(new BoolSetting.Builder().name("unsprint-in-water").description("Whether to stop sprinting when in water.").defaultValue(true).build());
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onDeactivate() {
        this.mc.field_1724.method_5728(false);
    }

    @EventHandler
    private void onTickMovement(TickEvent.Post event) {
        if (shouldSprint()) {
            this.mc.field_1724.method_5728(true);
        }
    }

    @EventHandler(priority = 100)
    private void onPacketSend(PacketEvent.Send event) {
        if (this.unsprintOnHit.get().booleanValue()) {
            IPlayerInteractEntityC2SPacket iPlayerInteractEntityC2SPacket = event.packet;
            if (iPlayerInteractEntityC2SPacket instanceof IPlayerInteractEntityC2SPacket) {
                IPlayerInteractEntityC2SPacket packet = iPlayerInteractEntityC2SPacket;
                if (packet.meteor$getType() != class_2824.class_5907.field_29172) {
                    return;
                }
                this.mc.method_1562().method_52787(new class_2848(this.mc.field_1724, class_2848.class_2849.field_12985));
                this.mc.field_1724.method_5728(false);
            }
        }
    }

    @EventHandler
    private void onPacketSent(PacketEvent.Sent event) {
        if (this.unsprintOnHit.get().booleanValue() && this.keepSprint.get().booleanValue()) {
            IPlayerInteractEntityC2SPacket iPlayerInteractEntityC2SPacket = event.packet;
            if (iPlayerInteractEntityC2SPacket instanceof IPlayerInteractEntityC2SPacket) {
                IPlayerInteractEntityC2SPacket packet = iPlayerInteractEntityC2SPacket;
                if (packet.meteor$getType() == class_2824.class_5907.field_29172 && shouldSprint() && !this.mc.field_1724.method_5624()) {
                    this.mc.method_1562().method_52787(new class_2848(this.mc.field_1724, class_2848.class_2849.field_12981));
                    this.mc.field_1724.method_5728(true);
                }
            }
        }
    }

    public boolean shouldSprint() {
        if (this.unsprintInWater.get().booleanValue() && (this.mc.field_1724.method_5799() || this.mc.field_1724.method_5869())) {
            return false;
        }
        boolean strictSprint = this.mc.field_1724.field_6250 > 1.0E-5f && this.mc.field_1724.invokeCanSprint() && (!this.mc.field_1724.field_5976 || this.mc.field_1724.field_34927) && (!this.mc.field_1724.method_5799() || this.mc.field_1724.method_5869());
        return isActive() && (this.mode.get() == Mode.Rage || strictSprint) && (this.mc.field_1755 == null || ((GUIMove) Modules.get().get(GUIMove.class)).sprint.get().booleanValue());
    }

    public boolean rageSprint() {
        return isActive() && this.mode.get() == Mode.Rage;
    }

    public boolean unsprintInWater() {
        return isActive() && this.unsprintInWater.get().booleanValue();
    }

    public boolean stopSprinting() {
        return (isActive() && this.keepSprint.get().booleanValue()) ? false : true;
    }
}
