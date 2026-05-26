package meteordevelopment.meteorclient.renderer.text;

import java.io.InputStream;
import meteordevelopment.meteorclient.utils.render.FontUtils;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/renderer/text/BuiltinFontFace.class */
public class BuiltinFontFace extends FontFace {
    private final String name;

    public BuiltinFontFace(FontInfo info, String name) {
        super(info);
        this.name = name;
    }

    @Override // meteordevelopment.meteorclient.renderer.text.FontFace
    public InputStream toStream() {
        InputStream in = FontUtils.stream(this.name);
        if (in == null) {
            throw new RuntimeException("Failed to load builtin font " + this.name + ".");
        }
        return in;
    }

    @Override // meteordevelopment.meteorclient.renderer.text.FontFace
    public String toString() {
        return super.toString() + " (builtin)";
    }
}
