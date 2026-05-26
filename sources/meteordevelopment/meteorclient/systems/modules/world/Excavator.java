package meteordevelopment.meteorclient.systems.modules.world;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.utils.BetterBlockPos;
import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.meteorclient.events.meteor.MouseButtonEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_3965;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/world/Excavator.class */
public class Excavator extends Module {
    private final IBaritone baritone;
    private final SettingGroup sgGeneral;
    private final SettingGroup sgRendering;
    private final Setting<Keybind> selectionBind;
    private final Setting<Boolean> logSelection;
    private final Setting<Boolean> keepActive;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    private Status status;
    private BetterBlockPos start;
    private BetterBlockPos end;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/world/Excavator$Status.class */
    private enum Status {
        SEL_START,
        SEL_END,
        WORKING
    }

    public Excavator() {
        super(Categories.World, "excavator", "Excavate a selection area.");
        this.baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgRendering = this.settings.createGroup("Rendering");
        this.selectionBind = this.sgGeneral.add(new KeybindSetting.Builder().name("selection-bind").description("Bind to draw selection.").defaultValue(Keybind.fromButton(1)).build());
        this.logSelection = this.sgGeneral.add(new BoolSetting.Builder().name("log-selection").description("Logs the selection coordinates to the chat.").defaultValue(true).build());
        this.keepActive = this.sgGeneral.add(new BoolSetting.Builder().name("keep-active").description("Keep the module active after finishing the excavation.").defaultValue(false).build());
        this.shapeMode = this.sgRendering.add(new EnumSetting.Builder().name("shape-mode").description("How the shapes are rendered.").defaultValue(ShapeMode.Both).build());
        this.sideColor = this.sgRendering.add(new ColorSetting.Builder().name("side-color").description("The side color.").defaultValue(new SettingColor(255, 255, 255, 50)).build());
        this.lineColor = this.sgRendering.add(new ColorSetting.Builder().name("line-color").description("The line color.").defaultValue(new SettingColor(255, 255, 255, 255)).build());
        this.status = Status.SEL_START;
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onDeactivate() {
        this.baritone.getSelectionManager().removeSelection(this.baritone.getSelectionManager().getLastSelection());
        if (this.baritone.getBuilderProcess().isActive()) {
            this.baritone.getCommandManager().execute("stop");
        }
        this.status = Status.SEL_START;
    }

    @EventHandler
    private void onMouseButton(MouseButtonEvent event) {
        if (event.action != KeyAction.Press || !this.selectionBind.get().isPressed() || this.mc.field_1755 != null) {
            return;
        }
        selectCorners();
    }

    @EventHandler
    private void onKey(KeyEvent event) {
        if (event.action != KeyAction.Press || !this.selectionBind.get().isPressed() || this.mc.field_1755 != null) {
            return;
        }
        selectCorners();
    }

    private void selectCorners() {
        class_3965 class_3965Var = this.mc.field_1765;
        if (class_3965Var instanceof class_3965) {
            class_3965 result = class_3965Var;
            if (this.status == Status.SEL_START) {
                this.start = BetterBlockPos.from(result.method_17777());
                this.status = Status.SEL_END;
                if (this.logSelection.get().booleanValue()) {
                    info("Start corner set: (%d, %d, %d)".formatted(Integer.valueOf(this.start.method_10263()), Integer.valueOf(this.start.method_10264()), Integer.valueOf(this.start.method_10260())), new Object[0]);
                    return;
                }
                return;
            }
            if (this.status == Status.SEL_END) {
                this.end = BetterBlockPos.from(result.method_17777());
                this.status = Status.WORKING;
                if (this.logSelection.get().booleanValue()) {
                    info("End corner set: (%d, %d, %d)".formatted(Integer.valueOf(this.end.method_10263()), Integer.valueOf(this.end.method_10264()), Integer.valueOf(this.end.method_10260())), new Object[0]);
                }
                this.baritone.getSelectionManager().addSelection(this.start, this.end);
                this.baritone.getBuilderProcess().clearArea(this.start, this.end);
            }
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (this.status == Status.SEL_START || this.status == Status.SEL_END) {
            class_3965 class_3965Var = this.mc.field_1765;
            if (class_3965Var instanceof class_3965) {
                class_3965 result = class_3965Var;
                event.renderer.box(result.method_17777(), this.sideColor.get(), this.lineColor.get(), this.shapeMode.get(), 0);
                return;
            }
            return;
        }
        if (this.status == Status.WORKING && !this.baritone.getBuilderProcess().isActive()) {
            if (this.keepActive.get().booleanValue()) {
                this.baritone.getSelectionManager().removeSelection(this.baritone.getSelectionManager().getLastSelection());
                this.status = Status.SEL_START;
            } else {
                toggle();
            }
        }
    }
}
