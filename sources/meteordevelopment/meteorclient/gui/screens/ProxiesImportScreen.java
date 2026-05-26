package meteordevelopment.meteorclient.gui.screens;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Matcher;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WSection;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.systems.proxies.Proxies;
import meteordevelopment.meteorclient.systems.proxies.Proxy;
import meteordevelopment.meteorclient.systems.proxies.ProxyType;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.class_437;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/screens/ProxiesImportScreen.class */
public class ProxiesImportScreen extends WindowScreen {
    private final File file;

    public ProxiesImportScreen(GuiTheme theme, File file) {
        super(theme, "Import Proxies");
        this.file = file;
        onClosed(() -> {
            class_437 patt0$temp = this.parent;
            if (patt0$temp instanceof ProxiesScreen) {
                ProxiesScreen screen = (ProxiesScreen) patt0$temp;
                screen.reload();
            }
        });
    }

    @Override // meteordevelopment.meteorclient.gui.WidgetScreen
    public void initWidgets() {
        if (this.file.exists() && this.file.isFile()) {
            add(this.theme.label("Importing proxies from " + this.file.getName() + "...").color(Color.GREEN));
            WVerticalList list = (WVerticalList) ((WSection) add(this.theme.section("Log", false)).widget()).add(this.theme.verticalList()).expandX().widget();
            Proxies proxies = Proxies.get();
            try {
                int success = 0;
                int fail = 0;
                for (String line : Files.readAllLines(this.file.toPath())) {
                    Proxy proxy = null;
                    Matcher matcher = Proxies.PROXY_PATTERN.matcher(line);
                    if (matcher.matches()) {
                        String address = matcher.group(2).replaceAll("\\b0+\\B", "");
                        int port = Integer.parseInt(matcher.group(3));
                        proxy = new Proxy.Builder().address(address).port(port).name(matcher.group(1) != null ? matcher.group(1) : address + ":" + port).type(matcher.group(4) != null ? ProxyType.parse(matcher.group(4)) : ProxyType.Socks4).build();
                    }
                    Matcher matcher2 = Proxies.PROXY_PATTERN_WEBSHARE.matcher(line);
                    if (proxy == null && matcher2.matches()) {
                        String address2 = matcher2.group(1).replaceAll("\\b0+\\B", "");
                        int port2 = Integer.parseInt(matcher2.group(2));
                        proxy = new Proxy.Builder().address(address2).port(port2).name(address2 + ":" + port2).username(matcher2.group(3) != null ? matcher2.group(3) : "").password(matcher2.group(4) != null ? matcher2.group(4) : "").type(ProxyType.Socks5).build();
                    }
                    Matcher matcher3 = Proxies.PROXY_PATTERN_URI.matcher(line);
                    if (proxy == null && matcher3.matches()) {
                        String address3 = matcher3.group("addr").replaceAll("\\b0+\\B", "");
                        int port3 = Integer.parseInt(matcher3.group("port"));
                        ProxyType type = ProxyType.parse(matcher3.group(1));
                        if (type == null) {
                            type = ((matcher3.group(1) == null || !matcher3.group(1).equals("socks")) && matcher3.group("pass") == null) ? ProxyType.Socks4 : ProxyType.Socks5;
                        }
                        proxy = new Proxy.Builder().address(address3).port(port3).name(address3 + ":" + port3).username(matcher3.group("user") != null ? matcher3.group("user") : "").password(matcher3.group("pass") != null ? matcher3.group("pass") : "").type(type).build();
                    }
                    if (proxy == null) {
                        list.add(this.theme.label("Unrecognised proxy format: " + line).color(Color.RED));
                        fail++;
                    } else if (proxies.add(proxy)) {
                        list.add(this.theme.label("Imported proxy: " + proxy.name.get()).color(Color.GREEN));
                        success++;
                    } else {
                        list.add(this.theme.label("Proxy already exists: " + proxy.name.get()).color(Color.ORANGE));
                        fail++;
                    }
                }
                add(this.theme.label("Successfully imported " + success + "/" + (fail + success) + " proxies.").color(Utils.lerp(Color.RED, Color.GREEN, success / (success + fail))));
            } catch (IOException e) {
                MeteorClient.LOG.error("An error occurred while importing the proxy file", e);
            }
        } else {
            add(this.theme.label("Invalid File!"));
        }
        add(this.theme.horizontalSeparator()).expandX();
        WButton btnBack = (WButton) add(this.theme.button("Back")).expandX().widget();
        btnBack.action = this::method_25419;
    }
}
