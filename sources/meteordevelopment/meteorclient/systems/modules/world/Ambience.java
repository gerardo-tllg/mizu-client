package meteordevelopment.meteorclient.systems.modules.world;

import java.util.Objects;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.class_243;
import net.minecraft.class_5294;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/world/Ambience.class */
public class Ambience extends Module {
    private final SettingGroup sgSky;
    private final SettingGroup sgWorld;
    public final Setting<Boolean> endSky;
    public final Setting<Boolean> customSkyColor;
    public final Setting<SettingColor> overworldSkyColor;
    public final Setting<SettingColor> netherSkyColor;
    public final Setting<SettingColor> endSkyColor;
    public final Setting<Boolean> customCloudColor;
    public final Setting<SettingColor> cloudColor;
    public final Setting<Boolean> changeLightningColor;
    public final Setting<SettingColor> lightningColor;
    public final Setting<Boolean> customGrassColor;
    public final Setting<SettingColor> grassColor;
    public final Setting<Boolean> customFoliageColor;
    public final Setting<SettingColor> foliageColor;
    public final Setting<Boolean> customWaterColor;
    public final Setting<SettingColor> waterColor;
    public final Setting<Boolean> customLavaColor;
    public final Setting<SettingColor> lavaColor;
    public final Setting<Boolean> customFogColor;
    public final Setting<SettingColor> fogColor;

