package meteordevelopment.meteorclient.gui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorGuiTheme;
import meteordevelopment.meteorclient.gui.themes.revive.ReviveGuiTheme;
import meteordevelopment.meteorclient.utils.PostInit;
import meteordevelopment.meteorclient.utils.PreInit;
import net.minecraft.class_2487;
import net.minecraft.class_2507;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/GuiThemes.class */
public class GuiThemes {
    private static final File FOLDER = new File(MeteorClient.FOLDER, "gui");
    private static final File THEMES_FOLDER = new File(FOLDER, "themes");
    private static final File FILE = new File(FOLDER, "gui.nbt");
    private static final List<GuiTheme> themes = new ArrayList();
    private static GuiTheme theme;

    private GuiThemes() {
    }

    @PreInit
    public static void init() {
        add(new ReviveGuiTheme());
        add(new MeteorGuiTheme());
    }

    @PostInit
    public static void postInit() {
        if (FILE.exists()) {
            try {
                class_2487 tag = class_2507.method_10633(FILE.toPath());
                if (tag != null) {
                    select(tag.method_68564("currentTheme", ""));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (theme == null) {
            select("Meteor");
        }
    }

    public static void add(GuiTheme theme2) {
        Iterator<GuiTheme> it = themes.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            if (it.next().name.equals(theme2.name)) {
                it.remove();
                MeteorClient.LOG.error("Theme with the name '{}' has already been added.", theme2.name);
                break;
            }
        }
        themes.add(theme2);
    }

    public static void select(String name) {
        class_2487 tag;
        GuiTheme theme2 = null;
        Iterator<GuiTheme> it = themes.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            GuiTheme t = it.next();
            if (t.name.equals(name)) {
                theme2 = t;
                break;
            }
        }
        if (theme2 != null) {
            saveTheme();
            theme = theme2;
            try {
                File file = new File(THEMES_FOLDER, get().name + ".nbt");
                if (file.exists() && (tag = class_2507.method_10633(file.toPath())) != null) {
                    get().fromTag2(tag);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            saveGlobal();
        }
    }

    public static GuiTheme get() {
        return theme;
    }

    public static String[] getNames() {
        String[] names = new String[themes.size()];
        for (int i = 0; i < themes.size(); i++) {
            names[i] = themes.get(i).name;
        }
        return names;
    }

    private static void saveTheme() {
        if (get() != null) {
            try {
                class_2487 tag = get().toTag();
                THEMES_FOLDER.mkdirs();
                class_2507.method_10630(tag, new File(THEMES_FOLDER, get().name + ".nbt").toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void saveGlobal() {
        try {
            class_2487 tag = new class_2487();
            tag.method_10582("currentTheme", get().name);
            FOLDER.mkdirs();
            class_2507.method_10630(tag, FILE.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        saveTheme();
        saveGlobal();
    }
}
