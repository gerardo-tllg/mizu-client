package meteordevelopment.meteorclient.utils.other;

import com.sun.jna.platform.win32.COM.util.Factory;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/other/MediaSessionController.class */
public class MediaSessionController {
    public static void play() {
        Factory factory = new Factory();
        ISystemMediaTransportControls controls = (ISystemMediaTransportControls) factory.createObject(ISystemMediaTransportControls.class);
        controls.Play();
    }

    public static void pause() {
        Factory factory = new Factory();
        ISystemMediaTransportControls controls = (ISystemMediaTransportControls) factory.createObject(ISystemMediaTransportControls.class);
        controls.Pause();
    }

    public static void nextTrack() {
        Factory factory = new Factory();
        ISystemMediaTransportControls controls = (ISystemMediaTransportControls) factory.createObject(ISystemMediaTransportControls.class);
        controls.NextTrack();
    }

    public static void previousTrack() {
        Factory factory = new Factory();
        ISystemMediaTransportControls controls = (ISystemMediaTransportControls) factory.createObject(ISystemMediaTransportControls.class);
        controls.PreviousTrack();
    }
}
