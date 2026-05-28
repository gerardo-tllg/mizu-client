/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.hunting;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.ItemEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;

import java.util.*;

/**
 * Search — item entity ESP + alerting module.
 *
 * Scans loaded entities every tick for dropped items matching the configured
 * list. Items found in or adjacent to lava are treated as high priority:
 * netherite items are immune to fire and float on lava, so a netherite
 * item detected in nether lava is strong portal-skip evidence.
 *
 * On Paper/Folia the scan is purely client-side (entity list traversal),
 * so there are no region-thread concerns.
 */
public class Search extends Module {

    // ── Alert sound enum ─────────────────────────────────────────────────────
    public enum AlertSound { NOTE_BLOCK, PLING, BELL, EXPERIENCE_ORB }

    // ── Setting groups ───────────────────────────────────────────────────────
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender  = settings.createGroup("Render");
    private final SettingGroup sgAlert   = settings.createGroup("Alert");

    // ── General ──────────────────────────────────────────────────────────────
    private final Setting<List<Item>> items = sgGeneral.add(new ItemListSetting.Builder()
        .name("items")
        .description("Items to scan for on the ground.")
        .defaultValue(
            Items.ELYTRA,
            Items.NETHERITE_HELMET,
            Items.NETHERITE_CHESTPLATE,
            Items.NETHERITE_LEGGINGS,
            Items.NETHERITE_BOOTS,
            Items.NETHERITE_SWORD,
            Items.NETHERITE_PICKAXE,
            Items.NETHERITE_AXE,
            Items.NETHERITE_SHOVEL,
            Items.NETHERITE_HOE,
            Items.NETHERITE_SCRAP,
            Items.ANCIENT_DEBRIS,
            Items.TOTEM_OF_UNDYING,
            Items.ENCHANTED_GOLDEN_APPLE
        )
        .build()
    );

    private final Setting<Integer> scanRadius = sgGeneral.add(new IntSetting.Builder()
        .name("scan-radius")
        .description("Chunk radius around the player to scan for item entities.")
        .defaultValue(5)
        .min(1)
        .sliderRange(1, 16)
        .build()
    );

    private final Setting<Boolean> lavaItems = sgGeneral.add(new BoolSetting.Builder()
        .name("lava-items")
        .description("Separately highlight items that are in or directly above lava (high priority).")
        .defaultValue(true)
        .build()
    );

    // ── Alert ────────────────────────────────────────────────────────────────
    private final Setting<Boolean> soundAlert = sgAlert.add(new BoolSetting.Builder()
        .name("sound-alert")
        .description("Play an in-game sound when a matching item is first detected.")
        .defaultValue(true)
        .build()
    );

    private final Setting<AlertSound> alertSound = sgAlert.add(new EnumSetting.Builder<AlertSound>()
        .name("alert-sound")
        .defaultValue(AlertSound.PLING)
        .visible(soundAlert::get)
        .build()
    );

    private final Setting<Double> alertVolume = sgAlert.add(new DoubleSetting.Builder()
        .name("alert-volume")
        .defaultValue(1.0)
        .sliderRange(0.1, 3.0)
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
        .description("Seconds before the same item UUID can trigger another alert.")
        .defaultValue(10)
        .min(1)
        .sliderRange(1, 60)
        .build()
    );

