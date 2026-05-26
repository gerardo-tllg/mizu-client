package meteordevelopment.meteorclient.utils.other;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/other/WindowsMediaController.class */
public class WindowsMediaController {
    public static String getCurrentMedia() {
        try {
            ProcessBuilder builder = new ProcessBuilder("powershell", "-Command", "(Get-Process -Name vlc, chrome, firefox, msedge -ErrorAction SilentlyContinue | Where-Object { $_.MainWindowTitle -match '-' }) | Select-Object -ExpandProperty MainWindowTitle");
            builder.redirectErrorStream(true);
            Process process = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String title = reader.readLine();
            if (title != null) {
                if (!title.isEmpty()) {
                    return title;
                }
            }
            return "Not Playing";
        } catch (Exception e) {
            return "Not Playing";
        }
    }

    public static void playPause() {
        sendMediaCommand("playpause");
    }

    public static void nextTrack() {
        sendMediaCommand("next");
    }

    public static void previousTrack() {
        sendMediaCommand("prev");
    }

    private static void sendMediaCommand(String command) {
        try {
            new ProcessBuilder("powershell", "-Command", "(New-Object -ComObject WScript.Shell).SendKeys('^{VK_MEDIA_" + command.toUpperCase() + "}')").start();
        } catch (IOException e) {
            System.err.println("Failed to send media command: " + command);
        }
    }
}
