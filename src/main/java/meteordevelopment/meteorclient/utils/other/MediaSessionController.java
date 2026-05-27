package meteordevelopment.meteorclient.utils.other;

import com.sun.jna.platform.win32.COM.util.Factory;
import com.sun.jna.platform.win32.COM.util.IUnknown;
import com.sun.jna.platform.win32.COM.util.annotation.ComInterface;
import com.sun.jna.platform.win32.COM.util.annotation.ComMethod;
import com.sun.jna.platform.win32.COM.util.annotation.ComObject;

@ComObject(clsId = "{EFA02BA1-47A1-4572-8371-689F5EB50AF6}")
interface ISystemMediaTransportControlsInterop extends IUnknown {
}

@ComInterface(iid = "{BC8C3698-42C5-4C8B-89AA-27D4DA543589}")
interface ISystemMediaTransportControls extends IUnknown {
    @ComMethod
    void Play();

    @ComMethod
    void Pause();

    @ComMethod
    void NextTrack();

    @ComMethod
    void PreviousTrack();
}

public class MediaSessionController {
    public static void play() {
        Factory factory = new Factory();
        ISystemMediaTransportControls controls = factory.createObject(ISystemMediaTransportControls.class);
        controls.Play();
    }

    public static void pause() {
        Factory factory = new Factory();
        ISystemMediaTransportControls controls = factory.createObject(ISystemMediaTransportControls.class);
        controls.Pause();
    }

    public static void nextTrack() {
        Factory factory = new Factory();
        ISystemMediaTransportControls controls = factory.createObject(ISystemMediaTransportControls.class);
        controls.NextTrack();
    }

    public static void previousTrack() {
        Factory factory = new Factory();
        ISystemMediaTransportControls controls = factory.createObject(ISystemMediaTransportControls.class);
        controls.PreviousTrack();
    }
}

