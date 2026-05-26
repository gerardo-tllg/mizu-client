package meteordevelopment.meteorclient.utils.misc.text;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.ObjectMethods;
import meteordevelopment.meteorclient.utils.render.color.Color;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/misc/text/ColoredText.class */
public final class ColoredText extends Record {
    private final String text;
    private final Color color;

    public ColoredText(String text, Color color) {
        this.text = text;
        this.color = color;
    }

    @Override // java.lang.Record
    public final String toString() {
        return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, ColoredText.class), ColoredText.class, "text;color", "FIELD:Lmeteordevelopment/meteorclient/utils/misc/text/ColoredText;->text:Ljava/lang/String;", "FIELD:Lmeteordevelopment/meteorclient/utils/misc/text/ColoredText;->color:Lmeteordevelopment/meteorclient/utils/render/color/Color;").dynamicInvoker().invoke(this) /* invoke-custom */;
    }

    @Override // java.lang.Record
    public final int hashCode() {
        return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, ColoredText.class), ColoredText.class, "text;color", "FIELD:Lmeteordevelopment/meteorclient/utils/misc/text/ColoredText;->text:Ljava/lang/String;", "FIELD:Lmeteordevelopment/meteorclient/utils/misc/text/ColoredText;->color:Lmeteordevelopment/meteorclient/utils/render/color/Color;").dynamicInvoker().invoke(this) /* invoke-custom */;
    }

    @Override // java.lang.Record
    public final boolean equals(Object o) {
        return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, ColoredText.class, Object.class), ColoredText.class, "text;color", "FIELD:Lmeteordevelopment/meteorclient/utils/misc/text/ColoredText;->text:Ljava/lang/String;", "FIELD:Lmeteordevelopment/meteorclient/utils/misc/text/ColoredText;->color:Lmeteordevelopment/meteorclient/utils/render/color/Color;").dynamicInvoker().invoke(this, o) /* invoke-custom */;
    }

    public String text() {
        return this.text;
    }

    public Color color() {
        return this.color;
    }
}
