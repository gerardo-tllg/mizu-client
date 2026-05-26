package meteordevelopment.meteorclient.systems.modules.combat;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.SlotUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1713;
import net.minecraft.class_1802;
import net.minecraft.class_2248;
import net.minecraft.class_2325;
import net.minecraft.class_2336;
import net.minecraft.class_2363;
import net.minecraft.class_239;
import net.minecraft.class_2480;
import net.minecraft.class_2531;
import net.minecraft.class_2615;
import net.minecraft.class_2663;
import net.minecraft.class_2680;
import net.minecraft.class_3489;
import net.minecraft.class_3710;
import net.minecraft.class_3865;
import net.minecraft.class_3965;
import net.minecraft.class_408;
import net.minecraft.class_473;
import net.minecraft.class_4739;
import net.minecraft.class_7743;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/Offhand.class */
public class Offhand extends Module {
    private final SettingGroup sgTotem;
    private final SettingGroup sgCombat;
    private final Setting<Integer> totemOffhandHealth;
    private final Setting<Boolean> antiGhost;
    private final Setting<Boolean> mainHandTotem;
    private final Setting<Integer> mainHandTotemSlot;
    private final Setting<Boolean> swordGapple;

    public Offhand() {
        super(Categories.Combat, "offhand", "Allows you to hold specified items in your offhand.");
        this.sgTotem = this.settings.createGroup("Totem");
        this.sgCombat = this.settings.createGroup("Combat");
        this.totemOffhandHealth = this.sgTotem.add(new IntSetting.Builder().name("offhand-totem-health").description("The health to force hold a totem at.").defaultValue(10).range(0, 36).sliderMax(36).build());
        this.antiGhost = this.sgTotem.add(new BoolSetting.Builder().name("anti-ghost").description("Deletes your totem client side when you pop.").defaultValue(true).build());
        this.mainHandTotem = this.sgTotem.add(new BoolSetting.Builder().name("main-hand-totem").description("Whether or not to hold a totem in your main hand.").defaultValue(true).build());
        this.mainHandTotemSlot = this.sgTotem.add(new IntSetting.Builder().name("main-hand-totem-slot").description("The slot in your hotbar to hold your main hand totem.").defaultValue(3).range(1, 9).visible(() -> {
            return this.mainHandTotem.get().booleanValue();
        }).build());
        this.swordGapple = this.sgCombat.add(new BoolSetting.Builder().name("sword-gapple").description("Lets you right click while holding a sword to eat a golden apple.").defaultValue(true).build());
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
    }

    @EventHandler(priority = 1199)
    private void onTick(TickEvent.Pre event) {
        if (this.mc.field_1755 == null || (this.mc.field_1755 instanceof class_408) || (this.mc.field_1755 instanceof class_7743) || (this.mc.field_1755 instanceof class_473)) {
            if (this.mainHandTotem.get().booleanValue()) {
                updateMainHandTotem();
            }
            updateOffhandSlot();
        }
    }

    @EventHandler(priority = 100)
    private void onReceivePacket(PacketEvent.Receive event) {
        class_1297 entity;
        class_2663 class_2663Var = event.packet;
        if (!(class_2663Var instanceof class_2663)) {
            return;
        }
        class_2663 p = class_2663Var;
        if (p.method_11470() == 35 && (entity = p.method_11469(this.mc.field_1687)) != null && entity.equals(this.mc.field_1724) && this.antiGhost.get().booleanValue()) {
            this.mc.field_1724.method_31548().method_5441(45);
            updateOffhandSlot();
        }
    }

    private void updateMainHandTotem() {
        FindItemResult totemResult = findTotem();
        if (totemResult.found() && !totemResult.isOffhand() && this.mc.field_1724.method_31548().method_5438(this.mainHandTotemSlot.get().intValue() - 1).method_7909() != class_1802.field_8288) {
            this.mc.field_1761.method_2906(this.mc.field_1724.field_7498.field_7763, SlotUtils.indexToId(totemResult.slot()), this.mainHandTotemSlot.get().intValue() - 1, class_1713.field_7791, this.mc.field_1724);
        }
    }

