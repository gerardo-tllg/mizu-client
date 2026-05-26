package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.concurrent.CompletableFuture;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.player.ItemUseCrosshairTargetEvent;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.game.ResolutionChangedEvent;
import meteordevelopment.meteorclient.events.game.ResourcePacksReloadedEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.mixininterface.IMinecraftClient;
import meteordevelopment.meteorclient.renderer.MeteorRenderPipelines;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.GUIMove;
import meteordevelopment.meteorclient.systems.modules.player.FastUse;
import meteordevelopment.meteorclient.systems.modules.player.Multitask;
import meteordevelopment.meteorclient.systems.modules.render.ESP;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.CPSUtils;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.meteorclient.utils.network.OnlinePlayers;
import meteordevelopment.starscript.Script;
import net.minecraft.class_10209;
import net.minecraft.class_1041;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_239;
import net.minecraft.class_276;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_312;
import net.minecraft.class_315;
import net.minecraft.class_3304;
import net.minecraft.class_437;
import net.minecraft.class_636;
import net.minecraft.class_638;
import net.minecraft.class_746;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/MinecraftClientMixin.class */
@Mixin(value = {class_310.class}, priority = 1001)
public abstract class MinecraftClientMixin implements IMinecraftClient {

    @Unique
    private boolean doItemUseCalled;

    @Unique
    private boolean rightClick;

    @Unique
    private long lastTime;

    @Unique
    private boolean firstFrame;

    @Shadow
    public class_638 field_1687;

    @Shadow
    @Final
    public class_312 field_1729;

    @Shadow
    @Final
    private class_1041 field_1704;

    @Shadow
    public class_437 field_1755;

    @Shadow
    @Final
    public class_315 field_1690;

    @Shadow
    @Nullable
    public class_636 field_1761;

    @Shadow
    private int field_1752;

    @Shadow
    @Nullable
    public class_746 field_1724;

    @Shadow
    @Final
    private class_3304 field_1745;

    @Shadow
    @Mutable
    @Final
    private class_276 field_1689;

    @Shadow
    protected abstract void method_1583();

    @Inject(method = {"<init>"}, at = {@At("TAIL")})
    private void onInit(CallbackInfo info) {
        MeteorClient.INSTANCE.onInitializeClient();
        this.firstFrame = true;
    }

