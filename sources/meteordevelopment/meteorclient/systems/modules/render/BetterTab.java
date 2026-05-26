package meteordevelopment.meteorclient.systems.modules.render;

import java.util.Objects;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.friends.Friend;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.class_124;
import net.minecraft.class_1934;
import net.minecraft.class_2561;
import net.minecraft.class_5250;
import net.minecraft.class_5251;
import net.minecraft.class_640;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/BetterTab.class */
public class BetterTab extends Module {
    private final SettingGroup sgGeneral;
    public final Setting<Integer> tabSize;
    public final Setting<Integer> tabHeight;
    private final Setting<Boolean> self;
    private final Setting<SettingColor> selfColor;
    private final Setting<Boolean> friends;
    private final Setting<Boolean> onlyFriendsAndEnemeies;
    public final Setting<Boolean> accurateLatency;
    private final Setting<Boolean> gamemode;

    public BetterTab() {
        super(Categories.Render, "better-tab", "Various improvements to the tab list.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.tabSize = this.sgGeneral.add(new IntSetting.Builder().name("tablist-size").description("How many players in total to display in the tablist.").defaultValue(100).min(1).sliderRange(1, 1000).build());
        this.tabHeight = this.sgGeneral.add(new IntSetting.Builder().name("column-height").description("How many players to display in each column.").defaultValue(20).min(1).sliderRange(1, 1000).build());
        this.self = this.sgGeneral.add(new BoolSetting.Builder().name("highlight-self").description("Highlights yourself in the tablist.").defaultValue(true).build());
        SettingGroup settingGroup = this.sgGeneral;
        ColorSetting.Builder builderDefaultValue = new ColorSetting.Builder().name("self-color").description("The color to highlight your name with.").defaultValue(new SettingColor(250, Opcode.IXOR, 30));
        Setting<Boolean> setting = this.self;
        Objects.requireNonNull(setting);
        this.selfColor = settingGroup.add(builderDefaultValue.visible(setting::get).build());
        this.friends = this.sgGeneral.add(new BoolSetting.Builder().name("highlight-friends").description("Highlights friends in the tablist.").defaultValue(true).build());
        this.onlyFriendsAndEnemeies = this.sgGeneral.add(new BoolSetting.Builder().name("only-friends-and-enemies").description("Only shows friends and enemies in tab list.").defaultValue(true).build());
        this.accurateLatency = this.sgGeneral.add(new BoolSetting.Builder().name("accurate-latency").description("Shows latency as a number in the tablist.").defaultValue(true).build());
        this.gamemode = this.sgGeneral.add(new BoolSetting.Builder().name("gamemode").description("Display gamemode next to the nick.").defaultValue(false).build());
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: java.lang.MatchException */
    public class_2561 getPlayerName(class_640 playerListEntry) throws MatchException {
        String str;
        Color color = null;
        class_5250 class_5250VarMethod_2971 = playerListEntry.method_2971();
        if (class_5250VarMethod_2971 == null) {
            class_5250VarMethod_2971 = class_2561.method_43470(playerListEntry.method_2966().getName());
        }
        if (playerListEntry.method_2966().getId().toString().equals(this.mc.field_1724.method_7334().getId().toString()) && this.self.get().booleanValue()) {
            color = this.selfColor.get();
        } else if (this.friends.get().booleanValue() && Friends.get().isFriend(playerListEntry)) {
            Friend friend = Friends.get().get(playerListEntry);
            if (friend != null) {
                color = Config.get().friendColor.get();
            }
        }
        if (color != null) {
            String nameString = class_5250VarMethod_2971.getString();
            for (class_124 format : class_124.values()) {
                if (format.method_543()) {
                    nameString = nameString.replace(format.toString(), "");
                }
            }
            class_5250VarMethod_2971 = class_2561.method_43470(nameString).method_10862(class_5250VarMethod_2971.method_10866().method_27703(class_5251.method_27717(color.getPacked())));
        }
        if (this.gamemode.get().booleanValue()) {
            class_1934 gm = playerListEntry.method_2958();
            String gmText = "?";
            if (gm != null) {
                switch (AnonymousClass1.$SwitchMap$net$minecraft$world$GameMode[gm.ordinal()]) {
                    case 1:
                        str = "Sp";
                        break;
                    case 2:
                        str = "S";
                        break;
                    case 3:
                        str = "C";
                        break;
                    case 4:
                        str = "A";
                        break;
                    default:
                        throw new MatchException((String) null, (Throwable) null);
                }
                gmText = str;
            }
            class_5250 text = class_2561.method_43470("");
            text.method_10852(class_5250VarMethod_2971);
            text.method_27693(" [" + gmText + "]");
            class_5250VarMethod_2971 = text;
        }
        return class_5250VarMethod_2971;
    }

    /* JADX INFO: renamed from: meteordevelopment.meteorclient.systems.modules.render.BetterTab$1, reason: invalid class name */
    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/BetterTab$1.class */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$net$minecraft$world$GameMode = new int[class_1934.values().length];

        static {
            try {
                $SwitchMap$net$minecraft$world$GameMode[class_1934.field_9219.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$net$minecraft$world$GameMode[class_1934.field_9215.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$net$minecraft$world$GameMode[class_1934.field_9220.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$net$minecraft$world$GameMode[class_1934.field_9216.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    public boolean shouldShowPlayer(class_640 playerListEntry) {
        if (!isActive() || !this.onlyFriendsAndEnemeies.get().booleanValue() || Friends.get().isFriend(playerListEntry)) {
            return true;
        }
        return false;
    }
}
