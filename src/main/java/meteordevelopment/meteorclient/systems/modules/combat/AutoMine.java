package meteordevelopment.meteorclient.systems.modules.combat;

import meteordevelopment.meteorclient.events.meteor.SilentMineFinishedEvent;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.autocrystal.AutoCrystal;
import meteordevelopment.meteorclient.systems.modules.player.SilentMine;
import meteordevelopment.meteorclient.systems.modules.render.BreakIndicators;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;

import java.util.HashSet;
import java.util.Set;

public class AutoMine extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final double INVALID_SCORE = -1000;

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder().name("range")
        .description("Max range to target").defaultValue(6.5).min(0).sliderMax(7.0).build());

    private final Setting<Boolean> repositionIfNotAdjacent = sgGeneral.add(new BoolSetting.Builder()
        .name("reposition-if-not-adjacent")
        .description("Cancels mining and repositions if the target block isn't adjacent to or touching the target player.")
        .defaultValue(true)
        .build());

    private final Setting<SortPriority> targetPriority = sgGeneral
        .add(new EnumSetting.Builder<SortPriority>().name("target-priority")
            .description("How to choose the target").defaultValue(SortPriority.ClosestAngle)
            .build());

    private final Setting<Boolean> ignoreNakeds = sgGeneral.add(new BoolSetting.Builder().name("ignore-nakeds")
        .description("Ignore players with no items.").defaultValue(true).build());

    private final Setting<ExtendBreakMode> extendBreakMode = sgGeneral
        .add(new EnumSetting.Builder<ExtendBreakMode>().name("extend-break-mode")
            .description("How to mine outside of their surround to place crystals better")
            .defaultValue(ExtendBreakMode.None).build());

    private final Setting<AntiSwimMode> antiSwim = sgGeneral
        .add(new EnumSetting.Builder<AntiSwimMode>().name("anti-swim-mode")
            .description("Starts mining your head block when the enemy starts mining your feet")
            .defaultValue(AntiSwimMode.OnMineAndSwim).build());

    private final Setting<AntiSurroundMode> antiSurroundMode = sgGeneral
        .add(new EnumSetting.Builder<AntiSurroundMode>().name("anti-surround-mode")
            .description("Places crystals in places to prevent surround")
            .defaultValue(AntiSurroundMode.Auto).build());

    private final Setting<Boolean> antiSurroundInnerSnap = sgGeneral.add(new BoolSetting.Builder()
        .name("anti-surround-inner-snap")
        .description("Instantly snaps the camera when it needs to for inner place")
        .defaultValue(true).visible(() -> antiSurroundMode.get() == AntiSurroundMode.Auto
            || antiSurroundMode.get() == AntiSurroundMode.Inner)
        .build());

    private final Setting<Boolean> antiSurroundOuterSnap = sgGeneral.add(new BoolSetting.Builder()
        .name("anti-surround-outer-snap")
        .description("Instantly snaps the camera when it needs to for outer place")
        .defaultValue(true).visible(() -> antiSurroundMode.get() == AntiSurroundMode.Auto
            || antiSurroundMode.get() == AntiSurroundMode.Outer)
        .build());

    private final Setting<Double> antiSurroundOuterCooldown = sgGeneral.add(new DoubleSetting.Builder()
        .name("anti-surround-outer-cooldown")
        .description("Time to wait between placing crystals")
        .defaultValue(0.1).min(0).sliderMax(1.0).visible(() -> antiSurroundMode.get() == AntiSurroundMode.Auto
            || antiSurroundMode.get() == AntiSurroundMode.Outer)
        .build());

    private final Setting<Boolean> breakIndicatorsSync = sgGeneral.add(new BoolSetting.Builder()
        .name("break-indicators-sync")
        .description("Syncs auto-mine scoring with break indicators. Basically leads to quad mine :)")
        .defaultValue(true)
        .build());

    private final Setting<Boolean> breakIndicatorsSyncOnlyFriends = sgGeneral.add(new BoolSetting.Builder()
        .name("break-indicators-sync-only-friends")
        .description("Only penalizes blocks friends are mining")
        .defaultValue(false)
        .build());

    private final Setting<Double> breakIndicatorSyncPenalty = sgGeneral.add(new DoubleSetting.Builder()
        .name("break-indicators-sync-penalty")
        .description("Amount to penalize block for being broken by someone else")
        .defaultValue(8.5).min(0).sliderMax(25).visible(() -> breakIndicatorsSync.get())
        .build());

    private final Setting<Boolean> renderDebugScores = sgRender
        .add(new BoolSetting.Builder().name("render-debug-scores")
            .description("Renders scores and their blocks.").defaultValue(false).build());

    private SilentMine silentMine = null;
    private PlayerEntity targetPlayer = null;
    private CityBlock target1 = null;
    private CityBlock target2 = null;
    private BlockPos ignorePos = null;
    private long lastOuterPlaceTime = 0;

    public AutoMine() {
        super(Categories.Combat, "auto-mine",
            "Automatically mines blocks. Requires SilentMine to work.");
    }

    @Override
    public void onActivate() {
        super.onActivate();
        if (silentMine == null) {
            silentMine = Modules.get().get(SilentMine.class);
        }
    }

    @Override
    public void onDeactivate() {
        if (silentMine != null) {
            silentMine.cancelBreaking();
        }
        target1 = null;
        target2 = null;
        targetPlayer = null;
        super.onDeactivate();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (silentMine == null) {
            silentMine = Modules.get().get(SilentMine.class);
        }

        // Clear invalid targets if blocks are air or target moved away
        if (target1 != null) {
            if (!isValidTargetBlock(target1.blockPos)) {
                target1 = null;
            }
        }
        if (target2 != null) {
            if (!isValidTargetBlock(target2.blockPos)) {
                target2 = null;
            }
        }

        if (targetPlayer != null && (target1 != null || target2 != null) && repositionIfNotAdjacent.get()) {
            BlockPos currentTargetPos = target1 != null ? target1.blockPos : target2.blockPos;
            if (!isBlockAdjacentOrInsidePlayer(targetPlayer, currentTargetPos)) {
                silentMine.cancelBreaking();
                target1 = null;
                target2 = null;
            }
        }

        update();
    }

    private boolean isValidTargetBlock(BlockPos blockPos) {
        if (blockPos == null) return false;
        
        // Check if block is air and not being rebroken
        BlockState state = mc.world.getBlockState(blockPos);
        boolean isPosGoodRebreak = silentMine.canRebreakRebreakBlock() && blockPos.equals(silentMine.getRebreakBlockPos());
        
        if (state.isAir() && !isPosGoodRebreak) return false;
        
        // Check if target player is still valid
        if (targetPlayer == null || !targetPlayer.isAlive() || targetPlayer.isDead()) return false;
        
        // Check if block is still around the target
        if (!isStillAroundTarget(blockPos)) return false;
        
        return true;
    }

    private boolean isBlockAdjacentOrInsidePlayer(PlayerEntity player, BlockPos blockPos) {

        BlockPos playerPos = player.getBlockPos();
        if (blockPos.equals(playerPos)) return true;
        if (blockPos.equals(playerPos.up())) return true;
        if (blockPos.equals(playerPos.down())) return true;

        for (Direction dir : Direction.Type.HORIZONTAL) {
            if (blockPos.equals(playerPos.offset(dir))) return true;
            if (blockPos.equals(playerPos.up().offset(dir))) return true;
        }

        Box playerBox = player.getBoundingBox().shrink(0.01, 0.01, 0.01);
        Box blockBox = new Box(blockPos);
        return playerBox.intersects(blockBox);
    }

    @EventHandler
    private void onSilentMineFinished(SilentMineFinishedEvent.Pre event) {
        if (targetPlayer == null || antiSurroundMode.get() == AntiSurroundMode.None) return;

        if (antiSurroundMode.get() == AntiSurroundMode.Auto || antiSurroundMode.get() == AntiSurroundMode.Outer) {
            for (Direction dir : Direction.HORIZONTAL) {
                BlockPos playerSurroundBlock = targetPlayer.getBlockPos().offset(dir);
                if (event.getBlockPos().equals(playerSurroundBlock)) {
                    Box checkBox = Box.of(playerSurroundBlock.toCenterPos(), 2.5, 3.0, 2.5);
                    Box blockHitbox = new Box(playerSurroundBlock);
                    boolean outerSpeedCheck = (System.currentTimeMillis() - lastOuterPlaceTime) > (antiSurroundOuterCooldown.get() * 1000.0);
                    if (!outerSpeedCheck) return;
                    for (BlockPos blockPos : BlockUtils.iterate(checkBox)) {
                        if (!mc.world.isAir(blockPos)) continue;
                        BlockState downState = mc.world.getBlockState(blockPos.down());
                        if (!downState.isOf(Blocks.OBSIDIAN) && !downState.isOf(Blocks.BEDROCK)) continue;
                        Box crystalPlaceHitbox = new Box(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockPos.getX() + 1, blockPos.getY() + 2, blockPos.getZ() + 1);
                        if (EntityUtils.intersectsWithEntity(crystalPlaceHitbox, entity -> !entity.isSpectator())) continue;
                        Vec3d crystalPos = new Vec3d(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5);
                        Box crystalHitbox = new Box(crystalPos.x - 1, crystalPos.y, crystalPos.z - 1, crystalPos.x + 1, crystalPos.y + 2, crystalPos.z + 1);
                        if (crystalHitbox.intersects(blockHitbox)) {
                            Modules.get().get(AutoCrystal.class).preplaceCrystal(blockPos, antiSurroundOuterSnap.get());
                            lastOuterPlaceTime = System.currentTimeMillis();
                            return;
                        }
                    }
                }
            }
        }

        if (antiSurroundMode.get() == AntiSurroundMode.Auto || antiSurroundMode.get() == AntiSurroundMode.Inner) {
            for (Direction dir : Direction.HORIZONTAL) {
                BlockPos playerSurroundBlock = targetPlayer.getBlockPos().offset(dir);
                if (playerSurroundBlock.equals(event.getBlockPos())) {
                    Modules.get().get(AutoCrystal.class).preplaceCrystal(playerSurroundBlock, antiSurroundInnerSnap.get());
                }
            }
        }
    }

    private boolean hasAirAroundTarget() {
        if (targetPlayer == null) return false;

        BlockPos rebreakingPos = silentMine.getRebreakBlockPos();

        for (Direction dir : Direction.Type.HORIZONTAL) {
            BlockPos surroundPos = targetPlayer.getBlockPos().offset(dir);
            if (mc.world.getBlockState(surroundPos).isAir() || surroundPos.equals(rebreakingPos)) {
                return true;
            }
        }
        return false;
    }

    private boolean isRebreakingTargetSurround() {
        if (targetPlayer == null) return false;

        BlockPos rebreakPos = silentMine.getRebreakBlockPos();
        if (rebreakPos == null) return false;

        if (rebreakPos.equals(targetPlayer.getBlockPos())) return true;

        for (Direction dir : Direction.Type.HORIZONTAL) {
            if (rebreakPos.equals(targetPlayer.getBlockPos().offset(dir))) {
                return true;
            }
        }

        return false;
    }

    private boolean hasOpeningAroundTarget() {
        if (targetPlayer == null) return false;

        Box boundingBox = targetPlayer.getBoundingBox().shrink(0.01, 0.1, 0.01);
        double feetY = targetPlayer.getY();
        Box feetBox = new Box(boundingBox.minX, feetY, boundingBox.minZ, boundingBox.maxX, feetY + 0.1, boundingBox.maxZ);

        int blockCount = (int) BlockPos.stream(feetBox).count();

        if (blockCount > 1) {
            return false;
        }

        if (isPlayerAtBlockCorner(targetPlayer)) {
            return false;
        }

        BlockPos rebreakPos = silentMine.getRebreakBlockPos();
        boolean hasRebreak = rebreakPos != null && !silentMine.canRebreakRebreakBlock();

        boolean isPhased = false;

        for (BlockPos pos : BlockPos.iterate(
            (int) Math.floor(boundingBox.minX), (int) Math.floor(boundingBox.minY), (int) Math.floor(boundingBox.minZ),
            (int) Math.floor(boundingBox.maxX), (int) Math.floor(boundingBox.minY + 1.6), (int) Math.floor(boundingBox.maxZ))) {

            BlockState state = mc.world.getBlockState(pos);
            if (!state.isAir() && !state.getBlock().equals(Blocks.WATER) && !state.getBlock().equals(Blocks.LAVA)) {
                isPhased = true;
                break;
            }
        }

        if (isPhased) return false;

        BlockPos playerPos = targetPlayer.getBlockPos();
        for (Direction dir : Direction.Type.HORIZONTAL) {
            BlockPos surroundPos = playerPos.offset(dir);

            if (mc.world.getBlockState(surroundPos).isAir() ||
                (hasRebreak && surroundPos.equals(rebreakPos))) {
                return true;
            }
        }

        return false;
    }

    private boolean isPlayerAtBlockCorner(PlayerEntity player) {
        double x = player.getX();
        double z = player.getZ();

        double xFraction = Math.abs(x - Math.floor(x));
        double zFraction = Math.abs(z - Math.floor(z));

        return (xFraction < 0.3 || xFraction > 0.7) && (zFraction < 0.3 || zFraction > 0.7);
    }

    private void update() {
        if (silentMine == null) {
            silentMine = Modules.get().get(SilentMine.class);
        }

        BlockPos selfHeadPos = mc.player.getBlockPos().up();
        BlockState selfHeadBlock = mc.world.getBlockState(selfHeadPos);
        BlockState selfFeetBlock = mc.world.getBlockState(mc.player.getBlockPos());
        boolean isSwimming = mc.player.isSubmergedInWater() || mc.player.isInLava();
        boolean shouldBreakSelfHead = BlockUtils.canBreak(selfHeadPos, selfHeadBlock) &&
            (selfHeadBlock.isOf(Blocks.OBSIDIAN) || selfHeadBlock.isOf(Blocks.CRYING_OBSIDIAN));
        boolean prioHead = false;

        if (antiSwim.get() == AntiSwimMode.Always && shouldBreakSelfHead) {
            silentMine.silentBreakBlock(selfHeadPos, Direction.UP, 10.0);
            prioHead = true;
        }

        if (antiSwim.get() == AntiSwimMode.OnMineAndSwim && mc.player.isCrawling() && shouldBreakSelfHead) {
            silentMine.silentBreakBlock(selfHeadPos, Direction.UP, 30.0);
            prioHead = true;
        }

        if ((antiSwim.get() == AntiSwimMode.OnMine || antiSwim.get() == AntiSwimMode.OnMineAndSwim) &&
            Modules.get().get(BreakIndicators.class).isBlockBeingBroken(mc.player.getBlockPos()) && shouldBreakSelfHead) {
            silentMine.silentBreakBlock(selfHeadPos, Direction.UP, 20.0);
            prioHead = true;
        }

        targetPlayer = (PlayerEntity) TargetUtils.get(entity -> {
            if (entity.equals(mc.player) || entity.equals(mc.cameraEntity)) return false;
            if (!(entity instanceof PlayerEntity player)) return false;
            if (!player.isAlive() || player.isDead() || player.isCreative() || !Friends.get().shouldAttack(player)) return false;
            if (entity.getPos().distanceTo(mc.player.getEyePos()) > range.get()) return false;
            if (ignoreNakeds.get() && ((meteordevelopment.meteorclient.mixininterface.IPlayerInventory) player.getInventory()).meteor$getArmor().stream().allMatch(itemStack -> itemStack.isEmpty())) return false;
            return true;
        }, targetPriority.get());

        if (targetPlayer == null) {
            if (prioHead) return;
            return;
        }

        if (prioHead) return;

        if (silentMine.hasDelayedDestroy() && selfHeadBlock.isOf(Blocks.OBSIDIAN) && selfFeetBlock.isAir() &&
            silentMine.getRebreakBlockPos() != null && silentMine.getRebreakBlockPos().equals(selfHeadPos)) {
            return;
        }

        if (hasOpeningAroundTarget()) {

            if (isSwimming && shouldBreakSelfHead && silentMine.canRebreakRebreakBlock()) {
                silentMine.silentBreakBlock(selfHeadPos, Direction.UP, 20.0);
            }

            return;
        }

        if (isSwimming && shouldBreakSelfHead) {
            if (silentMine.canRebreakRebreakBlock() || !silentMine.hasRebreakBlock()) {
                silentMine.silentBreakBlock(selfHeadPos, Direction.UP, 20.0);

                if (silentMine.getRebreakBlockPos() != null && silentMine.getRebreakBlockPos().equals(selfHeadPos)) {
                    return;
                }
            }
        }

        findTargetBlocks();

        if (!silentMine.hasDelayedDestroy() && target1 != null) {
            silentMine.silentBreakBlock(target1.blockPos, Direction.UP, 10.0);
        }

        if ((silentMine.canRebreakRebreakBlock() || !silentMine.hasRebreakBlock()) && target2 != null) {
            silentMine.silentBreakBlock(target2.blockPos, Direction.UP, 10.0);
        }
    }

    private void findTargetBlocks() {
        target1 = findCityBlock(null);
        ignorePos = target1 != null ? target1.blockPos : null;
        target2 = findCityBlock(target1 != null ? target1.blockPos : null);
    }

    private boolean isStillAroundTarget(BlockPos blockPos) {
        if (targetPlayer == null) return false;

        if (blockPos.equals(targetPlayer.getBlockPos())) return true;

        for (Direction dir : Direction.Type.HORIZONTAL) {
            if (blockPos.equals(targetPlayer.getBlockPos().offset(dir))) {
                return true;
            }
        }

        return false;
    }

    private boolean isValidSurroundBlock(BlockPos blockPos) {
        if (blockPos == null) return false;

        BlockState state = mc.world.getBlockState(blockPos);
        return !state.isAir() && BlockUtils.canBreak(blockPos, state) && silentMine.inBreakRange(blockPos);
    }

    private CityBlock findCityBlock(BlockPos exclude) {
        if (targetPlayer == null) return null;

        boolean set = false;
        CityBlock bestBlock = new CityBlock();
        Set<CheckPos> checkPos = new HashSet<>();
        Box boundingBox = targetPlayer.getBoundingBox().shrink(0.01, 0.1, 0.01);
        double feetY = targetPlayer.getY();
        Box feetBox = new Box(boundingBox.minX, feetY, boundingBox.minZ, boundingBox.maxX, feetY + 0.1, boundingBox.maxZ);
        boolean inBedrock = BlockPos.stream(feetBox).anyMatch(blockPos -> mc.world.getBlockState(blockPos).getBlock() == Blocks.BEDROCK);

        if (inBedrock) addBedrockCaseCheckPositions(checkPos);
        else addNormalCaseCheckPositions(checkPos);

        for (CheckPos pos : checkPos) {
            BlockPos blockPos = pos.blockPos;
            if (blockPos.equals(exclude)) continue;

            BlockState block = mc.world.getBlockState(blockPos);
            boolean isPosGoodRebreak = silentMine.canRebreakRebreakBlock() && blockPos.equals(silentMine.getRebreakBlockPos());

            if (block.isAir() && !isPosGoodRebreak) continue;
            if (!BlockUtils.canBreak(blockPos, block) && !isPosGoodRebreak) continue;
            if (!silentMine.inBreakRange(blockPos)) continue;

            double score = inBedrock ? scoreBedrockCityBlock(pos) : scoreNormalCityBlock(pos);
            if (score == INVALID_SCORE) continue;

            if (isPosGoodRebreak) score += 40;
            else score -= getScorePenaltyForSync(pos.blockPos);

            if (score > bestBlock.score) {
                bestBlock.score = score;
                bestBlock.blockPos = blockPos;
                bestBlock.isFeetBlock = isBlockInFeet(blockPos);
                set = true;
            }
        }

        return set ? bestBlock : null;
    }

    private void addNormalCaseCheckPositions(Set<CheckPos> checkPos) {
        Box boundingBox = targetPlayer.getBoundingBox().shrink(0.01, 0.1, 0.01);
        double feetY = targetPlayer.getY();
        Box feetBox = new Box(boundingBox.minX, feetY, boundingBox.minZ, boundingBox.maxX, feetY + 0.1, boundingBox.maxZ);
        for (BlockPos pos : BlockUtils.iterate(feetBox)) checkPos.add(new CheckPos(pos, CheckPosType.Feet));
        for (BlockPos pos : BlockUtils.iterate(feetBox)) {
            for (Direction dir : Direction.Type.HORIZONTAL) checkPos.add(new CheckPos(pos.offset(dir), CheckPosType.Surround));
        }
        checkPos.add(new CheckPos(targetPlayer.getBlockPos(), CheckPosType.Feet));
        boolean inMultipleBlocks = BlockPos.stream(feetBox).count() > 1;
        if (!inMultipleBlocks) {
            for (Direction dir : Direction.Type.HORIZONTAL) {
                switch (extendBreakMode.get()) {
                    case None -> {}
                    case Long -> checkPos.add(new CheckPos(targetPlayer.getBlockPos().offset(dir, 2), CheckPosType.Extend));
                    case Corner -> {
                        Direction perpDir = getCornerPerpDir(dir);
                        checkPos.add(new CheckPos(targetPlayer.getBlockPos().offset(dir).offset(perpDir), CheckPosType.Extend));
                    }
                }
            }
        }
    }

    private Direction getCornerPerpDir(Direction dir) {
        return switch (dir) {
            case NORTH -> Direction.EAST;
            case SOUTH -> Direction.WEST;
            case EAST -> Direction.NORTH;
            case WEST -> Direction.SOUTH;
            default -> null;
        };
    }

    private void addBedrockCaseCheckPositions(Set<CheckPos> checkPos) {
        Box boundingBox = targetPlayer.getBoundingBox().shrink(0.01, 0.1, 0.01);
        double feetY = targetPlayer.getY();
        Box feetBox = new Box(boundingBox.minX, feetY, boundingBox.minZ, boundingBox.maxX, feetY + 0.1, boundingBox.maxZ);
        boolean canFallDown = BlockPos.stream(feetBox).allMatch(blockPos -> mc.world.getBlockState(blockPos.down()).getBlock() != Blocks.BEDROCK);
        boolean canBeHitUp = BlockPos.stream(feetBox).allMatch(blockPos -> mc.world.getBlockState(blockPos.up(2)).getBlock() != Blocks.BEDROCK);
        for (BlockPos pos : BlockUtils.iterate(feetBox)) {
            if (canFallDown) checkPos.add(new CheckPos(pos.down(), CheckPosType.Below));
            if (canBeHitUp) checkPos.add(new CheckPos(pos.up(2), CheckPosType.Head));
            checkPos.add(new CheckPos(pos.up(), CheckPosType.FacePlace));
            for (Direction dir : Direction.Type.HORIZONTAL) checkPos.add(new CheckPos(pos.up().offset(dir), CheckPosType.FacePlace));
            checkPos.add(new CheckPos(pos, CheckPosType.Surround));
            for (Direction dir : Direction.Type.HORIZONTAL) checkPos.add(new CheckPos(pos.offset(dir), CheckPosType.Surround));
        }
    }

    private double scoreNormalCityBlock(CheckPos pos) {
        BlockPos blockPos = pos.blockPos;
        double score = 0;
        BlockState block = mc.world.getBlockState(blockPos);

        if (blockPos.equals(targetPlayer.getBlockPos())) {
            BlockState headBlock = mc.world.getBlockState(blockPos.up());
            if (headBlock.getBlock().equals(Blocks.OBSIDIAN)) score += 100;
            else {
                if (block.getBlock() == Blocks.COBWEB) return INVALID_SCORE;
                score += 50;
            }
        } else {
            BlockState selfHeadState = mc.world.getBlockState(mc.player.getBlockPos().up());
            if (blockPos.equals(mc.player.getBlockPos()) && (selfHeadState.getBlock().equals(Blocks.OBSIDIAN) || selfHeadState.getBlock().equals(Blocks.BEDROCK))) return INVALID_SCORE;
            if (pos.type == CheckPosType.Surround) {
                score += 3;
                boolean isPosAntiSurround = false;
                for (Direction dir : Direction.Type.HORIZONTAL) {
                    if (!targetPlayer.getBlockPos().offset(dir).equals(blockPos)) continue;
                    BlockPos antiSurroundBlockPos = targetPlayer.getBlockPos().offset(dir, 2);
                    if (getBlockStateIgnore(antiSurroundBlockPos).isAir() && isCrystalBlock(antiSurroundBlockPos.down())) {
                        isPosAntiSurround = true;
                        break;
                    }
                    Direction perpDir = getCornerPerpDir(dir);
                    BlockPos antiSurroundCornerBlockPos = targetPlayer.getBlockPos().offset(dir).offset(perpDir);
                    if (getBlockStateIgnore(antiSurroundCornerBlockPos).isAir() && isCrystalBlock(antiSurroundCornerBlockPos.down())) {
                        isPosAntiSurround = true;
                        break;
                    }
                }
                if (isPosAntiSurround) score += 25;
            }
            if (pos.type == CheckPosType.Extend) score += 20;
        }

        double d = targetPlayer.getPos().distanceTo(Vec3d.ofCenter(blockPos));
        score += 10 / d;
        return score;
    }

    private double scoreBedrockCityBlock(CheckPos pos) {
        BlockPos blockPos = pos.blockPos;
        double score = 0;
        if (blockPos.getY() == targetPlayer.getBlockY() + 2 || blockPos.getY() == targetPlayer.getBlockY() - 1) score += 10;
        Box boundingBox = targetPlayer.getBoundingBox().shrink(0.01, 0.1, 0.01);
        double feetY = targetPlayer.getY();
        Box feetBox = new Box(boundingBox.minX, feetY, boundingBox.minZ, boundingBox.maxX, feetY + 0.1, boundingBox.maxZ);
        if (BlockPos.stream(feetBox).count() == 1) {
            boolean canMineFaceBlock = mc.world.getBlockState(targetPlayer.getBlockPos().up()).getBlock() != Blocks.BEDROCK;
            if (canMineFaceBlock) {
                if (blockPos.equals(targetPlayer.getBlockPos().up())) score += 20;
                else {
                    boolean isSelfTrapBlock = false;
                    for (Direction dir : Direction.HORIZONTAL) {
                        if (targetPlayer.getBlockPos().up().offset(dir).equals(blockPos)) {
                            isSelfTrapBlock = true;
                            break;
                        }
                    }
                    if (isSelfTrapBlock) score += 7.5;
                }
            }
        }
        double d = targetPlayer.getPos().distanceTo(Vec3d.ofCenter(blockPos));
        score += 10 / d;
        return score;
    }

    private boolean isBlockInFeet(BlockPos blockPos) {
        Box boundingBox = targetPlayer.getBoundingBox().shrink(0.01, 0.1, 0.01);
        double feetY = targetPlayer.getY();
        Box feetBox = new Box(boundingBox.minX, feetY, boundingBox.minZ, boundingBox.maxX, feetY + 0.1, boundingBox.maxZ);
        for (BlockPos pos : BlockPos.iterate((int) Math.floor(feetBox.minX), (int) Math.floor(feetBox.minY), (int) Math.floor(feetBox.minZ), (int) Math.floor(feetBox.maxX), (int) Math.floor(feetBox.maxY), (int) Math.floor(feetBox.maxZ))) {
            if (blockPos.equals(pos)) return true;
        }
        return false;
    }

    private boolean isCrystalBlock(BlockPos blockPos) {
        BlockState blockState = mc.world.getBlockState(blockPos);
        return blockState.isOf(Blocks.OBSIDIAN) || blockState.isOf(Blocks.BEDROCK);
    }

    public boolean isTargetedPos(BlockPos blockPos) {
        return (target1 != null && target1.blockPos.equals(blockPos)) || (target2 != null && target2.blockPos.equals(blockPos));
    }

    private BlockState getBlockStateIgnore(BlockPos blockPos) {
        if (blockPos == null || blockPos.equals(ignorePos)) return Blocks.AIR.getDefaultState();
        return mc.world.getBlockState(blockPos);
    }

    private double getScorePenaltyForSync(BlockPos blockPos) {
        if (!breakIndicatorsSync.get()) return 0;
        BreakIndicators breakIndicators = Modules.get().get(BreakIndicators.class);
        if (breakIndicators == null || !breakIndicators.isActive()) return 0;
        
        if (breakIndicators.isBeingDoublemined(blockPos)) {
            PlayerEntity doubleminer = breakIndicators.getPlayerDoubleminingBlock(blockPos);
            if (doubleminer == null) return 0.0;
            
            // Never penalize our own mining
            if (doubleminer == mc.player) return 0.0;
            
            // If only friends setting is enabled, check if the doubl eminer is a friend
            if (breakIndicatorsSyncOnlyFriends.get() && !Friends.get().isFriend(doubleminer)) return 0.0;
            
            // Apply penalty for blocks being mined by others
            return breakIndicatorSyncPenalty.get();
        }
        return 0.0;
    }

    public boolean isTargetingAnything() {
        return target1 != null || target2 != null;
    }

    private void render3d(Render3DEvent event) {
        if (targetPlayer == null || !renderDebugScores.get()) return;
        double bestScore = 0;
        Set<CheckPos> checkPos = new HashSet<>();
        Box boundingBox = targetPlayer.getBoundingBox().shrink(0.01, 0.1, 0.01);
        double feetY = targetPlayer.getY();
        Box feetBox = new Box(boundingBox.minX, feetY, boundingBox.minZ, boundingBox.maxX, feetY + 0.1, boundingBox.maxZ);
        boolean inBedrock = BlockPos.stream(feetBox).anyMatch(blockPos -> mc.world.getBlockState(blockPos).getBlock() == Blocks.BEDROCK);
        if (inBedrock) addBedrockCaseCheckPositions(checkPos);
        else addNormalCaseCheckPositions(checkPos);

        for (CheckPos pos : checkPos) {
            BlockPos blockPos = pos.blockPos;
            BlockState block = mc.world.getBlockState(blockPos);
            boolean isPosGoodRebreak = silentMine.canRebreakRebreakBlock() && blockPos.equals(silentMine.getRebreakBlockPos());
            if (block.isAir() && !isPosGoodRebreak) continue;
            if (!BlockUtils.canBreak(blockPos, block) && !isPosGoodRebreak) continue;
            if (!silentMine.inBreakRange(blockPos)) continue;
            double score = inBedrock ? scoreBedrockCityBlock(pos) : scoreNormalCityBlock(pos);
            if (score == INVALID_SCORE) continue;
            if (isPosGoodRebreak) score += 40;
            else score -= getScorePenaltyForSync(pos.blockPos);
            if (score > bestScore) bestScore = score;
        }

        Color color = Color.RED;
        for (CheckPos pos : checkPos) {
            BlockPos blockPos = pos.blockPos;
            BlockState block = mc.world.getBlockState(blockPos);
            boolean isPosGoodRebreak = silentMine.canRebreakRebreakBlock() && blockPos.equals(silentMine.getRebreakBlockPos());
            if (block.isAir() && !isPosGoodRebreak) continue;
            if (!BlockUtils.canBreak(blockPos, block) && !isPosGoodRebreak) continue;
            if (!silentMine.inBreakRange(blockPos)) continue;
            double score = inBedrock ? scoreBedrockCityBlock(pos) : scoreNormalCityBlock(pos);
            if (score == INVALID_SCORE) continue;
            if (isPosGoodRebreak) score += 40;
            else score -= getScorePenaltyForSync(pos.blockPos);
            double alpha = (score / bestScore) / 4;
            event.renderer.box(blockPos, color.a((int) (255.0 * alpha)), Color.WHITE, ShapeMode.Sides, 0);
        }
    }

    @EventHandler
    private void onRender2d(Render2DEvent event) {
        if (targetPlayer == null || !renderDebugScores.get()) return;
        Vector3d vec3 = new Vector3d();
        Set<CheckPos> checkPos = new HashSet<>();
        Box boundingBox = targetPlayer.getBoundingBox().shrink(0.01, 0.1, 0.01);
        double feetY = targetPlayer.getY();
        Box feetBox = new Box(boundingBox.minX, feetY, boundingBox.minZ, boundingBox.maxX, feetY + 0.1, boundingBox.maxZ);
        boolean inBedrock = BlockPos.stream(feetBox).anyMatch(blockPos -> mc.world.getBlockState(blockPos).getBlock() == Blocks.BEDROCK);
        if (inBedrock) addBedrockCaseCheckPositions(checkPos);
        else addNormalCaseCheckPositions(checkPos);

        for (CheckPos pos : checkPos) {
            BlockPos blockPos = pos.blockPos;
            BlockState block = mc.world.getBlockState(blockPos);
            boolean isPosGoodRebreak = silentMine.canRebreakRebreakBlock() && blockPos.equals(silentMine.getRebreakBlockPos());
            if (block.isAir() && !isPosGoodRebreak) continue;
            if (!BlockUtils.canBreak(blockPos, block) && !isPosGoodRebreak) continue;
            if (!silentMine.inBreakRange(blockPos)) continue;
            double score = inBedrock ? scoreBedrockCityBlock(pos) : scoreNormalCityBlock(pos);
            if (score == INVALID_SCORE) continue;
            if (isPosGoodRebreak) score += 40;
            else score -= getScorePenaltyForSync(pos.blockPos);
            vec3.set(blockPos.toCenterPos().x, blockPos.toCenterPos().y, blockPos.toCenterPos().z);
            if (NametagUtils.to2D(vec3, 1.25)) {
                NametagUtils.begin(vec3);
                TextRenderer.get().begin(1, false, true);
                String text = String.format("%.1f", score);
                double w = TextRenderer.get().getWidth(text) / 2;
                TextRenderer.get().render(text, -w, 0, Color.WHITE, true);
                TextRenderer.get().end();
                NametagUtils.end();
            }
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (mc.player == null || mc.world == null) return;
        update();
        render3d(event);
    }

    @Override
    public String getInfoString() {
        return targetPlayer != null ? EntityUtils.getName(targetPlayer) : null;
    }

    private class CityBlock {
        public BlockPos blockPos;
        public double score;
        public boolean isFeetBlock = false;
    }

    private class CheckPos {
        public final BlockPos blockPos;
        public final CheckPosType type;
        public CheckPos(BlockPos blockPos, CheckPosType type) {
            this.blockPos = blockPos;
            this.type = type;
        }
        @Override
        public int hashCode() {
            return blockPos.hashCode();
        }
    }

    public enum CheckPosType {
        Feet, Surround, Extend, FacePlace, Head, Below
    }

    private enum AntiSwimMode {
        None, Always, OnMine, OnMineAndSwim
    }

    private enum AntiSurroundMode {
        None, Inner, Outer, Auto
    }

    private enum ExtendBreakMode {
        None, Long, Corner
    }
}
