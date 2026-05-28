/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.misc;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.chars.Char2CharMap;
import it.unimi.dsi.fastutil.chars.Char2CharOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.events.game.SendMessageEvent;
import meteordevelopment.meteorclient.mixin.ChatHudAccessor;
import meteordevelopment.meteorclient.mixininterface.IChatHudLine;
import meteordevelopment.meteorclient.mixininterface.IChatHudLineVisible;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.text.MeteorClickEvent;
import meteordevelopment.meteorclient.utils.misc.text.TextVisitor;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.systems.friends.Friend;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class BetterChat extends Module {

    // -------------------------------------------------------------------------
    // Font enum
    // -------------------------------------------------------------------------
    public enum NameFont {
        DEFAULT("minecraft:default"),
        UNIFORM("minecraft:uniform"),
        ALT("minecraft:alt"),
        ILLAGERALT("minecraft:illageralt");

        public final String id;
        NameFont(String id) { this.id = id; }

        @Override
        public String toString() {
            return name().charAt(0) + name().substring(1).toLowerCase();
        }
    }

    private final SettingGroup sgGeneral         = settings.getDefaultGroup();
    private final SettingGroup sgFilter          = settings.createGroup("Filter");
    private final SettingGroup sgLongerChat      = settings.createGroup("Longer Chat");
    private final SettingGroup sgPrefix          = settings.createGroup("Prefix");
    private final SettingGroup sgSuffix          = settings.createGroup("Suffix");
    private final SettingGroup sgNameHighlight   = settings.createGroup("Name Highlight");
    private final SettingGroup sgFriendHighlight = settings.createGroup("Friend Highlight");

    // -------------------------------------------------------------------------
    // General
    // -------------------------------------------------------------------------

    private final Setting<Boolean> annoy = sgGeneral.add(new BoolSetting.Builder()
        .name("annoy")
        .description("Makes your messages aNnOyInG.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> fancy = sgGeneral.add(new BoolSetting.Builder()
        .name("fancy-chat")
        .description("Makes your messages ғᴀɴᴄʏ!")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> timestamps = sgGeneral.add(new BoolSetting.Builder()
        .name("timestamps")
        .description("Adds client-side time stamps to the beginning of chat messages.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> playerHeads = sgGeneral.add(new BoolSetting.Builder()
        .name("player-heads")
        .description("Displays player heads next to their messages.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> coordsProtection = sgGeneral.add(new BoolSetting.Builder()
        .name("coords-protection")
        .description("Prevents you from sending messages in chat that may contain coordinates.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> keepHistory = sgGeneral.add(new BoolSetting.Builder()
        .name("keep-history")
        .description("Prevents the chat history from being cleared when disconnecting.")
        .defaultValue(true)
        .build()
    );

    // Global bracket removal — flattens the whole message to a string first,
    // strips <n> → n, then rebuilds as a single literal per original style segment.
    private final Setting<Boolean> globalRemoveBrackets = sgGeneral.add(new BoolSetting.Builder()
        .name("remove-brackets")
        .description("Removes < > brackets from all player names in chat globally.")
        .defaultValue(false)
        .build()
    );

    // -------------------------------------------------------------------------
    // Filter
    // -------------------------------------------------------------------------

    private final Setting<Boolean> antiSpam = sgFilter.add(new BoolSetting.Builder()
        .name("anti-spam")
        .description("Blocks duplicate messages from filling your chat.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> antiSpamDepth = sgFilter.add(new IntSetting.Builder()
        .name("depth")
        .description("How many messages to filter.")
        .defaultValue(20)
        .min(1)
        .sliderMin(1)
        .visible(antiSpam::get)
        .build()
    );

    private final Setting<Boolean> antiClear = sgFilter.add(new BoolSetting.Builder()
        .name("anti-clear")
        .description("Prevents servers from clearing chat.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> filterRegex = sgFilter.add(new BoolSetting.Builder()
        .name("filter-regex")
        .description("Filter out chat messages that match the regex filter.")
        .defaultValue(false)
        .build()
    );

    private final Setting<List<String>> regexFilters = sgFilter.add(new StringListSetting.Builder()
        .name("regex-filter")
        .description("Regex filter used for filtering chat messages.")
        .visible(filterRegex::get)
        .onChanged(strings -> compileFilterRegexList())
        .build()
    );

    // -------------------------------------------------------------------------
    // Longer Chat
    // -------------------------------------------------------------------------

    private final Setting<Boolean> infiniteChatBox = sgLongerChat.add(new BoolSetting.Builder()
        .name("infinite-chat-box")
        .description("Lets you type infinitely long messages.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> longerChatHistory = sgLongerChat.add(new BoolSetting.Builder()
        .name("longer-chat-history")
        .description("Extends chat length.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> longerChatLines = sgLongerChat.add(new IntSetting.Builder()
        .name("extra-lines")
        .description("The amount of extra chat lines.")
        .defaultValue(1000)
        .min(0)
        .sliderRange(0, 1000)
        .visible(longerChatHistory::get)
        .build()
    );

    // -------------------------------------------------------------------------
    // Prefix
    // -------------------------------------------------------------------------

    private final Setting<Boolean> prefix = sgPrefix.add(new BoolSetting.Builder()
        .name("prefix")
        .description("Adds a prefix to your chat messages.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> prefixRandom = sgPrefix.add(new BoolSetting.Builder()
        .name("random")
        .description("Uses a random number as your prefix.")
        .defaultValue(false)
        .build()
    );

    private final Setting<String> prefixText = sgPrefix.add(new StringSetting.Builder()
        .name("text")
        .description("The text to add as your prefix.")
        .defaultValue("> ")
        .visible(() -> !prefixRandom.get())
        .build()
    );

    private final Setting<Boolean> prefixSmallCaps = sgPrefix.add(new BoolSetting.Builder()
        .name("small-caps")
        .description("Uses small caps in the prefix.")
        .defaultValue(false)
        .visible(() -> !prefixRandom.get())
        .build()
    );

    // -------------------------------------------------------------------------
    // Suffix
    // -------------------------------------------------------------------------

    private final Setting<Boolean> suffix = sgSuffix.add(new BoolSetting.Builder()
        .name("suffix")
        .description("Adds a suffix to your chat messages.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> suffixRandom = sgSuffix.add(new BoolSetting.Builder()
        .name("random")
        .description("Uses a random number as your suffix.")
        .defaultValue(false)
        .build()
    );

    private final Setting<String> suffixText = sgSuffix.add(new StringSetting.Builder()
        .name("text")
        .description("The text to add as your suffix.")
        .defaultValue(" | meteor on crack!")
        .visible(() -> !suffixRandom.get())
        .build()
    );

    private final Setting<Boolean> suffixSmallCaps = sgSuffix.add(new BoolSetting.Builder()
        .name("small-caps")
        .description("Uses small caps in the suffix.")
        .defaultValue(true)
        .visible(() -> !suffixRandom.get())
        .build()
    );

    // -------------------------------------------------------------------------
    // Name Highlight
    // -------------------------------------------------------------------------

    private final Setting<Boolean> nameHighlight = sgNameHighlight.add(new BoolSetting.Builder()
        .name("name-highlight")
        .description("Highlights your username when it appears in chat.")
        .defaultValue(true)
        .build()
    );

    public final Setting<SettingColor> nameHighlightColor = sgNameHighlight.add(new ColorSetting.Builder()
        .name("highlight-color")
        .description("Color used to highlight your name in chat.")
        .defaultValue(new SettingColor(255, 255, 0, 255))
        .build()
    );

    public final Setting<Boolean> nameHighlightShadow = sgNameHighlight.add(new BoolSetting.Builder()
        .name("name-shadow")
        .description("Renders a drop shadow behind your highlighted name.")
        .defaultValue(true)
        .visible(nameHighlight::get)
        .build()
    );

    public final Setting<Boolean> nameHighlightRemoveBrackets = sgNameHighlight.add(new BoolSetting.Builder()
        .name("remove-brackets")
        .description("Removes the < > brackets surrounding your name specifically. Overrides the global setting.")
        .defaultValue(false)
        .visible(nameHighlight::get)
        .build()
    );

    public final Setting<NameFont> nameHighlightFont = sgNameHighlight.add(new EnumSetting.Builder<NameFont>()
        .name("name-font")
        .description("Font used to render your highlighted name. Alt = enchanting table font.")
        .defaultValue(NameFont.DEFAULT)
        .visible(nameHighlight::get)
        .build()
    );

    // -------------------------------------------------------------------------
    // Friend Highlight
    // -------------------------------------------------------------------------

    private final Setting<Boolean> friendHighlight = sgFriendHighlight.add(new BoolSetting.Builder()
        .name("friend-highlight")
        .description("Highlights friends names when they appear in chat.")
        .defaultValue(true)
        .build()
    );

    public final Setting<SettingColor> friendHighlightColor = sgFriendHighlight.add(new ColorSetting.Builder()
        .name("highlight-color")
        .description("Color used to highlight friend names in chat.")
        .defaultValue(new SettingColor(0, 255, 0, 255))
        .build()
    );

    public final Setting<Boolean> friendHighlightShadow = sgFriendHighlight.add(new BoolSetting.Builder()
        .name("friend-shadow")
        .description("Renders a drop shadow behind highlighted friend names.")
        .defaultValue(true)
        .visible(friendHighlight::get)
        .build()
    );

    public final Setting<Boolean> friendHighlightRemoveBrackets = sgFriendHighlight.add(new BoolSetting.Builder()
        .name("remove-brackets")
        .description("Removes the < > brackets surrounding friend names specifically. Overrides the global setting.")
        .defaultValue(false)
        .visible(friendHighlight::get)
        .build()
    );

    public final Setting<NameFont> friendHighlightFont = sgFriendHighlight.add(new EnumSetting.Builder<NameFont>()
        .name("friend-font")
        .description("Font used to render highlighted friend names. Alt = enchanting table font.")
        .defaultValue(NameFont.DEFAULT)
        .visible(friendHighlight::get)
        .build()
    );

    // -------------------------------------------------------------------------
    // Patterns & state
    // -------------------------------------------------------------------------

    // Matches <anything> that is NOT a timestamp like <12:34>
    private static final Pattern globalBracketRegex = Pattern.compile("<(?!\\d{1,2}:\\d{2}>)([^>]+)>");

    private static final Pattern antiSpamRegex  = Pattern.compile(" \\(([0-9]{1,9})\\)$");
    private static final Pattern antiClearRegex = Pattern.compile("\\n(\\n|\\s)+\\n");
    private static final Pattern timestampRegex = Pattern.compile("^(<[0-9]{2}:[0-9]{2}>\\s)");
    private static final Pattern usernameRegex  = Pattern.compile("^(?:<[0-9]{2}:[0-9]{2}>\\s)?<(.*?)>.*");

    private final Char2CharMap SMALL_CAPS = new Char2CharOpenHashMap();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
    public final IntList lines = new IntArrayList();

    public BetterChat() {
        super(Categories.Misc, "better-chat", "Improves your chat experience in various ways.");

        String[] a = "abcdefghijklmnopqrstuvwxyz".split("");
        String[] b = "ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴩqʀꜱᴛᴜᴠᴡxyᴢ".split("");
        for (int i = 0; i < a.length; i++) SMALL_CAPS.put(a[i].charAt(0), b[i].charAt(0));
        compileFilterRegexList();
    }

    // -------------------------------------------------------------------------
    // Event handlers
    // -------------------------------------------------------------------------

    @EventHandler
    private void onMessageReceive(ReceiveMessageEvent event) {
        Text message = event.getMessage();

        if (filterRegex.get()) {
            String messageString = message.getString();
            for (Pattern pattern : filterRegexList) {
                if (pattern.matcher(messageString).find()) {
                    event.cancel();
                    return;
                }
            }
        }

        if (antiClear.get()) {
            String messageString = message.getString();
            if (antiClearRegex.matcher(messageString).find()) {
                MutableText newMessage = Text.empty();
                TextVisitor.visit(message, (text, style, string) -> {
                    Matcher antiClearMatcher = antiClearRegex.matcher(string);
                    if (antiClearMatcher.find()) {
                        newMessage.append(Text.literal(antiClearMatcher.replaceAll("\n\n")).setStyle(style));
                    } else {
                        newMessage.append(text.copyContentOnly().setStyle(style));
                    }
                    return Optional.empty();
                }, Style.EMPTY);
                message = newMessage;
            }
        }

        if (antiSpam.get()) {
            Text antiSpammed = appendAntiSpam(message);
            if (antiSpammed != null) message = antiSpammed;
        }

        if (timestamps.get()) {
            Text timestamp = Text.literal("<" + dateFormat.format(new Date()) + "> ").formatted(Formatting.GRAY);
            message = Text.empty().append(timestamp).append(message);
        }

        // Global bracket removal — works on the flattened string so split nodes
        // like "<" + "Name" + ">" are handled correctly.
        if (globalRemoveBrackets.get()) {
            message = applyGlobalBracketRemoval(message);
        }

        // Own name highlight
        if (nameHighlight.get() && mc.player != null) {
            String playerName = mc.player.getName().getString();
            String msgStr = message.getString();
            if (msgStr.contains("<" + playerName + ">") || msgStr.contains(playerName)) {
                message = highlightName(message, playerName);
            }
        }

        // Friend highlight
        if (friendHighlight.get()) {
            message = highlightListNames(message);
        }

        event.setMessage(message);
    }

    @EventHandler
    private void onMessageSend(SendMessageEvent event) {
        String message = event.message;

        if (annoy.get()) message = applyAnnoy(message);
        if (fancy.get()) message = applyFancy(message);

        message = getPrefix() + message + getSuffix();

        if (coordsProtection.get() && containsCoordinates(message)) {
            MutableText warningMessage = Text.literal("It looks like there are coordinates in your message! ");
            warningMessage.append(getSendButton(message));
            ChatUtils.sendMsg(warningMessage);
            event.cancel();
            return;
        }

        event.message = message;
    }

    // -------------------------------------------------------------------------
    // Global bracket removal
    // -------------------------------------------------------------------------

    /**
     * The core problem: Minecraft's chat Text tree stores "<", "Name", ">"
     * as three separate sibling nodes, each with their own Style. A per-node
     * regex will never see "<Name>" as one string.
     *
     * Fix: collect every (style, string) leaf pair into a flat list, then
     * operate on the fully concatenated string with the bracket regex, and
     * finally re-slice the replacement string back into the original style
     * segments so colours / formatting are preserved everywhere else.
     */
    private MutableText applyGlobalBracketRemoval(Text message) {
        // 1. Collect all leaf (style, content) pairs in order.
        List<StyledSegment> segments = new ArrayList<>();
        TextVisitor.visit(message, (text, style, string) -> {
            segments.add(new StyledSegment(style, string));
            return Optional.empty();
        }, Style.EMPTY);

        // 2. Build the full flat string and keep a map of char-index → segment index.
        StringBuilder full = new StringBuilder();
        int[] segmentIndex = new int[segments.stream().mapToInt(s -> s.text.length()).sum()];
        int charPos = 0;
        for (int si = 0; si < segments.size(); si++) {
            String seg = segments.get(si).text;
            for (int ci = 0; ci < seg.length(); ci++) {
                segmentIndex[charPos++] = si;
            }
            full.append(seg);
        }

        // 3. Run the bracket regex on the full string, collecting replacement ranges.
        //    Each match of <n> becomes just "n" (inner group 1).
        String fullStr = full.toString();
        Matcher m = globalBracketRegex.matcher(fullStr);
        // Build a list of (start, end, replacement) spans to apply.
        record Replacement(int start, int end, String text) {}
        List<Replacement> replacements = new ArrayList<>();
        while (m.find()) {
            replacements.add(new Replacement(m.start(), m.end(), m.group(1)));
        }

        if (replacements.isEmpty()) {
            // Nothing to do — return original to avoid unnecessary rebuild.
            return Text.literal("").append(message);
        }

        // 4. Rebuild: walk through fullStr, applying replacements, and emit
        //    Text nodes styled according to which segment each character came from.
        MutableText result = Text.literal("");
        int pos = 0;
        // We emit character-by-character into runs of the same (style, isReplacement).
        // For efficiency we accumulate a buffer per style.

        // Merge replacements into a flat list of (outputText, style) spans.
        List<StyledSegment> outputSpans = new ArrayList<>();

        int repIdx = 0;
        while (pos < fullStr.length()) {
            if (repIdx < replacements.size() && pos == replacements.get(repIdx).start()) {
                // Emit the replacement text using the style of the first char of the match
                // (the '<' character's style — usually the same as the name's anyway).
                Replacement rep = replacements.get(repIdx);
                Style repStyle = segments.get(segmentIndex[Math.min(pos, segmentIndex.length - 1)]).style;
                outputSpans.add(new StyledSegment(repStyle, rep.text));
                pos = rep.end();
                repIdx++;
            } else {
                // Normal character — use its original segment's style.
                int segIdx = segmentIndex[pos];
                Style style = segments.get(segIdx).style;
                // Accumulate consecutive characters that share the same style segment.
                int endPos = pos;
                int nextRepStart = repIdx < replacements.size() ? replacements.get(repIdx).start() : fullStr.length();
                while (endPos < fullStr.length()
                    && segmentIndex[endPos] == segIdx
                    && endPos < nextRepStart) {
                    endPos++;
                }
                outputSpans.add(new StyledSegment(style, fullStr.substring(pos, endPos)));
                pos = endPos;
            }
        }

        // 5. Merge adjacent spans with the same style (optional but tidy), then emit.
        for (StyledSegment span : outputSpans) {
            if (!span.text.isEmpty()) {
                result.append(Text.literal(span.text).setStyle(span.style));
            }
        }

        return result;
    }

    /** Simple value type used by applyGlobalBracketRemoval. */
    private record StyledSegment(Style style, String text) {}

    // -------------------------------------------------------------------------
    // Name / Friend Highlight
    // -------------------------------------------------------------------------

    private MutableText highlightName(Text message, String name) {
        SettingColor col = nameHighlightColor.get();
        int rgb = (col.r << 16) | (col.g << 8) | col.b;
        Style style = Style.EMPTY
            .withColor(TextColor.fromRgb(rgb))
            .withBold(true)
            .withFont(Identifier.of(nameHighlightFont.get().id));
        if (!nameHighlightShadow.get()) style = style.withShadowColor(0x00000000);

        boolean strippedAlready = globalRemoveBrackets.get();
        boolean stripHere       = !strippedAlready && nameHighlightRemoveBrackets.get();

        return stripHere
            ? applyHighlightStripBrackets(message, name, style)
            : applyHighlight(message, name, style);
    }

    public MutableText highlightListNames(Text message) {
        String msgStr = message.getString();
        MutableText result = Text.literal("").append(message);

        for (Friend friend : Friends.get()) {
            String name = friend.name;
            if (name == null || name.isBlank()) continue;

            if (!msgStr.contains("<" + name + ">") && !msgStr.contains(name)) continue;

            SettingColor col = friendHighlightColor.get();
            int rgb = (col.r << 16) | (col.g << 8) | col.b;
            Style style = Style.EMPTY
                .withColor(TextColor.fromRgb(rgb))
                .withBold(true)
                .withFont(Identifier.of(friendHighlightFont.get().id));
            if (!friendHighlightShadow.get()) style = style.withShadowColor(0x00000000);

            boolean strippedAlready = globalRemoveBrackets.get();
            boolean stripHere       = !strippedAlready && friendHighlightRemoveBrackets.get();

            result = stripHere
                ? applyHighlightStripBrackets(result, name, style)
                : applyHighlight(result, name, style);
        }

        return result;
    }

    /** Standard highlight: colours every plain occurrence of {@code name}. */
    public MutableText applyHighlight(Text message, String name, Style nameStyle) {
        MutableText result = Text.literal("");
        TextVisitor.visit(message, (text, style, string) -> {
            if (string.contains(name)) {
                int start = 0, idx;
                while ((idx = string.indexOf(name, start)) != -1) {
                    if (idx > start)
                        result.append(Text.literal(string.substring(start, idx)).setStyle(style));
                    result.append(Text.literal(name).setStyle(nameStyle));
                    start = idx + name.length();
                }
                if (start < string.length())
                    result.append(Text.literal(string.substring(start)).setStyle(style));
            } else {
                result.append(text.copyContentOnly().setStyle(style));
            }
            return Optional.empty();
        }, Style.EMPTY);
        return result;
    }

    /**
     * Bracket-stripping highlight for a specific name: replaces {@code <n>}
     * with the highlighted name only. Falls back to plain highlight for
     * occurrences that aren't bracket-wrapped.
     */
    private MutableText applyHighlightStripBrackets(Text message, String name, Style nameStyle) {
        String bracketedName = "<" + name + ">";
        MutableText result = Text.literal("");

        TextVisitor.visit(message, (text, style, string) -> {
            if (string.contains(bracketedName)) {
                int start = 0, idx;
                while ((idx = string.indexOf(bracketedName, start)) != -1) {
                    if (idx > start)
                        result.append(Text.literal(string.substring(start, idx)).setStyle(style));
                    result.append(Text.literal(" ").setStyle(style));
                    result.append(Text.literal(name).setStyle(nameStyle));
                    result.append(Text.literal(" ").setStyle(style));
                    start = idx + bracketedName.length();
                }
                if (start < string.length())
                    result.append(Text.literal(string.substring(start)).setStyle(style));
            } else if (string.contains(name)) {
                int start = 0, idx;
                while ((idx = string.indexOf(name, start)) != -1) {
                    if (idx > start)
                        result.append(Text.literal(string.substring(start, idx)).setStyle(style));
                    result.append(Text.literal(name).setStyle(nameStyle));
                    start = idx + name.length();
                }
                if (start < string.length())
                    result.append(Text.literal(string.substring(start)).setStyle(style));
            } else {
                result.append(text.copyContentOnly().setStyle(style));
            }
            return Optional.empty();
        }, Style.EMPTY);

        return result;
    }

    // -------------------------------------------------------------------------
    // Anti Spam
    // -------------------------------------------------------------------------

    private Text appendAntiSpam(Text text) {
        String textString = text.getString();
        Text returnText = null;
        int messageIndex = -1;

        List<ChatHudLine> messages = ((ChatHudAccessor) mc.inGameHud.getChatHud()).getMessages();
        if (messages.isEmpty()) return null;

        for (int i = 0; i < Math.min(antiSpamDepth.get(), messages.size()); i++) {
            String stringToCheck = messages.get(i).content().getString();

            Matcher timestampMatcher = timestampRegex.matcher(stringToCheck);
            if (timestampMatcher.find()) stringToCheck = stringToCheck.substring(8);

            if (textString.equals(stringToCheck)) {
                messageIndex = i;
                returnText = text.copy().append(Text.literal(" (2)").formatted(Formatting.GRAY));
                break;
            } else {
                Matcher matcher = antiSpamRegex.matcher(stringToCheck);
                if (!matcher.find()) continue;

                String group = matcher.group(matcher.groupCount());
                int number = Integer.parseInt(group);

                if (stringToCheck.substring(0, matcher.start()).equals(textString)) {
                    messageIndex = i;
                    returnText = text.copy().append(Text.literal(" (" + (number + 1) + ")").formatted(Formatting.GRAY));
                    break;
                }
            }
        }

        if (returnText != null) {
            List<ChatHudLine.Visible> visible = ((ChatHudAccessor) mc.inGameHud.getChatHud()).getVisibleMessages();

            int start = -1;
            for (int i = 0; i < messageIndex; i++) start += lines.getInt(i);

            int i = lines.getInt(messageIndex);
            while (i > 0) { visible.remove(start + 1); i--; }

            messages.remove(messageIndex);
            lines.removeInt(messageIndex);
        }

        return returnText;
    }

    public void removeLine(int index) {
        if (index >= lines.size()) {
            if (antiSpam.get()) {
                error("Issue detected with the anti-spam system! Likely a compatibility issue with another mod. Disabling anti-spam to protect chat integrity.");
                antiSpam.set(false);
            }
            return;
        }
        lines.removeInt(index);
    }

    // -------------------------------------------------------------------------
    // Player Heads
    // -------------------------------------------------------------------------

    private record CustomHeadEntry(String prefix, Identifier texture) {}

    private static final List<CustomHeadEntry> CUSTOM_HEAD_ENTRIES = new ArrayList<>();
    private static final Pattern TIMESTAMP_REGEX = Pattern.compile("^<\\d{1,2}:\\d{1,2}>");

    public static void registerCustomHead(String prefix, Identifier texture) {
        CUSTOM_HEAD_ENTRIES.add(new CustomHeadEntry(prefix, texture));
    }

    static {
        registerCustomHead("[Meteor]",   MeteorClient.identifier("textures/icons/chat/meteor.png"));
        registerCustomHead("[Baritone]", MeteorClient.identifier("textures/icons/chat/baritone.png"));
    }

    public int modifyChatWidth(int width) {
        if (isActive() && playerHeads.get()) return width + 10;
        return width;
    }

    public void drawPlayerHead(DrawContext context, ChatHudLine.Visible line, int y, int color) {
        if (!isActive() || !playerHeads.get()) return;

        if (((IChatHudLineVisible) (Object) line).meteor$isStartOfEntry()) {
            RenderSystem.setShaderColor(1, 1, 1, Color.toRGBAA(color) / 255f);
            drawTexture(context, (IChatHudLine) (Object) line, y);
            RenderSystem.setShaderColor(1, 1, 1, 1);
        }

        context.getMatrices().translate(10, 0, 0);
    }

    private void drawTexture(DrawContext context, IChatHudLine line, int y) {
        String text = line.meteor$getText().trim();
        int startOffset = 0;

        try {
            Matcher m = TIMESTAMP_REGEX.matcher(text);
            if (m.find()) startOffset = m.end() + 1;
        } catch (IllegalStateException ignored) {}

        for (CustomHeadEntry entry : CUSTOM_HEAD_ENTRIES) {
            if (text.startsWith(entry.prefix(), startOffset)) {
                context.drawTexture(RenderLayer::getGuiTextured, entry.texture(), 0, y, 0, 0, 8, 8, 64, 64, 64, 64);
                return;
            }
        }

        GameProfile sender = getSender(line, text);
        if (sender == null) return;

        PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(sender.getId());
        if (entry == null) return;

        PlayerSkinDrawer.draw(context, entry.getSkinTextures(), 0, y, 8);
    }

    private GameProfile getSender(IChatHudLine line, String text) {
        GameProfile sender = line.meteor$getSender();

        if (sender == null) {
            Matcher usernameMatcher = usernameRegex.matcher(text);
            if (usernameMatcher.matches()) {
                String username = usernameMatcher.group(1);
                PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(username);
                if (entry != null) sender = entry.getProfile();
            }
        }

        return sender;
    }

    // -------------------------------------------------------------------------
    // Annoy / Fancy
    // -------------------------------------------------------------------------

    private String applyAnnoy(String message) {
        StringBuilder sb = new StringBuilder(message.length());
        boolean upperCase = true;
        for (int cp : message.codePoints().toArray()) {
            if (upperCase) sb.appendCodePoint(Character.toUpperCase(cp));
            else           sb.appendCodePoint(Character.toLowerCase(cp));
            upperCase = !upperCase;
        }
        return sb.toString();
    }

    private String applyFancy(String message) {
        StringBuilder sb = new StringBuilder();
        for (char ch : message.toCharArray()) sb.append(SMALL_CAPS.getOrDefault(ch, ch));
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Filter Regex
    // -------------------------------------------------------------------------

    private final List<Pattern> filterRegexList = new ArrayList<>();

    private void compileFilterRegexList() {
        filterRegexList.clear();
        for (int i = 0; i < regexFilters.get().size(); i++) {
            try {
                filterRegexList.add(Pattern.compile(regexFilters.get().get(i)));
            } catch (PatternSyntaxException e) {
                String removed = regexFilters.get().remove(i);
                error("Removing Invalid regex: %s", removed);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Prefix / Suffix
    // -------------------------------------------------------------------------

    private String getPrefix() {
        return prefix.get() ? getAffix(prefixText.get(), prefixSmallCaps.get(), prefixRandom.get()) : "";
    }

    private String getSuffix() {
        return suffix.get() ? getAffix(suffixText.get(), suffixSmallCaps.get(), suffixRandom.get()) : "";
    }

    private String getAffix(String text, boolean smallcaps, boolean random) {
        if (random)         return String.format("(%03d) ", Utils.random(0, 1000));
        else if (smallcaps) return applyFancy(text);
        else                return text;
    }

    // -------------------------------------------------------------------------
    // Coords Protection
    // -------------------------------------------------------------------------

    private static final Pattern coordRegex = Pattern.compile(
        "(?<x>-?\\d{3,}(?:\\.\\d*)?)(?:\\s+(?<y>-?\\d{1,3}(?:\\.\\d*)?))?\\s+(?<z>-?\\d{3,}(?:\\.\\d*)?)"
    );

    private boolean containsCoordinates(String message) {
        return coordRegex.matcher(message).find();
    }

    private MutableText getSendButton(String message) {
        MutableText sendButton   = Text.literal("[SEND ANYWAY]");
        MutableText hintBaseText = Text.literal("");

        MutableText hintMsg = Text.literal("Send your message to the global chat even if there are coordinates:");
        hintMsg.setStyle(hintBaseText.getStyle().withFormatting(Formatting.GRAY));
        hintBaseText.append(hintMsg);
        hintBaseText.append(Text.literal('\n' + message));

        sendButton.setStyle(sendButton.getStyle()
            .withFormatting(Formatting.DARK_RED)
            .withClickEvent(new MeteorClickEvent(Commands.get("say").toString(message)))
            .withHoverEvent(new HoverEvent.ShowText(hintBaseText)));
        return sendButton;
    }

    // -------------------------------------------------------------------------
    // Longer Chat / public accessors
    // -------------------------------------------------------------------------

    public boolean isInfiniteChatBox() { return isActive() && infiniteChatBox.get(); }
    public boolean isLongerChat()      { return isActive() && longerChatHistory.get(); }
    public boolean keepHistory()       { return isActive() && keepHistory.get(); }
    public int     getExtraChatLines() { return longerChatLines.get(); }
}
