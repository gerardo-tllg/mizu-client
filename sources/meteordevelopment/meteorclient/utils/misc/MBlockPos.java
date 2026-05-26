package meteordevelopment.meteorclient.utils.misc;

import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.class_1297;
import net.minecraft.class_2338;
import net.minecraft.class_2680;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/misc/MBlockPos.class */
public class MBlockPos {
    private static final class_2338.class_2339 POS = new class_2338.class_2339();
    public int x;
    public int y;
    public int z;

    public MBlockPos() {
    }

    public MBlockPos(class_1297 entity) {
        set(entity);
    }

    public MBlockPos set(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public MBlockPos set(MBlockPos pos) {
        return set(pos.x, pos.y, pos.z);
    }

    public MBlockPos set(class_1297 entity) {
        return set(entity.method_31477(), entity.method_31478(), entity.method_31479());
    }

    public MBlockPos coerceBlockLevel(class_1297 entity) {
        return set(entity.method_31477(), (int) Math.round(entity.method_23318()), entity.method_31479());
    }

    public MBlockPos offset(HorizontalDirection dir, int amount) {
        this.x += dir.offsetX * amount;
        this.z += dir.offsetZ * amount;
        return this;
    }

    public MBlockPos offset(HorizontalDirection dir) {
        return offset(dir, 1);
    }

    public MBlockPos add(int x, int y, int z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public class_2338 getBlockPos() {
        return POS.method_10103(this.x, this.y, this.z);
    }

    public class_2680 getState() {
        return MeteorClient.mc.field_1687.method_8320(getBlockPos());
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MBlockPos mBlockPos = (MBlockPos) o;
        return this.x == mBlockPos.x && this.y == mBlockPos.y && this.z == mBlockPos.z;
    }

    public int hashCode() {
        int result = this.x;
        return (31 * ((31 * result) + this.y)) + this.z;
    }

    public String toString() {
        return this.x + ", " + this.y + ", " + this.z;
    }
}
