package meteordevelopment.meteorclient.systems.modules.hunting;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.pathing.BaritoneUtils;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.SlotUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

/**
 * BaritoneElytraGoto - Baritone elytra flight for Overworld & Nether
 * 
 * Integrates with Xaero's map or manual coordinates
 * Triggers Baritone elytra flight to destination
 * Works in both Overworld and Nether (unlike XaeroPlus native feature)
 */
public class BaritoneElytraGoto extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgDestination = settings.createGroup("Destination");
    private final SettingGroup sgBehavior = settings.createGroup("Behavior");

    // General
    private final Setting<Keybind> mapClickKey = sgGeneral.add(new KeybindSetting.Builder()
        .name("map-click-key")
        .description("Press this key while hovering over Xaero's map to fly to that location.")
        .defaultValue(Keybind.fromKey(GLFW.GLFW_KEY_F))
        .build()
    );

    private final Setting<Boolean> autoAcceptTerms = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-accept-terms")
        .description("Automatically accept Baritone elytra terms.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> requireElytra = sgGeneral.add(new BoolSetting.Builder()
        .name("require-elytra")
        .description("Only activate if elytra is equipped.")
        .defaultValue(true)
        .build()
    );

    // Destination
    private final Setting<Boolean> useManualCoords = sgDestination.add(new BoolSetting.Builder()
        .name("use-manual-coords")
        .description("Use manually set coordinates instead of waiting for waypoint.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> targetX = sgDestination.add(new IntSetting.Builder()
        .name("target-x")
        .description("Manual target X coordinate.")
        .defaultValue(0)
        .visible(useManualCoords::get)
        .build()
    );

    private final Setting<Integer> targetZ = sgDestination.add(new IntSetting.Builder()
        .name("target-z")
        .description("Manual target Z coordinate.")
        .defaultValue(0)
        .visible(useManualCoords::get)
        .build()
    );

    private final Setting<Boolean> showHud = sgDestination.add(new BoolSetting.Builder()
        .name("show-hud")
        .description("Show destination info in HUD.")
        .defaultValue(true)
        .build()
    );

    // Behavior
    private final Setting<Integer> arrivalRadius = sgBehavior.add(new IntSetting.Builder()
        .name("arrival-radius")
        .description("Consider arrived when within this distance (blocks).")
        .defaultValue(50)
        .min(10)
        .max(500)
        .sliderRange(10, 200)
        .build()
    );

    private final Setting<Boolean> autoDisable = sgBehavior.add(new BoolSetting.Builder()
        .name("auto-disable")
        .description("Auto-disable module on arrival.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> notifyArrival = sgBehavior.add(new BoolSetting.Builder()
        .name("notify-arrival")
        .description("Send chat notification on arrival.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> retryDelay = sgBehavior.add(new IntSetting.Builder()
        .name("retry-delay")
        .description("Ticks between Baritone command retries.")
        .defaultValue(100)
        .min(20)
        .max(200)
        .sliderRange(20, 200)
        .build()
    );

    private final Setting<Boolean> cancelOnDisable = sgBehavior.add(new BoolSetting.Builder()
        .name("cancel-on-disable")
        .description("Cancel Baritone pathing when module disabled.")
        .defaultValue(true)
        .build()
    );

    // State
    private Vec3d destination = null;
    private boolean flyingActive = false;
    private int ticksSinceCommand = 0;
    private double initialDistance = 0;

    public BaritoneElytraGoto() {
        super(Categories.Hunting, "baritone-elytra-goto", "Baritone elytra flight to coordinates. Works in Overworld & Nether.");
    }

    @Override
    public void onActivate() {
        if (mc.player == null || mc.world == null) return;

        // Check if Baritone is available
        if (!BaritoneUtils.IS_AVAILABLE) {
            error("This module requires Baritone to be installed!");
            toggle();
            return;
        }

        // Check elytra requirement
        if (requireElytra.get() && !mc.player.getInventory().getStack(SlotUtils.ARMOR_START + 2).getItem().toString().contains("elytra")) {
            error("Elytra not equipped!");
            toggle();
            return;
        }

        // Set destination
        if (useManualCoords.get()) {
            destination = new Vec3d(targetX.get(), mc.player.getY(), targetZ.get());
            initialDistance = mc.player.getPos().distanceTo(destination);
            info(String.format("Flying to manual coordinates: [%d, %d]", targetX.get(), targetZ.get()));
            startBaritoneElytra();
        } else {
            destination = null;
            flyingActive = false;
            info("Waiting for destination... Use command or set manual coordinates.");
        }
    }

    @Override
    public void onDeactivate() {
        if (cancelOnDisable.get() && flyingActive && BaritoneUtils.IS_AVAILABLE) {
            try {
                Class<?> baritoneAPI = Class.forName("baritone.api.BaritoneAPI");
                Object provider = baritoneAPI.getMethod("getProvider").invoke(null);
                Object baritone = provider.getClass().getMethod("getPrimaryBaritone").invoke(provider);
                Object commandManager = baritone.getClass().getMethod("getCommandManager").invoke(baritone);
                commandManager.getClass().getMethod("execute", String.class).invoke(commandManager, "cancel");
                info("Baritone elytra flight cancelled.");
            } catch (Exception e) {
                warning("Could not cancel Baritone: " + e.getMessage());
            }
        }
        
        destination = null;
        flyingActive = false;
        ticksSinceCommand = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;

        // Check for map click keybind
        if (mapClickKey.get().isPressed()) {
            tryFlyToMapLocation();
        }

        // Wait for destination if not set
        if (destination == null && !useManualCoords.get()) {
            return;
        }

        // Update manual coords if changed
        if (useManualCoords.get() && destination != null) {
            Vec3d newDest = new Vec3d(targetX.get(), mc.player.getY(), targetZ.get());
            if (!newDest.equals(destination)) {
                destination = newDest;
                initialDistance = mc.player.getPos().distanceTo(destination);
                info(String.format("Destination updated: [%d, %d]", targetX.get(), targetZ.get()));
                startBaritoneElytra();
            }
        }

        // Check if arrived
        if (destination != null) {
            double distance = mc.player.getPos().distanceTo(destination);
            
            if (distance <= arrivalRadius.get()) {
                if (notifyArrival.get()) {
                    info(String.format("Arrived at destination! (%.1f blocks)", distance));
                }
                
                if (autoDisable.get()) {
                    toggle();
                    return;
                }
                
                destination = null;
                flyingActive = false;
            }
        }

        // Retry Baritone command if needed
        if (flyingActive && destination != null) {
            ticksSinceCommand++;
            
            if (ticksSinceCommand >= retryDelay.get()) {
                // Check if Baritone is still active
                boolean isPathing = false;
                if (BaritoneUtils.IS_AVAILABLE) {
                    try {
                        Class<?> baritoneAPI = Class.forName("baritone.api.BaritoneAPI");
                        Object provider = baritoneAPI.getMethod("getProvider").invoke(null);
                        Object baritone = provider.getClass().getMethod("getPrimaryBaritone").invoke(provider);
                        Object pathingBehavior = baritone.getClass().getMethod("getPathingBehavior").invoke(baritone);
                        isPathing = (Boolean) pathingBehavior.getClass().getMethod("isPathing").invoke(pathingBehavior);
                    } catch (Exception e) {
                        // Ignore
                    }
                }
                
                if (!isPathing) {
                    // Restart elytra flight
                    startBaritoneElytra();
                }
                
                ticksSinceCommand = 0;
            }
        }
    }

    private void startBaritoneElytra() {
        if (destination == null || mc.player == null) return;
        if (!BaritoneUtils.IS_AVAILABLE) {
            error("Baritone is not available!");
            toggle();
            return;
        }

        try {
            Class<?> baritoneAPI = Class.forName("baritone.api.BaritoneAPI");
            
            // Accept terms if needed
            if (autoAcceptTerms.get()) {
                Object settings = baritoneAPI.getMethod("getSettings").invoke(null);
                Object elytraTerms = settings.getClass().getField("elytraTermsAccepted").get(settings);
                elytraTerms.getClass().getField("value").setBoolean(elytraTerms, true);
            }

            // Check if terms accepted
            Object settings = baritoneAPI.getMethod("getSettings").invoke(null);
            Object elytraTerms = settings.getClass().getField("elytraTermsAccepted").get(settings);
            boolean termsAccepted = elytraTerms.getClass().getField("value").getBoolean(elytraTerms);
            
            if (!termsAccepted) {
                error("Baritone elytra terms not accepted! Enable auto-accept or run #elytra once.");
                toggle();
                return;
            }

            // Set goal using reflection
            Class<?> goalXZClass = Class.forName("baritone.api.pathing.goals.GoalXZ");
            Object goal = goalXZClass.getConstructor(int.class, int.class)
                .newInstance((int) destination.x, (int) destination.z);
            
            Object provider = baritoneAPI.getMethod("getProvider").invoke(null);
            Object baritone = provider.getClass().getMethod("getPrimaryBaritone").invoke(provider);
            Object customGoalProcess = baritone.getClass().getMethod("getCustomGoalProcess").invoke(baritone);
            customGoalProcess.getClass().getMethod("setGoalAndPath", Class.forName("baritone.api.pathing.goals.Goal"))
                .invoke(customGoalProcess, goal);

            // Execute elytra command
            Object commandManager = baritone.getClass().getMethod("getCommandManager").invoke(baritone);
            commandManager.getClass().getMethod("execute", String.class).invoke(commandManager, "elytra");
            
            flyingActive = true;
            ticksSinceCommand = 0;
            
            double distance = mc.player.getPos().distanceTo(destination);
            info(String.format("Baritone elytra flight started! Distance: %.0f blocks", distance));

        } catch (Exception e) {
            error("Failed to start Baritone elytra: " + e.getMessage());
            e.printStackTrace();
            toggle();
        }
    }

    /**
     * Set destination from command or external source
     */
    public void setDestination(int x, int z) {
        if (mc.player == null) return;
        
        destination = new Vec3d(x, mc.player.getY(), z);
        initialDistance = mc.player.getPos().distanceTo(destination);
        
        if (!isActive()) {
            toggle();
        } else {
            startBaritoneElytra();
        }
        
        info(String.format("Destination set: [%d, %d] (%.0f blocks away)", x, z, initialDistance));
    }

    /**
     * Get info string for HUD
     */
    @Override
    public String getInfoString() {
        if (!showHud.get()) return null;
        if (destination == null) return "No destination";
        
        double distance = mc.player.getPos().distanceTo(destination);
        
        if (distance > 1000) {
            return String.format("%.1fk blocks", distance / 1000);
        } else {
            return String.format("%.0f blocks", distance);
        }
    }

    // Getters for command access
    public Vec3d getDestination() {
        return destination;
    }

    public boolean isFlying() {
        return flyingActive && destination != null;
    }

    /**
     * Try to fly to map location using reflection to access Xaero's map coordinates
     */
    private void tryFlyToMapLocation() {
        if (mc.currentScreen == null) return;

        Screen screen = mc.currentScreen;
        String screenClass = screen.getClass().getName();

        // Check if it's Xaero's map screen
        if (!screenClass.contains("xaero") && !screenClass.contains("Xaero")) {
            return;
        }

        try {
            // Try to get map coordinates from Xaero's World Map
            if (screenClass.contains("GuiMap") || screenClass.contains("WorldMap")) {
                // Get map center coordinates using reflection
                int mapX = getMapCoordinate(screen, "x");
                int mapZ = getMapCoordinate(screen, "z");

                if (mapX != Integer.MIN_VALUE && mapZ != Integer.MIN_VALUE) {
                    setDestination(mapX, mapZ);
                    mc.currentScreen = null; // Close map
                    return;
                }
            }

            // If reflection failed, show help message
            warning("Could not get map coordinates. Use command: .fly <x> <z>");

        } catch (Exception e) {
            warning("Map integration failed. Use command: .fly <x> <z>");
        }
    }

    /**
     * Get map coordinate using reflection (tries multiple field names)
     */
    private int getMapCoordinate(Screen screen, String axis) {
        try {
            // Common field names in Xaero's map
            String[] possibleNames;
            if (axis.equals("x")) {
                possibleNames = new String[]{"centerX", "mapX", "worldX", "x"};
            } else {
                possibleNames = new String[]{"centerZ", "mapZ", "worldZ", "z"};
            }

            for (String fieldName : possibleNames) {
                try {
                    var field = screen.getClass().getDeclaredField(fieldName);
                    field.setAccessible(true);
                    Object value = field.get(screen);
                    
                    if (value instanceof Integer) {
                        return (Integer) value;
                    } else if (value instanceof Double) {
                        return ((Double) value).intValue();
                    } else if (value instanceof Float) {
                        return ((Float) value).intValue();
                    }
                } catch (NoSuchFieldException ignored) {
                    // Try next field name
                }
            }
        } catch (Exception e) {
            // Reflection failed
        }

        return Integer.MIN_VALUE;
    }
}