    // ── Render ───────────────────────────────────────────────────────────────
    private final Setting<Boolean> renderESP = sgRender.add(new BoolSetting.Builder()
        .name("render-esp")
        .description("Draw a box around detected items.")
        .defaultValue(true)
        .build()
    );

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .defaultValue(ShapeMode.Both)
        .build()
    );

    private final Setting<SettingColor> itemSideColor = sgRender.add(new ColorSetting.Builder()
        .name("item-color")
        .description("Fill color for detected items.")
        .defaultValue(new SettingColor(255, 255, 0, 40))
        .build()
    );

    private final Setting<SettingColor> itemLineColor = sgRender.add(new ColorSetting.Builder()
        .name("item-line-color")
        .description("Outline color for detected items.")
        .defaultValue(new SettingColor(255, 255, 0, 255))
        .build()
    );

    private final Setting<SettingColor> lavaSideColor = sgRender.add(new ColorSetting.Builder()
        .name("lava-item-color")
        .description("Fill color for items in or above lava.")
        .defaultValue(new SettingColor(255, 40, 0, 60))
        .visible(lavaItems::get)
        .build()
    );

    private final Setting<SettingColor> lavaLineColor = sgRender.add(new ColorSetting.Builder()
        .name("lava-item-line-color")
        .description("Outline color for items in or above lava.")
        .defaultValue(new SettingColor(255, 40, 0, 255))
        .visible(lavaItems::get)
        .build()
    );

    // ── State ─────────────────────────────────────────────────────────────────
    /** UUID → last alert timestamp (ms). Prevents duplicate alerts per item. */
    private final Map<UUID, Long> alertCooldowns = new HashMap<>();
    /** Items detected last tick — rebuilt every tick for rendering. */
    private final List<ItemEntity> detectedNormal = new ArrayList<>();
    private final List<ItemEntity> detectedLava   = new ArrayList<>();

    public Search() {
        super(Categories.Hunting,
            "search",
            "ESP and alerts for high-value dropped items. Netherite in lava = highest priority (portal skip indicator).");
    }

    @Override
    public void onActivate() {
        alertCooldowns.clear();
        detectedNormal.clear();
        detectedLava.clear();
    }

    @Override
    public void onDeactivate() {
        alertCooldowns.clear();
        detectedNormal.clear();
        detectedLava.clear();
    }

    // ── Tick ──────────────────────────────────────────────────────────────────

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.world == null) return;

        detectedNormal.clear();
        detectedLava.clear();

        double r = scanRadius.get() * 16.0;
        Box searchBox = mc.player.getBoundingBox().expand(r, r * 0.5, r);

        List<ItemEntity> found = mc.world.getEntitiesByClass(
            ItemEntity.class,
            searchBox,
            e -> items.get().contains(e.getStack().getItem())
        );

        long now = System.currentTimeMillis();
        Set<UUID> scannedUuids = new HashSet<>();

        for (ItemEntity entity : found) {
            UUID uuid = entity.getUuid();
            scannedUuids.add(uuid);

            boolean inLava = lavaItems.get() && isNearLava(entity);

            if (inLava) detectedLava.add(entity);
            else        detectedNormal.add(entity);

            // Alert logic (respects cooldown per UUID)
            long lastAlert = alertCooldowns.getOrDefault(uuid, 0L);
            if (now - lastAlert < alertCooldown.get() * 1_000L) continue;
            alertCooldowns.put(uuid, now);

            String itemName = entity.getStack().getItem().getName().getString();
            String coords   = (int) entity.getX() + " " + (int) entity.getY() + " " + (int) entity.getZ();
            String lavaTag  = inLava ? " §c[IN LAVA]§r" : "";

            info("Found §b" + itemName + "§r at " + coords + lavaTag);

            if (soundAlert.get()) playAlert();

            if (!webhookLink.get().isEmpty()) {
                final String msg    = "Item: **" + itemName + "**" + (inLava ? " *(IN LAVA — high priority)*" : "")
                    + "\\nCoords: `" + coords + "`";
                final String player = mc.player.getGameProfile().getName();
                new Thread(() -> HuntingUtils.sendWebhook(
                    webhookLink.get(),
                    "Search: " + itemName + " detected",
                    msg, null, player
                )).start();
            }
        }

        // Remove cooldown entries for items that are no longer in scan range (despawned/moved away)
        alertCooldowns.keySet().removeIf(uuid -> !scannedUuids.contains(uuid));
    }

    // ── Render ────────────────────────────────────────────────────────────────

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (!renderESP.get() || mc.player == null) return;
        Camera camera = mc.gameRenderer.getCamera();

        for (ItemEntity e : detectedLava) {
            renderItemBox(event, e, camera, lavaSideColor.get(), lavaLineColor.get());
        }
        for (ItemEntity e : detectedNormal) {
            renderItemBox(event, e, camera, itemSideColor.get(), itemLineColor.get());
        }
    }

    private void renderItemBox(Render3DEvent event, ItemEntity e, Camera camera, SettingColor side, SettingColor line) {
        double ix = MathHelper.lerp(event.tickDelta, e.lastX, e.getX()) - camera.getPos().x;
        double iy = MathHelper.lerp(event.tickDelta, e.lastY, e.getY()) - camera.getPos().y;
        double iz = MathHelper.lerp(event.tickDelta, e.lastZ, e.getZ()) - camera.getPos().z;
        Box world = e.getBoundingBox().expand(0.15);
        double hw = (world.maxX - world.minX) / 2.0;
        double hd = (world.maxZ - world.minZ) / 2.0;
        double height = world.maxY - world.minY;
        event.renderer.box(ix - hw, iy, iz - hd, ix + hw, iy + height, iz + hd,
            side, line, shapeMode.get(), 0);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Returns true if the item entity is inside lava fluid or positioned directly
     * above a lava block (netherite items float on lava surface).
     */
    private boolean isNearLava(ItemEntity entity) {
        if (mc.world == null) return false;
        BlockPos pos = entity.getBlockPos();
        // Block the entity occupies
        FluidState fs = mc.world.getFluidState(pos);
        if (fs.getFluid() == Fluids.LAVA || fs.getFluid() == Fluids.FLOWING_LAVA) return true;
        // Block directly below (item floating on lava surface)
        FluidState below = mc.world.getFluidState(pos.down());
        return below.getFluid() == Fluids.LAVA || below.getFluid() == Fluids.FLOWING_LAVA;
    }

    private void playAlert() {
        if (mc.player == null || mc.world == null) return;
        SoundEvent sound = switch (alertSound.get()) {
            case NOTE_BLOCK     -> SoundEvents.BLOCK_NOTE_BLOCK_BASS.value();
            case PLING          -> SoundEvents.BLOCK_NOTE_BLOCK_PLING.value();
            case BELL           -> SoundEvents.BLOCK_BELL_USE;
            case EXPERIENCE_ORB -> SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP;
        };
        mc.world.playSoundFromEntity(
            mc.player, mc.player, sound,
            SoundCategory.BLOCKS,
            alertVolume.get().floatValue(), 1.0f
        );
    }
}
