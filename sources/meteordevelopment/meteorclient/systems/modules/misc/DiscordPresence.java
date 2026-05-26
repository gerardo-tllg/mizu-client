package meteordevelopment.meteorclient.systems.modules.misc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.discordipc.DiscordIPC;
import meteordevelopment.meteorclient.discordipc.RichPresence;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.utils.StarscriptTextBoxRenderer;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringListSetting;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.starscript.Script;
import net.minecraft.class_156;
import net.minecraft.class_3545;
import net.minecraft.class_3928;
import net.minecraft.class_404;
import net.minecraft.class_412;
import net.minecraft.class_4189;
import net.minecraft.class_420;
import net.minecraft.class_422;
import net.minecraft.class_426;
import net.minecraft.class_429;
import net.minecraft.class_440;
import net.minecraft.class_442;
import net.minecraft.class_443;
import net.minecraft.class_445;
import net.minecraft.class_446;
import net.minecraft.class_458;
import net.minecraft.class_4905;
import net.minecraft.class_500;
import net.minecraft.class_5235;
import net.minecraft.class_524;
import net.minecraft.class_525;
import net.minecraft.class_526;
import net.minecraft.class_5375;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/misc/DiscordPresence.class */
public class DiscordPresence extends Module {
    private final SettingGroup sgLine1;
    private final SettingGroup sgLine2;
    private final Setting<List<String>> line1Strings;
    private final Setting<Integer> line1UpdateDelay;
    private final Setting<SelectMode> line1SelectMode;
    private final Setting<List<String>> line2Strings;
    private final Setting<Integer> line2UpdateDelay;
    private final Setting<SelectMode> line2SelectMode;
    private SmallImage currentSmallImage;
    private int ticks;
    private boolean forceUpdate;
    private boolean lastWasInMainMenu;
    private final List<Script> line1Scripts;
    private int line1Ticks;
    private int line1I;
    private final List<Script> line2Scripts;
    private int line2Ticks;
    private int line2I;
    private static final RichPresence rpc = new RichPresence();
    public static final List<class_3545<String, String>> customStates = new ArrayList();

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/misc/DiscordPresence$SelectMode.class */
    public enum SelectMode {
        Random,
        Sequential
    }

    static {
        registerCustomState("com.terraformersmc.modmenu.gui", "Browsing mods");
        registerCustomState("me.jellysquid.mods.sodium.client", "Changing options");
    }

    public DiscordPresence() {
        super(Categories.Misc, "discord-presence", "Displays Mizu as your presence on Discord.");
        this.sgLine1 = this.settings.createGroup("Line 1");
        this.sgLine2 = this.settings.createGroup("Line 2");
        this.line1Strings = this.sgLine1.add(new StringListSetting.Builder().name("line-1-messages").description("Messages used for the first line.").defaultValue("{player}", "{server}").onChanged(strings -> {
            recompileLine1();
        }).renderer(StarscriptTextBoxRenderer.class).build());
        this.line1UpdateDelay = this.sgLine1.add(new IntSetting.Builder().name("line-1-update-delay").description("How fast to update the first line in ticks.").defaultValue(200).min(10).sliderRange(10, 200).build());
        this.line1SelectMode = this.sgLine1.add(new EnumSetting.Builder().name("line-1-select-mode").description("How to select messages for the first line.").defaultValue(SelectMode.Sequential).build());
        this.line2Strings = this.sgLine2.add(new StringListSetting.Builder().name("line-2-messages").description("Messages used for the second line.").defaultValue("Minecraft on Meth!", "{round(server.tps, 1)} TPS", "Playing on {server.difficulty} difficulty.", "{server.player_count} Players online").onChanged(strings2 -> {
            recompileLine2();
        }).renderer(StarscriptTextBoxRenderer.class).build());
        this.line2UpdateDelay = this.sgLine2.add(new IntSetting.Builder().name("line-2-update-delay").description("How fast to update the second line in ticks.").defaultValue(60).min(10).sliderRange(10, 200).build());
        this.line2SelectMode = this.sgLine2.add(new EnumSetting.Builder().name("line-2-select-mode").description("How to select messages for the second line.").defaultValue(SelectMode.Sequential).build());
        this.line1Scripts = new ArrayList();
        this.line2Scripts = new ArrayList();
        this.runInMainMenu = true;
    }

    public static void registerCustomState(String packageName, String state) {
        for (class_3545<String, String> pair : customStates) {
            if (((String) pair.method_15442()).equals(packageName)) {
                pair.method_34965(state);
                return;
            }
        }
        customStates.add(new class_3545<>(packageName, state));
    }

