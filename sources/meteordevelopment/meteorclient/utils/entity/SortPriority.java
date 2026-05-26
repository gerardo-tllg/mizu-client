package meteordevelopment.meteorclient.utils.entity;

import java.util.Comparator;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import net.minecraft.class_1297;
import net.minecraft.class_1309;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/entity/SortPriority.class */
public enum SortPriority implements Comparator<class_1297> {
    LowestDistance(Comparator.comparingDouble(PlayerUtils::squaredDistanceTo)),
    HighestDistance((e1, e2) -> {
        return Double.compare(PlayerUtils.squaredDistanceTo(e2), PlayerUtils.squaredDistanceTo(e1));
    }),
    LowestHealth(SortPriority::sortHealth),
    HighestHealth((e12, e22) -> {
        return sortHealth(e22, e12);
    }),
    ClosestAngle(SortPriority::sortAngle);

    private final Comparator<class_1297> comparator;

    SortPriority(Comparator comparator) {
        this.comparator = comparator;
    }

    @Override // java.util.Comparator
    public int compare(class_1297 o1, class_1297 o2) {
        return this.comparator.compare(o1, o2);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static int sortHealth(class_1297 e1, class_1297 e2) {
        boolean e1l = e1 instanceof class_1309;
        boolean e2l = e2 instanceof class_1309;
        if (!e1l && !e2l) {
            return 0;
        }
        if (e1l && !e2l) {
            return 1;
        }
        if (e1l) {
            return Float.compare(((class_1309) e1).method_6032(), ((class_1309) e2).method_6032());
        }
        return -1;
    }

    private static int sortAngle(class_1297 e1, class_1297 e2) {
        boolean e1l = e1 instanceof class_1309;
        boolean e2l = e2 instanceof class_1309;
        if (!e1l && !e2l) {
            return 0;
        }
        if (e1l && !e2l) {
            return 1;
        }
        if (!e1l) {
            return -1;
        }
        double e1yaw = Math.abs(Rotations.getYaw(e1) - ((double) MeteorClient.mc.field_1724.method_36454()));
        double e2yaw = Math.abs(Rotations.getYaw(e2) - ((double) MeteorClient.mc.field_1724.method_36454()));
        double e1pitch = Math.abs(Rotations.getPitch(e1) - ((double) MeteorClient.mc.field_1724.method_36455()));
        double e2pitch = Math.abs(Rotations.getPitch(e2) - ((double) MeteorClient.mc.field_1724.method_36455()));
        return Double.compare((e1yaw * e1yaw) + (e1pitch * e1pitch), (e2yaw * e2yaw) + (e2pitch * e2pitch));
    }
}
