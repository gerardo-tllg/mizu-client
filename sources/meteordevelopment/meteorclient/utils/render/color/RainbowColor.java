package meteordevelopment.meteorclient.utils.render.color;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/render/color/RainbowColor.class */
public class RainbowColor extends Color {
    private double speed;
    private static final float[] hsb = new float[3];

    public double getSpeed() {
        return this.speed;
    }

    public RainbowColor setSpeed(double speed) {
        this.speed = speed;
        return this;
    }

    public RainbowColor getNext() {
        return getNext(1.0d);
    }

    public RainbowColor getNext(double delta) {
        if (this.speed > 0.0d) {
            java.awt.Color.RGBtoHSB(this.r, this.g, this.b, hsb);
            int c = java.awt.Color.HSBtoRGB(hsb[0] + ((float) (this.speed * delta)), 1.0f, 1.0f);
            this.r = toRGBAR(c);
            this.g = toRGBAG(c);
            this.b = toRGBAB(c);
        }
        return this;
    }

    public RainbowColor set(RainbowColor color) {
        this.r = color.r;
        this.g = color.g;
        this.b = color.b;
        this.a = color.a;
        this.speed = color.speed;
        return this;
    }

    @Override // meteordevelopment.meteorclient.utils.render.color.Color
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o != null && getClass() == o.getClass() && super.equals(o) && Double.compare(((RainbowColor) o).speed, this.speed) == 0;
    }

    @Override // meteordevelopment.meteorclient.utils.render.color.Color
    public int hashCode() {
        int result = super.hashCode();
        long temp = Double.doubleToLongBits(this.speed);
        return (31 * result) + ((int) (temp ^ (temp >>> 32)));
    }
}
