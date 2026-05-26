package meteordevelopment.meteorclient.systems.modules.movement;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.player.PlayerTickMovementEvent;
import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.mixin.CreativeInventoryScreenAccessor;
import meteordevelopment.meteorclient.mixin.KeyBindingAccessor;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.input.Input;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_304;
import net.minecraft.class_3532;
import net.minecraft.class_3675;
import net.minecraft.class_408;
import net.minecraft.class_463;
import net.minecraft.class_471;
import net.minecraft.class_481;
import net.minecraft.class_497;
import net.minecraft.class_498;
import net.minecraft.class_7706;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/movement/GUIMove.class */
public class GUIMove extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Screens> screens;
    public final Setting<Boolean> jump;
    public final Setting<Boolean> sneak;
    public final Setting<Boolean> sprint;
    private final Setting<Boolean> arrowsRotate;
    private final Setting<Double> rotateSpeed;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/movement/GUIMove$Screens.class */
    public enum Screens {
        GUI,
        Inventory,
        Both
    }

    public GUIMove() {
        super(Categories.Movement, "gui-move", "Allows you to perform various actions while in GUIs.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.screens = this.sgGeneral.add(new EnumSetting.Builder().name("guis").description("Which GUIs to move in.").defaultValue(Screens.Inventory).build());
        this.jump = this.sgGeneral.add(new BoolSetting.Builder().name("jump").description("Allows you to jump while in GUIs.").defaultValue(true).onChanged(aBoolean -> {
            if (!isActive() || aBoolean.booleanValue()) {
                return;
            }
            set(this.mc.field_1690.field_1903, false);
        }).build());
        this.sneak = this.sgGeneral.add(new BoolSetting.Builder().name("sneak").description("Allows you to sneak while in GUIs.").defaultValue(true).onChanged(aBoolean2 -> {
            if (!isActive() || aBoolean2.booleanValue()) {
                return;
            }
            set(this.mc.field_1690.field_1832, false);
        }).build());
        this.sprint = this.sgGeneral.add(new BoolSetting.Builder().name("sprint").description("Allows you to sprint while in GUIs.").defaultValue(true).onChanged(aBoolean3 -> {
            if (!isActive() || aBoolean3.booleanValue()) {
                return;
            }
            set(this.mc.field_1690.field_1867, false);
        }).build());
        this.arrowsRotate = this.sgGeneral.add(new BoolSetting.Builder().name("arrows-rotate").description("Allows you to use your arrow keys to rotate while in GUIs.").defaultValue(true).build());
        this.rotateSpeed = this.sgGeneral.add(new DoubleSetting.Builder().name("rotate-speed").description("Rotation speed while in GUIs.").defaultValue(4.0d).min(0.0d).build());
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onDeactivate() {
        set(this.mc.field_1690.field_1894, false);
        set(this.mc.field_1690.field_1881, false);
        set(this.mc.field_1690.field_1913, false);
        set(this.mc.field_1690.field_1849, false);
        if (this.jump.get().booleanValue()) {
            set(this.mc.field_1690.field_1903, false);
        }
        if (this.sneak.get().booleanValue()) {
            set(this.mc.field_1690.field_1832, false);
        }
        if (this.sprint.get().booleanValue()) {
            set(this.mc.field_1690.field_1867, false);
        }
    }

    public boolean disableSpace() {
        return isActive() && this.jump.get().booleanValue() && this.mc.field_1690.field_1903.method_1427();
    }

    public boolean disableArrows() {
        return isActive() && this.arrowsRotate.get().booleanValue();
    }

    @EventHandler
    private void onPlayerMoveEvent(PlayerTickMovementEvent event) {
        if (skip()) {
            return;
        }
        if (this.screens.get() != Screens.GUI || (this.mc.field_1755 instanceof WidgetScreen)) {
            if (this.screens.get() == Screens.Inventory && (this.mc.field_1755 instanceof WidgetScreen)) {
                return;
            }
            set(this.mc.field_1690.field_1894, Input.isPressed(this.mc.field_1690.field_1894));
            set(this.mc.field_1690.field_1881, Input.isPressed(this.mc.field_1690.field_1881));
            set(this.mc.field_1690.field_1913, Input.isPressed(this.mc.field_1690.field_1913));
            set(this.mc.field_1690.field_1849, Input.isPressed(this.mc.field_1690.field_1849));
            if (this.jump.get().booleanValue()) {
                set(this.mc.field_1690.field_1903, Input.isPressed(this.mc.field_1690.field_1903));
            }
            if (this.sneak.get().booleanValue()) {
                set(this.mc.field_1690.field_1832, Input.isPressed(this.mc.field_1690.field_1832));
            }
            if (this.sprint.get().booleanValue()) {
                set(this.mc.field_1690.field_1867, Input.isPressed(this.mc.field_1690.field_1867));
            }
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (skip()) {
            return;
        }
        if (this.screens.get() != Screens.GUI || (this.mc.field_1755 instanceof WidgetScreen)) {
            if (this.screens.get() == Screens.Inventory && (this.mc.field_1755 instanceof WidgetScreen)) {
                return;
            }
            float rotationDelta = Math.min((float) (this.rotateSpeed.get().doubleValue() * event.frameTime * 20.0d), 100.0f);
            if (this.arrowsRotate.get().booleanValue()) {
                float yaw = this.mc.field_1724.method_36454();
                float pitch = this.mc.field_1724.method_36455();
                if (Input.isKeyPressed(263)) {
                    yaw -= rotationDelta;
                }
                if (Input.isKeyPressed(262)) {
                    yaw += rotationDelta;
                }
                if (Input.isKeyPressed(265)) {
                    pitch -= rotationDelta;
                }
                if (Input.isKeyPressed(264)) {
                    pitch += rotationDelta;
                }
                float pitch2 = class_3532.method_15363(pitch, -90.0f, 90.0f);
                this.mc.field_1724.method_36456(yaw);
                this.mc.field_1724.method_36457(pitch2);
            }
        }
    }

    private void set(class_304 bind, boolean pressed) {
        boolean wasPressed = bind.method_1434();
        bind.method_23481(pressed);
        class_3675.class_306 key = ((KeyBindingAccessor) bind).getKey();
        if (wasPressed != pressed && key.method_1442() == class_3675.class_307.field_1668) {
            MeteorClient.EVENT_BUS.post(KeyEvent.get(key.method_1444(), 0, pressed ? KeyAction.Press : KeyAction.Release));
        }
    }

    public boolean skip() {
        return this.mc.field_1755 == null || ((this.mc.field_1755 instanceof class_481) && CreativeInventoryScreenAccessor.getSelectedTab() == class_7706.method_47344()) || (this.mc.field_1755 instanceof class_408) || (this.mc.field_1755 instanceof class_498) || (this.mc.field_1755 instanceof class_471) || (this.mc.field_1755 instanceof class_463) || (this.mc.field_1755 instanceof class_497);
    }
}
