package meteordevelopment.meteorclient.renderer.text;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.ObjectMethods;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/renderer/text/FontInfo.class */
public final class FontInfo extends Record {
    private final String family;
    private final Type type;

    public FontInfo(String family, Type type) {
        this.family = family;
        this.type = type;
    }

    @Override // java.lang.Record
    public final int hashCode() {
        return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, FontInfo.class), FontInfo.class, "family;type", "FIELD:Lmeteordevelopment/meteorclient/renderer/text/FontInfo;->family:Ljava/lang/String;", "FIELD:Lmeteordevelopment/meteorclient/renderer/text/FontInfo;->type:Lmeteordevelopment/meteorclient/renderer/text/FontInfo$Type;").dynamicInvoker().invoke(this) /* invoke-custom */;
    }

    @Override // java.lang.Record
    public final boolean equals(Object o) {
        return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, FontInfo.class, Object.class), FontInfo.class, "family;type", "FIELD:Lmeteordevelopment/meteorclient/renderer/text/FontInfo;->family:Ljava/lang/String;", "FIELD:Lmeteordevelopment/meteorclient/renderer/text/FontInfo;->type:Lmeteordevelopment/meteorclient/renderer/text/FontInfo$Type;").dynamicInvoker().invoke(this, o) /* invoke-custom */;
    }

    public String family() {
        return this.family;
    }

    public Type type() {
        return this.type;
    }

    @Override // java.lang.Record
    public String toString() {
        return this.family + " " + String.valueOf(this.type);
    }

    public boolean equals(FontInfo info) {
        if (this == info) {
            return true;
        }
        return (info == null || this.family == null || this.type == null || !this.family.equals(info.family) || this.type != info.type) ? false : true;
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/renderer/text/FontInfo$Type.class */
    public enum Type {
        Regular,
        Bold,
        Italic,
        BoldItalic;

        public static Type fromString(String str) {
            switch (str) {
                case "Bold":
                    return Bold;
                case "Italic":
                    return Italic;
                case "Bold Italic":
                case "BoldItalic":
                    return BoldItalic;
                default:
                    return Regular;
            }
        }

        @Override // java.lang.Enum
        public String toString() {
            switch (ordinal()) {
                case 1:
                    return "Bold";
                case 2:
                    return "Italic";
                case 3:
                    return "Bold Italic";
                default:
                    return "Regular";
            }
        }
    }
}
