package meteordevelopment.meteorclient.systems.modules;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.meteor.ActiveModulesChangedEvent;
import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.meteorclient.events.meteor.ModuleBindChangedEvent;
import meteordevelopment.meteorclient.events.meteor.MouseButtonEvent;
import meteordevelopment.meteorclient.pathing.BaritoneUtils;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.combat.AnchorAura;
import meteordevelopment.meteorclient.systems.modules.combat.AntiAnchor;
import meteordevelopment.meteorclient.systems.modules.combat.AntiAnvil;
import meteordevelopment.meteorclient.systems.modules.combat.AntiBed;
import meteordevelopment.meteorclient.systems.modules.combat.AntiDigDown;
import meteordevelopment.meteorclient.systems.modules.combat.AntiMace;
import meteordevelopment.meteorclient.systems.modules.combat.ArrowDodge;
import meteordevelopment.meteorclient.systems.modules.combat.AutoAnvil;
import meteordevelopment.meteorclient.systems.modules.combat.AutoArmor;
import meteordevelopment.meteorclient.systems.modules.combat.AutoEXP;
import meteordevelopment.meteorclient.systems.modules.combat.AutoMine;
import meteordevelopment.meteorclient.systems.modules.combat.AutoTrap;
import meteordevelopment.meteorclient.systems.modules.combat.AutoWeapon;
import meteordevelopment.meteorclient.systems.modules.combat.AutoWeb;
import meteordevelopment.meteorclient.systems.modules.combat.BasePlace;
import meteordevelopment.meteorclient.systems.modules.combat.BedAura;
import meteordevelopment.meteorclient.systems.modules.combat.BowAimbot;
import meteordevelopment.meteorclient.systems.modules.combat.BowSpam;
import meteordevelopment.meteorclient.systems.modules.combat.Burrow;
import meteordevelopment.meteorclient.systems.modules.combat.ChineseAura;
import meteordevelopment.meteorclient.systems.modules.combat.Criticals;
import meteordevelopment.meteorclient.systems.modules.combat.ForceSwim;
import meteordevelopment.meteorclient.systems.modules.combat.Hitboxes;
import meteordevelopment.meteorclient.systems.modules.combat.HoleFiller;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura;
import meteordevelopment.meteorclient.systems.modules.combat.MaceAura;
import meteordevelopment.meteorclient.systems.modules.combat.MaceDive;
import meteordevelopment.meteorclient.systems.modules.combat.Offhand;
import meteordevelopment.meteorclient.systems.modules.combat.PearlPhase;
import meteordevelopment.meteorclient.systems.modules.combat.SelfAnvil;
import meteordevelopment.meteorclient.systems.modules.combat.Surround;
import meteordevelopment.meteorclient.systems.modules.combat.SwordAura;
import meteordevelopment.meteorclient.systems.modules.combat.autocrystal.AutoCrystal;
import meteordevelopment.meteorclient.systems.modules.gui.AntiCheat;
import meteordevelopment.meteorclient.systems.modules.gui.Gui;
import meteordevelopment.meteorclient.systems.modules.gui.Hud;
import meteordevelopment.meteorclient.systems.modules.hunting.AFKVanillaFly;
import meteordevelopment.meteorclient.systems.modules.hunting.AutoEXPPlus;
import meteordevelopment.meteorclient.systems.modules.hunting.AutoLogPlus;
import meteordevelopment.meteorclient.systems.modules.hunting.AutoPortal;
import meteordevelopment.meteorclient.systems.modules.hunting.AutoRegear;
import meteordevelopment.meteorclient.systems.modules.hunting.BaritoneElytraGoto;
import meteordevelopment.meteorclient.systems.modules.hunting.BetterStashFinder;
import meteordevelopment.meteorclient.systems.modules.hunting.DiscordNotifs;
import meteordevelopment.meteorclient.systems.modules.hunting.ElytraFlyPlusPlus;
import meteordevelopment.meteorclient.systems.modules.hunting.GotoPosition;
import meteordevelopment.meteorclient.systems.modules.hunting.HighlightOldLava;
import meteordevelopment.meteorclient.systems.modules.hunting.Pitch40Util;
import meteordevelopment.meteorclient.systems.modules.hunting.TrailFollower;
import meteordevelopment.meteorclient.systems.modules.hunting.VanityESP;
import meteordevelopment.meteorclient.systems.modules.misc.AdBlocker;
import meteordevelopment.meteorclient.systems.modules.misc.AntiPacketKick;
import meteordevelopment.meteorclient.systems.modules.misc.AutoLog;
import meteordevelopment.meteorclient.systems.modules.misc.AutoReconnect;
import meteordevelopment.meteorclient.systems.modules.misc.AutoRespawn;
import meteordevelopment.meteorclient.systems.modules.misc.BetterBeacons;
import meteordevelopment.meteorclient.systems.modules.misc.BetterChat;
import meteordevelopment.meteorclient.systems.modules.misc.ChatFilterBypass;
import meteordevelopment.meteorclient.systems.modules.misc.ChatSigns;
import meteordevelopment.meteorclient.systems.modules.misc.DiscordPresence;
import meteordevelopment.meteorclient.systems.modules.misc.InventoryTweaks;
import meteordevelopment.meteorclient.systems.modules.misc.Notebot;
import meteordevelopment.meteorclient.systems.modules.misc.Notifier;
import meteordevelopment.meteorclient.systems.modules.misc.PacketCanceller;
import meteordevelopment.meteorclient.systems.modules.misc.Spam;
import meteordevelopment.meteorclient.systems.modules.movement.AntiAFK;
import meteordevelopment.meteorclient.systems.modules.movement.AutoWalk;
import meteordevelopment.meteorclient.systems.modules.movement.AutoWasp;
import meteordevelopment.meteorclient.systems.modules.movement.ElytraLaunch;
import meteordevelopment.meteorclient.systems.modules.movement.EntityControl;
import meteordevelopment.meteorclient.systems.modules.movement.Flight;
import meteordevelopment.meteorclient.systems.modules.movement.GUIMove;
import meteordevelopment.meteorclient.systems.modules.movement.GrimDisabler;
import meteordevelopment.meteorclient.systems.modules.movement.MovementFix;
import meteordevelopment.meteorclient.systems.modules.movement.NoFall;
import meteordevelopment.meteorclient.systems.modules.movement.NoSlow;
import meteordevelopment.meteorclient.systems.modules.movement.Scaffold;
import meteordevelopment.meteorclient.systems.modules.movement.Sneak;
import meteordevelopment.meteorclient.systems.modules.movement.Sprint;
import meteordevelopment.meteorclient.systems.modules.movement.Velocity;
import meteordevelopment.meteorclient.systems.modules.movement.VelocityBypass;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFly;
import meteordevelopment.meteorclient.systems.modules.player.AutoEat;
import meteordevelopment.meteorclient.systems.modules.player.AutoReplenish;
import meteordevelopment.meteorclient.systems.modules.player.AutoTool;
import meteordevelopment.meteorclient.systems.modules.player.BreakDelay;
import meteordevelopment.meteorclient.systems.modules.player.ChestSwap;
import meteordevelopment.meteorclient.systems.modules.player.FakePlayer;
import meteordevelopment.meteorclient.systems.modules.player.FastUse;
import meteordevelopment.meteorclient.systems.modules.player.HotbarLock;
import meteordevelopment.meteorclient.systems.modules.player.InstantRebreak;
import meteordevelopment.meteorclient.systems.modules.player.LiquidInteract;
import meteordevelopment.meteorclient.systems.modules.player.MiddleClickExtra;
import meteordevelopment.meteorclient.systems.modules.player.Multitask;
import meteordevelopment.meteorclient.systems.modules.player.NoRotate;
import meteordevelopment.meteorclient.systems.modules.player.NoStatusEffects;
import meteordevelopment.meteorclient.systems.modules.player.OffhandCrash;
import meteordevelopment.meteorclient.systems.modules.player.Rotation;
import meteordevelopment.meteorclient.systems.modules.player.SilentMine;
import meteordevelopment.meteorclient.systems.modules.player.SpeedMine;
import meteordevelopment.meteorclient.systems.modules.render.BetterTab;
import meteordevelopment.meteorclient.systems.modules.render.BetterTooltips;
import meteordevelopment.meteorclient.systems.modules.render.BlockSelection;
import meteordevelopment.meteorclient.systems.modules.render.Blur;
import meteordevelopment.meteorclient.systems.modules.render.BossStack;
import meteordevelopment.meteorclient.systems.modules.render.BreakIndicators;
import meteordevelopment.meteorclient.systems.modules.render.Chams;
import meteordevelopment.meteorclient.systems.modules.render.ESP;
import meteordevelopment.meteorclient.systems.modules.render.EntityOwner;
import meteordevelopment.meteorclient.systems.modules.render.FootprintESP;
import meteordevelopment.meteorclient.systems.modules.render.FreeLook;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import meteordevelopment.meteorclient.systems.modules.render.Fullbright;
import meteordevelopment.meteorclient.systems.modules.render.HandView;
import meteordevelopment.meteorclient.systems.modules.render.HoleESP;
import meteordevelopment.meteorclient.systems.modules.render.LogoutSpots;
import meteordevelopment.meteorclient.systems.modules.render.Nametags;
import meteordevelopment.meteorclient.systems.modules.render.NoRender;
import meteordevelopment.meteorclient.systems.modules.render.PhaseESP;
import meteordevelopment.meteorclient.systems.modules.render.PopChams;
import meteordevelopment.meteorclient.systems.modules.render.StorageESP;
import meteordevelopment.meteorclient.systems.modules.render.Tracers;
import meteordevelopment.meteorclient.systems.modules.render.Trajectories;
import meteordevelopment.meteorclient.systems.modules.render.TunnelESP;
import meteordevelopment.meteorclient.systems.modules.render.UnfocusedCPU;
import meteordevelopment.meteorclient.systems.modules.render.WaypointsModule;
import meteordevelopment.meteorclient.systems.modules.render.Xray;
import meteordevelopment.meteorclient.systems.modules.render.Zoom;
import meteordevelopment.meteorclient.systems.modules.render.blockesp.BlockESP;
import meteordevelopment.meteorclient.systems.modules.world.AirPlace;
import meteordevelopment.meteorclient.systems.modules.world.Ambience;
import meteordevelopment.meteorclient.systems.modules.world.AutoMount;
import meteordevelopment.meteorclient.systems.modules.world.AutoSign;
import meteordevelopment.meteorclient.systems.modules.world.BuildHeight;
import meteordevelopment.meteorclient.systems.modules.world.EChestFarmer;
import meteordevelopment.meteorclient.systems.modules.world.Excavator;
import meteordevelopment.meteorclient.systems.modules.world.InfinityMiner;
import meteordevelopment.meteorclient.systems.modules.world.LiquidFiller;
import meteordevelopment.meteorclient.systems.modules.world.NoGhostBlocks;
import meteordevelopment.meteorclient.systems.modules.world.Nuker;
import meteordevelopment.meteorclient.systems.modules.world.PacketMine;
import meteordevelopment.meteorclient.systems.modules.world.StashFinder;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.misc.ValueComparableMap;
import meteordevelopment.meteorclient.utils.misc.input.Input;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.orbit.EventHandler;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_2487;
import net.minecraft.class_2499;
import net.minecraft.class_2520;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/Modules.class */
public class Modules extends System<Modules> {
    private static final List<Category> CATEGORIES = new ArrayList();
    private final List<Module> modules;
    private final Map<Class<? extends Module>, Module> moduleInstances;
    private final Map<Category, List<Module>> groups;
    private final List<Module> active;
    private Module moduleToBind;
    private boolean awaitingKeyRelease;

