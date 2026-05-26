package meteordevelopment.meteorclient;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/Main.class */
public class Main {
    public static void main(String[] args) throws IllegalAccessException, UnsupportedLookAndFeelException, InstantiationException, ClassNotFoundException {
        String path;
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        int option = JOptionPane.showOptionDialog((Component) null, "To install Meteor Client you need to put it in your mods folder and run Fabric for latest Minecraft version.", "Meteor Client", 0, 0, (Icon) null, new String[]{"Open Wiki", "Open Mods Folder"}, (Object) null);
        switch (option) {
            case 0:
                getOS().open("https://meteorclient.com/faq/installation");
                break;
            case 1:
                switch (getOS().ordinal()) {
                    case 1:
                        path = System.getenv("AppData") + "/.minecraft/mods";
                        break;
                    case 2:
                        path = System.getProperty("user.home") + "/Library/Application Support/minecraft/mods";
                        break;
                    default:
                        path = System.getProperty("user.home") + "/.minecraft";
                        break;
                }
                File mods = new File(path);
                if (!mods.exists()) {
                    mods.mkdirs();
                }
                getOS().open(mods);
                break;
        }
    }

    private static OperatingSystem getOS() {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        return (os.contains("linux") || os.contains("unix")) ? OperatingSystem.LINUX : os.contains("mac") ? OperatingSystem.OSX : os.contains("win") ? OperatingSystem.WINDOWS : OperatingSystem.UNKNOWN;
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/Main$OperatingSystem.class */
    private enum OperatingSystem {
        LINUX,
        WINDOWS { // from class: meteordevelopment.meteorclient.Main.OperatingSystem.1
            @Override // meteordevelopment.meteorclient.Main.OperatingSystem
            protected String[] getURLOpenCommand(URL url) {
                return new String[]{"rundll32", "url.dll,FileProtocolHandler", url.toString()};
            }
        },
        OSX { // from class: meteordevelopment.meteorclient.Main.OperatingSystem.2
            @Override // meteordevelopment.meteorclient.Main.OperatingSystem
            protected String[] getURLOpenCommand(URL url) {
                return new String[]{"open", url.toString()};
            }
        },
        UNKNOWN;

        public void open(URL url) {
            try {
                Runtime.getRuntime().exec(getURLOpenCommand(url));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void open(String url) {
            try {
                open(new URI(url).toURL());
            } catch (MalformedURLException | URISyntaxException e) {
                e.printStackTrace();
            }
        }

        public void open(File file) {
            try {
                open(file.toURI().toURL());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        protected String[] getURLOpenCommand(URL url) {
            String string = url.toString();
            if ("file".equals(url.getProtocol())) {
                string = string.replace("file:", "file://");
            }
            return new String[]{"xdg-open", string};
        }
    }
}
