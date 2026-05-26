package meteordevelopment.meteorclient.systems.waypoints;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.settings.BlockPosSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ProvidedStringSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.Dimension;
import net.minecraft.class_1044;
import net.minecraft.class_2338;
import net.minecraft.class_2487;
import net.minecraft.class_2520;
import net.minecraft.class_4844;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/waypoints/Waypoint.class */
public class Waypoint implements ISerializable<Waypoint> {
    public final Settings settings;
    private final SettingGroup sgVisual;
    private final SettingGroup sgPosition;
    public Setting<String> name;
    public Setting<String> icon;
    public Setting<SettingColor> color;
    public Setting<Boolean> visible;
    public Setting<Integer> maxVisible;
    public Setting<Double> scale;
    public Setting<class_2338> pos;
    public Setting<Dimension> dimension;
    public Setting<Boolean> opposite;
    public final UUID uuid;

    private Waypoint() {
        this.settings = new Settings();
        this.sgVisual = this.settings.createGroup("Visual");
        this.sgPosition = this.settings.createGroup("Position");
        this.name = this.sgVisual.add(new StringSetting.Builder().name("name").description("The name of the waypoint.").defaultValue("Home").build());
        this.icon = this.sgVisual.add(new ProvidedStringSetting.Builder().name("icon").description("The icon of the waypoint.").defaultValue("Square").supplier(() -> {
            return Waypoints.BUILTIN_ICONS;
        }).onChanged(v -> {
            validateIcon();
        }).build());
        this.color = this.sgVisual.add(new ColorSetting.Builder().name("color").description("The color of the waypoint.").defaultValue(MeteorClient.ADDON.color.toSetting()).build());
        this.visible = this.sgVisual.add(new BoolSetting.Builder().name("visible").description("Whether to show the waypoint.").defaultValue(true).build());
        this.maxVisible = this.sgVisual.add(new IntSetting.Builder().name("max-visible-distance").description("How far away to render the waypoint.").defaultValue(5000).build());
        this.scale = this.sgVisual.add(new DoubleSetting.Builder().name("scale").description("The scale of the waypoint.").defaultValue(1.0d).build());
        this.pos = this.sgPosition.add(new BlockPosSetting.Builder().name("location").description("The location of the waypoint.").defaultValue(class_2338.field_10980).build());
        this.dimension = this.sgPosition.add(new EnumSetting.Builder().name("dimension").description("Which dimension the waypoint is in.").defaultValue(Dimension.Overworld).build());
        this.opposite = this.sgPosition.add(new BoolSetting.Builder().name("opposite-dimension").description("Whether to show the waypoint in the opposite dimension.").defaultValue(true).visible(() -> {
            return this.dimension.get() != Dimension.End;
        }).build());
        this.uuid = UUID.randomUUID();
    }

    public Waypoint(class_2520 tag) {
        this.settings = new Settings();
        this.sgVisual = this.settings.createGroup("Visual");
        this.sgPosition = this.settings.createGroup("Position");
        this.name = this.sgVisual.add(new StringSetting.Builder().name("name").description("The name of the waypoint.").defaultValue("Home").build());
        this.icon = this.sgVisual.add(new ProvidedStringSetting.Builder().name("icon").description("The icon of the waypoint.").defaultValue("Square").supplier(() -> {
            return Waypoints.BUILTIN_ICONS;
        }).onChanged(v -> {
            validateIcon();
        }).build());
        this.color = this.sgVisual.add(new ColorSetting.Builder().name("color").description("The color of the waypoint.").defaultValue(MeteorClient.ADDON.color.toSetting()).build());
        this.visible = this.sgVisual.add(new BoolSetting.Builder().name("visible").description("Whether to show the waypoint.").defaultValue(true).build());
        this.maxVisible = this.sgVisual.add(new IntSetting.Builder().name("max-visible-distance").description("How far away to render the waypoint.").defaultValue(5000).build());
        this.scale = this.sgVisual.add(new DoubleSetting.Builder().name("scale").description("The scale of the waypoint.").defaultValue(1.0d).build());
        this.pos = this.sgPosition.add(new BlockPosSetting.Builder().name("location").description("The location of the waypoint.").defaultValue(class_2338.field_10980).build());
        this.dimension = this.sgPosition.add(new EnumSetting.Builder().name("dimension").description("Which dimension the waypoint is in.").defaultValue(Dimension.Overworld).build());
        this.opposite = this.sgPosition.add(new BoolSetting.Builder().name("opposite-dimension").description("Whether to show the waypoint in the opposite dimension.").defaultValue(true).visible(() -> {
            return this.dimension.get() != Dimension.End;
        }).build());
        class_2487 nbt = (class_2487) tag;
        if (nbt.method_10545("uuid")) {
            this.uuid = (UUID) nbt.method_67491("uuid", class_4844.field_25122).get();
        } else {
            this.uuid = UUID.randomUUID();
        }
        fromTag(nbt);
    }

