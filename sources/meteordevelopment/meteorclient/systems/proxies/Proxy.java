package meteordevelopment.meteorclient.systems.proxies;

import java.util.Objects;
import java.util.Optional;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import net.minecraft.class_2487;
import net.minecraft.class_2520;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/proxies/Proxy.class */
public class Proxy implements ISerializable<Proxy> {
    public final Settings settings = new Settings();
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final SettingGroup sgOptional = this.settings.createGroup("Optional");
    public Setting<String> name = this.sgGeneral.add(new StringSetting.Builder().name("name").description("The name of the proxy.").build());
    public Setting<ProxyType> type = this.sgGeneral.add(new EnumSetting.Builder().name("type").description("The type of proxy.").defaultValue(ProxyType.Socks5).build());
    public Setting<String> address = this.sgGeneral.add(new StringSetting.Builder().name("address").description("The ip address of the proxy.").filter(Utils::ipFilter).build());
    public Setting<Integer> port = this.sgGeneral.add(new IntSetting.Builder().name("port").description("The port of the proxy.").defaultValue(0).range(0, 65535).sliderMax(65535).noSlider().build());
    public Setting<Boolean> enabled = this.sgGeneral.add(new BoolSetting.Builder().name("enabled").description("Whether the proxy is enabled.").defaultValue(true).build());
    public Setting<String> username = this.sgOptional.add(new StringSetting.Builder().name("username").description("The username of the proxy.").build());
    public Setting<String> password = this.sgOptional.add(new StringSetting.Builder().name("password").description("The password of the proxy.").visible(() -> {
        return this.type.get().equals(ProxyType.Socks5);
    }).build());

    private Proxy() {
    }

    public Proxy(class_2520 tag) {
        fromTag2((class_2487) tag);
    }

    public boolean resolveAddress() {
        return Utils.resolveAddress(this.address.get(), this.port.get().intValue());
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/proxies/Proxy$Builder.class */
    public static class Builder {
        protected ProxyType type = ProxyType.Socks5;
        protected String address = "";
        protected int port = 0;
        protected String name = "";
        protected String username = "";
        protected String password = "";
        protected boolean enabled = false;

        public Builder type(ProxyType type) {
            this.type = type;
            return this;
        }

        public Builder address(String address) {
            this.address = address;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Proxy build() {
            Proxy proxy = new Proxy();
            if (!this.type.equals(proxy.type.getDefaultValue())) {
                proxy.type.set(this.type);
            }
            if (!this.address.equals(proxy.address.getDefaultValue())) {
                proxy.address.set(this.address);
            }
            if (this.port != proxy.port.getDefaultValue().intValue()) {
                proxy.port.set(Integer.valueOf(this.port));
            }
            if (!this.name.equals(proxy.name.getDefaultValue())) {
                proxy.name.set(this.name);
            }
            if (!this.username.equals(proxy.username.getDefaultValue())) {
                proxy.username.set(this.username);
            }
            if (!this.password.equals(proxy.password.getDefaultValue())) {
                proxy.password.set(this.password);
            }
            if (this.enabled != proxy.enabled.getDefaultValue().booleanValue()) {
                proxy.enabled.set(Boolean.valueOf(this.enabled));
            }
            return proxy;
        }
    }

    @Override // meteordevelopment.meteorclient.utils.misc.ISerializable
    public class_2487 toTag() {
        class_2487 tag = new class_2487();
        tag.method_10566("settings", this.settings.toTag());
        return tag;
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // meteordevelopment.meteorclient.utils.misc.ISerializable
    /* JADX INFO: renamed from: fromTag */
    public Proxy fromTag2(class_2487 tag) {
        Optional optionalMethod_10562 = tag.method_10562("settings");
        Settings settings = this.settings;
        Objects.requireNonNull(settings);
        optionalMethod_10562.ifPresent(settings::fromTag2);
        return this;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Proxy proxy = (Proxy) o;
        return Objects.equals(proxy.address.get(), this.address.get()) && Objects.equals(proxy.port.get(), this.port.get());
    }
}
