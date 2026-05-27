
/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.render;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IPlayerInventory;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.NameProtect;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.misc.Names;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import org.joml.Vector3d;

import java.util.*;

public class Nametags extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgPlayers = settings.createGroup("Players");
    private final SettingGroup sgItems = settings.createGroup("Items");
    private final SettingGroup sgRender = settings.createGroup("Render");

    // General

    private final Setting<Set<EntityType<?>>> entities = sgGeneral.add(new EntityTypeListSetting.Builder()
        .name("entities")
        .description("Select entities to draw nametags on.")
        .defaultValue(EntityType.PLAYER, EntityType.ITEM)
        .build()
    );

    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("scale")
        .description("The scale of the nametag.")
        .defaultValue(1.1)
        .min(0.1)
        .build()
    );

    private final Setting<Boolean> ignoreSelf = sgGeneral.add(new BoolSetting.Builder()
        .name("ignore-self")
        .description("Ignore yourself when in third person or freecam.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> ignoreFriends = sgGeneral.add(new BoolSetting.Builder()
        .name("ignore-friends")
        .description("Ignore rendering nametags for friends.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> ignoreBots = sgGeneral.add(new BoolSetting.Builder()
        .name("ignore-bots")
        .description("Only render non-bot nametags.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> culling = sgGeneral.add(new BoolSetting.Builder()
        .name("culling")
        .description("Only render a certain number of nametags at a certain distance.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Double> maxCullRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("culling-range")
        .description("Only render nametags within this distance of your player.")
        .defaultValue(20)
        .min(0)
        .sliderMax(200)
        .visible(culling::get)
        .build()
    );

    private final Setting<Integer> maxCullCount = sgGeneral.add(new IntSetting.Builder()
        .name("culling-count")
        .description("Only render this many nametags.")
        .defaultValue(50)
        .min(1)
        .sliderRange(1, 100)
        .visible(culling::get)
        .build()
    );

    //Players

    private final Setting<Boolean> displayHealth = sgPlayers.add(new BoolSetting.Builder()
        .name("health")
        .description("Shows the player's health.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> displayTotemPops = sgPlayers.add(new BoolSetting.Builder()
        .name("display-totem-pops")
        .description("Shows the player's totem pops.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> displayGameMode = sgPlayers.add(new BoolSetting.Builder()
        .name("gamemode")
        .description("Shows the player's GameMode.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> displayDistance = sgPlayers.add(new BoolSetting.Builder()
        .name("distance")
        .description("Shows the distance between you and the player.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> displayPing = sgPlayers.add(new BoolSetting.Builder()
        .name("ping")
        .description("Shows the player's ping.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> displayItems = sgPlayers.add(new BoolSetting.Builder()
        .name("items")
        .description("Displays armor and hand items above the name tags.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showTrackedHotbar = sgPlayers.add(new BoolSetting.Builder()
        .name("show-hotbar")
        .description("Enables tracked hotbar rendering and hotbar settings.")
        .defaultValue(true)
        .visible(displayItems::get)
        .build()
    );

    private final Setting<Keybind> showHotbarKey = sgPlayers.add(new KeybindSetting.Builder()
        .name("show-hotbar-key")
        .description("Displays the tracked hotbar instead of armor when pressed.")
        .visible(() -> displayItems.get() && showTrackedHotbar.get())
        .build()
    );

    private final Setting<Integer> hotbarOffhandMargin = sgPlayers.add(new IntSetting.Builder()
        .name("hotbar-offhand-margin")
        .description("Spacing in pixels between offhand (index 0) and hotbar (indices 1-9).")
        .defaultValue(12)
        .range(0, 64)
        .sliderRange(0, 32)
        .visible(() -> displayItems.get() && showTrackedHotbar.get())
        .build()
    );

    private final Setting<Boolean> showNormalItemsWithHotbar = sgPlayers.add(new BoolSetting.Builder()
        .name("show-normal-items-with-hotbar")
        .description("Also render the normal items row above the hotbar row when the hotbar is shown.")
        .defaultValue(false)
        .visible(() -> displayItems.get() && showTrackedHotbar.get())
        .build()
    );

    private final Setting<Boolean> ignoreEmpty = sgPlayers.add(new BoolSetting.Builder()
        .name("ignore-empty-slots")
        .description("Doesn't add spacing where an empty item stack would be.")
        .defaultValue(true)
        .visible(displayItems::get)
        .build()
    );

    private final Setting<Durability> itemDurability = sgPlayers.add(new EnumSetting.Builder<Durability>()
        .name("durability")
        .description("Displays item durability as either a total, percentage, or neither.")
        .defaultValue(Durability.None)
        .visible(displayItems::get)
        .build()
    );

    private final Setting<Boolean> displayEnchants = sgPlayers.add(new BoolSetting.Builder()
        .name("display-enchants")
        .description("Displays item enchantments on the items.")
        .defaultValue(false)
        .visible(displayItems::get)
        .build()
    );

    private final Setting<Set<RegistryKey<Enchantment>>> shownEnchantments = sgPlayers.add(new EnchantmentListSetting.Builder()
        .name("shown-enchantments")
        .description("The enchantments that are shown on nametags.")
        .visible(() -> displayItems.get() && displayEnchants.get())
        .defaultValue(
            Enchantments.PROTECTION,
            Enchantments.BLAST_PROTECTION,
            Enchantments.FIRE_PROTECTION,
            Enchantments.PROJECTILE_PROTECTION
        )
        .build()
    );

    private final Setting<Position> enchantPos = sgPlayers.add(new EnumSetting.Builder<Position>()
        .name("enchantment-position")
        .description("Where the enchantments are rendered.")
        .defaultValue(Position.Above)
        .visible(() -> displayItems.get() && displayEnchants.get())
        .build()
    );

    private final Setting<Integer> enchantLength = sgPlayers.add(new IntSetting.Builder()
        .name("enchant-name-length")
        .description("The length enchantment names are trimmed to.")
        .defaultValue(3)
        .range(1, 5)
        .sliderRange(1, 5)
        .visible(() -> displayItems.get() && displayEnchants.get())
        .build()
    );

    private final Setting<Double> enchantTextScale = sgPlayers.add(new DoubleSetting.Builder()
        .name("enchant-text-scale")
        .description("The scale of the enchantment text.")
        .defaultValue(1)
        .range(0.1, 2)
        .sliderRange(0.1, 2)
        .visible(() -> displayItems.get() && displayEnchants.get())
        .build()
    );

    //Items

    private final Setting<Boolean> itemCount = sgItems.add(new BoolSetting.Builder()
        .name("show-count")
        .description("Displays the number of items in the stack.")
        .defaultValue(true)
        .build()
    );

    // Render

    private final Setting<SettingColor> background = sgRender.add(new ColorSetting.Builder()
        .name("background-color")
        .description("The color of the nametag background.")
        .defaultValue(new SettingColor(0, 0, 0, 75))
        .build()
    );

    private final Setting<SettingColor> nameColor = sgRender.add(new ColorSetting.Builder()
        .name("name-color")
        .description("The color of the nametag names.")
        .defaultValue(new SettingColor())
        .build()
    );

    private final Setting<SettingColor> totemPopsColorColor = sgRender.add(new ColorSetting.Builder()
        .name("totem-pop-color")
        .description("The color of the nametag totem pops.")
        .defaultValue(new SettingColor(225, 120, 20))
        .build()
    );

    private final Setting<SettingColor> pingColor = sgRender.add(new ColorSetting.Builder()
        .name("ping-color")
        .description("The color of the nametag ping.")
        .defaultValue(new SettingColor(20, 170, 170))
        .visible(displayPing::get)
        .build()
    );

    private final Setting<SettingColor> gamemodeColor = sgRender.add(new ColorSetting.Builder()
        .name("gamemode-color")
        .description("The color of the nametag gamemode.")
        .defaultValue(new SettingColor(232, 185, 35))
        .visible(displayGameMode::get)
        .build()
    );

    private final Setting<DistanceColorMode> distanceColorMode = sgRender.add(new EnumSetting.Builder<DistanceColorMode>()
        .name("distance-color-mode")
        .description("The mode to color the nametag distance with.")
        .defaultValue(DistanceColorMode.Gradient)
        .visible(displayDistance::get)
        .build()
    );

    private final Setting<SettingColor> distanceColor = sgRender.add(new ColorSetting.Builder()
        .name("distance-color")
        .description("The color of the nametag distance.")
        .defaultValue(new SettingColor(150, 150, 150))
        .visible(() -> displayDistance.get() && distanceColorMode.get() == DistanceColorMode.Flat)
        .build()
    );

    private final Color WHITE = new Color(255, 255, 255);
    private final Color RED = new Color(255, 25, 25);
    private final Color AMBER = new Color(255, 105, 25);
    private final Color GREEN = new Color(25, 252, 25);
    private final Color GOLD = new Color(232, 185, 35);

    private final Vector3d pos = new Vector3d();
    private final double[] itemWidths = new double[10];
    private final List<ItemStack> items = new ArrayList<>();
    private final Map<UUID, ItemStack[]> trackedHotbars = new HashMap<>();
    private final Map<UUID, Integer> lastTrackedSlots = new HashMap<>();
    private final List<Entity> entityList = new ArrayList<>();
    private boolean showHotbarKeyPressed = false;
    private boolean showHotbarToggled = false;

    public Nametags() {
        super(Categories.Render, "nametags", "Displays customizable nametags above players, items and other entities.");
    }

    private static String ticksToTime(int ticks) {
        if (ticks > 20 * 3600) {
            int h = ticks / 20 / 3600;
            return h + " h";
        } else if (ticks > 20 * 60) {
            int m = ticks / 20 / 60;
            return m + " m";
        } else {
            int s = ticks / 20;
            int ms = (ticks % 20) / 2;
            return s + "." + ms + " s";
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        boolean isPressed = showHotbarKey.get().isPressed();
        if (isPressed && !showHotbarKeyPressed) {
            showHotbarToggled = !showHotbarToggled;
        }
        showHotbarKeyPressed = isPressed;

        entityList.clear();

        boolean freecamNotActive = !Modules.get().isActive(Freecam.class);
        boolean notThirdPerson = mc.options.getPerspective().isFirstPerson();
        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();

        for (Entity entity : mc.world.getEntities()) {
            EntityType<?> type = entity.getType();
            if (!entities.get().contains(type)) continue;

            if (type == EntityType.PLAYER) {
                if ((ignoreSelf.get() || (freecamNotActive && notThirdPerson)) && entity == mc.player) continue;
                if (EntityUtils.getGameMode((PlayerEntity) entity) == null && ignoreBots.get()) continue;
                if (Friends.get().isFriend((PlayerEntity) entity) && ignoreFriends.get()) continue;
            }

            if (!culling.get() || PlayerUtils.isWithinCamera(entity, maxCullRange.get())) {
                entityList.add(entity);
            }
        }

        entityList.sort(Comparator.comparing(e -> e.squaredDistanceTo(cameraPos)));
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        int count = getRenderCount();
        boolean shadow = Config.get().customFont.get();

        for (int i = count - 1; i > -1; i--) {
            Entity entity = entityList.get(i);

            Utils.set(pos, entity, event.tickDelta);
            pos.add(0, getHeight(entity), 0);

            EntityType<?> type = entity.getType();

            if (NametagUtils.to2D(pos, scale.get())) {
                // Update hotbar tracking for players
                if (type == EntityType.PLAYER) {
                    PlayerEntity player = (PlayerEntity) entity;
                    UUID uuid = player.getUuid();

                    if (!player.isRemoved() && !(player.getHealth() <= 0.0F)) {
                        ItemStack[] trackedHotbar = trackedHotbars.computeIfAbsent(uuid, id -> {
                            ItemStack[] newArr = new ItemStack[10];
                            Arrays.fill(newArr, ItemStack.EMPTY);
                            return newArr;
                        });

                        List<ItemStack> currentHotbar = new ArrayList<>(9);
                        for (int s = 0; s < 9; s++) {
                            currentHotbar.add(((IPlayerInventory) player.getInventory()).meteor$getMain().get(s));
                        }

                        Set<Item> insertedThisTick = new HashSet<>();
                        for (ItemStack stack : currentHotbar) {
                            if (stack.isEmpty()) continue;

                            Item item = stack.getItem();
                            boolean found = false;
                            for (int prev = 1; prev <= 9; prev++) {
                                if (!trackedHotbar[prev].isEmpty() && trackedHotbar[prev].getItem() == item) {
                                    trackedHotbar[prev] = stack.copy();
                                    found = true;
                                    break;
                                }
                            }

                            if (!found && insertedThisTick.add(item)) {
                                int prev = lastTrackedSlots.getOrDefault(uuid, 1);
                                int index = prev + 1;
                                if (index > 9) index = 1;
                                trackedHotbar[index] = stack.copy();
                                lastTrackedSlots.put(uuid, index);
                            }
                        }

                        ItemStack offhand = player.getOffHandStack();
                        trackedHotbar[0] = offhand.isEmpty() ? ItemStack.EMPTY : offhand.copy();
                    } else {
                        lastTrackedSlots.remove(uuid);
                        trackedHotbars.remove(uuid);
                    }
                }

                // Render nametags
                if (type == EntityType.PLAYER) renderNametagPlayer(event, (PlayerEntity) entity, shadow);
                else if (type == EntityType.ITEM) renderNametagItem(((ItemEntity) entity).getStack(), shadow);
                else if (type == EntityType.ITEM_FRAME)
                    renderNametagItem(((ItemFrameEntity) entity).getHeldItemStack(), shadow);
                else if (type == EntityType.TNT) renderTntNametag(ticksToTime(((TntEntity) entity).getFuse()), shadow);
                else if (type == EntityType.TNT_MINECART && ((TntMinecartEntity) entity).isPrimed())
                    renderTntNametag(ticksToTime(((TntMinecartEntity) entity).getFuseTicks()), shadow);
                else if (entity instanceof LivingEntity) renderGenericLivingNametag((LivingEntity) entity, shadow);
                else renderGenericNametag(entity, shadow);
            }
        }
    }

    private int getRenderCount() {
        int count = culling.get() ? maxCullCount.get() : entityList.size();
        count = MathHelper.clamp(count, 0, entityList.size());

        return count;
    }

    @Override
    public String getInfoString() {
        return Integer.toString(getRenderCount());
    }

    private double getHeight(Entity entity) {
        double height = entity.getEyeHeight(entity.getPose());

        if (entity.getType() == EntityType.ITEM || entity.getType() == EntityType.ITEM_FRAME) height += 0.2;
        else height += 0.5;

        return height;
    }

    private void renderNametagPlayer(Render2DEvent event, PlayerEntity player, boolean shadow) {
        TextRenderer text = TextRenderer.get();
        NametagUtils.begin(pos, event.drawContext);

        // Gamemode
        GameMode gm = EntityUtils.getGameMode(player);
        String gmText = "BOT";
        if (gm != null) {
            gmText = switch (gm) {
                case SPECTATOR -> "Sp";
                case SURVIVAL -> "S";
                case CREATIVE -> "C";
                case ADVENTURE -> "A";
            };
        }

        gmText = "[" + gmText + "] ";

        // Name
        String name;
        Color nameColor = PlayerUtils.getPlayerColor(player, this.nameColor.get());

        if (player == mc.player) name = Modules.get().get(NameProtect.class).getName(player.getName().getString());
        else name = player.getName().getString();

        // Health
        float absorption = player.getAbsorptionAmount();
        int health = Math.round(player.getHealth() + absorption);
        double healthPercentage = health / (player.getMaxHealth() + absorption);

        String healthText = " " + health;
        Color healthColor;

        if (healthPercentage <= 0.333) healthColor = RED;
        else if (healthPercentage <= 0.666) healthColor = AMBER;
        else healthColor = GREEN;

        // Totem pops
        String totemPopsText = " " + (-MeteorClient.INFO.getPops(player));

        // Ping
        int ping = EntityUtils.getPing(player);
        String pingText = " [" + ping + "ms]";

        // Distance
        double dist = Math.round(PlayerUtils.distanceToCamera(player) * 10.0) / 10.0;
        String distText = " " + dist + "m";

        // Calc widths
        double gmWidth = text.getWidth(gmText, shadow);
        double nameWidth = text.getWidth(name, shadow);
        double healthWidth = text.getWidth(healthText, shadow);
        double totemPopsWidth = text.getWidth(totemPopsText, shadow);
        double pingWidth = text.getWidth(pingText, shadow);
        double distWidth = text.getWidth(distText, shadow);

        double width = nameWidth;

        boolean renderPlayerDistance = player != mc.cameraEntity || Modules.get().isActive(Freecam.class);

        if (displayHealth.get()) width += healthWidth;
        if (displayTotemPops.get() && MeteorClient.INFO.getPops(player) > 0) width += totemPopsWidth;
        if (displayGameMode.get()) width += gmWidth;
        if (displayPing.get()) width += pingWidth;
        if (displayDistance.get() && renderPlayerDistance) width += distWidth;

        double widthHalf = width / 2;
        double heightDown = text.getHeight(shadow);

        drawBg(-widthHalf, -heightDown, width, heightDown);

        // Render texts
        text.beginBig();
        double hX = -widthHalf;
        double hY = -heightDown;

        if (displayGameMode.get()) hX = text.render(gmText, hX, hY, gamemodeColor.get(), shadow);
        hX = text.render(name, hX, hY, nameColor, shadow);

        if (displayHealth.get()) hX = text.render(healthText, hX, hY, healthColor, shadow);
        if (displayTotemPops.get() && MeteorClient.INFO.getPops(player) > 0) hX = text.render(totemPopsText, hX, hY, totemPopsColorColor.get(), shadow);
        if (displayPing.get()) hX = text.render(pingText, hX, hY, pingColor.get(), shadow);
        if (displayDistance.get() && renderPlayerDistance) {
            switch (distanceColorMode.get()) {
                case Flat ->  text.render(distText, hX, hY, distanceColor.get(), shadow);
                case Gradient -> text.render(distText, hX, hY, EntityUtils.getColorFromDistance(player), shadow);
            }
        }

        text.end();

        if (displayItems.get()) {
            boolean hotbarEnabled = showTrackedHotbar.get();
            boolean showHotbar = hotbarEnabled && showHotbarToggled;
            boolean showBoth = showHotbar && showNormalItemsWithHotbar.get();

            if (showBoth) {
                // Build normal row (mainhand + armor + offhand)
                List<ItemStack> normalRow = new ArrayList<>(6);
                normalRow.add(player.getMainHandStack());
                normalRow.add(((IPlayerInventory) player.getInventory()).meteor$getArmor().get(3));
                normalRow.add(((IPlayerInventory) player.getInventory()).meteor$getArmor().get(2));
                normalRow.add(((IPlayerInventory) player.getInventory()).meteor$getArmor().get(1));
                normalRow.add(((IPlayerInventory) player.getInventory()).meteor$getArmor().get(0));
                normalRow.add(player.getOffHandStack());

                // Build hotbar row
                ItemStack[] tracked = trackedHotbars.get(player.getUuid());
                List<ItemStack> hotbarRow = new ArrayList<>(10);
                if (tracked != null) {
                    Collections.addAll(hotbarRow, tracked);
                } else {
                    for (int j = 0; j < 10; j++) hotbarRow.add(ItemStack.EMPTY);
                }

                // Calculate widths for both rows
                double[] widthsNorm = new double[normalRow.size()];
                double[] widthsHB = new double[hotbarRow.size()];
                boolean hasNorm = false;
                boolean hasHB = false;
                int maxEnchantCountNorm = 0;
                int maxEnchantCountHB = 0;

                for (int j = 0; j < normalRow.size(); j++) {
                    ItemStack stack = normalRow.get(j);
                    if (!ignoreEmpty.get() || !stack.isEmpty()) widthsNorm[j] = 32;
                    if (!stack.isEmpty()) hasNorm = true;

                    if (displayEnchants.get()) {
                        ItemEnchantmentsComponent ench = EnchantmentHelper.getEnchantments(stack);
                        int size = 0;
                        for (RegistryEntry<Enchantment> enchantment : ench.getEnchantments()) {
                            if (enchantment.getKey().isPresent() && !shownEnchantments.get().contains(enchantment.getKey().get())) continue;
                            String eName = Utils.getEnchantSimpleName(enchantment, enchantLength.get()) + " " + ench.getLevel(enchantment);
                            widthsNorm[j] = Math.max(widthsNorm[j], text.getWidth(eName, shadow) / 2.0);
                            size++;
                        }
                        maxEnchantCountNorm = Math.max(maxEnchantCountNorm, size);
                    }
                }

                for (int j = 0; j < hotbarRow.size(); j++) {
                    ItemStack stack = hotbarRow.get(j);
                    if (!ignoreEmpty.get() || !stack.isEmpty()) {
                        double extra = j == 0 ? hotbarOffhandMargin.get() : 0;
                        widthsHB[j] = 32 + extra;
                    }
                    if (!stack.isEmpty()) hasHB = true;

                    if (displayEnchants.get()) {
                        ItemEnchantmentsComponent ench = EnchantmentHelper.getEnchantments(stack);
                        int size = 0;
                        for (RegistryEntry<Enchantment> enchantment : ench.getEnchantments()) {
                            if (enchantment.getKey().isPresent() && !shownEnchantments.get().contains(enchantment.getKey().get())) continue;
                            String eName = Utils.getEnchantSimpleName(enchantment, enchantLength.get()) + " " + ench.getLevel(enchantment);
                            widthsHB[j] = Math.max(widthsHB[j], text.getWidth(eName, shadow) / 2.0);
                            size++;
                        }
                        maxEnchantCountHB = Math.max(maxEnchantCountHB, size);
                    }
                }

                double totalWidthNorm = 0;
                for (double w : widthsNorm) totalWidthNorm += w;
                double totalWidthHB = 0;
                for (double w : widthsHB) totalWidthHB += w;

                double totalHeightItems = 0;
                if (hasNorm) totalHeightItems += 32;
                if (hasHB) totalHeightItems += (totalHeightItems > 0 ? 2 : 0) + 32;

                double baseY = -heightDown - 7 - totalHeightItems;

                // Render normal row
                if (hasNorm) {
                    double normX = -totalWidthNorm / 2;
                    double normY = baseY;

                    for (int j = 0; j < normalRow.size(); j++) {
                        ItemStack stack = normalRow.get(j);
                        if (!ignoreEmpty.get() || !stack.isEmpty()) {
                            RenderUtils.drawItem(event.drawContext, stack, (int) normX, (int) normY, 2.0F, true);
                        }

                        if (stack.isDamageable() && itemDurability.get() != Durability.None) {
                            text.begin(0.75, false, true);
                            String damageText = switch (itemDurability.get()) {
                                case Total -> Integer.toString(stack.getMaxDamage() - stack.getDamage());
                                case Percentage -> String.format("%.0f%%", ((stack.getMaxDamage() - stack.getDamage()) * 100f) / (float) stack.getMaxDamage());
                                default -> "err";
                            };
                            Color damageColor = new Color(stack.getItemBarColor());
                            text.render(damageText, (double) ((int) normX), (double) ((int) normY), damageColor.a(255), true);
                            text.end();
                        }

                        if (maxEnchantCountNorm > 0 && displayEnchants.get()) {
                            text.begin(0.5 * enchantTextScale.get(), false, true);
                            ItemEnchantmentsComponent enchantments = EnchantmentHelper.getEnchantments(stack);
                            Object2IntMap<RegistryEntry<Enchantment>> enchantmentsToShow = new Object2IntOpenHashMap<>();
                            for (RegistryEntry<Enchantment> enchantment : enchantments.getEnchantments()) {
                                if (enchantment.matches(shownEnchantments.get()::contains)) {
                                    enchantmentsToShow.put(enchantment, enchantments.getLevel(enchantment));
                                }
                            }
                            double aW = widthsNorm[j];
                            double enchantY = 0;
                            double addY = switch (enchantPos.get()) {
                                case Above -> -((enchantmentsToShow.size() + 1) * text.getHeight(shadow));
                                case OnTop -> (32.0 - enchantmentsToShow.size() * text.getHeight(shadow)) / 2;
                            };
                            for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : Object2IntMaps.fastIterable(enchantmentsToShow)) {
                                String enchantName = Utils.getEnchantSimpleName(entry.getKey(), enchantLength.get()) + " " + entry.getIntValue();
                                Color enchantColor = WHITE;
                                if (entry.getKey().isIn(EnchantmentTags.CURSE)) enchantColor = RED;
                                double enchantX = switch (enchantPos.get()) {
                                    case Above -> normX + aW / 2 - text.getWidth(enchantName, shadow) / 2;
                                    case OnTop -> normX + (aW - text.getWidth(enchantName, shadow)) / 2;
                                };
                                text.render(enchantName, enchantX, normY + addY + enchantY, enchantColor, shadow);
                                enchantY += text.getHeight(shadow);
                            }
                            text.end();
                        }

                        normX += widthsNorm[j];
                    }
                }

                // Render hotbar row
                if (hasHB) {
                    double hbY = baseY + (hasNorm ? 34 : 0);
                    double hbX = -totalWidthHB / 2;

                    for (int j = 0; j < hotbarRow.size(); j++) {
                        ItemStack stack = hotbarRow.get(j);
                        if (!ignoreEmpty.get() || !stack.isEmpty()) {
                            RenderUtils.drawItem(event.drawContext, stack, (int) hbX, (int) hbY, 2.0F, true);
                        }

                        if (stack.isDamageable() && itemDurability.get() != Durability.None) {
                            text.begin(0.75, false, true);
                            String damageText = switch (itemDurability.get()) {
                                case Total -> Integer.toString(stack.getMaxDamage() - stack.getDamage());
                                case Percentage -> String.format("%.0f%%", ((stack.getMaxDamage() - stack.getDamage()) * 100f) / (float) stack.getMaxDamage());
                                default -> "err";
                            };
                            Color damageColor = new Color(stack.getItemBarColor());
                            text.render(damageText, (double) ((int) hbX), (double) ((int) hbY), damageColor.a(255), true);
                            text.end();
                        }

                        if (maxEnchantCountHB > 0 && displayEnchants.get()) {
                            text.begin(0.5 * enchantTextScale.get(), false, true);
                            ItemEnchantmentsComponent enchantments = EnchantmentHelper.getEnchantments(stack);
                            Object2IntMap<RegistryEntry<Enchantment>> enchantmentsToShow = new Object2IntOpenHashMap<>();
                            for (RegistryEntry<Enchantment> enchantment : enchantments.getEnchantments()) {
                                if (enchantment.matches(shownEnchantments.get()::contains)) {
                                    enchantmentsToShow.put(enchantment, enchantments.getLevel(enchantment));
                                }
                            }
                            double aW = widthsHB[j];
                            double enchantY = 0;
                            double addY = switch (enchantPos.get()) {
                                case Above -> -((enchantmentsToShow.size() + 1) * text.getHeight(shadow));
                                case OnTop -> (32.0 - enchantmentsToShow.size() * text.getHeight(shadow)) / 2;
                            };
                            for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : Object2IntMaps.fastIterable(enchantmentsToShow)) {
                                String enchantName = Utils.getEnchantSimpleName(entry.getKey(), enchantLength.get()) + " " + entry.getIntValue();
                                Color enchantColor = WHITE;
                                if (entry.getKey().isIn(EnchantmentTags.CURSE)) enchantColor = RED;
                                double enchantX = switch (enchantPos.get()) {
                                    case Above -> hbX + aW / 2 - text.getWidth(enchantName, shadow) / 2;
                                    case OnTop -> hbX + (aW - text.getWidth(enchantName, shadow)) / 2;
                                };
                                text.render(enchantName, enchantX, hbY + addY + enchantY, enchantColor, shadow);
                                enchantY += text.getHeight(shadow);
                            }
                            text.end();
                        }

                        hbX += widthsHB[j];
                    }

                    // Selected slot highlight on hotbar row
                    int selectedSlot = -1;
                    ItemStack currentHand = player.getMainHandStack();
                    if (!currentHand.isEmpty()) {
                        for (int j = 1; j <= 9; j++) {
                            if (!hotbarRow.get(j).isEmpty() && hotbarRow.get(j).getItem() == currentHand.getItem()) {
                                selectedSlot = j;
                                break;
                            }
                        }
                    }
                    if (selectedSlot == -1) {
                        selectedSlot = MathHelper.clamp(player.getInventory().selectedSlot + 1, 1, 9);
                    }
                    double selX = -totalWidthHB / 2;
                    for (int j = 0; j < selectedSlot; j++) selX += widthsHB[j];

                    if (!ignoreEmpty.get() || (selectedSlot >= 1 && selectedSlot <= 9 && !hotbarRow.get(selectedSlot).isEmpty())) {
                        Renderer2D.COLOR.begin();
                        float qx = (float) (selX - 1), qy = (float) (hbY - 1), qw = 34, qh = 34;
                        Renderer2D.COLOR.quad(qx, qy, qw, 1, Color.WHITE);
                        Renderer2D.COLOR.quad(qx, qy + qh - 1, qw, 1, Color.WHITE);
                        Renderer2D.COLOR.quad(qx, qy + 1, 1, qh - 2, Color.WHITE);
                        Renderer2D.COLOR.quad(qx + qw - 1, qy + 1, 1, qh - 2, Color.WHITE);
                        Renderer2D.COLOR.render();
                    }
                }
            } else {
                // Single row mode
                items.clear();

                if (hotbarEnabled && showHotbar) {
                    ItemStack[] trackedHotbar = trackedHotbars.get(player.getUuid());
                    if (trackedHotbar != null) {
                        Collections.addAll(items, trackedHotbar);
                    } else {
                        for (int j = 0; j < 10; j++) items.add(ItemStack.EMPTY);
                    }
                } else {
                    items.add(player.getMainHandStack());
                    items.add(((IPlayerInventory) player.getInventory()).meteor$getArmor().get(3));
                    items.add(((IPlayerInventory) player.getInventory()).meteor$getArmor().get(2));
                    items.add(((IPlayerInventory) player.getInventory()).meteor$getArmor().get(1));
                    items.add(((IPlayerInventory) player.getInventory()).meteor$getArmor().get(0));
                    items.add(player.getOffHandStack());
                }

                int itemCount = items.size();
                Arrays.fill(itemWidths, 0);
                boolean hasItems = false;
                int maxEnchantCount = 0;

                for (int j = 0; j < itemCount; j++) {
                    ItemStack itemStack = items.get(j);

                    if (hotbarEnabled && showHotbar) {
                        if (!ignoreEmpty.get() || !itemStack.isEmpty()) {
                            double extraSpacing = j == 0 ? hotbarOffhandMargin.get() : 0;
                            itemWidths[j] = 32 + extraSpacing;
                        }
                    } else if (itemWidths[j] == 0 && (!ignoreEmpty.get() || !itemStack.isEmpty())) {
                        itemWidths[j] = 32;
                    }

                    if (!itemStack.isEmpty()) hasItems = true;

                    if (displayEnchants.get()) {
                        ItemEnchantmentsComponent enchantments = EnchantmentHelper.getEnchantments(itemStack);
                        int size = 0;
                        for (RegistryEntry<Enchantment> enchantment : enchantments.getEnchantments()) {
                            if (enchantment.getKey().isPresent() && !shownEnchantments.get().contains(enchantment.getKey().get())) continue;
                            String enchantName = Utils.getEnchantSimpleName(enchantment, enchantLength.get()) + " " + enchantments.getLevel(enchantment);
                            itemWidths[j] = Math.max(itemWidths[j], text.getWidth(enchantName, shadow) / 2);
                            size++;
                        }
                        maxEnchantCount = Math.max(maxEnchantCount, size);
                    }
                }

                double itemsHeight = hasItems ? 32 : 0;
                double itemWidthTotal = 0;
                for (int j = 0; j < itemCount; j++) itemWidthTotal += itemWidths[j];
                double itemWidthHalf = itemWidthTotal / 2;

                double y = -heightDown - 7 - itemsHeight;
                double x = -itemWidthHalf;

                for (int j = 0; j < itemCount; j++) {
                    ItemStack stack = items.get(j);

                    if (!hotbarEnabled || !showHotbar || !ignoreEmpty.get() || !stack.isEmpty()) {
                        RenderUtils.drawItem(event.drawContext, stack, (int) x, (int) y, 2.0F, true);
                    }

                    if (stack.isDamageable() && itemDurability.get() != Durability.None) {
                        text.begin(0.75, false, true);
                        String damageText = switch (itemDurability.get()) {
                            case Total -> Integer.toString(stack.getMaxDamage() - stack.getDamage());
                            case Percentage -> String.format("%.0f%%", ((stack.getMaxDamage() - stack.getDamage()) * 100f) / (float) stack.getMaxDamage());
                            default -> "err";
                        };
                        Color damageColor = new Color(stack.getItemBarColor());
                        text.render(damageText, (double) ((int) x), (double) ((int) y), damageColor.a(255), true);
                        text.end();
                    }

                    if (maxEnchantCount > 0 && displayEnchants.get()) {
                        text.begin(0.5 * enchantTextScale.get(), false, true);
                        ItemEnchantmentsComponent enchantments = EnchantmentHelper.getEnchantments(stack);
                        Object2IntMap<RegistryEntry<Enchantment>> enchantmentsToShow = new Object2IntOpenHashMap<>();
                        for (RegistryEntry<Enchantment> enchantment : enchantments.getEnchantments()) {
                            if (enchantment.matches(shownEnchantments.get()::contains)) {
                                enchantmentsToShow.put(enchantment, enchantments.getLevel(enchantment));
                            }
                        }
                        double aW = itemWidths[j];
                        double enchantY = 0;
                        double addY = switch (enchantPos.get()) {
                            case Above -> -((enchantmentsToShow.size() + 1) * text.getHeight(shadow));
                            case OnTop -> (itemsHeight - enchantmentsToShow.size() * text.getHeight(shadow)) / 2;
                        };
                        for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : Object2IntMaps.fastIterable(enchantmentsToShow)) {
                            String enchantName = Utils.getEnchantSimpleName(entry.getKey(), enchantLength.get()) + " " + entry.getIntValue();
                            Color enchantColor = WHITE;
                            if (entry.getKey().isIn(EnchantmentTags.CURSE)) enchantColor = RED;
                            double enchantX = switch (enchantPos.get()) {
                                case Above -> x + aW / 2 - text.getWidth(enchantName, shadow) / 2;
                                case OnTop -> x + (aW - text.getWidth(enchantName, shadow)) / 2;
                            };
                            text.render(enchantName, enchantX, y + addY + enchantY, enchantColor, shadow);
                            enchantY += text.getHeight(shadow);
                        }
                        text.end();
                    }

                    x += itemWidths[j];
                }

                // Selected slot highlight for hotbar mode
                if (hotbarEnabled && showHotbar) {
                    int selectedSlot = -1;
                    ItemStack currentHand = player.getMainHandStack();
                    if (!items.isEmpty() && !currentHand.isEmpty()) {
                        for (int j = 1; j <= 9; j++) {
                            if (j < items.size() && !items.get(j).isEmpty() && items.get(j).getItem() == currentHand.getItem()) {
                                selectedSlot = j;
                                break;
                            }
                        }
                    }
                    if (selectedSlot == -1) {
                        selectedSlot = MathHelper.clamp(player.getInventory().selectedSlot + 1, 1, 9);
                    }
                    double selX = -itemWidthHalf;
                    for (int j = 0; j < selectedSlot; j++) selX += itemWidths[j];

                    if (!ignoreEmpty.get() || (selectedSlot >= 1 && selectedSlot <= 9 && selectedSlot < items.size() && !items.get(selectedSlot).isEmpty())) {
                        Renderer2D.COLOR.begin();
                        float qx = (float) (selX - 1), qy = (float) (y - 1), qw = 34, qh = 34;
                        Renderer2D.COLOR.quad(qx, qy, qw, 1, Color.WHITE);
                        Renderer2D.COLOR.quad(qx, qy + qh - 1, qw, 1, Color.WHITE);
                        Renderer2D.COLOR.quad(qx, qy + 1, 1, qh - 2, Color.WHITE);
                        Renderer2D.COLOR.quad(qx + qw - 1, qy + 1, 1, qh - 2, Color.WHITE);
                        Renderer2D.COLOR.render();
                    }
                }
            }
        } else if (displayEnchants.get()) displayEnchants.set(false);

        NametagUtils.end(event.drawContext);
    }

    private void renderNametagItem(ItemStack stack, boolean shadow) {
        if (stack.isEmpty()) return;

        TextRenderer text = TextRenderer.get();
        NametagUtils.begin(pos);

        String name = Names.get(stack);
        String count = " x" + stack.getCount();

        double nameWidth = text.getWidth(name, shadow);
        double countWidth = text.getWidth(count, shadow);
        double heightDown = text.getHeight(shadow);

        double width = nameWidth;
        if (itemCount.get()) width += countWidth;
        double widthHalf = width / 2;

        drawBg(-widthHalf, -heightDown, width, heightDown);

        text.beginBig();
        double hX = -widthHalf;
        double hY = -heightDown;

        hX = text.render(name, hX, hY, nameColor.get(), shadow);
        if (itemCount.get()) text.render(count, hX, hY, GOLD, shadow);
        text.end();

        NametagUtils.end();
    }

    private void renderGenericLivingNametag(LivingEntity entity, boolean shadow) {
        TextRenderer text = TextRenderer.get();
        NametagUtils.begin(pos);

        //Name
        String nameText = entity.getType().getName().getString();
        nameText += " ";

        //Health
        float absorption = entity.getAbsorptionAmount();
        int health = Math.round(entity.getHealth() + absorption);
        double healthPercentage = health / (entity.getMaxHealth() + absorption);

        String healthText = String.valueOf(health);
        Color healthColor;

        if (healthPercentage <= 0.333) healthColor = RED;
        else if (healthPercentage <= 0.666) healthColor = AMBER;
        else healthColor = GREEN;

        double nameWidth = text.getWidth(nameText, shadow);
        double healthWidth = text.getWidth(healthText, shadow);
        double heightDown = text.getHeight(shadow);

        double width = nameWidth + healthWidth;
        double widthHalf = width / 2;

        drawBg(-widthHalf, -heightDown, width, heightDown);

        text.beginBig();
        double hX = -widthHalf;
        double hY = -heightDown;

        hX = text.render(nameText, hX, hY, nameColor.get(), shadow);
        text.render(healthText, hX, hY, healthColor, shadow);
        text.end();

        NametagUtils.end();
    }

    private void renderGenericNametag(Entity entity, boolean shadow) {
        TextRenderer text = TextRenderer.get();
        NametagUtils.begin(pos);

        //Name
        String nameText = entity.getType().getName().getString();

        double nameWidth = text.getWidth(nameText, shadow);
        double heightDown = text.getHeight(shadow);
        double widthHalf = nameWidth / 2;

        drawBg(-widthHalf, -heightDown, nameWidth, heightDown);

        text.beginBig();
        double hX = -widthHalf;
        double hY = -heightDown;

        text.render(nameText, hX, hY, nameColor.get(), shadow);
        text.end();

        NametagUtils.end();
    }

    private void renderTntNametag(String fuseText, boolean shadow) {
        TextRenderer text = TextRenderer.get();
        NametagUtils.begin(pos);

        double width = text.getWidth(fuseText, shadow);
        double heightDown = text.getHeight(shadow);

        double widthHalf = width / 2;

        drawBg(-widthHalf, -heightDown, width, heightDown);

        text.beginBig();
        double hX = -widthHalf;
        double hY = -heightDown;

        text.render(fuseText, hX, hY, nameColor.get(), shadow);
        text.end();

        NametagUtils.end();
    }

    private void drawBg(double x, double y, double width, double height) {
        Renderer2D.COLOR.begin();
        Renderer2D.COLOR.quad(x - 1, y - 1, width + 2, height + 2, background.get());
        Renderer2D.COLOR.render();
    }

    public enum Position {
        Above,
        OnTop
    }

    public enum Durability {
        None,
        Total,
        Percentage
    }

    public enum DistanceColorMode {
        Gradient,
        Flat
    }

    public boolean excludeBots() {
        return ignoreBots.get();
    }

    public boolean playerNametags() {
        return isActive() && entities.get().contains(EntityType.PLAYER);
    }
}
