package meteordevelopment.meteorclient.systems.hud;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.meteor.CustomFontChangedEvent;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorListSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.elements.ActiveModulesHud;
import meteordevelopment.meteorclient.systems.hud.elements.ArmorHud;
import meteordevelopment.meteorclient.systems.hud.elements.AutoCrystalHud;
import meteordevelopment.meteorclient.systems.hud.elements.BindsHud;
import meteordevelopment.meteorclient.systems.hud.elements.CombatHud;
import meteordevelopment.meteorclient.systems.hud.elements.CompassHud;
import meteordevelopment.meteorclient.systems.hud.elements.EntityCountHud;
import meteordevelopment.meteorclient.systems.hud.elements.HoleHud;
import meteordevelopment.meteorclient.systems.hud.elements.InventoryHud;
import meteordevelopment.meteorclient.systems.hud.elements.ItemHud;
import meteordevelopment.meteorclient.systems.hud.elements.KeystrokesHud;
import meteordevelopment.meteorclient.systems.hud.elements.LagNotifierHud;
import meteordevelopment.meteorclient.systems.hud.elements.MediaHud;
import meteordevelopment.meteorclient.systems.hud.elements.MeteorTextHud;
import meteordevelopment.meteorclient.systems.hud.elements.ModuleInfosHud;
import meteordevelopment.meteorclient.systems.hud.elements.PhaseCompassHud;
import meteordevelopment.meteorclient.systems.hud.elements.PlayerModelHud;
import meteordevelopment.meteorclient.systems.hud.elements.PlayerRadarHud;
import meteordevelopment.meteorclient.systems.hud.elements.PotionTimersHud;
import meteordevelopment.meteorclient.systems.hud.elements.SpeedMineHud;
import meteordevelopment.meteorclient.systems.hud.elements.SpotifyHud;
import meteordevelopment.meteorclient.systems.hud.elements.TargetHud;
import meteordevelopment.meteorclient.systems.hud.elements.TimerHud;
import meteordevelopment.meteorclient.systems.hud.screens.HudEditorScreen;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_2487;
import net.minecraft.class_2499;
import org.jetbrains.annotations.NotNull;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/hud/Hud.class */
public class Hud extends System<Hud> implements Iterable<HudElement> {
    public static final HudGroup GROUP = new HudGroup("Meteor");
    public boolean active;
    public Settings settings;
    public final Map<String, HudElementInfo<?>> infos;
    private final List<HudElement> elements;
    private final SettingGroup sgGeneral;
    private final SettingGroup sgEditor;
    private final SettingGroup sgKeybind;
    private final Setting<Boolean> customFont;
    private final Setting<Boolean> hideInMenus;
    private final Setting<Double> textScale;
    public final Setting<List<SettingColor>> textColors;
    public final Setting<Integer> border;
    public final Setting<Integer> snappingRange;
    private final Setting<Keybind> keybind;
    private boolean resetToDefaultElements;

    public Hud() {
        super("hud");
        this.settings = new Settings();
        this.infos = new TreeMap();
        this.elements = new ArrayList();
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgEditor = this.settings.createGroup("Editor");
        this.sgKeybind = this.settings.createGroup("Bind");
        this.customFont = this.sgGeneral.add(new BoolSetting.Builder().name("custom-font").description("Text will use custom font.").defaultValue(true).onChanged(aBoolean -> {
            for (HudElement element : this.elements) {
                element.onFontChanged();
            }
        }).build());
        this.hideInMenus = this.sgGeneral.add(new BoolSetting.Builder().name("hide-in-menus").description("Hides the meteor hud when in inventory screens or game menus.").defaultValue(false).build());
        this.textScale = this.sgGeneral.add(new DoubleSetting.Builder().name("text-scale").description("Scale of text if not overridden by the element. Synced with the clickgui's text-scale by default.").defaultValue(0.5d).min(0.5d).sliderRange(0.5d, 3.0d).build());
        this.textColors = this.sgGeneral.add(new ColorListSetting.Builder().name("text-colors").description("Colors used for the Text element.").defaultValue(List.of(new SettingColor(), new SettingColor(Opcode.DRETURN, Opcode.DRETURN, Opcode.DRETURN), new SettingColor(25, 225, 25), new SettingColor(225, 25, 25))).build());
        this.border = this.sgEditor.add(new IntSetting.Builder().name("border").description("Space around the edges of the screen.").defaultValue(4).sliderMax(20).build());
        this.snappingRange = this.sgEditor.add(new IntSetting.Builder().name("snapping-range").description("Snapping range in editor.").defaultValue(10).sliderMax(20).build());
        this.keybind = this.sgKeybind.add(new KeybindSetting.Builder().name("bind").defaultValue(Keybind.none()).action(() -> {
            this.active = !this.active;
        }).build());
    }

