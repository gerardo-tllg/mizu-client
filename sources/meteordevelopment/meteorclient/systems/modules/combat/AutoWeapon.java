package meteordevelopment.meteorclient.systems.modules.combat;

import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.DamageUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_3489;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/AutoWeapon.class */
public class AutoWeapon extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Weapon> weapon;
    private final Setting<Integer> threshold;
    private final Setting<Boolean> antiBreak;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/AutoWeapon$Weapon.class */
    public enum Weapon {
        Sword,
        Axe
    }

    public AutoWeapon() {
        super(Categories.Combat, "auto-weapon", "Finds the best weapon to use in your hotbar.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.weapon = this.sgGeneral.add(new EnumSetting.Builder().name("weapon").description("What type of weapon to use.").defaultValue(Weapon.Sword).build());
        this.threshold = this.sgGeneral.add(new IntSetting.Builder().name("threshold").description("If the non-preferred weapon produces this much damage this will favor it over your preferred weapon.").defaultValue(4).build());
        this.antiBreak = this.sgGeneral.add(new BoolSetting.Builder().name("anti-break").description("Prevents you from breaking your weapon.").defaultValue(false).build());
    }

    @EventHandler
    private void onAttack(AttackEntityEvent event) {
        class_1297 class_1297Var = event.entity;
        if (class_1297Var instanceof class_1309) {
            class_1309 livingEntity = (class_1309) class_1297Var;
            InvUtils.swap(getBestWeapon(livingEntity), false);
        }
    }

    private int getBestWeapon(class_1309 target) {
        int slotS = this.mc.field_1724.method_31548().field_7545;
        int slotA = this.mc.field_1724.method_31548().field_7545;
        double damageS = 0.0d;
        double damageA = 0.0d;
        for (int i = 0; i < 9; i++) {
            class_1799 stack = this.mc.field_1724.method_31548().method_5438(i);
            if (stack.method_31573(class_3489.field_42611) && (!this.antiBreak.get().booleanValue() || stack.method_7936() - stack.method_7919() > 10)) {
                double currentDamageS = DamageUtils.getAttackDamage(this.mc.field_1724, target, stack);
                if (currentDamageS > damageS) {
                    damageS = currentDamageS;
                    slotS = i;
                }
            } else if (stack.method_31573(class_3489.field_42612) && (!this.antiBreak.get().booleanValue() || stack.method_7936() - stack.method_7919() > 10)) {
                double currentDamageA = DamageUtils.getAttackDamage(this.mc.field_1724, target, stack);
                if (currentDamageA > damageA) {
                    damageA = currentDamageA;
                    slotA = i;
                }
            }
        }
        return (this.weapon.get() != Weapon.Sword || ((double) this.threshold.get().intValue()) <= damageA - damageS) ? (this.weapon.get() != Weapon.Axe || ((double) this.threshold.get().intValue()) <= damageS - damageA) ? (this.weapon.get() != Weapon.Sword || ((double) this.threshold.get().intValue()) >= damageA - damageS) ? (this.weapon.get() != Weapon.Axe || ((double) this.threshold.get().intValue()) >= damageS - damageA) ? this.mc.field_1724.method_31548().method_67532() : slotS : slotA : slotA : slotS;
    }
}
