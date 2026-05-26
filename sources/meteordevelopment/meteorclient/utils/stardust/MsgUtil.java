package meteordevelopment.meteorclient.utils.stardust;

import java.util.HashMap;
import java.util.Map;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_2583;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/stardust/MsgUtil.class */
public class MsgUtil {
    private static final Map<String, String> modulePrefixes = new HashMap();

    public static String getPrefix() {
        return String.valueOf(class_124.field_1063) + "[" + String.valueOf(class_124.field_1080) + "MasterClient" + String.valueOf(class_124.field_1063) + "]";
    }

    public static String getRawPrefix() {
        return "[Stardust]";
    }

    public static String getRawPrefix(String module) {
        return "[" + module + "]";
    }

    public static void initModulePrefixes() {
        for (Module module : Modules.get().getGroup(Categories.Misc)) {
            String name = module.name;
            modulePrefixes.put(name, "MasterClient");
        }
    }

    public static String getModulePrefix(String module) {
        if (!modulePrefixes.containsKey(module)) {
            return String.valueOf(class_124.field_1063) + "[MasterClient] " + String.valueOf(class_124.field_1063) + "[" + String.valueOf(class_124.field_1080) + Utils.nameToTitle(module) + String.valueOf(class_124.field_1063) + "]";
        }
        return String.valueOf(class_124.field_1063) + "[" + modulePrefixes.get(module) + "] " + String.valueOf(class_124.field_1063) + "[" + String.valueOf(class_124.field_1080) + Utils.nameToTitle(module) + String.valueOf(class_124.field_1063) + "]";
    }

    public static void sendRawMsg(String msg) {
        if (MeteorClient.mc.field_1724 == null) {
            return;
        }
        MeteorClient.mc.field_1724.method_7353(class_2561.method_43470(msg), false);
    }

    public static void sendMsg(String msg) {
        if (MeteorClient.mc.field_1724 == null) {
            return;
        }
        try {
            StringBuilder sb = new StringBuilder();
            MeteorClient.mc.field_1724.method_7353(class_2561.method_43470(sb.append(getPrefix()).append(' ').append(class_124.field_1080).append(msg).toString()), false);
        } catch (Exception e) {
        }
    }

    public static void sendMsg(String msg, class_2583 style) {
        if (MeteorClient.mc.field_1724 == null) {
            return;
        }
        try {
            String message = getPrefix() + " " + String.valueOf(class_124.field_1080) + msg;
            MeteorClient.mc.field_1724.method_7353(class_2561.method_43470(message).method_10862(style), false);
        } catch (Exception e) {
        }
    }

    public static void sendModuleMsg(String msg, String module) {
        if (MeteorClient.mc.field_1724 == null) {
            return;
        }
        try {
            StringBuilder sb = new StringBuilder();
            MeteorClient.mc.field_1724.method_7353(class_2561.method_43470(sb.append(getModulePrefix(module)).append(' ').append(class_124.field_1080).append(msg).toString()), false);
        } catch (Exception e) {
        }
    }

    public static void sendModuleMsg(String msg, class_2583 style, String module) {
        if (MeteorClient.mc.field_1724 == null) {
            return;
        }
        try {
            String message = getModulePrefix(module) + " " + String.valueOf(class_124.field_1080) + msg;
            MeteorClient.mc.field_1724.method_7353(class_2561.method_43470(message).method_10862(style), false);
        } catch (Exception e) {
        }
    }

    public static void updateMsg(String msg, int hashcode) {
        if (MeteorClient.mc.field_1724 == null) {
            return;
        }
        try {
            StringBuilder sb = new StringBuilder();
            MeteorClient.mc.field_1705.method_1743().meteor$add(class_2561.method_43470(sb.append(getPrefix()).append(' ').append(class_124.field_1080).append(msg).toString()), hashcode);
        } catch (Exception e) {
        }
    }

    public static void updateModuleMsg(String msg, String module, int hashcode) {
        if (MeteorClient.mc.field_1724 == null) {
            return;
        }
        try {
            StringBuilder sb = new StringBuilder();
            MeteorClient.mc.field_1705.method_1743().meteor$add(class_2561.method_43470(sb.append(getModulePrefix(module)).append(' ').append(class_124.field_1080).append(msg).toString()), hashcode);
        } catch (Exception e) {
        }
    }
}