    public Modules() {
        super("modules");
        this.modules = new ArrayList();
        this.moduleInstances = new Reference2ReferenceOpenHashMap();
        this.groups = new Reference2ReferenceOpenHashMap();
        this.active = new ArrayList();
        this.awaitingKeyRelease = false;
    }

    public static Modules get() {
        return (Modules) Systems.get(Modules.class);
    }

    @Override // meteordevelopment.meteorclient.systems.System
    public void init() {
        initCombat();
        initPlayer();
        initMovement();
        initRender();
        initWorld();
        initMisc();
        initHunting();
        initGui();
    }

    @Override // meteordevelopment.meteorclient.systems.System
    public void load(File folder) {
        for (Module module : getAll()) {
            for (SettingGroup group : module.settings) {
                for (Setting<?> setting : group) {
                    setting.reset();
                }
            }
        }
        super.load(folder);
    }

    public void sortModules() {
        for (List<Module> modules : this.groups.values()) {
            modules.sort(Comparator.comparing(o -> {
                return o.title;
            }));
        }
        this.modules.sort(Comparator.comparing(o2 -> {
            return o2.title;
        }));
    }

    public static void registerCategory(Category category) {
        if (!Categories.REGISTERING) {
            throw new RuntimeException("Modules.registerCategory - Cannot register category outside of onRegisterCategories callback.");
        }
        CATEGORIES.add(category);
    }

