package meteordevelopment.meteorclient.systems.modules.render;

import meteordevelopment.meteorclient.events.entity.DamageEvent;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.meteorclient.events.meteor.MouseButtonEvent;
import meteordevelopment.meteorclient.events.meteor.MouseScrollEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.ChunkOcclusionEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.GUIMove;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.input.Input;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_3966;
import net.minecraft.class_5498;
import net.minecraft.class_5892;
import org.joml.Vector3d;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/Freecam.class */
public class Freecam extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Double> speed;
    private final Setting<Double> speedScrollSensitivity;
    private final Setting<Boolean> toggleOnDamage;
    private final Setting<Boolean> toggleOnDeath;
    private final Setting<Boolean> toggleOnLog;
    private final Setting<Boolean> reloadChunks;
    private final Setting<Boolean> renderHands;
    private final Setting<Boolean> rotate;
    private final Setting<Boolean> staticView;
    public final Vector3d pos;
    public final Vector3d prevPos;
    private class_5498 perspective;
    private double speedValue;
    public float yaw;
    public float pitch;
    public float prevYaw;
    public float prevPitch;
    private double fovScale;
    private boolean bobView;
    private boolean forward;
    private boolean backward;
    private boolean right;
    private boolean left;
    private boolean up;
    private boolean down;

    public Freecam() {
        super(Categories.Render, "freecam", "Allows the camera to move away from the player.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.speed = this.sgGeneral.add(new DoubleSetting.Builder().name("speed").description("Your speed while in freecam.").onChanged(aDouble -> {
            this.speedValue = aDouble.doubleValue();
        }).defaultValue(1.0d).min(0.0d).build());
        this.speedScrollSensitivity = this.sgGeneral.add(new DoubleSetting.Builder().name("speed-scroll-sensitivity").description("Allows you to change speed value using scroll wheel. 0 to disable.").defaultValue(0.0d).min(0.0d).sliderMax(2.0d).build());
        this.toggleOnDamage = this.sgGeneral.add(new BoolSetting.Builder().name("toggle-on-damage").description("Disables freecam when you take damage.").defaultValue(false).build());
        this.toggleOnDeath = this.sgGeneral.add(new BoolSetting.Builder().name("toggle-on-death").description("Disables freecam when you die.").defaultValue(false).build());
        this.toggleOnLog = this.sgGeneral.add(new BoolSetting.Builder().name("toggle-on-log").description("Disables freecam when you disconnect from a server.").defaultValue(true).build());
        this.reloadChunks = this.sgGeneral.add(new BoolSetting.Builder().name("reload-chunks").description("Disables cave culling.").defaultValue(true).build());
        this.renderHands = this.sgGeneral.add(new BoolSetting.Builder().name("show-hands").description("Whether or not to render your hands in freecam.").defaultValue(true).build());
        this.rotate = this.sgGeneral.add(new BoolSetting.Builder().name("rotate").description("Rotates to the block or entity you are looking at.").defaultValue(false).build());
        this.staticView = this.sgGeneral.add(new BoolSetting.Builder().name("static").description("Disables settings that move the view.").defaultValue(true).build());
        this.pos = new Vector3d();
        this.prevPos = new Vector3d();
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        this.fovScale = ((Double) this.mc.field_1690.method_42454().method_41753()).doubleValue();
        this.bobView = ((Boolean) this.mc.field_1690.method_42448().method_41753()).booleanValue();
        if (this.staticView.get().booleanValue()) {
            this.mc.field_1690.method_42454().method_41748(Double.valueOf(0.0d));
            this.mc.field_1690.method_42448().method_41748(false);
        }
        this.yaw = this.mc.field_1724.method_36454();
        this.pitch = this.mc.field_1724.method_36455();
        this.perspective = this.mc.field_1690.method_31044();
        this.speedValue = this.speed.get().doubleValue();
        Utils.set(this.pos, this.mc.field_1773.method_19418().method_19326());
        Utils.set(this.prevPos, this.mc.field_1773.method_19418().method_19326());
        if (this.mc.field_1690.method_31044() == class_5498.field_26666) {
            this.yaw += 180.0f;
            this.pitch *= -1.0f;
        }
        this.prevYaw = this.yaw;
        this.prevPitch = this.pitch;
        this.forward = this.mc.field_1690.field_1894.method_1434();
        this.backward = this.mc.field_1690.field_1881.method_1434();
        this.right = this.mc.field_1690.field_1849.method_1434();
        this.left = this.mc.field_1690.field_1913.method_1434();
        this.up = this.mc.field_1690.field_1903.method_1434();
        this.down = this.mc.field_1690.field_1832.method_1434();
        unpress();
        if (this.reloadChunks.get().booleanValue()) {
            this.mc.field_1769.method_3279();
        }
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onDeactivate() {
        if (this.reloadChunks.get().booleanValue()) {
            this.mc.field_1769.method_3279();
        }
        this.mc.field_1690.method_31043(this.perspective);
        if (this.staticView.get().booleanValue()) {
            this.mc.field_1690.method_42454().method_41748(Double.valueOf(this.fovScale));
            this.mc.field_1690.method_42448().method_41748(Boolean.valueOf(this.bobView));
        }
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        unpress();
        this.prevPos.set(this.pos);
        this.prevYaw = this.yaw;
        this.prevPitch = this.pitch;
    }

    private void unpress() {
        this.mc.field_1690.field_1894.method_23481(false);
        this.mc.field_1690.field_1881.method_23481(false);
        this.mc.field_1690.field_1849.method_23481(false);
        this.mc.field_1690.field_1913.method_23481(false);
        this.mc.field_1690.field_1903.method_23481(false);
        this.mc.field_1690.field_1832.method_23481(false);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (this.mc.field_1719.method_5757()) {
            this.mc.method_1560().field_5960 = true;
        }
        if (!this.perspective.method_31034()) {
            this.mc.field_1690.method_31043(class_5498.field_26664);
        }
        class_243 forward = class_243.method_1030(0.0f, this.yaw);
        class_243 right = class_243.method_1030(0.0f, this.yaw + 90.0f);
        double velX = 0.0d;
        double velY = 0.0d;
        double velZ = 0.0d;
        if (this.rotate.get().booleanValue()) {
            if (this.mc.field_1765 instanceof class_3966) {
                class_2338 crossHairPos = this.mc.field_1765.method_17782().method_24515();
                Rotations.rotate(Rotations.getYaw(crossHairPos), Rotations.getPitch(crossHairPos), 0, null);
            } else {
                class_243 crossHairPosition = this.mc.field_1765.method_17784();
                if (!this.mc.field_1687.method_8320(this.mc.field_1765.method_17777()).method_26215()) {
                    Rotations.rotate(Rotations.getYaw(crossHairPosition), Rotations.getPitch(crossHairPosition), 0, null);
                }
            }
        }
        double s = 0.5d;
        if (this.mc.field_1690.field_1867.method_1434()) {
            s = 1.0d;
        }
        boolean a = false;
        if (this.forward) {
            velX = 0.0d + (forward.field_1352 * s * this.speedValue);
            velZ = 0.0d + (forward.field_1350 * s * this.speedValue);
            a = true;
        }
        if (this.backward) {
            velX -= (forward.field_1352 * s) * this.speedValue;
            velZ -= (forward.field_1350 * s) * this.speedValue;
            a = true;
        }
        boolean b = false;
        if (this.right) {
            velX += right.field_1352 * s * this.speedValue;
            velZ += right.field_1350 * s * this.speedValue;
            b = true;
        }
        if (this.left) {
            velX -= (right.field_1352 * s) * this.speedValue;
            velZ -= (right.field_1350 * s) * this.speedValue;
            b = true;
        }
        if (a && b) {
            double diagonal = 1.0d / Math.sqrt(2.0d);
            velX *= diagonal;
            velZ *= diagonal;
        }
        if (this.up) {
            velY = 0.0d + (s * this.speedValue);
        }
        if (this.down) {
            velY -= s * this.speedValue;
        }
        this.prevPos.set(this.pos);
        this.pos.set(this.pos.x + velX, this.pos.y + velY, this.pos.z + velZ);
    }

    @EventHandler
    public void onKey(KeyEvent event) {
        if (Input.isKeyPressed(292) || checkGuiMove()) {
            return;
        }
        boolean cancel = true;
        if (this.mc.field_1690.field_1894.method_1417(event.key, 0)) {
            this.forward = event.action != KeyAction.Release;
            this.mc.field_1690.field_1894.method_23481(false);
        } else if (this.mc.field_1690.field_1881.method_1417(event.key, 0)) {
            this.backward = event.action != KeyAction.Release;
            this.mc.field_1690.field_1881.method_23481(false);
        } else if (this.mc.field_1690.field_1849.method_1417(event.key, 0)) {
            this.right = event.action != KeyAction.Release;
            this.mc.field_1690.field_1849.method_23481(false);
        } else if (this.mc.field_1690.field_1913.method_1417(event.key, 0)) {
            this.left = event.action != KeyAction.Release;
            this.mc.field_1690.field_1913.method_23481(false);
        } else if (this.mc.field_1690.field_1903.method_1417(event.key, 0)) {
            this.up = event.action != KeyAction.Release;
            this.mc.field_1690.field_1903.method_23481(false);
        } else if (this.mc.field_1690.field_1832.method_1417(event.key, 0)) {
            this.down = event.action != KeyAction.Release;
            this.mc.field_1690.field_1832.method_23481(false);
        } else {
            cancel = false;
        }
        if (cancel) {
            event.cancel();
        }
    }

    @EventHandler
    private void onMouseButton(MouseButtonEvent event) {
        if (checkGuiMove()) {
            return;
        }
        boolean cancel = true;
        if (this.mc.field_1690.field_1894.method_1433(event.button)) {
            this.forward = event.action != KeyAction.Release;
            this.mc.field_1690.field_1894.method_23481(false);
        } else if (this.mc.field_1690.field_1881.method_1433(event.button)) {
            this.backward = event.action != KeyAction.Release;
            this.mc.field_1690.field_1881.method_23481(false);
        } else if (this.mc.field_1690.field_1849.method_1433(event.button)) {
            this.right = event.action != KeyAction.Release;
            this.mc.field_1690.field_1849.method_23481(false);
        } else if (this.mc.field_1690.field_1913.method_1433(event.button)) {
            this.left = event.action != KeyAction.Release;
            this.mc.field_1690.field_1913.method_23481(false);
        } else if (this.mc.field_1690.field_1903.method_1433(event.button)) {
            this.up = event.action != KeyAction.Release;
            this.mc.field_1690.field_1903.method_23481(false);
        } else if (this.mc.field_1690.field_1832.method_1433(event.button)) {
            this.down = event.action != KeyAction.Release;
            this.mc.field_1690.field_1832.method_23481(false);
        } else {
            cancel = false;
        }
        if (cancel) {
            event.cancel();
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onMouseScroll(MouseScrollEvent event) {
        if (this.speedScrollSensitivity.get().doubleValue() > 0.0d && this.mc.field_1755 == null) {
            this.speedValue += event.value * 0.25d * this.speedScrollSensitivity.get().doubleValue() * this.speedValue;
            if (this.speedValue < 0.1d) {
                this.speedValue = 0.1d;
            }
            event.cancel();
        }
    }

    @EventHandler
    private void onChunkOcclusion(ChunkOcclusionEvent event) {
        event.cancel();
    }

    @EventHandler
    private void onDamage(DamageEvent event) {
        if (event.entity.method_5667() != null && event.entity.method_5667().equals(this.mc.field_1724.method_5667()) && this.toggleOnDamage.get().booleanValue()) {
            toggle();
            info("Toggled off because you took damage.", new Object[0]);
        }
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        if (this.toggleOnLog.get().booleanValue()) {
            toggle();
        }
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        class_5892 class_5892Var = event.packet;
        if (class_5892Var instanceof class_5892) {
            class_5892 packet = class_5892Var;
            if (this.mc.field_1687.method_8469(packet.comp_2275()) == this.mc.field_1724 && this.toggleOnDeath.get().booleanValue()) {
                toggle();
                info("Toggled off because you died.", new Object[0]);
            }
        }
    }

    private boolean checkGuiMove() {
        GUIMove guiMove = (GUIMove) Modules.get().get(GUIMove.class);
        if (this.mc.field_1755 == null || guiMove.isActive()) {
            return this.mc.field_1755 != null && guiMove.isActive() && guiMove.skip();
        }
        return true;
    }

    public void changeLookDirection(double deltaX, double deltaY) {
        this.prevYaw = this.yaw;
        this.prevPitch = this.pitch;
        this.yaw = (float) (((double) this.yaw) + deltaX);
        this.pitch = (float) (((double) this.pitch) + deltaY);
        this.pitch = class_3532.method_15363(this.pitch, -90.0f, 90.0f);
    }

    public boolean renderHands() {
        return !isActive() || this.renderHands.get().booleanValue();
    }

    public double getX(float tickDelta) {
        return class_3532.method_16436(tickDelta, this.prevPos.x, this.pos.x);
    }

    public double getY(float tickDelta) {
        return class_3532.method_16436(tickDelta, this.prevPos.y, this.pos.y);
    }

    public double getZ(float tickDelta) {
        return class_3532.method_16436(tickDelta, this.prevPos.z, this.pos.z);
    }

    public double getYaw(float tickDelta) {
        return class_3532.method_16439(tickDelta, this.prevYaw, this.yaw);
    }

    public double getPitch(float tickDelta) {
        return class_3532.method_16439(tickDelta, this.prevPitch, this.pitch);
    }

    public boolean staySneaking() {
        return false;
    }
}
