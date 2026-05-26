package meteordevelopment.meteorclient.systems.proxies;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import net.minecraft.class_2487;
import org.jetbrains.annotations.NotNull;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/proxies/Proxies.class */
public class Proxies extends System<Proxies> implements Iterable<Proxy> {
    public static final Pattern PROXY_PATTERN = Pattern.compile("^(?:([\\w\\s]+)=)?((?:0*(?:\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5])(?:\\.(?!:)|)){4}):(?!0)(\\d{1,4}|[1-5]\\d{4}|6[0-4]\\d{3}|65[0-4]\\d{2}|655[0-2]\\d|6553[0-5])(?i:@(socks[45]))?$", 8);
    public static final Pattern PROXY_PATTERN_WEBSHARE = Pattern.compile("^((?:0*(?:\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5])(?:\\.(?!:)|)){4}):(?!0)(\\d{1,4}|[1-5]\\d{4}|6[0-4]\\d{3}|65[0-4]\\d{2}|655[0-2]\\d|6553[0-5])(?::([^:]+)(?::(.+))?)$", 8);
    public static final Pattern PROXY_PATTERN_URI = Pattern.compile("^(?:(socks|socks4|socks5)://)?(?:(?<user>[\\w~-]+)(:(?<pass>[\\w~-]+))?@)?(?<addr>(?:0*(?:\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5])(?:\\.(?!:)|)){4}):(?!0)(?<port>\\d{1,4}|[1-5]\\d{4}|6[0-4]\\d{3}|65[0-4]\\d{2}|655[0-2]\\d|6553[0-5])$", 8);
    private List<Proxy> proxies;

    public Proxies() {
        super("proxies");
        this.proxies = new ArrayList();
    }

    public static Proxies get() {
        return (Proxies) Systems.get(Proxies.class);
    }

    public boolean add(Proxy proxy) {
        for (Proxy p : this.proxies) {
            if (p.type.get().equals(proxy.type.get()) && p.address.get().equals(proxy.address.get()) && Objects.equals(p.port.get(), proxy.port.get())) {
                return false;
            }
        }
        if (this.proxies.isEmpty()) {
            proxy.enabled.set(true);
        }
        this.proxies.add(proxy);
        save();
        return true;
    }

    public void remove(Proxy proxy) {
        if (this.proxies.remove(proxy)) {
            save();
        }
    }

    public Proxy getEnabled() {
        for (Proxy proxy : this.proxies) {
            if (proxy.enabled.get().booleanValue()) {
                return proxy;
            }
        }
        return null;
    }

    public void setEnabled(Proxy proxy, boolean enabled) {
        for (Proxy p : this.proxies) {
            p.enabled.set(false);
        }
        proxy.enabled.set(Boolean.valueOf(enabled));
        save();
    }

    public boolean isEmpty() {
        return this.proxies.isEmpty();
    }

    @Override // java.lang.Iterable
    @NotNull
    public Iterator<Proxy> iterator() {
        return this.proxies.iterator();
    }

    @Override // meteordevelopment.meteorclient.systems.System, meteordevelopment.meteorclient.utils.misc.ISerializable
    public class_2487 toTag() {
        class_2487 tag = new class_2487();
        tag.method_10566("proxies", NbtUtils.listToTag(this.proxies));
        return tag;
    }

    @Override // meteordevelopment.meteorclient.systems.System, meteordevelopment.meteorclient.utils.misc.ISerializable
    /* JADX INFO: renamed from: fromTag */
    public Proxies fromTag2(class_2487 tag) {
        this.proxies = NbtUtils.listFromTag(tag.method_68569("proxies"), Proxy::new);
        return this;
    }
}
