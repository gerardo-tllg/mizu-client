package meteordevelopment.meteorclient.systems.modules.movement;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.PlayerMoveC2SPacketAccessor;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.minecraft.class_2596;
import net.minecraft.class_2828;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/movement/Flight.class */
public class Flight extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgAntiKick;
    private final Setting<Mode> mode;
    private final Setting<Double> speed;
    private final Setting<Boolean> verticalSpeedMatch;
    private final Setting<Boolean> noSneak;
    private final Setting<AntiKickMode> antiKickMode;
    private final Setting<Integer> delay;
    private final Setting<Integer> offTime;
    private int delayLeft;
    private int offLeft;
    private boolean flip;
    private float lastYaw;
    private double lastPacketY;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/movement/Flight$AntiKickMode.class */
    public enum AntiKickMode {
        Normal,
        Packet,
        None
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/movement/Flight$Mode.class */
    public enum Mode {
        Abilities,
        Velocity
    }

    public Flight() {
        super(Categories.Movement, "flight", "FLYYYY! No Fall is recommended with this module.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgAntiKick = this.settings.createGroup("Anti Kick");
        this.mode = this.sgGeneral.add(new EnumSetting.Builder().name("mode").description("The mode for Flight.").defaultValue(Mode.Abilities).onChanged(mode -> {
            if (isActive() && Utils.canUpdate()) {
                abilitiesOff();
            }
        }).build());
        this.speed = this.sgGeneral.add(new DoubleSetting.Builder().name("speed").description("Your speed when flying.").defaultValue(0.1d).min(0.0d).build());
        this.verticalSpeedMatch = this.sgGeneral.add(new BoolSetting.Builder().name("vertical-speed-match").description("Matches your vertical speed to your horizontal speed, otherwise uses vanilla ratio.").defaultValue(false).build());
        this.noSneak = this.sgGeneral.add(new BoolSetting.Builder().name("no-sneak").description("Prevents you from sneaking while flying.").defaultValue(false).visible(() -> {
            return this.mode.get() == Mode.Velocity;
        }).build());
        this.antiKickMode = this.sgAntiKick.add(new EnumSetting.Builder().name("mode").description("The mode for anti kick.").defaultValue(AntiKickMode.Packet).build());
        this.delay = this.sgAntiKick.add(new IntSetting.Builder().name("delay").description("The amount of delay, in ticks, between flying down a bit and return to original position").defaultValue(20).min(1).sliderMax(200).build());
        this.offTime = this.sgAntiKick.add(new IntSetting.Builder().name("off-time").description("The amount of delay, in milliseconds, to fly down a bit to reset floating ticks.").defaultValue(1).min(1).sliderRange(1, 20).build());
        this.delayLeft = this.delay.get().intValue();
        this.offLeft = this.offTime.get().intValue();
        this.lastPacketY = Double.MAX_VALUE;
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        if (this.mode.get() == Mode.Abilities && !this.mc.field_1724.method_7325()) {
            this.mc.field_1724.method_31549().field_7479 = true;
            if (this.mc.field_1724.method_31549().field_7477) {
                return;
            }
            this.mc.field_1724.method_31549().field_7478 = true;
        }
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onDeactivate() {
        if (this.mode.get() == Mode.Abilities && !this.mc.field_1724.method_7325()) {
            abilitiesOff();
        }
    }

    @EventHandler
    private void onPreTick(TickEvent.Pre event) {
        float currentYaw = this.mc.field_1724.method_36454();
        if (this.mc.field_1724.field_6017 >= 3.0d && currentYaw == this.lastYaw && this.mc.field_1724.method_18798().method_1033() < 0.003d) {
            this.mc.field_1724.method_36456(currentYaw + (this.flip ? 1 : -1));
            this.flip = !this.flip;
        }
        this.lastYaw = currentYaw;
    }

    @EventHandler
    private void onPostTick(TickEvent.Post event) {
        if (this.delayLeft > 0) {
            this.delayLeft--;
        }
        if (this.offLeft <= 0 && this.delayLeft <= 0) {
            this.delayLeft = this.delay.get().intValue();
            this.offLeft = this.offTime.get().intValue();
            if (this.antiKickMode.get() == AntiKickMode.Packet) {
                this.mc.field_1724.setTicksSinceLastPositionPacketSent(20);
            }
        } else if (this.delayLeft <= 0) {
            boolean shouldReturn = false;
            if (this.antiKickMode.get() == AntiKickMode.Normal) {
                if (this.mode.get() == Mode.Abilities) {
                    abilitiesOff();
                    shouldReturn = true;
                }
            } else if (this.antiKickMode.get() == AntiKickMode.Packet && this.offLeft == this.offTime.get().intValue()) {
                this.mc.field_1724.setTicksSinceLastPositionPacketSent(20);
            }
            this.offLeft--;
            if (shouldReturn) {
                return;
            }
        }
        if (this.mc.field_1724.method_36454() != this.lastYaw) {
            this.mc.field_1724.method_36456(this.lastYaw);
        }
        switch (this.mode.get()) {
            case Abilities:
                if (!this.mc.field_1724.method_7325()) {
                    this.mc.field_1724.method_31549().method_7248(this.speed.get().floatValue());
                    this.mc.field_1724.method_31549().field_7479 = true;
                    if (!this.mc.field_1724.method_31549().field_7477) {
                        this.mc.field_1724.method_31549().field_7478 = true;
                        break;
                    }
                }
                break;
            case Velocity:
                this.mc.field_1724.method_31549().field_7479 = false;
                this.mc.field_1724.method_18800(0.0d, 0.0d, 0.0d);
                class_243 playerVelocity = this.mc.field_1724.method_18798();
                if (this.mc.field_1690.field_1903.method_1434()) {
                    playerVelocity = playerVelocity.method_1031(0.0d, this.speed.get().doubleValue() * ((double) (this.verticalSpeedMatch.get().booleanValue() ? 10.0f : 5.0f)), 0.0d);
                }
                if (this.mc.field_1690.field_1832.method_1434()) {
                    playerVelocity = playerVelocity.method_1023(0.0d, this.speed.get().doubleValue() * ((double) (this.verticalSpeedMatch.get().booleanValue() ? 10.0f : 5.0f)), 0.0d);
                }
                this.mc.field_1724.method_18799(playerVelocity);
                if (this.noSneak.get().booleanValue()) {
                    this.mc.field_1724.method_24830(false);
                }
                break;
        }
    }

    private void antiKickPacket(class_2828 packet, double currentY) {
        if (this.delayLeft <= 0 && this.lastPacketY != Double.MAX_VALUE && shouldFlyDown(currentY, this.lastPacketY) && isEntityOnAir(this.mc.field_1724)) {
            ((PlayerMoveC2SPacketAccessor) packet).setY(this.lastPacketY - 0.0313d);
        } else {
            this.lastPacketY = currentY;
        }
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        class_2828.class_2830 class_2829Var;
        class_2596<?> class_2596Var = event.packet;
        if (class_2596Var instanceof class_2828) {
            class_2828 packet = (class_2828) class_2596Var;
            if (this.antiKickMode.get() != AntiKickMode.Packet) {
                return;
            }
            double currentY = packet.method_12268(Double.MAX_VALUE);
            if (currentY != Double.MAX_VALUE) {
                antiKickPacket(packet, currentY);
                return;
            }
            if (packet.method_36172()) {
                class_2829Var = new class_2828.class_2830(this.mc.field_1724.method_23317(), this.mc.field_1724.method_23318(), this.mc.field_1724.method_23321(), packet.method_12271(0.0f), packet.method_12270(0.0f), packet.method_12273(), this.mc.field_1724.field_5976);
            } else {
                class_2829Var = new class_2828.class_2829(this.mc.field_1724.method_23317(), this.mc.field_1724.method_23318(), this.mc.field_1724.method_23321(), packet.method_12273(), this.mc.field_1724.field_5976);
            }
            event.cancel();
            antiKickPacket(class_2829Var, this.mc.field_1724.method_23318());
            this.mc.method_1562().method_52787(class_2829Var);
        }
    }

    private boolean shouldFlyDown(double currentY, double lastY) {
        return currentY >= lastY || lastY - currentY < 0.0313d;
    }

    private void abilitiesOff() {
        this.mc.field_1724.method_31549().field_7479 = false;
        this.mc.field_1724.method_31549().method_7248(0.05f);
        if (this.mc.field_1724.method_31549().field_7477) {
            return;
        }
        this.mc.field_1724.method_31549().field_7478 = false;
    }

    private boolean isEntityOnAir(class_1297 entity) {
        return entity.method_37908().method_29546(entity.method_5829().method_1014(0.0625d).method_1012(0.0d, -0.55d, 0.0d)).allMatch((v0) -> {
            return v0.method_26215();
        });
    }

    public float getOffGroundSpeed() {
        if (isActive() && this.mode.get() == Mode.Velocity) {
            return this.speed.get().floatValue() * (this.mc.field_1724.method_5624() ? 15.0f : 10.0f);
        }
        return -1.0f;
    }

    public boolean noSneak() {
        return isActive() && this.mode.get() == Mode.Velocity && this.noSneak.get().booleanValue();
    }
}
