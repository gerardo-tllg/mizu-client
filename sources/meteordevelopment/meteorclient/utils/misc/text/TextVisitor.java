package meteordevelopment.meteorclient.utils.misc.text;

import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;
import net.minecraft.class_2561;
import net.minecraft.class_2583;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/misc/text/TextVisitor.class */
@FunctionalInterface
public interface TextVisitor<T> {
    Optional<T> accept(class_2561 class_2561Var, class_2583 class_2583Var, String str);

    static <T> Optional<T> visit(class_2561 text, TextVisitor<T> visitor, class_2583 baseStyle) {
        Queue<class_2561> queue = collectSiblings(text);
        return text.method_27658((style, string) -> {
            return visitor.accept((class_2561) queue.remove(), style, string);
        }, baseStyle);
    }

    static ArrayDeque<class_2561> collectSiblings(class_2561 text) {
        ArrayDeque<class_2561> queue = new ArrayDeque<>();
        collectSiblings(text, queue);
        return queue;
    }

    /* JADX WARN: Removed duplicated region for block: B:6:0x001f  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private static void collectSiblings(net.minecraft.class_2561 r3, java.util.Queue<net.minecraft.class_2561> r4) {
        /*
            r0 = r3
            net.minecraft.class_7417 r0 = r0.method_10851()
            r6 = r0
            r0 = r6
            boolean r0 = r0 instanceof net.minecraft.class_8828
            if (r0 == 0) goto L1f
            r0 = r6
            net.minecraft.class_8828 r0 = (net.minecraft.class_8828) r0
            r5 = r0
            r0 = r5
            java.lang.String r0 = r0.comp_737()
            boolean r0 = r0.isEmpty()
            if (r0 != 0) goto L27
        L1f:
            r0 = r4
            r1 = r3
            boolean r0 = r0.add(r1)
        L27:
            r0 = r3
            java.util.List r0 = r0.method_10855()
            java.util.Iterator r0 = r0.iterator()
            r5 = r0
        L33:
            r0 = r5
            boolean r0 = r0.hasNext()
            if (r0 == 0) goto L4e
            r0 = r5
            java.lang.Object r0 = r0.next()
            net.minecraft.class_2561 r0 = (net.minecraft.class_2561) r0
            r6 = r0
            r0 = r6
            r1 = r4
            collectSiblings(r0, r1)
            goto L33
        L4e:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: meteordevelopment.meteorclient.utils.misc.text.TextVisitor.collectSiblings(net.minecraft.class_2561, java.util.Queue):void");
    }
}
