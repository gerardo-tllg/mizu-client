/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.hunting;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.*;

/**
 * NetherMobDetector — flags overworld mobs present in the Nether.
 *
 * Overworld mobs do not naturally spawn in the Nether. Any such mob is
 * evidence of a player who transported it or a nearby base with a portal.
 * Endermen are excluded (they spawn naturally in the Nether).
 * Chickens are excluded (they can appear as zombie-jockey mounts in Nether).
 * All nether-native mobs (Blaze, Ghast, Piglin, Hoglin, Zoglin, Strider,
 * Wither Skeleton, Magma Cube) are not included in the default list.
 */
public class NetherMobDetector extends Module {

    // ── Alert sound enum ─────────────────────────────────────────────────────
    public enum AlertSound { NOTE_BLOCK, PLING, BELL }

    // ── Setting groups ───────────────────────────────────────────────────────
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender  = settings.createGroup("Render");
    private final SettingGroup sgAlert   = settings.createGroup("Alert");

    // ── General ──────────────────────────────────────────────────────────────
    private final Setting<Set<EntityType<?>>> overworldMobs = sgGeneral.add(new EntityTypeListSetting.Builder()
        .name("mobs")
        .description("Overworld mobs to flag when detected in the Nether.")
        .defaultValue(new ObjectOpenHashSet<>(Set.of(
            // Passive overworld creatures
            EntityType.COW,
            EntityType.SHEEP,
            EntityType.PIG,
            EntityType.HORSE,
            EntityType.DONKEY,
            EntityType.MULE,
            EntityType.LLAMA,
            EntityType.TRADER_LLAMA,
            EntityType.RABBIT,
            EntityType.FOX,
            EntityType.WOLF,
            EntityType.CAT,
            EntityType.OCELOT,
            EntityType.POLAR_BEAR,
            EntityType.PANDA,
            EntityType.GOAT,
            EntityType.AXOLOTL,
            EntityType.BEE,
            EntityType.IRON_GOLEM,
            EntityType.SNOW_GOLEM,
            EntityType.VILLAGER,
            EntityType.WANDERING_TRADER,
            EntityType.BAT,
            // Overworld hostiles (excluding nether-native mobs)
            EntityType.ZOMBIE_VILLAGER,
            EntityType.WITCH,    // can enter nether via portal
            EntityType.CREEPER,
            EntityType.SKELETON,
            EntityType.SPIDER,
            EntityType.CAVE_SPIDER,
            EntityType.SLIME,
            EntityType.ZOMBIE,
            EntityType.DROWNED,
            EntityType.HUSK,
            EntityType.STRAY,
            EntityType.PHANTOM
            // NOT included: Enderman (nether-native), Chicken (zombie-jockey),
            // Blaze, Ghast, Piglin, Hoglin, Zoglin, Strider, Wither Skeleton, Magma Cube
        )))
        .build()
    );

    private final Setting<Integer> scanRadius = sgGeneral.add(new IntSetting.Builder()
        .name("scan-radius")
        .description("Chunk radius to scan for living entities.")
        .defaultValue(8)
        .min(1)
        .sliderRange(1, 16)
        .build()
    );

    private final Setting<Boolean> netherOnly = sgGeneral.add(new BoolSetting.Builder()
        .name("nether-only")
        .description("Only scan while in the Nether dimension.")
        .defaultValue(true)
        .build()
    );

    // ── Alert ────────────────────────────────────────────────────────────────
    private final Setting<Boolean> soundAlert = sgAlert.add(new BoolSetting.Builder()
        .name("sound-alert")
        .description("Play a sound when a new overworld mob is detected.")
        .defaultValue(true)
        .build()
    );

    private final Setting<AlertSound> alertSound = sgAlert.add(new EnumSetting.Builder<AlertSound>()
        .name("alert-sound")
        .defaultValue(AlertSound.PLING)
        .visible(soundAlert::get)
        .build()
    );

    private final Setting<String> webhookLink = sgAlert.add(new StringSetting.Builder()
        .name("webhook-link")
        .description("Discord webhook URL. Leave blank to disable.")
        .defaultValue("")
        .build()
    );

    private final Setting<Integer> alertCooldown = sgAlert.add(new IntSetting.Builder()
        .name("alert-cooldown")
        .description("Seconds before the same entity UUID can trigger another alert.")
        .defaultValue(30)
        .min(5)
        .sliderRange(5, 120)
        .build()
    );

