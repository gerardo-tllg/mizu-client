package meteordevelopment.meteorclient.systems.modules.render;

import java.util.Objects;
import java.util.Set;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_2960;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/Chams.class */
public class Chams extends Module {
    private final SettingGroup sgThroughWalls;
    private final SettingGroup sgPlayers;
    private final SettingGroup sgCrystals;
    private final SettingGroup sgHand;
    public final Setting<Set<class_1299<?>>> entities;
    public final Setting<Shader> shader;
    public final Setting<SettingColor> shaderColor;
    public final Setting<Boolean> ignoreSelfDepth;
    public final Setting<Boolean> players;
    public final Setting<Boolean> ignoreSelf;
    public final Setting<Boolean> playersTexture;
    public final Setting<SettingColor> playersColor;
    public final Setting<Double> playersScale;
    public final Setting<Boolean> crystals;
    public final Setting<Double> crystalsScale;
    public final Setting<Double> crystalsBounce;
    public final Setting<Double> crystalsRotationSpeed;
    public final Setting<Boolean> crystalsTexture;
    public final Setting<Boolean> renderCore;
    public final Setting<SettingColor> crystalsCoreColor;
    public final Setting<Boolean> renderFrame1;
    public final Setting<SettingColor> crystalsFrame1Color;
    public final Setting<Boolean> renderFrame2;
    public final Setting<SettingColor> crystalsFrame2Color;
    public final Setting<Boolean> hand;
    public final Setting<Boolean> handTexture;
    public final Setting<SettingColor> handColor;
    public static final class_2960 BLANK = MeteorClient.identifier("textures/blank.png");

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/Chams$Shader.class */
    public enum Shader {
        Image,
        None
    }

