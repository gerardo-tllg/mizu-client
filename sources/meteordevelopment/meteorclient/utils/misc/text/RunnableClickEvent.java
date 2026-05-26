package meteordevelopment.meteorclient.utils.misc.text;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/misc/text/RunnableClickEvent.class */
public class RunnableClickEvent extends MeteorClickEvent {
    public final Runnable runnable;

    public RunnableClickEvent(Runnable runnable) {
        super(null);
        this.runnable = runnable;
    }
}
