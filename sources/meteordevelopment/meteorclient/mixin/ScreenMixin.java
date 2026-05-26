package meteordevelopment.meteorclient.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import javassist.compiler.TokenId;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.GUIMove;
import meteordevelopment.meteorclient.systems.modules.render.NoRender;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.text.MeteorClickEvent;
import meteordevelopment.meteorclient.utils.misc.text.RunnableClickEvent;
import net.minecraft.class_2558;
import net.minecraft.class_2583;
import net.minecraft.class_408;
import net.minecraft.class_437;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/ScreenMixin.class */
@Mixin(value = {class_437.class}, priority = TokenId.BadToken)
public abstract class ScreenMixin {
    @Inject(method = {"renderInGameBackground"}, at = {@At("HEAD")}, cancellable = true)
    private void onRenderInGameBackground(CallbackInfo info) {
        if (Utils.canUpdate() && ((NoRender) Modules.get().get(NoRender.class)).noGuiBackground()) {
            info.cancel();
        }
    }

    @Inject(method = {"handleTextClick"}, at = {@At("HEAD")}, cancellable = true)
    private void onInvalidClickEvent(@Nullable class_2583 style, CallbackInfoReturnable<Boolean> cir) {
        if (style != null) {
            class_2558 class_2558VarMethod_10970 = style.method_10970();
            if (class_2558VarMethod_10970 instanceof RunnableClickEvent) {
                RunnableClickEvent runnableClickEvent = (RunnableClickEvent) class_2558VarMethod_10970;
                runnableClickEvent.runnable.run();
                cir.setReturnValue(true);
            }
        }
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: com.mojang.brigadier.exceptions.CommandSyntaxException */
    @Inject(method = {"handleTextClick"}, at = {@At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;)V", ordinal = 1, remap = false)}, cancellable = true)
    private void onRunCommand(class_2583 style, CallbackInfoReturnable<Boolean> cir) {
        class_2558 class_2558VarMethod_10970 = style.method_10970();
        if (class_2558VarMethod_10970 instanceof MeteorClickEvent) {
            MeteorClickEvent clickEvent = (MeteorClickEvent) class_2558VarMethod_10970;
            if (clickEvent.value.startsWith(Config.get().prefix.get())) {
                try {
                    Commands.dispatch(clickEvent.value.substring(Config.get().prefix.get().length()));
                    cir.setReturnValue(true);
                } catch (CommandSyntaxException e) {
                    MeteorClient.LOG.error("Failed to run command", e);
                }
            }
        }
    }

    @Inject(method = {"keyPressed"}, at = {@At("HEAD")}, cancellable = true)
    private void onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (this instanceof class_408) {
            return;
        }
        GUIMove guiMove = (GUIMove) Modules.get().get(GUIMove.class);
        List<Integer> arrows = List.of(262, 263, 264, 265);
        if ((guiMove.disableArrows() && arrows.contains(Integer.valueOf(keyCode))) || (guiMove.disableSpace() && keyCode == 32)) {
            cir.setReturnValue(true);
        }
    }
}
