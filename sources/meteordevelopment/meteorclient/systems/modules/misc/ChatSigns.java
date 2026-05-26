package meteordevelopment.meteorclient.systems.modules.misc;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.ObjectMethods;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javassist.bytecode.Opcode;
import javassist.compiler.TokenId;
import javax.annotation.Nullable;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.ChunkDataEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.DisconnectS2CPacketAccessor;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.GenericSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.render.blockesp.ESPBlockData;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.stardust.LogUtil;
import meteordevelopment.meteorclient.utils.stardust.MsgUtil;
import meteordevelopment.meteorclient.utils.stardust.StardustUtil;
import meteordevelopment.orbit.EventHandler;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_1923;
import net.minecraft.class_1937;
import net.minecraft.class_1959;
import net.minecraft.class_1972;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_2487;
import net.minecraft.class_2508;
import net.minecraft.class_2551;
import net.minecraft.class_2558;
import net.minecraft.class_2561;
import net.minecraft.class_2568;
import net.minecraft.class_2583;
import net.minecraft.class_2586;
import net.minecraft.class_2625;
import net.minecraft.class_265;
import net.minecraft.class_2661;
import net.minecraft.class_2680;
import net.minecraft.class_2724;
import net.minecraft.class_2818;
import net.minecraft.class_310;
import net.minecraft.class_3965;
import net.minecraft.class_4719;
import net.minecraft.class_5250;
import net.minecraft.class_5321;
import net.minecraft.class_746;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/misc/ChatSigns.class */
public class ChatSigns extends Module {
    private final String BLACKLIST_FILE = "meteor-client/chatsigns-blacklist.txt";
    private final SettingGroup modesGroup;
    private final SettingGroup formatGroup;
    private final SettingGroup oldSignGroup;
    private final SettingGroup blacklistGroup;
    private final SettingGroup signBoardGroup;
    private final Setting<ChatMode> chatMode;
    private final Setting<RepeatMode> repeatMode;
    private final Setting<Integer> repeatSeconds;
    private final Setting<Integer> chatSpeed;
    private final Setting<Boolean> showOldSigns;
    private final Setting<Boolean> onlyOldSigns;
    private final Setting<Boolean> ignoreNether;
    private final Setting<Boolean> ignoreDuplicates;
    private final Setting<StardustUtil.TextColor> signColor;
    private final Setting<StardustUtil.TextFormat> textFormat;
    private final Setting<StardustUtil.TextColor> oldSignColor;
    private final Setting<StardustUtil.TextFormat> oldSignFormat;
    private final Setting<Boolean> chatFormat;
    private final Setting<Boolean> showCoords;
    private final Setting<Boolean> renderOldSigns;
    private final Setting<ESPBlockData> espSettings;
    private final Setting<ESPBlockData> clickESPSettings;
    private final Setting<Integer> clickESPTimeout;
    private final Setting<Boolean> signBoardWaypoints;
    private final Setting<Boolean> temporaryWaypoints;
    private final Setting<Boolean> waypointsIgnoreEmpty;
    private final Setting<Boolean> waypointsIgnoreBlacklist;
    private final Setting<Integer> signBoardWaypointsAmount;
    private final Setting<Boolean> signBoardAutoLog;
    private final Setting<Boolean> forceKick;
    private final Setting<Integer> signBoardAutoLogAmount;
    private final Setting<Boolean> signBlacklist;
    private final Setting<Boolean> caseSensitive;
    private final Setting<Boolean> openBlacklistFile;
    private int timer;
    private int chatTimer;
    private int clusterAmount;
    private int fullClusterAmount;
    private int emptyClusterAmount;

    @Nullable
    private class_2561 disconnectReason;

    @Nullable
    private class_2338 lastFocusedSign;

    @Nullable
    private class_2338 lastFullClusterPos;

    @Nullable
    private class_2338 lastEmptyClusterPos;
    private final HashSet<class_2338> posSet;
    private final HashSet<class_2338> oldSet;
    private final ArrayList<String> blacklisted;
    private final ArrayDeque<ChatSignsJob> jobQueue;
    private final HashMap<class_2338, Instant> cooldowns;
    private final HashMap<String, Integer> signMessages;
    private final HashMap<class_1923, Boolean> chunkCache;
    private final HashMap<class_2338, Long> signsToHighlight;
    private final Pattern fullYearsPattern;
    private final Pattern fullDatesPattern;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/misc/ChatSigns$ChatMode.class */
    public enum ChatMode {
        ESP,
        Targeted,
        Both
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/misc/ChatSigns$RepeatMode.class */
    public enum RepeatMode {
        Cooldown,
        Focus
    }

