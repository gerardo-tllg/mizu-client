package meteordevelopment.meteorclient.gui.utils;

import meteordevelopment.meteorclient.utils.misc.ISerializable;
import net.minecraft.class_2487;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/utils/WindowConfig.class */
public class WindowConfig implements ISerializable<WindowConfig> {
    public boolean expanded = true;
    public double x = -1.0d;
    public double y = -1.0d;

    @Override // meteordevelopment.meteorclient.utils.misc.ISerializable
    public class_2487 toTag() {
        class_2487 tag = new class_2487();
        tag.method_10556("expanded", this.expanded);
        tag.method_10549("x", this.x);
        tag.method_10549("y", this.y);
        return tag;
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // meteordevelopment.meteorclient.utils.misc.ISerializable
    public WindowConfig fromTag(class_2487 tag) {
        tag.method_10577("expanded").ifPresent(bool -> {
            this.expanded = bool.booleanValue();
        });
        tag.method_10574("x").ifPresent(x1 -> {
            this.x = x1.doubleValue();
        });
        tag.method_10574("y").ifPresent(y1 -> {
            this.y = y1.doubleValue();
        });
        return this;
    }
}
