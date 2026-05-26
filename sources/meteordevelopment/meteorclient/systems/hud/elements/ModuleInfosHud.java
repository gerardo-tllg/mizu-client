package meteordevelopment.meteorclient.systems.hud.elements;

import java.util.List;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.ModuleListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.Alignment;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.AnchorAura;
import meteordevelopment.meteorclient.systems.modules.combat.BedAura;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura;
import meteordevelopment.meteorclient.systems.modules.combat.Surround;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/hud/elements/ModuleInfosHud.class */
public class ModuleInfosHud extends HudElement {
    public static final HudElementInfo<ModuleInfosHud> INFO = new HudElementInfo<>(Hud.GROUP, "module-infos", "Displays if selected modules are enabled or disabled.", ModuleInfosHud::new);
    private final SettingGroup sgGeneral;
    private final Setting<List<Module>> modules;
    private final Setting<Boolean> additionalInfo;
    private final Setting<Boolean> textShadow;
    private final Setting<SettingColor> moduleColor;
    private final Setting<SettingColor> onColor;
    private final Setting<SettingColor> offColor;
    private final Setting<Alignment> alignment;

    public ModuleInfosHud() {
        super(INFO);
        this.sgGeneral = this.settings.getDefaultGroup();
        this.modules = this.sgGeneral.add(new ModuleListSetting.Builder().name("modules").description("Which modules to display").defaultValue(KillAura.class, AnchorAura.class, BedAura.class, Surround.class).build());
        this.additionalInfo = this.sgGeneral.add(new BoolSetting.Builder().name("additional-info").description("Shows additional info from the module next to the name in the module info list.").defaultValue(true).build());
        this.textShadow = this.sgGeneral.add(new BoolSetting.Builder().name("text-shadow").description("Renders shadow behind text.").defaultValue(true).build());
        this.moduleColor = this.sgGeneral.add(new ColorSetting.Builder().name("module-color").description("Module color.").defaultValue(new SettingColor()).build());
        this.onColor = this.sgGeneral.add(new ColorSetting.Builder().name("on-color").description("Color when module is on.").defaultValue(new SettingColor(25, 225, 25)).build());
        this.offColor = this.sgGeneral.add(new ColorSetting.Builder().name("off-color").description("Color when module is off.").defaultValue(new SettingColor(225, 25, 25)).build());
        this.alignment = this.sgGeneral.add(new EnumSetting.Builder().name("alignment").description("Horizontal alignment.").defaultValue(Alignment.Auto).build());
    }

    @Override // meteordevelopment.meteorclient.systems.hud.HudElement
    public void render(HudRenderer renderer) {
        String info;
        if (Modules.get() == null || this.modules.get().isEmpty()) {
            renderer.text("Module Info", this.x, this.y, this.moduleColor.get(), this.textShadow.get().booleanValue());
            setSize(renderer.textWidth("Module Info"), renderer.textHeight());
            return;
        }
        double y = this.y;
        double width = 0.0d;
        double height = 0.0d;
        int i = 0;
        for (Module module : this.modules.get()) {
            double moduleWidth = renderer.textWidth(module.title) + renderer.textWidth(" ");
            String text = null;
            if (module.isActive()) {
                if (this.additionalInfo.get().booleanValue() && (info = module.getInfoString()) != null) {
                    text = info;
                }
                if (text == null) {
                    text = "ON";
                }
            } else {
                text = "OFF";
            }
            double moduleWidth2 = moduleWidth + renderer.textWidth(text);
            double x = ((double) this.x) + alignX(moduleWidth2, this.alignment.get());
            renderer.text(text, renderer.text(module.title, x, y, this.moduleColor.get(), this.textShadow.get().booleanValue()) + renderer.textWidth(" "), y, module.isActive() ? this.onColor.get() : this.offColor.get(), this.textShadow.get().booleanValue());
            y += renderer.textHeight() + 2.0d;
            width = Math.max(width, moduleWidth2);
            height += renderer.textHeight();
            if (i > 0) {
                height += 2.0d;
            }
            i++;
        }
        setSize(width, height);
    }
}
