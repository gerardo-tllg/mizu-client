package meteordevelopment.meteorclient.systems.modules.combat;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.*;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.Hand;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.*;

public class ChineseAura extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Set<Item> allowedFeetItems = new HashSet<>() {{
        add(Items.LADDER);
        add(Items.VINE);
        add(Items.ITEM_FRAME);
        add(Items.COBWEB);
        add(Items.SCAFFOLDING);
    }};

    private final Set<Item> allowedHeadItems = new HashSet<>() {{
        add(Items.LADDER);
        add(Items.VINE);
        add(Items.COBWEB);
        add(Items.OAK_TRAPDOOR);
        add(Items.SPRUCE_TRAPDOOR);
        add(Items.BIRCH_TRAPDOOR);
        add(Items.JUNGLE_TRAPDOOR);
        add(Items.ACACIA_TRAPDOOR);
        add(Items.DARK_OAK_TRAPDOOR);
        add(Items.MANGROVE_TRAPDOOR);
        add(Items.CHERRY_TRAPDOOR);
        add(Items.BAMBOO_TRAPDOOR);
        add(Items.CRIMSON_TRAPDOOR);
        add(Items.WARPED_TRAPDOOR);
    }};

    private final Setting<Boolean> pauseEat = sgGeneral.add(new BoolSetting.Builder()
        .name("pause-eat").description("Pauses while eating.").defaultValue(true).build());

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("target-range").description("The maximum distance to target players.")
        .defaultValue(5).range(0, 5).sliderMax(5).build());

    private final Setting<SortPriority> priority =
        sgGeneral.add(new EnumSetting.Builder<SortPriority>().name("target-priority")
            .description("How to filter targets within range.")
            .defaultValue(SortPriority.ClosestAngle).build());

    private final Setting<List<Item>> feetItems = sgGeneral.add(new ItemListSetting.Builder()
        .name("feet-items").description("Items to place on enemies feet").filter(x -> {
            return allowedFeetItems.contains(x);
        }).build());

    private final Setting<List<Item>> headItems = sgGeneral.add(new ItemListSetting.Builder()
        .name("head-items").description("Items to place on enemies heads").filter(x -> {
            return allowedHeadItems.contains(x);
        }).build());

    private final Setting<Double> placeDelay = sgGeneral.add(new DoubleSetting.Builder()
        .name("place-delay").description("How many seconds to wait between placing stuff again")
        .defaultValue(0.2).min(0).sliderMax(2).build());

    private final Setting<Boolean> ignoreNakeds = sgGeneral.add(new BoolSetting.Builder()
        .name("ignore-nakeds").description("Ignores players with no armor.").defaultValue(true).build());

    private PlayerEntity targetPlayer = null;
    private final Map<BlockPos, Long> timeOfLastPlace = new HashMap<>();
    private int tickCounter = 0;

    public ChineseAura() {
        super(Categories.Combat, "chinese-aura", "Places whatever you want on your enemies. Extremely chinese.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (TargetUtils.isBadTarget(targetPlayer, range.get())) {
            targetPlayer = TargetUtils.getPlayerTarget(range.get(), priority.get());
            if (TargetUtils.isBadTarget(targetPlayer, range.get())) return;
        }

        if (ignoreNakeds.get() && isNaked(targetPlayer)) return;
        if (pauseEat.get() && mc.player.isUsingItem()) return;

        tickCounter++;

        FindItemResult headItemResult = InvUtils.find(x -> headItems.get().contains(x.getItem()));
        FindItemResult feetItemResult = InvUtils.find(x -> feetItems.get().contains(x.getItem()));

        long currentTime = System.currentTimeMillis();

        if (headItemResult.found()) {
            Item item = mc.player.getInventory().getStack(headItemResult.slot()).getItem();
            BlockPos upperPos = targetPlayer.getBlockPos().up();
            BlockPos lowerPos = targetPlayer.getBlockPos();
            boolean didPlace = false;

            if (MeteorClient.SWAP.beginSwap(headItemResult, true)) {
                if (item == Items.VINE) {
                    placeVine(headItemResult, lowerPos);
                    didPlace = true;
                } else if (item == Items.LADDER) {
                    placeLadder(headItemResult, lowerPos);
                    didPlace = true;
                } else if (item == Items.COBWEB) {
                    placeWeb(headItemResult, lowerPos);
                    didPlace = true;
                } else if (isTrapdoor(item)) {
                    didPlace = placeTrapdoor(headItemResult, upperPos, lowerPos, currentTime);
                }
                MeteorClient.SWAP.endSwap(true);
            }

            if (didPlace) {
                timeOfLastPlace.put(lowerPos, currentTime);
            }
        }

        if (feetItemResult.found()) {
            Item item = mc.player.getInventory().getStack(feetItemResult.slot()).getItem();
            boolean cooldownCheck = !timeOfLastPlace.containsKey(targetPlayer.getBlockPos()) ||
                ((double) currentTime - timeOfLastPlace.get(targetPlayer.getBlockPos())) / 1000.0 > placeDelay.get();

            if (cooldownCheck) {
                boolean didPlace = true;

                if (MeteorClient.SWAP.beginSwap(feetItemResult, true)) {
                    if (item == Items.ITEM_FRAME) {
                        placeItemFrame(feetItemResult);
                    } else if (item == Items.VINE) {
                        placeVine(feetItemResult, targetPlayer.getBlockPos());
                    } else if (item == Items.LADDER) {
                        placeLadder(feetItemResult, targetPlayer.getBlockPos());
                    } else if (item == Items.COBWEB) {
                        placeWeb(feetItemResult, targetPlayer.getBlockPos());
                    } else if (item == Items.SCAFFOLDING) {
                        placeScaffold(feetItemResult, targetPlayer.getBlockPos());
                    } else {
                        didPlace = false;
                    }
                    MeteorClient.SWAP.endSwap(true);
                }

                if (didPlace) {
                    timeOfLastPlace.put(targetPlayer.getBlockPos(), currentTime);
                }
            }
        }
    }

    private boolean isNaked(PlayerEntity player) {
        return ((meteordevelopment.meteorclient.mixininterface.IPlayerInventory) player.getInventory()).meteor$getArmor().get(0).isEmpty() &&
            ((meteordevelopment.meteorclient.mixininterface.IPlayerInventory) player.getInventory()).meteor$getArmor().get(1).isEmpty() &&
            ((meteordevelopment.meteorclient.mixininterface.IPlayerInventory) player.getInventory()).meteor$getArmor().get(2).isEmpty() &&
            ((meteordevelopment.meteorclient.mixininterface.IPlayerInventory) player.getInventory()).meteor$getArmor().get(3).isEmpty();
    }

    private void placeItemFrame(FindItemResult itemResult) {
        if (!mc.world.isAir(targetPlayer.getBlockPos())) return;

        BlockPos blockPos = targetPlayer.getBlockPos().down();
        Direction dir = Direction.UP;

        Box boundingBox = targetPlayer.getBoundingBox().shrink(0.01, 0.1, 0.01);
        double feetY = targetPlayer.getY();
        Box feetBox = new Box(boundingBox.minX, feetY, boundingBox.minZ, boundingBox.maxX,
            feetY + 0.1, boundingBox.maxZ);

        boolean inMultipleBlocks = BlockPos.stream(feetBox).count() > 1;

        if (inMultipleBlocks) return;

        final Vec3d hitPos = blockPos.toCenterPos().add(dir.getOffsetX() * 0.5,
            dir.getOffsetY() * 0.5, dir.getOffsetZ() * 0.5);

        List<ItemFrameEntity> entities =
            mc.world.getEntitiesByType(TypeFilter.instanceOf(ItemFrameEntity.class),
                Box.of(hitPos, 0.1, 0.1, 0.1), (entity) -> true);

        if (entities.isEmpty()) {
            mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
                new BlockHitResult(hitPos, dir, blockPos, false),
                getSequence()
                ));
        }
    }

    private void placeVine(FindItemResult itemResult, BlockPos pos) {
        if (!mc.world.isAir(pos)) return;

        for (Direction dir : Direction.HORIZONTAL) {
            BlockPos supportPos = pos.offset(dir);
            dir = dir.getOpposite();
            if (canPlaceVine(pos, dir)) {
                final Vec3d hitPos = supportPos.toCenterPos().add(dir.getOffsetX() * 0.5,
                    0.75, dir.getOffsetZ() * 0.5);

                mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
                    new BlockHitResult(hitPos, dir, supportPos, false),
                    getSequence()
                ));
            }
        }
    }

    private void placeLadder(FindItemResult itemResult, BlockPos pos) {
        if (!mc.world.isAir(pos)) return;

        for (Direction dir : Direction.HORIZONTAL) {
            BlockPos supportPos = pos.offset(dir);
            dir = dir.getOpposite();
            if (canPlaceLadder(pos, dir)) {
                final Vec3d hitPos = supportPos.toCenterPos().add(dir.getOffsetX() * 0.5,
                    0.75, dir.getOffsetZ() * 0.5);

                mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
                    new BlockHitResult(hitPos, dir, supportPos, false),
                    getSequence()
                ));
            }
        }
    }

    private void placeWeb(FindItemResult itemResult, BlockPos pos) {
        List<BlockPos> placePoses = new ArrayList<>();
        placePoses.add(pos);

        if (!MeteorClient.BLOCK.beginPlacement(placePoses, Items.COBWEB)) return;

        placePoses.forEach(blockPos -> {
            MeteorClient.BLOCK.placeBlock(Items.COBWEB, blockPos);
        });

        MeteorClient.BLOCK.endPlacement();
    }

    private void placeScaffold(FindItemResult itemResult, BlockPos pos) {
        List<BlockPos> placePoses = new ArrayList<>();
        placePoses.add(pos);

        if (!MeteorClient.BLOCK.beginPlacement(placePoses, Items.SCAFFOLDING)) return;

        placePoses.forEach(blockPos -> {
            MeteorClient.BLOCK.placeBlock(Items.SCAFFOLDING, blockPos);
        });

        MeteorClient.BLOCK.endPlacement();
    }

    private boolean placeTrapdoor(FindItemResult itemResult, BlockPos upperPos, BlockPos lowerPos, long currentTime) {
        BlockState upperState = mc.world.getBlockState(upperPos);
        BlockPos supportPos = upperPos.up();
        boolean didPlace = false;

        if (!mc.world.getBlockState(supportPos).isSolidBlock(mc.world, supportPos)) return false;

        boolean canPlace = !timeOfLastPlace.containsKey(upperPos) ||
            ((double) currentTime - timeOfLastPlace.get(upperPos)) / 1000.0 > placeDelay.get();

        if (canPlace) {
            // Place trapdoor if not present
            if (!(upperState.getBlock() instanceof TrapdoorBlock)) {
                final Vec3d hitPos = supportPos.toCenterPos().add(0, -0.5, 0);
                mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
                    new BlockHitResult(hitPos, Direction.DOWN, supportPos, false),
                    getSequence()
                ));
                didPlace = true;
            }
            // Toggle trapdoor state if needed
            else if (isPlayerCrawling() != upperState.get(TrapdoorBlock.OPEN)) {
                mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
                    new BlockHitResult(upperPos.toCenterPos(), Direction.UP, upperPos, false),
                    getSequence()
                ));
                didPlace = true;
            }
        }

        return didPlace;
    }

    private boolean isPlayerCrawling() {
        if (targetPlayer == null) return false;
        return targetPlayer.getBoundingBox().getLengthY() < 1.5;
    }

    private boolean isTrapdoor(Item item) {
        return item == Items.OAK_TRAPDOOR || item == Items.SPRUCE_TRAPDOOR ||
            item == Items.BIRCH_TRAPDOOR || item == Items.JUNGLE_TRAPDOOR ||
            item == Items.ACACIA_TRAPDOOR || item == Items.DARK_OAK_TRAPDOOR ||
            item == Items.MANGROVE_TRAPDOOR || item == Items.CHERRY_TRAPDOOR ||
            item == Items.BAMBOO_TRAPDOOR || item == Items.CRIMSON_TRAPDOOR ||
            item == Items.WARPED_TRAPDOOR;
    }

    private boolean canPlaceVine(BlockPos pos, Direction side) {
        BlockState blockState = mc.world.getBlockState(pos);

        if (side == Direction.UP || side == Direction.DOWN) return false;

        if (!VineBlock.shouldConnectTo(mc.world, pos.offset(side), side.getOpposite())) return false;

        if (blockState.isOf(Blocks.VINE)) {
            BooleanProperty sideProperty = VineBlock.getFacingProperty(side);
            return !blockState.get(sideProperty);
        }

        return true;
    }

    public boolean canPlaceLadder(BlockPos pos, Direction side) {
        if (side == Direction.UP || side == Direction.DOWN) return false;

        BlockState blockState = mc.world.getBlockState(pos);

        BlockPos attachedPos = pos.offset(side.getOpposite());
        BlockState attachedState = mc.world.getBlockState(attachedPos);

        if (!Block.isFaceFullSquare(attachedState.getCollisionShape(mc.world, attachedPos), side)) return false;

        if (blockState.isOf(Blocks.LADDER)) {
            Direction existingDirection = blockState.get(LadderBlock.FACING);
            return existingDirection != side;
        }

        if (!blockState.isAir() && !blockState.isReplaceable()) return false;

        return true;
    }

    private int getSequence() {
        if (mc.world == null) return 0;
        return ((meteordevelopment.meteorclient.mixininterface.IClientWorld) mc.world).meteor$getAndIncrementSequence();
    }
}
