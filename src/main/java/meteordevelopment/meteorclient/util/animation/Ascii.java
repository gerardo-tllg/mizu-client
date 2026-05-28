package meteordevelopment.meteorclient.util.animation;



import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.minecraft.resource.Resource;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Optional;

public class Ascii {
    private static class ColorChar {
        char character;
        int colorValue;

        ColorChar(char character, int colorValue) {
            this.character = character;
            this.colorValue = colorValue;
        }
    }

    private final List<ColorChar[][]> frames = new ArrayList<>();
    private final List<BufferedImage> rawFrames = new ArrayList<>();
    private int currentFrame = 0;
    private int frameCount = 0;
    private boolean isLoaded = false;
    private String errorMessage = null;

    private long lastFrameTime = 0;
    private final long frameDelay;

    private int x;
    private int y;
    private int width;
    private int height;

    private float textScale = 0.65f;

    private float animationTime = 0.0f;
    private final Random random = new Random();

    private boolean useOriginalColors = true;

    private String lastLoadedResource;

    private static final char[] BRIGHT_CHARS = {'@', '#', '&', '$', 'X', 'L'};
    private static final char[] MEDIUM_CHARS = {'S', '8', 'O', 'o', '*', '+'};
    private static final char[] DARK_CHARS = {':', ';', ',', '.', '-', '='};
    private static final char[] SKIN_CHARS = {'@', '#', '$', '&', '%', 'O'};

    public Ascii(int screenWidth, int screenHeight, String resourcePath, int frameDelayMs) {
        this.x = screenWidth - 480;
        this.y = screenHeight / 2 - 200;
        this.frameDelay = frameDelayMs;
        this.lastLoadedResource = resourcePath;

        loadGifFromResource(resourcePath);
    }

