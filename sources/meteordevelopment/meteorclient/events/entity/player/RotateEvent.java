package meteordevelopment.meteorclient.events.entity.player;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/entity/player/RotateEvent.class */
public class RotateEvent {
    private float yaw;
    private float pitch;
    private boolean modified;

    public RotateEvent(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public float getYaw() {
        return this.yaw;
    }

    public void setYaw(float yaw) {
        this.modified = true;
        setYawNoModify(yaw);
    }

    public float getPitch() {
        return this.pitch;
    }

    public void setPitch(float pitch) {
        this.modified = true;
        setPitchNoModify(pitch);
    }

    public boolean isModified() {
        return this.modified;
    }

    public void setRotation(float yaw, float pitch) {
        setYaw(yaw);
        setPitch(pitch);
    }

    public void setYawNoModify(float yaw) {
        this.yaw = yaw;
    }

    public void setPitchNoModify(float pitch) {
        this.pitch = pitch;
    }
}
