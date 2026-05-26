package meteordevelopment.meteorclient.systems.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.renderer.Fonts;
import meteordevelopment.meteorclient.renderer.text.FontFace;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.FontFaceSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ModuleListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.class_2487;
import net.minecraft.class_2499;
import net.minecraft.class_2519;
import net.minecraft.class_2520;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/config/Config.class */
public class Config extends System<Config> {
    public final Settings settings;
    private final SettingGroup sgVisual;
    private final SettingGroup sgModules;
    private final SettingGroup sgChat;
    private final SettingGroup sgMisc;
    private final SettingGroup sgGui;
    public final Setting<Boolean> cleanModulesUi;
    public final Setting<Boolean> customFont;
    public final Setting<FontFace> font;
    public final Setting<Double> rainbowSpeed;
    public final Setting<Boolean> titleScreenCredits;
    public final Setting<Boolean> titleScreenSplashes;
    public final Setting<Boolean> customWindowTitle;
    public final Setting<String> customWindowTitleText;
    public final Setting<SettingColor> friendColor;
    public final Setting<List<Module>> hiddenModules;
    public final Setting<Integer> moduleSearchCount;
    public final Setting<Boolean> moduleAliases;
    public final Setting<String> prefix;
    public final Setting<Boolean> chatFeedback;
    public final Setting<Boolean> deleteChatFeedback;
    public final Setting<Integer> rotationHoldTicks;
    public final Setting<Boolean> useTeamColor;
    public List<String> dontShowAgainPrompts;

    public Config() {
        super("config");
        this.settings = new Settings();
        this.sgVisual = this.settings.createGroup("Visual");
        this.sgModules = this.settings.createGroup("Modules");
        this.sgChat = this.settings.createGroup("Chat");
        this.sgMisc = this.settings.createGroup("Misc");
        this.sgGui = this.settings.createGroup("GUI");
        this.cleanModulesUi = this.sgGui.add(new BoolSetting.Builder().name("clean-modules-ui").description("Use the clean category-select screen instead of the default layout.").defaultValue(false).build());
        this.customFont = this.sgVisual.add(new BoolSetting.Builder().name("custom-font").description("Use a custom font.").defaultValue(true).build());
        SettingGroup settingGroup = this.sgVisual;
        FontFaceSetting.Builder builderDescription = new FontFaceSetting.Builder().name("font").description("Custom font to use.");
        Setting<Boolean> setting = this.customFont;
        Objects.requireNonNull(setting);
        this.font = settingGroup.add(builderDescription.visible(setting::get).onChanged(Fonts::load).build());
        this.rainbowSpeed = this.sgVisual.add(new DoubleSetting.Builder().name("rainbow-speed").description("The global rainbow speed.").defaultValue(0.5d).range(0.0d, 10.0d).sliderMax(5.0d).build());
        this.titleScreenCredits = this.sgVisual.add(new BoolSetting.Builder().name("title-screen-credits").description("Show Meteor credits on title screen").defaultValue(true).build());
        this.titleScreenSplashes = this.sgVisual.add(new BoolSetting.Builder().name("title-screen-splashes").description("Show Meteor splash texts on title screen").defaultValue(true).build());
        this.customWindowTitle = this.sgVisual.add(new BoolSetting.Builder().name("custom-window-title").description("Show custom text in the window title.").defaultValue(false).onModuleActivated(setting2 -> {
            MeteorClient.mc.method_24288();
        }).onChanged(value -> {
            MeteorClient.mc.method_24288();
        }).build());
        SettingGroup settingGroup2 = this.sgVisual;
        StringSetting.Builder builderDescription2 = new StringSetting.Builder().name("window-title-text").description("The text it displays in the window title.");
        Setting<Boolean> setting3 = this.customWindowTitle;
        Objects.requireNonNull(setting3);
        this.customWindowTitleText = settingGroup2.add(builderDescription2.visible(setting3::get).defaultValue("Minecraft {mc_version} - {meteor.name} {meteor.version}").onChanged(value2 -> {
            MeteorClient.mc.method_24288();
        }).build());
        this.friendColor = this.sgVisual.add(new ColorSetting.Builder().name("friend-color").description("The color used to show friends.").defaultValue(new SettingColor(0, 255, Opcode.GETFIELD)).build());
        this.hiddenModules = this.sgModules.add(new ModuleListSetting.Builder().name("hidden-modules").description("Prevent these modules from being rendered as options in the clickgui.").build());
        this.moduleSearchCount = this.sgModules.add(new IntSetting.Builder().name("module-search-count").description("Amount of modules and settings to be shown in the module search bar.").defaultValue(8).min(1).sliderMax(12).build());
        this.moduleAliases = this.sgModules.add(new BoolSetting.Builder().name("search-module-aliases").description("Whether or not module aliases will be used in the module search bar.").defaultValue(true).build());
        this.prefix = this.sgChat.add(new StringSetting.Builder().name("prefix").description("Prefix.").defaultValue(".").build());
        this.chatFeedback = this.sgChat.add(new BoolSetting.Builder().name("chat-feedback").description("Sends chat feedback when meteor performs certain actions.").defaultValue(true).build());
        SettingGroup settingGroup3 = this.sgChat;
        BoolSetting.Builder builderDescription3 = new BoolSetting.Builder().name("delete-chat-feedback").description("Delete previous matching chat feedback to keep chat clear.");
        Setting<Boolean> setting4 = this.chatFeedback;
        Objects.requireNonNull(setting4);
        this.deleteChatFeedback = settingGroup3.add(builderDescription3.visible(setting4::get).defaultValue(true).build());
        this.rotationHoldTicks = this.sgMisc.add(new IntSetting.Builder().name("rotation-hold").description("Hold long to hold server side rotation when not sending any packets.").defaultValue(4).build());
        this.useTeamColor = this.sgMisc.add(new BoolSetting.Builder().name("use-team-color").description("Uses player's team color for rendering things like esp and tracers.").defaultValue(true).build());
        this.dontShowAgainPrompts = new ArrayList();
    }

    public static Config get() {
        return (Config) Systems.get(Config.class);
    }

    @Override // meteordevelopment.meteorclient.systems.System, meteordevelopment.meteorclient.utils.misc.ISerializable
    public class_2487 toTag() {
        class_2487 tag = new class_2487();
        tag.method_10582("version", MeteorClient.VERSION.toString());
        tag.method_10566("settings", this.settings.toTag());
        tag.method_10566("dontShowAgainPrompts", listToTag(this.dontShowAgainPrompts));
        return tag;
    }

    @Override // meteordevelopment.meteorclient.systems.System, meteordevelopment.meteorclient.utils.misc.ISerializable
    /* JADX INFO: renamed from: fromTag */
    public Config fromTag2(class_2487 tag) {
        if (tag.method_10545("settings")) {
            this.settings.fromTag2(tag.method_68568("settings"));
        }
        if (tag.method_10545("dontShowAgainPrompts")) {
            this.dontShowAgainPrompts = listFromTag(tag, "dontShowAgainPrompts");
        }
        return this;
    }

    private class_2499 listToTag(List<String> list) {
        class_2499 nbt = new class_2499();
        for (String item : list) {
            nbt.add(class_2519.method_23256(item));
        }
        return nbt;
    }

    private List<String> listFromTag(class_2487 tag, String key) {
        List<String> list = new ArrayList<>();
        for (class_2520 item : tag.method_68569(key)) {
            list.add((String) item.method_68658().orElse(""));
        }
        return list;
    }
}
