package meteordevelopment.meteorclient.systems.hud.elements;

import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.class_746;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/hud/elements/TargetHud.class */
public class TargetHud extends HudElement {
    public static final HudElementInfo<TargetHud> INFO = new HudElementInfo<>(Hud.GROUP, "target", "Displays information about your current target.", TargetHud::new);
    private final SettingGroup sgGeneral;
    private final Setting<Double> range;
    private final Setting<Boolean> showHealth;
    private final Setting<Boolean> showDistance;
    private final Setting<Boolean> showPing;
    private final Setting<Boolean> textShadow;
    private final Setting<SettingColor> titleColor;
    private final Setting<SettingColor> valueColor;

    public TargetHud() {
        super(INFO);
        this.sgGeneral = this.settings.getDefaultGroup();
        this.range = this.sgGeneral.add(new DoubleSetting.Builder().name("range").description("The range to target players.").defaultValue(100.0d).min(1.0d).sliderMax(200.0d).build());
        this.showHealth = this.sgGeneral.add(new BoolSetting.Builder().name("show-health").description("Shows the target's health.").defaultValue(true).build());
        this.showDistance = this.sgGeneral.add(new BoolSetting.Builder().name("show-distance").description("Shows the distance to the target.").defaultValue(true).build());
        this.showPing = this.sgGeneral.add(new BoolSetting.Builder().name("show-ping").description("Shows the target's ping.").defaultValue(true).build());
        this.textShadow = this.sgGeneral.add(new BoolSetting.Builder().name("text-shadow").description("Renders shadow behind text.").defaultValue(true).build());
        this.titleColor = this.sgGeneral.add(new ColorSetting.Builder().name("title-color").description("Title color.").defaultValue(new SettingColor()).build());
        this.valueColor = this.sgGeneral.add(new ColorSetting.Builder().name("value-color").description("Value color.").defaultValue(new SettingColor(Opcode.DRETURN, Opcode.DRETURN, Opcode.DRETURN)).build());
    }

    @Override // meteordevelopment.meteorclient.systems.hud.HudElement
    public void render(HudRenderer renderer) {
        class_746 playerTarget = TargetUtils.getPlayerTarget(this.range.get().doubleValue(), SortPriority.LowestDistance);
        if (playerTarget == null && !isInEditor()) {
            renderer.text("Target: None", this.x, this.y, this.titleColor.get(), this.textShadow.get().booleanValue());
            setSize(renderer.textWidth("Target: None"), renderer.textHeight());
            return;
        }
        if (isInEditor()) {
            playerTarget = MeteorClient.mc.field_1724;
        }
        double y = this.y;
        String nameText = "Target: " + (playerTarget != null ? playerTarget.method_5477().getString() : "None");
        renderer.text(nameText, this.x, y, this.titleColor.get(), this.textShadow.get().booleanValue());
        double width = Math.max(0.0d, renderer.textWidth(nameText));
        double y2 = y + renderer.textHeight() + 2.0d;
        if (playerTarget != null) {
            if (this.showHealth.get().booleanValue()) {
                double health = Math.round(((double) playerTarget.method_6032()) * 10.0d) / 10.0d;
                double dRound = Math.round(((double) playerTarget.method_6063()) * 10.0d) / 10.0d;
                double x1 = renderer.text("Health: ", this.x, y2, this.titleColor.get(), this.textShadow.get().booleanValue());
                String healthValue = health + "/" + health;
                renderer.text(healthValue, x1, y2, this.valueColor.get(), this.textShadow.get().booleanValue());
                width = Math.max(width, renderer.textWidth("Health: " + healthValue));
                y2 += renderer.textHeight() + 2.0d;
            }
            if (this.showDistance.get().booleanValue() && !isInEditor()) {
                double dist = Math.round(((double) MeteorClient.mc.field_1724.method_5739(playerTarget)) * 10.0d) / 10.0d;
                double x12 = renderer.text("Distance: ", this.x, y2, this.titleColor.get(), this.textShadow.get().booleanValue());
                String distValue = dist + "m";
                renderer.text(distValue, x12, y2, this.valueColor.get(), this.textShadow.get().booleanValue());
                width = Math.max(width, renderer.textWidth("Distance: " + distValue));
                y2 += renderer.textHeight() + 2.0d;
            }
            if (this.showPing.get().booleanValue()) {
                int ping = EntityUtils.getPing(playerTarget);
                double x13 = renderer.text("Ping: ", this.x, y2, this.titleColor.get(), this.textShadow.get().booleanValue());
                String pingValue = ping + "ms";
                renderer.text(pingValue, x13, y2, this.valueColor.get(), this.textShadow.get().booleanValue());
                width = Math.max(width, renderer.textWidth("Ping: " + pingValue));
                y2 += renderer.textHeight() + 2.0d;
            }
        }
        setSize(width, y2 - ((double) this.y));
    }
}