    public void renderIcon(double x, double y, double a, double size) {
        class_1044 texture = Waypoints.get().icons.get(this.icon.get());
        if (texture == null) {
            return;
        }
        int preA = this.color.get().a;
        SettingColor settingColor = this.color.get();
        settingColor.a = (int) (((double) settingColor.a) * a);
        Renderer2D.TEXTURE.begin();
        Renderer2D.TEXTURE.texQuad(x, y, size, size, this.color.get());
        Renderer2D.TEXTURE.render(texture.method_68004());
        this.color.get().a = preA;
    }

    public class_2338 getPos() {
        Dimension dim = this.dimension.get();
        class_2338 pos = this.pos.get();
        Dimension currentDim = PlayerUtils.getDimension();
        if (dim == currentDim || dim.equals(Dimension.End)) {
            return this.pos.get();
        }
        switch (dim) {
            case Overworld:
                return new class_2338(pos.method_10263() / 8, pos.method_10264(), pos.method_10260() / 8);
            case Nether:
                return new class_2338(pos.method_10263() * 8, pos.method_10264(), pos.method_10260() * 8);
            default:
                return null;
        }
    }

    private void validateIcon() {
        Map<String, class_1044> icons = Waypoints.get().icons;
        class_1044 texture = icons.get(this.icon.get());
        if (texture == null && !icons.isEmpty()) {
            this.icon.set(icons.keySet().iterator().next());
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/waypoints/Waypoint$Builder.class */
    public static class Builder {
        private String name = "";
        private String icon = "";
        private class_2338 pos = class_2338.field_10980;
        private Dimension dimension = Dimension.Overworld;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder icon(String icon) {
            this.icon = icon;
            return this;
        }

        public Builder pos(class_2338 pos) {
            this.pos = pos;
            return this;
        }

        public Builder dimension(Dimension dimension) {
            this.dimension = dimension;
            return this;
        }

        public Waypoint build() {
            Waypoint waypoint = new Waypoint();
            if (!this.name.equals(waypoint.name.getDefaultValue())) {
                waypoint.name.set(this.name);
            }
            if (!this.icon.equals(waypoint.icon.getDefaultValue())) {
                waypoint.icon.set(this.icon);
            }
            if (!this.pos.equals(waypoint.pos.getDefaultValue())) {
                waypoint.pos.set(this.pos);
            }
            if (!this.dimension.equals(waypoint.dimension.getDefaultValue())) {
                waypoint.dimension.set(this.dimension);
            }
            return waypoint;
        }
    }

    @Override // meteordevelopment.meteorclient.utils.misc.ISerializable
    public class_2487 toTag() {
        class_2487 tag = new class_2487();
        tag.method_67494("uuid", class_4844.field_25122, this.uuid);
        tag.method_10566("settings", this.settings.toTag());
        return tag;
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // meteordevelopment.meteorclient.utils.misc.ISerializable
    /* JADX INFO: renamed from: fromTag */
    public Waypoint fromTag(class_2487 tag) {
        if (tag.method_10545("settings")) {
            this.settings.fromTag(tag.method_68568("settings"));
        }
        return this;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Waypoint waypoint = (Waypoint) o;
        return Objects.equals(this.uuid, waypoint.uuid);
    }

    public int hashCode() {
        return Objects.hashCode(this.uuid);
    }

    public String toString() {
        return this.name.get();
    }
}
