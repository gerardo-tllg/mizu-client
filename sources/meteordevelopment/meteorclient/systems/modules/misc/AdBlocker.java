package meteordevelopment.meteorclient.systems.modules.misc;

import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.StringListSetting;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.stardust.MsgUtil;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_2561;
import net.minecraft.class_2568;
import net.minecraft.class_7439;
import net.minecraft.class_7472;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/misc/AdBlocker.class */
public class AdBlocker extends Module {
    private final Setting<IgnoreStyle> ignoreStyle;
    private final Setting<List<String>> patterns;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/misc/AdBlocker$IgnoreStyle.class */
    public enum IgnoreStyle {
        None,
        Ignore,
        HardIgnore
    }

    public AdBlocker() {
        super(Categories.Misc, "AdBlocker", "Blocks advertisers in chat.");
        this.ignoreStyle = this.settings.getDefaultGroup().add(new EnumSetting.Builder().name("ignore-advertisers").description("Whether to ignore accounts which trigger the blocked patterns filter.").defaultValue(IgnoreStyle.Ignore).build());
        this.patterns = this.settings.getDefaultGroup().add(new StringListSetting.Builder().name("blocked-patterns").description("Chat messages matching any of these patterns will be blocked, and ignore preferences applied to the culprit.").defaultValue(List.of((Object[]) new String[]{"thishttp", "discord.com", "discord.gg", "gg/", "com/", "/invite/", "% off", ".store", "cheapest price", "cheapest kit", "cheap price", "cheap kit", "use code", "at checkout", "join now", "rusherhack.org", "nox2b", ".shop"})).build());
    }

    @EventHandler(priority = 200)
    private void onPacketReceive(PacketEvent.Receive event) {
        String cmd;
        if (this.mc.method_1562() == null) {
            return;
        }
        class_7439 class_7439Var = event.packet;
        if (class_7439Var instanceof class_7439) {
            class_7439 packet = class_7439Var;
            if (packet.comp_763() == null) {
                return;
            }
            String content = packet.comp_763().getString();
            for (String pattern : this.patterns.get()) {
                if (content.toLowerCase().contains(pattern.toLowerCase())) {
                    event.cancel();
                    if (!this.ignoreStyle.get().equals(IgnoreStyle.None)) {
                        String name = getNameFromMessage(content);
                        if (this.ignoreStyle.get().equals(IgnoreStyle.Ignore)) {
                            cmd = "ignore";
                        } else {
                            cmd = "ignorehard";
                        }
                        if (name.isBlank()) {
                            List<String> responsible = new ArrayList<>();
                            extractNamesFromDeathMessage(packet.comp_763(), responsible);
                            for (String culprit : responsible) {
                                if (this.chatFeedback) {
                                    MsgUtil.sendModuleMsg("Ignoring death-message advertiser \"§c" + culprit + "§7\"§a..!", this.name);
                                }
                                this.mc.method_1562().method_48296().invokeSendImmediately(new class_7472("ignoredeathmsgs" + " " + culprit), null, true);
                            }
                            return;
                        }
                        this.mc.method_1562().method_48296().invokeSendImmediately(new class_7472(cmd + " " + name), null, true);
                        return;
                    }
                    return;
                }
            }
        }
    }

    private String getNameFromMessage(String message) {
        String name = "";
        String[] parts = message.split(" ");
        if (parts.length >= 3 && parts[1].equals("whispers:")) {
            name = parts[0];
        } else if (parts[0].startsWith("<") && parts[0].endsWith(">")) {
            name = parts[0].substring(1, parts[0].length() - 1);
        }
        return name;
    }

    private void extractNamesFromDeathMessage(class_2561 msg, List<String> names) {
        class_2568.class_10613 class_10613VarMethod_10969 = msg.method_10866().method_10969();
        if (class_10613VarMethod_10969 instanceof class_2568.class_10613) {
            class_2568.class_10613 showText = class_10613VarMethod_10969;
            class_2561 text = showText.comp_3510();
            if (text != null && text.getString().startsWith("Message ")) {
                names.add(text.getString().substring(8).trim());
            }
        }
        for (class_2561 sibling : msg.method_10855()) {
            extractNamesFromDeathMessage(sibling, names);
        }
    }
}
