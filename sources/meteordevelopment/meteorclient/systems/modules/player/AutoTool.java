package meteordevelopment.meteorclient.systems.modules.player;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import meteordevelopment.meteorclient.events.entity.player.StartBreakingBlockEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ItemListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.Xray;
import meteordevelopment.meteorclient.systems.modules.world.InfinityMiner;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1820;
import net.minecraft.class_1887;
import net.minecraft.class_1893;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2302;
import net.minecraft.class_2397;
import net.minecraft.class_2560;
import net.minecraft.class_2680;
import net.minecraft.class_3481;
import net.minecraft.class_3489;
import net.minecraft.class_5321;
import net.minecraft.class_9334;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/player/AutoTool.class */
public class AutoTool extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgWhitelist;
    private final Setting<EnchantPreference> prefer;
    private final Setting<Boolean> silkTouchForEnderChest;
    private final Setting<Boolean> fortuneForOresCrops;
    private final Setting<Boolean> antiBreak;
    private final Setting<Integer> breakDurability;
    private final Setting<Boolean> switchBack;
    private final Setting<Integer> switchDelay;
    private final Setting<ListMode> listMode;
    private final Setting<List<class_1792>> whitelist;
    private final Setting<List<class_1792>> blacklist;
    private boolean wasPressed;
    private boolean shouldSwitch;
    private int ticks;
    private int bestSlot;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/player/AutoTool$EnchantPreference.class */
    public enum EnchantPreference {
        None,
        Fortune,
        SilkTouch
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/player/AutoTool$ListMode.class */
    public enum ListMode {
        Whitelist,
        Blacklist
    }

    public AutoTool() {
        super(Categories.Player, "auto-tool", "Automatically switches to the most effective tool when performing an action.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgWhitelist = this.settings.createGroup("Whitelist");
        this.prefer = this.sgGeneral.add(new EnumSetting.Builder().name("prefer").description("Either to prefer Silk Touch, Fortune, or none.").defaultValue(EnchantPreference.Fortune).build());
        this.silkTouchForEnderChest = this.sgGeneral.add(new BoolSetting.Builder().name("silk-touch-for-ender-chest").description("Mines Ender Chests only with the Silk Touch enchantment.").defaultValue(true).build());
        this.fortuneForOresCrops = this.sgGeneral.add(new BoolSetting.Builder().name("fortune-for-ores-and-crops").description("Mines Ores and crops only with the Fortune enchantment.").defaultValue(false).build());
        this.antiBreak = this.sgGeneral.add(new BoolSetting.Builder().name("anti-break").description("Stops you from breaking your tool.").defaultValue(false).build());
        SettingGroup settingGroup = this.sgGeneral;
        IntSetting.Builder builderSliderRange = new IntSetting.Builder().name("anti-break-percentage").description("The durability percentage to stop using a tool.").defaultValue(10).range(1, 100).sliderRange(1, 100);
        Setting<Boolean> setting = this.antiBreak;
        Objects.requireNonNull(setting);
        this.breakDurability = settingGroup.add(builderSliderRange.visible(setting::get).build());
        this.switchBack = this.sgGeneral.add(new BoolSetting.Builder().name("switch-back").description("Switches your hand to whatever was selected when releasing your attack key.").defaultValue(false).build());
        this.switchDelay = this.sgGeneral.add(new IntSetting.Builder().name("switch-delay").description("Delay in ticks before switching tools.").defaultValue(0).build());
        this.listMode = this.sgWhitelist.add(new EnumSetting.Builder().name("list-mode").description("Selection mode.").defaultValue(ListMode.Blacklist).build());
        this.whitelist = this.sgWhitelist.add(new ItemListSetting.Builder().name("whitelist").description("The tools you want to use.").visible(() -> {
            return this.listMode.get() == ListMode.Whitelist;
        }).filter(AutoTool::isTool).build());
        this.blacklist = this.sgWhitelist.add(new ItemListSetting.Builder().name("blacklist").description("The tools you don't want to use.").visible(() -> {
            return this.listMode.get() == ListMode.Blacklist;
        }).filter(AutoTool::isTool).build());
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (Modules.get().isActive(InfinityMiner.class)) {
            return;
        }
        if (this.switchBack.get().booleanValue() && !this.mc.field_1690.field_1886.method_1434() && this.wasPressed && InvUtils.previousSlot != -1) {
            InvUtils.swapBack();
            this.wasPressed = false;
            return;
        }
        if (this.ticks <= 0 && this.shouldSwitch && this.bestSlot != -1) {
            InvUtils.swap(this.bestSlot, this.switchBack.get().booleanValue());
            this.shouldSwitch = false;
        } else {
            this.ticks--;
        }
        this.wasPressed = this.mc.field_1690.field_1886.method_1434();
    }

    @EventHandler(priority = 100)
    private void onStartBreakingBlock(StartBreakingBlockEvent event) {
        if (Modules.get().isActive(InfinityMiner.class)) {
            return;
        }
        class_2680 blockState = this.mc.field_1687.method_8320(event.blockPos);
        if (BlockUtils.canBreak(event.blockPos, blockState)) {
            class_1799 currentStack = this.mc.field_1724.method_6047();
            double bestScore = -1.0d;
            this.bestSlot = -1;
            for (int i = 0; i < 9; i++) {
                class_1799 itemStack = this.mc.field_1724.method_31548().method_5438(i);
                if ((this.listMode.get() != ListMode.Whitelist || this.whitelist.get().contains(itemStack.method_7909())) && (this.listMode.get() != ListMode.Blacklist || !this.blacklist.get().contains(itemStack.method_7909()))) {
                    double score = getScore(itemStack, blockState, this.silkTouchForEnderChest.get().booleanValue(), this.fortuneForOresCrops.get().booleanValue(), this.prefer.get(), itemStack2 -> {
                        return !shouldStopUsing(itemStack2);
                    });
                    if (score >= 0.0d && score > bestScore) {
                        bestScore = score;
                        this.bestSlot = i;
                    }
                }
            }
            if ((this.bestSlot != -1 && bestScore > getScore(currentStack, blockState, this.silkTouchForEnderChest.get().booleanValue(), this.fortuneForOresCrops.get().booleanValue(), this.prefer.get(), itemStack3 -> {
                return !shouldStopUsing(itemStack3);
            })) || shouldStopUsing(currentStack) || !isTool(currentStack)) {
                this.ticks = this.switchDelay.get().intValue();
                if (this.ticks == 0) {
                    InvUtils.swap(this.bestSlot, true);
                } else {
                    this.shouldSwitch = true;
                }
            }
            class_1799 currentStack2 = this.mc.field_1724.method_6047();
            if (shouldStopUsing(currentStack2) && isTool(currentStack2)) {
                this.mc.field_1690.field_1886.method_23481(false);
                event.cancel();
            }
        }
    }

    private boolean shouldStopUsing(class_1799 itemStack) {
        return this.antiBreak.get().booleanValue() && itemStack.method_7936() - itemStack.method_7919() < (itemStack.method_7936() * this.breakDurability.get().intValue()) / 100;
    }

    public static double getScore(class_1799 itemStack, class_2680 state, boolean silkTouchEnderChest, boolean fortuneOre, EnchantPreference enchantPreference, Predicate<class_1799> good) {
        if (!good.test(itemStack) || !isTool(itemStack)) {
            return -1.0d;
        }
        if (!itemStack.method_7951(state) && ((!itemStack.method_31573(class_3489.field_42611) || (!(state.method_26204() instanceof class_2560) && !state.method_26164(class_3481.field_44469))) && ((!(itemStack.method_7909() instanceof class_1820) || !(state.method_26204() instanceof class_2397)) && !state.method_26164(class_3481.field_44469)))) {
            return -1.0d;
        }
        if (silkTouchEnderChest && state.method_26204() == class_2246.field_10443 && !Utils.hasEnchantments(itemStack, class_1893.field_9099)) {
            return -1.0d;
        }
        if (fortuneOre && isFortunable(state.method_26204()) && !Utils.hasEnchantments(itemStack, class_1893.field_9130)) {
            return -1.0d;
        }
        double score = 0.0d + ((double) (itemStack.method_7924(state) * 1000.0f)) + ((double) Utils.getEnchantmentLevel(itemStack, (class_5321<class_1887>) class_1893.field_9119)) + ((double) Utils.getEnchantmentLevel(itemStack, (class_5321<class_1887>) class_1893.field_9131)) + ((double) Utils.getEnchantmentLevel(itemStack, (class_5321<class_1887>) class_1893.field_9101));
        if (enchantPreference == EnchantPreference.Fortune) {
            score += (double) Utils.getEnchantmentLevel(itemStack, (class_5321<class_1887>) class_1893.field_9130);
        }
        if (enchantPreference == EnchantPreference.SilkTouch) {
            score += (double) Utils.getEnchantmentLevel(itemStack, (class_5321<class_1887>) class_1893.field_9099);
        }
        if (itemStack.method_31573(class_3489.field_42611) && state.method_26164(class_3481.field_44469)) {
            score += (double) (9000.0f + (itemStack.method_7924(state) * 1000.0f));
        }
        return score;
    }

    public static boolean isTool(class_1792 item) {
        return item.method_7854().method_57826(class_9334.field_50077) || (item instanceof class_1820);
    }

    public static boolean isTool(class_1799 itemStack) {
        return isTool(itemStack.method_7909());
    }

    private static boolean isFortunable(class_2248 block) {
        if (block == class_2246.field_22109) {
            return false;
        }
        return Xray.ORES.contains(block) || (block instanceof class_2302);
    }
}
