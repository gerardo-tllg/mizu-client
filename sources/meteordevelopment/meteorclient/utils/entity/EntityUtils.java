package meteordevelopment.meteorclient.utils.entity;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongBidirectionalIterator;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.mixin.EntityTrackingSectionAccessor;
import meteordevelopment.meteorclient.mixin.SectionedEntityCacheAccessor;
import meteordevelopment.meteorclient.mixin.SimpleEntityLookupAccessor;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1690;
import net.minecraft.class_1934;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_2586;
import net.minecraft.class_2680;
import net.minecraft.class_3609;
import net.minecraft.class_3612;
import net.minecraft.class_4076;
import net.minecraft.class_5572;
import net.minecraft.class_5578;
import net.minecraft.class_640;
import net.minecraft.class_7264;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/entity/EntityUtils.class */
public class EntityUtils {
    private static final class_2338.class_2339 testPos = new class_2338.class_2339();

    private EntityUtils() {
    }

    public static boolean isAttackable(class_1299<?> type) {
        return (type == class_1299.field_6083 || type == class_1299.field_6122 || type == class_1299.field_6089 || type == class_1299.field_6133 || type == class_1299.field_6052 || type == class_1299.field_6124 || type == class_1299.field_6135 || type == class_1299.field_6082 || type == class_1299.field_6064 || type == class_1299.field_56254 || type == class_1299.field_56255 || type == class_1299.field_6127 || type == class_1299.field_6112 || type == class_1299.field_6103 || type == class_1299.field_6044 || type == class_1299.field_6144) ? false : true;
    }

    public static boolean isRideable(class_1299<?> type) {
        return type == class_1299.field_6096 || class_1690.class.isAssignableFrom(type.method_31794()) || class_7264.class.isAssignableFrom(type.method_31794()) || type == class_1299.field_40116 || type == class_1299.field_6067 || type == class_1299.field_6139 || type == class_1299.field_6074 || type == class_1299.field_6057 || type == class_1299.field_6093 || type == class_1299.field_6075 || type == class_1299.field_23214 || type == class_1299.field_6048;
    }

    public static float getTotalHealth(class_1309 target) {
        return target.method_6032() + target.method_6067();
    }

