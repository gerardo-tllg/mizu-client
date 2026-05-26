package meteordevelopment.meteorclient.utils.world;

import net.minecraft.class_2350;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/world/CardinalDirection.class */
public enum CardinalDirection {
    North,
    East,
    South,
    West;

    /* JADX INFO: Thrown type has an unknown type hierarchy: java.lang.MatchException */
    public class_2350 toDirection() throws MatchException {
        switch (this) {
            case North:
                return class_2350.field_11043;
            case East:
                return class_2350.field_11034;
            case South:
                return class_2350.field_11035;
            case West:
                return class_2350.field_11039;
            default:
                throw new MatchException((String) null, (Throwable) null);
        }
    }

    /* JADX INFO: renamed from: meteordevelopment.meteorclient.utils.world.CardinalDirection$1, reason: invalid class name */
    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/world/CardinalDirection$1.class */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$net$minecraft$util$math$Direction = new int[class_2350.values().length];

        static {
            try {
                $SwitchMap$net$minecraft$util$math$Direction[class_2350.field_11043.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$net$minecraft$util$math$Direction[class_2350.field_11035.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$net$minecraft$util$math$Direction[class_2350.field_11039.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$net$minecraft$util$math$Direction[class_2350.field_11034.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$net$minecraft$util$math$Direction[class_2350.field_11033.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$net$minecraft$util$math$Direction[class_2350.field_11036.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
        }
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: java.lang.MatchException */
    public static CardinalDirection fromDirection(class_2350 direction) throws MatchException {
        switch (AnonymousClass1.$SwitchMap$net$minecraft$util$math$Direction[direction.ordinal()]) {
            case 1:
                return North;
            case 2:
                return South;
            case 3:
                return East;
            case 4:
                return West;
            case 5:
            case 6:
                return null;
            default:
                throw new MatchException((String) null, (Throwable) null);
        }
    }
}
