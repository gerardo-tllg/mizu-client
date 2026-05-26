package meteordevelopment.meteorclient.systems.modules.movement;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IHorseBaseEntity;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1496;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/movement/EntityControl.class */
public class EntityControl extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Boolean> maxJump;

    public EntityControl() {
        super(Categories.Movement, "entity-control", "Lets you control rideable entities without a saddle.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.maxJump = this.sgGeneral.add(new BoolSetting.Builder().name("max-jump").description("Sets jump power to maximum.").defaultValue(true).build());
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onDeactivate() {
        if (!Utils.canUpdate() || this.mc.field_1687.method_18112() == null) {
            return;
        }
        for (IHorseBaseEntity iHorseBaseEntity : this.mc.field_1687.method_18112()) {
            if (iHorseBaseEntity instanceof class_1496) {
                iHorseBaseEntity.setSaddled(false);
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        for (IHorseBaseEntity iHorseBaseEntity : this.mc.field_1687.method_18112()) {
            if (iHorseBaseEntity instanceof class_1496) {
                iHorseBaseEntity.setSaddled(true);
            }
        }
        if (this.maxJump.get().booleanValue()) {
            this.mc.field_1724.setMountJumpStrength(1.0f);
        }
    }
}
