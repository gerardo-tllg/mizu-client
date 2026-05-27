package meteordevelopment.meteorclient.systems.modules.combat.autocrystal;

/**
 * Tracks per-tick packet budget shared across combat modules.
 * Reset to false once per tick by AutoCrystal's LOWEST-priority TickEvent handler.
 * Other modules (Surround, FeetTrap, etc.) can check and set these flags to
 * coordinate so only one place and one break packet is sent per tick.
 */
public class CrystalTickBudget {
    private CrystalTickBudget() {}

    public static boolean placeUsed   = false;
    public static boolean breakUsed   = false;
    public static boolean surroundUsed = false;

    public static void reset() {
        placeUsed    = false;
        breakUsed    = false;
        surroundUsed = false;
    }
}
