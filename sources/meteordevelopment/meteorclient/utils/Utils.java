package meteordevelopment.meteorclient.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Reference2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javassist.bytecode.Opcode;
import javassist.compiler.TokenId;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.mixin.ContainerComponentAccessor;
import meteordevelopment.meteorclient.mixin.ReloadStateAccessor;
import meteordevelopment.meteorclient.settings.StatusEffectAmplifierMapSetting;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.BetterTooltips;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.meteorclient.utils.misc.Names;
import meteordevelopment.meteorclient.utils.player.EChestMemory;
import meteordevelopment.meteorclient.utils.render.PeekScreen;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.world.BlockEntityIterator;
import meteordevelopment.meteorclient.utils.world.ChunkIterator;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_10366;
import net.minecraft.class_1291;
import net.minecraft.class_1297;
import net.minecraft.class_1747;
import net.minecraft.class_1753;
import net.minecraft.class_1764;
import net.minecraft.class_1767;
import net.minecraft.class_1771;
import net.minecraft.class_1776;
import net.minecraft.class_1779;
import net.minecraft.class_1787;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1803;
import net.minecraft.class_1823;
import net.minecraft.class_1828;
import net.minecraft.class_1835;
import net.minecraft.class_1887;
import net.minecraft.class_1890;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2371;
import net.minecraft.class_243;
import net.minecraft.class_2480;
import net.minecraft.class_2487;
import net.minecraft.class_2499;
import net.minecraft.class_2520;
import net.minecraft.class_2586;
import net.minecraft.class_2791;
import net.minecraft.class_3532;
import net.minecraft.class_437;
import net.minecraft.class_442;
import net.minecraft.class_500;
import net.minecraft.class_526;
import net.minecraft.class_5321;
import net.minecraft.class_6880;
import net.minecraft.class_9279;
import net.minecraft.class_9304;
import net.minecraft.class_9323;
import net.minecraft.class_9334;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.lwjgl.glfw.GLFW;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/Utils.class */
public class Utils {
    public static boolean isReleasingTrident;
    public static double frameTime;
    public static class_437 screenToOpen;
    public static final Pattern FILE_NAME_INVALID_CHARS_PATTERN = Pattern.compile("[\\s\\\\/:*?\"<>|]");
    public static final Color WHITE = new Color(255, 255, 255);
    private static final Random random = new Random();
    public static boolean rendering3D = true;

    private Utils() {
    }

    @PreInit
    public static void init() {
        MeteorClient.EVENT_BUS.subscribe(Utils.class);
    }

    @EventHandler
    private static void onTick(TickEvent.Post event) {
        if (screenToOpen != null && MeteorClient.mc.field_1755 == null) {
            MeteorClient.mc.method_1507(screenToOpen);
            screenToOpen = null;
        }
    }

    public static class_243 getPlayerSpeed() {
        if (MeteorClient.mc.field_1724 == null) {
            return class_243.field_1353;
        }
        double tX = MeteorClient.mc.field_1724.method_23317() - MeteorClient.mc.field_1724.field_6014;
        double tY = MeteorClient.mc.field_1724.method_23318() - MeteorClient.mc.field_1724.field_6036;
        double tZ = MeteorClient.mc.field_1724.method_23321() - MeteorClient.mc.field_1724.field_5969;
        Timer timer = (Timer) Modules.get().get(Timer.class);
        if (timer.isActive()) {
            tX *= timer.getMultiplier();
            tY *= timer.getMultiplier();
            tZ *= timer.getMultiplier();
        }
        return new class_243(tX * 20.0d, tY * 20.0d, tZ * 20.0d);
    }

    public static String getWorldTime() {
        if (MeteorClient.mc.field_1687 == null) {
            return "00:00";
        }
        int ticks = ((int) (MeteorClient.mc.field_1687.method_8532() % 24000)) + 6000;
        if (ticks > 24000) {
            ticks -= 24000;
        }
        return String.format("%02d:%02d", Integer.valueOf(ticks / 1000), Integer.valueOf((int) ((((double) (ticks % 1000)) / 1000.0d) * 60.0d)));
    }

    public static Iterable<class_2791> chunks(boolean onlyWithLoadedNeighbours) {
        return () -> {
            return new ChunkIterator(onlyWithLoadedNeighbours);
        };
    }

    public static Iterable<class_2791> chunks() {
        return chunks(false);
    }

