package meteordevelopment.meteorclient.utils.other;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/other/Snapper.class */
public class Snapper {
    private final Container container;
    private Element snappedTo;
    private Direction mainDir;
    private int mainPos;
    private boolean secondary;
    private int secondaryPos;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/other/Snapper$Container.class */
    public interface Container {
        Iterable<Element> getElements();

        boolean shouldNotSnapTo(Element element);

        int getSnappingRange();
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/other/Snapper$Direction.class */
    private enum Direction {
        Right,
        Left,
        Top,
        Bottom
    }

    public Snapper(Container container) {
        this.container = container;
    }

    public void move(Element element, int deltaX, int deltaY) {
        if (this.container.getSnappingRange() == 0) {
            element.move(deltaX, deltaY);
        } else if (this.snappedTo != null) {
            moveSnapped(element, deltaX, deltaY);
        } else {
            moveUnsnapped(element, deltaX, deltaY);
        }
    }

    public void unsnap() {
        this.snappedTo = null;
    }

    private void moveUnsnapped(Element element, int deltaX, int deltaY) {
        int dist;
        int dist2;
        int dist3;
        int dist4;
        element.move(deltaX, deltaY);
        if (deltaX > 0) {
            Element closest = null;
            int closestDist = Integer.MAX_VALUE;
            for (Element e : this.container.getElements()) {
                if (!this.container.shouldNotSnapTo(e) && (dist4 = e.getX() - element.getX2()) > 0 && dist4 <= this.container.getSnappingRange() && (closest == null || dist4 < closestDist)) {
                    if (isNextToHorizontally(element, e)) {
                        closest = e;
                        closestDist = dist4;
                    }
                }
            }
            if (closest != null) {
                element.setPos(closest.getX() - element.getWidth(), element.getY());
                snapMain(closest, Direction.Right);
                return;
            }
            return;
        }
        if (deltaX < 0) {
            Element closest2 = null;
            int closestDist2 = Integer.MAX_VALUE;
            for (Element e2 : this.container.getElements()) {
                if (!this.container.shouldNotSnapTo(e2) && (dist3 = element.getX() - e2.getX2()) > 0 && dist3 <= this.container.getSnappingRange() && (closest2 == null || dist3 < closestDist2)) {
                    if (isNextToHorizontally(element, e2)) {
                        closest2 = e2;
                        closestDist2 = dist3;
                    }
                }
            }
            if (closest2 != null) {
                element.setPos(closest2.getX2(), element.getY());
                snapMain(closest2, Direction.Left);
                return;
            }
            return;
        }
        if (deltaY > 0) {
            Element closest3 = null;
            int closestDist3 = Integer.MAX_VALUE;
            for (Element e3 : this.container.getElements()) {
                if (!this.container.shouldNotSnapTo(e3) && (dist2 = e3.getY() - element.getY2()) > 0 && dist2 <= this.container.getSnappingRange() && (closest3 == null || dist2 < closestDist3)) {
                    if (isNextToVertically(element, e3)) {
                        closest3 = e3;
                        closestDist3 = dist2;
                    }
                }
            }
            if (closest3 != null) {
                element.setPos(element.getX(), closest3.getY() - element.getHeight());
                snapMain(closest3, Direction.Top);
                return;
            }
            return;
        }
        if (deltaY < 0) {
            Element closest4 = null;
            int closestDist4 = Integer.MAX_VALUE;
            for (Element e4 : this.container.getElements()) {
                if (!this.container.shouldNotSnapTo(e4) && (dist = element.getY() - e4.getY2()) > 0 && dist <= this.container.getSnappingRange() && (closest4 == null || dist < closestDist4)) {
                    if (isNextToVertically(element, e4)) {
                        closest4 = e4;
                        closestDist4 = dist;
                    }
                }
            }
            if (closest4 != null) {
                element.setPos(element.getX(), closest4.getY2());
                snapMain(closest4, Direction.Bottom);
            }
        }
    }

    private void moveSnapped(Element element, int deltaX, int deltaY) {
        int dist;
        int dist2;
        switch (this.mainDir) {
            case Right:
            case Left:
                if (this.secondary) {
                    this.secondaryPos += deltaY;
                } else {
                    element.move(0, deltaY);
                }
                this.mainPos += deltaX;
                if (!isNextToHorizontally(element, this.snappedTo)) {
                    unsnap();
                } else if (!this.secondary) {
                    if (deltaY > 0) {
                        int dist3 = this.snappedTo.getY2() - element.getY2();
                        if (dist3 > 0 && dist3 < this.container.getSnappingRange()) {
                            element.setPos(element.getX(), this.snappedTo.getY2() - element.getHeight());
                            snapSecondary();
                        }
                    } else if (deltaY < 0 && (dist2 = this.snappedTo.getY() - element.getY()) < 0 && dist2 > (-this.container.getSnappingRange())) {
                        element.setPos(element.getX(), this.snappedTo.getY());
                        snapSecondary();
                    }
                }
                break;
            case Top:
            case Bottom:
                if (this.secondary) {
                    this.secondaryPos += deltaX;
                } else {
                    element.move(deltaX, 0);
                }
                this.mainPos += deltaY;
                if (!isNextToVertically(element, this.snappedTo)) {
                    unsnap();
                } else if (!this.secondary) {
                    if (deltaX > 0) {
                        int dist4 = this.snappedTo.getX2() - element.getX2();
                        if (dist4 > 0 && dist4 < this.container.getSnappingRange()) {
                            element.setPos(this.snappedTo.getX2() - element.getWidth(), element.getY());
                            snapSecondary();
                        }
                    } else if (deltaX < 0 && (dist = element.getX() - this.snappedTo.getX()) > 0 && dist < this.container.getSnappingRange()) {
                        element.setPos(this.snappedTo.getX(), element.getY());
                        snapSecondary();
                    }
                }
                break;
        }
        if (Math.abs(this.mainPos) <= this.container.getSnappingRange() * 5) {
            if (Math.abs(this.secondaryPos) > this.container.getSnappingRange() * 5) {
                this.secondary = false;
                return;
            }
            return;
        }
        unsnap();
    }

    private void snapMain(Element element, Direction dir) {
        this.snappedTo = element;
        this.mainDir = dir;
        this.mainPos = 0;
        this.secondary = false;
    }

    private void snapSecondary() {
        this.secondary = true;
        this.secondaryPos = 0;
    }

    private boolean isBetween(int value, int min, int max) {
        return value > min && value < max;
    }

    private boolean isNextToHorizontally(Element e1, Element e2) {
        int y1 = e1.getY();
        int h1 = e1.getHeight();
        int y2 = e2.getY();
        int h2 = e2.getHeight();
        return isBetween(y1, y2, y2 + h2) || isBetween(y1 + h1, y2, y2 + h2) || isBetween(y2, y1, y1 + h1) || isBetween(y2 + h2, y1, y1 + h1);
    }

    private boolean isNextToVertically(Element e1, Element e2) {
        int x1 = e1.getX();
        int w1 = e1.getWidth();
        int x2 = e2.getX();
        int w2 = e2.getWidth();
        return isBetween(x1, x2, x2 + w2) || isBetween(x1 + w1, x2, x2 + w2) || isBetween(x2, x1, x1 + w1) || isBetween(x2 + w2, x1, x1 + w1);
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/other/Snapper$Element.class */
    public interface Element {
        int getX();

        int getY();

        int getWidth();

        int getHeight();

        void setPos(int i, int i2);

        void move(int i, int i2);

        default int getX2() {
            return getX() + getWidth();
        }

        default int getY2() {
            return getY() + getHeight();
        }
    }
}
