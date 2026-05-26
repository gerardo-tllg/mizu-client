package meteordevelopment.meteorclient.systems.modules.misc;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.chars.Char2CharMap;
import it.unimi.dsi.fastutil.chars.Char2CharOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.ObjectMethods;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.events.game.SendMessageEvent;
import meteordevelopment.meteorclient.mixininterface.IChatHudLine;
import meteordevelopment.meteorclient.mixininterface.IChatHudLineVisible;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringListSetting;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.friends.Friend;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.text.MeteorClickEvent;
import meteordevelopment.meteorclient.utils.misc.text.TextVisitor;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_124;
import net.minecraft.class_1921;
import net.minecraft.class_2561;
import net.minecraft.class_2568;
import net.minecraft.class_2583;
import net.minecraft.class_2960;
import net.minecraft.class_303;
import net.minecraft.class_332;
import net.minecraft.class_5250;
import net.minecraft.class_5251;
import net.minecraft.class_640;
import net.minecraft.class_7532;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/misc/BetterChat.class */
public class BetterChat extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgFilter;
    private final SettingGroup sgLongerChat;
    private final SettingGroup sgPrefix;
    private final SettingGroup sgSuffix;
    private final SettingGroup sgNameHighlight;
    private final SettingGroup sgFriendHighlight;
    private final Setting<Boolean> annoy;
    private final Setting<Boolean> fancy;
    private final Setting<Boolean> timestamps;
    private final Setting<Boolean> playerHeads;
    private final Setting<Boolean> coordsProtection;
    private final Setting<Boolean> keepHistory;
    private final Setting<Boolean> globalRemoveBrackets;
    private final Setting<Boolean> antiSpam;
    private final Setting<Integer> antiSpamDepth;
    private final Setting<Boolean> antiClear;
    private final Setting<Boolean> filterRegex;
    private final Setting<List<String>> regexFilters;
    private final Setting<Boolean> infiniteChatBox;
    private final Setting<Boolean> longerChatHistory;
    private final Setting<Integer> longerChatLines;
    private final Setting<Boolean> prefix;
    private final Setting<Boolean> prefixRandom;
    private final Setting<String> prefixText;
    private final Setting<Boolean> prefixSmallCaps;
    private final Setting<Boolean> suffix;
    private final Setting<Boolean> suffixRandom;
    private final Setting<String> suffixText;
    private final Setting<Boolean> suffixSmallCaps;
    private final Setting<Boolean> nameHighlight;
    public final Setting<SettingColor> nameHighlightColor;
    public final Setting<Boolean> nameHighlightShadow;
    public final Setting<Boolean> nameHighlightRemoveBrackets;
    public final Setting<NameFont> nameHighlightFont;
    private final Setting<Boolean> friendHighlight;
    public final Setting<SettingColor> friendHighlightColor;
    public final Setting<Boolean> friendHighlightShadow;
    public final Setting<Boolean> friendHighlightRemoveBrackets;
    public final Setting<NameFont> friendHighlightFont;
    private final Char2CharMap SMALL_CAPS;
    private final SimpleDateFormat dateFormat;
    public final IntList lines;
    private final List<Pattern> filterRegexList;
    private static final Pattern coordRegex;
    private static final Pattern globalBracketRegex = Pattern.compile("<(?!\\d{1,2}:\\d{2}>)([^>]+)>");
    private static final Pattern antiSpamRegex = Pattern.compile(" \\(([0-9]{1,9})\\)$");
    private static final Pattern antiClearRegex = Pattern.compile("\\n(\\n|\\s)+\\n");
    private static final Pattern timestampRegex = Pattern.compile("^(<[0-9]{2}:[0-9]{2}>\\s)");
    private static final Pattern usernameRegex = Pattern.compile("^(?:<[0-9]{2}:[0-9]{2}>\\s)?<(.*?)>.*");
    private static final List<CustomHeadEntry> CUSTOM_HEAD_ENTRIES = new ArrayList();
    private static final Pattern TIMESTAMP_REGEX = Pattern.compile("^<\\d{1,2}:\\d{1,2}>");

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/misc/BetterChat$NameFont.class */
    public enum NameFont {
        DEFAULT("minecraft:default"),
        UNIFORM("minecraft:uniform"),
        ALT("minecraft:alt"),
        ILLAGERALT("minecraft:illageralt");

        public final String id;

        NameFont(String id) {
            this.id = id;
        }

        @Override // java.lang.Enum
        public String toString() {
            return name().charAt(0) + name().substring(1).toLowerCase();
        }
    }

    static {
        registerCustomHead("[Meteor]", MeteorClient.identifier("textures/icons/chat/meteor.png"));
        registerCustomHead("[Baritone]", MeteorClient.identifier("textures/icons/chat/baritone.png"));
        coordRegex = Pattern.compile("(?<x>-?\\d{3,}(?:\\.\\d*)?)(?:\\s+(?<y>-?\\d{1,3}(?:\\.\\d*)?))?\\s+(?<z>-?\\d{3,}(?:\\.\\d*)?)");
    }

    public BetterChat() {
        super(Categories.Misc, "better-chat", "Improves your chat experience in various ways.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgFilter = this.settings.createGroup("Filter");
        this.sgLongerChat = this.settings.createGroup("Longer Chat");
        this.sgPrefix = this.settings.createGroup("Prefix");
        this.sgSuffix = this.settings.createGroup("Suffix");
        this.sgNameHighlight = this.settings.createGroup("Name Highlight");
        this.sgFriendHighlight = this.settings.createGroup("Friend Highlight");
        this.annoy = this.sgGeneral.add(new BoolSetting.Builder().name("annoy").description("Makes your messages aNnOyInG.").defaultValue(false).build());
        this.fancy = this.sgGeneral.add(new BoolSetting.Builder().name("fancy-chat").description("Makes your messages ғᴀɴᴄʏ!").defaultValue(false).build());
        this.timestamps = this.sgGeneral.add(new BoolSetting.Builder().name("timestamps").description("Adds client-side time stamps to the beginning of chat messages.").defaultValue(false).build());
        this.playerHeads = this.sgGeneral.add(new BoolSetting.Builder().name("player-heads").description("Displays player heads next to their messages.").defaultValue(true).build());
        this.coordsProtection = this.sgGeneral.add(new BoolSetting.Builder().name("coords-protection").description("Prevents you from sending messages in chat that may contain coordinates.").defaultValue(true).build());
        this.keepHistory = this.sgGeneral.add(new BoolSetting.Builder().name("keep-history").description("Prevents the chat history from being cleared when disconnecting.").defaultValue(true).build());
        this.globalRemoveBrackets = this.sgGeneral.add(new BoolSetting.Builder().name("remove-brackets").description("Removes < > brackets from all player names in chat globally.").defaultValue(false).build());
        this.antiSpam = this.sgFilter.add(new BoolSetting.Builder().name("anti-spam").description("Blocks duplicate messages from filling your chat.").defaultValue(true).build());
        SettingGroup settingGroup = this.sgFilter;
        IntSetting.Builder builderSliderMin = new IntSetting.Builder().name("depth").description("How many messages to filter.").defaultValue(20).min(1).sliderMin(1);
        Setting<Boolean> setting = this.antiSpam;
        Objects.requireNonNull(setting);
        this.antiSpamDepth = settingGroup.add(builderSliderMin.visible(setting::get).build());
        this.antiClear = this.sgFilter.add(new BoolSetting.Builder().name("anti-clear").description("Prevents servers from clearing chat.").defaultValue(true).build());
        this.filterRegex = this.sgFilter.add(new BoolSetting.Builder().name("filter-regex").description("Filter out chat messages that match the regex filter.").defaultValue(false).build());
        SettingGroup settingGroup2 = this.sgFilter;
        StringListSetting.Builder builderDescription = new StringListSetting.Builder().name("regex-filter").description("Regex filter used for filtering chat messages.");
        Setting<Boolean> setting2 = this.filterRegex;
        Objects.requireNonNull(setting2);
        this.regexFilters = settingGroup2.add(builderDescription.visible(setting2::get).onChanged(strings -> {
            compileFilterRegexList();
        }).build());
        this.infiniteChatBox = this.sgLongerChat.add(new BoolSetting.Builder().name("infinite-chat-box").description("Lets you type infinitely long messages.").defaultValue(true).build());
        this.longerChatHistory = this.sgLongerChat.add(new BoolSetting.Builder().name("longer-chat-history").description("Extends chat length.").defaultValue(true).build());
        SettingGroup settingGroup3 = this.sgLongerChat;
        IntSetting.Builder builderSliderRange = new IntSetting.Builder().name("extra-lines").description("The amount of extra chat lines.").defaultValue(1000).min(0).sliderRange(0, 1000);
        Setting<Boolean> setting3 = this.longerChatHistory;
        Objects.requireNonNull(setting3);
        this.longerChatLines = settingGroup3.add(builderSliderRange.visible(setting3::get).build());
        this.prefix = this.sgPrefix.add(new BoolSetting.Builder().name("prefix").description("Adds a prefix to your chat messages.").defaultValue(false).build());
        this.prefixRandom = this.sgPrefix.add(new BoolSetting.Builder().name("random").description("Uses a random number as your prefix.").defaultValue(false).build());
        this.prefixText = this.sgPrefix.add(new StringSetting.Builder().name("text").description("The text to add as your prefix.").defaultValue("> ").visible(() -> {
            return !this.prefixRandom.get().booleanValue();
        }).build());
        this.prefixSmallCaps = this.sgPrefix.add(new BoolSetting.Builder().name("small-caps").description("Uses small caps in the prefix.").defaultValue(false).visible(() -> {
            return !this.prefixRandom.get().booleanValue();
        }).build());
        this.suffix = this.sgSuffix.add(new BoolSetting.Builder().name("suffix").description("Adds a suffix to your chat messages.").defaultValue(false).build());
        this.suffixRandom = this.sgSuffix.add(new BoolSetting.Builder().name("random").description("Uses a random number as your suffix.").defaultValue(false).build());
        this.suffixText = this.sgSuffix.add(new StringSetting.Builder().name("text").description("The text to add as your suffix.").defaultValue(" | meteor on crack!").visible(() -> {
            return !this.suffixRandom.get().booleanValue();
        }).build());
        this.suffixSmallCaps = this.sgSuffix.add(new BoolSetting.Builder().name("small-caps").description("Uses small caps in the suffix.").defaultValue(true).visible(() -> {
            return !this.suffixRandom.get().booleanValue();
        }).build());
        this.nameHighlight = this.sgNameHighlight.add(new BoolSetting.Builder().name("name-highlight").description("Highlights your username when it appears in chat.").defaultValue(true).build());
        this.nameHighlightColor = this.sgNameHighlight.add(new ColorSetting.Builder().name("highlight-color").description("Color used to highlight your name in chat.").defaultValue(new SettingColor(255, 255, 0, 255)).build());
        SettingGroup settingGroup4 = this.sgNameHighlight;
        BoolSetting.Builder builderDefaultValue = new BoolSetting.Builder().name("name-shadow").description("Renders a drop shadow behind your highlighted name.").defaultValue(true);
        Setting<Boolean> setting4 = this.nameHighlight;
        Objects.requireNonNull(setting4);
        this.nameHighlightShadow = settingGroup4.add(builderDefaultValue.visible(setting4::get).build());
        SettingGroup settingGroup5 = this.sgNameHighlight;
        BoolSetting.Builder builderDefaultValue2 = new BoolSetting.Builder().name("remove-brackets").description("Removes the < > brackets surrounding your name specifically. Overrides the global setting.").defaultValue(false);
        Setting<Boolean> setting5 = this.nameHighlight;
        Objects.requireNonNull(setting5);
        this.nameHighlightRemoveBrackets = settingGroup5.add(builderDefaultValue2.visible(setting5::get).build());
        SettingGroup settingGroup6 = this.sgNameHighlight;
        EnumSetting.Builder builderDefaultValue3 = new EnumSetting.Builder().name("name-font").description("Font used to render your highlighted name. Alt = enchanting table font.").defaultValue(NameFont.DEFAULT);
        Setting<Boolean> setting6 = this.nameHighlight;
        Objects.requireNonNull(setting6);
        this.nameHighlightFont = settingGroup6.add(builderDefaultValue3.visible(setting6::get).build());
        this.friendHighlight = this.sgFriendHighlight.add(new BoolSetting.Builder().name("friend-highlight").description("Highlights friends names when they appear in chat.").defaultValue(true).build());
        this.friendHighlightColor = this.sgFriendHighlight.add(new ColorSetting.Builder().name("highlight-color").description("Color used to highlight friend names in chat.").defaultValue(new SettingColor(0, 255, 0, 255)).build());
        SettingGroup settingGroup7 = this.sgFriendHighlight;
        BoolSetting.Builder builderDefaultValue4 = new BoolSetting.Builder().name("friend-shadow").description("Renders a drop shadow behind highlighted friend names.").defaultValue(true);
        Setting<Boolean> setting7 = this.friendHighlight;
        Objects.requireNonNull(setting7);
        this.friendHighlightShadow = settingGroup7.add(builderDefaultValue4.visible(setting7::get).build());
        SettingGroup settingGroup8 = this.sgFriendHighlight;
        BoolSetting.Builder builderDefaultValue5 = new BoolSetting.Builder().name("remove-brackets").description("Removes the < > brackets surrounding friend names specifically. Overrides the global setting.").defaultValue(false);
        Setting<Boolean> setting8 = this.friendHighlight;
        Objects.requireNonNull(setting8);
        this.friendHighlightRemoveBrackets = settingGroup8.add(builderDefaultValue5.visible(setting8::get).build());
        SettingGroup settingGroup9 = this.sgFriendHighlight;
        EnumSetting.Builder builderDefaultValue6 = new EnumSetting.Builder().name("friend-font").description("Font used to render highlighted friend names. Alt = enchanting table font.").defaultValue(NameFont.DEFAULT);
        Setting<Boolean> setting9 = this.friendHighlight;
        Objects.requireNonNull(setting9);
        this.friendHighlightFont = settingGroup9.add(builderDefaultValue6.visible(setting9::get).build());
        this.SMALL_CAPS = new Char2CharOpenHashMap();
        this.dateFormat = new SimpleDateFormat("HH:mm");
        this.lines = new IntArrayList();
        this.filterRegexList = new ArrayList();
        String[] a = "abcdefghijklmnopqrstuvwxyz".split("");
        String[] b = "ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴩqʀꜱᴛᴜᴠᴡxyᴢ".split("");
        for (int i = 0; i < a.length; i++) {
            this.SMALL_CAPS.put(a[i].charAt(0), b[i].charAt(0));
        }
        compileFilterRegexList();
    }

    @EventHandler
    private void onMessageReceive(ReceiveMessageEvent event) {
        class_2561 antiSpammed;
        class_2561 message = event.getMessage();
        if (this.filterRegex.get().booleanValue()) {
            String messageString = message.getString();
            for (Pattern pattern : this.filterRegexList) {
                if (pattern.matcher(messageString).find()) {
                    event.cancel();
                    return;
                }
            }
        }
        if (this.antiClear.get().booleanValue()) {
            String messageString2 = message.getString();
            if (antiClearRegex.matcher(messageString2).find()) {
                class_2561 class_2561VarMethod_43473 = class_2561.method_43473();
                TextVisitor.visit(message, (text, style, string) -> {
                    Matcher antiClearMatcher = antiClearRegex.matcher(string);
                    if (antiClearMatcher.find()) {
                        class_2561VarMethod_43473.method_10852(class_2561.method_43470(antiClearMatcher.replaceAll("\n\n")).method_10862(style));
                    } else {
                        class_2561VarMethod_43473.method_10852(text.method_27662().method_10862(style));
                    }
                    return Optional.empty();
                }, class_2583.field_24360);
                message = class_2561VarMethod_43473;
            }
        }
        if (this.antiSpam.get().booleanValue() && (antiSpammed = appendAntiSpam(message)) != null) {
            message = antiSpammed;
        }
        if (this.timestamps.get().booleanValue()) {
            message = class_2561.method_43473().method_10852(class_2561.method_43470("<" + this.dateFormat.format(new Date()) + "> ").method_27692(class_124.field_1080)).method_10852(message);
        }
        if (this.globalRemoveBrackets.get().booleanValue()) {
            message = applyGlobalBracketRemoval(message);
        }
        if (this.nameHighlight.get().booleanValue() && this.mc.field_1724 != null) {
            String playerName = this.mc.field_1724.method_5477().getString();
            String msgStr = message.getString();
            if (msgStr.contains("<" + playerName + ">") || msgStr.contains(playerName)) {
                message = highlightName(message, playerName);
            }
        }
        if (this.friendHighlight.get().booleanValue()) {
            message = highlightListNames(message);
        }
        event.setMessage(message);
    }

    @EventHandler
    private void onMessageSend(SendMessageEvent event) {
        String message = event.message;
        if (this.annoy.get().booleanValue()) {
            message = applyAnnoy(message);
        }
        if (this.fancy.get().booleanValue()) {
            message = applyFancy(message);
        }
        String message2 = getPrefix() + message + getSuffix();
        if (this.coordsProtection.get().booleanValue() && containsCoordinates(message2)) {
            class_5250 warningMessage = class_2561.method_43470("It looks like there are coordinates in your message! ");
            warningMessage.method_10852(getSendButton(message2));
            ChatUtils.sendMsg(warningMessage);
            event.cancel();
            return;
        }
        event.message = message2;
    }

    private class_5250 applyGlobalBracketRemoval(class_2561 message) {
        List<StyledSegment> segments = new ArrayList<>();
        TextVisitor.visit(message, (text, style, string) -> {
            segments.add(new StyledSegment(style, string));
            return Optional.empty();
        }, class_2583.field_24360);
        StringBuilder full = new StringBuilder();
        int[] segmentIndex = new int[segments.stream().mapToInt(s -> {
            return s.text().length();
        }).sum()];
        int charPos = 0;
        for (int si = 0; si < segments.size(); si++) {
            String seg = segments.get(si).text();
            for (int ci = 0; ci < seg.length(); ci++) {
                int i = charPos;
                charPos++;
                segmentIndex[i] = si;
            }
            full.append(seg);
        }
        String fullStr = full.toString();
        Matcher m = globalBracketRegex.matcher(fullStr);
        List<C1Replacement> replacements = new ArrayList<>();
        while (m.find()) {
            replacements.add(new C1Replacement(m.start(), m.end(), m.group(1)));
        }
        if (replacements.isEmpty()) {
            return class_2561.method_43470("").method_10852(message);
        }
        class_5250 result = class_2561.method_43470("");
        int pos = 0;
        List<StyledSegment> outputSpans = new ArrayList<>();
        int repIdx = 0;
        while (pos < fullStr.length()) {
            if (repIdx < replacements.size() && pos == replacements.get(repIdx).start()) {
                C1Replacement rep = replacements.get(repIdx);
                class_2583 repStyle = segments.get(segmentIndex[Math.min(pos, segmentIndex.length - 1)]).style();
                outputSpans.add(new StyledSegment(repStyle, rep.text()));
                pos = rep.end();
                repIdx++;
            } else {
                int segIdx = segmentIndex[pos];
                class_2583 style2 = segments.get(segIdx).style();
                int endPos = pos;
                int nextRepStart = repIdx < replacements.size() ? replacements.get(repIdx).start() : fullStr.length();
                while (endPos < fullStr.length() && segmentIndex[endPos] == segIdx && endPos < nextRepStart) {
                    endPos++;
                }
                outputSpans.add(new StyledSegment(style2, fullStr.substring(pos, endPos)));
                pos = endPos;
            }
        }
        for (StyledSegment span : outputSpans) {
            if (!span.text().isEmpty()) {
                result.method_10852(class_2561.method_43470(span.text()).method_10862(span.style()));
            }
        }
        return result;
    }

    /* JADX INFO: renamed from: meteordevelopment.meteorclient.systems.modules.misc.BetterChat$1Replacement, reason: invalid class name */
    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/misc/BetterChat$1Replacement.class */
    record C1Replacement(int start, int end, String text) {
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/misc/BetterChat$StyledSegment.class */
    private record StyledSegment(class_2583 style, String text) {
    }

    private class_5250 highlightName(class_2561 message, String name) {
        SettingColor col = this.nameHighlightColor.get();
        int rgb = (col.r << 16) | (col.g << 8) | col.b;
        class_2583 style = class_2583.field_24360.method_27703(class_5251.method_27717(rgb)).method_10982(true).method_27704(class_2960.method_60654(this.nameHighlightFont.get().id));
        if (!this.nameHighlightShadow.get().booleanValue()) {
            style = style.method_65302(0);
        }
        boolean strippedAlready = this.globalRemoveBrackets.get().booleanValue();
        boolean stripHere = !strippedAlready && this.nameHighlightRemoveBrackets.get().booleanValue();
        if (stripHere) {
            return applyHighlightStripBrackets(message, name, style);
        }
        return applyHighlight(message, name, style);
    }

    public class_5250 highlightListNames(class_2561 message) {
        class_5250 class_5250VarApplyHighlight;
        String msgStr = message.getString();
        class_5250 result = class_2561.method_43470("").method_10852(message);
        for (Friend friend : Friends.get()) {
            String name = friend.name;
            if (name != null && !name.isBlank() && (msgStr.contains("<" + name + ">") || msgStr.contains(name))) {
                SettingColor col = this.friendHighlightColor.get();
                int rgb = (col.r << 16) | (col.g << 8) | col.b;
                class_2583 style = class_2583.field_24360.method_27703(class_5251.method_27717(rgb)).method_10982(true).method_27704(class_2960.method_60654(this.friendHighlightFont.get().id));
                if (!this.friendHighlightShadow.get().booleanValue()) {
                    style = style.method_65302(0);
                }
                boolean strippedAlready = this.globalRemoveBrackets.get().booleanValue();
                boolean stripHere = !strippedAlready && this.friendHighlightRemoveBrackets.get().booleanValue();
                if (stripHere) {
                    class_5250VarApplyHighlight = applyHighlightStripBrackets(result, name, style);
                } else {
                    class_5250VarApplyHighlight = applyHighlight(result, name, style);
                }
                result = class_5250VarApplyHighlight;
            }
        }
        return result;
    }

    public class_5250 applyHighlight(class_2561 message, String name, class_2583 nameStyle) {
        class_5250 result = class_2561.method_43470("");
        TextVisitor.visit(message, (text, style, string) -> {
            int start;
            if (string.contains(name)) {
                int length = 0;
                while (true) {
                    start = length;
                    int idx = string.indexOf(name, start);
                    if (idx == -1) {
                        break;
                    }
                    if (idx > start) {
                        result.method_10852(class_2561.method_43470(string.substring(start, idx)).method_10862(style));
                    }
                    result.method_10852(class_2561.method_43470(name).method_10862(nameStyle));
                    length = idx + name.length();
                }
                if (start < string.length()) {
                    result.method_10852(class_2561.method_43470(string.substring(start)).method_10862(style));
                }
            } else {
                result.method_10852(text.method_27662().method_10862(style));
            }
            return Optional.empty();
        }, class_2583.field_24360);
        return result;
    }

    private class_5250 applyHighlightStripBrackets(class_2561 message, String name, class_2583 nameStyle) {
        String bracketedName = "<" + name + ">";
        class_5250 result = class_2561.method_43470("");
        TextVisitor.visit(message, (text, style, string) -> {
            int start;
            int start2;
            if (string.contains(bracketedName)) {
                int length = 0;
                while (true) {
                    start2 = length;
                    int idx = string.indexOf(bracketedName, start2);
                    if (idx == -1) {
                        break;
                    }
                    if (idx > start2) {
                        result.method_10852(class_2561.method_43470(string.substring(start2, idx)).method_10862(style));
                    }
                    result.method_10852(class_2561.method_43470(" ").method_10862(style));
                    result.method_10852(class_2561.method_43470(name).method_10862(nameStyle));
                    result.method_10852(class_2561.method_43470(" ").method_10862(style));
                    length = idx + bracketedName.length();
                }
                if (start2 < string.length()) {
                    result.method_10852(class_2561.method_43470(string.substring(start2)).method_10862(style));
                }
            } else if (string.contains(name)) {
                int length2 = 0;
                while (true) {
                    start = length2;
                    int idx2 = string.indexOf(name, start);
                    if (idx2 == -1) {
                        break;
                    }
                    if (idx2 > start) {
                        result.method_10852(class_2561.method_43470(string.substring(start, idx2)).method_10862(style));
                    }
                    result.method_10852(class_2561.method_43470(name).method_10862(nameStyle));
                    length2 = idx2 + name.length();
                }
                if (start < string.length()) {
                    result.method_10852(class_2561.method_43470(string.substring(start)).method_10862(style));
                }
            } else {
                result.method_10852(text.method_27662().method_10862(style));
            }
            return Optional.empty();
        }, class_2583.field_24360);
        return result;
    }

    private class_2561 appendAntiSpam(class_2561 text) {
        String textString = text.getString();
        class_5250 class_5250VarMethod_10852 = null;
        int messageIndex = -1;
        List<class_303> messages = this.mc.field_1705.method_1743().getMessages();
        if (messages.isEmpty()) {
            return null;
        }
        int i = 0;
        while (true) {
            if (i >= Math.min(this.antiSpamDepth.get().intValue(), messages.size())) {
                break;
            }
            String stringToCheck = messages.get(i).comp_893().getString();
            Matcher timestampMatcher = timestampRegex.matcher(stringToCheck);
            if (timestampMatcher.find()) {
                stringToCheck = stringToCheck.substring(8);
            }
            if (textString.equals(stringToCheck)) {
                messageIndex = i;
                class_5250VarMethod_10852 = text.method_27661().method_10852(class_2561.method_43470(" (2)").method_27692(class_124.field_1080));
                break;
            }
            Matcher matcher = antiSpamRegex.matcher(stringToCheck);
            if (matcher.find()) {
                String group = matcher.group(matcher.groupCount());
                int number = Integer.parseInt(group);
                if (stringToCheck.substring(0, matcher.start()).equals(textString)) {
                    messageIndex = i;
                    class_5250VarMethod_10852 = text.method_27661().method_10852(class_2561.method_43470(" (" + (number + 1) + ")").method_27692(class_124.field_1080));
                    break;
                }
            }
            i++;
        }
        if (class_5250VarMethod_10852 != null) {
            List<class_303.class_7590> visible = this.mc.field_1705.method_1743().getVisibleMessages();
            int start = -1;
            for (int i2 = 0; i2 < messageIndex; i2++) {
                start += this.lines.getInt(i2);
            }
            for (int i3 = this.lines.getInt(messageIndex); i3 > 0; i3--) {
                visible.remove(start + 1);
            }
            messages.remove(messageIndex);
            this.lines.removeInt(messageIndex);
        }
        return class_5250VarMethod_10852;
    }

    public void removeLine(int index) {
        if (index >= this.lines.size()) {
            if (this.antiSpam.get().booleanValue()) {
                error("Issue detected with the anti-spam system! Likely a compatibility issue with another mod. Disabling anti-spam to protect chat integrity.", new Object[0]);
                this.antiSpam.set(false);
                return;
            }
            return;
        }
        this.lines.removeInt(index);
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/misc/BetterChat$CustomHeadEntry.class */
    private record CustomHeadEntry(String prefix, class_2960 texture) {
    }


    public static void registerCustomHead(String prefix, class_2960 texture) {
        CUSTOM_HEAD_ENTRIES.add(new CustomHeadEntry(prefix, texture));
    }

    public int modifyChatWidth(int width) {
        return (isActive() && this.playerHeads.get().booleanValue()) ? width + 10 : width;
    }

    public void drawPlayerHead(class_332 context, class_303.class_7590 line, int y, int color) {
        if (isActive() && this.playerHeads.get().booleanValue()) {
            if (((IChatHudLineVisible) line).meteor$isStartOfEntry()) {
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, Color.toRGBAA(color) / 255.0f);
                drawTexture(context, (IChatHudLine) line, y);
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            }
            context.method_51448().method_46416(10.0f, 0.0f, 0.0f);
        }
    }

    private void drawTexture(class_332 context, IChatHudLine line, int y) {
        class_640 entry;
        String text = line.meteor$getText().trim();
        int startOffset = 0;
        try {
            Matcher m = TIMESTAMP_REGEX.matcher(text);
            if (m.find()) {
                startOffset = m.end() + 1;
            }
        } catch (IllegalStateException e) {
        }
        for (CustomHeadEntry entry2 : CUSTOM_HEAD_ENTRIES) {
            if (text.startsWith(entry2.prefix(), startOffset)) {
                context.method_25302(class_1921::method_62277, entry2.texture(), 0, y, 0.0f, 0.0f, 8, 8, 64, 64, 64, 64);
                return;
            }
        }
        GameProfile sender = getSender(line, text);
        if (sender == null || (entry = this.mc.method_1562().method_2871(sender.getId())) == null) {
            return;
        }
        class_7532.method_52722(context, entry.method_52810(), 0, y, 8);
    }

    private GameProfile getSender(IChatHudLine line, String text) {
        GameProfile sender = line.meteor$getSender();
        if (sender == null) {
            Matcher usernameMatcher = usernameRegex.matcher(text);
            if (usernameMatcher.matches()) {
                String username = usernameMatcher.group(1);
                class_640 entry = this.mc.method_1562().method_2874(username);
                if (entry != null) {
                    sender = entry.method_2966();
                }
            }
        }
        return sender;
    }

    private String applyAnnoy(String message) {
        StringBuilder sb = new StringBuilder(message.length());
        boolean upperCase = true;
        for (int cp : message.codePoints().toArray()) {
            if (upperCase) {
                sb.appendCodePoint(Character.toUpperCase(cp));
            } else {
                sb.appendCodePoint(Character.toLowerCase(cp));
            }
            upperCase = !upperCase;
        }
        return sb.toString();
    }

    private String applyFancy(String message) {
        StringBuilder sb = new StringBuilder();
        for (char ch : message.toCharArray()) {
            sb.append(this.SMALL_CAPS.getOrDefault(ch, ch));
        }
        return sb.toString();
    }

    private void compileFilterRegexList() {
        this.filterRegexList.clear();
        for (int i = 0; i < this.regexFilters.get().size(); i++) {
            try {
                this.filterRegexList.add(Pattern.compile(this.regexFilters.get().get(i)));
            } catch (PatternSyntaxException e) {
                String removed = this.regexFilters.get().remove(i);
                error("Removing Invalid regex: %s", removed);
            }
        }
    }

    private String getPrefix() {
        return this.prefix.get().booleanValue() ? getAffix(this.prefixText.get(), this.prefixSmallCaps.get().booleanValue(), this.prefixRandom.get().booleanValue()) : "";
    }

    private String getSuffix() {
        return this.suffix.get().booleanValue() ? getAffix(this.suffixText.get(), this.suffixSmallCaps.get().booleanValue(), this.suffixRandom.get().booleanValue()) : "";
    }

    private String getAffix(String text, boolean smallcaps, boolean random) {
        return random ? String.format("(%03d) ", Integer.valueOf(Utils.random(0, 1000))) : smallcaps ? applyFancy(text) : text;
    }

    private boolean containsCoordinates(String message) {
        return coordRegex.matcher(message).find();
    }

    private class_5250 getSendButton(String message) {
        class_5250 sendButton = class_2561.method_43470("[SEND ANYWAY]");
        class_5250 hintBaseText = class_2561.method_43470("");
        class_5250 hintMsg = class_2561.method_43470("Send your message to the global chat even if there are coordinates:");
        hintMsg.method_10862(hintBaseText.method_10866().method_27706(class_124.field_1080));
        hintBaseText.method_10852(hintMsg);
        hintBaseText.method_10852(class_2561.method_43470("\n" + message));
        sendButton.method_10862(sendButton.method_10866().method_27706(class_124.field_1079).method_10958(new MeteorClickEvent(Commands.get("say").toString(message))).method_10949(new class_2568.class_10613(hintBaseText)));
        return sendButton;
    }

    public boolean isInfiniteChatBox() {
        return isActive() && this.infiniteChatBox.get().booleanValue();
    }

    public boolean isLongerChat() {
        return isActive() && this.longerChatHistory.get().booleanValue();
    }

    public boolean keepHistory() {
        return isActive() && this.keepHistory.get().booleanValue();
    }

    public int getExtraChatLines() {
        return this.longerChatLines.get().intValue();
    }
}
