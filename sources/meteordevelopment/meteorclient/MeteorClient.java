package meteordevelopment.meteorclient;

import java.io.File;
import java.lang.invoke.MethodHandles;
import meteordevelopment.meteorclient.addons.AddonManager;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.meteorclient.events.meteor.MouseButtonEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.newgui.NewGuiScreen;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.hud.screens.HudEditorScreen;
import meteordevelopment.meteorclient.systems.managers.BlockPlacementManager;
import meteordevelopment.meteorclient.systems.managers.InformationManager;
import meteordevelopment.meteorclient.systems.managers.RotationManager;
import meteordevelopment.meteorclient.systems.managers.SwapManager;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.DiscordPresence;
import meteordevelopment.meteorclient.utils.PostInit;
import meteordevelopment.meteorclient.utils.PreInit;
import meteordevelopment.meteorclient.utils.ReflectInit;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.Version;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.meteorclient.utils.misc.input.KeyBinds;
import meteordevelopment.meteorclient.utils.network.OnlinePlayers;
import meteordevelopment.orbit.EventBus;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import meteordevelopment.orbit.IEventBus;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_408;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.MixinEnvironment;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/MeteorClient.class */
public class MeteorClient implements ClientModInitializer {
    public static final ModMetadata MOD_META;
    public static final String NAME;
    public static final Version VERSION;
    public static final String BUILD_NUMBER;
    public static MeteorClient INSTANCE;
    public static MeteorAddon ADDON;
    public static class_310 mc;
    public static final Logger LOG;
    public static RotationManager ROTATION;
    public static InformationManager INFO;
    public static SwapManager SWAP;
    public static BlockPlacementManager BLOCK;
    private boolean wasWidgetScreen;
    private boolean wasHudHiddenRoot;
    public static final IEventBus EVENT_BUS = new EventBus();
    public static final String MOD_ID = "meteor-client";
    public static final File FOLDER = FabricLoader.getInstance().getGameDir().resolve(MOD_ID).toFile();

    static {
        try {
            EVENT_BUS.registerLambdaFactory("meteordevelopment.meteorclient", (lookupInMethod, klass) -> {
                return (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup());
            });
            MOD_META = ((ModContainer) FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow()).getMetadata();
            NAME = MOD_META.getName();
            LOG = LoggerFactory.getLogger(NAME);
            String versionString = MOD_META.getVersion().getFriendlyString();
            if (versionString.contains("-")) {
                versionString = versionString.split("-")[0];
            }
            if (versionString.equals("${version}")) {
                versionString = "0.0.0";
            }
            VERSION = new Version(versionString);
            BUILD_NUMBER = MOD_META.getCustomValue("meteor-client:build_number").getAsString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to register lambda factory for meteorclient", e);
        }
    }

    public void onInitializeClient() {
        if (INSTANCE == null) {
            INSTANCE = this;
            return;
        }
        mc = class_310.method_1551();
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            LOG.info("Force loading mixins");
            MixinEnvironment.getCurrentEnvironment().audit();
        }
        LOG.info("Initializing {}", NAME);
        if (!FOLDER.exists()) {
            FOLDER.getParentFile().mkdirs();
            FOLDER.mkdir();
            Systems.addPreLoadTask(() -> {
                ((DiscordPresence) Modules.get().get(DiscordPresence.class)).toggle();
            });
        }
        AddonManager.init();
        AddonManager.ADDONS.forEach(addon -> {
            try {
                EVENT_BUS.registerLambdaFactory(addon.getPackage(), (lookupInMethod, klass) -> {
                    return (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup());
                });
            } catch (AbstractMethodError e) {
                throw new RuntimeException("Addon \"%s\" is too old and cannot be ran.".formatted(addon.name), e);
            }
        });
        ReflectInit.registerPackages();
        ReflectInit.init(PreInit.class);
        Categories.init();
        Systems.init();
        ROTATION = new RotationManager();
        INFO = new InformationManager();
        SWAP = new SwapManager();
        BLOCK = new BlockPlacementManager();
        EVENT_BUS.subscribe(this);
        AddonManager.ADDONS.forEach((v0) -> {
            v0.onInitialize();
        });
        Modules.get().sortModules();
        Systems.load();
        ReflectInit.init(PostInit.class);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            OnlinePlayers.leave();
            Systems.save();
            GuiThemes.save();
        }));
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.field_1755 == null && mc.method_18506() == null && KeyBinds.OPEN_COMMANDS.method_1436()) {
            mc.method_1507(new class_408(Config.get().prefix.get()));
        }
    }

    @EventHandler
    private void onKey(KeyEvent event) {
        if (event.action == KeyAction.Press && KeyBinds.OPEN_GUI.method_1417(event.key, 0)) {
            toggleGui();
        }
    }

    @EventHandler
    private void onMouseButton(MouseButtonEvent event) {
        if (event.action == KeyAction.Press && KeyBinds.OPEN_GUI.method_1433(event.button)) {
            toggleGui();
        }
    }

    private void toggleGui() {
        if (!Utils.canCloseGui()) {
            if (Utils.canOpenGui()) {
                mc.method_1507(new NewGuiScreen());
                return;
            }
            return;
        }
        mc.field_1755.method_25419();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onOpenScreen(OpenScreenEvent event) {
        if (event.screen instanceof WidgetScreen) {
            if (!this.wasWidgetScreen) {
                this.wasHudHiddenRoot = mc.field_1690.field_1842;
            }
            if (GuiThemes.get().hideHUD() || this.wasHudHiddenRoot) {
                mc.field_1690.field_1842 = !(event.screen instanceof HudEditorScreen);
            }
        } else {
            if (this.wasWidgetScreen) {
                mc.field_1690.field_1842 = this.wasHudHiddenRoot;
            }
            this.wasHudHiddenRoot = mc.field_1690.field_1842;
        }
        this.wasWidgetScreen = event.screen instanceof WidgetScreen;
    }

    public static class_2960 identifier(String path) {
        return class_2960.method_60655(MOD_ID, path);
    }
}
