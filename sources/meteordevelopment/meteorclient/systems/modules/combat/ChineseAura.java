package meteordevelopment.meteorclient.systems.modules.combat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.ItemListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1268;
import net.minecraft.class_1533;
import net.minecraft.class_1657;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_2399;
import net.minecraft.class_243;
import net.minecraft.class_2533;
import net.minecraft.class_2541;
import net.minecraft.class_2680;
import net.minecraft.class_2746;
import net.minecraft.class_2885;
import net.minecraft.class_3965;
import net.minecraft.class_5575;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/ChineseAura.class */
public class ChineseAura extends Module {
    private final SettingGroup sgGeneral;
    private final Set<class_1792> allowedFeetItems;
    private final Set<class_1792> allowedHeadItems;
    private final Setting<Boolean> pauseEat;
    private final Setting<Double> range;
    private final Setting<SortPriority> priority;
    private final Setting<List<class_1792>> feetItems;
    private final Setting<List<class_1792>> headItems;
    private final Setting<Double> placeDelay;
    private final Setting<Boolean> ignoreNakeds;
    private class_1657 targetPlayer;
    private final Map<class_2338, Long> timeOfLastPlace;
    private int tickCounter;

    public ChineseAura() {
        super(Categories.Combat, "chinese-aura", "Places whatever you want on your enemies. Extremely chinese.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.allowedFeetItems = new HashSet<class_1792>() { // from class: meteordevelopment.meteorclient.systems.modules.combat.ChineseAura.1
            {
                add(class_1802.field_8121);
                add(class_1802.field_17523);
                add(class_1802.field_8143);
                add(class_1802.field_8786);
                add(class_1802.field_16482);
            }
        };
        this.allowedHeadItems = new HashSet<class_1792>() { // from class: meteordevelopment.meteorclient.systems.modules.combat.ChineseAura.2
            {
                add(class_1802.field_8121);
                add(class_1802.field_17523);
                add(class_1802.field_8786);
                add(class_1802.field_8376);
                add(class_1802.field_8495);
                add(class_1802.field_8774);
                add(class_1802.field_8321);
                add(class_1802.field_8190);
                add(class_1802.field_8844);
                add(class_1802.field_37529);
                add(class_1802.field_42702);
                add(class_1802.field_40226);
                add(class_1802.field_22002);
                add(class_1802.field_22003);
            }
        };
        this.pauseEat = this.sgGeneral.add(new BoolSetting.Builder().name("pause-eat").description("Pauses while eating.").defaultValue(true).build());
        this.range = this.sgGeneral.add(new DoubleSetting.Builder().name("target-range").description("The maximum distance to target players.").defaultValue(5.0d).range(0.0d, 5.0d).sliderMax(5.0d).build());
        this.priority = this.sgGeneral.add(new EnumSetting.Builder().name("target-priority").description("How to filter targets within range.").defaultValue(SortPriority.ClosestAngle).build());
        this.feetItems = this.sgGeneral.add(new ItemListSetting.Builder().name("feet-items").description("Items to place on enemies feet").filter(x -> {
            return this.allowedFeetItems.contains(x);
        }).build());
        this.headItems = this.sgGeneral.add(new ItemListSetting.Builder().name("head-items").description("Items to place on enemies heads").filter(x2 -> {
            return this.allowedHeadItems.contains(x2);
        }).build());
        this.placeDelay = this.sgGeneral.add(new DoubleSetting.Builder().name("place-delay").description("How many seconds to wait between placing stuff again").defaultValue(0.2d).min(0.0d).sliderMax(2.0d).build());
        this.ignoreNakeds = this.sgGeneral.add(new BoolSetting.Builder().name("ignore-nakeds").description("Ignores players with no armor.").defaultValue(true).build());
        this.targetPlayer = null;
        this.timeOfLastPlace = new HashMap();
        this.tickCounter = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (TargetUtils.isBadTarget(this.targetPlayer, this.range.get().doubleValue())) {
            this.targetPlayer = TargetUtils.getPlayerTarget(this.range.get().doubleValue(), this.priority.get());
            if (TargetUtils.isBadTarget(this.targetPlayer, this.range.get().doubleValue())) {
                return;
            }
        }
        if (this.ignoreNakeds.get().booleanValue() && isNaked(this.targetPlayer)) {
            return;
        }
        if (this.pauseEat.get().booleanValue() && this.mc.field_1724.method_6115()) {
            return;
        }
        this.tickCounter++;
        FindItemResult headItemResult = InvUtils.find((Predicate<class_1799>) x -> {
            return this.headItems.get().contains(x.method_7909());
        });
        FindItemResult feetItemResult = InvUtils.find((Predicate<class_1799>) x2 -> {
            return this.feetItems.get().contains(x2.method_7909());
        });
        long currentTime = System.currentTimeMillis();
        if (headItemResult.found()) {
            class_1792 item = this.mc.field_1724.method_31548().method_5438(headItemResult.slot()).method_7909();
            class_2338 upperPos = this.targetPlayer.method_24515().method_10084();
            class_2338 lowerPos = this.targetPlayer.method_24515();
            boolean didPlace = false;
            if (MeteorClient.SWAP.beginSwap(headItemResult, true)) {
                if (item == class_1802.field_17523) {
                    placeVine(headItemResult, lowerPos);
                    didPlace = true;
                } else if (item == class_1802.field_8121) {
                    placeLadder(headItemResult, lowerPos);
                    didPlace = true;
                } else if (item == class_1802.field_8786) {
                    placeWeb(headItemResult, lowerPos);
                    didPlace = true;
                } else if (isTrapdoor(item)) {
                    didPlace = placeTrapdoor(headItemResult, upperPos, lowerPos, currentTime);
                }
                MeteorClient.SWAP.endSwap(true);
            }
            if (didPlace) {
                this.timeOfLastPlace.put(lowerPos, Long.valueOf(currentTime));
            }
        }
        if (feetItemResult.found()) {
            class_1792 item2 = this.mc.field_1724.method_31548().method_5438(feetItemResult.slot()).method_7909();
            boolean cooldownCheck = !this.timeOfLastPlace.containsKey(this.targetPlayer.method_24515()) || (((double) currentTime) - ((double) this.timeOfLastPlace.get(this.targetPlayer.method_24515()).longValue())) / 1000.0d > this.placeDelay.get().doubleValue();
            if (cooldownCheck) {
                boolean didPlace2 = true;
                if (MeteorClient.SWAP.beginSwap(feetItemResult, true)) {
                    if (item2 == class_1802.field_8143) {
                        placeItemFrame(feetItemResult);
                    } else if (item2 == class_1802.field_17523) {
                        placeVine(feetItemResult, this.targetPlayer.method_24515());
                    } else if (item2 == class_1802.field_8121) {
                        placeLadder(feetItemResult, this.targetPlayer.method_24515());
                    } else if (item2 == class_1802.field_8786) {
                        placeWeb(feetItemResult, this.targetPlayer.method_24515());
                    } else if (item2 == class_1802.field_16482) {
                        placeScaffold(feetItemResult, this.targetPlayer.method_24515());
                    } else {
                        didPlace2 = false;
                    }
                    MeteorClient.SWAP.endSwap(true);
                }
                if (didPlace2) {
                    this.timeOfLastPlace.put(this.targetPlayer.method_24515(), Long.valueOf(currentTime));
                }
            }
        }
    }

