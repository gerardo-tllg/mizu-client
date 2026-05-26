package meteordevelopment.meteorclient.systems.hud.elements;

import java.util.ArrayList;
import java.util.List;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/hud/elements/BindsHud.class */
public class BindsHud extends HudElement {
    public static final HudElementInfo<BindsHud> INFO = new HudElementInfo<>(Hud.GROUP, "binds", "Displays modules with keybinds.", BindsHud::new);
    private final SettingGroup sgGeneral;
    private final Setting<Integer> limit;
    private final Setting<Boolean> activeOnly;
    private final Setting<Boolean> textShadow;
    private final Setting<SettingColor> titleColor;
    private final Setting<SettingColor> bindColor;

    public BindsHud() {
        super(INFO);
        this.sgGeneral = this.settings.getDefaultGroup();
        this.limit = this.sgGeneral.add(new IntSetting.Builder().name("limit").description("Maximum number of binds to display.").defaultValue(10).min(1).sliderRange(1, 20).build());
        this.activeOnly = this.sgGeneral.add(new BoolSetting.Builder().name("active-only").description("Only shows binds for active modules.").defaultValue(false).build());
        this.textShadow = this.sgGeneral.add(new BoolSetting.Builder().name("text-shadow").description("Renders shadow behind text.").defaultValue(true).build());
        this.titleColor = this.sgGeneral.add(new ColorSetting.Builder().name("title-color").description("Title color.").defaultValue(new SettingColor()).build());
        this.bindColor = this.sgGeneral.add(new ColorSetting.Builder().name("bind-color").description("Bind color.").defaultValue(new SettingColor(Opcode.DRETURN, Opcode.DRETURN, Opcode.DRETURN)).build());
    }

    @Override // meteordevelopment.meteorclient.systems.hud.HudElement
    public void render(HudRenderer renderer) {
        List<Module> modules = new ArrayList<>();
        for (Module module : Modules.get().getAll()) {
            if (module.keybind.isSet() && (!this.activeOnly.get().booleanValue() || module.isActive())) {
                modules.add(module);
            }
        }
        if (modules.isEmpty()) {
            renderer.text("No binds", this.x, this.y, this.titleColor.get(), this.textShadow.get().booleanValue());
            setSize(renderer.textWidth("No binds"), renderer.textHeight());
            return;
        }
        double y = this.y;
        double width = 0.0d;
        int count = 0;
        for (Module module2 : modules) {
            if (count >= this.limit.get().intValue()) {
                break;
            }
            String titleText = module2.title + ": ";
            String bindText = module2.keybind.toString();
            double lineWidth = renderer.textWidth(titleText + bindText);
            double x1 = renderer.text(titleText, this.x, y, this.titleColor.get(), this.textShadow.get().booleanValue());
            renderer.text(bindText, x1, y, this.bindColor.get(), this.textShadow.get().booleanValue());
            width = Math.max(width, lineWidth);
            y += renderer.textHeight() + 2.0d;
            count++;
        }
        setSize(width, y - ((double) this.y));
    }
}