    public ChatSigns() {
        super(Categories.Misc, "ChatSigns", "Read nearby signs in your chat.");
        this.BLACKLIST_FILE = "meteor-client/chatsigns-blacklist.txt";
        this.modesGroup = this.settings.createGroup("Modes", true);
        this.formatGroup = this.settings.createGroup("Formatting", true);
        this.oldSignGroup = this.settings.createGroup("OldSigns Settings", true);
        this.blacklistGroup = this.settings.createGroup("SignText Blacklist", true);
        this.signBoardGroup = this.settings.createGroup("Sign Cluster Settings", true);
        this.chatMode = this.modesGroup.add(new EnumSetting.Builder().name("chat-mode").description("ESP = nearby only, Targeted = looking at only").defaultValue(ChatMode.Both).build());
        this.repeatMode = this.modesGroup.add(new EnumSetting.Builder().name("repeat-mode").description("How to handle repeating signs you're actively looking at.").defaultValue(RepeatMode.Cooldown).visible(() -> {
            return this.chatMode.get() != ChatMode.ESP;
        }).build());
        this.repeatSeconds = this.modesGroup.add(new IntSetting.Builder().name("repeat-cooldown").description("Value in seconds to wait before repeating looked-at signs.").visible(() -> {
            return this.repeatMode.get() == RepeatMode.Cooldown && this.repeatMode.isVisible();
        }).range(1, 3600).sliderRange(1, Opcode.ISHL).defaultValue(10).build());
        this.chatSpeed = this.modesGroup.add(new IntSetting.Builder().name("chat-speed").description("How many ticks to wait before printing the next encountered sign into chat.").range(0, TokenId.BadToken).sliderRange(0, 100).defaultValue(0).build());
        this.showOldSigns = this.oldSignGroup.add(new BoolSetting.Builder().name("show-old-signs*").description("*will show signs placed before 1.8, AND after 1.19. Use your best judgment to determine what's legit.").defaultValue(false).build());
        SettingGroup settingGroup = this.oldSignGroup;
        BoolSetting.Builder builderDefaultValue = new BoolSetting.Builder().name("only-show-old-signs").description("Only display text from signs that are either really old, or really new.").defaultValue(false);
        Setting<Boolean> setting = this.showOldSigns;
        Objects.requireNonNull(setting);
        this.onlyOldSigns = settingGroup.add(builderDefaultValue.visible(setting::get).build());
        SettingGroup settingGroup2 = this.oldSignGroup;
        BoolSetting.Builder builderDescription = new BoolSetting.Builder().name("ignore-nether").description("Ignore potentially-old signs in the nether (near highways they're all certainly new.)");
        Setting<Boolean> setting2 = this.showOldSigns;
        Objects.requireNonNull(setting2);
        this.ignoreNether = settingGroup2.add(builderDescription.visible(setting2::get).defaultValue(true).build());
        this.ignoreDuplicates = this.modesGroup.add(new BoolSetting.Builder().name("ignore-duplicates").description("Ignore duplicate signs instead of displaying them with a counter.").defaultValue(false).build());
        this.signColor = this.formatGroup.add(new EnumSetting.Builder().name("sign-color").description("The color of displayed sign text.").defaultValue(StardustUtil.TextColor.Light_Gray).build());
        this.textFormat = this.formatGroup.add(new EnumSetting.Builder().name("text-formatting").description("Apply formatting to displayed sign text.").defaultValue(StardustUtil.TextFormat.Italic).build());
        SettingGroup settingGroup3 = this.oldSignGroup;
        EnumSetting.Builder builderDefaultValue2 = new EnumSetting.Builder().name("old-sign-color").description("Text color for signs that might be old.").defaultValue(StardustUtil.TextColor.Yellow);
        Setting<Boolean> setting3 = this.showOldSigns;
        Objects.requireNonNull(setting3);
        this.oldSignColor = settingGroup3.add(builderDefaultValue2.visible(setting3::get).build());
        SettingGroup settingGroup4 = this.oldSignGroup;
        EnumSetting.Builder builderDefaultValue3 = new EnumSetting.Builder().name("old-text-format").description("Apply formatting to text displayed from (maybe) old signs.").defaultValue(StardustUtil.TextFormat.Italic);
        Setting<Boolean> setting4 = this.showOldSigns;
        Objects.requireNonNull(setting4);
        this.oldSignFormat = settingGroup4.add(builderDefaultValue3.visible(setting4::get).build());
        this.chatFormat = this.formatGroup.add(new BoolSetting.Builder().name("fancy-display").description("Displays each line of the sign on separate lines in chat.").defaultValue(false).build());
        this.showCoords = this.formatGroup.add(new BoolSetting.Builder().name("show-coordinates").description("Display sign coordinates in chat.").defaultValue(false).build());
        this.renderOldSigns = this.oldSignGroup.add(new BoolSetting.Builder().name("oldSign-ESP").description("Render signs which could be old through walls.").defaultValue(false).build());
        this.espSettings = this.oldSignGroup.add(new GenericSetting.Builder().name("ESP-settings").defaultValue(new ESPBlockData(ShapeMode.Both, new SettingColor(Opcode.I2S, 233, Opcode.ARRAYLENGTH, 255), new SettingColor(Opcode.I2S, 233, Opcode.ARRAYLENGTH, 25), true, new SettingColor(Opcode.I2S, 233, Opcode.ARRAYLENGTH, Opcode.LUSHR))).build());
        this.clickESPSettings = this.formatGroup.add(new GenericSetting.Builder().name("clickESP-settings").description("Click on a chat entry to ESP the sign it belongs to. Click again or toggle the module to disable.").defaultValue(new ESPBlockData(ShapeMode.Both, new SettingColor(51, Opcode.FSUB, Opcode.FSUB, 255), new SettingColor(51, Opcode.FSUB, Opcode.FSUB, 25), true, new SettingColor(51, Opcode.FSUB, Opcode.FSUB, Opcode.L2F))).build());
        this.clickESPTimeout = this.formatGroup.add(new IntSetting.Builder().name("clickESP-timeout-seconds").description("Automatic timeout for active ClickESP entries. Set to 0 in order to disable timeout.").range(0, 1200).sliderRange(0, Opcode.ISHL).defaultValue(30).build());
        this.signBoardWaypoints = this.signBoardGroup.add(new BoolSetting.Builder().name("signBoard-waypoints").description("Adds waypoints to your Xaeros map when a cluster of signs is rendered.").defaultValue(false).visible(() -> {
            return StardustUtil.XAERO_AVAILABLE;
        }).build());
        this.temporaryWaypoints = this.signBoardGroup.add(new BoolSetting.Builder().name("temporary-waypoints").description("Temporary waypoints are cleared when you disconnect from the server or close the game.").defaultValue(true).visible(() -> {
            return StardustUtil.XAERO_AVAILABLE && this.signBoardWaypoints.get().booleanValue();
        }).build());
        this.waypointsIgnoreEmpty = this.signBoardGroup.add(new BoolSetting.Builder().name("waypoints-ignore-empty").description("If enabled, empty signs will not count towards a cluster waypoint.").defaultValue(true).visible(() -> {
            return StardustUtil.XAERO_AVAILABLE && this.signBoardWaypoints.get().booleanValue();
        }).build());
        this.waypointsIgnoreBlacklist = this.signBoardGroup.add(new BoolSetting.Builder().name("waypoints-ignore-blacklist").description("If enabled, signs containing blocked text will still count towards the waypoint cluster.").defaultValue(false).visible(() -> {
            return StardustUtil.XAERO_AVAILABLE && this.signBoardWaypoints.get().booleanValue();
        }).build());
        this.signBoardWaypointsAmount = this.signBoardGroup.add(new IntSetting.Builder().name("signBoard-waypoints-amount").description("The amount of signs to trigger adding a waypoint.").range(1, 1200).sliderRange(1, Opcode.ISHL).defaultValue(3).visible(() -> {
            return StardustUtil.XAERO_AVAILABLE && this.signBoardWaypoints.get().booleanValue();
        }).build());
        this.signBoardAutoLog = this.signBoardGroup.add(new BoolSetting.Builder().name("signBoard-autoDisconnect").description("Disconnect from the server when you render a cluster of signs.").defaultValue(false).build());
        this.forceKick = this.signBoardGroup.add(new BoolSetting.Builder().name("force-kick").description("Forces the server to kick you by sending an illegal packet.").defaultValue(false).build());
        this.signBoardAutoLogAmount = this.signBoardGroup.add(new IntSetting.Builder().name("signBoard-autoLog-threshold").description("The amount of signs to trigger a disconnect.").range(1, 1200).sliderRange(1, Opcode.ISHL).defaultValue(5).build());
        this.signBlacklist = this.blacklistGroup.add(new BoolSetting.Builder().name("signText-blacklist").description("Ignore signs that contain specific text (line-separated list in chatsigns-blacklist.txt)").defaultValue(false).onChanged(it -> {
            if (it.booleanValue() && isActive() && StardustUtil.checkOrCreateFile(this.mc, "meteor-client/chatsigns-blacklist.txt")) {
                this.blacklisted.clear();
                initBlacklistText();
                if (this.mc.field_1724 != null) {
                    MsgUtil.sendMsg("Please write one blacklisted item for each line of the file.");
                    MsgUtil.sendMsg("Spaces and other punctuation will be treated literally.");
                    MsgUtil.sendMsg("Toggle the ChatSigns module after updating the file's contents.");
                }
            }
        }).build());
        SettingGroup settingGroup5 = this.blacklistGroup;
        BoolSetting.Builder builderDefaultValue4 = new BoolSetting.Builder().name("case-sensitive-blacklist").description("Force matches in the blacklist file to be case-sensitive.").defaultValue(false);
        Setting<Boolean> setting5 = this.signBlacklist;
        Objects.requireNonNull(setting5);
        this.caseSensitive = settingGroup5.add(builderDefaultValue4.visible(setting5::get).build());
        SettingGroup settingGroup6 = this.blacklistGroup;
        BoolSetting.Builder builderDefaultValue5 = new BoolSetting.Builder().name("open-blacklist-file").description("Open the chatsigns-blacklist.txt file.").defaultValue(false);
        Setting<Boolean> setting6 = this.signBlacklist;
        Objects.requireNonNull(setting6);
        this.openBlacklistFile = settingGroup6.add(builderDefaultValue5.visible(setting6::get).onChanged(it2 -> {
            if (it2.booleanValue()) {
                if (!StardustUtil.checkOrCreateFile(this.mc, "meteor-client/chatsigns-blacklist.txt")) {
                    resetBlacklistFileSetting();
                } else {
                    openBlacklistFile();
                }
            }
        }).build());
        this.timer = 0;
        this.chatTimer = 0;
        this.clusterAmount = 0;
        this.fullClusterAmount = 0;
        this.emptyClusterAmount = 0;
        this.disconnectReason = null;
        this.lastFocusedSign = null;
        this.lastFullClusterPos = null;
        this.lastEmptyClusterPos = null;
        this.posSet = new HashSet<>();
        this.oldSet = new HashSet<>();
        this.blacklisted = new ArrayList<>();
        this.jobQueue = new ArrayDeque<>();
        this.cooldowns = new HashMap<>();
        this.signMessages = new HashMap<>();
        this.chunkCache = new HashMap<>();
        this.signsToHighlight = new HashMap<>();
        this.fullYearsPattern = Pattern.compile("202[0-9]");
        this.fullDatesPattern = Pattern.compile("\\b(\\d{1,2}[-/\\. _,'+]\\d{1,2}[-/\\. _,'+]\\d{2,4}|\\d{4}[-/\\. _,'+]\\d{1,2}[-/\\. _,'+]\\d{1,2})\\b");
    }

