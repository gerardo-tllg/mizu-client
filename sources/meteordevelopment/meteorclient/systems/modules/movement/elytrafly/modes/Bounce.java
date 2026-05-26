package meteordevelopment.meteorclient.systems.modules.movement.elytrafly.modes;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFlightMode;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFlightModes;
import meteordevelopment.meteorclient.utils.misc.input.Input;
import net.minecraft.class_1294;
import net.minecraft.class_1304;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2708;
import net.minecraft.class_2848;
import net.minecraft.class_304;
import net.minecraft.class_746;
import net.minecraft.class_9334;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/movement/elytrafly/modes/Bounce.class */
public class Bounce extends ElytraFlightMode {
    boolean rubberbanded;
    int tickDelay;
    double prevFov;

    public Bounce() {
        super(ElytraFlightModes.Bounce);
        this.rubberbanded = false;
        this.tickDelay = this.elytraFly.restartDelay.get().intValue();
    }

    @Override // meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFlightMode
    public void onTick() {
        super.onTick();
        if (this.mc.field_1690.field_1903.method_1434() && !this.mc.field_1724.method_6128()) {
            this.mc.method_1562().method_52787(new class_2848(this.mc.field_1724, class_2848.class_2849.field_12982));
        }
        if (checkConditions(this.mc.field_1724)) {
            if (!this.rubberbanded) {
                if (this.prevFov != 0.0d && !this.elytraFly.sprint.get().booleanValue()) {
                    this.mc.field_1690.method_42454().method_41748(Double.valueOf(0.0d));
                }
                if (this.elytraFly.autoJump.get().booleanValue()) {
                    setPressed(this.mc.field_1690.field_1903, true);
                }
                setPressed(this.mc.field_1690.field_1894, true);
                this.mc.field_1724.method_36456(getYawDirection());
                this.mc.field_1724.method_36457(this.elytraFly.pitch.get().floatValue());
            }
            if (!this.elytraFly.sprint.get().booleanValue()) {
                if (this.mc.field_1724.method_6128()) {
                    this.mc.field_1724.method_5728(this.mc.field_1724.method_24828());
                } else {
                    this.mc.field_1724.method_5728(true);
                }
            }
            if (this.rubberbanded && this.elytraFly.restart.get().booleanValue()) {
                if (this.tickDelay > 0) {
                    this.tickDelay--;
                    return;
                }
                this.mc.method_1562().method_52787(new class_2848(this.mc.field_1724, class_2848.class_2849.field_12982));
                this.rubberbanded = false;
                this.tickDelay = this.elytraFly.restartDelay.get().intValue();
            }
        }
    }

    @Override // meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFlightMode
    public void onPreTick() {
        super.onPreTick();
        if (!checkConditions(this.mc.field_1724) || !this.elytraFly.sprint.get().booleanValue()) {
            return;
        }
        this.mc.field_1724.method_5728(true);
    }

    private void unpress() {
        setPressed(this.mc.field_1690.field_1894, false);
        if (this.elytraFly.autoJump.get().booleanValue()) {
            setPressed(this.mc.field_1690.field_1903, false);
        }
    }

    @Override // meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFlightMode
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.packet instanceof class_2708) {
            this.rubberbanded = true;
            this.mc.field_1724.method_66281();
        }
    }

    @Override // meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFlightMode
    public void onPacketSend(PacketEvent.Send event) {
        if ((event.packet instanceof class_2848) && event.packet.method_12365().equals(class_2848.class_2849.field_12982) && !this.elytraFly.sprint.get().booleanValue()) {
            this.mc.field_1724.method_5728(true);
        }
    }

    private void setPressed(class_304 key, boolean pressed) {
        key.method_23481(pressed);
        Input.setKeyState(key, pressed);
    }

    public static boolean recastElytra(class_746 player) {
        if (checkConditions(player) && ignoreGround(player)) {
            player.field_3944.method_52787(new class_2848(player, class_2848.class_2849.field_12982));
            return true;
        }
        return false;
    }

    public static boolean checkConditions(class_746 player) {
        class_1799 itemStack = player.method_6118(class_1304.field_6174);
        return (player.method_31549().field_7479 || player.method_5765() || player.method_6101() || !itemStack.method_31574(class_1802.field_8833) || !isElytraUsable(itemStack)) ? false : true;
    }

    private static boolean isElytraUsable(class_1799 stack) {
        return stack.method_58694(class_9334.field_49629) == null || stack.method_7936() - ((Integer) stack.method_58694(class_9334.field_49629)).intValue() > 1;
    }

    private static boolean ignoreGround(class_746 player) {
        if (!player.method_5799() && !player.method_6059(class_1294.field_5902)) {
            class_1799 itemStack = player.method_6118(class_1304.field_6174);
            if (itemStack.method_31574(class_1802.field_8833) && isElytraUsable(itemStack)) {
                player.method_23669();
                return true;
            }
            return false;
        }
        return false;
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: java.lang.MatchException */
    private float getYawDirection() throws MatchException {
        switch (this.elytraFly.yawLockMode.get()) {
            case None:
                return this.mc.field_1724.method_36454();
            case Smart:
                return Math.round((this.mc.field_1724.method_36454() + 1.0f) / 45.0f) * 45.0f;
            case Simple:
                return this.elytraFly.yaw.get().floatValue();
            default:
                throw new MatchException((String) null, (Throwable) null);
        }
    }

    @Override // meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFlightMode
    public void onActivate() {
        this.prevFov = ((Double) this.mc.field_1690.method_42454().method_41753()).doubleValue();
    }

    @Override // meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFlightMode
    public void onDeactivate() {
        unpress();
        this.rubberbanded = false;
        if (this.prevFov == 0.0d || this.elytraFly.sprint.get().booleanValue()) {
            return;
        }
        this.mc.field_1690.method_42454().method_41748(Double.valueOf(this.prevFov));
    }
}
