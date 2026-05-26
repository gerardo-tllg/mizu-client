package meteordevelopment.meteorclient.systems.modules.hunting;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1533;
import net.minecraft.class_2215;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_2546;
import net.minecraft.class_2573;
import net.minecraft.class_2680;
import net.minecraft.class_2818;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/hunting/VanityESP.class */
public class VanityESP extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgColors;
    private final Setting<Boolean> highlightItemFrames;
    private final Setting<Boolean> highlightBanners;
    private final Setting<SettingColor> mapColor;
    private final Setting<SettingColor> mapOutlineColor;
    private final Setting<SettingColor> bannerColor;
    private final Setting<SettingColor> bannerOutline;

    public VanityESP() {
        super(Categories.Hunting, "vanity-esp", "Highlights maparts and banners");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgColors = this.settings.createGroup("Colors");
        this.highlightItemFrames = this.sgGeneral.add(new BoolSetting.Builder().name("item-frames").description("highlights item frames that contain maps.").defaultValue(true).build());
        this.highlightBanners = this.sgGeneral.add(new BoolSetting.Builder().name("banners").description("highlights banners.").defaultValue(true).build());
        this.mapColor = this.sgColors.add(new ColorSetting.Builder().name("map-color").description("fill color for item frames containing maps.").defaultValue(new SettingColor(255, 255, 0, 50)).build());
        this.mapOutlineColor = this.sgColors.add(new ColorSetting.Builder().name("map-outline-color").description("outline color for item frames containing maps.").defaultValue(new SettingColor(255, 255, 0, 255)).build());
        this.bannerColor = this.sgColors.add(new ColorSetting.Builder().name("banner-fill").description("fill color for banners.").defaultValue(new SettingColor(255, 0, 0, 50)).build());
        this.bannerOutline = this.sgColors.add(new ColorSetting.Builder().name("banner-outline").description("outline color for banners.").defaultValue(new SettingColor(255, 0, 0, 255)).build());
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        class_238 class_238Var;
        class_238 class_238Var2;
        class_238 class_238VarMethod_1009;
        if (this.mc.field_1687 == null || this.mc.field_1724 == null) {
            return;
        }
        if (this.highlightItemFrames.get().booleanValue()) {
            for (class_1533 frame : this.mc.field_1687.method_8390(class_1533.class, this.mc.field_1724.method_5829().method_1014(64.0d), e -> {
                return e.method_6940().method_7909().method_7876().equals("item.minecraft.filled_map");
            })) {
                float pitch = frame.method_36455();
                if (pitch == 90.0f || pitch == -90.0f) {
                    class_238VarMethod_1009 = frame.method_5829().method_1009(0.12d, 0.01d, 0.12d);
                } else {
                    class_238VarMethod_1009 = frame.method_5829().method_1009(0.12d, 0.12d, 0.01d);
                }
                class_238 box = class_238VarMethod_1009;
                event.renderer.box(box, new Color(this.mapColor.get()), new Color(this.mapOutlineColor.get()), ShapeMode.Both, 0);
            }
        }
        if (this.highlightBanners.get().booleanValue()) {
            class_2338 playerPos = this.mc.field_1724.method_24515();
            for (int dx = -8; dx <= 8; dx++) {
                for (int dz = -8; dz <= 8; dz++) {
                    class_2818 chunk = this.mc.field_1687.method_8497((playerPos.method_10263() / 16) + dx, (playerPos.method_10260() / 16) + dz);
                    if (chunk != null) {
                        for (class_2573 class_2573Var : chunk.method_12214().values()) {
                            if (class_2573Var instanceof class_2573) {
                                class_2573 banner = class_2573Var;
                                class_2338 pos = banner.method_11016();
                                class_2680 state = this.mc.field_1687.method_8320(pos);
                                Color fill = new Color(this.bannerColor.get());
                                Color outline = new Color(this.bannerOutline.get());
                                if (state.method_28498(class_2546.field_11722)) {
                                    class_2350 facing = state.method_11654(class_2546.field_11722);
                                    double centerX = ((double) pos.method_10263()) + 0.5d;
                                    double centerZ = ((double) pos.method_10260()) + 0.5d;
                                    double y1 = ((double) pos.method_10264()) - 0.95d;
                                    double y2 = ((double) pos.method_10264()) + 0.85d;
                                    switch (AnonymousClass1.$SwitchMap$net$minecraft$util$math$Direction[facing.ordinal()]) {
                                        case 1:
                                            class_238Var = new class_238(centerX - 0.45d, y1, (((double) (pos.method_10260() + 1)) - 0.1d) - 0.03d, centerX + 0.45d, y2, ((double) (pos.method_10260() + 1)) - 0.1d);
                                            class_238 box2 = class_238Var;
                                            event.renderer.box(box2, fill, outline, ShapeMode.Both, 0);
                                            break;
                                        case 2:
                                            class_238Var = new class_238(centerX - 0.45d, y1, ((double) pos.method_10260()) + 0.1d, centerX + 0.45d, y2, ((double) pos.method_10260()) + 0.1d + 0.03d);
                                            class_238 box22 = class_238Var;
                                            event.renderer.box(box22, fill, outline, ShapeMode.Both, 0);
                                            break;
                                        case 3:
                                            class_238Var = new class_238((((double) (pos.method_10263() + 1)) - 0.1d) - 0.03d, y1, centerZ - 0.45d, ((double) (pos.method_10263() + 1)) - 0.1d, y2, centerZ + 0.45d);
                                            class_238 box222 = class_238Var;
                                            event.renderer.box(box222, fill, outline, ShapeMode.Both, 0);
                                            break;
                                        case 4:
                                            class_238Var = new class_238(((double) pos.method_10263()) + 0.1d, y1, centerZ - 0.45d, ((double) pos.method_10263()) + 0.1d + 0.03d, y2, centerZ + 0.45d);
                                            class_238 box2222 = class_238Var;
                                            event.renderer.box(box2222, fill, outline, ShapeMode.Both, 0);
                                            break;
                                    }
                                } else if (state.method_28498(class_2215.field_9924)) {
                                    int rotation = ((Integer) state.method_11654(class_2215.field_9924)).intValue();
                                    double centerX2 = ((double) pos.method_10263()) + 0.5d;
                                    double centerZ2 = ((double) pos.method_10260()) + 0.5d;
                                    double y12 = pos.method_10264();
                                    double y22 = ((double) pos.method_10264()) + 1.85d;
                                    if (rotation == 0 || rotation == 8) {
                                        class_238Var2 = new class_238(centerX2 - 0.45d, y12, centerZ2 - 0.03d, centerX2 + 0.45d, y22, centerZ2 + 0.03d);
                                    } else if (rotation == 4 || rotation == 12) {
                                        class_238Var2 = new class_238(centerX2 - 0.03d, y12, centerZ2 - 0.45d, centerX2 + 0.03d, y22, centerZ2 + 0.45d);
                                    } else {
                                        class_238Var2 = new class_238(centerX2 - 0.3d, y12, centerZ2 - 0.3d, centerX2 + 0.3d, y22, centerZ2 + 0.3d);
                                    }
                                    class_238 box3 = class_238Var2;
                                    event.renderer.box(box3, fill, outline, ShapeMode.Both, 0);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /* JADX INFO: renamed from: meteordevelopment.meteorclient.systems.modules.hunting.VanityESP$1, reason: invalid class name */
    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/hunting/VanityESP$1.class */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$net$minecraft$util$math$Direction = new int[class_2350.values().length];

        static {
            try {
                $SwitchMap$net$minecraft$util$math$Direction[class_2350.field_11043.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$net$minecraft$util$math$Direction[class_2350.field_11035.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$net$minecraft$util$math$Direction[class_2350.field_11039.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$net$minecraft$util$math$Direction[class_2350.field_11034.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }
}
