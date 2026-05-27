package meteordevelopment.meteorclient.utils.other;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.*;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;

import java.util.ArrayList;
import java.util.List;

public class SpotifyGrabber {
    public interface Kernel32Ext extends Kernel32 {
        Kernel32Ext INSTANCE = Native.load("kernel32", Kernel32Ext.class);

        HANDLE OpenProcess(int dwDesiredAccess, boolean bInheritHandle, int dwProcessId);
        boolean QueryFullProcessImageNameW(HANDLE hProcess, int dwFlags, char[] lpExeName, IntByReference lpdwSize);
        boolean CloseHandle(HANDLE hObject);
        HANDLE CreateToolhelp32Snapshot(int dwFlags, int th32ProcessID);
    }

    public interface User32Ext extends User32 {
        User32Ext INSTANCE = Native.load("user32", User32Ext.class);

        int GetWindowTextW(HWND hWnd, char[] lpString, int nMaxCount);
        int GetWindowThreadProcessId(HWND hWnd, IntByReference lpdwProcessId);
        boolean EnumWindows(WNDENUMPROC lpEnumFunc, Pointer data);
    }

    public interface WNDENUMPROC extends StdCallLibrary.StdCallCallback {
        boolean callback(WinDef.HWND hWnd, Pointer data);
    }


    private static final int PROCESS_QUERY_LIMITED_INFORMATION = 0x1000;
    private static final int TH32CS_SNAPPROCESS = 0x00000002;

    /**
     * Gets all process IDs (PIDs) of processes that contain "Spotify"
     */
    public static List<Integer> getSpotifyPIDs() {
        List<Integer> spotifyPIDs = new ArrayList<>();

        // ✅ Correctly call CreateToolhelp32Snapshot
        WinNT.HANDLE snapshot = Kernel32Ext.INSTANCE.CreateToolhelp32Snapshot(TH32CS_SNAPPROCESS, 0);
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

                if (processPath.toLowerCase().contains("spotify.exe")) { // ✅ Ensure correct process detection
                    spotifyPIDs.add(pid);
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

    /**
     * Retrieves the full executable path of a process using its PID
     */
    private static String getProcessPath(int pid) {
        WinNT.HANDLE hProcess = Kernel32Ext.INSTANCE.OpenProcess(PROCESS_QUERY_LIMITED_INFORMATION, false, pid);
        if (hProcess == null || WinNT.INVALID_HANDLE_VALUE.equals(hProcess)) {
            return "Unknown";
        }

        char[] buffer = new char[1024];
        IntByReference size = new IntByReference(buffer.length);

        boolean success = Kernel32Ext.INSTANCE.QueryFullProcessImageNameW(hProcess, 0, buffer, size);
        Kernel32Ext.INSTANCE.CloseHandle(hProcess);

        if (!success) return "Unknown";

        return new String(buffer, 0, size.getValue()).trim();
    }

    /**
     * Finds the correct Spotify window (one that contains a song title)
     */
    public static String getSpotifySongTitle() {
        List<Integer> spotifyPIDs = getSpotifyPIDs();
        if (spotifyPIDs.isEmpty()) {
            return "Not Playing";
        }

        final char[] windowText = new char[512];
        final WinDef.HWND[] bestMatchHwnd = new WinDef.HWND[1];

        System.out.println("[DEBUG] Checking Spotify windows...");

        // Find all windows that belong to Spotify's PIDs
        User32Ext.INSTANCE.EnumWindows((hWnd, data) -> {
            IntByReference pidRef = new IntByReference();
            User32Ext.INSTANCE.GetWindowThreadProcessId(hWnd, pidRef);
            int windowPID = pidRef.getValue();

            if (spotifyPIDs.contains(windowPID)) {
                User32Ext.INSTANCE.GetWindowTextW(hWnd, windowText, 512);
                String title = Native.toString(windowText).trim();

                System.out.println("[DEBUG] Checking window title: " + title);

                //  Ensure only titles
                if (title.contains(" - ")) {
                    bestMatchHwnd[0] = hWnd;
                    return false; // Stop searching once found
                }
            }
            return true;
        }, null);

        // If a matching window, return its title
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














