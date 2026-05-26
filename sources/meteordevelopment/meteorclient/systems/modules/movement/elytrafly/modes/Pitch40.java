package meteordevelopment.meteorclient.systems.modules.movement.elytrafly.modes;

import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFlightMode;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFlightModes;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/movement/elytrafly/modes/Pitch40.class */
public class Pitch40 extends ElytraFlightMode {
    private boolean pitchingDown;
    private int pitch;

    public Pitch40() {
        super(ElytraFlightModes.Pitch40);
        this.pitchingDown = true;
    }

    @Override // meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFlightMode
    public void onActivate() {
        if (this.mc.field_1724.method_23318() < this.elytraFly.pitch40upperBounds.get().doubleValue()) {
            this.elytraFly.error("Player must be above upper bounds!", new Object[0]);
            this.elytraFly.toggle();
        }
        this.pitch = 40;
    }

    @Override // meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFlightMode
    public void onDeactivate() {
    }

    @Override // meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFlightMode
    public void onTick() {
        super.onTick();
        if (this.pitchingDown && this.mc.field_1724.method_23318() <= this.elytraFly.pitch40lowerBounds.get().doubleValue()) {
            this.pitchingDown = false;
        } else if (!this.pitchingDown && this.mc.field_1724.method_23318() >= this.elytraFly.pitch40upperBounds.get().doubleValue()) {
            this.pitchingDown = true;
        }
        if (!this.pitchingDown && this.mc.field_1724.method_36455() > -40.0f) {
            this.pitch = (int) (((double) this.pitch) - this.elytraFly.pitch40rotationSpeed.get().doubleValue());
            if (this.pitch < -40) {
                this.pitch = -40;
            }
        } else if (this.pitchingDown && this.mc.field_1724.method_36455() < 40.0f) {
            this.pitch = (int) (((double) this.pitch) + this.elytraFly.pitch40rotationSpeed.get().doubleValue());
            if (this.pitch > 40) {
                this.pitch = 40;
            }
        }
        this.mc.field_1724.method_36457(this.pitch);
    }

    @Override // meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFlightMode
    public void autoTakeoff() {
    }

    @Override // meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFlightMode
    public void handleHorizontalSpeed(PlayerMoveEvent event) {
        this.velX = event.movement.field_1352;
        this.velZ = event.movement.field_1350;
    }

    @Override // meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFlightMode
    public void handleVerticalSpeed(PlayerMoveEvent event) {
    }

    @Override // meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFlightMode
    public void handleFallMultiplier() {
    }

    @Override // meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFlightMode
    public void handleAutopilot() {
    }
}
