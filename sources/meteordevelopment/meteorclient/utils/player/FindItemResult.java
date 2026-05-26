package meteordevelopment.meteorclient.utils.player;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.ObjectMethods;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.class_1268;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/player/FindItemResult.class */
public final class FindItemResult extends Record {
    private final int slot;
    private final int count;

    public FindItemResult(int slot, int count) {
        this.slot = slot;
        this.count = count;
    }

    @Override // java.lang.Record
    public final String toString() {
        return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, FindItemResult.class), FindItemResult.class, "slot;count", "FIELD:Lmeteordevelopment/meteorclient/utils/player/FindItemResult;->slot:I", "FIELD:Lmeteordevelopment/meteorclient/utils/player/FindItemResult;->count:I").dynamicInvoker().invoke(this) /* invoke-custom */;
    }

    @Override // java.lang.Record
    public final int hashCode() {
        return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, FindItemResult.class), FindItemResult.class, "slot;count", "FIELD:Lmeteordevelopment/meteorclient/utils/player/FindItemResult;->slot:I", "FIELD:Lmeteordevelopment/meteorclient/utils/player/FindItemResult;->count:I").dynamicInvoker().invoke(this) /* invoke-custom */;
    }

    @Override // java.lang.Record
    public final boolean equals(Object o) {
        return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, FindItemResult.class, Object.class), FindItemResult.class, "slot;count", "FIELD:Lmeteordevelopment/meteorclient/utils/player/FindItemResult;->slot:I", "FIELD:Lmeteordevelopment/meteorclient/utils/player/FindItemResult;->count:I").dynamicInvoker().invoke(this, o) /* invoke-custom */;
    }

    public int slot() {
        return this.slot;
    }

    public int count() {
        return this.count;
    }

    public boolean found() {
        return this.slot != -1;
    }

    public class_1268 getHand() {
        if (this.slot == 45) {
            return class_1268.field_5810;
        }
        if (this.slot == MeteorClient.mc.field_1724.method_31548().method_67532()) {
            return class_1268.field_5808;
        }
        return null;
    }

    public boolean isMainHand() {
        return getHand() == class_1268.field_5808;
    }

    public boolean isOffhand() {
        return getHand() == class_1268.field_5810;
    }

    public boolean isHotbar() {
        return this.slot >= 0 && this.slot <= 8;
    }

    public boolean isMain() {
        return this.slot >= 9 && this.slot <= 35;
    }

    public boolean isArmor() {
        return this.slot >= 36 && this.slot <= 39;
    }
}
