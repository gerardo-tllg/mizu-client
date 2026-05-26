package meteordevelopment.meteorclient.systems.modules.render;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import javassist.compiler.TokenId;
import meteordevelopment.meteorclient.events.game.ItemStackTooltipEvent;
import meteordevelopment.meteorclient.events.render.TooltipDataEvent;
import meteordevelopment.meteorclient.mixin.EntityBucketItemAccessor;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.player.EChestMemory;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.tooltip.BannerTooltipComponent;
import meteordevelopment.meteorclient.utils.tooltip.BookTooltipComponent;
import meteordevelopment.meteorclient.utils.tooltip.ContainerTooltipComponent;
import meteordevelopment.meteorclient.utils.tooltip.MapTooltipComponent;
import meteordevelopment.meteorclient.utils.tooltip.TextTooltipComponent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_10706;
import net.minecraft.class_124;
import net.minecraft.class_1291;
import net.minecraft.class_1292;
import net.minecraft.class_1293;
import net.minecraft.class_1746;
import net.minecraft.class_1767;
import net.minecraft.class_1785;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2561;
import net.minecraft.class_5250;
import net.minecraft.class_5632;
import net.minecraft.class_9209;
import net.minecraft.class_9262;
import net.minecraft.class_9275;
import net.minecraft.class_9298;
import net.minecraft.class_9301;
import net.minecraft.class_9302;
import net.minecraft.class_9307;
import net.minecraft.class_9334;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/BetterTooltips.class */
public class BetterTooltips extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgPreviews;
    private final SettingGroup sgOther;
    private final SettingGroup sgHideFlags;
    private final Setting<DisplayWhen> displayWhen;
    private final Setting<Keybind> keybind;
    private final Setting<Boolean> middleClickOpen;
    private final Setting<Boolean> pauseInCreative;
    private final Setting<Boolean> shulkers;
    private final Setting<Boolean> shulkerCompactTooltip;
    public final Setting<Boolean> echest;
    private final Setting<Boolean> maps;
    public final Setting<Double> mapsScale;
    private final Setting<Boolean> books;
    private final Setting<Boolean> banners;
    private final Setting<Boolean> entitiesInBuckets;
    public final Setting<Boolean> byteSize;
    private final Setting<Boolean> statusEffects;
    private final Setting<Boolean> beehive;
    public final Setting<Boolean> tooltip;
    public final Setting<Boolean> enchantments;
    public final Setting<Boolean> modifiers;
    public final Setting<Boolean> unbreakable;
    public final Setting<Boolean> canDestroy;
    public final Setting<Boolean> canPlaceOn;
    public final Setting<Boolean> additional;
    public final Setting<Boolean> dye;
    public final Setting<Boolean> upgrades;
    private boolean updateTooltips;
    public static final Color ECHEST_COLOR = new Color(0, 50, 50);
    private static final class_1799[] ITEMS = new class_1799[27];

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/BetterTooltips$DisplayWhen.class */
    public enum DisplayWhen {
        Keybind,
        Always
    }

    public BetterTooltips() {
        super(Categories.Render, "better-tooltips", "Displays more useful tooltips for certain items.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgPreviews = this.settings.createGroup("Previews");
        this.sgOther = this.settings.createGroup("Other");
        this.sgHideFlags = this.settings.createGroup("Hide Flags");
        this.displayWhen = this.sgGeneral.add(new EnumSetting.Builder().name("display-when").description("When to display previews.").defaultValue(DisplayWhen.Keybind).onChanged(value -> {
            this.updateTooltips = true;
        }).build());
        this.keybind = this.sgGeneral.add(new KeybindSetting.Builder().name("keybind").description("The bind for keybind mode.").defaultValue(Keybind.fromKey(TokenId.TRANSIENT)).visible(() -> {
            return this.displayWhen.get() == DisplayWhen.Keybind;
        }).onChanged(value2 -> {
            this.updateTooltips = true;
        }).build());
        this.middleClickOpen = this.sgGeneral.add(new BoolSetting.Builder().name("middle-click-open").description("Opens a GUI window with the inventory of the storage block or book when you middle click the item.").defaultValue(true).build());
        SettingGroup settingGroup = this.sgGeneral;
        BoolSetting.Builder builderDefaultValue = new BoolSetting.Builder().name("pause-in-creative").description("Pauses middle click open while the player is in creative mode.").defaultValue(true);
        Setting<Boolean> setting = this.middleClickOpen;
        Objects.requireNonNull(setting);
        this.pauseInCreative = settingGroup.add(builderDefaultValue.visible(setting::get).build());
        this.shulkers = this.sgPreviews.add(new BoolSetting.Builder().name("containers").description("Shows a preview of a containers when hovering over it in an inventory.").defaultValue(true).onChanged(value3 -> {
            this.updateTooltips = true;
        }).build());
        this.shulkerCompactTooltip = this.sgPreviews.add(new BoolSetting.Builder().name("compact-shulker-tooltip").description("Compacts the lines of the shulker tooltip.").defaultValue(true).build());
        this.echest = this.sgPreviews.add(new BoolSetting.Builder().name("echests").description("Shows a preview of your echest when hovering over it in an inventory.").defaultValue(true).onChanged(value4 -> {
            this.updateTooltips = true;
        }).build());
        this.maps = this.sgPreviews.add(new BoolSetting.Builder().name("maps").description("Shows a preview of a map when hovering over it in an inventory.").defaultValue(true).onChanged(value5 -> {
            this.updateTooltips = true;
        }).build());
        SettingGroup settingGroup2 = this.sgPreviews;
        DoubleSetting.Builder builderSliderMax = new DoubleSetting.Builder().name("map-scale").description("The scale of the map preview.").defaultValue(1.0d).min(0.001d).sliderMax(1.0d);
        Setting<Boolean> setting2 = this.maps;
        Objects.requireNonNull(setting2);
        this.mapsScale = settingGroup2.add(builderSliderMax.visible(setting2::get).build());
        this.books = this.sgPreviews.add(new BoolSetting.Builder().name("books").description("Shows contents of a book when hovering over it in an inventory.").defaultValue(true).onChanged(value6 -> {
            this.updateTooltips = true;
        }).build());
        this.banners = this.sgPreviews.add(new BoolSetting.Builder().name("banners").description("Shows banners' patterns when hovering over it in an inventory. Also works with shields.").defaultValue(true).onChanged(value7 -> {
            this.updateTooltips = true;
        }).build());
        this.entitiesInBuckets = this.sgPreviews.add(new BoolSetting.Builder().name("entities-in-buckets").description("Shows entities in buckets when hovering over it in an inventory.").defaultValue(true).onChanged(value8 -> {
            this.updateTooltips = true;
        }).build());
        this.byteSize = this.sgOther.add(new BoolSetting.Builder().name("byte-size").description("Displays an item's size in bytes in the tooltip.").defaultValue(true).onChanged(value9 -> {
            this.updateTooltips = true;
        }).build());
        this.statusEffects = this.sgOther.add(new BoolSetting.Builder().name("status-effects").description("Adds list of status effects to tooltips of food items.").defaultValue(true).onChanged(value10 -> {
            this.updateTooltips = true;
        }).build());
        this.beehive = this.sgOther.add(new BoolSetting.Builder().name("beehive").description("Displays information about a beehive or bee nest.").defaultValue(true).onChanged(value11 -> {
            this.updateTooltips = true;
        }).build());
        this.tooltip = this.sgHideFlags.add(new BoolSetting.Builder().name("tooltip").description("Show the tooltip when it's hidden.").defaultValue(false).build());
        this.enchantments = this.sgHideFlags.add(new BoolSetting.Builder().name("enchantments").description("Show enchantments when it's hidden.").defaultValue(false).build());
        this.modifiers = this.sgHideFlags.add(new BoolSetting.Builder().name("modifiers").description("Show item modifiers when it's hidden.").defaultValue(false).build());
        this.unbreakable = this.sgHideFlags.add(new BoolSetting.Builder().name("unbreakable").description("Show \"Unbreakable\" tag when it's hidden.").defaultValue(false).build());
        this.canDestroy = this.sgHideFlags.add(new BoolSetting.Builder().name("can-destroy").description("Show \"CanDestroy\" tag when it's hidden.").defaultValue(false).build());
        this.canPlaceOn = this.sgHideFlags.add(new BoolSetting.Builder().name("can-place-on").description("Show \"CanPlaceOn\" tag when it's hidden.").defaultValue(false).build());
        this.additional = this.sgHideFlags.add(new BoolSetting.Builder().name("additional").description("Show potion effects, firework status, book author, etc when it's hidden.").defaultValue(false).build());
        this.dye = this.sgHideFlags.add(new BoolSetting.Builder().name("dye").description("Show dyed item tags when it's hidden.").defaultValue(false).build());
        this.upgrades = this.sgHideFlags.add(new BoolSetting.Builder().name("armor-trim").description("Show armor trims when it's hidden.").defaultValue(false).build());
        this.updateTooltips = false;
    }

    @EventHandler
    private void appendTooltip(ItemStackTooltipEvent event) {
        if (!this.tooltip.get().booleanValue() && event.list().isEmpty()) {
            appendPreviewTooltipText(event, false);
            return;
        }
        if (this.statusEffects.get().booleanValue()) {
            if (event.itemStack().method_7909() == class_1802.field_8766) {
                class_9298 stewEffectsComponent = (class_9298) event.itemStack().method_58694(class_9334.field_49652);
                if (stewEffectsComponent != null) {
                    for (class_9298.class_8751 effectTag : stewEffectsComponent.comp_2416()) {
                        class_1293 effect = new class_1293(effectTag.comp_1838(), effectTag.comp_1839(), 0);
                        event.appendStart(getStatusText(effect));
                    }
                }
            }
        }
        if (this.beehive.get().booleanValue() && (event.itemStack().method_7909() == class_1802.field_20416 || event.itemStack().method_7909() == class_1802.field_20415)) {
            class_9275 blockStateComponent = (class_9275) event.itemStack().method_58694(class_9334.field_49623);
            if (blockStateComponent != null) {
                String level = (String) blockStateComponent.comp_2381().get("honey_level");
                event.appendStart(class_2561.method_43470(String.format("%sHoney level: %s%s%s.", class_124.field_1080, class_124.field_1054, level, class_124.field_1080)));
            }
            class_10706 bees = (class_10706) event.itemStack().method_58694(class_9334.field_49624);
            if (bees != null) {
                event.appendStart(class_2561.method_43470(String.format("%sBees: %s%d%s.", class_124.field_1080, class_124.field_1054, Integer.valueOf(bees.comp_3585().size()), class_124.field_1080)));
            }
        }
        if (this.byteSize.get().booleanValue()) {
        }
        appendPreviewTooltipText(event, true);
    }

    @EventHandler
    private void getTooltipData(TooltipDataEvent event) {
        class_5632 textTooltipComponent;
        if (previewShulkers() && Utils.hasItems(event.itemStack)) {
            Utils.getItemsInContainerItem(event.itemStack, ITEMS);
            event.tooltipData = new ContainerTooltipComponent(ITEMS, Utils.getShulkerColor(event.itemStack));
            return;
        }
        if (event.itemStack.method_7909() == class_1802.field_8466 && previewEChest()) {
            if (EChestMemory.isKnown()) {
                textTooltipComponent = new ContainerTooltipComponent((class_1799[]) EChestMemory.ITEMS.toArray(new class_1799[27]), ECHEST_COLOR);
            } else {
                textTooltipComponent = new TextTooltipComponent((class_2561) class_2561.method_43470("Unknown ender chest inventory.").method_27692(class_124.field_1079));
            }
            event.tooltipData = textTooltipComponent;
            return;
        }
        if (event.itemStack.method_7909() == class_1802.field_8204 && previewMaps()) {
            class_9209 mapIdComponent = (class_9209) event.itemStack.method_58694(class_9334.field_49646);
            if (mapIdComponent != null) {
                event.tooltipData = new MapTooltipComponent(mapIdComponent.comp_2315());
                return;
            }
            return;
        }
        if ((event.itemStack.method_7909() == class_1802.field_8674 || event.itemStack.method_7909() == class_1802.field_8360) && previewBooks()) {
            class_2561 page = getFirstPage(event.itemStack);
            if (page != null) {
                event.tooltipData = new BookTooltipComponent(page);
                return;
            }
            return;
        }
        if ((event.itemStack.method_7909() instanceof class_1746) && previewBanners()) {
            event.tooltipData = new BannerTooltipComponent(event.itemStack);
            return;
        }
        if (event.itemStack.method_7909() == class_1802.field_8255 && previewBanners()) {
            if (event.itemStack.method_58694(class_9334.field_49620) != null || !((class_9307) event.itemStack.method_58695(class_9334.field_49619, class_9307.field_49404)).comp_2428().isEmpty()) {
                event.tooltipData = createBannerFromShield(event.itemStack);
                return;
            }
            return;
        }
        EntityBucketItemAccessor entityBucketItemAccessorMethod_7909 = event.itemStack.method_7909();
        if (entityBucketItemAccessorMethod_7909 instanceof class_1785) {
            EntityBucketItemAccessor entityBucketItemAccessor = (class_1785) entityBucketItemAccessorMethod_7909;
            if (previewEntities()) {
                entityBucketItemAccessor.getEntityType();
            }
        }
    }

    public void applyCompactShulkerTooltip(class_1799 shulkerItem, List<class_2561> tooltip) {
        if (shulkerItem.method_57826(class_9334.field_49626)) {
            tooltip.add(class_2561.method_43470("???????"));
        }
        if (Utils.hasItems(shulkerItem)) {
            Utils.getItemsInContainerItem(shulkerItem, ITEMS);
            Object2IntOpenHashMap object2IntOpenHashMap = new Object2IntOpenHashMap();
            for (class_1799 item : ITEMS) {
                if (!item.method_7960()) {
                    int count = object2IntOpenHashMap.getInt(item.method_7909());
                    object2IntOpenHashMap.put(item.method_7909(), count + item.method_7947());
                }
            }
            object2IntOpenHashMap.keySet().stream().sorted(Comparator.comparingInt(value -> {
                return -object2IntOpenHashMap.getInt(value);
            })).limit(5L).forEach(item2 -> {
                class_5250 mutableText = item2.method_63680().method_27662();
                mutableText.method_10852(class_2561.method_43470(" x").method_27693(String.valueOf(object2IntOpenHashMap.getInt(item2))).method_27692(class_124.field_1080));
                tooltip.add(mutableText);
            });
            if (object2IntOpenHashMap.size() > 5) {
                tooltip.add(class_2561.method_43469("container.shulkerBox.more", new Object[]{Integer.valueOf(object2IntOpenHashMap.size() - 5)}).method_27692(class_124.field_1056));
            }
        }
    }

    private void appendPreviewTooltipText(ItemStackTooltipEvent event, boolean spacer) {
        if (!isPressed()) {
            if ((this.shulkers.get().booleanValue() && Utils.hasItems(event.itemStack())) || ((event.itemStack().method_7909() == class_1802.field_8466 && this.echest.get().booleanValue()) || ((event.itemStack().method_7909() == class_1802.field_8204 && this.maps.get().booleanValue()) || ((event.itemStack().method_7909() == class_1802.field_8674 && this.books.get().booleanValue()) || ((event.itemStack().method_7909() == class_1802.field_8360 && this.books.get().booleanValue()) || (((event.itemStack().method_7909() instanceof class_1785) && this.entitiesInBuckets.get().booleanValue()) || (((event.itemStack().method_7909() instanceof class_1746) && this.banners.get().booleanValue()) || (event.itemStack().method_7909() == class_1802.field_8255 && this.banners.get().booleanValue())))))))) {
                if (spacer) {
                    event.appendEnd(class_2561.method_43470(""));
                }
                event.appendEnd(class_2561.method_43470("Hold " + String.valueOf(class_124.field_1054) + String.valueOf(this.keybind) + String.valueOf(class_124.field_1070) + " to preview"));
            }
        }
    }

    private class_5250 getStatusText(class_1293 effect) {
        class_5250 text = class_2561.method_43471(effect.method_5586());
        if (effect.method_5578() != 0) {
            text.method_27693(String.format(" %d (%s)", Integer.valueOf(effect.method_5578() + 1), class_1292.method_5577(effect, 1.0f, this.mc.field_1687.method_54719().method_54748()).getString()));
        } else {
            text.method_27693(String.format(" (%s)", class_1292.method_5577(effect, 1.0f, this.mc.field_1687.method_54719().method_54748()).getString()));
        }
        return ((class_1291) effect.method_5579().comp_349()).method_5573() ? text.method_27692(class_124.field_1078) : text.method_27692(class_124.field_1061);
    }

    private class_2561 getFirstPage(class_1799 bookItem) {
        if (bookItem.method_58694(class_9334.field_49653) != null) {
            List<class_9262<String>> pages = ((class_9301) bookItem.method_58694(class_9334.field_49653)).comp_2422();
            if (pages.isEmpty()) {
                return null;
            }
            return class_2561.method_43470((String) ((class_9262) pages.getFirst()).method_57140(false));
        }
        if (bookItem.method_58694(class_9334.field_49606) != null) {
            List<class_9262<class_2561>> pages2 = ((class_9302) bookItem.method_58694(class_9334.field_49606)).comp_2422();
            if (pages2.isEmpty()) {
                return null;
            }
            return (class_2561) ((class_9262) pages2.getFirst()).method_57140(false);
        }
        return null;
    }

    private BannerTooltipComponent createBannerFromShield(class_1799 shieldItem) {
        class_1767 dyeColor2 = (class_1767) shieldItem.method_58695(class_9334.field_49620, class_1767.field_7952);
        class_9307 bannerPatternsComponent = (class_9307) shieldItem.method_58695(class_9334.field_49619, class_9307.field_49404);
        return new BannerTooltipComponent(dyeColor2, bannerPatternsComponent);
    }

    public boolean middleClickOpen() {
        return isActive() && this.middleClickOpen.get().booleanValue() && !(this.pauseInCreative.get().booleanValue() && this.mc.field_1724.method_56992());
    }

    public boolean previewShulkers() {
        return isActive() && isPressed() && this.shulkers.get().booleanValue();
    }

    public boolean shulkerCompactTooltip() {
        return isActive() && this.shulkerCompactTooltip.get().booleanValue();
    }

    private boolean previewEChest() {
        return isPressed() && this.echest.get().booleanValue();
    }

    private boolean previewMaps() {
        return isPressed() && this.maps.get().booleanValue();
    }

    private boolean previewBooks() {
        return isPressed() && this.books.get().booleanValue();
    }

    private boolean previewBanners() {
        return isPressed() && this.banners.get().booleanValue();
    }

    private boolean previewEntities() {
        return isPressed() && this.entitiesInBuckets.get().booleanValue();
    }

    private boolean isPressed() {
        return (this.keybind.get().isPressed() && this.displayWhen.get() == DisplayWhen.Keybind) || this.displayWhen.get() == DisplayWhen.Always;
    }

    public boolean updateTooltips() {
        if (this.updateTooltips && isActive()) {
            this.updateTooltips = false;
            return true;
        }
        return false;
    }
}
