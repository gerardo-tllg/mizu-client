package meteordevelopment.meteorclient.systems.modules.movement.elytrafly;

import java.util.function.Predicate;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import net.minecraft.class_1268;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_243;
import net.minecraft.class_2848;
import net.minecraft.class_310;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/movement/elytrafly/ElytraFlightMode.class */
public class ElytraFlightMode {
    protected final class_310 mc = class_310.method_1551();
    protected final ElytraFly elytraFly = (ElytraFly) Modules.get().get(ElytraFly.class);
    private final ElytraFlightModes type;
    protected boolean lastJumpPressed;
    protected boolean incrementJumpTimer;
    protected boolean lastForwardPressed;
    protected int jumpTimer;
    protected double velX;
    protected double velY;
    protected double velZ;
    protected double ticksLeft;
    protected class_243 forward;
    protected class_243 right;
    protected double acceleration;

    public ElytraFlightMode(ElytraFlightModes type) {
        this.type = type;
    }

    public void onTick() {
        if (this.elytraFly.autoReplenish.get().booleanValue()) {
            FindItemResult fireworks = InvUtils.find(class_1802.field_8639);
            if (fireworks.found() && !fireworks.isHotbar()) {
                InvUtils.move().from(fireworks.slot()).toHotbar(this.elytraFly.replenishSlot.get().intValue() - 1);
            }
        }
        if (this.elytraFly.replace.get().booleanValue()) {
            class_1799 chestStack = (class_1799) this.mc.field_1724.method_31548().meteor$getArmor().get(2);
            if (chestStack.method_7909() == class_1802.field_8833 && chestStack.method_7936() - chestStack.method_7919() <= this.elytraFly.replaceDurability.get().intValue()) {
                FindItemResult elytra = InvUtils.find((Predicate<class_1799>) stack -> {
                    return stack.method_7936() - stack.method_7919() > this.elytraFly.replaceDurability.get().intValue() && stack.method_7909() == class_1802.field_8833;
                });
                InvUtils.move().from(elytra.slot()).toArmor(2);
            }
        }
    }

    public void onPreTick() {
    }

    public void onPacketSend(PacketEvent.Send event) {
    }

    public void onPacketReceive(PacketEvent.Receive event) {
    }

    public void onPlayerMove() {
    }

    public void onActivate() {
        this.lastJumpPressed = false;
        this.jumpTimer = 0;
        this.ticksLeft = 0.0d;
        this.acceleration = 0.0d;
    }

    public void onDeactivate() {
    }

    public void autoTakeoff() {
        if (this.incrementJumpTimer) {
            this.jumpTimer++;
        }
        boolean jumpPressed = this.mc.field_1690.field_1903.method_1434();
        if (this.elytraFly.autoTakeOff.get().booleanValue() && jumpPressed) {
            if (!this.lastJumpPressed && !this.mc.field_1724.method_6128()) {
                this.jumpTimer = 0;
                this.incrementJumpTimer = true;
            }
            if (this.jumpTimer >= 8) {
                this.jumpTimer = 0;
                this.incrementJumpTimer = false;
                this.mc.field_1724.method_6100(false);
                this.mc.field_1724.method_5728(true);
                this.mc.field_1724.method_6043();
                this.mc.method_1562().method_52787(new class_2848(this.mc.field_1724, class_2848.class_2849.field_12982));
            }
        }
        this.lastJumpPressed = jumpPressed;
    }

    public void handleAutopilot() {
        if (this.mc.field_1724.method_6128()) {
            if (this.elytraFly.autoPilot.get().booleanValue() && this.mc.field_1724.method_23318() > this.elytraFly.autoPilotMinimumHeight.get().doubleValue() && this.elytraFly.flightMode.get() != ElytraFlightModes.Bounce) {
                this.mc.field_1690.field_1894.method_23481(true);
                this.lastForwardPressed = true;
            }
            if (this.elytraFly.useFireworks.get().booleanValue()) {
                if (this.ticksLeft <= 0.0d) {
                    this.ticksLeft = this.elytraFly.autoPilotFireworkDelay.get().doubleValue() * 20.0d;
                    FindItemResult itemResult = InvUtils.findInHotbar(class_1802.field_8639);
                    if (!itemResult.found()) {
                        return;
                    }
                    if (itemResult.isOffhand()) {
                        this.mc.field_1761.method_2919(this.mc.field_1724, class_1268.field_5810);
                        this.mc.field_1724.method_6104(class_1268.field_5810);
                    } else {
                        InvUtils.swap(itemResult.slot(), true);
                        this.mc.field_1761.method_2919(this.mc.field_1724, class_1268.field_5808);
                        this.mc.field_1724.method_6104(class_1268.field_5808);
                        InvUtils.swapBack();
                    }
                }
                this.ticksLeft -= 1.0d;
            }
        }
    }

    public void handleHorizontalSpeed(PlayerMoveEvent event) {
        boolean a = false;
        boolean b = false;
        if (this.mc.field_1690.field_1894.method_1434()) {
            this.velX += this.forward.field_1352 * getSpeed() * 10.0d;
            this.velZ += this.forward.field_1350 * getSpeed() * 10.0d;
            a = true;
        } else if (this.mc.field_1690.field_1881.method_1434()) {
            this.velX -= (this.forward.field_1352 * getSpeed()) * 10.0d;
            this.velZ -= (this.forward.field_1350 * getSpeed()) * 10.0d;
            a = true;
        }
        if (this.mc.field_1690.field_1849.method_1434()) {
            this.velX += this.right.field_1352 * getSpeed() * 10.0d;
            this.velZ += this.right.field_1350 * getSpeed() * 10.0d;
            b = true;
        } else if (this.mc.field_1690.field_1913.method_1434()) {
            this.velX -= (this.right.field_1352 * getSpeed()) * 10.0d;
            this.velZ -= (this.right.field_1350 * getSpeed()) * 10.0d;
            b = true;
        }
        if (a && b) {
            double diagonal = 1.0d / Math.sqrt(2.0d);
            this.velX *= diagonal;
            this.velZ *= diagonal;
        }
    }

    public void handleVerticalSpeed(PlayerMoveEvent event) {
        if (!this.mc.field_1690.field_1903.method_1434()) {
            if (this.mc.field_1690.field_1832.method_1434()) {
                this.velY -= 0.5d * this.elytraFly.verticalSpeed.get().doubleValue();
                return;
            }
            return;
        }
        this.velY += 0.5d * this.elytraFly.verticalSpeed.get().doubleValue();
    }

    public void handleFallMultiplier() {
        if (this.velY < 0.0d) {
            this.velY *= this.elytraFly.fallMultiplier.get().doubleValue();
        } else if (this.velY > 0.0d) {
            this.velY = 0.0d;
        }
    }

    public void handleAcceleration() {
        if (this.elytraFly.acceleration.get().booleanValue()) {
            if (!PlayerUtils.isMoving()) {
                this.acceleration = 0.0d;
            }
            this.acceleration = Math.min(this.acceleration + this.elytraFly.accelerationMin.get().doubleValue() + (this.elytraFly.accelerationStep.get().doubleValue() * 0.1d), this.elytraFly.horizontalSpeed.get().doubleValue());
            return;
        }
        this.acceleration = 0.0d;
    }

    public void zeroAcceleration() {
        this.acceleration = 0.0d;
    }

    protected double getSpeed() {
        return this.elytraFly.acceleration.get().booleanValue() ? this.acceleration : this.elytraFly.horizontalSpeed.get().doubleValue();
    }

    public String getHudString() {
        return this.type.name();
    }
}
