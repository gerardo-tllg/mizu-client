package meteordevelopment.meteorclient.utils.render;

import java.util.Objects;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/render/Box.class */
public class Box {
    public double x;
    public double y;
    public double width;
    public double height;

    public Box(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Box() {
        this(0.0d, 0.0d, 0.0d, 0.0d);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Box box = (Box) o;
        return Double.compare(box.x, this.x) == 0 && Double.compare(box.y, this.y) == 0 && Double.compare(box.width, this.width) == 0 && Double.compare(box.height, this.height) == 0;
    }

    public int hashCode() {
        return Objects.hash(Double.valueOf(this.x), Double.valueOf(this.y), Double.valueOf(this.width), Double.valueOf(this.height));
    }
}
