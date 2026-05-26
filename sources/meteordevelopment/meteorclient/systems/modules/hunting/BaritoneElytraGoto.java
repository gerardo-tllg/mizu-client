package meteordevelopment.meteorclient.systems.modules.hunting;

import java.lang.reflect.Field;
import java.util.Objects;
import javassist.compiler.TokenId;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.pathing.BaritoneUtils;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_243;
import net.minecraft.class_437;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/hunting/BaritoneElytraGoto.class */
public class BaritoneElytraGoto extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgDestination;
    private final SettingGroup sgBehavior;
    private final Setting<Keybind> mapClickKey;
    private final Setting<Boolean> autoAcceptTerms;
    private final Setting<Boolean> requireElytra;
    private final Setting<Boolean> useManualCoords;
    private final Setting<Integer> targetX;
    private final Setting<Integer> targetZ;
    private final Setting<Boolean> showHud;
    private final Setting<Integer> arrivalRadius;
    private final Setting<Boolean> autoDisable;
    private final Setting<Boolean> notifyArrival;
    private final Setting<Integer> retryDelay;
    private final Setting<Boolean> cancelOnDisable;
    private class_243 destination;
    private boolean flyingActive;
    private int ticksSinceCommand;
    private double initialDistance;

    public BaritoneElytraGoto() {
        super(Categories.Hunting, "baritone-elytra-goto", "Baritone elytra flight to coordinates. Works in Overworld & Nether.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgDestination = this.settings.createGroup("Destination");
        this.sgBehavior = this.settings.createGroup("Behavior");
        this.mapClickKey = this.sgGeneral.add(new KeybindSetting.Builder().name("map-click-key").description("Press this key while hovering over Xaero's map to fly to that location.").defaultValue(Keybind.fromKey(70)).build());
        this.autoAcceptTerms = this.sgGeneral.add(new BoolSetting.Builder().name("auto-accept-terms").description("Automatically accept Baritone elytra terms.").defaultValue(true).build());
        this.requireElytra = this.sgGeneral.add(new BoolSetting.Builder().name("require-elytra").description("Only activate if elytra is equipped.").defaultValue(true).build());
        this.useManualCoords = this.sgDestination.add(new BoolSetting.Builder().name("use-manual-coords").description("Use manually set coordinates instead of waiting for waypoint.").defaultValue(false).build());
        SettingGroup settingGroup = this.sgDestination;
        IntSetting.Builder builderDefaultValue = new IntSetting.Builder().name("target-x").description("Manual target X coordinate.").defaultValue(0);
        Setting<Boolean> setting = this.useManualCoords;
        Objects.requireNonNull(setting);
        this.targetX = settingGroup.add(builderDefaultValue.visible(setting::get).build());
        SettingGroup settingGroup2 = this.sgDestination;
        IntSetting.Builder builderDefaultValue2 = new IntSetting.Builder().name("target-z").description("Manual target Z coordinate.").defaultValue(0);
        Setting<Boolean> setting2 = this.useManualCoords;
        Objects.requireNonNull(setting2);
        this.targetZ = settingGroup2.add(builderDefaultValue2.visible(setting2::get).build());
        this.showHud = this.sgDestination.add(new BoolSetting.Builder().name("show-hud").description("Show destination info in HUD.").defaultValue(true).build());
        this.arrivalRadius = this.sgBehavior.add(new IntSetting.Builder().name("arrival-radius").description("Consider arrived when within this distance (blocks).").defaultValue(50).min(10).max(TokenId.BadToken).sliderRange(10, 200).build());
        this.autoDisable = this.sgBehavior.add(new BoolSetting.Builder().name("auto-disable").description("Auto-disable module on arrival.").defaultValue(true).build());
        this.notifyArrival = this.sgBehavior.add(new BoolSetting.Builder().name("notify-arrival").description("Send chat notification on arrival.").defaultValue(true).build());
        this.retryDelay = this.sgBehavior.add(new IntSetting.Builder().name("retry-delay").description("Ticks between Baritone command retries.").defaultValue(100).min(20).max(200).sliderRange(20, 200).build());
        this.cancelOnDisable = this.sgBehavior.add(new BoolSetting.Builder().name("cancel-on-disable").description("Cancel Baritone pathing when module disabled.").defaultValue(true).build());
        this.destination = null;
        this.flyingActive = false;
        this.ticksSinceCommand = 0;
        this.initialDistance = 0.0d;
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) {
            return;
        }
        if (!BaritoneUtils.IS_AVAILABLE) {
            error("This module requires Baritone to be installed!", new Object[0]);
            toggle();
            return;
        }
        if (this.requireElytra.get().booleanValue() && !this.mc.field_1724.method_31548().method_5438(38).method_7909().toString().contains("elytra")) {
            error("Elytra not equipped!", new Object[0]);
            toggle();
        } else {
            if (this.useManualCoords.get().booleanValue()) {
                this.destination = new class_243(this.targetX.get().intValue(), this.mc.field_1724.method_23318(), this.targetZ.get().intValue());
                this.initialDistance = this.mc.field_1724.method_19538().method_1022(this.destination);
                info(String.format("Flying to manual coordinates: [%d, %d]", this.targetX.get(), this.targetZ.get()), new Object[0]);
                startBaritoneElytra();
                return;
            }
            this.destination = null;
            this.flyingActive = false;
            info("Waiting for destination... Use command or set manual coordinates.", new Object[0]);
        }
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onDeactivate() {
        if (this.cancelOnDisable.get().booleanValue() && this.flyingActive && BaritoneUtils.IS_AVAILABLE) {
            try {
                Class<?> baritoneAPI = Class.forName("baritone.api.BaritoneAPI");
                Object provider = baritoneAPI.getMethod("getProvider", new Class[0]).invoke(null, new Object[0]);
                Object baritone = provider.getClass().getMethod("getPrimaryBaritone", new Class[0]).invoke(provider, new Object[0]);
                Object commandManager = baritone.getClass().getMethod("getCommandManager", new Class[0]).invoke(baritone, new Object[0]);
                commandManager.getClass().getMethod("execute", String.class).invoke(commandManager, "cancel");
                info("Baritone elytra flight cancelled.", new Object[0]);
            } catch (Exception e) {
                warning("Could not cancel Baritone: " + e.getMessage(), new Object[0]);
            }
        }
        this.destination = null;
        this.flyingActive = false;
        this.ticksSinceCommand = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) {
            return;
        }
        if (this.mapClickKey.get().isPressed()) {
            tryFlyToMapLocation();
        }
        if (this.destination == null && !this.useManualCoords.get().booleanValue()) {
            return;
        }
        if (this.useManualCoords.get().booleanValue() && this.destination != null) {
            class_243 newDest = new class_243(this.targetX.get().intValue(), this.mc.field_1724.method_23318(), this.targetZ.get().intValue());
            if (!newDest.equals(this.destination)) {
                this.destination = newDest;
                this.initialDistance = this.mc.field_1724.method_19538().method_1022(this.destination);
                info(String.format("Destination updated: [%d, %d]", this.targetX.get(), this.targetZ.get()), new Object[0]);
                startBaritoneElytra();
            }
        }
        if (this.destination != null) {
            double distance = this.mc.field_1724.method_19538().method_1022(this.destination);
            if (distance <= this.arrivalRadius.get().intValue()) {
                if (this.notifyArrival.get().booleanValue()) {
                    info(String.format("Arrived at destination! (%.1f blocks)", Double.valueOf(distance)), new Object[0]);
                }
                if (this.autoDisable.get().booleanValue()) {
                    toggle();
                    return;
                } else {
                    this.destination = null;
                    this.flyingActive = false;
                }
            }
        }
        if (this.flyingActive && this.destination != null) {
            this.ticksSinceCommand++;
            if (this.ticksSinceCommand >= this.retryDelay.get().intValue()) {
                boolean isPathing = false;
                if (BaritoneUtils.IS_AVAILABLE) {
                    try {
                        Class<?> baritoneAPI = Class.forName("baritone.api.BaritoneAPI");
                        Object provider = baritoneAPI.getMethod("getProvider", new Class[0]).invoke(null, new Object[0]);
                        Object baritone = provider.getClass().getMethod("getPrimaryBaritone", new Class[0]).invoke(provider, new Object[0]);
                        Object pathingBehavior = baritone.getClass().getMethod("getPathingBehavior", new Class[0]).invoke(baritone, new Object[0]);
                        isPathing = ((Boolean) pathingBehavior.getClass().getMethod("isPathing", new Class[0]).invoke(pathingBehavior, new Object[0])).booleanValue();
                    } catch (Exception e) {
                    }
                }
                if (!isPathing) {
                    startBaritoneElytra();
                }
                this.ticksSinceCommand = 0;
            }
        }
    }

    private void startBaritoneElytra() {
        if (this.destination == null || this.mc.field_1724 == null) {
            return;
        }
        if (!BaritoneUtils.IS_AVAILABLE) {
            error("Baritone is not available!", new Object[0]);
            toggle();
            return;
        }
        try {
            Class<?> baritoneAPI = Class.forName("baritone.api.BaritoneAPI");
            if (this.autoAcceptTerms.get().booleanValue()) {
                Object settings = baritoneAPI.getMethod("getSettings", new Class[0]).invoke(null, new Object[0]);
                Object elytraTerms = settings.getClass().getField("elytraTermsAccepted").get(settings);
                elytraTerms.getClass().getField("value").setBoolean(elytraTerms, true);
            }
            Object settings2 = baritoneAPI.getMethod("getSettings", new Class[0]).invoke(null, new Object[0]);
            Object elytraTerms2 = settings2.getClass().getField("elytraTermsAccepted").get(settings2);
            boolean termsAccepted = elytraTerms2.getClass().getField("value").getBoolean(elytraTerms2);
            if (!termsAccepted) {
                error("Baritone elytra terms not accepted! Enable auto-accept or run #elytra once.", new Object[0]);
                toggle();
                return;
            }
            Class<?> goalXZClass = Class.forName("baritone.api.pathing.goals.GoalXZ");
            Object goal = goalXZClass.getConstructor(Integer.TYPE, Integer.TYPE).newInstance(Integer.valueOf((int) this.destination.field_1352), Integer.valueOf((int) this.destination.field_1350));
            Object provider = baritoneAPI.getMethod("getProvider", new Class[0]).invoke(null, new Object[0]);
            Object baritone = provider.getClass().getMethod("getPrimaryBaritone", new Class[0]).invoke(provider, new Object[0]);
            Object customGoalProcess = baritone.getClass().getMethod("getCustomGoalProcess", new Class[0]).invoke(baritone, new Object[0]);
            customGoalProcess.getClass().getMethod("setGoalAndPath", Class.forName("baritone.api.pathing.goals.Goal")).invoke(customGoalProcess, goal);
            Object commandManager = baritone.getClass().getMethod("getCommandManager", new Class[0]).invoke(baritone, new Object[0]);
            commandManager.getClass().getMethod("execute", String.class).invoke(commandManager, "elytra");
            this.flyingActive = true;
            this.ticksSinceCommand = 0;
            double distance = this.mc.field_1724.method_19538().method_1022(this.destination);
            info(String.format("Baritone elytra flight started! Distance: %.0f blocks", Double.valueOf(distance)), new Object[0]);
        } catch (Exception e) {
            error("Failed to start Baritone elytra: " + e.getMessage(), new Object[0]);
            e.printStackTrace();
            toggle();
        }
    }

    public void setDestination(int x, int z) {
        if (this.mc.field_1724 == null) {
            return;
        }
        this.destination = new class_243(x, this.mc.field_1724.method_23318(), z);
        this.initialDistance = this.mc.field_1724.method_19538().method_1022(this.destination);
        if (!isActive()) {
            toggle();
        } else {
            startBaritoneElytra();
        }
        info(String.format("Destination set: [%d, %d] (%.0f blocks away)", Integer.valueOf(x), Integer.valueOf(z), Double.valueOf(this.initialDistance)), new Object[0]);
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public String getInfoString() {
        if (!this.showHud.get().booleanValue()) {
            return null;
        }
        if (this.destination == null) {
            return "No destination";
        }
        double distance = this.mc.field_1724.method_19538().method_1022(this.destination);
        if (distance > 1000.0d) {
            return String.format("%.1fk blocks", Double.valueOf(distance / 1000.0d));
        }
        return String.format("%.0f blocks", Double.valueOf(distance));
    }

    public class_243 getDestination() {
        return this.destination;
    }

    public boolean isFlying() {
        return this.flyingActive && this.destination != null;
    }

    private void tryFlyToMapLocation() {
        if (this.mc.field_1755 == null) {
            return;
        }
        class_437 screen = this.mc.field_1755;
        String screenClass = screen.getClass().getName();
        if (!screenClass.contains("xaero") && !screenClass.contains("Xaero")) {
            return;
        }
        try {
            if (screenClass.contains("GuiMap") || screenClass.contains("WorldMap")) {
                int mapX = getMapCoordinate(screen, "x");
                int mapZ = getMapCoordinate(screen, "z");
                if (mapX != Integer.MIN_VALUE && mapZ != Integer.MIN_VALUE) {
                    setDestination(mapX, mapZ);
                    this.mc.field_1755 = null;
                    return;
                }
            }
            warning("Could not get map coordinates. Use command: .fly <x> <z>", new Object[0]);
        } catch (Exception e) {
            warning("Map integration failed. Use command: .fly <x> <z>", new Object[0]);
        }
    }

    private int getMapCoordinate(class_437 screen, String axis) {
        String[] possibleNames;
        Object value;
        try {
            if (axis.equals("x")) {
                possibleNames = new String[]{"centerX", "mapX", "worldX", "x"};
            } else {
                possibleNames = new String[]{"centerZ", "mapZ", "worldZ", "z"};
            }
            for (String fieldName : possibleNames) {
                try {
                    Field field = screen.getClass().getDeclaredField(fieldName);
                    field.setAccessible(true);
                    value = field.get(screen);
                } catch (NoSuchFieldException e) {
                }
                if (value instanceof Integer) {
                    return ((Integer) value).intValue();
                }
                if (value instanceof Double) {
                    return ((Double) value).intValue();
                }
                if (value instanceof Float) {
                    return ((Float) value).intValue();
                }
            }
            return Integer.MIN_VALUE;
        } catch (Exception e2) {
            return Integer.MIN_VALUE;
        }
    }
}
