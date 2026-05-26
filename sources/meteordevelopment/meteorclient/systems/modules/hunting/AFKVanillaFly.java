package meteordevelopment.meteorclient.systems.modules.hunting;

import java.util.Objects;
import javassist.compiler.TokenId;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1802;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/hunting/AFKVanillaFly.class */
public class AFKVanillaFly extends Module {
    private long lastRocketUse;
    private boolean launched;
    private double yTarget;
    private float targetPitch;
    private final SettingGroup sgGeneral;
    private final Setting<Integer> fireworkDelay;
    private final Setting<Boolean> useManualY;
    private final Setting<Integer> manualYLevel;

    public AFKVanillaFly() {
        super(Categories.Hunting, "afk-vanilla-fly", "Maintains a level Y-flight with fireworks and smooth pitch control.");
        this.lastRocketUse = 0L;
        this.launched = false;
        this.yTarget = -1.0d;
        this.targetPitch = 0.0f;
        this.sgGeneral = this.settings.getDefaultGroup();
        this.fireworkDelay = this.sgGeneral.add(new IntSetting.Builder().name("timed-delay").description("The delay between firework usages in milliseconds.").defaultValue(4000).sliderRange(0, 10000).build());
        this.useManualY = this.sgGeneral.add(new BoolSetting.Builder().name("use-manual-y-level").description("Use a manually set Y level instead of the Y level when activated.").defaultValue(false).build());
        SettingGroup settingGroup = this.sgGeneral;
        IntSetting.Builder builderSliderRange = new IntSetting.Builder().name("manual-y-level").description("The Y level to maintain when using manual Y level.").defaultValue(256).sliderRange(-64, TokenId.IF);
        Setting<Boolean> setting = this.useManualY;
        Objects.requireNonNull(setting);
        this.manualYLevel = settingGroup.add(builderSliderRange.visible(setting::get).onChanged(val -> {
            this.yTarget = val.intValue();
        }).build());
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        this.launched = false;
        this.yTarget = -1.0d;
        if (this.mc.field_1724 == null || !this.mc.field_1724.method_6128()) {
            info("You must be flying before enabling AFKVanillaFly.", new Object[0]);
        }
    }

    public void tickFlyLogic() {
        if (this.mc.field_1724 == null) {
            return;
        }
        double currentY = this.mc.field_1724.method_23318();
        if (this.mc.field_1724.method_6128()) {
            if (this.yTarget == -1.0d || !this.launched) {
                if (this.useManualY.get().booleanValue()) {
                    this.yTarget = this.manualYLevel.get().intValue();
                } else {
                    this.yTarget = currentY;
                }
                this.launched = true;
            }
            if (!this.useManualY.get().booleanValue()) {
                double yDiffFromLock = currentY - this.yTarget;
                if (Math.abs(yDiffFromLock) > 10.0d) {
                    this.yTarget = currentY;
                    info("Y-lock reset due to altitude deviation.", new Object[0]);
                }
            }
            double yDiff = currentY - this.yTarget;
            if (Math.abs(yDiff) > 10.0d) {
                this.targetPitch = (float) (Math.atan2(yDiff, 100.0d) * 57.29577951308232d);
            } else if (yDiff > 2.0d) {
                this.targetPitch = 10.0f;
            } else if (yDiff < -2.0d) {
                this.targetPitch = -10.0f;
            } else {
                this.targetPitch = 0.0f;
            }
            float currentPitch = this.mc.field_1724.method_36455();
            float pitchDiff = this.targetPitch - currentPitch;
            this.mc.field_1724.method_36457(currentPitch + (pitchDiff * 0.1f));
            if (System.currentTimeMillis() - this.lastRocketUse > this.fireworkDelay.get().intValue()) {
                tryUseFirework();
                return;
            }
            return;
        }
        if (!this.launched) {
            this.mc.field_1724.method_6043();
            this.launched = true;
        } else if (System.currentTimeMillis() - this.lastRocketUse > 1000) {
            tryUseFirework();
        }
        this.yTarget = -1.0d;
    }

    public void resetYLock() {
        this.yTarget = -1.0d;
        this.launched = false;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        tickFlyLogic();
    }

    private void tryUseFirework() {
        FindItemResult hotbar = InvUtils.findInHotbar(class_1802.field_8639);
        if (!hotbar.found()) {
            FindItemResult inv = InvUtils.find(class_1802.field_8639);
            if (inv.found()) {
                int hotbarSlot = findEmptyHotbarSlot();
                if (hotbarSlot != -1) {
                    InvUtils.move().from(inv.slot()).to(hotbarSlot);
                } else {
                    info("No empty hotbar slot available to move fireworks.", new Object[0]);
                    return;
                }
            } else {
                info("No fireworks found in hotbar or inventory.", new Object[0]);
                return;
            }
        }
        HuntingUtils.firework(this.mc, false);
        this.lastRocketUse = System.currentTimeMillis();
    }

    private int findEmptyHotbarSlot() {
        if (this.mc.field_1724 == null) {
            return -1;
        }
        for (int i = 0; i < 9; i++) {
            if (this.mc.field_1724.method_31548().method_5438(i).method_7960()) {
                return i;
            }
        }
        return -1;
    }
}