    public static void unregisterCustomState(String packageName) {
        customStates.removeIf(pair -> {
            return ((String) pair.method_15442()).equals(packageName);
        });
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        DiscordIPC.stop();
        DiscordIPC.start(1492306835724697781L, null);
        rpc.setStart(System.currentTimeMillis() / 1000);
        String largeText = "%s %s".formatted(MeteorClient.NAME, MeteorClient.VERSION);
        if (!MeteorClient.BUILD_NUMBER.isEmpty()) {
            largeText = largeText + " Build: " + MeteorClient.BUILD_NUMBER;
        }
        rpc.setLargeImage("overlay", largeText);
        this.currentSmallImage = SmallImage.Swavez;
        recompileLine1();
        recompileLine2();
        this.ticks = 0;
        this.line1Ticks = 0;
        this.line2Ticks = 0;
        this.lastWasInMainMenu = false;
        this.line1I = 0;
        this.line2I = 0;
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onDeactivate() {
        DiscordIPC.stop();
    }

    private void recompile(List<String> messages, List<Script> scripts) {
        scripts.clear();
        for (String message : messages) {
            Script script = MeteorStarscript.compile(message);
            if (script != null) {
                scripts.add(script);
            }
        }
        this.forceUpdate = true;
    }

    private void recompileLine1() {
        recompile(this.line1Strings.get(), this.line1Scripts);
    }

    private void recompileLine2() {
        recompile(this.line2Strings.get(), this.line2Scripts);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        boolean update = false;
        if (this.ticks >= 200 || this.forceUpdate) {
            this.currentSmallImage = this.currentSmallImage.next();
            this.currentSmallImage.apply();
            update = true;
            this.ticks = 0;
        } else {
            this.ticks++;
        }
        if (Utils.canUpdate()) {
            if (this.line1Ticks >= this.line1UpdateDelay.get().intValue() || this.forceUpdate) {
                if (!this.line1Scripts.isEmpty()) {
                    int i = Utils.random(0, this.line1Scripts.size());
                    if (this.line1SelectMode.get() == SelectMode.Sequential) {
                        if (this.line1I >= this.line1Scripts.size()) {
                            this.line1I = 0;
                        }
                        int i2 = this.line1I;
                        this.line1I = i2 + 1;
                        i = i2;
                    }
                    String message = MeteorStarscript.run(this.line1Scripts.get(i));
                    if (message != null) {
                        rpc.setDetails(message);
                    }
                }
                update = true;
                this.line1Ticks = 0;
            } else {
                this.line1Ticks++;
            }
            if (this.line2Ticks >= this.line2UpdateDelay.get().intValue() || this.forceUpdate) {
                if (!this.line2Scripts.isEmpty()) {
                    int i3 = Utils.random(0, this.line2Scripts.size());
                    if (this.line2SelectMode.get() == SelectMode.Sequential) {
                        if (this.line2I >= this.line2Scripts.size()) {
                            this.line2I = 0;
                        }
                        int i4 = this.line2I;
                        this.line2I = i4 + 1;
                        i3 = i4;
                    }
                    String message2 = MeteorStarscript.run(this.line2Scripts.get(i3));
                    if (message2 != null) {
                        rpc.setState(message2);
                    }
                }
                update = true;
                this.line2Ticks = 0;
            } else {
                this.line2Ticks++;
            }
        } else if (!this.lastWasInMainMenu) {
            rpc.setDetails(MeteorClient.NAME + " " + String.valueOf(MeteorClient.BUILD_NUMBER.isEmpty() ? MeteorClient.VERSION : String.valueOf(MeteorClient.VERSION) + " " + MeteorClient.BUILD_NUMBER));
            if (this.mc.field_1755 instanceof class_442) {
                rpc.setState("Looking at title screen");
            } else if (this.mc.field_1755 instanceof class_526) {
                rpc.setState("Selecting world");
            } else if ((this.mc.field_1755 instanceof class_525) || (this.mc.field_1755 instanceof class_5235)) {
                rpc.setState("Creating world");
            } else if (this.mc.field_1755 instanceof class_524) {
                rpc.setState("Editing world");
            } else if (this.mc.field_1755 instanceof class_3928) {
                rpc.setState("Loading world");
            } else if (this.mc.field_1755 instanceof class_500) {
                rpc.setState("Selecting server");
            } else if (this.mc.field_1755 instanceof class_422) {
                rpc.setState("Adding server");
            } else if ((this.mc.field_1755 instanceof class_412) || (this.mc.field_1755 instanceof class_420)) {
                rpc.setState("Connecting to server");
            } else if (this.mc.field_1755 instanceof WidgetScreen) {
                rpc.setState("Browsing Mizu's GUI");
            } else if ((this.mc.field_1755 instanceof class_429) || (this.mc.field_1755 instanceof class_440) || (this.mc.field_1755 instanceof class_443) || (this.mc.field_1755 instanceof class_446) || (this.mc.field_1755 instanceof class_458) || (this.mc.field_1755 instanceof class_426) || (this.mc.field_1755 instanceof class_404) || (this.mc.field_1755 instanceof class_5375) || (this.mc.field_1755 instanceof class_4189)) {
                rpc.setState("Changing options");
            } else if (this.mc.field_1755 instanceof class_445) {
                rpc.setState("Reading credits");
            } else if (this.mc.field_1755 instanceof class_4905) {
                rpc.setState("Browsing Realms");
            } else {
                boolean setState = false;
                if (this.mc.field_1755 != null) {
                    String className = this.mc.field_1755.getClass().getName();
                    Iterator<class_3545<String, String>> it = customStates.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        class_3545<String, String> pair = it.next();
                        if (className.startsWith((String) pair.method_15442())) {
                            rpc.setState((String) pair.method_15441());
                            setState = true;
                            break;
                        }
                    }
                }
                if (!setState) {
                    rpc.setState("In main menu");
                }
            }
            update = true;
        }
        if (update) {
            DiscordIPC.setActivity(rpc);
        }
        this.forceUpdate = false;
        this.lastWasInMainMenu = !Utils.canUpdate();
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        if (!Utils.canUpdate()) {
            this.lastWasInMainMenu = false;
        }
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public WWidget getWidget(GuiTheme theme) {
        WButton help = theme.button("Open documentation.");
        help.action = () -> {
            class_156.method_668().method_670("https://github.com/MeteorDevelopment/meteor-client/wiki/Starscript");
        };
        return help;
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/misc/DiscordPresence$SmallImage.class */
    private enum SmallImage {
        Swavez("swavez", "swavez");

        private final String key;
        private final String text;

        SmallImage(String key, String text) {
            this.key = key;
            this.text = text;
        }

        void apply() {
            DiscordPresence.rpc.setSmallImage(this.key, this.text);
        }

        SmallImage next() {
            return Swavez;
        }
    }
}
