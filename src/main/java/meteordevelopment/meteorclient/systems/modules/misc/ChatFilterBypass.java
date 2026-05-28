package meteordevelopment.meteorclient.systems.modules.misc;

import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.events.game.SendMessageEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringListSetting;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class ChatFilterBypass extends Module {

    private final SettingGroup sgEncrypt = settings.createGroup("Encrypt");
    private final SettingGroup sgDecrypt = settings.createGroup("Decrypt");

    // --- Encrypt settings ---
    private final Setting<List<String>> encryptWords = sgEncrypt.add(new StringListSetting.Builder()
        .name("words-to-encrypt")
        .description("Words in your outgoing messages that will be replaced with their Base64 encoding.")
        .defaultValue(List.of())
        .onChanged(v -> rebuildCache())
        .build()
    );

    private final Setting<Boolean> caseSensitive = sgEncrypt.add(new BoolSetting.Builder()
        .name("case-sensitive")
        .description("When off, 'hello' will also match 'Hello' and 'HELLO'.")
        .defaultValue(true)
        .onChanged(v -> rebuildCache())
        .build()
    );

    // --- Decrypt settings ---
    private final Setting<Boolean> decryptIncoming = sgDecrypt.add(new BoolSetting.Builder()
        .name("decrypt-incoming")
        .description("Scan incoming chat messages for Base64 tokens and replace them with their decoded text.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showDecryptMarker = sgDecrypt.add(new BoolSetting.Builder()
        .name("show-decrypt-marker")
        .description("Wraps decrypted words in [brackets] so you know they were encrypted.")
        .defaultValue(true)
        .build()
    );

    private final Setting<SettingColor> decryptColor = sgDecrypt.add(new ColorSetting.Builder()
        .name("decrypt-color")
        .description("Color used to highlight decrypted words in chat.")
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .build()
    );

    private final Setting<Integer> minTokenLength = sgDecrypt.add(new IntSetting.Builder()
        .name("min-token-length")
        .description("Minimum character length of a token before attempting Base64 decoding. Raise to reduce false positives.")
        .defaultValue(8)
        .min(4)
        .sliderMax(24)
        .build()
    );

    // Cache: word -> precompiled Pattern, rebuilt whenever the word list or case setting changes
    private final Map<String, String> encodeCache = new HashMap<>(); // word -> base64
    private final Map<String, Pattern> patternCache = new HashMap<>(); // word -> compiled regex

    public ChatFilterBypass() {
        super(Categories.Misc, "chat-encrypt",
            "Encrypts chosen words in your outgoing messages to Base64, and optionally decodes Base64 tokens in incoming chat.");
    }

    @Override
    public void onActivate() {
        rebuildCache();
    }

    // -------------------------------------------------------------------------
    // Outgoing -- encrypt matching words before the message is sent
    // -------------------------------------------------------------------------
    @EventHandler
    private void onMessageSend(SendMessageEvent event) {
        if (mc.player == null) return;
        if (encodeCache.isEmpty()) return;

        String msg = event.message;

        for (Map.Entry<String, Pattern> entry : patternCache.entrySet()) {
            String encoded = encodeCache.get(entry.getKey());
            msg = entry.getValue().matcher(msg).replaceAll(encoded);
        }

        event.message = msg;
    }

    // -------------------------------------------------------------------------
    // Incoming -- decode any Base64 tokens found in the chat message
    // -------------------------------------------------------------------------
    @EventHandler
    private void onMessageReceive(ReceiveMessageEvent event) {
        if (mc.player == null) return;
        if (!decryptIncoming.get()) return;

        // Strip formatting codes first so they don't create fake Base64 tokens
        String raw = Formatting.strip(event.getMessage().getString());
        if (raw == null) return;

        MutableText newText = buildColoredText(raw);
        if (newText != null) {
            event.setMessage(newText);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Rebuilds the encode/pattern caches whenever the word list or case sensitivity changes.
     * Avoids recompiling regexes every time a message is sent.
     */
    private void rebuildCache() {
        encodeCache.clear();
        patternCache.clear();

        for (String word : encryptWords.get()) {
            if (word == null || word.isBlank()) continue;

            String encoded = Base64.getEncoder()
                .encodeToString(word.getBytes(StandardCharsets.UTF_8));
            encodeCache.put(word, encoded);

            int flags = caseSensitive.get() ? 0 : Pattern.CASE_INSENSITIVE;
            Pattern pattern = Pattern.compile(
                "(?<![\\w])" + Pattern.quote(word) + "(?![\\w])", flags
            );
            patternCache.put(word, pattern);
        }
    }

    /**
     * Scans space-separated tokens in a message, decodes any valid Base64 ones,
     * and returns a MutableText where decoded words are rendered in decryptColor
     * while the rest of the message keeps its normal white color.
     * Also re-applies BetterChat name highlighting on non-decoded tokens so the
     * player name is not lost when the message is reconstructed.
     * Returns null if nothing was decoded so the original message is left alone.
     */
    private MutableText buildColoredText(String message) {
        String[] parts = message.split(" ", -1);
        MutableText result = Text.literal("");
        boolean anyDecoded = false;

        SettingColor col = decryptColor.get();
        int rgb = (col.r << 16) | (col.g << 8) | col.b;
        Style colorStyle = Style.EMPTY.withColor(TextColor.fromRgb(rgb));

        // Grab the player name once so we can re-highlight it in plain tokens
        String playerName = (mc.player != null) ? mc.player.getName().getString() : null;
        BetterChat betterChat = Modules.get().get(BetterChat.class);
        boolean doNameHighlight = playerName != null && betterChat != null && betterChat.isActive();

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            String decoded = tryDecode(part);

            if (i > 0) result.append(Text.literal(" ").setStyle(Style.EMPTY));

            if (decoded != null) {
                anyDecoded = true;
                String display = showDecryptMarker.get() ? "[" + decoded + "]" : decoded;
                result.append(Text.literal(display).setStyle(colorStyle));
            } else {
                // Re-apply name highlight on plain tokens so BetterChat's highlight
                // is not lost when we reconstruct the message from the stripped string
                if (doNameHighlight && part.contains(playerName)) {
                    appendWithNameHighlight(result, part, playerName);
                } else {
                    result.append(Text.literal(part).setStyle(Style.EMPTY));
                }
            }
        }

        // Re-apply friend highlight so it survives message reconstruction
        if (anyDecoded) {
            betterChat = Modules.get().get(BetterChat.class);
            if (betterChat != null && betterChat.isActive()) {
                result = betterChat.highlightListNames(result);
            }
        }

        return anyDecoded ? result : null;
    }

    /**
     * Appends the given token to result, wrapping occurrences of playerName in the
     * style that BetterChat uses for name highlighting.
     * Delegates to BetterChat.applyHighlight so color stays in sync with the setting.
     */
    private void appendWithNameHighlight(MutableText result, String token, String playerName) {
        BetterChat betterChat = Modules.get().get(BetterChat.class);
        if (betterChat == null) {
            result.append(Text.literal(token).setStyle(Style.EMPTY));
            return;
        }

        SettingColor nhCol = betterChat.nameHighlightColor.get();
        int nhRgb = (nhCol.r << 16) | (nhCol.g << 8) | nhCol.b;
        Style nameStyle = Style.EMPTY.withColor(TextColor.fromRgb(nhRgb)).withBold(true);

        result.append(betterChat.applyHighlight(Text.literal(token), playerName, nameStyle));
    }

    /**
     * Attempts to decode a single token as Base64.
     * Returns the decoded string if valid, or null if it should be kept as-is.
     */
    private String tryDecode(String part) {
        if (!isBase64Candidate(part)) return null;
        try {
            String padded = part;
            int rem = padded.length() % 4;
            if (rem == 2) padded += "==";
            else if (rem == 3) padded += "=";

            byte[] bytes = Base64.getDecoder().decode(padded);
            String text = new String(bytes, StandardCharsets.UTF_8);
            return isPrintable(text) ? text : null;
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    /**
     * Returns true if the token looks like Base64 -- only the standard
     * Base64 alphabet, no surrounding punctuation allowed.
     */
    private boolean isBase64Candidate(String s) {
        if (s.length() < minTokenLength.get()) return false;
        if (s.length() % 4 != 0) return false; // valid Base64 length is always a multiple of 4
        return s.matches("^[A-Za-z0-9+/]*={0,2}$");
    }

    /**
     * Returns true if the decoded string looks like a real word:
     * - No characters outside printable ASCII (rejects unicode garbage)
     * - At least 50% of characters are letters (rejects punctuation soup)
     * - No longer than 64 characters (single words should not be huge)
     */
    private boolean isPrintable(String s) {
        if (s.isBlank()) return false;
        if (s.length() > 64) return false;

        int asciiLetterCount = 0;
        for (char c : s.toCharArray()) {
            if (c < 0x20 && c != '\t' && c != '\n' && c != '\r') return false;
            if (c == 0x7F) return false;
            if (c > 0x7E) return false;
            if (Character.isLetter(c)) asciiLetterCount++;
        }

        return asciiLetterCount >= Math.max(1, s.length() / 2);
    }
}
