package meteordevelopment.meteorclient.renderer.text;

import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.renderer.text.FontInfo;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/renderer/text/FontFamily.class */
public class FontFamily {
    private final String name;
    private final List<FontFace> fonts = new ArrayList();

    public FontFamily(String name) {
        this.name = name;
    }

    public boolean addFont(FontFace font) {
        return this.fonts.add(font);
    }

    public boolean hasType(FontInfo.Type type) {
        return get(type) != null;
    }

    public FontFace get(FontInfo.Type type) {
        if (type == null) {
            return null;
        }
        for (FontFace font : this.fonts) {
            if (font.info.type().equals(type)) {
                return font;
            }
        }
        return null;
    }

    public String getName() {
        return this.name;
    }
}
