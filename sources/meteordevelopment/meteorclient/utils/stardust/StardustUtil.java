package meteordevelopment.meteorclient.utils.stardust;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.netty.util.internal.ThreadLocalRandom;
import java.io.File;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.AutoReconnect;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.world.Dimension;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_1268;
import net.minecraft.class_1306;
import net.minecraft.class_1659;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2558;
import net.minecraft.class_2583;
import net.minecraft.class_2797;
import net.minecraft.class_2803;
import net.minecraft.class_2824;
import net.minecraft.class_2828;
import net.minecraft.class_2868;
import net.minecraft.class_2886;
import net.minecraft.class_310;
import net.minecraft.class_3515;
import net.minecraft.class_7469;
import net.minecraft.class_8791;
import net.minecraft.class_9296;
import net.minecraft.class_9334;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/stardust/StardustUtil.class */
public class StardustUtil {
    public static final boolean XAERO_AVAILABLE;
    private static final class_1799[] discIcons;
    private static final class_1799[] doorIcons;
    private static final class_1799[] menuIcons;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/stardust/StardustUtil$IllegalDisconnectMethod.class */
    public enum IllegalDisconnectMethod {
        Slot,
        Chat,
        Interact,
        Movement,
        SequenceBreak,
        InvalidSettings
    }

