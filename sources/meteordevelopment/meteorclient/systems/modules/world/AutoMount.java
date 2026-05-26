package meteordevelopment.meteorclient.systems.modules.world;

import java.util.Set;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1308;
import net.minecraft.class_1501;
import net.minecraft.class_1826;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/world/AutoMount.class */
public class AutoMount extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Boolean> checkSaddle;
    private final Setting<Boolean> rotate;
    private final Setting<Set<class_1299<?>>> entities;

    public AutoMount() {
        super(Categories.World, "auto-mount", "Automatically mounts entities.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.checkSaddle = this.sgGeneral.add(new BoolSetting.Builder().name("check-saddle").description("Checks if the entity contains a saddle before mounting.").defaultValue(false).build());
        this.rotate = this.sgGeneral.add(new BoolSetting.Builder().name("rotate").description("Faces the entity you mount.").defaultValue(true).build());
        this.entities = this.sgGeneral.add(new EntityTypeListSetting.Builder().name("entities").description("Rideable entities.").filter(EntityUtils::isRideable).build());
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.mc.field_1724.method_5765() || this.mc.field_1724.method_5715() || (this.mc.field_1724.method_6047().method_7909() instanceof class_1826)) {
            return;
        }
        for (class_1308 class_1308Var : this.mc.field_1687.method_18112()) {
            if (this.entities.get().contains(class_1308Var.method_5864()) && PlayerUtils.isWithin((class_1297) class_1308Var, 4.0d)) {
                if (class_1308Var instanceof class_1308) {
                    class_1308 mobEntity = class_1308Var;
                    if (!mobEntity.method_66672()) {
                        continue;
                    }
                }
                if (!(class_1308Var instanceof class_1501) && (class_1308Var instanceof class_1308)) {
                    class_1308 mobEntity2 = class_1308Var;
                    if (!this.checkSaddle.get().booleanValue() || mobEntity2.method_66672()) {
                    }
                }
                interact(class_1308Var);
                return;
            }
        }
    }

    private void interact(class_1297 entity) {
        if (!this.rotate.get().booleanValue()) {
            this.mc.field_1761.method_2905(this.mc.field_1724, entity, class_1268.field_5808);
        } else {
            Rotations.rotate(Rotations.getYaw(entity), Rotations.getPitch(entity), -100, () -> {
                this.mc.field_1761.method_2905(this.mc.field_1724, entity, class_1268.field_5808);
            });
        }
    }
}
