package meteordevelopment.meteorclient.mixin;

import com.mojang.authlib.GameProfile;
import meteordevelopment.meteorclient.mixininterface.IChatHudLineVisible;
import net.minecraft.class_303;
import net.minecraft.class_5481;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/ChatHudLineVisibleMixin.class */
@Mixin({class_303.class_7590.class})
public abstract class ChatHudLineVisibleMixin implements IChatHudLineVisible {

    @Shadow
    @Final
    private class_5481 comp_896;

    @Unique
    private int id;

    @Unique
    private GameProfile sender;

    @Unique
    private boolean startOfEntry;

    @Override // meteordevelopment.meteorclient.mixininterface.IChatHudLine
    public String meteor$getText() {
        StringBuilder sb = new StringBuilder();
        this.comp_896.accept((index, style, codePoint) -> {
            sb.appendCodePoint(codePoint);
            return true;
        });
        return sb.toString();
    }

    @Override // meteordevelopment.meteorclient.mixininterface.IChatHudLine
    public int meteor$getId() {
        return this.id;
    }

    @Override // meteordevelopment.meteorclient.mixininterface.IChatHudLine
    public void meteor$setId(int id) {
        this.id = id;
    }

    @Override // meteordevelopment.meteorclient.mixininterface.IChatHudLine
    public GameProfile meteor$getSender() {
        return this.sender;
    }

    @Override // meteordevelopment.meteorclient.mixininterface.IChatHudLine
    public void meteor$setSender(GameProfile profile) {
        this.sender = profile;
    }

    @Override // meteordevelopment.meteorclient.mixininterface.IChatHudLineVisible
    public boolean meteor$isStartOfEntry() {
        return this.startOfEntry;
    }

    @Override // meteordevelopment.meteorclient.mixininterface.IChatHudLineVisible
    public void meteor$setStartOfEntry(boolean start) {
        this.startOfEntry = start;
    }
}
