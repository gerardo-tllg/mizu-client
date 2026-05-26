package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.mixininterface.IPlayerMoveC2SPacket;
import net.minecraft.class_2828;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/PlayerMoveC2SPacketMixin.class */
@Mixin({class_2828.class})
public abstract class PlayerMoveC2SPacketMixin implements IPlayerMoveC2SPacket {

    @Unique
    private int tag;

    @Override // meteordevelopment.meteorclient.mixininterface.IPlayerMoveC2SPacket
    public void meteor$setTag(int tag) {
        this.tag = tag;
    }

    @Override // meteordevelopment.meteorclient.mixininterface.IPlayerMoveC2SPacket
    public int meteor$getTag() {
        return this.tag;
    }
}
