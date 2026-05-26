package meteordevelopment.meteorclient.systems.hud;

import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import net.minecraft.class_2487;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/hud/HudBox.class */
public class HudBox implements ISerializable<HudBox> {
    private final HudElement element;
    public XAnchor xAnchor = XAnchor.Left;
    public YAnchor yAnchor = YAnchor.Top;
    public int x;
    public int y;
    int width;
    int height;

    public HudBox(HudElement element) {
        this.element = element;
    }

    public void setSize(double width, double height) {
        if (width >= 0.0d) {
            this.width = (int) Math.ceil(width);
        }
        if (height >= 0.0d) {
            this.height = (int) Math.ceil(height);
        }
    }

    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: java.lang.MatchException */
    public void setXAnchor(XAnchor anchor) throws MatchException {
        if (this.xAnchor != anchor) {
            int renderX = getRenderX();
            switch (anchor) {
                case Left:
                    this.x = renderX;
                    break;
                case Center:
                    this.x = (renderX + (this.width / 2)) - (Utils.getWindowWidth() / 2);
                    break;
                case Right:
                    this.x = (renderX + this.width) - Utils.getWindowWidth();
                    break;
            }
            this.xAnchor = anchor;
        }
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: java.lang.MatchException */
    public void setYAnchor(YAnchor anchor) throws MatchException {
        if (this.yAnchor != anchor) {
            int renderY = getRenderY();
            switch (anchor) {
                case Top:
                    this.y = renderY;
                    break;
                case Center:
                    this.y = (renderY + (this.height / 2)) - (Utils.getWindowHeight() / 2);
                    break;
                case Bottom:
                    this.y = (renderY + this.height) - Utils.getWindowHeight();
                    break;
            }
            this.yAnchor = anchor;
        }
    }

    public void updateAnchors() {
        setXAnchor(getXAnchor(getRenderX()));
        setYAnchor(getYAnchor(getRenderY()));
    }

    public void move(int deltaX, int deltaY) {
        this.x += deltaX;
        this.y += deltaY;
        if (this.element.autoAnchors) {
            updateAnchors();
        }
        int border = Hud.get().border.get().intValue();
        if (this.xAnchor == XAnchor.Left && this.x < border) {
            this.x = border;
        } else if (this.xAnchor == XAnchor.Right && this.x > border) {
            this.x = border;
        }
        if (this.yAnchor != YAnchor.Top || this.y >= border) {
            if (this.yAnchor != YAnchor.Bottom || this.y <= border) {
                return;
            }
            this.y = border;
            return;
        }
        this.y = border;
    }

    public XAnchor getXAnchor(double x) {
        double splitLeft = ((double) Utils.getWindowWidth()) / 3.0d;
        double splitRight = splitLeft * 2.0d;
        boolean left = x <= splitLeft;
        boolean right = x + ((double) this.width) >= splitRight;
        return (!(left && right) && (left || right)) ? left ? XAnchor.Left : XAnchor.Right : XAnchor.Center;
    }

    public YAnchor getYAnchor(double y) {
        double splitTop = ((double) Utils.getWindowHeight()) / 3.0d;
        double splitBottom = splitTop * 2.0d;
        boolean top = y <= splitTop;
        boolean bottom = y + ((double) this.height) >= splitBottom;
        return (!(top && bottom) && (top || bottom)) ? top ? YAnchor.Top : YAnchor.Bottom : YAnchor.Center;
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: java.lang.MatchException */
    public int getRenderX() throws MatchException {
        switch (this.xAnchor) {
            case Left:
                return this.x;
            case Center:
                return ((Utils.getWindowWidth() / 2) - (this.width / 2)) + this.x;
            case Right:
                return (Utils.getWindowWidth() - this.width) + this.x;
            default:
                throw new MatchException((String) null, (Throwable) null);
        }
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: java.lang.MatchException */
    public int getRenderY() throws MatchException {
        switch (this.yAnchor) {
            case Top:
                return this.y;
            case Center:
                return ((Utils.getWindowHeight() / 2) - (this.height / 2)) + this.y;
            case Bottom:
                return (Utils.getWindowHeight() - this.height) + this.y;
            default:
                throw new MatchException((String) null, (Throwable) null);
        }
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: java.lang.MatchException */
    public double alignX(double selfWidth, double width, Alignment alignment) throws MatchException {
        XAnchor anchor = this.xAnchor;
        if (alignment == Alignment.Left) {
            anchor = XAnchor.Left;
        } else if (alignment == Alignment.Center) {
            anchor = XAnchor.Center;
        } else if (alignment == Alignment.Right) {
            anchor = XAnchor.Right;
        }
        switch (anchor) {
            case Left:
                return 0.0d;
            case Center:
                return (selfWidth / 2.0d) - (width / 2.0d);
            case Right:
                return selfWidth - width;
            default:
                throw new MatchException((String) null, (Throwable) null);
        }
    }

    @Override // meteordevelopment.meteorclient.utils.misc.ISerializable
    public class_2487 toTag() {
        class_2487 tag = new class_2487();
        tag.method_10582("x-anchor", this.xAnchor.name());
        tag.method_10582("y-anchor", this.yAnchor.name());
        tag.method_10569("x", this.x);
        tag.method_10569("y", this.y);
        return tag;
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // meteordevelopment.meteorclient.utils.misc.ISerializable
    /* JADX INFO: renamed from: fromTag */
    public HudBox fromTag(class_2487 tag) {
        if (tag.method_10545("x-anchor")) {
            this.xAnchor = XAnchor.valueOf((String) tag.method_10558("x-anchor").orElse("Left"));
        }
        if (tag.method_10545("y-anchor")) {
            this.yAnchor = YAnchor.valueOf((String) tag.method_10558("y-anchor").orElse("Top"));
        }
        if (tag.method_10545("x")) {
            this.x = ((Integer) tag.method_10550("x").orElse(0)).intValue();
        }
        if (tag.method_10545("y")) {
            this.y = ((Integer) tag.method_10550("y").orElse(0)).intValue();
        }
        return this;
    }
}
