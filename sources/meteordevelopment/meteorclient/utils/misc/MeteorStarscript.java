package meteordevelopment.meteorclient.utils.misc;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.Goal;
import baritone.api.process.IBaritoneProcess;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.SwitchBootstraps;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.mixin.MinecraftClientAccessor;
import meteordevelopment.meteorclient.pathing.BaritoneUtils;
import meteordevelopment.meteorclient.pathing.PathManagers;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.PreInit;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.world.Dimension;
import meteordevelopment.meteorclient.utils.world.TickRate;
import meteordevelopment.starscript.Script;
import meteordevelopment.starscript.Section;
import meteordevelopment.starscript.StandardLib;
import meteordevelopment.starscript.Starscript;
import meteordevelopment.starscript.compiler.Compiler;
import meteordevelopment.starscript.compiler.Parser;
import meteordevelopment.starscript.utils.Error;
import meteordevelopment.starscript.utils.StarscriptError;
import meteordevelopment.starscript.value.Value;
import meteordevelopment.starscript.value.ValueMap;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1309;
import net.minecraft.class_151;
import net.minecraft.class_155;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1959;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_2799;
import net.minecraft.class_2960;
import net.minecraft.class_3445;
import net.minecraft.class_3468;
import net.minecraft.class_3965;
import net.minecraft.class_640;
import net.minecraft.class_6880;
import net.minecraft.class_7923;
import net.minecraft.class_7924;
import org.apache.commons.lang3.StringUtils;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/misc/MeteorStarscript.class */
public class MeteorStarscript {
    public static Starscript ss = new Starscript();
    private static final class_2338.class_2339 BP = new class_2338.class_2339();
    private static final StringBuilder SB = new StringBuilder();
    private static long lastRequestedStatsTime = 0;

