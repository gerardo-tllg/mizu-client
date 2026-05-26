package meteordevelopment.meteorclient.util.animation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import javassist.bytecode.Opcode;
import javassist.compiler.TokenId;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3298;
import net.minecraft.class_332;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/util/animation/Ascii.class */
public class Ascii {
    private final long frameDelay;
    private int x;
    private int y;
    private int width;
    private int height;
    private String lastLoadedResource;
    private static final char[] BRIGHT_CHARS = {'@', '#', '&', '$', 'X', 'L'};
    private static final char[] MEDIUM_CHARS = {'S', '8', 'O', 'o', '*', '+'};
    private static final char[] DARK_CHARS = {':', ';', ',', '.', '-', '='};
    private static final char[] SKIN_CHARS = {'@', '#', '$', '&', '%', 'O'};
    private final List<ColorChar[][]> frames = new ArrayList();
    private final List<BufferedImage> rawFrames = new ArrayList();
    private int currentFrame = 0;
    private int frameCount = 0;
    private boolean isLoaded = false;
    private String errorMessage = null;
    private long lastFrameTime = 0;
    private float textScale = 0.65f;
    private float animationTime = 0.0f;
    private final Random random = new Random();
    private boolean useOriginalColors = true;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/util/animation/Ascii$ColorChar.class */
    private static class ColorChar {
        char character;
        int colorValue;

        ColorChar(char character, int colorValue) {
            this.character = character;
            this.colorValue = colorValue;
        }
    }

    public Ascii(int screenWidth, int screenHeight, String resourcePath, int frameDelayMs) {
        this.x = screenWidth - 480;
        this.y = (screenHeight / 2) - 200;
        this.frameDelay = frameDelayMs;
        this.lastLoadedResource = resourcePath;
        loadGifFromResource(resourcePath);
    }

