package meteordevelopment.meteorclient.utils.other;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Tlhelp32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/other/SpotifyGrabber.class */
public class SpotifyGrabber {
    private static final int PROCESS_QUERY_LIMITED_INFORMATION = 4096;
    private static final int TH32CS_SNAPPROCESS = 2;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/other/SpotifyGrabber$Kernel32Ext.class */
    public interface Kernel32Ext extends Kernel32 {
        public static final Kernel32Ext INSTANCE = Native.load("kernel32", Kernel32Ext.class);

        WinNT.HANDLE OpenProcess(int i, boolean z, int i2);

        boolean QueryFullProcessImageNameW(WinNT.HANDLE handle, int i, char[] cArr, IntByReference intByReference);

        boolean CloseHandle(WinNT.HANDLE handle);

        WinNT.HANDLE CreateToolhelp32Snapshot(int i, int i2);
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/other/SpotifyGrabber$User32Ext.class */
    public interface User32Ext extends User32 {
        public static final User32Ext INSTANCE = Native.load("user32", User32Ext.class);

        int GetWindowTextW(WinDef.HWND hwnd, char[] cArr, int i);

        int GetWindowThreadProcessId(WinDef.HWND hwnd, IntByReference intByReference);

        boolean EnumWindows(WinUser.WNDENUMPROC wndenumproc, Pointer pointer);
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/other/SpotifyGrabber$WNDENUMPROC.class */
    public interface WNDENUMPROC extends StdCallLibrary.StdCallCallback {
        boolean callback(WinDef.HWND hwnd, Pointer pointer);
    }

    public static List<Integer> getSpotifyPIDs() {
        List<Integer> spotifyPIDs = new ArrayList<>();
        WinNT.HANDLE snapshot = Kernel32Ext.INSTANCE.CreateToolhelp32Snapshot(2, 0);
        if (snapshot == null || WinNT.INVALID_HANDLE_VALUE.equals(snapshot)) {
            System.out.println("[ERROR] Failed to create process snapshot.");
            return spotifyPIDs;
        }
        Tlhelp32.PROCESSENTRY32 processEntry = new Tlhelp32.PROCESSENTRY32();
        processEntry.dwSize = new WinDef.DWORD(processEntry.size());
        System.out.println("[DEBUG] Scanning processes...");
        if (Kernel32Ext.INSTANCE.Process32First(snapshot, processEntry)) {
            do {
                int pid = processEntry.th32ProcessID.intValue();
                String processPath = getProcessPath(pid);
                if (processPath.toLowerCase().contains("spotify.exe")) {
                    spotifyPIDs.add(Integer.valueOf(pid));
                    System.out.println("[DEBUG] Matched Spotify PID: " + pid + " (" + processPath + ")");
                }
            } while (Kernel32Ext.INSTANCE.Process32Next(snapshot, processEntry));
        }
        Kernel32Ext.INSTANCE.CloseHandle(snapshot);
        if (spotifyPIDs.isEmpty()) {
            System.out.println("[DEBUG] No Spotify process found.");
        }
        return spotifyPIDs;
    }

    private static String getProcessPath(int pid) {
        WinNT.HANDLE hProcess = Kernel32Ext.INSTANCE.OpenProcess(4096, false, pid);
        if (hProcess == null || WinNT.INVALID_HANDLE_VALUE.equals(hProcess)) {
            return "Unknown";
        }
        char[] buffer = new char[1024];
        IntByReference size = new IntByReference(buffer.length);
        boolean success = Kernel32Ext.INSTANCE.QueryFullProcessImageNameW(hProcess, 0, buffer, size);
        Kernel32Ext.INSTANCE.CloseHandle(hProcess);
        return !success ? "Unknown" : new String(buffer, 0, size.getValue()).trim();
    }

    public static String getSpotifySongTitle() {
        List<Integer> spotifyPIDs = getSpotifyPIDs();
        if (spotifyPIDs.isEmpty()) {
            return "Not Playing";
        }
        char[] windowText = new char[512];
        WinDef.HWND[] bestMatchHwnd = new WinDef.HWND[1];
        System.out.println("[DEBUG] Checking Spotify windows...");
        User32Ext.INSTANCE.EnumWindows((hWnd, data) -> {
            IntByReference pidRef = new IntByReference();
            User32Ext.INSTANCE.GetWindowThreadProcessId(hWnd, pidRef);
            int windowPID = pidRef.getValue();
            if (spotifyPIDs.contains(Integer.valueOf(windowPID))) {
                User32Ext.INSTANCE.GetWindowTextW(hWnd, windowText, 512);
                String title = Native.toString(windowText).trim();
                System.out.println("[DEBUG] Checking window title: " + title);
                if (title.contains(" - ")) {
                    bestMatchHwnd[0] = hWnd;
                    return false;
                }
                return true;
            }
            return true;
        }, null);
        if (bestMatchHwnd[0] != null) {
            User32Ext.INSTANCE.GetWindowTextW(bestMatchHwnd[0], windowText, 512);
            String fullTitle = Native.toString(windowText).trim();
            System.out.println("[DEBUG] Found Spotify Song Title: " + fullTitle);
            if (fullTitle.endsWith(" - Spotify")) {
                fullTitle = fullTitle.substring(0, fullTitle.length() - 10).trim();
            }
            return fullTitle.isEmpty() ? "Not Playing" : fullTitle;
        }
        System.out.println("[DEBUG] No valid Spotify song title found.");
        return "Not Playing";
    }
}
