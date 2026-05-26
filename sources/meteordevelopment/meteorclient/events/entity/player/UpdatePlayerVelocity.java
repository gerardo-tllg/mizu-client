package meteordevelopment.meteorclient.events.entity.player;

import meteordevelopment.meteorclient.events.Cancellable;
import net.minecraft.class_243;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/entity/player/UpdatePlayerVelocity.class */
public class UpdatePlayerVelocity extends Cancellable {
    class_243 movementInput;
    float speed;
    float yaw;
    class_243 velocity;

    public UpdatePlayerVelocity(class_243 movementInput, float speed, float yaw, class_243 velocity) {
        this.movementInput = movementInput;
        this.speed = speed;
        this.yaw = yaw;
        this.velocity = velocity;
    }

    public class_243 getMovementInput() {
        return this.movementInput;
    }

    public float getSpeed() {
        return this.speed;
    }

    public class_243 getVelocity() {
        return this.velocity;
    }

    public void setVelocity(class_243 velocity) {
        this.velocity = velocity;
    }
}
