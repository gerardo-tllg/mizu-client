package meteordevelopment.meteorclient.systems.modules.hunting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.pathing.BaritoneUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.SlotUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.io.*;
import java.util.*;

/**
 * AutoRegear - Automatically regears from ender chest during AFK hunting or PvP
 * 
 * Usage:
 * 1. Fill inventory with desired items when ready
 * 2. Toggle "save-snapshot" to save inventory state
 * 3. Module auto-triggers when inventory differs from snapshot
 * 4. Pauses Baritone/TrailFollower, builds safe box, regears from echest, resumes
 * 
 * Snapshot persists to file - survives disconnects/kicks/crashes
 * 
 * Use Cases:
 * - AFK Hunting: Auto-regear rockets, elytras, food during long flights
 * - PvP: Auto-regear after respawn or during combat breaks
 * 
 * Works alongside Loadouts module but adds automation + safety features
 */
public class AutoRegear extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgTriggers = settings.createGroup("Triggers");
    private final SettingGroup sgSafety = settings.createGroup("Safety");

    // General
    private final Setting<Integer> tickDelay = sgGeneral.add(new IntSetting.Builder()
        .name("tick-delay")
        .description("Delay in ticks between actions.")
        .defaultValue(4)
        .min(1)
        .sliderMax(20)
        .build()
    );

    private final Setting<Boolean> pauseOnRegear = sgGeneral.add(new BoolSetting.Builder()
        .name("pause-baritone")
        .description("Automatically pause Baritone when regearing.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> resumeAfterRegear = sgGeneral.add(new BoolSetting.Builder()
        .name("resume-baritone")
        .description("Automatically resume Baritone after regearing.")
        .defaultValue(true)
        .build()
    );

    // Triggers
    private final Setting<Boolean> autoTrigger = sgTriggers.add(new BoolSetting.Builder()
        .name("auto-trigger")
        .description("Automatically start regearing when inventory differs from snapshot.")
        .defaultValue(true)
        .build()
    );

    private final Setting<RegearMode> regearMode = sgTriggers.add(new EnumSetting.Builder<RegearMode>()
        .name("regear-mode")
        .description("How aggressive to regear. Change anytime!")
        .defaultValue(RegearMode.BALANCED)
        .build()
    );

    private final Setting<Integer> triggerThreshold = sgTriggers.add(new IntSetting.Builder()
        .name("difference-threshold")
        .description("Number of slot differences needed to trigger regear. Override with regear-mode.")
        .defaultValue(10)
        .min(1)
        .sliderMax(36)
        .visible(() -> regearMode.get() == RegearMode.CUSTOM)
        .build()
    );

    private final Setting<Integer> minElytraDurability = sgTriggers.add(new IntSetting.Builder()
        .name("min-elytra-durability")
        .description("Trigger regear if elytra durability % drops below this. Override with regear-mode.")
        .defaultValue(20)
        .min(1)
        .max(100)
        .sliderMax(100)
        .visible(() -> regearMode.get() == RegearMode.CUSTOM)
        .build()
    );

    // Safety
    private final Setting<Boolean> buildObsidianBox = sgSafety.add(new BoolSetting.Builder()
        .name("build-obsidian-box")
        .description("Build obsidian box for safety during regear.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> breakBoxAfter = sgSafety.add(new BoolSetting.Builder()
        .name("break-box-after")
        .description("Break obsidian box after regearing.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> checkSafeSpot = sgSafety.add(new BoolSetting.Builder()
        .name("check-safe-spot")
        .description("Verify no players nearby before regearing.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> safeRadius = sgSafety.add(new IntSetting.Builder()
        .name("safe-radius")
        .description("Radius to check for players.")
        .defaultValue(32)
        .min(8)
        .sliderMax(64)
        .build()
    );

    // State management
    private RegearState state = RegearState.IDLE;
    private int tickTimer = 0;
    private BlockPos regearPos = null;
    private boolean wasBaritoneActive = false;
    private TrailFollower trailFollower = null;
    private boolean wasTrailFollowerActive = false;
    
    // Inventory snapshot: slot -> ItemStack data
    private final Map<Integer, InventorySlot> inventorySnapshot = new HashMap<>();
    private final List<BlockPos> obsidianBlocks = new ArrayList<>();
    private BlockPos enderChestPos = null;
    private int regearAttempts = 0;
    private static final int MAX_REGEAR_ATTEMPTS = 3;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private File getSnapshotFile() {
        return new File(MeteorClient.FOLDER, "autoregear-snapshot.json");
    }

    public AutoRegear() {
        super(Categories.Hunting, "auto-regear", "Automatically regears from ender chest to match saved inventory snapshot. Works for AFK hunting and PvP.");
    }

    @Override
    public void onActivate() {
        state = RegearState.IDLE;
        tickTimer = 0;
        regearPos = null;
        obsidianBlocks.clear();
        enderChestPos = null;
        trailFollower = Modules.get().get(TrailFollower.class);
        regearAttempts = 0;
        
        // Load snapshot from file
        loadSnapshotFromFile();
        
        if (inventorySnapshot.isEmpty()) {
            warning("No inventory snapshot saved! Use chat command: .autoregear save");
        } else {
            info("Loaded snapshot with " + inventorySnapshot.size() + " slots");
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;

        tickTimer++;
        if (tickTimer < tickDelay.get()) return;
        tickTimer = 0;

        switch (state) {
            case IDLE -> {
                if (autoTrigger.get() && shouldRegear()) {
                    info("Regear triggered - inventory differs from snapshot");
                    startRegear();
                }
            }
            case PAUSING -> pauseModules();
            case CHECKING_SAFETY -> checkSafety();
            case BUILDING_BOX -> buildObsidianBox();
            case PLACING_ENDER_CHEST -> placeEnderChest();
            case OPENING_ENDER_CHEST -> openEnderChest();
            case REGEARING -> performRegear();
            case CLOSING_CHEST -> closeChest();
            case BREAKING_BOX -> breakObsidianBox();
            case RESUMING -> resumeModules();
        }
    }

    // Public methods for command usage
    public void saveSnapshot() {
        if (mc.player == null) return;
        
        inventorySnapshot.clear();
        
        for (int slot = 0; slot < 36; slot++) {
            ItemStack stack = mc.player.getInventory().getStack(slot);
            if (!stack.isEmpty()) {
                InventorySlot slotData = new InventorySlot();
                slotData.slot = slot;
                slotData.itemId = Registries.ITEM.getId(stack.getItem()).toString();
                slotData.count = stack.getCount();
                slotData.customName = stack.contains(DataComponentTypes.CUSTOM_NAME) ? 
                    stack.getName().getString() : null;
                
                inventorySnapshot.put(slot, slotData);
            }
        }
        
        saveSnapshotToFile();
        info("Saved snapshot: " + inventorySnapshot.size() + " slots");
    }

    public void clearSnapshot() {
        inventorySnapshot.clear();
        saveSnapshotToFile();
        info("Snapshot cleared");
    }

    public int getSnapshotSize() {
        return inventorySnapshot.size();
    }

    // Public method to force regear
    public void forceRegear() {
        if (state != RegearState.IDLE) {
            warning("Already regearing, please wait");
            return;
        }
        
        if (inventorySnapshot.isEmpty()) {
            error("No snapshot saved - use .ar save first");
            return;
        }
        
        info("Manual regear triggered");
        startRegear();
    }

    private boolean shouldRegear() {
        if (inventorySnapshot.isEmpty()) return false;
        
        // Get thresholds based on mode
        int durabilityThreshold = getElytraDurabilityThreshold();
        int slotThreshold = getSlotDifferenceThreshold();
        
        // Check elytra durability first
        ItemStack chestplate = mc.player.getInventory().getStack(SlotUtils.ARMOR_START + 2);
        if (chestplate.getItem() == Items.ELYTRA) {
            int maxDur = chestplate.getMaxDamage();
            int currentDur = maxDur - chestplate.getDamage();
            double percent = (currentDur / (double) maxDur) * 100;
            if (percent < durabilityThreshold) {
                return true;
            }
        }

        // Count differences between current inventory and snapshot
        int differences = 0;
        for (int slot = 0; slot < 36; slot++) { // Main inventory only
            ItemStack current = mc.player.getInventory().getStack(slot);
            InventorySlot snapshotSlot = inventorySnapshot.get(slot);
            
            if (!itemStacksMatch(current, snapshotSlot)) {
                differences++;
            }
        }

        return differences >= slotThreshold;
    }

    private int getElytraDurabilityThreshold() {
        return switch (regearMode.get()) {
            case EMERGENCY -> 5;      // Only when critical
            case CONSERVATIVE -> 10;   // Low durability
            case BALANCED -> 20;       // Default
            case AGGRESSIVE -> 40;     // Early regear
            case TOP_OFF -> 50;        // Keep topped off
            case CUSTOM -> minElytraDurability.get();
        };
    }

    private int getSlotDifferenceThreshold() {
        return switch (regearMode.get()) {
            case EMERGENCY -> 20;      // Only when very empty
            case CONSERVATIVE -> 15;   // Most items used
            case BALANCED -> 10;       // Default
            case AGGRESSIVE -> 5;      // Few items missing
            case TOP_OFF -> 1;         // Any item missing (PvP top-off mode)
            case CUSTOM -> triggerThreshold.get();
        };
    }

    private boolean itemStacksMatch(ItemStack current, InventorySlot snapshot) {
        if (snapshot == null) return current.isEmpty();
        if (current.isEmpty()) return false;
        
        // Check item type
        if (!Registries.ITEM.getId(current.getItem()).toString().equals(snapshot.itemId)) {
            return false;
        }
        
        // Check count (allow some variance for consumables)
        if (Math.abs(current.getCount() - snapshot.count) > 5) {
            return false;
        }
        
        return true;
    }

    private void saveSnapshotToFile() {
        try {
            File file = getSnapshotFile();
            file.getParentFile().mkdirs();
            try (Writer writer = new FileWriter(file)) {
                GSON.toJson(new ArrayList<>(inventorySnapshot.values()), writer);
            }
        } catch (IOException e) {
            error("Failed to save snapshot: " + e.getMessage());
        }
    }

    private void loadSnapshotFromFile() {
        File file = getSnapshotFile();
        if (!file.exists()) return;
        
        try (Reader reader = new FileReader(file)) {
            InventorySlot[] slots = GSON.fromJson(reader, InventorySlot[].class);
            if (slots != null) {
                inventorySnapshot.clear();
                for (InventorySlot slot : slots) {
                    inventorySnapshot.put(slot.slot, slot);
                }
            }
        } catch (IOException e) {
            error("Failed to load snapshot: " + e.getMessage());
        }
    }

    private void startRegear() {
        if (regearAttempts >= MAX_REGEAR_ATTEMPTS) {
            error("Max regear attempts reached, resetting");
            state = RegearState.IDLE;
            regearAttempts = 0;
            return;
        }
        
        state = RegearState.PAUSING;
        regearPos = mc.player.getBlockPos();
        obsidianBlocks.clear();
        regearAttempts++;
    }

    private void pauseModules() {
        // Pause Baritone
        if (pauseOnRegear.get() && BaritoneUtils.IS_AVAILABLE) {
            try {
                // Use reflection to avoid compile-time dependency
                Class<?> baritoneAPI = Class.forName("baritone.api.BaritoneAPI");
                Object provider = baritoneAPI.getMethod("getProvider").invoke(null);
                Object baritone = provider.getClass().getMethod("getPrimaryBaritone").invoke(provider);
                Object pathingBehavior = baritone.getClass().getMethod("getPathingBehavior").invoke(baritone);
                wasBaritoneActive = (Boolean) pathingBehavior.getClass().getMethod("isPathing").invoke(pathingBehavior);
                
                if (wasBaritoneActive) {
                    Object commandManager = baritone.getClass().getMethod("getCommandManager").invoke(baritone);
                    commandManager.getClass().getMethod("execute", String.class).invoke(commandManager, "pause");
                    info("Paused Baritone");
                }
            } catch (Exception e) {
                warning("Failed to pause Baritone: " + e.getMessage());
            }
        }

        // Pause TrailFollower
        if (trailFollower != null && trailFollower.isActive()) {
            wasTrailFollowerActive = true;
            trailFollower.toggle();
            info("Paused TrailFollower");
        }

        state = RegearState.CHECKING_SAFETY;
    }

    private void checkSafety() {
        if (checkSafeSpot.get() && !isSafeSpot()) {
            warning("Players nearby - waiting for safety");
            return;
        }
        
        state = RegearState.BUILDING_BOX;
    }

    private boolean isSafeSpot() {
        // Check for nearby players
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;
            if (Friends.get().isFriend(player)) continue;
            
            if (PlayerUtils.isWithin(player, safeRadius.get())) {
                return false;
            }
        }
        return true;
    }

    private void buildObsidianBox() {
        if (!buildObsidianBox.get()) {
            state = RegearState.PLACING_ENDER_CHEST;
            return;
        }

        FindItemResult obsidian = InvUtils.find(Items.OBSIDIAN);
        if (!obsidian.found()) {
            error("No obsidian found - skipping safety box");
            state = RegearState.PLACING_ENDER_CHEST;
            return;
        }

        BlockPos playerPos = mc.player.getBlockPos();
        
        // Build 3x3x3 box with player in center
        List<BlockPos> boxPositions = getBoxPositions(playerPos);
        
        for (BlockPos pos : boxPositions) {
            if (mc.world.getBlockState(pos).isReplaceable()) {
                if (BlockUtils.place(pos, obsidian, false, 50, true)) {
                    obsidianBlocks.add(pos);
                    return; // Place one per tick
                }
            }
        }

        info("Obsidian box complete");
        state = RegearState.PLACING_ENDER_CHEST;
    }

    private List<BlockPos> getBoxPositions(BlockPos center) {
        List<BlockPos> positions = new ArrayList<>();
        
        // Floor and ceiling
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                positions.add(center.add(x, -1, z)); // Floor
                positions.add(center.add(x, 2, z));  // Ceiling
            }
        }
        
        // Walls
        for (int y = 0; y <= 1; y++) {
            positions.add(center.add(-1, y, -1));
            positions.add(center.add(-1, y, 0));
            positions.add(center.add(-1, y, 1));
            positions.add(center.add(1, y, -1));
            positions.add(center.add(1, y, 0));
            positions.add(center.add(1, y, 1));
            positions.add(center.add(0, y, -1));
            positions.add(center.add(0, y, 1));
        }
        
        return positions;
    }

    private void placeEnderChest() {
        FindItemResult enderChest = InvUtils.find(Items.ENDER_CHEST);
        if (!enderChest.found()) {
            error("No ender chest found - cannot regear");
            state = RegearState.BREAKING_BOX;
            return;
        }

        BlockPos placePos = mc.player.getBlockPos().add(0, 0, 1);
        
        if (mc.world.getBlockState(placePos).isReplaceable()) {
            if (BlockUtils.place(placePos, enderChest, false, 50, true)) {
                enderChestPos = placePos;
                info("Placed ender chest");
                state = RegearState.OPENING_ENDER_CHEST;
                tickTimer = -10; // Wait before opening
            }
        } else {
            // Already something there, try to use it
            if (mc.world.getBlockState(placePos).getBlock() instanceof EnderChestBlock) {
                enderChestPos = placePos;
                state = RegearState.OPENING_ENDER_CHEST;
            } else {
                error("Cannot place ender chest");
                state = RegearState.BREAKING_BOX;
            }
        }
    }

    private void openEnderChest() {
        if (enderChestPos == null) {
            error("Ender chest position lost");
            state = RegearState.BREAKING_BOX;
            return;
        }

        if (mc.world.getBlockState(enderChestPos).getBlock() instanceof EnderChestBlock) {
            BlockHitResult hitResult = new BlockHitResult(
                Vec3d.ofCenter(enderChestPos),
                Direction.UP,
                enderChestPos,
                false
            );
            
            BlockUtils.interact(hitResult, Hand.MAIN_HAND, false);
            tickTimer = -15; // Wait for screen to open
            state = RegearState.REGEARING;
            info("Opening ender chest");
        } else {
            error("Ender chest not found at expected position");
            state = RegearState.BREAKING_BOX;
        }
    }

    private void performRegear() {
        if (!(mc.player.currentScreenHandler instanceof GenericContainerScreenHandler)) {
            // Screen not open yet, wait
            return;
        }

        GenericContainerScreenHandler handler = (GenericContainerScreenHandler) mc.player.currentScreenHandler;
        
        // Regear based on snapshot
        for (InventorySlot targetSlot : inventorySnapshot.values()) {
            ItemStack current = mc.player.getInventory().getStack(targetSlot.slot);
            
            // Check if this slot needs regearing
            if (itemStacksMatch(current, targetSlot)) {
                continue; // Slot already matches
            }
            
            // Find item in ender chest
            for (int i = 0; i < handler.getRows() * 9; i++) {
                ItemStack chestStack = handler.getSlot(i).getStack();
                if (chestStack.isEmpty()) continue;
                
                String chestItemId = Registries.ITEM.getId(chestStack.getItem()).toString();
                if (chestItemId.equals(targetSlot.itemId)) {
                    // Found matching item, check if it's in a shulker or direct
                    if (targetSlot.customName != null && chestStack.contains(DataComponentTypes.CUSTOM_NAME)) {
                        String chestName = chestStack.getName().getString();
                        if (chestName.contains(targetSlot.customName)) {
                            // Take the shulker
                            mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
                            info("Took shulker: " + chestName);
                            return; // One action per tick
                        }
                    } else {
                        // Take the item directly
                        mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
                        return; // One action per tick
                    }
                }
            }
        }

        // If we get here, regear is complete (or as complete as possible)
        info("Regear complete");
        state = RegearState.CLOSING_CHEST;
    }

    private void closeChest() {
        if (mc.player.currentScreenHandler instanceof GenericContainerScreenHandler) {
            mc.player.closeHandledScreen();
        }
        state = RegearState.BREAKING_BOX;
        tickTimer = -5; // Wait a bit
    }

    private void breakObsidianBox() {
        if (!breakBoxAfter.get() || !buildObsidianBox.get()) {
            state = RegearState.RESUMING;
            return;
        }

        if (!obsidianBlocks.isEmpty()) {
            BlockPos toBreak = obsidianBlocks.remove(0);
            if (!mc.world.getBlockState(toBreak).isAir()) {
                BlockUtils.breakBlock(toBreak, false);
                return; // Break one per tick
            }
        }

        // Also break ender chest
        if (enderChestPos != null && mc.world.getBlockState(enderChestPos).getBlock() instanceof EnderChestBlock) {
            BlockUtils.breakBlock(enderChestPos, false);
            enderChestPos = null;
            return;
        }

        info("Box cleanup complete");
        state = RegearState.RESUMING;
    }

    private void resumeModules() {
        // Resume Baritone
        if (resumeAfterRegear.get() && wasBaritoneActive && BaritoneUtils.IS_AVAILABLE) {
            try {
                // Use reflection to avoid compile-time dependency
                Class<?> baritoneAPI = Class.forName("baritone.api.BaritoneAPI");
                Object provider = baritoneAPI.getMethod("getProvider").invoke(null);
                Object baritone = provider.getClass().getMethod("getPrimaryBaritone").invoke(provider);
                Object commandManager = baritone.getClass().getMethod("getCommandManager").invoke(baritone);
                commandManager.getClass().getMethod("execute", String.class).invoke(commandManager, "resume");
                info("Resumed Baritone");
            } catch (Exception e) {
                warning("Failed to resume Baritone: " + e.getMessage());
            }
        }

        // Resume TrailFollower
        if (wasTrailFollowerActive && trailFollower != null) {
            trailFollower.toggle();
            info("Resumed TrailFollower");
        }

        info("Regear cycle complete (attempt " + regearAttempts + "/" + MAX_REGEAR_ATTEMPTS + ")");
        state = RegearState.IDLE;
        wasBaritoneActive = false;
        wasTrailFollowerActive = false;
        
        // Reset attempts counter on successful completion
        if (regearAttempts > 0) {
            regearAttempts = 0;
        }
    }

    @Override
    public String getInfoString() {
        if (!inventorySnapshot.isEmpty()) {
            return state.toString() + " (" + inventorySnapshot.size() + " slots)";
        }
        return "No snapshot";
    }

    private enum RegearState {
        IDLE,
        PAUSING,
        CHECKING_SAFETY,
        BUILDING_BOX,
        PLACING_ENDER_CHEST,
        OPENING_ENDER_CHEST,
        REGEARING,
        CLOSING_CHEST,
        BREAKING_BOX,
        RESUMING
    }

    public enum RegearMode {
        EMERGENCY("Only when critical (20+ slots, 5% elytra)", 5, 20),
        CONSERVATIVE("Low supplies (15+ slots, 10% elytra)", 10, 15),
        BALANCED("Default balanced (10+ slots, 20% elytra)", 20, 10),
        AGGRESSIVE("Early regear (5+ slots, 40% elytra)", 40, 5),
        TOP_OFF("PvP top-off (1+ slot missing, 50% elytra)", 50, 1),
        CUSTOM("Custom thresholds", 20, 10);

        private final String description;
        private final int elytraDurability;
        private final int slotDifference;

        RegearMode(String description, int elytraDurability, int slotDifference) {
            this.description = description;
            this.elytraDurability = elytraDurability;
            this.slotDifference = slotDifference;
        }

        @Override
        public String toString() {
            return description;
        }
    }

    // Data class for inventory snapshot
    private static class InventorySlot {
        int slot;
        String itemId;
        int count;
        String customName; // For shulker boxes
    }
}