    public static Iterable<Category> loopCategories() {
        return CATEGORIES;
    }

    public <T extends Module> T get(Class<T> klass) {
        return (T) this.moduleInstances.get(klass);
    }

    public Module get(String name) {
        for (Module module : this.moduleInstances.values()) {
            if (module.name.equalsIgnoreCase(name)) {
                return module;
            }
        }
        return null;
    }

    public boolean isActive(Class<? extends Module> klass) {
        Module module = get((Class<Module>) klass);
        return module != null && module.isActive();
    }

    public List<Module> getGroup(Category category) {
        return this.groups.computeIfAbsent(category, category1 -> {
            return new ArrayList();
        });
    }

    public Collection<Module> getAll() {
        return this.moduleInstances.values();
    }

    @Deprecated(forRemoval = true)
    public List<Module> getList() {
        return this.modules;
    }

    public int getCount() {
        return this.moduleInstances.size();
    }

    public List<Module> getActive() {
        List<Module> list;
        synchronized (this.active) {
            list = this.active;
        }
        return list;
    }

    public Set<Module> searchTitles(String text) {
        Map<Module, Integer> modules = new ValueComparableMap<>(Comparator.naturalOrder());
        for (Module module : this.moduleInstances.values()) {
            int score = Utils.searchLevenshteinDefault(module.title, text, false);
            if (Config.get().moduleAliases.get().booleanValue()) {
                for (String alias : module.aliases) {
                    int aliasScore = Utils.searchLevenshteinDefault(alias, text, false);
                    if (aliasScore < score) {
                        score = aliasScore;
                    }
                }
            }
            modules.put(module, Integer.valueOf(modules.getOrDefault(module, 0).intValue() + score));
        }
        return modules.keySet();
    }

