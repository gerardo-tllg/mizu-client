package meteordevelopment.meteorclient.systems.modules.combat;

import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IClientWorld;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class PearlPhase extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Keybind> phaseBind;
    private final Setting<Boolean> chatFeedback;
    private final Setting<Double> pitch;
    private final Setting<Boolean> placeBlock;
    private final Setting<Boolean> pauseOnEat;
    private boolean active;
    private boolean keyUnpressed;
    private BlockPos phasePos;

    public PearlPhase() {
        super(Categories.Combat, "pearl-phase", "Phases into walls using pearls");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.phaseBind = this.sgGeneral.add(new KeybindSetting.Builder()
            .name("key-bind")
            .description("Phase on keybind press")
            .build());
        this.chatFeedback = this.sgGeneral.add(new BoolSetting.Builder()
            .name("chat-feedback")
            .description("Sends a colored chat message when the phase key is pressed.")
            .defaultValue(false)
            .build());
        this.pitch = this.sgGeneral.add(new DoubleSetting.Builder()
            .name("pitch")
            .description("The pitch to throw the ender pearl at.")
            .defaultValue(75.0)
            .min(0.0)
            .max(90.0)
            .sliderMin(0.0)
            .sliderMax(90.0)
            .build());
        this.placeBlock = this.sgGeneral.add(new BoolSetting.Builder()
            .name("place-block")
            .description("Places a block at your phase spot.")
            .defaultValue(false)
            .build());
        this.pauseOnEat = this.sgGeneral.add(new BoolSetting.Builder()
            .name("pause-on-eat")
            .description("Prevents placing the optional block while using an item (eating, drinking, etc.).")
            .defaultValue(true)
            .build());
        this.active = false;
        this.keyUnpressed = false;
        this.phasePos = null;
    }

    private void activate() {
        this.active = true;
        if (mc.player != null && mc.world != null) {
            this.update();
        }
    }

    private void deactivate(boolean phased) {
        this.active = false;
        this.phasePos = null;
    }

    private void update() {
        if (mc.player != null && mc.world != null) {
            if (this.active) {
                if (!MeteorClient.SWAP.canSwap(Items.ENDER_PEARL)) {
                    this.deactivate(false);
                } else if (mc.player.getMainHandStack().isOf(Items.ENDER_PEARL)) {
                    this.deactivate(false);
                }
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (this.active && mc.player != null && mc.world != null && mc.player.input != null) {
            float forward = mc.player.forwardSpeed;
            float strafe = mc.player.sidewaysSpeed;
            float currentYaw = mc.player.getYaw();
            float targetYaw = currentYaw;

            if (forward > 0.0F) {
                if (strafe > 0.0F) {
                    targetYaw = currentYaw - 45.0F;
                } else if (strafe < 0.0F) {
                    targetYaw = currentYaw + 45.0F;
                }
            } else if (forward < 0.0F) {
                targetYaw = currentYaw + 180.0F;
                if (strafe > 0.0F) {
                    targetYaw += 45.0F;
                } else if (strafe < 0.0F) {
                    targetYaw -= 45.0F;
                }
            } else if (strafe > 0.0F) {
                targetYaw = currentYaw - 90.0F;
            } else if (strafe < 0.0F) {
                targetYaw = currentYaw + 90.0F;
            }

            targetYaw = MathHelper.wrapDegrees(targetYaw);
            float targetPitch = this.pitch.get().floatValue();
            final float yawFinal = targetYaw;
            final float pitchFinal = targetPitch;
            Rotations.rotate((double) yawFinal, (double) pitchFinal, () -> this.throwPearl(yawFinal, pitchFinal));
        }
    }

    private void throwPearl(float yaw, float pitch) {
        if (mc.world == null || mc.player == null) return;

        if (this.placeBlock.get() && (!this.pauseOnEat.get() || !mc.player.isUsingItem())) {
            BlockPos offset = this.getPlacementDirectionOffset();
            BlockPos supportPos = mc.player.getBlockPos().add(offset);

            if (!(mc.player.getPos().distanceTo(Vec3d.ofCenter(supportPos)) > 1.25) && BlockUtils.canPlace(supportPos)) {
                FindItemResult obsidian = InvUtils.findInHotbar(Items.OBSIDIAN);

                if (obsidian.found()) {
                    if (MeteorClient.BLOCK.beginPlacement(List.of(supportPos), Items.OBSIDIAN)) {
                        InvUtils.swap(obsidian.slot(), true);
                        MeteorClient.BLOCK.placeBlock(Items.OBSIDIAN, supportPos);
                        InvUtils.swapBack();
                        MeteorClient.BLOCK.endPlacement();
                    }
                } else {
                    obsidian = InvUtils.find(Items.OBSIDIAN);
                    if (obsidian.found()) {
                        int obsInvSlot = obsidian.slot();
                        int hotbarSlot = mc.player.getInventory().selectedSlot;
                        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, obsInvSlot, hotbarSlot, SlotActionType.SWAP, mc.player);
                        if (MeteorClient.BLOCK.beginPlacement(List.of(supportPos), Items.OBSIDIAN)) {
                            MeteorClient.BLOCK.placeBlock(Items.OBSIDIAN, supportPos);
                            MeteorClient.BLOCK.endPlacement();
                        }
                        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, obsInvSlot, hotbarSlot, SlotActionType.SWAP, mc.player);
                    }
                }
            }
        }

        FindItemResult pearl = InvUtils.findInHotbar(Items.ENDER_PEARL);
        if (pearl.found()) {
            InvUtils.swap(pearl.slot(), true);
            int sequence = ((IClientWorld) mc.world).meteor$getAndIncrementSequence();
            mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, sequence, yaw, pitch));
            InvUtils.swapBack();
        } else {
            pearl = InvUtils.find(Items.ENDER_PEARL);
            if (!pearl.found()) {
                this.deactivate(false);
                return;
            }

            int pearlInvSlot = pearl.slot();
            int hotbarSlot = mc.player.getInventory().selectedSlot;
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, pearlInvSlot, hotbarSlot, SlotActionType.SWAP, mc.player);
            int sequence = ((IClientWorld) mc.world).meteor$getAndIncrementSequence();
            mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, sequence, yaw, pitch));
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, pearlInvSlot, hotbarSlot, SlotActionType.SWAP, mc.player);
        }

        this.deactivate(true);
    }

    @EventHandler(priority = 200)
    private void onRender(Render3DEvent event) {
        if (!this.phaseBind.get().isPressed()) {
            this.keyUnpressed = true;
        }

        if (this.phaseBind.get().isPressed() && this.keyUnpressed && !(mc.currentScreen instanceof ChatScreen)) {
            if (this.chatFeedback.get()) {
                this.sendChatFeedback();
            }
            this.activate();
            this.keyUnpressed = false;
        }

        this.update();
    }

    private BlockPos getPlacementDirectionOffset() {
        if (mc.player == null) return BlockPos.ORIGIN;

        int yaw = MathHelper.floor((double) (mc.player.getYaw() * 8.0F / 360.0F) + 0.5) & 7;

        return switch (yaw) {
            case 0 -> new BlockPos(0, 0, 1);
            case 1 -> new BlockPos(-1, 0, 1);
            case 2 -> new BlockPos(-1, 0, 0);
            case 3 -> new BlockPos(-1, 0, -1);
            case 4 -> new BlockPos(0, 0, -1);
            case 5 -> new BlockPos(1, 0, -1);
            case 6 -> new BlockPos(1, 0, 0);
            case 7 -> new BlockPos(1, 0, 1);
            default -> BlockPos.ORIGIN;
        };
    }

    private void sendChatFeedback() {
        if (mc.player != null) {
            var prefix = Text.literal("[PearPhase] ").formatted(Formatting.GOLD);
            var body = Text.literal("Phase key pressed.").formatted(Formatting.YELLOW);
            mc.player.sendMessage(prefix.append(body), false);
        }
    }
}
