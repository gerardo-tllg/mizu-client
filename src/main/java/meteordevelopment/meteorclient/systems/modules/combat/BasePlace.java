package meteordevelopment.meteorclient.systems.modules.combat;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.managers.RotationManager;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.autocrystal.AutoCrystal;
import meteordevelopment.meteorclient.systems.modules.player.SilentMine;
import meteordevelopment.meteorclient.utils.entity.DamageUtils;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.*;

public class BasePlace extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgPlace = settings.createGroup("Place");
    private final SettingGroup sgDamage = settings.createGroup("Damage");
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<List<Block>> blocks = sgGeneral.add(
        new BlockListSetting.Builder().name("whitelist").description("Which blocks to use.")
            .defaultValue(Blocks.OBSIDIAN).build());

    private final Setting<Integer> targetRange =
        sgGeneral.add(new IntSetting.Builder().name("target-range")
            .description("The range players can be targeted.").defaultValue(4).build());

    private final Setting<Double> placeRange =
        sgGeneral.add(new DoubleSetting.Builder().name("place-range")
            .description("Maximum range for obsidian placement.")
            .defaultValue(4.5)
            .min(0)
            .sliderMax(6)
            .build());

    private final Setting<SortPriority> priority =
        sgGeneral.add(new EnumSetting.Builder<SortPriority>().name("target-priority")
            .description("How to select the player to target.")
            .defaultValue(SortPriority.LowestHealth).build());

    private final Setting<Boolean> pauseEat = sgGeneral.add(new BoolSetting.Builder()
        .name("pause-eat").description("Pauses while eating.").defaultValue(true).build());

    private final Setting<Boolean> smartPlace = sgGeneral.add(new BoolSetting.Builder()
        .name("smart-place")
        .description("Only places obsidian when necessary for crystal damage.")
        .defaultValue(true).build());

    private final Setting<Boolean> antiSurroundBreak = sgGeneral.add(new BoolSetting.Builder()
        .name("anti-surround-break")
        .description("Places obsidian to prevent surround breaking.")
        .defaultValue(true).build());

    private final Setting<Double> placeDelay = sgPlace.add(new DoubleSetting.Builder()
        .name("place-delay")
        .description("Delay between obsidian placements in seconds.")
        .defaultValue(0.1)
        .min(0)
        .sliderMax(1)
        .build());

    private final Setting<Integer> placesPerTick = sgPlace.add(new IntSetting.Builder()
        .name("places-per-tick")
        .description("Maximum obsidian placements per tick.")
        .defaultValue(1)
        .min(1)
        .max(10)
        .build());

    private final Setting<Double> minPlace = sgDamage.add(new DoubleSetting.Builder()
        .name("min-place-damage")
        .description("Minimum enemy damage to place obsidian.")
        .defaultValue(8)
        .min(0)
        .sliderRange(0, 20)
        .build());

    private final Setting<Double> maxSelfDamage = sgDamage.add(new DoubleSetting.Builder()
        .name("max-self-damage")
        .description("Maximum self damage from potential crystal placements.")
        .defaultValue(4)
        .min(0)
        .sliderRange(0, 20)
        .build());

    private final Setting<Double> minDamageImprovement = sgDamage.add(new DoubleSetting.Builder()
        .name("min-damage-improvement")
        .description("Minimum damage improvement to trigger obsidian placement.")
        .defaultValue(2)
        .min(0)
        .sliderRange(0, 10)
        .build());

    private final Setting<Boolean> render = sgRender.add(new BoolSetting.Builder().name("render")
        .description("Renders an overlay where blocks will be placed.").defaultValue(true)
        .build());

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode").description("How the shapes are rendered.")
        .defaultValue(ShapeMode.Both).build());

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("side-color").description("The side color of the target block rendering.")
        .defaultValue(new SettingColor(197, 137, 232, 10)).build());

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("line-color").description("The line color of the target block rendering.")
        .defaultValue(new SettingColor(197, 137, 232)).build());

    private PlayerEntity target;
    private BlockPos bestPos;
    private Map<BlockPos, Double> possiblePlacements = new HashMap<>();
    private long lastPlacedTime;
    private double maxCurrentCrystalDamage = 0.0;
    private int placeCooldown = 0;

    public BasePlace() {
        super(Categories.Combat, "base-place",
            "Places blocks next to enemies to allow for crystal placement.");
    }

    @Override
    public void onActivate() {
        target = null;
        bestPos = null;
        possiblePlacements.clear();
        lastPlacedTime = 0;
        maxCurrentCrystalDamage = 0.0;
        placeCooldown = 0;
    }

    @Override
    public void onDeactivate() {
        possiblePlacements.clear();
    }

    @EventHandler
    private void onBlockUpdate(PacketEvent.Receive event) {
        if (event.packet instanceof BlockUpdateS2CPacket packet) {
            if (!packet.getState().isAir()) return;

            BlockPos pos = packet.getPos();
            if (possiblePlacements.containsKey(pos)) {
                possiblePlacements.remove(pos);
            }

            if (target != null) {
                BlockPos targetPos = target.getBlockPos();
                if (pos.equals(targetPos.north()) || pos.equals(targetPos.south()) ||
                    pos.equals(targetPos.east()) || pos.equals(targetPos.west())) {

                    maxCurrentCrystalDamage = 0.0;
                }
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (placeCooldown > 0) {
            placeCooldown--;
            return;
        }

        if (target == null || TargetUtils.isBadTarget(target, targetRange.get())) {
            target = TargetUtils.getPlayerTarget(targetRange.get(), priority.get());
            if (TargetUtils.isBadTarget(target, targetRange.get())) {
                bestPos = null;
                return;
            }
        }

        updateCurrentMaxDamage();

        calculateOptimalPlacements();
        bestPos = findBestPlacement();
        if (bestPos == null) return;

        if (smartPlace.get() && maxCurrentCrystalDamage >= minPlace.get()) {
            double bestPosDamage = possiblePlacements.getOrDefault(bestPos, 0.0);
            if (bestPosDamage - maxCurrentCrystalDamage < minDamageImprovement.get()) {
                return;
            }
        }

        if (System.currentTimeMillis() - lastPlacedTime < placeDelay.get() * 1000) return;

        Item useItem = findUseItem();
        if (useItem == null) return;

        if (pauseEat.get() && mc.player.isUsingItem()) return;

        SilentMine silentMine = Modules.get().get(SilentMine.class);
        if (silentMine != null && silentMine.isActive() &&
            ((silentMine.getDelayedDestroyBlockPos() != null && bestPos.equals(silentMine.getDelayedDestroyBlockPos())) ||
                (silentMine.getRebreakBlockPos() != null && bestPos.equals(silentMine.getRebreakBlockPos())))) {
            return;
        }

        Vec3d centerPos = bestPos.toCenterPos();
        Box boundingBox = new Box(centerPos.subtract(0.5, 0.5, 0.5), centerPos.add(0.5, 0.5, 0.5));

        MeteorClient.ROTATION.requestRotation(centerPos, 11);

        if (!MeteorClient.ROTATION.lookingAt(boundingBox) && RotationManager.lastGround) {
            MeteorClient.ROTATION.snapAt(centerPos);
        }

        if (MeteorClient.ROTATION.lookingAt(boundingBox)) {
            if (!MeteorClient.BLOCK.beginPlacement(bestPos, mc.world.getBlockState(bestPos), useItem)) return;
            MeteorClient.BLOCK.placeBlock(useItem, bestPos, mc.world.getBlockState(bestPos));
            MeteorClient.BLOCK.endPlacement();
            lastPlacedTime = System.currentTimeMillis();
            placeCooldown = 1;
        }
    }

    private void updateCurrentMaxDamage() {
        maxCurrentCrystalDamage = 0.0;
        if (target == null || mc.world == null) return;

        BlockPos targetPos = target.getBlockPos();
        BlockPos playerPos = mc.player.getBlockPos();
        Set<BlockPos> validCrystalPositions = new HashSet<>();
        AutoCrystal autoCrystal = Modules.get().get(AutoCrystal.class);
        double crystalPlaceRange = autoCrystal != null ? autoCrystal.placeRange.get() : 4.0;

        int scanRange = 8;

        for (int x = -scanRange; x <= scanRange; x++) {
            for (int y = -scanRange/2; y <= scanRange/2; y++) {
                for (int z = -scanRange; z <= scanRange; z++) {
                    BlockPos checkPos = playerPos.add(x, y, z);

                    if (mc.player.getEyePos().distanceTo(checkPos.up().toCenterPos()) > crystalPlaceRange) continue;

                    Block block = mc.world.getBlockState(checkPos).getBlock();

                    if ((block == Blocks.OBSIDIAN || block == Blocks.BEDROCK) &&
                        mc.world.getBlockState(checkPos.up()).isAir() &&
                        mc.world.getBlockState(checkPos.up(2)).isAir()) {

                        validCrystalPositions.add(checkPos);
                    }
                }
            }
        }

        for (BlockPos pos : validCrystalPositions) {
            Vec3d crystalPos = new Vec3d(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
            double targetDamage = DamageUtils.crystalDamage(target, crystalPos);
            double selfDamage = DamageUtils.crystalDamage(mc.player, crystalPos);

            if (selfDamage <= maxSelfDamage.get() && targetDamage > maxCurrentCrystalDamage) {
                maxCurrentCrystalDamage = targetDamage;
            }
        }
    }

    private void calculateOptimalPlacements() {
        possiblePlacements.clear();
        if (target == null) return;

        BlockPos targetPos = target.getBlockPos();
        BlockPos playerPos = mc.player.getBlockPos();
        Set<BlockPos> candidatePositions = new HashSet<>();
        AutoCrystal autoCrystal = Modules.get().get(AutoCrystal.class);
        double crystalPlaceRange = autoCrystal != null ? autoCrystal.placeRange.get() : 4.0;

        int horizontalRange = (int) Math.ceil(placeRange.get());
        int verticalRange = 3;

        for (int x = -horizontalRange; x <= horizontalRange; x++) {
            for (int y = -verticalRange; y <= verticalRange; y++) {
                for (int z = -horizontalRange; z <= horizontalRange; z++) {
                    BlockPos checkPos = playerPos.add(x, y, z);

                    if (mc.player.getEyePos().distanceTo(checkPos.toCenterPos()) > placeRange.get()) continue;

                    if (!mc.world.getBlockState(checkPos).isAir()) continue;

                    if (!mc.world.getBlockState(checkPos.up()).isAir()) continue;

                    double crystalDistToPlayer = mc.player.getEyePos().distanceTo(checkPos.up().toCenterPos());
                    if (crystalDistToPlayer > crystalPlaceRange) continue;

                    candidatePositions.add(checkPos);
                }
            }
        }

        for (Direction direction : Direction.values()) {
            if (direction == Direction.UP || direction == Direction.DOWN) continue;

            BlockPos adjacentPos = targetPos.offset(direction);
            if (mc.world.getBlockState(adjacentPos).isAir() &&
                mc.world.getBlockState(adjacentPos.up()).isAir() &&
                mc.player.getEyePos().distanceTo(adjacentPos.toCenterPos()) <= placeRange.get()) {
                candidatePositions.add(adjacentPos);
            }
        }

        BlockPos[] diagonals = {
            targetPos.add(1, 0, 1), targetPos.add(1, 0, -1),
            targetPos.add(-1, 0, 1), targetPos.add(-1, 0, -1)
        };

        for (BlockPos pos : diagonals) {
            if (mc.world.getBlockState(pos).isAir() &&
                mc.world.getBlockState(pos.up()).isAir() &&
                mc.player.getEyePos().distanceTo(pos.toCenterPos()) <= placeRange.get()) {
                candidatePositions.add(pos);
            }
        }

        for (Direction direction : Direction.values()) {
            if (direction == Direction.UP || direction == Direction.DOWN) continue;

            BlockPos belowAdjacent = targetPos.down().offset(direction);
            if (mc.world.getBlockState(belowAdjacent).isAir() &&
                mc.world.getBlockState(belowAdjacent.up()).isAir() &&
                mc.world.getBlockState(belowAdjacent.up(2)).isAir() &&
                mc.player.getEyePos().distanceTo(belowAdjacent.toCenterPos()) <= placeRange.get()) {
                candidatePositions.add(belowAdjacent);
            }
        }

        for (BlockPos pos : candidatePositions) {
            if (EntityUtils.intersectsWithEntity(new Box(pos), entity ->
                !entity.isSpectator() && entity.getBoundingBox().intersects(new Box(pos)))) {
                continue;
            }

            Vec3d crystalPos = new Vec3d(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
            double targetDamage = DamageUtils.crystalDamage(target, crystalPos);
            double selfDamage = DamageUtils.crystalDamage(mc.player, crystalPos);

            boolean isValidCrystalPos = true;
            if (autoCrystal != null) {
                double crystalDistToPlayer = mc.player.getEyePos().distanceTo(crystalPos);
                if (crystalDistToPlayer > autoCrystal.placeRange.get()) {
                    isValidCrystalPos = false;
                }
            }

            if (isValidCrystalPos && selfDamage <= maxSelfDamage.get() && targetDamage >= minPlace.get()) {
                possiblePlacements.put(pos, targetDamage);
            }
        }

        if (antiSurroundBreak.get()) {
            for (Direction direction : Direction.values()) {
                if (direction == Direction.UP || direction == Direction.DOWN) continue;

                BlockPos surroundPos = targetPos.offset(direction);
                Block surroundBlock = mc.world.getBlockState(surroundPos).getBlock();

                if (surroundBlock == Blocks.OBSIDIAN) {
                    SilentMine silentMine = Modules.get().get(SilentMine.class);
                    boolean isBeingMined = (silentMine != null && silentMine.isActive() &&
                        (surroundPos.equals(silentMine.getDelayedDestroyBlockPos()) ||
                            surroundPos.equals(silentMine.getRebreakBlockPos())));

                    if (isBeingMined) {

                        for (Direction dir2 : Direction.values()) {
                            if (dir2 == Direction.UP || dir2 == Direction.DOWN || dir2 == direction.getOpposite()) continue;

                            BlockPos supportPos = surroundPos.offset(dir2);
                            if (mc.world.getBlockState(supportPos).isAir() &&
                                mc.world.getBlockState(supportPos.up()).isAir() &&
                                !EntityUtils.intersectsWithEntity(new Box(supportPos), entity ->
                                    !entity.isSpectator() && entity.getBoundingBox().intersects(new Box(supportPos)))) {

                                Vec3d crystalPos = new Vec3d(supportPos.getX() + 0.5, supportPos.getY() + 1, supportPos.getZ() + 0.5);
                                double targetDamage = DamageUtils.crystalDamage(target, crystalPos);
                                double selfDamage = DamageUtils.crystalDamage(mc.player, crystalPos);

                                if (selfDamage <= maxSelfDamage.get()) {

                                    possiblePlacements.put(supportPos, Math.max(targetDamage, minPlace.get()));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private BlockPos findBestPlacement() {
        if (possiblePlacements.isEmpty()) return null;

        BlockPos bestPos = null;
        double bestDamage = 0.0;

        for (Map.Entry<BlockPos, Double> entry : possiblePlacements.entrySet()) {
            if (entry.getValue() > bestDamage) {
                BlockPos pos = entry.getKey();

                if (mc.world.getBlockState(pos).isAir() &&
                    !EntityUtils.intersectsWithEntity(new Box(pos), entity ->
                        !entity.isSpectator() && entity.getBoundingBox().intersects(new Box(pos)))) {
                    bestDamage = entry.getValue();
                    bestPos = pos;
                }
            }
        }

        return bestPos;
    }

    private Item findUseItem() {
        FindItemResult result = InvUtils.findInHotbar(itemStack -> {
            for (Block block : blocks.get()) {
                if (block.asItem() == itemStack.getItem()) {
                    return true;
                }
            }
            return false;
        });

        if (!result.found()) return null;
        return mc.player.getInventory().getStack(result.slot()).getItem();
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (!render.get() || bestPos == null) return;
        event.renderer.box(bestPos, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
    }

    @Override
    public String getInfoString() {
        return EntityUtils.getName(target);
    }
}