    // ── Render ───────────────────────────────────────────────────────────────
    private final Setting<Boolean> renderESP = sgRender.add(new BoolSetting.Builder()
        .name("render-esp")
        .description("Draw a box around detected overworld mobs.")
        .defaultValue(true)
        .build()
    );

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .defaultValue(ShapeMode.Both)
        .build()
    );

    private final Setting<SettingColor> mobSideColor = sgRender.add(new ColorSetting.Builder()
        .name("mob-color")
        .description("Fill color for detected overworld mobs.")
        .defaultValue(new SettingColor(255, 220, 0, 40))
        .build()
    );

    private final Setting<SettingColor> mobLineColor = sgRender.add(new ColorSetting.Builder()
        .name("mob-line-color")
        .description("Outline color for detected overworld mobs.")
        .defaultValue(new SettingColor(255, 220, 0, 255))
        .build()
    );

    // ── State ─────────────────────────────────────────────────────────────────
    /** UUID → last alert timestamp (ms). Prevents duplicate alerts. */
    private final Map<UUID, Long> alertCooldowns = new HashMap<>();
    /** Mobs detected last tick — rebuilt every tick for rendering. */
    private final List<Entity> detectedMobs = new ArrayList<>();

    public NetherMobDetector() {
        super(Categories.Hunting,
            "nether-mob-detector",
            "Flags overworld mobs in the Nether — evidence of player activity or a nearby base.");
    }

    @Override
    public void onActivate() {
        alertCooldowns.clear();
        detectedMobs.clear();
    }

    @Override
    public void onDeactivate() {
        alertCooldowns.clear();
        detectedMobs.clear();
    }

    // ── Tick ──────────────────────────────────────────────────────────────────

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.world == null) return;
        if (netherOnly.get() && !mc.world.getRegistryKey().equals(World.NETHER)) return;

        detectedMobs.clear();

        double r = scanRadius.get() * 16.0;
        Box searchBox = mc.player.getBoundingBox().expand(r, r * 0.5, r);
        Set<EntityType<?>> targets = overworldMobs.get();

        List<LivingEntity> candidates = mc.world.getEntitiesByClass(
            LivingEntity.class,
            searchBox,
            e -> e != mc.player && targets.contains(e.getType())
        );

        long now = System.currentTimeMillis();

        for (LivingEntity entity : candidates) {
            detectedMobs.add(entity);

            UUID uuid      = entity.getUuid();
            long lastAlert = alertCooldowns.getOrDefault(uuid, 0L);
            if (now - lastAlert < alertCooldown.get() * 1_000L) continue;
            alertCooldowns.put(uuid, now);

            String mobName = entity.getType().getName().getString();
            int    dist    = (int) mc.player.distanceTo(entity);
            String coords  = (int) entity.getX() + " " + (int) entity.getY() + " " + (int) entity.getZ();

            info("Overworld mob: §c" + mobName + "§r at " + coords + " (" + dist + "m)");

            if (soundAlert.get()) playAlert();

            if (!webhookLink.get().isEmpty()) {
                final String msg    = "Mob: **" + mobName + "**"
                    + "\\nCoords: `" + coords + "`"
                    + "\\nDistance: " + dist + " blocks";
                final String player = mc.player.getGameProfile().getName();
                new Thread(() -> HuntingUtils.sendWebhook(
                    webhookLink.get(),
                    "NetherMobDetector: " + mobName,
                    msg, null, player
                )).start();
            }
        }
    }

    // ── Render ────────────────────────────────────────────────────────────────

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (!renderESP.get() || mc.player == null) return;
        for (Entity e : detectedMobs) {
            Box box = e.getBoundingBox().expand(0.1);
            event.renderer.box(box, mobSideColor.get(), mobLineColor.get(), shapeMode.get(), 0);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void playAlert() {
        if (mc.player == null || mc.world == null) return;
        SoundEvent sound = switch (alertSound.get()) {
            case NOTE_BLOCK -> SoundEvents.BLOCK_NOTE_BLOCK_BASS.value();
            case PLING      -> SoundEvents.BLOCK_NOTE_BLOCK_PLING.value();
            case BELL       -> SoundEvents.BLOCK_BELL_USE;
        };
        mc.world.playSoundFromEntity(
            mc.player, mc.player, sound,
            SoundCategory.BLOCKS,
            2.0f, 1.0f
        );
    }
}