    public static int getPing(class_1657 player) {
        if (MeteorClient.mc.method_1562() == null) {
            return 0;
        }
        class_640 playerListEntry = MeteorClient.mc.method_1562().method_2871(player.method_5667());
        if (playerListEntry == null && MeteorClient.mc.method_1562().method_2880() != null) {
            Iterator it = MeteorClient.mc.method_1562().method_2880().iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                class_640 entry = (class_640) it.next();
                if (entry.method_2966().getName().equals(player.method_7334().getName())) {
                    playerListEntry = entry;
                    break;
                }
            }
        }
        if (playerListEntry == null) {
            return 0;
        }
        return playerListEntry.method_2959();
    }

    public static class_1934 getGameMode(class_1657 player) {
        if (player == null) {
            return null;
        }
        class_640 playerListEntry = MeteorClient.mc.method_1562().method_2871(player.method_5667());
        if (playerListEntry == null && MeteorClient.mc.method_1562() != null && MeteorClient.mc.method_1562().method_2880() != null) {
            Iterator it = MeteorClient.mc.method_1562().method_2880().iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                class_640 entry = (class_640) it.next();
                if (entry.method_2966().getName().equals(player.method_7334().getName())) {
                    playerListEntry = entry;
                    break;
                }
            }
        }
        if (playerListEntry == null) {
            return null;
        }
        return playerListEntry.method_2958();
    }

    public static boolean isAboveWater(class_1297 entity) {
        class_2338.class_2339 blockPos = entity.method_24515().method_25503();
        for (int i = 0; i < 64; i++) {
            class_2680 state = MeteorClient.mc.field_1687.method_8320(blockPos);
            if (!state.method_51366()) {
                class_3609 class_3609VarMethod_15772 = state.method_26227().method_15772();
                if (class_3609VarMethod_15772 == class_3612.field_15910 || class_3609VarMethod_15772 == class_3612.field_15909) {
                    return true;
                }
                blockPos.method_10100(0, -1, 0);
            } else {
                return false;
            }
        }
        return false;
    }

    public static boolean isInRenderDistance(class_1297 entity) {
        if (entity == null) {
            return false;
        }
        return isInRenderDistance(entity.method_23317(), entity.method_23321());
    }

    public static boolean isInRenderDistance(class_2586 entity) {
        if (entity == null) {
            return false;
        }
        return isInRenderDistance(entity.method_11016().method_10263(), entity.method_11016().method_10260());
    }

    public static boolean isInRenderDistance(class_2338 pos) {
        if (pos == null) {
            return false;
        }
        return isInRenderDistance(pos.method_10263(), pos.method_10260());
    }

    public static boolean isInRenderDistance(double posX, double posZ) {
        double x = Math.abs(MeteorClient.mc.field_1773.method_19418().method_19326().field_1352 - posX);
        double z = Math.abs(MeteorClient.mc.field_1773.method_19418().method_19326().field_1350 - posZ);
        double d = (((Integer) MeteorClient.mc.field_1690.method_42503().method_41753()).intValue() + 1) * 16;
        return x < d && z < d;
    }

    public static class_2338 getCityBlock(class_1657 player) {
        if (player == null) {
            return null;
        }
        double bestDistanceSquared = 36.0d;
        class_2350 bestDirection = null;
        for (class_2350 direction : class_2350.field_11041) {
            testPos.method_10101(player.method_24515().method_10093(direction));
            class_2248 block = MeteorClient.mc.field_1687.method_8320(testPos).method_26204();
            if (block == class_2246.field_10540 || block == class_2246.field_22108 || block == class_2246.field_22423 || block == class_2246.field_23152 || block == class_2246.field_22109) {
                double testDistanceSquared = PlayerUtils.squaredDistanceTo((class_2338) testPos);
                if (testDistanceSquared < bestDistanceSquared) {
                    bestDistanceSquared = testDistanceSquared;
                    bestDirection = direction;
                }
            }
        }
        if (bestDirection == null) {
            return null;
        }
        return player.method_24515().method_10093(bestDirection);
    }

    public static String getName(class_1297 entity) {
        if (entity == null) {
            return null;
        }
        return entity instanceof class_1657 ? entity.method_5477().getString() : entity.method_5864().method_5897().getString();
    }

    public static Color getColorFromDistance(class_1297 entity) {
        int g;
        int r;
        Color distanceColor = new Color(255, 255, 255);
        double distance = PlayerUtils.distanceToCamera(entity);
        double percent = distance / 60.0d;
        if (percent < 0.0d || percent > 1.0d) {
            distanceColor.set(0, 255, 0, 255);
            return distanceColor;
        }
        if (percent < 0.5d) {
            r = 255;
            g = (int) ((255.0d * percent) / 0.5d);
        } else {
            g = 255;
            r = 255 - ((int) ((255.0d * (percent - 0.5d)) / 0.5d));
        }
        distanceColor.set(r, g, 0, 255);
        return distanceColor;
    }

    public static boolean intersectsWithEntity(class_238 box, Predicate<class_1297> predicate) {
        EntityTrackingSectionAccessor entityTrackingSectionAccessor;
        SimpleEntityLookupAccessor entityLookup = MeteorClient.mc.field_1687.getEntityLookup();
        if (entityLookup instanceof class_5578) {
            SectionedEntityCacheAccessor cache = ((class_5578) entityLookup).getCache();
            LongSortedSet trackedPositions = cache.getTrackedPositions();
            Long2ObjectMap<class_5572<class_1297>> trackingSections = cache.getTrackingSections();
            int i = class_4076.method_32204(box.field_1323 - 2.0d);
            int j = class_4076.method_32204(box.field_1322 - 2.0d);
            int k = class_4076.method_32204(box.field_1321 - 2.0d);
            int l = class_4076.method_32204(box.field_1320 + 2.0d);
            int m = class_4076.method_32204(box.field_1325 + 2.0d);
            int n = class_4076.method_32204(box.field_1324 + 2.0d);
            for (int o = i; o <= l; o++) {
                long p = class_4076.method_18685(o, 0, 0);
                long q = class_4076.method_18685(o, -1, -1);
                LongBidirectionalIterator longIterator = trackedPositions.subSet(p, q + 1).iterator();
                while (longIterator.hasNext()) {
                    long r = longIterator.nextLong();
                    int s = class_4076.method_18689(r);
                    int t = class_4076.method_18690(r);
                    if (s >= j && s <= m && t >= k && t <= n && (entityTrackingSectionAccessor = (class_5572) trackingSections.get(r)) != null && entityTrackingSectionAccessor.method_31768().method_31885()) {
                        for (class_1297 entity : entityTrackingSectionAccessor.getCollection()) {
                            if (entity.method_5829().method_994(box) && predicate.test(entity)) {
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        }
        AtomicBoolean found = new AtomicBoolean(false);
        entityLookup.method_31807(box, entity2 -> {
            if (found.get() || !predicate.test(entity2)) {
                return;
            }
            found.set(true);
        });
        return found.get();
    }

    public static class_1299<?> getGroup(class_1297 entity) {
        return entity.method_5864();
    }
}