    @Nullable
    private class_2338 getTargetedSign() {
        class_746 player = this.mc.field_1724;
        if (player == null || this.mc.field_1687 == null) {
            return null;
        }
        int viewDistance = ((Integer) this.mc.field_1690.method_42503().method_41753()).intValue();
        double maxRangeBlocks = viewDistance * 16;
        class_3965 class_3965VarMethod_5745 = this.mc.method_1560().method_5745(maxRangeBlocks, 0.0f, false);
        if (class_3965VarMethod_5745 != null) {
            class_2338 pos = class_3965VarMethod_5745.method_17777();
            if (this.mc.field_1687.method_8321(pos) instanceof class_2625) {
                return pos;
            }
            return null;
        }
        return null;
    }

    private ArrayList<class_2625> getNearbySigns(class_2818 chunk) {
        ArrayList<class_2625> signs = new ArrayList<>();
        Map<class_2338, class_2586> blockEntities = chunk.method_12214();
        if (blockEntities == null) {
            return signs;
        }
        blockEntities.forEach((pos, entity) -> {
            if (entity instanceof class_2625) {
                class_2625 sbe = (class_2625) entity;
                signs.add(sbe);
            }
        });
        return signs;
    }

    private boolean isSignEmpty(class_2625 sbe) {
        return (sbe.method_49853().method_49861(this.mc.field_1724) || sbe.method_49854().method_49861(this.mc.field_1724)) ? false : true;
    }

