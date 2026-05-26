package meteordevelopment.meteorclient.systems.modules.combat;

import java.util.Set;
import java.util.function.Predicate;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_3489;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/Hitboxes.class */
public class Hitboxes extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Set<class_1299<?>>> entities;
    private final Setting<Double> value;
    private final Setting<Boolean> ignoreFriends;
    private final Setting<Boolean> onlyOnWeapon;

    public Hitboxes() {
        super(Categories.Combat, "hitboxes", "Expands an entity's hitboxes.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.entities = this.sgGeneral.add(new EntityTypeListSetting.Builder().name("entities").description("Which entities to target.").defaultValue(class_1299.field_6097).build());
        this.value = this.sgGeneral.add(new DoubleSetting.Builder().name("expand").description("How much to expand the hitbox of the entity.").defaultValue(0.5d).build());
        this.ignoreFriends = this.sgGeneral.add(new BoolSetting.Builder().name("ignore-friends").description("Doesn't expand the hitboxes of friends.").defaultValue(true).build());
        this.onlyOnWeapon = this.sgGeneral.add(new BoolSetting.Builder().name("only-on-weapon").description("Only modifies hitbox when holding a weapon in hand.").defaultValue(false).build());
    }

    public double getEntityValue(class_1297 entity) {
        if (!isActive() || !testWeapon()) {
            return 0.0d;
        }
        if (!(this.ignoreFriends.get().booleanValue() && (entity instanceof class_1657) && Friends.get().isFriend((class_1657) entity)) && this.entities.get().contains(entity.method_5864())) {
            return this.value.get().doubleValue();
        }
        return 0.0d;
    }

    private boolean testWeapon() {
        if (this.onlyOnWeapon.get().booleanValue()) {
            return InvUtils.testInHands((Predicate<class_1799>) itemStack -> {
                return itemStack.method_31573(class_3489.field_42611) || itemStack.method_31573(class_3489.field_42612);
            });
        }
        return true;
    }
}
