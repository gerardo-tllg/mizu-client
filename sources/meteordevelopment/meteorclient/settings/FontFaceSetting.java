package meteordevelopment.meteorclient.settings;

import java.util.List;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.renderer.Fonts;
import meteordevelopment.meteorclient.renderer.text.FontFace;
import meteordevelopment.meteorclient.renderer.text.FontFamily;
import meteordevelopment.meteorclient.renderer.text.FontInfo;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.class_2487;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/settings/FontFaceSetting.class */
public class FontFaceSetting extends Setting<FontFace> {
    public FontFaceSetting(String name, String description, FontFace defaultValue, Consumer<FontFace> onChanged, Consumer<Setting<FontFace>> onModuleActivated, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Can't rename method to resolve collision */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public FontFace parseImpl(String str) {
        String[] split = str.replace(" ", "").split("-");
        if (split.length != 2) {
            return null;
        }
        for (FontFamily family : Fonts.FONT_FAMILIES) {
            if (family.getName().replace(" ", "").equals(split[0])) {
                try {
                    return family.get(FontInfo.Type.valueOf(split[1]));
                } catch (IllegalArgumentException e) {
                    return null;
                }
            }
        }
        return null;
    }

    @Override // meteordevelopment.meteorclient.settings.Setting
    public List<String> getSuggestions() {
        return List.of("JetBrainsMono-Regular", "Arial-Bold");
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public boolean isValueValid(FontFace value) {
        if (value == null) {
            return false;
        }
        for (FontFamily fontFamily : Fonts.FONT_FAMILIES) {
            if (fontFamily.hasType(value.info.type())) {
                return true;
            }
        }
        return false;
    }

    @Override // meteordevelopment.meteorclient.settings.Setting
    protected class_2487 save(class_2487 tag) {
        tag.method_10582("family", get().info.family());
        tag.method_10582("type", get().info.type().toString());
        return tag;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Can't rename method to resolve collision */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public FontFace load(class_2487 tag) {
        String family = tag.method_68564("family", "");
        try {
            FontInfo.Type type = FontInfo.Type.valueOf(tag.method_68564("type", ""));
            boolean changed = false;
            for (FontFamily fontFamily : Fonts.FONT_FAMILIES) {
                if (fontFamily.getName().equals(family)) {
                    set(fontFamily.get(type));
                    changed = true;
                }
            }
            if (!changed) {
                set(Fonts.DEFAULT_FONT);
            }
            return get();
        } catch (IllegalArgumentException e) {
            set(Fonts.DEFAULT_FONT);
            return get();
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/settings/FontFaceSetting$Builder.class */
    public static class Builder extends Setting.SettingBuilder<Builder, FontFace, FontFaceSetting> {
        public Builder() {
            super(Fonts.DEFAULT_FONT);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        /* JADX WARN: Multi-variable type inference failed */
        @Override // meteordevelopment.meteorclient.settings.Setting.SettingBuilder
        public FontFaceSetting build() {
            return new FontFaceSetting(this.name, this.description, (FontFace) this.defaultValue, this.onChanged, this.onModuleActivated, this.visible);
        }
    }
}
