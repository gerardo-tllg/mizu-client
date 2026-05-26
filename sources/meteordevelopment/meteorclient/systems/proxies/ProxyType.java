package meteordevelopment.meteorclient.systems.proxies;

import org.jetbrains.annotations.Nullable;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/proxies/ProxyType.class */
public enum ProxyType {
    Socks4,
    Socks5;

    @Nullable
    public static ProxyType parse(String group) {
        for (ProxyType type : values()) {
            if (type.name().equalsIgnoreCase(group)) {
                return type;
            }
        }
        return null;
    }
}
