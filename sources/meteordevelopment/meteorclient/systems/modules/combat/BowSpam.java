package meteordevelopment.meteorclient.systems.modules.combat;

import java.util.function.Predicate;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1744;
import net.minecraft.class_1799;
import net.minecraft.class_1802;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/BowSpam.class */
public class BowSpam extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Integer> charge;
    private final Setting<Boolean> onlyWhenHoldingRightClick;
    private boolean wasBow;
    private boolean wasHoldingRightClick;

    public BowSpam() {
        super(Categories.Combat, "bow-spam", "Spams arrows.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.charge = this.sgGeneral.add(new IntSetting.Builder().name("charge").description("How long to charge the bow before releasing in ticks.").defaultValue(5).range(5, 20).sliderRange(5, 20).build());
        this.onlyWhenHoldingRightClick = this.sgGeneral.add(new BoolSetting.Builder().name("when-holding-right-click").description("Works only when holding right click.").defaultValue(false).build());
        this.wasBow = false;
        this.wasHoldingRightClick = false;
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        this.wasBow = false;
        this.wasHoldingRightClick = false;
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onDeactivate() {
        setPressed(false);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!this.mc.field_1724.method_31549().field_7477 && !InvUtils.find((Predicate<class_1799>) itemStack -> {
            return itemStack.method_7909() instanceof class_1744;
        }).found()) {
            return;
        }
        if (!this.onlyWhenHoldingRightClick.get().booleanValue() || this.mc.field_1690.field_1904.method_1434()) {
            boolean isBow = this.mc.field_1724.method_6047().method_7909() == class_1802.field_8102;
            if (!isBow && this.wasBow) {
                setPressed(false);
            }
            this.wasBow = isBow;
            if (isBow) {
                if (this.mc.field_1724.method_6048() >= this.charge.get().intValue()) {
                    this.mc.field_1761.method_2897(this.mc.field_1724);
                } else {
                    setPressed(true);
                }
                this.wasHoldingRightClick = this.mc.field_1690.field_1904.method_1434();
                return;
            }
            return;
        }
        if (this.wasHoldingRightClick) {
            setPressed(false);
            this.wasHoldingRightClick = false;
        }
    }

    private void setPressed(boolean pressed) {
        this.mc.field_1690.field_1904.method_23481(pressed);
    }
}
