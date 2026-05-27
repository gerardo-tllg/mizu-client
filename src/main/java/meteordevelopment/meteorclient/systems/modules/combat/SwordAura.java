package meteordevelopment.meteorclient.systems.modules.combat;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.managers.RotationManager;
import meteordevelopment.meteorclient.systems.managers.SwapManager;
import meteordevelopment.meteorclient.systems.managers.TargetManager;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.TickRate;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.mutable.MutableDouble;

import java.util.List;

public class SwordAura extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<Boolean> silentSwapOverrideDelay = sgGeneral.add(new BoolSetting.Builder()
        .name("silent-swap-override-delay")
        .description("Whether or not to use the held items delay when attacking with silent swap")
        .defaultValue(true)
        .visible(() -> MeteorClient.SWAP.getItemSwapMode() != SwapManager.SwapMode.None)
        .build());

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate")
        .description("Whether or not to rotate to the entity to attack it.")
        .defaultValue(true)
        .build());

    private final Setting<Boolean> snapRotation = sgGeneral.add(new BoolSetting.Builder()
        .name("snap-rotate")
        .description("Instantly rotates to the targeted entity.")
        .defaultValue(true)
        .visible(() -> rotate.get())
        .build());

    private final Setting<Boolean> swordPull = sgGeneral.add(new BoolSetting.Builder()
        .name("sword-pull")
        .description("Pulls the target towards you")
        .defaultValue(true)
        .build());

    private final Setting<Boolean> tpsSync = sgGeneral.add(new BoolSetting.Builder()
        .name("tps-sync")
        .description("Adjusts attack speed to match the server's TPS.")
        .defaultValue(true)
        .build());

    private final Setting<Boolean> forcePauseEat = sgGeneral.add(new BoolSetting.Builder()
        .name("force-pause-on-eat")
        .description("Does not attack while using an item.")
        .defaultValue(false)
        .build());

    private final Setting<Boolean> pauseInAir = sgGeneral.add(new BoolSetting.Builder()
        .name("pause-in-air")
        .description("Does not attack while rising during a jump.")
        .defaultValue(false)
        .build());

    private final Setting<Boolean> pauseInventoryOepn = sgGeneral.add(new BoolSetting.Builder()
        .name("pause-on-inventory")
        .description("Does not attack when the inventory is open. Disabling this may cause unhappiness.")
        .defaultValue(true)
        .build());

    private final Setting<CritMode> critMode = sgGeneral.add(new EnumSetting.Builder<CritMode>()
        .name("crit-mode")
        .description("The mode to use for critical hits.")
        .defaultValue(CritMode.None)
        .build());

    private final Setting<Boolean> awaitJumpCrit = sgGeneral.add(new BoolSetting.Builder()
        .name("await-jump-crit")
        .description("Waits until you are falling after a jump to perform a vanilla critical hit.")
        .defaultValue(true)
        .build());

    private final Setting<Boolean> wallCritsPauseOnMove = sgGeneral.add(new BoolSetting.Builder()
        .name("wall-crits-pause-on-move")
        .description("Only for Old modes. Pauses crits when moving. (Redundant, packet crits only work when still).")
        .defaultValue(true)
        .visible(() -> critMode.get() == CritMode.OldWall || critMode.get() == CritMode.OldAlways)
        .build());

    private final Setting<Boolean> wallCritsOnlyOnSword = sgGeneral.add(new BoolSetting.Builder()
        .name("wall-crits-only-on-sword")
        .description("Only for Old modes. Only crits when you swapped to the sword.")
        .defaultValue(true)
        .visible(() -> critMode.get() == CritMode.OldWall || critMode.get() == CritMode.OldAlways)
        .build());

    private final Setting<Boolean> onlyCritWhenFullyPhased = sgGeneral.add(new BoolSetting.Builder()
        .name("only-crit-when-fully-phased")
        .description("Only perform OldWall crits if your head is also phased (fully phased).")
        .defaultValue(true)
        .visible(() -> critMode.get() == CritMode.OldWall)
        .build());

    private final Setting<Boolean> render = sgRender.add(new BoolSetting.Builder()
        .name("render")
        .description("Whether or not to render attacks")
        .defaultValue(false)
        .build());

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("How the shapes are rendered.")
        .defaultValue(ShapeMode.Both)
        .visible(() -> render.get())
        .build());

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("side-color")
        .description("The side color of the rendering.")
        .defaultValue(new SettingColor(160, 0, 225, 35))
        .visible(() -> shapeMode.get().sides())
        .build());

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("line-color")
        .description("The line color of the rendering.")
        .defaultValue(new SettingColor(255, 255, 255, 50))
        .visible(() -> render.get() && shapeMode.get().lines())
        .build());

    private final Setting<Double> fadeTime = sgRender.add(new DoubleSetting.Builder()
        .name("fade-time")
        .description("How long to fade the bounding box render.")
        .min(0)
        .sliderMax(2.0)
        .defaultValue(0.8)
        .build());

    private final TargetManager targetManager = new TargetManager(this, true);

    private long lastAttackTime = 0;
    private List<Entity> targets = null;
    private Entity lastAttackedEntity = null;
    private int targetIndex = 0;

    public SwordAura() {
        super(Categories.Combat, "sword-aura", "Automatically attacks entities with your sword");
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null || mc.player.isDead() || mc.player.isSpectator()) return;

        if (forcePauseEat.get() && mc.player.isUsingItem() && mc.player.getActiveHand() == Hand.MAIN_HAND) return;

        if (pauseInventoryOepn.get() && mc.currentScreen instanceof HandledScreen) return;

        FindItemResult result = MeteorClient.SWAP.getSlot(Items.NETHERITE_SWORD);
        if (!result.found()) {
            result = MeteorClient.SWAP.getSlot(Items.DIAMOND_SWORD);
        }

        if (!result.found()) return;

        targets = targetManager.getEntityTargets();
        if (targets.isEmpty()) return;

        Entity target = targets.get(targetIndex % targets.size());

        int delayCheckSlot = result.slot();
        if (silentSwapOverrideDelay.get()) {
            delayCheckSlot = mc.player.getInventory().selectedSlot;
        }

        if (delayCheck(delayCheckSlot)) {
            boolean isFalling = !mc.player.isOnGround()
                && mc.player.getVelocity().y < -0.1
                && !mc.player.isTouchingWater()
                && !mc.player.isInLava()
                && !mc.player.isSneaking()
                && !mc.player.isClimbing();

            boolean isMoving = mc.player.input.playerInput.forward()
                || mc.player.input.playerInput.backward()
                || mc.player.input.playerInput.left()
                || mc.player.input.playerInput.right();

            boolean isStandingOnGround = mc.player.isOnGround()
                && !isMoving
                && !mc.player.isTouchingWater()
                && !mc.player.isInLava();

            CritMode currentCritMode = critMode.get();
            boolean awaitingCrit = awaitJumpCrit.get();

            if (pauseInAir.get() && !mc.player.isOnGround()) return;

            if (awaitingCrit && !mc.player.isOnGround() && !isFalling && !mc.player.isSneaking()) return;

            boolean sendPackets = isStandingOnGround && currentCritMode != CritMode.None;

            if (rotate.get() || !mc.player.isSneaking()) {
                Vec3d point = getClosestPointOnBox(target.getBoundingBox(), mc.player.getEyePos());

                if (snapRotation.get() || !mc.player.isSneaking()) {
                    MeteorClient.ROTATION.snapAt(point);
                }

                MeteorClient.ROTATION.requestRotation(point, 9.0);

                if (!MeteorClient.ROTATION.lookingAt(target.getBoundingBox())) return;
            }

            boolean isHolding = result.isMainHand();

            if (MeteorClient.SWAP.beginSwap(result, true)) {
                attack(target, !isHolding, sendPackets);
                MeteorClient.SWAP.endSwap(true);
            }
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (!render.get() || lastAttackedEntity == null || mc.world == null || mc.player == null) return;

        double secondsSinceAttack = (System.currentTimeMillis() - lastAttackTime) / 1000.0;
        if (secondsSinceAttack > fadeTime.get()) return;

        double alpha = 1.0 - secondsSinceAttack / fadeTime.get();

        double x = MathHelper.lerp(event.tickDelta, lastAttackedEntity.lastRenderX, lastAttackedEntity.getX()) - lastAttackedEntity.getX();
        double y = MathHelper.lerp(event.tickDelta, lastAttackedEntity.lastRenderY, lastAttackedEntity.getY()) - lastAttackedEntity.getY();
        double z = MathHelper.lerp(event.tickDelta, lastAttackedEntity.lastRenderZ, lastAttackedEntity.getZ()) - lastAttackedEntity.getZ();

        Box box = lastAttackedEntity.getBoundingBox();

        event.renderer.box(
            x + box.minX, y + box.minY, z + box.minZ,
            x + box.maxX, y + box.maxY, z + box.maxZ,
            sideColor.get().copy().a((int) (sideColor.get().a * alpha)),
            lineColor.get().copy().a((int) (lineColor.get().a * alpha)),
            shapeMode.get(), 0
        );
    }

    public void attack(Entity target, boolean didSwap, boolean sendPackets) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        ClientPlayNetworkHandler net = mc.getNetworkHandler();

        Vec3d pos;
        float legitYaw;
        float legitPitch;
        if (RotationManager.lastGround) {
            pos = new Vec3d(MeteorClient.ROTATION.lastX, MeteorClient.ROTATION.lastY, MeteorClient.ROTATION.lastZ);
            legitYaw = MeteorClient.ROTATION.lastYaw;
            legitPitch = MeteorClient.ROTATION.lastPitch;
        } else {
            pos = mc.player.getPos();
            legitYaw = MeteorClient.ROTATION.rotationYaw;
            legitPitch = MeteorClient.ROTATION.rotationPitch;
        }

        boolean willSendCrits = false;
        boolean pulling;
        if (sendPackets) {
            pulling = mc.player.input.playerInput.forward()
                || mc.player.input.playerInput.backward()
                || mc.player.input.playerInput.left()
                || mc.player.input.playerInput.right();

            if ((!wallCritsPauseOnMove.get() || !pulling) && (!wallCritsOnlyOnSword.get() || !didSwap)) {
                switch (critMode.get()) {
                    case OldWall:
                        if (PlayerUtils.isPlayerPhased() && (!onlyCritWhenFullyPhased.get() || isHeadPhased())) {
                            willSendCrits = true;
                        }
                        break;
                    case OldAlways:
                        willSendCrits = true;
                        break;
                    default:
                        break;
                }
            }
        }

        pulling = swordPull.get();
        if (willSendCrits) pulling = false;

        if (pulling) {
            float spoofYaw = (legitYaw + 180.0f) % 360.0f;
            net.sendPacket(new PlayerMoveC2SPacket.Full(pos.x, pos.y, pos.z, legitYaw, legitPitch, mc.player.isOnGround(), mc.player.horizontalCollision));
            net.sendPacket(new PlayerMoveC2SPacket.Full(pos.x, pos.y, pos.z, spoofYaw, legitPitch, mc.player.isOnGround(), mc.player.horizontalCollision));
        } else if (willSendCrits) {
            sendCrits(didSwap);
        }

        net.sendPacket(PlayerInteractEntityC2SPacket.attack(target, mc.player.isSneaking()));
        mc.player.swingHand(Hand.MAIN_HAND);
        lastAttackedEntity = target;
        lastAttackTime = System.currentTimeMillis();
        targetIndex++;
    }

    private boolean delayCheck(int slot) {
        if (mc.player == null) return false;

        PlayerInventory inventory = mc.player.getInventory();
        ItemStack itemStack = inventory.getStack(slot);

        MutableDouble attackSpeed = new MutableDouble(mc.player.getAttributeBaseValue(EntityAttributes.ATTACK_SPEED));

        AttributeModifiersComponent attributeModifiers = itemStack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        if (attributeModifiers != null) {
            attributeModifiers.applyModifiers(EquipmentSlot.MAINHAND, (entry, modifier) -> {
                if (entry == EntityAttributes.ATTACK_SPEED) {
                    attackSpeed.add(modifier.value());
                }
            });
        }

        double attackCooldownTicks = 1.0 / attackSpeed.getValue() * 20.0;

        if (tpsSync.get()) {
            double tps = TickRate.INSTANCE.getTickRate() * 0.95;
            if (tps < 19.5) {
                double tpsFactor = tps / 20.0;
                attackCooldownTicks /= tpsFactor;
            }
        }

        long currentTime = System.currentTimeMillis();
        return (currentTime - lastAttackTime) / 50.0 > attackCooldownTicks;
    }

    private void sendCrits(boolean didSwap) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        ClientPlayNetworkHandler net = mc.getNetworkHandler();

        Vec3d pos;
        float packetYaw;
        float packetPitch;
        if (RotationManager.lastGround) {
            pos = new Vec3d(MeteorClient.ROTATION.lastX, MeteorClient.ROTATION.lastY, MeteorClient.ROTATION.lastZ);
            packetYaw = MeteorClient.ROTATION.lastYaw;
            packetPitch = MeteorClient.ROTATION.lastPitch;
        } else {
            pos = mc.player.getPos();
            packetYaw = mc.player.getYaw();
            packetPitch = mc.player.getPitch();
        }

        boolean isMoving = mc.player.input.playerInput.forward()
            || mc.player.input.playerInput.backward()
            || mc.player.input.playerInput.left()
            || mc.player.input.playerInput.right();

        if ((!wallCritsPauseOnMove.get() || !isMoving) && (!wallCritsOnlyOnSword.get() || !didSwap)) {
            switch (critMode.get()) {
                case OldWall:
                    if (PlayerUtils.isPlayerPhased() && (!onlyCritWhenFullyPhased.get() || isHeadPhased())) {
                        net.sendPacket(new PlayerMoveC2SPacket.Full(pos.x, pos.y, pos.z, packetYaw, packetPitch, true, mc.player.horizontalCollision));
                        net.sendPacket(new PlayerMoveC2SPacket.Full(pos.x, pos.y + 0.0625, pos.z, packetYaw, packetPitch, false, mc.player.horizontalCollision));
                        net.sendPacket(new PlayerMoveC2SPacket.Full(pos.x, pos.y + 0.045, pos.z, packetYaw, packetPitch, false, mc.player.horizontalCollision));
                    }
                    break;
                case OldAlways:
                    net.sendPacket(new PlayerMoveC2SPacket.Full(pos.x, pos.y, pos.z, packetYaw, packetPitch, true, mc.player.horizontalCollision));
                    net.sendPacket(new PlayerMoveC2SPacket.Full(pos.x, pos.y + 0.0625, pos.z, packetYaw, packetPitch, false, mc.player.horizontalCollision));
                    net.sendPacket(new PlayerMoveC2SPacket.Full(pos.x, pos.y + 0.045, pos.z, packetYaw, packetPitch, false, mc.player.horizontalCollision));
                    break;
                default:
                    break;
            }
        }
    }

    private boolean isHeadPhased() {
        if (mc.player == null || mc.world == null) return false;

        Vec3d eyePos = mc.player.getEyePos();
        BlockPos pos = new BlockPos(MathHelper.floor(eyePos.x), MathHelper.floor(eyePos.y), MathHelper.floor(eyePos.z));
        return !mc.world.getBlockState(pos).isAir();
    }

    public Vec3d getClosestPointOnBox(Box box, Vec3d point) {
        if (mc.player == null) return Vec3d.ZERO;

        double x = Math.max(box.minX, Math.min(point.x, box.maxX));
        double y = Math.max(box.minY, Math.min(point.y, box.maxY));
        double z = Math.max(box.minZ, Math.min(point.z, box.maxZ));
        return new Vec3d(x, y, z);
    }

    public enum CritMode {
        None, OldWall, OldAlways
    }

    public enum SwitchMode {
        None, SilentHotbar, SilentSwap, Auto
    }
}
