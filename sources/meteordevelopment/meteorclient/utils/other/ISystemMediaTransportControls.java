package meteordevelopment.meteorclient.utils.other;

import com.sun.jna.platform.win32.COM.util.IUnknown;
import com.sun.jna.platform.win32.COM.util.annotation.ComInterface;
import com.sun.jna.platform.win32.COM.util.annotation.ComMethod;

/* JADX INFO: compiled from: MediaSessionController.java */
/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/other/ISystemMediaTransportControls.class */
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