    public static Iterable<class_2586> blockEntities() {
        return BlockEntityIterator::new;
    }

    public static void getEnchantments(class_1799 itemStack, Object2IntMap<class_6880<class_1887>> enchantments) {
        Set<Object2IntMap.Entry<class_6880<class_1887>>> setMethod_57539;
        enchantments.clear();
        if (!itemStack.method_7960()) {
            if (itemStack.method_7909() == class_1802.field_8598) {
                setMethod_57539 = ((class_9304) itemStack.method_58695(class_9334.field_49643, class_9304.field_49385)).method_57539();
            } else {
                setMethod_57539 = itemStack.method_58657().method_57539();
            }
            Set<Object2IntMap.Entry<class_6880<class_1887>>> itemEnchantments = setMethod_57539;
            for (Object2IntMap.Entry<class_6880<class_1887>> entry : itemEnchantments) {
                enchantments.put((class_6880) entry.getKey(), entry.getIntValue());
            }
        }
    }

    public static int getEnchantmentLevel(class_1799 itemStack, class_5321<class_1887> enchantment) {
        if (itemStack.method_7960()) {
            return 0;
        }
        Object2IntArrayMap object2IntArrayMap = new Object2IntArrayMap();
        getEnchantments(itemStack, object2IntArrayMap);
        return getEnchantmentLevel((Object2IntMap<class_6880<class_1887>>) object2IntArrayMap, enchantment);
    }

    public static int getEnchantmentLevel(Object2IntMap<class_6880<class_1887>> itemEnchantments, class_5321<class_1887> enchantment) {
        ObjectIterator it = Object2IntMaps.fastIterable(itemEnchantments).iterator();
        while (it.hasNext()) {
            Object2IntMap.Entry<class_6880<class_1887>> entry = (Object2IntMap.Entry) it.next();
            if (((class_6880) entry.getKey()).method_40225(enchantment)) {
                return entry.getIntValue();
            }
        }
        return 0;
    }

    @SafeVarargs
    public static boolean hasEnchantments(class_1799 itemStack, class_5321<class_1887>... enchantments) {
        if (itemStack.method_7960()) {
            return false;
        }
        Object2IntArrayMap object2IntArrayMap = new Object2IntArrayMap();
        getEnchantments(itemStack, object2IntArrayMap);
        for (class_5321<class_1887> enchantment : enchantments) {
            if (!hasEnchantment((Object2IntMap<class_6880<class_1887>>) object2IntArrayMap, enchantment)) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasEnchantment(class_1799 itemStack, class_5321<class_1887> enchantmentKey) {
        if (itemStack.method_7960()) {
            return false;
        }
        Object2IntArrayMap object2IntArrayMap = new Object2IntArrayMap();
        getEnchantments(itemStack, object2IntArrayMap);
        return hasEnchantment((Object2IntMap<class_6880<class_1887>>) object2IntArrayMap, enchantmentKey);
    }

    private static boolean hasEnchantment(Object2IntMap<class_6880<class_1887>> itemEnchantments, class_5321<class_1887> enchantmentKey) {
        ObjectIterator it = itemEnchantments.keySet().iterator();
        while (it.hasNext()) {
            class_6880<class_1887> enchantment = (class_6880) it.next();
            if (enchantment.method_40225(enchantmentKey)) {
                return true;
            }
        }
        return false;
    }

    public static int getRenderDistance() {
        return Math.max(((Integer) MeteorClient.mc.field_1690.method_42503().method_41753()).intValue(), MeteorClient.mc.method_1562().getChunkLoadDistance());
    }

    public static int getWindowWidth() {
        return MeteorClient.mc.method_22683().method_4489();
    }

    public static int getWindowHeight() {
        return MeteorClient.mc.method_22683().method_4506();
    }

    public static void unscaledProjection() {
        RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0.0f, MeteorClient.mc.method_22683().method_4489(), MeteorClient.mc.method_22683().method_4506(), 0.0f, 1000.0f, 21000.0f), class_10366.field_54954);
        rendering3D = false;
    }

