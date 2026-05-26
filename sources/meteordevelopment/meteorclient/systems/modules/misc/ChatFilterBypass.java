package meteordevelopment.meteorclient.systems.modules.misc;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
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
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_5250;
import net.minecraft.class_5251;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/misc/ChatFilterBypass.class */
public class ChatFilterBypass extends Module {
    private final SettingGroup sgEncrypt;
    private final SettingGroup sgDecrypt;
    private final Setting<List<String>> encryptWords;
    private final Setting<Boolean> caseSensitive;
    private final Setting<Boolean> decryptIncoming;
    private final Setting<Boolean> showDecryptMarker;
    private final Setting<SettingColor> decryptColor;
    private final Setting<Integer> minTokenLength;
    private final Map<String, String> encodeCache;
    private final Map<String, Pattern> patternCache;

    public ChatFilterBypass() {
        super(Categories.Misc, "chat-encrypt", "Encrypts chosen words in your outgoing messages to Base64, and optionally decodes Base64 tokens in incoming chat.");
        this.sgEncrypt = this.settings.createGroup("Encrypt");
        this.sgDecrypt = this.settings.createGroup("Decrypt");
        this.encryptWords = this.sgEncrypt.add(new StringListSetting.Builder().name("words-to-encrypt").description("Words in your outgoing messages that will be replaced with their Base64 encoding.").defaultValue(List.of()).onChanged(v -> {
            rebuildCache();
        }).build());
        this.caseSensitive = this.sgEncrypt.add(new BoolSetting.Builder().name("case-sensitive").description("When off, 'hello' will also match 'Hello' and 'HELLO'.").defaultValue(true).onChanged(v2 -> {
            rebuildCache();
        }).build());
        this.decryptIncoming = this.sgDecrypt.add(new BoolSetting.Builder().name("decrypt-incoming").description("Scan incoming chat messages for Base64 tokens and replace them with their decoded text.").defaultValue(true).build());
        this.showDecryptMarker = this.sgDecrypt.add(new BoolSetting.Builder().name("show-decrypt-marker").description("Wraps decrypted words in [brackets] so you know they were encrypted.").defaultValue(true).build());
        this.decryptColor = this.sgDecrypt.add(new ColorSetting.Builder().name("decrypt-color").description("Color used to highlight decrypted words in chat.").defaultValue(new SettingColor(255, 0, 0, 255)).build());
        this.minTokenLength = this.sgDecrypt.add(new IntSetting.Builder().name("min-token-length").description("Minimum character length of a token before attempting Base64 decoding. Raise to reduce false positives.").defaultValue(8).min(4).sliderMax(24).build());
        this.encodeCache = new HashMap();
        this.patternCache = new HashMap();
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        rebuildCache();
    }

    @EventHandler
    private void onMessageSend(SendMessageEvent event) {
        if (this.mc.field_1724 == null || this.encodeCache.isEmpty()) {
            return;
        }
        String msg = event.message;
        for (Map.Entry<String, Pattern> entry : this.patternCache.entrySet()) {
            String encoded = this.encodeCache.get(entry.getKey());
            msg = entry.getValue().matcher(msg).replaceAll(encoded);
        }
        event.message = msg;
    }

    @EventHandler
    private void onMessageReceive(ReceiveMessageEvent event) {
        String raw;
        class_5250 newText;
        if (this.mc.field_1724 != null && this.decryptIncoming.get().booleanValue() && (raw = class_124.method_539(event.getMessage().getString())) != null && (newText = buildColoredText(raw)) != null) {
            event.setMessage(newText);
        }
    }

    private void rebuildCache() {
        this.encodeCache.clear();
        this.patternCache.clear();
        for (String word : this.encryptWords.get()) {
            if (word != null && !word.isBlank()) {
                String encoded = Base64.getEncoder().encodeToString(word.getBytes(StandardCharsets.UTF_8));
                this.encodeCache.put(word, encoded);
                int flags = this.caseSensitive.get().booleanValue() ? 0 : 2;
                Pattern pattern = Pattern.compile("(?<![\\w])" + Pattern.quote(word) + "(?![\\w])", flags);
                this.patternCache.put(word, pattern);
            }
        }
    }

