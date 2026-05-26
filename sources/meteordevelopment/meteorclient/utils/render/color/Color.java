package meteordevelopment.meteorclient.utils.render.color;

import meteordevelopment.meteorclient.utils.misc.ICopyable;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import net.minecraft.class_124;
import net.minecraft.class_243;
import net.minecraft.class_2487;
import net.minecraft.class_2583;
import net.minecraft.class_5251;
import org.joml.Vector3f;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/render/color/Color.class */
public class Color implements ICopyable<Color>, ISerializable<Color> {
    public static final Color CLEAR = new Color(0, 0, 0, 0);
    public static final Color WHITE = new Color(java.awt.Color.WHITE);
    public static final Color LIGHT_GRAY = new Color(java.awt.Color.LIGHT_GRAY);
    public static final Color GRAY = new Color(java.awt.Color.GRAY);
    public static final Color DARK_GRAY = new Color(java.awt.Color.DARK_GRAY);
    public static final Color BLACK = new Color(java.awt.Color.BLACK);
    public static final Color RED = new Color(java.awt.Color.RED);
    public static final Color PINK = new Color(java.awt.Color.PINK);
    public static final Color ORANGE = new Color(java.awt.Color.ORANGE);
    public static final Color YELLOW = new Color(java.awt.Color.YELLOW);
    public static final Color GREEN = new Color(java.awt.Color.GREEN);
    public static final Color MAGENTA = new Color(java.awt.Color.MAGENTA);
    public static final Color CYAN = new Color(java.awt.Color.CYAN);
    public static final Color BLUE = new Color(java.awt.Color.BLUE);
    public int r;
    public int g;
    public int b;
    public int a;

    public Color() {
        this(255, 255, 255, 255);
    }

    public Color(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = 255;
        validate();
    }

    public Color(int r, int g, int b, int a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        validate();
    }

    public Color(float r, float g, float b, float a) {
        this.r = (int) (r * 255.0f);
        this.g = (int) (g * 255.0f);
        this.b = (int) (b * 255.0f);
        this.a = (int) (a * 255.0f);
        validate();
    }

    public Color(int packed) {
        this.r = toRGBAR(packed);
        this.g = toRGBAG(packed);
        this.b = toRGBAB(packed);
        this.a = toRGBAA(packed);
    }

    public Color(Color color) {
        this.r = color.r;
        this.g = color.g;
        this.b = color.b;
        this.a = color.a;
    }

    public Color(java.awt.Color color) {
        this.r = color.getRed();
        this.g = color.getGreen();
        this.b = color.getBlue();
        this.a = color.getAlpha();
    }

    public Color(class_124 formatting) {
        if (formatting.method_543()) {
            this.r = toRGBAR(formatting.method_532().intValue());
            this.g = toRGBAG(formatting.method_532().intValue());
            this.b = toRGBAB(formatting.method_532().intValue());
            this.a = toRGBAA(formatting.method_532().intValue());
            return;
        }
        this.r = 255;
        this.g = 255;
        this.b = 255;
        this.a = 255;
    }

    public Color(class_5251 textColor) {
        this.r = toRGBAR(textColor.method_27716());
        this.g = toRGBAG(textColor.method_27716());
        this.b = toRGBAB(textColor.method_27716());
        this.a = toRGBAA(textColor.method_27716());
    }

    public Color(class_2583 style) {
        class_5251 textColor = style.method_10973();
        if (textColor == null) {
            this.r = 255;
            this.g = 255;
            this.b = 255;
            this.a = 255;
            return;
        }
        this.r = toRGBAR(textColor.method_27716());
        this.g = toRGBAG(textColor.method_27716());
        this.b = toRGBAB(textColor.method_27716());
        this.a = toRGBAA(textColor.method_27716());
    }

    public static int fromRGBA(int r, int g, int b, int a) {
        return (r << 16) + (g << 8) + b + (a << 24);
    }

    public static int toRGBAR(int color) {
        return (color >> 16) & 255;
    }

    public static int toRGBAG(int color) {
        return (color >> 8) & 255;
    }

    public static int toRGBAB(int color) {
        return color & 255;
    }

    public static int toRGBAA(int color) {
        return (color >> 24) & 255;
    }

