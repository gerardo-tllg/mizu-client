package meteordevelopment.meteorclient.systems.hud.elements;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectIntImmutablePair;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnchantmentListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.class_1657;
import net.minecraft.class_1748;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1887;
import net.minecraft.class_1890;
import net.minecraft.class_3489;
import net.minecraft.class_3532;
import net.minecraft.class_490;
import net.minecraft.class_5321;
import net.minecraft.class_6880;
import net.minecraft.class_9304;
import net.minecraft.class_9636;
import org.joml.Matrix4fStack;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/hud/elements/CombatHud.class */
public class CombatHud extends HudElement {
    private static final Color GREEN = new Color(15, 255, 15);
    private static final Color RED = new Color(255, 15, 15);
    private static final Color BLACK = new Color(0, 0, 0, 255);
    public static final HudElementInfo<CombatHud> INFO = new HudElementInfo<>(Hud.GROUP, "combat", "Displays information about your combat target.", CombatHud::new);
    private final SettingGroup sgGeneral;
    private final Setting<Double> scale;
    private final Setting<Double> range;
    private final Setting<Boolean> displayPing;
    private final Setting<Boolean> displayDistance;
    private final Setting<Set<class_5321<class_1887>>> displayedEnchantments;
    private final Setting<SettingColor> backgroundColor;
    private final Setting<SettingColor> enchantmentTextColor;
    private final Setting<SettingColor> pingColor1;
    private final Setting<SettingColor> pingColor2;
    private final Setting<SettingColor> pingColor3;
    private final Setting<SettingColor> distColor1;
    private final Setting<SettingColor> distColor2;
    private final Setting<SettingColor> distColor3;
    private final Setting<SettingColor> healthColor1;
    private final Setting<SettingColor> healthColor2;
    private final Setting<SettingColor> healthColor3;
    private class_1657 playerEntity;

