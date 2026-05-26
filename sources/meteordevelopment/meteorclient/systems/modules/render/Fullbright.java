package meteordevelopment.meteorclient.systems.modules.render;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.StatusEffectInstanceAccessor;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1944;
import net.minecraft.class_7923;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/Fullbright.class */
public class Fullbright extends Module {
    private final SettingGroup sgGeneral;
    public final Setting<Mode> mode;
    public final Setting<class_1944> lightType;
    private final Setting<Integer> minimumLightLevel;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/Fullbright$Mode.class */
    public enum Mode {
        Gamma,
        Potion,
        Luminance
    }

    public Fullbright() {
        super(Categories.Render, "fullbright", "Lights up your world!");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.mode = this.sgGeneral.add(new EnumSetting.Builder().name("mode").description("The mode to use for Fullbright.").defaultValue(Mode.Gamma).onChanged(mode -> {
            if (isActive()) {
                if (mode != Mode.Potion) {
                    disableNightVision();
                }
                if (this.mc.field_1769 != null) {
                    this.mc.field_1769.method_3279();
                }
            }
        }).build());
        this.lightType = this.sgGeneral.add(new EnumSetting.Builder().name("light-type").description("Which type of light to use for Luminance mode.").defaultValue(class_1944.field_9282).visible(() -> {
            return this.mode.get() == Mode.Luminance;
        }).onChanged(integer -> {
            if (this.mc.field_1769 == null || !isActive()) {
                return;
            }
            this.mc.field_1769.method_3279();
        }).build());
        this.minimumLightLevel = this.sgGeneral.add(new IntSetting.Builder().name("minimum-light-level").description("Minimum light level when using Luminance mode.").visible(() -> {
            return this.mode.get() == Mode.Luminance;
        }).defaultValue(8).range(0, 15).sliderMax(15).onChanged(integer2 -> {
            if (this.mc.field_1769 == null || !isActive()) {
                return;
            }
            this.mc.field_1769.method_3279();
        }).build());
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        if (this.mode.get() == Mode.Luminance) {
            this.mc.field_1769.method_3279();
        }
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onDeactivate() {
        if (this.mode.get() != Mode.Luminance) {
            if (this.mode.get() == Mode.Potion) {
                disableNightVision();
                return;
            }
            return;
        }
        this.mc.field_1769.method_3279();
    }

    public int getLuminance(class_1944 type) {
        if (isActive() && this.mode.get() == Mode.Luminance && type == this.lightType.get()) {
            return this.minimumLightLevel.get().intValue();
        }
        return 0;
    }

    public boolean getGamma() {
        return isActive() && this.mode.get() == Mode.Gamma;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (this.mc.field_1724 == null || !this.mode.get().equals(Mode.Potion)) {
            return;
        }
        if (this.mc.field_1724.method_6059(class_7923.field_41174.method_47983((class_1291) class_1294.field_5925.comp_349()))) {
            StatusEffectInstanceAccessor statusEffectInstanceAccessorMethod_6112 = this.mc.field_1724.method_6112(class_7923.field_41174.method_47983((class_1291) class_1294.field_5925.comp_349()));
            if (statusEffectInstanceAccessorMethod_6112 == null || statusEffectInstanceAccessorMethod_6112.method_5584() >= 420) {
                return;
            }
            statusEffectInstanceAccessorMethod_6112.setDuration(420);
            return;
        }
        this.mc.field_1724.method_6092(new class_1293(class_7923.field_41174.method_47983((class_1291) class_1294.field_5925.comp_349()), 420, 0));
    }

    private void disableNightVision() {
        if (this.mc.field_1724 != null && this.mc.field_1724.method_6059(class_7923.field_41174.method_47983((class_1291) class_1294.field_5925.comp_349()))) {
            this.mc.field_1724.method_6016(class_7923.field_41174.method_47983((class_1291) class_1294.field_5925.comp_349()));
        }
    }
}