    public Chams() {
        super(Categories.Render, "chams", "Tweaks rendering of entities.");
        this.sgThroughWalls = this.settings.createGroup("Through Walls");
        this.sgPlayers = this.settings.createGroup("Players");
        this.sgCrystals = this.settings.createGroup("Crystals");
        this.sgHand = this.settings.createGroup("Hand");
        this.entities = this.sgThroughWalls.add(new EntityTypeListSetting.Builder().name("entities").description("Select entities to show through walls.").build());
        this.shader = this.sgThroughWalls.add(new EnumSetting.Builder().name("shader").description("Renders a shader over of the entities.").defaultValue(Shader.Image).onModuleActivated(setting -> {
            updateShader((Shader) setting.get());
        }).onChanged(this::updateShader).build());
        this.shaderColor = this.sgThroughWalls.add(new ColorSetting.Builder().name("color").description("The color that the shader is drawn with.").defaultValue(new SettingColor(255, 255, 255, Opcode.FCMPG)).visible(() -> {
            return this.shader.get() != Shader.None;
        }).build());
        this.ignoreSelfDepth = this.sgThroughWalls.add(new BoolSetting.Builder().name("ignore-self").description("Ignores yourself drawing the player.").defaultValue(true).build());
        this.players = this.sgPlayers.add(new BoolSetting.Builder().name("players").description("Enables model tweaks for players.").defaultValue(false).build());
        SettingGroup settingGroup = this.sgPlayers;
        BoolSetting.Builder builderDefaultValue = new BoolSetting.Builder().name("ignore-self").description("Ignores yourself when tweaking player models.").defaultValue(false);
        Setting<Boolean> setting2 = this.players;
        Objects.requireNonNull(setting2);
        this.ignoreSelf = settingGroup.add(builderDefaultValue.visible(setting2::get).build());
        SettingGroup settingGroup2 = this.sgPlayers;
        BoolSetting.Builder builderDefaultValue2 = new BoolSetting.Builder().name("texture").description("Enables player model textures.").defaultValue(false);
        Setting<Boolean> setting3 = this.players;
        Objects.requireNonNull(setting3);
        this.playersTexture = settingGroup2.add(builderDefaultValue2.visible(setting3::get).build());
        SettingGroup settingGroup3 = this.sgPlayers;
        ColorSetting.Builder builderDefaultValue3 = new ColorSetting.Builder().name("color").description("The color of player models.").defaultValue(new SettingColor(Opcode.IFNULL, Opcode.I2D, 254, Opcode.FCMPG));
        Setting<Boolean> setting4 = this.players;
        Objects.requireNonNull(setting4);
        this.playersColor = settingGroup3.add(builderDefaultValue3.visible(setting4::get).build());
        SettingGroup settingGroup4 = this.sgPlayers;
        DoubleSetting.Builder builderMin = new DoubleSetting.Builder().name("scale").description("Players scale.").defaultValue(1.0d).min(0.0d);
        Setting<Boolean> setting5 = this.players;
        Objects.requireNonNull(setting5);
        this.playersScale = settingGroup4.add(builderMin.visible(setting5::get).build());
        this.crystals = this.sgCrystals.add(new BoolSetting.Builder().name("crystals").description("Enables model tweaks for end crystals.").defaultValue(false).build());
        SettingGroup settingGroup5 = this.sgCrystals;
        DoubleSetting.Builder builderMin2 = new DoubleSetting.Builder().name("scale").description("Crystal scale.").defaultValue(0.6d).min(0.0d);
        Setting<Boolean> setting6 = this.crystals;
        Objects.requireNonNull(setting6);
        this.crystalsScale = settingGroup5.add(builderMin2.visible(setting6::get).build());
        SettingGroup settingGroup6 = this.sgCrystals;
        DoubleSetting.Builder builderMin3 = new DoubleSetting.Builder().name("bounce").description("How high crystals bounce.").defaultValue(0.6d).min(0.0d);
        Setting<Boolean> setting7 = this.crystals;
        Objects.requireNonNull(setting7);
        this.crystalsBounce = settingGroup6.add(builderMin3.visible(setting7::get).build());
        SettingGroup settingGroup7 = this.sgCrystals;
        DoubleSetting.Builder builderMin4 = new DoubleSetting.Builder().name("rotation-speed").description("Multiplies the rotation speed of the crystal.").defaultValue(0.3d).min(0.0d);
        Setting<Boolean> setting8 = this.crystals;
        Objects.requireNonNull(setting8);
        this.crystalsRotationSpeed = settingGroup7.add(builderMin4.visible(setting8::get).build());
        SettingGroup settingGroup8 = this.sgCrystals;
        BoolSetting.Builder builderDefaultValue4 = new BoolSetting.Builder().name("texture").description("Whether to render crystal model textures.").defaultValue(true);
        Setting<Boolean> setting9 = this.crystals;
        Objects.requireNonNull(setting9);
        this.crystalsTexture = settingGroup8.add(builderDefaultValue4.visible(setting9::get).build());
        SettingGroup settingGroup9 = this.sgCrystals;
        BoolSetting.Builder builderDefaultValue5 = new BoolSetting.Builder().name("render-core").description("Enables rendering of the core of the crystal.").defaultValue(false);
        Setting<Boolean> setting10 = this.crystals;
        Objects.requireNonNull(setting10);
        this.renderCore = settingGroup9.add(builderDefaultValue5.visible(setting10::get).build());
        this.crystalsCoreColor = this.sgCrystals.add(new ColorSetting.Builder().name("core-color").description("The color of the core of the crystal.").defaultValue(new SettingColor(Opcode.IFNULL, Opcode.I2D, 254, 255)).visible(() -> {
            return this.crystals.get().booleanValue() && this.renderCore.get().booleanValue();
        }).build());
        SettingGroup settingGroup10 = this.sgCrystals;
        BoolSetting.Builder builderDefaultValue6 = new BoolSetting.Builder().name("render-inner-frame").description("Enables rendering of the inner frame of the crystal.").defaultValue(true);
        Setting<Boolean> setting11 = this.crystals;
        Objects.requireNonNull(setting11);
        this.renderFrame1 = settingGroup10.add(builderDefaultValue6.visible(setting11::get).build());
        this.crystalsFrame1Color = this.sgCrystals.add(new ColorSetting.Builder().name("inner-frame-color").description("The color of the inner frame of the crystal.").defaultValue(new SettingColor(Opcode.IFNULL, Opcode.I2D, 254, 255)).visible(() -> {
            return this.crystals.get().booleanValue() && this.renderFrame1.get().booleanValue();
        }).build());
        SettingGroup settingGroup11 = this.sgCrystals;
        BoolSetting.Builder builderDefaultValue7 = new BoolSetting.Builder().name("render-outer-frame").description("Enables rendering of the outer frame of the crystal.").defaultValue(true);
        Setting<Boolean> setting12 = this.crystals;
        Objects.requireNonNull(setting12);
        this.renderFrame2 = settingGroup11.add(builderDefaultValue7.visible(setting12::get).build());
        this.crystalsFrame2Color = this.sgCrystals.add(new ColorSetting.Builder().name("outer-frame-color").description("The color of the outer frame of the crystal.").defaultValue(new SettingColor(Opcode.IFNULL, Opcode.I2D, 254, 255)).visible(() -> {
            return this.crystals.get().booleanValue() && this.renderFrame2.get().booleanValue();
        }).build());
        this.hand = this.sgHand.add(new BoolSetting.Builder().name("enabled").description("Enables tweaks of hand rendering.").defaultValue(false).build());
        SettingGroup settingGroup12 = this.sgHand;
        BoolSetting.Builder builderDefaultValue8 = new BoolSetting.Builder().name("texture").description("Whether to render hand textures.").defaultValue(false);
        Setting<Boolean> setting13 = this.hand;
        Objects.requireNonNull(setting13);
        this.handTexture = settingGroup12.add(builderDefaultValue8.visible(setting13::get).build());
        SettingGroup settingGroup13 = this.sgHand;
        ColorSetting.Builder builderDefaultValue9 = new ColorSetting.Builder().name("hand-color").description("The color of your hand.").defaultValue(new SettingColor(Opcode.IFNULL, Opcode.I2D, 254, Opcode.FCMPG));
        Setting<Boolean> setting14 = this.hand;
        Objects.requireNonNull(setting14);
        this.handColor = settingGroup13.add(builderDefaultValue9.visible(setting14::get).build());
    }

    public boolean shouldRender(class_1297 entity) {
        return isActive() && !isShader() && this.entities.get().contains(entity.method_5864()) && (entity != this.mc.field_1724 || this.ignoreSelfDepth.get().booleanValue());
    }

    public boolean isShader() {
        return isActive() && this.shader.get() != Shader.None;
    }

    public void updateShader(Shader value) {
    }
}