    public static Hud get() {
        return (Hud) Systems.get(Hud.class);
    }

    @Override // meteordevelopment.meteorclient.systems.System
    public void init() {
        this.settings.registerColorSettings(null);
        register(MeteorTextHud.INFO);
        register(ItemHud.INFO);
        register(InventoryHud.INFO);
        register(CompassHud.INFO);
        register(PhaseCompassHud.INFO);
        register(SpotifyHud.INFO);
        register(MediaHud.INFO);
        register(ArmorHud.INFO);
        register(HoleHud.INFO);
        register(PlayerModelHud.INFO);
        register(ActiveModulesHud.INFO);
        register(LagNotifierHud.INFO);
        register(PlayerRadarHud.INFO);
        register(ModuleInfosHud.INFO);
        register(PotionTimersHud.INFO);
        register(CombatHud.INFO);
        register(AutoCrystalHud.INFO);
        register(TargetHud.INFO);
        register(KeystrokesHud.INFO);
        register(BindsHud.INFO);
        register(TimerHud.INFO);
        register(EntityCountHud.INFO);
        register(SpeedMineHud.INFO);
        if (this.isFirstInit) {
            resetToDefaultElements();
        }
    }

    public void register(HudElementInfo<?> info) {
        this.infos.put(info.name, info);
    }

    private void add(HudElement element, int x, int y, XAnchor xAnchor, YAnchor yAnchor) {
        element.box.setPos(x, y);
        if (xAnchor == null || yAnchor == null) {
            element.box.updateAnchors();
        } else {
            element.box.xAnchor = xAnchor;
            element.box.yAnchor = yAnchor;
        }
        element.settings.registerColorSettings(null);
        this.elements.add(element);
    }

    public void add(HudElementInfo<?> info, int x, int y, XAnchor xAnchor, YAnchor yAnchor) {
        add(info.create(), x, y, xAnchor, yAnchor);
    }

    public void add(HudElementInfo<?> info, int x, int y) {
        add(info, x, y, (XAnchor) null, (YAnchor) null);
    }

    /* JADX WARN: Type inference incomplete: some casts might be missing */
    public void add(HudElementInfo.Preset preset, int i, int i2, XAnchor xAnchor, YAnchor yAnchor) {
        HudElement hudElementCreate = preset.info.create();
        preset.callback.accept((T) hudElementCreate);
        add(hudElementCreate, i, i2, xAnchor, yAnchor);
    }

    public void add(HudElementInfo<?>.Preset preset, int x, int y) {
        add(preset, x, y, (XAnchor) null, (YAnchor) null);
    }

    void remove(HudElement element) {
        element.settings.unregisterColorSettings();
        this.elements.remove(element);
    }

    public void clear() {
        this.elements.clear();
    }

    public void resetToDefaultElements() {
        this.resetToDefaultElements = true;
    }

