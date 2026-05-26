package meteordevelopment.meteorclient.systems.modules.render;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1657;
import net.minecraft.class_243;
import net.minecraft.class_2663;
import net.minecraft.class_746;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/PopChams.class */
public class PopChams extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Boolean> onlyOne;
    private final Setting<Double> renderTime;
    private final Setting<Double> yModifier;
    private final Setting<Double> scaleModifier;
    private final Setting<Boolean> fadeOut;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    private final List<GhostPlayer> ghosts;

    public PopChams() {
        super(Categories.Render, "pop-chams", "Renders a ghost where players pop totem.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.onlyOne = this.sgGeneral.add(new BoolSetting.Builder().name("only-one").description("Only allow one ghost per player.").defaultValue(false).build());
        this.renderTime = this.sgGeneral.add(new DoubleSetting.Builder().name("render-time").description("How long the ghost is rendered in seconds.").defaultValue(1.0d).min(0.1d).sliderMax(6.0d).build());
        this.yModifier = this.sgGeneral.add(new DoubleSetting.Builder().name("y-modifier").description("How much should the Y position of the ghost change per second.").defaultValue(0.75d).sliderRange(-4.0d, 4.0d).build());
        this.scaleModifier = this.sgGeneral.add(new DoubleSetting.Builder().name("scale-modifier").description("How much should the scale of the ghost change per second.").defaultValue(-0.25d).sliderRange(-4.0d, 4.0d).build());
        this.fadeOut = this.sgGeneral.add(new BoolSetting.Builder().name("fade-out").description("Fades out the color.").defaultValue(true).build());
        this.shapeMode = this.sgGeneral.add(new EnumSetting.Builder().name("shape-mode").description("How the shapes are rendered.").defaultValue(ShapeMode.Both).build());
        this.sideColor = this.sgGeneral.add(new ColorSetting.Builder().name("side-color").description("The side color.").defaultValue(new SettingColor(255, 255, 255, 25)).build());
        this.lineColor = this.sgGeneral.add(new ColorSetting.Builder().name("line-color").description("The line color.").defaultValue(new SettingColor(255, 255, 255, 60)).build());
        this.ghosts = new ArrayList();
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onDeactivate() {
        synchronized (this.ghosts) {
            this.ghosts.clear();
        }
    }

    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        class_2663 class_2663Var = event.packet;
        if (class_2663Var instanceof class_2663) {
            class_2663 p = class_2663Var;
            if (p.method_11470() != 35) {
                return;
            }
            class_746 class_746VarMethod_11469 = p.method_11469(this.mc.field_1687);
            if (class_746VarMethod_11469 instanceof class_1657) {
                class_1657 player = (class_1657) class_746VarMethod_11469;
                if (class_746VarMethod_11469 == this.mc.field_1724) {
                    return;
                }
                synchronized (this.ghosts) {
                    if (this.onlyOne.get().booleanValue()) {
                        this.ghosts.removeIf(ghostPlayer -> {
                            return ghostPlayer.uuid.equals(class_746VarMethod_11469.method_5667());
                        });
                    }
                    this.ghosts.add(new GhostPlayer(player));
                }
            }
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        synchronized (this.ghosts) {
            this.ghosts.removeIf(ghostPlayer -> {
                return ghostPlayer.render(event);
            });
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/PopChams$GhostPlayer.class */
    private class GhostPlayer {
        private final UUID uuid;
        private double timer;
        private class_1657 player;
        private double scale = 1.0d;
        private class_243 pos = new class_243(0.0d, 0.0d, 0.0d);

        public GhostPlayer(class_1657 player) {
            this.uuid = player.method_5667();
            this.player = player;
        }

        public boolean render(Render3DEvent event) {
            this.timer += event.frameTime;
            if (this.timer > PopChams.this.renderTime.get().doubleValue()) {
                return true;
            }
            this.pos.meteor$setY(this.pos.field_1351 + (PopChams.this.yModifier.get().doubleValue() * event.frameTime));
            this.scale += PopChams.this.scaleModifier.get().doubleValue() * event.frameTime;
            int preSideA = PopChams.this.sideColor.get().a;
            int preLineA = PopChams.this.lineColor.get().a;
            if (PopChams.this.fadeOut.get().booleanValue()) {
                SettingColor settingColor = PopChams.this.sideColor.get();
                settingColor.a = (int) (((double) settingColor.a) * (1.0d - (this.timer / PopChams.this.renderTime.get().doubleValue())));
                SettingColor settingColor2 = PopChams.this.lineColor.get();
                settingColor2.a = (int) (((double) settingColor2.a) * (1.0d - (this.timer / PopChams.this.renderTime.get().doubleValue())));
            }
            PopChams.this.sideColor.get().a = preSideA;
            PopChams.this.lineColor.get().a = preLineA;
            return false;
        }
    }
}