    @PreInit(dependencies = {PathManagers.class})
    public static void init() {
        StandardLib.init(ss);
        ss.set("mc_version", class_155.method_16673().method_48019());
        ss.set("fps", () -> {
            return Value.number(MinecraftClientAccessor.getFps());
        });
        ss.set("ping", MeteorStarscript::ping);
        ss.set("time", () -> {
            return Value.string(LocalTime.now().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)));
        });
        ss.set("cps", () -> {
            return Value.number(CPSUtils.getCpsAverage());
        });
        ss.set("meteor", new ValueMap().set("name", MeteorClient.NAME).set("version", MeteorClient.VERSION != null ? MeteorClient.BUILD_NUMBER.isEmpty() ? MeteorClient.VERSION.toString() : String.valueOf(MeteorClient.VERSION) + " " + MeteorClient.BUILD_NUMBER : "").set("modules", () -> {
            return Value.number(Modules.get().getAll().size());
        }).set("active_modules", () -> {
            return Value.number(Modules.get().getActive().size());
        }).set("is_module_active", MeteorStarscript::isModuleActive).set("get_module_info", MeteorStarscript::getModuleInfo).set("get_module_setting", MeteorStarscript::getModuleSetting).set("prefix", MeteorStarscript::getMeteorPrefix));
        if (BaritoneUtils.IS_AVAILABLE) {
            ss.set("baritone", new ValueMap().set("is_pathing", () -> {
                return Value.bool(BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing());
            }).set("distance_to_goal", MeteorStarscript::baritoneDistanceToGoal).set("process", MeteorStarscript::baritoneProcess).set("process_name", MeteorStarscript::baritoneProcessName).set("eta", MeteorStarscript::baritoneETA));
        }
        ss.set("camera", new ValueMap().set("pos", new ValueMap().set("_toString", () -> {
            return posString(false, true);
        }).set("x", () -> {
            return Value.number(MeteorClient.mc.field_1773.method_19418().method_19326().field_1352);
        }).set("y", () -> {
            return Value.number(MeteorClient.mc.field_1773.method_19418().method_19326().field_1351);
        }).set("z", () -> {
            return Value.number(MeteorClient.mc.field_1773.method_19418().method_19326().field_1350);
        })).set("opposite_dim_pos", new ValueMap().set("_toString", () -> {
            return posString(true, true);
        }).set("x", () -> {
            return oppositeX(true);
        }).set("y", () -> {
            return Value.number(MeteorClient.mc.field_1773.method_19418().method_19326().field_1351);
        }).set("z", () -> {
            return oppositeZ(true);
        })).set("yaw", () -> {
            return yaw(true);
        }).set("pitch", () -> {
            return pitch(true);
        }).set("direction", () -> {
            return direction(true);
        }));
        ss.set("player", new ValueMap().set("_toString", () -> {
            return Value.string(MeteorClient.mc.method_1548().method_1676());
        }).set("health", () -> {
            return Value.number(MeteorClient.mc.field_1724 != null ? MeteorClient.mc.field_1724.method_6032() : 0.0d);
        }).set("absorption", () -> {
            return Value.number(MeteorClient.mc.field_1724 != null ? MeteorClient.mc.field_1724.method_6067() : 0.0d);
        }).set("hunger", () -> {
            return Value.number(MeteorClient.mc.field_1724 != null ? MeteorClient.mc.field_1724.method_7344().method_7586() : 0.0d);
        }).set("saturation", () -> {
            return Value.number(MeteorClient.mc.field_1724 != null ? MeteorClient.mc.field_1724.method_7344().method_7589() : 0.0d);
        }).set("speed", () -> {
            return Value.number(Utils.getPlayerSpeed().method_37267());
        }).set("speed_all", new ValueMap().set("_toString", () -> {
            return Value.string(MeteorClient.mc.field_1724 != null ? Utils.getPlayerSpeed().toString() : "");
        }).set("x", () -> {
            return Value.number(MeteorClient.mc.field_1724 != null ? Utils.getPlayerSpeed().field_1352 : 0.0d);
        }).set("y", () -> {
            return Value.number(MeteorClient.mc.field_1724 != null ? Utils.getPlayerSpeed().field_1351 : 0.0d);
        }).set("z", () -> {
            return Value.number(MeteorClient.mc.field_1724 != null ? Utils.getPlayerSpeed().field_1350 : 0.0d);
        })).set("breaking_progress", () -> {
            return Value.number(MeteorClient.mc.field_1761 != null ? MeteorClient.mc.field_1761.getBreakingProgress() : 0.0d);
        }).set("biome", MeteorStarscript::biome).set("dimension", () -> {
            return Value.string(PlayerUtils.getDimension().name());
        }).set("opposite_dimension", () -> {
            return Value.string(PlayerUtils.getDimension().opposite().name());
        }).set("gamemode", () -> {
            return PlayerUtils.getGameMode() != null ? Value.string(StringUtils.capitalize(PlayerUtils.getGameMode().method_8381())) : Value.null_();
        }).set("pos", new ValueMap().set("_toString", () -> {
            return posString(false, false);
        }).set("x", () -> {
            return Value.number(MeteorClient.mc.field_1724 != null ? MeteorClient.mc.field_1724.method_23317() : 0.0d);
        }).set("y", () -> {
            return Value.number(MeteorClient.mc.field_1724 != null ? MeteorClient.mc.field_1724.method_23318() : 0.0d);
        }).set("z", () -> {
            return Value.number(MeteorClient.mc.field_1724 != null ? MeteorClient.mc.field_1724.method_23321() : 0.0d);
        })).set("opposite_dim_pos", new ValueMap().set("_toString", () -> {
            return posString(true, false);
        }).set("x", () -> {
            return oppositeX(false);
        }).set("y", () -> {
            return Value.number(MeteorClient.mc.field_1724 != null ? MeteorClient.mc.field_1724.method_23318() : 0.0d);
        }).set("z", () -> {
            return oppositeZ(false);
        })).set("yaw", () -> {
            return yaw(false);
        }).set("pitch", () -> {
            return pitch(false);
        }).set("direction", () -> {
            return direction(false);
        }).set("hand", () -> {
            return MeteorClient.mc.field_1724 != null ? wrap(MeteorClient.mc.field_1724.method_6047()) : Value.null_();
        }).set("offhand", () -> {
            return MeteorClient.mc.field_1724 != null ? wrap(MeteorClient.mc.field_1724.method_6079()) : Value.null_();
        }).set("hand_or_offhand", MeteorStarscript::handOrOffhand).set("get_item", MeteorStarscript::getItem).set("count_items", MeteorStarscript::countItems).set("xp", new ValueMap().set("level", () -> {
            return Value.number(MeteorClient.mc.field_1724 != null ? MeteorClient.mc.field_1724.field_7520 : 0.0d);
        }).set("progress", () -> {
            return Value.number(MeteorClient.mc.field_1724 != null ? MeteorClient.mc.field_1724.field_7510 : 0.0d);
        }).set("total", () -> {
            return Value.number(MeteorClient.mc.field_1724 != null ? MeteorClient.mc.field_1724.field_7495 : 0.0d);
        })).set("has_potion_effect", MeteorStarscript::hasPotionEffect).set("get_potion_effect", MeteorStarscript::getPotionEffect).set("get_stat", MeteorStarscript::getStat));
        ss.set("crosshair_target", new ValueMap().set("type", MeteorStarscript::crosshairType).set("value", MeteorStarscript::crosshairValue));
        ss.set("server", new ValueMap().set("_toString", () -> {
            return Value.string(Utils.getWorldName());
        }).set("tps", () -> {
            return Value.number(TickRate.INSTANCE.getTickRate());
        }).set("time", () -> {
            return Value.string(Utils.getWorldTime());
        }).set("player_count", () -> {
            return Value.number(MeteorClient.mc.method_1562() != null ? MeteorClient.mc.method_1562().method_2880().size() : 0.0d);
        }).set("difficulty", () -> {
            return Value.string(MeteorClient.mc.field_1687 != null ? MeteorClient.mc.field_1687.method_8407().method_5460() : "");
        }));
    }

    public static Script compile(String source) {
        Parser.Result result = Parser.parse(source);
        if (result.hasErrors()) {
            for (Error error : result.errors) {
                printChatError(error);
            }
            return null;
        }
        return Compiler.compile(result);
    }

    public static Section runSection(Script script, StringBuilder sb) {
        try {
            return ss.run(script, sb);
        } catch (StarscriptError error) {
            printChatError(error);
            return null;
        }
    }

    public static String run(Script script, StringBuilder sb) {
        Section section = runSection(script, sb);
        if (section != null) {
            return section.toString();
        }
        return null;
    }

    public static Section runSection(Script script) {
        return runSection(script, new StringBuilder());
    }

    public static String run(Script script) {
        return run(script, new StringBuilder());
    }

    public static void printChatError(int i, Error error) {
        String caller = getCallerName();
        if (caller != null) {
            if (i == -1) {
                ChatUtils.errorPrefix("Starscript", "%d '%c': %s (from %s)", Integer.valueOf(error.character), Character.valueOf(error.ch), error.message, caller);
                return;
            } else {
                ChatUtils.errorPrefix("Starscript", "%d, %d '%c': %s (from %s)", Integer.valueOf(i), Integer.valueOf(error.character), Character.valueOf(error.ch), error.message, caller);
                return;
            }
        }
        if (i == -1) {
            ChatUtils.errorPrefix("Starscript", "%d '%c': %s", Integer.valueOf(error.character), Character.valueOf(error.ch), error.message);
        } else {
            ChatUtils.errorPrefix("Starscript", "%d, %d '%c': %s", Integer.valueOf(i), Integer.valueOf(error.character), Character.valueOf(error.ch), error.message);
        }
    }

    public static void printChatError(Error error) {
        printChatError(-1, error);
    }

    public static void printChatError(StarscriptError e) {
        String caller = getCallerName();
        if (caller == null) {
            ChatUtils.errorPrefix("Starscript", "%s", e.getMessage());
        } else {
            ChatUtils.errorPrefix("Starscript", "%s (from %s)", e.getMessage(), caller);
        }
    }

    private static String getCallerName() {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        if (elements.length == 0) {
            return null;
        }
        for (int i = 1; i < elements.length; i++) {
            String name = elements[i].getClassName();
            if (!name.startsWith(Starscript.class.getPackageName()) && !name.equals(MeteorStarscript.class.getName())) {
                return name.substring(name.lastIndexOf(46) + 1);
            }
        }
        return null;
    }

    private static Value hasPotionEffect(Starscript ss2, int argCount) {
        if (argCount < 1) {
            ss2.error("player.has_potion_effect() requires 1 argument, got %d.", Integer.valueOf(argCount));
        }
        if (MeteorClient.mc.field_1724 == null) {
            return Value.bool(false);
        }
        class_2960 name = popIdentifier(ss2, "First argument to player.has_potion_effect() needs to a string.");
        Optional<class_6880.class_6883<class_1291>> effect = class_7923.field_41174.method_10223(name);
        if (effect.isEmpty()) {
            return Value.bool(false);
        }
        class_1293 effectInstance = MeteorClient.mc.field_1724.method_6112((class_6880) effect.get());
        return Value.bool(effectInstance != null);
    }

    private static Value getPotionEffect(Starscript ss2, int argCount) {
        class_1293 effectInstance;
        if (argCount < 1) {
            ss2.error("player.get_potion_effect() requires 1 argument, got %d.", Integer.valueOf(argCount));
        }
        if (MeteorClient.mc.field_1724 == null) {
            return Value.null_();
        }
        class_2960 name = popIdentifier(ss2, "First argument to player.get_potion_effect() needs to a string.");
        Optional<class_6880.class_6883<class_1291>> effect = class_7923.field_41174.method_10223(name);
        if (!effect.isEmpty() && (effectInstance = MeteorClient.mc.field_1724.method_6112((class_6880) effect.get())) != null) {
            return wrap(effectInstance);
        }
        return Value.null_();
    }

    private static Value getStat(Starscript ss2, int argCount) {
        class_2960 name;
        class_3445<?> class_3445VarMethod_14956;
        if (argCount < 1) {
            ss2.error("player.get_stat() requires 1 argument, got %d.", Integer.valueOf(argCount));
        }
        if (MeteorClient.mc.field_1724 == null) {
            return Value.number(0.0d);
        }
        long time = System.currentTimeMillis();
        if ((time - lastRequestedStatsTime) / 1000.0d >= 1.0d && MeteorClient.mc.method_1562() != null) {
            MeteorClient.mc.method_1562().method_52787(new class_2799(class_2799.class_2800.field_12775));
            lastRequestedStatsTime = time;
        }
        String type = argCount > 1 ? ss2.popString("First argument to player.get_stat() needs to be a string.") : "custom";
        name = popIdentifier(ss2, (argCount > 1 ? "Second" : "First") + " argument to player.get_stat() needs to be a string.");
        switch (type) {
            case "mined":
                class_3445VarMethod_14956 = class_3468.field_15427.method_14956((class_2248) class_7923.field_41175.method_63535(name));
                break;
            case "crafted":
                class_3445VarMethod_14956 = class_3468.field_15370.method_14956((class_1792) class_7923.field_41178.method_63535(name));
                break;
            case "used":
                class_3445VarMethod_14956 = class_3468.field_15372.method_14956((class_1792) class_7923.field_41178.method_63535(name));
                break;
            case "broken":
                class_3445VarMethod_14956 = class_3468.field_15383.method_14956((class_1792) class_7923.field_41178.method_63535(name));
                break;
            case "picked_up":
                class_3445VarMethod_14956 = class_3468.field_15392.method_14956((class_1792) class_7923.field_41178.method_63535(name));
                break;
            case "dropped":
                class_3445VarMethod_14956 = class_3468.field_15405.method_14956((class_1792) class_7923.field_41178.method_63535(name));
                break;
            case "killed":
                class_3445VarMethod_14956 = class_3468.field_15403.method_14956((class_1299) class_7923.field_41177.method_63535(name));
                break;
            case "killed_by":
                class_3445VarMethod_14956 = class_3468.field_15411.method_14956((class_1299) class_7923.field_41177.method_63535(name));
                break;
            case "custom":
                class_2960 name2 = (class_2960) class_7923.field_41183.method_63535(name);
                if (name2 == null) {
                    class_3445VarMethod_14956 = null;
                    break;
                } else {
                    class_3445VarMethod_14956 = class_3468.field_15419.method_14956(name2);
                    break;
                }
                break;
            default:
                class_3445VarMethod_14956 = null;
                break;
        }
        class_3445<?> stat = class_3445VarMethod_14956;
        return Value.number(stat != null ? MeteorClient.mc.field_1724.method_3143().method_15025(stat) : 0.0d);
    }

    private static Value getModuleInfo(Starscript ss2, int argCount) {
        if (argCount != 1) {
            ss2.error("meteor.get_module_info() requires 1 argument, got %d.", Integer.valueOf(argCount));
        }
        Module module = Modules.get().get(ss2.popString("First argument to meteor.get_module_info() needs to be a string."));
        if (module != null && module.isActive()) {
            String info = module.getInfoString();
            return Value.string(info == null ? "" : info);
        }
        return Value.string("");
    }

    private static Value getModuleSetting(Starscript ss2, int argCount) {
        if (argCount != 2) {
            ss2.error("meteor.get_module_setting() requires 2 arguments, got %d.", Integer.valueOf(argCount));
        }
        String settingName = ss2.popString("Second argument to meteor.get_module_setting() needs to be a string.");
        String moduleName = ss2.popString("First argument to meteor.get_module_setting() needs to be a string.");
        Module module = Modules.get().get(moduleName);
        if (module == null) {
            ss2.error("Unable to get module %s for meteor.get_module_setting()", moduleName);
        }
        Setting<?> setting = module.settings.get(settingName);
        if (setting == null) {
            ss2.error("Unable to get setting %s for module %s for meteor.get_module_setting()", settingName, moduleName);
        }
        Object value = setting.get();
        switch ((int) SwitchBootstraps.typeSwitch(MethodHandles.lookup(), "typeSwitch", MethodType.methodType(Integer.TYPE, Object.class, Integer.TYPE), Double.class, Integer.class, Boolean.class, List.class).dynamicInvoker().invoke(value, 0) /* invoke-custom */) {
            case -1:
            default:
                return Value.string(value.toString());
            case 0:
                Double d = (Double) value;
                return Value.number(d.doubleValue());
            case 1:
                Integer i = (Integer) value;
                return Value.number(i.intValue());
            case 2:
                Boolean b = (Boolean) value;
                return Value.bool(b.booleanValue());
            case 3:
                List<?> list = (List) value;
                return Value.number(list.size());
        }
    }

    private static Value isModuleActive(Starscript ss2, int argCount) {
        if (argCount != 1) {
            ss2.error("meteor.is_module_active() requires 1 argument, got %d.", Integer.valueOf(argCount));
        }
        Module module = Modules.get().get(ss2.popString("First argument to meteor.is_module_active() needs to be a string."));
        return Value.bool(module != null && module.isActive());
    }

    private static Value getItem(Starscript ss2, int argCount) {
        if (argCount != 1) {
            ss2.error("player.get_item() requires 1 argument, got %d.", Integer.valueOf(argCount));
        }
        int i = (int) ss2.popNumber("First argument to player.get_item() needs to be a number.");
        if (i < 0) {
            ss2.error("First argument to player.get_item() needs to be a non-negative integer.", Integer.valueOf(i));
        }
        return MeteorClient.mc.field_1724 != null ? wrap(MeteorClient.mc.field_1724.method_31548().method_5438(i)) : Value.null_();
    }

    private static Value countItems(Starscript ss2, int argCount) {
        if (argCount != 1) {
            ss2.error("player.count_items() requires 1 argument, got %d.", Integer.valueOf(argCount));
        }
        String idRaw = ss2.popString("First argument to player.count_items() needs to be a string.");
        class_2960 id = class_2960.method_12829(idRaw);
        if (id == null) {
            return Value.number(0.0d);
        }
        class_1792 item = (class_1792) class_7923.field_41178.method_63535(id);
        if (item == class_1802.field_8162 || MeteorClient.mc.field_1724 == null) {
            return Value.number(0.0d);
        }
        int count = 0;
        for (int i = 0; i < MeteorClient.mc.field_1724.method_31548().method_5439(); i++) {
            class_1799 itemStack = MeteorClient.mc.field_1724.method_31548().method_5438(i);
            if (itemStack.method_7909() == item) {
                count += itemStack.method_7947();
            }
        }
        return Value.number(count);
    }

    private static Value getMeteorPrefix() {
        return Config.get() == null ? Value.null_() : Value.string(Config.get().prefix.get());
    }

    private static Value baritoneProcess() {
        Optional<IBaritoneProcess> process = BaritoneAPI.getProvider().getPrimaryBaritone().getPathingControlManager().mostRecentInControl();
        return Value.string(process.isEmpty() ? "" : process.get().displayName0());
    }

    private static Value baritoneProcessName() {
        Optional<IBaritoneProcess> process = BaritoneAPI.getProvider().getPrimaryBaritone().getPathingControlManager().mostRecentInControl();
        if (process.isEmpty()) {
            return Value.string("");
        }
        String className = process.get().getClass().getSimpleName();
        if (className.endsWith("Process")) {
            className = className.substring(0, className.length() - 7);
        }
        SB.append(className);
        int i = 0;
        for (int j = 0; j < className.length(); j++) {
            if (j > 0 && Character.isUpperCase(className.charAt(j))) {
                SB.insert(i, ' ');
                i++;
            }
            i++;
        }
        String name = SB.toString();
        SB.setLength(0);
        return Value.string(name);
    }

    private static Value baritoneETA() {
        if (MeteorClient.mc.field_1724 == null) {
            return Value.number(0.0d);
        }
        Optional<Double> ticksTillGoal = BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().estimatedTicksToGoal();
        return (Value) ticksTillGoal.map(aDouble -> {
            return Value.number(aDouble.doubleValue() / 20.0d);
        }).orElseGet(() -> {
            return Value.number(0.0d);
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static Value oppositeX(boolean camera) {
        double x = camera ? MeteorClient.mc.field_1773.method_19418().method_19326().field_1352 : MeteorClient.mc.field_1724 != null ? MeteorClient.mc.field_1724.method_23317() : 0.0d;
        Dimension dimension = PlayerUtils.getDimension();
        if (dimension == Dimension.Overworld) {
            x /= 8.0d;
        } else if (dimension == Dimension.Nether) {
            x *= 8.0d;
        }
        return Value.number(x);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static Value oppositeZ(boolean camera) {
        double z = camera ? MeteorClient.mc.field_1773.method_19418().method_19326().field_1350 : MeteorClient.mc.field_1724 != null ? MeteorClient.mc.field_1724.method_23321() : 0.0d;
        Dimension dimension = PlayerUtils.getDimension();
        if (dimension == Dimension.Overworld) {
            z /= 8.0d;
        } else if (dimension == Dimension.Nether) {
            z *= 8.0d;
        }
        return Value.number(z);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static Value yaw(boolean camera) {
        float yaw;
        if (camera) {
            yaw = MeteorClient.mc.field_1773.method_19418().method_19330();
        } else {
            yaw = MeteorClient.mc.field_1724 != null ? MeteorClient.mc.field_1724.method_36454() : 0.0f;
        }
        float yaw2 = yaw % 360.0f;
        if (yaw2 < 0.0f) {
            yaw2 += 360.0f;
        }
        if (yaw2 > 180.0f) {
            yaw2 -= 360.0f;
        }
        return Value.number(yaw2);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static Value pitch(boolean camera) {
        float pitch;
        if (camera) {
            pitch = MeteorClient.mc.field_1773.method_19418().method_19329();
        } else {
            pitch = MeteorClient.mc.field_1724 != null ? MeteorClient.mc.field_1724.method_36455() : 0.0f;
        }
        float pitch2 = pitch % 360.0f;
        if (pitch2 < 0.0f) {
            pitch2 += 360.0f;
        }
        if (pitch2 > 180.0f) {
            pitch2 -= 360.0f;
        }
        return Value.number(pitch2);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static Value direction(boolean camera) {
        float yaw;
        if (camera) {
            yaw = MeteorClient.mc.field_1773.method_19418().method_19330();
        } else {
            yaw = MeteorClient.mc.field_1724 != null ? MeteorClient.mc.field_1724.method_36454() : 0.0f;
        }
        return wrap(HorizontalDirection.get(yaw));
    }

    private static Value biome() {
        if (MeteorClient.mc.field_1724 == null || MeteorClient.mc.field_1687 == null) {
            return Value.string("");
        }
        BP.method_10102(MeteorClient.mc.field_1724.method_23317(), MeteorClient.mc.field_1724.method_23318(), MeteorClient.mc.field_1724.method_23321());
        return (Value) MeteorClient.mc.field_1687.method_30349().method_46759(class_7924.field_41236).map(biomeRegistry -> {
            class_2960 id = biomeRegistry.method_10221((class_1959) MeteorClient.mc.field_1687.method_23753(BP).comp_349());
            return id == null ? Value.string("Unknown") : Value.string((String) Arrays.stream(id.method_12832().split("_")).map(StringUtils::capitalize).collect(Collectors.joining(" ")));
        }).orElse(Value.string("Unknown"));
    }

    private static Value handOrOffhand() {
        if (MeteorClient.mc.field_1724 == null) {
            return Value.null_();
        }
        class_1799 itemStack = MeteorClient.mc.field_1724.method_6047();
        if (itemStack.method_7960()) {
            itemStack = MeteorClient.mc.field_1724.method_6079();
        }
        return itemStack != null ? wrap(itemStack) : Value.null_();
    }

    private static Value ping() {
        if (MeteorClient.mc.method_1562() == null || MeteorClient.mc.field_1724 == null) {
            return Value.number(0.0d);
        }
        class_640 playerListEntry = MeteorClient.mc.method_1562().method_2871(MeteorClient.mc.field_1724.method_5667());
        return Value.number(playerListEntry != null ? playerListEntry.method_2959() : 0.0d);
    }

    private static Value baritoneDistanceToGoal() {
        Goal goal = BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().getGoal();
        return Value.number((goal == null || MeteorClient.mc.field_1724 == null) ? 0.0d : goal.heuristic(MeteorClient.mc.field_1724.method_24515()));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static Value posString(boolean opposite, boolean camera) {
        class_243 pos;
        if (camera) {
            pos = MeteorClient.mc.field_1773.method_19418().method_19326();
        } else {
            pos = MeteorClient.mc.field_1724 != null ? MeteorClient.mc.field_1724.method_19538() : class_243.field_1353;
        }
        double x = pos.field_1352;
        double z = pos.field_1350;
        if (opposite) {
            Dimension dimension = PlayerUtils.getDimension();
            if (dimension == Dimension.Overworld) {
                x /= 8.0d;
                z /= 8.0d;
            } else if (dimension == Dimension.Nether) {
                x *= 8.0d;
                z *= 8.0d;
            }
        }
        return posString(x, pos.field_1351, z);
    }

    private static Value posString(double x, double y, double z) {
        return Value.string(String.format("X: %.0f Y: %.0f Z: %.0f", Double.valueOf(x), Double.valueOf(y), Double.valueOf(z)));
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: java.lang.MatchException */
    private static Value crosshairType() throws MatchException {
        String str;
        if (MeteorClient.mc.field_1765 == null) {
            return Value.string("miss");
        }
        switch (AnonymousClass1.$SwitchMap$net$minecraft$util$hit$HitResult$Type[MeteorClient.mc.field_1765.method_17783().ordinal()]) {
            case 1:
                str = "miss";
                break;
            case 2:
                str = "block";
                break;
            case 3:
                str = "entity";
                break;
            default:
                throw new MatchException((String) null, (Throwable) null);
        }
        return Value.string(str);
    }

    /* JADX INFO: renamed from: meteordevelopment.meteorclient.utils.misc.MeteorStarscript$1, reason: invalid class name */
    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/misc/MeteorStarscript$1.class */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$net$minecraft$util$hit$HitResult$Type = new int[class_239.class_240.values().length];

        static {
            try {
                $SwitchMap$net$minecraft$util$hit$HitResult$Type[class_239.class_240.field_1333.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$net$minecraft$util$hit$HitResult$Type[class_239.class_240.field_1332.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$net$minecraft$util$hit$HitResult$Type[class_239.class_240.field_1331.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    private static Value crosshairValue() {
        if (MeteorClient.mc.field_1687 == null || MeteorClient.mc.field_1765 == null) {
            return Value.null_();
        }
        if (MeteorClient.mc.field_1765.method_17783() == class_239.class_240.field_1333) {
            return Value.string("");
        }
        class_3965 class_3965Var = MeteorClient.mc.field_1765;
        if (!(class_3965Var instanceof class_3965)) {
            return wrap(MeteorClient.mc.field_1765.method_17782());
        }
        class_3965 hit = class_3965Var;
        return wrap(hit.method_17777(), MeteorClient.mc.field_1687.method_8320(hit.method_17777()));
    }

    public static class_2960 popIdentifier(Starscript ss2, String errorMessage) {
        try {
            return class_2960.method_60654(ss2.popString(errorMessage));
        } catch (class_151 e) {
            ss2.error(e.getMessage(), new Object[0]);
            return null;
        }
    }

    public static Value wrap(class_1799 itemStack) {
        String name = itemStack.method_7960() ? "" : Names.get(itemStack.method_7909());
        int durability = 0;
        if (!itemStack.method_7960() && itemStack.method_7963()) {
            durability = itemStack.method_7936() - itemStack.method_7919();
        }
        return Value.map(new ValueMap().set("_toString", Value.string(itemStack.method_7947() <= 1 ? name : String.format("%s %dx", name, Integer.valueOf(itemStack.method_7947())))).set("name", Value.string(name)).set("id", Value.string(class_7923.field_41178.method_10221(itemStack.method_7909()).toString())).set("count", Value.number(itemStack.method_7947())).set("durability", Value.number(durability)).set("max_durability", Value.number(itemStack.method_7936())));
    }

    public static Value wrap(class_2338 blockPos, class_2680 blockState) {
        return Value.map(new ValueMap().set("_toString", Value.string(Names.get(blockState.method_26204()))).set("id", Value.string(class_7923.field_41175.method_10221(blockState.method_26204()).toString())).set("pos", Value.map(new ValueMap().set("_toString", posString(blockPos.method_10263(), blockPos.method_10264(), blockPos.method_10260())).set("x", Value.number(blockPos.method_10263())).set("y", Value.number(blockPos.method_10264())).set("z", Value.number(blockPos.method_10260())))));
    }

    public static Value wrap(class_1297 entity) {
        double dMethod_6032;
        double dMethod_6067;
        ValueMap valueMap = new ValueMap().set("_toString", Value.string(entity.method_5477().getString())).set("id", Value.string(class_7923.field_41177.method_10221(entity.method_5864()).toString()));
        if (entity instanceof class_1309) {
            class_1309 e = (class_1309) entity;
            dMethod_6032 = e.method_6032();
        } else {
            dMethod_6032 = 0.0d;
        }
        ValueMap valueMap2 = valueMap.set("health", Value.number(dMethod_6032));
        if (entity instanceof class_1309) {
            class_1309 e2 = (class_1309) entity;
            dMethod_6067 = e2.method_6067();
        } else {
            dMethod_6067 = 0.0d;
        }
        return Value.map(valueMap2.set("absorption", Value.number(dMethod_6067)).set("pos", Value.map(new ValueMap().set("_toString", posString(entity.method_23317(), entity.method_23318(), entity.method_23321())).set("x", Value.number(entity.method_23317())).set("y", Value.number(entity.method_23318())).set("z", Value.number(entity.method_23321())))));
    }

    public static Value wrap(HorizontalDirection dir) {
        return Value.map(new ValueMap().set("_toString", Value.string(dir.name + " " + dir.axis)).set("name", Value.string(dir.name)).set("axis", Value.string(dir.axis)));
    }

    public static Value wrap(class_1293 effectInstance) {
        return Value.map(new ValueMap().set("duration", effectInstance.method_5584()).set("level", effectInstance.method_5578() + 1));
    }
}
