package meteordevelopment.meteorclient.systems.modules.render;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import javassist.bytecode.Opcode;
import javassist.compiler.TokenId;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.Target;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1311;
import net.minecraft.class_1657;
import net.minecraft.class_241;
import org.joml.Vector2f;
import org.joml.Vector3d;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/Tracers.class */
public class Tracers extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgAppearance;
    private final SettingGroup sgColors;
    private final Setting<Set<class_1299<?>>> entities;
    private final Setting<Boolean> ignoreSelf;
    public final Setting<Boolean> ignoreFriends;
    public final Setting<Boolean> showInvis;
    private final Setting<TracerStyle> style;
    private final Setting<Target> target;
    private final Setting<Boolean> stem;
    private final Setting<Integer> maxDist;
    private final Setting<Integer> distanceOffscreen;
    private final Setting<Integer> sizeOffscreen;
    private final Setting<Boolean> blinkOffscreen;
    private final Setting<Double> blinkOffscreenSpeed;
    public final Setting<Boolean> distance;
    public final Setting<Boolean> friendOverride;
    private final Setting<SettingColor> playersColor;
    private final Setting<SettingColor> animalsColor;
    private final Setting<SettingColor> waterAnimalsColor;
    private final Setting<SettingColor> monstersColor;
    private final Setting<SettingColor> ambientColor;
    private final Setting<SettingColor> miscColor;
    private int count;
    private final Instant initTimer;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/Tracers$TracerStyle.class */
    public enum TracerStyle {
        Lines,
        Offscreen
    }

    public Tracers() {
        super(Categories.Render, "tracers", "Displays tracer lines to specified entities.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgAppearance = this.settings.createGroup("Appearance");
        this.sgColors = this.settings.createGroup("Colors");
        this.entities = this.sgGeneral.add(new EntityTypeListSetting.Builder().name("entities").description("Select specific entities.").defaultValue(class_1299.field_6097).build());
        this.ignoreSelf = this.sgGeneral.add(new BoolSetting.Builder().name("ignore-self").description("Doesn't draw tracers to yourself when in third person or freecam.").defaultValue(false).build());
        this.ignoreFriends = this.sgGeneral.add(new BoolSetting.Builder().name("ignore-friends").description("Doesn't draw tracers to friends.").defaultValue(false).build());
        this.showInvis = this.sgGeneral.add(new BoolSetting.Builder().name("show-invisible").description("Shows invisible entities.").defaultValue(true).build());
        this.style = this.sgAppearance.add(new EnumSetting.Builder().name("style").description("What display mode should be used").defaultValue(TracerStyle.Lines).build());
        this.target = this.sgAppearance.add(new EnumSetting.Builder().name("target").description("What part of the entity to target.").defaultValue(Target.Body).visible(() -> {
            return this.style.get() == TracerStyle.Lines;
        }).build());
        this.stem = this.sgAppearance.add(new BoolSetting.Builder().name("stem").description("Draw a line through the center of the tracer target.").defaultValue(true).visible(() -> {
            return this.style.get() == TracerStyle.Lines;
        }).build());
        this.maxDist = this.sgAppearance.add(new IntSetting.Builder().name("max-distance").description("Maximum distance for tracers to show.").defaultValue(256).min(0).sliderMax(256).build());
        this.distanceOffscreen = this.sgAppearance.add(new IntSetting.Builder().name("distance-offscreen").description("Offscreen's distance from center.").defaultValue(200).min(0).sliderMax(TokenId.BadToken).visible(() -> {
            return this.style.get() == TracerStyle.Offscreen;
        }).build());
        this.sizeOffscreen = this.sgAppearance.add(new IntSetting.Builder().name("size-offscreen").description("Offscreen's size.").defaultValue(10).min(2).sliderMax(50).visible(() -> {
            return this.style.get() == TracerStyle.Offscreen;
        }).build());
        this.blinkOffscreen = this.sgAppearance.add(new BoolSetting.Builder().name("blink-offscreen").description("Make offscreen Blink.").defaultValue(true).visible(() -> {
            return this.style.get() == TracerStyle.Offscreen;
        }).build());
        this.blinkOffscreenSpeed = this.sgAppearance.add(new DoubleSetting.Builder().name("blink-offscreen-speed").description("Offscreen's blink speed.").defaultValue(4.0d).min(1.0d).sliderMax(15.0d).visible(() -> {
            return this.style.get() == TracerStyle.Offscreen && this.blinkOffscreen.get().booleanValue();
        }).build());
        this.distance = this.sgColors.add(new BoolSetting.Builder().name("distance-colors").description("Changes the color of tracers depending on distance.").defaultValue(false).build());
        this.friendOverride = this.sgColors.add(new BoolSetting.Builder().name("show-friend-colors").description("Whether or not to override the distance color of friends with the friend color.").defaultValue(true).visible(() -> {
            return this.distance.get().booleanValue() && !this.ignoreFriends.get().booleanValue();
        }).build());
        this.playersColor = this.sgColors.add(new ColorSetting.Builder().name("players-colors").description("The player's color.").defaultValue(new SettingColor(205, 205, 205, Opcode.LAND)).visible(() -> {
            return !this.distance.get().booleanValue();
        }).build());
        this.animalsColor = this.sgColors.add(new ColorSetting.Builder().name("animals-color").description("The animal's color.").defaultValue(new SettingColor(Opcode.I2B, 255, Opcode.I2B, Opcode.LAND)).visible(() -> {
            return !this.distance.get().booleanValue();
        }).build());
        this.waterAnimalsColor = this.sgColors.add(new ColorSetting.Builder().name("water-animals-color").description("The water animal's color.").defaultValue(new SettingColor(Opcode.I2B, Opcode.I2B, 255, Opcode.LAND)).visible(() -> {
            return !this.distance.get().booleanValue();
        }).build());
        this.monstersColor = this.sgColors.add(new ColorSetting.Builder().name("monsters-color").description("The monster's color.").defaultValue(new SettingColor(255, Opcode.I2B, Opcode.I2B, Opcode.LAND)).visible(() -> {
            return !this.distance.get().booleanValue();
        }).build());
        this.ambientColor = this.sgColors.add(new ColorSetting.Builder().name("ambient-color").description("The ambient color.").defaultValue(new SettingColor(75, 75, 75, Opcode.LAND)).visible(() -> {
            return !this.distance.get().booleanValue();
        }).build());
        this.miscColor = this.sgColors.add(new ColorSetting.Builder().name("misc-color").description("The misc color.").defaultValue(new SettingColor(Opcode.I2B, Opcode.I2B, Opcode.I2B, Opcode.LAND)).visible(() -> {
            return !this.distance.get().booleanValue();
        }).build());
        this.initTimer = Instant.now();
    }

    private boolean shouldBeIgnored(class_1297 entity) {
        if (PlayerUtils.isWithin(entity, this.maxDist.get().intValue()) && ((Modules.get().isActive(Freecam.class) || entity != this.mc.field_1724) && this.entities.get().contains(entity.method_5864()) && ((!this.ignoreSelf.get().booleanValue() || entity != this.mc.field_1724) && (!this.ignoreFriends.get().booleanValue() || !(entity instanceof class_1657) || !Friends.get().isFriend((class_1657) entity))))) {
            if (!((!this.showInvis.get().booleanValue() && entity.method_5767()) | (!EntityUtils.isInRenderDistance(entity)))) {
                return false;
            }
        }
        return true;
    }

    private Color getEntityColor(class_1297 entity) {
        SettingColor settingColor;
        Color color;
        if (this.distance.get().booleanValue()) {
            if (this.friendOverride.get().booleanValue() && (entity instanceof class_1657) && Friends.get().isFriend((class_1657) entity)) {
                color = Config.get().friendColor.get();
            } else {
                color = EntityUtils.getColorFromDistance(entity);
            }
        } else if (entity instanceof class_1657) {
            color = PlayerUtils.getPlayerColor((class_1657) entity, this.playersColor.get());
        } else {
            switch (AnonymousClass1.$SwitchMap$net$minecraft$entity$SpawnGroup[entity.method_5864().method_5891().ordinal()]) {
                case 1:
                    settingColor = this.animalsColor.get();
                    break;
                case 2:
                case 3:
                case 4:
                case 5:
                    settingColor = this.waterAnimalsColor.get();
                    break;
                case 6:
                    settingColor = this.monstersColor.get();
                    break;
                case 7:
                    settingColor = this.ambientColor.get();
                    break;
                default:
                    settingColor = this.miscColor.get();
                    break;
            }
            color = settingColor;
        }
        return new Color(color);
    }

    /* JADX INFO: renamed from: meteordevelopment.meteorclient.systems.modules.render.Tracers$1, reason: invalid class name */
    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/Tracers$1.class */
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

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (this.mc.field_1690.field_1842 || this.style.get() == TracerStyle.Offscreen) {
            return;
        }
        this.count = 0;
        for (class_1297 entity : this.mc.field_1687.method_18112()) {
            if (!shouldBeIgnored(entity)) {
                Color color = getEntityColor(entity);
                double x = entity.field_6014 + ((entity.method_23317() - entity.field_6014) * ((double) event.tickDelta));
                double y = entity.field_6036 + ((entity.method_23318() - entity.field_6036) * ((double) event.tickDelta));
                double z = entity.field_5969 + ((entity.method_23321() - entity.field_5969) * ((double) event.tickDelta));
                double height = entity.method_5829().field_1325 - entity.method_5829().field_1322;
                if (this.target.get() == Target.Head) {
                    y += height;
                } else if (this.target.get() == Target.Body) {
                    y += height / 2.0d;
                }
                event.renderer.line(RenderUtils.center.field_1352, RenderUtils.center.field_1351, RenderUtils.center.field_1350, x, y, z, color);
                if (this.stem.get().booleanValue()) {
                    event.renderer.line(x, entity.method_23318(), z, x, entity.method_23318() + height, z, color);
                }
                this.count++;
            }
        }
    }

    @EventHandler
    public void onRender2D(Render2DEvent event) {
        if (this.mc.field_1690.field_1842 || this.style.get() != TracerStyle.Offscreen) {
            return;
        }
        this.count = 0;
        Renderer2D.COLOR.begin();
        for (class_1297 entity : this.mc.field_1687.method_18112()) {
            if (!shouldBeIgnored(entity)) {
                Color color = getEntityColor(entity);
                if (this.blinkOffscreen.get().booleanValue()) {
                    color.a = (int) (color.a * getAlpha());
                }
                class_241 screenCenter = new class_241(this.mc.method_22683().method_4489() / 2.0f, this.mc.method_22683().method_4506() / 2.0f);
                Vector3d projection = new Vector3d(entity.field_6014, entity.field_6036, entity.field_5969);
                boolean projSucceeded = NametagUtils.to2D(projection, 1.0d, false, false);
                if (!projSucceeded || projection.x <= 0.0d || projection.x >= this.mc.method_22683().method_4489() || projection.y <= 0.0d || projection.y >= this.mc.method_22683().method_4506()) {
                    Vector3d projection2 = new Vector3d(entity.field_6014, entity.field_6036, entity.field_5969);
                    NametagUtils.to2D(projection2, 1.0d, false, true);
                    Vector2f angle = vectorAngles(new Vector3d(((double) screenCenter.field_1343) - projection2.x, ((double) screenCenter.field_1342) - projection2.y, 0.0d));
                    angle.y += 180.0f;
                    float angleYawRad = (float) Math.toRadians(angle.y);
                    Vector2f newPoint = new Vector2f(screenCenter.field_1343 + (this.distanceOffscreen.get().intValue() * ((float) Math.cos(angleYawRad))), screenCenter.field_1342 + (this.distanceOffscreen.get().intValue() * ((float) Math.sin(angleYawRad))));
                    Vector2f[] trianglePoints = {new Vector2f(newPoint.x - this.sizeOffscreen.get().intValue(), newPoint.y - this.sizeOffscreen.get().intValue()), new Vector2f(newPoint.x + (this.sizeOffscreen.get().intValue() * 0.73205f), newPoint.y), new Vector2f(newPoint.x - this.sizeOffscreen.get().intValue(), newPoint.y + this.sizeOffscreen.get().intValue())};
                    rotateTriangle(trianglePoints, angle.y);
                    Renderer2D.COLOR.triangle(trianglePoints[0].x, trianglePoints[0].y, trianglePoints[1].x, trianglePoints[1].y, trianglePoints[2].x, trianglePoints[2].y, color);
                    this.count++;
                }
            }
        }
        Renderer2D.COLOR.render();
    }

    private void rotateTriangle(Vector2f[] points, float ang) {
        Vector2f triangleCenter = new Vector2f(0.0f, 0.0f);
        triangleCenter.add(points[0]).add(points[1]).add(points[2]).div(3.0f);
        float theta = (float) Math.toRadians(ang);
        float cos = (float) Math.cos(theta);
        float sin = (float) Math.sin(theta);
        for (int i = 0; i < 3; i++) {
            Vector2f point = new Vector2f(points[i].x, points[i].y).sub(triangleCenter);
            Vector2f newPoint = new Vector2f((point.x * cos) - (point.y * sin), (point.x * sin) + (point.y * cos));
            newPoint.add(triangleCenter);
            points[i] = newPoint;
        }
    }

    private Vector2f vectorAngles(Vector3d forward) {
        float yaw;
        float pitch;
        if (forward.x == 0.0d && forward.y == 0.0d) {
            yaw = 0.0f;
            if (forward.z > 0.0d) {
                pitch = 270.0f;
            } else {
                pitch = 90.0f;
            }
        } else {
            yaw = (float) ((Math.atan2(forward.y, forward.x) * 180.0d) / 3.141592653589793d);
            if (yaw < 0.0f) {
                yaw += 360.0f;
            }
            float tmp = (float) Math.sqrt((forward.x * forward.x) + (forward.y * forward.y));
            pitch = (float) ((Math.atan2(-forward.z, tmp) * 180.0d) / 3.141592653589793d);
            if (pitch < 0.0f) {
                pitch += 360.0f;
            }
        }
        return new Vector2f(pitch, yaw);
    }

    private float getAlpha() {
        double speed = this.blinkOffscreenSpeed.get().doubleValue() / 4.0d;
        double duration = Math.abs(Duration.between(Instant.now(), this.initTimer).toMillis()) * speed;
        return ((float) Math.abs((duration % 1000.0d) - 500.0d)) / 500.0f;
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public String getInfoString() {
        return Integer.toString(this.count);
    }
}
