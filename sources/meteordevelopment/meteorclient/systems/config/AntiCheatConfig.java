package meteordevelopment.meteorclient.systems.config;

import java.util.Objects;
import java.util.Optional;
import javassist.compiler.TokenId;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.systems.managers.SwapManager;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_2487;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/config/AntiCheatConfig.class */
public class AntiCheatConfig extends System<AntiCheatConfig> {
    public final Settings settings;
    private final SettingGroup sgRotations;
    private final SettingGroup sgBlockPlacement;
    private final SettingGroup sgSwap;
    private final SettingGroup sgNetwork;
    public final Setting<Boolean> tickSync;
    public final Setting<Boolean> grimSync;
    public final Setting<Boolean> grimRotation;
    public final Setting<Boolean> grimSnapRotation;
    public final Setting<Boolean> blockRotatePlace;
    public final Setting<Boolean> blockPlaceAirPlace;
    public final Setting<Boolean> mainHandAirPlace;
    public final Setting<Boolean> forceAirPlace;
    public final Setting<Double> blockPlaceRange;
    public final Setting<Double> blockPlacePerBlockCooldown;
    public final Setting<Double> blocksPerSecondCap;
    public final Setting<Boolean> blockPlaceTpsSync;
    public final Setting<Integer> blockPacketLimit;
    public final Setting<Boolean> swapAntiScreenClose;
    public final Setting<SwapManager.SwapMode> swapMode;
    private volatile int sentThisSecond;
    private volatile int recvThisSecond;
    private volatile int sentLastSecond;
    private volatile int recvLastSecond;
    private long windowStartMs;

    public AntiCheatConfig() {
        super("anti-cheat-config");
        this.settings = new Settings();
        this.sgRotations = this.settings.createGroup("Rotations");
        this.sgBlockPlacement = this.settings.createGroup("Block Placement");
        this.sgSwap = this.settings.createGroup("Swap");
        this.sgNetwork = this.settings.createGroup("Network");
        this.tickSync = this.sgRotations.add(new BoolSetting.Builder().name("tick-sync").description("Rotate every tick.").defaultValue(true).build());
        this.grimSync = this.sgRotations.add(new BoolSetting.Builder().name("grim-sync").description("Send full movement packet every tick.").defaultValue(false).visible(() -> {
            return this.tickSync.get().booleanValue();
        }).build());
        this.grimRotation = this.sgRotations.add(new BoolSetting.Builder().name("grim-rotation").description("Send full movement packet when look changes.").defaultValue(true).visible(() -> {
            return this.tickSync.get().booleanValue();
        }).build());
        this.grimSnapRotation = this.sgRotations.add(new BoolSetting.Builder().name("grim-snap-rotation").description("Send full movement packet when snapping rotation.").defaultValue(true).build());
        this.blockRotatePlace = this.sgBlockPlacement.add(new BoolSetting.Builder().name("block-rotate-place").description("Rotate to place blocks.").defaultValue(false).build());
        this.blockPlaceAirPlace = this.sgBlockPlacement.add(new BoolSetting.Builder().name("grim-air-place").description("Allow air placing blocks using offhand swap exploit (for Grim servers).").defaultValue(false).build());
        this.mainHandAirPlace = this.sgBlockPlacement.add(new BoolSetting.Builder().name("main-hand-air-place").description("Allow air placing blocks using vanilla interaction (works on most servers).").defaultValue(true).visible(() -> {
            return !this.blockPlaceAirPlace.get().booleanValue();
        }).build());
        this.forceAirPlace = this.sgBlockPlacement.add(new BoolSetting.Builder().name("force-air-place").description("Only air place blocks.").defaultValue(false).visible(() -> {
            return this.blockPlaceAirPlace.get().booleanValue() || this.mainHandAirPlace.get().booleanValue();
        }).build());
        this.blockPlaceRange = this.sgBlockPlacement.add(new DoubleSetting.Builder().name("place-range").description("Max range for block placement. 0 = use vanilla interaction range.").defaultValue(0.0d).min(0.0d).sliderMax(6.0d).build());
        this.blockPlacePerBlockCooldown = this.sgBlockPlacement.add(new DoubleSetting.Builder().name("block-place-cooldown").description("Cooldown before retrying same position.").defaultValue(0.05d).min(0.0d).sliderMax(0.3d).build());
        this.blocksPerSecondCap = this.sgBlockPlacement.add(new DoubleSetting.Builder().name("blocks-per-second").description("Max blocks per second.").defaultValue(30.0d).min(0.0d).sliderMax(30.0d).build());
        this.blockPlaceTpsSync = this.sgBlockPlacement.add(new BoolSetting.Builder().name("tps-sync").description("Syncs block placement speed with server TPS.").defaultValue(false).build());
        this.blockPacketLimit = this.sgBlockPlacement.add(new IntSetting.Builder().name("block-packet-limit").description("Milliseconds to wait after rate limit.").defaultValue(Integer.valueOf(TokenId.ABSTRACT)).min(TokenId.ABSTRACT).sliderMax(TokenId.NEQ).build());
        this.swapAntiScreenClose = this.sgSwap.add(new BoolSetting.Builder().name("anti-screen-close").description("Pause swapping when screens are open.").defaultValue(true).build());
        this.swapMode = this.sgSwap.add(new EnumSetting.Builder().name("item-swap-mode").description("How to swap items.").defaultValue(SwapManager.SwapMode.Auto).build());
        this.sentThisSecond = 0;
        this.recvThisSecond = 0;
        this.sentLastSecond = 0;
        this.recvLastSecond = 0;
        this.windowStartMs = 0L;
    }

    public static AntiCheatConfig get() {
        return (AntiCheatConfig) Systems.get(AntiCheatConfig.class);
    }

    @Override // meteordevelopment.meteorclient.systems.System, meteordevelopment.meteorclient.utils.misc.ISerializable
    public class_2487 toTag() {
        class_2487 tag = new class_2487();
        tag.method_10582("version", MeteorClient.VERSION.toString());
        tag.method_10566("settings", this.settings.toTag());
        return tag;
    }

    @Override // meteordevelopment.meteorclient.systems.System, meteordevelopment.meteorclient.utils.misc.ISerializable
    /* JADX INFO: renamed from: fromTag */
    public AntiCheatConfig fromTag2(class_2487 tag) {
        if (tag.method_10545("settings")) {
            Optional optionalMethod_10562 = tag.method_10562("settings");
            Settings settings = this.settings;
            Objects.requireNonNull(settings);
            optionalMethod_10562.ifPresent(settings::fromTag2);
        }
        return this;
    }

    @EventHandler
    private void onSend(PacketEvent.Send event) {
        this.sentThisSecond++;
    }

    @EventHandler
    private void onReceive(PacketEvent.Receive event) {
        this.recvThisSecond++;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        long now = System.currentTimeMillis();
        if (this.windowStartMs == 0) {
            this.windowStartMs = now;
        }
        if (now - this.windowStartMs >= 1000) {
            this.windowStartMs = now;
            this.sentLastSecond = this.sentThisSecond;
            this.recvLastSecond = this.recvThisSecond;
            this.sentThisSecond = 0;
            this.recvThisSecond = 0;
        }
    }

    public int getSentPps() {
        return this.sentLastSecond;
    }

    public int getRecvPps() {
        return this.recvLastSecond;
    }
}
