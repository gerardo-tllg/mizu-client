package meteordevelopment.meteorclient.utils.misc;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/misc/HorizontalDirection.class */
public enum HorizontalDirection {
    South("South", "Z+", false, 0.0f, 0, 1),
    SouthEast("South East", "X+ Z+", true, -45.0f, 1, 1),
    West("West", "X-", false, 90.0f, -1, 0),
    NorthWest("North West", "X- Z-", true, 135.0f, -1, -1),
    North("North", "Z-", false, 180.0f, 0, -1),
    NorthEast("North East", "X+ Z-", true, -135.0f, 1, -1),
    East("East", "X+", false, -90.0f, 1, 0),
    SouthWest("South West", "X- Z+", true, 45.0f, -1, 1);

    public final String name;
    public final String axis;
    public final boolean diagonal;
    public final float yaw;
    public final int offsetX;
    public final int offsetZ;

    HorizontalDirection(String name, String axis, boolean diagonal, float yaw, int offsetX, int offsetZ) {
        this.axis = axis;
        this.name = name;
        this.diagonal = diagonal;
        this.yaw = yaw;
        this.offsetX = offsetX;
        this.offsetZ = offsetZ;
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: java.lang.MatchException */
    public HorizontalDirection opposite() throws MatchException {
        switch (this) {
            case South:
                return North;
            case SouthEast:
                return NorthWest;
            case West:
                return East;
            case NorthWest:
                return SouthEast;
            case North:
                return South;
            case NorthEast:
                return SouthWest;
            case East:
                return West;
            case SouthWest:
                return NorthEast;
            default:
                throw new MatchException((String) null, (Throwable) null);
        }
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: java.lang.MatchException */
    public HorizontalDirection rotateLeft() throws MatchException {
        switch (this) {
            case South:
                return SouthEast;
            case SouthEast:
                return East;
            case West:
                return SouthWest;
            case NorthWest:
                return West;
            case North:
                return NorthWest;
            case NorthEast:
                return North;
            case East:
                return NorthEast;
            case SouthWest:
                return South;
            default:
                throw new MatchException((String) null, (Throwable) null);
        }
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: java.lang.MatchException */
    public HorizontalDirection rotateLeftSkipOne() throws MatchException {
        switch (this) {
            case South:
                return East;
            case SouthEast:
                return NorthEast;
            case West:
                return South;
            case NorthWest:
                return SouthWest;
            case North:
                return West;
            case NorthEast:
                return NorthWest;
            case East:
                return North;
            case SouthWest:
                return SouthEast;
            default:
                throw new MatchException((String) null, (Throwable) null);
        }
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: java.lang.MatchException */
    public HorizontalDirection rotateRight() throws MatchException {
        switch (this) {
            case South:
                return SouthWest;
            case SouthEast:
                return South;
            case West:
                return NorthWest;
            case NorthWest:
                return North;
            case North:
                return NorthEast;
            case NorthEast:
                return East;
            case East:
                return SouthEast;
            case SouthWest:
                return West;
            default:
                throw new MatchException((String) null, (Throwable) null);
        }
    }

    public static HorizontalDirection get(float yaw) {
        float yaw2 = yaw % 360.0f;
        if (yaw2 < 0.0f) {
            yaw2 += 360.0f;
        }
        return (((double) yaw2) >= 337.5d || ((double) yaw2) < 22.5d) ? South : (((double) yaw2) < 22.5d || ((double) yaw2) >= 67.5d) ? (((double) yaw2) < 67.5d || ((double) yaw2) >= 112.5d) ? (((double) yaw2) < 112.5d || ((double) yaw2) >= 157.5d) ? (((double) yaw2) < 157.5d || ((double) yaw2) >= 202.5d) ? (((double) yaw2) < 202.5d || ((double) yaw2) >= 247.5d) ? (((double) yaw2) < 247.5d || ((double) yaw2) >= 292.5d) ? (((double) yaw2) < 292.5d || ((double) yaw2) >= 337.5d) ? South : SouthEast : East : NorthEast : North : NorthWest : West : SouthWest;
    }
}
