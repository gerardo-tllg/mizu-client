package meteordevelopment.meteorclient.mixin;

import com.mojang.authlib.GameProfile;
import meteordevelopment.meteorclient.mixininterface.IChatHudLine;
import net.minecraft.class_2561;
import net.minecraft.class_303;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/ChatHudLineMixin.class */
@Mixin({class_303.class})
public abstract class ChatHudLineMixin implements IChatHudLine {

    @Shadow
    @Final
    private class_2561 comp_893;

    @Unique
    private int id;

    @Unique
    private GameProfile sender;

    @Override // meteordevelopment.meteorclient.mixininterface.IChatHudLine
    public String meteor$getText() {
        return this.comp_893.getString();
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
}