    public Set<Module> searchSettingTitles(String text) {
        Map<Module, Integer> modules = new ValueComparableMap<>(Comparator.naturalOrder());
        for (Module module : this.moduleInstances.values()) {
            int lowest = Integer.MAX_VALUE;
            for (SettingGroup sg : module.settings) {
                for (Setting<?> setting : sg) {
                    int score = Utils.searchLevenshteinDefault(setting.title, text, false);
                    if (score < lowest) {
                        lowest = score;
                    }
                }
            }
            modules.put(module, Integer.valueOf(modules.getOrDefault(module, 0).intValue() + lowest));
        }
        return modules.keySet();
    }

    void addActive(Module module) {
        synchronized (this.active) {
            if (!this.active.contains(module)) {
                this.active.add(module);
                MeteorClient.EVENT_BUS.post(ActiveModulesChangedEvent.get());
            }
        }
    }

    void removeActive(Module module) {
        synchronized (this.active) {
            if (this.active.remove(module)) {
                MeteorClient.EVENT_BUS.post(ActiveModulesChangedEvent.get());
            }
        }
    }

    public void setModuleToBind(Module moduleToBind) {
        this.moduleToBind = moduleToBind;
    }

    public void awaitKeyRelease() {
        this.awaitingKeyRelease = true;
    }

    public boolean isBinding() {
        return this.moduleToBind != null;
    }

    @EventHandler(priority = 200)
    private void onKeyBinding(KeyEvent event) {
        if (event.action != KeyAction.Release || !onBinding(true, event.key, event.modifiers)) {
            return;
        }
        event.cancel();
    }

    @EventHandler(priority = 200)
    private void onButtonBinding(MouseButtonEvent event) {
        if (event.action != KeyAction.Release || !onBinding(false, event.button, 0)) {
            return;
        }
        event.cancel();
    }

    private boolean onBinding(boolean isKey, int value, int modifiers) {
        if (!isBinding()) {
            return false;
        }
        if (this.awaitingKeyRelease) {
            if (!isKey) {
                return false;
            }
            if (value != 257 && value != 335) {
                return false;
            }
            this.awaitingKeyRelease = false;
            return false;
        }
        if (this.moduleToBind.keybind.canBindTo(isKey, value, modifiers)) {
            this.moduleToBind.keybind.set(isKey, value, modifiers);
            this.moduleToBind.info("Bound to (highlight)%s(default).", this.moduleToBind.keybind);
        } else if (value == 256) {
            this.moduleToBind.keybind.set(Keybind.none());
            this.moduleToBind.info("Removed bind.", new Object[0]);
        } else {
            return false;
        }
        MeteorClient.EVENT_BUS.post(ModuleBindChangedEvent.get(this.moduleToBind));
        this.moduleToBind = null;
        return true;
    }

