package meteordevelopment.meteorclient.utils.misc.input;

import java.util.Iterator;
import java.util.Map;
import javassist.compiler.TokenId;
import meteordevelopment.meteorclient.mixin.KeyBindingAccessor;
import net.minecraft.class_304;
import net.minecraft.class_3675;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/misc/input/KeyBinds.class */
public class KeyBinds {
    private static final String CATEGORY = "ReviveClient";
    public static class_304 OPEN_GUI = new class_304("key.meteor-client.open-gui", class_3675.class_307.field_1668, TokenId.VOID, CATEGORY);
    public static class_304 OPEN_COMMANDS = new class_304("key.meteor-client.open-commands", class_3675.class_307.field_1668, 46, CATEGORY);

    private KeyBinds() {
    }

    public static class_304[] apply(class_304[] binds) {
        Map<String, Integer> categories = KeyBindingAccessor.getCategoryOrderMap();
        int highest = 0;
        Iterator<Integer> it = categories.values().iterator();
        while (it.hasNext()) {
            int i = it.next().intValue();
            if (i > highest) {
                highest = i;
            }
        }
        categories.put(CATEGORY, Integer.valueOf(highest + 1));
        class_304[] newBinds = new class_304[binds.length + 2];
        System.arraycopy(binds, 0, newBinds, 0, binds.length);
        newBinds[binds.length] = OPEN_GUI;
        newBinds[binds.length + 1] = OPEN_COMMANDS;
        return newBinds;
    }
}
