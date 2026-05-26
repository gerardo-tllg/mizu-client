package meteordevelopment.meteorclient.systems.modules.movement;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_2246;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/movement/NoSlow.class */
public class NoSlow extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Boolean> items;
    private final Setting<WebMode> web;
    private final Setting<Double> webTimer;
    private final Setting<Boolean> honeyBlock;
    private final Setting<Boolean> soulSand;
    private final Setting<Boolean> slimeBlock;
    private final Setting<Boolean> berryBush;
    private final Setting<Boolean> airStrict;
    private final Setting<Boolean> fluidDrag;
    private final Setting<Boolean> sneaking;
    private final Setting<Boolean> crawling;
    private final Setting<Boolean> hunger;
    private final Setting<Boolean> slowness;
    private final Setting<Boolean> climbing;
    private boolean resetTimer;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/movement/NoSlow$WebMode.class */
    public enum WebMode {
        Vanilla,
        Timer,
        Grim,
        None
    }

    public NoSlow() {
        super(Categories.Movement, "no-slow", "Allows you to move normally when using objects that will slow you.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.items = this.sgGeneral.add(new BoolSetting.Builder().name("items").description("Whether or not using items will slow you.").defaultValue(true).build());
        this.web = this.sgGeneral.add(new EnumSetting.Builder().name("web").description("Whether or not cobwebs will not slow you down.").defaultValue(WebMode.Vanilla).build());
        this.webTimer = this.sgGeneral.add(new DoubleSetting.Builder().name("web-timer").description("The timer value for WebMode Timer.").defaultValue(10.0d).min(1.0d).sliderMin(1.0d).visible(() -> {
            return this.web.get() == WebMode.Timer;
        }).build());
        this.honeyBlock = this.sgGeneral.add(new BoolSetting.Builder().name("honey-block").description("Whether or not honey blocks will not slow you down.").defaultValue(true).build());
        this.soulSand = this.sgGeneral.add(new BoolSetting.Builder().name("soul-sand").description("Whether or not soul sand will not slow you down.").defaultValue(true).build());
        this.slimeBlock = this.sgGeneral.add(new BoolSetting.Builder().name("slime-block").description("Whether or not slime blocks will not slow you down.").defaultValue(true).build());
        this.berryBush = this.sgGeneral.add(new BoolSetting.Builder().name("berry-bush").description("Whether or not berry bushes will not slow you down.").defaultValue(true).build());
        this.airStrict = this.sgGeneral.add(new BoolSetting.Builder().name("air-strict").description("Will attempt to bypass anti-cheats like 2b2t's. Only works while in air.").defaultValue(false).build());
        this.fluidDrag = this.sgGeneral.add(new BoolSetting.Builder().name("fluid-drag").description("Whether or not fluid drag will not slow you down.").defaultValue(false).build());
        this.sneaking = this.sgGeneral.add(new BoolSetting.Builder().name("sneaking").description("Whether or not sneaking will not slow you down.").defaultValue(false).build());
        this.crawling = this.sgGeneral.add(new BoolSetting.Builder().name("crawling").description("Whether or not crawling will not slow you down.").defaultValue(false).build());
        this.hunger = this.sgGeneral.add(new BoolSetting.Builder().name("hunger").description("Whether or not hunger will not slow you down.").defaultValue(false).build());
        this.slowness = this.sgGeneral.add(new BoolSetting.Builder().name("slowness").description("Whether or not slowness will not slow you down.").defaultValue(false).build());
        this.climbing = this.sgGeneral.add(new BoolSetting.Builder().name("climbing").description("Whether or not climbing will slow you down.").defaultValue(false).build());
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        this.resetTimer = false;
    }

    public boolean airStrict() {
        return isActive() && this.airStrict.get().booleanValue() && this.mc.field_1724.method_6115();
    }

    public boolean items() {
        return isActive() && this.items.get().booleanValue();
    }

    public boolean honeyBlock() {
        return isActive() && this.honeyBlock.get().booleanValue();
    }

    public boolean soulSand() {
        return isActive() && this.soulSand.get().booleanValue();
    }

    public boolean slimeBlock() {
        return isActive() && this.slimeBlock.get().booleanValue();
    }

    public boolean cobweb() {
        return isActive() && this.web.get() == WebMode.Vanilla;
    }

    public boolean cobwebGrim() {
        return isActive() && this.web.get() == WebMode.Grim;
    }

    public boolean berryBush() {
        return isActive() && this.berryBush.get().booleanValue();
    }

    public boolean fluidDrag() {
        return isActive() && this.fluidDrag.get().booleanValue();
    }

    public boolean sneaking() {
        return isActive() && this.sneaking.get().booleanValue();
    }

    public boolean crawling() {
        return isActive() && this.crawling.get().booleanValue();
    }

    public boolean hunger() {
        return isActive() && this.hunger.get().booleanValue();
    }

    public boolean slowness() {
        return isActive() && this.slowness.get().booleanValue();
    }

    public boolean climbing() {
        return isActive() && this.climbing.get().booleanValue();
    }

    @EventHandler
    private void onPreTick(TickEvent.Pre event) {
        if (this.web.get() == WebMode.Timer) {
            if (this.mc.field_1687.method_8320(this.mc.field_1724.method_24515()).method_26204() == class_2246.field_10343 && !this.mc.field_1724.method_24828()) {
                this.resetTimer = false;
                ((Timer) Modules.get().get(Timer.class)).setOverride(this.webTimer.get().doubleValue());
            } else if (!this.resetTimer) {
                ((Timer) Modules.get().get(Timer.class)).setOverride(1.0d);
                this.resetTimer = true;
            }
        }
        if (this.web.get() == WebMode.Grim) {
        }
    }
}
