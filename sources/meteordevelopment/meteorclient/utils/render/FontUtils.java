package meteordevelopment.meteorclient.utils.render;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.renderer.Fonts;
import meteordevelopment.meteorclient.renderer.text.BuiltinFontFace;
import meteordevelopment.meteorclient.renderer.text.FontFace;
import meteordevelopment.meteorclient.renderer.text.FontFamily;
import meteordevelopment.meteorclient.renderer.text.FontInfo;
import meteordevelopment.meteorclient.renderer.text.SystemFontFace;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.class_156;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/render/FontUtils.class */
public class FontUtils {
    private FontUtils() {
    }

    public static FontInfo getSysFontInfo(File file) {
        return getFontInfo(stream(file));
    }

    public static FontInfo getBuiltinFontInfo(String builtin) {
        return getFontInfo(stream(builtin));
    }

    public static FontInfo getFontInfo(InputStream stream) {
        if (stream == null) {
            return null;
        }
        byte[] bytes = Utils.readBytes(stream);
        if (bytes.length < 5 || bytes[0] != 0 || bytes[1] != 1 || bytes[2] != 0 || bytes[3] != 0 || bytes[4] != 0) {
            return null;
        }
        ByteBuffer buffer = BufferUtils.createByteBuffer(bytes.length).put(bytes).flip();
        STBTTFontinfo fontInfo = STBTTFontinfo.create();
        if (!STBTruetype.stbtt_InitFont(fontInfo, buffer)) {
            return null;
        }
        ByteBuffer nameBuffer = STBTruetype.stbtt_GetFontNameString(fontInfo, 3, 1, 1033, 1);
        ByteBuffer typeBuffer = STBTruetype.stbtt_GetFontNameString(fontInfo, 3, 1, 1033, 2);
        if (typeBuffer == null || nameBuffer == null) {
            return null;
        }
        return new FontInfo(StandardCharsets.UTF_16.decode(nameBuffer).toString(), FontInfo.Type.fromString(StandardCharsets.UTF_16.decode(typeBuffer).toString()));
    }

    public static Set<String> getSearchPaths() {
        Set<String> paths = new HashSet<>();
        paths.add(System.getProperty("java.home") + "/lib/fonts");
        for (File dir : getUFontDirs()) {
            if (dir.exists()) {
                paths.add(dir.getAbsolutePath());
            }
        }
        for (File dir2 : getSFontDirs()) {
            if (dir2.exists()) {
                paths.add(dir2.getAbsolutePath());
            }
        }
        return paths;
    }

    /* JADX INFO: renamed from: meteordevelopment.meteorclient.utils.render.FontUtils$1, reason: invalid class name */
    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/render/FontUtils$1.class */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$net$minecraft$util$Util$OperatingSystem = new int[class_156.class_158.values().length];

        static {
            try {
                $SwitchMap$net$minecraft$util$Util$OperatingSystem[class_156.class_158.field_1133.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$net$minecraft$util$Util$OperatingSystem[class_156.class_158.field_1137.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    public static List<File> getUFontDirs() {
        switch (AnonymousClass1.$SwitchMap$net$minecraft$util$Util$OperatingSystem[class_156.method_668().ordinal()]) {
            case 1:
                return List.of(new File(System.getProperty("user.home") + "\\AppData\\Local\\Microsoft\\Windows\\Fonts"));
            case 2:
                return List.of(new File(System.getProperty("user.home") + "/Library/Fonts/"));
            default:
                return List.of(new File(System.getProperty("user.home") + "/.local/share/fonts"), new File(System.getProperty("user.home") + "/.fonts"));
        }
    }

    public static List<File> getSFontDirs() {
        switch (AnonymousClass1.$SwitchMap$net$minecraft$util$Util$OperatingSystem[class_156.method_668().ordinal()]) {
            case 1:
                return List.of(new File(System.getenv("SystemRoot") + "\\Fonts"));
            case 2:
                return List.of(new File("/System/Library/Fonts/"));
            default:
                return List.of(new File("/usr/share/fonts/"));
        }
    }

    public static void loadBuiltin(List<FontFamily> fontList, String builtin) {
        FontInfo fontInfo = getBuiltinFontInfo(builtin);
        if (fontInfo == null) {
            return;
        }
        FontFace fontFace = new BuiltinFontFace(fontInfo, builtin);
        if (!addFont(fontList, fontFace)) {
            MeteorClient.LOG.warn("Failed to load builtin font {}", fontFace);
        }
    }

    public static void loadSystem(List<FontFamily> fontList, File dir) {
        File[] files;
        if (dir.exists() && dir.isDirectory() && (files = dir.listFiles(file -> {
            return (file.isFile() && file.getName().endsWith(".ttf")) || file.isDirectory();
        })) != null) {
            for (File file2 : files) {
                if (file2.isDirectory()) {
                    loadSystem(fontList, file2);
                } else {
                    FontInfo fontInfo = getSysFontInfo(file2);
                    if (fontInfo != null) {
                        boolean isBuiltin = false;
                        String[] strArr = Fonts.BUILTIN_FONTS;
                        int length = strArr.length;
                        int i = 0;
                        while (true) {
                            if (i >= length) {
                                break;
                            }
                            String builtinFont = strArr[i];
                            if (!builtinFont.equals(fontInfo.family())) {
                                i++;
                            } else {
                                isBuiltin = true;
                                break;
                            }
                        }
                        if (!isBuiltin) {
                            FontFace fontFace = new SystemFontFace(fontInfo, file2.toPath());
                            if (!addFont(fontList, fontFace)) {
                                MeteorClient.LOG.warn("Failed to load system font {}", fontFace);
                            }
                        }
                    }
                }
            }
        }
    }

    public static boolean addFont(List<FontFamily> fontList, FontFace font) {
        if (font == null) {
            return false;
        }
        FontInfo info = font.info;
        FontFamily family = Fonts.getFamily(info.family());
        if (family == null) {
            family = new FontFamily(info.family());
            fontList.add(family);
        }
        if (family.hasType(info.type())) {
            return false;
        }
        return family.addFont(font);
    }

    public static InputStream stream(String builtin) {
        return FontUtils.class.getResourceAsStream("/assets/meteor-client/fonts/" + builtin + ".ttf");
    }

    public static InputStream stream(File file) {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
