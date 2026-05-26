package meteordevelopment.meteorclient.systems.modules.player;

import meteordevelopment.meteorclient.events.entity.player.StartBreakingBlockEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1268;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2846;
import net.minecraft.class_2879;
import net.minecraft.class_9334;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/player/InstantRebreak.class */
public class InstantRebreak extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgRender;
    private final Setting<Integer> tickDelay;
    private final Setting<Boolean> pick;
    private final Setting<Boolean> rotate;
    private final Setting<Boolean> render;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    public final class_2338.class_2339 blockPos;
    private int ticks;
    private class_2350 direction;

    public InstantRebreak() {
        super(Categories.Player, "instant-rebreak", "Instantly re-breaks blocks in the same position.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgRender = this.settings.createGroup("Render");
        this.tickDelay = this.sgGeneral.add(new IntSetting.Builder().name("delay").description("The delay between break attempts.").defaultValue(0).min(0).sliderMax(20).build());
        this.pick = this.sgGeneral.add(new BoolSetting.Builder().name("only-pick").description("Only tries to mine the block if you are holding a pickaxe.").defaultValue(true).build());
        this.rotate = this.sgGeneral.add(new BoolSetting.Builder().name("rotate").description("Faces the block being mined server side.").defaultValue(true).build());
        this.render = this.sgRender.add(new BoolSetting.Builder().name("render").description("Renders an overlay on the block being broken.").defaultValue(true).build());
        this.shapeMode = this.sgRender.add(new EnumSetting.Builder().name("shape-mode").description("How the shapes are rendered.").defaultValue(ShapeMode.Both).build());
        this.sideColor = this.sgRender.add(new ColorSetting.Builder().name("side-color").description("The color of the sides of the blocks being rendered.").defaultValue(new SettingColor(204, 0, 0, 10)).build());
        this.lineColor = this.sgRender.add(new ColorSetting.Builder().name("line-color").description("The color of the lines of the blocks being rendered.").defaultValue(new SettingColor(204, 0, 0, 255)).build());
        this.blockPos = new class_2338.class_2339(0, Integer.MIN_VALUE, 0);
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        this.ticks = 0;
        this.blockPos.method_10103(0, -1, 0);
    }

    @EventHandler
    private void onStartBreakingBlock(StartBreakingBlockEvent event) {
        this.direction = event.direction;
        this.blockPos.method_10101(event.blockPos);
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.ticks >= this.tickDelay.get().intValue()) {
            this.ticks = 0;
            if (shouldMine()) {
                if (this.rotate.get().booleanValue()) {
                    Rotations.rotate(Rotations.getYaw((class_2338) this.blockPos), Rotations.getPitch((class_2338) this.blockPos), this::sendPacket);
                } else {
                    sendPacket();
                }
                this.mc.method_1562().method_52787(new class_2879(class_1268.field_5808));
                return;
            }
            return;
        }
        this.ticks++;
    }

    public void sendPacket() {
        this.mc.method_1562().method_52787(new class_2846(class_2846.class_2847.field_12971, this.blockPos, this.direction == null ? class_2350.field_11036 : this.direction));
    }

    public boolean shouldMine() {
        if (this.mc.field_1687.method_31606(this.blockPos) || !BlockUtils.canBreak(this.blockPos)) {
            return false;
        }
        return !this.pick.get().booleanValue() || this.mc.field_1724.method_6047().method_57826(class_9334.field_50077);
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (this.render.get().booleanValue() && shouldMine()) {
            event.renderer.box((class_2338) this.blockPos, (Color) this.sideColor.get(), (Color) this.lineColor.get(), this.shapeMode.get(), 0);
        }
    }
}