    @EventHandler(priority = 100)
    private void onKey(KeyEvent event) {
        if (event.action == KeyAction.Repeat) {
            return;
        }
        onAction(true, event.key, event.modifiers, event.action == KeyAction.Press);
    }

    @EventHandler(priority = 100)
    private void onMouseButton(MouseButtonEvent event) {
        if (event.action == KeyAction.Repeat) {
            return;
        }
        onAction(false, event.button, 0, event.action == KeyAction.Press);
    }

    private void onAction(boolean isKey, int value, int modifiers, boolean isPress) {
        if (MeteorClient.mc.field_1755 != null || Input.isKeyPressed(292)) {
            return;
        }
        for (Module module : this.moduleInstances.values()) {
            if (module.keybind.matches(isKey, value, modifiers) && (isPress || (module.toggleOnBindRelease && module.isActive()))) {
                module.toggle();
                module.sendToggledMsg();
            }
        }
    }

    @EventHandler(priority = Opcode.JSR_W)
    private void onOpenScreen(OpenScreenEvent event) {
        if (Utils.canUpdate()) {
            for (Module module : this.moduleInstances.values()) {
                if (module.toggleOnBindRelease && module.isActive()) {
                    module.toggle();
                    module.sendToggledMsg();
                }
            }
        }
    }

    @EventHandler
    private void onGameJoined(GameJoinedEvent event) {
        synchronized (this.active) {
            for (Module module : getAll()) {
                if (module.isActive() && !module.runInMainMenu) {
                    MeteorClient.EVENT_BUS.subscribe(module);
                    module.onActivate();
                }
            }
        }
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        synchronized (this.active) {
            for (Module module : getAll()) {
                if (module.isActive() && !module.runInMainMenu) {
                    MeteorClient.EVENT_BUS.unsubscribe(module);
                    module.onDeactivate();
                }
            }
        }
    }

    public void disableAll() {
        synchronized (this.active) {
            for (Module module : getAll()) {
                if (module.isActive()) {
                    module.toggle();
                }
            }
        }
    }

    @Override // meteordevelopment.meteorclient.systems.System, meteordevelopment.meteorclient.utils.misc.ISerializable
    public class_2487 toTag() {
        class_2487 tag = new class_2487();
        class_2499 modulesTag = new class_2499();
        for (Module module : getAll()) {
            class_2487 moduleTag = module.toTag();
            if (moduleTag != null) {
                modulesTag.add(moduleTag);
            }
        }
        tag.method_10566("modules", modulesTag);
        return tag;
    }

    @Override // meteordevelopment.meteorclient.systems.System, meteordevelopment.meteorclient.utils.misc.ISerializable
    /* JADX INFO: renamed from: fromTag */
    public Modules fromTag2(class_2487 tag) {
        disableAll();
        class_2499<class_2520> modulesTag = tag.method_68569("modules");
        for (class_2520 moduleTagI : modulesTag) {
            class_2487 moduleTag = (class_2487) moduleTagI;
            Module module = get(moduleTag.method_68564("name", ""));
            if (module != null) {
                module.fromTag2(moduleTag);
            }
        }
        return this;
    }

    public void add(Module module) {
        if (!CATEGORIES.contains(module.category)) {
            throw new RuntimeException("Modules.addModule - Module's category was not registered.");
        }
        AtomicReference atomicReference = new AtomicReference();
        if (this.moduleInstances.values().removeIf(module1 -> {
            if (module1.name.equals(module.name)) {
                atomicReference.set(module1);
                module1.settings.unregisterColorSettings();
                return true;
            }
            return false;
        })) {
            getGroup(((Module) atomicReference.get()).category).remove(atomicReference.get());
        }
        this.moduleInstances.put((Class<? extends Module>) module.getClass(), module);
        this.modules.add(module);
        getGroup(module.category).add(module);
        module.settings.registerColorSettings(module);
    }

    private void initCombat() {
        add(new AnchorAura());
        add(new AntiAnchor());
        add(new MaceDive());
        add(new AntiMace());
        add(new AntiAnvil());
        add(new AntiBed());
        add(new AntiDigDown());
        add(new ArrowDodge());
        add(new AutoAnvil());
        add(new AutoArmor());
        add(new AutoCrystal());
        add(new AutoEXP());
        add(new AutoMine());
        add(new AutoTrap());
        add(new AutoWeapon());
        add(new AutoWeb());
        add(new BasePlace());
        add(new BedAura());
        add(new BowAimbot());
        add(new BowSpam());
        add(new Burrow());
        add(new ChineseAura());
        add(new Criticals());
        add(new ForceSwim());
        add(new Hitboxes());
        add(new HoleFiller());
        add(new KillAura());
        add(new MaceAura());
        add(new Offhand());
        add(new PearlPhase());
        add(new SelfAnvil());
        add(new Surround());
        add(new SwordAura());
    }