    public static void scaledProjection() {
        RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0.0f, (float) (((double) MeteorClient.mc.method_22683().method_4489()) / MeteorClient.mc.method_22683().method_4495()), (float) (((double) MeteorClient.mc.method_22683().method_4506()) / MeteorClient.mc.method_22683().method_4495()), 0.0f, 1000.0f, 21000.0f), class_10366.field_54953);
        rendering3D = true;
    }

    public static class_243 vec3d(class_2338 pos) {
        return new class_243(pos.method_10263(), pos.method_10264(), pos.method_10260());
    }

    public static boolean openContainer(class_1799 itemStack, class_1799[] contents, boolean pause) {
        if (hasItems(itemStack) || itemStack.method_7909() == class_1802.field_8466) {
            getItemsInContainerItem(itemStack, contents);
            if (!pause) {
                MeteorClient.mc.method_1507(new PeekScreen(itemStack, contents));
                return true;
            }
            screenToOpen = new PeekScreen(itemStack, contents);
            return true;
        }
        return false;
    }

    public static void getItemsInContainerItem(class_1799 itemStack, class_1799[] items) {
        class_2499 nbt3;
        if (itemStack.method_7909() == class_1802.field_8466) {
            for (int i = 0; i < EChestMemory.ITEMS.size(); i++) {
                items[i] = (class_1799) EChestMemory.ITEMS.get(i);
            }
            return;
        }
        Arrays.fill(items, class_1799.field_8037);
        class_9323 components = itemStack.method_57353();
        if (components.method_57832(class_9334.field_49622)) {
            ContainerComponentAccessor container = (ContainerComponentAccessor) components.method_58694(class_9334.field_49622);
            class_2371<class_1799> stacks = container.getStacks();
            for (int i2 = 0; i2 < stacks.size(); i2++) {
                if (i2 >= 0 && i2 < items.length) {
                    items[i2] = (class_1799) stacks.get(i2);
                }
            }
            return;
        }
        if (components.method_57832(class_9334.field_49611)) {
            class_9279 nbt2 = (class_9279) components.method_58694(class_9334.field_49611);
            if (!nbt2.method_57450("Items") || (nbt3 = nbt2.method_57463().method_10580("Items")) == null) {
                return;
            }
            for (int i3 = 0; i3 < nbt3.size(); i3++) {
                Optional<class_2487> compound = nbt3.method_10602(i3);
                if (!compound.isEmpty()) {
                    Optional<Byte> slot = compound.get().method_10571("Slot");
                    if (!slot.isEmpty() && slot.get().byteValue() >= 0 && slot.get().byteValue() < items.length) {
                        Optional<class_1799> stack = class_1799.method_57360(MeteorClient.mc.field_1724.method_56673(), (class_2520) compound.get());
                        if (stack.isEmpty()) {
                            stack = Optional.of(class_1799.field_8037);
                        }
                        items[slot.get().byteValue()] = stack.get();
                    }
                }
            }
        }
    }

    public static Color getShulkerColor(class_1799 shulkerItem) {
        class_1747 class_1747VarMethod_7909 = shulkerItem.method_7909();
        if (class_1747VarMethod_7909 instanceof class_1747) {
            class_1747 blockItem = class_1747VarMethod_7909;
            class_2480 class_2480VarMethod_7711 = blockItem.method_7711();
            if (class_2480VarMethod_7711 == class_2246.field_10443) {
                return BetterTooltips.ECHEST_COLOR;
            }
            if (class_2480VarMethod_7711 instanceof class_2480) {
                class_2480 shulkerBlock = class_2480VarMethod_7711;
                class_1767 dye = shulkerBlock.method_10528();
                if (dye == null) {
                    return WHITE;
                }
                int color = dye.method_7787();
                return new Color((color >> 16) & 255, (color >> 8) & 255, color & 255, 255);
            }
        }
        return WHITE;
    }

    public static boolean hasItems(class_1799 itemStack) {
        ContainerComponentAccessor container = (ContainerComponentAccessor) itemStack.method_58694(class_9334.field_49622);
        if (container != null && !container.getStacks().isEmpty()) {
            return true;
        }
        class_2487 compoundTag = ((class_9279) itemStack.method_58695(class_9334.field_49611, class_9279.field_49302)).method_57463();
        return compoundTag != null && compoundTag.method_10545("Items");
    }

    public static Reference2IntMap<class_1291> createStatusEffectMap() {
        return new Reference2IntArrayMap(StatusEffectAmplifierMapSetting.EMPTY_STATUS_EFFECT_MAP);
    }

    public static String getEnchantSimpleName(class_6880<class_1887> enchantment, int length) {
        String name = Names.get(enchantment);
        return name.length() > length ? name.substring(0, length) : name;
    }

    public static boolean searchTextDefault(String text, String filter, boolean caseSensitive) {
        return searchInWords(text, filter) > 0 || searchLevenshteinDefault(text, filter, caseSensitive) < text.length() / 2;
    }

    public static int searchLevenshteinDefault(String text, String filter, boolean caseSensitive) {
        return levenshteinDistance(caseSensitive ? filter : filter.toLowerCase(Locale.ROOT), caseSensitive ? text : text.toLowerCase(Locale.ROOT), 1, 8, 8);
    }

    public static int searchInWords(String text, String filter) {
        if (filter.isEmpty()) {
            return 1;
        }
        int wordsFound = 0;
        String text2 = text.toLowerCase(Locale.ROOT);
        String[] words = filter.toLowerCase(Locale.ROOT).split(" ");
        for (String word : words) {
            if (!text2.contains(word)) {
                return 0;
            }
            wordsFound += StringUtils.countMatches(text2, word);
        }
        return wordsFound;
    }

    public static int levenshteinDistance(String from, String to, int insCost, int subCost, int delCost) {
        int textLength = from.length();
        int filterLength = to.length();
        if (textLength == 0) {
            return filterLength * insCost;
        }
        if (filterLength == 0) {
            return textLength * delCost;
        }
        int[][] d = new int[textLength + 1][filterLength + 1];
        for (int i = 0; i <= textLength; i++) {
            d[i][0] = i * delCost;
        }
        for (int j = 0; j <= filterLength; j++) {
            d[0][j] = j * insCost;
        }
        for (int i2 = 1; i2 <= textLength; i2++) {
            for (int j2 = 1; j2 <= filterLength; j2++) {
                int sCost = d[i2 - 1][j2 - 1] + (from.charAt(i2 - 1) == to.charAt(j2 - 1) ? 0 : subCost);
                int dCost = d[i2 - 1][j2] + delCost;
                int iCost = d[i2][j2 - 1] + insCost;
                d[i2][j2] = Math.min(Math.min(dCost, iCost), sCost);
            }
        }
        return d[textLength][filterLength];
    }

    public static double squaredDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dX = x2 - x1;
        double dY = y2 - y1;
        double dZ = z2 - z1;
        return (dX * dX) + (dY * dY) + (dZ * dZ);
    }

    public static double distance(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dX = x2 - x1;
        double dY = y2 - y1;
        double dZ = z2 - z1;
        return Math.sqrt((dX * dX) + (dY * dY) + (dZ * dZ));
    }

    public static String getFileWorldName() {
        return FILE_NAME_INVALID_CHARS_PATTERN.matcher(getWorldName()).replaceAll("_");
    }

    public static String getWorldName() {
        if (MeteorClient.mc.method_1542()) {
            if (MeteorClient.mc.field_1687 == null) {
                return "";
            }
            if (MeteorClient.mc.method_1576() == null) {
                return "FAILED_BECAUSE_LEFT_WORLD";
            }
            File folder = MeteorClient.mc.method_1576().getSession().method_27424(MeteorClient.mc.field_1687.method_27983()).toFile();
            if (folder.toPath().relativize(MeteorClient.mc.field_1697.toPath()).getNameCount() != 2) {
                folder = folder.getParentFile();
            }
            return folder.getName();
        }
        if (MeteorClient.mc.method_1558() != null) {
            return MeteorClient.mc.method_1558().method_52811() ? "realms" : MeteorClient.mc.method_1558().field_3761;
        }
        return "";
    }

    public static String nameToTitle(String name) {
        return (String) Arrays.stream(name.split("-")).map(StringUtils::capitalize).collect(Collectors.joining(" "));
    }

    public static String titleToName(String title) {
        return title.replace(" ", "-").toLowerCase(Locale.ROOT);
    }

    public static String getKeyName(int key) {
        switch (key) {
            case -1:
                return "Unknown";
            case 32:
                return "Space";
            case 39:
                return "Apostrophe";
            case Opcode.IADD /* 96 */:
                return "Grave Accent";
            case Opcode.IF_ICMPLT /* 161 */:
                return "World 1";
            case Opcode.IF_ICMPGE /* 162 */:
                return "World 2";
            case 256:
                return "Esc";
            case 257:
                return "Enter";
            case 258:
                return "Tab";
            case 259:
                return "Backspace";
            case 260:
                return "Insert";
            case 261:
                return "Delete";
            case 262:
                return "Arrow Right";
            case 263:
                return "Arrow Left";
            case 264:
                return "Arrow Down";
            case 265:
                return "Arrow Up";
            case 266:
                return "Page Up";
            case 267:
                return "Page Down";
            case 268:
                return "Home";
            case 269:
                return "End";
            case 280:
                return "Caps Lock";
            case 282:
                return "Num Lock";
            case 283:
                return "Print Screen";
            case 284:
                return "Pause";
            case 290:
                return "F1";
            case 291:
                return "F2";
            case 292:
                return "F3";
            case 293:
                return "F4";
            case 294:
                return "F5";
            case 295:
                return "F6";
            case 296:
                return "F7";
            case 297:
                return "F8";
            case 298:
                return "F9";
            case 299:
                return "F10";
            case TokenId.ABSTRACT /* 300 */:
                return "F11";
            case TokenId.BOOLEAN /* 301 */:
                return "F12";
            case TokenId.BREAK /* 302 */:
                return "F13";
            case TokenId.BYTE /* 303 */:
                return "F14";
            case TokenId.CASE /* 304 */:
                return "F15";
            case TokenId.CATCH /* 305 */:
                return "F16";
            case TokenId.CHAR /* 306 */:
                return "F17";
            case TokenId.CLASS /* 307 */:
                return "F18";
            case TokenId.CONST /* 308 */:
                return "F19";
            case TokenId.CONTINUE /* 309 */:
                return "F20";
            case TokenId.DEFAULT /* 310 */:
                return "F21";
            case TokenId.DO /* 311 */:
                return "F22";
            case TokenId.DOUBLE /* 312 */:
                return "F23";
            case TokenId.ELSE /* 313 */:
                return "F24";
            case TokenId.EXTENDS /* 314 */:
                return "F25";
            case TokenId.STATIC /* 335 */:
                return "Numpad Enter";
            case TokenId.THROW /* 340 */:
                return "Left Shift";
            case TokenId.THROWS /* 341 */:
                return "Left Control";
            case TokenId.TRANSIENT /* 342 */:
                return "Left Alt";
            case TokenId.TRY /* 343 */:
                return "Left Super";
            case TokenId.VOID /* 344 */:
                return "Right Shift";
            case TokenId.VOLATILE /* 345 */:
                return "Right Control";
            case TokenId.WHILE /* 346 */:
                return "Right Alt";
            case TokenId.STRICT /* 347 */:
                return "Right Super";
            case 348:
                return "Menu";
            default:
                String keyName = GLFW.glfwGetKeyName(key, 0);
                return keyName == null ? "Unknown" : StringUtils.capitalize(keyName);
        }
    }

    public static String getButtonName(int button) {
        switch (button) {
            case -1:
                return "Unknown";
            case 0:
                return "Mouse Left";
            case 1:
                return "Mouse Right";
            case 2:
                return "Mouse Middle";
            default:
                return "Mouse " + button;
        }
    }

    public static byte[] readBytes(InputStream in) {
        try {
            return in.readAllBytes();
        } catch (IOException e) {
            MeteorClient.LOG.error("Error reading from stream.", e);
            return new byte[0];
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    public static boolean canUpdate() {
        return (MeteorClient.mc == null || MeteorClient.mc.field_1687 == null || MeteorClient.mc.field_1724 == null) ? false : true;
    }

    public static boolean canOpenGui() {
        return canUpdate() ? MeteorClient.mc.field_1755 == null : (MeteorClient.mc.field_1755 instanceof class_442) || (MeteorClient.mc.field_1755 instanceof class_500) || (MeteorClient.mc.field_1755 instanceof class_526);
    }

    public static boolean canCloseGui() {
        return MeteorClient.mc.field_1755 instanceof TabScreen;
    }

    public static int random(int min, int max) {
        return random.nextInt(max - min) + min;
    }

    public static double random(double min, double max) {
        return min + ((max - min) * random.nextDouble());
    }

    public static void leftClick() {
        int attackCooldown = MeteorClient.mc.getAttackCooldown();
        if (attackCooldown == 10000) {
            MeteorClient.mc.setAttackCooldown(0);
        }
        MeteorClient.mc.field_1690.field_1886.method_23481(true);
        MeteorClient.mc.leftClick();
        MeteorClient.mc.field_1690.field_1886.method_23481(false);
    }

    public static void rightClick() {
        MeteorClient.mc.meteor$rightClick();
    }

    public static boolean isShulker(class_1792 item) {
        return item == class_1802.field_8545 || item == class_1802.field_8722 || item == class_1802.field_8380 || item == class_1802.field_8050 || item == class_1802.field_8829 || item == class_1802.field_8271 || item == class_1802.field_8548 || item == class_1802.field_8520 || item == class_1802.field_8627 || item == class_1802.field_8451 || item == class_1802.field_8213 || item == class_1802.field_8816 || item == class_1802.field_8350 || item == class_1802.field_8584 || item == class_1802.field_8461 || item == class_1802.field_8676 || item == class_1802.field_8268;
    }

    public static boolean isThrowable(class_1792 item) {
        return (item instanceof class_1779) || (item instanceof class_1753) || (item instanceof class_1764) || (item instanceof class_1823) || (item instanceof class_1771) || (item instanceof class_1776) || (item instanceof class_1828) || (item instanceof class_1803) || (item instanceof class_1787) || (item instanceof class_1835);
    }

    public static void addEnchantment(class_1799 itemStack, class_6880<class_1887> enchantment, int level) {
        class_9304.class_9305 b = new class_9304.class_9305(class_1890.method_57532(itemStack));
        b.method_57550(enchantment, level);
        class_1890.method_57530(itemStack, b.method_57549());
    }

    public static void clearEnchantments(class_1799 itemStack) {
        class_1890.method_57531(itemStack, components -> {
            components.method_57548(a -> {
                return true;
            });
        });
    }

    public static void removeEnchantment(class_1799 itemStack, class_1887 enchantment) {
        class_1890.method_57531(itemStack, components -> {
            components.method_57548(enchantment1 -> {
                return ((class_1887) enchantment1.comp_349()).equals(enchantment);
            });
        });
    }

    public static Color lerp(Color first, Color second, float v) {
        return new Color((int) ((first.r * (1.0f - v)) + (second.r * v)), (int) ((first.g * (1.0f - v)) + (second.g * v)), (int) ((first.b * (1.0f - v)) + (second.b * v)));
    }

    public static boolean isLoading() {
        ReloadStateAccessor reloadState = MeteorClient.mc.getResourceReloadLogger().getReloadState();
        return reloadState == null || !reloadState.isFinished();
    }

    public static int parsePort(String full) {
        int port;
        if (full == null || full.isBlank() || !full.contains(":")) {
            return -1;
        }
        try {
            port = Integer.parseInt(full.substring(full.lastIndexOf(58) + 1, full.length() - 1));
        } catch (NumberFormatException e) {
            port = -1;
        }
        return port;
    }

    public static String parseAddress(String full) {
        return (full == null || full.isBlank() || !full.contains(":")) ? full : full.substring(0, full.lastIndexOf(58));
    }

    public static boolean resolveAddress(String address) {
        if (address == null || address.isBlank()) {
            return false;
        }
        int port = parsePort(address);
        if (port == -1) {
            port = 25565;
        } else {
            address = parseAddress(address);
        }
        return resolveAddress(address, port);
    }

    public static boolean resolveAddress(String address, int port) {
        if (port <= 0 || port > 65535 || address == null || address.isBlank()) {
            return false;
        }
        InetSocketAddress socketAddress = new InetSocketAddress(address, port);
        return !socketAddress.isUnresolved();
    }

    public static Vector3d set(Vector3d vec, class_243 v) {
        vec.x = v.field_1352;
        vec.y = v.field_1351;
        vec.z = v.field_1350;
        return vec;
    }

    public static Vector3d set(Vector3d vec, class_1297 entity, double tickDelta) {
        vec.x = class_3532.method_16436(tickDelta, entity.field_6038, entity.method_23317());
        vec.y = class_3532.method_16436(tickDelta, entity.field_5971, entity.method_23318());
        vec.z = class_3532.method_16436(tickDelta, entity.field_5989, entity.method_23321());
        return vec;
    }

    public static boolean nameFilter(String text, char character) {
        return (character >= 'a' && character <= 'z') || (character >= 'A' && character <= 'Z') || ((character >= '0' && character <= '9') || character == '_' || character == '-' || character == '.' || character == ' ');
    }

    public static boolean ipFilter(String text, char character) {
        if (text.contains(":") && character == ':') {
            return false;
        }
        return (character >= 'a' && character <= 'z') || (character >= 'A' && character <= 'Z') || ((character >= '0' && character <= '9') || character == '.' || character == '-');
    }
}
