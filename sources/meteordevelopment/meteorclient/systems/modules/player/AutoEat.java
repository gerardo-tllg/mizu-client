package meteordevelopment.meteorclient.systems.modules.player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import meteordevelopment.meteorclient.events.entity.player.ItemUseCrosshairTargetEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.pathing.PathManagers;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ItemListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.AnchorAura;
import meteordevelopment.meteorclient.systems.modules.combat.BedAura;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_4174;
import net.minecraft.class_9334;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/player/AutoEat.class */
public class AutoEat extends Module {
    private static final Class<? extends Module>[] AURAS = {KillAura.class, AnchorAura.class, BedAura.class};
    private final SettingGroup sgGeneral;
    private final SettingGroup sgThreshold;
    public final Setting<List<class_1792>> blacklist;
    private final Setting<Boolean> pauseAuras;
    private final Setting<Boolean> pauseBaritone;
    private final Setting<ThresholdMode> thresholdMode;
    private final Setting<Double> healthThreshold;
    private final Setting<Integer> hungerThreshold;
    public boolean eating;
    private int slot;
    private int prevSlot;
    private final List<Class<? extends Module>> wasAura;
    private boolean wasBaritone;

    public AutoEat() {
        super(Categories.Player, "auto-eat", "Automatically eats food.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgThreshold = this.settings.createGroup("Threshold");
        this.blacklist = this.sgGeneral.add(new ItemListSetting.Builder().name("blacklist").description("Which items to not eat.").defaultValue(class_1802.field_8367, class_1802.field_8463, class_1802.field_8233, class_1802.field_8635, class_1802.field_8323, class_1802.field_8726, class_1802.field_8511, class_1802.field_8680, class_1802.field_8766).filter(item -> {
            return item.method_57347().method_58694(class_9334.field_50075) != null;
        }).build());
        this.pauseAuras = this.sgGeneral.add(new BoolSetting.Builder().name("pause-auras").description("Pauses all auras when eating.").defaultValue(true).build());
        this.pauseBaritone = this.sgGeneral.add(new BoolSetting.Builder().name("pause-baritone").description("Pause baritone when eating.").defaultValue(true).build());
        this.thresholdMode = this.sgThreshold.add(new EnumSetting.Builder().name("threshold-mode").description("The threshold mode to trigger auto eat.").defaultValue(ThresholdMode.Any).build());
        this.healthThreshold = this.sgThreshold.add(new DoubleSetting.Builder().name("health-threshold").description("The level of health you eat at.").defaultValue(10.0d).range(1.0d, 19.0d).sliderRange(1.0d, 19.0d).visible(() -> {
            return this.thresholdMode.get() != ThresholdMode.Hunger;
        }).build());
        this.hungerThreshold = this.sgThreshold.add(new IntSetting.Builder().name("hunger-threshold").description("The level of hunger you eat at.").defaultValue(16).range(1, 19).sliderRange(1, 19).visible(() -> {
            return this.thresholdMode.get() != ThresholdMode.Health;
        }).build());
        this.wasAura = new ArrayList();
        this.wasBaritone = false;
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onDeactivate() {
        if (this.eating) {
            stopEating();
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onTick(TickEvent.Pre event) {
        if (this.eating) {
            if (shouldEat()) {
                if (this.mc.field_1724.method_31548().method_5438(this.slot).method_58694(class_9334.field_50075) != null) {
                    int slot = findSlot();
                    if (slot == -1) {
                        stopEating();
                        return;
                    }
                    changeSlot(slot);
                }
                eat();
                return;
            }
            stopEating();
            return;
        }
        if (shouldEat()) {
            this.slot = findSlot();
            if (this.slot != -1) {
                startEating();
            }
        }
    }

    @EventHandler
    private void onItemUseCrosshairTarget(ItemUseCrosshairTargetEvent event) {
        if (this.eating) {
            event.target = null;
        }
    }

    private void startEating() {
        this.prevSlot = this.mc.field_1724.method_31548().field_7545;
        eat();
        this.wasAura.clear();
        if (this.pauseAuras.get().booleanValue()) {
            for (Class<? extends Module> klass : AURAS) {
                Module module = Modules.get().get((Class<Module>) klass);
                if (module.isActive()) {
                    this.wasAura.add(klass);
                    module.toggle();
                }
            }
        }
        if (this.pauseBaritone.get().booleanValue() && PathManagers.get().isPathing() && !this.wasBaritone) {
            this.wasBaritone = true;
            PathManagers.get().pause();
        }
    }

    private void eat() {
        changeSlot(this.slot);
        setPressed(true);
        if (!this.mc.field_1724.method_6115()) {
            Utils.rightClick();
        }
        this.eating = true;
    }

    private void stopEating() {
        changeSlot(this.prevSlot);
        setPressed(false);
        this.eating = false;
        if (this.pauseAuras.get().booleanValue()) {
            for (Class<? extends Module> klass : AURAS) {
                Module module = Modules.get().get((Class<Module>) klass);
                if (this.wasAura.contains(klass) && !module.isActive()) {
                    module.toggle();
                }
            }
        }
        if (this.pauseBaritone.get().booleanValue() && this.wasBaritone) {
            this.wasBaritone = false;
            PathManagers.get().resume();
        }
    }

    private void setPressed(boolean pressed) {
        this.mc.field_1690.field_1904.method_23481(pressed);
    }

    private void changeSlot(int slot) {
        InvUtils.swap(slot, false);
        this.slot = slot;
    }

    public boolean shouldEat() {
        boolean health = ((double) this.mc.field_1724.method_6032()) <= this.healthThreshold.get().doubleValue();
        boolean hunger = this.mc.field_1724.method_7344().method_7586() <= this.hungerThreshold.get().intValue();
        return this.thresholdMode.get().test(health, hunger);
    }

    private int findSlot() {
        int hunger;
        int slot = -1;
        int bestHunger = -1;
        for (int i = 0; i < 9; i++) {
            class_1792 item = this.mc.field_1724.method_31548().method_5438(i).method_7909();
            class_4174 foodComponent = (class_4174) item.method_57347().method_58694(class_9334.field_50075);
            if (foodComponent != null && (hunger = foodComponent.comp_2491()) > bestHunger && !this.blacklist.get().contains(item)) {
                slot = i;
                bestHunger = hunger;
            }
        }
        class_1792 offHandItem = this.mc.field_1724.method_6079().method_7909();
        if (offHandItem.method_57347().method_58694(class_9334.field_50075) != null && !this.blacklist.get().contains(offHandItem) && ((class_4174) offHandItem.method_57347().method_58694(class_9334.field_50075)).comp_2491() > bestHunger) {
            slot = 45;
        }
        return slot;
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/player/AutoEat$ThresholdMode.class */
    public enum ThresholdMode {
        Health((health, hunger) -> {
            return health.booleanValue();
        }),
        Hunger((health2, hunger2) -> {
            return hunger2.booleanValue();
        }),
        Any((health3, hunger3) -> {
            return health3.booleanValue() || hunger3.booleanValue();
        }),
        Both((health4, hunger4) -> {
            return health4.booleanValue() && hunger4.booleanValue();
        });

        private final BiPredicate<Boolean, Boolean> predicate;

        ThresholdMode(BiPredicate predicate) {
            this.predicate = predicate;
        }

        public boolean test(boolean health, boolean hunger) {
            return this.predicate.test(Boolean.valueOf(health), Boolean.valueOf(hunger));
        }
    }
}
