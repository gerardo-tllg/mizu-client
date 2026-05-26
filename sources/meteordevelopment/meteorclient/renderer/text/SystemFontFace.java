package meteordevelopment.meteorclient.renderer.text;

import java.io.InputStream;
import java.nio.file.Path;
import meteordevelopment.meteorclient.utils.render.FontUtils;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/renderer/text/SystemFontFace.class */
public class SystemFontFace extends FontFace {
    private final Path path;

    public SystemFontFace(FontInfo info, Path path) {
        super(info);
        this.path = path;
    }

    @Override // meteordevelopment.meteorclient.renderer.text.FontFace
    public InputStream toStream() {
        if (!this.path.toFile().exists()) {
            throw new RuntimeException("Tried to load font that no longer exists.");
        }
        InputStream in = FontUtils.stream(this.path.toFile());
        if (in == null) {
            throw new RuntimeException("Failed to load font from " + String.valueOf(this.path) + ".");
        }
        return in;
    }

    @Override // meteordevelopment.meteorclient.renderer.text.FontFace
    public String toString() {
        return super.toString() + " (" + this.path.toString() + ")";
    }
}
