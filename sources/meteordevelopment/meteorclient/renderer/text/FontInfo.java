package meteordevelopment.meteorclient.renderer.text;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/renderer/text/FontInfo.class */
public record FontInfo(String family, Type type) {

    @Override
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