    private void loadGifFromResource(String resourcePath) {
        this.lastLoadedResource = resourcePath;
        try {
            class_2960 id = class_2960.method_60655(MeteorClient.MOD_ID, resourcePath);
            Optional<class_3298> resourceOptional = class_310.method_1551().method_1478().method_14486(id);
            if (resourceOptional.isPresent()) {
                class_3298 resource = resourceOptional.get();
                InputStream inputStream = resource.method_14482();
                ImageReader reader = (ImageReader) ImageIO.getImageReadersByFormatName("gif").next();
                ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream);
                reader.setInput(imageInputStream);
                int numFrames = reader.getNumImages(true);
                this.width = 70;
                this.height = 75;
                this.frames.clear();
                this.rawFrames.clear();
                for (int i = 0; i < numFrames; i++) {
                    BufferedImage frame = reader.read(i);
                    this.rawFrames.add(frame);
                    this.frames.add(convertToAscii(frame));
                }
                imageInputStream.close();
                inputStream.close();
                reader.dispose();
                this.frameCount = this.frames.size();
                this.isLoaded = true;
            } else {
                this.errorMessage = "Resource not found: " + resourcePath;
            }
        } catch (Exception e) {
            this.errorMessage = "Error loading GIF: " + e.getMessage();
            e.printStackTrace();
        }
    }

    private ColorChar[][] convertToAscii(BufferedImage image) {
        char asciiChar;
        int finalColor;
        BufferedImage resized = new BufferedImage(this.width, this.height, 2);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(image, 0, 0, this.width, this.height, (ImageObserver) null);
        g.dispose();
        BufferedImage enhanced = enhanceImage(resized);
        boolean[][] isFaceRegion = new boolean[this.height][this.width];
        boolean[][] isSkinRegion = new boolean[this.height][this.width];
        int faceTop = this.height;
        int faceBottom = 0;
        int faceLeft = this.width;
        int faceRight = 0;
        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                int pixel = enhanced.getRGB(x, y);
                int alpha = (pixel >> 24) & 255;
                if (alpha >= 50) {
                    boolean isSkin = isSkin((pixel >> 16) & 255, (pixel >> 8) & 255, pixel & 255);
                    if (isSkin) {
                        isSkinRegion[y][x] = true;
                        boolean possiblyFace = ((double) x) > ((double) this.width) * 0.25d && ((double) x) < ((double) this.width) * 0.75d && ((double) y) > ((double) this.height) * 0.1d && ((double) y) < ((double) this.height) * 0.5d;
                        if (possiblyFace) {
                            isFaceRegion[y][x] = true;
                            faceTop = Math.min(faceTop, y);
                            faceBottom = Math.max(faceBottom, y);
                            faceLeft = Math.min(faceLeft, x);
                            faceRight = Math.max(faceRight, x);
                        }
                    }
                }
            }
        }
        int faceTop2 = Math.max(0, faceTop - 5);
        int faceBottom2 = Math.min(this.height - 1, faceBottom + 5);
        int faceLeft2 = Math.max(0, faceLeft - 5);
        int faceRight2 = Math.min(this.width - 1, faceRight + 5);
        ColorChar[][] result = new ColorChar[this.height][this.width];
        int y2 = 0;
        while (y2 < this.height) {
            int x2 = 0;
            while (x2 < this.width) {
                int pixel2 = enhanced.getRGB(x2, y2);
                int alpha2 = (pixel2 >> 24) & 255;
                boolean isInFaceRegion = y2 >= faceTop2 && y2 <= faceBottom2 && x2 >= faceLeft2 && x2 <= faceRight2;
                if (alpha2 < 30) {
                    result[y2][x2] = new ColorChar(' ', 0);
                } else {
                    int red = (pixel2 >> 16) & 255;
                    int green = (pixel2 >> 8) & 255;
                    int blue = pixel2 & 255;
                    if (red == 0 && green == 0 && blue == 0) {
                        result[y2][x2] = new ColorChar(' ', 0);
                    } else {
                        double brightness = (0.299d * ((double) red)) + (0.587d * ((double) green)) + (0.114d * ((double) blue));
                        boolean isPurple = isPurple(red, green, blue);
                        boolean isSkin2 = isSkin(red, green, blue) || isSkinRegion[y2][x2] || (isInFaceRegion && brightness > 30.0d);
                        if (isPurple) {
                            asciiChar = BRIGHT_CHARS[Math.min(BRIGHT_CHARS.length - 1, (int) (brightness / 50.0d))];
                            red = Math.min(255, (int) (((double) red) * 1.3d));
                            green = Math.min(255, (int) (((double) green) * 0.8d));
                            blue = Math.min(255, (int) (((double) blue) * 1.6d));
                        } else if (isSkin2) {
                            int skinCharIndex = Math.min(SKIN_CHARS.length - 1, (int) ((255.0d - brightness) / 43.0d));
                            asciiChar = SKIN_CHARS[skinCharIndex];
                            if (isInFaceRegion) {
                                red = Math.min(255, red + 100);
                                green = Math.min(255, green + (100 - 15));
                                blue = Math.min(255, blue + (100 - 20));
                            } else {
                                red = Math.min(255, red + 80);
                                green = Math.min(255, green + (80 - 10));
                                blue = Math.min(255, blue + (80 - 15));
                            }
                        } else if (brightness > 200.0d) {
                            asciiChar = BRIGHT_CHARS[0];
                            red = Math.min(255, (int) (((double) red) * 1.2d));
                            green = Math.min(255, (int) (((double) green) * 1.2d));
                            blue = Math.min(255, (int) (((double) blue) * 1.2d));
                        } else if (brightness > 150.0d) {
                            asciiChar = BRIGHT_CHARS[Math.min(BRIGHT_CHARS.length - 1, (int) ((255.0d - brightness) / 17.0d))];
                        } else if (brightness > 100.0d) {
                            asciiChar = MEDIUM_CHARS[Math.min(MEDIUM_CHARS.length - 1, (int) ((150.0d - brightness) / 8.0d))];
                        } else if (brightness > 50.0d) {
                            asciiChar = DARK_CHARS[Math.min(DARK_CHARS.length - 1, (int) ((100.0d - brightness) / 8.0d))];
                        } else {
                            asciiChar = DARK_CHARS[DARK_CHARS.length - 1];
                            red = Math.min(255, red + 40);
                            green = Math.min(255, green + 30);
                            blue = Math.min(255, blue + 50);
                        }
                        if (this.useOriginalColors) {
                            finalColor = (-16777216) | (red << 16) | (green << 8) | blue;
                        } else {
                            Color themeColor = new Color(Opcode.I2B, 61, 226);
                            float brightnessFactor = (float) (brightness / 255.0d);
                            int themeRed = Math.min(255, (int) (((double) (themeColor.getRed() * brightnessFactor)) * 1.2d));
                            int themeGreen = Math.min(255, (int) (((double) (themeColor.getGreen() * brightnessFactor)) * 1.2d));
                            int themeBlue = Math.min(255, (int) (((double) (themeColor.getBlue() * brightnessFactor)) * 1.2d));
                            finalColor = (-16777216) | (themeRed << 16) | (themeGreen << 8) | themeBlue;
                        }
                        result[y2][x2] = new ColorChar(asciiChar, finalColor);
                    }
                }
                x2++;
            }
            y2++;
        }
        return result;
    }

    private BufferedImage enhanceImage(BufferedImage original) {
        int red;
        int green;
        int blue;
        int red2;
        int green2;
        int blue2;
        BufferedImage enhanced = new BufferedImage(original.getWidth(), original.getHeight(), 2);
        for (int y = 0; y < original.getHeight(); y++) {
            for (int x = 0; x < original.getWidth(); x++) {
                int rgb = original.getRGB(x, y);
                int alpha = (rgb >> 24) & 255;
                if (alpha < 15) {
                    enhanced.setRGB(x, y, rgb);
                } else {
                    int red3 = (rgb >> 16) & 255;
                    int green3 = (rgb >> 8) & 255;
                    int blue3 = rgb & 255;
                    boolean isSkinTone = isSkin(red3, green3, blue3);
                    boolean isPurple = isPurple(red3, green3, blue3);
                    if (isSkinTone) {
                        red = Math.min(255, Math.max(0, (int) (red3 * 1.8f)));
                        green = Math.min(255, Math.max(0, (int) (green3 * 1.8f)));
                        blue = Math.min(255, Math.max(0, (int) (blue3 * 1.8f)));
                    } else if (isPurple) {
                        red = Math.min(255, Math.max(0, (int) (red3 * 1.6f)));
                        green = Math.min(255, Math.max(0, (int) (green3 * 0.9f)));
                        blue = Math.min(255, Math.max(0, (int) (blue3 * 1.6f)));
                    } else {
                        red = Math.min(255, Math.max(0, (int) (red3 * 1.5f)));
                        green = Math.min(255, Math.max(0, (int) (green3 * 1.5f)));
                        blue = Math.min(255, Math.max(0, (int) (blue3 * 1.5f)));
                    }
                    if (isSkinTone) {
                        red2 = applyContrast(red, 0.90000004f);
                        green2 = applyContrast(green, 0.90000004f);
                        blue2 = applyContrast(blue, 0.90000004f);
                    } else if (isPurple) {
                        red2 = applyContrast(red, 1.8000001f);
                        green2 = applyContrast(green, 1.2f);
                        blue2 = applyContrast(blue, 1.8000001f);
                    } else {
                        red2 = applyContrast(red, 1.5f);
                        green2 = applyContrast(green, 1.5f);
                        blue2 = applyContrast(blue, 1.5f);
                    }
                    if (isSkinTone) {
                        red2 = Math.min(255, red2 + 60);
                        green2 = Math.min(255, green2 + 50);
                        blue2 = Math.min(255, blue2 + 45);
                        alpha = Math.max(alpha, 240);
                    }
                    if (red2 > 200 && green2 > 200 && blue2 > 200) {
                        red2 = Math.min(255, red2 + 40);
                        green2 = Math.min(255, green2 + 40);
                        blue2 = Math.min(255, blue2 + 40);
                        alpha = 255;
                    }
                    if (red2 < 40 && green2 < 40 && blue2 < 40 && (red2 > 0 || green2 > 0 || blue2 > 0)) {
                        red2 = Math.max(45, red2);
                        green2 = Math.max(45, green2);
                        blue2 = Math.max(45, blue2);
                        alpha = Math.max(alpha, 200);
                    }
                    int enhancedPixel = (alpha << 24) | (red2 << 16) | (green2 << 8) | blue2;
                    enhanced.setRGB(x, y, enhancedPixel);
                }
            }
        }
        return enhanced;
    }

    private int applyContrast(int value, float factor) {
        float result = (factor * (value - 128)) + 128.0f;
        return Math.min(255, Math.max(0, (int) result));
    }

    private boolean isPurple(int r, int g, int b) {
        boolean redBlueBalance = r > g + 30 && b > g + 30;
        boolean purpleHue = r >= 80 && r <= 200 && g <= 100 && b >= 100;
        boolean darkPurple = r < 100 && g < 80 && b > 100;
        boolean lightPurple = r > 100 && g < 100 && b > 150;
        return redBlueBalance || purpleHue || darkPurple || lightPurple;
    }

    private boolean isSkin(int r, int g, int b) {
        boolean paleSkinRatio = r > g - 5 && g > b - 5;
        boolean veryPaleSkin = r > 180 && g > 170 && b > 160;
        boolean paleSkin = r > 160 && g > 140 && b > 120;
        boolean lightSkin = r > 140 && g > 120 && b > 100;
        boolean almostWhiteSkin = r > 220 && g > 220 && b > 210;
        boolean lightGraySkin = Math.abs(r - g) < 15 && Math.abs(g - b) < 15 && Math.abs(r - b) < 15 && r > 160;
        return (paleSkinRatio && (veryPaleSkin || paleSkin || lightSkin || almostWhiteSkin)) || lightGraySkin;
    }

    public void updatePosition(int screenWidth, int screenHeight) {
        this.x = screenWidth - TokenId.ABSTRACT;
        this.y = (screenHeight / 2) - Opcode.DDIV;
    }

    public void reloadFrames() {
        if (this.lastLoadedResource != null) {
            this.frames.clear();
            this.rawFrames.clear();
            loadGifFromResource(this.lastLoadedResource);
        }
    }

    public void render(class_332 context, int mouseX, int mouseY, float delta, float bassLevel, float trebleLevel) {
        this.animationTime += delta * 0.01f;
        long currentTime = System.currentTimeMillis();
        if (currentTime - this.lastFrameTime > this.frameDelay && this.frameCount > 0) {
            this.currentFrame = (this.currentFrame + 1) % this.frameCount;
            this.lastFrameTime = currentTime;
        }
        if (this.errorMessage != null) {
            context.method_51433(class_310.method_1551().field_1772, this.errorMessage, this.x, this.y, Color.RED.hashCode(), false);
            return;
        }
        if (this.frames.isEmpty() || this.currentFrame >= this.frames.size()) {
            return;
        }
        ColorChar[][] currentFrameData = this.frames.get(this.currentFrame);
        context.method_51448().method_22903();
        context.method_51448().method_22905(this.textScale, this.textScale, 1.0f);
        float scaledX = this.x / this.textScale;
        float scaledY = this.y / this.textScale;
        int charWidth = class_310.method_1551().field_1772.method_1727("X");
        for (int i = 0; i < currentFrameData.length; i++) {
            for (int j = 0; j < currentFrameData[i].length; j++) {
                ColorChar colorChar = currentFrameData[i][j];
                if (colorChar.character != ' ') {
                    float posX = scaledX + (j * charWidth);
                    float posY = scaledY + (i * 8);
                    context.method_51433(class_310.method_1551().field_1772, String.valueOf(colorChar.character), (int) posX, (int) posY, colorChar.colorValue, false);
                }
            }
        }
        context.method_51448().method_22909();
    }

    public void render(class_332 context, int mouseX, int mouseY, float delta) {
        render(context, mouseX, mouseY, delta, 0.0f, 0.0f);
    }

    public void toggleColorMode() {
        this.useOriginalColors = !this.useOriginalColors;
        reloadFrames();
    }
}
