package meteordevelopment.meteorclient.mixin;

import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.class_2487;
import net.minecraft.class_2499;
import net.minecraft.class_2505;
import net.minecraft.class_2507;
import net.minecraft.class_2519;
import net.minecraft.class_2561;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_473;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/BookEditScreenMixin.class */
@Mixin({class_473.class})
public abstract class BookEditScreenMixin extends class_437 {

    @Shadow
    @Final
    private List<String> field_17116;

    @Shadow
    private int field_2840;

    @Shadow
    private boolean field_2837;

    @Shadow
    protected abstract void method_2413();

    public BookEditScreenMixin(class_2561 title) {
        super(title);
    }

    @Inject(method = {"init"}, at = {@At("TAIL")})
    private void onInit(CallbackInfo info) {
        method_37063(new class_4185.class_7840(class_2561.method_43470("Copy"), button -> {
            class_2499 listTag = new class_2499();
            Stream<R> map = this.field_17116.stream().map(class_2519::method_23256);
            Objects.requireNonNull(listTag);
            map.forEach((v1) -> {
                r1.add(v1);
            });
            class_2487 tag = new class_2487();
            tag.method_10566("pages", listTag);
            tag.method_10569("currentPage", this.field_2840);
            FastByteArrayOutputStream bytes = new FastByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bytes);
            try {
                class_2507.method_55324(tag, out);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                GLFW.glfwSetClipboardString(MeteorClient.mc.method_22683().method_4490(), Base64.getEncoder().encodeToString(bytes.array));
            } catch (OutOfMemoryError exception) {
                GLFW.glfwSetClipboardString(MeteorClient.mc.method_22683().method_4490(), exception.toString());
            }
        }).method_46433(4, 4).method_46437(Opcode.ISHL, 20).method_46431());
        method_37063(new class_4185.class_7840(class_2561.method_43470("Paste"), button2 -> {
            String clipboard = GLFW.glfwGetClipboardString(MeteorClient.mc.method_22683().method_4490());
            if (clipboard == null) {
                return;
            }
            try {
                byte[] bytes = Base64.getDecoder().decode(clipboard);
                DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));
                try {
                    class_2487 tag = class_2507.method_10629(in, class_2505.method_53898());
                    class_2499 listTag = tag.method_68569("pages").method_10612();
                    this.field_17116.clear();
                    for (int i = 0; i < listTag.size(); i++) {
                        this.field_17116.add(listTag.method_68577(i, ""));
                    }
                    if (this.field_17116.isEmpty()) {
                        this.field_17116.add("");
                    }
                    this.field_2840 = tag.method_68083("currentPage", 0);
                    this.field_2837 = true;
                    method_2413();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IllegalArgumentException e2) {
            }
        }).method_46433(4, 26).method_46437(Opcode.ISHL, 20).method_46431());
    }
}
