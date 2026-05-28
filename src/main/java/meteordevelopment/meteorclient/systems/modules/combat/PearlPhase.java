package meteordevelopment.meteorclient.systems.modules.combat;

import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.mixininterface.IClientWorld;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.managers.RotationManager;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.MovementFix;
import meteordevelopment.meteorclient.systems.modules.player.SilentMine;
import meteordevelopment.meteorclient.utils.entity.ProjectileEntitySimulator;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class PearlPhase extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Keybind> phaseBind = sgGeneral.add(new KeybindSetting.Builder()
        .name("key-bind").description("Phase on keybind press").build());

    private final Setting<RotateMode> rotateMode =
        sgGeneral.add(new EnumSetting.Builder<RotateMode>().name("rotate-mode")
            .description("Which method of rotating should be used.")
            .defaultValue(RotateMode.DelayedInstantWebOnly).build());

    private final Setting<Boolean> burrow =
        sgGeneral.add(new BoolSetting.Builder().name("burrow")
            .description("Places a block where you phase.").defaultValue(true).build());

    private final Setting<Integer> burrowDelay =
        sgGeneral.add(new IntSetting.Builder().name("burrow-delay")
            .description("Delay in ticks after placing the block before phasing.")
            .defaultValue(0).min(0).max(10).sliderMin(0).sliderMax(10).build());

    private final Setting<Boolean> antiPearlFail = sgGeneral.add(new BoolSetting.Builder()
        .name("anti-pearl-fail").description("Hits entities below you when you phase.")
        .defaultValue(true).build());

    private final Setting<Boolean> antiPearlFailStrict =
        sgGeneral.add(new BoolSetting.Builder().name("anti-pearl-fail-strict")
            .description("Waits for the entity to disappear before phasing.")
            .defaultValue(false).build());

    private final Setting<Boolean> rephase =
        sgGeneral.add(new BoolSetting.Builder().name("rephase")
            .description("Automatically rephase when the keybind is held down.")
            .defaultValue(false).build());

    private final Setting<Integer> scaffoldBreakDelay = sgGeneral.add(new IntSetting.Builder()
        .name("scaffold-break-delay")
        .description("Delay in ticks before throwing the pearl after breaking scaffolds.")
        .defaultValue(1)
        .min(0)
        .max(20)
        .sliderMin(0)
        .sliderMax(10)
        .visible(antiPearlFail::get)
        .build());

    private boolean active = false;
    private boolean keyUnpressed = false;
    private boolean blockPlaced = false;
    private int burrowTimer = 0;
    private final ProjectileEntitySimulator simulator = new ProjectileEntitySimulator();
    private int scaffoldBreakTimer = 0;
    private boolean scaffoldCleared = false;

    public PearlPhase() {
        super(Categories.Combat, "pearl-phase", "Phases into walls using pearls");
    }

    private void activate() {
        active = true;
        blockPlaced = false;
        burrowTimer = 0;
        scaffoldBreakTimer = 0;
        scaffoldCleared = false;

        if (mc.player == null || mc.world == null) {
            deactivate(false);
            return;
        }

        update();
    }

    private void deactivate(boolean phased) {
        active = false;
        blockPlaced = false;
        burrowTimer = 0;
        scaffoldBreakTimer = 0;
        scaffoldCleared = false;

        if (phased) {
            info("Phased");
        }
    }

    private void update() {
        if (mc.player == null || mc.world == null) {
            deactivate(false);
            return;
        }

        if (!active) {
            return;
        }

        Box boundingBox = mc.player.getBoundingBox().shrink(0.05, 0.1, 0.05);
        double feetY = mc.player.getY();

        Box feetBox = new Box(boundingBox.minX, feetY, boundingBox.minZ, boundingBox.maxX,
            feetY + 0.1, boundingBox.maxZ);

        if (BlockPos.stream(feetBox).anyMatch(blockPos -> {
            return mc.world.getBlockState(blockPos).isSolidBlock(mc.world, blockPos);
        })) {
            deactivate(false);
        }

        if (!MeteorClient.SWAP.canSwap(Items.ENDER_PEARL)) {
            deactivate(false);
            return;
        }

        if (mc.player.getItemCooldownManager().isCoolingDown(Items.ENDER_PEARL.getDefaultStack())) {
            deactivate(false);
            return;
        }

        if (mc.options.sneakKey.isPressed()) {
            deactivate(false);
            return;
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!active) {
            return;
        }

        if (burrow.get() && !blockPlaced) {
            placeBlock();
            blockPlaced = true;
            burrowTimer = burrowDelay.get();
            return;
        }

        if (burrowTimer > 0) {
            burrowTimer--;
            return;
        }

        if (scaffoldBreakTimer > 0) {
            scaffoldBreakTimer--;
            if (scaffoldBreakTimer == 0) {
                scaffoldCleared = true;
            }
            return;
        }

        if (antiPearlFailStrict.get() && mc.world.getBlockState(mc.player.getBlockPos()).isOf(Blocks.SCAFFOLDING)) {
            return;
        }

        Vec3d targetPos = calculateTargetPos();
        float[] angle = MeteorClient.ROTATION.getRotation(targetPos);

        switch (rotateMode.get()) {
            case Movement -> {
                MeteorClient.ROTATION.requestRotation(targetPos, 1000f);

                if (MeteorClient.ROTATION.lookingAt(Box.of(targetPos, 0.05, 0.05, 0.05))) {
                    throwPearl(angle[0], angle[1]);
                }
            }
            case Instant -> {
                if (mc.player.isOnGround()) {
                    MeteorClient.ROTATION.snapAt(targetPos);

                    throwPearl(angle[0], angle[1]);
                }
            }
            case DelayedInstant -> {
                MeteorClient.ROTATION.requestRotation(targetPos, 1000f);

                if (MeteorClient.ROTATION.lookingAt(Box.of(targetPos, 0.05, 0.05, 0.05))) {
                    MeteorClient.ROTATION.snapAt(targetPos);

                    throwPearl(angle[0], angle[1]);
                }
            }
            case DelayedInstantWebOnly -> {
                MeteorClient.ROTATION.requestRotation(targetPos, 1000f);

                if (MeteorClient.ROTATION.lookingAt(Box.of(targetPos, 0.05, 0.05, 0.05))) {
                    if (MovementFix.inWebs) {
                        MeteorClient.ROTATION.snapAt(targetPos);
                    }

                    throwPearl(angle[0], angle[1]);
                }
            }
        }
    }

    private void placeBlock() {
        Vec3d targetPos = calculateTargetPos();
        Box newHitbox = mc.player.getBoundingBox()
            .offset(targetPos.x - mc.player.getX(), 0, targetPos.z - mc.player.getZ())
            .expand(0.05);

        List<BlockPos> placePoses = new ArrayList<>();

        int minX = (int) Math.floor(newHitbox.minX);
        int maxX = (int) Math.floor(newHitbox.maxX);
        int minZ = (int) Math.floor(newHitbox.minZ);
        int maxZ = (int) Math.floor(newHitbox.maxZ);

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                BlockPos feetPos = new BlockPos(x, mc.player.getBlockPos().getY(), z);
                placePoses.add(feetPos);
            }
        }

        if (MeteorClient.BLOCK.beginPlacement(placePoses, Items.OBSIDIAN)) {
            placePoses.forEach(blockPos -> {
                MeteorClient.BLOCK.placeBlock(Items.OBSIDIAN, blockPos);
            });

            MeteorClient.BLOCK.endPlacement();
        }
    }

    private void throwPearl(float yaw, float pitch) {
        if (antiPearlFail.get()) {
            HitResult hitResult = getEnderPearlHitResult();

            if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
                Entity hitEntity = ((EntityHitResult) hitResult).getEntity();

                if (hitEntity instanceof EndCrystalEntity || hitEntity instanceof ItemFrameEntity) {
                    MeteorClient.ROTATION.requestRotation(hitEntity.getPos(), 11);

                    if (!MeteorClient.ROTATION.lookingAt(hitEntity.getBoundingBox())
                        && RotationManager.lastGround) {
                        MeteorClient.ROTATION.snapAt(hitEntity.getPos());
                    }

                    if (MeteorClient.ROTATION.lookingAt(hitEntity.getBoundingBox())) {
                        mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket
                            .attack(hitEntity, mc.player.isSneaking()));
                    }
                }

                if (antiPearlFailStrict.get() && hitEntity != null) {
                    return;
                }
            }

            if (mc.world.getBlockState(mc.player.getBlockPos()).isOf(Blocks.SCAFFOLDING)) {
                if (!scaffoldCleared) {
                    SilentMine silentMine = Modules.get().get(SilentMine.class);
                    if (silentMine.isActive()) {
                        silentMine.silentBreakBlock(mc.player.getBlockPos(), Direction.UP, 100f);
                    } else {
                        int sequence = ((IClientWorld) mc.world).meteor$getAndIncrementSequence();
                        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(
                            PlayerActionC2SPacket.Action.START_DESTROY_BLOCK,
                            mc.player.getBlockPos(),
                            Direction.UP,
                            sequence));
                        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(
                            PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK,
                            mc.player.getBlockPos(),
                            Direction.UP,
                            sequence + 1));
                    }

                    scaffoldBreakTimer = scaffoldBreakDelay.get();
                    return;
                }
            }
        }

        if (MeteorClient.SWAP.beginSwap(Items.ENDER_PEARL, true)) {
            int sequence = ((IClientWorld) mc.world).meteor$getAndIncrementSequence();

            mc.getNetworkHandler().sendPacket(
                new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, sequence, yaw, pitch));

            deactivate(true);
            MeteorClient.SWAP.endSwap(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST + 20)
    private void onRender(Render3DEvent event) {
        if (!phaseBind.get().isPressed()) {
            keyUnpressed = true;
        }

        if (phaseBind.get().isPressed() && keyUnpressed
            && !(mc.currentScreen instanceof ChatScreen)) {
            activate();
            keyUnpressed = false;
        }

        if (rephase.get() && phaseBind.get().isPressed() && !active) {
            activate();
        }

        update();
    }

    private HitResult getEnderPearlHitResult() {
        if (!simulator.set(mc.player, Items.ENDER_PEARL.getDefaultStack(), 0, false, 1f)) {
            return null;
        }

        for (int i = 0; i < 256; i++) {
            HitResult result = simulator.tick();

            if (result != null) {
                return result;
            }
        }

        return null;
    }

    private Vec3d calculateTargetPos() {
        final double X_OFFSET = Math.PI / 13;
        final double Z_OFFSET = Math.PI / 4;

        double playerX = mc.player.getX();
        double playerZ = mc.player.getZ();

        double x = playerX + MathHelper.clamp(
            toClosest(playerX, Math.floor(playerX) + X_OFFSET, Math.floor(playerX) + Z_OFFSET)
                - playerX,
            -0.2, 0.2);

        double z = playerZ + MathHelper.clamp(
            toClosest(playerZ, Math.floor(playerZ) + X_OFFSET, Math.floor(playerZ) + Z_OFFSET)
                - playerZ,
            -0.2, 0.2);

        double yOffset = mc.player.isCrawling() ? -0.1 : -0.5;
        return new Vec3d(x, mc.player.getY() + yOffset, z);
    }

    private double toClosest(double num, double min, double max) {
        double dmin = num - min;
        double dmax = max - num;

        if (dmax > dmin) {
            return min;
        } else {
            return max;
        }
    }

    public enum SwitchMode {
        SilentHotbar, SilentSwap
    }

    public enum RotateMode {
        Movement, Instant, DelayedInstant, DelayedInstantWebOnly
    }
}
