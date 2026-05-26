package meteordevelopment.meteorclient.systems.modules.player;

import java.util.List;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.ClientPlayerInteractionManagerAccessor;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2846;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/player/SpeedMine.class */
public class SpeedMine extends Module {
    private final SettingGroup sgGeneral;
    public final Setting<Mode> mode;
    private final Setting<List<class_2248>> blocks;
    private final Setting<ListMode> blocksFilter;
    public final Setting<Double> modifier;
    private final Setting<Integer> hasteAmplifier;
    private final Setting<Boolean> instamine;
    private final Setting<Boolean> grimBypass;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/player/SpeedMine$ListMode.class */
    public enum ListMode {
        Whitelist,
        Blacklist
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/player/SpeedMine$Mode.class */
    public enum Mode {
        Normal,
        Haste,
        Damage
    }

    public SpeedMine() {
        super(Categories.Player, "speed-mine", "Allows you to quickly mine blocks.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.mode = this.sgGeneral.add(new EnumSetting.Builder().name("mode").defaultValue(Mode.Damage).onChanged(mode -> {
            removeHaste();
        }).build());
        this.blocks = this.sgGeneral.add(new BlockListSetting.Builder().name("blocks").description("Selected blocks.").filter(block -> {
            return block.method_36555() > 0.0f;
        }).visible(() -> {
            return this.mode.get() != Mode.Haste;
        }).build());
        this.blocksFilter = this.sgGeneral.add(new EnumSetting.Builder().name("blocks-filter").description("How to use the blocks setting.").defaultValue(ListMode.Blacklist).visible(() -> {
            return this.mode.get() != Mode.Haste;
        }).build());
        this.modifier = this.sgGeneral.add(new DoubleSetting.Builder().name("modifier").description("Mining speed modifier. An additional value of 0.2 is equivalent to one haste level (1.2 = haste 1).").defaultValue(1.4d).visible(() -> {
            return this.mode.get() == Mode.Normal;
        }).min(0.0d).build());
        this.hasteAmplifier = this.sgGeneral.add(new IntSetting.Builder().name("haste-amplifier").description("What value of haste to give you. Above 2 not recommended.").defaultValue(2).min(1).visible(() -> {
            return this.mode.get() == Mode.Haste;
        }).onChanged(i -> {
            removeHaste();
        }).build());
        this.instamine = this.sgGeneral.add(new BoolSetting.Builder().name("instamine").description("Whether or not to instantly mine blocks under certain conditions.").defaultValue(true).visible(() -> {
            return this.mode.get() == Mode.Damage;
        }).build());
        this.grimBypass = this.sgGeneral.add(new BoolSetting.Builder().name("grim-bypass").description("Bypasses Grim's fastbreak check, working as of 2.3.58").defaultValue(false).visible(() -> {
            return this.mode.get() == Mode.Damage;
        }).build());
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onDeactivate() {
        removeHaste();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (Utils.canUpdate()) {
            if (this.mode.get() == Mode.Haste) {
                class_1293 haste = this.mc.field_1724.method_6112(class_1294.field_5917);
                if (haste == null || haste.method_5578() <= this.hasteAmplifier.get().intValue() - 1) {
                    this.mc.field_1724.method_26082(new class_1293(class_1294.field_5917, -1, this.hasteAmplifier.get().intValue() - 1, false, false, false), (class_1297) null);
                    return;
                }
                return;
            }
            if (this.mode.get() == Mode.Damage) {
                ClientPlayerInteractionManagerAccessor im = this.mc.field_1761;
                float progress = im.getBreakingProgress();
                class_2338 pos = im.getCurrentBreakingBlockPos();
                if (pos != null && progress > 0.0f && progress + this.mc.field_1687.method_8320(pos).method_26165(this.mc.field_1724, this.mc.field_1687, pos) >= 0.7f) {
                    im.setCurrentBreakingProgress(1.0f);
                }
            }
        }
    }

    @EventHandler
    private void onPacket(PacketEvent.Send event) {
        if (this.mode.get() == Mode.Damage && this.grimBypass.get().booleanValue()) {
            class_2846 class_2846Var = event.packet;
            if (class_2846Var instanceof class_2846) {
                class_2846 packet = class_2846Var;
                if (packet.method_12363() == class_2846.class_2847.field_12973) {
                    this.mc.method_1562().method_52787(new class_2846(class_2846.class_2847.field_12971, packet.method_12362().method_10084(), packet.method_12360()));
                }
            }
        }
    }

    private void removeHaste() {
        class_1293 haste;
        if (Utils.canUpdate() && (haste = this.mc.field_1724.method_6112(class_1294.field_5917)) != null && !haste.method_5592()) {
            this.mc.field_1724.method_6016(class_1294.field_5917);
        }
    }

    public boolean filter(class_2248 block) {
        if (this.blocksFilter.get() != ListMode.Blacklist || this.blocks.get().contains(block)) {
            return this.blocksFilter.get() == ListMode.Whitelist && this.blocks.get().contains(block);
        }
        return true;
    }

    public boolean instamine() {
        return isActive() && this.mode.get() == Mode.Damage && this.instamine.get().booleanValue();
    }
}
