package meteordevelopment.meteorclient.systems.modules.hunting;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BlockPosSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.input.Input;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2661;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/hunting/GotoPosition.class */
public class GotoPosition extends Module {
    private final SettingGroup sgGeneral;
    public final Setting<class_2338> target;
    public final Setting<Boolean> disconnectOnComplete;

    public GotoPosition() {
        super(Categories.Hunting, "goto-position", "Goes in a straight line towards the position you give and stops once there.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.target = this.sgGeneral.add(new BlockPosSetting.Builder().name("target-position").description("Coords to go to. Y is ignored.").defaultValue(new class_2338(0, 0, 0)).build());
        this.disconnectOnComplete = this.sgGeneral.add(new BoolSetting.Builder().name("disconnect-when-complete").description("Disconnects when you get to the target").defaultValue(false).build());
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        if (this.mc.field_1724 == null) {
            return;
        }
        double distance = Math.sqrt(this.mc.field_1724.method_24515().method_40081(this.target.get().method_10263(), this.mc.field_1724.method_23318(), this.target.get().method_10260()));
        long totalSeconds = (long) (distance / 70.0d);
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        info("Completion will take an estimated %02d hours %02d minutes %02d seconds at an average speed of 70bps", Long.valueOf(hours), Long.valueOf(minutes), Long.valueOf(seconds));
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onDeactivate() {
        if (this.mc.field_1724 == null) {
            return;
        }
        this.mc.field_1690.field_1894.method_23481(false);
        Input.setKeyState(this.mc.field_1690.field_1894, false);
        this.mc.field_1724.method_18800(0.0d, 0.0d, 0.0d);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (this.mc.field_1724 == null) {
            return;
        }
        if (Math.sqrt(this.mc.field_1724.method_24515().method_40081(this.target.get().method_10263(), this.mc.field_1724.method_23318(), this.target.get().method_10260())) > 5.0d) {
            this.mc.field_1724.method_36456((float) Rotations.getYaw(new class_243(this.target.get().method_10263(), (int) this.mc.field_1724.method_23318(), this.target.get().method_10260())));
            this.mc.field_1690.field_1894.method_23481(true);
            Input.setKeyState(this.mc.field_1690.field_1894, true);
            return;
        }
        this.mc.field_1690.field_1894.method_23481(false);
        Input.setKeyState(this.mc.field_1690.field_1894, false);
        this.mc.field_1724.method_18800(0.0d, 0.0d, 0.0d);
        if (this.disconnectOnComplete.get().booleanValue()) {
            this.mc.field_1724.field_3944.method_52781(new class_2661(class_2561.method_43470("[GotoPosition] You are at your destination!")));
        }
        this.target.reset();
        toggle();
    }
}
