package meteordevelopment.meteorclient.systems.modules.combat;

import java.util.List;
import java.util.function.Predicate;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.player.SilentMine;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1657;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2626;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/AntiDigDown.class */
public class AntiDigDown extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgRender;
    private final Setting<List<class_2248>> blocks;
    private final Setting<Integer> range;
    private final Setting<SortPriority> priority;
    private final Setting<Boolean> pauseEat;
    private final Setting<Boolean> render;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    private class_1657 target;

    public AntiDigDown() {
        super(Categories.Combat, "anti-dig-down", "Places blocks directly below other players to stop them from digging down.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgRender = this.settings.createGroup("Render");
        this.blocks = this.sgGeneral.add(new BlockListSetting.Builder().name("whitelist").description("Which blocks to use.").defaultValue(class_2246.field_10540, class_2246.field_22108).build());
        this.range = this.sgGeneral.add(new IntSetting.Builder().name("target-range").description("The range players can be targeted.").defaultValue(4).build());
        this.priority = this.sgGeneral.add(new EnumSetting.Builder().name("target-priority").description("How to select the player to target.").defaultValue(SortPriority.LowestHealth).build());
        this.pauseEat = this.sgGeneral.add(new BoolSetting.Builder().name("pause-eat").description("Pauses while eating.").defaultValue(true).build());
        this.render = this.sgRender.add(new BoolSetting.Builder().name("render").description("Renders an overlay where blocks will be placed.").defaultValue(true).build());
        this.shapeMode = this.sgRender.add(new EnumSetting.Builder().name("shape-mode").description("How the shapes are rendered.").defaultValue(ShapeMode.Both).build());
        this.sideColor = this.sgRender.add(new ColorSetting.Builder().name("side-color").description("The side color of the target block rendering.").defaultValue(new SettingColor(Opcode.MULTIANEWARRAY, Opcode.L2F, 232, 10)).build());
        this.lineColor = this.sgRender.add(new ColorSetting.Builder().name("line-color").description("The line color of the target block rendering.").defaultValue(new SettingColor(Opcode.MULTIANEWARRAY, Opcode.L2F, 232)).build());
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        this.target = null;
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onDeactivate() {
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.target == null || TargetUtils.isBadTarget(this.target, this.range.get().intValue())) {
            this.target = TargetUtils.getPlayerTarget(this.range.get().intValue(), this.priority.get());
            if (TargetUtils.isBadTarget(this.target, this.range.get().intValue())) {
            }
        }
    }

    private class_1792 findUseItem() {
        FindItemResult result = InvUtils.findInHotbar((Predicate<class_1799>) itemStack -> {
            for (class_2248 blocks : this.blocks.get()) {
                if (blocks.method_8389() == itemStack.method_7909()) {
                    return true;
                }
            }
            return false;
        });
        if (!result.found()) {
            return null;
        }
        return this.mc.field_1724.method_31548().method_5438(result.slot()).method_7909();
    }

    private class_2338 getBelowBlockPos() {
        return this.target.method_24515().method_10074();
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        class_1792 useItem;
        class_2338 belowPos;
        class_2626 class_2626Var = event.packet;
        if (class_2626Var instanceof class_2626) {
            class_2626 packet = class_2626Var;
            if (this.target == null) {
                return;
            }
            if ((this.pauseEat.get().booleanValue() && this.mc.field_1724.method_6115()) || (useItem = findUseItem()) == null || (belowPos = getBelowBlockPos()) == null) {
                return;
            }
            SilentMine silentMine = (SilentMine) Modules.get().get(SilentMine.class);
            if (silentMine.getDelayedDestroyBlockPos() == null || !belowPos.equals(silentMine.getDelayedDestroyBlockPos())) {
                if ((silentMine.getRebreakBlockPos() != null && belowPos.equals(silentMine.getRebreakBlockPos())) || !packet.method_11309().equals(belowPos) || !packet.method_11308().method_26215() || !MeteorClient.BLOCK.beginPlacement(belowPos, packet.method_11308(), useItem)) {
                    return;
                }
                MeteorClient.BLOCK.placeBlock(useItem, belowPos, packet.method_11308());
                MeteorClient.BLOCK.endPlacement();
            }
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        class_2338 pos;
        if (!this.render.get().booleanValue() || this.target == null || (pos = getBelowBlockPos()) == null) {
            return;
        }
        event.renderer.box(pos, this.sideColor.get(), this.lineColor.get(), this.shapeMode.get(), 0);
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public String getInfoString() {
        return EntityUtils.getName(this.target);
    }
}