    private class_5250 buildColoredText(String message) {
        BetterChat betterChat;
        String[] parts = message.split(" ", -1);
        class_5250 result = class_2561.method_43470("");
        boolean anyDecoded = false;
        SettingColor col = this.decryptColor.get();
        int rgb = (col.r << 16) | (col.g << 8) | col.b;
        class_2583 colorStyle = class_2583.field_24360.method_27703(class_5251.method_27717(rgb));
        String playerName = this.mc.field_1724 != null ? this.mc.field_1724.method_5477().getString() : null;
        BetterChat betterChat2 = (BetterChat) Modules.get().get(BetterChat.class);
        boolean doNameHighlight = (playerName == null || betterChat2 == null || !betterChat2.isActive()) ? false : true;
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            String decoded = tryDecode(part);
            if (i > 0) {
                result.method_10852(class_2561.method_43470(" ").method_10862(class_2583.field_24360));
            }
            if (decoded != null) {
                anyDecoded = true;
                String display = this.showDecryptMarker.get().booleanValue() ? "[" + decoded + "]" : decoded;
                result.method_10852(class_2561.method_43470(display).method_10862(colorStyle));
            } else if (doNameHighlight && part.contains(playerName)) {
                appendWithNameHighlight(result, part, playerName);
            } else {
                result.method_10852(class_2561.method_43470(part).method_10862(class_2583.field_24360));
            }
        }
        if (anyDecoded && (betterChat = (BetterChat) Modules.get().get(BetterChat.class)) != null && betterChat.isActive()) {
            result = betterChat.highlightListNames(result);
        }
        if (anyDecoded) {
            return result;
        }
        return null;
    }

    private void appendWithNameHighlight(class_5250 result, String token, String playerName) {
        BetterChat betterChat = (BetterChat) Modules.get().get(BetterChat.class);
        if (betterChat == null) {
            result.method_10852(class_2561.method_43470(token).method_10862(class_2583.field_24360));
            return;
        }
        SettingColor nhCol = betterChat.nameHighlightColor.get();
        int nhRgb = (nhCol.r << 16) | (nhCol.g << 8) | nhCol.b;
        class_2583 nameStyle = class_2583.field_24360.method_27703(class_5251.method_27717(nhRgb)).method_10982(true);
        result.method_10852(betterChat.applyHighlight(class_2561.method_43470(token), playerName, nameStyle));
    }

    private String tryDecode(String part) {
        if (!isBase64Candidate(part)) {
            return null;
        }
        try {
            String padded = part;
            int rem = padded.length() % 4;
            if (rem == 2) {
                padded = padded + "==";
            } else if (rem == 3) {
                padded = padded + "=";
            }
            byte[] bytes = Base64.getDecoder().decode(padded);
            String text = new String(bytes, StandardCharsets.UTF_8);
            if (isPrintable(text)) {
                return text;
            }
            return null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private boolean isBase64Candidate(String s) {
        if (s.length() >= this.minTokenLength.get().intValue() && s.length() % 4 == 0) {
            return s.matches("^[A-Za-z0-9+/]*={0,2}$");
        }
        return false;
    }

    private boolean isPrintable(String s) {
        if (s.isBlank() || s.length() > 64) {
            return false;
        }
        int asciiLetterCount = 0;
        for (char c : s.toCharArray()) {
            if ((c < ' ' && c != '\t' && c != '\n' && c != '\r') || c == 127 || c > '~') {
                return false;
            }
            if (Character.isLetter(c)) {
                asciiLetterCount++;
            }
        }
        return asciiLetterCount >= Math.max(1, s.length() / 2);
    }
}
