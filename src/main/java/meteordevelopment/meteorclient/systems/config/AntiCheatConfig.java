package meteordevelopment.meteorclient.systems.config;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.systems.managers.SwapManager;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.nbt.NbtCompound;

public class AntiCheatConfig extends System<AntiCheatConfig> {
    public final Settings settings = new Settings();

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
    public final Setting<Boolean> forceAirPlace;
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

        this.sgRotations = this.settings.createGroup("Rotations");
        this.sgBlockPlacement = this.settings.createGroup("Block Placement");
        this.sgSwap = this.settings.createGroup("Swap");
        this.sgNetwork = this.settings.createGroup("Network");

        this.tickSync = this.sgRotations.add(new BoolSetting.Builder()
            .name("tick-sync")
            .description("Rotate every tick.")
            .defaultValue(true)
            .build());

        this.grimSync = this.sgRotations.add(new BoolSetting.Builder()
            .name("grim-sync")
            .description("Send full movement packet every tick.")
            .defaultValue(false)
            .visible(() -> this.tickSync.get())
            .build());

        this.grimRotation = this.sgRotations.add(new BoolSetting.Builder()
            .name("grim-rotation")
            .description("Send full movement packet when look changes.")
            .defaultValue(true)
            .visible(() -> this.tickSync.get())
            .build());

        this.grimSnapRotation = this.sgRotations.add(new BoolSetting.Builder()
            .name("grim-snap-rotation")
            .description("Send full movement packet when snapping rotation.")
            .defaultValue(true)
            .build());

        this.blockRotatePlace = this.sgBlockPlacement.add(new BoolSetting.Builder()
            .name("block-rotate-place")
            .description("Rotate to place blocks.")
            .defaultValue(false)
            .build());

        this.blockPlaceAirPlace = this.sgBlockPlacement.add(new BoolSetting.Builder()
            .name("grim-air-place")
            .description("Allow air placing blocks.")
            .defaultValue(true)
            .build());

        this.forceAirPlace = this.sgBlockPlacement.add(new BoolSetting.Builder()
            .name("force-air-place")
            .description("Only air place blocks.")
            .defaultValue(true)
            .visible(() -> this.blockPlaceAirPlace.get())
            .build());

        this.blockPlacePerBlockCooldown = this.sgBlockPlacement.add(new DoubleSetting.Builder()
            .name("block-place-cooldown")
            .description("Cooldown before retrying same position.")
            .defaultValue(0.05)
            .min(0.0)
            .sliderMax(0.3)
            .build());

        this.blocksPerSecondCap = this.sgBlockPlacement.add(new DoubleSetting.Builder()
            .name("blocks-per-second")
            .description("Max blocks per second.")
            .defaultValue(30.0)
            .min(0.0)
            .sliderMax(30.0)
            .build());

        this.blockPlaceTpsSync = this.sgBlockPlacement.add(new BoolSetting.Builder()
            .name("tps-sync")
            .description("Syncs block placement speed with server TPS.")
            .defaultValue(false)
            .build());

        this.blockPacketLimit = this.sgBlockPlacement.add(new IntSetting.Builder()
            .name("block-packet-limit")
            .description("Milliseconds to wait after rate limit.")
            .defaultValue(300)
            .min(300)
            .sliderMax(350)
            .build());

        this.swapAntiScreenClose = this.sgSwap.add(new BoolSetting.Builder()
            .name("anti-screen-close")
            .description("Pause swapping when screens are open.")
            .defaultValue(true)
            .build());

        this.swapMode = this.sgSwap.add(new EnumSetting.Builder<SwapManager.SwapMode>()
            .name("item-swap-mode")
            .description("How to swap items.")
            .defaultValue(SwapManager.SwapMode.Auto)
            .build());

        this.sentThisSecond = 0;
        this.recvThisSecond = 0;
        this.sentLastSecond = 0;
        this.recvLastSecond = 0;
        this.windowStartMs = 0L;
    }

    public static AntiCheatConfig get() {
        return Systems.get(AntiCheatConfig.class);
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        tag.putString("version", MeteorClient.VERSION.toString());
        tag.put("settings", settings.toTag());
        return tag;
    }

    @Override
    public AntiCheatConfig fromTag(NbtCompound tag) {
        if (tag.contains("settings")) {
            tag.getCompound("settings").ifPresent(settings::fromTag);
        }
        return this;
    }

    @EventHandler
    private void onSend(PacketEvent.Send event) {
        ++this.sentThisSecond;
    }

    @EventHandler
    private void onReceive(PacketEvent.Receive event) {
        ++this.recvThisSecond;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        long now = java.lang.System.currentTimeMillis();
        if (this.windowStartMs == 0L) {
            this.windowStartMs = now;
        }

        if (now - this.windowStartMs >= 1000L) {
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
