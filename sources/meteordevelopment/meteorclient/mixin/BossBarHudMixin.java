package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.Iterator;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.RenderBossBarEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.NoRender;
import net.minecraft.class_2561;
import net.minecraft.class_337;
import net.minecraft.class_345;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/BossBarHudMixin.class */
@Mixin({class_337.class})
public abstract class BossBarHudMixin {
    @Inject(method = {"render"}, at = {@At("HEAD")}, cancellable = true)
    private void onRender(CallbackInfo info) {
        if (((NoRender) Modules.get().get(NoRender.class)).noBossBar()) {
            info.cancel();
        }
    }

    @ModifyExpressionValue(method = {"render"}, at = {@At(value = "INVOKE", target = "Ljava/util/Collection;iterator()Ljava/util/Iterator;")})
    public Iterator<class_345> modifyBossBarIterator(Iterator<class_345> original) {
        RenderBossBarEvent.BossIterator event = (RenderBossBarEvent.BossIterator) MeteorClient.EVENT_BUS.post(RenderBossBarEvent.BossIterator.get(original));
        return event.iterator;
    }

    @ModifyExpressionValue(method = {"render"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ClientBossBar;getName()Lnet/minecraft/text/Text;")})
    public class_2561 modifyBossBarName(class_2561 original, @Local class_345 clientBossBar) {
        RenderBossBarEvent.BossText event = (RenderBossBarEvent.BossText) MeteorClient.EVENT_BUS.post(RenderBossBarEvent.BossText.get(clientBossBar, original));
        return event.name;
    }

    @ModifyConstant(method = {"render"}, constant = {@Constant(intValue = 9, ordinal = 1)})
    public int modifySpacingConstant(int j) {
        RenderBossBarEvent.BossSpacing event = (RenderBossBarEvent.BossSpacing) MeteorClient.EVENT_BUS.post(RenderBossBarEvent.BossSpacing.get(j));
        return event.spacing;
    }
}
