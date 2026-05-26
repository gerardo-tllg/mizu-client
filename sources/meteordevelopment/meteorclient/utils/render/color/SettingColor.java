package meteordevelopment.meteorclient.utils.render.color;

import net.minecraft.class_124;
import net.minecraft.class_2487;
import net.minecraft.class_2583;
import net.minecraft.class_5251;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/render/color/SettingColor.class */
public class SettingColor extends Color {
    public boolean rainbow;

    public SettingColor() {
    }

    public SettingColor(int packed) {
        super(packed);
    }

    public SettingColor(int r, int g, int b) {
        super(r, g, b);
    }

    public SettingColor(int r, int g, int b, boolean rainbow) {
        this(r, g, b, 255, rainbow);
    }

    public SettingColor(int r, int g, int b, int a) {
        super(r, g, b, a);
    }

    public SettingColor(float r, float g, float b, float a) {
        super(r, g, b, a);
    }

    public SettingColor(int r, int g, int b, int a, boolean rainbow) {
        super(r, g, b, a);
        this.rainbow = rainbow;
    }

    public SettingColor(SettingColor color) {
        super(color);
        this.rainbow = color.rainbow;
    }

    public SettingColor(java.awt.Color color) {
        super(color);
    }

    public SettingColor(class_124 formatting) {
        super(formatting);
    }

    public SettingColor(class_5251 textColor) {
        super(textColor);
    }

    public SettingColor(class_2583 style) {
        super(style);
    }

    public SettingColor rainbow(boolean rainbow) {
        this.rainbow = rainbow;
        return this;
    }

    public void update() {
        if (this.rainbow) {
            set(RainbowColors.GLOBAL.r, RainbowColors.GLOBAL.g, RainbowColors.GLOBAL.b, this.a);
        }
    }

    @Override // meteordevelopment.meteorclient.utils.render.color.Color, meteordevelopment.meteorclient.utils.misc.ICopyable
    public SettingColor set(Color value) {
        super.set(value);
        if (value instanceof SettingColor) {
            this.rainbow = ((SettingColor) value).rainbow;
        }
        return this;
    }

    @Override // meteordevelopment.meteorclient.utils.render.color.Color, meteordevelopment.meteorclient.utils.misc.ICopyable
    public Color copy() {
        return new SettingColor(this.r, this.g, this.b, this.a, this.rainbow);
    }

    @Override // meteordevelopment.meteorclient.utils.render.color.Color, meteordevelopment.meteorclient.utils.misc.ISerializable
    public class_2487 toTag() {
        class_2487 tag = super.toTag();
        tag.method_10556("rainbow", this.rainbow);
        return tag;
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // meteordevelopment.meteorclient.utils.render.color.Color, meteordevelopment.meteorclient.utils.misc.ISerializable
    /* JADX INFO: renamed from: fromTag, reason: merged with bridge method [inline-methods] */
    public Color fromTag2(class_2487 tag) {
        super.fromTag2(tag);
        this.rainbow = tag.method_68566("rainbow", false);
        return this;
    }

    @Override // meteordevelopment.meteorclient.utils.render.color.Color
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o != null && getClass() == o.getClass() && super.equals(o) && this.rainbow == ((SettingColor) o).rainbow;
    }

    @Override // meteordevelopment.meteorclient.utils.render.color.Color
    public int hashCode() {
        int result = super.hashCode();
        return (31 * result) + (this.rainbow ? 1 : 0);
    }
}
