package meteordevelopment.meteorclient.renderer.text;

import java.io.InputStream;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/renderer/text/FontFace.class */
public abstract class FontFace {
    public final FontInfo info;

    public abstract InputStream toStream();

    protected FontFace(FontInfo info) {
        this.info = info;
    }

    public String toString() {
        return this.info.toString();
    }
}
