package meteordevelopment.meteorclient.systems.modules.render;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnchantmentListSetting;
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
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.misc.Names;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1304;
import net.minecraft.class_1309;
import net.minecraft.class_1533;
import net.minecraft.class_1541;
import net.minecraft.class_1542;
import net.minecraft.class_1657;
import net.minecraft.class_1701;
import net.minecraft.class_1799;
import net.minecraft.class_1887;
import net.minecraft.class_1890;
import net.minecraft.class_1893;
import net.minecraft.class_1934;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_5321;
import net.minecraft.class_6880;
import net.minecraft.class_9304;
import net.minecraft.class_9636;
import org.joml.Vector3d;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/Nametags.class */
public class Nametags extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgPlayers;
    private final SettingGroup sgItems;
    private final SettingGroup sgRender;
    private final Setting<Set<class_1299<?>>> entities;
    private final Setting<Double> scale;
    private final Setting<Boolean> ignoreSelf;
    private final Setting<Boolean> ignoreFriends;
    private final Setting<Boolean> ignoreBots;
    private final Setting<Boolean> culling;
    private final Setting<Double> maxCullRange;
    private final Setting<Integer> maxCullCount;
    private final Setting<Boolean> displayHealth;
    private final Setting<Boolean> displayGameMode;
    private final Setting<Boolean> displayDistance;
    private final Setting<Boolean> displayPing;
    private final Setting<Boolean> displayItems;
    private final Setting<Double> itemSpacing;
    private final Setting<Boolean> ignoreEmpty;
    private final Setting<Durability> itemDurability;
    private final Setting<Boolean> displayEnchants;
    private final Setting<Set<class_5321<class_1887>>> shownEnchantments;
    private final Setting<Position> enchantPos;
    private final Setting<Integer> enchantLength;
    private final Setting<Double> enchantTextScale;
    private final Setting<Boolean> itemCount;
    private final Setting<SettingColor> background;
    private final Setting<SettingColor> nameColor;
    private final Setting<SettingColor> pingColor;
    private final Setting<SettingColor> gamemodeColor;
    private final Setting<DistanceColorMode> distanceColorMode;
    private final Setting<SettingColor> distanceColor;
    private final Color WHITE;
    private final Color RED;
    private final Color AMBER;
    private final Color GREEN;
    private final Color GOLD;
    private final Vector3d pos;
    private final double[] itemWidths;
    private final List<class_1297> entityList;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/Nametags$DistanceColorMode.class */
    public enum DistanceColorMode {
        Gradient,
        Flat
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/Nametags$Durability.class */
    public enum Durability {
        None,
        Total,
        Percentage
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/Nametags$Position.class */
    public enum Position {
        Above,
        OnTop
    }

    public Nametags() {
        super(Categories.Render, "nametags", "Displays customizable nametags above players, items and other entities.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgPlayers = this.settings.createGroup("Players");
        this.sgItems = this.settings.createGroup("Items");
        this.sgRender = this.settings.createGroup("Render");
        this.entities = this.sgGeneral.add(new EntityTypeListSetting.Builder().name("entities").description("Select entities to draw nametags on.").defaultValue(class_1299.field_6097, class_1299.field_6052).build());
        this.scale = this.sgGeneral.add(new DoubleSetting.Builder().name("scale").description("The scale of the nametag.").defaultValue(1.1d).min(0.1d).build());
        this.ignoreSelf = this.sgGeneral.add(new BoolSetting.Builder().name("ignore-self").description("Ignore yourself when in third person or freecam.").defaultValue(true).build());
        this.ignoreFriends = this.sgGeneral.add(new BoolSetting.Builder().name("ignore-friends").description("Ignore rendering nametags for friends.").defaultValue(false).build());
        this.ignoreBots = this.sgGeneral.add(new BoolSetting.Builder().name("ignore-bots").description("Only render non-bot nametags.").defaultValue(true).build());
        this.culling = this.sgGeneral.add(new BoolSetting.Builder().name("culling").description("Only render a certain number of nametags at a certain distance.").defaultValue(false).build());
        SettingGroup settingGroup = this.sgGeneral;
        DoubleSetting.Builder builderSliderMax = new DoubleSetting.Builder().name("culling-range").description("Only render nametags within this distance of your player.").defaultValue(20.0d).min(0.0d).sliderMax(200.0d);
        Setting<Boolean> setting = this.culling;
        Objects.requireNonNull(setting);
        this.maxCullRange = settingGroup.add(builderSliderMax.visible(setting::get).build());
        SettingGroup settingGroup2 = this.sgGeneral;
        IntSetting.Builder builderSliderRange = new IntSetting.Builder().name("culling-count").description("Only render this many nametags.").defaultValue(50).min(1).sliderRange(1, 100);
        Setting<Boolean> setting2 = this.culling;
        Objects.requireNonNull(setting2);
        this.maxCullCount = settingGroup2.add(builderSliderRange.visible(setting2::get).build());
        this.displayHealth = this.sgPlayers.add(new BoolSetting.Builder().name("health").description("Shows the player's health.").defaultValue(true).build());
        this.displayGameMode = this.sgPlayers.add(new BoolSetting.Builder().name("gamemode").description("Shows the player's GameMode.").defaultValue(false).build());
        this.displayDistance = this.sgPlayers.add(new BoolSetting.Builder().name("distance").description("Shows the distance between you and the player.").defaultValue(false).build());
        this.displayPing = this.sgPlayers.add(new BoolSetting.Builder().name("ping").description("Shows the player's ping.").defaultValue(true).build());
        this.displayItems = this.sgPlayers.add(new BoolSetting.Builder().name("items").description("Displays armor and hand items above the name tags.").defaultValue(true).build());
        SettingGroup settingGroup3 = this.sgPlayers;
        DoubleSetting.Builder builderRange = new DoubleSetting.Builder().name("item-spacing").description("The spacing between items.").defaultValue(2.0d).range(0.0d, 10.0d);
        Setting<Boolean> setting3 = this.displayItems;
        Objects.requireNonNull(setting3);
        this.itemSpacing = settingGroup3.add(builderRange.visible(setting3::get).build());
        SettingGroup settingGroup4 = this.sgPlayers;
        BoolSetting.Builder builderDefaultValue = new BoolSetting.Builder().name("ignore-empty-slots").description("Doesn't add spacing where an empty item stack would be.").defaultValue(true);
        Setting<Boolean> setting4 = this.displayItems;
        Objects.requireNonNull(setting4);
        this.ignoreEmpty = settingGroup4.add(builderDefaultValue.visible(setting4::get).build());
        SettingGroup settingGroup5 = this.sgPlayers;
        EnumSetting.Builder builderDefaultValue2 = new EnumSetting.Builder().name("durability").description("Displays item durability as either a total, percentage, or neither.").defaultValue(Durability.None);
        Setting<Boolean> setting5 = this.displayItems;
        Objects.requireNonNull(setting5);
        this.itemDurability = settingGroup5.add(builderDefaultValue2.visible(setting5::get).build());
        SettingGroup settingGroup6 = this.sgPlayers;
        BoolSetting.Builder builderDefaultValue3 = new BoolSetting.Builder().name("display-enchants").description("Displays item enchantments on the items.").defaultValue(false);
        Setting<Boolean> setting6 = this.displayItems;
        Objects.requireNonNull(setting6);
        this.displayEnchants = settingGroup6.add(builderDefaultValue3.visible(setting6::get).build());
        this.shownEnchantments = this.sgPlayers.add(new EnchantmentListSetting.Builder().name("shown-enchantments").description("The enchantments that are shown on nametags.").visible(() -> {
            return this.displayItems.get().booleanValue() && this.displayEnchants.get().booleanValue();
        }).defaultValue(class_1893.field_9111, class_1893.field_9107, class_1893.field_9095, class_1893.field_9096).build());
        this.enchantPos = this.sgPlayers.add(new EnumSetting.Builder().name("enchantment-position").description("Where the enchantments are rendered.").defaultValue(Position.Above).visible(() -> {
            return this.displayItems.get().booleanValue() && this.displayEnchants.get().booleanValue();
        }).build());
        this.enchantLength = this.sgPlayers.add(new IntSetting.Builder().name("enchant-name-length").description("The length enchantment names are trimmed to.").defaultValue(3).range(1, 5).sliderRange(1, 5).visible(() -> {
            return this.displayItems.get().booleanValue() && this.displayEnchants.get().booleanValue();
        }).build());
        this.enchantTextScale = this.sgPlayers.add(new DoubleSetting.Builder().name("enchant-text-scale").description("The scale of the enchantment text.").defaultValue(1.0d).range(0.1d, 2.0d).sliderRange(0.1d, 2.0d).visible(() -> {
            return this.displayItems.get().booleanValue() && this.displayEnchants.get().booleanValue();
        }).build());
        this.itemCount = this.sgItems.add(new BoolSetting.Builder().name("show-count").description("Displays the number of items in the stack.").defaultValue(true).build());
        this.background = this.sgRender.add(new ColorSetting.Builder().name("background-color").description("The color of the nametag background.").defaultValue(new SettingColor(0, 0, 0, 75)).build());
        this.nameColor = this.sgRender.add(new ColorSetting.Builder().name("name-color").description("The color of the nametag names.").defaultValue(new SettingColor()).build());
        SettingGroup settingGroup7 = this.sgRender;
        ColorSetting.Builder builderDefaultValue4 = new ColorSetting.Builder().name("ping-color").description("The color of the nametag ping.").defaultValue(new SettingColor(20, Opcode.TABLESWITCH, Opcode.TABLESWITCH));
        Setting<Boolean> setting7 = this.displayPing;
        Objects.requireNonNull(setting7);
        this.pingColor = settingGroup7.add(builderDefaultValue4.visible(setting7::get).build());
        SettingGroup settingGroup8 = this.sgRender;
        ColorSetting.Builder builderDefaultValue5 = new ColorSetting.Builder().name("gamemode-color").description("The color of the nametag gamemode.").defaultValue(new SettingColor(232, Opcode.INVOKEINTERFACE, 35));
        Setting<Boolean> setting8 = this.displayGameMode;
        Objects.requireNonNull(setting8);
        this.gamemodeColor = settingGroup8.add(builderDefaultValue5.visible(setting8::get).build());
        SettingGroup settingGroup9 = this.sgRender;
        EnumSetting.Builder builderDefaultValue6 = new EnumSetting.Builder().name("distance-color-mode").description("The mode to color the nametag distance with.").defaultValue(DistanceColorMode.Gradient);
        Setting<Boolean> setting9 = this.displayDistance;
        Objects.requireNonNull(setting9);
        this.distanceColorMode = settingGroup9.add(builderDefaultValue6.visible(setting9::get).build());
        this.distanceColor = this.sgRender.add(new ColorSetting.Builder().name("distance-color").description("The color of the nametag distance.").defaultValue(new SettingColor(Opcode.FCMPG, Opcode.FCMPG, Opcode.FCMPG)).visible(() -> {
            return this.displayDistance.get().booleanValue() && this.distanceColorMode.get() == DistanceColorMode.Flat;
        }).build());
        this.WHITE = new Color(255, 255, 255);
        this.RED = new Color(255, 25, 25);
        this.AMBER = new Color(255, Opcode.LMUL, 25);
        this.GREEN = new Color(25, 252, 25);
        this.GOLD = new Color(232, Opcode.INVOKEINTERFACE, 35);
        this.pos = new Vector3d();
        this.itemWidths = new double[6];
        this.entityList = new ArrayList();
    }

    private static String ticksToTime(int ticks) {
        if (ticks > 72000) {
            int h = (ticks / 20) / 3600;
            return h + " h";
        }
        if (ticks > 1200) {
            int m = (ticks / 20) / 60;
            return m + " m";
        }
        int s = ticks / 20;
        int ms = (ticks % 20) / 2;
        return s + "." + ms + " s";
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        this.entityList.clear();
        boolean freecamNotActive = !Modules.get().isActive(Freecam.class);
        boolean notThirdPerson = this.mc.field_1690.method_31044().method_31034();
        class_243 cameraPos = this.mc.field_1773.method_19418().method_19326();
        for (class_1657 class_1657Var : this.mc.field_1687.method_18112()) {
            class_1299<?> type = class_1657Var.method_5864();
            if (this.entities.get().contains(type)) {
                if (type == class_1299.field_6097) {
                    if ((!this.ignoreSelf.get().booleanValue() && (!freecamNotActive || !notThirdPerson)) || class_1657Var != this.mc.field_1724) {
                        if (EntityUtils.getGameMode(class_1657Var) != null || !this.ignoreBots.get().booleanValue()) {
                            if (!Friends.get().isFriend(class_1657Var) || !this.ignoreFriends.get().booleanValue()) {
                            }
                        }
                    }
                }
                if (!this.culling.get().booleanValue() || PlayerUtils.isWithinCamera((class_1297) class_1657Var, this.maxCullRange.get().doubleValue())) {
                    this.entityList.add(class_1657Var);
                }
            }
        }
        this.entityList.sort(Comparator.comparing(e -> {
            return Double.valueOf(e.method_5707(cameraPos));
        }));
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: java.lang.MatchException */
    @EventHandler
    private void onRender2D(Render2DEvent event) throws MatchException {
        int count = getRenderCount();
        boolean shadow = Config.get().customFont.get().booleanValue();
        for (int i = count - 1; i > -1; i--) {
            class_1542 class_1542Var = (class_1297) this.entityList.get(i);
            Utils.set(this.pos, class_1542Var, event.tickDelta);
            this.pos.add(0.0d, getHeight(class_1542Var), 0.0d);
            class_1299<?> type = class_1542Var.method_5864();
            if (NametagUtils.to2D(this.pos, this.scale.get().doubleValue())) {
                if (type == class_1299.field_6097) {
                    renderNametagPlayer(event, (class_1657) class_1542Var, shadow);
                } else if (type == class_1299.field_6052) {
                    renderNametagItem(class_1542Var.method_6983(), shadow);
                } else if (type == class_1299.field_6043 || type == class_1299.field_28401) {
                    renderNametagItem(((class_1533) class_1542Var).method_6940(), shadow);
                } else if (type == class_1299.field_6063) {
                    renderTntNametag(ticksToTime(((class_1541) class_1542Var).method_6969()), shadow);
                } else if (type == class_1299.field_6053 && ((class_1701) class_1542Var).method_7578()) {
                    renderTntNametag(ticksToTime(((class_1701) class_1542Var).method_7577()), shadow);
                } else if (class_1542Var instanceof class_1309) {
                    renderGenericLivingNametag((class_1309) class_1542Var, shadow);
                } else {
                    renderGenericNametag(class_1542Var, shadow);
                }
            }
        }
    }

    private int getRenderCount() {
        int count = this.culling.get().booleanValue() ? this.maxCullCount.get().intValue() : this.entityList.size();
        return class_3532.method_15340(count, 0, this.entityList.size());
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public String getInfoString() {
        return Integer.toString(getRenderCount());
    }

    private double getHeight(class_1297 entity) {
        double height = entity.method_18381(entity.method_18376());
        return (entity.method_5864() == class_1299.field_6052 || entity.method_5864() == class_1299.field_6043 || entity.method_5864() == class_1299.field_28401) ? height + 0.2d : height + 0.5d;
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: java.lang.MatchException */
    private void renderNametagPlayer(Render2DEvent event, class_1657 player, boolean shadow) throws MatchException {
        Color healthColor;
        double size;
        double width;
        String string;
        String str;
        TextRenderer text = TextRenderer.get();
        NametagUtils.begin(this.pos);
        class_1934 gm = EntityUtils.getGameMode(player);
        String gmText = "BOT";
        if (gm != null) {
            switch (AnonymousClass1.$SwitchMap$net$minecraft$world$GameMode[gm.ordinal()]) {
                case 1:
                    str = "Sp";
                    break;
                case 2:
                    str = "S";
                    break;
                case 3:
                    str = "C";
                    break;
                case 4:
                    str = "A";
                    break;
                default:
                    throw new MatchException((String) null, (Throwable) null);
            }
            gmText = str;
        }
        String gmText2 = "[" + gmText + "] ";
        Color nameColor = PlayerUtils.getPlayerColor(player, this.nameColor.get());
        String name = player.method_5477().getString();
        float absorption = player.method_6067();
        int health = Math.round(player.method_6032() + absorption);
        double healthPercentage = health / (player.method_6063() + absorption);
        String healthText = " " + health;
        if (healthPercentage <= 0.333d) {
            healthColor = this.RED;
        } else {
            healthColor = healthPercentage <= 0.666d ? this.AMBER : this.GREEN;
        }
        int ping = EntityUtils.getPing(player);
        String pingText = " [" + ping + "ms]";
        double dist = Math.round(PlayerUtils.distanceToCamera(player) * 10.0d) / 10.0d;
        String distText = " " + dist + "m";
        double gmWidth = text.getWidth(gmText2, shadow);
        double nameWidth = text.getWidth(name, shadow);
        double healthWidth = text.getWidth(healthText, shadow);
        double pingWidth = text.getWidth(pingText, shadow);
        double distWidth = text.getWidth(distText, shadow);
        double width2 = nameWidth;
        boolean renderPlayerDistance = player != this.mc.field_1719 || Modules.get().isActive(Freecam.class);
        if (this.displayHealth.get().booleanValue()) {
            width2 += healthWidth;
        }
        if (this.displayGameMode.get().booleanValue()) {
            width2 += gmWidth;
        }
        if (this.displayPing.get().booleanValue()) {
            width2 += pingWidth;
        }
        if (this.displayDistance.get().booleanValue() && renderPlayerDistance) {
            width2 += distWidth;
        }
        double widthHalf = width2 / 2.0d;
        double heightDown = text.getHeight(shadow);
        drawBg(-widthHalf, -heightDown, width2, heightDown);
        text.beginBig();
        double hX = -widthHalf;
        double hY = -heightDown;
        if (this.displayGameMode.get().booleanValue()) {
            hX = text.render(gmText2, hX, hY, this.gamemodeColor.get(), shadow);
        }
        double hX2 = text.render(name, hX, hY, nameColor, shadow);
        if (this.displayHealth.get().booleanValue()) {
            hX2 = text.render(healthText, hX2, hY, healthColor, shadow);
        }
        if (this.displayPing.get().booleanValue()) {
            hX2 = text.render(pingText, hX2, hY, this.pingColor.get(), shadow);
        }
        if (this.displayDistance.get().booleanValue() && renderPlayerDistance) {
            switch (this.distanceColorMode.get()) {
                case Gradient:
                    text.render(distText, hX2, hY, EntityUtils.getColorFromDistance(player), shadow);
                    break;
                case Flat:
                    text.render(distText, hX2, hY, this.distanceColor.get(), shadow);
                    break;
            }
        }
        text.end();
        if (this.displayItems.get().booleanValue()) {
            Arrays.fill(this.itemWidths, 0.0d);
            boolean hasItems = false;
            int maxEnchantCount = 0;
            for (int i = 0; i < 6; i++) {
                class_1799 itemStack = getItem(player, i);
                if (this.itemWidths[i] == 0.0d && (!this.ignoreEmpty.get().booleanValue() || !itemStack.method_7960())) {
                    this.itemWidths[i] = 32.0d + this.itemSpacing.get().doubleValue();
                }
                if (!itemStack.method_7960()) {
                    hasItems = true;
                }
                if (this.displayEnchants.get().booleanValue()) {
                    class_9304 enchantments = class_1890.method_57532(itemStack);
                    int size2 = 0;
                    for (class_6880<class_1887> enchantment : enchantments.method_57534()) {
                        if (!enchantment.method_40230().isPresent() || this.shownEnchantments.get().contains(enchantment.method_40230().get())) {
                            this.itemWidths[i] = Math.max(this.itemWidths[i], text.getWidth(Utils.getEnchantSimpleName(enchantment, this.enchantLength.get().intValue()) + " " + enchantments.method_57536(enchantment), shadow) / 2.0d);
                            size2++;
                        }
                    }
                    maxEnchantCount = Math.max(maxEnchantCount, size2);
                }
            }
            double itemsHeight = hasItems ? 32 : 0;
            double itemWidthTotal = 0.0d;
            for (double w : this.itemWidths) {
                itemWidthTotal += w;
            }
            double itemWidthHalf = itemWidthTotal / 2.0d;
            double y = ((-heightDown) - 7.0d) - itemsHeight;
            double x = -itemWidthHalf;
            for (int i2 = 0; i2 < 6; i2++) {
                class_1799 stack = getItem(player, i2);
                RenderUtils.drawItem(event.drawContext, stack, (int) x, (int) y, 2.0f, true);
                if (stack.method_7963() && this.itemDurability.get() != Durability.None) {
                    text.begin(0.75d, false, true);
                    switch (this.itemDurability.get().ordinal()) {
                        case 1:
                            string = Integer.toString(stack.method_7936() - stack.method_7919());
                            break;
                        case 2:
                            string = String.format("%.0f%%", Float.valueOf(((stack.method_7936() - stack.method_7919()) * 100.0f) / stack.method_7936()));
                            break;
                        default:
                            string = "err";
                            break;
                    }
                    String damageText = string;
                    Color damageColor = new Color(stack.method_31580());
                    text.render(damageText, (int) x, (int) y, damageColor.a(255), true);
                    text.end();
                }
                if (maxEnchantCount > 0 && this.displayEnchants.get().booleanValue()) {
                    text.begin(0.5d * this.enchantTextScale.get().doubleValue(), false, true);
                    class_9304 enchantments2 = class_1890.method_57532(stack);
                    Object2IntOpenHashMap object2IntOpenHashMap = new Object2IntOpenHashMap();
                    for (class_6880<class_1887> enchantment2 : enchantments2.method_57534()) {
                        Set<class_5321<class_1887>> set = this.shownEnchantments.get();
                        Objects.requireNonNull(set);
                        if (enchantment2.method_40224((v1) -> {
                            return r1.contains(v1);
                        })) {
                            object2IntOpenHashMap.put(enchantment2, enchantments2.method_57536(enchantment2));
                        }
                    }
                    double aW = this.itemWidths[i2];
                    double enchantY = 0.0d;
                    switch (this.enchantPos.get()) {
                        case Above:
                            size = -(((double) (object2IntOpenHashMap.size() + 1)) * text.getHeight(shadow));
                            break;
                        case OnTop:
                            size = (itemsHeight - (((double) object2IntOpenHashMap.size()) * text.getHeight(shadow))) / 2.0d;
                            break;
                        default:
                            throw new MatchException((String) null, (Throwable) null);
                    }
                    double addY = size;
                    ObjectIterator it = Object2IntMaps.fastIterable(object2IntOpenHashMap).iterator();
                    while (it.hasNext()) {
                        Object2IntMap.Entry<class_6880<class_1887>> entry = (Object2IntMap.Entry) it.next();
                        String enchantName = Utils.getEnchantSimpleName((class_6880) entry.getKey(), this.enchantLength.get().intValue()) + " " + entry.getIntValue();
                        Color enchantColor = this.WHITE;
                        if (((class_6880) entry.getKey()).method_40220(class_9636.field_51551)) {
                            enchantColor = this.RED;
                        }
                        switch (this.enchantPos.get()) {
                            case Above:
                                width = (x + (aW / 2.0d)) - (text.getWidth(enchantName, shadow) / 2.0d);
                                break;
                            case OnTop:
                                width = x + ((aW - text.getWidth(enchantName, shadow)) / 2.0d);
                                break;
                            default:
                                throw new MatchException((String) null, (Throwable) null);
                        }
                        double enchantX = width;
                        text.render(enchantName, enchantX, y + addY + enchantY, enchantColor, shadow);
                        enchantY += text.getHeight(shadow);
                    }
                    text.end();
                }
                x += this.itemWidths[i2];
            }
        } else if (this.displayEnchants.get().booleanValue()) {
            this.displayEnchants.set(false);
        }
        NametagUtils.end();
    }

    /* JADX INFO: renamed from: meteordevelopment.meteorclient.systems.modules.render.Nametags$1, reason: invalid class name */
    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/Nametags$1.class */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$net$minecraft$world$GameMode = new int[class_1934.values().length];

        static {
            try {
                $SwitchMap$net$minecraft$world$GameMode[class_1934.field_9219.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$net$minecraft$world$GameMode[class_1934.field_9215.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$net$minecraft$world$GameMode[class_1934.field_9220.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$net$minecraft$world$GameMode[class_1934.field_9216.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    private void renderNametagItem(class_1799 stack, boolean shadow) {
        if (stack.method_7960()) {
            return;
        }
        TextRenderer text = TextRenderer.get();
        NametagUtils.begin(this.pos);
        String name = Names.get(stack);
        String count = " x" + stack.method_7947();
        double nameWidth = text.getWidth(name, shadow);
        double countWidth = text.getWidth(count, shadow);
        double heightDown = text.getHeight(shadow);
        double width = nameWidth;
        if (this.itemCount.get().booleanValue()) {
            width += countWidth;
        }
        double widthHalf = width / 2.0d;
        drawBg(-widthHalf, -heightDown, width, heightDown);
        text.beginBig();
        double hX = -widthHalf;
        double hY = -heightDown;
        double hX2 = text.render(name, hX, hY, this.nameColor.get(), shadow);
        if (this.itemCount.get().booleanValue()) {
            text.render(count, hX2, hY, this.GOLD, shadow);
        }
        text.end();
        NametagUtils.end();
    }

    private void renderGenericLivingNametag(class_1309 entity, boolean shadow) {
        Color healthColor;
        TextRenderer text = TextRenderer.get();
        NametagUtils.begin(this.pos);
        String nameText = entity.method_5864().method_5897().getString() + " ";
        float absorption = entity.method_6067();
        int health = Math.round(entity.method_6032() + absorption);
        double healthPercentage = health / (entity.method_6063() + absorption);
        String healthText = String.valueOf(health);
        if (healthPercentage <= 0.333d) {
            healthColor = this.RED;
        } else {
            healthColor = healthPercentage <= 0.666d ? this.AMBER : this.GREEN;
        }
        double nameWidth = text.getWidth(nameText, shadow);
        double healthWidth = text.getWidth(healthText, shadow);
        double heightDown = text.getHeight(shadow);
        double width = nameWidth + healthWidth;
        double widthHalf = width / 2.0d;
        drawBg(-widthHalf, -heightDown, width, heightDown);
        text.beginBig();
        double hX = -widthHalf;
        double hY = -heightDown;
        text.render(healthText, text.render(nameText, hX, hY, this.nameColor.get(), shadow), hY, healthColor, shadow);
        text.end();
        NametagUtils.end();
    }

    private void renderGenericNametag(class_1297 entity, boolean shadow) {
        TextRenderer text = TextRenderer.get();
        NametagUtils.begin(this.pos);
        String nameText = entity.method_5864().method_5897().getString();
        double nameWidth = text.getWidth(nameText, shadow);
        double heightDown = text.getHeight(shadow);
        double widthHalf = nameWidth / 2.0d;
        drawBg(-widthHalf, -heightDown, nameWidth, heightDown);
        text.beginBig();
        double hX = -widthHalf;
        double hY = -heightDown;
        text.render(nameText, hX, hY, this.nameColor.get(), shadow);
        text.end();
        NametagUtils.end();
    }

    private void renderTntNametag(String fuseText, boolean shadow) {
        TextRenderer text = TextRenderer.get();
        NametagUtils.begin(this.pos);
        double width = text.getWidth(fuseText, shadow);
        double heightDown = text.getHeight(shadow);
        double widthHalf = width / 2.0d;
        drawBg(-widthHalf, -heightDown, width, heightDown);
        text.beginBig();
        double hX = -widthHalf;
        double hY = -heightDown;
        text.render(fuseText, hX, hY, this.nameColor.get(), shadow);
        text.end();
        NametagUtils.end();
    }

    private class_1799 getItem(class_1657 entity, int index) {
        switch (index) {
            case 0:
                return entity.method_6047();
            case 1:
                return entity.method_6118(class_1304.field_6169);
            case 2:
                return entity.method_6118(class_1304.field_6174);
            case 3:
                return entity.method_6118(class_1304.field_6172);
            case 4:
                return entity.method_6118(class_1304.field_6166);
            case 5:
                return entity.method_6079();
            default:
                return class_1799.field_8037;
        }
    }

    private void drawBg(double x, double y, double width, double height) {
        Renderer2D.COLOR.begin();
        Renderer2D.COLOR.quad(x - 1.0d, y - 1.0d, width + 2.0d, height + 2.0d, this.background.get());
        Renderer2D.COLOR.render();
    }

    public boolean excludeBots() {
        return this.ignoreBots.get().booleanValue();
    }

    public boolean playerNametags() {
        return isActive() && this.entities.get().contains(class_1299.field_6097);
    }
}
