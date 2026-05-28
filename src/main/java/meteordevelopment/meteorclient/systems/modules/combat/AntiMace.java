package meteordevelopment.meteorclient.systems.modules.combat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MaceItem;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AntiMace extends Module {
    public enum Material {
        Obsidian,
        Cobweb
    }

    private static final double GRAVITY = -0.08D;
    private static final double TERMINAL_VELOCITY = -3.92D;
    private static final double DRAG_XZ_FLY = 0.99D;
    private static final double DRAG_Y_FLY = 0.98D;
    private static final double ALIGN_D = 1.5D;
    private static final double ALIGN_E = 0.01D;
    private static final double LOOK_PUSH = 0.1D;
    private static final int BOOST_DURATION_TICKS = 40;
    private static final double ELYTRA_GRAVITY = -0.04D;

    private final SettingGroup sgGeneral;
    private final SettingGroup sgPrediction;
    private final SettingGroup sgRender;

    private final Setting<Material> material;
    private final Setting<Integer> aboveOffset;
    private final Setting<Double> placeRange;
    private final Setting<SortPriority> priority;
    private final Setting<Boolean> onlyAbove;
    private final Setting<Boolean> requireMaceInHand;
    private final Setting<Boolean> pauseEat;
    private final Setting<Boolean> angleDetection;
    private final Setting<Double> steepAngleThreshold;
    private final Setting<Boolean> autoAirPlace;
    private final Setting<Boolean> predictionEnabled;
    private final Setting<Integer> fallPredictionTicks;
    private final Setting<Integer> elytraPredictionTicks;
    private final Setting<Boolean> render;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> predictColor;
    private final Setting<SettingColor> placementColor;

    private PlayerEntity target;
    private Vec3d lastPredictedCenter;
    private final List<BlockPos> lastPlacedThisTick;
    private final Map<UUID, Vec3d> lastPos;
    private final Map<UUID, Vec3d> estVel;
    private final Map<UUID, Long> lastServerTick;
    private final Map<UUID, Integer> boostingTicks;
    private double lastPredictedDistance;

    public AntiMace() {
        super(Categories.Combat, "anti-mace", "Places a 7-block pattern above you to block a falling/elytra mace attacker.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgPrediction = this.settings.createGroup("Prediction");
        this.sgRender = this.settings.createGroup("Render");

        this.material = this.sgGeneral.add(new EnumSetting.Builder<Material>()
            .name("material")
            .description("Material to counter the mace with.")
            .defaultValue(Material.Obsidian)
            .build());
        this.aboveOffset = this.sgGeneral.add(new IntSetting.Builder()
            .name("above-offset")
            .description("How many blocks above your head to place the pattern so mace dive/aura cannot reach you.")
            .defaultValue(3)
            .min(1)
            .sliderRange(1, 6)
            .build());
        this.placeRange = this.sgGeneral.add(new DoubleSetting.Builder()
            .name("place-range")
            .description("Max distance to the predicted point to activate.")
            .defaultValue(4.5D)
            .min(1.0D)
            .sliderMax(8.0D)
            .build());
        this.priority = this.sgGeneral.add(new EnumSetting.Builder<SortPriority>()
            .name("target-priority")
            .description("How to select the player to target.")
            .defaultValue(SortPriority.ClosestAngle)
            .build());
        this.onlyAbove = this.sgGeneral.add(new BoolSetting.Builder()
            .name("only-above")
            .description("Only target players above you.")
            .defaultValue(true)
            .build());
        this.requireMaceInHand = this.sgGeneral.add(new BoolSetting.Builder()
            .name("require-holding-mace")
            .description("Require the target to be holding a mace (toggle off for silent swap).")
            .defaultValue(true)
            .build());
        this.pauseEat = this.sgGeneral.add(new BoolSetting.Builder()
            .name("pause-eat")
            .description("Pause while using an item (e.g., eating).")
            .defaultValue(true)
            .build());
        this.angleDetection = this.sgGeneral.add(new BoolSetting.Builder()
            .name("angle-detection")
            .description("(Obsidian) Auto-detect the attacker's mace dive/aura angle and shape the obsidian pattern to intercept it.")
            .defaultValue(true)
            .visible(() -> this.material.get() == Material.Obsidian)
            .build());
        this.steepAngleThreshold = this.sgGeneral.add(new DoubleSetting.Builder()
            .name("steep-angle-threshold")
            .description("Pitch (degrees) above which the attack is treated as a vertical dive. Below it, a wedge is built toward the attacker.")
            .defaultValue(60.0D)
            .min(20.0D)
            .max(89.0D)
            .sliderRange(20.0D, 89.0D)
            .visible(() -> this.material.get() == Material.Obsidian && this.angleDetection.get())
            .build());
        this.autoAirPlace = this.sgGeneral.add(new BoolSetting.Builder()
            .name("auto-air-place")
            .description("(Obsidian) Order placements so blocks adjacent to existing supports go first; the seed block is air-placed when needed and the rest chain off it.")
            .defaultValue(true)
            .visible(() -> this.material.get() == Material.Obsidian)
            .build());

        this.predictionEnabled = this.sgPrediction.add(new BoolSetting.Builder()
            .name("prediction")
            .description("Predict with vanilla-like physics (no ground raycast).")
            .defaultValue(true)
            .build());
        this.fallPredictionTicks = this.sgPrediction.add(new IntSetting.Builder()
            .name("fall-prediction-ticks")
            .description("Ticks to simulate ahead while falling.")
            .defaultValue(7)
            .min(0)
            .sliderMax(20)
            .visible(predictionEnabled::get)
            .build());
        this.elytraPredictionTicks = this.sgPrediction.add(new IntSetting.Builder()
            .name("elytra-prediction-ticks")
            .description("Ticks to simulate ahead while elytra flying.")
            .defaultValue(7)
            .min(0)
            .sliderMax(20)
            .visible(predictionEnabled::get)
            .build());

        this.render = this.sgRender.add(new BoolSetting.Builder()
            .name("render")
            .description("Render predicted place position and last placed blocks.")
            .defaultValue(true)
            .build());
        this.shapeMode = this.sgRender.add(new EnumSetting.Builder<ShapeMode>()
            .name("shape-mode")
            .description("How shapes are rendered.")
            .defaultValue(ShapeMode.Both)
            .build());
        this.predictColor = this.sgRender.add(new ColorSetting.Builder()
            .name("predict-color")
            .description("Predicted attacker point color.")
            .defaultValue(new SettingColor(255, 140, 0, 35))
            .build());
        this.placementColor = this.sgRender.add(new ColorSetting.Builder()
            .name("placement-color")
            .description("Color of the placed blocks.")
            .defaultValue(new SettingColor(0, 200, 255, 35))
            .build());

        this.lastPlacedThisTick = new ArrayList<>();
        this.lastPos = new HashMap<>();
        this.estVel = new HashMap<>();
        this.lastServerTick = new HashMap<>();
        this.boostingTicks = new HashMap<>();
        this.lastPredictedDistance = 0.0D;
    }

    public void onActivate() {
        this.target = null;
        this.lastPredictedCenter = null;
        this.lastPlacedThisTick.clear();
        this.lastPos.clear();
        this.estVel.clear();
        this.lastServerTick.clear();
        this.boostingTicks.clear();
        this.lastPredictedDistance = 0.0D;
    }

    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        if (this.mc.world != null && !this.mc.world.getPlayers().isEmpty()) {
            Packet<?> var3 = event.packet;
            if (var3 instanceof PlaySoundS2CPacket) {
                PlaySoundS2CPacket packet = (PlaySoundS2CPacket) var3;
                if (packet.getSound().value() == SoundEvents.ITEM_MACE_SMASH_GROUND) {
                    Vec3d soundPos = new Vec3d(packet.getX(), packet.getY(), packet.getZ());
                    for (PlayerEntity player : this.mc.world.getPlayers()) {
                        if (player.getPos().squaredDistanceTo(soundPos) < 9.0D) {
                            this.boostingTicks.put(player.getUuid(), 40);
                            break;
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!this.boostingTicks.isEmpty()) {
            this.boostingTicks.entrySet().removeIf((entry) -> {
                int ticks = entry.getValue() - 1;
                if (ticks <= 0) {
                    return true;
                } else {
                    entry.setValue(ticks);
                    return false;
                }
            });
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        this.lastPredictedCenter = null;
        this.lastPlacedThisTick.clear();
        this.lastPredictedDistance = 0.0D;
        if (this.mc.world == null || this.mc.player == null) return;
        if (this.pauseEat.get() && this.mc.player.isUsingItem()) return;

        if (this.target == null || TargetUtils.isBadTarget(this.target, 14.0D)) {
            this.target = TargetUtils.getPlayerTarget(14.0D, this.priority.get());
            if (this.target == null) return;
        }

        if (this.onlyAbove.get() && this.target.getY() <= this.mc.player.getY() + 1.0D) return;
        if (this.requireMaceInHand.get() && !this.isHoldingMace(this.target)) return;

        UUID id = this.target.getUuid();
        long svTime = this.mc.world.getTime();
        Vec3d currPos = this.target.getPos();
        Vec3d prevPos = this.lastPos.get(id);
        Long lastSv = this.lastServerTick.get(id);
        if (prevPos != null && lastSv != null) {
            long dt = Math.max(1L, svTime - lastSv);
            Vec3d delta = currPos.subtract(prevPos);
            this.estVel.put(id, new Vec3d(delta.x / dt, delta.y / dt, delta.z / dt));
        } else {
            this.estVel.put(id, this.target.getVelocity());
        }
        this.lastPos.put(id, currPos);
        this.lastServerTick.put(id, svTime);

        boolean elytra = this.target.isGliding();
        boolean falling = !elytra && !this.target.isOnGround();
        if (!falling && !elytra) return;

        Vec3d future;
        if (this.predictionEnabled.get()) {
            if (elytra) {
                if (this.boostingTicks.containsKey(id)) {
                    future = this.simulateBoostedElytraFuturePos(this.target, this.elytraPredictionTicks.get());
                } else {
                    future = this.simulateElytraFuturePos(this.target, this.elytraPredictionTicks.get());
                }
            } else {
                future = this.simulateFallFrom(currPos, this.estVel.getOrDefault(id, this.target.getVelocity()), this.fallPredictionTicks.get());
            }
        } else {
            future = currPos;
        }

        Vec3d eyes = this.mc.player.getEyePos();
        double distanceToRawFuture = eyes.distanceTo(future);
        this.lastPredictedDistance = distanceToRawFuture;
        this.lastPredictedCenter = Vec3d.ofCenter(BlockPos.ofFloored(future));

        if (distanceToRawFuture > this.placeRange.get()) {
            return;
        }

        // Place the pattern above the player's head, not at the attacker's position.
        // This reliably blocks mace dive / mace aura regardless of small prediction error.
        BlockPos centerPos = this.mc.player.getBlockPos().up(this.aboveOffset.get());

        boolean obsidianAngle = this.material.get() == Material.Obsidian && this.angleDetection.get();
        List<BlockPos> pattern;
        if (obsidianAngle) {
            pattern = this.buildAttackAnglePattern(centerPos, future);
        } else {
            pattern = new ArrayList<>(7);
            pattern.add(centerPos);
            pattern.add(centerPos.up());
            pattern.add(centerPos.down());
            pattern.add(centerPos.north());
            pattern.add(centerPos.south());
            pattern.add(centerPos.east());
            pattern.add(centerPos.west());
        }

        // Only attempt positions that don't already contain our block.
        // This lets partial placements complete on subsequent ticks (once the
        // server acks prior blocks, adjacent "floating" seeds gain real supports)
        // and automatically repairs blocks broken by the attacker.
        Block desiredBlock = this.material.get() == Material.Obsidian ? Blocks.OBSIDIAN : Blocks.COBWEB;
        pattern.removeIf(pos -> this.mc.world.getBlockState(pos).getBlock() == desiredBlock);
        if (pattern.isEmpty()) return;

        if (this.material.get() == Material.Obsidian && this.autoAirPlace.get()) {
            // Order placements so blocks with existing supports place first;
            // floating seeds get air-placed, then their neighbours chain off them.
            pattern = this.orderForCascade(pattern);
        } else {
            // Sort topmost first so the ceiling blocks go down before side fillers.
            pattern.sort(Comparator.comparingInt(BlockPos::getY).reversed()
                .thenComparingDouble(p -> eyes.squaredDistanceTo(Vec3d.ofCenter(p))));
        }

        Item useItem = this.findUseItem();
        if (useItem == null) return;

        if (MeteorClient.BLOCK.beginPlacement(pattern, useItem)) {
            for (BlockPos pos : pattern) {
                if (MeteorClient.BLOCK.placeBlock(useItem, pos)) {
                    this.lastPlacedThisTick.add(pos);
                }
            }
            MeteorClient.BLOCK.endPlacement();
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (!this.render.get()) return;

        if (this.lastPredictedCenter != null) {
            event.renderer.box(Box.of(this.lastPredictedCenter, 0.1D, 0.1D, 0.1D),
                this.predictColor.get(), this.predictColor.get(), ShapeMode.Both, 0);
        }

        if (!this.lastPlacedThisTick.isEmpty()) {
            for (BlockPos pos : this.lastPlacedThisTick) {
                event.renderer.box(pos, this.placementColor.get(), this.placementColor.get(), this.shapeMode.get(), 0);
            }
        }
    }

    public String getInfoString() {
        return this.target == null ? null : String.format("%s [%.1f]", EntityUtils.getName(this.target), this.lastPredictedDistance);
    }

    private boolean isHoldingMace(PlayerEntity p) {
        return this.isMace(p.getMainHandStack()) || this.isMace(p.getOffHandStack());
    }

    private boolean isMace(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Item item = stack.getItem();
        if (item == Items.MACE) return true;
        if (item instanceof MaceItem) return true;
        if (item == Items.WIND_CHARGE) return true;

        Identifier id;
        if (item instanceof AxeItem) {
            id = Registries.ITEM.getId(item);
            if (id != null && ("netherite_axe".equals(id.getPath()) || "minecraft:netherite_axe".equals(id.toString()))) {
                return true;
            }
        }

        id = Registries.ITEM.getId(item);
        return id != null && ("mace".equals(id.getPath()) || "minecraft:mace".equals(id.toString()));
    }

    private Item findUseItem() {
        Item targetItem = switch (this.material.get()) {
            case Obsidian -> Items.OBSIDIAN;
            case Cobweb -> Items.COBWEB;
        };
        FindItemResult result = InvUtils.findInHotbar(targetItem);
        return result.found() ? targetItem : null;
    }

    private Vec3d simulateFallFrom(Vec3d pos, Vec3d velPerTick, int ticks) {
        Vec3d p = pos;
        Vec3d v = velPerTick;

        for (int i = 0; i < ticks; ++i) {
            p = p.add(v);
            double vy = v.y + GRAVITY;
            double vx = v.x * DRAG_Y_FLY;
            vy *= DRAG_Y_FLY;
            double vz = v.z * DRAG_Y_FLY;
            if (vy < TERMINAL_VELOCITY) {
                vy = TERMINAL_VELOCITY;
            }
            v = new Vec3d(vx, vy, vz);
        }

        return p;
    }

    private Vec3d simulateElytraFuturePos(PlayerEntity player, int ticks) {
        Vec3d pos = player.getPos();
        Vec3d vel = player.getVelocity();
        float pitchRad = (float) Math.toRadians(player.getPitch());
        Vec3d look = player.getRotationVector();
        double cos = Math.cos(pitchRad);

        for (int i = 0; i < ticks; ++i) {
            pos = pos.add(vel);
            double horizSpeed = Math.hypot(vel.x, vel.z);
            double len = vel.length();
            double liftFactor = cos * cos * Math.min(1.0D, len / 0.4D);
            double vy = vel.y + ELYTRA_GRAVITY + liftFactor * 0.06D;
            if (vel.y < 0.0D && horizSpeed > 0.0D) {
                vy += -0.1D * vel.y * liftFactor;
            }

            Vec3d vAfterLift = new Vec3d(vel.x, vy, vel.z);
            Vec3d align = new Vec3d(
                look.x * LOOK_PUSH + (look.x * ALIGN_D - vAfterLift.x) * ALIGN_E,
                look.y * LOOK_PUSH + (look.y * ALIGN_D - vAfterLift.y) * ALIGN_E,
                look.z * LOOK_PUSH + (look.z * ALIGN_D - vAfterLift.z) * ALIGN_E);
            Vec3d vAligned = vAfterLift.add(align);
            vel = new Vec3d(vAligned.x * DRAG_XZ_FLY, vAligned.y * DRAG_Y_FLY, vAligned.z * DRAG_XZ_FLY);
        }

        return pos;
    }

    private List<BlockPos> buildAttackAnglePattern(BlockPos centerPos, Vec3d futurePos) {
        Vec3d playerCenter = this.mc.player.getPos().add(0.0D, this.mc.player.getHeight() / 2.0D, 0.0D);
        Vec3d toAttacker = futurePos.subtract(playerCenter);
        double horizLen = Math.hypot(toAttacker.x, toAttacker.z);
        double pitchDeg = Math.toDegrees(Math.atan2(Math.abs(toAttacker.y), Math.max(horizLen, 1.0E-4D)));

        List<BlockPos> p = new ArrayList<>(7);
        // The block directly above the head is always part of the shield.
        p.add(centerPos);

        if (pitchDeg >= this.steepAngleThreshold.get()) {
            // Near-vertical mace dive — build a tight ceiling cap with side fillers.
            p.add(centerPos.up());
            p.add(centerPos.north());
            p.add(centerPos.south());
            p.add(centerPos.east());
            p.add(centerPos.west());
        } else {
            // Angled dive / mace aura — wedge into the attacker's direction.
            Direction primary = this.dominantHorizontal(toAttacker.x, toAttacker.z);
            Direction secondary = this.secondaryHorizontal(toAttacker.x, toAttacker.z, primary);

            // Cap the ceiling so a sweep from above is still blocked.
            p.add(centerPos.up());

            // Wall in the direction the attack is coming from.
            BlockPos side = centerPos.offset(primary);
            p.add(side);
            p.add(side.up());

            // Diagonal cover when the attack vector sits between two cardinals.
            if (secondary != null && secondary != primary) {
                p.add(centerPos.offset(secondary));
            }
        }

        return p;
    }

    private Direction dominantHorizontal(double dx, double dz) {
        if (Math.abs(dx) >= Math.abs(dz)) {
            return dx >= 0.0D ? Direction.EAST : Direction.WEST;
        }
        return dz >= 0.0D ? Direction.SOUTH : Direction.NORTH;
    }

    private Direction secondaryHorizontal(double dx, double dz, Direction primary) {
        double ax = Math.abs(dx);
        double az = Math.abs(dz);
        // Only return a secondary direction when the vector is meaningfully diagonal.
        double ratio = Math.min(ax, az) / Math.max(Math.max(ax, az), 1.0E-4D);
        if (ratio < 0.45D) return null;
        if (primary == Direction.EAST || primary == Direction.WEST) {
            return dz >= 0.0D ? Direction.SOUTH : Direction.NORTH;
        }
        return dx >= 0.0D ? Direction.EAST : Direction.WEST;
    }

    private List<BlockPos> orderForCascade(List<BlockPos> pattern) {
        List<BlockPos> remaining = new ArrayList<>(pattern);
        List<BlockPos> ordered = new ArrayList<>(pattern.size());
        Set<BlockPos> planned = new HashSet<>();

        while (!remaining.isEmpty()) {
            BlockPos best = null;
            int bestScore = -1;
            for (BlockPos pos : remaining) {
                int score = this.supportScore(pos, planned);
                if (score > bestScore) {
                    bestScore = score;
                    best = pos;
                }
            }
            ordered.add(best);
            planned.add(best);
            remaining.remove(best);
        }
        return ordered;
    }

    private int supportScore(BlockPos pos, Set<BlockPos> planned) {
        int score = 0;
        for (Direction d : Direction.values()) {
            BlockPos n = pos.offset(d);
            if (!this.mc.world.getBlockState(n).isAir()) {
                score += 3;
            } else if (planned.contains(n)) {
                score += 2;
            }
        }
        return score;
    }

    private Vec3d simulateBoostedElytraFuturePos(PlayerEntity player, int ticks) {
        Vec3d pos = player.getPos();
        Vec3d vel = player.getVelocity();
        double BOOST_DRAG = 0.991D;

        for (int i = 0; i < ticks; ++i) {
            pos = pos.add(vel);
            double vy = vel.y;
            if (vy < TERMINAL_VELOCITY) {
                vy = TERMINAL_VELOCITY;
            }
            vel = new Vec3d(vel.x * BOOST_DRAG, vy, vel.z * BOOST_DRAG);
        }

        return pos;
    }
}