    private void resetToDefaultElementsImpl() {
        this.elements.clear();
        int h = (int) Math.ceil(HudRenderer.INSTANCE.textHeight(true));
        add(MeteorTextHud.WATERMARK, 4, 4, XAnchor.Left, YAnchor.Top);
        add(MeteorTextHud.FPS, 4, 4 + h, XAnchor.Left, YAnchor.Top);
        add(MeteorTextHud.TPS, 4, 4 + (h * 2), XAnchor.Left, YAnchor.Top);
        add(MeteorTextHud.PING, 4, 4 + (h * 3), XAnchor.Left, YAnchor.Top);
        add(MeteorTextHud.SPEED, 4, 4 + (h * 4), XAnchor.Left, YAnchor.Top);
        add(ActiveModulesHud.INFO, -4, 4, XAnchor.Right, YAnchor.Top);
        add(MeteorTextHud.POSITION, -4, -4, XAnchor.Right, YAnchor.Bottom);
        add(MeteorTextHud.OPPOSITE_POSITION, -4, (-4) - h, XAnchor.Right, YAnchor.Bottom);
        add(MeteorTextHud.ROTATION, -4, (-4) - (h * 2), XAnchor.Right, YAnchor.Bottom);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (Utils.isLoading()) {
            return;
        }
        if (this.resetToDefaultElements) {
            resetToDefaultElementsImpl();
            this.resetToDefaultElements = false;
        }
        if (this.active || HudEditorScreen.isOpen()) {
            for (HudElement element : this.elements) {
                if (element.isActive()) {
                    element.tick(HudRenderer.INSTANCE);
                }
            }
        }
    }

    @EventHandler
    private void onRender(Render2DEvent event) {
        if (Utils.isLoading() || !this.active || shouldHideHud()) {
            return;
        }
        if ((MeteorClient.mc.field_1690.field_1842 || MeteorClient.mc.field_1705.method_53531().method_53536()) && !HudEditorScreen.isOpen()) {
            return;
        }
        HudRenderer.INSTANCE.begin(event.drawContext);
        for (HudElement element : this.elements) {
            element.updatePos();
            if (element.isActive()) {
                element.render(HudRenderer.INSTANCE);
            }
        }
        HudRenderer.INSTANCE.end();
    }

    private boolean shouldHideHud() {
        return (!this.hideInMenus.get().booleanValue() || MeteorClient.mc.field_1755 == null || (MeteorClient.mc.field_1755 instanceof WidgetScreen)) ? false : true;
    }

    @EventHandler
    private void onCustomFontChanged(CustomFontChangedEvent event) {
        if (this.customFont.get().booleanValue()) {
            for (HudElement element : this.elements) {
                element.onFontChanged();
            }
        }
    }

    public boolean hasCustomFont() {
        return this.customFont.get().booleanValue();
    }

    public double getTextScale() {
        return this.textScale.get().doubleValue();
    }

    @Override // java.lang.Iterable
    @NotNull
    public Iterator<HudElement> iterator() {
        return this.elements.iterator();
    }

    @Override // meteordevelopment.meteorclient.systems.System, meteordevelopment.meteorclient.utils.misc.ISerializable
    public class_2487 toTag() {
        class_2487 tag = new class_2487();
        tag.method_10569("__version__", 1);
        tag.method_10556("active", this.active);
        tag.method_10566("settings", this.settings.toTag());
        tag.method_10566("elements", NbtUtils.listToTag(this.elements));
        return tag;
    }

    @Override // meteordevelopment.meteorclient.systems.System, meteordevelopment.meteorclient.utils.misc.ISerializable
    /* JADX INFO: renamed from: fromTag */
    public Hud fromTag(class_2487 tag) {
        HudElementInfo<?> info;
        if (!tag.method_10545("__version__")) {
            resetToDefaultElements();
            return this;
        }
        this.active = ((Boolean) tag.method_10577("active").orElse(false)).booleanValue();
        this.settings.fromTag((class_2487) tag.method_10562("settings").orElse(new class_2487()));
        this.elements.clear();
        if (!tag.method_10545("elements")) {
            return this;
        }
        Optional<class_2499> optionalList = tag.method_10554("elements");
        if (!optionalList.isPresent()) {
            return this;
        }
        class_2499 elementsList = optionalList.get();
        for (int i = 0; i < elementsList.size(); i++) {
            if (i < elementsList.size() && (elementsList.get(i) instanceof class_2487)) {
                class_2487 c = (class_2487) elementsList.get(i);
                if (c.method_10545("name")) {
                    Optional<String> optionalName = c.method_10558("name");
                    if (optionalName.isPresent() && (info = this.infos.get(optionalName.get())) != null) {
                        HudElement element = info.create();
                        element.fromTag(c);
                        this.elements.add(element);
                    }
                }
            }
        }
        return this;
    }
}
