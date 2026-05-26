package meteordevelopment.meteorclient.systems.modules.render;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javassist.bytecode.Opcode;
import javassist.compiler.TokenId;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ItemListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.ProjectileEntitySimulator;
import meteordevelopment.meteorclient.utils.misc.Pool;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1676;
import net.minecraft.class_1764;
import net.minecraft.class_1771;
import net.minecraft.class_1776;
import net.minecraft.class_1779;
import net.minecraft.class_1787;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1811;
import net.minecraft.class_1823;
import net.minecraft.class_1835;
import net.minecraft.class_1887;
import net.minecraft.class_1893;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_3532;
import net.minecraft.class_3965;
import net.minecraft.class_3966;
import net.minecraft.class_4537;
import net.minecraft.class_5321;
import net.minecraft.class_746;
import net.minecraft.class_7923;
import net.minecraft.class_9239;
import org.joml.Vector3d;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/Trajectories.class */
public class Trajectories extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgRender;
    private final Setting<List<class_1792>> items;
    private final Setting<Boolean> otherPlayers;
    private final Setting<Boolean> firedProjectiles;
    private final Setting<Boolean> accurate;
    public final Setting<Integer> simulationSteps;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    private final Setting<Boolean> renderPositionBox;
    private final Setting<Double> positionBoxSize;
    private final Setting<SettingColor> positionSideColor;
    private final Setting<SettingColor> positionLineColor;
    private final ProjectileEntitySimulator simulator;
    private final Pool<Vector3d> vec3s;
    private final List<Path> paths;
    private static final double MULTISHOT_OFFSET = Math.toRadians(10.0d);

    public Trajectories() {
        super(Categories.Render, "trajectories", "Predicts the trajectory of throwable items.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgRender = this.settings.createGroup("Render");
        this.items = this.sgGeneral.add(new ItemListSetting.Builder().name("items").description("Items to display trajectories for.").defaultValue(getDefaultItems()).filter(this::itemFilter).build());
        this.otherPlayers = this.sgGeneral.add(new BoolSetting.Builder().name("other-players").description("Calculates trajectories for other players.").defaultValue(true).build());
        this.firedProjectiles = this.sgGeneral.add(new BoolSetting.Builder().name("fired-projectiles").description("Calculates trajectories for already fired projectiles.").defaultValue(false).build());
        this.accurate = this.sgGeneral.add(new BoolSetting.Builder().name("accurate").description("Whether or not to calculate more accurate.").defaultValue(false).build());
        this.simulationSteps = this.sgGeneral.add(new IntSetting.Builder().name("simulation-steps").description("How many steps to simulate projectiles. Zero for no limit").defaultValue(Integer.valueOf(TokenId.BadToken)).sliderMax(5000).build());
        this.shapeMode = this.sgRender.add(new EnumSetting.Builder().name("shape-mode").description("How the shapes are rendered.").defaultValue(ShapeMode.Both).build());
        this.sideColor = this.sgRender.add(new ColorSetting.Builder().name("side-color").description("The side color.").defaultValue(new SettingColor(255, Opcode.FCMPG, 0, 35)).build());
        this.lineColor = this.sgRender.add(new ColorSetting.Builder().name("line-color").description("The line color.").defaultValue(new SettingColor(255, Opcode.FCMPG, 0)).build());
        this.renderPositionBox = this.sgRender.add(new BoolSetting.Builder().name("render-position-boxes").description("Renders the actual position the projectile will be at each tick along it's trajectory.").defaultValue(false).build());
        SettingGroup settingGroup = this.sgRender;
        DoubleSetting.Builder builderSliderRange = new DoubleSetting.Builder().name("position-box-size").description("The size of the box drawn at the simulated positions.").defaultValue(0.02d).sliderRange(0.01d, 0.1d);
        Setting<Boolean> setting = this.renderPositionBox;
        Objects.requireNonNull(setting);
        this.positionBoxSize = settingGroup.add(builderSliderRange.visible(setting::get).build());
        SettingGroup settingGroup2 = this.sgRender;
        ColorSetting.Builder builderDefaultValue = new ColorSetting.Builder().name("position-side-color").description("The side color.").defaultValue(new SettingColor(255, Opcode.FCMPG, 0, 35));
        Setting<Boolean> setting2 = this.renderPositionBox;
        Objects.requireNonNull(setting2);
        this.positionSideColor = settingGroup2.add(builderDefaultValue.visible(setting2::get).build());
        SettingGroup settingGroup3 = this.sgRender;
        ColorSetting.Builder builderDefaultValue2 = new ColorSetting.Builder().name("position-line-color").description("The line color.").defaultValue(new SettingColor(255, Opcode.FCMPG, 0));
        Setting<Boolean> setting3 = this.renderPositionBox;
        Objects.requireNonNull(setting3);
        this.positionLineColor = settingGroup3.add(builderDefaultValue2.visible(setting3::get).build());
        this.simulator = new ProjectileEntitySimulator();
        this.vec3s = new Pool<>(Vector3d::new);
        this.paths = new ArrayList();
    }

    private boolean itemFilter(class_1792 item) {
        return (item instanceof class_1811) || (item instanceof class_1787) || (item instanceof class_1835) || (item instanceof class_1823) || (item instanceof class_1771) || (item instanceof class_1776) || (item instanceof class_1779) || (item instanceof class_4537) || (item instanceof class_9239);
    }

    private List<class_1792> getDefaultItems() {
        List<class_1792> items = new ArrayList<>();
        for (class_1792 item : class_7923.field_41178) {
            if (itemFilter(item)) {
                items.add(item);
            }
        }
        return items;
    }

    private Path getEmptyPath() {
        for (Path path : this.paths) {
            if (path.points.isEmpty()) {
                return path;
            }
        }
        Path path2 = new Path();
        this.paths.add(path2);
        return path2;
    }

    private void calculatePath(class_1657 player, float tickDelta) {
        for (Path path : this.paths) {
            path.clear();
        }
        class_1799 itemStack = player.method_6047();
        if (!this.items.get().contains(itemStack.method_7909())) {
            itemStack = player.method_6079();
            if (!this.items.get().contains(itemStack.method_7909())) {
                return;
            }
        }
        if (this.simulator.set(player, itemStack, 0.0d, this.accurate.get().booleanValue(), tickDelta)) {
            getEmptyPath().calculate();
            if ((itemStack.method_7909() instanceof class_1764) && Utils.hasEnchantment(itemStack, (class_5321<class_1887>) class_1893.field_9108) && this.simulator.set(player, itemStack, MULTISHOT_OFFSET, this.accurate.get().booleanValue(), tickDelta)) {
                getEmptyPath().calculate();
                if (this.simulator.set(player, itemStack, -MULTISHOT_OFFSET, this.accurate.get().booleanValue(), tickDelta)) {
                    getEmptyPath().calculate();
                }
            }
        }
    }

    private void calculateFiredPath(class_1297 entity, double tickDelta) {
        for (Path path : this.paths) {
            path.clear();
        }
        if (this.simulator.set(entity, this.accurate.get().booleanValue())) {
            getEmptyPath().setStart(entity, tickDelta).calculate();
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        float tickDelta = this.mc.field_1687.method_54719().method_54754() ? 1.0f : event.tickDelta;
        for (class_746 class_746Var : this.mc.field_1687.method_18456()) {
            if (this.otherPlayers.get().booleanValue() || class_746Var == this.mc.field_1724) {
                calculatePath(class_746Var, tickDelta);
                for (Path path : this.paths) {
                    path.render(event);
                }
            }
        }
        if (this.firedProjectiles.get().booleanValue()) {
            for (class_1297 entity : this.mc.field_1687.method_18112()) {
                if (entity instanceof class_1676) {
                    calculateFiredPath(entity, tickDelta);
                    for (Path path2 : this.paths) {
                        path2.render(event);
                    }
                }
            }
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/Trajectories$Path.class */
    private class Path {
        private final List<Vector3d> points = new ArrayList();
        private boolean hitQuad;
        private boolean hitQuadHorizontal;
        private double hitQuadX1;
        private double hitQuadY1;
        private double hitQuadZ1;
        private double hitQuadX2;
        private double hitQuadY2;
        private double hitQuadZ2;
        private class_1297 collidingEntity;
        public Vector3d lastPoint;

        private Path() {
        }

        public void clear() {
            for (Vector3d point : this.points) {
                Trajectories.this.vec3s.free(point);
            }
            this.points.clear();
            this.hitQuad = false;
            this.collidingEntity = null;
            this.lastPoint = null;
        }

        public void calculate() {
            addPoint();
            int i = 0;
            while (true) {
                if (i < (Trajectories.this.simulationSteps.get().intValue() > 0 ? Trajectories.this.simulationSteps.get().intValue() : Integer.MAX_VALUE)) {
                    class_239 result = Trajectories.this.simulator.tick();
                    if (result != null) {
                        processHitResult(result);
                        return;
                    } else {
                        addPoint();
                        i++;
                    }
                } else {
                    return;
                }
            }
        }

        public Path setStart(class_1297 entity, double tickDelta) {
            this.lastPoint = new Vector3d(class_3532.method_16436(tickDelta, entity.field_6038, entity.method_23317()), class_3532.method_16436(tickDelta, entity.field_5971, entity.method_23318()), class_3532.method_16436(tickDelta, entity.field_5989, entity.method_23321()));
            return this;
        }

        private void addPoint() {
            this.points.add(Trajectories.this.vec3s.get().set(Trajectories.this.simulator.pos));
        }

        private void processHitResult(class_239 result) {
            if (result.method_17783() == class_239.class_240.field_1332) {
                class_3965 r = (class_3965) result;
                this.hitQuad = true;
                this.hitQuadX1 = r.method_17784().field_1352;
                this.hitQuadY1 = r.method_17784().field_1351;
                this.hitQuadZ1 = r.method_17784().field_1350;
                this.hitQuadX2 = r.method_17784().field_1352;
                this.hitQuadY2 = r.method_17784().field_1351;
                this.hitQuadZ2 = r.method_17784().field_1350;
                if (r.method_17780() == class_2350.field_11036 || r.method_17780() == class_2350.field_11033) {
                    this.hitQuadHorizontal = true;
                    this.hitQuadX1 -= 0.25d;
                    this.hitQuadZ1 -= 0.25d;
                    this.hitQuadX2 += 0.25d;
                    this.hitQuadZ2 += 0.25d;
                } else if (r.method_17780() == class_2350.field_11043 || r.method_17780() == class_2350.field_11035) {
                    this.hitQuadHorizontal = false;
                    this.hitQuadX1 -= 0.25d;
                    this.hitQuadY1 -= 0.25d;
                    this.hitQuadX2 += 0.25d;
                    this.hitQuadY2 += 0.25d;
                } else {
                    this.hitQuadHorizontal = false;
                    this.hitQuadZ1 -= 0.25d;
                    this.hitQuadY1 -= 0.25d;
                    this.hitQuadZ2 += 0.25d;
                    this.hitQuadY2 += 0.25d;
                }
                this.points.add(Utils.set(Trajectories.this.vec3s.get(), result.method_17784()));
                return;
            }
            if (result.method_17783() == class_239.class_240.field_1331) {
                this.collidingEntity = ((class_3966) result).method_17782();
                this.points.add(Utils.set(Trajectories.this.vec3s.get(), result.method_17784()).add(0.0d, this.collidingEntity.method_17682() / 2.0f, 0.0d));
            }
        }

        public void render(Render3DEvent event) {
            for (Vector3d point : this.points) {
                if (this.lastPoint != null) {
                    event.renderer.line(this.lastPoint.x, this.lastPoint.y, this.lastPoint.z, point.x, point.y, point.z, Trajectories.this.lineColor.get());
                    if (Trajectories.this.renderPositionBox.get().booleanValue()) {
                        event.renderer.box(point.x - Trajectories.this.positionBoxSize.get().doubleValue(), point.y - Trajectories.this.positionBoxSize.get().doubleValue(), point.z - Trajectories.this.positionBoxSize.get().doubleValue(), point.x + Trajectories.this.positionBoxSize.get().doubleValue(), point.y + Trajectories.this.positionBoxSize.get().doubleValue(), point.z + Trajectories.this.positionBoxSize.get().doubleValue(), Trajectories.this.positionSideColor.get(), Trajectories.this.positionLineColor.get(), Trajectories.this.shapeMode.get(), 0);
                    }
                }
                this.lastPoint = point;
            }
            if (this.hitQuad) {
                if (this.hitQuadHorizontal) {
                    event.renderer.sideHorizontal(this.hitQuadX1, this.hitQuadY1, this.hitQuadZ1, this.hitQuadX1 + 0.5d, this.hitQuadZ1 + 0.5d, Trajectories.this.sideColor.get(), Trajectories.this.lineColor.get(), Trajectories.this.shapeMode.get());
                } else {
                    event.renderer.sideVertical(this.hitQuadX1, this.hitQuadY1, this.hitQuadZ1, this.hitQuadX2, this.hitQuadY2, this.hitQuadZ2, Trajectories.this.sideColor.get(), Trajectories.this.lineColor.get(), Trajectories.this.shapeMode.get());
                }
            }
            if (this.collidingEntity != null) {
                double x = (this.collidingEntity.method_23317() - this.collidingEntity.field_6038) * ((double) event.tickDelta);
                double y = (this.collidingEntity.method_23318() - this.collidingEntity.field_5971) * ((double) event.tickDelta);
                double z = (this.collidingEntity.method_23321() - this.collidingEntity.field_5989) * ((double) event.tickDelta);
                class_238 box = this.collidingEntity.method_5829();
                event.renderer.box(x + box.field_1323, y + box.field_1322, z + box.field_1321, x + box.field_1320, y + box.field_1325, z + box.field_1324, Trajectories.this.sideColor.get(), Trajectories.this.lineColor.get(), Trajectories.this.shapeMode.get(), 0);
            }
        }
    }
}
