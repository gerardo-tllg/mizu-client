package meteordevelopment.meteorclient.systems.hud.elements;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.hud.Alignment;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.class_1657;
import net.minecraft.class_742;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/hud/elements/PlayerRadarHud.class */
public class PlayerRadarHud extends HudElement {
    public static final HudElementInfo<PlayerRadarHud> INFO = new HudElementInfo<>(Hud.GROUP, "player-radar", "Displays players in your visual range.", PlayerRadarHud::new);
    private final SettingGroup sgGeneral;
    private final SettingGroup sgScale;
    private final SettingGroup sgBackground;
    private final Setting<Integer> limit;
    private final Setting<Boolean> distance;
    private final Setting<Boolean> totemPops;
    private final Setting<Boolean> friends;
    private final Setting<Boolean> shadow;
    private final Setting<SettingColor> primaryColor;
    private final Setting<SettingColor> secondaryColor;
    private final Setting<SettingColor> totemPopColor;
    private final Setting<Alignment> alignment;
    private final Setting<Integer> border;
    private final Setting<Boolean> customScale;
    private final Setting<Double> scale;
    private final Setting<Boolean> background;
    private final Setting<SettingColor> backgroundColor;
    private final List<class_742> players;

    public PlayerRadarHud() {
        super(INFO);
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgScale = this.settings.createGroup("Scale");
        this.sgBackground = this.settings.createGroup("Background");
        this.limit = this.sgGeneral.add(new IntSetting.Builder().name("limit").description("The max number of players to show.").defaultValue(10).min(1).sliderRange(1, 20).build());
        this.distance = this.sgGeneral.add(new BoolSetting.Builder().name("distance").description("Shows the distance to the player next to their name.").defaultValue(false).build());
        this.totemPops = this.sgGeneral.add(new BoolSetting.Builder().name("display-totem-pops").description("Whether to show totem pops or not.").defaultValue(true).build());
        this.friends = this.sgGeneral.add(new BoolSetting.Builder().name("display-friends").description("Whether to show friends or not.").defaultValue(true).build());
        this.shadow = this.sgGeneral.add(new BoolSetting.Builder().name("shadow").description("Renders shadow behind text.").defaultValue(true).build());
        this.primaryColor = this.sgGeneral.add(new ColorSetting.Builder().name("primary-color").description("Primary color.").defaultValue(new SettingColor()).build());
        this.secondaryColor = this.sgGeneral.add(new ColorSetting.Builder().name("secondary-color").description("Secondary color.").defaultValue(new SettingColor(Opcode.DRETURN, Opcode.DRETURN, Opcode.DRETURN)).build());
        this.totemPopColor = this.sgGeneral.add(new ColorSetting.Builder().name("totem-pop-color").description("Totem pop color.").defaultValue(new SettingColor(225, Opcode.ISHL, 20)).build());
        this.alignment = this.sgGeneral.add(new EnumSetting.Builder().name("alignment").description("Horizontal alignment.").defaultValue(Alignment.Auto).build());
        this.border = this.sgGeneral.add(new IntSetting.Builder().name("border").description("How much space to add around the element.").defaultValue(0).build());
        this.customScale = this.sgScale.add(new BoolSetting.Builder().name("custom-scale").description("Applies custom text scale rather than the global one.").defaultValue(false).build());
        SettingGroup settingGroup = this.sgScale;
        DoubleSetting.Builder builderDescription = new DoubleSetting.Builder().name("scale").description("Custom scale.");
        Setting<Boolean> setting = this.customScale;
        Objects.requireNonNull(setting);
        this.scale = settingGroup.add(builderDescription.visible(setting::get).defaultValue(1.0d).min(0.5d).sliderRange(0.5d, 3.0d).build());
        this.background = this.sgBackground.add(new BoolSetting.Builder().name("background").description("Displays background.").defaultValue(false).build());
        SettingGroup settingGroup2 = this.sgBackground;
        ColorSetting.Builder builderDescription2 = new ColorSetting.Builder().name("background-color").description("Color used for the background.");
        Setting<Boolean> setting2 = this.background;
        Objects.requireNonNull(setting2);
        this.backgroundColor = settingGroup2.add(builderDescription2.visible(setting2::get).defaultValue(new SettingColor(25, 25, 25, 50)).build());
        this.players = new ArrayList();
    }

