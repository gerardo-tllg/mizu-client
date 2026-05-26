package meteordevelopment.meteorclient.systems.modules.movement;

import java.util.function.Predicate;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.pathing.PathManagers;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1268;
import net.minecraft.class_1747;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_239;
import net.minecraft.class_2828;
import net.minecraft.class_3959;
import net.minecraft.class_3965;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/movement/NoFall.class */
public class NoFall extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Mode> mode;
    private final Setting<PlacedItem> placedItem;
    private final Setting<PlaceMode> airPlaceMode;
    private final Setting<Boolean> anchor;
    private final Setting<Boolean> antiBounce;
    private boolean placedWater;
    private class_2338 targetPos;
    private int timer;
    private boolean prePathManagerNoFall;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/movement/NoFall$Mode.class */
    public enum Mode {
        Packet,
        AirPlace,
        Place,
        Elytra
    }

    public NoFall() {
        super(Categories.Movement, "no-fall", "Attempts to prevent you from taking fall damage.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.mode = this.sgGeneral.add(new EnumSetting.Builder().name("mode").description("The way you are saved from fall damage.").defaultValue(Mode.Packet).build());
        this.placedItem = this.sgGeneral.add(new EnumSetting.Builder().name("placed-item").description("Which block to place.").defaultValue(PlacedItem.Bucket).visible(() -> {
            return this.mode.get() == Mode.Place;
        }).build());
        this.airPlaceMode = this.sgGeneral.add(new EnumSetting.Builder().name("air-place-mode").description("Whether place mode places before you die or before you take damage.").defaultValue(PlaceMode.BeforeDeath).visible(() -> {
            return this.mode.get() == Mode.AirPlace;
        }).build());
        this.anchor = this.sgGeneral.add(new BoolSetting.Builder().name("anchor").description("Centers the player and reduces movement when using bucket or air place mode.").defaultValue(true).visible(() -> {
            return this.mode.get() != Mode.Packet;
        }).build());
        this.antiBounce = this.sgGeneral.add(new BoolSetting.Builder().name("anti-bounce").description("Disables bouncing on slime-block and bed upon landing.").defaultValue(true).build());
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        this.prePathManagerNoFall = PathManagers.get().getSettings().getNoFall().get().booleanValue();
        if (this.mode.get() == Mode.Packet) {
            PathManagers.get().getSettings().getNoFall().set(true);
        }
        this.placedWater = false;
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onDeactivate() {
        PathManagers.get().getSettings().getNoFall().set(Boolean.valueOf(this.prePathManagerNoFall));
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (this.mc.field_1724.method_31549().field_7477 || !(event.packet instanceof class_2828) || this.mode.get() != Mode.Packet) {
            return;
        }
        if (!Modules.get().isActive(Flight.class)) {
            if (!this.mc.field_1724.method_6128() && this.mc.field_1724.method_18798().field_1351 <= -0.5d) {
                event.packet.setOnGround(true);
                return;
            }
            return;
        }
        event.packet.setOnGround(true);
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.timer > 20) {
            this.placedWater = false;
            this.timer = 0;
        }
        if (this.mc.field_1724.method_31549().field_7477) {
            return;
        }
        if (this.mode.get() == Mode.AirPlace) {
            if (this.airPlaceMode.get().test((float) this.mc.field_1724.field_6017)) {
                if (this.anchor.get().booleanValue()) {
                    PlayerUtils.centerPlayer();
                }
                Rotations.rotate(this.mc.field_1724.method_36454(), 90.0d, Integer.MAX_VALUE, () -> {
                    double preY = this.mc.field_1724.method_18798().field_1351;
                    this.mc.field_1724.method_18798().meteor$setY(0.0d);
                    BlockUtils.place(this.mc.field_1724.method_24515().method_10074(), InvUtils.findInHotbar((Predicate<class_1799>) itemStack -> {
                        return itemStack.method_7909() instanceof class_1747;
                    }), false, 0, true);
                    this.mc.field_1724.method_18798().meteor$setY(preY);
                });
                return;
            }
            return;
        }
        if (this.mode.get() == Mode.Place) {
            PlacedItem placedItem1 = (this.mc.field_1687.method_8597().comp_644() && this.placedItem.get() == PlacedItem.Bucket) ? PlacedItem.PowderSnow : this.placedItem.get();
            if (this.mc.field_1724.field_6017 > 3.0d && !EntityUtils.isAboveWater(this.mc.field_1724)) {
                class_1792 item = placedItem1.item;
                FindItemResult findItemResult = InvUtils.findInHotbar(item);
                if (!findItemResult.found()) {
                    return;
                }
                if (this.anchor.get().booleanValue()) {
                    PlayerUtils.centerPlayer();
                }
                class_3965 result = this.mc.field_1687.method_17742(new class_3959(this.mc.field_1724.method_19538(), this.mc.field_1724.method_19538().method_1023(0.0d, 5.0d, 0.0d), class_3959.class_3960.field_17559, class_3959.class_242.field_1348, this.mc.field_1724));
                if (result != null && result.method_17783() == class_239.class_240.field_1332) {
                    this.targetPos = result.method_17777().method_10084();
                    if (placedItem1 == PlacedItem.Bucket) {
                        useItem(findItemResult, true, this.targetPos, true);
                    } else {
                        useItem(findItemResult, placedItem1 == PlacedItem.PowderSnow, this.targetPos, false);
                    }
                }
            }
            if (this.placedWater) {
                this.timer++;
                if (this.mc.field_1724.method_55667().method_26204() == placedItem1.block) {
                    useItem(InvUtils.findInHotbar(class_1802.field_8550), false, this.targetPos, true);
                    return;
                } else {
                    if (this.mc.field_1687.method_8320(this.mc.field_1724.method_24515().method_10074()).method_26204() == class_2246.field_27879 && this.mc.field_1724.field_6017 == 0.0d && placedItem1.block == class_2246.field_27879) {
                        useItem(InvUtils.findInHotbar(class_1802.field_8550), false, this.targetPos.method_10074(), true);
                        return;
                    }
                    return;
                }
            }
            return;
        }
        if (this.mode.get() == Mode.Elytra) {
        }
    }

    public boolean cancelBounce() {
        return isActive() && this.antiBounce.get().booleanValue();
    }

    private void useItem(FindItemResult item, boolean placedWater, class_2338 blockPos, boolean interactItem) {
        if (item.found()) {
            if (interactItem) {
                Rotations.rotate(Rotations.getYaw(blockPos), Rotations.getPitch(blockPos), 10, true, () -> {
                    if (item.isOffhand()) {
                        this.mc.field_1761.method_2919(this.mc.field_1724, class_1268.field_5810);
                        return;
                    }
                    InvUtils.swap(item.slot(), true);
                    this.mc.field_1761.method_2919(this.mc.field_1724, class_1268.field_5808);
                    InvUtils.swapBack();
                });
            } else {
                BlockUtils.place(blockPos, item, true, 10, true);
            }
            this.placedWater = placedWater;
        }
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public String getInfoString() {
        return this.mode.get().toString();
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/movement/NoFall$PlacedItem.class */
    public enum PlacedItem {
        Bucket(class_1802.field_8705, class_2246.field_10382),
        PowderSnow(class_1802.field_27876, class_2246.field_27879),
        HayBale(class_1802.field_17528, class_2246.field_10359),
        Cobweb(class_1802.field_8786, class_2246.field_10343),
        SlimeBlock(class_1802.field_8828, class_2246.field_10030);

        private final class_1792 item;
        private final class_2248 block;

        PlacedItem(class_1792 item, class_2248 block) {
            this.item = item;
            this.block = block;
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/movement/NoFall$PlaceMode.class */
    public enum PlaceMode {
        BeforeDamage(height -> {
            return height.floatValue() > 2.0f;
        }),
        BeforeDeath(height2 -> {
            return height2.floatValue() > Math.max(PlayerUtils.getTotalHealth(), 2.0f);
        });

        private final Predicate<Float> fallHeight;

        PlaceMode(Predicate fallHeight) {
            this.fallHeight = fallHeight;
        }

        public boolean test(float fallheight) {
            return this.fallHeight.test(Float.valueOf(fallheight));
        }
    }
}