    public static Color fromHsv(double h, double s, double v) {
        double r;
        double g;
        double b;
        if (s <= 0.0d) {
            return new Color((int) (v * 255.0d), (int) (v * 255.0d), (int) (v * 255.0d), 255);
        }
        double hh = h;
        if (hh >= 360.0d) {
            hh = 0.0d;
        }
        double hh2 = hh / 60.0d;
        int i = (int) hh2;
        double ff = hh2 - ((double) i);
        double p = v * (1.0d - s);
        double q = v * (1.0d - (s * ff));
        double t = v * (1.0d - (s * (1.0d - ff)));
        switch (i) {
            case 0:
                r = v;
                g = t;
                b = p;
                break;
            case 1:
                r = q;
                g = v;
                b = p;
                break;
            case 2:
                r = p;
                g = v;
                b = t;
                break;
            case 3:
                r = p;
                g = q;
                b = v;
                break;
            case 4:
                r = t;
                g = p;
                b = v;
                break;
            case 5:
            default:
                r = v;
                g = p;
                b = q;
                break;
        }
        return new Color((int) (r * 255.0d), (int) (g * 255.0d), (int) (b * 255.0d), 255);
    }

    public Color set(int r, int g, int b, int a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        validate();
        return this;
    }

    public Color r(int r) {
        this.r = r;
        validate();
        return this;
    }

    public Color g(int g) {
        this.g = g;
        validate();
        return this;
    }

    public Color b(int b) {
        this.b = b;
        validate();
        return this;
    }

    public Color a(int a) {
        this.a = a;
        validate();
        return this;
    }

    @Override // meteordevelopment.meteorclient.utils.misc.ICopyable
    public Color set(Color value) {
        this.r = value.r;
        this.g = value.g;
        this.b = value.b;
        this.a = value.a;
        validate();
        return this;
    }

    public boolean parse(String text) {
        String[] split = text.split(",");
        if (split.length != 3 && split.length != 4) {
            return false;
        }
        try {
            int r = Integer.parseInt(split[0]);
            int g = Integer.parseInt(split[1]);
            int b = Integer.parseInt(split[2]);
            int a = split.length == 4 ? Integer.parseInt(split[3]) : this.a;
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override // meteordevelopment.meteorclient.utils.misc.ICopyable
    public Color copy() {
        return new Color(this.r, this.g, this.b, this.a);
    }

    public SettingColor toSetting() {
        return new SettingColor(this.r, this.g, this.b, this.a);
    }

    public class_5251 toTextColor() {
        return class_5251.method_27717(getPacked());
    }

    public class_2583 toStyle() {
        return class_2583.field_24360.method_27703(toTextColor());
    }

    public class_2583 styleWith(class_2583 style) {
        return style.method_27703(toTextColor());
    }

    public void validate() {
        if (this.r < 0) {
            this.r = 0;
        } else if (this.r > 255) {
            this.r = 255;
        }
        if (this.g < 0) {
            this.g = 0;
        } else if (this.g > 255) {
            this.g = 255;
        }
        if (this.b < 0) {
            this.b = 0;
        } else if (this.b > 255) {
            this.b = 255;
        }
        if (this.a >= 0) {
            if (this.a > 255) {
                this.a = 255;
                return;
            }
            return;
        }
        this.a = 0;
    }

    public class_243 getVec3d() {
        return new class_243(((double) this.r) / 255.0d, ((double) this.g) / 255.0d, ((double) this.b) / 255.0d);
    }

    public Vector3f getVec3f() {
        return new Vector3f(this.r / 255.0f, this.g / 255.0f, this.b / 255.0f);
    }

    public int getPacked() {
        return fromRGBA(this.r, this.g, this.b, this.a);
    }

    @Override // meteordevelopment.meteorclient.utils.misc.ISerializable
    public class_2487 toTag() {
        class_2487 tag = new class_2487();
        tag.method_10569("r", this.r);
        tag.method_10569("g", this.g);
        tag.method_10569("b", this.b);
        tag.method_10569("a", this.a);
        return tag;
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // meteordevelopment.meteorclient.utils.misc.ISerializable
    /* JADX INFO: renamed from: fromTag */
    public Color fromTag(class_2487 tag) {
        this.r = tag.method_68083("r", 0);
        this.g = tag.method_68083("g", 0);
        this.b = tag.method_68083("b", 0);
        this.a = tag.method_68083("a", 0);
        validate();
        return this;
    }

    public String toString() {
        return this.r + " " + this.g + " " + this.b + " " + this.a;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Color color = (Color) o;
        return this.r == color.r && this.g == color.g && this.b == color.b && this.a == color.a;
    }

    public int hashCode() {
        int result = this.r;
        return (31 * ((31 * ((31 * result) + this.g)) + this.b)) + this.a;
    }
}
