package meteordevelopment.meteorclient.utils;

import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.class_1657;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_3532;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/CrystalUtils.class */
public class CrystalUtils {
    public static double calculateCrystalDamage(class_243 crystalPos, class_1657 player) {
        return calculateCrystalDamage(crystalPos, player, false);
    }

    public static double calculateCrystalDamage(class_243 crystalPos, class_1657 player, boolean ignoreWalls) {
        if (MeteorClient.mc.field_1687 == null || player == null) {
            return 0.0d;
        }
        double distance = Math.sqrt(player.method_5707(crystalPos));
        if (distance > 12.0d) {
            return 0.0d;
        }
        double impact = (1.0d - (distance / 12.0d)) * ((double) getExposure(crystalPos, player, ignoreWalls));
        double damage = ((((impact * impact) + impact) / 2.0d) * 7.0d * 6.0d) + 1.0d;
        return applyDamageReduction(class_3532.method_15350(damage, 0.0d, 36.0d), player);
    }

    private static float getExposure(class_243 source, class_1657 player, boolean ignoreWalls) {
        if (ignoreWalls) {
            return 1.0f;
        }
        class_238 box = player.method_5829();
        double xDiff = box.field_1320 - box.field_1323;
        double yDiff = box.field_1325 - box.field_1322;
        double zDiff = box.field_1324 - box.field_1321;
        double xStep = 1.0d / ((xDiff * 2.0d) + 1.0d);
        double yStep = 1.0d / ((yDiff * 2.0d) + 1.0d);
        double zStep = 1.0d / ((zDiff * 2.0d) + 1.0d);
        if (xStep <= 0.0d || yStep <= 0.0d || zStep <= 0.0d) {
            return 0.0f;
        }
        int misses = 0;
        int hits = 0;
        double xOffset = (1.0d - (Math.floor(1.0d / xStep) * xStep)) * 0.5d;
        double zOffset = (1.0d - (Math.floor(1.0d / zStep) * zStep)) * 0.5d;
        double xStep2 = xStep * xDiff;
        double yStep2 = yStep * yDiff;
        double zStep2 = zStep * zDiff;
        double startX = box.field_1323 + xOffset;
        double startY = box.field_1322;
        double startZ = box.field_1321 + zOffset;
        double endX = box.field_1320 + xOffset;
        double endY = box.field_1325;
        double endZ = box.field_1324 + zOffset;
        double d = startX;
        while (true) {
            double x = d;
            if (x <= endX) {
                double d2 = startY;
                while (true) {
                    double y = d2;
                    if (y <= endY) {
                        double d3 = startZ;
                        while (true) {
                            double z = d3;
                            if (z <= endZ) {
                                class_243 point = new class_243(x, y, z);
                                if (!isOccluded(source, point)) {
                                    misses++;
                                }
                                hits++;
                                d3 = z + zStep2;
                            }
                        }
                        d2 = y + yStep2;
                    }
                }
                d = x + xStep2;
            } else {
                return misses / hits;
            }
        }
    }

    private static boolean isOccluded(class_243 start, class_243 end) {
        class_2338 startPos = new class_2338((int) start.field_1352, (int) start.field_1351, (int) start.field_1350);
        class_2338 endPos = new class_2338((int) end.field_1352, (int) end.field_1351, (int) end.field_1350);
        if (!startPos.equals(endPos)) {
            class_243 direction = end.method_1020(start).method_1029();
            double distance = start.method_1022(end);
            double d = 0.0d;
            while (true) {
                double d2 = d;
                if (d2 <= distance) {
                    class_243 point = start.method_1019(direction.method_1021(d2));
                    class_2338 pos = new class_2338((int) point.field_1352, (int) point.field_1351, (int) point.field_1350);
                    if (!MeteorClient.mc.field_1687.method_8320(pos).method_26225()) {
                        d = d2 + 0.1d;
                    } else {
                        return true;
                    }
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    private static double applyDamageReduction(double damage, class_1657 player) {
        double armorValue = player.method_6096();
        double reductionFactor = armorValue * (1.0d - (damage / ((damage + 2.0d) + 8.0d)));
        return Math.max(damage - (reductionFactor / 2.0d), damage * 0.2d);
    }
}
