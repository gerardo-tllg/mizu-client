package meteordevelopment.meteorclient.systems.modules.combat;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnchantmentListSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.player.ChestSwap;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_10192;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1887;
import net.minecraft.class_1893;
import net.minecraft.class_5321;
import net.minecraft.class_6880;
import net.minecraft.class_9334;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/AutoArmor.class */
public class AutoArmor extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Protection> preferredProtection;
    private final Setting<Integer> delay;
    private final Setting<Set<class_5321<class_1887>>> avoidedEnchantments;
    private final Setting<Boolean> blastLeggings;
    private final Setting<Boolean> antiBreak;
    private final Setting<Boolean> ignoreElytra;
    private final Object2IntMap<class_6880<class_1887>> enchantments;
    private final ArmorPiece[] armorPieces;
    private final ArmorPiece helmet;
    private final ArmorPiece chestplate;
    private final ArmorPiece leggings;
    private final ArmorPiece boots;
    private int timer;

    public AutoArmor() {
        super(Categories.Combat, "auto-armor", "Automatically equips armor.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.preferredProtection = this.sgGeneral.add(new EnumSetting.Builder().name("preferred-protection").description("Which type of protection to prefer.").defaultValue(Protection.Protection).build());
        this.delay = this.sgGeneral.add(new IntSetting.Builder().name("swap-delay").description("The delay between equipping armor pieces.").defaultValue(1).min(0).sliderMax(5).build());
        this.avoidedEnchantments = this.sgGeneral.add(new EnchantmentListSetting.Builder().name("avoided-enchantments").description("Enchantments that should be avoided.").defaultValue(class_1893.field_9113, class_1893.field_9122).build());
        this.blastLeggings = this.sgGeneral.add(new BoolSetting.Builder().name("blast-prot-leggings").description("Uses blast protection for leggings regardless of preferred protection.").defaultValue(true).build());
        this.antiBreak = this.sgGeneral.add(new BoolSetting.Builder().name("anti-break").description("Takes off armor if it is about to break.").defaultValue(false).build());
        this.ignoreElytra = this.sgGeneral.add(new BoolSetting.Builder().name("ignore-elytra").description("Will not replace your elytra if you have it equipped.").defaultValue(true).build());
        this.enchantments = new Object2IntOpenHashMap();
        this.armorPieces = new ArmorPiece[4];
        this.helmet = new ArmorPiece(3);
        this.chestplate = new ArmorPiece(2);
        this.leggings = new ArmorPiece(1);
        this.boots = new ArmorPiece(0);
        this.armorPieces[0] = this.helmet;
        this.armorPieces[1] = this.chestplate;
        this.armorPieces[2] = this.leggings;
        this.armorPieces[3] = this.boots;
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        this.timer = 0;
    }

    @EventHandler
    private void onPreTick(TickEvent.Pre event) {
        if (this.timer > 0) {
            this.timer--;
            return;
        }
        for (ArmorPiece armorPiece : this.armorPieces) {
            armorPiece.reset();
        }
        for (int i = 0; i < this.mc.field_1724.method_31548().meteor$getMain().size(); i++) {
            class_1799 itemStack = this.mc.field_1724.method_31548().method_5438(i);
            if (!itemStack.method_7960() && isArmor(itemStack) && (!this.antiBreak.get().booleanValue() || !itemStack.method_7963() || itemStack.method_7936() - itemStack.method_7919() > 10)) {
                Utils.getEnchantments(itemStack, this.enchantments);
                if (!hasAvoidedEnchantment()) {
                    switch (getItemSlotId(itemStack)) {
                        case 0:
                            this.boots.add(itemStack, i);
                            break;
                        case 1:
                            this.leggings.add(itemStack, i);
                            break;
                        case 2:
                            this.chestplate.add(itemStack, i);
                            break;
                        case 3:
                            this.helmet.add(itemStack, i);
                            break;
                    }
                }
            }
        }
        for (ArmorPiece armorPiece2 : this.armorPieces) {
            armorPiece2.calculate();
        }
        Arrays.sort(this.armorPieces, Comparator.comparingInt((v0) -> {
            return v0.getSortScore();
        }));
        for (ArmorPiece armorPiece3 : this.armorPieces) {
            armorPiece3.apply();
        }
    }

    private boolean hasAvoidedEnchantment() {
        ObjectIterator it = this.enchantments.keySet().iterator();
        while (it.hasNext()) {
            class_6880<class_1887> enchantment = (class_6880) it.next();
            Set<class_5321<class_1887>> set = this.avoidedEnchantments.get();
            Objects.requireNonNull(set);
            if (enchantment.method_40224((v1) -> {
                return r1.contains(v1);
            })) {
                return true;
            }
        }
        return false;
    }

    private int getItemSlotId(class_1799 itemStack) {
        if (itemStack.method_7909() == class_1802.field_8833) {
            return 2;
        }
        class_10192 equippable = (class_10192) itemStack.method_58694(class_9334.field_54196);
        if (equippable != null) {
            return equippable.comp_3174().method_5927();
        }
        return -1;
    }

    private boolean isArmor(class_1799 itemStack) {
        return itemStack.method_7909() == class_1802.field_8833 || itemStack.method_58694(class_9334.field_54196) != null;
    }

    private int getProtection(class_1799 itemStack) {
        return 0;
    }

    private float getToughness(class_1799 itemStack) {
        return 0.0f;
    }

    private int getScore(class_1799 itemStack) {
        if (itemStack.method_7960()) {
            return 0;
        }
        class_5321<class_1887> protection = this.preferredProtection.get().enchantment;
        if (isArmor(itemStack) && this.blastLeggings.get().booleanValue() && getItemSlotId(itemStack) == 1) {
            protection = class_1893.field_9107;
        }
        int score = 0 + (3 * Utils.getEnchantmentLevel(this.enchantments, protection));
        return score + Utils.getEnchantmentLevel(this.enchantments, (class_5321<class_1887>) class_1893.field_9111) + Utils.getEnchantmentLevel(this.enchantments, (class_5321<class_1887>) class_1893.field_9107) + Utils.getEnchantmentLevel(this.enchantments, (class_5321<class_1887>) class_1893.field_9095) + Utils.getEnchantmentLevel(this.enchantments, (class_5321<class_1887>) class_1893.field_9096) + Utils.getEnchantmentLevel(this.enchantments, (class_5321<class_1887>) class_1893.field_9119) + (2 * Utils.getEnchantmentLevel(this.enchantments, (class_5321<class_1887>) class_1893.field_9101)) + getProtection(itemStack) + ((int) getToughness(itemStack));
    }

    private boolean cannotSwap() {
        return this.timer > 0;
    }

    private void swap(int from, int armorSlotId) {
        InvUtils.move().from(from).toArmor(armorSlotId);
        this.timer = this.delay.get().intValue();
    }

    private void moveToEmpty(int armorSlotId) {
        for (int i = 0; i < this.mc.field_1724.method_31548().meteor$getMain().size(); i++) {
            if (this.mc.field_1724.method_31548().method_5438(i).method_7960()) {
                InvUtils.move().fromArmor(armorSlotId).to(i);
                this.timer = this.delay.get().intValue();
                return;
            }
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/AutoArmor$Protection.class */
    public enum Protection {
        Protection(class_1893.field_9111),
        BlastProtection(class_1893.field_9107),
        FireProtection(class_1893.field_9095),
        ProjectileProtection(class_1893.field_9096);

        private final class_5321<class_1887> enchantment;

        Protection(class_5321 enchantment) {
            this.enchantment = enchantment;
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/AutoArmor$ArmorPiece.class */
    private class ArmorPiece {
        private final int id;
        private int bestSlot;
        private int bestScore;
        private int score;
        private int durability;

        public ArmorPiece(int id) {
            this.id = id;
        }

        public void reset() {
            this.bestSlot = -1;
            this.bestScore = -1;
            this.score = -1;
            this.durability = Integer.MAX_VALUE;
        }

        public void add(class_1799 itemStack, int slot) {
            int score = AutoArmor.this.getScore(itemStack);
            if (score > this.bestScore) {
                this.bestScore = score;
                this.bestSlot = slot;
            }
        }

        public void calculate() {
            if (AutoArmor.this.cannotSwap()) {
                return;
            }
            class_1799 itemStack = (class_1799) AutoArmor.this.mc.field_1724.method_31548().meteor$getArmor().get(this.id);
            if ((AutoArmor.this.ignoreElytra.get().booleanValue() || Modules.get().isActive(ChestSwap.class)) && itemStack.method_7909() == class_1802.field_8833) {
                this.score = Integer.MAX_VALUE;
                return;
            }
            Utils.getEnchantments(itemStack, AutoArmor.this.enchantments);
            if (AutoArmor.this.enchantments.containsKey(class_1893.field_9113)) {
                this.score = Integer.MAX_VALUE;
                return;
            }
            this.score = AutoArmor.this.getScore(itemStack);
            this.score = decreaseScoreByAvoidedEnchantments(this.score);
            this.score = applyAntiBreakScore(this.score, itemStack);
            if (!itemStack.method_7960()) {
                this.durability = itemStack.method_7936() - itemStack.method_7919();
            }
        }

        public int getSortScore() {
            if (!AutoArmor.this.antiBreak.get().booleanValue() || this.durability > 10) {
                return this.bestScore;
            }
            return -1;
        }

        public void apply() {
            if (AutoArmor.this.cannotSwap() || this.score == Integer.MAX_VALUE) {
                return;
            }
            if (this.bestScore <= this.score) {
                if (AutoArmor.this.antiBreak.get().booleanValue() && this.durability <= 10) {
                    AutoArmor.this.moveToEmpty(this.id);
                    return;
                }
                return;
            }
            AutoArmor.this.swap(this.bestSlot, this.id);
        }

        private int decreaseScoreByAvoidedEnchantments(int score) {
            for (class_5321<class_1887> enchantment : AutoArmor.this.avoidedEnchantments.get()) {
                score -= 2 * AutoArmor.this.enchantments.getInt(enchantment);
            }
            return score;
        }

        private int applyAntiBreakScore(int score, class_1799 itemStack) {
            if (AutoArmor.this.antiBreak.get().booleanValue() && itemStack.method_7963() && itemStack.method_7936() - itemStack.method_7919() <= 10) {
                return -1;
            }
            return score;
        }
    }
}
