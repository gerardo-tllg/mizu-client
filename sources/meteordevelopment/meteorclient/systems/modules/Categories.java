package meteordevelopment.meteorclient.systems.modules;

import meteordevelopment.meteorclient.addons.AddonManager;
import net.minecraft.class_1802;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/Categories.class */
public class Categories {
    public static final Category Combat = new Category("Combat", class_1802.field_8845.method_7854());
    public static final Category Player = new Category("Player", class_1802.field_8694.method_7854());
    public static final Category Movement = new Category("Movement", class_1802.field_8285.method_7854());
    public static final Category Render = new Category("Render", class_1802.field_8280.method_7854());
    public static final Category World = new Category("World", class_1802.field_8270.method_7854());
    public static final Category Misc = new Category("Misc", class_1802.field_8187.method_7854());
    public static final Category Hunting = new Category("Hunting", class_1802.field_8102.method_7854());
    public static final Category Gui = new Category("Gui", class_1802.field_8251.method_7854());
    public static boolean REGISTERING;

    public static void init() {
        REGISTERING = true;
        Modules.registerCategory(Combat);
        Modules.registerCategory(Player);
        Modules.registerCategory(Movement);
        Modules.registerCategory(Render);
        Modules.registerCategory(World);
        Modules.registerCategory(Misc);
        Modules.registerCategory(Hunting);
        Modules.registerCategory(Gui);
        AddonManager.ADDONS.forEach((v0) -> {
            v0.onRegisterCategories();
        });
        REGISTERING = false;
    }
}
