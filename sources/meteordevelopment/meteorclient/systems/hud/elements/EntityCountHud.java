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
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.class_1296;
import net.minecraft.class_1588;
import net.minecraft.class_1657;
import net.minecraft.class_746;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/hud/elements/EntityCountHud.class */
public class EntityCountHud extends HudElement {
    public static final HudElementInfo<EntityCountHud> INFO = new HudElementInfo<>(Hud.GROUP, "entity-count", "Displays entity counts around you.", EntityCountHud::new);
    private final SettingGroup sgGeneral;
    private final Setting<Boolean> showPlayers;
    private final Setting<Boolean> showHostile;
    private final Setting<Boolean> showPassive;
    private final Setting<Boolean> showTotal;
    private final Setting<Boolean> textShadow;
    private final Setting<SettingColor> titleColor;
    private final Setting<SettingColor> valueColor;

    public EntityCountHud() {
        super(INFO);
        this.sgGeneral = this.settings.getDefaultGroup();
        this.showPlayers = this.sgGeneral.add(new BoolSetting.Builder().name("show-players").description("Shows player count.").defaultValue(true).build());
        this.showHostile = this.sgGeneral.add(new BoolSetting.Builder().name("show-hostile").description("Shows hostile mob count.").defaultValue(true).build());
        this.showPassive = this.sgGeneral.add(new BoolSetting.Builder().name("show-passive").description("Shows passive mob count.").defaultValue(true).build());
        this.showTotal = this.sgGeneral.add(new BoolSetting.Builder().name("show-total").description("Shows total entity count.").defaultValue(true).build());
        this.textShadow = this.sgGeneral.add(new BoolSetting.Builder().name("text-shadow").description("Renders shadow behind text.").defaultValue(true).build());
        this.titleColor = this.sgGeneral.add(new ColorSetting.Builder().name("title-color").description("Title color.").defaultValue(new SettingColor()).build());
        this.valueColor = this.sgGeneral.add(new ColorSetting.Builder().name("value-color").description("Value color.").defaultValue(new SettingColor(Opcode.DRETURN, Opcode.DRETURN, Opcode.DRETURN)).build());
    }

    @Override // meteordevelopment.meteorclient.systems.hud.HudElement
    public void render(HudRenderer renderer) {
        if (MeteorClient.mc.field_1687 == null) {
            renderer.text("Entities: 0", this.x, this.y, this.titleColor.get(), this.textShadow.get().booleanValue());
            setSize(renderer.textWidth("Entities: 0"), renderer.textHeight());
            return;
        }
        int players = 0;
        int hostile = 0;
        int passive = 0;
        int total = 0;
        for (class_746 class_746Var : MeteorClient.mc.field_1687.method_18112()) {
            if (class_746Var != MeteorClient.mc.field_1724) {
                if (class_746Var instanceof class_1657) {
                    players++;
                } else if (class_746Var instanceof class_1588) {
                    hostile++;
                } else if (class_746Var instanceof class_1296) {
                    passive++;
                }
                total++;
            }
        }
        double y = this.y;
        double width = 0.0d;
        if (this.showPlayers.get().booleanValue()) {
            double x1 = renderer.text("Players: ", this.x, y, this.titleColor.get(), this.textShadow.get().booleanValue());
            String valueText = String.valueOf(players);
            renderer.text(valueText, x1, y, this.valueColor.get(), this.textShadow.get().booleanValue());
            width = Math.max(0.0d, renderer.textWidth("Players: " + valueText));
            y += renderer.textHeight() + 2.0d;
        }
        if (this.showHostile.get().booleanValue()) {
            double x12 = renderer.text("Hostile: ", this.x, y, this.titleColor.get(), this.textShadow.get().booleanValue());
            String valueText2 = String.valueOf(hostile);
            renderer.text(valueText2, x12, y, this.valueColor.get(), this.textShadow.get().booleanValue());
            width = Math.max(width, renderer.textWidth("Hostile: " + valueText2));
            y += renderer.textHeight() + 2.0d;
        }
        if (this.showPassive.get().booleanValue()) {
            double x13 = renderer.text("Passive: ", this.x, y, this.titleColor.get(), this.textShadow.get().booleanValue());
            String valueText3 = String.valueOf(passive);
            renderer.text(valueText3, x13, y, this.valueColor.get(), this.textShadow.get().booleanValue());
            width = Math.max(width, renderer.textWidth("Passive: " + valueText3));
            y += renderer.textHeight() + 2.0d;
        }
        if (this.showTotal.get().booleanValue()) {
            double x14 = renderer.text("Total: ", this.x, y, this.titleColor.get(), this.textShadow.get().booleanValue());
            String valueText4 = String.valueOf(total);
            renderer.text(valueText4, x14, y, this.valueColor.get(), this.textShadow.get().booleanValue());
            width = Math.max(width, renderer.textWidth("Total: " + valueText4));
            y += renderer.textHeight() + 2.0d;
        }
        setSize(width, y - ((double) this.y));
    }
}