    public CombatHud() {
        super(INFO);
        this.sgGeneral = this.settings.getDefaultGroup();
        this.scale = this.sgGeneral.add(new DoubleSetting.Builder().name("scale").description("The scale.").defaultValue(2.0d).min(1.0d).sliderRange(1.0d, 5.0d).onChanged(aDouble -> {
            calculateSize();
        }).build());
        this.range = this.sgGeneral.add(new DoubleSetting.Builder().name("range").description("The range to target players.").defaultValue(100.0d).min(1.0d).sliderMax(200.0d).build());
        this.displayPing = this.sgGeneral.add(new BoolSetting.Builder().name("ping").description("Shows the player's ping.").defaultValue(true).build());
        this.displayDistance = this.sgGeneral.add(new BoolSetting.Builder().name("distance").description("Shows the distance between you and the player.").defaultValue(true).build());
        this.displayedEnchantments = this.sgGeneral.add(new EnchantmentListSetting.Builder().name("displayed-enchantments").description("The enchantments that are shown on nametags.").vanillaDefaults().build());
        this.backgroundColor = this.sgGeneral.add(new ColorSetting.Builder().name("background-color").description("Color of background.").defaultValue(new SettingColor(0, 0, 0, 64)).build());
        this.enchantmentTextColor = this.sgGeneral.add(new ColorSetting.Builder().name("enchantment-color").description("Color of enchantment text.").defaultValue(new SettingColor(255, 255, 255)).build());
        SettingGroup settingGroup = this.sgGeneral;
        ColorSetting.Builder builderDefaultValue = new ColorSetting.Builder().name("ping-stage-1").description("Color of ping text when under 75.").defaultValue(new SettingColor(15, 255, 15));
        Setting<Boolean> setting = this.displayPing;
        Objects.requireNonNull(setting);
        this.pingColor1 = settingGroup.add(builderDefaultValue.visible(setting::get).build());
        SettingGroup settingGroup2 = this.sgGeneral;
        ColorSetting.Builder builderDefaultValue2 = new ColorSetting.Builder().name("ping-stage-2").description("Color of ping text when between 75 and 200.").defaultValue(new SettingColor(255, Opcode.FCMPG, 15));
        Setting<Boolean> setting2 = this.displayPing;
        Objects.requireNonNull(setting2);
        this.pingColor2 = settingGroup2.add(builderDefaultValue2.visible(setting2::get).build());
        SettingGroup settingGroup3 = this.sgGeneral;
        ColorSetting.Builder builderDefaultValue3 = new ColorSetting.Builder().name("ping-stage-3").description("Color of ping text when over 200.").defaultValue(new SettingColor(255, 15, 15));
        Setting<Boolean> setting3 = this.displayPing;
        Objects.requireNonNull(setting3);
        this.pingColor3 = settingGroup3.add(builderDefaultValue3.visible(setting3::get).build());
        SettingGroup settingGroup4 = this.sgGeneral;
        ColorSetting.Builder builderDefaultValue4 = new ColorSetting.Builder().name("distance-stage-1").description("The color when a player is within 10 blocks of you.").defaultValue(new SettingColor(255, 15, 15));
        Setting<Boolean> setting4 = this.displayDistance;
        Objects.requireNonNull(setting4);
        this.distColor1 = settingGroup4.add(builderDefaultValue4.visible(setting4::get).build());
        SettingGroup settingGroup5 = this.sgGeneral;
        ColorSetting.Builder builderDefaultValue5 = new ColorSetting.Builder().name("distance-stage-2").description("The color when a player is within 50 blocks of you.").defaultValue(new SettingColor(255, Opcode.FCMPG, 15));
        Setting<Boolean> setting5 = this.displayDistance;
        Objects.requireNonNull(setting5);
        this.distColor2 = settingGroup5.add(builderDefaultValue5.visible(setting5::get).build());
        SettingGroup settingGroup6 = this.sgGeneral;
        ColorSetting.Builder builderDefaultValue6 = new ColorSetting.Builder().name("distance-stage-3").description("The color when a player is greater then 50 blocks away from you.").defaultValue(new SettingColor(15, 255, 15));
        Setting<Boolean> setting6 = this.displayDistance;
        Objects.requireNonNull(setting6);
        this.distColor3 = settingGroup6.add(builderDefaultValue6.visible(setting6::get).build());
        this.healthColor1 = this.sgGeneral.add(new ColorSetting.Builder().name("health-stage-1").description("The color on the left of the health gradient.").defaultValue(new SettingColor(255, 15, 15)).build());
        this.healthColor2 = this.sgGeneral.add(new ColorSetting.Builder().name("health-stage-2").description("The color in the middle of the health gradient.").defaultValue(new SettingColor(255, Opcode.FCMPG, 15)).build());
        this.healthColor3 = this.sgGeneral.add(new ColorSetting.Builder().name("health-stage-3").description("The color on the right of the health gradient.").defaultValue(new SettingColor(15, 255, 15)).build());
        calculateSize();
    }

    private void calculateSize() {
        setSize(175.0d * this.scale.get().doubleValue(), 95.0d * this.scale.get().doubleValue());
    }

