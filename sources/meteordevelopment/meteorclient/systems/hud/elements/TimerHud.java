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
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/hud/elements/TimerHud.class */
public class TimerHud extends HudElement {
    public static final HudElementInfo<TimerHud> INFO = new HudElementInfo<>(Hud.GROUP, "timer", "Displays the timer multiplier.", TimerHud::new);
    private final SettingGroup sgGeneral;
    private final Setting<Boolean> textShadow;
    private final Setting<SettingColor> titleColor;
    private final Setting<SettingColor> valueColor;

    public TimerHud() {
        super(INFO);
        this.sgGeneral = this.settings.getDefaultGroup();
        this.textShadow = this.sgGeneral.add(new BoolSetting.Builder().name("text-shadow").description("Renders shadow behind text.").defaultValue(true).build());
        this.titleColor = this.sgGeneral.add(new ColorSetting.Builder().name("title-color").description("Title color.").defaultValue(new SettingColor()).build());
        this.valueColor = this.sgGeneral.add(new ColorSetting.Builder().name("value-color").description("Value color.").defaultValue(new SettingColor(Opcode.DRETURN, Opcode.DRETURN, Opcode.DRETURN)).build());
    }

    @Override // meteordevelopment.meteorclient.systems.hud.HudElement
    public void render(HudRenderer renderer) {
        Timer timer = (Timer) Modules.get().get(Timer.class);
        if (timer == null || !timer.isActive()) {
            renderer.text("Timer: OFF", this.x, this.y, this.titleColor.get(), this.textShadow.get().booleanValue());
            setSize(renderer.textWidth("Timer: OFF"), renderer.textHeight());
            return;
        }
        double multiplier = timer.getMultiplier();
        String valueText = String.format("%.2fx", Double.valueOf(multiplier));
        double width = renderer.textWidth("Timer: " + valueText);
        double x1 = renderer.text("Timer: ", this.x, this.y, this.titleColor.get(), this.textShadow.get().booleanValue());
        renderer.text(valueText, x1, this.y, this.valueColor.get(), this.textShadow.get().booleanValue());
        setSize(width, renderer.textHeight());
    }
}
