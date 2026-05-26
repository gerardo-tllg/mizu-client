package meteordevelopment.meteorclient.systems.modules.render;

import java.util.Set;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1311;
import net.minecraft.class_1511;
import net.minecraft.class_1657;
import net.minecraft.class_238;
import net.minecraft.class_3532;
import org.joml.Vector3d;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/ESP.class */
public class ESP extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgColors;
    public final Setting<Mode> mode;
    public final Setting<Integer> outlineWidth;
    public final Setting<Double> glowMultiplier;
    public final Setting<Boolean> ignoreSelf;
    public final Setting<ShapeMode> shapeMode;
    public final Setting<Double> fillOpacity;
    private final Setting<Double> fadeDistance;
    private final Setting<Double> endCrystalFadeDistance;
    private final Setting<Set<class_1299<?>>> entities;
    private final Setting<SettingColor> playersLineColor;
    private final Setting<SettingColor> playersSideColor;
    private final Setting<SettingColor> friendPlayersLineColor;
    private final Setting<SettingColor> friendPlayersSideColor;
    private final Setting<SettingColor> enemyPlayersLineColor;
    private final Setting<SettingColor> enemyPlayersSideColor;
    private final Setting<SettingColor> animalsLineColor;
    private final Setting<SettingColor> animalsSideColor;
    private final Setting<SettingColor> waterAnimalsLineColor;
    private final Setting<SettingColor> waterAnimalsSideColor;
    private final Setting<SettingColor> monstersLineColor;
    private final Setting<SettingColor> monstersSideColor;
    private final Setting<SettingColor> ambientLineColor;
    private final Setting<SettingColor> ambientSideColor;
    private final Setting<SettingColor> miscLineColor;
    private final Setting<SettingColor> miscSideColor;
    private final Color lineColor;
    private final Color sideColor;
    private final Color baseSideColor;
    private final Color baseLineColor;
    private final Vector3d pos1;
    private final Vector3d pos2;
    private final Vector3d pos;
    private int count;

    public ESP() {
        super(Categories.Render, "esp", "Renders entities through walls.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgColors = this.settings.createGroup("Colors");
        this.mode = this.sgGeneral.add(new EnumSetting.Builder().name("mode").description("Rendering mode.").defaultValue(Mode.Shader).build());
        this.outlineWidth = this.sgGeneral.add(new IntSetting.Builder().name("outline-width").description("The width of the shader outline.").visible(() -> {
            return this.mode.get() == Mode.Shader;
        }).defaultValue(2).range(1, 10).sliderRange(1, 5).build());
        this.glowMultiplier = this.sgGeneral.add(new DoubleSetting.Builder().name("glow-multiplier").description("Multiplier for glow effect").visible(() -> {
            return this.mode.get() == Mode.Shader;
        }).decimalPlaces(3).defaultValue(3.5d).min(0.0d).sliderMax(10.0d).build());
        this.ignoreSelf = this.sgGeneral.add(new BoolSetting.Builder().name("ignore-self").description("Ignores yourself drawing the shader.").defaultValue(true).build());
        this.shapeMode = this.sgGeneral.add(new EnumSetting.Builder().name("shape-mode").description("How the shapes are rendered.").visible(() -> {
            return this.mode.get() != Mode.Glow;
        }).defaultValue(ShapeMode.Both).build());
        this.fillOpacity = this.sgGeneral.add(new DoubleSetting.Builder().name("fill-opacity").description("The opacity of the shape fill.").visible(() -> {
            return (this.shapeMode.get() == ShapeMode.Lines || this.mode.get() == Mode.Glow) ? false : true;
        }).defaultValue(0.3d).range(0.0d, 1.0d).sliderMax(1.0d).build());
        this.fadeDistance = this.sgGeneral.add(new DoubleSetting.Builder().name("fade-distance").description("The distance from an entity where the color begins to fade.").defaultValue(3.0d).min(0.0d).sliderMax(12.0d).build());
        this.endCrystalFadeDistance = this.sgGeneral.add(new DoubleSetting.Builder().name("end-crystal-fade-distance").description("The distance from an end crystal where the color begins to fade.").defaultValue(3.0d).min(0.0d).sliderMax(12.0d).build());
        this.entities = this.sgGeneral.add(new EntityTypeListSetting.Builder().name("entities").description("Select specific entities.").defaultValue(class_1299.field_6097).build());
        this.playersLineColor = this.sgColors.add(new ColorSetting.Builder().name("players-line-color").description("The line color for players.").defaultValue(new SettingColor(255, 255, 255)).build());
        this.playersSideColor = this.sgColors.add(new ColorSetting.Builder().name("players-side-color").description("The side color for players.").defaultValue(new SettingColor(255, 255, 255)).build());
        this.friendPlayersLineColor = this.sgColors.add(new ColorSetting.Builder().name("friend-players-line-color").description("The line color for players you have added.").defaultValue(new SettingColor(255, 255, 255)).build());
        this.friendPlayersSideColor = this.sgColors.add(new ColorSetting.Builder().name("friend-players-side-color").description("The side color for playersyou have added.").defaultValue(new SettingColor(255, 255, 255)).build());
        this.enemyPlayersLineColor = this.sgColors.add(new ColorSetting.Builder().name("enemy-players-line-color").description("The line color for players you have enemied.").defaultValue(new SettingColor(255, 255, 255)).build());
        this.enemyPlayersSideColor = this.sgColors.add(new ColorSetting.Builder().name("enemy-players-side-color").description("The side color for players you have enemied.").defaultValue(new SettingColor(255, 255, 255)).build());
        this.animalsLineColor = this.sgColors.add(new ColorSetting.Builder().name("animals-line-color").description("The line color for animals.").defaultValue(new SettingColor(25, 255, 25, 255)).build());
        this.animalsSideColor = this.sgColors.add(new ColorSetting.Builder().name("animals-side-color").description("The side color for animals.").defaultValue(new SettingColor(25, 255, 25, 255)).build());
        this.waterAnimalsLineColor = this.sgColors.add(new ColorSetting.Builder().name("water-animals-line-color").description("The line color for water animals.").defaultValue(new SettingColor(25, 25, 255, 255)).build());
        this.waterAnimalsSideColor = this.sgColors.add(new ColorSetting.Builder().name("water-animals-side-color").description("The side color for water animals.").defaultValue(new SettingColor(25, 25, 255, 255)).build());
        this.monstersLineColor = this.sgColors.add(new ColorSetting.Builder().name("monsters-line-color").description("The line color for monsters.").defaultValue(new SettingColor(255, 25, 25, 255)).build());
        this.monstersSideColor = this.sgColors.add(new ColorSetting.Builder().name("monsters-side-color").description("The side color for monsters.").defaultValue(new SettingColor(255, 25, 25, 255)).build());
        this.ambientLineColor = this.sgColors.add(new ColorSetting.Builder().name("ambient-line-color").description("The line color for ambient entities.").defaultValue(new SettingColor(25, 25, 25, 255)).build());
        this.ambientSideColor = this.sgColors.add(new ColorSetting.Builder().name("ambient-side-color").description("The side color for ambient entities.").defaultValue(new SettingColor(25, 25, 25, 255)).build());
        this.miscLineColor = this.sgColors.add(new ColorSetting.Builder().name("misc-line-color").description("The line color for miscellaneous entities.").defaultValue(new SettingColor(Opcode.DRETURN, Opcode.DRETURN, Opcode.DRETURN, 255)).build());
        this.miscSideColor = this.sgColors.add(new ColorSetting.Builder().name("misc-side-color").description("The side color for miscellaneous entities.").defaultValue(new SettingColor(Opcode.DRETURN, Opcode.DRETURN, Opcode.DRETURN, 255)).build());
        this.lineColor = new Color();
        this.sideColor = new Color();
        this.baseSideColor = new Color();
        this.baseLineColor = new Color();
        this.pos1 = new Vector3d();
        this.pos2 = new Vector3d();
        this.pos = new Vector3d();
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (this.mode.get() == Mode._2D) {
            return;
        }
        this.count = 0;
        for (class_1297 entity : this.mc.field_1687.method_18112()) {
            if (!shouldSkip(entity)) {
                if (this.mode.get() == Mode.Box || this.mode.get() == Mode.Wireframe) {
                    drawBoundingBox(event, entity);
                }
                this.count++;
            }
        }
    }

    private void drawBoundingBox(Render3DEvent event, class_1297 entity) {
        double alpha;
        Color entitySideColor = getSideColor(entity);
        Color entityLineColor = getLineColor(entity);
        if (entitySideColor != null && entityLineColor != null) {
            double alpha2 = 1.0d;
            if (entity instanceof class_1511) {
                double fadeDist = this.endCrystalFadeDistance.get().doubleValue() * this.endCrystalFadeDistance.get().doubleValue();
                double distance = PlayerUtils.squaredDistanceToCamera(entity);
                if (distance <= fadeDist / 2.0d) {
                    alpha = 1.0d;
                } else if (distance >= fadeDist * 2.0d) {
                    alpha = 0.0d;
                } else {
                    alpha = 1.0d - ((distance - (fadeDist / 2.0d)) / (fadeDist * 1.5d));
                }
                if (alpha <= 0.075d) {
                    alpha2 = 0.0d;
                } else {
                    alpha2 = alpha + 0.1d;
                }
                if (alpha2 > 1.0d) {
                    alpha2 = 1.0d;
                }
            }
            this.sideColor.set(entitySideColor).a((int) (((double) this.sideColor.a) * this.fillOpacity.get().doubleValue() * alpha2 * alpha2));
            this.lineColor.set(entityLineColor).a((int) (((double) this.lineColor.a) * alpha2 * alpha2));
        }
        if (this.mode.get() == Mode.Box) {
            double x = class_3532.method_16436(event.tickDelta, entity.field_6038, entity.method_23317()) - entity.method_23317();
            double y = class_3532.method_16436(event.tickDelta, entity.field_5971, entity.method_23318()) - entity.method_23318();
            double z = class_3532.method_16436(event.tickDelta, entity.field_5989, entity.method_23321()) - entity.method_23321();
            class_238 box = entity.method_5829();
            event.renderer.box(x + box.field_1323, y + box.field_1322, z + box.field_1321, x + box.field_1320, y + box.field_1325, z + box.field_1324, this.sideColor, this.lineColor, this.shapeMode.get(), 0);
        }
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        if (this.mode.get() != Mode._2D) {
            return;
        }
        Renderer2D.COLOR.begin();
        this.count = 0;
        for (class_1297 entity : this.mc.field_1687.method_18112()) {
            if (!shouldSkip(entity)) {
                class_238 box = entity.method_5829();
                double x = class_3532.method_16436(event.tickDelta, entity.field_6038, entity.method_23317()) - entity.method_23317();
                double y = class_3532.method_16436(event.tickDelta, entity.field_5971, entity.method_23318()) - entity.method_23318();
                double z = class_3532.method_16436(event.tickDelta, entity.field_5989, entity.method_23321()) - entity.method_23321();
                this.pos1.set(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
                this.pos2.set(0.0d, 0.0d, 0.0d);
                if (!checkCorner(box.field_1323 + x, box.field_1322 + y, box.field_1321 + z, this.pos1, this.pos2) && !checkCorner(box.field_1320 + x, box.field_1322 + y, box.field_1321 + z, this.pos1, this.pos2) && !checkCorner(box.field_1323 + x, box.field_1322 + y, box.field_1324 + z, this.pos1, this.pos2) && !checkCorner(box.field_1320 + x, box.field_1322 + y, box.field_1324 + z, this.pos1, this.pos2) && !checkCorner(box.field_1323 + x, box.field_1325 + y, box.field_1321 + z, this.pos1, this.pos2) && !checkCorner(box.field_1320 + x, box.field_1325 + y, box.field_1321 + z, this.pos1, this.pos2) && !checkCorner(box.field_1323 + x, box.field_1325 + y, box.field_1324 + z, this.pos1, this.pos2) && !checkCorner(box.field_1320 + x, box.field_1325 + y, box.field_1324 + z, this.pos1, this.pos2)) {
                    Color entitySideColor = getSideColor(entity);
                    Color entityLineColor = getLineColor(entity);
                    if (entitySideColor != null && entityLineColor != null) {
                        this.sideColor.set(entitySideColor).a((int) (((double) this.sideColor.a) * this.fillOpacity.get().doubleValue()));
                        this.lineColor.set(entityLineColor);
                    }
                    if (this.shapeMode.get() != ShapeMode.Lines && this.sideColor.a > 0) {
                        Renderer2D.COLOR.quad(this.pos1.x, this.pos1.y, this.pos2.x - this.pos1.x, this.pos2.y - this.pos1.y, this.sideColor);
                    }
                    if (this.shapeMode.get() != ShapeMode.Sides) {
                        Renderer2D.COLOR.line(this.pos1.x, this.pos1.y, this.pos1.x, this.pos2.y, this.lineColor);
                        Renderer2D.COLOR.line(this.pos2.x, this.pos1.y, this.pos2.x, this.pos2.y, this.lineColor);
                        Renderer2D.COLOR.line(this.pos1.x, this.pos1.y, this.pos2.x, this.pos1.y, this.lineColor);
                        Renderer2D.COLOR.line(this.pos1.x, this.pos2.y, this.pos2.x, this.pos2.y, this.lineColor);
                    }
                    this.count++;
                }
            }
        }
        Renderer2D.COLOR.render();
    }

    private boolean checkCorner(double x, double y, double z, Vector3d min, Vector3d max) {
        this.pos.set(x, y, z);
        if (!NametagUtils.to2D(this.pos, 1.0d)) {
            return true;
        }
        if (this.pos.x < min.x) {
            min.x = this.pos.x;
        }
        if (this.pos.y < min.y) {
            min.y = this.pos.y;
        }
        if (this.pos.z < min.z) {
            min.z = this.pos.z;
        }
        if (this.pos.x > max.x) {
            max.x = this.pos.x;
        }
        if (this.pos.y > max.y) {
            max.y = this.pos.y;
        }
        if (this.pos.z > max.z) {
            max.z = this.pos.z;
            return false;
        }
        return false;
    }

    public boolean shouldSkip(class_1297 entity) {
        if (!this.entities.get().contains(entity.method_5864())) {
            return true;
        }
        if (entity == this.mc.field_1724 && this.ignoreSelf.get().booleanValue()) {
            return true;
        }
        return (entity == this.mc.field_1719 && this.mc.field_1690.method_31044().method_31034()) || !EntityUtils.isInRenderDistance(entity);
    }

    public Color getLineColor(class_1297 entity) {
        if (!this.entities.get().contains(entity.method_5864())) {
            return null;
        }
        double alpha = getFadeAlpha(entity);
        if (alpha == 0.0d) {
            return null;
        }
        Color color = getEntityTypeLineColor(entity);
        return this.baseLineColor.set(color.r, color.g, color.b, (int) (((double) color.a) * alpha));
    }

    public Color getColor(class_1297 entity) {
        return getLineColor(entity);
    }

    public Color getSideColor(class_1297 entity) {
        if (!this.entities.get().contains(entity.method_5864())) {
            return null;
        }
        double alpha = getFadeAlpha(entity);
        if (alpha == 0.0d) {
            return null;
        }
        Color color = getEntityTypeSideColor(entity);
        return this.baseSideColor.set(color.r, color.g, color.b, (int) (((double) color.a) * alpha));
    }

    public Color getEntityTypeLineColor(class_1297 entity) {
        if (entity instanceof class_1657) {
            class_1657 player = (class_1657) entity;
            if (Friends.get().isFriend(player)) {
                return this.friendPlayersLineColor.get();
            }
            return this.playersLineColor.get();
        }
        switch (AnonymousClass1.$SwitchMap$net$minecraft$entity$SpawnGroup[entity.method_5864().method_5891().ordinal()]) {
            case 1:
                return this.animalsLineColor.get();
            case 2:
            case 3:
            case 4:
            case 5:
                return this.waterAnimalsLineColor.get();
            case 6:
                return this.monstersLineColor.get();
            case 7:
                return this.ambientLineColor.get();
            default:
                return this.miscLineColor.get();
        }
    }

    /* JADX INFO: renamed from: meteordevelopment.meteorclient.systems.modules.render.ESP$1, reason: invalid class name */
    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/ESP$1.class */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$net$minecraft$entity$SpawnGroup = new int[class_1311.values().length];

        static {
            try {
                $SwitchMap$net$minecraft$entity$SpawnGroup[class_1311.field_6294.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$net$minecraft$entity$SpawnGroup[class_1311.field_24460.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$net$minecraft$entity$SpawnGroup[class_1311.field_6300.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$net$minecraft$entity$SpawnGroup[class_1311.field_30092.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$net$minecraft$entity$SpawnGroup[class_1311.field_34447.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$net$minecraft$entity$SpawnGroup[class_1311.field_6302.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$net$minecraft$entity$SpawnGroup[class_1311.field_6303.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
        }
    }

    public Color getEntityTypeSideColor(class_1297 entity) {
        if (entity instanceof class_1657) {
            class_1657 player = (class_1657) entity;
            if (Friends.get().isFriend(player)) {
                return this.friendPlayersSideColor.get();
            }
            return this.playersSideColor.get();
        }
        switch (AnonymousClass1.$SwitchMap$net$minecraft$entity$SpawnGroup[entity.method_5864().method_5891().ordinal()]) {
            case 1:
                return this.animalsSideColor.get();
            case 2:
            case 3:
            case 4:
            case 5:
                return this.waterAnimalsSideColor.get();
            case 6:
                return this.monstersSideColor.get();
            case 7:
                return this.ambientSideColor.get();
            default:
                return this.miscSideColor.get();
        }
    }

    private double getFadeAlpha(class_1297 entity) {
        double dist = PlayerUtils.squaredDistanceToCamera(entity.method_23317() + ((double) (entity.method_17681() / 2.0f)), entity.method_23318() + ((double) entity.method_18381(entity.method_18376())), entity.method_23321() + ((double) (entity.method_17681() / 2.0f)));
        double fadeDist = Math.pow(this.fadeDistance.get().doubleValue(), 2.0d);
        double alpha = 1.0d;
        if (dist <= fadeDist * fadeDist) {
            alpha = (float) (Math.sqrt(dist) / fadeDist);
        }
        if (alpha <= 0.075d) {
            alpha = 0.0d;
        }
        return alpha;
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public String getInfoString() {
        return Integer.toString(this.count);
    }

    public boolean isShader() {
        return isActive() && this.mode.get() == Mode.Shader;
    }

    public boolean isGlow() {
        return isActive() && this.mode.get() == Mode.Glow;
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/ESP$Mode.class */
    public enum Mode {
        Box,
        Wireframe,
        _2D,
        Shader,
        Glow;

        @Override // java.lang.Enum
        public String toString() {
            return this == _2D ? "2D" : super.toString();
        }
    }
}