    @Inject(method = {"<init>"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/resource/ReloadableResourceManagerImpl;reload(Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;Ljava/util/List;)Lnet/minecraft/resource/ResourceReload;", shift = At.Shift.BEFORE)})
    private void init$beforeReload(CallbackInfo info) {
        this.field_1745.method_14477(new MeteorRenderPipelines.Reloader());
    }

    @Inject(at = {@At("HEAD")}, method = {"tick"})
    private void onPreTick(CallbackInfo info) {
        OnlinePlayers.update();
        this.doItemUseCalled = false;
        class_10209.method_64146().method_15396("meteor-client_pre_update");
        MeteorClient.EVENT_BUS.post(TickEvent.Pre.get());
        class_10209.method_64146().method_15407();
        if (this.rightClick && !this.doItemUseCalled && this.field_1761 != null) {
            method_1583();
        }
        this.rightClick = false;
    }

    @Inject(at = {@At("TAIL")}, method = {"tick"})
    private void onTick(CallbackInfo info) {
        class_10209.method_64146().method_15396("meteor-client_post_update");
        MeteorClient.EVENT_BUS.post(TickEvent.Post.get());
        class_10209.method_64146().method_15407();
    }

    @Inject(method = {"doAttack"}, at = {@At("HEAD")})
    private void onAttack(CallbackInfoReturnable<Boolean> cir) {
        CPSUtils.onAttack();
    }

    @Inject(method = {"doItemUse"}, at = {@At("HEAD")})
    private void onDoItemUse(CallbackInfo info) {
        this.doItemUseCalled = true;
    }

    @Inject(method = {"disconnect(Lnet/minecraft/client/gui/screen/Screen;Z)V"}, at = {@At("HEAD")})
    private void onDisconnect(class_437 screen, boolean transferring, CallbackInfo info) {
        if (this.field_1687 != null) {
            MeteorClient.EVENT_BUS.post(GameLeftEvent.get());
        }
    }

    @Inject(method = {"setScreen"}, at = {@At("HEAD")}, cancellable = true)
    private void onSetScreen(class_437 screen, CallbackInfo info) {
        if (screen instanceof WidgetScreen) {
            screen.method_16014(this.field_1729.method_1603() * this.field_1704.method_4495(), this.field_1729.method_1604() * this.field_1704.method_4495());
        }
        OpenScreenEvent event = OpenScreenEvent.get(screen);
        MeteorClient.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            info.cancel();
        }
    }

    @WrapOperation(method = {"setScreen"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;unpressAll()V")})
    private void onSetScreenKeyBindingUnpressAll(Operation<Void> op) {
        Modules modules = Modules.get();
        if (modules == null) {
            op.call(new Object[0]);
            return;
        }
        GUIMove guimove = (GUIMove) modules.get(GUIMove.class);
        if (guimove == null || !guimove.isActive() || guimove.skip()) {
            op.call(new Object[0]);
            return;
        }
        class_315 options = MeteorClient.mc.field_1690;
        for (class_304 kb : KeyBindingAccessor.getKeysById().values()) {
            if (kb != options.field_1894 && kb != options.field_1913 && kb != options.field_1849 && kb != options.field_1881 && (!guimove.sneak.get().booleanValue() || kb != options.field_1832)) {
                if (!guimove.sprint.get().booleanValue() || kb != options.field_1867) {
                    if (!guimove.jump.get().booleanValue() || kb != options.field_1903) {
                        ((KeyBindingAccessor) kb).invokeReset();
                    }
                }
            }
        }
    }

    @Inject(method = {"doItemUse"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isItemEnabled(Lnet/minecraft/resource/featuretoggle/FeatureSet;)Z")})
    private void onDoItemUseHand(CallbackInfo ci, @Local class_1799 itemStack) {
        FastUse fastUse = (FastUse) Modules.get().get(FastUse.class);
        if (fastUse.isActive()) {
            this.field_1752 = fastUse.getItemUseCooldown(itemStack);
        }
    }

    @ModifyExpressionValue(method = {"doItemUse"}, at = {@At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;crosshairTarget:Lnet/minecraft/util/hit/HitResult;", ordinal = 1)})
    private class_239 doItemUseMinecraftClientCrosshairTargetProxy(class_239 original) {
        return ((ItemUseCrosshairTargetEvent) MeteorClient.EVENT_BUS.post(ItemUseCrosshairTargetEvent.get(original))).target;
    }

    @ModifyReturnValue(method = {"method_36561"}, at = {@At("RETURN")})
    private CompletableFuture<Void> onReloadResourcesNewCompletableFuture(CompletableFuture<Void> original) {
        return original.thenRun(() -> {
            MeteorClient.EVENT_BUS.post(ResourcePacksReloadedEvent.get());
        });
    }

    @ModifyArg(method = {"updateWindowTitle"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Window;setTitle(Ljava/lang/String;)V"))
    private String setTitle(String original) {
        String title;
        if (Config.get() == null || !Config.get().customWindowTitle.get().booleanValue()) {
            return original;
        }
        String customTitle = Config.get().customWindowTitleText.get();
        Script script = MeteorStarscript.compile(customTitle);
        if (script != null && (title = MeteorStarscript.run(script)) != null) {
            customTitle = title;
        }
        return customTitle;
    }

    @WrapWithCondition(method = {"handleInputEvents"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;stopUsingItem(Lnet/minecraft/entity/player/PlayerEntity;)V")})
    private boolean wrapStopUsing(class_636 instance, class_1657 player) {
        return true;
    }

    @Inject(method = {"onResolutionChanged"}, at = {@At("TAIL")})
    private void onResolutionChanged(CallbackInfo info) {
        MeteorClient.EVENT_BUS.post(ResolutionChangedEvent.get());
    }

    @Inject(method = {"render"}, at = {@At("HEAD")})
    private void onRender(CallbackInfo info) {
        long time = System.currentTimeMillis();
        if (this.firstFrame) {
            this.lastTime = time;
            this.firstFrame = false;
        }
        Utils.frameTime = (time - this.lastTime) / 1000.0d;
        this.lastTime = time;
    }

    @ModifyExpressionValue(method = {"doItemUse"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;isBreakingBlock()Z")})
    private boolean doItemUseModifyIsBreakingBlock(boolean original) {
        return !Modules.get().isActive(Multitask.class) && original;
    }

    @ModifyExpressionValue(method = {"handleBlockBreaking"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z")})
    private boolean handleBlockBreakingModifyIsUsingItem(boolean original) {
        return !Modules.get().isActive(Multitask.class) && original;
    }

    @ModifyExpressionValue(method = {"handleInputEvents"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z", ordinal = 0)})
    private boolean handleInputEventsModifyIsUsingItem(boolean original) {
        return !((Multitask) Modules.get().get(Multitask.class)).attackingEntities() && original;
    }

    @Inject(method = {"handleInputEvents"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z", ordinal = 0, shift = At.Shift.BEFORE)})
    private void handleInputEventsInjectStopUsingItem(CallbackInfo info) {
        if (((Multitask) Modules.get().get(Multitask.class)).attackingEntities() && this.field_1724.method_6115()) {
            if (!this.field_1690.field_1904.method_1434()) {
                this.field_1761.method_2897(this.field_1724);
            }
            while (this.field_1690.field_1904.method_1436()) {
            }
        }
    }

    @ModifyReturnValue(method = {"hasOutline"}, at = {@At("RETURN")})
    private boolean hasOutlineModifyIsOutline(boolean original, class_1297 entity) {
        ESP esp = (ESP) Modules.get().get(ESP.class);
        return esp == null ? original : (!esp.isGlow() || esp.shouldSkip(entity)) ? original : esp.getColor(entity) != null || original;
    }

    @Override // meteordevelopment.meteorclient.mixininterface.IMinecraftClient
    public void meteor$rightClick() {
        this.rightClick = true;
    }

    @Override // meteordevelopment.meteorclient.mixininterface.IMinecraftClient
    public void meteor$setFramebuffer(class_276 framebuffer) {
        this.field_1689 = framebuffer;
    }
}