    private void updateOffhandSlot() {
        boolean isLowHealth = (this.mc.field_1724.method_6032() + this.mc.field_1724.method_6067()) - PlayerUtils.possibleHealthReductions(true, true) <= ((float) this.totemOffhandHealth.get().intValue());
        boolean flying = this.mc.field_1724.method_6118(class_1304.field_6174).method_7909() == class_1802.field_8833 && this.mc.field_1724.method_6128();
        if (!isLowHealth && !flying) {
            if (this.swordGapple.get().booleanValue() && this.mc.field_1724.method_6047().method_31573(class_3489.field_42611) && this.mc.field_1690.field_1904.method_1434()) {
                moveGappleToOffhand();
                return;
            } else {
                moveTotemToOffhand();
                return;
            }
        }
        moveTotemToOffhand();
    }

    private void moveTotemToOffhand() {
        if (this.mc.field_1724.method_6079().method_7909() != class_1802.field_8288) {
            FindItemResult totemResult = findTotem();
            if (totemResult.isHotbar()) {
                this.mc.field_1761.method_2906(this.mc.field_1724.field_7498.field_7763, 45, totemResult.slot(), class_1713.field_7791, this.mc.field_1724);
                updateMainHandTotem();
            } else if (totemResult.found()) {
                int selectedSlot = this.mc.field_1724.method_31548().field_7545;
                this.mc.field_1761.method_2906(this.mc.field_1724.field_7498.field_7763, totemResult.slot(), selectedSlot, class_1713.field_7791, this.mc.field_1724);
                this.mc.field_1761.method_2906(this.mc.field_1724.field_7498.field_7763, 45, selectedSlot, class_1713.field_7791, this.mc.field_1724);
                this.mc.field_1761.method_2906(this.mc.field_1724.field_7498.field_7763, totemResult.slot(), selectedSlot, class_1713.field_7791, this.mc.field_1724);
            }
        }
    }

    private void moveGappleToOffhand() {
        if (this.mc.field_1724.method_6079().method_7909() != class_1802.field_8367 && !willInteractWithChestBlock()) {
            FindItemResult inventoryGappleResult = InvUtils.find(class_1802.field_8367);
            if (inventoryGappleResult.found()) {
                if (inventoryGappleResult.isHotbar()) {
                    this.mc.field_1761.method_2906(this.mc.field_1724.field_7498.field_7763, 45, inventoryGappleResult.slot(), class_1713.field_7791, this.mc.field_1724);
                    return;
                }
                int selectedSlot = this.mc.field_1724.method_31548().field_7545;
                this.mc.field_1761.method_2906(this.mc.field_1724.field_7498.field_7763, inventoryGappleResult.slot(), selectedSlot, class_1713.field_7791, this.mc.field_1724);
                this.mc.field_1761.method_2906(this.mc.field_1724.field_7498.field_7763, 45, selectedSlot, class_1713.field_7791, this.mc.field_1724);
                this.mc.field_1761.method_2906(this.mc.field_1724.field_7498.field_7763, inventoryGappleResult.slot(), selectedSlot, class_1713.field_7791, this.mc.field_1724);
            }
        }
    }

    private boolean willInteractWithChestBlock() {
        if (this.mc.field_1765 != null && this.mc.field_1765.method_17783() == class_239.class_240.field_1332) {
            class_3965 blockHitResult = this.mc.field_1765;
            class_2680 blockState = this.mc.field_1687.method_8320(blockHitResult.method_17777());
            class_2248 block = blockState.method_26204();
            if ((block instanceof class_2480) || (block instanceof class_4739) || (block instanceof class_2336) || (block instanceof class_2531) || (block instanceof class_3865) || (block instanceof class_2363) || (block instanceof class_3710) || (block instanceof class_2325) || (block instanceof class_2615)) {
                return true;
            }
            return false;
        }
        return false;
    }

    private FindItemResult findTotem() {
        return InvUtils.find(x -> {
            return x.method_7909().equals(class_1802.field_8288);
        }, 0, 35);
    }
}
