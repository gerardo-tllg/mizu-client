package meteordevelopment.meteorclient.mixin;

import java.util.Objects;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.systems.proxies.Proxies;
import meteordevelopment.meteorclient.systems.proxies.Proxy;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_500;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/MultiplayerScreenMixin.class */
@Mixin({class_500.class})
public abstract class MultiplayerScreenMixin extends class_437 {

    @Unique
    private int textColor1;

    @Unique
    private int textColor2;

    @Unique
    private String loggedInAs;

    @Unique
    private int loggedInAsLength;

    public MultiplayerScreenMixin(class_2561 title) {
        super(title);
    }

    @Inject(method = {"init"}, at = {@At("TAIL")})
    private void onInit(CallbackInfo info) {
        this.textColor1 = Color.fromRGBA(255, 255, 255, 255);
        this.textColor2 = Color.fromRGBA(Opcode.DRETURN, Opcode.DRETURN, Opcode.DRETURN, 255);
        this.loggedInAs = "Logged in as ";
        this.loggedInAsLength = this.field_22793.method_1727(this.loggedInAs);
        method_37063(new class_4185.class_7840(class_2561.method_43470("Accounts"), button -> {
            this.field_22787.method_1507(GuiThemes.get().accountsScreen());
        }).method_46433((this.field_22789 - 75) - 3, 3).method_46437(75, 20).method_46431());
        method_37063(new class_4185.class_7840(class_2561.method_43470("Proxies"), button2 -> {
            this.field_22787.method_1507(GuiThemes.get().proxiesScreen());
        }).method_46433((((this.field_22789 - 75) - 3) - 75) - 2, 3).method_46437(75, 20).method_46431());
    }

    @Inject(method = {"render"}, at = {@At("TAIL")})
    private void onRender(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        String str;
        context.method_25303(MeteorClient.mc.field_1772, this.loggedInAs, 3, 3, this.textColor1);
        Objects.requireNonNull(this.field_22793);
        int y = 3 + 9 + 2;
        Proxy proxy = Proxies.get().getEnabled();
        String left = proxy != null ? "Using proxy " : "Not using a proxy";
        if (proxy != null) {
            str = ((proxy.name.get() == null || proxy.name.get().isEmpty()) ? "" : "(" + proxy.name.get() + ") ") + proxy.address.get() + ":" + String.valueOf(proxy.port.get());
        } else {
            str = null;
        }
        String right = str;
        context.method_25303(MeteorClient.mc.field_1772, left, 3, y, this.textColor1);
        if (right != null) {
            context.method_25303(MeteorClient.mc.field_1772, right, 3 + this.field_22793.method_1727(left), y, this.textColor2);
        }
    }
}
