package meteordevelopment.orbit;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/orbit/ICancellable.class */
public interface ICancellable {
    void setCancelled(boolean z);

    boolean isCancelled();

    default void cancel() {
        setCancelled(true);
    }
}
