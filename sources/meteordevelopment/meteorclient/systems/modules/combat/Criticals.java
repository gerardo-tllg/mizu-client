package meteordevelopment.meteorclient.systems.modules.combat;

import java.util.Objects;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IPlayerInteractEntityC2SPacket;
import meteordevelopment.meteorclient.mixininterface.IPlayerMoveC2SPacket;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_2824;
import net.minecraft.class_2828;
import net.minecraft.class_2879;
import net.minecraft.class_9362;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/Criticals.class */
public class Criticals extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgMace;
    private final Setting<Mode> mode;
    private final Setting<Boolean> ka;
    private final Setting<Boolean> mace;
    private final Setting<Double> extraHeight;
    private class_2824 attackPacket;
    private class_2879 swingPacket;
    private boolean sendPackets;
    private int sendTimer;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/Criticals$Mode.class */
    public enum Mode {
        None,
        Packet,
        Bypass,
        Jump,
        MiniJump
    }

    public Criticals() {
        super(Categories.Combat, "criticals", "Performs critical attacks when you hit your target.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgMace = this.settings.createGroup("Mace");
        this.mode = this.sgGeneral.add(new EnumSetting.Builder().name("mode").description("The mode on how Criticals will function.").defaultValue(Mode.Packet).build());
        this.ka = this.sgGeneral.add(new BoolSetting.Builder().name("only-killaura").description("Only performs crits when using killaura.").defaultValue(false).visible(() -> {
            return this.mode.get() != Mode.None;
        }).build());
        this.mace = this.sgMace.add(new BoolSetting.Builder().name("smash-attack").description("Will always perform smash attacks when using a mace.").defaultValue(true).build());
        Setting<Boolean> maceRef = this.mace;
        Objects.requireNonNull(maceRef);
        SettingGroup settingGroup = this.sgMace;
        DoubleSetting.Builder builderSliderRange = new DoubleSetting.Builder().name("additional-height").description("The amount of additional height to spoof. More height means more damage.").defaultValue(0.0d).min(0.0d).sliderRange(0.0d, 100.0d);
        Objects.requireNonNull(maceRef);
        this.extraHeight = settingGroup.add(builderSliderRange.visible(maceRef::get).build());
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        this.attackPacket = null;
        this.swingPacket = null;
        this.sendPackets = false;
        this.sendTimer = 0;
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        IPlayerInteractEntityC2SPacket iPlayerInteractEntityC2SPacket = event.packet;
        if (iPlayerInteractEntityC2SPacket instanceof IPlayerInteractEntityC2SPacket) {
            IPlayerInteractEntityC2SPacket packet = iPlayerInteractEntityC2SPacket;
            if (packet.meteor$getType() == class_2824.class_5907.field_29172) {
                if (this.mace.get().booleanValue() && (this.mc.field_1724.method_6047().method_7909() instanceof class_9362)) {
                    if (this.mc.field_1724.method_6128()) {
                        return;
                    }
                    sendPacket(0.0d);
                    sendPacket(1.501d + this.extraHeight.get().doubleValue());
                    sendPacket(0.0d);
                    return;
                }
                if (skipCrit()) {
                    return;
                }
                class_1297 entity = packet.meteor$getEntity();
                if (entity instanceof class_1309) {
                    if (entity != ((KillAura) Modules.get().get(KillAura.class)).getTarget() && this.ka.get().booleanValue()) {
                        return;
                    }
                    switch (this.mode.get().ordinal()) {
                        case 1:
                            sendPacket(0.0625d);
                            sendPacket(0.0d);
                            break;
                        case 2:
                            sendPacket(0.11d);
                            sendPacket(0.1100013579d);
                            sendPacket(1.3579E-6d);
                            break;
                        case 3:
                        case 4:
                            if (!this.sendPackets) {
                                this.sendPackets = true;
                                this.sendTimer = this.mode.get() == Mode.Jump ? 6 : 4;
                                this.attackPacket = event.packet;
                                if (this.mode.get() == Mode.Jump) {
                                    this.mc.field_1724.method_6043();
                                } else {
                                    this.mc.field_1724.method_18798().meteor$setY(0.25d);
                                }
                                event.cancel();
                            }
                            break;
                    }
                    return;
                }
                return;
            }
        }
        if ((event.packet instanceof class_2879) && this.mode.get() != Mode.Packet && !skipCrit() && this.sendPackets && this.swingPacket == null) {
            this.swingPacket = event.packet;
            event.cancel();
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.sendPackets) {
            if (this.sendTimer <= 0) {
                this.sendPackets = false;
                if (this.attackPacket == null || this.swingPacket == null) {
                    return;
                }
                this.mc.method_1562().method_52787(this.attackPacket);
                this.mc.method_1562().method_52787(this.swingPacket);
                this.attackPacket = null;
                this.swingPacket = null;
                return;
            }
            this.sendTimer--;
        }
    }

    private void sendPacket(double height) {
        double x = this.mc.field_1724.method_23317();
        double y = this.mc.field_1724.method_23318();
        double z = this.mc.field_1724.method_23321();
        IPlayerMoveC2SPacket class_2829Var = new class_2828.class_2829(x, y + height, z, false, this.mc.field_1724.field_5976);
        class_2829Var.meteor$setTag(1337);
        this.mc.field_1724.field_3944.method_52787(class_2829Var);
    }

    private boolean skipCrit() {
        return !this.mc.field_1724.method_24828() || this.mc.field_1724.method_5869() || this.mc.field_1724.method_5771() || this.mc.field_1724.method_6101();
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public String getInfoString() {
        return this.mode.get().name();
    }
}
