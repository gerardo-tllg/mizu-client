package meteordevelopment.meteorclient.systems.hud.elements;

import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.player.SpeedMine;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/hud/elements/SpeedMineHud.class */
public class SpeedMineHud extends HudElement {
    public static final HudElementInfo<SpeedMineHud> INFO = new HudElementInfo<>(Hud.GROUP, "speed-mine", "Displays SpeedMine module information.", SpeedMineHud::new);
    private final SettingGroup sgGeneral;
    private final Setting<Boolean> showMode;
    private final Setting<Boolean> showProgress;
    private final Setting<Boolean> textShadow;
    private final Setting<SettingColor> titleColor;
    private final Setting<SettingColor> valueColor;

    public SpeedMineHud() {
        super(INFO);
        this.sgGeneral = this.settings.getDefaultGroup();
        this.showMode = this.sgGeneral.add(new BoolSetting.Builder().name("show-mode").description("Shows the current mode.").defaultValue(true).build());
        this.showProgress = this.sgGeneral.add(new BoolSetting.Builder().name("show-progress").description("Shows mining progress.").defaultValue(true).build());
        this.textShadow = this.sgGeneral.add(new BoolSetting.Builder().name("text-shadow").description("Renders shadow behind text.").defaultValue(true).build());
        this.titleColor = this.sgGeneral.add(new ColorSetting.Builder().name("title-color").description("Title color.").defaultValue(new SettingColor()).build());
        this.valueColor = this.sgGeneral.add(new ColorSetting.Builder().name("value-color").description("Value color.").defaultValue(new SettingColor(Opcode.DRETURN, Opcode.DRETURN, Opcode.DRETURN)).build());
    }

    @Override // meteordevelopment.meteorclient.systems.hud.HudElement
    public void render(HudRenderer renderer) {
        SpeedMine speedMine = (SpeedMine) Modules.get().get(SpeedMine.class);
        if (speedMine == null || !speedMine.isActive()) {
            renderer.text("SpeedMine: OFF", this.x, this.y, this.titleColor.get(), this.textShadow.get().booleanValue());
            setSize(renderer.textWidth("SpeedMine: OFF"), renderer.textHeight());
            return;
        }
        double y = this.y;
        renderer.text("SpeedMine", this.x, y, this.titleColor.get(), this.textShadow.get().booleanValue());
        double width = Math.max(0.0d, renderer.textWidth("SpeedMine"));
        double y2 = y + renderer.textHeight() + 2.0d;
        if (this.showMode.get().booleanValue()) {
            double x1 = renderer.text("Mode: ", this.x, y2, this.titleColor.get(), this.textShadow.get().booleanValue());
            String modeValue = speedMine.mode.get().toString();
            renderer.text(modeValue, x1, y2, this.valueColor.get(), this.textShadow.get().booleanValue());
            width = Math.max(width, renderer.textWidth("Mode: " + modeValue));
            y2 += renderer.textHeight() + 2.0d;
        }
        if (this.showProgress.get().booleanValue() && MeteorClient.mc.field_1761 != null) {
            float progress = MeteorClient.mc.field_1761.getBreakingProgress();
            if (progress > 0.0f) {
                double x12 = renderer.text("Progress: ", this.x, y2, this.titleColor.get(), this.textShadow.get().booleanValue());
                String progressValue = String.format("%.1f%%", Float.valueOf(progress * 100.0f));
                renderer.text(progressValue, x12, y2, this.valueColor.get(), this.textShadow.get().booleanValue());
                width = Math.max(width, renderer.textWidth("Progress: " + progressValue));
                y2 += renderer.textHeight() + 2.0d;
            }
        }
        setSize(width, y2 - ((double) this.y));
    }
}
