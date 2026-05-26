package meteordevelopment.meteorclient.utils.world;

import net.minecraft.class_2350;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/world/Dir.class */
public class Dir {
    public static final byte UP = 2;
    public static final byte DOWN = 4;
    public static final byte NORTH = 8;
    public static final byte SOUTH = 16;
    public static final byte WEST = 32;
    public static final byte EAST = 64;

    private Dir() {
    }

    /* JADX INFO: renamed from: meteordevelopment.meteorclient.utils.world.Dir$1, reason: invalid class name */
    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/world/Dir$1.class */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$net$minecraft$util$math$Direction = new int[class_2350.values().length];

        static {
            try {
                $SwitchMap$net$minecraft$util$math$Direction[class_2350.field_11036.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$net$minecraft$util$math$Direction[class_2350.field_11033.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$net$minecraft$util$math$Direction[class_2350.field_11043.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$net$minecraft$util$math$Direction[class_2350.field_11035.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$net$minecraft$util$math$Direction[class_2350.field_11039.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$net$minecraft$util$math$Direction[class_2350.field_11034.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
        }
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: java.lang.MatchException */
    public static byte get(class_2350 dir) throws MatchException {
        switch (AnonymousClass1.$SwitchMap$net$minecraft$util$math$Direction[dir.ordinal()]) {
            case 1:
                return (byte) 2;
            case 2:
                return (byte) 4;
            case 3:
                return (byte) 8;
            case 4:
                return (byte) 16;
            case 5:
                return (byte) 32;
            case 6:
                return (byte) 64;
            default:
                throw new MatchException((String) null, (Throwable) null);
        }
    }

    public static boolean is(int dir, byte idk) {
        return (dir & idk) == idk;
    }

    public static boolean isNot(int dir, byte idk) {
        return (dir & idk) != idk;
    }
}
