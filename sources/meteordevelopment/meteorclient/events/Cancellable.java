package meteordevelopment.meteorclient.events;

import meteordevelopment.orbit.ICancellable;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/events/Cancellable.class */
public class Cancellable implements ICancellable {
    private boolean cancelled = false;

    @Override // meteordevelopment.orbit.ICancellable
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override // meteordevelopment.orbit.ICancellable
    public boolean isCancelled() {
        return this.cancelled;
    }
}
