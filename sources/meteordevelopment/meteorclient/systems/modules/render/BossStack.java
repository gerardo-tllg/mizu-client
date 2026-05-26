package meteordevelopment.meteorclient.systems.modules.render;

import java.util.HashMap;
import java.util.WeakHashMap;
import meteordevelopment.meteorclient.events.render.RenderBossBarEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_2561;
import net.minecraft.class_345;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/BossStack.class */
public class BossStack extends Module {
    private final SettingGroup sgGeneral;
    public final Setting<Boolean> stack;
    public final Setting<Boolean> hideName;
    private final Setting<Double> spacing;
    public static final WeakHashMap<class_345, Integer> barMap = new WeakHashMap<>();

    public BossStack() {
        super(Categories.Render, "boss-stack", "Stacks boss bars to make your HUD less cluttered.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.stack = this.sgGeneral.add(new BoolSetting.Builder().name("stack").description("Stacks boss bars and adds a counter to the text.").defaultValue(true).build());
        this.hideName = this.sgGeneral.add(new BoolSetting.Builder().name("hide-name").description("Hides the names of boss bars.").defaultValue(false).build());
        this.spacing = this.sgGeneral.add(new DoubleSetting.Builder().name("bar-spacing").description("The spacing reduction between each boss bar.").defaultValue(10.0d).min(0.0d).build());
    }

    @EventHandler
    private void onFetchText(RenderBossBarEvent.BossText event) {
        if (this.hideName.get().booleanValue()) {
            event.name = class_2561.method_30163("");
            return;
        }
        if (barMap.isEmpty() || !this.stack.get().booleanValue()) {
            return;
        }
        class_345 bar = event.bossBar;
        Integer integer = barMap.get(bar);
        barMap.remove(bar);
        if (integer == null || this.hideName.get().booleanValue()) {
            return;
        }
        event.name = event.name.method_27661().method_27693(" x" + integer);
    }

    @EventHandler
    private void onSpaceBars(RenderBossBarEvent.BossSpacing event) {
        event.spacing = this.spacing.get().intValue();
    }

    @EventHandler
    private void onGetBars(RenderBossBarEvent.BossIterator event) {
        if (this.stack.get().booleanValue()) {
            HashMap<String, class_345> chosenBarMap = new HashMap<>();
            event.iterator.forEachRemaining(bar -> {
                String name = bar.method_5414().getString();
                if (chosenBarMap.containsKey(name)) {
                    barMap.compute((class_345) chosenBarMap.get(name), (clientBossBar, integer) -> {
                        return Integer.valueOf(integer == null ? 2 : integer.intValue() + 1);
                    });
                } else {
                    chosenBarMap.put(name, bar);
                }
            });
            event.iterator = chosenBarMap.values().iterator();
        }
    }
}