    private void loadGifFromResource(String resourcePath) {
        this.lastLoadedResource = resourcePath;
        try {
            Identifier id = Identifier.of("meteor-client", resourcePath);
            Optional<Resource> resourceOptional = MinecraftClient.getInstance().getResourceManager().getResource(id);

            if (resourceOptional.isPresent()) {
                Resource resource = resourceOptional.get();
                InputStream inputStream = resource.getInputStream();

                ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
                ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream);
                reader.setInput(imageInputStream);

                int numFrames = reader.getNumImages(true);
                this.width = 70;
                this.height = 75;

                frames.clear();
                rawFrames.clear();

                for (int i = 0; i < numFrames; i++) {
                    BufferedImage frame = reader.read(i);
                    rawFrames.add(frame);
                    frames.add(convertToAscii(frame));
                }

                imageInputStream.close();
                inputStream.close();
                reader.dispose();

                frameCount = frames.size();
                isLoaded = true;
            } else {
                errorMessage = "Resource not found: " + resourcePath;
            }
        } catch (Exception e) {
            errorMessage = "Error loading GIF: " + e.getMessage();
            e.printStackTrace();
        }
    }

    private ColorChar[][] convertToAscii(BufferedImage image) {
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(image, 0, 0, width, height, null);
        g.dispose();

        BufferedImage enhanced = enhanceImage(resized);

        boolean[][] isFaceRegion = new boolean[height][width];
        boolean[][] isSkinRegion = new boolean[height][width];
        int faceTop = height;
        int faceBottom = 0;
        int faceLeft = width;
        int faceRight = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = enhanced.getRGB(x, y);
                int alpha = (pixel >> 24) & 0xff;

                if (alpha < 50) continue;

                int red = (pixel >> 16) & 0xff;
                int green = (pixel >> 8) & 0xff;
                int blue = pixel & 0xff;

                boolean isSkin = isSkin(red, green, blue);
                if (isSkin) {
                    isSkinRegion[y][x] = true;

                    boolean possiblyFace =
                            x > width * 0.25 && x < width * 0.75 &&
                                    y > height * 0.1 && y < height * 0.5;

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

        faceTop = Math.max(0, faceTop - 5);
        faceBottom = Math.min(height - 1, faceBottom + 5);
        faceLeft = Math.max(0, faceLeft - 5);
        faceRight = Math.min(width - 1, faceRight + 5);

        ColorChar[][] result = new ColorChar[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = enhanced.getRGB(x, y);
                int alpha = (pixel >> 24) & 0xff;

                boolean isInFaceRegion = y >= faceTop && y <= faceBottom &&
                        x >= faceLeft && x <= faceRight;

                if (alpha < 30) {
                    result[y][x] = new ColorChar(' ', 0);
                    continue;
                }

                int red = (pixel >> 16) & 0xff;
                int green = (pixel >> 8) & 0xff;
                int blue = pixel & 0xff;

                if (red == 0 && green == 0 && blue == 0) {
                    result[y][x] = new ColorChar(' ', 0);
                    continue;
                }

                double brightness = 0.299 * red + 0.587 * green + 0.114 * blue;

                char asciiChar;

                boolean isPurple = isPurple(red, green, blue);
                boolean isSkin = isSkin(red, green, blue) || isSkinRegion[y][x] || (isInFaceRegion && brightness > 30);

                if (isPurple) {
                    asciiChar = BRIGHT_CHARS[Math.min(BRIGHT_CHARS.length - 1, (int)(brightness / 50))];

                    red = Math.min(255, (int)(red * 1.3));
                    green = Math.min(255, (int)(green * 0.8));
                    blue = Math.min(255, (int)(blue * 1.6));
                } else if (isSkin) {
                    int skinCharIndex = Math.min(SKIN_CHARS.length - 1, (int)((255 - brightness) / 43));
                    asciiChar = SKIN_CHARS[skinCharIndex];

                    if (isInFaceRegion) {
                        int facialBoost = 100;
                        red = Math.min(255, red + facialBoost);
                        green = Math.min(255, green + (facialBoost - 15));
                        blue = Math.min(255, blue + (facialBoost - 20));
                    } else {
                        int boost = 80;
                        red = Math.min(255, red + boost);
                        green = Math.min(255, green + (boost - 10));
                        blue = Math.min(255, blue + (boost - 15));
                    }
                } else if (brightness > 200) {
                    asciiChar = BRIGHT_CHARS[0];

                    red = Math.min(255, (int)(red * 1.2));
                    green = Math.min(255, (int)(green * 1.2));
                    blue = Math.min(255, (int)(blue * 1.2));
                } else if (brightness > 150) {
                    asciiChar = BRIGHT_CHARS[Math.min(BRIGHT_CHARS.length - 1, (int)((255 - brightness) / 17))];
                } else if (brightness > 100) {
                    asciiChar = MEDIUM_CHARS[Math.min(MEDIUM_CHARS.length - 1, (int)((150 - brightness) / 8))];
                } else if (brightness > 50) {
                    asciiChar = DARK_CHARS[Math.min(DARK_CHARS.length - 1, (int)((100 - brightness) / 8))];
                } else {
                    asciiChar = DARK_CHARS[DARK_CHARS.length - 1];

                    red = Math.min(255, red + 40);
                    green = Math.min(255, green + 30);
                    blue = Math.min(255, blue + 50);
                }

                int finalColor;
                if (useOriginalColors) {
                    finalColor = (255 << 24) | (red << 16) | (green << 8) | blue;
                } else {
                    Color themeColor = new Color(145, 61, 226);
                    float brightnessFactor = (float)(brightness / 255.0);
                    int themeRed = Math.min(255, (int)(themeColor.getRed() * brightnessFactor * 1.2));
                    int themeGreen = Math.min(255, (int)(themeColor.getGreen() * brightnessFactor * 1.2));
                    int themeBlue = Math.min(255, (int)(themeColor.getBlue() * brightnessFactor * 1.2));
                    finalColor = (255 << 24) | (themeRed << 16) | (themeGreen << 8) | themeBlue;
                }

                result[y][x] = new ColorChar(asciiChar, finalColor);
            }
        }

        return result;
    }

    private BufferedImage enhanceImage(BufferedImage original) {
        BufferedImage enhanced = new BufferedImage(
                original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_ARGB);

        final float contrastFactor = 1.5f;
        final float brightnessFactor = 1.5f;
        final float skinBrightnessFactor = 1.8f;
        final float purpleBrightnessFactor = 1.6f;

        for (int y = 0; y < original.getHeight(); y++) {
            for (int x = 0; x < original.getWidth(); x++) {
                int rgb = original.getRGB(x, y);
                int alpha = (rgb >> 24) & 0xFF;

                if (alpha < 15) {
                    enhanced.setRGB(x, y, rgb);
                    continue;
                }

                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;

                boolean isSkinTone = isSkin(red, green, blue);
                boolean isPurple = isPurple(red, green, blue);

                if (isSkinTone) {
                    red = Math.min(255, Math.max(0, (int)(red * skinBrightnessFactor)));
                    green = Math.min(255, Math.max(0, (int)(green * skinBrightnessFactor)));
                    blue = Math.min(255, Math.max(0, (int)(blue * skinBrightnessFactor)));
                } else if (isPurple) {
                    red = Math.min(255, Math.max(0, (int)(red * purpleBrightnessFactor)));
                    green = Math.min(255, Math.max(0, (int)(green * 0.9f)));
                    blue = Math.min(255, Math.max(0, (int)(blue * purpleBrightnessFactor)));
                } else {
                    red = Math.min(255, Math.max(0, (int)(red * brightnessFactor)));
                    green = Math.min(255, Math.max(0, (int)(green * brightnessFactor)));
                    blue = Math.min(255, Math.max(0, (int)(blue * brightnessFactor)));
                }

                if (isSkinTone) {
                    red = applyContrast(red, contrastFactor * 0.6f);
                    green = applyContrast(green, contrastFactor * 0.6f);
                    blue = applyContrast(blue, contrastFactor * 0.6f);
                } else if (isPurple) {
                    red = applyContrast(red, contrastFactor * 1.2f);
                    green = applyContrast(green, contrastFactor * 0.8f);
                    blue = applyContrast(blue, contrastFactor * 1.2f);
                } else {
                    red = applyContrast(red, contrastFactor);
                    green = applyContrast(green, contrastFactor);
                    blue = applyContrast(blue, contrastFactor);
                }

                if (isSkinTone) {
                    red = Math.min(255, red + 60);
                    green = Math.min(255, green + 50);
                    blue = Math.min(255, blue + 45);
                    alpha = Math.max(alpha, 240);
                }

                if (red > 200 && green > 200 && blue > 200) {
                    red = Math.min(255, red + 40);
                    green = Math.min(255, green + 40);
                    blue = Math.min(255, blue + 40);
                    alpha = 255;
                }

                if (red < 40 && green < 40 && blue < 40 && (red > 0 || green > 0 || blue > 0)) {
                    red = Math.max(45, red);
                    green = Math.max(45, green);
                    blue = Math.max(45, blue);
                    alpha = Math.max(alpha, 200);
                }

                int enhancedPixel = (alpha << 24) | (red << 16) | (green << 8) | blue;
                enhanced.setRGB(x, y, enhancedPixel);
            }
        }

        return enhanced;
    }

    private int applyContrast(int value, float factor) {
        float result = (factor * (value - 128)) + 128;
        return Math.min(255, Math.max(0, (int)result));
    }

    private boolean isPurple(int r, int g, int b) {

        boolean redBlueBalance = r > g + 30 && b > g + 30;
        boolean purpleHue = (r >= 80 && r <= 200) && (g <= 100) && (b >= 100);
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

        boolean lightGraySkin = Math.abs(r - g) < 15 && Math.abs(g - b) < 15 &&
                Math.abs(r - b) < 15 && r > 160;

        return (paleSkinRatio && (veryPaleSkin || paleSkin || lightSkin || almostWhiteSkin)) ||
                lightGraySkin;
    }

    public void updatePosition(int screenWidth, int screenHeight) {
        this.x = screenWidth - 300;
        this.y = screenHeight / 2 - 111;
    }

    public void reloadFrames() {
        if (lastLoadedResource != null) {
            frames.clear();
            rawFrames.clear();
            loadGifFromResource(lastLoadedResource);
        }
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta, float bassLevel, float trebleLevel) {
        animationTime += delta * 0.01f;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFrameTime > frameDelay && frameCount > 0) {
            currentFrame = (currentFrame + 1) % frameCount;
            lastFrameTime = currentTime;
        }

        if (errorMessage != null) {
            context.drawText(
                    MinecraftClient.getInstance().textRenderer,
                    errorMessage,
                    x,
                    y,
                    Color.RED.hashCode(),
                    false
            );
            return;
        }

        if (frames.isEmpty() || currentFrame >= frames.size()) return;

        ColorChar[][] currentFrameData = frames.get(currentFrame);

        context.getMatrices().push();
        context.getMatrices().scale(textScale, textScale, 1.0f);

        float scaledX = x / textScale;
        float scaledY = y / textScale;

        int charWidth = MinecraftClient.getInstance().textRenderer.getWidth("X");
        int charHeight = 8;

        for (int i = 0; i < currentFrameData.length; i++) {
            for (int j = 0; j < currentFrameData[i].length; j++) {
                ColorChar colorChar = currentFrameData[i][j];

                if (colorChar.character == ' ') continue;

                float posX = scaledX + j * charWidth;
                float posY = scaledY + i * charHeight;

                context.drawText(
                        MinecraftClient.getInstance().textRenderer,
                        String.valueOf(colorChar.character),
                        (int)posX,
                        (int)posY,
                        colorChar.colorValue,
                        false
                );
            }
        }

        context.getMatrices().pop();
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        render(context, mouseX, mouseY, delta, 0, 0);
    }

    public void toggleColorMode() {
        useOriginalColors = !useOriginalColors;
        reloadFrames();
    }
}