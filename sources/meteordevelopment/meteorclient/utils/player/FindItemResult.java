package meteordevelopment.meteorclient.utils.player;

import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.class_1268;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/player/FindItemResult.class */
public record FindItemResult(int slot, int count) {

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
