package meteordevelopment.meteorclient.systems.modules.combat;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.managers.TargetManager;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.player.ChestSwap;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.SlotUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.mutable.MutableDouble;

import java.util.List;

public class MaceAura extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private boolean isMace(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;

        // 1. Native check (1.21+ clients)
        if (stack.getItem() instanceof net.minecraft.item.MaceItem) return true;

        // 2. Registry check (when mappings are correct)
        try {
            if (stack.isOf(net.minecraft.item.Items.MACE)) return true;
        } catch (Throwable ignored) {}

        // 3. Registry ID fallback (covers weird IDs like modid:1.21(mace))
        try {
            String id = net.minecraft.registry.Registries.ITEM
                .getId(stack.getItem())
                .toString()
                .toLowerCase();

            if (id.contains("mace")) return true;
        } catch (Throwable ignored) {}

        // 4. Display name fallback (ViaVersion / renamed items)
        String name = stack.getName().getString().toLowerCase().trim();
        return name.contains("mace");
    }

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("Attack range.")
        .defaultValue(3.0)
        .min(1.0)
        .sliderMax(6.0)
        .build());

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate")
        .description("Rotate to face the target before attacking.")
        .defaultValue(true)
        .build());

    private final Setting<Boolean> snapRotation = sgGeneral.add(new BoolSetting.Builder()
        .name("snap-rotate")
        .description("Instantly rotate to target when in range.")
        .defaultValue(true)
        .visible(rotate::get)
        .build());

    private final Setting<Boolean> silentSwapOverrideDelay = sgGeneral.add(new BoolSetting.Builder()
        .name("silent-swap-override-delay")
        .description("Use held-item delay when silent swapping to mace.")
        .defaultValue(true)
        .build());

    private final Setting<Boolean> chestSwapOnApproach = sgGeneral.add(new BoolSetting.Builder()
        .name("chest-swap-on-approach")
        .description("If wearing elytra, swap to chestplate when a target is within range (not hit range).")
        .defaultValue(true)
        .build());

    private final Setting<Double> swapRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("swap-range")
        .description("Range to trigger chestplate swap when wearing elytra.")
        .defaultValue(6.0)
        .min(1.0)
        .sliderMax(12.0)
        .visible(chestSwapOnApproach::get)
        .build());

    private final Setting<Boolean> breachSwap = sgGeneral.add(new BoolSetting.Builder()
        .name("breach-swap")
        .description("Hit with a breach mace, swap to sword and hit, then swap back and hit again.")
        .defaultValue(false)
        .build());

    private final Setting<Boolean> flipFlop = sgGeneral.add(new BoolSetting.Builder()
        .name("flip-flop")
        .description("Alternate between breach and density maces after each successful hit.")
        .defaultValue(false)
        .build());

    private static final RegistryKey<Enchantment> BREACH_KEY = Enchantments.BREACH;

    private long lastSwordAttackTime = 0L;
    private int flipFlopCount = 0;
    private boolean useDensityNext = false;
    private SwapState swapState = SwapState.NONE;

    private final TargetManager targetManager = new TargetManager(this, true);

    private final SettingGroup sgGrim = settings.createGroup("2b2t / Grim");

    private final Setting<Boolean> grimSafe = sgGrim.add(new BoolSetting.Builder()
        .name("grim-safe")
        .description("Clamp rotation and avoid sketchy behavior for Grim v3.")
        .defaultValue(true)
        .build());

    private final Setting<Boolean> enablePrediction = sgGrim.add(new BoolSetting.Builder()
        .name("enable-prediction")
        .description("Predict target movement when aiming.")
        .defaultValue(true)
        .build());

    private final Setting<Boolean> adjustForFlying = sgGrim.add(new BoolSetting.Builder()
        .name("adjust-for-flying")
        .description("Adjust range and prediction for flying targets.")
        .defaultValue(true)
        .build());

    private final Setting<Double> maxRotStep = sgGrim.add(new DoubleSetting.Builder()
        .name("max-rot-step")
        .description("Max degrees per tick to rotate when grim-safe.")
        .defaultValue(35.0)
        .min(5.0)
        .sliderMax(90.0)
        .visible(grimSafe::get)
        .build());

    private final Setting<Double> predictionMs = sgGrim.add(new DoubleSetting.Builder()
        .name("prediction-ms")
        .description("Lead prediction in milliseconds.")
        .defaultValue(120.0)
        .min(0.0)
        .sliderMax(300.0)
        .visible(enablePrediction::get)
        .build());

    private final Setting<Double> elytraPredictionScale = sgGrim.add(new DoubleSetting.Builder()
        .name("elytra-predict-scale")
        .description("Extra prediction scale when target is flying.")
        .defaultValue(1.4)
        .min(1.0)
        .sliderMax(2.5)
        .visible(() -> enablePrediction.get() && adjustForFlying.get())
        .build());

    private final Setting<Double> flyingRangeBonus = sgGrim.add(new DoubleSetting.Builder()
        .name("flying-range-bonus")
        .description("Extra acquisition range when target is flying.")
        .defaultValue(0.3)
        .min(0.0)
        .sliderMax(1.0)
        .visible(adjustForFlying::get)
        .build());

    private final Setting<Double> aimYOffset = sgGrim.add(new DoubleSetting.Builder()
        .name("aim-y-offset")
        .description("Vertical aim offset for flying targets.")
        .defaultValue(-0.2)
        .min(-1.0)
        .sliderMax(1.0)
        .visible(() -> enablePrediction.get() && adjustForFlying.get())
        .build());

    public MaceAura() {
        super(Categories.Combat, "mace-aura", "Automatically attacks targets with a mace using vanilla delays.");
    }

    @Override
    public String getInfoString() {
        return flipFlop.get() && flipFlopCount > 0 ? "FF:" + flipFlopCount : null;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.player.isSpectator() || !mc.player.isAlive()) return;
        if (mc.player.isUsingItem()) return;

        FindItemResult weapon = findWeapon();
        if (!weapon.found()) return;

        List<Entity> targets = targetManager.getEntityTargets();
        if (targets.isEmpty()) return;

        Entity best = null;
        double bestDist = Double.MAX_VALUE;
        Vec3d eyes = mc.player.getEyePos();

        for (Entity e : targets) {
            double acqRange = range.get();
            if (adjustForFlying.get() && isEntityFlying(e)) {
                acqRange += flyingRangeBonus.get();
            }
            double d = closestPointOnBox(e.getBoundingBox(), eyes).distanceTo(eyes);
            if (d <= acqRange && d < bestDist) {
                best = e;
                bestDist = d;
            }
        }

        if (best == null) return;

        if (chestSwapOnApproach.get() && mc.player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA)) {
            double swapDist = closestPointOnBox(best.getBoundingBox(), eyes).distanceTo(eyes);
            if (swapDist <= swapRange.get()) {
                ChestSwap chestSwap = Modules.get().get(ChestSwap.class);
                if (chestSwap != null) {
                    PlayerUtils.silentSwapEquipChestplate();
                }
            }
        }

        if (rotate.get()) {
            Vec3d point = enablePrediction.get() ? predictedAimPoint(best, eyes) : closestPointOnBox(best.getBoundingBox(), eyes);
            if (grimSafe.get()) {
                float[] tgt = MeteorClient.ROTATION.getRotation(point);
                float curYaw = mc.player.getYaw();
                float curPitch = mc.player.getPitch();
                float nextYaw = clampAngle(curYaw, tgt[0], maxRotStep.get().floatValue());
                float nextPitch = clampAngle(curPitch, tgt[1], maxRotStep.get().floatValue());
                MeteorClient.ROTATION.requestRotation(nextYaw, nextPitch, 9.0);
            } else {
                if (snapRotation.get()) {
                    MeteorClient.ROTATION.snapAt(point);
                }
                MeteorClient.ROTATION.requestRotation(point, 9.0);
            }

            if (!MeteorClient.ROTATION.lookingAt(best.getBoundingBox())) return;
        }

        if (breachSwap.get()) {
            handleBreachSwap(best);
        } else if (flipFlop.get()) {
            FindItemResult densityMace = findWeaponWithEnchant(Enchantments.DENSITY);
            FindItemResult breachMace = findWeaponWithEnchant(BREACH_KEY);
            FindItemResult chosen = useDensityNext ? densityMace : breachMace;
            if (!chosen.found()) {
                chosen = useDensityNext ? breachMace : densityMace;
            }
            if (!chosen.found()) {
                chosen = weapon;
            }

            int delayCheckSlot = chosen.slot();
            if (silentSwapOverrideDelay.get()) {
                delayCheckSlot = mc.player.getInventory().selectedSlot;
            }

            if (delayReady(delayCheckSlot)) {
                boolean isHolding = chosen.isMainHand();
                if (MeteorClient.SWAP.beginSwap(chosen, true)) {
                    attack(best, !isHolding);
                    MeteorClient.SWAP.endSwap(true);
                    useDensityNext = !useDensityNext;
                    flipFlopCount++;
                }
            }
        } else {
            int delayCheckSlot = weapon.slot();
            if (silentSwapOverrideDelay.get()) {
                delayCheckSlot = mc.player.getInventory().selectedSlot;
            }

            if (delayReady(delayCheckSlot)) {
                boolean isHolding = weapon.isMainHand();
                if (MeteorClient.SWAP.beginSwap(weapon, true)) {
                    attack(best, !isHolding);
                    MeteorClient.SWAP.endSwap(true);
                }
            }
        }
    }

    private boolean delayReady(int slotForCooldown) {
        return mc.player.getAttackCooldownProgress(0.0F) >= 1.0F;
    }

    private void attack(Entity target, boolean didSwap) {
        mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(target, mc.player.isSneaking()));
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    private void handleBreachSwap(Entity target) {
        if (swapState == SwapState.NONE) {
            FindItemResult breachMace = findWeaponWithEnchant(BREACH_KEY);
            if (breachMace.found()) {
                int delayCheckSlot = breachMace.slot();
                if (silentSwapOverrideDelay.get()) {
                    delayCheckSlot = mc.player.getInventory().selectedSlot;
                }
                if (delayReady(delayCheckSlot)) {
                    boolean isHolding = breachMace.isMainHand();
                    if (MeteorClient.SWAP.beginSwap(breachMace, true)) {
                        attack(target, !isHolding);
                        MeteorClient.SWAP.endSwap(true);
                        swapState = SwapState.SWORD_PENDING;
                    }
                }
            }
        } else if (swapState == SwapState.SWORD_PENDING) {
            FindItemResult sword = findSword();
            if (!sword.found()) {
                swapState = SwapState.NONE;
            } else if (swordDelayReady(sword.slot())) {
                boolean isHolding = sword.isMainHand();
                if (MeteorClient.SWAP.beginSwap(sword, true)) {
                    mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(target, mc.player.isSneaking()));
                    mc.player.swingHand(Hand.MAIN_HAND);
                    lastSwordAttackTime = System.currentTimeMillis();
                    MeteorClient.SWAP.endSwap(true);
                    swapState = SwapState.MACE2_PENDING;
                }
            }
        } else if (swapState == SwapState.MACE2_PENDING) {
            FindItemResult breachMace = findWeaponWithEnchant(BREACH_KEY);
            if (!breachMace.found()) {
                swapState = SwapState.NONE;
                return;
            }

            int delayCheckSlot = breachMace.slot();
            if (silentSwapOverrideDelay.get()) {
                delayCheckSlot = mc.player.getInventory().selectedSlot;
            }

            if (!delayReady(delayCheckSlot)) return;

            boolean isHolding = breachMace.isMainHand();
            if (MeteorClient.SWAP.beginSwap(breachMace, true)) {
                attack(target, !isHolding);
                MeteorClient.SWAP.endSwap(true);
                swapState = SwapState.NONE;
            }
        }
    }

    private boolean swordDelayReady(int slot) {
        ItemStack itemStack = mc.player.getInventory().getStack(slot);
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
        long currentTime = System.currentTimeMillis();
        return (double)(currentTime - lastSwordAttackTime) / 50.0 > attackCooldownTicks;
    }

    private FindItemResult findWeaponWithEnchant(RegistryKey<Enchantment> enchantKey) {
        for (int slot = 0; slot < 36; slot++) {
            ItemStack stack = mc.player.getInventory().getStack(slot);
            if (isMace(stack) && Utils.getEnchantmentLevel(stack, enchantKey) > 0) {
                return new FindItemResult(slot, stack.getCount());
            }
        }

        ItemStack off = mc.player.getOffHandStack();
        if (isMace(off) && Utils.getEnchantmentLevel(off, enchantKey) > 0) {
            return new FindItemResult(SlotUtils.OFFHAND, off.getCount());
        }

        return new FindItemResult(-1, 0);
    }

    private FindItemResult findWeapon() {
        for (int slot = 0; slot < 36; slot++) {
            ItemStack stack = mc.player.getInventory().getStack(slot);
            if (isMace(stack)) {
                return new FindItemResult(slot, stack.getCount());
            }
        }

        ItemStack off = mc.player.getOffHandStack();
        if (isMace(off)) {
            return new FindItemResult(SlotUtils.OFFHAND, off.getCount());
        }

        return new FindItemResult(-1, 0);
    }

    private FindItemResult findSword() {
        FindItemResult res = MeteorClient.SWAP.getSlot(Items.NETHERITE_SWORD);
        if (!res.found()) {
            res = MeteorClient.SWAP.getSlot(Items.DIAMOND_SWORD);
        }
        return res;
    }

    private static Vec3d closestPointOnBox(Box box, Vec3d point) {
        double x = Math.max(box.minX, Math.min(point.x, box.maxX));
        double y = Math.max(box.minY, Math.min(point.y, box.maxY));
        double z = Math.max(box.minZ, Math.min(point.z, box.maxZ));
        return new Vec3d(x, y, z);
    }

    private boolean isEntityFlying(Entity e) {
        return e.getVelocity().y < -0.1 || e.getVelocity().y > 0.1 || (double) e.fallDistance > 1.5;
    }

    private Vec3d predictedAimPoint(Entity target, Vec3d eyes) {
        Vec3d base = closestPointOnBox(target.getBoundingBox(), eyes);
        double ms = predictionMs.get();
        Vec3d vel = target.getVelocity();
        if (adjustForFlying.get() && isEntityFlying(target)) {
            vel = vel.multiply(elytraPredictionScale.get());
        }
        Vec3d lead = vel.multiply(ms / 1000.0);
        return base.add(lead.x, lead.y + aimYOffset.get(), lead.z);
    }

    private float clampAngle(float cur, float target, float maxStep) {
        float diff = wrapDegrees(target - cur);
        return Math.abs(diff) <= maxStep ? target : cur + Math.copySign(maxStep, diff);
    }

    private float wrapDegrees(float f) {
        f %= 360.0F;
        if (f >= 180.0F) f -= 360.0F;
        if (f < -180.0F) f += 360.0F;
        return f;
    }

    private enum SwapState {
        NONE,
        SWORD_PENDING,
        MACE2_PENDING
    }
}