    private boolean isNaked(class_1657 player) {
        return ((class_1799) player.method_31548().meteor$getArmor().get(0)).method_7960() && ((class_1799) player.method_31548().meteor$getArmor().get(1)).method_7960() && ((class_1799) player.method_31548().meteor$getArmor().get(2)).method_7960() && ((class_1799) player.method_31548().meteor$getArmor().get(3)).method_7960();
    }

    private void placeItemFrame(FindItemResult itemResult) {
        if (this.mc.field_1687.method_22347(this.targetPlayer.method_24515())) {
            class_2338 blockPos = this.targetPlayer.method_24515().method_10074();
            class_2350 dir = class_2350.field_11036;
            class_238 boundingBox = this.targetPlayer.method_5829().method_1002(0.01d, 0.1d, 0.01d);
            double feetY = this.targetPlayer.method_23318();
            class_238 feetBox = new class_238(boundingBox.field_1323, feetY, boundingBox.field_1321, boundingBox.field_1320, feetY + 0.1d, boundingBox.field_1324);
            boolean inMultipleBlocks = class_2338.method_29715(feetBox).count() > 1;
            if (inMultipleBlocks) {
                return;
            }
            class_243 hitPos = blockPos.method_46558().method_1031(((double) dir.method_10148()) * 0.5d, ((double) dir.method_10164()) * 0.5d, ((double) dir.method_10165()) * 0.5d);
            List<class_1533> entities = this.mc.field_1687.method_18023(class_5575.method_31795(class_1533.class), class_238.method_30048(hitPos, 0.1d, 0.1d, 0.1d), entity -> {
                return true;
            });
            if (entities.isEmpty()) {
                this.mc.method_1562().method_52787(new class_2885(class_1268.field_5808, new class_3965(hitPos, dir, blockPos, false), getSequence()));
            }
        }
    }

    private void placeVine(FindItemResult itemResult, class_2338 pos) {
        if (this.mc.field_1687.method_22347(pos)) {
            for (class_2350 dir : class_2350.field_11041) {
                class_2338 supportPos = pos.method_10093(dir);
                class_2350 dir2 = dir.method_10153();
                if (canPlaceVine(pos, dir2)) {
                    class_243 hitPos = supportPos.method_46558().method_1031(((double) dir2.method_10148()) * 0.5d, 0.75d, ((double) dir2.method_10165()) * 0.5d);
                    this.mc.method_1562().method_52787(new class_2885(class_1268.field_5808, new class_3965(hitPos, dir2, supportPos, false), getSequence()));
                }
            }
        }
    }

