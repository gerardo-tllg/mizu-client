package meteordevelopment.meteorclient.systems.modules.hunting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.pathing.BaritoneUtils;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1707;
import net.minecraft.class_1713;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2336;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_3965;
import net.minecraft.class_7923;
import net.minecraft.class_9334;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/hunting/AutoRegear.class */
public class AutoRegear extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgTriggers;
    private final SettingGroup sgSafety;
    private final Setting<Integer> tickDelay;
    private final Setting<Boolean> pauseOnRegear;
    private final Setting<Boolean> resumeAfterRegear;
    private final Setting<Boolean> autoTrigger;
    private final Setting<RegearMode> regearMode;
    private final Setting<Integer> triggerThreshold;
    private final Setting<Integer> minElytraDurability;
    private final Setting<Boolean> buildObsidianBox;
    private final Setting<Boolean> breakBoxAfter;
    private final Setting<Boolean> checkSafeSpot;
    private final Setting<Integer> safeRadius;
    private RegearState state;
    private int tickTimer;
    private class_2338 regearPos;
    private boolean wasBaritoneActive;
    private final Map<Integer, InventorySlot> inventorySnapshot;
    private final List<class_2338> obsidianBlocks;
    private class_2338 enderChestPos;
    private int regearAttempts;
    private static final int MAX_REGEAR_ATTEMPTS = 3;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/hunting/AutoRegear$RegearState.class */
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

    private File getSnapshotFile() {
        return new File(MeteorClient.FOLDER, "autoregear-snapshot.json");
    }

    public AutoRegear() {
        super(Categories.Hunting, "auto-regear", "Automatically regears from ender chest to match saved inventory snapshot. Works for AFK hunting and PvP.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgTriggers = this.settings.createGroup("Triggers");
        this.sgSafety = this.settings.createGroup("Safety");
        this.tickDelay = this.sgGeneral.add(new IntSetting.Builder().name("tick-delay").description("Delay in ticks between actions.").defaultValue(4).min(1).sliderMax(20).build());
        this.pauseOnRegear = this.sgGeneral.add(new BoolSetting.Builder().name("pause-baritone").description("Automatically pause Baritone when regearing.").defaultValue(true).build());
        this.resumeAfterRegear = this.sgGeneral.add(new BoolSetting.Builder().name("resume-baritone").description("Automatically resume Baritone after regearing.").defaultValue(true).build());
        this.autoTrigger = this.sgTriggers.add(new BoolSetting.Builder().name("auto-trigger").description("Automatically start regearing when inventory differs from snapshot.").defaultValue(true).build());
        this.regearMode = this.sgTriggers.add(new EnumSetting.Builder().name("regear-mode").description("How aggressive to regear. Change anytime!").defaultValue(RegearMode.BALANCED).build());
        this.triggerThreshold = this.sgTriggers.add(new IntSetting.Builder().name("difference-threshold").description("Number of slot differences needed to trigger regear. Override with regear-mode.").defaultValue(10).min(1).sliderMax(36).visible(() -> {
            return this.regearMode.get() == RegearMode.CUSTOM;
        }).build());
        this.minElytraDurability = this.sgTriggers.add(new IntSetting.Builder().name("min-elytra-durability").description("Trigger regear if elytra durability % drops below this. Override with regear-mode.").defaultValue(20).min(1).max(100).sliderMax(100).visible(() -> {
            return this.regearMode.get() == RegearMode.CUSTOM;
        }).build());
        this.buildObsidianBox = this.sgSafety.add(new BoolSetting.Builder().name("build-obsidian-box").description("Build obsidian box for safety during regear.").defaultValue(true).build());
        this.breakBoxAfter = this.sgSafety.add(new BoolSetting.Builder().name("break-box-after").description("Break obsidian box after regearing.").defaultValue(true).build());
        this.checkSafeSpot = this.sgSafety.add(new BoolSetting.Builder().name("check-safe-spot").description("Verify no players nearby before regearing.").defaultValue(true).build());
        this.safeRadius = this.sgSafety.add(new IntSetting.Builder().name("safe-radius").description("Radius to check for players.").defaultValue(32).min(8).sliderMax(64).build());
        this.state = RegearState.IDLE;
        this.tickTimer = 0;
        this.regearPos = null;
        this.wasBaritoneActive = false;
        this.inventorySnapshot = new HashMap();
        this.obsidianBlocks = new ArrayList();
        this.enderChestPos = null;
        this.regearAttempts = 0;
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        this.state = RegearState.IDLE;
        this.tickTimer = 0;
        this.regearPos = null;
        this.obsidianBlocks.clear();
        this.enderChestPos = null;
        this.regearAttempts = 0;
        loadSnapshotFromFile();
        if (this.inventorySnapshot.isEmpty()) {
            warning("No inventory snapshot saved! Use chat command: .autoregear save", new Object[0]);
        } else {
            info("Loaded snapshot with " + this.inventorySnapshot.size() + " slots", new Object[0]);
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) {
            return;
        }
        this.tickTimer++;
        if (this.tickTimer < this.tickDelay.get().intValue()) {
        }
        this.tickTimer = 0;
        switch (this.state) {
            case IDLE:
                if (this.autoTrigger.get().booleanValue() && shouldRegear()) {
                    info("Regear triggered - inventory differs from snapshot", new Object[0]);
                    startRegear();
                    break;
                }
                break;
            case PAUSING:
                pauseModules();
                break;
            case CHECKING_SAFETY:
                checkSafety();
                break;
            case BUILDING_BOX:
                buildObsidianBox();
                break;
            case PLACING_ENDER_CHEST:
                placeEnderChest();
                break;
            case OPENING_ENDER_CHEST:
                openEnderChest();
                break;
            case REGEARING:
                performRegear();
                break;
            case CLOSING_CHEST:
                closeChest();
                break;
            case BREAKING_BOX:
                breakObsidianBox();
                break;
            case RESUMING:
                resumeModules();
                break;
        }
    }

    public void saveSnapshot() {
        if (this.mc.field_1724 == null) {
            return;
        }
        this.inventorySnapshot.clear();
        for (int slot = 0; slot < 36; slot++) {
            class_1799 stack = this.mc.field_1724.method_31548().method_5438(slot);
            if (!stack.method_7960()) {
                InventorySlot slotData = new InventorySlot();
                slotData.slot = slot;
                slotData.itemId = class_7923.field_41178.method_10221(stack.method_7909()).toString();
                slotData.count = stack.method_7947();
                slotData.customName = stack.method_57826(class_9334.field_49631) ? stack.method_7964().getString() : null;
                this.inventorySnapshot.put(Integer.valueOf(slot), slotData);
            }
        }
        saveSnapshotToFile();
        info("Saved snapshot: " + this.inventorySnapshot.size() + " slots", new Object[0]);
    }

    public void clearSnapshot() {
        this.inventorySnapshot.clear();
        saveSnapshotToFile();
        info("Snapshot cleared", new Object[0]);
    }

    public int getSnapshotSize() {
        return this.inventorySnapshot.size();
    }

    public void forceRegear() {
        if (this.state != RegearState.IDLE) {
            warning("Already regearing, please wait", new Object[0]);
        } else if (this.inventorySnapshot.isEmpty()) {
            error("No snapshot saved - use .ar save first", new Object[0]);
        } else {
            info("Manual regear triggered", new Object[0]);
            startRegear();
        }
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: java.lang.MatchException */
    private boolean shouldRegear() throws MatchException {
        if (this.inventorySnapshot.isEmpty()) {
            return false;
        }
        int durabilityThreshold = getElytraDurabilityThreshold();
        int slotThreshold = getSlotDifferenceThreshold();
        class_1799 chestplate = this.mc.field_1724.method_31548().method_5438(38);
        if (chestplate.method_7909() == class_1802.field_8833) {
            int maxDur = chestplate.method_7936();
            int currentDur = maxDur - chestplate.method_7919();
            double percent = (((double) currentDur) / ((double) maxDur)) * 100.0d;
            if (percent < durabilityThreshold) {
                return true;
            }
        }
        int differences = 0;
        for (int slot = 0; slot < 36; slot++) {
            class_1799 current = this.mc.field_1724.method_31548().method_5438(slot);
            InventorySlot snapshotSlot = this.inventorySnapshot.get(Integer.valueOf(slot));
            if (!itemStacksMatch(current, snapshotSlot)) {
                differences++;
            }
        }
        return differences >= slotThreshold;
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: java.lang.MatchException */
    private int getElytraDurabilityThreshold() throws MatchException {
        switch (this.regearMode.get()) {
            case EMERGENCY:
                return 5;
            case CONSERVATIVE:
                return 10;
            case BALANCED:
                return 20;
            case AGGRESSIVE:
                return 40;
            case TOP_OFF:
                return 50;
            case CUSTOM:
                return this.minElytraDurability.get().intValue();
            default:
                throw new MatchException((String) null, (Throwable) null);
        }
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: java.lang.MatchException */
    private int getSlotDifferenceThreshold() throws MatchException {
        switch (this.regearMode.get()) {
            case EMERGENCY:
                return 20;
            case CONSERVATIVE:
                return 15;
            case BALANCED:
                return 10;
            case AGGRESSIVE:
                return 5;
            case TOP_OFF:
                return 1;
            case CUSTOM:
                return this.triggerThreshold.get().intValue();
            default:
                throw new MatchException((String) null, (Throwable) null);
        }
    }

    private boolean itemStacksMatch(class_1799 current, InventorySlot snapshot) {
        if (snapshot == null) {
            return current.method_7960();
        }
        if (current.method_7960() || !class_7923.field_41178.method_10221(current.method_7909()).toString().equals(snapshot.itemId) || Math.abs(current.method_7947() - snapshot.count) > 5) {
            return false;
        }
        return true;
    }

    private void saveSnapshotToFile() {
        try {
            File file = getSnapshotFile();
            file.getParentFile().mkdirs();
            Writer writer = new FileWriter(file);
            try {
                GSON.toJson(new ArrayList(this.inventorySnapshot.values()), writer);
                writer.close();
            } finally {
            }
        } catch (IOException e) {
            error("Failed to save snapshot: " + e.getMessage(), new Object[0]);
        }
    }

    private void loadSnapshotFromFile() {
        File file = getSnapshotFile();
        if (file.exists()) {
            try {
                Reader reader = new FileReader(file);
                try {
                    InventorySlot[] slots = (InventorySlot[]) GSON.fromJson(reader, InventorySlot[].class);
                    if (slots != null) {
                        this.inventorySnapshot.clear();
                        for (InventorySlot slot : slots) {
                            this.inventorySnapshot.put(Integer.valueOf(slot.slot), slot);
                        }
                    }
                    reader.close();
                } finally {
                }
            } catch (IOException e) {
                error("Failed to load snapshot: " + e.getMessage(), new Object[0]);
            }
        }
    }

    private void startRegear() {
        if (this.regearAttempts >= 3) {
            error("Max regear attempts reached, resetting", new Object[0]);
            this.state = RegearState.IDLE;
            this.regearAttempts = 0;
        } else {
            this.state = RegearState.PAUSING;
            this.regearPos = this.mc.field_1724.method_24515();
            this.obsidianBlocks.clear();
            this.regearAttempts++;
        }
    }

    private void pauseModules() {
        if (this.pauseOnRegear.get().booleanValue() && BaritoneUtils.IS_AVAILABLE) {
            try {
                Class<?> baritoneAPI = Class.forName("baritone.api.BaritoneAPI");
                Object provider = baritoneAPI.getMethod("getProvider", new Class[0]).invoke(null, new Object[0]);
                Object baritone = provider.getClass().getMethod("getPrimaryBaritone", new Class[0]).invoke(provider, new Object[0]);
                Object pathingBehavior = baritone.getClass().getMethod("getPathingBehavior", new Class[0]).invoke(baritone, new Object[0]);
                this.wasBaritoneActive = ((Boolean) pathingBehavior.getClass().getMethod("isPathing", new Class[0]).invoke(pathingBehavior, new Object[0])).booleanValue();
                if (this.wasBaritoneActive) {
                    Object commandManager = baritone.getClass().getMethod("getCommandManager", new Class[0]).invoke(baritone, new Object[0]);
                    commandManager.getClass().getMethod("execute", String.class).invoke(commandManager, "pause");
                    info("Paused Baritone", new Object[0]);
                }
            } catch (Exception e) {
                warning("Failed to pause Baritone: " + e.getMessage(), new Object[0]);
            }
        }
        this.state = RegearState.CHECKING_SAFETY;
    }

    private void checkSafety() {
        if (this.checkSafeSpot.get().booleanValue() && !isSafeSpot()) {
            warning("Players nearby - waiting for safety", new Object[0]);
        } else {
            this.state = RegearState.BUILDING_BOX;
        }
    }

    private boolean isSafeSpot() {
        for (class_1657 player : this.mc.field_1687.method_18456()) {
            if (player != this.mc.field_1724 && !Friends.get().isFriend(player) && PlayerUtils.isWithin((class_1297) player, this.safeRadius.get().intValue())) {
                return false;
            }
        }
        return true;
    }

    private void buildObsidianBox() {
        if (!this.buildObsidianBox.get().booleanValue()) {
            this.state = RegearState.PLACING_ENDER_CHEST;
            return;
        }
        FindItemResult obsidian = InvUtils.find(class_1802.field_8281);
        if (!obsidian.found()) {
            error("No obsidian found - skipping safety box", new Object[0]);
            this.state = RegearState.PLACING_ENDER_CHEST;
            return;
        }
        class_2338 playerPos = this.mc.field_1724.method_24515();
        List<class_2338> boxPositions = getBoxPositions(playerPos);
        for (class_2338 pos : boxPositions) {
            if (this.mc.field_1687.method_8320(pos).method_45474() && BlockUtils.place(pos, obsidian, false, 50, true)) {
                this.obsidianBlocks.add(pos);
                return;
            }
        }
        info("Obsidian box complete", new Object[0]);
        this.state = RegearState.PLACING_ENDER_CHEST;
    }

    private List<class_2338> getBoxPositions(class_2338 center) {
        List<class_2338> positions = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                positions.add(center.method_10069(x, -1, z));
                positions.add(center.method_10069(x, 2, z));
            }
        }
        for (int y = 0; y <= 1; y++) {
            positions.add(center.method_10069(-1, y, -1));
            positions.add(center.method_10069(-1, y, 0));
            positions.add(center.method_10069(-1, y, 1));
            positions.add(center.method_10069(1, y, -1));
            positions.add(center.method_10069(1, y, 0));
            positions.add(center.method_10069(1, y, 1));
            positions.add(center.method_10069(0, y, -1));
            positions.add(center.method_10069(0, y, 1));
        }
        return positions;
    }

    private void placeEnderChest() {
        FindItemResult enderChest = InvUtils.find(class_1802.field_8466);
        if (!enderChest.found()) {
            error("No ender chest found - cannot regear", new Object[0]);
            this.state = RegearState.BREAKING_BOX;
            return;
        }
        class_2338 placePos = this.mc.field_1724.method_24515().method_10069(0, 0, 1);
        if (this.mc.field_1687.method_8320(placePos).method_45474()) {
            if (BlockUtils.place(placePos, enderChest, false, 50, true)) {
                this.enderChestPos = placePos;
                info("Placed ender chest", new Object[0]);
                this.state = RegearState.OPENING_ENDER_CHEST;
                this.tickTimer = -10;
                return;
            }
            return;
        }
        if (this.mc.field_1687.method_8320(placePos).method_26204() instanceof class_2336) {
            this.enderChestPos = placePos;
            this.state = RegearState.OPENING_ENDER_CHEST;
        } else {
            error("Cannot place ender chest", new Object[0]);
            this.state = RegearState.BREAKING_BOX;
        }
    }

    private void openEnderChest() {
        if (this.enderChestPos == null) {
            error("Ender chest position lost", new Object[0]);
            this.state = RegearState.BREAKING_BOX;
        } else {
            if (this.mc.field_1687.method_8320(this.enderChestPos).method_26204() instanceof class_2336) {
                class_3965 hitResult = new class_3965(class_243.method_24953(this.enderChestPos), class_2350.field_11036, this.enderChestPos, false);
                BlockUtils.interact(hitResult, class_1268.field_5808, false);
                this.tickTimer = -15;
                this.state = RegearState.REGEARING;
                info("Opening ender chest", new Object[0]);
                return;
            }
            error("Ender chest not found at expected position", new Object[0]);
            this.state = RegearState.BREAKING_BOX;
        }
    }

    private void performRegear() {
        if (!(this.mc.field_1724.field_7512 instanceof class_1707)) {
            return;
        }
        class_1707 handler = this.mc.field_1724.field_7512;
        for (InventorySlot targetSlot : this.inventorySnapshot.values()) {
            class_1799 current = this.mc.field_1724.method_31548().method_5438(targetSlot.slot);
            if (!itemStacksMatch(current, targetSlot)) {
                for (int i = 0; i < handler.method_17388() * 9; i++) {
                    class_1799 chestStack = handler.method_7611(i).method_7677();
                    if (!chestStack.method_7960()) {
                        String chestItemId = class_7923.field_41178.method_10221(chestStack.method_7909()).toString();
                        if (!chestItemId.equals(targetSlot.itemId)) {
                            continue;
                        } else if (targetSlot.customName != null && chestStack.method_57826(class_9334.field_49631)) {
                            String chestName = chestStack.method_7964().getString();
                            if (chestName.contains(targetSlot.customName)) {
                                this.mc.field_1761.method_2906(handler.field_7763, i, 0, class_1713.field_7794, this.mc.field_1724);
                                info("Took shulker: " + chestName, new Object[0]);
                                return;
                            }
                        } else {
                            this.mc.field_1761.method_2906(handler.field_7763, i, 0, class_1713.field_7794, this.mc.field_1724);
                            return;
                        }
                    }
                }
            }
        }
        info("Regear complete", new Object[0]);
        this.state = RegearState.CLOSING_CHEST;
    }

    private void closeChest() {
        if (this.mc.field_1724.field_7512 instanceof class_1707) {
            this.mc.field_1724.method_7346();
        }
        this.state = RegearState.BREAKING_BOX;
        this.tickTimer = -5;
    }

    private void breakObsidianBox() {
        if (!this.breakBoxAfter.get().booleanValue() || !this.buildObsidianBox.get().booleanValue()) {
            this.state = RegearState.RESUMING;
            return;
        }
        if (!this.obsidianBlocks.isEmpty()) {
            class_2338 toBreak = this.obsidianBlocks.remove(0);
            if (!this.mc.field_1687.method_8320(toBreak).method_26215()) {
                BlockUtils.breakBlock(toBreak, false);
                return;
            }
        }
        if (this.enderChestPos != null && (this.mc.field_1687.method_8320(this.enderChestPos).method_26204() instanceof class_2336)) {
            BlockUtils.breakBlock(this.enderChestPos, false);
            this.enderChestPos = null;
        } else {
            info("Box cleanup complete", new Object[0]);
            this.state = RegearState.RESUMING;
        }
    }

    private void resumeModules() {
        if (this.resumeAfterRegear.get().booleanValue() && this.wasBaritoneActive && BaritoneUtils.IS_AVAILABLE) {
            try {
                Class<?> baritoneAPI = Class.forName("baritone.api.BaritoneAPI");
                Object provider = baritoneAPI.getMethod("getProvider", new Class[0]).invoke(null, new Object[0]);
                Object baritone = provider.getClass().getMethod("getPrimaryBaritone", new Class[0]).invoke(provider, new Object[0]);
                Object commandManager = baritone.getClass().getMethod("getCommandManager", new Class[0]).invoke(baritone, new Object[0]);
                commandManager.getClass().getMethod("execute", String.class).invoke(commandManager, "resume");
                info("Resumed Baritone", new Object[0]);
            } catch (Exception e) {
                warning("Failed to resume Baritone: " + e.getMessage(), new Object[0]);
            }
        }
        info("Regear cycle complete (attempt " + this.regearAttempts + "/3)", new Object[0]);
        this.state = RegearState.IDLE;
        this.wasBaritoneActive = false;
        if (this.regearAttempts > 0) {
            this.regearAttempts = 0;
        }
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public String getInfoString() {
        if (!this.inventorySnapshot.isEmpty()) {
            return this.state.toString() + " (" + this.inventorySnapshot.size() + " slots)";
        }
        return "No snapshot";
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/hunting/AutoRegear$RegearMode.class */
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

        @Override // java.lang.Enum
        public String toString() {
            return this.description;
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/hunting/AutoRegear$InventorySlot.class */
    private static class InventorySlot {
        int slot;
        String itemId;
        int count;
        String customName;

        private InventorySlot() {
        }
    }
}