    @Override // meteordevelopment.meteorclient.systems.hud.HudElement
    public void render(HudRenderer renderer) {
        renderer.post(() -> {
            Color pingColor;
            Color distColor;
            double x = this.x;
            double y = this.y;
            Color primaryColor = TextHud.getSectionColor(0);
            Color secondaryColor = TextHud.getSectionColor(1);
            if (isInEditor()) {
                this.playerEntity = MeteorClient.mc.field_1724;
            } else {
                this.playerEntity = TargetUtils.getPlayerTarget(this.range.get().doubleValue(), SortPriority.LowestDistance);
            }
            if (this.playerEntity != null || isInEditor()) {
                Renderer2D.COLOR.begin();
                Renderer2D.COLOR.quad(x, y, getWidth(), getHeight(), this.backgroundColor.get());
                if (this.playerEntity == null) {
                    if (isInEditor()) {
                        renderer.line(x, y, x + ((double) getWidth()), y + ((double) getHeight()), Color.GRAY);
                        renderer.line(x + ((double) getWidth()), y, x, y + ((double) getHeight()), Color.GRAY);
                        Renderer2D.COLOR.render();
                        return;
                    }
                    return;
                }
                Renderer2D.COLOR.render();
                class_490.method_2486(renderer.drawContext, (int) x, (int) y, (int) (x + (25.0d * this.scale.get().doubleValue())), (int) (y + (66.0d * this.scale.get().doubleValue())), (int) (30.0d * this.scale.get().doubleValue()), 0.0f, -class_3532.method_15393(this.playerEntity.method_36454()), -this.playerEntity.method_36455(), this.playerEntity);
                double x2 = x + (50.0d * this.scale.get().doubleValue());
                double y2 = y + (5.0d * this.scale.get().doubleValue());
                String nameText = this.playerEntity.method_5477().getString();
                Color nameColor = PlayerUtils.getPlayerColor(this.playerEntity, primaryColor);
                int ping = EntityUtils.getPing(this.playerEntity);
                String pingText = ping + "ms";
                if (ping <= 75) {
                    pingColor = this.pingColor1.get();
                } else {
                    pingColor = ping <= 200 ? this.pingColor2.get() : this.pingColor3.get();
                }
                double dist = isInEditor() ? 0.0d : Math.round(((double) MeteorClient.mc.field_1724.method_5739(this.playerEntity)) * 100.0d) / 100.0d;
                String distText = dist + "m";
                if (dist <= 10.0d) {
                    distColor = this.distColor1.get();
                } else {
                    distColor = dist <= 50.0d ? this.distColor2.get() : this.distColor3.get();
                }
                String friendText = "Unknown";
                Color friendColor = primaryColor;
                if (Friends.get().isFriend(this.playerEntity)) {
                    friendText = "Friend";
                    friendColor = Config.get().friendColor.get();
                } else {
                    boolean naked = true;
                    for (int position = 3; position >= 0; position--) {
                        if (!getItem(position).method_7960()) {
                            naked = false;
                        }
                    }
                    if (naked) {
                        friendText = "Naked";
                        friendColor = GREEN;
                    } else {
                        boolean threat = false;
                        for (int position2 = 5; position2 >= 0; position2--) {
                            class_1799 itemStack = getItem(position2);
                            if (itemStack.method_31573(class_3489.field_42611) || itemStack.method_7909() == class_1802.field_8301 || itemStack.method_7909() == class_1802.field_23141 || (itemStack.method_7909() instanceof class_1748)) {
                                threat = true;
                            }
                        }
                        if (threat) {
                            friendText = "Threat";
                            friendColor = RED;
                        }
                    }
                }
                TextRenderer.get().begin(0.45d * this.scale.get().doubleValue(), false, true);
                double breakWidth = TextRenderer.get().getWidth(" | ");
                double pingWidth = TextRenderer.get().getWidth(pingText);
                double friendWidth = TextRenderer.get().getWidth(friendText);
                TextRenderer.get().render(nameText, x2, y2, nameColor != null ? nameColor : primaryColor);
                double y3 = y2 + TextRenderer.get().getHeight();
                TextRenderer.get().render(friendText, x2, y3, friendColor);
                if (this.displayPing.get().booleanValue()) {
                    TextRenderer.get().render(" | ", x2 + friendWidth, y3, secondaryColor);
                    TextRenderer.get().render(pingText, x2 + friendWidth + breakWidth, y3, pingColor);
                    if (this.displayDistance.get().booleanValue()) {
                        TextRenderer.get().render(" | ", x2 + friendWidth + breakWidth + pingWidth, y3, secondaryColor);
                        TextRenderer.get().render(distText, x2 + friendWidth + breakWidth + pingWidth + breakWidth, y3, distColor);
                    }
                } else if (this.displayDistance.get().booleanValue()) {
                    TextRenderer.get().render(" | ", x2 + friendWidth, y3, secondaryColor);
                    TextRenderer.get().render(distText, x2 + friendWidth + breakWidth, y3, distColor);
                }
                TextRenderer.get().end();
                double y4 = y3 + (10.0d * this.scale.get().doubleValue());
                int slot = 5;
                Matrix4fStack matrices = RenderSystem.getModelViewStack();
                matrices.pushMatrix();
                matrices.scale(this.scale.get().floatValue(), this.scale.get().floatValue(), 1.0f);
                double x3 = x2 / this.scale.get().doubleValue();
                double y5 = y4 / this.scale.get().doubleValue();
                TextRenderer.get().begin(0.35d, false, true);
                for (int position3 = 0; position3 < 6; position3++) {
                    double armorX = x3 + ((double) (position3 * 20));
                    class_1799 itemStack2 = getItem(slot);
                    renderer.item(itemStack2, (int) (armorX * this.scale.get().doubleValue()), (int) (y5 * this.scale.get().doubleValue()), this.scale.get().floatValue(), true);
                    double armorY = y5 + 18.0d;
                    class_9304 enchantments = class_1890.method_57532(itemStack2);
                    List<ObjectIntPair<class_6880<class_1887>>> enchantmentsToShow = new ArrayList<>();
                    for (Object2IntMap.Entry<class_6880<class_1887>> entry : enchantments.method_57539()) {
                        class_6880 class_6880Var = (class_6880) entry.getKey();
                        Set<class_5321<class_1887>> set = this.displayedEnchantments.get();
                        Objects.requireNonNull(set);
                        if (class_6880Var.method_40224((v1) -> {
                            return r1.contains(v1);
                        })) {
                            enchantmentsToShow.add(new ObjectIntImmutablePair((class_6880) entry.getKey(), entry.getIntValue()));
                        }
                    }
                    for (ObjectIntPair<class_6880<class_1887>> entry2 : enchantmentsToShow) {
                        String enchantName = Utils.getEnchantSimpleName((class_6880) entry2.left(), 3) + " " + entry2.rightInt();
                        double enchX = (armorX + 8.0d) - (TextRenderer.get().getWidth(enchantName) / 2.0d);
                        TextRenderer.get().render(enchantName, enchX, armorY, ((class_6880) entry2.left()).method_40220(class_9636.field_51551) ? RED : this.enchantmentTextColor.get());
                        armorY += TextRenderer.get().getHeight();
                    }
                    slot--;
                }
                TextRenderer.get().end();
                double y6 = (int) (((double) this.y) + (75.0d * this.scale.get().doubleValue()));
                double x4 = ((double) this.x) / this.scale.get().doubleValue();
                double y7 = y6 / this.scale.get().doubleValue();
                double x5 = x4 + 5.0d;
                double y8 = y7 + 5.0d;
                Renderer2D.COLOR.begin();
                Renderer2D.COLOR.boxLines(x5, y8, 165.0d, 11.0d, BLACK);
                Renderer2D.COLOR.render();
                double x6 = x5 + 2.0d;
                double y9 = y8 + 2.0d;
                float maxHealth = this.playerEntity.method_6063();
                int maxTotal = (int) (maxHealth + 16);
                int totalHealthWidth = (int) ((161.0f * maxHealth) / maxTotal);
                int totalAbsorbWidth = (Opcode.IF_ICMPLT * 16) / maxTotal;
                float health = this.playerEntity.method_6032();
                float absorb = this.playerEntity.method_6067();
                double healthPercent = health / maxHealth;
                double absorbPercent = absorb / 16;
                int healthWidth = (int) (((double) totalHealthWidth) * healthPercent);
                int absorbWidth = (int) (((double) totalAbsorbWidth) * absorbPercent);
                Renderer2D.COLOR.begin();
                Renderer2D.COLOR.quad(x6, y9, healthWidth, 7.0d, this.healthColor1.get(), this.healthColor2.get(), this.healthColor2.get(), this.healthColor1.get());
                Renderer2D.COLOR.quad(x6 + ((double) healthWidth), y9, absorbWidth, 7.0d, this.healthColor2.get(), this.healthColor3.get(), this.healthColor3.get(), this.healthColor2.get());
                Renderer2D.COLOR.render();
                matrices.popMatrix();
            }
        });
    }

    private class_1799 getItem(int i) {
        if (isInEditor()) {
            switch (i) {
            }
            return class_1799.field_8037;
        }
        if (this.playerEntity == null) {
            return class_1799.field_8037;
        }
        switch (i) {
            case 4:
                break;
            case 5:
                break;
        }
        return class_1799.field_8037;
    }
}
