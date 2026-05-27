/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.world;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.network.ChestApiClient;
import meteordevelopment.meteorclient.utils.network.ItemData;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChestScanner extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgApi = settings.createGroup("API");

    private final Setting<Boolean> autoScan = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-scan")
        .description("Automatically scan and upload chests when opened.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> notifications = sgGeneral.add(new BoolSetting.Builder()
        .name("notifications")
        .description("Show chat notifications when scanning.")
        .defaultValue(true)
        .build()
    );

    private final Setting<String> apiUrl = sgApi.add(new StringSetting.Builder()
        .name("webhook-url")
        .description("Your API webhook URL (paste from your dashboard).")
        .defaultValue("")
        .build()
    );

    private final Setting<String> apiKey = sgApi.add(new StringSetting.Builder()
        .name("api-key")
        .description("Your API key for authentication.")
        .defaultValue("")
        .build()
    );

    private ChestApiClient apiClient;

    public ChestScanner() {
        super(Categories.World, "chest-scanner", "Scans and uploads chest contents to your database.");
    }

    @Override
    public void onActivate() {
        // Validate configuration
        if (apiUrl.get().isEmpty()) {
            error("Webhook URL not configured! Set it in module settings.");
            toggle();
            return;
        }

        if (apiKey.get().isEmpty()) {
            warning("API Key not set. Upload may fail if required.");
        }

        apiClient = new ChestApiClient(apiUrl.get(), apiKey.get());

        if (notifications.get()) {
            info("Chest Scanner enabled. Auto-scan: " + (autoScan.get() ? "ON" : "OFF"));
        }
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        if (!autoScan.get()) return;
        if (!(event.screen instanceof HandledScreen)) return;

        // Wait a bit for the screen to fully load
        MinecraftClient client = MinecraftClient.getInstance();
        new Thread(() -> {
            try {
                Thread.sleep(500);
                client.execute(this::scanCurrentChest);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    public void scanCurrentChest() {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null) return;

        if (client.currentScreen instanceof GenericContainerScreen screen) {
            scanGenericContainer(client, screen);
        } else if (client.currentScreen instanceof ShulkerBoxScreen screen) {
            scanShulkerBox(client, screen);
        } else if (client.currentScreen instanceof HopperScreen screen) {
            scanHopper(client, screen);
        } else if (client.currentScreen instanceof Generic3x3ContainerScreen screen) {
            scanDispenser(client, screen);
        } else {
            if (notifications.get()) {
                error("No container open! Open a chest first.");
            }
        }
    }

    private void scanGenericContainer(MinecraftClient client, GenericContainerScreen screen) {
        String title = screen.getTitle().getString();
        List<ItemData> items = extractItemsFromSlots(screen.getScreenHandler().slots, screen.getScreenHandler().getRows() * 9);

        if (items.isEmpty()) {
            if (notifications.get()) {
                warning("Chest is empty, skipping upload.");
            }
            return;
        }

        uploadItems(client, title, items);
    }

    private void scanShulkerBox(MinecraftClient client, ShulkerBoxScreen screen) {
        String title = screen.getTitle().getString();
        List<ItemData> items = extractItemsFromSlots(screen.getScreenHandler().slots, 27);

        if (items.isEmpty()) return;
        uploadItems(client, title, items);
    }

    private void scanHopper(MinecraftClient client, HopperScreen screen) {
        String title = screen.getTitle().getString();
        List<ItemData> items = extractItemsFromSlots(screen.getScreenHandler().slots, 5);

        if (items.isEmpty()) return;
        uploadItems(client, title, items);
    }

    private void scanDispenser(MinecraftClient client, Generic3x3ContainerScreen screen) {
        String title = screen.getTitle().getString();
        List<ItemData> items = extractItemsFromSlots(screen.getScreenHandler().slots, 9);

        if (items.isEmpty()) return;
        uploadItems(client, title, items);
    }

    private List<ItemData> extractItemsFromSlots(List<Slot> slots, int containerSize) {
        Map<String, ItemData> itemMap = new HashMap<>();

        for (int i = 0; i < Math.min(containerSize, slots.size()); i++) {
            Slot slot = slots.get(i);
            ItemStack stack = slot.getStack();

            if (stack.isEmpty()) continue;

            String itemName = stack.getName().getString();
            int count = stack.getCount();

            if (itemMap.containsKey(itemName)) {
                ItemData existing = itemMap.get(itemName);
                existing.quantity += count;
            } else {
                String category = categorizeItem(stack);
                itemMap.put(itemName, new ItemData(itemName, count, category));
            }
        }

        return new ArrayList<>(itemMap.values());
    }

    private String categorizeItem(ItemStack stack) {
        String itemId = stack.getItem().toString();

        if (itemId.contains("diamond") || itemId.contains("emerald") || itemId.contains("gold") ||
            itemId.contains("iron") || itemId.contains("netherite")) {
            return "resources";
        } else if (itemId.contains("sword") || itemId.contains("bow") || itemId.contains("crossbow") ||
                   itemId.contains("trident")) {
            return "weapons";
        } else if (itemId.contains("pickaxe") || itemId.contains("axe") || itemId.contains("shovel") ||
                   itemId.contains("hoe")) {
            return "tools";
        } else if (itemId.contains("helmet") || itemId.contains("chestplate") || itemId.contains("leggings") ||
                   itemId.contains("boots") || itemId.contains("elytra")) {
            return "armor";
        } else if (itemId.contains("apple") || itemId.contains("bread") || itemId.contains("cooked") ||
                   itemId.contains("stew") || itemId.contains("soup")) {
            return "food";
        } else if (itemId.contains("potion") || itemId.contains("totem")) {
            return "special";
        } else if (itemId.contains("shulker")) {
            return "storage";
        } else if (stack.getItem().toString().contains("Block")) {
            return "blocks";
        }

        return "other";
    }

    private void uploadItems(MinecraftClient client, String location, List<ItemData> items) {
        if (notifications.get()) {
            info("Scanning... Found " + items.size() + " unique items.");
        }

        // Validate configuration before uploading
        if (apiUrl.get().isEmpty()) {
            error("Webhook URL not configured! Open module settings to set it.");
            return;
        }

        new Thread(() -> {
            try {
                if (apiClient == null) {
                    apiClient = new ChestApiClient(apiUrl.get(), apiKey.get());
                }

                apiClient.uploadChestContents(location, items);

                client.execute(() -> {
                    if (notifications.get()) {
                        info("Successfully uploaded chest contents!");
                    }
                });
            } catch (Exception e) {
                MeteorClient.LOG.error("Failed to upload chest contents", e);
                client.execute(() -> {
                    if (notifications.get()) {
                        error("Failed to upload: " + e.getMessage());
                    }
                });
            }
        }).start();
    }
}