    private void initPlayer() {
        add(new AutoEat());
        add(new AutoReplenish());
        add(new AutoTool());
        add(new BreakDelay());
        add(new ChestSwap());
        add(new FakePlayer());
        add(new FastUse());
        add(new HotbarLock());
        add(new InstantRebreak());
        add(new LiquidInteract());
        add(new MiddleClickExtra());
        add(new Multitask());
        add(new NoRotate());
        add(new NoStatusEffects());
        add(new OffhandCrash());
        add(new Rotation());
        add(new SilentMine());
        add(new SpeedMine());
    }

    private void initMovement() {
        add(new AntiAFK());
        add(new AutoWalk());
        add(new AutoWasp());
        add(new ElytraFly());
        add(new ElytraLaunch());
        add(new EntityControl());
        add(new Flight());
        add(new GrimDisabler());
        add(new GUIMove());
        add(new MovementFix());
        add(new NoFall());
        add(new NoSlow());
        add(new Scaffold());
        add(new Sneak());
        add(new Sprint());
        add(new Velocity());
        add(new VelocityBypass());
    }

    private void initRender() {
        add(new BetterTab());
        add(new BetterTooltips());
        add(new BlockESP());
        add(new BlockSelection());
        add(new Blur());
        add(new BossStack());
        add(new BreakIndicators());
        add(new Chams());
        add(new EntityOwner());
        add(new ESP());
        add(new FootprintESP());
        add(new Freecam());
        add(new FreeLook());
        add(new Fullbright());
        add(new HandView());
        add(new HoleESP());
        add(new LogoutSpots());
        add(new Nametags());
        add(new NoRender());
        add(new PhaseESP());
        add(new PopChams());
        add(new StorageESP());
        add(new Tracers());
        add(new Trajectories());
        add(new TunnelESP());
        add(new UnfocusedCPU());
        add(new WaypointsModule());
        add(new Xray());
        add(new Zoom());
    }

    private void initWorld() {
        add(new AirPlace());
        add(new Ambience());
        add(new AutoMount());
        add(new AutoSign());
        add(new BuildHeight());
        add(new EChestFarmer());
        add(new LiquidFiller());
        add(new NoGhostBlocks());
        add(new Nuker());
        add(new PacketMine());
        add(new StashFinder());
        add(new Timer());
        if (BaritoneUtils.IS_AVAILABLE) {
            add(new Excavator());
            add(new InfinityMiner());
        }
    }

    private void initMisc() {
        add(new AdBlocker());
        add(new AntiPacketKick());
        add(new AutoLog());
        add(new AutoReconnect());
        add(new ChatFilterBypass());
        add(new AutoRespawn());
        add(new BetterBeacons());
        add(new BetterChat());
        add(new ChatSigns());
        add(new DiscordPresence());
        add(new InventoryTweaks());
        add(new Notebot());
        add(new Notifier());
        add(new PacketCanceller());
        add(new Spam());
    }

    private void initHunting() {
        add(new AFKVanillaFly());
        add(new AutoEXPPlus());
        add(new AutoLogPlus());
        add(new AutoPortal());
        if (FabricLoader.getInstance().isModLoaded("baritone")) {
            try {
                add(new AutoRegear());
                add(new BaritoneElytraGoto());
                add(new ElytraFlyPlusPlus());
                add(new TrailFollower());
            } catch (Throwable e) {
                MeteorClient.LOG.error("Failed to load Baritone-dependent hunting modules", e);
            }
        }
        if (FabricLoader.getInstance().isModLoaded("xaerominimap")) {
            try {
                add(new BetterStashFinder());
            } catch (Throwable e2) {
                MeteorClient.LOG.error("Failed to load Xaero's Minimap-dependent hunting modules", e2);
            }
        }
        add(new DiscordNotifs());
        add(new GotoPosition());
        add(new HighlightOldLava());
        add(new Pitch40Util());
        add(new VanityESP());
    }

    private void initGui() {
        add(new Gui());
        add(new AntiCheat());
        add(new Hud());
    }
}