    private void placeLadder(FindItemResult itemResult, class_2338 pos) {
        if (this.mc.field_1687.method_22347(pos)) {
            for (class_2350 dir : class_2350.field_11041) {
                class_2338 supportPos = pos.method_10093(dir);
                class_2350 dir2 = dir.method_10153();
                if (canPlaceLadder(pos, dir2)) {
                    class_243 hitPos = supportPos.method_46558().method_1031(((double) dir2.method_10148()) * 0.5d, 0.75d, ((double) dir2.method_10165()) * 0.5d);
                    this.mc.method_1562().method_52787(new class_2885(class_1268.field_5808, new class_3965(hitPos, dir2, supportPos, false), getSequence()));
                }
            }
        }
    }

    private void placeWeb(FindItemResult itemResult, class_2338 pos) {
        List<class_2338> placePoses = new ArrayList<>();
        placePoses.add(pos);
        if (MeteorClient.BLOCK.beginPlacement(placePoses, class_1802.field_8786)) {
            placePoses.forEach(blockPos -> {
                MeteorClient.BLOCK.placeBlock(class_1802.field_8786, blockPos);
            });
            MeteorClient.BLOCK.endPlacement();
        }
    }

    private void placeScaffold(FindItemResult itemResult, class_2338 pos) {
        List<class_2338> placePoses = new ArrayList<>();
        placePoses.add(pos);
        if (MeteorClient.BLOCK.beginPlacement(placePoses, class_1802.field_16482)) {
            placePoses.forEach(blockPos -> {
                MeteorClient.BLOCK.placeBlock(class_1802.field_16482, blockPos);
            });
            MeteorClient.BLOCK.endPlacement();
        }
    }

    private boolean placeTrapdoor(FindItemResult itemResult, class_2338 upperPos, class_2338 lowerPos, long currentTime) {
        class_2680 upperState = this.mc.field_1687.method_8320(upperPos);
        class_2338 supportPos = upperPos.method_10084();
        boolean didPlace = false;
        if (!this.mc.field_1687.method_8320(supportPos).method_26212(this.mc.field_1687, supportPos)) {
            return false;
        }
        boolean canPlace = !this.timeOfLastPlace.containsKey(upperPos) || (((double) currentTime) - ((double) this.timeOfLastPlace.get(upperPos).longValue())) / 1000.0d > this.placeDelay.get().doubleValue();
        if (canPlace) {
            if (!(upperState.method_26204() instanceof class_2533)) {
                class_243 hitPos = supportPos.method_46558().method_1031(0.0d, -0.5d, 0.0d);
                this.mc.method_1562().method_52787(new class_2885(class_1268.field_5808, new class_3965(hitPos, class_2350.field_11033, supportPos, false), getSequence()));
                didPlace = true;
            } else if (isPlayerCrawling() != ((Boolean) upperState.method_11654(class_2533.field_11631)).booleanValue()) {
                this.mc.method_1562().method_52787(new class_2885(class_1268.field_5808, new class_3965(upperPos.method_46558(), class_2350.field_11036, upperPos, false), getSequence()));
                didPlace = true;
            }
        }
        return didPlace;
    }

    private boolean isPlayerCrawling() {
        return this.targetPlayer != null && this.targetPlayer.method_5829().method_17940() < 1.5d;
    }

    private boolean isTrapdoor(class_1792 item) {
        return item == class_1802.field_8376 || item == class_1802.field_8495 || item == class_1802.field_8774 || item == class_1802.field_8321 || item == class_1802.field_8190 || item == class_1802.field_8844 || item == class_1802.field_37529 || item == class_1802.field_42702 || item == class_1802.field_40226 || item == class_1802.field_22002 || item == class_1802.field_22003;
    }

    private boolean canPlaceVine(class_2338 pos, class_2350 side) {
        class_2680 blockState = this.mc.field_1687.method_8320(pos);
        if (side == class_2350.field_11036 || side == class_2350.field_11033 || !class_2541.method_10821(this.mc.field_1687, pos.method_10093(side), side.method_10153())) {
            return false;
        }
        if (blockState.method_27852(class_2246.field_10597)) {
            class_2746 sideProperty = class_2541.method_10828(side);
            return !((Boolean) blockState.method_11654(sideProperty)).booleanValue();
        }
        return true;
    }

    public boolean canPlaceLadder(class_2338 pos, class_2350 side) {
        if (side == class_2350.field_11036 || side == class_2350.field_11033) {
            return false;
        }
        class_2680 blockState = this.mc.field_1687.method_8320(pos);
        class_2338 attachedPos = pos.method_10093(side.method_10153());
        class_2680 attachedState = this.mc.field_1687.method_8320(attachedPos);
        if (!class_2248.method_9501(attachedState.method_26220(this.mc.field_1687, attachedPos), side)) {
            return false;
        }
        if (!blockState.method_27852(class_2246.field_9983)) {
            return blockState.method_26215() || blockState.method_45474();
        }
        class_2350 existingDirection = (class_2350) blockState.method_11654(class_2399.field_11253);
        return existingDirection != side;
    }

    private int getSequence() {
        if (this.mc.field_1687 == null) {
            return 0;
        }
        return this.mc.field_1687.meteor$getAndIncrementSequence();
    }
}
