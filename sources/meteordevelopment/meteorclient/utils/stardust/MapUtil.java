package meteordevelopment.meteorclient.utils.stardust;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import javassist.bytecode.Opcode;
import net.minecraft.class_2338;
import org.jetbrains.annotations.Nullable;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.waypoint.WaypointColor;
import xaero.hud.minimap.waypoint.WaypointPurpose;
import xaero.hud.minimap.waypoint.set.WaypointSet;
import xaero.hud.minimap.world.MinimapWorld;
import xaero.map.mods.SupportMods;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/stardust/MapUtil.class */
public class MapUtil {

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/stardust/MapUtil$Purpose.class */
    public enum Purpose {
        Normal,
        Destination
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/stardust/MapUtil$WpColor.class */
    public enum WpColor {
        Black,
        Dark_Blue,
        Dark_Green,
        Dark_Aqua,
        Dark_Red,
        Dark_Purple,
        Gold,
        Gray,
        Dark_Gray,
        Blue,
        Green,
        Aqua,
        Red,
        Purple,
        Yellow,
        White,
        Random
    }

    public static void addWaypoint(class_2338 pos, String name, String initials, Purpose purpose, WpColor color, boolean temp) {
        if (StardustUtil.XAERO_AVAILABLE) {
            XaeroIntegration.addWaypoint(pos, name, initials, purpose, color, temp);
        }
    }

    public static void removeWaypoints(String name, Predicate<class_2338> posPredicate, Optional<Integer> yOverride) {
        if (StardustUtil.XAERO_AVAILABLE) {
            XaeroIntegration.removeWaypoints(name, posPredicate, yOverride);
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/stardust/MapUtil$XaeroIntegration.class */
    private static class XaeroIntegration {
        private XaeroIntegration() {
        }

        static void addWaypoint(class_2338 pos, String name, String initials, Purpose purpose, WpColor color, boolean temp) {
            try {
                if (getWaypointByCoordinate(pos.method_10263(), pos.method_10260()) != null) {
                    LogUtil.warn("Cancelling duplicate waypoint with name: \"" + name + "\"..!", "MapUtil");
                    return;
                }
                WaypointSet set = getWaypointSet();
                if (set == null) {
                    LogUtil.warn("Cancelling waypoint with name \"" + name + "\" because the waypoint set is null..!", "MapUtil");
                    return;
                }
                Waypoint waypoint = new Waypoint(pos.method_10263(), pos.method_10264(), pos.method_10260(), name, initials, getColor(color), getPurpose(purpose), temp);
                set.add(waypoint);
                SupportMods.xaeroMinimap.requestWaypointsRefresh();
                saveWaypoints();
            } catch (Exception err) {
                LogUtil.error("Error while trying to add waypoint to Xaero map! Why - " + String.valueOf(err), "MapUtil");
            }
        }

        @Nullable
        static MinimapSession getMinimapSession() {
            return BuiltInHudModules.MINIMAP.getCurrentSession();
        }

        @Nullable
        static MinimapWorld getWaypointWorld() {
            MinimapSession session = getMinimapSession();
            if (session == null) {
                return null;
            }
            return session.getWorldManager().getCurrentWorld();
        }

        @Nullable
        static WaypointSet getWaypointSet() {
            MinimapWorld currentWorld = getWaypointWorld();
            if (currentWorld == null) {
                return null;
            }
            return currentWorld.getCurrentWaypointSet();
        }

        @Nullable
        static Waypoint getWaypointByCoordinate(int x, int z) {
            WaypointSet waypointSet = getWaypointSet();
            if (waypointSet == null) {
                return null;
            }
            for (Waypoint waypoint : waypointSet.getWaypoints()) {
                if (waypoint.getX() == x && waypoint.getZ() == z) {
                    return waypoint;
                }
            }
            return null;
        }

        static void removeWaypoints(String name, Predicate<class_2338> posPredicate, Optional<Integer> yOverride) {
            try {
                WaypointSet set = getWaypointSet();
                if (set == null) {
                    return;
                }
                class_2338.class_2339 mPos = new class_2338.class_2339();
                ObjectArrayList objectArrayList = new ObjectArrayList();
                for (Waypoint wp : set.getWaypoints()) {
                    if (wp.getName().trim().startsWith(name.trim())) {
                        Objects.requireNonNull(wp);
                        int y = yOverride.orElseGet(wp::getY).intValue();
                        mPos.method_10103(wp.getX(), y, wp.getZ());
                        if (posPredicate.test(mPos)) {
                            objectArrayList.add(wp);
                        }
                    }
                }
                Iterator it = objectArrayList.iterator();
                while (it.hasNext()) {
                    set.remove((Waypoint) it.next());
                }
                saveWaypoints();
            } catch (Exception err) {
                LogUtil.error("Error while trying to remove waypoints from Xaero map! Why - " + String.valueOf(err), "MapUtil");
            }
        }

        static void saveWaypoints() {
            try {
                MinimapSession session = getMinimapSession();
                MinimapWorld world = getWaypointWorld();
                if (world != null) {
                    session.getWorldManagerIO().saveWorld(world);
                }
            } catch (Exception err) {
                LogUtil.error("Failed saving minimap waypoints! Why: " + String.valueOf(err), "MapUtil");
            }
        }

        /* JADX INFO: Thrown type has an unknown type hierarchy: java.lang.MatchException */
        static WaypointPurpose getPurpose(Purpose purpose) throws MatchException {
            switch (purpose) {
                case Normal:
                    return WaypointPurpose.NORMAL;
                case Destination:
                    return WaypointPurpose.DESTINATION;
                default:
                    throw new MatchException((String) null, (Throwable) null);
            }
        }

        /* JADX INFO: Thrown type has an unknown type hierarchy: java.lang.MatchException */
        static WaypointColor getColor(WpColor color) throws MatchException {
            switch (color.ordinal()) {
                case 0:
                    return WaypointColor.BLACK;
                case 1:
                    return WaypointColor.DARK_BLUE;
                case 2:
                    return WaypointColor.DARK_GREEN;
                case 3:
                    return WaypointColor.DARK_AQUA;
                case 4:
                    return WaypointColor.DARK_RED;
                case 5:
                    return WaypointColor.DARK_PURPLE;
                case 6:
                    return WaypointColor.GOLD;
                case 7:
                    return WaypointColor.GRAY;
                case 8:
                    return WaypointColor.DARK_GRAY;
                case 9:
                    return WaypointColor.BLUE;
                case 10:
                    return WaypointColor.GREEN;
                case 11:
                    return WaypointColor.AQUA;
                case 12:
                    return WaypointColor.RED;
                case Opcode.FCONST_2 /* 13 */:
                    return WaypointColor.PURPLE;
                case Opcode.DCONST_0 /* 14 */:
                    return WaypointColor.YELLOW;
                case 15:
                    return WaypointColor.WHITE;
                case 16:
                    return WaypointColor.getRandom();
                default:
                    throw new MatchException((String) null, (Throwable) null);
            }
        }
    }
}