    @Nullable
    private LocalDate parseDate(String dateStr) {
        String[] delimiters = {".", "-", "_", ",", "'", "+", "\\"};
        String[] formats = {"M/d/yy", "M/dd/yy", "MM/d/yy", "MM/dd/yy", "M/d/yyyy", "M/dd/yyyy", "MM/d/yyyy", "MM/dd/yyyy", "d/M/yy", "d/MM/yy", "dd/M/yy", "dd/MM/yy", "d/M/yyyy", "d/MM/yyyy", "dd/M/yyyy", "dd/MM/yyyy", "yyyy/M/d", "yyyy/MM/d", "yyyy/M/dd", "yyyy/MM/dd", "yyyy/d/M", "yyyy/dd/M", "yyyy/d/MM", "yyyy/dd/MM"};
        for (String format : formats) {
            LocalDate date = null;
            try {
                date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(format));
            } catch (Exception e) {
            }
            if (date != null) {
                return date;
            }
            for (String delimiter : delimiters) {
                String fmt = format.replace("/", delimiter);
                try {
                    date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(fmt));
                } catch (Exception e2) {
                }
                if (date != null) {
                    return date;
                }
            }
        }
        return null;
    }

    private String formatSignText(class_2625 sign, class_2818 chunk) {
        if (this.mc.field_1687 == null || isSignEmpty(sign)) {
            return "";
        }
        ArrayList<String> lines = new ArrayList<>();
        String color = this.signColor.get().label;
        String format = this.textFormat.get().label;
        for (class_2561 line : sign.method_49853().method_49877(false)) {
            line.method_27657(msg -> {
                if (this.chatFormat.get().booleanValue()) {
                    lines.add(msg);
                } else {
                    lines.add(msg.trim());
                }
                return Optional.empty();
            });
        }
        boolean couldBeOld = false;
        class_5321<class_1937> dimension = this.mc.field_1687.method_27983();
        if ((dimension != class_1937.field_25180 || !this.ignoreNether.get().booleanValue()) && !String.join(" ", lines).contains("**Pre-1.19 Sign restored by 0xTas' SignHistorian**")) {
            class_4719 woodType = class_4719.field_40350;
            class_2508 class_2508VarMethod_26204 = sign.method_11010().method_26204();
            if (class_2508VarMethod_26204 instanceof class_2508) {
                class_2508 signBlock = class_2508VarMethod_26204;
                woodType = signBlock.method_24025();
            } else if (class_2508VarMethod_26204 instanceof class_2551) {
                class_2551 wallSignBlock = (class_2551) class_2508VarMethod_26204;
                woodType = wallSignBlock.method_24025();
            }
            if (woodType == class_4719.field_21676) {
                class_2487 metadata = sign.method_38244(this.mc.field_1687.method_30349());
                if (!metadata.toString().contains("{\"extra\":[") && !lines.isEmpty()) {
                    String testString = String.join(" ", lines);
                    Matcher fullYearsMatcher = this.fullYearsPattern.matcher(testString);
                    if (!fullYearsMatcher.find()) {
                        boolean invalidDate = false;
                        Matcher dateMatcher = this.fullDatesPattern.matcher(testString);
                        while (dateMatcher.find()) {
                            String dateStr = dateMatcher.group();
                            LocalDate date = parseDate(dateStr);
                            if (date != null && date.getYear() > 2015) {
                                invalidDate = true;
                            }
                        }
                        if (!invalidDate) {
                            couldBeOld = !inNewChunk(chunk, this.mc, dimension);
                        }
                    }
                }
            }
        }
        if (!couldBeOld && this.showOldSigns.get().booleanValue() && this.onlyOldSigns.get().booleanValue()) {
            return "";
        }
        if (couldBeOld && this.showOldSigns.get().booleanValue()) {
            color = this.oldSignColor.get().label;
            this.oldSet.add(sign.method_11016());
        }
        String signText = this.chatFormat.get().booleanValue() ? String.join("\n" + color + format, lines) : String.join(" ", lines);
        if (signText.trim().isEmpty()) {
            return "";
        }
        StringBuilder txt = new StringBuilder();
        if (this.showOldSigns.get().booleanValue() && couldBeOld) {
            txt.append("?8[?7MasterClient?8] ");
            txt.append("?8[?4Old?7..?a??8] ");
            if (this.chatFormat.get().booleanValue()) {
                txt.append("\n     ");
            }
            txt.append(this.oldSignColor.get().label).append(this.oldSignFormat.get().label).append(this.chatFormat.get().booleanValue() ? signText.replace("\n", "\n     ") : signText.trim());
        } else {
            txt.append("?8[?7MasterClient?8]").append(this.chatFormat.get().booleanValue() ? "\n      " : " ");
            txt.append(color).append(format).append(this.chatFormat.get().booleanValue() ? signText.replace("\n", "\n     ") : signText.trim());
        }
        if (this.showCoords.get().booleanValue()) {
            class_2338 pos = sign.method_11016();
            txt.append(this.chatFormat.get().booleanValue() ? "\n?8[" : " ?8[").append(color).append(pos.method_10263()).append("?8, ").append(color).append(pos.method_10264()).append("?8, ").append(color).append(pos.method_10260()).append("?r?8]");
        }
        return txt.toString();
    }

    private boolean inNewChunk(class_2818 chunk, class_310 mc, class_5321<class_1937> dimension) {
        if (mc.field_1687 == null) {
            return false;
        }
        class_1923 chunkPos = chunk.method_12004();
        if (this.chunkCache.containsKey(chunkPos)) {
            return this.chunkCache.get(chunkPos).booleanValue();
        }
        if (dimension == class_1937.field_25180) {
            class_2338 startPosDebris = chunkPos.method_35231(0, 0, 0);
            class_2338 endPosDebris = chunkPos.method_35231(15, Opcode.FNEG, 15);
            int newBlocks = 0;
            for (class_2338 pos : class_2338.method_10097(startPosDebris, endPosDebris)) {
                if (newBlocks >= 13) {
                    this.chunkCache.put(chunkPos, true);
                    return true;
                }
                class_2248 block = mc.field_1687.method_8320(pos).method_26204();
                if (block == class_2246.field_22109 || block == class_2246.field_23869 || block == class_2246.field_22091 || block == class_2246.field_22113 || block == class_2246.field_22120 || block == class_2246.field_22090) {
                    newBlocks++;
                }
            }
            this.chunkCache.put(chunkPos, Boolean.valueOf(newBlocks >= 13));
            return newBlocks >= 13;
        }
        if (dimension == class_1937.field_25179) {
            class_2338 startPosAltStones = chunkPos.method_35231(0, 0, 0);
            class_2338 endPosAltStones = chunkPos.method_35231(15, 128, 15);
            int newBlocks2 = 0;
            for (class_2338 pos2 : class_2338.method_10097(startPosAltStones, endPosAltStones)) {
                if (newBlocks2 >= 33) {
                    this.chunkCache.put(chunkPos, true);
                    return true;
                }
                class_2248 block2 = mc.field_1687.method_8320(pos2).method_26204();
                if (block2 == class_2246.field_10115 || block2 == class_2246.field_10474 || block2 == class_2246.field_10508) {
                    newBlocks2++;
                }
            }
            this.chunkCache.put(chunkPos, Boolean.valueOf(newBlocks2 >= 33));
            return newBlocks2 >= 33;
        }
        if (dimension == class_1937.field_25181) {
            class_5321<class_1959> biome = (class_5321) mc.field_1687.method_23753(new class_2338(chunkPos.method_33940(), 64, chunkPos.method_33942())).method_40230().orElse(class_1972.field_34471);
            boolean bl = (biome == class_1972.field_9411 || biome == class_1972.field_9451) ? false : true;
            this.chunkCache.put(chunkPos, Boolean.valueOf(bl));
            return bl;
        }
        this.chunkCache.put(chunkPos, true);
        return true;
    }

    private void chatSigns(List<class_2625> signs, class_2818 chunk, class_310 mc) {
        if (mc.field_1687 == null || signs.isEmpty()) {
            return;
        }
        signs.forEach(sign -> {
            this.clusterAmount++;
            String textOnSign = ((String) Arrays.stream(sign.method_49853().method_49877(false)).map((v0) -> {
                return v0.getString();
            }).collect(Collectors.joining(" "))).trim();
            if (this.signMessages.containsKey(textOnSign) && this.ignoreDuplicates.get().booleanValue()) {
                this.fullClusterAmount++;
                this.lastFullClusterPos = sign.method_11016();
                return;
            }
            if (this.chatMode.get() == ChatMode.ESP && this.posSet.contains(sign.method_11016())) {
                return;
            }
            if (this.chatMode.get() == ChatMode.Both && this.posSet.contains(sign.method_11016()) && !sign.method_11016().equals(this.lastFocusedSign)) {
                return;
            }
            String msg = formatSignText(sign, chunk);
            this.posSet.add(sign.method_11016());
            if (msg.isBlank()) {
                this.emptyClusterAmount++;
                this.lastEmptyClusterPos = sign.method_11016();
                return;
            }
            if (this.signBlacklist.get().booleanValue() && !this.blacklisted.isEmpty()) {
                if (this.caseSensitive.get().booleanValue()) {
                    if (this.blacklisted.stream().anyMatch(line -> {
                        return textOnSign.contains(line.trim());
                    })) {
                        if (this.waypointsIgnoreBlacklist.get().booleanValue()) {
                            this.fullClusterAmount++;
                            this.lastFullClusterPos = sign.method_11016();
                            return;
                        }
                        return;
                    }
                } else if (this.blacklisted.stream().anyMatch(line2 -> {
                    return textOnSign.toLowerCase().contains(line2.trim().toLowerCase());
                })) {
                    if (this.waypointsIgnoreBlacklist.get().booleanValue()) {
                        this.fullClusterAmount++;
                        this.lastFullClusterPos = sign.method_11016();
                        return;
                    }
                    return;
                }
            }
            this.fullClusterAmount++;
            this.lastFullClusterPos = sign.method_11016();
            class_2583 clickESP = class_2583.field_24360.method_10958(new class_2558.class_10609("clickESP~chatSigns~" + sign.method_11016().method_10063())).method_10949(new class_2568.class_10613(class_2561.method_43470(this.signsToHighlight.containsKey(sign.method_11016()) ? "?4?oDisable ?7?oESP for this sign." : "?2?oEnable ?7?oESP for this sign.")));
            if (this.signMessages.containsKey(textOnSign) && !sign.method_11016().equals(this.lastFocusedSign)) {
                int timesSeen = this.signMessages.get(textOnSign).intValue() + 1;
                this.signMessages.put(textOnSign, Integer.valueOf(timesSeen));
                msg = msg + " ?8[?7?ox?4?o" + timesSeen + "?r?8]";
                this.jobQueue.removeIf(job -> {
                    return job.hashcode == textOnSign.hashCode();
                });
            } else {
                this.signMessages.put(textOnSign, 1);
            }
            if (this.chatSpeed.get().intValue() > 0) {
                this.jobQueue.add(new ChatSignsJob(class_2561.method_43470(msg).method_10862(clickESP), textOnSign.hashCode()));
            } else {
                mc.field_1705.method_1743().meteor$add(class_2561.method_43470(msg).method_10862(clickESP), textOnSign.hashCode());
            }
        });
    }

    private void initBlacklistText() {
        File blackListFile = FabricLoader.getInstance().getGameDir().resolve("meteor-client/chatsigns-blacklist.txt").toFile();
        try {
            Stream<String> lineStream = Files.lines(blackListFile.toPath());
            try {
                this.blacklisted.addAll(lineStream.toList());
                if (lineStream != null) {
                    lineStream.close();
                }
            } finally {
            }
        } catch (Exception err) {
            LogUtil.error("Failed to read from " + blackListFile.getAbsolutePath() + "! - Why: " + String.valueOf(err), this.name);
        }
    }

    private void openBlacklistFile() {
        resetBlacklistFileSetting();
        StardustUtil.openFile("meteor-client/chatsigns-blacklist.txt");
    }

    private void resetBlacklistFileSetting() {
        this.openBlacklistFile.set(false);
    }

    public boolean toggleClickESP(class_2338 pos, long timestamp) {
        if (this.signsToHighlight.containsKey(pos)) {
            this.signsToHighlight.remove(pos);
            return false;
        }
        this.signsToHighlight.put(pos, Long.valueOf(timestamp));
        return true;
    }

    private class_243 getTracerOffset(class_2680 state, class_2338 pos) {
        double offsetX;
        double offsetY;
        double offsetZ;
        try {
            if (!(state.method_26204() instanceof class_2551)) {
                return class_243.method_24953(pos);
            }
            class_2350 facing = state.method_11654(class_2551.field_11726);
            switch (AnonymousClass1.$SwitchMap$net$minecraft$util$math$Direction[facing.ordinal()]) {
                case 1:
                    offsetX = ((double) pos.method_10263()) + 0.5d;
                    offsetY = ((double) pos.method_10264()) + 0.5d;
                    offsetZ = ((double) pos.method_10260()) + 0.937d;
                    break;
                case 2:
                    offsetX = ((double) pos.method_10263()) + 0.1337d;
                    offsetY = ((double) pos.method_10264()) + 0.5d;
                    offsetZ = ((double) pos.method_10260()) + 0.5d;
                    break;
                case 3:
                    offsetX = ((double) pos.method_10263()) + 0.5d;
                    offsetY = ((double) pos.method_10264()) + 0.5d;
                    offsetZ = ((double) pos.method_10260()) + 0.1337d;
                    break;
                case 4:
                    offsetX = ((double) pos.method_10263()) + 0.937d;
                    offsetY = ((double) pos.method_10264()) + 0.5d;
                    offsetZ = ((double) pos.method_10260()) + 0.5d;
                    break;
                default:
                    offsetX = ((double) pos.method_10263()) + 0.5d;
                    offsetY = ((double) pos.method_10264()) + 0.5d;
                    offsetZ = ((double) pos.method_10260()) + 0.5d;
                    break;
            }
            return new class_243(offsetX, offsetY, offsetZ);
        } catch (Exception err) {
            LogUtil.error("Failed to get tracer offset. Why: " + String.valueOf(err), this.name);
            return class_243.method_24953(pos);
        }
    }

    /* JADX INFO: renamed from: meteordevelopment.meteorclient.systems.modules.misc.ChatSigns$1, reason: invalid class name */
    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/misc/ChatSigns$1.class */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$net$minecraft$util$math$Direction = new int[class_2350.values().length];

        static {
            try {
                $SwitchMap$net$minecraft$util$math$Direction[class_2350.field_11043.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$net$minecraft$util$math$Direction[class_2350.field_11034.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$net$minecraft$util$math$Direction[class_2350.field_11035.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$net$minecraft$util$math$Direction[class_2350.field_11039.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    private void doForceKick(class_2561 reason) {
        this.disconnectReason = reason;
        StardustUtil.illegalDisconnect(true, StardustUtil.IllegalDisconnectMethod.Slot);
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) {
            return;
        }
        if (this.signBlacklist.get().booleanValue() && StardustUtil.checkOrCreateFile(this.mc, "meteor-client/chatsigns-blacklist.txt")) {
            initBlacklistText();
        }
        class_2338 pos = this.mc.field_1724.method_24515();
        if (this.chatMode.get() == ChatMode.ESP || this.chatMode.get() == ChatMode.Both) {
            int viewDistance = ((Integer) this.mc.field_1690.method_42503().method_41753()).intValue();
            int startChunkX = (pos.method_10263() - (viewDistance * 16)) >> 4;
            int endChunkX = (pos.method_10263() + (viewDistance * 16)) >> 4;
            int startChunkZ = (pos.method_10260() - (viewDistance * 16)) >> 4;
            int endChunkZ = (pos.method_10260() + (viewDistance * 16)) >> 4;
            for (int x = startChunkX; x < endChunkX; x++) {
                for (int z = startChunkZ; z < endChunkZ; z++) {
                    if (this.mc.field_1687.method_8393(x, z)) {
                        class_2818 chunk = this.mc.field_1687.method_8497(x, z);
                        List<class_2625> signs = getNearbySigns(chunk);
                        chatSigns(signs, chunk, this.mc);
                    }
                }
            }
        }
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onDeactivate() {
        this.timer = 0;
        this.chatTimer = 0;
        this.clusterAmount = 0;
        this.fullClusterAmount = 0;
        this.emptyClusterAmount = 0;
        this.posSet.clear();
        this.oldSet.clear();
        this.jobQueue.clear();
        this.cooldowns.clear();
        this.chunkCache.clear();
        this.blacklisted.clear();
        this.signMessages.clear();
        this.lastFocusedSign = null;
        this.disconnectReason = null;
        this.signsToHighlight.clear();
        this.lastFullClusterPos = null;
        this.lastEmptyClusterPos = null;
    }

    @EventHandler
    private void onPacketReceived(PacketEvent.Receive event) {
        if (this.disconnectReason != null) {
            DisconnectS2CPacketAccessor disconnectS2CPacketAccessor = event.packet;
            if (disconnectS2CPacketAccessor instanceof class_2661) {
                ((class_2661) disconnectS2CPacketAccessor).setReason(this.disconnectReason);
                this.signBoardAutoLog.set(false);
                return;
            }
        }
        if (event.packet instanceof class_2724) {
            this.posSet.clear();
            this.oldSet.clear();
            this.chunkCache.clear();
            this.signsToHighlight.clear();
        }
    }

    @EventHandler
    private void onReceiveChunkData(ChunkDataEvent event) {
        if (this.mc.field_1687 != null && this.mc.field_1724 != null && this.chatMode.get() != ChatMode.Targeted) {
            List<class_2625> signs = getNearbySigns(event.chunk());
            chatSigns(signs, event.chunk(), this.mc);
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (StardustUtil.XAERO_AVAILABLE && this.signBoardWaypoints.get().booleanValue()) {
            if (!this.waypointsIgnoreEmpty.get().booleanValue() && this.emptyClusterAmount >= this.signBoardWaypointsAmount.get().intValue() && this.lastEmptyClusterPos != null) {
                // MapUtil waypoint removed (Xaero Minimap not available)
                this.emptyClusterAmount = 0;
            }
            if (this.fullClusterAmount >= this.signBoardWaypointsAmount.get().intValue() && this.lastFullClusterPos != null) {
                // MapUtil waypoint removed (Xaero Minimap not available)
                this.fullClusterAmount = 0;
            }
        }
        if (this.signBoardAutoLog.get().booleanValue() && this.clusterAmount >= this.signBoardAutoLogAmount.get().intValue()) {
            class_5250 class_5250VarMethod_43470 = class_2561.method_43470("?8[?a?oChatSigns?8] ?7Disconnected you because you rendered a cluster of ?a?o" + this.clusterAmount + " ?7signs?a!");
            if (this.forceKick.get().booleanValue()) {
                doForceKick(class_5250VarMethod_43470);
            } else {
                this.signBoardAutoLog.set(false);
                StardustUtil.disableAutoReconnect();
                this.mc.method_1562().method_52781(new class_2661(class_5250VarMethod_43470));
            }
            toggle();
            return;
        }
        if (this.chatMode.get() == ChatMode.ESP || this.mc.field_1687 == null || this.mc.field_1724 == null) {
            return;
        }
        if (this.timer >= 65535) {
            this.timer = 0;
        } else if (this.timer % 6000 == 0) {
            this.signMessages.clear();
        }
        this.timer++;
        this.chatTimer++;
        if (this.timer % 5 == 0) {
            this.timer = 0;
            this.clusterAmount = 0;
            this.fullClusterAmount = 0;
            this.emptyClusterAmount = 0;
            class_2338 targetedSign = getTargetedSign();
            if (targetedSign == null) {
                this.lastFocusedSign = null;
                return;
            }
            if (targetedSign.equals(this.lastFocusedSign) && this.repeatMode.get() == RepeatMode.Focus) {
                return;
            }
            if (!targetedSign.equals(this.lastFocusedSign)) {
                this.lastFocusedSign = targetedSign;
            }
            class_2818 chunk = this.mc.field_1687.method_8497(targetedSign.method_10263() >> 4, targetedSign.method_10260() >> 4);
            if (this.repeatMode.get() == RepeatMode.Cooldown) {
                if (this.cooldowns.containsKey(targetedSign)) {
                    Instant now = Instant.now();
                    Instant stamp = this.cooldowns.get(targetedSign);
                    if (Duration.between(stamp, now).toSeconds() < this.repeatSeconds.get().intValue()) {
                        return;
                    }
                    class_2625 class_2625VarMethod_8321 = this.mc.field_1687.method_8321(targetedSign);
                    if (class_2625VarMethod_8321 instanceof class_2625) {
                        class_2625 sign = class_2625VarMethod_8321;
                        chatSigns(List.of(sign), chunk, this.mc);
                    }
                } else {
                    class_2625 class_2625VarMethod_83212 = this.mc.field_1687.method_8321(targetedSign);
                    if (class_2625VarMethod_83212 instanceof class_2625) {
                        class_2625 sign2 = class_2625VarMethod_83212;
                        chatSigns(List.of(sign2), chunk, this.mc);
                    }
                }
            } else {
                class_2625 class_2625VarMethod_83213 = this.mc.field_1687.method_8321(targetedSign);
                if (class_2625VarMethod_83213 instanceof class_2625) {
                    class_2625 sign3 = class_2625VarMethod_83213;
                    chatSigns(List.of(sign3), chunk, this.mc);
                }
            }
            this.cooldowns.put(targetedSign, Instant.now());
            Iterator<class_2586> it = Utils.blockEntities().iterator();
            while (it.hasNext()) {
                class_2625 class_2625Var = (class_2586) it.next();
                if (class_2625Var instanceof class_2625) {
                    class_2625 sbe = class_2625Var;
                    if (!this.posSet.contains(sbe.method_11016())) {
                        class_2818 sbeChunk = this.mc.field_1687.method_8497(sbe.method_11016().method_10263() >> 4, sbe.method_11016().method_10260() >> 4);
                        chatSigns(List.of(sbe), sbeChunk, this.mc);
                    }
                }
            }
        }
        if (this.chatTimer >= this.chatSpeed.get().intValue() && !this.jobQueue.isEmpty()) {
            this.chatTimer = 0;
            if (this.chatSpeed.get().intValue() <= 0) {
                for (int n = 0; n < this.jobQueue.size(); n++) {
                    ChatSignsJob job = this.jobQueue.removeFirst();
                    this.mc.field_1705.method_1743().meteor$add(job.getMessage(), job.getHashcode());
                }
                return;
            }
            ChatSignsJob job2 = this.jobQueue.removeFirst();
            this.mc.field_1705.method_1743().meteor$add(job2.getMessage(), job2.getHashcode());
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) {
            return;
        }
        ESPBlockData highlight = this.clickESPSettings.get();
        List<class_2338> signsToRemove = new ArrayList<>();
        if (!this.signsToHighlight.isEmpty()) {
            for (class_2338 p : this.signsToHighlight.keySet()) {
                if (this.clickESPTimeout.get().intValue() > 0) {
                    long now = System.currentTimeMillis();
                    if (now - this.signsToHighlight.get(p).longValue() >= this.clickESPTimeout.get().intValue() * 1000) {
                        signsToRemove.add(p);
                    }
                }
                class_2680 state = this.mc.field_1687.method_8320(p);
                class_2586 sbe = this.mc.field_1687.method_8321(p);
                if (highlight.tracer && highlight.tracerColor.a > 0) {
                    class_243 offset = getTracerOffset(state, p);
                    event.renderer.line(RenderUtils.center.field_1352, RenderUtils.center.field_1351, RenderUtils.center.field_1350, offset.method_10216(), offset.method_10214(), offset.method_10215(), highlight.tracerColor);
                }
                if (state != null && sbe != null && (sbe instanceof class_2625)) {
                    class_265 shape = state.method_26218(this.mc.field_1687, p);
                    double x1 = ((double) p.method_10263()) + shape.method_1091(class_2350.class_2351.field_11048);
                    double y1 = ((double) p.method_10264()) + shape.method_1091(class_2350.class_2351.field_11052);
                    double z1 = ((double) p.method_10260()) + shape.method_1091(class_2350.class_2351.field_11051);
                    double x2 = ((double) p.method_10263()) + shape.method_1105(class_2350.class_2351.field_11048);
                    double y2 = ((double) p.method_10264()) + shape.method_1105(class_2350.class_2351.field_11052);
                    double z2 = ((double) p.method_10260()) + shape.method_1105(class_2350.class_2351.field_11051);
                    event.renderer.box(x1, y1, z1, x2, y2, z2, highlight.sideColor, highlight.lineColor, highlight.shapeMode, 0);
                }
            }
            Iterator<class_2338> it = signsToRemove.iterator();
            while (it.hasNext()) {
                this.signsToHighlight.remove(it.next());
            }
        }
        if (this.renderOldSigns.get().booleanValue()) {
            List<class_2338> inRange = this.oldSet.stream().filter(pos -> {
                return pos.method_19771(this.mc.field_1724.method_24515(), (((Integer) this.mc.field_1690.method_42503().method_41753()).intValue() * 16) + 32);
            }).toList();
            ESPBlockData esp = this.espSettings.get();
            for (class_2338 pos2 : inRange) {
                class_2680 state2 = this.mc.field_1687.method_8320(pos2);
                if ((state2.method_26204() instanceof class_2508) || (state2.method_26204() instanceof class_2551)) {
                    class_265 shape2 = state2.method_26218(this.mc.field_1687, pos2);
                    double x12 = ((double) pos2.method_10263()) + shape2.method_1091(class_2350.class_2351.field_11048);
                    double y12 = ((double) pos2.method_10264()) + shape2.method_1091(class_2350.class_2351.field_11052);
                    double z12 = ((double) pos2.method_10260()) + shape2.method_1091(class_2350.class_2351.field_11051);
                    double x22 = ((double) pos2.method_10263()) + shape2.method_1105(class_2350.class_2351.field_11048);
                    double y22 = ((double) pos2.method_10264()) + shape2.method_1105(class_2350.class_2351.field_11052);
                    double z22 = ((double) pos2.method_10260()) + shape2.method_1105(class_2350.class_2351.field_11051);
                    event.renderer.box(x12, y12, z12, x22, y22, z22, esp.sideColor, esp.lineColor, esp.shapeMode, 0);
                    if (esp.tracer) {
                        class_243 offsetVec = getTracerOffset(state2, pos2);
                        event.renderer.line(RenderUtils.center.field_1352, RenderUtils.center.field_1351, RenderUtils.center.field_1350, offsetVec.field_1352, offsetVec.field_1351, offsetVec.field_1350, esp.tracerColor);
                    }
                }
            }
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/misc/ChatSigns$ChatSignsJob.class */
    private record ChatSignsJob(class_2561 message, int hashcode) {

        public class_2561 getMessage() {
            return this.message;
        }

        public int getHashcode() {
            return this.hashcode;
        }
    }
}
