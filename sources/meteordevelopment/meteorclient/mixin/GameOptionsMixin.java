package meteordevelopment.meteorclient.mixin;

import java.io.File;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.ChangePerspectiveEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import meteordevelopment.meteorclient.utils.misc.input.KeyBinds;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_315;
import net.minecraft.class_5498;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/GameOptionsMixin.class */
@Mixin({class_315.class})
public abstract class GameOptionsMixin {

    @Shadow
    @Mutable
    @Final
    public class_304[] field_1839;

    @Inject(method = {"<init>"}, at = {@At(value = "FIELD", target = "Lnet/minecraft/client/option/GameOptions;allKeys:[Lnet/minecraft/client/option/KeyBinding;", opcode = Opcode.PUTFIELD, shift = At.Shift.AFTER)})
    private void onInitAfterKeysAll(class_310 client, File optionsFile, CallbackInfo info) {
        this.field_1839 = KeyBinds.apply(this.field_1839);
    }

    @Inject(method = {"setPerspective"}, at = {@At("HEAD")}, cancellable = true)
    private void setPerspective(class_5498 perspective, CallbackInfo info) {
        if (Modules.get() == null) {
            return;
        }
        ChangePerspectiveEvent event = (ChangePerspectiveEvent) MeteorClient.EVENT_BUS.post(ChangePerspectiveEvent.get(perspective));
        if (event.isCancelled()) {
            info.cancel();
        }
        if (Modules.get().isActive(Freecam.class)) {
            info.cancel();
        }
    }
}