    static {
        XAERO_AVAILABLE = FabricLoader.getInstance().isModLoaded("xaeroworldmap") && FabricLoader.getInstance().isModLoaded("xaerominimap");
        discIcons = new class_1799[]{class_1802.field_38973.method_7854(), class_1802.field_8731.method_7854(), class_1802.field_8144.method_7854(), class_1802.field_8075.method_7854(), class_1802.field_8502.method_7854(), class_1802.field_8534.method_7854(), class_1802.field_8834.method_7854(), class_1802.field_8355.method_7854(), class_1802.field_8806.method_7854(), class_1802.field_8623.method_7854(), class_1802.field_8065.method_7854(), class_1802.field_44705.method_7854(), class_1802.field_8425.method_7854(), class_1802.field_8344.method_7854(), class_1802.field_23984.method_7854(), class_1802.field_51628.method_7854(), class_1802.field_51630.method_7854(), class_1802.field_35358.method_7854(), class_1802.field_51629.method_7854()};
        doorIcons = new class_1799[]{class_1802.field_8691.method_7854(), class_1802.field_8594.method_7854(), class_1802.field_8438.method_7854(), class_1802.field_40222.method_7854(), class_1802.field_42705.method_7854(), class_1802.field_8199.method_7854(), class_1802.field_8758.method_7854(), class_1802.field_8165.method_7854(), class_1802.field_22009.method_7854(), class_1802.field_46982.method_7854(), class_1802.field_22010.method_7854(), class_1802.field_37528.method_7854(), class_1802.field_8517.method_7854(), class_1802.field_46983.method_7854(), class_1802.field_46985.method_7854(), class_1802.field_46984.method_7854()};
        menuIcons = new class_1799[]{class_1802.field_17534.method_7854(), class_1802.field_8849.method_7854(), class_1802.field_8542.method_7854(), class_1802.field_39057.method_7854(), class_1802.field_20414.method_7854(), class_1802.field_23256.method_7854(), class_1802.field_8840.method_7854(), class_1802.field_8204.method_7854(), class_1802.field_17511.method_7854(), class_1802.field_8618.method_7854(), class_1802.field_8137.method_7854(), class_1802.field_17515.method_7854(), class_1802.field_42696.method_7854(), class_1802.field_22012.method_7854(), class_1802.field_42709.method_7854(), class_1802.field_49098.method_7854(), class_1802.field_8360.method_7854(), class_1802.field_8427.method_7854(), class_1802.field_42688.method_7854(), class_1802.field_8693.method_7854(), class_1802.field_8361.method_7854(), class_1802.field_8639.method_7854(), class_1802.field_8288.method_7854(), class_1802.field_8548.method_7854(), class_1802.field_27069.method_7854(), class_1802.field_28651.method_7854(), class_1802.field_8520.method_7854(), class_1802.field_23847.method_7854(), class_1802.field_50139.method_7854(), class_1802.field_43210.method_7854(), class_1802.field_8829.method_7854(), class_1802.field_8367.method_7854(), class_1802.field_43211.method_7854(), class_1802.field_41952.method_7854(), discIcons[ThreadLocalRandom.current().nextInt(discIcons.length)], doorIcons[ThreadLocalRandom.current().nextInt(doorIcons.length)], getCustomIcons()[ThreadLocalRandom.current().nextInt(getCustomIcons().length)]};
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/stardust/StardustUtil$RainbowColor.class */
    public enum RainbowColor {
        Reds(new String[]{"§c", "§4"}),
        Yellows(new String[]{"§e", "§6"}),
        Greens(new String[]{"§a", "§2"}),
        Cyans(new String[]{"§b", "§3"}),
        Blues(new String[]{"§9", "§1"}),
        Purples(new String[]{"§d", "§5"});

        public final String[] labels;

        RainbowColor(String[] labels) {
            this.labels = labels;
        }

        public static RainbowColor getFirst() {
            return values()[ThreadLocalRandom.current().nextInt(values().length)];
        }

        /* JADX INFO: Thrown type has an unknown type hierarchy: java.lang.MatchException */
        public static RainbowColor getNext(RainbowColor previous) throws MatchException {
            switch (previous) {
                case Reds:
                    return Yellows;
                case Yellows:
                    return Greens;
                case Greens:
                    return Cyans;
                case Cyans:
                    return Blues;
                case Blues:
                    return Purples;
                case Purples:
                    return Reds;
                default:
                    throw new MatchException((String) null, (Throwable) null);
            }
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/stardust/StardustUtil$TextColor.class */
    public enum TextColor {
        Black("§0"),
        White("§f"),
        Gray("§8"),
        Light_Gray("§7"),
        Dark_Green("§2"),
        Green("§a"),
        Dark_Aqua("§3"),
        Aqua("§b"),
        Dark_Blue("§1"),
        Blue("§9"),
        Dark_Red("§4"),
        Red("§c"),
        Dark_Purple("§5"),
        Purple("§d"),
        Gold("§6"),
        Yellow("§e"),
        Random("");

        public final String label;

        TextColor(String label) {
            this.label = label;
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/stardust/StardustUtil$TextFormat.class */
    public enum TextFormat {
        Plain(""),
        Italic("§o"),
        Bold("§l"),
        Underline("§n"),
        Strikethrough("§m"),
        Obfuscated("§k");

        public final String label;

        TextFormat(String label) {
            this.label = label;
        }
    }

    public static String rCC() {
        String color = "§7";
        TextColor[] colors = TextColor.values();
        while (true) {
            if (color.equals("§0") || color.equals("§8") || color.equals("§7")) {
                int luckyIndex = ThreadLocalRandom.current().nextInt(colors.length);
                color = colors[luckyIndex].label;
            } else {
                return color;
            }
        }
    }

    public static class_1799 chooseMenuIcon() {
        int luckyIndex = ThreadLocalRandom.current().nextInt(menuIcons.length);
        return menuIcons[luckyIndex];
    }

    private static class_1799[] getCustomIcons() {
        String currentPlayerHeadTexture;
        Optional<Property> currentPlayerProfileProperties = MeteorClient.mc.method_53462().getProperties().get("textures").stream().findFirst();
        if (currentPlayerProfileProperties.isPresent()) {
            currentPlayerHeadTexture = currentPlayerProfileProperties.get().value();
        } else {
            currentPlayerHeadTexture = "ewogICJ0aW1lc3RhbXAiIDogMTcyODQ5NzQxNzUwNCwKICAicHJvZmlsZUlkIiA6ICJkMDUwMzNmYzM3N2Q0OGU1ODFiMGJhYTY0NDBmNTIyOCIsCiAgInByb2ZpbGVOYW1lIiA6ICJQYXVsc3RldmUwMDciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTk1YmQzOWQ5M2ZiYjI4NGVhNGEzYmJiMTljNzRlNTUxOGQwODRiNmZiMGQ5YjE1ZWQ2YzU2NzdmMDhkY2FhYyIKICAgIH0KICB9Cn0=";
        }
        String[] playerHeadTextures = {currentPlayerHeadTexture, "ewogICJ0aW1lc3RhbXAiIDogMTcyODQwNzM3MDc3MiwKICAicHJvZmlsZUlkIiA6ICJjZTA5ODE3NzBkMjc0NmY1YTM3ODUxODg5NzcxYmEyNyIsCiAgInByb2ZpbGVOYW1lIiA6ICIweFRhcyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8yZGNlNGNlNWVhOWJjNWI1OTI1MmJlNDk1YTA5ZTQ0ZWFmMzc5NmRmNDY5OTU2MTdmZGQ4ZjFmMTBkNjU0ZjQyIgogICAgfQogIH0KfQ==", "eyJ0aW1lc3RhbXAiOjE0MTYwOTQxOTU4NTYsInByb2ZpbGVJZCI6IjBmNzVhODFkNzBlNTQzYzViODkyZjMzYzUyNDI4NGYyIiwicHJvZmlsZU5hbWUiOiJwb3Bib2IiLCJpc1B1YmxpYyI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzEyNTY4ODQ4NWI3MjUxMWFmOWY4NzVjZjQ4NjlmNjYxOTkwNWU2ZjJjNzc3NGIyMjYxNTJjYTY3ODIzODFlNiJ9fX0=", "eyJ0aW1lc3RhbXAiOjE0MTYwOTQxOTUxOTUsInByb2ZpbGVJZCI6IjY4YjFiYjExY2ZhMzRlMTZhMDFkYjZkZGRhMGExMDgzIiwicHJvZmlsZU5hbWUiOiJQeXJvYnl0ZSIsImlzUHVibGljIjp0cnVlLCJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzhjZTMwODMxYjU1YTI0MTFjMGYzMTI2ZDVhNThlMzE2NDZkNGE4YjZmMzYxZjcyMzc5ZGY0ZTY5OTE0OTkifX19", "ewogICJ0aW1lc3RhbXAiIDogMTcyODUwMDk2NjEwMywKICAicHJvZmlsZUlkIiA6ICI4ZDNmYTEyMmFjNGI0YjM1OGI1MzM5Mjc5NGJkZDU2MSIsCiAgInByb2ZpbGVOYW1lIiA6ICJUaGVTZW5wYWlPZjJiMnQiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTcxNTFkNzliMDQzZWY5N2FkMjhhMjc5NDVmODY3OGRmMmE3OGU2NGE1MmQxYzkzMDgwNTdhMjFmMDQyMDNlNCIKICAgIH0KICB9Cn0=", "eyJ0aW1lc3RhbXAiOjE0MTYwOTQxOTU2NjIsInByb2ZpbGVJZCI6IjhmMmNlNDUzY2VmMjRiM2ViNjg2ZGMyMWI1MTlhMGExIiwicHJvZmlsZU5hbWUiOiJIYXVzZW1hc3RlciIsImlzUHVibGljIjp0cnVlLCJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGJiY2IyZTE5OTdjN2NiMWJkZjU2MTNkMTMyZWVjNmQ2NzEzM2EyMTYyMWUwZmFlMTU3YTZhZDhmOWIyIn19fQ==", "eyJ0aW1lc3RhbXAiOjE0MTYwOTQxOTUxOTMsInByb2ZpbGVJZCI6IjdmMTk3NjE4MzJjMjQ4NzY4NDFiY2VhMjliZDU4Y2FlIiwicHJvZmlsZU5hbWUiOiJKYWNrdGhlcmlwcGEiLCJpc1B1YmxpYyI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzExYjk0OWE2MWZhNGNjOGZmZjNkM2I0OTY4MmQyZjk2ZjQxMThmOTI4ZDg2MjIyMmVmNjU2ZTMyYTVmMTIifX19", "eyJ0aW1lc3RhbXAiOjE0MDY0MTc0NTE1MDgsInByb2ZpbGVJZCI6ImE0YTVlYmM0OWY0ZTQ3OTVhMjUzN2I4YjA1M2ZiMTdmIiwicHJvZmlsZU5hbWUiOiJDeXRvdG94aWNUY2VsbCIsImlzUHVibGljIjp0cnVlLCJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTlkMWU2YzRmNjFkZmNmZGE2NDE3MjJmNjU3NzJiMTI3YmI0NDFkMGViMjU4YTM2Y2MxOTEzYmU3NTkyNGIxIn19fQ=="};
        class_1799 playerHead = new class_1799(class_1802.field_8575);
        GameProfile profile = new GameProfile(UUID.randomUUID(), "Stardust");
        class_9296 profileComponent = new class_9296(profile);
        profileComponent.comp_2412().put("textures", new Property("textures", playerHeadTextures[ThreadLocalRandom.current().nextInt(playerHeadTextures.length)], ""));
        playerHead.method_57379(class_9334.field_49617, profileComponent);
        class_1799 enchantedPick = new class_1799(ThreadLocalRandom.current().nextInt(2) == 0 ? class_1802.field_8377 : class_1802.field_22024);
        enchantedPick.method_57379(class_9334.field_49641, true);
        class_1799[] enchantedGlass = {class_1802.field_8280.method_7854(), class_1802.field_8636.method_7854(), class_1802.field_8685.method_7854(), class_1802.field_8340.method_7854(), class_1802.field_8770.method_7854(), class_1802.field_8483.method_7854(), class_1802.field_8410.method_7854(), class_1802.field_8869.method_7854()};
        for (class_1799 g : enchantedGlass) {
            g.method_57379(class_9334.field_49641, true);
        }
        class_1799 cgiElytra = new class_1799(class_1802.field_8833);
        cgiElytra.method_57379(class_9334.field_49641, true);
        class_1799 sword32k = new class_1799(ThreadLocalRandom.current().nextInt(2) == 0 ? class_1802.field_8802 : class_1802.field_8091);
        sword32k.method_57379(class_9334.field_49641, true);
        class_1799 illegalBow = new class_1799(class_1802.field_8102);
        illegalBow.method_57379(class_9334.field_49641, true);
        class_1799 bindingPumpkin = new class_1799(class_1802.field_17519);
        bindingPumpkin.method_57379(class_9334.field_49641, true);
        class_1799 ripTridentFly = new class_1799(class_1802.field_8547);
        ripTridentFly.method_57379(class_9334.field_49641, true);
        return new class_1799[]{playerHead, enchantedPick, sword32k, illegalBow, bindingPumpkin, cgiElytra, ripTridentFly, enchantedGlass[ThreadLocalRandom.current().nextInt(enchantedGlass.length)]};
    }

    public static boolean checkOrCreateFile(class_310 mc, String fileName) {
        File file = FabricLoader.getInstance().getGameDir().resolve(fileName).toFile();
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    if (mc.field_1724 != null) {
                        MsgUtil.sendMsg("Created " + file.getName() + " in your meteor-client folder.");
                        class_2583 style = class_2583.field_24360.method_10958(new class_2558.class_10607(file.getAbsolutePath()));
                        MsgUtil.sendMsg("Click §2§lhere §r§7to open the file.", style);
                        return true;
                    }
                    return true;
                }
                return false;
            } catch (Exception err) {
                LogUtil.error("Error creating " + file.getAbsolutePath() + "! - Why:\n" + String.valueOf(err), "StardustUtil#checkOrCreateFile");
                return false;
            }
        }
        return true;
    }

    public static void openFile(String fileName) {
        File file = FabricLoader.getInstance().getGameDir().resolve(fileName).toFile();
        try {
            Runtime runtime = Runtime.getRuntime();
            if (System.getenv("OS") == null) {
                return;
            }
            if (System.getenv("OS").contains("Windows")) {
                runtime.exec(new String[]{"rundll32", "url.dll,", "FileProtocolHandler", file.getAbsolutePath()});
            } else {
                runtime.exec(new String[]{"xdg-open", file.getAbsolutePath()});
            }
        } catch (Exception err) {
            MsgUtil.sendMsg("Failed to open " + file.getName() + "§c..!");
            LogUtil.error("Failed to open " + file.getAbsolutePath() + "! - Why:\n" + String.valueOf(err), "StardustUtil#openFile");
        }
    }

    public static boolean isIn2b2tQueue() {
        return MeteorClient.mc.field_1724 != null && MeteorClient.mc.method_1562() != null && PlayerUtils.getDimension().equals(Dimension.End) && MeteorClient.mc.field_1724.method_31549().field_7478 && MeteorClient.mc.method_1562().method_2880().size() <= 1;
    }

    public static void illegalDisconnect(boolean disableAutoReconnect, IllegalDisconnectMethod illegalDisconnectMethod) {
        if (Utils.canUpdate()) {
            if (disableAutoReconnect) {
                disableAutoReconnect();
            }
            class_2868 class_2803Var = null;
            switch (illegalDisconnectMethod) {
                case Slot:
                    class_2803Var = new class_2868(-69);
                    break;
                case Chat:
                    class_2803Var = new class_2797("§", Instant.now(), class_3515.class_7426.method_43531(), (class_7469) null, MeteorClient.mc.method_1562().getLastSeenMessagesCollector().method_46266().comp_1074());
                    break;
                case Interact:
                    class_2803Var = class_2824.method_34207(MeteorClient.mc.field_1724, false, class_1268.field_5808);
                    break;
                case Movement:
                    class_2803Var = new class_2828.class_2829(Double.NaN, 69.0d, Double.NaN, false, false);
                    break;
                case SequenceBreak:
                    class_2803Var = new class_2886(class_1268.field_5808, -420, 13.37f, 69.69f);
                    break;
                case InvalidSettings:
                    class_2803Var = new class_2803(new class_8791(MeteorClient.mc.field_1690.field_1883, -69, (class_1659) MeteorClient.mc.field_1690.method_42539().method_41753(), ((Boolean) MeteorClient.mc.field_1690.method_42427().method_41753()).booleanValue(), MeteorClient.mc.field_1690.method_53842().comp_1955(), (class_1306) MeteorClient.mc.field_1690.method_42552().method_41753(), MeteorClient.mc.field_1690.method_53842().comp_1957(), ((Boolean) MeteorClient.mc.field_1690.method_42441().method_41753()).booleanValue(), MeteorClient.mc.field_1690.method_53842().comp_2906()));
                    break;
            }
            if (class_2803Var != null) {
                MeteorClient.mc.method_1562().method_48296().invokeSendImmediately(class_2803Var, null, true);
            }
        }
    }

    public static void disableAutoReconnect() {
        Modules mods = Modules.get();
        if (mods == null) {
            return;
        }
        AutoReconnect atrc = (AutoReconnect) mods.get(AutoReconnect.class);
        if (atrc.isActive()) {
            atrc.toggle();
        }
    }
}
