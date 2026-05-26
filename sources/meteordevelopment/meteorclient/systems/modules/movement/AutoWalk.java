package meteordevelopment.meteorclient.systems.modules.movement;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.pathing.NopPathManager;
import meteordevelopment.meteorclient.pathing.PathManagers;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.input.Input;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_304;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/movement/AutoWalk.class */
public class AutoWalk extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Mode> mode;
    private final Setting<Direction> direction;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/movement/AutoWalk$Direction.class */
    public enum Direction {
        Forwards,
        Backwards,
        Left,
        Right
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/movement/AutoWalk$Mode.class */
    public enum Mode {
        Simple,
        Smart
    }

    public AutoWalk() {
        super(Categories.Movement, "auto-walk", "Automatically walks forward.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.mode = this.sgGeneral.add(new EnumSetting.Builder().name("mode").description("Walking mode.").defaultValue(Mode.Smart).onChanged(mode1 -> {
            if (isActive()) {
                if (mode1 == Mode.Simple) {
                    PathManagers.get().stop();
                } else {
                    createGoal();
                }
                unpress();
            }
        }).build());
        this.direction = this.sgGeneral.add(new EnumSetting.Builder().name("simple-direction").description("The direction to walk in simple mode.").defaultValue(Direction.Forwards).onChanged(direction1 -> {
            if (isActive()) {
                unpress();
            }
        }).visible(() -> {
            return this.mode.get() == Mode.Simple;
        }).build());
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        if (this.mode.get() == Mode.Smart) {
            createGoal();
        }
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onDeactivate() {
        if (this.mode.get() != Mode.Simple) {
            PathManagers.get().stop();
        } else {
            unpress();
        }
    }

    @EventHandler(priority = 100)
    private void onTick(TickEvent.Pre event) {
        if (this.mode.get() == Mode.Simple) {
            switch (this.direction.get()) {
                case Forwards:
                    setPressed(this.mc.field_1690.field_1894, true);
                    break;
                case Backwards:
                    setPressed(this.mc.field_1690.field_1881, true);
                    break;
                case Left:
                    setPressed(this.mc.field_1690.field_1913, true);
                    break;
                case Right:
                    setPressed(this.mc.field_1690.field_1849, true);
                    break;
            }
        }
        if (PathManagers.get() instanceof NopPathManager) {
            info("Smart mode requires Baritone", new Object[0]);
            toggle();
        }
    }

    private void unpress() {
        setPressed(this.mc.field_1690.field_1894, false);
        setPressed(this.mc.field_1690.field_1881, false);
        setPressed(this.mc.field_1690.field_1913, false);
        setPressed(this.mc.field_1690.field_1849, false);
    }

    private void setPressed(class_304 key, boolean pressed) {
        key.method_23481(pressed);
        Input.setKeyState(key, pressed);
    }

    private void createGoal() {
        PathManagers.get().moveInDirection(this.mc.field_1724.method_36454());
    }
}
