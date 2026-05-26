package meteordevelopment.meteorclient.systems.hud.elements;

import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.autocrystal.AutoCrystal;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/hud/elements/AutoCrystalHud.class */
public class AutoCrystalHud extends HudElement {
    public static final HudElementInfo<AutoCrystalHud> INFO = new HudElementInfo<>(Hud.GROUP, "auto-crystal", "Displays AutoCrystal module information.", AutoCrystalHud::new);
    private final SettingGroup sgGeneral;
    private final Setting<Boolean> textShadow;
    private final Setting<SettingColor> titleColor;
    private final Setting<SettingColor> valueColor;

    public AutoCrystalHud() {
        super(INFO);
        this.sgGeneral = this.settings.getDefaultGroup();
        this.textShadow = this.sgGeneral.add(new BoolSetting.Builder().name("text-shadow").description("Renders shadow behind text.").defaultValue(true).build());
        this.titleColor = this.sgGeneral.add(new ColorSetting.Builder().name("title-color").description("Title color.").defaultValue(new SettingColor()).build());
        this.valueColor = this.sgGeneral.add(new ColorSetting.Builder().name("value-color").description("Value color.").defaultValue(new SettingColor(Opcode.DRETURN, Opcode.DRETURN, Opcode.DRETURN)).build());
    }

    @Override // meteordevelopment.meteorclient.systems.hud.HudElement
    public void render(HudRenderer renderer) {
        AutoCrystal autoCrystal = (AutoCrystal) Modules.get().get(AutoCrystal.class);
        if (autoCrystal == null || !autoCrystal.isActive()) {
            renderer.text("AutoCrystal: OFF", this.x, this.y, this.titleColor.get(), this.textShadow.get().booleanValue());
            setSize(renderer.textWidth("AutoCrystal: OFF"), renderer.textHeight());
            return;
        }
        double y = this.y;
        double titleWidth = renderer.textWidth("AutoCrystal");
        renderer.text("AutoCrystal", this.x, y, this.titleColor.get(), this.textShadow.get().booleanValue());
        double y2 = y + renderer.textHeight() + 2.0d;
        double width = Math.max(0.0d, titleWidth);
        String info = autoCrystal.getInfoString();
        if (info != null && !info.isEmpty()) {
            double infoWidth = renderer.textWidth("Info: ") + renderer.textWidth(info);
            double x1 = renderer.text("Info: ", this.x, y2, this.titleColor.get(), this.textShadow.get().booleanValue());
            renderer.text(info, x1, y2, this.valueColor.get(), this.textShadow.get().booleanValue());
            y2 += renderer.textHeight() + 2.0d;
            width = Math.max(width, infoWidth);
        }
        setSize(width, y2 - ((double) this.y));
    }
}