    public Ambience() {
        super(Categories.World, "ambience", "Change the color of various pieces of the environment.");
        this.sgSky = this.settings.createGroup("Sky");
        this.sgWorld = this.settings.createGroup("World");
        this.endSky = this.sgSky.add(new BoolSetting.Builder().name("end-sky").description("Makes the sky like the end.").defaultValue(false).build());
        this.customSkyColor = this.sgSky.add(new BoolSetting.Builder().name("custom-sky-color").description("Whether the sky color should be changed.").defaultValue(false).build());
        SettingGroup settingGroup = this.sgSky;
        ColorSetting.Builder builderDefaultValue = new ColorSetting.Builder().name("overworld-sky-color").description("The color of the overworld sky.").defaultValue(new SettingColor(0, Opcode.LUSHR, 255));
        Setting<Boolean> setting = this.customSkyColor;
        Objects.requireNonNull(setting);
        this.overworldSkyColor = settingGroup.add(builderDefaultValue.visible(setting::get).build());
        SettingGroup settingGroup2 = this.sgSky;
        ColorSetting.Builder builderDefaultValue2 = new ColorSetting.Builder().name("nether-sky-color").description("The color of the nether sky.").defaultValue(new SettingColor(Opcode.FSUB, 0, 0));
        Setting<Boolean> setting2 = this.customSkyColor;
        Objects.requireNonNull(setting2);
        this.netherSkyColor = settingGroup2.add(builderDefaultValue2.visible(setting2::get).build());
        SettingGroup settingGroup3 = this.sgSky;
        ColorSetting.Builder builderDefaultValue3 = new ColorSetting.Builder().name("end-sky-color").description("The color of the end sky.").defaultValue(new SettingColor(65, 30, 90));
        Setting<Boolean> setting3 = this.customSkyColor;
        Objects.requireNonNull(setting3);
        this.endSkyColor = settingGroup3.add(builderDefaultValue3.visible(setting3::get).build());
        this.customCloudColor = this.sgSky.add(new BoolSetting.Builder().name("custom-cloud-color").description("Whether the clouds color should be changed.").defaultValue(false).build());
        SettingGroup settingGroup4 = this.sgSky;
        ColorSetting.Builder builderDefaultValue4 = new ColorSetting.Builder().name("cloud-color").description("The color of the clouds.").defaultValue(new SettingColor(Opcode.FSUB, 0, 0));
        Setting<Boolean> setting4 = this.customCloudColor;
        Objects.requireNonNull(setting4);
        this.cloudColor = settingGroup4.add(builderDefaultValue4.visible(setting4::get).build());
        this.changeLightningColor = this.sgSky.add(new BoolSetting.Builder().name("custom-lightning-color").description("Whether the lightning color should be changed.").defaultValue(false).build());
        SettingGroup settingGroup5 = this.sgSky;
        ColorSetting.Builder builderDefaultValue5 = new ColorSetting.Builder().name("lightning-color").description("The color of the lightning.").defaultValue(new SettingColor(Opcode.FSUB, 0, 0));
        Setting<Boolean> setting5 = this.changeLightningColor;
        Objects.requireNonNull(setting5);
        this.lightningColor = settingGroup5.add(builderDefaultValue5.visible(setting5::get).build());
        this.customGrassColor = this.sgWorld.add(new BoolSetting.Builder().name("custom-grass-color").description("Whether the grass color should be changed.").defaultValue(false).onChanged(val -> {
            reload();
        }).build());
        SettingGroup settingGroup6 = this.sgWorld;
        ColorSetting.Builder builderDefaultValue6 = new ColorSetting.Builder().name("grass-color").description("The color of the grass.").defaultValue(new SettingColor(Opcode.FSUB, 0, 0));
        Setting<Boolean> setting6 = this.customGrassColor;
        Objects.requireNonNull(setting6);
        this.grassColor = settingGroup6.add(builderDefaultValue6.visible(setting6::get).onChanged(val2 -> {
            reload();
        }).build());
        this.customFoliageColor = this.sgWorld.add(new BoolSetting.Builder().name("custom-foliage-color").description("Whether the foliage color should be changed.").defaultValue(false).onChanged(val3 -> {
            reload();
        }).build());
        SettingGroup settingGroup7 = this.sgWorld;
        ColorSetting.Builder builderDefaultValue7 = new ColorSetting.Builder().name("foliage-color").description("The color of the foliage.").defaultValue(new SettingColor(Opcode.FSUB, 0, 0));
        Setting<Boolean> setting7 = this.customFoliageColor;
        Objects.requireNonNull(setting7);
        this.foliageColor = settingGroup7.add(builderDefaultValue7.visible(setting7::get).onChanged(val4 -> {
            reload();
        }).build());
        this.customWaterColor = this.sgWorld.add(new BoolSetting.Builder().name("custom-water-color").description("Whether the water color should be changed.").defaultValue(false).onChanged(val5 -> {
            reload();
        }).build());
        SettingGroup settingGroup8 = this.sgWorld;
        ColorSetting.Builder builderDefaultValue8 = new ColorSetting.Builder().name("water-color").description("The color of the water.").defaultValue(new SettingColor(Opcode.FSUB, 0, 0));
        Setting<Boolean> setting8 = this.customWaterColor;
        Objects.requireNonNull(setting8);
        this.waterColor = settingGroup8.add(builderDefaultValue8.visible(setting8::get).onChanged(val6 -> {
            reload();
        }).build());
        this.customLavaColor = this.sgWorld.add(new BoolSetting.Builder().name("custom-lava-color").description("Whether the lava color should be changed.").defaultValue(false).onChanged(val7 -> {
            reload();
        }).build());
        SettingGroup settingGroup9 = this.sgWorld;
        ColorSetting.Builder builderDefaultValue9 = new ColorSetting.Builder().name("lava-color").description("The color of the lava.").defaultValue(new SettingColor(Opcode.FSUB, 0, 0));
        Setting<Boolean> setting9 = this.customLavaColor;
        Objects.requireNonNull(setting9);
        this.lavaColor = settingGroup9.add(builderDefaultValue9.visible(setting9::get).onChanged(val8 -> {
            reload();
        }).build());
        this.customFogColor = this.sgWorld.add(new BoolSetting.Builder().name("custom-fog-color").description("Whether the fog color should be changed.").defaultValue(false).build());
        SettingGroup settingGroup10 = this.sgWorld;
        ColorSetting.Builder builderDefaultValue10 = new ColorSetting.Builder().name("fog-color").description("The color of the fog.").defaultValue(new SettingColor(Opcode.FSUB, 0, 0));
        Setting<Boolean> setting10 = this.customFogColor;
        Objects.requireNonNull(setting10);
        this.fogColor = settingGroup10.add(builderDefaultValue10.visible(setting10::get).build());
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        reload();
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onDeactivate() {
        reload();
    }

    private void reload() {
        if (this.mc.field_1769 == null || !isActive()) {
            return;
        }
        this.mc.field_1769.method_3279();
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/world/Ambience$Custom.class */
    public static class Custom extends class_5294 {
        public Custom() {
            super(Float.NaN, true, class_5294.class_5401.field_25641, true, false);
        }

        public class_243 method_28112(class_243 color, float sunHeight) {
            return color.method_1021(0.15000000596046448d);
        }

        public boolean method_28110(int camX, int camY) {
            return false;
        }
    }

    public SettingColor skyColor() {
        switch (PlayerUtils.getDimension()) {
            case Overworld:
                return this.overworldSkyColor.get();
            case Nether:
                return this.netherSkyColor.get();
            case End:
                return this.endSkyColor.get();
            default:
                return null;
        }
    }
}