    @Override // meteordevelopment.meteorclient.systems.hud.HudElement
    public void setSize(double width, double height) {
        super.setSize(width + ((double) (this.border.get().intValue() * 2)), height + ((double) (this.border.get().intValue() * 2)));
    }

    @Override // meteordevelopment.meteorclient.systems.hud.HudElement
    protected double alignX(double width, Alignment alignment) {
        return this.box.alignX(getWidth() - (this.border.get().intValue() * 2), width, alignment);
    }

    @Override // meteordevelopment.meteorclient.systems.hud.HudElement
    public void tick(HudRenderer renderer) {
        double width = renderer.textWidth("Players:", this.shadow.get().booleanValue(), getScale());
        double height = renderer.textHeight(this.shadow.get().booleanValue(), getScale());
        if (MeteorClient.mc.field_1687 == null) {
            setSize(width, height);
            return;
        }
        for (class_1657 entity : getPlayers()) {
            if (!entity.equals(MeteorClient.mc.field_1724) && (this.friends.get().booleanValue() || !Friends.get().isFriend(entity))) {
                String text = entity.method_5477().getString();
                if (this.distance.get().booleanValue()) {
                    text = text + String.format("(%sm)", Integer.valueOf(Math.round(MeteorClient.mc.method_1560().method_5739(entity))));
                }
                width = Math.max(width, renderer.textWidth(text, this.shadow.get().booleanValue(), getScale()));
                height += renderer.textHeight(this.shadow.get().booleanValue(), getScale()) + 2.0d;
            }
        }
        setSize(width, height);
    }

    @Override // meteordevelopment.meteorclient.systems.hud.HudElement
    public void render(HudRenderer renderer) {
        double y = this.y + this.border.get().intValue();
        if (this.background.get().booleanValue()) {
            renderer.quad(this.x, this.y, getWidth(), getHeight(), this.backgroundColor.get());
        }
        renderer.text("Players:", ((double) (this.x + this.border.get().intValue())) + alignX(renderer.textWidth("Players:", this.shadow.get().booleanValue(), getScale()), this.alignment.get()), y, this.secondaryColor.get(), this.shadow.get().booleanValue(), getScale());
        if (MeteorClient.mc.field_1687 == null) {
            return;
        }
        double spaceWidth = renderer.textWidth(" ", this.shadow.get().booleanValue(), getScale());
        for (class_1657 entity : getPlayers()) {
            if (!entity.equals(MeteorClient.mc.field_1724) && (this.friends.get().booleanValue() || !Friends.get().isFriend(entity))) {
                String text = entity.method_5477().getString();
                Color color = PlayerUtils.getPlayerColor(entity, this.primaryColor.get());
                String distanceText = null;
                double width = renderer.textWidth(text, this.shadow.get().booleanValue(), getScale());
                if (this.distance.get().booleanValue()) {
                    width += spaceWidth;
                }
                if (this.distance.get().booleanValue()) {
                    distanceText = String.format("(%sm)", Integer.valueOf(Math.round(MeteorClient.mc.method_1560().method_5739(entity))));
                    width += renderer.textWidth(distanceText, this.shadow.get().booleanValue(), getScale());
                }
                double x = ((double) (this.x + this.border.get().intValue())) + alignX(width, this.alignment.get());
                y += renderer.textHeight(this.shadow.get().booleanValue(), getScale()) + 2.0d;
                double x2 = renderer.text(text, x, y, color, this.shadow.get().booleanValue());
                if (this.distance.get().booleanValue()) {
                    renderer.text(distanceText, x2 + spaceWidth, y, this.secondaryColor.get(), this.shadow.get().booleanValue(), getScale());
                }
                if (this.totemPops.get().booleanValue() && 0 != 0) {
                    renderer.text(null, x2 + spaceWidth, y, this.totemPopColor.get(), this.shadow.get().booleanValue(), getScale());
                }
            }
        }
    }

    private List<class_742> getPlayers() {
        this.players.clear();
        this.players.addAll(MeteorClient.mc.field_1687.method_18456());
        if (this.players.size() > this.limit.get().intValue()) {
            this.players.subList(this.limit.get().intValue() - 1, this.players.size() - 1).clear();
        }
        this.players.sort(Comparator.comparingDouble(e -> {
            return e.method_5858(MeteorClient.mc.method_1560());
        }));
        return this.players;
    }

    private double getScale() {
        if (this.customScale.get().booleanValue()) {
            return this.scale.get().doubleValue();
        }
        return -1.0d;
    }
}
